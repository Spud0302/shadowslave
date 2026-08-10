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

class NightmareFactionAnswerFollowupOutcomeCatalogTest {
    @Test
    void waveOneHasFourUniquePrimitivesPerKind() {
        var all = NightmareFactionAnswerFollowupOutcomeCatalog.waveOne();
        assertEquals(16, all.size());
        assertEquals(16, all.stream().map(NightmareFactionAnswerFollowupOutcomeCatalog.Primitive::id).distinct().count());
        for (var kind : NightmareFactionAnswerFollowupOutcomeCatalog.OutcomeKind.values()) {
            assertEquals(4, all.stream().filter(p -> p.kind() == kind).count(), kind.name());
        }
    }

    @Test
    void everyPrimitiveHasBoundedPlayerFacingContent() {
        List<String> backendTerms = List.of("java", "caller-owned", "authoritative", "resolutiongraph");
        for (var primitive : NightmareFactionAnswerFollowupOutcomeCatalog.waveOne()) {
            assertEquals(3, primitive.playerResponses().size());
            assertEquals(2, primitive.presentationCues().size());
            assertFalse(primitive.outcomeRead().isBlank());
            assertFalse(primitive.carryForwardPrompt().isBlank());
            assertFalse(primitive.antiOverclaimBoundary().isBlank());
            assertFalse(primitive.affinityTags().isEmpty());
            String playerCopy = String.join(" ", primitive.title(), primitive.outcomeRead(), primitive.carryForwardPrompt(),
                    String.join(" ", primitive.playerResponses()), String.join(" ", primitive.presentationCues())).toLowerCase();
            for (String backend : backendTerms) assertFalse(playerCopy.contains(backend), primitive.id() + " leaked " + backend);
        }
    }

    @Test
    void authoredCollectionsAreImmutable() {
        var primitive = NightmareFactionAnswerFollowupOutcomeCatalog.waveOne().getFirst();
        assertThrows(UnsupportedOperationException.class, () -> primitive.affinityTags().add("mutated"));
        assertThrows(UnsupportedOperationException.class, () -> primitive.playerResponses().add("mutated"));
        assertThrows(UnsupportedOperationException.class, () -> primitive.presentationCues().add("mutated"));
        var selected = NightmareFactionAnswerFollowupOutcomeCatalog.compose(1L, "S", "F", "A", "FU", "O",
                NightmareFactionAnswerFollowupOutcomeCatalog.OutcomeKind.RECORDED, Map.of("record", 1));
        assertThrows(UnsupportedOperationException.class, () -> selected.matchedEvidenceTags().add("mutated"));
    }

    @Test
    void composePreservesOpaqueAuthorityAndExactKindAcrossSeeds() {
        String scenario = "Scenario:First/Nightmare#A";
        String faction = "Faction:Archive/Watch#B";
        String answer = "Answer:ResolvedByCore/MixedCase#C";
        String followup = "Followup:ResolvedByCore/MixedCase#D";
        String outcome = "Outcome:ResolvedByCore/MixedCase#E";
        for (var kind : NightmareFactionAnswerFollowupOutcomeCatalog.OutcomeKind.values()) {
            for (long seed = 0; seed < 4096; seed++) {
                var selected = NightmareFactionAnswerFollowupOutcomeCatalog.compose(seed, scenario, faction, answer,
                        followup, outcome, kind, Map.of("record", 1, "scope", 999));
                assertEquals(scenario, selected.scenarioId());
                assertEquals(faction, selected.factionId());
                assertEquals(answer, selected.answerId());
                assertEquals(followup, selected.followupId());
                assertEquals(outcome, selected.outcomeId());
                assertEquals(kind, selected.kind());
                assertEquals(kind, selected.primitive().kind());
            }
        }
    }

    @Test
    void evidenceOrderAndPositiveMagnitudeDoNotChangeSelection() {
        Map<String, Integer> first = new LinkedHashMap<>();
        first.put("record", 1);
        first.put("scope", 999);
        Map<String, Integer> second = new LinkedHashMap<>();
        second.put("scope", 1);
        second.put("record", 999);
        var a = NightmareFactionAnswerFollowupOutcomeCatalog.compose(77L, "S", "F", "A", "FU", "O",
                NightmareFactionAnswerFollowupOutcomeCatalog.OutcomeKind.RECORDED, first);
        var b = NightmareFactionAnswerFollowupOutcomeCatalog.compose(77L, "S", "F", "A", "FU", "O",
                NightmareFactionAnswerFollowupOutcomeCatalog.OutcomeKind.RECORDED, second);
        assertEquals(a.primitive().id(), b.primitive().id());
        assertEquals(a.presentationCue(), b.presentationCue());
        assertEquals(a.matchedEvidenceTags(), b.matchedEvidenceTags());
    }

