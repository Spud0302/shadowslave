package dev.spud.shadowslave.dreamrealm;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StormLanternCoastDiscoveryPlanTest {
    @Test
    void derivesOneNonOmniscientCluePerEncounter() {
        var encounters = encounters();
        var discovery = StormLanternCoastDiscoveryPlan.fromEncounters(encounters);

        assertEquals(encounters.encounters().size(), discovery.clues().size());
        for (int i = 0; i < encounters.encounters().size(); i++) {
            var encounter = encounters.encounters().get(i);
            var clue = discovery.clues().get(i);
            assertEquals(encounter.anchorId(), clue.anchorId());
            assertEquals(encounter.pressure(), clue.pressure());
            assertTrue(Math.abs(encounter.x() - clue.x()) <= 5);
            assertTrue(Math.abs(encounter.z() - clue.z()) <= 5);
            assertNotEquals(encounter.x() + ":" + encounter.z(), clue.x() + ":" + clue.z(),
                    "clue must not expose the exact hostile coordinate");
        }
    }

    @Test
    void pressureMapsToReadableClueFamily() {
        var discovery = StormLanternCoastDiscoveryPlan.fromEncounters(encounters());
        assertEquals(StormLanternCoastDiscoveryPlan.ClueKind.DISTURBED_FLOOD_EDGE, discovery.clues().get(0).kind());
        assertEquals(StormLanternCoastDiscoveryPlan.ClueKind.CHAIN_SCAR, discovery.clues().get(1).kind());
        assertEquals(StormLanternCoastDiscoveryPlan.ClueKind.EXPOSED_ROUTE_DAMAGE, discovery.clues().get(2).kind());
    }

    @Test
    void clueMovementIsBoundedTowardArrival() {
        assertEquals(5, StormLanternCoastDiscoveryPlan.toward(10, 0, 5));
        assertEquals(-5, StormLanternCoastDiscoveryPlan.toward(-10, 0, 5));
        assertEquals(0, StormLanternCoastDiscoveryPlan.toward(2, 0, 5));
        assertEquals(10, StormLanternCoastDiscoveryPlan.toward(10, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> StormLanternCoastDiscoveryPlan.toward(10, 0, -1));
    }

    @Test
    void sameEncounterPlanProducesSameDiscoveryPlan() {
        assertEquals(
                StormLanternCoastDiscoveryPlan.fromEncounters(encounters()),
                StormLanternCoastDiscoveryPlan.fromEncounters(encounters())
        );
    }

    private static StormLanternCoastEncounterPlan.Plan encounters() {
        return new StormLanternCoastEncounterPlan.Plan(
                42L,
                StormLanternCoastEncounterPlan.EncounterContext.developmentFixture(),
                List.of(
                        new StormLanternCoastEncounterPlan.Encounter(
                                "drowned_listener", "drowned_harbour_terraces",
                                StormLanternCoastEncounterPlan.Pressure.FLOOD_EDGE,
                                StormLanternCoastEncounterPlan.EcologyContext.FLOOD_MARGIN,
                                12, 3, 18),
                        new StormLanternCoastEncounterPlan.Encounter(
                                "chainback", "storm_belfry",
                                StormLanternCoastEncounterPlan.Pressure.RUIN_GUARD,
                                StormLanternCoastEncounterPlan.EcologyContext.HISTORIC_RUIN,
                                -14, 7, 2),
                        new StormLanternCoastEncounterPlan.Encounter(
                                "drowned_listener", "coast_watch_0",
                                StormLanternCoastEncounterPlan.Pressure.EXPOSED_ROUTE,
                                StormLanternCoastEncounterPlan.EcologyContext.HIGH_EXPOSURE,
                                9, 6, -8)
                )
        );
    }
}
