package dev.spud.shadowslave.citadel;

import dev.spud.shadowslave.clan.ClanData;
import dev.spud.shadowslave.clan.ClanService;
import dev.spud.shadowslave.dreamrealm.DreamCrossing;
import dev.spud.shadowslave.dreamrealm.DreamRealm;
import dev.spud.shadowslave.spell.SpellEvent;
import dev.spud.shadowslave.spell.SpellRecorder;
import dev.spud.shadowslave.spell.calling.Calling;
import dev.spud.shadowslave.spell.calling.SpellCallings;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Clan wars (DESIGN, Run I, locked with Jack 2026-10-31; numbers in
 * {@link CitadelWarSettings}). War is formal: a clan's leader declares it at
 * their own Gateway and pays for it, the Spell calls both clans, and nothing
 * can be struck until the notice runs out. After that it is a long state with
 * no time limit, and either side may:
 * <ul>
 *     <li>fight the other's members anywhere (the server's own PvP rule
 *     applies; this adds no protection);</li>
 *     <li>raid a Citadel's outskirts: stand on its land, unanswered, until
 *     the raid lands and damages upgrades, without taking it;</li>
 *     <li>strike its supply line: stand at its Link Anchor, unanswered,
 *     until it is cut, and the Gateway starves until a member mends it;</li>
 *     <li>assault the enemy's seat: announce it at least the notice ahead,
 *     the Spell calls both clans, and in its window the attackers channel the
 *     seat's Gateway while defenders stand at it to break the channel. Nobody
 *     comes, and the capture is uncontested. A failed assault puts a cooldown
 *     on the next.</li>
 * </ul>
 * A clan's seat is its first claimed Citadel and moves only in peace, with a
 * cooldown. Taking it ends the war: the winners keep it with its upgrades,
 * the losers keep their other Citadels, name a new seat and get a truce.
 * A seat whose clan nobody has logged into for days turns Abandoned, and a
 * seat at the last stage of neglect is Forsaken; either can be taken at any
 * time. A server may instead fix war hours, when seats may be assaulted
 * without announcement.
 */
public final class CitadelWars {
    static final int TICK_INTERVAL = 20;
    /** How often missing Link Anchors are raised, in ticks. */
    static final int ANCHOR_INTERVAL = 600;
    /** How long a member must be online before it counts as seen again, so the clan record is not saved every second. */
    private static final long SEEN_EVERY = 60_000L;

    /** Attacker-seconds spent raiding each Citadel's outskirts, keyed by "citadel/raiding clan"; not saved. */
    private static final Map<String, Integer> RAIDS = new HashMap<>();
    /** Attacker-seconds spent at each Link Anchor, keyed the same way; not saved. */
    private static final Map<String, Integer> STRIKES = new HashMap<>();

    private CitadelWars() {
    }

    static CitadelWarSettings settings() {
        return CitadelContentLoader.content().wars();
    }

    /** Ends a clan's wars when it dissolves or merges into another; there is no one left to fight them. */
    public static void bootstrap() {
        ClanService.addListener(new ClanService.Listener() {
            @Override
            public void dissolved(MinecraftServer server, UUID clan) {
                endAll(server, clan);
            }

            @Override
            public void merged(MinecraftServer server, UUID from, UUID into) {
                endAll(server, from);
            }
        });
    }

    private static void endAll(MinecraftServer server, UUID clan) {
        WarData data = WarData.get(server);
        for (WarData.War war : data.of(clan)) {
            data.wars.remove(war);
            ClanService.byId(server, war.enemyOf(clan)).ifPresent(enemy ->
                    ClanService.tell(server, enemy, "The clan you were at war with is no more. The war is over."));
        }
        data.setDirty();
    }

    /** The war clock: real time, plus whatever an operator has skipped. */
    public static long now(MinecraftServer server) {
        return System.currentTimeMillis() + WarData.get(server).clockOffset;
    }

    // ---- seats ---------------------------------------------------------------------------------------------------

    /**
     * A clan's seat: the Citadel it named, or if it holds that no longer, the
     * first it still holds. Empty while it holds none.
     */
    public static Optional<CitadelData.Citadel> seat(MinecraftServer server, ClanData.Clan clan) {
        ServerLevel realm = DreamRealm.level(server);
        if (realm == null) {
            return Optional.empty();
        }
        List<CitadelData.Citadel> held = CitadelData.get(realm).citadels.stream()
                .filter(citadel -> citadel.hold == CitadelData.Hold.CLAN && clan.id().equals(citadel.clan)).toList();
        Optional<UUID> named = clan.seat();
        Optional<CitadelData.Citadel> seat = held.stream().filter(citadel -> named.filter(citadel.id::equals).isPresent()).findFirst();
        if (seat.isPresent() || held.isEmpty()) {
            if (seat.isEmpty() && named.isPresent()) {
                clan.setSeat(null, clan.seatMovedAt());
                ClanData.get(server).setDirty();
            }
            return seat;
        }
        clan.setSeat(held.get(0).id, clan.seatMovedAt());
        ClanData.get(server).setDirty();
        return Optional.of(held.get(0));
    }

    public static boolean isSeat(MinecraftServer server, CitadelData.Citadel citadel) {
        return citadel.hold == CitadelData.Hold.CLAN && citadel.clan != null && ClanService.byId(server, citadel.clan)
                .flatMap(clan -> seat(server, clan)).filter(seat -> seat == citadel).isPresent();
    }

    /** A clan claimed a Citadel: if it had no seat, this is it. */
    static void claimed(MinecraftServer server, CitadelData.Citadel citadel) {
        Optional.ofNullable(citadel.clan).flatMap(id -> ClanService.byId(server, id)).ifPresent(clan -> seat(server, clan));
    }

