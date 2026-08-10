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

class NightmareFactionInformationThreadNextStepCatalogTest {
    @Test
    void waveOneHasFourUniquePrimitivesPerFamily() {
        var all = NightmareFactionInformationThreadNextStepCatalog.waveOne();
        assertEquals(16, all.size());
        assertEquals(16, all.stream().map(NightmareFactionInformationThreadNextStepCatalog.Primitive::id).distinct().count());
        for (var family : NightmareFactionInformationThreadNextStepCatalog.Family.values()) {
            assertEquals(4, all.stream().filter(p -> p.family() == family).count(), family.name());
        }
    }

    @Test
    void everyPrimitiveHasBoundedPlayerFacingContent() {
        List<String> backendTerms = List.of("java", "caller-owned", "authoritative", "resolutiongraph");
        for (var primitive : NightmareFactionInformationThreadNextStepCatalog.waveOne()) {
            assertEquals(3, primitive.playerChoices().size());
            assertEquals(2, primitive.presentationCues().size());
            assertFalse(primitive.situationRead().isBlank());
            assertFalse(primitive.actionPrompt().isBlank());
            assertFalse(primitive.antiOverclaimBoundary().isBlank());
            assertFalse(primitive.affinityTags().isEmpty());
            String playerCopy = String.join(" ", primitive.title(), primitive.situationRead(), primitive.actionPrompt(),
                    String.join(" ", primitive.playerChoices()), String.join(" ", primitive.presentationCues())).toLowerCase();
            for (String backend : backendTerms) assertFalse(playerCopy.contains(backend), primitive.id() + " leaked " + backend);
        }
    }

    @Test
    void authoredAndSelectedCollectionsAreImmutable() {
        var primitive = NightmareFactionInformationThreadNextStepCatalog.waveOne().getFirst();
        assertThrows(UnsupportedOperationException.class, () -> primitive.affinityTags().add("mutated"));
        assertThrows(UnsupportedOperationException.class, () -> primitive.playerChoices().add("mutated"));
        assertThrows(UnsupportedOperationException.class, () -> primitive.presentationCues().add("mutated"));
        var selected = NightmareFactionInformationThreadNextStepCatalog.compose(1L, "S", "F", "T", "SUM",
                Set.of(NightmareFactionInformationThreadNextStepCatalog.Family.RECHECK), Map.of("recheck", 1));
        assertThrows(UnsupportedOperationException.class, () -> selected.allowedFamilies().add(NightmareFactionInformationThreadNextStepCatalog.Family.ARCHIVE));
        assertThrows(UnsupportedOperationException.class, () -> selected.matchedEvidenceTags().add("mutated"));
    }

    @Test
    void composePreservesOpaqueAuthorityAndAllowedFamilyAcrossSeeds() {
        String scenario = "  Scenario:First/Nightmare#A  ";
        String faction = "\tFaction:Archive/Watch#B\t";
        String thread = " Thread:ResolvedByCore/MixedCase#C ";
        String summary = "  Summary:ResolvedByCore/MixedCase#D\n";
        for (var family : NightmareFactionInformationThreadNextStepCatalog.Family.values()) {
            Set<NightmareFactionInformationThreadNextStepCatalog.Family> allowed = Set.of(family);
            for (long seed = 0; seed < 4096; seed++) {
                var selected = NightmareFactionInformationThreadNextStepCatalog.compose(seed, scenario, faction, thread,
                        summary, allowed, Map.of("history", 1, "current", 999));
                assertEquals(scenario, selected.scenarioId());
                assertEquals(faction, selected.factionId());
                assertEquals(thread, selected.threadId());
                assertEquals(summary, selected.latestSummaryId());
                assertEquals(allowed, selected.allowedFamilies());
                assertEquals(family, selected.primitive().family());
            }
        }
    }

    @Test
    void allowedFamilySetOrderDoesNotChangeSelection() {
        Set<NightmareFactionInformationThreadNextStepCatalog.Family> a = new java.util.LinkedHashSet<>();
        a.add(NightmareFactionInformationThreadNextStepCatalog.Family.RECHECK);
        a.add(NightmareFactionInformationThreadNextStepCatalog.Family.COMPARE);
        Set<NightmareFactionInformationThreadNextStepCatalog.Family> b = new java.util.LinkedHashSet<>();
        b.add(NightmareFactionInformationThreadNextStepCatalog.Family.COMPARE);
        b.add(NightmareFactionInformationThreadNextStepCatalog.Family.RECHECK);
        var first = NightmareFactionInformationThreadNextStepCatalog.compose(88L, "S", "F", "T", "SUM", a, Map.of());
        var second = NightmareFactionInformationThreadNextStepCatalog.compose(88L, "S", "F", "T", "SUM", b, Map.of());
        assertEquals(first.primitive().id(), second.primitive().id());
        assertEquals(first.presentationCue(), second.presentationCue());
    }

