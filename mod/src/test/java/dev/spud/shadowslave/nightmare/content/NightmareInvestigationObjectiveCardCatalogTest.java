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

class NightmareInvestigationObjectiveCardCatalogTest {
    @Test
    void waveOneHasExactlyFourCardsPerJavaOwnedState() {
        List<NightmareInvestigationObjectiveCardCatalog.Primitive> cards = NightmareInvestigationObjectiveCardCatalog.waveOne();
        assertEquals(16, cards.size());
        assertEquals(16, cards.stream().map(NightmareInvestigationObjectiveCardCatalog.Primitive::id).distinct().count());
        for (NightmareInvestigationObjectiveCardCatalog.State state : NightmareInvestigationObjectiveCardCatalog.State.values()) {
            assertEquals(4, cards.stream().filter(card -> card.state() == state).count(), state.name());
        }
        assertEquals(EnumSet.allOf(NightmareInvestigationObjectiveCardCatalog.State.class),
                cards.stream().map(NightmareInvestigationObjectiveCardCatalog.Primitive::state)
                        .collect(java.util.stream.Collectors.toCollection(() -> EnumSet.noneOf(NightmareInvestigationObjectiveCardCatalog.State.class))));
    }

    @Test
    void everyCardHasBoundedPlayerFacingContent() {
        for (NightmareInvestigationObjectiveCardCatalog.Primitive card : NightmareInvestigationObjectiveCardCatalog.waveOne()) {
            assertEquals(3, card.playerOptions().size(), card.id());
            assertEquals(2, card.presentationCues().size(), card.id());
            assertFalse(card.affinityTags().isEmpty(), card.id());
            assertTrue(card.statusRead().length() >= 45, card.id());
            assertTrue(card.nextPrompt().length() >= 35, card.id());
            assertTrue(card.antiOverclaimBoundary().length() >= 45, card.id());
        }
    }

    @Test
    void compositionPreservesCallerOwnedAuthorityAcrossSeedSweep() {
        String scenario = "lantern_below";
        String actor = "survey_clerk_assistant";
        String plan = "test_route_marker";
        String objective = "verify_lower_gallery_route";
        for (NightmareInvestigationObjectiveCardCatalog.State state : NightmareInvestigationObjectiveCardCatalog.State.values()) {
            for (long seed = 0; seed < 4096; seed++) {
                NightmareInvestigationObjectiveCardCatalog.Selection selection = NightmareInvestigationObjectiveCardCatalog.compose(
                        seed, scenario, actor, plan, objective, state, Map.of());
                assertEquals(scenario, selection.scenarioId());
                assertEquals(actor, selection.actorContextId());
                assertEquals(plan, selection.planId());
                assertEquals(objective, selection.objectiveId());
                assertEquals(state, selection.state());
                assertEquals(state, selection.primitive().state());
                assertTrue(selection.primitive().presentationCues().contains(selection.presentationCue()));
            }
        }
    }

    @Test
    void neutralSeedsReachEveryCardAndCuePair() {
        Set<String> cards = new HashSet<>();
        Set<String> pairs = new HashSet<>();
        for (NightmareInvestigationObjectiveCardCatalog.State state : NightmareInvestigationObjectiveCardCatalog.State.values()) {
            for (long seed = 0; seed < 16384; seed++) {
                NightmareInvestigationObjectiveCardCatalog.Selection selection = NightmareInvestigationObjectiveCardCatalog.compose(
                        seed, "scenario", "actor", "plan", "objective_" + state.name().toLowerCase(), state, Map.of());
                cards.add(selection.primitive().id());
                pairs.add(selection.primitive().id() + "|" + selection.presentationCue());
            }
        }
        assertEquals(16, cards.size());
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

        NightmareInvestigationObjectiveCardCatalog.Selection a = NightmareInvestigationObjectiveCardCatalog.compose(
                77L, "scenario", "actor", "plan", "objective", NightmareInvestigationObjectiveCardCatalog.State.ACTIVE, first);
        NightmareInvestigationObjectiveCardCatalog.Selection b = NightmareInvestigationObjectiveCardCatalog.compose(
                77L, "scenario", "actor", "plan", "objective", NightmareInvestigationObjectiveCardCatalog.State.ACTIVE, second);

        assertEquals(a.primitive().id(), b.primitive().id());
        assertEquals(a.presentationCue(), b.presentationCue());
        assertEquals(Set.of("route", "record"), a.matchedEvidenceTags());
        assertEquals(a.matchedEvidenceTags(), b.matchedEvidenceTags());
        assertEquals(NightmareInvestigationObjectiveCardCatalog.State.ACTIVE, a.state());
    }

