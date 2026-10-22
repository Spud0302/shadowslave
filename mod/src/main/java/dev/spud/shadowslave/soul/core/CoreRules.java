package dev.spud.shadowslave.soul.core;

import dev.spud.shadowslave.soul.SoulRank;

/**
 * Pure numbers for soul cores (DESIGN, from the chapters' rules). Each core
 * has a limit; once saturated, more shards do nothing until the core changes
 * rank (CANON, ch. 289 and 345). Shards from weaker creatures help a stronger
 * core far less, so a Dormant shard fills an Awakened core a quarter as much.
 */
public final class CoreRules {
    public static final int DORMANT_LIMIT = 160;
    public static final int AWAKENED_LIMIT = 600;
    /** Max health a saturated Dormant core adds; an Awakened core keeps it and adds the same again when full. */
    static final double HEALTH_PER_RANK = 4.0;

    private CoreRules() {
    }

    public static int limit(SoulRank rank) {
        return switch (rank) {
            case DORMANT -> DORMANT_LIMIT;
            case AWAKENED -> AWAKENED_LIMIT;
            // Higher ranks are not playable yet; keep the curve going for admin tools.
            default -> AWAKENED_LIMIT * 4 * rank.ordinal();
        };
    }

    /** Saturation a shard of {@code shardRank}, carrying {@code units}, adds to a core of {@code coreRank}. */
    public static int absorbValue(ShardRank shardRank, int units, SoulRank coreRank) {
        int below = coreRank.ordinal() - shardRank.tier();
        if (below <= 0 || units <= 0) {
            return Math.max(0, units);
        }
        // A quarter for each rank the shard sits below the core, never less than one.
        int shift = Math.min(30, 2 * below);
        return Math.max(1, units >> shift);
    }

    public static boolean saturated(int saturation, SoulRank rank) {
        return saturation >= limit(rank);
    }

    public static float fraction(int saturation, SoulRank rank) {
        return Math.min(1.0F, (float) saturation / limit(rank));
    }

    /** Extra max health from the core: the full bonus of each rank already left behind, plus the current fill. */
    public static double healthBonus(int saturation, SoulRank rank) {
        int passed = Math.min(rank.ordinal(), SoulRank.AWAKENED.ordinal());
        double current = rank.ordinal() <= SoulRank.AWAKENED.ordinal() ? fraction(saturation, rank) : 1.0;
        return HEALTH_PER_RANK * (passed + current);
    }

    /** The share of a PvP victim's saturation that leaves with their shard. */
    public static int pvpLoss(int saturation) {
        return saturation <= 0 ? 0 : Math.max(1, (int) Math.ceil(saturation * 0.25));
    }
}
