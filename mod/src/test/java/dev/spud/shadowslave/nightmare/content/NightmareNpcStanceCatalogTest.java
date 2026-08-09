package dev.spud.shadowslave.nightmare.content;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightmareNpcStanceCatalogTest {
    @Test
    void waveOneHasFourPrimitivesPerFamily() {
        var all = NightmareNpcStanceCatalog.waveOne();
        assertEquals(24, all.size());
        assertEquals(24, all.stream().map(NightmareNpcStanceCatalog.StancePrimitive::id).distinct().count());

        for (NightmareNpcStanceCatalog.StanceFamily family : NightmareNpcStanceCatalog.StanceFamily.values()) {
            assertEquals(4, all.stream().filter(value -> value.family() == family).count(), family.name());
        }

        for (var primitive : all) {
            assertFalse(primitive.title().isBlank());
            assertFalse(primitive.responseRead().isBlank());
            assertEquals(2, primitive.spokenHooks().size());
            assertEquals(2, primitive.playerLevers().size());
            assertFalse(primitive.affinityTags().isEmpty());
            assertEquals(2, primitive.presentationCues().size());
            assertFalse(primitive.antiOverclaimBoundary().isBlank());
        }
    }

    @Test
    void compositionPreservesCallerOwnedActorAndAllowedFamilies() {
        String actor = "hollow_treaty/interpreter_companion";
        Set<NightmareNpcStanceCatalog.StanceFamily> allowed = EnumSet.of(
                NightmareNpcStanceCatalog.StanceFamily.WARNING,
                NightmareNpcStanceCatalog.StanceFamily.CONDITIONAL_HELP
        );

        for (long seed = 0; seed < 4096; seed++) {
            var result = NightmareNpcStanceCatalog.compose(seed, actor, allowed, Map.of("evidence", 1));
            assertEquals(actor, result.actorContextId());
            assertTrue(allowed.contains(result.primitive().family()));
            assertEquals(NightmareNpcStanceCatalog.GENERATOR_VERSION, result.generatorVersion());
        }
    }

    @Test
    void sameInputIsDeterministicAndEvidenceMapOrderDoesNotMatter() {
        Map<String, Integer> first = new LinkedHashMap<>();
        first.put("route", 1);
        first.put("warning", 1);
        Map<String, Integer> second = new LinkedHashMap<>();
        second.put("warning", 1);
        second.put("route", 1);

        var families = EnumSet.allOf(NightmareNpcStanceCatalog.StanceFamily.class);
        var a = NightmareNpcStanceCatalog.compose(712L, "actor/one", families, first);
        var b = NightmareNpcStanceCatalog.compose(712L, "actor/one", families, second);
        assertEquals(a, b);
    }

    @Test
    void evidenceMagnitudeDoesNotBecomePersuasionOrDifficultyMath() {
        var families = EnumSet.allOf(NightmareNpcStanceCatalog.StanceFamily.class);
        var low = NightmareNpcStanceCatalog.compose(91L, "actor/two", families, Map.of("warning", 1, "route", 1));
        var high = NightmareNpcStanceCatalog.compose(91L, "actor/two", families, Map.of("warning", 999, "route", 999));
        assertEquals(low, high);
    }

    @Test
    void compatibleEvidenceCanPreferMatchingPresentationWithoutAuthorizingOutcome() {
        var result = NightmareNpcStanceCatalog.compose(
                33L,
                "actor/witness",
                EnumSet.of(NightmareNpcStanceCatalog.StanceFamily.WARNING),
                Map.of("evidence", 1, "verification", 1)
        );
        assertTrue(result.matchedEvidenceTags() > 0);
        assertTrue(result.primitive().affinityTags().contains("evidence") || result.primitive().affinityTags().contains("verification"));
        assertTrue(result.primitive().antiOverclaimBoundary().toLowerCase().contains("does not"));
    }

    @Test
    void neutralCompositionCanReachEveryPrimitiveAndCue() {
        Set<String> primitiveIds = new HashSet<>();
        Set<String> primitiveCuePairs = new HashSet<>();
        var families = EnumSet.allOf(NightmareNpcStanceCatalog.StanceFamily.class);

        for (long seed = 0; seed < 16384; seed++) {
            var result = NightmareNpcStanceCatalog.compose(seed, "actor/reachability", families, Map.of());
            primitiveIds.add(result.primitive().id());
            primitiveCuePairs.add(result.primitive().id() + "|" + result.presentationCue());
        }

        assertEquals(24, primitiveIds.size());
        assertEquals(48, primitiveCuePairs.size());
    }

    @Test
    void catalogueKeepsHighRiskAuthorityOutOfPresentation() {
        String combined = NightmareNpcStanceCatalog.waveOne().stream()
                .map(value -> value.responseRead() + " " + value.antiOverclaimBoundary())
                .reduce("", (a, b) -> a + " " + b)
                .toLowerCase();

        assertTrue(combined.contains("truth"));
        assertTrue(combined.contains("allegiance"));
        assertTrue(combined.contains("persuasion"));
        assertTrue(combined.contains("safe-zone") || combined.contains("safe zone"));
        assertTrue(combined.contains("scenario"));
    }

    @Test
    void invalidAuthorityInputsFailClosed() {
        var allFamilies = EnumSet.allOf(NightmareNpcStanceCatalog.StanceFamily.class);
        assertThrows(IllegalArgumentException.class,
                () -> NightmareNpcStanceCatalog.compose(1L, " ", allFamilies, Map.of()));
        assertThrows(IllegalArgumentException.class,
                () -> NightmareNpcStanceCatalog.compose(1L, "actor", Set.of(), Map.of()));
        assertThrows(IllegalArgumentException.class,
                () -> NightmareNpcStanceCatalog.compose(1L, "actor", allFamilies, null));
        assertThrows(IllegalArgumentException.class,
                () -> NightmareNpcStanceCatalog.compose(1L, "actor", allFamilies, Map.of("warning", -1)));
    }
}
