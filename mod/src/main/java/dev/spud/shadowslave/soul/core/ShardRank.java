package dev.spud.shadowslave.soul.core;

import com.mojang.serialization.Codec;
import dev.spud.shadowslave.soul.SoulRank;
import net.minecraft.util.StringRepresentable;

import java.util.Optional;

/**
 * The creature rank a soul shard carries (CANON: shards keep their source's
 * rank, ch. 9 and 204). Only the four ranks the first slice can drop are
 * listed. {@code value} is how much a shard fills a core of the same rank, in
 * saturation units (DESIGN).
 */
public enum ShardRank implements StringRepresentable {
    DORMANT("dormant", 4),
    AWAKENED("awakened", 20),
    FALLEN("fallen", 80),
    CORRUPTED("corrupted", 240);

    public static final Codec<ShardRank> CODEC = StringRepresentable.fromEnum(ShardRank::values);

    private final String serializedName;
    private final int value;

    ShardRank(String serializedName, int value) {
        this.serializedName = serializedName;
        this.value = value;
    }

    public int value() {
        return value;
    }

    /** Position on the shared ladder: a Fallen creature stands where an Ascended human does. */
    public int tier() {
        return ordinal();
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    /** The shard a human of this Soul Rank leaves behind, for ranks the slice can drop. */
    public static Optional<ShardRank> ofSoulRank(SoulRank rank) {
        return switch (rank) {
            case DORMANT -> Optional.of(DORMANT);
            case AWAKENED -> Optional.of(AWAKENED);
            case ASCENDED -> Optional.of(FALLEN);
            case TRANSCENDENT -> Optional.of(CORRUPTED);
            default -> Optional.empty();
        };
    }
}
