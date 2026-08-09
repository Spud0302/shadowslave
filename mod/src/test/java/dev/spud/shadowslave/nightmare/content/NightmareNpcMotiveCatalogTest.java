package dev.spud.shadowslave.nightmare.content;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightmareNpcMotiveCatalogTest {
    @Test
    void waveOneProvidesFourPrimitivesForEveryFamily() {
        assertEquals(28, NightmareNpcMotiveCatalog.waveOne().size());
        assertEquals(28, NightmareNpcMotiveCatalog.waveOne().stream().map(NightmareNpcMotiveCatalog.MotivePrimitive::id).distinct().count());

        for (NightmareNpcMotiveCatalog.MotiveFamily family : NightmareNpcMotiveCatalog.MotiveFamily.values()) {
            assertEquals(4, NightmareNpcMotiveCatalog.byFamily().get(family).size(), family.name());
        }
    }

    @Test
    void everyPrimitiveHasSubstantivePlayerFacingHooksAndBoundary() {
        for (NightmareNpcMotiveCatalog.MotivePrimitive primitive : NightmareNpcMotiveCatalog.waveOne()) {
            assertFalse(primitive.title().isBlank(), primitive.id());
            assertTrue(primitive.motiveRead().length() >= 50, primitive.id());
            assertEquals(2, primitive.dialogueHooks().size(), primitive.id());
            assertEquals(2, primitive.behaviorHooks().size(), primitive.id());
            assertTrue(primitive.affinityTags().size() >= 3, primitive.id());
            assertEquals(2, primitive.presentationCues().size(), primitive.id());
            assertTrue(primitive.antiOverclaimBoundary().length() >= 60, primitive.id());
        }
    }

    @Test
    void compositionPreservesCallerOwnedRoleAndAllowedFamiliesAcrossSeedSweep() {
        Set<NightmareNpcMotiveCatalog.MotiveFamily> allowed = EnumSet.of(
                NightmareNpcMotiveCatalog.MotiveFamily.DUTY,
                NightmareNpcMotiveCatalog.MotiveFamily.CONFLICTING_LOYALTY
        );

        for (long seed = 0; seed < 4096; seed++) {
            NightmareNpcMotiveCatalog.Composition composition = NightmareNpcMotiveCatalog.compose(
                    seed,
                    "survey_clerks_assistant",
                    allowed,
                    Map.of()
            );

            assertEquals("survey_clerks_assistant", composition.historicalRoleId());
            assertTrue(allowed.contains(composition.primitive().family()));
            assertEquals(seed, composition.seed());
            assertEquals(NightmareNpcMotiveCatalog.GENERATOR_VERSION, composition.generatorVersion());
            assertTrue(composition.primitive().presentationCues().contains(composition.presentationCue()));
        }
    }

    @Test
    void positiveEvidenceCanPreferCompatibleAuthoredMotivesWithoutUsingMagnitude() {
        Set<NightmareNpcMotiveCatalog.MotiveFamily> allowed = EnumSet.allOf(NightmareNpcMotiveCatalog.MotiveFamily.class);

        for (long seed = 0; seed < 256; seed++) {
            NightmareNpcMotiveCatalog.Composition one = NightmareNpcMotiveCatalog.compose(
                    seed,
                    "hostage_interpreter",
                    allowed,
                    Map.of("evidence", 1, "relationship", 1)
            );
            NightmareNpcMotiveCatalog.Composition huge = NightmareNpcMotiveCatalog.compose(
                    seed,
                    "hostage_interpreter",
                    allowed,
                    Map.of("relationship", 999, "evidence", 5000)
            );

            assertEquals(one.primitive().id(), huge.primitive().id());
            assertEquals(one.presentationCue(), huge.presentationCue());
            assertEquals(one.matchedEvidenceTags(), huge.matchedEvidenceTags());
            assertTrue(one.matchedEvidenceTags() > 0);
        }
    }

    @Test
    void evidenceMapOrderDoesNotChangeComposition() {
        Map<String, Integer> first = new HashMap<>();
        first.put("relationship", 1);
        first.put("loyalty", 1);
        Map<String, Integer> second = new HashMap<>();
        second.put("loyalty", 1);
        second.put("relationship", 1);

        NightmareNpcMotiveCatalog.Composition a = NightmareNpcMotiveCatalog.compose(
                73,
                "span_ward_runner",
                EnumSet.allOf(NightmareNpcMotiveCatalog.MotiveFamily.class),
                first
        );
        NightmareNpcMotiveCatalog.Composition b = NightmareNpcMotiveCatalog.compose(
                73,
                "span_ward_runner",
                EnumSet.allOf(NightmareNpcMotiveCatalog.MotiveFamily.class),
                second
        );

        assertEquals(a, b);
    }

    @Test
    void everyPrimitiveAndCueIsReachableUnderNeutralAllFamilyComposition() {
        Set<String> primitiveIds = new HashSet<>();
        Set<String> primitiveCuePairs = new HashSet<>();

        for (long seed = 0; seed < 16384; seed++) {
            NightmareNpcMotiveCatalog.Composition composition = NightmareNpcMotiveCatalog.compose(
                    seed,
                    "neutral_role",
                    EnumSet.allOf(NightmareNpcMotiveCatalog.MotiveFamily.class),
                    Map.of()
            );
            primitiveIds.add(composition.primitive().id());
            primitiveCuePairs.add(composition.primitive().id() + "|" + composition.presentationCue());
        }

        assertEquals(28, primitiveIds.size());
        assertEquals(56, primitiveCuePairs.size());
    }

    @Test
    void compositionFailsClosedOnInvalidAuthorityInputs() {
        assertThrows(IllegalArgumentException.class, () -> NightmareNpcMotiveCatalog.compose(
                1,
                " ",
                EnumSet.of(NightmareNpcMotiveCatalog.MotiveFamily.DUTY),
                Map.of()
        ));
        assertThrows(IllegalArgumentException.class, () -> NightmareNpcMotiveCatalog.compose(
                1,
                "role",
                Set.of(),
                Map.of()
        ));
        assertThrows(IllegalArgumentException.class, () -> NightmareNpcMotiveCatalog.compose(
                1,
                "role",
                EnumSet.of(NightmareNpcMotiveCatalog.MotiveFamily.DUTY),
                Map.of("duty", -1)
        ));
    }

    @Test
    void highRiskPrimitivesExplicitlyRefuseTruthAllegianceAndOutcomeAuthority() {
        String joined = NightmareNpcMotiveCatalog.waveOne().stream()
                .map(NightmareNpcMotiveCatalog.MotivePrimitive::antiOverclaimBoundary)
                .reduce("", (left, right) -> left + " " + right)
                .toLowerCase();

        assertTrue(joined.contains("lie detection"));
        assertTrue(joined.contains("allegiance"));
        assertTrue(joined.contains("persuasion"));
        assertTrue(joined.contains("appraisal"));
        assertTrue(joined.contains("canonical"));
    }
}
