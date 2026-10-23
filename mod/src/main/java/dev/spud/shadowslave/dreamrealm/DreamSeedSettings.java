package dev.spud.shadowslave.dreamrealm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * Tuning for natural Nightmare Seeds, read from {@code data/<ns>/dream_realm/seeds.json}
 * (DESIGN). Every {@code spawn_interval} ticks each Awakened player in the
 * Dream Realm has a {@code spawn_chance} of a Seed taking root
 * {@code min_distance} to {@code max_distance} blocks away, if fewer than
 * {@code max_near} Seeds already grow within {@code near_range} and none
 * within {@code spacing}. A Seed blooms after {@code growth_days} in-game
 * days at normal growth speed. A lit Dream Candle within {@code ward_radius}
 * Overworld blocks of where its Gate would open wards it. Cleansing a Seed
 * rolls its region's loot {@code cleanse_rolls[stage]} times, so the
 * earlier, the better.
 */
public record DreamSeedSettings(int spawnInterval, float spawnChance, int minDistance, int maxDistance, int spacing,
                                int maxNear, int nearRange, float growthDays, int wardRadius, List<Integer> cleanseRolls) {
    public static final int STAGES = 4;
    public static final DreamSeedSettings DEFAULT = new DreamSeedSettings(1200, 0.35F, 24, 64, 64, 2, 160, 3.0F, 48,
            List.of(3, 2, 2, 1));

    public static final Codec<DreamSeedSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(20, 24000 * 10).optionalFieldOf("spawn_interval", DEFAULT.spawnInterval).forGetter(DreamSeedSettings::spawnInterval),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("spawn_chance", DEFAULT.spawnChance).forGetter(DreamSeedSettings::spawnChance),
            Codec.intRange(0, 512).optionalFieldOf("min_distance", DEFAULT.minDistance).forGetter(DreamSeedSettings::minDistance),
            Codec.intRange(1, 512).optionalFieldOf("max_distance", DEFAULT.maxDistance).forGetter(DreamSeedSettings::maxDistance),
            Codec.intRange(0, 1024).optionalFieldOf("spacing", DEFAULT.spacing).forGetter(DreamSeedSettings::spacing),
            Codec.intRange(0, 64).optionalFieldOf("max_near", DEFAULT.maxNear).forGetter(DreamSeedSettings::maxNear),
            Codec.intRange(1, 4096).optionalFieldOf("near_range", DEFAULT.nearRange).forGetter(DreamSeedSettings::nearRange),
            Codec.floatRange(0.01F, 1000.0F).optionalFieldOf("growth_days", DEFAULT.growthDays).forGetter(DreamSeedSettings::growthDays),
            Codec.intRange(0, 1024).optionalFieldOf("ward_radius", DEFAULT.wardRadius).forGetter(DreamSeedSettings::wardRadius),
            Codec.intRange(0, 64).listOf(STAGES, STAGES).optionalFieldOf("cleanse_rolls", DEFAULT.cleanseRolls).forGetter(DreamSeedSettings::cleanseRolls)
    ).apply(instance, DreamSeedSettings::new));

    /** Growth, in ticks at 100% speed, from a fresh Seed to its bloom. */
    public long growthTicks() {
        return Math.max(1L, (long) (growthDays * 24000L));
    }

    /** The visible stage, 0 (a fresh Seed) to 3 (about to bloom), for a growth of {@code grown} ticks. */
    public int stage(long grown) {
        return (int) Math.max(0, Math.min(STAGES - 1, grown * STAGES / growthTicks()));
    }

    /** Loot rolls for cleansing a Seed at this stage. */
    public int cleanseRolls(int stage) {
        return cleanseRolls.get(Math.max(0, Math.min(STAGES - 1, stage)));
    }
}