    /** The clan's Citadel whose Gateway the player stands at, if any. */
    static Optional<CitadelData.Citadel> ownGateway(ServerPlayer player, ClanData.Clan clan) {
        if (!DreamRealm.is(player.level())) {
            return Optional.empty();
        }
        return CitadelService.near(player.serverLevel(), player.blockPosition(), CitadelLife.REACH)
                .filter(citadel -> citadel.hold == CitadelData.Hold.CLAN && clan.id().equals(citadel.clan));
    }

    /** Makes the Citadel whose Gateway the leader stands at the clan's seat, in peace and once the cooldown allows. */
    public static String moveSeat(ServerPlayer player) {
        MinecraftServer server = player.server;
        Optional<ClanData.Clan> found = ClanService.clanOf(server, player.getUUID());
        if (found.isEmpty()) {
            return "You belong to no clan.";
        }
        ClanData.Clan clan = found.get();
        if (!clan.leader().equals(player.getUUID())) {
            return "Only " + clan.memberName(clan.leader()) + ", who leads the clan, can move its seat.";
        }
        Optional<CitadelData.Citadel> here = ownGateway(player, clan);
        if (here.isEmpty()) {
            return "Stand at the Gateway of one of your clan's Citadels to make it your seat.";
        }
        if (seat(server, clan).filter(here.get()::equals).isPresent()) {
            return here.get().name + " is already the seat of " + clan.name() + ".";
        }
        if (!WarData.get(server).of(clan.id()).isEmpty()) {
            return "A seat moves only in peace. " + clan.name() + " is at war.";
        }
        long now = now(server);
        long ready = clan.seatMovedAt() + CitadelWarSettings.hours(settings().seat().moveCooldownDays() * 24);
        if (clan.seatMovedAt() > 0 && now < ready) {
            return "The seat moved lately. It can move again in " + span(ready - now) + ".";
        }
        clan.setSeat(here.get().id, now);
        ClanData.get(server).setDirty();
        ClanService.tell(server, clan, here.get().name + " is now the seat of " + clan.name() + ".");
        return "";
    }

    // ---- declaring war -------------------------------------------------------------------------------------------

    /** A leader declares war on another clan at their own Gateway, paying its cost. Returns why not, or empty. */
    public static String declare(ServerPlayer player, String targetName) {
        MinecraftServer server = player.server;
        CitadelWarSettings settings = settings();
        if (!settings.enabled()) {
            return "War between clans is not allowed on this server.";
        }
        Optional<ClanData.Clan> found = ClanService.clanOf(server, player.getUUID());
        if (found.isEmpty()) {
            return "You belong to no clan.";
        }
        ClanData.Clan clan = found.get();
        if (!clan.leader().equals(player.getUUID())) {
            return "Only " + clan.memberName(clan.leader()) + ", who leads the clan, can declare war.";
        }
        if (ownGateway(player, clan).isEmpty()) {
            return "War is declared at the Gateway of one of your clan's Citadels.";
        }
        Optional<ClanData.Clan> target = ClanData.get(server).named(targetName);
        if (target.isEmpty()) {
            return "No clan is called " + targetName.trim() + ".";
        }
        ClanData.Clan enemy = target.get();
        if (enemy.id().equals(clan.id())) {
            return "A clan cannot go to war with itself.";
        }
        if (seat(server, enemy).isEmpty()) {
            return enemy.name() + " holds no Citadel, so it has no seat to take.";
        }
        WarData data = WarData.get(server);
        if (data.between(clan.id(), enemy.id()).isPresent()) {
            return clan.name() + " is already at war with " + enemy.name() + ".";
        }
        long now = now(server);
        Optional<WarData.Truce> truce = data.truce(clan.id(), enemy.id(), now);
        if (truce.isPresent()) {
            return "A truce holds between " + clan.name() + " and " + enemy.name() + " for " + span(truce.get().until() - now) + ".";
        }
        if (!CitadelLife.pay(player, settings.declare().cost(), 1.0)) {
            return "Declaring war costs " + describe(settings.declare().cost()) + ". You do not carry it.";
        }
        long notice = CitadelWarSettings.hours(settings.declare().noticeHours());
        WarData.War war = new WarData.War(UUID.randomUUID(), clan.id(), enemy.id(), now, now + notice);
        data.wars.add(war);
        data.setDirty();
        seat(server, clan);
        SpellRecorder.record(server, SpellEvent.Kind.WAR_DECLARED, Optional.of(player), enemy.name(), clan.name());
        ServerLevel level = player.serverLevel();
        level.playSound(null, player.blockPosition(), SoundEvents.RAID_HORN.value(), SoundSource.PLAYERS, 2.0F, 0.8F);
        String when = notice <= 0 ? "now" : "in " + span(notice);
        call(server, clan, "war_declared", enemy.name(), seatName(server, enemy),
                clan.name() + " has declared war on " + enemy.name() + ". It begins " + when + ".");
        call(server, enemy, "war_declared_on", clan.name(), seatName(server, enemy),
                clan.name() + " has declared war on " + enemy.name() + ". It begins " + when + ". Its seat, "
                        + seatName(server, enemy) + ", is what they will come for.");
        if (notice <= 0) {
            begin(server, war);
        }
        return "";
    }

    private static void begin(MinecraftServer server, WarData.War war) {
        war.begun = true;
        WarData.get(server).setDirty();
        for (UUID side : List.of(war.attacker, war.defender)) {
            Optional<ClanData.Clan> clan = ClanService.byId(server, side);
            Optional<ClanData.Clan> enemy = ClanService.byId(server, war.enemyOf(side));
            if (clan.isPresent() && enemy.isPresent()) {
                call(server, clan.get(), "war_begins", enemy.get().name(), seatName(server, enemy.get()),
                        "The notice is over. " + clan.get().name() + " and " + enemy.get().name() + " are at war.");
            }
        }
    }

    // ---- assaults on a seat --------------------------------------------------------------------------------------

