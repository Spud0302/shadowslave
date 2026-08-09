package dev.spud.shadowslave.nightmare.content;

import dev.spud.shadowslave.nightmare.resolution.ResolutionGraph;
import dev.spud.shadowslave.nightmare.resolution.ResolutionState;
import dev.spud.shadowslave.nightmare.resolution.ResolutionStep;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LanternBelowScenarioDefinitionTest {
    @Test
    void contentProvidesDistinctInvestigationAndRescueScenario() {
        LanternBelowScenarioDefinition.ScenarioContent content = LanternBelowScenarioDefinition.content();

        assertEquals("the_lantern_below", content.id());
        assertEquals("survey_clerks_assistant", content.historicalRoleId());
        assertEquals(5, content.locations().size());
        assertEquals(5, content.characters().size());
        assertEquals(6, content.pressures().size());
        assertEquals(12, content.choices().size());
        assertEquals(5, content.resolutions().size());
        assertEquals(
                Set.of("all_rescued", "injured_rescued", "breach_sealed", "ledger_exposed", "evidence_carried"),
                content.resolutions().keySet()
        );
    }

    @Test
    void everyDeclaredChoiceAppearsInAtLeastOneAcceptedPath() {
        ResolutionGraph graph = LanternBelowScenarioDefinition.resolutionGraph();
        Set<String> reachableEvents = new HashSet<>();
        Set<ResolutionState> frontier = new HashSet<>();
        Set<ResolutionState> visited = new HashSet<>();
        frontier.add(graph.initial());

        while (!frontier.isEmpty()) {
            ResolutionState current = frontier.iterator().next();
            frontier.remove(current);
            if (!visited.add(current) || current.terminalResolutionId().isPresent()) {
                continue;
            }

            for (LanternBelowScenarioDefinition.Choice choice : LanternBelowScenarioDefinition.content().choices()) {
                ResolutionStep step = graph.apply(current, choice.eventId());
                if (step.accepted()) {
                    reachableEvents.add(choice.eventId());
                    frontier.add(step.state());
                }
            }
        }

        Set<String> declaredEvents = new HashSet<>();
        LanternBelowScenarioDefinition.content().choices().forEach(choice -> declaredEvents.add(choice.eventId()));
        assertEquals(declaredEvents, reachableEvents);
    }

    @Test
    void allFiveTerminalResolutionsAreReachableWithoutBossOrKillObjectives() {
        ResolutionGraph graph = LanternBelowScenarioDefinition.resolutionGraph();

        assertTerminal(graph, "all_rescued",
                "inspect_guide_rope", "mark_safe_route", "follow_doubled_chalk", "open_old_vent", "verify_knocking", "guide_all_out");
        assertTerminal(graph, "injured_rescued",
                "question_survivors", "mark_safe_route", "follow_doubled_chalk", "open_old_vent", "verify_knocking", "extract_injured_porter");
        assertTerminal(graph, "breach_sealed",
                "inspect_guide_rope", "recover_shift_ledger", "seal_deep_breach");
        assertTerminal(graph, "ledger_exposed",
                "question_survivors", "recover_shift_ledger", "confront_overseer");
        assertTerminal(graph, "evidence_carried",
                "inspect_guide_rope", "recover_shift_ledger", "carry_evidence_out");

        LanternBelowScenarioDefinition.content().choices().forEach(choice -> {
            String normalized = (choice.eventId() + " " + choice.name()).toLowerCase(java.util.Locale.ROOT);
            assertFalse(normalized.contains("kill"));
            assertFalse(normalized.contains("slay"));
            assertFalse(normalized.contains("boss"));
        });
    }

    @Test
    void rescueRequiresVerifiedRouteAndAirRatherThanOneObjectiveInteraction() {
        ResolutionGraph graph = LanternBelowScenarioDefinition.resolutionGraph();
        ResolutionState initial = graph.initial();

        ResolutionStep prematureRescue = graph.apply(initial, "guide_all_out");
        assertFalse(prematureRescue.accepted());
        assertEquals(initial, prematureRescue.state());

        ResolutionState traced = graph.apply(initial, "inspect_guide_rope").state();
        ResolutionStep stillPremature = graph.apply(traced, "verify_knocking");
        assertFalse(stillPremature.accepted());
        assertEquals(traced, stillPremature.state());
    }

    @Test
    void investigationCanResolveTheConflictWithoutCompletingTheDeepRescue() {
        ResolutionGraph graph = LanternBelowScenarioDefinition.resolutionGraph();

        assertTerminal(graph, "ledger_exposed",
                "question_survivors", "recover_shift_ledger", "confront_overseer");
        assertTerminal(graph, "evidence_carried",
                "question_survivors", "recover_shift_ledger", "carry_evidence_out");
        assertTerminal(graph, "breach_sealed",
                "question_survivors", "recover_shift_ledger", "seal_deep_breach");
    }

    @Test
    void eachEndingProducesDistinctPositiveAppraisalEvidenceWithoutCalculatingVerdict() {
        LanternBelowScenarioDefinition.ScenarioContent content = LanternBelowScenarioDefinition.content();
        Set<String> evidenceShapes = new HashSet<>();

        content.resolutions().values().forEach(resolution -> {
            assertFalse(resolution.evidenceWeights().isEmpty());
            assertTrue(resolution.evidenceWeights().values().stream().allMatch(weight -> weight > 0));
            evidenceShapes.add(resolution.evidenceWeights().entrySet().stream()
                    .sorted(java.util.Map.Entry.comparingByKey())
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .reduce((left, right) -> left + "|" + right)
                    .orElseThrow());
        });

        assertEquals(content.resolutions().size(), evidenceShapes.size());
    }

    private static void assertTerminal(ResolutionGraph graph, String expectedResolution, String... events) {
        ResolutionState state = graph.initial();
        for (String event : events) {
            ResolutionStep step = graph.apply(state, event);
            assertTrue(step.accepted(), "Expected event to be accepted: " + event + " from " + state.stateId());
            state = step.state();
        }
        assertEquals(expectedResolution, state.terminalResolutionId().orElseThrow());
    }
}
