package dev.spud.shadowslave.nightmare.resolution;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * DESIGN: deterministic scenario conflict graph. Canon constrains Nightmare
 * structure but does not prescribe this data model or transition algorithm.
 */
public final class ResolutionGraph {
    private final String initialState;
    private final Set<String> states;
    private final Map<EdgeKey, ResolutionTransition> transitions;
    private final Map<String, String> terminalResolutionByState;

    public ResolutionGraph(
            String initialState,
            Collection<String> states,
            Collection<ResolutionTransition> transitions,
            Map<String, String> terminalResolutionByState
    ) {
        this.initialState = requireId(initialState, "initialState");
        this.states = Set.copyOf(new LinkedHashSet<>(Objects.requireNonNull(states, "states")));
        if (!this.states.contains(this.initialState)) {
            throw new IllegalArgumentException("Initial state must exist in graph states");
        }

        Map<EdgeKey, ResolutionTransition> indexed = new LinkedHashMap<>();
        for (ResolutionTransition transition : Objects.requireNonNull(transitions, "transitions")) {
            if (!this.states.contains(transition.fromState()) || !this.states.contains(transition.toState())) {
                throw new IllegalArgumentException("Transition references an unknown state: " + transition);
            }
            EdgeKey key = new EdgeKey(transition.fromState(), transition.eventId());
            if (indexed.putIfAbsent(key, transition) != null) {
                throw new IllegalArgumentException("Ambiguous transition for " + key);
            }
        }
        this.transitions = Map.copyOf(indexed);

        Map<String, String> terminals = new LinkedHashMap<>();
        Objects.requireNonNull(terminalResolutionByState, "terminalResolutionByState").forEach((state, resolution) -> {
            String checkedState = requireId(state, "terminal state");
            String checkedResolution = requireId(resolution, "resolution id");
            if (!this.states.contains(checkedState)) {
                throw new IllegalArgumentException("Terminal resolution references unknown state " + checkedState);
            }
            terminals.put(checkedState, checkedResolution);
        });
        this.terminalResolutionByState = Map.copyOf(terminals);
    }

    public ResolutionState initial() {
        return new ResolutionState(initialState, Optional.empty());
    }

    public ResolutionStep apply(ResolutionState current, String eventId) {
        ResolutionState checkedCurrent = Objects.requireNonNull(current, "current");
        String checkedEvent = requireId(eventId, "eventId");
        if (!states.contains(checkedCurrent.stateId())) {
            throw new IllegalArgumentException("State does not belong to this graph: " + checkedCurrent.stateId());
        }
        if (checkedCurrent.terminalResolutionId().isPresent()) {
            return ResolutionStep.rejected(checkedCurrent, "scenario_already_terminal");
        }

        ResolutionTransition transition = transitions.get(new EdgeKey(checkedCurrent.stateId(), checkedEvent));
        if (transition == null) {
            return ResolutionStep.rejected(checkedCurrent, "event_not_accepted_in_current_state");
        }

        Optional<String> terminal = Optional.ofNullable(terminalResolutionByState.get(transition.toState()));
        return ResolutionStep.accepted(new ResolutionState(transition.toState(), terminal));
    }

    public boolean isTerminalState(String stateId) {
        return terminalResolutionByState.containsKey(requireId(stateId, "stateId"));
    }

    private static String requireId(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }

    private record EdgeKey(String stateId, String eventId) {
    }
}
