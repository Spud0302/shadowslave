package dev.spud.shadowslave.soul;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Arrays;

/**
 * Soul Rank is separate from {@link SpellState}. The Java foundation records
 * the full ladder now, while only Mundane and Dormant are reachable in the
 * first comparison slice.
 */
public enum SoulRank {
    MUNDANE("mundane"),
    DORMANT("dormant"),
    AWAKENED("awakened"),
    ASCENDED("ascended"),
    TRANSCENDENT("transcendent"),
    SUPREME("supreme"),
    SACRED("sacred"),
    DIVINE("divine");

    public static final Codec<SoulRank> CODEC = Codec.STRING.comapFlatMap(
            serialized -> Arrays.stream(values())
                    .filter(value -> value.serializedName.equals(serialized))
                    .findFirst()
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "Unknown Soul Rank: " + serialized)),
            SoulRank::serializedName
    );

    private final String serializedName;

    SoulRank(String serializedName) {
        this.serializedName = serializedName;
    }

    public String serializedName() {
        return serializedName;
    }
}
