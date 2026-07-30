package dev.spud.shadowslave.soul;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Arrays;

/** The route by which a person enters and advances along the soul-rank ladder. */
public enum AwakeningPath {
    UNDECIDED("undecided"),
    NIGHTMARE_SPELL("nightmare_spell"),
    NATURAL("natural");

    public static final Codec<AwakeningPath> CODEC = Codec.STRING.comapFlatMap(
            serialized -> Arrays.stream(values())
                    .filter(value -> value.serializedName.equals(serialized))
                    .findFirst()
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "Unknown awakening path: " + serialized)),
            AwakeningPath::serializedName
    );

    private final String serializedName;

    AwakeningPath(String serializedName) {
        this.serializedName = serializedName;
    }

    public String serializedName() {
        return serializedName;
    }
}
