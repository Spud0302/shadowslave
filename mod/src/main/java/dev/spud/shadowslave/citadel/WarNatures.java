package dev.spud.shadowslave.citadel;

import dev.spud.shadowslave.ability.SaboteurCraft;
import dev.spud.shadowslave.ability.StandardColours;
import dev.spud.shadowslave.clan.ClanData;
import dev.spud.shadowslave.clan.ClanService;
import dev.spud.shadowslave.dreamrealm.DreamRealm;
import dev.spud.shadowslave.spell.SpellEvent;
import dev.spud.shadowslave.spell.SpellRecorder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Where the war Natures (Saboteur and Standard, Run I) meet clan wars: what a
 * Saboteur senses, what an Undermine charge does on a Citadel's land, and how
 * a planted Standard leans on a Gateway's channel.
 */
public final class WarNatures {
    /** How near a charge must sit to a Link Anchor to strike it. */
    static final double ANCHOR_REACH = 2.5;
    /** A member of the Citadel's clan this near a charge stamps it out. */
    static final double DEFUSE_RADIUS = 4.0;
    /** A Standard planted this near a Gateway leans on its channel. */
    public static final double CHANNEL_REACH = 12.0;

    /** A Citadel a Saboteur feels: its name, its Gateway, its Link Anchor if raised, and whether its clan is at war with theirs. */
    public record Sensed(UUID id, String name, BlockPos gateway, Optional<BlockPos> anchor, boolean enemy) {
    }

    /** What an Undermine charge did when it went. */
    public enum Undermined {
        /** Nothing of a clan's here: it cracks the stone around it. */
        WILD,
        /** A clan's land the Spell will not let it touch. */
        HELD,
        /** A member of the clan stood over it and stamped it out. */
        DEFUSED,
        /** It cut an enemy Link Anchor. */
        CUT,
        /** It damaged an upgrade on an enemy Citadel's land. */
        RAIDED
    }

    private WarNatures() {
    }

    /** The clan Citadels within {@code radius} of the player that are not their own clan's. */
    public static List<Sensed> sense(ServerPlayer player, double radius) {
        List<Sensed> found = new ArrayList<>();
        if (!DreamRealm.is(player.level())) {
            return found;
        }
        ServerLevel realm = player.serverLevel();
        Optional<ClanData.Clan> mine = ClanService.clanOf(player.server, player.getUUID());
        for (CitadelData.Citadel citadel : CitadelData.get(realm).citadels) {
            if (citadel.hold != CitadelData.Hold.CLAN || citadel.clan == null
                    || mine.filter(clan -> clan.id().equals(citadel.clan)).isPresent()) {
                continue;
            }
            boolean near = citadel.gateway.distSqr(player.blockPosition()) <= radius * radius
                    || (citadel.linkAnchor != null && citadel.linkAnchor.distSqr(player.blockPosition()) <= radius * radius);
            if (near) {
                boolean enemy = mine.isPresent() && CitadelWars.atWar(player.server, mine.get().id(), citadel.clan);
                found.add(new Sensed(citadel.id, citadel.name, citadel.gateway, Optional.ofNullable(citadel.linkAnchor), enemy));
            }
        }
        return found;
    }

