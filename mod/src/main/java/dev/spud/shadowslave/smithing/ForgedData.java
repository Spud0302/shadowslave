package dev.spud.shadowslave.smithing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Stamped on a finished piece: its grade, the score behind it, and the
 * smith's mark. Pieces made before Smithing existed are given a Fair grade
 * with score -1. {@code extraThreads} are the threads a master smith's hand
 * adds beyond what the grade gives (a Forging perk).
 */
public record ForgedData(Quality quality, int score, String smith, int extraThreads) {
    public static final Codec<ForgedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Quality.CODEC.fieldOf("quality").forGetter(ForgedData::quality),
            Codec.INT.optionalFieldOf("score", -1).forGetter(ForgedData::score),
            Codec.STRING.optionalFieldOf("smith", "").forGetter(ForgedData::smith),
            Codec.intRange(0, 8).optionalFieldOf("extra_threads", 0).forGetter(ForgedData::extraThreads)
    ).apply(instance, ForgedData::new));

    public ForgedData(Quality quality, int score, String smith) {
        this(quality, score, smith, 0);
    }

    /** What an item woven before Smithing becomes: Fair, its maker kept. */
    public static ForgedData legacy(String smith) {
        return new ForgedData(Quality.FAIR, -1, smith);
    }

    public int threads() {
        return quality.threads() + extraThreads;
    }
}
