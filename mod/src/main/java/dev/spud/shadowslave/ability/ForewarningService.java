package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.network.payload.ForewarningPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Seer's innate rule, Forewarning: a flash on screen just before a creature
 * near you lands a big attack. Nightmare Creatures report their own windups
 * (a Tyrant's slam, a Chainback's chain); a creeper starting to hiss counts too.
 * Shared Sight passes the flash on to players near the Seer.
 */
public final class ForewarningService {
    static final double SENSE_RANGE = 24.0;
    static final double CREEPER_RANGE = 16.0;
    /** Roughly how long a creeper's fuse runs once it starts to hiss. */
    static final int CREEPER_FUSE_TICKS = 30;

    /** Creepers already flashed, so one hiss warns once. Cleared when the creeper stops or dies. */
    private static final Map<UUID, Set<UUID>> WARNED_CREEPERS = new ConcurrentHashMap<>();

    private ForewarningService() {
    }

    /** A creature has started winding up a big attack that lands in about {@code windupTicks}. */
    public static void bigAttack(LivingEntity attacker, int windupTicks) {
        if (!(attacker.level() instanceof ServerLevel level)) {
            return;
        }
        Set<ServerPlayer> seers = new LinkedHashSet<>();
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(attacker) <= SENSE_RANGE * SENSE_RANGE && isSeer(player)) {
                seers.add(player);
            }
        }
        Set<ServerPlayer> told = new HashSet<>();
        for (ServerPlayer seer : seers) {
            send(seer, attacker, windupTicks, false);
            told.add(seer);
        }
        for (ServerPlayer seer : seers) {
            if (!SharedSightAbility.active(seer)) {
                continue;
            }
            for (ServerPlayer ally : level.players()) {
                if (!told.contains(ally) && !ally.isSpectator()
                        && ally.distanceToSqr(seer) <= SharedSightAbility.SHARE_RANGE * SharedSightAbility.SHARE_RANGE) {
                    send(ally, attacker, windupTicks, true);
                    told.add(ally);
                }
            }
        }
    }

    public static boolean isSeer(ServerPlayer player) {
        return KitRules.hasInnate(player, AspectKitCatalog.SEER.innateId());
    }

    private static void send(ServerPlayer player, LivingEntity attacker, int ticks, boolean shared) {
        if (player.connection != null && player.connection.hasChannel(ForewarningPayload.TYPE)) {
            PacketDistributor.sendToPlayer(player, new ForewarningPayload(attacker.getId(), ticks, shared));
        }
    }

    /** Twice a second for each Seer: a creeper that has just started to hiss is a big attack too. */
    static void tickSeer(ServerPlayer player) {
        if (player.tickCount % 10 != 0) {
            return;
        }
        Set<UUID> warned = WARNED_CREEPERS.computeIfAbsent(player.getUUID(), id -> new HashSet<>());
        Set<UUID> hissing = new HashSet<>();
        for (Creeper creeper : player.serverLevel().getEntitiesOfClass(Creeper.class,
                player.getBoundingBox().inflate(CREEPER_RANGE), creeper -> creeper.isAlive() && creeper.getSwellDir() > 0)) {
            hissing.add(creeper.getUUID());
            if (!warned.contains(creeper.getUUID())) {
                send(player, creeper, CREEPER_FUSE_TICKS, false);
            }
        }
        warned.retainAll(hissing);
        warned.addAll(hissing);
    }

    static void forget(UUID player) {
        WARNED_CREEPERS.remove(player);
    }
}
