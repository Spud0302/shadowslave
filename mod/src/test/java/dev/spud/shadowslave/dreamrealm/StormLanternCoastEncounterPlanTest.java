package dev.spud.shadowslave.dreamrealm;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StormLanternCoastEncounterPlanTest {
    @Test
    void sameWorldSeedProducesExactlyTheSameEncounterBudget() {
        var first = StormLanternCoastEncounterPlan.forSite(StormLanternCoastSitePlan.drownedBellLater(91L));
        var second = StormLanternCoastEncounterPlan.forSite(StormLanternCoastSitePlan.drownedBellLater(91L));

        assertEquals(first, second);
    }

    @Test
    void encounterBudgetUsesOnlyRegionAuthorizedPhysicalCreatures() {
        var site = StormLanternCoastSitePlan.drownedBellLater(17L);
        var plan = StormLanternCoastEncounterPlan.forSite(site);

        assertTrue(plan.encounters().size() >= 2 && plan.encounters().size() <= 4);
        for (var encounter : plan.encounters()) {
            assertTrue(site.region().creatureAffinityIds().contains(encounter.creatureId()));
            assertTrue(Set.of("drowned_listener", "chainback").contains(encounter.creatureId()));
        }
    }

    @Test
    void floodedTerracesAndASecondRuinPressureAlwaysExist() {
        for (long seed = 1; seed <= 128; seed++) {
            var plan = StormLanternCoastEncounterPlan.forSite(StormLanternCoastSitePlan.drownedBellLater(seed));

            assertTrue(plan.encounters().stream().anyMatch(encounter ->
                    encounter.anchorId().equals("drowned_harbour_terraces")
                            && encounter.creatureId().equals("drowned_listener")
                            && encounter.pressure() == StormLanternCoastEncounterPlan.Pressure.FLOOD_EDGE));
            assertTrue(plan.encounters().stream().anyMatch(encounter ->
                    encounter.pressure() == StormLanternCoastEncounterPlan.Pressure.RUIN_GUARD
                            && encounter.creatureId().equals("chainback")));
        }
    }

    @Test
    void differentWorldSeedsProduceVariableBudgetsAndPlacements() {
        Set<Integer> sizes = new HashSet<>();
        Set<String> fingerprints = new HashSet<>();

        for (long seed = 1; seed <= 256; seed++) {
            var plan = StormLanternCoastEncounterPlan.forSite(StormLanternCoastSitePlan.drownedBellLater(seed));
            sizes.add(plan.encounters().size());
            fingerprints.add(plan.encounters().toString());
        }

        assertTrue(sizes.size() >= 2, () -> "expected variable encounter budgets but got " + sizes);
        assertTrue(fingerprints.size() >= 32, () -> "expected varied encounter placements but got " + fingerprints.size());
    }

    @Test
    void encounterSeedIsSeparateFromStructureSiteSeed() {
        var site = StormLanternCoastSitePlan.drownedBellLater(1234L);
        var encounters = StormLanternCoastEncounterPlan.forSite(site);

        assertNotEquals(site.siteSeed(), encounters.encounterSeed());
    }
}
