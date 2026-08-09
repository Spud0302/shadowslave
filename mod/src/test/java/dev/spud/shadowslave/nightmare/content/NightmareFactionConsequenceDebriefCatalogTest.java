package dev.spud.shadowslave.nightmare.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class NightmareFactionConsequenceDebriefCatalogTest {

    @Test
    void waveOneHasSixteenUniqueDebriefsAndFourPerKind() {
        List<NightmareFactionConsequenceDebriefCatalog.Primitive> primitives =
                NightmareFactionConsequenceDebriefCatalog.waveOne();
        assertEquals(16, primitives.size());
        assertEquals(16, primitives.stream().map(NightmareFactionConsequenceDebriefCatalog.Primitive::id)
                .collect(Collectors.toSet()).size());
        Map<NightmareFactionConsequenceDebriefCatalog.ConsequenceKind, Long> byKind = primitives.stream()
                .collect(Collectors.groupingBy(NightmareFactionConsequenceDebriefCatalog.Primitive::kind,
                        Collectors.counting()));
        for (NightmareFactionConsequenceDebriefCatalog.ConsequenceKind kind
                : NightmareFactionConsequenceDebriefCatalog.ConsequenceKind.values()) {
            assertEquals(4L, byKind.getOrDefault(kind, 0L), kind.name());
        }
    }

    @Test
    void everyDebriefHasBoundedPlayerFacingContent() {
        for (var primitive : NightmareFactionConsequenceDebriefCatalog.waveOne()) {
            assertFalse(primitive.title().isBlank());
            assertTrue(primitive.consequenceRead().length() >= 45, primitive.id());
            assertTrue(primitive.carryForwardPrompt().length() >= 30, primitive.id());
            assertEquals(3, primitive.playerResponses().size(), primitive.id());
            assertEquals(2, primitive.presentationCues().size(), primitive.id());
            assertFalse(primitive.affinityTags().isEmpty(), primitive.id());
            assertTrue(primitive.antiOverclaimBoundary().length() >= 70, primitive.id());
        }
        String boundaries = NightmareFactionConsequenceDebriefCatalog.waveOne().stream()
                .map(NightmareFactionConsequenceDebriefCatalog.Primitive::antiOverclaimBoundary)
                .collect(Collectors.joining(" ")).toLowerCase();
        assertTrue(boundaries.contains("allegiance"));
        assertTrue(boundaries.contains("reputation"));
        assertTrue(boundaries.contains("truth") || boundaries.contains("authenticity"));
        assertTrue(boundaries.contains("scenario"));
        assertTrue(boundaries.contains("appraisal"));
        assertTrue(boundaries.contains("resource"));
        assertTrue(boundaries.contains("ownership"));
    }

    @Test
    void playerFacingCopyDoesNotLeakBackendTerminology() {
        for (var primitive : NightmareFactionConsequenceDebriefCatalog.waveOne()) {
            String playerCopy = String.join(" ",
                    primitive.title(), primitive.consequenceRead(), primitive.carryForwardPrompt(),
                    String.join(" ", primitive.playerResponses()), String.join(" ", primitive.presentationCues()))
                    .toLowerCase();
            assertFalse(playerCopy.contains("java"), primitive.id());
            assertFalse(playerCopy.contains("caller-owned"), primitive.id());
            assertFalse(playerCopy.contains("authoritative"), primitive.id());
            assertFalse(playerCopy.contains("resolutiongraph"), primitive.id());
        }
    }

    @Test
    void composePreservesOpaqueAuthorityAndExactJavaOwnedKindAcrossSeeds() {
        String scenarioId = "Scenario::Mictlan/CaseSensitive";
        String factionId = "Faction::NorthWatch/CaseSensitive";
        String agreementId = "Agreement::Terms/CaseSensitive";
        String consequenceId = "Consequence::Local/CaseSensitive";
        for (NightmareFactionConsequenceDebriefCatalog.ConsequenceKind kind
                : NightmareFactionConsequenceDebriefCatalog.ConsequenceKind.values()) {
            for (long seed = 0; seed < 4096; seed++) {
                var selection = NightmareFactionConsequenceDebriefCatalog.compose(seed, scenarioId, factionId,
                        agreementId, consequenceId, kind, Map.of());
                assertEquals(scenarioId, selection.scenarioId());
                assertEquals(factionId, selection.factionId());
                assertEquals(agreementId, selection.agreementId());
                assertEquals(consequenceId, selection.consequenceId());
                assertEquals(kind, selection.kind());
                assertEquals(kind, selection.primitive().kind());
            }
        }
    }

    @Test
    void evidenceMapOrderAndPositiveMagnitudeDoNotChangeSelection() {
        Map<String, Integer> first = new LinkedHashMap<>();
        first.put("resource", 1);
        first.put("commitment", 1);
        Map<String, Integer> second = new LinkedHashMap<>();
        second.put("commitment", 999);
        second.put("resource", 999);
        var left = NightmareFactionConsequenceDebriefCatalog.compose(8123L,
                "Scenario::A", "Faction::B", "Agreement::C", "Consequence::D",
                NightmareFactionConsequenceDebriefCatalog.ConsequenceKind.RESOURCE_STATE_CHANGED, first);
        var right = NightmareFactionConsequenceDebriefCatalog.compose(8123L,
                "Scenario::A", "Faction::B", "Agreement::C", "Consequence::D",
                NightmareFactionConsequenceDebriefCatalog.ConsequenceKind.RESOURCE_STATE_CHANGED, second);
        assertEquals(left.primitive(), right.primitive());
        assertEquals(left.presentationCue(), right.presentationCue());
        assertEquals(left.matchedEvidenceTags(), right.matchedEvidenceTags());
    }

    @Test
    void compatibleEvidenceCanPreferCommittedResourceDebriefWithoutMutatingAuthority() {
        for (long seed = 0; seed < 512; seed++) {
            var selection = NightmareFactionConsequenceDebriefCatalog.compose(seed,
                    "Scenario::Opaque", "Faction::Opaque", "Agreement::Opaque", "Consequence::Opaque",
                    NightmareFactionConsequenceDebriefCatalog.ConsequenceKind.RESOURCE_STATE_CHANGED,
                    Map.of("resource", 1, "commitment", 1));
            assertEquals("resource_commitment_settled", selection.primitive().id());
            assertEquals(NightmareFactionConsequenceDebriefCatalog.ConsequenceKind.RESOURCE_STATE_CHANGED,
                    selection.kind());
            assertEquals("Scenario::Opaque", selection.scenarioId());
            assertEquals("Faction::Opaque", selection.factionId());
            assertEquals("Agreement::Opaque", selection.agreementId());
            assertEquals("Consequence::Opaque", selection.consequenceId());
            assertTrue(selection.matchedEvidenceTags().contains("resource"));
            assertTrue(selection.matchedEvidenceTags().contains("commitment"));
        }
    }

    @Test
    void neutralSweepReachesEveryDebriefAndCuePair() {
        Set<String> ids = new HashSet<>();
        Set<String> pairs = new HashSet<>();
        for (NightmareFactionConsequenceDebriefCatalog.ConsequenceKind kind
                : EnumSet.allOf(NightmareFactionConsequenceDebriefCatalog.ConsequenceKind.class)) {
            for (long seed = 0; seed < 16384; seed++) {
                var selection = NightmareFactionConsequenceDebriefCatalog.compose(seed,
                        "Scenario::Neutral", "Faction::Neutral", "Agreement::Neutral", "Consequence::Neutral",
                        kind, Map.of());
                ids.add(selection.primitive().id());
                pairs.add(selection.primitive().id() + "|" + selection.presentationCue());
            }
        }
        assertEquals(16, ids.size());
        assertEquals(32, pairs.size());
    }

    @Test
    void sameInputIsDeterministic() {
        var left = NightmareFactionConsequenceDebriefCatalog.compose(42L,
                "Scenario::A", "Faction::B", "Agreement::C", "Consequence::D",
                NightmareFactionConsequenceDebriefCatalog.ConsequenceKind.RELATIONSHIP_UNRESOLVED,
                Map.of("relationship", 1));
        var right = NightmareFactionConsequenceDebriefCatalog.compose(42L,
                "Scenario::A", "Faction::B", "Agreement::C", "Consequence::D",
                NightmareFactionConsequenceDebriefCatalog.ConsequenceKind.RELATIONSHIP_UNRESOLVED,
                Map.of("relationship", 1));
        assertEquals(left, right);
    }

    @Test
    void malformedAuthorityAndEvidenceFailClosed() {
        var kind = NightmareFactionConsequenceDebriefCatalog.ConsequenceKind.ACCESS_CHANGED;
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionConsequenceDebriefCatalog.compose(
                1L, " ", "Faction", "Agreement", "Consequence", kind, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionConsequenceDebriefCatalog.compose(
                1L, "Scenario", " ", "Agreement", "Consequence", kind, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionConsequenceDebriefCatalog.compose(
                1L, "Scenario", "Faction", " ", "Consequence", kind, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionConsequenceDebriefCatalog.compose(
                1L, "Scenario", "Faction", "Agreement", " ", kind, Map.of()));
        assertThrows(NullPointerException.class, () -> NightmareFactionConsequenceDebriefCatalog.compose(
                1L, "Scenario", "Faction", "Agreement", "Consequence", null, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionConsequenceDebriefCatalog.compose(
                1L, "Scenario", "Faction", "Agreement", "Consequence", kind, Map.of("access", -1)));
        assertThrows(IllegalArgumentException.class,
                () -> NightmareFactionConsequenceDebriefCatalog.requirePrimitive("not_a_debrief"));
    }
}
