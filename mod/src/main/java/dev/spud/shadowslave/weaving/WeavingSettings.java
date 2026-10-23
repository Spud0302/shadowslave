package dev.spud.shadowslave.weaving;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Tuning shared by every branch, read from {@code data/<ns>/weaving/rules.json}
 * (DESIGN). {@code xpPerPoint} is the Weaving XP each point costs to earn;
 * the first weave of a pattern gives {@code firstWeaveMultiplier} times its XP
 * so experimenting pays; a failed attempt uses up {@code failureConsumes}
 * items from the table (0 keeps trial and error free).
 */
public record WeavingSettings(int xpPerPoint, int firstWeaveMultiplier, int failureConsumes) {
    public static final WeavingSettings DEFAULT = new WeavingSettings(100, 5, 0);

    public static final Codec<WeavingSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, 1_000_000).optionalFieldOf("xp_per_point", DEFAULT.xpPerPoint).forGetter(WeavingSettings::xpPerPoint),
            Codec.intRange(1, 100).optionalFieldOf("first_weave_multiplier", DEFAULT.firstWeaveMultiplier).forGetter(WeavingSettings::firstWeaveMultiplier),
            Codec.intRange(0, WeavingRules.MATERIAL_SLOTS * 64).optionalFieldOf("failure_consumes", DEFAULT.failureConsumes).forGetter(WeavingSettings::failureConsumes)
    ).apply(instance, WeavingSettings::new));
}
