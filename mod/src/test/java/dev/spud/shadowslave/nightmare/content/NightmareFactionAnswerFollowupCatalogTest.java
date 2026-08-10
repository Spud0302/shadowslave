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

class NightmareFactionAnswerFollowupCatalogTest {
    @Test
    void waveOneHasFourUniquePrimitivesPerFamily() {
        var all = NightmareFactionAnswerFollowupCatalog.waveOne();
        assertEquals(16, all.size());
        assertEquals(16, all.stream().map(NightmareFactionAnswerFollowupCatalog.Primitive::id).distinct().count());
        for (var family : NightmareFactionAnswerFollowupCatalog.FollowupFamily.values()) {
            assertEquals(4, all.stream().filter(p -> p.family() == family).count(), family.name());
        }
    }

    @Test
    void everyPrimitiveHasBoundedPlayerFacingContent() {
        List<String> backendTerms = List.of("java", "caller-owned", "authoritative", "resolutiongraph");
        for (var primitive : NightmareFactionAnswerFollowupCatalog.waveOne()) {
            assertEquals(3, primitive.playerActions().size());
            assertEquals(2, primitive.presentationCues().size());
            assertFalse(primitive.situationRead().isBlank());
            assertFalse(primitive.playerPrompt().isBlank());
            assertFalse(primitive.antiOverclaimBoundary().isBlank());
            assertFalse(primitive.affinityTags().isEmpty());

            String playerCopy = String.join(" ", primitive.title(), primitive.situationRead(), primitive.playerPrompt(),
                    String.join(" ", primitive.playerActions()), String.join(" ", primitive.presentationCues())).toLowerCase();
            for (String backend : backendTerms) {
                assertFalse(playerCopy.contains(backend), primitive.id() + " leaked backend term " + backend);
            }
        }
    }

    @Test
    void authoredCollectionsAreImmutable() {
        var primitive = NightmareFactionAnswerFollowupCatalog.waveOne().getFirst();
        assertThrows(UnsupportedOperationException.class, () -> primitive.affinityTags().add("mutated"));
        assertThrows(UnsupportedOperationException.class, () -> primitive.playerActions().add("mutated"));
        assertThrows(UnsupportedOperationException.class, () -> primitive.presentationCues().add("mutated"));
        var selected = NightmareFactionAnswerFollowupCatalog.compose(1L, "S", "F", "A", "FU",
                NightmareFactionAnswerFollowupCatalog.FollowupFamily.RECORD, Map.of("record", 1));
        assertThrows(UnsupportedOperationException.class, () -> selected.matchedEvidenceTags().add("mutated"));
    }

    @Test
    void composePreservesOpaqueAuthorityAndExactFamilyAcrossSeeds() {
        String scenario = "Scenario:First/Nightmare#A";
        String faction = "Faction:Archive/Watch#B";
        String answer = "Answer:ResolvedByCore/MixedCase#C";
        String followup = "Followup:Authorized/MixedCase#D";
        for (var family : NightmareFactionAnswerFollowupCatalog.FollowupFamily.values()) {
            for (long seed = 0; seed < 4096; seed++) {
                var selected = NightmareFactionAnswerFollowupCatalog.compose(seed, scenario, faction, answer,
                        followup, family, Map.of("record", 1, "scope", 999));
                assertEquals(scenario, selected.scenarioId());
                assertEquals(faction, selected.factionId());
                assertEquals(answer, selected.answerId());
                assertEquals(followup, selected.followupId());
                assertEquals(family, selected.family());
                assertEquals(family, selected.primitive().family());
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

        var a = NightmareFactionAnswerFollowupCatalog.compose(77L, "S", "F", "A", "FU",
                NightmareFactionAnswerFollowupCatalog.FollowupFamily.RECORD, first);
        var b = NightmareFactionAnswerFollowupCatalog.compose(77L, "S", "F", "A", "FU",
                NightmareFactionAnswerFollowupCatalog.FollowupFamily.RECORD, second);
        assertEquals(a.primitive().id(), b.primitive().id());
        assertEquals(a.presentationCue(), b.presentationCue());
        assertEquals(a.matchedEvidenceTags(), b.matchedEvidenceTags());
    }

    @Test
    void compatibleEvidenceCanPreferMatchingPresentationWithoutChangingAuthority() {
        for (long seed = 0; seed < 1024; seed++) {
            var selected = NightmareFactionAnswerFollowupCatalog.compose(seed,
                    "Scenario:Opaque", "Faction:Opaque", "Answer:Opaque", "Followup:Opaque",
                    NightmareFactionAnswerFollowupCatalog.FollowupFamily.VERIFY, Map.of("access", 1));
            assertTrue(selected.primitive().affinityTags().contains("access"));
            assertEquals("Scenario:Opaque", selected.scenarioId());
            assertEquals("Faction:Opaque", selected.factionId());
            assertEquals("Answer:Opaque", selected.answerId());
            assertEquals("Followup:Opaque", selected.followupId());
        }
    }

    @Test
    void neutralSweepReachesEveryPrimitiveAndCuePair() {
        Set<String> primitiveIds = new HashSet<>();
        Set<String> cuePairs = new HashSet<>();
        for (var family : NightmareFactionAnswerFollowupCatalog.FollowupFamily.values()) {
            for (long seed = 0; seed < 16384; seed++) {
                var selected = NightmareFactionAnswerFollowupCatalog.compose(seed, "S", "F", "A", "FU",
                        family, Map.of());
                primitiveIds.add(selected.primitive().id());
                cuePairs.add(selected.primitive().id() + "|" + selected.presentationCue());
            }
        }
        assertEquals(16, primitiveIds.size());
        assertEquals(32, cuePairs.size());
    }

    @Test
    void malformedInputsFailClosed() {
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionAnswerFollowupCatalog.compose(
                1L, " ", "F", "A", "FU", NightmareFactionAnswerFollowupCatalog.FollowupFamily.RECORD, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionAnswerFollowupCatalog.compose(
                1L, "S", " ", "A", "FU", NightmareFactionAnswerFollowupCatalog.FollowupFamily.RECORD, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionAnswerFollowupCatalog.compose(
                1L, "S", "F", " ", "FU", NightmareFactionAnswerFollowupCatalog.FollowupFamily.RECORD, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionAnswerFollowupCatalog.compose(
                1L, "S", "F", "A", " ", NightmareFactionAnswerFollowupCatalog.FollowupFamily.RECORD, Map.of()));
        assertThrows(NullPointerException.class, () -> NightmareFactionAnswerFollowupCatalog.compose(
                1L, "S", "F", "A", "FU", null, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionAnswerFollowupCatalog.compose(
                1L, "S", "F", "A", "FU", NightmareFactionAnswerFollowupCatalog.FollowupFamily.RECORD,
                Map.of("record", -1)));
        assertThrows(IllegalArgumentException.class,
                () -> NightmareFactionAnswerFollowupCatalog.requirePrimitive("not_a_real_primitive"));
    }

    @Test
    void boundariesRejectTruthRelationshipAndWorldStateOverclaim() {
        String allBoundaries = NightmareFactionAnswerFollowupCatalog.waveOne().stream()
                .map(NightmareFactionAnswerFollowupCatalog.Primitive::antiOverclaimBoundary)
                .reduce("", (a, b) -> a + " " + b).toLowerCase();
        for (String required : List.of("truth", "motive", "access", "ownership", "trust", "allegiance",
                "reputation", "future", "route safety", "scenario")) {
            assertTrue(allBoundaries.contains(required), required);
        }
    }
}
