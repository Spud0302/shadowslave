package dev.spud.shadowslave.nightmare;

import java.util.Locale;
import java.util.Optional;

/** Development-only durable boundaries available for one-shot process fault injection. */
public enum NightmareCompletionFaultPoint {
    AFTER_TERMINAL_REGISTRY_SAVE,
    AFTER_APPRAISAL_PLAYER_SAVE,
    AFTER_APPRAISAL_REGISTRY_SAVE,
    AFTER_RETURN_PLAYER_SAVE,
    AFTER_RETURN_REGISTRY_SAVE,
    AFTER_TEARDOWN_REGISTRY_SAVE;

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Optional<NightmareCompletionFaultPoint> parse(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(value.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }
}
