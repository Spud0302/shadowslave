package dev.spud.shadowslave.nightmare.content;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightmarePressureComplicationCatalogTest {
    @Test
    void waveOneProvidesFourAuthoredPrimitivesPerFamily() {
        var primitives = NightmarePressureComplicationCatalog.primitives();
        assertEquals(24, primitives.size());
        assertEquals(24, primitives.stream().map(NightmarePressureComplicationCatalog.Primitive::id).distinct().count());

        for (NightmarePressureComplicationCatalog.Family family : NightmarePressureComplicationCatalog.Family.values()) {
            assertEquals(4, primitives.stream().filter(primitive -> primitive.family() == family).count(), family.name());
        }

        primitives.forEach(primitive -> {
            assertEquals(3, primitive.responseHooks().size(), primitive.id());
            assertTrue(primitive.affinityTags().size() >= 3, primitive.id());
            assertEquals(2, primitive.presentationCues().size(), primitive.id());
            assertTrue(primitive.antiOverclaimBoundary().length() > 60, primitive.id());
        });
    }

    @Test
    void sameInputsProduceSameComposition() {
        var families = EnumSet.of(
                NightmarePressureComplicationCatalog.Family.MISINFORMATION,
                NightmarePressureComplicationCatalog.Family.DIVIDED_OBLIGATION
        );
        Map<String, Integer> evidence = Map.of("warning", 1, "evidence", 1);

        assertEquals(
                NightmarePressureComplicationCatalog.compose(731L, "resolved_scenario", families, evidence),
                NightmarePressureComplicationCatalog.compose(731L, "resolved_scenario", families, evidence)
        );
    }

    @Test
    void evidenceMagnitudeCannotBecomeAHiddenDifficultyFormula() {
        var families = EnumSet.allOf(NightmarePressureComplicationCatalog.Family.class);

        var one = NightmarePressureComplicationCatalog.compose(29L, "resolved_scenario", families, Map.of("route", 1));
        var huge = NightmarePressureComplicationCatalog.compose(29L, "resolved_scenario", families, Map.of("route", 999));

        assertEquals(one, huge);
    }

    @Test
    void positiveEvidenceCanPreferACompatibleAuthoredPrimitiveWithoutClaimingTruth() {
        var result = NightmarePressureComplicationCatalog.compose(
                11L,
                "resolved_scenario",
                EnumSet.of(NightmarePressureComplicationCatalog.Family.MISINFORMATION),
                Map.of("marker", 1)
        );

        assertEquals("false_route_marker", result.primitive().id());
        assertTrue(result.primitive().affinityTags().contains("marker"));
    }

    @Test
    void seedCannotChangeCallerOwnedScenarioIdentityOrEscapeAllowedFamilies() {
        String scenarioId = "java_owned_scenario_identity";
        Set<NightmarePressureComplicationCatalog.Family> allowed = EnumSet.of(
                NightmarePressureComplicationCatalog.Family.RESOURCE_LOSS,
                NightmarePressureComplicationCatalog.Family.TIME_SENSITIVE_ROUTE_CHANGE
        );

        for (long seed = 0; seed < 4096; seed++) {
            var result = NightmarePressureComplicationCatalog.compose(seed, scenarioId, allowed, Map.of());
            assertEquals(scenarioId, result.resolvedScenarioId());
            assertTrue(allowed.contains(result.primitive().family()));
            assertEquals(NightmarePressureComplicationCatalog.GENERATOR_VERSION, result.generatorVersion());
        }
    }

    @Test
    void authoredCatalogueAndCueVariationAreReachableDeterministically() {
        Set<String> reachedPrimitiveIds = new HashSet<>();
        Set<String> reachedPrimitiveCuePairs = new HashSet<>();
        var families = EnumSet.allOf(NightmarePressureComplicationCatalog.Family.class);

        for (long seed = 0; seed < 65536 && reachedPrimitiveCuePairs.size() < 48; seed++) {
            var result = NightmarePressureComplicationCatalog.compose(seed, "reachability_probe", families, Map.of());
            reachedPrimitiveIds.add(result.primitive().id());
            reachedPrimitiveCuePairs.add(result.primitive().id() + "|" + result.presentationCue());
        }

        assertEquals(24, reachedPrimitiveIds.size());
        assertEquals(48, reachedPrimitiveCuePairs.size());
    }

    @Test
    void boundariesExplicitlyRejectCanonicalFormulaAndAuthorityOverclaims() {
        String boundaries = NightmarePressureComplicationCatalog.primitives().stream()
                .map(NightmarePressureComplicationCatalog.Primitive::antiOverclaimBoundary)
                .reduce("", (left, right) -> left + " " + right)
                .toLowerCase();

        assertTrue(boundaries.contains("probability"));
        assertTrue(boundaries.contains("difficulty"));
        assertTrue(boundaries.contains("canonical"));
        assertTrue(boundaries.contains("java-owned"));
        assertTrue(boundaries.contains("appraisal"));
        assertFalse(boundaries.contains("canonical generation formula is"));
    }

    @Test
    void malformedInputsFailClosed() {
        var families = EnumSet.of(NightmarePressureComplicationCatalog.Family.MISINFORMATION);

        assertThrows(IllegalArgumentException.class,
                () -> NightmarePressureComplicationCatalog.compose(0L, " ", families, Map.of()));
        assertThrows(IllegalArgumentException.class,
                () -> NightmarePressureComplicationCatalog.compose(0L, "scenario", Set.of(), Map.of()));
        assertThrows(IllegalArgumentException.class,
                () -> NightmarePressureComplicationCatalog.compose(0L, "scenario", families, Map.of("route", -1)));
        assertTrue(NightmarePressureComplicationCatalog.byId("missing_primitive").isEmpty());
    }
}
