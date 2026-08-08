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

class HollowTreatyScenarioDefinitionTest {
    @Test
    void contentProvidesACompleteSocialConflictScenario() {
        HollowTreatyScenarioDefinition.ScenarioContent content = HollowTreatyScenarioDefinition.content();

        assertEquals("the_hollow_treaty", content.id());
        assertEquals("hostage_interpreter", content.historicalRoleId());
        assertEquals(5, content.locations().size());
        assertEquals(5, content.characters().size());
        assertEquals(6, content.pressures().size());
        assertEquals(12, content.choices().size());
        assertEquals(5, content.resolutions().size());
        assertEquals(
                Set.of("treaty_restored", "passage_brokered", "brittle_peace", "accuser_broken", "truth_buried"),
                content.resolutions().keySet()
        );
    }

    @Test
    void everyDeclaredChoiceAppearsInAtLeastOneAcceptedPath() {
        ResolutionGraph graph = HollowTreatyScenarioDefinition.resolutionGraph();
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

            for (HollowTreatyScenarioDefinition.Choice choice : HollowTreatyScenarioDefinition.content().choices()) {
                ResolutionStep step = graph.apply(current, choice.eventId());
                if (step.accepted()) {
                    reachableEvents.add(choice.eventId());
                    frontier.add(step.state());
                }
            }
        }

        Set<String> declaredEvents = new HashSet<>();
        HollowTreatyScenarioDefinition.content().choices().forEach(choice -> declaredEvents.add(choice.eventId()));
        assertEquals(declaredEvents, reachableEvents);
    }

    @Test
    void scenarioSupportsFiveDistinctTerminalSolutions() {
        ResolutionGraph graph = HollowTreatyScenarioDefinition.resolutionGraph();

        assertTerminal(graph, "treaty_restored", "search_archive", "question_page", "convene_council", "expose_forgery");
        assertTerminal(graph, "passage_brokered", "prepare_refugees", "negotiate_passage");
        assertTerminal(graph, "brittle_peace", "plant_false_copy", "present_false_copy");
        assertTerminal(graph, "accuser_broken", "challenge_accuser", "expose_blackmail");
        assertTerminal(graph, "truth_buried", "search_archive", "burn_ledger");
    }

    @Test
    void scenarioDoesNotRequireCreatureOrBossKillEvents() {
        HollowTreatyScenarioDefinition.ScenarioContent content = HollowTreatyScenarioDefinition.content();

        assertTrue(content.choices().stream().noneMatch(choice -> {
            String id = choice.eventId().toLowerCase(java.util.Locale.ROOT);
            return id.contains("kill") || id.contains("slay") || id.contains("boss");
        }));

        ResolutionGraph graph = HollowTreatyScenarioDefinition.resolutionGraph();
        assertTrue(graph.apply(graph.initial(), "prepare_refugees").accepted());
        assertTerminal(graph, "passage_brokered", "prepare_refugees", "negotiate_passage");
    }

    @Test
    void decisiveClaimsRequireTheirAuthoredPreparation() {
        ResolutionGraph graph = HollowTreatyScenarioDefinition.resolutionGraph();
        ResolutionState initial = graph.initial();

        ResolutionStep prematureForgery = graph.apply(initial, "expose_forgery");
        ResolutionStep prematurePassage = graph.apply(initial, "negotiate_passage");
        ResolutionStep prematureBlackmail = graph.apply(initial, "expose_blackmail");

        assertFalse(prematureForgery.accepted());
        assertFalse(prematurePassage.accepted());
        assertFalse(prematureBlackmail.accepted());
        assertEquals(initial, prematureForgery.state());
        assertEquals(initial, prematurePassage.state());
        assertEquals(initial, prematureBlackmail.state());
        assertEquals("event_not_accepted_in_current_state", prematureForgery.rejectionReason().orElseThrow());
    }

    @Test
    void eachEndingProducesDistinctPositiveAppraisalEvidence() {
        HollowTreatyScenarioDefinition.ScenarioContent content = HollowTreatyScenarioDefinition.content();
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

    @Test
    void morallyDifferentOutcomesCanAllResolveTheConflict() {
        ResolutionGraph graph = HollowTreatyScenarioDefinition.resolutionGraph();

        assertTerminal(graph, "treaty_restored", "search_archive", "question_page", "convene_council", "expose_forgery");
        assertTerminal(graph, "brittle_peace", "plant_false_copy", "present_false_copy");
        assertTerminal(graph, "truth_buried", "search_archive", "burn_ledger");
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
