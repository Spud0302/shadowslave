package dev.spud.shadowslave.item;

import dev.spud.shadowslave.dreamrealm.DreamRealmPreviewService;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AshCompassMemoryItemTest {
    @Test
    void readingReportsJavaOwnedCinderRestDirectionAndDistance() {
        assertEquals("Cinder Rest is here", AshCompassMemoryItem.reading(new BlockPos(0, 160, 0), DreamRealmPreviewService.cinderRestAnchor()));
        assertEquals("Cinder Rest east, about 10 blocks away", AshCompassMemoryItem.reading(new BlockPos(-10, 160, -1), DreamRealmPreviewService.cinderRestAnchor()));
        assertEquals("Cinder Rest northwest, about 14 blocks away", AshCompassMemoryItem.reading(new BlockPos(10, 160, 9), DreamRealmPreviewService.cinderRestAnchor()));
    }

    @Test
    void allCardinalAndDiagonalDirectionsAreStable() {
        assertEquals("north", AshCompassMemoryItem.direction(0, -1));
        assertEquals("northeast", AshCompassMemoryItem.direction(1, -1));
        assertEquals("east", AshCompassMemoryItem.direction(1, 0));
        assertEquals("southeast", AshCompassMemoryItem.direction(1, 1));
        assertEquals("south", AshCompassMemoryItem.direction(0, 1));
        assertEquals("southwest", AshCompassMemoryItem.direction(-1, 1));
        assertEquals("west", AshCompassMemoryItem.direction(-1, 0));
        assertEquals("northwest", AshCompassMemoryItem.direction(-1, -1));
    }

    @Test
    void cinderRestAnchorMatchesThePhysicalSettlementIntegration() {
        assertEquals(new BlockPos(0, 161, -1), DreamRealmPreviewService.cinderRestAnchor());
    }
}
