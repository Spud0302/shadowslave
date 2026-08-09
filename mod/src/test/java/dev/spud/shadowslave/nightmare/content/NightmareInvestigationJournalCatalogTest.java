package dev.spud.shadowslave.nightmare.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class NightmareInvestigationJournalCatalogTest {
    @Test
    void waveOneHasExactlyTwentyUniquePrimitivesAndFivePerState() {
        var all = NightmareInvestigationJournalCatalog.waveOne();
        assertEquals(20, all.size());
        assertEquals(20, all.stream().map(NightmareInvestigationJournalCatalog.Primitive::id).distinct().count());
        for (var state : NightmareInvestigationJournalCatalog.EntryState.values()) {
            assertEquals(5, all.stream().filter(primitive -> primitive.state() == state).count());
        }
    }

    @Test
    void everyPrimitiveHasBoundedPlayerFacingContent() {
        for (var primitive : NightmareInvestigationJournalCatalog.waveOne()) {
            assertFalse(primitive.title().isBlank());
            assertFalse(primitive.journalRead().isBlank());
            assertFalse(primitive.nextQuestion().isBlank());
            assertEquals(3, primitive.playerActions().size());
            assertEquals(2, primitive.presentationCues().size());
            assertFalse(primitive.affinityTags().isEmpty());
            assertFalse(primitive.antiOverclaimBoundary().isBlank());
        }
    }

    @Test
    void deterministicCompositionIgnoresEvidenceMapOrder() {
        Map<String, Integer> first = new HashMap<>();
        first.put("record", 1);
        first.put("contradiction", 1);
        Map<String, Integer> second = new HashMap<>();
        second.put("contradiction", 1);
        second.put("record", 1);
        var states = EnumSet.allOf(NightmareInvestigationJournalCatalog.EntryState.class);
        assertEquals(
                NightmareInvestigationJournalCatalog.compose(44L, "scenario_a", "actor_a", "evidence_a", "verify_a", states, first),
                NightmareInvestigationJournalCatalog.compose(44L, "scenario_a", "actor_a", "evidence_a", "verify_a", states, second));
    }

    @Test
    void evidenceMagnitudeDoesNotBecomeWeightOrCertainty() {
        var states = EnumSet.allOf(NightmareInvestigationJournalCatalog.EntryState.class);
        var low = NightmareInvestigationJournalCatalog.compose(918L, "scenario_a", "actor_a", "evidence_a", "verify_a", states,
                Map.of("record", 1, "uncertainty", 1));
        var high = NightmareInvestigationJournalCatalog.compose(918L, "scenario_a", "actor_a", "evidence_a", "verify_a", states,
                Map.of("record", 999, "uncertainty", 999));
        assertEquals(low, high);
    }

    @Test
    void positiveEvidenceCanPreferCompatiblePresentationWithoutTruthInference() {
        var result = NightmareInvestigationJournalCatalog.compose(7L, "scenario_a", "actor_a", "evidence_a", "verify_a",
                EnumSet.allOf(NightmareInvestigationJournalCatalog.EntryState.class), Map.of("refusal", 1));
        assertTrue(result.primitive().affinityTags().contains("refusal"));
        assertEquals(Set.of("refusal"), result.matchedEvidenceTags());
        assertTrue(result.primitive().antiOverclaimBoundary().toLowerCase().contains("guilt"));
    }

    @Test
    void seedCannotMutateCallerOwnedAuthorityOrEscapeAllowedStates() {
        var allowed = EnumSet.of(
                NightmareInvestigationJournalCatalog.EntryState.CONTRADICTED,
                NightmareInvestigationJournalCatalog.EntryState.UNRESOLVED);
        for (long seed = 0; seed < 4096; seed++) {
            var result = NightmareInvestigationJournalCatalog.compose(seed, "scenario_owned", "actor_owned", "evidence_owned",
                    "verification_owned", allowed, Map.of());
            assertEquals("scenario_owned", result.scenarioId());
            assertEquals("actor_owned", result.actorContextId());
            assertEquals("evidence_owned", result.evidenceLinkId());
            assertEquals("verification_owned", result.verificationExchangeId());
            assertTrue(allowed.contains(result.primitive().state()));
        }
    }

    @Test
    void neutralSweepReachesAllPrimitivesAndAllPrimitiveCuePairs() {
        Set<String> primitiveIds = new HashSet<>();
        Set<String> primitiveCuePairs = new HashSet<>();
        var states = EnumSet.allOf(NightmareInvestigationJournalCatalog.EntryState.class);
        for (long seed = 0; seed < 16384; seed++) {
            var result = NightmareInvestigationJournalCatalog.compose(seed, "scenario_a", "actor_a", "evidence_a", "verify_a",
                    states, Map.of());
            primitiveIds.add(result.primitive().id());
            primitiveCuePairs.add(result.primitive().id() + "|" + result.presentationCue());
        }
        assertEquals(20, primitiveIds.size());
        assertEquals(40, primitiveCuePairs.size());
    }

    @Test
    void antiOverclaimBoundariesCoverTruthAuthorityAndScenarioState() {
        String boundaries = NightmareInvestigationJournalCatalog.waveOne().stream()
                .map(NightmareInvestigationJournalCatalog.Primitive::antiOverclaimBoundary)
                .map(String::toLowerCase)
                .reduce("", (left, right) -> left + " " + right);
        assertTrue(boundaries.contains("truth") || boundaries.contains("truthfulness"));
        assertTrue(boundaries.contains("guilt"));
        assertTrue(boundaries.contains("scenario event"));
        assertTrue(boundaries.contains("probabilities") || boundaries.contains("confidence percentages"));
        assertTrue(boundaries.contains("safety"));
    }

    @Test
    void malformedAuthorityAndNegativeEvidenceFailClosed() {
        var states = EnumSet.allOf(NightmareInvestigationJournalCatalog.EntryState.class);
        assertThrows(IllegalArgumentException.class,
                () -> NightmareInvestigationJournalCatalog.compose(1L, " ", "actor", "evidence", "verify", states, Map.of()));
        assertThrows(IllegalArgumentException.class,
                () -> NightmareInvestigationJournalCatalog.compose(1L, "scenario", " ", "evidence", "verify", states, Map.of()));
        assertThrows(IllegalArgumentException.class,
                () -> NightmareInvestigationJournalCatalog.compose(1L, "scenario", "actor", " ", "verify", states, Map.of()));
        assertThrows(IllegalArgumentException.class,
                () -> NightmareInvestigationJournalCatalog.compose(1L, "scenario", "actor", "evidence", " ", states, Map.of()));
        assertThrows(IllegalArgumentException.class,
                () -> NightmareInvestigationJournalCatalog.compose(1L, "scenario", "actor", "evidence", "verify", Set.of(), Map.of()));
        assertThrows(IllegalArgumentException.class,
                () -> NightmareInvestigationJournalCatalog.compose(1L, "scenario", "actor", "evidence", "verify", states, Map.of("record", -1)));
        assertThrows(IllegalArgumentException.class, () -> NightmareInvestigationJournalCatalog.byId("missing_entry"));
    }
}