    @Test
    void positiveEvidenceMayPreferCompatiblePresentationWithoutChangingState() {
        for (long seed = 0; seed < 512; seed++) {
            NightmareInvestigationObjectiveCardCatalog.Selection selection = NightmareInvestigationObjectiveCardCatalog.compose(
                    seed, "scenario", "actor", "plan", "objective", NightmareInvestigationObjectiveCardCatalog.State.DEFERRED,
                    Map.of("route", 1));
            assertEquals(NightmareInvestigationObjectiveCardCatalog.State.DEFERRED, selection.state());
            assertEquals(NightmareInvestigationObjectiveCardCatalog.State.DEFERRED, selection.primitive().state());
            assertTrue(selection.primitive().affinityTags().contains("route"));
        }
    }

    @Test
    void stateSpecificBoundariesRejectPresentationAuthority() {
        String active = NightmareInvestigationObjectiveCardCatalog.byId("active_test_route").antiOverclaimBoundary().toLowerCase();
        String deferred = NightmareInvestigationObjectiveCardCatalog.byId("deferred_missing_fact").antiOverclaimBoundary().toLowerCase();
        String blocked = NightmareInvestigationObjectiveCardCatalog.byId("blocked_authority").antiOverclaimBoundary().toLowerCase();
        String completed = NightmareInvestigationObjectiveCardCatalog.byId("completed_observation").antiOverclaimBoundary().toLowerCase();

        assertTrue(active.contains("guarantee"));
        assertTrue(deferred.contains("freeze"));
        assertTrue(blocked.contains("resolutiongraph"));
        assertTrue(completed.contains("scenario resolution"));
        assertTrue(completed.contains("appraisal"));
    }

    @Test
    void malformedAuthorityAndEvidenceFailClosed() {
        List<Runnable> invalid = new ArrayList<>();
        invalid.add(() -> NightmareInvestigationObjectiveCardCatalog.compose(1L, "", "actor", "plan", "objective",
                NightmareInvestigationObjectiveCardCatalog.State.ACTIVE, Map.of()));
        invalid.add(() -> NightmareInvestigationObjectiveCardCatalog.compose(1L, "scenario", "", "plan", "objective",
                NightmareInvestigationObjectiveCardCatalog.State.ACTIVE, Map.of()));
        invalid.add(() -> NightmareInvestigationObjectiveCardCatalog.compose(1L, "scenario", "actor", "", "objective",
                NightmareInvestigationObjectiveCardCatalog.State.ACTIVE, Map.of()));
        invalid.add(() -> NightmareInvestigationObjectiveCardCatalog.compose(1L, "scenario", "actor", "plan", "",
                NightmareInvestigationObjectiveCardCatalog.State.ACTIVE, Map.of()));
        invalid.add(() -> NightmareInvestigationObjectiveCardCatalog.compose(1L, "scenario", "actor", "plan", "objective",
                null, Map.of()));
        invalid.add(() -> NightmareInvestigationObjectiveCardCatalog.compose(1L, "scenario", "actor", "plan", "objective",
                NightmareInvestigationObjectiveCardCatalog.State.ACTIVE, Map.of("route", -1)));
        invalid.add(() -> NightmareInvestigationObjectiveCardCatalog.byId("unknown_card"));
        for (Runnable call : invalid) assertThrows(RuntimeException.class, call::run);
    }
}
