package dev.spud.shadowslave.creature;

import dev.spud.shadowslave.soul.core.ShardRank;
import net.minecraft.util.StringRepresentable;

import java.util.Optional;

/**
 * The quality of a Nightmare Creature's soul core. Creature ranks descend
 * toward profanity, from Dormant to Unholy (CANON, ch. 9). Rank sets a
 * creature's health, damage and armour and the rank of the shards it leaves.
 */
public enum CreatureRank implements StringRepresentable {
    DORMANT("dormant", "Dormant"),
    AWAKENED("awakened", "Awakened"),
    FALLEN("fallen", "Fallen"),
    CORRUPTED("corrupted", "Corrupted"),
    GREAT("great", "Great"),
    CURSED("cursed", "Cursed"),
    UNHOLY("unholy", "Unholy");

    public static final com.mojang.serialization.Codec<CreatureRank> CODEC = StringRepresentable.fromEnum(CreatureRank::values);

    private final String serializedName;
    private final String displayName;

    CreatureRank(String serializedName, String displayName) {
        this.serializedName = serializedName;
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    /** The shard rank its cores break into, for the ranks shards exist for so far. */
    public Optional<ShardRank> shard() {
        return switch (this) {
            case DORMANT -> Optional.of(ShardRank.DORMANT);
            case AWAKENED -> Optional.of(ShardRank.AWAKENED);
            case FALLEN -> Optional.of(ShardRank.FALLEN);
            default -> Optional.of(ShardRank.CORRUPTED);
        };
    }

    /** One rank weaker, never below Dormant. */
    public CreatureRank lower() {
        return this == DORMANT ? DORMANT : values()[ordinal() - 1];
    }

    public static CreatureRank of(ShardRank shard) {
        return values()[shard.tier()];
    }

    public static CreatureRank byOrdinal(int ordinal) {
        CreatureRank[] values = values();
        return values[Math.max(0, Math.min(values.length - 1, ordinal))];
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
