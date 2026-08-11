package dev.spud.shadowslave.dreamrealm;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
                            && encounter.pressure() == StormLanternCoastEncounterPlan.Pressure.FLOOD_EDGE
                            && encounter.ecologyContext() == StormLanternCoastEncounterPlan.EcologyContext.FLOOD_MARGIN));
            assertTrue(plan.encounters().stream().anyMatch(encounter ->
                    encounter.pressure() == StormLanternCoastEncounterPlan.Pressure.RUIN_GUARD
                            && encounter.creatureId().equals("chainback")
                            && encounter.ecologyContext() == StormLanternCoastEncounterPlan.EcologyContext.HISTORIC_RUIN));
        }
    }

    @Test
    void optionalPressureRespondsToExposureResourcesAndShelterMargins() {
        Set<StormLanternCoastEncounterPlan.EcologyContext> seen = new HashSet<>();
        Set<String> resourceAnchors = new HashSet<>();
        Set<String> shelterAnchors = new HashSet<>();

        for (long seed = 1; seed <= 2048; seed++) {
            var site = StormLanternCoastSitePlan.drownedBellLater(seed);
            var plan = StormLanternCoastEncounterPlan.forSite(site);
            var shelter = site.pieces().stream()
                    .filter(piece -> piece.anchorId().equals("storm_shelter"))
                    .findFirst().orElseThrow();

            for (var encounter : plan.encounters()) {
                if (encounter.pressure() != StormLanternCoastEncounterPlan.Pressure.EXPOSED_ROUTE) continue;
                seen.add(encounter.ecologyContext());
                if (encounter.ecologyContext() == StormLanternCoastEncounterPlan.EcologyContext.RESOURCE_EDGE) {
                    resourceAnchors.add(encounter.anchorId());
                }
                if (encounter.ecologyContext() == StormLanternCoastEncounterPlan.EcologyContext.SHELTER_MARGIN) {
                    shelterAnchors.add(encounter.anchorId());
                    assertTrue(Math.abs(encounter.x() - shelter.x()) >= 8,
                            "shelter pressure must stay on the approach margin rather than inside the shelter");
                }
            }
        }

        assertTrue(seen.contains(StormLanternCoastEncounterPlan.EcologyContext.HIGH_EXPOSURE));
        assertTrue(seen.contains(StormLanternCoastEncounterPlan.EcologyContext.RESOURCE_EDGE));
        assertTrue(seen.contains(StormLanternCoastEncounterPlan.EcologyContext.SHELTER_MARGIN));
        assertEquals(Set.of("salvage_ledge"), resourceAnchors);
        assertEquals(Set.of("storm_shelter"), shelterAnchors);
    }

    @Test
    void optionalSelectionDoesNotDuplicateOneContextAnchorWithinAPlan() {
        for (long seed = 1; seed <= 512; seed++) {
            var plan = StormLanternCoastEncounterPlan.forSite(StormLanternCoastSitePlan.drownedBellLater(seed));
            Set<String> optionalAnchors = new HashSet<>();
            for (var encounter : plan.encounters()) {
                if (encounter.pressure() == StormLanternCoastEncounterPlan.Pressure.EXPOSED_ROUTE) {
                    assertTrue(optionalAnchors.add(encounter.anchorId()),
                            "duplicate optional ecology anchor in seed " + seed + ": " + encounter.anchorId());
                }
            }
        }
    }

    @Test
    void progressionAndSettlementDistanceBoundOptionalPressure() {
        var unrankedNear = new StormLanternCoastEncounterPlan.EncounterContext(
                StormLanternCoastEncounterPlan.ProgressionBand.UNRANKED, 48);
        var dormantNear = new StormLanternCoastEncounterPlan.EncounterContext(
                StormLanternCoastEncounterPlan.ProgressionBand.DORMANT, 96);
        var awakenedNear = new StormLanternCoastEncounterPlan.EncounterContext(
                StormLanternCoastEncounterPlan.ProgressionBand.AWAKENED_OR_HIGHER, 96);
        var unrankedRemote = new StormLanternCoastEncounterPlan.EncounterContext(
                StormLanternCoastEncounterPlan.ProgressionBand.UNRANKED, 224);
        var dormantRemote = new StormLanternCoastEncounterPlan.EncounterContext(
                StormLanternCoastEncounterPlan.ProgressionBand.DORMANT, 224);

        assertEquals(0, StormLanternCoastEncounterPlan.optionalCapacity(unrankedNear));
        assertEquals(0, StormLanternCoastEncounterPlan.optionalCapacity(dormantNear));
        assertEquals(1, StormLanternCoastEncounterPlan.optionalCapacity(awakenedNear));
        assertEquals(1, StormLanternCoastEncounterPlan.optionalCapacity(unrankedRemote));
        assertEquals(2, StormLanternCoastEncounterPlan.optionalCapacity(dormantRemote));
    }

    @Test
    void dormantOptionalPressureDoesNotAddAwakenedChainbacks() {
        var context = new StormLanternCoastEncounterPlan.EncounterContext(
                StormLanternCoastEncounterPlan.ProgressionBand.DORMANT, 224);
        for (long seed = 1; seed <= 1024; seed++) {
            var plan = StormLanternCoastEncounterPlan.forSite(StormLanternCoastSitePlan.drownedBellLater(seed), context);
            for (var encounter : plan.encounters()) {
                if (encounter.pressure() == StormLanternCoastEncounterPlan.Pressure.EXPOSED_ROUTE) {
                    assertEquals("drowned_listener", encounter.creatureId());
                }
            }
        }
    }

    @Test
    void awakenedBandCanAddEitherExecutableAffinity() {
        var context = new StormLanternCoastEncounterPlan.EncounterContext(
                StormLanternCoastEncounterPlan.ProgressionBand.AWAKENED_OR_HIGHER, 224);
        Set<String> optionalCreatures = new HashSet<>();
        for (long seed = 1; seed <= 2048; seed++) {
            var plan = StormLanternCoastEncounterPlan.forSite(StormLanternCoastSitePlan.drownedBellLater(seed), context);
            plan.encounters().stream()
                    .filter(encounter -> encounter.pressure() == StormLanternCoastEncounterPlan.Pressure.EXPOSED_ROUTE)
                    .map(StormLanternCoastEncounterPlan.Encounter::creatureId)
                    .forEach(optionalCreatures::add);
        }

        assertEquals(Set.of("drowned_listener", "chainback"), optionalCreatures);
    }

    @Test
    void nearSettlementAndUnrankedContextAlwaysLeavesOnlyLandmarkPressures() {
        var context = new StormLanternCoastEncounterPlan.EncounterContext(
                StormLanternCoastEncounterPlan.ProgressionBand.UNRANKED, 24);
        for (long seed = 1; seed <= 256; seed++) {
            var plan = StormLanternCoastEncounterPlan.forSite(StormLanternCoastSitePlan.drownedBellLater(seed), context);
            assertEquals(2, plan.encounters().size());
        }
    }

    @Test
    void contextRejectsNegativeSettlementDistance() {
        assertThrows(IllegalArgumentException.class, () -> new StormLanternCoastEncounterPlan.EncounterContext(
                StormLanternCoastEncounterPlan.ProgressionBand.DORMANT, -1));
    }

    @Test
    void currentDevelopmentSettlementIsRemoteFromStormLanternCoast() {
        assertEquals(224, StormLanternCoastEncounterService.nearestPhysicalSettlementBlocks());
        assertEquals(StormLanternCoastEncounterPlan.SettlementDistanceBand.REMOTE,
                StormLanternCoastEncounterPlan.EncounterContext.developmentFixture().settlementDistanceBand());
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
