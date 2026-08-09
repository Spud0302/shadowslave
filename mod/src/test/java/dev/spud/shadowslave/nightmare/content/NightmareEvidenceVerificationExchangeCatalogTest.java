package dev.spud.shadowslave.nightmare.content;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightmareEvidenceVerificationExchangeCatalogTest {
    @Test
    void waveOneContainsFourUniquePrimitivesPerFamily() {
        List<NightmareEvidenceVerificationExchangeCatalog.Primitive> primitives = NightmareEvidenceVerificationExchangeCatalog.waveOne();
        assertEquals(20, primitives.size());
        assertEquals(20, primitives.stream().map(NightmareEvidenceVerificationExchangeCatalog.Primitive::id).collect(Collectors.toSet()).size());
        for (NightmareEvidenceVerificationExchangeCatalog.Family family : NightmareEvidenceVerificationExchangeCatalog.Family.values()) {
            assertEquals(4, primitives.stream().filter(p -> p.family() == family).count(), family.name());
        }
    }

    @Test
    void everyPrimitiveHasBoundedPlayerFacingContent() {
        for (NightmareEvidenceVerificationExchangeCatalog.Primitive primitive : NightmareEvidenceVerificationExchangeCatalog.waveOne()) {
            assertFalse(primitive.title().isBlank());
            assertFalse(primitive.exchangeRead().isBlank());
            assertFalse(primitive.verificationPrompt().isBlank());
            assertEquals(3, primitive.playerResponses().size());
            assertEquals(2, primitive.presentationCues().size());
            assertFalse(primitive.affinityTags().isEmpty());
            assertFalse(primitive.antiOverclaimBoundary().isBlank());
        }
    }

    @Test
    void compositionIsDeterministicAndEvidenceMapOrderIndependent() {
        Set<NightmareEvidenceVerificationExchangeCatalog.Family> families = EnumSet.allOf(NightmareEvidenceVerificationExchangeCatalog.Family.class);
        Map<String, Integer> first = new HashMap<>();
        first.put("record", 1);
        first.put("sequence", 2);
        Map<String, Integer> second = new HashMap<>();
        second.put("sequence", 2);
        second.put("record", 1);

        var left = NightmareEvidenceVerificationExchangeCatalog.compose(42L, "scenario_alpha", "actor_beta", "evidence_gamma", families, first);
        var right = NightmareEvidenceVerificationExchangeCatalog.compose(42L, "scenario_alpha", "actor_beta", "evidence_gamma", families, second);
        assertEquals(left, right);
    }

    @Test
    void positiveEvidenceMagnitudeDoesNotBecomeConfidenceOrScoringFormula() {
        Set<NightmareEvidenceVerificationExchangeCatalog.Family> families = EnumSet.allOf(NightmareEvidenceVerificationExchangeCatalog.Family.class);
        var small = NightmareEvidenceVerificationExchangeCatalog.compose(991L, "scenario_alpha", "actor_beta", "evidence_gamma", families,
                Map.of("record", 1, "uncertainty", 1));
        var huge = NightmareEvidenceVerificationExchangeCatalog.compose(991L, "scenario_alpha", "actor_beta", "evidence_gamma", families,
                Map.of("record", 999, "uncertainty", 999));
        assertEquals(small, huge);
    }

    @Test
    void compatibleEvidenceCanPreferARelevantExchangeWithoutAdjudicatingTruth() {
        Set<NightmareEvidenceVerificationExchangeCatalog.Family> families = EnumSet.allOf(NightmareEvidenceVerificationExchangeCatalog.Family.class);
        for (long seed = 0; seed < 256; seed++) {
            var selection = NightmareEvidenceVerificationExchangeCatalog.compose(seed, "scenario_alpha", "actor_beta", "evidence_gamma", families,
                    Map.of("corroboration", 1));
            assertTrue(selection.primitive().affinityTags().contains("corroboration"));
            assertEquals(Set.of("corroboration"), selection.matchedEvidenceTags());
        }
    }

    @Test
    void seedCannotEscapeCallerOwnedAuthorityOrAllowedFamilies() {
        Set<NightmareEvidenceVerificationExchangeCatalog.Family> allowed = EnumSet.of(
                NightmareEvidenceVerificationExchangeCatalog.Family.COMPARE,
                NightmareEvidenceVerificationExchangeCatalog.Family.PRESERVE_UNCERTAINTY);
        for (long seed = 0; seed < 4096; seed++) {
            var selection = NightmareEvidenceVerificationExchangeCatalog.compose(seed, "hollow_treaty", "interpreter_context", "amended_order", allowed, Map.of());
            assertEquals("hollow_treaty", selection.scenarioId());
            assertEquals("interpreter_context", selection.actorContextId());
            assertEquals("amended_order", selection.evidenceLinkId());
            assertTrue(allowed.contains(selection.primitive().family()));
            assertEquals(NightmareEvidenceVerificationExchangeCatalog.GENERATOR_VERSION, selection.generatorVersion());
        }
    }

    @Test
    void neutralSweepReachesAllPrimitivesAndPresentationCues() {
        Set<NightmareEvidenceVerificationExchangeCatalog.Family> families = EnumSet.allOf(NightmareEvidenceVerificationExchangeCatalog.Family.class);
        Set<String> primitiveIds = new HashSet<>();
        Set<String> primitiveCuePairs = new HashSet<>();
        for (long seed = 0; seed < 16384; seed++) {
            var selection = NightmareEvidenceVerificationExchangeCatalog.compose(seed, "scenario_alpha", "actor_beta", "evidence_gamma", families, Map.of());
            primitiveIds.add(selection.primitive().id());
            primitiveCuePairs.add(selection.primitive().id() + "|" + selection.presentationCue());
        }
        assertEquals(20, primitiveIds.size());
        assertEquals(40, primitiveCuePairs.size());
    }

    @Test
    void explicitBoundariesPreserveTruthCertaintyAuthorityAndResolutionLimits() {
        String boundaries = NightmareEvidenceVerificationExchangeCatalog.waveOne().stream()
                .map(NightmareEvidenceVerificationExchangeCatalog.Primitive::antiOverclaimBoundary)
                .collect(Collectors.joining(" ")).toLowerCase();
        assertTrue(boundaries.contains("truth"));
        assertTrue(boundaries.contains("authentic"));
        assertTrue(boundaries.contains("guilt"));
        assertTrue(boundaries.contains("certainty") || boundaries.contains("confidence"));
        assertTrue(boundaries.contains("authority"));
        assertTrue(boundaries.contains("scenario") || boundaries.contains("world state"));
    }

    @Test
    void invalidAuthorityAndEvidenceInputsFailClosed() {
        Set<NightmareEvidenceVerificationExchangeCatalog.Family> families = EnumSet.allOf(NightmareEvidenceVerificationExchangeCatalog.Family.class);
        assertThrows(IllegalArgumentException.class, () -> NightmareEvidenceVerificationExchangeCatalog.compose(1L, " ", "actor", "evidence", families, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareEvidenceVerificationExchangeCatalog.compose(1L, "scenario", " ", "evidence", families, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareEvidenceVerificationExchangeCatalog.compose(1L, "scenario", "actor", " ", families, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareEvidenceVerificationExchangeCatalog.compose(1L, "scenario", "actor", "evidence", Set.of(), Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareEvidenceVerificationExchangeCatalog.compose(1L, "scenario", "actor", "evidence", families, Map.of("record", -1)));
    }
}