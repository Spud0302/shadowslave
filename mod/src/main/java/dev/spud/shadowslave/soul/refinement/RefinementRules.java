package dev.spud.shadowslave.soul.refinement;

import dev.spud.shadowslave.soul.SoulRank;

import java.util.Optional;

/** Pure numbers for refinement (DESIGN): what points are earned and what each one does. */
public final class RefinementRules {
    /** One point when the Spell names an Aspect. */
    static final int DORMANT_POINTS = 1;
    /** Three more with each rank after that. */
    static final int POINTS_PER_RANK = 3;

    public static final double HEALTH_PER_BODY = 2.0;
    /** Fraction of base movement speed. */
    public static final double SPEED_PER_BODY = 0.03;
    public static final double DAMAGE_PER_BODY = 0.5;
    public static final int CAPACITY_PER_SOUL = 15;
    /** Fraction of the base refill rate. */
    public static final double REGEN_PER_SOUL = 0.10;
    /** Fraction of every ability's essence cost. */
    public static final double COST_CUT_PER_ASPECT = 0.06;
    static final double MAX_COST_CUT = 0.30;

    private RefinementRules() {
    }

    /** Points a rank brings in total, before respecs. */
    public static int pointsFor(Optional<SoulRank> rank) {
        return rank.map(value -> DORMANT_POINTS + POINTS_PER_RANK * value.ordinal()).orElse(0);
    }

    public static int earned(Optional<SoulRank> rank, RefinementData data) {
        return Math.max(0, pointsFor(rank) - data.respecs());
    }

    public static int unspent(Optional<SoulRank> rank, RefinementData data) {
        return Math.max(0, earned(rank, data) - data.allocated());
    }

    /** A respec gives back every placed point and loses one for good. */
    public static boolean canRespec(Optional<SoulRank> rank, RefinementData data) {
        return data.allocated() > 0 && earned(rank, data) >= 1;
    }

    public static int capacityBonus(RefinementData data) {
        return data.soul() * CAPACITY_PER_SOUL;
    }

    public static double regenMultiplier(RefinementData data) {
        return 1.0 + data.soul() * REGEN_PER_SOUL;
    }

    /** An ability's cost after Aspect refinement; never below one. */
    public static int cost(int base, RefinementData data) {
        if (base <= 0) {
            return base;
        }
        double cut = Math.min(MAX_COST_CUT, data.aspect() * COST_CUT_PER_ASPECT);
        return Math.max(1, (int) Math.round(base * (1.0 - cut)));
    }

    /** What one point in a stat does, for the Soul Sea. */
    public static String perPoint(RefinementStat stat) {
        return switch (stat) {
            case BODY -> "+1 heart, +3% speed, +0.5 melee damage each";
            case SOUL -> "+15 essence and 10% faster refill each";
            case ASPECT -> "abilities cost 6% less essence each, up to 30%";
        };
    }
}
