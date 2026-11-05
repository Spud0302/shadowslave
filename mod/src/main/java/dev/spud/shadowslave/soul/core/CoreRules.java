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
    public static final int ASCENDED_LIMIT = 2400;
    public static final int TRANSCENDENT_LIMIT = 7200;
    /** Max health a saturated core adds at each rank; a risen core keeps what the ranks below it gave. */
    static final double HEALTH_PER_RANK = 4.0;

    /** A faint shard (from an ordinary mob) carries this fraction of a creature shard's value. */
    public static final int FAINT_DIVISOR = 4;
    /** From this Rank on, faint shards give a core nothing at all. */
    public static final SoulRank FAINT_CUTOFF = SoulRank.ASCENDED;

    private CoreRules() {
    }

    public static int limit(SoulRank rank) {
        return switch (rank) {
            case DORMANT -> DORMANT_LIMIT;
            case AWAKENED -> AWAKENED_LIMIT;
            case ASCENDED -> ASCENDED_LIMIT;
            case TRANSCENDENT -> TRANSCENDENT_LIMIT;
            // Higher ranks are not playable yet; keep the curve going for admin tools.
            default -> TRANSCENDENT_LIMIT * 3 * (rank.ordinal() - SoulRank.TRANSCENDENT.ordinal());
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

    /** Units a faint shard of this rank carries: a quarter of a creature shard's, never less than one. */
    public static int faintUnits(ShardRank rank) {
        return Math.max(1, rank.value() / FAINT_DIVISOR);
    }

    /** Whether faint shards still stir a core of this rank. */
    public static boolean faintShardsHelp(SoulRank coreRank) {
        return coreRank.ordinal() < FAINT_CUTOFF.ordinal();
    }

    public static boolean saturated(int saturation, SoulRank rank) {
        return saturation >= limit(rank);
    }

    public static float fraction(int saturation, SoulRank rank) {
        return Math.min(1.0F, (float) saturation / limit(rank));
    }

    /** Extra max health from the core: the full bonus of each rank already left behind, plus the current fill. */
    public static double healthBonus(int saturation, SoulRank rank) {
        int passed = Math.min(rank.ordinal(), SoulRank.TRANSCENDENT.ordinal());
        double current = rank.ordinal() <= SoulRank.TRANSCENDENT.ordinal() ? fraction(saturation, rank) : 1.0;
        return HEALTH_PER_RANK * (passed + current);
    }

    /** The share of a PvP victim's saturation that leaves with their shard. */
    public static int pvpLoss(int saturation) {
        return saturation <= 0 ? 0 : Math.max(1, (int) Math.ceil(saturation * 0.25));
    }
}
