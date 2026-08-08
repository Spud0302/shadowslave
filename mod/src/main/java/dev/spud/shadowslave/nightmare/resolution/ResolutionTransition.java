package dev.spud.shadowslave.nightmare.resolution;

import java.util.Objects;

/** One accepted scenario event edge between named conflict states. */
public record ResolutionTransition(String fromState, String eventId, String toState) {
    public ResolutionTransition {
        fromState = requireId(fromState, "fromState");
        eventId = requireId(eventId, "eventId");
        toState = requireId(toState, "toState");
    }

    private static String requireId(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }
}
