package dev.spud.shadowslave.dreamrealm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * How Nightmare Creatures roam the Dream Realm's regions, read from
 * {@code data/<ns>/dream_realm/roaming.json} (DESIGN). Every {@code interval}
 * ticks each dreamer in a region with {@code creatures} may draw a group, with
 * {@code chance}, unless {@code cap} creatures already prowl within
 * {@code cap_range} blocks of them. A group comes {@code min_distance} to
 * {@code max_distance} blocks away, never within {@code candle_ward} blocks of a
 * lit Dream Candle. The {@code shadowslaveDreamCreatures} game rule turns it off.
 */
public record RoamingSettings(int interval, float chance, int cap, int capRange, int minDistance, int maxDistance, int candleWard) {
    public static final RoamingSettings DEFAULT = new RoamingSettings(200, 0.35F, 6, 64, 24, 48, 32);

    public static final Codec<RoamingSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(20, 24000).optionalFieldOf("interval", DEFAULT.interval).forGetter(RoamingSettings::interval),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("chance", DEFAULT.chance).forGetter(RoamingSettings::chance),
            Codec.intRange(0, 64).optionalFieldOf("cap", DEFAULT.cap).forGetter(RoamingSettings::cap),
            Codec.intRange(8, 256).optionalFieldOf("cap_range", DEFAULT.capRange).forGetter(RoamingSettings::capRange),
            Codec.intRange(4, 128).optionalFieldOf("min_distance", DEFAULT.minDistance).forGetter(RoamingSettings::minDistance),
            Codec.intRange(8, 192).optionalFieldOf("max_distance", DEFAULT.maxDistance).forGetter(RoamingSettings::maxDistance),
            Codec.intRange(0, 256).optionalFieldOf("candle_ward", DEFAULT.candleWard).forGetter(RoamingSettings::candleWard)
    ).apply(instance, RoamingSettings::new));
}
