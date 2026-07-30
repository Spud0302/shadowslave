package dev.spud.shadowslave.soul;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Arrays;

/**
 * The player's relationship with the Nightmare Spell.
 *
 * <p>This is deliberately separate from {@link SoulRank}: becoming a Carrier is
 * not a Soul Rank, and surviving the First Nightmare produces a Sleeper holding
 * a Dormant Aspect.</p>
 */
public enum SpellState {
    MUNDANE("mundane"),
    CARRIER("carrier"),
    SLEEPER("sleeper");

    public static final Codec<SpellState> CODEC = Codec.STRING.comapFlatMap(
            serialized -> Arrays.stream(values())
                    .filter(value -> value.serializedName.equals(serialized))
                    .findFirst()
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "Unknown Spell state: " + serialized)),
            SpellState::serializedName
    );

    private final String serializedName;

    SpellState(String serializedName) {
        this.serializedName = serializedName;
    }

    public String serializedName() {
        return serializedName;
    }
}
