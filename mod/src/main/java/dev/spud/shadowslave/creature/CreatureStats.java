package dev.spud.shadowslave.creature;

/**
 * Health, damage and armour of a creature at one Rank and Class (DESIGN).
 * Each creature declares its stats at its own native Rank and Class; a copy
 * at another Rank or Class, like a Tyrant's lesser spawn, is scaled from them.
 * Every Rank step multiplies health by {@link #HEALTH_PER_RANK} and damage by
 * {@link #DAMAGE_PER_RANK} and adds {@link #ARMOUR_PER_RANK}; every core
 * adds a smaller share on top, so an Awakened Tyrant still outlasts an
 * Awakened Beast.
 */
public record CreatureStats(double health, double damage, double armour, double speed) {
    public static final double HEALTH_PER_RANK = 2.0;
    public static final double DAMAGE_PER_RANK = 1.6;
    public static final double ARMOUR_PER_RANK = 4.0;
    public static final double HEALTH_PER_CORE = 0.35;
    public static final double DAMAGE_PER_CORE = 0.15;

    public CreatureStats {
        if (health <= 0 || damage < 0 || armour < 0 || speed <= 0) {
            throw new IllegalArgumentException("Creature stats must be positive");
        }
    }

    /** These stats, declared for {@code fromRank} and {@code fromClass}, moved to another Rank and Class. */
    public CreatureStats scaled(CreatureRank fromRank, CreatureClass fromClass, CreatureRank toRank, CreatureClass toClass) {
        int ranks = toRank.ordinal() - fromRank.ordinal();
        int cores = toClass.cores() - fromClass.cores();
        double healthScale = Math.pow(HEALTH_PER_RANK, ranks) * Math.max(0.2, 1.0 + HEALTH_PER_CORE * cores);
        double damageScale = Math.pow(DAMAGE_PER_RANK, ranks) * Math.max(0.2, 1.0 + DAMAGE_PER_CORE * cores);
        return new CreatureStats(
                Math.max(1.0, Math.round(health * healthScale)),
                Math.round(damage * damageScale * 2.0) / 2.0,
                Math.max(0.0, Math.min(30.0, armour + ARMOUR_PER_RANK * ranks)),
                speed);
    }
}
