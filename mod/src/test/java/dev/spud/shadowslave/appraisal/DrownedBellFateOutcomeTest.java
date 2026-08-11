package dev.spud.shadowslave.appraisal;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DrownedBellFateOutcomeTest {
    @Test
    void eachTerminalResolutionCreditsOnlyProvenHistoricalChanges() {
        assertEquals(Map.of(
                "warning_bell", "sounded",
                "lower_village", "warned"
        ), DrownedBellFateOutcome.resolvedHistory("tower_held"));
        assertEquals(Map.of(
                "quarry_route", "opened",
                "lower_village", "evacuated"
        ), DrownedBellFateOutcome.resolvedHistory("villagers_evacuated"));
        assertEquals(Map.of(
                "sea_gate", "diverted",
                "lower_village", "spared"
        ), DrownedBellFateOutcome.resolvedHistory("flood_diverted"));
        assertEquals(Map.of(
                "warning_bell", "used_as_lure",
                "drowned_listener", "buried"
        ), DrownedBellFateOutcome.resolvedHistory("creature_buried"));
    }

    @Test
    void divergenceComesFromOriginalHistoryWeightsNotDeedFlavourWeights() {
        var tower = DrownedBellFateOutcome.appraise("tower_held");
        var evacuation = DrownedBellFateOutcome.appraise("villagers_evacuated");
        var flood = DrownedBellFateOutcome.appraise("flood_diverted");
        var creature = DrownedBellFateOutcome.appraise("creature_buried");

        assertEquals(7, tower.score());
        assertEquals(6, evacuation.score());
        assertEquals(7, flood.score());
        assertEquals(6, creature.score());
        assertEquals(15, tower.maximumScore());
        assertTrue(tower.unknownAxes().contains("drowned_listener"));
        assertTrue(creature.unknownAxes().contains("lower_village"));
    }

    @Test
    void unknownTerminalResolutionFailsClosed() {
        assertThrows(IllegalArgumentException.class, () -> DrownedBellFateOutcome.resolvedHistory("invented_ending"));
    }
}
