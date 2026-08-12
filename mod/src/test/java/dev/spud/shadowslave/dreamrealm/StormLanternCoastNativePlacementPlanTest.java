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
    void seaGateIsASecondNativeHistoricalPieceInItsOwnProjectedChunk() {
        for (long seed = 0; seed < 256; seed++) {
            var plan = StormLanternCoastNativePlacementPlan.drownedBellLater(seed);
            var belfry = plan.anchorPiece();
            var seaGate = plan.piece(StormLanternCoastNativePlacementPlan.NATIVE_SEA_GATE_ID);

            assertTrue(seaGate.historicalAnchor());
            assertEquals(38, plan.globalX(seaGate) - plan.globalX(belfry));
            assertEquals(35, plan.globalZ(seaGate) - plan.globalZ(belfry));
            assertNotEquals(plan.chunkX(belfry) + ":" + plan.chunkZ(belfry),
                    plan.chunkX(seaGate) + ":" + plan.chunkZ(seaGate));
            assertEquals(14, Math.floorMod(plan.globalX(seaGate), 16));
            assertEquals(11, Math.floorMod(plan.globalZ(seaGate), 16));
            assertEquals(seaGate, plan.nativePieceForChunk(plan.chunkX(seaGate), plan.chunkZ(seaGate)).orElseThrow());
            assertEquals(belfry, plan.nativePieceForChunk(plan.chunkX(belfry), plan.chunkZ(belfry)).orElseThrow());
        }
    }

    @Test
    void quarryIsAThirdNativeHistoricalPieceWithChunkSafeLocalRoom() {
        for (long seed = 0; seed < 256; seed++) {
            var plan = StormLanternCoastNativePlacementPlan.drownedBellLater(seed);
            var belfry = plan.anchorPiece();
            var seaGate = plan.piece(StormLanternCoastNativePlacementPlan.NATIVE_SEA_GATE_ID);
            var quarry = plan.piece(StormLanternCoastNativePlacementPlan.NATIVE_QUARRY_ID);

            assertTrue(quarry.historicalAnchor());
            assertEquals(44, plan.globalX(quarry) - plan.globalX(belfry));
            assertEquals(-3, plan.globalZ(quarry) - plan.globalZ(belfry));
            assertEquals(4, Math.floorMod(plan.globalX(quarry), 16));
            assertEquals(5, Math.floorMod(plan.globalZ(quarry), 16));
            assertNotEquals(plan.chunkX(belfry) + ":" + plan.chunkZ(belfry),
                    plan.chunkX(quarry) + ":" + plan.chunkZ(quarry));
            assertNotEquals(plan.chunkX(seaGate) + ":" + plan.chunkZ(seaGate),
                    plan.chunkX(quarry) + ":" + plan.chunkZ(quarry));
            assertEquals(quarry, plan.nativePieceForChunk(plan.chunkX(quarry), plan.chunkZ(quarry)).orElseThrow());

            // The quarry executor uses x [-3,3] and z [-1,5] around this anchor.
            int localX = Math.floorMod(plan.globalX(quarry), 16);
            int localZ = Math.floorMod(plan.globalZ(quarry), 16);
            assertTrue(localX - 3 >= 0 && localX + 3 <= 15);
            assertTrue(localZ - 1 >= 0 && localZ + 5 <= 15);
        }
    }

    @Test
    void onlyExplicitlyMigratedHistoricalPiecesOwnNativeChunks() {
        long seed = 91L;
        var plan = StormLanternCoastNativePlacementPlan.drownedBellLater(seed);
        var belfry = plan.anchorPiece();
        var seaGate = plan.piece(StormLanternCoastNativePlacementPlan.NATIVE_SEA_GATE_ID);
        var quarry = plan.piece(StormLanternCoastNativePlacementPlan.NATIVE_QUARRY_ID);
        var terraces = plan.piece("drowned_harbour_terraces");

        assertEquals(belfry, plan.nativePieceForChunk(plan.chunkX(belfry), plan.chunkZ(belfry)).orElseThrow());
        assertEquals(seaGate, plan.nativePieceForChunk(plan.chunkX(seaGate), plan.chunkZ(seaGate)).orElseThrow());
        assertEquals(quarry, plan.nativePieceForChunk(plan.chunkX(quarry), plan.chunkZ(quarry)).orElseThrow());
        assertTrue(plan.nativePieceForChunk(plan.chunkX(terraces), plan.chunkZ(terraces)).isEmpty());
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
