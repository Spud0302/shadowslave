package dev.spud.shadowslave.nightmare.resolution;

import java.util.Objects;
import java.util.Optional;

/** Persistable logical position in a scenario conflict graph. */
public record ResolutionState(String stateId, Optional<String> terminalResolutionId) {
    public ResolutionState {
        stateId = requireId(stateId, "stateId");
        terminalResolutionId = Objects.requireNonNull(terminalResolutionId, "terminalResolutionId")
                .map(value -> requireId(value, "terminalResolutionId"));
    }

    public boolean isTerminal() {
        return terminalResolutionId.isPresent();
    }

    private static String requireId(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }
}
