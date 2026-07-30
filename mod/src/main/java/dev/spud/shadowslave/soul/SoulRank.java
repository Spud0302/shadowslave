package dev.spud.shadowslave.soul;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Arrays;

/**
 * The seven canonical qualities of a soul core.
 *
 * <p>An ordinary human can be described as mundane, but Mundane is not an
 * eighth Rank below Dormant. Players without a ranked core represent that fact
 * with an absent Soul Rank in {@link SoulData}.</p>
 */
public enum SoulRank {
    DORMANT("dormant"),
    AWAKENED("awakened"),
    ASCENDED("ascended"),
    TRANSCENDENT("transcendent"),
    SUPREME("supreme"),
    SACRED("sacred"),
    DIVINE("divine");

    public static final Codec<SoulRank> CODEC = Codec.STRING.comapFlatMap(
            SoulRank::decode,
            SoulRank::serializedName
    );

    private final String serializedName;

    SoulRank(String serializedName) {
        this.serializedName = serializedName;
    }

    public String serializedName() {
        return serializedName;
    }

    public static DataResult<SoulRank> decode(String serialized) {
        return Arrays.stream(values())
                .filter(value -> value.serializedName.equals(serialized))
                .findFirst()
                .map(DataResult::success)
                .orElseGet(() -> DataResult.error(() -> "Unknown Soul Rank: " + serialized));
    }
}
