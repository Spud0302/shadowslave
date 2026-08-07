package dev.spud.shadowslave.soul.identity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Locale;

/**
 * Lore-facing classification for abilities belonging to one Aspect.
 * Aspect Legacies remain a separate system and are deliberately not represented here.
 */
public enum AspectAbilityKind {
    INNATE,
    RANK_GRANTED,
    /**
     * Compatibility-only classification for legacy saves whose single ability_id did not record
     * whether the ability was innate or rank-granted. New content must not create this kind.
     */
    LEGACY_UNCLASSIFIED;

    public static final Codec<AspectAbilityKind> CODEC = Codec.STRING.comapFlatMap(
            AspectAbilityKind::decode,
            AspectAbilityKind::serializedName
    );

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    private static DataResult<AspectAbilityKind> decode(String value) {
        try {
            return DataResult.success(valueOf(value.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException exception) {
            return DataResult.error(() -> "Unknown Aspect ability kind: " + value);
        }
    }
}
