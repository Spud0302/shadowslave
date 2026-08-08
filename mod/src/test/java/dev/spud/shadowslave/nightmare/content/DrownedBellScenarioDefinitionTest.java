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

class DrownedBellScenarioDefinitionTest {
    @Test
    void contentProvidesACompleteSecondScenarioModule() {
        DrownedBellScenarioDefinition.ScenarioContent content = DrownedBellScenarioDefinition.content();

        assertEquals("the_drowned_bell", content.id());
        assertEquals("bell_keepers_apprentice", content.historicalRoleId());
        assertEquals(4, content.locations().size());
        assertEquals(4, content.characters().size());
        assertEquals("Dormant Monster", content.creature().classification());
        assertEquals(8, content.choices().size());
        assertEquals(4, content.resolutions().size());

        Set<String> resolutionIds = content.resolutions().keySet();
        assertEquals(Set.of("tower_held", "villagers_evacuated", "flood_diverted", "creature_buried"), resolutionIds);
    }

    @Test
    void everyDeclaredChoiceAppearsInAtLeastOneAcceptedPath() {
        ResolutionGraph graph = DrownedBellScenarioDefinition.resolutionGraph();
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

            for (DrownedBellScenarioDefinition.Choice choice : DrownedBellScenarioDefinition.content().choices()) {
                ResolutionStep step = graph.apply(current, choice.eventId());
                if (step.accepted()) {
                    reachableEvents.add(choice.eventId());
                    frontier.add(step.state());
                }
            }
        }

        Set<String> declaredEvents = new HashSet<>();
        DrownedBellScenarioDefinition.content().choices().forEach(choice -> declaredEvents.add(choice.eventId()));
        assertEquals(declaredEvents, reachableEvents);
    }

    @Test
    void scenarioSupportsFourDistinctTerminalSolutions() {
        ResolutionGraph graph = DrownedBellScenarioDefinition.resolutionGraph();

        assertTerminal(graph, "tower_held", "repair_bell", "ring_bell");
        assertTerminal(graph, "villagers_evacuated", "open_quarry_route", "guide_evacuation");
        assertTerminal(graph, "flood_diverted", "reach_floodgate", "divert_flood");
        assertTerminal(graph, "creature_buried", "repair_bell", "lure_creature", "collapse_quarry");
    }

    @Test
    void rushingAnUnavailableSolutionIsRejectedWithoutChangingState() {
        ResolutionGraph graph = DrownedBellScenarioDefinition.resolutionGraph();
        ResolutionState initial = graph.initial();

        ResolutionStep premature = graph.apply(initial, "collapse_quarry");

        assertFalse(premature.accepted());
        assertEquals(initial, premature.state());
        assertEquals("event_not_accepted_in_current_state", premature.rejectionReason().orElseThrow());
    }

    @Test
    void eachEndingProducesDistinctPositiveAppraisalEvidence() {
        DrownedBellScenarioDefinition.ScenarioContent content = DrownedBellScenarioDefinition.content();
        Set<String> canonicalEvidenceShapes = new HashSet<>();

        content.resolutions().values().forEach(resolution -> {
            assertFalse(resolution.evidenceWeights().isEmpty());
            assertTrue(resolution.evidenceWeights().values().stream().allMatch(weight -> weight > 0));
            canonicalEvidenceShapes.add(resolution.evidenceWeights().entrySet().stream()
                    .sorted(java.util.Map.Entry.comparingByKey())
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .reduce((left, right) -> left + "|" + right)
                    .orElseThrow());
        });

        assertEquals(content.resolutions().size(), canonicalEvidenceShapes.size());
    }

    private static void assertTerminal(ResolutionGraph graph, String expectedResolution, String... events) {
        ResolutionState state = graph.initial();
        for (String event : events) {
            ResolutionStep step = graph.apply(state, event);
            assertTrue(step.accepted(), () -> "Expected event to be accepted: " + event);
            state = step.state();
        }
        assertEquals(expectedResolution, state.terminalResolutionId().orElseThrow());
    }
}
