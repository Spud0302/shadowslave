package dev.spud.shadowslave.nightmare.resolution;

import java.util.Objects;
import java.util.Optional;

/** Result of offering one world or actor event to the current conflict state. */
public record ResolutionStep(ResolutionState state, boolean accepted, Optional<String> rejectionReason) {
    public ResolutionStep {
        state = Objects.requireNonNull(state, "state");
        rejectionReason = Objects.requireNonNull(rejectionReason, "rejectionReason");
        if (accepted == rejectionReason.isPresent()) {
            throw new IllegalArgumentException("Accepted steps cannot have a rejection reason and rejected steps require one");
        }
    }

    public static ResolutionStep accepted(ResolutionState state) {
        return new ResolutionStep(state, true, Optional.empty());
    }

    public static ResolutionStep rejected(ResolutionState state, String reason) {
        return new ResolutionStep(state, false, Optional.of(Objects.requireNonNull(reason, "reason")));
    }
}
