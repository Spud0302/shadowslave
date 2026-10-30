package dev.spud.shadowslave.citadel;

import dev.spud.shadowslave.clan.ClanData;
import dev.spud.shadowslave.clan.ClanService;
import dev.spud.shadowslave.creature.NightmareCreature;
import dev.spud.shadowslave.dreamrealm.DreamCrossing;
import dev.spud.shadowslave.dreamrealm.DreamRealm;
import dev.spud.shadowslave.dreamrealm.seed.SeedData;
import dev.spud.shadowslave.gate.GateEvent;
import dev.spud.shadowslave.gate.GateForecast;
import dev.spud.shadowslave.gate.GateService;
import dev.spud.shadowslave.gate.GateTrigger;
import dev.spud.shadowslave.item.ModItems;
import dev.spud.shadowslave.spell.SpellEvent;
import dev.spud.shadowslave.spell.SpellRecord;
import dev.spud.shadowslave.spell.SpellRecorder;
import dev.spud.shadowslave.spell.SpellWords;
import dev.spud.shadowslave.spell.calling.Calling;
import dev.spud.shadowslave.spell.calling.SpellCallings;
import dev.spud.shadowslave.spell.truename.TrueNames;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Raids on Citadels (DESIGN, Run C, Jack 2026-10-30: only from Seeds blooming
 * near a Citadel). A Seed that blooms near a claimed Citadel opens its Gate
 * where it grew, at its region's Category, instead of in the waking world, and
 * the Spell calls every member of the clan to it. The Gate's waves march on
 * the Gateway, and the Guardian's echo strikes at them while the Gateway is
 * fed enough. Slaying the Gate's Guardian holds the Citadel: each defender
 * earns soul shards, the Spell's record keeps the deed for their next
 * appraisal, and a lone defender may earn a True Name. Raiders who hold the
 * Gateway long enough, or a raid nobody answers, lose it: upgrades are
 * damaged and provisions spoiled, and the Citadel is never lost.
 */
public final class CitadelRaids {
    static final int TICK_INTERVAL = 20;
    /** How close behind its target a raider keeps chasing before it turns back to the Gateway. */
    static final double CHASE_RANGE = 24.0;

    private CitadelRaids() {
    }

    public static void bootstrap() {
        GateService.addCloseListener(CitadelRaids::onGateClosed);
    }

    static CitadelRaidSettings settings() {
        return CitadelContentLoader.content().raids();
    }

    // ---- a Seed blooms at the walls ------------------------------------------------------------------------------

    /**
     * Called when a Seed blooms. If a claimed Citadel stands within the raid
     * radius, its Gate opens here and raids it. Returns empty if no Citadel is
     * near, true if a raid began (the Seed is spent), or false if one is near
     * but the Gate cannot open yet (the Seed tries again later).
     */
    public static Optional<Boolean> bloomNear(ServerLevel realm, SeedData.Seed seed, GateForecast forecast) {
        if (seed.echoSite().isPresent()) {
            return Optional.empty();
        }
        double reach = settings().radius();
        Optional<CitadelData.Citadel> target = CitadelData.get(realm).citadels.stream()
                .filter(citadel -> citadel.hold == CitadelData.Hold.CLAN && citadel.clan != null)
                .filter(citadel -> citadel.gateway.distSqr(seed.pos()) <= reach * reach)
                .min((a, b) -> Double.compare(a.gateway.distSqr(seed.pos()), b.gateway.distSqr(seed.pos())));
        if (target.isEmpty()) {
            return Optional.empty();
        }
        if (target.get().raid != null) {
            return Optional.of(false);
        }
        return Optional.of(begin(realm, target.get(), seed.pos().below(), forecast));
    }

    /** Opens a raid's Gate at {@code at} against a Citadel and calls its clan. Returns false if no Gate can open there yet. */
    public static boolean begin(ServerLevel realm, CitadelData.Citadel citadel, BlockPos at, GateForecast forecast) {
        Optional<GateEvent> gate = GateService.open(realm, at, GateTrigger.CITADEL_RAID, forecast, citadel.id);
        if (gate.isEmpty()) {
            return false;
        }
        citadel.raid = new CitadelData.Raid(gate.get().id(), realm.getGameTime(), forecast.likeliest());
        CitadelData.get(realm).setDirty();
        SpellRecorder.record(realm.getServer(), SpellEvent.Kind.CITADEL_RAIDED, Optional.empty(), citadel.name,
                "category " + forecast.likeliest());
        call(realm, citadel, forecast.likeliest(), at);
        return true;
    }