    /** An Undermine charge laid by {@code player} goes off at {@code at}. */
    public static Undermined undermine(ServerPlayer player, ServerLevel realm, BlockPos at) {
        if (!DreamRealm.is(realm)) {
            return Undermined.WILD;
        }
        MinecraftServer server = player.server;
        Optional<ClanData.Clan> mine = ClanService.clanOf(server, player.getUUID());
        double land = CitadelContentLoader.settings().landRadius();
        for (CitadelData.Citadel citadel : List.copyOf(CitadelData.get(realm).citadels)) {
            boolean atAnchor = citadel.linkAnchor != null && citadel.linkAnchor.getCenter().distanceTo(at.getCenter()) <= ANCHOR_REACH;
            boolean onLand = citadel.gateway.distSqr(at) <= land * land;
            if (!atAnchor && !onLand) {
                continue;
            }
            if (citadel.hold != CitadelData.Hold.CLAN || citadel.clan == null) {
                // Wild or held by its guardian: the Citadel's own stone does not crack.
                return onLand ? Undermined.HELD : Undermined.WILD;
            }
            Optional<ClanData.Clan> owner = ClanService.byId(server, citadel.clan);
            boolean enemy = owner.isPresent() && mine.isPresent() && CitadelWars.atWar(server, mine.get().id(), owner.get().id());
            if (!enemy) {
                return Undermined.HELD;
            }
            if (!CitadelWars.present(realm, owner.get(), at, DEFUSE_RADIUS).isEmpty()) {
                return Undermined.DEFUSED;
            }
            if (atAnchor && !citadel.linkCut) {
                CitadelWars.cut(realm, citadel);
                SpellRecorder.record(server, SpellEvent.Kind.WAR_STRIKE, Optional.empty(), citadel.name, "supply line undermined by " + mine.get().name());
                ClanService.tell(server, mine.get(), player.getGameProfile().getName() + " undermined the Link Anchor of " + citadel.name
                        + ". Its Gateway starves until they mend it.");
                ClanService.tell(server, owner.get(), "A charge of " + mine.get().name() + " cut the supply line of " + citadel.name
                        + ". Mend its Link Anchor at " + citadel.linkAnchor.getX() + ", " + citadel.linkAnchor.getZ() + ".");
                return Undermined.CUT;
            }
            if (!onLand) {
                return Undermined.HELD;
            }
            WarData data = WarData.get(server);
            long now = CitadelWars.now(server);
            if (data.raidedUntil.getOrDefault(citadel.id, 0L) > now) {
                player.displayClientMessage(Component.literal(citadel.name + " was struck too lately; its upgrades hold for now.")
                        .withStyle(ChatFormatting.GRAY), true);
                return Undermined.HELD;
            }
            CitadelWarSettings.Outskirts outskirts = CitadelWars.settings().outskirts();
            data.raidedUntil.put(citadel.id, now + CitadelWarSettings.hours(outskirts.cooldownHours()));
            data.setDirty();
            List<String> damaged = CitadelRaids.damage(realm, citadel, 1);
            CitadelData.get(realm).setDirty();
            SpellRecorder.record(server, SpellEvent.Kind.WAR_STRIKE, Optional.empty(), citadel.name, "undermined by " + mine.get().name());
            String harm = damaged.isEmpty() ? "found nothing built to break" : "damaged its " + CitadelRaids.join(damaged);
            ClanService.tell(server, mine.get(), player.getGameProfile().getName() + "'s charge at " + citadel.name + " " + harm + ".");
            ClanService.tell(server, owner.get(), "A charge of " + mine.get().name() + " " + harm.replace("its ", "the ") + " at " + citadel.name
                    + (damaged.isEmpty() ? "." : ". Repair it at the Gateway."));
            realm.playSound(null, at, SoundEvents.WITHER_BREAK_BLOCK, SoundSource.BLOCKS, 1.0F, 0.8F);
            return Undermined.RAIDED;
        }
        return Undermined.WILD;
    }

    /** How much each striker at an anchor counts for: twice for a Saboteur who cuts the lines. */
    static int strikeWeight(List<ServerPlayer> strikers) {
        int weight = 0;
        for (ServerPlayer striker : strikers) {
            weight += SaboteurCraft.cutsTheLines(striker) ? 2 : 1;
        }
        return weight;
    }

    /** Everyone who broke an enemy's structure: a Marked Saboteur among them glows. */
    static void broke(List<ServerPlayer> players) {
        players.forEach(SaboteurCraft::marked);
    }

    /** How many Standards of {@code clan} are planted near {@code gateway}. */
    static int standards(ServerLevel realm, ClanData.Clan clan, BlockPos gateway) {
        int count = 0;
        for (UUID owner : StandardColours.plantedNear(realm, gateway, CHANNEL_REACH)) {
            if (clan.has(owner)) {
                count++;
            }
        }
        return count;
    }

    /** A Standard at work at a Gateway: a column of its colour. */
    static void showStandards(ServerLevel realm, BlockPos gateway) {
        for (BlockPos banner : StandardColours.bannersNear(realm, gateway, CHANNEL_REACH)) {
            realm.sendParticles(ParticleTypes.WITCH, banner.getX() + 0.5, banner.getY() + 1.8, banner.getZ() + 0.5, 6, 0.2, 0.4, 0.2, 0.02);
        }
    }
}
