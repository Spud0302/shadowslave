package dev.spud.shadowslave.nightmare.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class NightmareInvestigationOutcomeSummaryCatalogTest {
    @Test
    void waveOneHasExactlyFourSummariesPerJavaOwnedOutcomeState() {
        List<NightmareInvestigationOutcomeSummaryCatalog.Primitive> summaries = NightmareInvestigationOutcomeSummaryCatalog.waveOne();
        assertEquals(16, summaries.size());
        assertEquals(16, summaries.stream().map(NightmareInvestigationOutcomeSummaryCatalog.Primitive::id).distinct().count());
        for (NightmareInvestigationOutcomeSummaryCatalog.OutcomeState state : NightmareInvestigationOutcomeSummaryCatalog.OutcomeState.values()) {
            assertEquals(4, summaries.stream().filter(summary -> summary.state() == state).count(), state.name());
        }
        assertEquals(EnumSet.allOf(NightmareInvestigationOutcomeSummaryCatalog.OutcomeState.class),
                summaries.stream().map(NightmareInvestigationOutcomeSummaryCatalog.Primitive::state)
                        .collect(java.util.stream.Collectors.toCollection(() -> EnumSet.noneOf(NightmareInvestigationOutcomeSummaryCatalog.OutcomeState.class))));
    }

    @Test
    void everySummaryHasBoundedPlayerFacingContent() {
        for (NightmareInvestigationOutcomeSummaryCatalog.Primitive summary : NightmareInvestigationOutcomeSummaryCatalog.waveOne()) {
            assertEquals(3, summary.playerReflections().size(), summary.id());
            assertEquals(2, summary.presentationCues().size(), summary.id());
            assertFalse(summary.affinityTags().isEmpty(), summary.id());
            assertTrue(summary.outcomeRead().length() >= 45, summary.id());
            assertTrue(summary.carryForward().length() >= 40, summary.id());
            assertTrue(summary.antiOverclaimBoundary().length() >= 45, summary.id());
        }
    }

    @Test
    void compositionPreservesOpaqueCallerOwnedAuthorityAcrossSeedSweep() {
        String scenario = "shadow:LanternBelow/V2";
        String actor = "Role:SurveyClerkAssistant";
        String investigation = "Investigation:LowerGallery/TraceA";
        String outcome = "Outcome:EvidenceExit#4";
        for (NightmareInvestigationOutcomeSummaryCatalog.OutcomeState state : NightmareInvestigationOutcomeSummaryCatalog.OutcomeState.values()) {
            for (long seed = 0; seed < 4096; seed++) {
                NightmareInvestigationOutcomeSummaryCatalog.Selection selection = NightmareInvestigationOutcomeSummaryCatalog.compose(
                        seed, scenario, actor, investigation, outcome, state, Map.of());
                assertEquals(scenario, selection.scenarioId());
                assertEquals(actor, selection.actorContextId());
                assertEquals(investigation, selection.investigationId());
                assertEquals(outcome, selection.outcomeId());
                assertEquals(state, selection.state());
                assertEquals(state, selection.primitive().state());
                assertTrue(selection.primitive().presentationCues().contains(selection.presentationCue()));
            }
        }
    }

    @Test
    void neutralSeedsReachEverySummaryAndCuePair() {
        Set<String> summaries = new HashSet<>();
        Set<String> pairs = new HashSet<>();
        for (NightmareInvestigationOutcomeSummaryCatalog.OutcomeState state : NightmareInvestigationOutcomeSummaryCatalog.OutcomeState.values()) {
            for (long seed = 0; seed < 16384; seed++) {
                NightmareInvestigationOutcomeSummaryCatalog.Selection selection = NightmareInvestigationOutcomeSummaryCatalog.compose(
                        seed, "scenario", "actor", "investigation", "outcome_" + state.name(), state, Map.of());
                summaries.add(selection.primitive().id());
                pairs.add(selection.primitive().id() + "|" + selection.presentationCue());
            }
        }
        assertEquals(16, summaries.size());
        assertEquals(32, pairs.size());
    }

    @Test
    void evidenceMapOrderAndMagnitudeCannotCreateAuthority() {
        Map<String, Integer> first = new HashMap<>();
        first.put("route", 1);
        first.put("record", 1);
        Map<String, Integer> second = new HashMap<>();
        second.put("record", 999);
        second.put("route", 999);

        NightmareInvestigationOutcomeSummaryCatalog.Selection a = NightmareInvestigationOutcomeSummaryCatalog.compose(
                77L, "Scenario:Opaque", "Actor:Opaque", "Investigation:Opaque", "Outcome:Opaque",
                NightmareInvestigationOutcomeSummaryCatalog.OutcomeState.FOUND, first);
        NightmareInvestigationOutcomeSummaryCatalog.Selection b = NightmareInvestigationOutcomeSummaryCatalog.compose(
                77L, "Scenario:Opaque", "Actor:Opaque", "Investigation:Opaque", "Outcome:Opaque",
                NightmareInvestigationOutcomeSummaryCatalog.OutcomeState.FOUND, second);

        assertEquals(a.primitive().id(), b.primitive().id());
        assertEquals(a.presentationCue(), b.presentationCue());
        assertEquals(a.matchedEvidenceTags(), b.matchedEvidenceTags());
        assertEquals(NightmareInvestigationOutcomeSummaryCatalog.OutcomeState.FOUND, a.state());
        assertEquals("Scenario:Opaque", a.scenarioId());
    }

    @Test
    void positiveEvidenceMayPreferCompatiblePresentationWithoutChangingOutcome() {
        for (long seed = 0; seed < 512; seed++) {
            NightmareInvestigationOutcomeSummaryCatalog.Selection selection = NightmareInvestigationOutcomeSummaryCatalog.compose(
                    seed, "scenario", "actor", "investigation", "outcome",
                    NightmareInvestigationOutcomeSummaryCatalog.OutcomeState.FOUND, Map.of("route", 1));
            assertEquals(NightmareInvestigationOutcomeSummaryCatalog.OutcomeState.FOUND, selection.state());
            assertEquals("found_route_answer", selection.primitive().id());
            assertTrue(selection.matchedEvidenceTags().contains("route"));
        }
    }

    @Test
    void stateSpecificBoundariesRejectPresentationAuthority() {
        String found = NightmareInvestigationOutcomeSummaryCatalog.byId("found_source_identity").antiOverclaimBoundary().toLowerCase();
        String unresolved = NightmareInvestigationOutcomeSummaryCatalog.byId("unresolved_missing_link").antiOverclaimBoundary().toLowerCase();
        String preserved = NightmareInvestigationOutcomeSummaryCatalog.byId("preserved_original").antiOverclaimBoundary().toLowerCase();
        String abandoned = NightmareInvestigationOutcomeSummaryCatalog.byId("abandoned_cost_choice").antiOverclaimBoundary().toLowerCase();

        assertTrue(found.contains("truth"));
        assertTrue(unresolved.contains("resolutiongraph"));
        assertTrue(preserved.contains("authenticity"));
        assertTrue(abandoned.contains("appraisal"));
        assertTrue(abandoned.contains("scenario success"));
    }

    @Test
    void malformedAuthorityAndEvidenceFailClosed() {
        List<Runnable> invalid = new ArrayList<>();
        invalid.add(() -> NightmareInvestigationOutcomeSummaryCatalog.compose(1L, "", "actor", "investigation", "outcome",
                NightmareInvestigationOutcomeSummaryCatalog.OutcomeState.FOUND, Map.of()));
        invalid.add(() -> NightmareInvestigationOutcomeSummaryCatalog.compose(1L, "scenario", "", "investigation", "outcome",
                NightmareInvestigationOutcomeSummaryCatalog.OutcomeState.FOUND, Map.of()));
        invalid.add(() -> NightmareInvestigationOutcomeSummaryCatalog.compose(1L, "scenario", "actor", "", "outcome",
                NightmareInvestigationOutcomeSummaryCatalog.OutcomeState.FOUND, Map.of()));
        invalid.add(() -> NightmareInvestigationOutcomeSummaryCatalog.compose(1L, "scenario", "actor", "investigation", "",
                NightmareInvestigationOutcomeSummaryCatalog.OutcomeState.FOUND, Map.of()));
        invalid.add(() -> NightmareInvestigationOutcomeSummaryCatalog.compose(1L, "scenario", "actor", "investigation", "outcome",
                null, Map.of()));
        invalid.add(() -> NightmareInvestigationOutcomeSummaryCatalog.compose(1L, "scenario", "actor", "investigation", "outcome",
                NightmareInvestigationOutcomeSummaryCatalog.OutcomeState.FOUND, Map.of("route", -1)));
        invalid.add(() -> NightmareInvestigationOutcomeSummaryCatalog.byId("unknown_summary"));
        for (Runnable call : invalid) assertThrows(RuntimeException.class, call::run);
    }
}