    /** The Spell calls every member online to the raid, wherever they are. */
    static void call(ServerLevel realm, CitadelData.Citadel citadel, int category, BlockPos at) {
        MinecraftServer server = realm.getServer();
        Optional<ClanData.Clan> clan = ClanService.byId(server, citadel.clan);
        if (clan.isEmpty()) {
            return;
        }
        for (UUID member : clan.get().members()) {
            ServerPlayer player = server.getPlayerList().getPlayer(member);
            if (player == null) {
                continue;
            }
            Map<String, String> facts = new java.util.HashMap<>(SpellCallings.playerFacts(player));
            facts.put("citadel", citadel.name);
            facts.put("category", SpellWords.ordinal(category));
            SpellCallings.call(player, Calling.CITADEL_RAID, "citadel_raid", facts, true);
            player.sendSystemMessage(Component.literal("A Nightmare Seed has bloomed at the walls of " + citadel.name + " ("
                    + at.getX() + ", " + at.getZ() + "). Its Gate will raid the Citadel. Defend it at "
                    + citadel.gateway.getX() + ", " + citadel.gateway.getZ() + ".").withStyle(ChatFormatting.DARK_RED));
            if (DreamRealm.is(player.level()) && player.blockPosition().closerThan(citadel.gateway, 160)) {
                DreamCrossing.title(player, Component.literal("Citadel Raid").withStyle(ChatFormatting.DARK_RED),
                        Component.literal(citadel.name + " is under attack").withStyle(ChatFormatting.GRAY));
            }
        }
    }

    // ---- the fight -----------------------------------------------------------------------------------------------

    /** Once a second while a raid lasts: count the defenders, march the raiders, let the echo strike, fill the breach. */
    static void tick(ServerLevel realm, CitadelData.Citadel citadel) {
        CitadelData.Raid raid = citadel.raid;
        Optional<GateEvent> gate = GateService.byId(realm, raid.gate);
        if (gate.isEmpty() || gate.get().phase() == GateEvent.Phase.CLOSED) {
            // The Gate is gone without a word to us (an operator, or a lost save): the raid simply ends.
            citadel.raid = null;
            return;
        }
        CitadelRaidSettings settings = settings();
        long now = realm.getGameTime();
        Optional<ClanData.Clan> clan = ClanService.byId(realm.getServer(), citadel.clan);
        double defend = (double) settings.defendRadius() * settings.defendRadius();
        for (ServerPlayer player : realm.players()) {
            if (player.isSpectator() || clan.isEmpty() || !clan.get().has(player.getUUID())) {
                continue;
            }
            if (player.blockPosition().distSqr(citadel.gateway) <= defend || player.blockPosition().distSqr(gate.get().center()) <= defend) {
                raid.defenders.add(player.getUUID());
                raid.lastDefended = now;
            }
        }
        if (now - raid.lastDefended > settings.unansweredMinutes() * 1200L) {
            lose(realm, citadel, "No one came to defend it.");
            return;
        }
        if (gate.get().phase() == GateEvent.Phase.WARNING) {
            return;
        }
        List<NightmareCreature> raiders = raiders(realm, citadel, gate.get());
        for (NightmareCreature raider : raiders) {
            march(raider, citadel);
        }
        strike(realm, citadel, raid, raiders);
        int pressing = 0;
        for (NightmareCreature raider : raiders) {
            if (raider.isAlive() && atGateway(raider, citadel, settings.breachRadius())) {
                pressing++;
            }
        }
        if (pressing > 0) {
            raid.breach += pressing;
            if (raid.breach % 10 < pressing) {
                realm.playSound(null, citadel.gateway, SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.HOSTILE, 1.0F, 0.6F);
            }
            if (raid.breach >= settings.breachLimit()) {
                lose(realm, citadel, "Its raiders held the Gateway.");
            }
        }
    }

    /** The living creatures the raid's Gate let out. */
    static List<NightmareCreature> raiders(ServerLevel realm, CitadelData.Citadel citadel, GateEvent gate) {
        AABB area = new AABB(citadel.gateway).inflate(settings().radius() + 48);
        return realm.getEntitiesOfClass(NightmareCreature.class, area, creature -> creature.isAlive()
                && creature.getPersistentData().hasUUID(GateService.GATE_TAG)
                && gate.id().equals(creature.getPersistentData().getUUID(GateService.GATE_TAG)));
    }

    /** A raider with nothing close to fight goes for the Gateway. */
    static void march(NightmareCreature raider, CitadelData.Citadel citadel) {
        LivingEntity target = raider.getTarget();
        if (target != null && target.isAlive() && raider.distanceToSqr(target) <= CHASE_RANGE * CHASE_RANGE) {
            return;
        }
        if (target != null) {
            raider.setTarget(null);
        }
        if (!atGateway(raider, citadel, 2)) {
            raider.getNavigation().moveTo(citadel.gateway.getX() + 0.5, citadel.gateway.getY(), citadel.gateway.getZ() + 0.5, 1.0);
        }
    }

