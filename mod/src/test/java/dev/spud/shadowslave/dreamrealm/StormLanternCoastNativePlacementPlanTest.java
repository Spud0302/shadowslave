package dev.spud.shadowslave.dreamrealm;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StormLanternCoastNativePlacementPlanTest {
    @Test
    void sameSeedProducesExactlyTheSameNativeAnchor() {
        var first = StormLanternCoastNativePlacementPlan.drownedBellLater(123456789L);
        var second = StormLanternCoastNativePlacementPlan.drownedBellLater(123456789L);

        assertEquals(first, second);
        assertEquals("storm_belfry", first.anchorPiece().anchorId());
        assertTrue(first.anchorPiece().historicalAnchor());
        assertTrue(first.ownsChunk(first.anchorChunkX(), first.anchorChunkZ()));
        assertFalse(first.ownsChunk(first.anchorChunkX() + 1, first.anchorChunkZ()));
    }

    @Test
    void nativeAnchorStaysInsideReservedMacroAreaAwayFromPreviewOrigins() {
        for (long seed = 0; seed < 4096; seed++) {
            var plan = StormLanternCoastNativePlacementPlan.drownedBellLater(seed);
            assertTrue(plan.anchorChunkX() >= StormLanternCoastNativePlacementPlan.MACRO_MIN_CHUNK_X);
            assertTrue(plan.anchorChunkX() < StormLanternCoastNativePlacementPlan.MACRO_MIN_CHUNK_X
                    + StormLanternCoastNativePlacementPlan.MACRO_SPAN_CHUNKS);
            assertTrue(plan.anchorChunkZ() >= StormLanternCoastNativePlacementPlan.MACRO_MIN_CHUNK_Z);
            assertTrue(plan.anchorChunkZ() < StormLanternCoastNativePlacementPlan.MACRO_MIN_CHUNK_Z
                    + StormLanternCoastNativePlacementPlan.MACRO_SPAN_CHUNKS);
            assertTrue(plan.anchorChunkX() >= 64, "native site must remain well east of bounded preview fixtures");
        }
    }

    @Test
    void nativeOriginProjectsEveryExistingSitePieceWithoutChangingRelativeGeography() {
        long seed = 77L;
        var nativePlan = StormLanternCoastNativePlacementPlan.drownedBellLater(seed);
        var sitePlan = StormLanternCoastSitePlan.drownedBellLater(seed);
        var belfry = nativePlan.anchorPiece();

        assertEquals(nativePlan.anchorChunkX() * 16 + 8, nativePlan.globalX(belfry));
        assertEquals(nativePlan.anchorChunkZ() * 16 + 8, nativePlan.globalZ(belfry));

        for (var piece : sitePlan.pieces()) {
            assertEquals(piece.x() - belfry.x(), nativePlan.globalX(piece) - nativePlan.globalX(belfry));
            assertEquals(piece.z() - belfry.z(), nativePlan.globalZ(piece) - nativePlan.globalZ(belfry));
        }
    }

    @Test
    void worldSeedVariesNativeAnchorAcrossTheReservedMacroArea() {
        Set<String> chunks = new HashSet<>();
        for (long seed = 0; seed < 512; seed++) {
            var plan = StormLanternCoastNativePlacementPlan.drownedBellLater(seed);
            chunks.add(plan.anchorChunkX() + ":" + plan.anchorChunkZ());
        }

        assertTrue(chunks.size() > 64, "seed sweep should use a meaningful part of the reserved macro-area");
        assertNotEquals(
                StormLanternCoastNativePlacementPlan.drownedBellLater(1L),
                StormLanternCoastNativePlacementPlan.drownedBellLater(2L)
        );
    }
}
