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

class NightmareFactionCurrentAnswerFramingCatalogTest {
    @Test
    void waveOneHasFourUniquePrimitivesPerFrame() {
        var all = NightmareFactionCurrentAnswerFramingCatalog.waveOne();
        assertEquals(16, all.size());
        assertEquals(16, all.stream().map(NightmareFactionCurrentAnswerFramingCatalog.Primitive::id).distinct().count());
        for (var frame : NightmareFactionCurrentAnswerFramingCatalog.AnswerFrame.values()) {
            assertEquals(4, all.stream().filter(p -> p.frame() == frame).count(), frame.name());
        }
    }

    @Test
    void everyPrimitiveHasBoundedPlayerFacingContent() {
        List<String> backendTerms = List.of("java", "caller-owned", "authoritative", "resolutiongraph");
        for (var primitive : NightmareFactionCurrentAnswerFramingCatalog.waveOne()) {
            assertEquals(3, primitive.playerResponses().size());
            assertEquals(2, primitive.presentationCues().size());
            assertFalse(primitive.answerRead().isBlank());
            assertFalse(primitive.factionLine().isBlank());
            assertFalse(primitive.antiOverclaimBoundary().isBlank());
            assertFalse(primitive.affinityTags().isEmpty());

            String playerCopy = String.join(" ", primitive.title(), primitive.answerRead(), primitive.factionLine(),
                    String.join(" ", primitive.playerResponses()), String.join(" ", primitive.presentationCues())).toLowerCase();
            for (String backend : backendTerms) {
                assertFalse(playerCopy.contains(backend), primitive.id() + " leaked backend term " + backend);
            }
        }
    }

    @Test
    void authoredCollectionsAreImmutable() {
        var primitive = NightmareFactionCurrentAnswerFramingCatalog.waveOne().getFirst();
        assertThrows(UnsupportedOperationException.class, () -> primitive.affinityTags().add("mutated"));
        assertThrows(UnsupportedOperationException.class, () -> primitive.playerResponses().add("mutated"));
        assertThrows(UnsupportedOperationException.class, () -> primitive.presentationCues().add("mutated"));
    }

    @Test
    void composePreservesOpaqueAuthorityAndExactFrameAcrossSeeds() {
        String scenario = "Scenario:First/Nightmare#A";
        String faction = "Faction:Archive/Watch#B";
        String question = "Question:CurrentIntent/MixedCase#C";
        String answer = "Answer:ResolvedByCore/MixedCase#D";
        for (var frame : NightmareFactionCurrentAnswerFramingCatalog.AnswerFrame.values()) {
            for (long seed = 0; seed < 4096; seed++) {
                var selected = NightmareFactionCurrentAnswerFramingCatalog.compose(seed, scenario, faction, question,
                        answer, frame, Map.of("goal", 1, "scope", 999));
                assertEquals(scenario, selected.scenarioId());
                assertEquals(faction, selected.factionId());
                assertEquals(question, selected.questionId());
                assertEquals(answer, selected.answerId());
                assertEquals(frame, selected.frame());
                assertEquals(frame, selected.primitive().frame());
            }
        }
    }

    @Test
    void evidenceOrderAndPositiveMagnitudeDoNotChangeSelection() {
        Map<String, Integer> first = new LinkedHashMap<>();
        first.put("goal", 1);
        first.put("scope", 999);
        Map<String, Integer> second = new LinkedHashMap<>();
        second.put("scope", 1);
        second.put("goal", 999);

        var a = NightmareFactionCurrentAnswerFramingCatalog.compose(77L, "S", "F", "Q", "A",
                NightmareFactionCurrentAnswerFramingCatalog.AnswerFrame.DIRECT, first);
        var b = NightmareFactionCurrentAnswerFramingCatalog.compose(77L, "S", "F", "Q", "A",
                NightmareFactionCurrentAnswerFramingCatalog.AnswerFrame.DIRECT, second);
        assertEquals(a.primitive().id(), b.primitive().id());
        assertEquals(a.presentationCue(), b.presentationCue());
        assertEquals(a.matchedEvidenceTags(), b.matchedEvidenceTags());
    }

    @Test
    void compatibleEvidenceCanPreferMatchingPresentationWithoutChangingAuthority() {
        for (long seed = 0; seed < 1024; seed++) {
            var selected = NightmareFactionCurrentAnswerFramingCatalog.compose(seed,
                    "Scenario:Opaque", "Faction:Opaque", "Question:Opaque", "Answer:Opaque",
                    NightmareFactionCurrentAnswerFramingCatalog.AnswerFrame.DIRECT, Map.of("access", 1));
            assertTrue(selected.primitive().affinityTags().contains("access"));
            assertEquals("Scenario:Opaque", selected.scenarioId());
            assertEquals("Faction:Opaque", selected.factionId());
            assertEquals("Question:Opaque", selected.questionId());
            assertEquals("Answer:Opaque", selected.answerId());
        }
    }

    @Test
    void neutralSweepReachesEveryPrimitiveAndCuePair() {
        Set<String> primitiveIds = new HashSet<>();
        Set<String> cuePairs = new HashSet<>();
        for (var frame : NightmareFactionCurrentAnswerFramingCatalog.AnswerFrame.values()) {
            for (long seed = 0; seed < 16384; seed++) {
                var selected = NightmareFactionCurrentAnswerFramingCatalog.compose(seed, "S", "F", "Q", "A",
                        frame, Map.of());
                primitiveIds.add(selected.primitive().id());
                cuePairs.add(selected.primitive().id() + "|" + selected.presentationCue());
            }
        }
        assertEquals(16, primitiveIds.size());
        assertEquals(32, cuePairs.size());
    }

    @Test
    void malformedInputsFailClosed() {
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionCurrentAnswerFramingCatalog.compose(
                1L, " ", "F", "Q", "A", NightmareFactionCurrentAnswerFramingCatalog.AnswerFrame.DIRECT, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionCurrentAnswerFramingCatalog.compose(
                1L, "S", " ", "Q", "A", NightmareFactionCurrentAnswerFramingCatalog.AnswerFrame.DIRECT, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionCurrentAnswerFramingCatalog.compose(
                1L, "S", "F", " ", "A", NightmareFactionCurrentAnswerFramingCatalog.AnswerFrame.DIRECT, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionCurrentAnswerFramingCatalog.compose(
                1L, "S", "F", "Q", " ", NightmareFactionCurrentAnswerFramingCatalog.AnswerFrame.DIRECT, Map.of()));
        assertThrows(NullPointerException.class, () -> NightmareFactionCurrentAnswerFramingCatalog.compose(
                1L, "S", "F", "Q", "A", null, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionCurrentAnswerFramingCatalog.compose(
                1L, "S", "F", "Q", "A", NightmareFactionCurrentAnswerFramingCatalog.AnswerFrame.DIRECT,
                Map.of("goal", -1)));
        assertThrows(IllegalArgumentException.class,
                () -> NightmareFactionCurrentAnswerFramingCatalog.requirePrimitive("not_a_real_primitive"));
    }

    @Test
    void boundariesRejectTruthAndRelationshipOverclaim() {
        String allBoundaries = NightmareFactionCurrentAnswerFramingCatalog.waveOne().stream()
                .map(NightmareFactionCurrentAnswerFramingCatalog.Primitive::antiOverclaimBoundary)
                .reduce("", (a, b) -> a + " " + b).toLowerCase();
        for (String required : List.of("truth", "motive", "access", "ownership", "trust", "allegiance", "reputation", "future")) {
            assertTrue(allBoundaries.contains(required), required);
        }
    }
}