    /**
     * A leader announces an assault on the enemy's seat {@code hours} from now.
     * Returns why not, or empty.
     */
    public static String announce(ServerPlayer player, String enemyName, double hours) {
        MinecraftServer server = player.server;
        CitadelWarSettings settings = settings();
        Optional<ClanData.Clan> found = ClanService.clanOf(server, player.getUUID());
        if (found.isEmpty()) {
            return "You belong to no clan.";
        }
        ClanData.Clan clan = found.get();
        if (!clan.leader().equals(player.getUUID())) {
            return "Only " + clan.memberName(clan.leader()) + ", who leads the clan, can announce an assault.";
        }
        Optional<ClanData.Clan> enemy = ClanData.get(server).named(enemyName);
        WarData data = WarData.get(server);
        Optional<WarData.War> war = enemy.flatMap(other -> data.between(clan.id(), other.id()));
        if (enemy.isEmpty() || war.isEmpty()) {
            return clan.name() + " is not at war with " + enemyName.trim() + ".";
        }
        long now = now(server);
        if (!war.get().active(now)) {
            return "The war's notice runs for " + span(war.get().activeAt - now) + " more.";
        }
        if (settings.warHours().enabled()) {
            return "On this server seats are assaulted in the war hours, " + hoursText() + ", with no announcement.";
        }
        if (war.get().assault(clan.id()).isPresent()) {
            return clan.name() + " has already announced an assault.";
        }
        if (now < war.get().cooldownUntil(clan.id())) {
            return "After a failed assault, " + clan.name() + " may announce another in " + span(war.get().cooldownUntil(clan.id()) - now) + ".";
        }
        Optional<CitadelData.Citadel> seat = seat(server, enemy.get());
        if (seat.isEmpty()) {
            return enemy.get().name() + " holds no seat.";
        }
        boolean open = openSeason(server, enemy.get(), seat.get(), now).isPresent();
        double least = open ? 0 : settings.assault().noticeHours();
        if (hours < least) {
            return "An assault is announced at least " + trim(least) + " hours ahead.";
        }
        if (hours > settings.assault().maxAheadDays() * 24) {
            return "An assault is announced at most " + trim(settings.assault().maxAheadDays()) + " days ahead.";
        }
        long at = now + CitadelWarSettings.hours(hours);
        war.get().assaults.put(clan.id(), new WarData.Assault(clan.id(), seat.get().id, at));
        data.setDirty();
        String when = at <= now ? "now" : "in " + span(at - now);
        call(server, clan, "war_assault_announced", enemy.get().name(), seat.get().name,
                clan.name() + " will assault " + seat.get().name + ", the seat of " + enemy.get().name() + ", " + when
                        + ". Channel its Gateway for " + settings.assault().channelSeconds() + " seconds.");
        call(server, enemy.get(), "war_assault_on_seat", clan.name(), seat.get().name,
                clan.name() + " will assault " + seat.get().name + ", your seat, " + when + ". Stand at its Gateway to break their channel.");
        return "";
    }

    /** Why a seat may be taken with no notice: it is Abandoned or Forsaken. Empty if neither. */
    static Optional<String> openSeason(MinecraftServer server, ClanData.Clan clan, CitadelData.Citadel seat, long now) {
        if (abandoned(server, clan, now)) {
            return Optional.of("Abandoned");
        }
        if (settings().abandon().forsakenOpen() && CitadelLife.stage(seat) == Neglect.FORSAKEN) {
            return Optional.of("Forsaken");
        }
        return Optional.empty();
    }

    /** No member of the clan is online, and none has been for the abandon time. */
    public static boolean abandoned(MinecraftServer server, ClanData.Clan clan, long now) {
        return !anyOnline(server, clan) && clan.lastSeen() > 0
                && now - clan.lastSeen() >= CitadelWarSettings.hours(settings().abandon().abandonedHours());
    }

    private static boolean anyOnline(MinecraftServer server, ClanData.Clan clan) {
        return clan.members().stream().anyMatch(member -> server.getPlayerList().getPlayer(member) != null);
    }

    /** Whether {@code by} may channel at its enemy's seat now: an open season, the war hours, or its announced window. */
    public static boolean assaultOpen(MinecraftServer server, WarData.War war, UUID by, long now) {
        if (!war.active(now)) {
            return false;
        }
        Optional<ClanData.Clan> enemy = ClanService.byId(server, war.enemyOf(by));
        Optional<CitadelData.Citadel> seat = enemy.flatMap(clan -> seat(server, clan));
        if (seat.isEmpty()) {
            return false;
        }
        if (openSeason(server, enemy.get(), seat.get(), now).isPresent()) {
            return true;
        }
        CitadelWarSettings settings = settings();
        if (settings.warHours().enabled()) {
            return settings.warHours().contains(now);
        }
        return war.assault(by).filter(assault -> now >= assault.at
                && now < assault.at + settings.assault().windowMinutes() * 60_000L).isPresent();
    }

