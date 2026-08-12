package dev.spud.shadowslave.dreamrealm;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DrownedBellHistoricalSitePlanTest {
    @Test
    void historicalPlanUsesTheAuthoredDrownedBellSiteAndScenario() {
        var plan = DrownedBellHistoricalSitePlan.drownedBell();

        assertEquals("drowned_bell_cliff_settlement", plan.site().id());
        assertEquals("the_drowned_bell", plan.scenario().id());
        assertEquals(4, plan.pieces().size());
    }

    @Test
    void historicalAnchorsOccupySameLocalGeographyAsLaterRuins() {
        var historical = DrownedBellHistoricalSitePlan.drownedBell();
        var later = StormLanternCoastSitePlan.drownedBellLater(12345L);

        Map<String, DrownedBellHistoricalSitePlan.Piece> historicalById = historical.pieces().stream()
                .collect(Collectors.toMap(DrownedBellHistoricalSitePlan.Piece::anchorId, piece -> piece));
        Map<String, StormLanternCoastSitePlan.Piece> laterById = later.pieces().stream()
                .filter(StormLanternCoastSitePlan.Piece::historicalAnchor)
                .collect(Collectors.toMap(StormLanternCoastSitePlan.Piece::anchorId, piece -> piece));

        assertSamePosition(historicalById.get("bell_tower"), laterById.get("storm_belfry"));
        assertSamePosition(historicalById.get("sea_gate"), laterById.get("sea_gate"));
        assertSamePosition(historicalById.get("quarry_tunnels"), laterById.get("collapsed_quarry_cut"));
        assertSamePosition(historicalById.get("lower_village"), laterById.get("drowned_harbour_terraces"));
    }

    @Test
    void historicalFamiliesAreFunctionalRatherThanRuined() {
        var families = DrownedBellHistoricalSitePlan.drownedBell().pieces().stream()
                .map(DrownedBellHistoricalSitePlan.Piece::pieceFamily)
                .collect(Collectors.toSet());

        assertTrue(families.contains("intact_bell_tower"));
        assertTrue(families.contains("working_sea_gate"));
        assertTrue(families.contains("open_quarry_tunnels"));
        assertTrue(families.contains("inhabited_harbour_terraces"));
    }

    private static void assertSamePosition(DrownedBellHistoricalSitePlan.Piece historical,
                                           StormLanternCoastSitePlan.Piece later) {
        assertEquals(historical.x(), later.x());
        assertEquals(historical.y(), later.y());
        assertEquals(historical.z(), later.z());
    }
}
