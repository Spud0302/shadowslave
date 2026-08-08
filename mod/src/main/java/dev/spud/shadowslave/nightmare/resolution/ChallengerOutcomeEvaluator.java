package dev.spud.shadowslave.nightmare.resolution;

import java.util.Objects;

/**
 * DESIGN policy for the first reusable outcome slice.
 *
 * <p>CANON supports survival and completion as distinct concerns. Exact edge
 * cases remain UNKNOWN and must be verified before broadening this policy.</p>
 */
public final class ChallengerOutcomeEvaluator {
    private ChallengerOutcomeEvaluator() {
    }

    public static ChallengerOutcome evaluate(
            boolean scenarioTerminal,
            boolean alive,
            boolean eligible,
            TechnicalExit technicalExit
    ) {
        TechnicalExit checkedExit = Objects.requireNonNull(technicalExit, "technicalExit");
        if (checkedExit == TechnicalExit.TECHNICAL_RECOVERY) {
            return ChallengerOutcome.TECHNICAL_RECOVERY;
        }
        if (checkedExit == TechnicalExit.ADMIN_ABORT) {
            return ChallengerOutcome.ADMIN_ABORT;
        }
        if (!alive) {
            return ChallengerOutcome.FAILED_DEATH;
        }
        if (!eligible || !scenarioTerminal) {
            return ChallengerOutcome.INELIGIBLE_OR_INVALIDATED;
        }
        return ChallengerOutcome.COMPLETED;
    }

    public enum TechnicalExit {
        NONE,
        TECHNICAL_RECOVERY,
        ADMIN_ABORT
    }
}
