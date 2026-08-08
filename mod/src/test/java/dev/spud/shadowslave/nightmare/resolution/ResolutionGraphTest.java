package dev.spud.shadowslave.nightmare.resolution;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResolutionGraphTest {
    private final ResolutionGraph graph = new ResolutionGraph(
            "watch_unreached",
            List.of("watch_unreached", "watch_reached", "warned", "abandoned"),
            List.of(
                    new ResolutionTransition("watch_unreached", "reach_watch", "watch_reached"),
                    new ResolutionTransition("watch_reached", "light_true_signal", "warned"),
                    new ResolutionTransition("watch_reached", "leave_with_knowledge", "abandoned")
            ),
            Map.of("warned", "settlement_warned", "abandoned", "watch_abandoned")
    );

    @Test
    void objectiveEventIsRejectedUntilItsPrerequisiteStateExists() {
        ResolutionStep rejected = graph.apply(graph.initial(), "light_true_signal");

        assertFalse(rejected.accepted());
        assertEquals("watch_unreached", rejected.state().stateId());
        assertEquals("event_not_accepted_in_current_state", rejected.rejectionReason().orElseThrow());
    }

    @Test
    void nonCombatPathsReachDifferentNamedTerminalResolutions() {
        ResolutionState reached = graph.apply(graph.initial(), "reach_watch").state();
        ResolutionState warned = graph.apply(reached, "light_true_signal").state();
        ResolutionState abandoned = graph.apply(reached, "leave_with_knowledge").state();

        assertEquals("settlement_warned", warned.terminalResolutionId().orElseThrow());
        assertEquals("watch_abandoned", abandoned.terminalResolutionId().orElseThrow());
        assertTrue(warned.isTerminal());
        assertTrue(abandoned.isTerminal());
    }

    @Test
    void terminalScenarioCannotAcceptLaterEvents() {
        ResolutionState reached = graph.apply(graph.initial(), "reach_watch").state();
        ResolutionState terminal = graph.apply(reached, "light_true_signal").state();

        ResolutionStep rejected = graph.apply(terminal, "leave_with_knowledge");
        assertFalse(rejected.accepted());
        assertEquals("scenario_already_terminal", rejected.rejectionReason().orElseThrow());
        assertEquals(terminal, rejected.state());
    }

    @Test
    void ambiguousEventEdgesAreRejectedAtDefinitionTime() {
        assertThrows(IllegalArgumentException.class, () -> new ResolutionGraph(
                "start",
                List.of("start", "a", "b"),
                List.of(
                        new ResolutionTransition("start", "finish", "a"),
                        new ResolutionTransition("start", "finish", "b")
                ),
                Map.of("a", "a_end", "b", "b_end")
        ));
    }

    @Test
    void oneSharedResolutionProducesIndependentChallengerOutcomes() {
        assertEquals(
                ChallengerOutcome.COMPLETED,
                ChallengerOutcomeEvaluator.evaluate(true, true, true,
                        ChallengerOutcomeEvaluator.TechnicalExit.NONE)
        );
        assertEquals(
                ChallengerOutcome.FAILED_DEATH,
                ChallengerOutcomeEvaluator.evaluate(true, false, true,
                        ChallengerOutcomeEvaluator.TechnicalExit.NONE)
        );
        assertEquals(
                ChallengerOutcome.INELIGIBLE_OR_INVALIDATED,
                ChallengerOutcomeEvaluator.evaluate(true, true, false,
                        ChallengerOutcomeEvaluator.TechnicalExit.NONE)
        );
        assertEquals(
                ChallengerOutcome.TECHNICAL_RECOVERY,
                ChallengerOutcomeEvaluator.evaluate(false, true, true,
                        ChallengerOutcomeEvaluator.TechnicalExit.TECHNICAL_RECOVERY)
        );
    }
}
