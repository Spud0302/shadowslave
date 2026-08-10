package dev.spud.shadowslave.nightmare.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class NightmareFactionInformationThreadSummaryCatalogTest {
    @Test
    void waveOneHasFourUniquePrimitivesPerState() {
        var all = NightmareFactionInformationThreadSummaryCatalog.waveOne();
        assertEquals(16, all.size());
        assertEquals(16, all.stream().map(NightmareFactionInformationThreadSummaryCatalog.Primitive::id).distinct().count());
        for (var state : NightmareFactionInformationThreadSummaryCatalog.ThreadState.values()) {
            assertEquals(4, all.stream().filter(p -> p.state() == state).count(), state.name());
        }
    }

    @Test
    void everyPrimitiveHasBoundedPlayerFacingContent() {
        List<String> backendTerms = List.of("java", "caller-owned", "authoritative", "resolutiongraph");
        for (var primitive : NightmareFactionInformationThreadSummaryCatalog.waveOne()) {
            assertEquals(3, primitive.playerActions().size());
            assertEquals(2, primitive.presentationCues().size());
            assertFalse(primitive.threadRead().isBlank());
            assertFalse(primitive.nextQuestion().isBlank());
            assertFalse(primitive.antiOverclaimBoundary().isBlank());
            assertFalse(primitive.affinityTags().isEmpty());
            String playerCopy = String.join(" ", primitive.title(), primitive.threadRead(), primitive.nextQuestion(),
                    String.join(" ", primitive.playerActions()), String.join(" ", primitive.presentationCues())).toLowerCase();
            for (String backend : backendTerms) assertFalse(playerCopy.contains(backend), primitive.id() + " leaked " + backend);
        }
    }

    @Test
    void authoredCollectionsAreImmutable() {
        var primitive = NightmareFactionInformationThreadSummaryCatalog.waveOne().getFirst();
        assertThrows(UnsupportedOperationException.class, () -> primitive.affinityTags().add("mutated"));
        assertThrows(UnsupportedOperationException.class, () -> primitive.playerActions().add("mutated"));
        assertThrows(UnsupportedOperationException.class, () -> primitive.presentationCues().add("mutated"));
        var selected = NightmareFactionInformationThreadSummaryCatalog.compose(1L, "S", "F", "T", "O",
                NightmareFactionInformationThreadSummaryCatalog.ThreadState.ACTIVE, Map.of("active", 1));
        assertThrows(UnsupportedOperationException.class, () -> selected.matchedEvidenceTags().add("mutated"));
    }

    @Test
    void composePreservesOpaqueAuthorityAndExactStateAcrossSeeds() {
        String scenario = "  Scenario:First/Nightmare#A  ";
        String faction = "\tFaction:Archive/Watch#B\t";
        String thread = " Thread:ResolvedByCore/MixedCase#C ";
        String outcome = "  Outcome:ResolvedByCore/MixedCase#D\n";
        for (var state : NightmareFactionInformationThreadSummaryCatalog.ThreadState.values()) {
            for (long seed = 0; seed < 4096; seed++) {
                var selected = NightmareFactionInformationThreadSummaryCatalog.compose(seed, scenario, faction, thread,
                        outcome, state, Map.of("history", 1, "current", 999));
                assertEquals(scenario, selected.scenarioId());
                assertEquals(faction, selected.factionId());
                assertEquals(thread, selected.threadId());
                assertEquals(outcome, selected.latestOutcomeId());
                assertEquals(state, selected.state());
                assertEquals(state, selected.primitive().state());
            }
        }
    }

    @Test
    void evidenceOrderAndPositiveMagnitudeDoNotChangeSelection() {
        Map<String, Integer> first = new LinkedHashMap<>();
        first.put("active", 1);
        first.put("source", 999);
        Map<String, Integer> second = new LinkedHashMap<>();
        second.put("source", 1);
        second.put("active", 999);
        var a = NightmareFactionInformationThreadSummaryCatalog.compose(77L, "S", "F", "T", "O",
                NightmareFactionInformationThreadSummaryCatalog.ThreadState.ACTIVE, first);
        var b = NightmareFactionInformationThreadSummaryCatalog.compose(77L, "S", "F", "T", "O",
                NightmareFactionInformationThreadSummaryCatalog.ThreadState.ACTIVE, second);
        assertEquals(a.primitive().id(), b.primitive().id());
        assertEquals(a.presentationCue(), b.presentationCue());
        assertEquals(a.matchedEvidenceTags(), b.matchedEvidenceTags());
    }

    @Test
    void compatibleEvidenceCanPreferMatchingPresentationWithoutChangingAuthority() {
        for (long seed = 0; seed < 1024; seed++) {
            var selected = NightmareFactionInformationThreadSummaryCatalog.compose(seed,
                    "Scenario:Opaque", "Faction:Opaque", "Thread:Opaque", "Outcome:Opaque",
                    NightmareFactionInformationThreadSummaryCatalog.ThreadState.CONTRADICTED,
                    Map.of("authority", 1, "scope", 1));
            assertEquals("contradicted_authority", selected.primitive().id());
            assertEquals("Scenario:Opaque", selected.scenarioId());
            assertEquals("Outcome:Opaque", selected.latestOutcomeId());
        }
    }

    @Test
    void neutralSweepReachesEveryPrimitiveAndCuePair() {
        Set<String> primitiveIds = new HashSet<>();
        Set<String> cuePairs = new HashSet<>();
        for (var state : NightmareFactionInformationThreadSummaryCatalog.ThreadState.values()) {
            for (long seed = 0; seed < 16384; seed++) {
                var selected = NightmareFactionInformationThreadSummaryCatalog.compose(seed, "S", "F", "T", "O",
                        state, Map.of());
                primitiveIds.add(selected.primitive().id());
                cuePairs.add(selected.primitive().id() + "|" + selected.presentationCue());
            }
        }
        assertEquals(16, primitiveIds.size());
        assertEquals(32, cuePairs.size());
    }

    @Test
    void malformedInputsFailClosed() {
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionInformationThreadSummaryCatalog.compose(
                1L, " ", "F", "T", "O", NightmareFactionInformationThreadSummaryCatalog.ThreadState.ACTIVE, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionInformationThreadSummaryCatalog.compose(
                1L, "S", " ", "T", "O", NightmareFactionInformationThreadSummaryCatalog.ThreadState.ACTIVE, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionInformationThreadSummaryCatalog.compose(
                1L, "S", "F", " ", "O", NightmareFactionInformationThreadSummaryCatalog.ThreadState.ACTIVE, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionInformationThreadSummaryCatalog.compose(
                1L, "S", "F", "T", " ", NightmareFactionInformationThreadSummaryCatalog.ThreadState.ACTIVE, Map.of()));
        assertThrows(NullPointerException.class, () -> NightmareFactionInformationThreadSummaryCatalog.compose(
                1L, "S", "F", "T", "O", null, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionInformationThreadSummaryCatalog.compose(
                1L, "S", "F", "T", "O", NightmareFactionInformationThreadSummaryCatalog.ThreadState.ACTIVE,
                Map.of("active", -1)));
        assertThrows(IllegalArgumentException.class,
                () -> NightmareFactionInformationThreadSummaryCatalog.requirePrimitive("not_a_real_primitive"));
    }

    @Test
    void boundariesRejectTruthRelationshipAndWorldStateOverclaim() {
        String allBoundaries = NightmareFactionInformationThreadSummaryCatalog.waveOne().stream()
                .map(NightmareFactionInformationThreadSummaryCatalog.Primitive::antiOverclaimBoundary)
                .reduce("", (a, b) -> a + " " + b).toLowerCase();
        for (String required : List.of("truth", "guilt", "access", "ownership", "trust", "allegiance",
                "reputation", "future", "route safety", "scenario", "appraisal")) {
            assertTrue(allBoundaries.contains(required), required);
        }
    }
}
