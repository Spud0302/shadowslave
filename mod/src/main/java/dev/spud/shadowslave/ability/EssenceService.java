package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.attachment.ModAttachments;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SoulService;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

/**
 * Essence, the one pool every Aspect ability draws on (DESIGN). There are no
 * cooldowns: the pool refills over time, so choosing what to spend it on is
 * the skill. A Sleeper already spends essence on their Dormant ability; the
 * pool grows with Soul Rank.
 */
public final class EssenceService {
    static final int DORMANT_CAPACITY = 100;
    static final int AWAKENED_CAPACITY = 200;
    /** An empty pool refills in this many ticks. */
    static final int FULL_REFILL_TICKS = 20 * 60;
    /** How often a quiet pool is resent to the client to correct drift in its prediction. */
    static final int RESYNC_TICKS = 20 * 5;

    private EssenceService() {
    }

    /** Essence the player's core holds when full; 0 before the Spell names an Aspect. */
    public static int capacity(ServerPlayer player) {
        return capacityFor(SoulService.get(player).soulRank());
    }

    static int capacityFor(Optional<SoulRank> rank) {
        return rank.map(EssenceService::baseCapacity).orElse(0);
    }

    static int baseCapacity(SoulRank rank) {
        return switch (rank) {
            case DORMANT -> DORMANT_CAPACITY;
            case AWAKENED -> AWAKENED_CAPACITY;
            // Higher ranks are not playable yet; keep the curve going so admin tools do not break.
            default -> AWAKENED_CAPACITY * (rank.ordinal());
        };
    }

    /** Essence regained per tick. */
    public static float regenPerTick(ServerPlayer player) {
        return regenPerTickFor(capacity(player));
    }

    static float regenPerTickFor(int capacity) {
        return (float) capacity / FULL_REFILL_TICKS;
    }

    public static float current(ServerPlayer player) {
        return Math.min(capacity(player), player.getData(ModAttachments.ESSENCE).current());
    }

    public static boolean covers(ServerPlayer player, int cost) {
        return new EssenceData(current(player)).covers(cost);
    }

    /** Takes up to {@code amount} essence; the pool never goes below zero. */
    public static void spend(ServerPlayer player, int amount) {
        player.setData(ModAttachments.ESSENCE, new EssenceData(current(player)).spend(amount));
    }

    /** Fills the pool, as on a fresh roll or a rank-up. */
    public static void fill(ServerPlayer player) {
        player.setData(ModAttachments.ESSENCE, new EssenceData(capacity(player)));
    }

    /** Refills a little each tick; returns true when the client should get a fresh reading. */
    static boolean tick(ServerPlayer player) {
        int capacity = capacity(player);
        if (capacity <= 0) {
            return false;
        }
        EssenceData before = player.getData(ModAttachments.ESSENCE);
        if (before.current() < capacity) {
            player.setData(ModAttachments.ESSENCE, before.refill(regenPerTickFor(capacity), capacity));
            return player.tickCount % RESYNC_TICKS == 0;
        }
        if (before.current() > capacity) {
            player.setData(ModAttachments.ESSENCE, new EssenceData(capacity));
            return true;
        }
        return false;
    }
}