    @Test
    void compatibleEvidenceCanPreferMatchingPresentationWithoutChangingAuthority() {
        for (long seed = 0; seed < 1024; seed++) {
            var selected = NightmareFactionAnswerFollowupOutcomeCatalog.compose(seed,
                    "Scenario:Opaque", "Faction:Opaque", "Answer:Opaque", "Followup:Opaque", "Outcome:Opaque",
                    NightmareFactionAnswerFollowupOutcomeCatalog.OutcomeKind.CHECKED, Map.of("access", 1));
            assertTrue(selected.primitive().affinityTags().contains("access"));
            assertEquals("Scenario:Opaque", selected.scenarioId());
            assertEquals("Outcome:Opaque", selected.outcomeId());
        }
    }

    @Test
    void neutralSweepReachesEveryPrimitiveAndCuePair() {
        Set<String> primitiveIds = new HashSet<>();
        Set<String> cuePairs = new HashSet<>();
        for (var kind : NightmareFactionAnswerFollowupOutcomeCatalog.OutcomeKind.values()) {
            for (long seed = 0; seed < 16384; seed++) {
                var selected = NightmareFactionAnswerFollowupOutcomeCatalog.compose(seed, "S", "F", "A", "FU", "O",
                        kind, Map.of());
                primitiveIds.add(selected.primitive().id());
                cuePairs.add(selected.primitive().id() + "|" + selected.presentationCue());
            }
        }
        assertEquals(16, primitiveIds.size());
        assertEquals(32, cuePairs.size());
    }

    @Test
    void malformedInputsFailClosed() {
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionAnswerFollowupOutcomeCatalog.compose(
                1L, " ", "F", "A", "FU", "O", NightmareFactionAnswerFollowupOutcomeCatalog.OutcomeKind.RECORDED, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionAnswerFollowupOutcomeCatalog.compose(
                1L, "S", " ", "A", "FU", "O", NightmareFactionAnswerFollowupOutcomeCatalog.OutcomeKind.RECORDED, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionAnswerFollowupOutcomeCatalog.compose(
                1L, "S", "F", " ", "FU", "O", NightmareFactionAnswerFollowupOutcomeCatalog.OutcomeKind.RECORDED, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionAnswerFollowupOutcomeCatalog.compose(
                1L, "S", "F", "A", " ", "O", NightmareFactionAnswerFollowupOutcomeCatalog.OutcomeKind.RECORDED, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionAnswerFollowupOutcomeCatalog.compose(
                1L, "S", "F", "A", "FU", " ", NightmareFactionAnswerFollowupOutcomeCatalog.OutcomeKind.RECORDED, Map.of()));
        assertThrows(NullPointerException.class, () -> NightmareFactionAnswerFollowupOutcomeCatalog.compose(
                1L, "S", "F", "A", "FU", "O", null, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionAnswerFollowupOutcomeCatalog.compose(
                1L, "S", "F", "A", "FU", "O", NightmareFactionAnswerFollowupOutcomeCatalog.OutcomeKind.RECORDED,
                Map.of("record", -1)));
        assertThrows(IllegalArgumentException.class,
                () -> NightmareFactionAnswerFollowupOutcomeCatalog.requirePrimitive("not_a_real_primitive"));
    }

    @Test
    void boundariesRejectTruthRelationshipAndWorldStateOverclaim() {
        String allBoundaries = NightmareFactionAnswerFollowupOutcomeCatalog.waveOne().stream()
                .map(NightmareFactionAnswerFollowupOutcomeCatalog.Primitive::antiOverclaimBoundary)
                .reduce("", (a, b) -> a + " " + b).toLowerCase();
        for (String required : List.of("truth", "motive", "access", "ownership", "trust", "allegiance",
                "reputation", "future", "route safety", "scenario")) assertTrue(allBoundaries.contains(required), required);
    }
}
