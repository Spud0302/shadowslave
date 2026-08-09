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

class NightmareEvidenceLinkCatalogTest {
    @Test
    void waveOneContainsFourUniquePrimitivesPerFamily() {
        List<NightmareEvidenceLinkCatalog.Primitive> primitives = NightmareEvidenceLinkCatalog.waveOne();

        assertEquals(24, primitives.size());
        assertEquals(24, primitives.stream().map(NightmareEvidenceLinkCatalog.Primitive::id).collect(Collectors.toSet()).size());
        for (NightmareEvidenceLinkCatalog.Family family : NightmareEvidenceLinkCatalog.Family.values()) {
            assertEquals(4, primitives.stream().filter(primitive -> primitive.family() == family).count(), family.name());
        }
    }

    @Test
    void everyPrimitiveHasBoundedPlayerFacingContent() {
        for (NightmareEvidenceLinkCatalog.Primitive primitive : NightmareEvidenceLinkCatalog.waveOne()) {
            assertFalse(primitive.title().isBlank());
            assertFalse(primitive.evidenceRead().isBlank());
            assertFalse(primitive.verificationQuestion().isBlank());
            assertEquals(3, primitive.playerResponses().size());
            assertEquals(2, primitive.presentationCues().size());
            assertFalse(primitive.affinityTags().isEmpty());
            assertFalse(primitive.antiOverclaimBoundary().isBlank());
        }
    }

    @Test
    void compositionIsDeterministicAndEvidenceMapOrderIndependent() {
        Set<NightmareEvidenceLinkCatalog.Family> families = EnumSet.allOf(NightmareEvidenceLinkCatalog.Family.class);
        Map<String, Integer> first = new HashMap<>();
        first.put("record", 1);
        first.put("sequence", 2);
        Map<String, Integer> second = new HashMap<>();
        second.put("sequence", 2);
        second.put("record", 1);

        NightmareEvidenceLinkCatalog.Selection left = NightmareEvidenceLinkCatalog.compose(
                42L, "scenario_alpha", "actor_beta", families, first);
        NightmareEvidenceLinkCatalog.Selection right = NightmareEvidenceLinkCatalog.compose(
                42L, "scenario_alpha", "actor_beta", families, second);

        assertEquals(left, right);
    }

    @Test
    void positiveEvidenceMagnitudeDoesNotBecomeAHiddenScoringFormula() {
        Set<NightmareEvidenceLinkCatalog.Family> families = EnumSet.allOf(NightmareEvidenceLinkCatalog.Family.class);

        NightmareEvidenceLinkCatalog.Selection small = NightmareEvidenceLinkCatalog.compose(
                991L, "scenario_alpha", "actor_beta", families, Map.of("route", 1, "record", 1));
        NightmareEvidenceLinkCatalog.Selection huge = NightmareEvidenceLinkCatalog.compose(
                991L, "scenario_alpha", "actor_beta", families, Map.of("route", 999, "record", 999));

        assertEquals(small, huge);
    }

    @Test
    void positiveEvidenceCanPreferCompatibleContentWithoutAdjudicatingTruth() {
        Set<NightmareEvidenceLinkCatalog.Family> families = EnumSet.allOf(NightmareEvidenceLinkCatalog.Family.class);

        for (long seed = 0; seed < 256; seed++) {
            NightmareEvidenceLinkCatalog.Selection selection = NightmareEvidenceLinkCatalog.compose(
                    seed, "scenario_alpha", "actor_beta", families, Map.of("authority", 1));
            assertTrue(selection.primitive().affinityTags().contains("authority"));
            assertEquals(Set.of("authority"), selection.matchedEvidenceTags());
        }
    }

    @Test
    void seedCannotEscapeCallerOwnedScenarioActorOrAllowedFamilies() {
        Set<NightmareEvidenceLinkCatalog.Family> allowed = EnumSet.of(
                NightmareEvidenceLinkCatalog.Family.PHYSICAL_RECORD,
                NightmareEvidenceLinkCatalog.Family.CONTRADICTION);

        for (long seed = 0; seed < 4096; seed++) {
            NightmareEvidenceLinkCatalog.Selection selection = NightmareEvidenceLinkCatalog.compose(
                    seed, "hollow_treaty", "interpreter_context", allowed, Map.of());
            assertEquals("hollow_treaty", selection.scenarioId());
            assertEquals("interpreter_context", selection.actorContextId());
            assertTrue(allowed.contains(selection.primitive().family()));
            assertEquals(NightmareEvidenceLinkCatalog.GENERATOR_VERSION, selection.generatorVersion());
        }
    }

    @Test
    void neutralSweepReachesAllPrimitivesAndPresentationCues() {
        Set<NightmareEvidenceLinkCatalog.Family> families = EnumSet.allOf(NightmareEvidenceLinkCatalog.Family.class);
        Set<String> primitiveIds = new HashSet<>();
        Set<String> primitiveCuePairs = new HashSet<>();

        for (long seed = 0; seed < 16384; seed++) {
            NightmareEvidenceLinkCatalog.Selection selection = NightmareEvidenceLinkCatalog.compose(
                    seed, "scenario_alpha", "actor_beta", families, Map.of());
            primitiveIds.add(selection.primitive().id());
            primitiveCuePairs.add(selection.primitive().id() + "|" + selection.presentationCue());
        }

        assertEquals(24, primitiveIds.size());
        assertEquals(48, primitiveCuePairs.size());
    }

    @Test
    void explicitBoundariesRejectTruthForgeryGuiltAndResolutionAuthority() {
        String boundaries = NightmareEvidenceLinkCatalog.waveOne().stream()
                .map(NightmareEvidenceLinkCatalog.Primitive::antiOverclaimBoundary)
                .collect(Collectors.joining(" "))
                .toLowerCase();

        assertTrue(boundaries.contains("truth") || boundaries.contains("truthful"));
        assertTrue(boundaries.contains("forgery") || boundaries.contains("authentic"));
        assertTrue(boundaries.contains("guilt") || boundaries.contains("culprit"));
        assertTrue(boundaries.contains("lie") || boundaries.contains("deception"));
        assertTrue(boundaries.contains("authority"));
    }

    @Test
    void invalidAuthorityAndEvidenceInputsFailClosed() {
        Set<NightmareEvidenceLinkCatalog.Family> families = EnumSet.allOf(NightmareEvidenceLinkCatalog.Family.class);

        assertThrows(IllegalArgumentException.class, () -> NightmareEvidenceLinkCatalog.compose(
                1L, " ", "actor", families, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareEvidenceLinkCatalog.compose(
                1L, "scenario", " ", families, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareEvidenceLinkCatalog.compose(
                1L, "scenario", "actor", Set.of(), Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareEvidenceLinkCatalog.compose(
                1L, "scenario", "actor", families, Map.of("route", -1)));
    }
}
