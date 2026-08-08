package dev.spud.shadowslave.nightmare.content;

import dev.spud.shadowslave.nightmare.LastSignalScenario;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightmareRoleScenarioCompatibilityCatalogTest {
    @Test
    void waveOneCoversCurrentAuthoredFirstNightmareFamilies() {
        var modules = NightmareRoleScenarioCompatibilityCatalog.waveOne();
        assertEquals(2, modules.size());
        assertEquals(
                Set.of(LastSignalScenario.SCENARIO_ID, DrownedBellScenarioDefinition.SCENARIO_ID),
                modules.stream().map(NightmareRoleScenarioCompatibilityCatalog.ScenarioCompatibility::scenarioId).collect(java.util.stream.Collectors.toSet())
        );

        Set<String> knownRoles = NightmareRoleContentCatalog.waveOne().stream()
                .map(NightmareRoleContentCatalog.RoleProfile::id)
                .collect(java.util.stream.Collectors.toSet());

        modules.forEach(module -> {
            assertTrue(module.variants().size() >= 6);
            assertFalse(module.scenarioAffinities().isEmpty());
            assertFalse(module.compatibilityNote().isBlank());
            Set<String> ids = new HashSet<>();
            module.variants().forEach(variant -> {
                assertTrue(ids.add(variant.roleId()));
                assertTrue(knownRoles.contains(variant.roleId()), () -> "unknown role " + variant.roleId());
                assertTrue(variant.baseWeight() > 0);
                assertFalse(variant.entryHook().isBlank());
                assertFalse(variant.conflictPressure().isBlank());
                assertFalse(variant.leverage().isBlank());
            });
        });
    }

    @Test
    void matcherNeverEscapesScenarioCompatibility() {
        for (var module : NightmareRoleScenarioCompatibilityCatalog.waveOne()) {
            Set<String> allowed = module.variants().stream()
                    .map(NightmareRoleScenarioCompatibilityCatalog.RoleVariant::roleId)
                    .collect(java.util.stream.Collectors.toSet());
            for (long seed = 0; seed < 2048; seed++) {
                var match = NightmareRoleScenarioCompatibilityCatalog.match(
                        module.scenarioId(), seed, Map.of("warning", 3, "water", 2, "route", 2));
                assertTrue(allowed.contains(match.role().id()));
                assertEquals(match.role().id(), match.variant().roleId());
                assertEquals(module.scenarioId(), match.scenarioId());
            }
        }
    }

    @Test
    void matcherIsDeterministicAndEvidenceOrderIndependent() {
        Map<String, Integer> first = new HashMap<>();
        first.put("water", 4);
        first.put("rescue", 3);
        first.put("warning", 2);

        Map<String, Integer> second = new HashMap<>();
        second.put("warning", 2);
        second.put("rescue", 3);
        second.put("water", 4);

        var a = NightmareRoleScenarioCompatibilityCatalog.match(DrownedBellScenarioDefinition.SCENARIO_ID, 99117L, first);
        var b = NightmareRoleScenarioCompatibilityCatalog.match(DrownedBellScenarioDefinition.SCENARIO_ID, 99117L, second);
        assertEquals(a, b);
    }

    @Test
    void authoredEvidenceBiasesCompatibleRolesWithoutBecomingAUniversalFormula() {
        int waterCompatibleWithEvidence = 0;
        int waterCompatibleNeutral = 0;
        for (long seed = 0; seed < 4096; seed++) {
            var biased = NightmareRoleScenarioCompatibilityCatalog.match(
                    DrownedBellScenarioDefinition.SCENARIO_ID, seed, Map.of("water", 10));
            var neutral = NightmareRoleScenarioCompatibilityCatalog.match(
                    DrownedBellScenarioDefinition.SCENARIO_ID, seed, Map.of());
            if (biased.role().evidenceAffinities().contains("water")) {
                waterCompatibleWithEvidence++;
            }
            if (neutral.role().evidenceAffinities().contains("water")) {
                waterCompatibleNeutral++;
            }
        }
        assertTrue(waterCompatibleWithEvidence > waterCompatibleNeutral,
                () -> waterCompatibleWithEvidence + " should exceed " + waterCompatibleNeutral);
    }

    @Test
    void scenarioVariantsProduceMeaningfulDiversity() {
        for (var module : NightmareRoleScenarioCompatibilityCatalog.waveOne()) {
            Set<String> selected = new HashSet<>();
            for (long seed = 0; seed < 2048; seed++) {
                selected.add(NightmareRoleScenarioCompatibilityCatalog.match(module.scenarioId(), seed, Map.of()).role().id());
            }
            assertTrue(selected.size() >= 4, () -> module.scenarioId() + " selected only " + selected);
        }
    }

    @Test
    void invalidInputsFailClosed() {
        assertThrows(IllegalArgumentException.class,
                () -> NightmareRoleScenarioCompatibilityCatalog.match("unknown_scenario", 1L, Map.of()));
        assertThrows(IllegalArgumentException.class,
                () -> NightmareRoleScenarioCompatibilityCatalog.match(LastSignalScenario.SCENARIO_ID, 1L, Map.of("warning", -1)));
    }
}