    // ---- the war, once a second ----------------------------------------------------------------------------------

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % TICK_INTERVAL != 0) {
            return;
        }
        tick(server, server.getTickCount() % ANCHOR_INTERVAL == 0);
    }

    static void tick(MinecraftServer server, boolean anchors) {
        ServerLevel realm = DreamRealm.level(server);
        if (realm == null) {
            return;
        }
        long now = now(server);
        WarData data = WarData.get(server);
        noteOnline(server, data, now);
        if (data.truces.removeIf(truce -> truce.until() <= now)) {
            data.setDirty();
        }
        if (anchors) {
            for (CitadelData.Citadel citadel : CitadelData.get(realm).citadels) {
                if (citadel.hold == CitadelData.Hold.CLAN && citadel.tier(CitadelLife.LINKS) > 0) {
                    raiseAnchor(realm, citadel);
                }
            }
        }
        if (!settings().enabled()) {
            return;
        }
        for (WarData.War war : List.copyOf(data.wars)) {
            Optional<ClanData.Clan> attacker = ClanService.byId(server, war.attacker);
            Optional<ClanData.Clan> defender = ClanService.byId(server, war.defender);
            if (attacker.isEmpty() || defender.isEmpty()) {
                data.wars.remove(war);
                data.setDirty();
                continue;
            }
            if (!war.active(now)) {
                continue;
            }
            if (!war.begun) {
                begin(server, war);
            }
            for (ClanData.Clan side : List.of(attacker.get(), defender.get())) {
                ClanData.Clan enemy = side == attacker.get() ? defender.get() : attacker.get();
                watchAbandon(server, war, side, enemy, now);
                if (sideTick(server, realm, data, war, side, enemy, now)) {
                    break;
                }
            }
        }
    }

    /** Members online keep their clan seen, and lift any warning that its seat was being abandoned. */
    private static void noteOnline(MinecraftServer server, WarData data, long now) {
        ClanData clans = ClanData.get(server);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            clans.of(player.getUUID()).ifPresent(clan -> {
                if (now - clan.lastSeen() >= SEEN_EVERY || clan.lastSeen() > now) {
                    clan.seen(now);
                    clans.setDirty();
                }
                for (WarData.War war : data.of(clan.id())) {
                    if (war.warned.remove(clan.id()) != null) {
                        data.setDirty();
                    }
                }
            });
        }
    }

    /** The Spell warns both clans when a side's seat nears Abandoned, and again when it is. */
    private static void watchAbandon(MinecraftServer server, WarData.War war, ClanData.Clan side, ClanData.Clan enemy, long now) {
        if (anyOnline(server, side)) {
            return;
        }
        if (side.lastSeen() <= 0) {
            side.seen(now);
            ClanData.get(server).setDirty();
            return;
        }
        CitadelWarSettings.Abandon abandon = settings().abandon();
        long away = now - side.lastSeen();
        int stage = war.warned.getOrDefault(side.id(), 0);
        String seat = seatName(server, side);
        if (stage < 2 && away >= CitadelWarSettings.hours(abandon.abandonedHours())) {
            war.warned.put(side.id(), 2);
            call(server, enemy, "war_seat_abandoned", side.name(), seat,
                    "No one of " + side.name() + " has come for " + span(away) + ". " + seat + " is Abandoned and can be taken at any time.");
        } else if (stage < 1 && away >= CitadelWarSettings.hours(abandon.warnHours())) {
            war.warned.put(side.id(), 1);
            long left = CitadelWarSettings.hours(abandon.abandonedHours()) - away;
            call(server, enemy, "war_abandon_countdown", side.name(), seat,
                    "No one of " + side.name() + " has come for " + span(away) + ". If none comes in " + span(left) + ", "
                            + seat + " turns Abandoned and can be taken at any time.");
        } else {
            return;
        }
        WarData.get(server).setDirty();
    }

    /** One side's second of war: its assault's window, its channel, its raids and strikes. Returns whether the war ended. */
    private static boolean sideTick(MinecraftServer server, ServerLevel realm, WarData data, WarData.War war, ClanData.Clan side,
                                    ClanData.Clan enemy, long now) {
        CitadelWarSettings settings = settings();
        Optional<WarData.Assault> assault = war.assault(side.id());
        if (assault.isPresent()) {
            long ends = assault.get().at + settings.assault().windowMinutes() * 60_000L;
            if (!assault.get().begun && now >= assault.get().at) {
                assault.get().begun = true;
                data.setDirty();
                String seat = seatName(server, enemy);
                call(server, side, "war_assault_begins", enemy.name(), seat,
                        "The assault on " + seat + " begins. Channel its Gateway: you have " + span(ends - now) + ".");
                call(server, enemy, "war_assault_begins_defend", side.name(), seat,
                        side.name() + " are assaulting " + seat + ". Stand at its Gateway to break their channel.");
            }
            if (now >= ends) {
                war.assaults.remove(side.id());
                war.channel.remove(side.id());
                war.cooldownUntil.put(side.id(), now + CitadelWarSettings.hours(settings.assault().cooldownHours()));
                data.setDirty();
                String seat = seatName(server, enemy);
                ClanService.tell(server, side, "The assault on " + seat + " has failed. You may announce another in "
                        + span(CitadelWarSettings.hours(settings.assault().cooldownHours())) + ".");
                ClanService.tell(server, enemy, seat + " held against the assault of " + side.name() + ".");
            }
        }
        if (assaultOpen(server, war, side.id(), now)) {
            Optional<CitadelData.Citadel> seat = seat(server, enemy);
            if (seat.isPresent() && channel(server, realm, war, side, enemy, seat.get(), now)) {
                return true;
            }
        } else if (war.channel.remove(side.id()) != null) {
            // Out of the window the channel fades.
            data.setDirty();
        }
        Optional<CitadelData.Citadel> besieged = assaultOpen(server, war, side.id(), now) ? seat(server, enemy) : Optional.empty();
        for (CitadelData.Citadel citadel : List.copyOf(CitadelData.get(realm).citadels)) {
            if (citadel.hold != CitadelData.Hold.CLAN || !enemy.id().equals(citadel.clan)) {
                continue;
            }
            if (besieged.filter(citadel::equals).isEmpty()) {
                // A seat under assault is channelled, not raided.
                raid(server, realm, data, side, enemy, citadel, now);
            }
            strike(server, realm, side, enemy, citadel);
        }
        return false;
    }

    /** Members of {@code clan} in the Dream Realm within {@code radius} of {@code at}. */
    static List<ServerPlayer> present(ServerLevel realm, ClanData.Clan clan, BlockPos at, double radius) {
        List<ServerPlayer> found = new ArrayList<>();
        for (ServerPlayer player : realm.players()) {
            if (!player.isSpectator() && player.isAlive() && clan.has(player.getUUID())
                    && player.blockPosition().distSqr(at) <= radius * radius) {
                found.add(player);
            }
        }
        return found;
    }

    /**
     * The attackers channel the seat's Gateway: each one standing at it adds
     * a second, defenders standing there alone break it down, and both there
     * holds it. Returns whether it was taken.
     */
    static boolean channel(MinecraftServer server, ServerLevel realm, WarData.War war, ClanData.Clan side, ClanData.Clan enemy,
                           CitadelData.Citadel seat, long now) {
        CitadelWarSettings.Assault settings = settings().assault();
        List<ServerPlayer> attackers = present(realm, side, seat.gateway, settings.channelRadius());
        List<ServerPlayer> defenders = present(realm, enemy, seat.gateway, settings.channelRadius());
        int before = war.channel(side.id());
        int progress = before;
        // Planted Standards lean on the channel: the attackers' speed it, the defenders' slow it.
        int lift = WarNatures.standards(realm, side, seat.gateway);
        int drag = WarNatures.standards(realm, enemy, seat.gateway);
        if (lift + drag > 0) {
            WarNatures.showStandards(realm, seat.gateway);
        }
        String line;
        if (!attackers.isEmpty() && defenders.isEmpty()) {
            progress += Math.max(0, attackers.size() + lift - drag);
            line = "Channelling the Gateway of " + seat.name + ": " + Math.min(progress, settings.channelSeconds()) + " / " + settings.channelSeconds();
            realm.sendParticles(ParticleTypes.REVERSE_PORTAL, seat.gateway.getX() + 0.5, seat.gateway.getY() + 1.0,
                    seat.gateway.getZ() + 0.5, 20 + attackers.size() * 10, 0.6, 1.0, 0.6, 0.15);
            if (before == 0) {
                ClanService.tell(server, enemy, side.name() + " are channelling the Gateway of " + seat.name + ", your seat. Stand at it to break them.");
                realm.playSound(null, seat.gateway, SoundEvents.RAID_HORN.value(), SoundSource.HOSTILE, 2.0F, 1.0F);
            }
        } else if (!defenders.isEmpty() && attackers.isEmpty()) {
            if (progress <= 0) {
                return false;
            }
            progress = Math.max(0, progress - (defenders.size() + drag) * 2);
            line = "The defenders break the channel: " + progress + " / " + settings.channelSeconds();
        } else if (!defenders.isEmpty()) {
            line = "The Gateway of " + seat.name + " is contested: " + progress + " / " + settings.channelSeconds();
        } else {
            return false;
        }
        war.channel.put(side.id(), progress);
        Component bar = Component.literal(line).withStyle(ChatFormatting.DARK_PURPLE);
        for (ServerPlayer player : realm.getPlayers(player -> player.blockPosition().closerThan(seat.gateway, 48))) {
            player.displayClientMessage(bar, true);
        }
        if (progress >= settings.channelSeconds()) {
            capture(server, realm, war, side, enemy, seat, now);
            return true;
        }
        return false;
    }

    /** The seat falls: the winners keep it and its upgrades, the losers name a new seat, and a truce begins. */
    static void capture(MinecraftServer server, ServerLevel realm, WarData.War war, ClanData.Clan winners, ClanData.Clan losers,
                        CitadelData.Citadel seat, long now) {
        WarData data = WarData.get(server);
        data.wars.remove(war);
        data.truces.add(new WarData.Truce(winners.id(), losers.id(), now + CitadelWarSettings.hours(settings().truceDays() * 24)));
        data.setDirty();
        seat.clan = winners.id();
        seat.hold = CitadelData.Hold.CLAN;
        seat.linkCut = false;
        CitadelLife.claimed(server, seat);
        CitadelData.get(realm).setDirty();
        CitadelService.showState(realm, seat);
        showAnchor(realm, seat);
        losers.setSeat(null, 0);
        ClanData.get(server).setDirty();
        Optional<CitadelData.Citadel> next = seat(server, losers);
        // The losers may name another seat at once, whatever the cooldown.
        losers.setSeat(next.map(citadel -> citadel.id).orElse(null), 0);
        SpellRecorder.record(server, SpellEvent.Kind.SEAT_CAPTURED, Optional.empty(), seat.name, winners.name() + " over " + losers.name());
        realm.playSound(null, seat.gateway, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 1.5F, 0.8F);
        realm.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, seat.gateway.getX() + 0.5, seat.gateway.getY() + 1.0,
                seat.gateway.getZ() + 0.5, 120, 0.8, 1.2, 0.8, 0.06);
        String truce = span(CitadelWarSettings.hours(settings().truceDays() * 24));
        call(server, winners, "war_won", losers.name(), seat.name,
                winners.name() + " has taken " + seat.name + ", the seat of " + losers.name() + ". The war is won, and it is yours with all it built. A truce holds for " + truce + ".");
        call(server, losers, "war_lost", winners.name(), seat.name,
                winners.name() + " has taken " + seat.name + ". The war is lost. " + (next.isPresent()
                        ? next.get().name + " stands as your seat for now; name another at its Gateway with /clan seat."
                        : "You hold no other Citadel.") + " A truce holds for " + truce + ".");
        for (ServerPlayer player : realm.getPlayers(player -> player.blockPosition().closerThan(seat.gateway, 96))) {
            boolean won = winners.has(player.getUUID());
            DreamCrossing.title(player, Component.literal(won ? "Seat Taken" : "The Seat Falls")
                            .withStyle(won ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.DARK_RED),
                    Component.literal(seat.name + " belongs to " + winners.name()).withStyle(ChatFormatting.GRAY));
        }
    }

    // ---- raids on the outskirts ----------------------------------------------------------------------------------

    /** Raiders on a Citadel's land, with none of its clan there to answer, damage an upgrade once they have stayed long enough. */
    static void raid(MinecraftServer server, ServerLevel realm, WarData data, ClanData.Clan side, ClanData.Clan enemy,
                     CitadelData.Citadel citadel, long now) {
        CitadelWarSettings.Outskirts outskirts = settings().outskirts();
        String key = citadel.id + "/" + side.id();
        if (data.raidedUntil.getOrDefault(citadel.id, 0L) > now) {
            RAIDS.remove(key);
            return;
        }
        double land = CitadelContentLoader.settings().landRadius();
        List<ServerPlayer> raiders = present(realm, side, citadel.gateway, land);
        List<ServerPlayer> defenders = present(realm, enemy, citadel.gateway, land);
        int before = RAIDS.getOrDefault(key, 0);
        int progress;
        if (!raiders.isEmpty() && defenders.isEmpty()) {
            progress = before + raiders.size();
            if (before == 0) {
                ClanService.tell(server, enemy, "Raiders of " + side.name() + " are on the land of " + citadel.name + ". Drive them off.");
            }
            Component bar = Component.literal("Raiding the outskirts of " + citadel.name + ": "
                    + Math.min(progress, outskirts.raidSeconds()) + " / " + outskirts.raidSeconds()).withStyle(ChatFormatting.DARK_RED);
            raiders.forEach(player -> player.displayClientMessage(bar, true));
        } else if (!defenders.isEmpty()) {
            progress = 0;
            if (before > 0 && !raiders.isEmpty()) {
                Component bar = Component.literal("The defenders of " + citadel.name + " drive off the raid.").withStyle(ChatFormatting.GRAY);
                raiders.forEach(player -> player.displayClientMessage(bar, true));
            }
        } else {
            progress = Math.max(0, before - 1);
        }
        if (progress <= 0) {
            RAIDS.remove(key);
            return;
        }
        RAIDS.put(key, progress);
        if (progress < outskirts.raidSeconds()) {
            return;
        }
        RAIDS.remove(key);
        data.raidedUntil.put(citadel.id, now + CitadelWarSettings.hours(outskirts.cooldownHours()));
        data.setDirty();
        List<String> damaged = CitadelRaids.damage(realm, citadel, outskirts.damaged());
        CitadelData.get(realm).setDirty();
        WarNatures.broke(raiders);
        SpellRecorder.record(server, SpellEvent.Kind.WAR_STRIKE, Optional.empty(), citadel.name, "raid by " + side.name());
        String harm = damaged.isEmpty() ? "found nothing built to break" : "damaged its " + CitadelRaids.join(damaged);
        ClanService.tell(server, side, "Your raid on " + citadel.name + " " + harm + ".");
        ClanService.tell(server, enemy, "Raiders of " + side.name() + " " + harm.replace("its ", "the ") + " at " + citadel.name
                + (damaged.isEmpty() ? "." : ". Repair it at the Gateway."));
        realm.playSound(null, citadel.gateway, SoundEvents.WITHER_BREAK_BLOCK, SoundSource.BLOCKS, 1.0F, 0.8F);
        realm.sendParticles(ParticleTypes.LARGE_SMOKE, citadel.gateway.getX() + 0.5, citadel.gateway.getY() + 0.8,
                citadel.gateway.getZ() + 0.5, 30, 0.8, 0.6, 0.8, 0.02);
        for (ServerPlayer player : raiders) {
            DreamCrossing.title(player, Component.literal("Raid Landed").withStyle(ChatFormatting.DARK_RED),
                    Component.literal(damaged.isEmpty() ? citadel.name + " had nothing to break" : CitadelRaids.join(damaged) + " damaged")
                            .withStyle(ChatFormatting.GRAY));
        }
    }

    // ---- the supply line -----------------------------------------------------------------------------------------

    /** An enemy at the Link Anchor, unanswered, cuts it once they have stood there long enough. */
    static void strike(MinecraftServer server, ServerLevel realm, ClanData.Clan side, ClanData.Clan enemy, CitadelData.Citadel citadel) {
        if (citadel.linkAnchor == null || citadel.linkCut) {
            return;
        }
        CitadelWarSettings.Supply supply = settings().supply();
        String key = citadel.id + "/" + side.id();
        List<ServerPlayer> strikers = present(realm, side, citadel.linkAnchor, supply.strikeRadius());
        List<ServerPlayer> guards = present(realm, enemy, citadel.linkAnchor, supply.guardRadius());
        int before = STRIKES.getOrDefault(key, 0);
        int progress = !strikers.isEmpty() && guards.isEmpty() ? before + WarNatures.strikeWeight(strikers)
                : !guards.isEmpty() ? 0 : Math.max(0, before - 1);
        if (progress <= 0) {
            STRIKES.remove(key);
            return;
        }
        if (before == 0) {
            ClanService.tell(server, enemy, side.name() + " are striking at the Link Anchor of " + citadel.name + ".");
        }
        if (!strikers.isEmpty()) {
            Component bar = Component.literal("Cutting the supply line of " + citadel.name + ": "
                    + Math.min(progress, supply.strikeSeconds()) + " / " + supply.strikeSeconds()).withStyle(ChatFormatting.DARK_RED);
            strikers.forEach(player -> player.displayClientMessage(bar, true));
            realm.sendParticles(ParticleTypes.CRIT, citadel.linkAnchor.getX() + 0.5, citadel.linkAnchor.getY() + 0.8,
                    citadel.linkAnchor.getZ() + 0.5, 6, 0.3, 0.4, 0.3, 0.1);
        }
        if (progress < supply.strikeSeconds()) {
            STRIKES.put(key, progress);
            return;
        }
        STRIKES.remove(key);
        cut(realm, citadel);
        WarNatures.broke(strikers);
        SpellRecorder.record(server, SpellEvent.Kind.WAR_STRIKE, Optional.empty(), citadel.name, "supply line cut by " + side.name());
        ClanService.tell(server, side, "You cut the supply line of " + citadel.name + ". Its Gateway starves until they mend it.");
        ClanService.tell(server, enemy, side.name() + " cut the supply line of " + citadel.name + ". Its Gateway starves and its Links are dark"
                + " until a member mends the Link Anchor at " + citadel.linkAnchor.getX() + ", " + citadel.linkAnchor.getZ() + ".");
    }

    /** Cuts a Citadel's Link Anchor: its Gateway cannot be fed, eats faster, and its Links are dark. */
    public static void cut(ServerLevel realm, CitadelData.Citadel citadel) {
        citadel.linkCut = true;
        CitadelData.get(realm).setDirty();
        showAnchor(realm, citadel);
        if (citadel.linkAnchor != null) {
            realm.playSound(null, citadel.linkAnchor, SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.BLOCKS, 1.5F, 0.8F);
            realm.sendParticles(ParticleTypes.LARGE_SMOKE, citadel.linkAnchor.getX() + 0.5, citadel.linkAnchor.getY() + 1.0,
                    citadel.linkAnchor.getZ() + 0.5, 30, 0.4, 0.6, 0.4, 0.02);
        }
    }

    /** How many times faster the Gateway eats: faster while its supply line is cut. */
    static double drain(CitadelData.Citadel citadel) {
        return citadel.linkCut ? settings().supply().cutDrain() : 1.0;
    }

    /** A player uses a Link Anchor: a member mends a cut one; anyone else learns whose it is. */
    public static void useAnchor(ServerPlayer player, BlockPos pos) {
        if (!DreamRealm.is(player.level())) {
            return;
        }
        ServerLevel realm = player.serverLevel();
        Optional<CitadelData.Citadel> found = CitadelData.get(realm).citadels.stream().filter(citadel -> pos.equals(citadel.linkAnchor)).findFirst();
        if (found.isEmpty() || found.get().hold != CitadelData.Hold.CLAN || found.get().clan == null) {
            player.displayClientMessage(Component.literal("This Link Anchor holds no supply line.").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        CitadelData.Citadel citadel = found.get();
        Optional<ClanData.Clan> owner = ClanService.byId(player.server, citadel.clan);
        if (owner.isPresent() && owner.get().has(player.getUUID())) {
            if (!citadel.linkCut) {
                player.displayClientMessage(Component.literal("The supply line of " + citadel.name + " holds.").withStyle(ChatFormatting.LIGHT_PURPLE), true);
                return;
            }
            List<Cost> cost = settings().supply().repairCost();
            if (!CitadelLife.pay(player, cost, 1.0)) {
                player.displayClientMessage(Component.literal("Mending the Link Anchor costs " + describe(cost) + ".").withStyle(ChatFormatting.GRAY), true);
                return;
            }
            citadel.linkCut = false;
            CitadelData.get(realm).setDirty();
            showAnchor(realm, citadel);
            realm.playSound(null, pos, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1.0F, 1.0F);
            realm.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 30, 0.3, 0.5, 0.3, 0.04);
            ClanService.tell(player.server, owner.get(), player.getGameProfile().getName() + " mended the Link Anchor of " + citadel.name
                    + ". Its supply line holds again.");
            return;
        }
        Optional<ClanData.Clan> mine = ClanService.clanOf(player.server, player.getUUID());
        boolean enemy = mine.isPresent() && atWar(player.server, mine.get().id(), citadel.clan);
        String line = "The Link Anchor of " + citadel.name + ", held by " + owner.map(ClanData.Clan::name).orElse("a clan") + "."
                + (enemy && !citadel.linkCut ? " Stand at it " + settings().supply().strikeSeconds() + " seconds, with none of them near, to cut it." : "");
        player.displayClientMessage(Component.literal(line).withStyle(enemy ? ChatFormatting.DARK_RED : ChatFormatting.GRAY), true);
    }

    /** Raises the Citadel's Link Anchor outside its north wall, or puts it back if its block is gone. */
    static void raiseAnchor(ServerLevel realm, CitadelData.Citadel citadel) {
        if (citadel.linkAnchor != null) {
            if (realm.isLoaded(citadel.linkAnchor) && !realm.getBlockState(citadel.linkAnchor).is(CitadelRegistry.LINK_ANCHOR.get())
                    && realm.getBlockState(citadel.linkAnchor).canBeReplaced()) {
                showAnchor(realm, citadel);
            }
            return;
        }
        int x = citadel.gateway.getX();
        int z = citadel.gateway.getZ() - settings().supply().anchorDistance();
        if (!realm.hasChunk(x >> 4, z >> 4)) {
            return;
        }
        int y = realm.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        citadel.linkAnchor = new BlockPos(x, y, z);
        CitadelData.get(realm).setDirty();
        showAnchor(realm, citadel);
        realm.sendParticles(ParticleTypes.REVERSE_PORTAL, x + 0.5, y + 1.0, z + 0.5, 40, 0.4, 0.8, 0.4, 0.1);
    }

    /** Sets the Link Anchor block to show whether its line is cut, if its chunk is loaded. */
    static void showAnchor(ServerLevel realm, CitadelData.Citadel citadel) {
        if (citadel.linkAnchor == null || !realm.isLoaded(citadel.linkAnchor)) {
            return;
        }
        BlockState want = CitadelRegistry.LINK_ANCHOR.get().defaultBlockState().setValue(LinkAnchorBlock.CUT, citadel.linkCut);
        if (!realm.getBlockState(citadel.linkAnchor).equals(want)) {
            realm.setBlock(citadel.linkAnchor, want, Block.UPDATE_ALL);
        }
    }

    // ---- reading the war -----------------------------------------------------------------------------------------

    public static boolean atWar(MinecraftServer server, UUID a, UUID b) {
        long now = now(server);
        return WarData.get(server).between(a, b).filter(war -> war.active(now)).isPresent();
    }

    /** What the Gateway screen says of the war, for a Citadel's clan: a few short lines, most pressing first. */
    public static List<String> lines(MinecraftServer server, CitadelData.Citadel citadel) {
        List<String> lines = new ArrayList<>();
        Optional<ClanData.Clan> clan = Optional.ofNullable(citadel.clan).flatMap(id -> ClanService.byId(server, id));
        if (clan.isEmpty()) {
            return lines;
        }
        if (citadel.linkCut) {
            lines.add("Supply line cut: the Gateway starves and its Links are dark. Mend the Link Anchor.");
        }
        lines.addAll(report(server, clan.get()));
        return lines;
    }

    /** The clan's wars and truces, in a line each. */
    public static List<String> report(MinecraftServer server, ClanData.Clan clan) {
        List<String> lines = new ArrayList<>();
        long now = now(server);
        WarData data = WarData.get(server);
        Optional<CitadelData.Citadel> mySeat = seat(server, clan);
        for (WarData.War war : data.of(clan.id())) {
            String enemy = ClanService.byId(server, war.enemyOf(clan.id())).map(ClanData.Clan::name).orElse("a clan");
            if (!war.active(now)) {
                lines.add("War with " + enemy + " begins in " + span(war.activeAt - now) + ".");
                continue;
            }
            StringBuilder line = new StringBuilder("At war with " + enemy + ".");
            war.assault(clan.id()).ifPresent(assault -> line.append(assaultText(server, war, assault, now, true)));
            war.assault(war.enemyOf(clan.id())).ifPresent(assault -> line.append(assaultText(server, war, assault, now, false)));
            if (war.cooldownUntil(clan.id()) > now && war.assault(clan.id()).isEmpty()) {
                line.append(" Next assault in ").append(span(war.cooldownUntil(clan.id()) - now)).append('.');
            }
            lines.add(line.toString());
            Optional<String> open = mySeat.flatMap(seat -> openSeason(server, clan, seat, now));
            open.ifPresent(why -> lines.add("Your seat is " + why + ": it can be taken at any time."));
        }
        if (settings().warHours().enabled() && !data.of(clan.id()).isEmpty()) {
            lines.add("War hours: " + hoursText() + (settings().warHours().contains(now) ? ", now." : "."));
        }
        for (WarData.Truce truce : data.truces) {
            if (truce.until() > now && (truce.first().equals(clan.id()) || truce.second().equals(clan.id()))) {
                UUID other = truce.first().equals(clan.id()) ? truce.second() : truce.first();
                String name = ClanService.byId(server, other).map(ClanData.Clan::name).orElse("a clan");
                lines.add("Truce with " + name + " for " + span(truce.until() - now) + ".");
            }
        }
        return lines;
    }

    private static String assaultText(MinecraftServer server, WarData.War war, WarData.Assault assault, long now, boolean ours) {
        int need = settings().assault().channelSeconds();
        String target = ours ? "Your assault" : "Their assault on your seat";
        if (now < assault.at) {
            return " " + target + " in " + span(assault.at - now) + ".";
        }
        return " " + target + " is under way: " + war.channel(assault.by) + " / " + need + ".";
    }

    static String seatName(MinecraftServer server, ClanData.Clan clan) {
        return seat(server, clan).map(CitadelData.Citadel::name).orElse("its seat");
    }

    /** "2 days", "14 hours", "an hour", "40 minutes". */
    public static String span(long millis) {
        double hours = millis / (double) CitadelWarSettings.HOUR;
        if (hours >= 47.5) {
            return Math.round(hours / 24) + " days";
        }
        if (hours >= 1.5) {
            return Math.round(hours) + " hours";
        }
        if (hours >= 0.95) {
            return "an hour";
        }
        long minutes = Math.max(1, Math.round(millis / 60_000.0));
        return minutes + (minutes == 1 ? " minute" : " minutes");
    }

    private static String hoursText() {
        CitadelWarSettings.WarHours hours = settings().warHours();
        return String.format(java.util.Locale.ROOT, "%02d:00 to %02d:00 UTC", hours.startHour(), hours.endHour());
    }

    private static String trim(double value) {
        return value == Math.floor(value) ? Integer.toString((int) value) : String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    private static String describe(List<Cost> cost) {
        return cost.isEmpty() ? "nothing" : String.join(", ", cost.stream().map(Cost::describe).toList());
    }

    /** The Spell calls every member online of a clan about the war, and tells them the particulars. */
    static void call(MinecraftServer server, ClanData.Clan clan, String key, String enemy, String citadel, String chat) {
        Component message = Component.literal(chat).withStyle(ChatFormatting.DARK_RED);
        for (UUID member : clan.members()) {
            ServerPlayer player = server.getPlayerList().getPlayer(member);
            if (player == null) {
                continue;
            }
            Map<String, String> facts = new HashMap<>(SpellCallings.playerFacts(player));
            facts.put("enemy", enemy);
            facts.put("citadel", citadel);
            SpellCallings.call(player, Calling.WAR, key, facts, true);
            player.sendSystemMessage(message);
        }
    }

    // ---- logging in and out --------------------------------------------------------------------------------------

    /** A member logs in: their clan is seen, and they hear how its wars stand. */
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        MinecraftServer server = player.server;
        ClanService.clanOf(server, player.getUUID()).ifPresent(clan -> {
            clan.seen(now(server));
            ClanData.get(server).setDirty();
            for (String line : report(server, clan)) {
                player.sendSystemMessage(Component.literal(line).withStyle(ChatFormatting.DARK_RED));
            }
        });
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ClanService.clanOf(player.server, player.getUUID()).ifPresent(clan -> {
                clan.seen(now(player.server));
                ClanData.get(player.server).setDirty();
            });
        }
    }

    // ---- operators -----------------------------------------------------------------------------------------------

    /** Moves the war clock ahead, so notices, windows and countdowns can be tried without waiting. */
    public static void skip(MinecraftServer server, double hours) {
        WarData data = WarData.get(server);
        data.clockOffset += CitadelWarSettings.hours(hours);
        data.setDirty();
    }

    /** Ends a clan's wars at once, with no truce. Returns how many. */
    public static int end(MinecraftServer server, UUID clan) {
        WarData data = WarData.get(server);
        List<WarData.War> wars = data.of(clan);
        data.wars.removeAll(wars);
        data.setDirty();
        return wars.size();
    }
}
