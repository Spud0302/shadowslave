package dev.spud.shadowslave.item;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AshCompassMemoryItemTest {
    @Test
    void readingReportsAnchoredRefugeDirectionAndDistance() {
        assertEquals("the refuge is here", AshCompassMemoryItem.reading(new BlockPos(0, 64, 0), new BlockPos(2, 70, 1)));
        assertEquals("refuge east, about 10 blocks away", AshCompassMemoryItem.reading(new BlockPos(0, 64, 0), new BlockPos(10, 80, 0)));
        assertEquals("refuge northwest, about 14 blocks away", AshCompassMemoryItem.reading(new BlockPos(0, 64, 0), new BlockPos(-10, 64, -10)));
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
}
