package dev.spud.shadowslave.dreamrealm.content;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DreamRealmShelterCampMicroModuleCatalogTest {
    @Test
    void waveOneProvidesExactlyTwoModulesForEveryMergedRegion() {
        List<DreamRealmShelterCampMicroModuleCatalog.CampModule> modules = DreamRealmShelterCampMicroModuleCatalog.waveOne();
        Set<String> regionIds = DreamRealmRegionContentCatalog.waveOne().stream()
                .map(DreamRealmRegionContentCatalog.RegionProfile::id)
                .collect(Collectors.toSet());

        assertEquals(20, modules.size());
        assertEquals(20, modules.stream().map(DreamRealmShelterCampMicroModuleCatalog.CampModule::id).distinct().count());
        assertEquals(regionIds, modules.stream().map(DreamRealmShelterCampMicroModuleCatalog.CampModule::regionId).collect(Collectors.toSet()));
        for (String regionId : regionIds) {
            assertEquals(2, modules.stream().filter(module -> module.regionId().equals(regionId)).count(), regionId);
        }
    }

    @Test
    void everyModuleUsesOnlySourceRegionHazardsAndOpportunities() {
        Map<String, DreamRealmRegionContentCatalog.RegionProfile> regions = DreamRealmRegionContentCatalog.waveOne().stream()
                .collect(Collectors.toMap(DreamRealmRegionContentCatalog.RegionProfile::id, region -> region));

        for (DreamRealmShelterCampMicroModuleCatalog.CampModule module : DreamRealmShelterCampMicroModuleCatalog.waveOne()) {
            DreamRealmRegionContentCatalog.RegionProfile region = regions.get(module.regionId());
            assertTrue(region.hazards().contains(module.pressure()), module.id());
            assertTrue(region.opportunities().contains(module.opportunity()), module.id());
            assertEquals(2, module.approachCues().size(), module.id());
            assertEquals(3, module.choices().size(), module.id());
            assertFalse(module.situation().isBlank(), module.id());
            assertFalse(module.decisionPrompt().isBlank(), module.id());
            assertFalse(module.antiOverclaimBoundary().isBlank(), module.id());
        }
    }

    @Test
    void allShelterDecisionFamiliesAreRepresented() {
        EnumSet<DreamRealmShelterCampMicroModuleCatalog.CampFamily> families = EnumSet.noneOf(DreamRealmShelterCampMicroModuleCatalog.CampFamily.class);
        DreamRealmShelterCampMicroModuleCatalog.waveOne().forEach(module -> families.add(module.family()));
        assertEquals(EnumSet.allOf(DreamRealmShelterCampMicroModuleCatalog.CampFamily.class), families);
    }

    @Test
    void compositionIsDeterministicAndCannotMutateRegionOrModuleMechanics() {
        for (DreamRealmRegionContentCatalog.RegionProfile region : DreamRealmRegionContentCatalog.waveOne()) {
            Set<String> reachedModules = new HashSet<>();
            Set<String> reachedModuleCuePairs = new HashSet<>();

            for (long seed = 0; seed < 2048; seed++) {
                DreamRealmShelterCampMicroModuleCatalog.ResolvedCampModule first = DreamRealmShelterCampMicroModuleCatalog.compose(seed, region.id());
                DreamRealmShelterCampMicroModuleCatalog.ResolvedCampModule second = DreamRealmShelterCampMicroModuleCatalog.compose(seed, region.id());
                DreamRealmShelterCampMicroModuleCatalog.CampModule source = DreamRealmShelterCampMicroModuleCatalog.require(first.module().id());

                assertEquals(first, second, region.id() + ":" + seed);
                assertEquals(region.id(), first.regionId(), region.id() + ":" + seed);
                assertEquals(region.id(), first.module().regionId(), region.id() + ":" + seed);
                assertEquals(source.family(), first.module().family(), source.id());
                assertEquals(source.pressure(), first.module().pressure(), source.id());
                assertEquals(source.opportunity(), first.module().opportunity(), source.id());
                assertEquals(source.situation(), first.module().situation(), source.id());
                assertEquals(source.decisionPrompt(), first.module().decisionPrompt(), source.id());
                assertEquals(source.choices(), first.module().choices(), source.id());
                assertEquals(source.antiOverclaimBoundary(), first.module().antiOverclaimBoundary(), source.id());
                assertTrue(source.approachCues().contains(first.approachCue()), source.id());

                reachedModules.add(source.id());
                reachedModuleCuePairs.add(source.id() + "|" + first.approachCue());
            }

            assertEquals(2, reachedModules.size(), region.id());
            assertEquals(4, reachedModuleCuePairs.size(), region.id());
        }
    }

    @Test
    void antiOverclaimBoundariesPreserveUnknownSafetyRecoveryAndResourceRules() {
        String allBoundaries = DreamRealmShelterCampMicroModuleCatalog.waveOne().stream()
                .map(DreamRealmShelterCampMicroModuleCatalog.CampModule::antiOverclaimBoundary)
                .collect(Collectors.joining(" "))
                .toLowerCase();

        assertTrue(allBoundaries.contains("safe zone"));
        assertTrue(allBoundaries.contains("recovery"));
        assertTrue(allBoundaries.contains("encounter"));
        assertTrue(allBoundaries.contains("rewards"));
        assertTrue(allBoundaries.contains("edibility"));
        assertTrue(allBoundaries.contains("forecast"));
        assertTrue(allBoundaries.contains("truth"));
    }

    @Test
    void unknownRegionAndModuleIdsFailClosed() {
        assertThrows(IllegalArgumentException.class, () -> DreamRealmShelterCampMicroModuleCatalog.compose(1L, "not_a_region"));
        assertThrows(IllegalArgumentException.class, () -> DreamRealmShelterCampMicroModuleCatalog.require("not_a_module"));
        assertThrows(IllegalArgumentException.class, () -> DreamRealmShelterCampMicroModuleCatalog.compose(1L, "BAD-ID"));
    }
}
