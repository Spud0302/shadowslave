package dev.spud.shadowslave.soul.refinement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Refinement points a player has placed in each stat, and how many times they
 * have respecced. Points earned come from Soul Rank (see {@link RefinementRules});
 * each respec permanently costs one of them.
 */
public record RefinementData(int body, int soul, int aspect, int respecs) {
    public static final MapCodec<RefinementData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.optionalFieldOf("body", 0).forGetter(RefinementData::body),
            Codec.INT.optionalFieldOf("soul", 0).forGetter(RefinementData::soul),
            Codec.INT.optionalFieldOf("aspect", 0).forGetter(RefinementData::aspect),
            Codec.INT.optionalFieldOf("respecs", 0).forGetter(RefinementData::respecs)
    ).apply(instance, RefinementData::new));

    public RefinementData {
        body = Math.max(0, body);
        soul = Math.max(0, soul);
        aspect = Math.max(0, aspect);
        respecs = Math.max(0, respecs);
    }

    public static RefinementData empty() {
        return new RefinementData(0, 0, 0, 0);
    }

    public int allocated() {
        return body + soul + aspect;
    }

    public int points(RefinementStat stat) {
        return switch (stat) {
            case BODY -> body;
            case SOUL -> soul;
            case ASPECT -> aspect;
        };
    }

    public RefinementData plus(RefinementStat stat) {
        return switch (stat) {
            case BODY -> new RefinementData(body + 1, soul, aspect, respecs);
            case SOUL -> new RefinementData(body, soul + 1, aspect, respecs);
            case ASPECT -> new RefinementData(body, soul, aspect + 1, respecs);
        };
    }

    /** All points back, one respec more. */
    public RefinementData respecced() {
        return new RefinementData(0, 0, 0, respecs + 1);
    }
}
