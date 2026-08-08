package dev.spud.shadowslave.nightmare;

import java.util.Objects;
import java.util.UUID;

/** Structured server-log evidence for Issue #34 physical restart verification. */
public record NightmareCompletionRecoveryEvidence(
        UUID nightmareId,
        UUID playerId,
        boolean appraisalApplied,
        boolean activePresent,
        boolean playerInNightmare
) {
    public static final String PREFIX = "COMPLETION RECOVERY EVIDENCE";

    public NightmareCompletionRecoveryEvidence {
        nightmareId = Objects.requireNonNull(nightmareId, "nightmareId");
        playerId = Objects.requireNonNull(playerId, "playerId");
    }

    public boolean recovered() {
        return appraisalApplied && !activePresent && !playerInNightmare;
    }

    public String logMarker() {
        return PREFIX
                + " nightmare=" + nightmareId
                + " player_uuid=" + playerId
                + " appraisal_applied=" + appraisalApplied
                + " active_present=" + activePresent
                + " in_nightmare=" + playerInNightmare;
    }
}
