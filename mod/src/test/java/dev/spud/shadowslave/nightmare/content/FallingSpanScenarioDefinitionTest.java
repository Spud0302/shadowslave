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

class FallingSpanScenarioDefinitionTest {
    @Test
    void contentProvidesDistinctNonBossScenarioModule() {
        FallingSpanScenarioDefinition.ScenarioContent content = FallingSpanScenarioDefinition.content();

        assertEquals("the_falling_span", content.id());
        assertEquals("span_ward_runner", content.historicalRoleId());
        assertEquals(5, content.locations().size());
        assertEquals(5, content.characters().size());
        assertEquals(5, content.pressures().size());
        assertEquals(11, content.events().size());
        assertEquals(5, content.resolutions().size());
        assertTrue(content.events().stream().noneMatch(event ->
                event.eventId().contains("kill") || event.eventId().contains("slay") || event.eventId().contains("boss")));
        assertTrue(content.premise().contains("no authority to command everyone"));
    }

    @Test
    void everyDeclaredEventAppearsInAtLeastOneAcceptedPath() {
        ResolutionGraph graph = FallingSpanScenarioDefinition.resolutionGraph();
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

            for (FallingSpanScenarioDefinition.ScenarioEvent event : FallingSpanScenarioDefinition.content().events()) {
                ResolutionStep step = graph.apply(current, event.eventId());
                if (step.accepted()) {
                    reachableEvents.add(event.eventId());
                    frontier.add(step.state());
                }
            }
        }

        Set<String> declaredEvents = new HashSet<>();
        FallingSpanScenarioDefinition.content().events().forEach(event -> declaredEvents.add(event.eventId()));
        assertEquals(declaredEvents, reachableEvents);
    }

    @Test
    void scenarioSupportsFiveMateriallyDifferentTerminalSolutions() {
        ResolutionGraph graph = FallingSpanScenarioDefinition.resolutionGraph();

        assertTerminal(graph, "last_crossing", "inspect_anchors", "organize_crossing", "clear_bridge", "cut_span");
        assertTerminal(graph, "path_below", "scout_lower_path", "guide_lower_path");
        assertTerminal(graph, "road_denied", "cut_span");
        assertTerminal(graph, "mountain_decides", "delay_pursuers", "prepare_rockfall", "storm_breaks_cliff");
        assertTerminal(graph, "passage_bargained", "open_parley", "accept_passage_terms");
    }

    @Test
    void identicalActionCanMeanDifferentOutcomeAfterPriorPreparation() {
        ResolutionGraph graph = FallingSpanScenarioDefinition.resolutionGraph();

        assertTerminal(graph, "road_denied", "cut_span");
        assertTerminal(graph, "last_crossing", "inspect_anchors", "organize_crossing", "clear_bridge", "cut_span");
    }

    @Test
    void worldCanCompletePreparedResolutionWithoutPlayerFinalTrigger() {
        ResolutionGraph graph = FallingSpanScenarioDefinition.resolutionGraph();
        ResolutionState state = graph.initial();

        state = accept(graph, state, "delay_pursuers");
        state = accept(graph, state, "prepare_rockfall");

        FallingSpanScenarioDefinition.ScenarioEvent worldEvent = FallingSpanScenarioDefinition.content().events().stream()
                .filter(event -> event.eventId().equals("storm_breaks_cliff"))
                .findFirst()
                .orElseThrow();
        assertEquals(FallingSpanScenarioDefinition.EventActor.WORLD, worldEvent.actor());

        ResolutionStep terminal = graph.apply(state, worldEvent.eventId());
        assertTrue(terminal.accepted());
        assertEquals("mountain_decides", terminal.state().terminalResolutionId().orElseThrow());
    }

    @Test
    void npcCanCompleteNegotiatedResolutionWithoutCombat() {
        FallingSpanScenarioDefinition.ScenarioEvent npcEvent = FallingSpanScenarioDefinition.content().events().stream()
                .filter(event -> event.eventId().equals("accept_passage_terms"))
                .findFirst()
                .orElseThrow();
        assertEquals(FallingSpanScenarioDefinition.EventActor.NPC, npcEvent.actor());

        assertTerminal(FallingSpanScenarioDefinition.resolutionGraph(),
                "passage_bargained", "open_parley", "accept_passage_terms");
    }

    @Test
    void rushingPreparedWorldOutcomeFailsClosed() {
        ResolutionGraph graph = FallingSpanScenarioDefinition.resolutionGraph();
        ResolutionState initial = graph.initial();

        ResolutionStep premature = graph.apply(initial, "storm_breaks_cliff");

        assertFalse(premature.accepted());
        assertEquals(initial, premature.state());
        assertEquals("event_not_accepted_in_current_state", premature.rejectionReason().orElseThrow());
    }

    @Test
    void eachEndingHasDistinctPositiveAppraisalEvidenceWithoutScoringClaim() {
        FallingSpanScenarioDefinition.ScenarioContent content = FallingSpanScenarioDefinition.content();
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

    private static ResolutionState accept(ResolutionGraph graph, ResolutionState state, String event) {
        ResolutionStep step = graph.apply(state, event);
        assertTrue(step.accepted(), () -> "Expected event to be accepted: " + event);
        return step.state();
    }

    private static void assertTerminal(ResolutionGraph graph, String expectedResolution, String... events) {
        ResolutionState state = graph.initial();
        for (String event : events) {
            state = accept(graph, state, event);
        }
        assertEquals(expectedResolution, state.terminalResolutionId().orElseThrow());
    }
}