    static boolean atGateway(LivingEntity entity, CitadelData.Citadel citadel, int radius) {
        double dx = entity.getX() - (citadel.gateway.getX() + 0.5);
        double dz = entity.getZ() - (citadel.gateway.getZ() + 0.5);
        return dx * dx + dz * dz <= radius * radius && Math.abs(entity.getY() - citadel.gateway.getY()) <= 4;
    }

    /** The Guardian's echo: while the Gateway is not too neglected, it strikes the nearest raider in reach. */
    static void strike(ServerLevel realm, CitadelData.Citadel citadel, CitadelData.Raid raid, List<NightmareCreature> raiders) {
        CitadelRaidSettings settings = settings();
        if (settings.echoRadius() <= 0 || CitadelLife.stage(citadel).ordinal() >= settings.echoFailsAt().ordinal()) {
            return;
        }
        raid.echoCooldown -= TICK_INTERVAL;
        if (raid.echoCooldown > 0) {
            return;
        }
        Vec3 from = Vec3.atCenterOf(citadel.gateway).add(0, 0.6, 0);
        double reach = (double) settings.echoRadius() * settings.echoRadius();
        Optional<NightmareCreature> nearest = raiders.stream().filter(raider -> raider.distanceToSqr(from) <= reach)
                .min((a, b) -> Double.compare(a.distanceToSqr(from), b.distanceToSqr(from)));
        if (nearest.isEmpty()) {
            return;
        }
        raid.echoCooldown = settings.echoSeconds() * 20;
        NightmareCreature raider = nearest.get();
        Vec3 to = raider.position().add(0, raider.getBbHeight() / 2, 0);
        Vec3 step = to.subtract(from);
        int points = Math.max(4, (int) (step.length() * 2));
        for (int i = 0; i <= points; i++) {
            Vec3 at = from.add(step.scale(i / (double) points));
            realm.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, at.x, at.y, at.z, 1, 0.02, 0.02, 0.02, 0.0);
        }
        realm.sendParticles(ParticleTypes.SOUL, to.x, to.y, to.z, 10, 0.3, 0.3, 0.3, 0.02);
        realm.playSound(null, citadel.gateway, SoundEvents.EVOKER_CAST_SPELL, SoundSource.BLOCKS, 1.0F, 0.6F);
        raider.hurt(realm.damageSources().magic(), settings.echoDamage() * (citadel.rank.ordinal() + 1));
    }

    // ---- the end of it -------------------------------------------------------------------------------------------

    /** The raid is lost: upgrades are damaged, provisions spoil, and its Gate closes. The Citadel stays the clan's. */
    public static void lose(ServerLevel realm, CitadelData.Citadel citadel, String why) {
        CitadelData.Raid raid = citadel.raid;
        if (raid == null || raid.lost) {
            return;
        }
        raid.lost = true;
        List<String> damaged = damage(realm, citadel);
        citadel.provisions *= 1.0 - settings().provisionsLost();
        CitadelData.get(realm).setDirty();
        SpellRecorder.record(realm.getServer(), SpellEvent.Kind.CITADEL_RAIDED, Optional.empty(), citadel.name, "lost");
        String harm = damaged.isEmpty() ? "" : " The raid damaged its " + join(damaged) + ". Repair them at the Gateway.";
        ClanService.byId(realm.getServer(), citadel.clan).ifPresent(clan -> ClanService.tell(realm.getServer(), clan,
                "The raid on " + citadel.name + " is lost. " + why + harm + " The Citadel is still yours."));
        for (ServerPlayer player : realm.getPlayers(player -> player.blockPosition().closerThan(citadel.gateway, 96))) {
            DreamCrossing.title(player, Component.literal("Raid Lost").withStyle(ChatFormatting.DARK_RED),
                    Component.literal(damaged.isEmpty() ? citadel.name + " stands, shaken" : join(damaged) + " damaged")
                            .withStyle(ChatFormatting.GRAY));
        }
        realm.playSound(null, citadel.gateway, SoundEvents.WITHER_BREAK_BLOCK, SoundSource.BLOCKS, 1.0F, 0.6F);
        realm.sendParticles(ParticleTypes.LARGE_SMOKE, citadel.gateway.getX() + 0.5, citadel.gateway.getY() + 0.8,
                citadel.gateway.getZ() + 0.5, 40, 0.8, 0.6, 0.8, 0.02);
        GateService.close(realm, raid.gate);
        citadel.raid = null;
    }

    /** Damages up to the set number of built, whole upgrades, chosen at random. Returns their names. */
    static List<String> damage(ServerLevel realm, CitadelData.Citadel citadel) {
        List<String> whole = new ArrayList<>();
        citadel.tiers.forEach((id, tier) -> {
            if (tier > 0 && !citadel.damaged.contains(id) && CitadelContentLoader.content().upgrade(id).isPresent()) {
                whole.add(id);
            }
        });
        Collections.shuffle(whole, new java.util.Random(realm.getRandom().nextLong()));
        List<String> names = new ArrayList<>();
        for (String id : whole.subList(0, Math.min(settings().damaged(), whole.size()))) {
            citadel.damaged.add(id);
            names.add(CitadelContentLoader.content().upgrade(id).map(CitadelUpgrade::name).orElse(id));
        }
        return names;
    }

    /** A raid's Gate closed: if its Guardian fell, the Citadel held. */
    static void onGateClosed(ServerLevel level, GateEvent gate, boolean guardianSlain) {
        if (gate.trigger() != GateTrigger.CITADEL_RAID || gate.source().isEmpty() || !DreamRealm.is(level)) {
            return;
        }
        Optional<CitadelData.Citadel> citadel = CitadelData.get(level).byId(gate.source().get());
        if (citadel.isEmpty() || citadel.get().raid == null || !citadel.get().raid.gate.equals(gate.id())) {
            return;
        }
        if (guardianSlain && !citadel.get().raid.lost) {
            held(level, citadel.get(), gate.category());
        }
        citadel.get().raid = null;
        CitadelData.get(level).setDirty();
    }

    /** The Citadel held: every defender is paid in shards, and a lone defender's deed is one the Spell may name. */
    static void held(ServerLevel realm, CitadelData.Citadel citadel, int category) {
        MinecraftServer server = realm.getServer();
        CitadelData.Raid raid = citadel.raid;
        List<ServerPlayer> defenders = new ArrayList<>();
        for (UUID id : raid.defenders) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null) {
                defenders.add(player);
            }
        }
        int shards = settings().shardsPerCategory() * Math.max(1, category);
        for (ServerPlayer player : defenders) {
            if (shards > 0) {
                citadel.rank.shard().ifPresent(rank -> {
                    ItemStack stack = new ItemStack(ModItems.shardFor(rank), shards);
                    if (!player.getInventory().add(stack)) {
                        player.drop(stack, false);
                    }
                });
            }
            SpellRecord.get(server).tally(player.getUUID()).heldRaid();
            SpellRecord.get(server).setDirty();
            SpellRecorder.record(server, SpellEvent.Kind.CITADEL_DEFENDED, Optional.of(player), citadel.name, "category " + category);
            DreamCrossing.title(player, Component.literal("Citadel Held").withStyle(ChatFormatting.LIGHT_PURPLE),
                    Component.literal(citadel.name + " stands").withStyle(ChatFormatting.GRAY));
            DreamCrossing.spell(player, Component.literal("You held " + citadel.name + " against the dream"
                    + (shards > 0 ? " and earned " + shards + " soul shards." : ".")).withStyle(ChatFormatting.LIGHT_PURPLE));
            SpellRecorder.first(player, "citadel_raid_held", citadel.name, "citadel_raid", "hold a Citadel against a raid");
        }
        if (defenders.size() == 1 && raid.defenders.size() == 1) {
            TrueNames.considerRaid(defenders.get(0), true);
        }
        ClanService.byId(server, citadel.clan).ifPresent(clan -> ClanService.tell(server, clan, citadel.name + " held against the raid"
                + (defenders.isEmpty() ? "." : ", defended by " + String.join(", ", defenders.stream().map(player -> player.getGameProfile().getName()).toList()) + ".")));
        realm.playSound(null, citadel.gateway, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1.5F, 1.2F);
        realm.sendParticles(ParticleTypes.END_ROD, citadel.gateway.getX() + 0.5, citadel.gateway.getY() + 1.0,
                citadel.gateway.getZ() + 0.5, 60, 0.6, 1.0, 0.6, 0.08);
    }

    /** One line for the Gateway screen on the raid under way, or empty. */
    static String describe(ServerLevel realm, CitadelData.Citadel citadel) {
        CitadelData.Raid raid = citadel.raid;
        if (raid == null) {
            return "";
        }
        Optional<GateEvent> gate = GateService.byId(realm, raid.gate);
        if (gate.isEmpty()) {
            return "";
        }
        String where = "at " + gate.get().center().getX() + ", " + gate.get().center().getZ();
        return switch (gate.get().phase()) {
            case WARNING -> "A Seed bloomed " + where + ". Its Gate is about to open: " + gate.get().forecast().describeLikeliest() + ".";
            case OPEN, GUARDIAN -> "Under raid from a Category " + gate.get().category() + " Gate " + where + ". Breach "
                    + raid.breach + " of " + settings().breachLimit() + ".";
            case CLOSED -> "";
        };
    }

    private static String join(List<String> names) {
        if (names.size() <= 1) {
            return String.join("", names);
        }
        return String.join(", ", names.subList(0, names.size() - 1)) + " and " + names.get(names.size() - 1);
    }
}