    @Test
    void evidenceOrderAndPositiveMagnitudeDoNotChangeSelection() {
        Map<String, Integer> first = new LinkedHashMap<>();
        first.put("source", 1);
        first.put("record", 999);
        Map<String, Integer> second = new LinkedHashMap<>();
        second.put("record", 1);
        second.put("source", 999);
        var allowed = Set.of(NightmareFactionInformationThreadNextStepCatalog.Family.SEEK_SOURCE);
        var a = NightmareFactionInformationThreadNextStepCatalog.compose(77L, "S", "F", "T", "SUM", allowed, first);
        var b = NightmareFactionInformationThreadNextStepCatalog.compose(77L, "S", "F", "T", "SUM", allowed, second);
        assertEquals(a.primitive().id(), b.primitive().id());
        assertEquals(a.presentationCue(), b.presentationCue());
        assertEquals(a.matchedEvidenceTags(), b.matchedEvidenceTags());
    }

    @Test
    void compatibleEvidenceCanPreferMatchingPresentationWithoutChangingAuthority() {
        var allowed = Set.of(NightmareFactionInformationThreadNextStepCatalog.Family.COMPARE);
        for (long seed = 0; seed < 1024; seed++) {
            var selected = NightmareFactionInformationThreadNextStepCatalog.compose(seed,
                    "Scenario:Opaque", "Faction:Opaque", "Thread:Opaque", "Summary:Opaque", allowed,
                    Map.of("claim", 1, "record", 1));
            assertEquals("compare_claim_record", selected.primitive().id());
            assertEquals("Scenario:Opaque", selected.scenarioId());
            assertEquals("Summary:Opaque", selected.latestSummaryId());
        }
    }

    @Test
    void neutralSweepReachesEveryPrimitiveAndCuePair() {
        Set<String> primitiveIds = new HashSet<>();
        Set<String> cuePairs = new HashSet<>();
        Set<NightmareFactionInformationThreadNextStepCatalog.Family> allFamilies = Set.of(
                NightmareFactionInformationThreadNextStepCatalog.Family.values());
        for (long seed = 0; seed < 16384; seed++) {
            var selected = NightmareFactionInformationThreadNextStepCatalog.compose(seed, "S", "F", "T", "SUM",
                    allFamilies, Map.of());
            primitiveIds.add(selected.primitive().id());
            cuePairs.add(selected.primitive().id() + "|" + selected.presentationCue());
        }
        assertEquals(16, primitiveIds.size());
        assertEquals(32, cuePairs.size());
    }

    @Test
    void malformedInputsFailClosed() {
        var allowed = Set.of(NightmareFactionInformationThreadNextStepCatalog.Family.RECHECK);
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionInformationThreadNextStepCatalog.compose(
                1L, " ", "F", "T", "SUM", allowed, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionInformationThreadNextStepCatalog.compose(
                1L, "S", " ", "T", "SUM", allowed, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionInformationThreadNextStepCatalog.compose(
                1L, "S", "F", " ", "SUM", allowed, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionInformationThreadNextStepCatalog.compose(
                1L, "S", "F", "T", " ", allowed, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionInformationThreadNextStepCatalog.compose(
                1L, "S", "F", "T", "SUM", Set.of(), Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionInformationThreadNextStepCatalog.compose(
                1L, "S", "F", "T", "SUM", allowed, Map.of("recheck", -1)));
        assertThrows(IllegalArgumentException.class,
                () -> NightmareFactionInformationThreadNextStepCatalog.requirePrimitive("not_a_real_primitive"));
    }

    @Test
    void boundariesRejectTruthRelationshipWorldAndProgressionOverclaim() {
        String allBoundaries = NightmareFactionInformationThreadNextStepCatalog.waveOne().stream()
                .map(NightmareFactionInformationThreadNextStepCatalog.Primitive::antiOverclaimBoundary)
                .reduce("", (a, b) -> a + " " + b).toLowerCase();
        for (String required : List.of("truth", "guilt", "access", "ownership", "trust", "allegiance",
                "reputation", "future", "route safety", "scenario", "appraisal", "progression")) {
            assertTrue(allBoundaries.contains(required), required);
        }
    }
}
