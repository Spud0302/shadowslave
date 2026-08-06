package dev.spud.shadowslave.nightmare;

import java.util.Locale;

/**
 * Durable milestones for a successful Nightmare completion.
 *
 * <p>The record is intentionally retained after teardown. Player attachment
 * data and overworld SavedData can reach disk in either order, so restart
 * recovery must reconcile the durable receipt against the player's actual
 * Soul state instead of assuming one save order.</p>
 */
public enum NightmareCompletionPhase {
    TERMINAL_RESOLUTION_RECORDED,
    APPRAISAL_COMMITTED,
    RETURN_COMMITTED,
    TEARDOWN_COMMITTED;

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static NightmareCompletionPhase parse(String serialized) {
        for (NightmareCompletionPhase value : values()) {
            if (value.serializedName().equals(serialized)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown Nightmare completion phase: " + serialized);
    }
}
