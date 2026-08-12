package dev.spud.shadowslave.dreamrealm;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StormLanternCoastSitePlanTest {
    @Test
    void sameWorldSeedProducesExactlyTheSameLaterSitePlan() {
        var first = StormLanternCoastSitePlan.drownedBellLater(123456789L);
        var second = StormLanternCoastSitePlan.drownedBellLater(123456789L);

        assertEquals(first, second);
        assertEquals("storm_lantern_coast", first.region().id());
        assertEquals("drowned_bell_cliff_settlement", first.site().id());
    }

    @Test
    void laterSitePreservesAllRecognizableHistoricalLandmarks() {
        var plan = StormLanternCoastSitePlan.drownedBellLater(17L);
        Set<String> anchors = plan.pieces().stream()
                .filter(StormLanternCoastSitePlan.Piece::historicalAnchor)
                .map(StormLanternCoastSitePlan.Piece::anchorId)
                .collect(Collectors.toSet());

        assertEquals(Set.of(
                "storm_belfry",
                "sea_gate",
                "collapsed_quarry_cut",
                "drowned_harbour_terraces"
        ), anchors);
        assertEquals(Set.copyOf(plan.site().historicalToFutureLandmarks().values()), anchors);
    }

    @Test
    void worldSeedChangesModularVariantsWithoutMovingHistoricalAnchors() {
        var first = StormLanternCoastSitePlan.drownedBellLater(1L);
        var second = StormLanternCoastSitePlan.drownedBellLater(2L);

        Map<String, String> firstPositions = first.pieces().stream()
                .filter(StormLanternCoastSitePlan.Piece::historicalAnchor)
                .collect(Collectors.toMap(StormLanternCoastSitePlan.Piece::anchorId,
                        piece -> piece.x() + ":" + piece.y() + ":" + piece.z()));
        Map<String, String> secondPositions = second.pieces().stream()
                .filter(StormLanternCoastSitePlan.Piece::historicalAnchor)
                .collect(Collectors.toMap(StormLanternCoastSitePlan.Piece::anchorId,
                        piece -> piece.x() + ":" + piece.y() + ":" + piece.z()));

        assertEquals(firstPositions, secondPositions);
        assertNotEquals(first.siteSeed(), second.siteSeed());
        assertNotEquals(first.pieces(), second.pieces());
    }

    @Test
    void planMixesHistoricalRecognitionWithGenericExplorationPieces() {
        var plan = StormLanternCoastSitePlan.drownedBellLater(42L);
        long historical = plan.pieces().stream().filter(StormLanternCoastSitePlan.Piece::historicalAnchor).count();
        long generic = plan.pieces().stream().filter(piece -> !piece.historicalAnchor()).count();

        assertEquals(4, historical);
        assertTrue(generic >= 4);
        assertTrue(plan.pieces().stream().map(StormLanternCoastSitePlan.Piece::pieceFamily).distinct().count() >= 7);
    }
}
