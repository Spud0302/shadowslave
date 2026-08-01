package dev.spud.shadowslave.soul;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Arrays;
import java.util.Optional;

/**
 * The Spell-recognised progression stage of a human.
 *
 * <p>This is deliberately separate from {@link SoulRank}. Carrier and Aspirant
 * are not Soul Ranks, Dreamer is the Spell-facing title commonly called
 * Sleeper by humans, and later human titles do not replace the underlying
 * seven-rank soul ladder.</p>
 */
public enum SpellState {
    UNINFECTED("uninfected"),
    CARRIER("carrier"),
    ASPIRANT("aspirant"),
    DREAMER("dreamer"),
    AWAKENED("awakened"),
    MASTER("master"),
    SAINT("saint"),
    SOVEREIGN("sovereign"),
    SPIRIT("spirit"),
    GOD("god");

    /**
     * Reads the current names and the two alpha-1 legacy names so development
     * worlds survive the lore-alignment schema migration.
     */
    public static final Codec<SpellState> CODEC = Codec.STRING.comapFlatMap(
            SpellState::decode,
            SpellState::serializedName
    );

    private final String serializedName;

    SpellState(String serializedName) {
        this.serializedName = serializedName;
    }

    public String serializedName() {
        return serializedName;
    }

    public Optional<SoulRank> requiredSoulRank() {
        return switch (this) {
            case UNINFECTED, CARRIER -> Optional.empty();
            case ASPIRANT, DREAMER -> Optional.of(SoulRank.DORMANT);
            case AWAKENED -> Optional.of(SoulRank.AWAKENED);
            case MASTER -> Optional.of(SoulRank.ASCENDED);
            case SAINT -> Optional.of(SoulRank.TRANSCENDENT);
            case SOVEREIGN -> Optional.of(SoulRank.SUPREME);
            case SPIRIT -> Optional.of(SoulRank.SACRED);
            case GOD -> Optional.of(SoulRank.DIVINE);
        };
    }

    /**
     * Whether this stage is reached only after the First Nightmare appraisal
     * on the Nightmare Spell path. Natural-awakening identity rules remain a
     * separate lore decision and are not inferred here.
     */
    public boolean isAtOrBeyondDreamer() {
        return switch (this) {
            case DREAMER, AWAKENED, MASTER, SAINT, SOVEREIGN, SPIRIT, GOD -> true;
            case UNINFECTED, CARRIER, ASPIRANT -> false;
        };
    }

    private static DataResult<SpellState> decode(String serialized) {
        if ("mundane".equals(serialized)) {
            return DataResult.success(UNINFECTED);
        }
        if ("sleeper".equals(serialized)) {
            return DataResult.success(DREAMER);
        }

        return Arrays.stream(values())
                .filter(value -> value.serializedName.equals(serialized))
                .findFirst()
                .map(DataResult::success)
                .orElseGet(() -> DataResult.error(() -> "Unknown Spell state: " + serialized));
    }
}
