package dev.spud.shadowslave.item;

import dev.spud.shadowslave.content.memory.MemoryContentCatalog;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RedThreadBraceletMemoryItemTest {
    @Test
    void runtimeIdentityConsumesExistingAuthoredTetheredPulse() {
        MemoryContentCatalog.MemoryProfile bracelet = MemoryContentCatalog.waveOne().memories().stream()
                .filter(memory -> memory.id().equals(RedThreadBraceletMemoryItem.MEMORY_ID))
                .findFirst()
                .orElseThrow();

        assertEquals("Red Thread Bracelet", bracelet.formalName());
        assertTrue(bracelet.enchantments().stream()
                .anyMatch(enchantment -> enchantment.id().getPath().equals("memory_enchantment/tethered_pulse")));
        assertTrue(bracelet.enchantments().stream()
                .anyMatch(enchantment -> enchantment.id().getPath().equals("memory_enchantment/strain_warning")));
    }

    @Test
    void directionMappingCoversCardinalDiagonalAndSamePosition() {
        assertEquals("east", RedThreadBraceletMemoryItem.direction(10, 0));
        assertEquals("west", RedThreadBraceletMemoryItem.direction(-10, 0));
        assertEquals("south", RedThreadBraceletMemoryItem.direction(0, 10));
        assertEquals("north", RedThreadBraceletMemoryItem.direction(0, -10));
        assertEquals("south-east", RedThreadBraceletMemoryItem.direction(10, 10));
        assertEquals("north-west", RedThreadBraceletMemoryItem.direction(-10, -10));
        assertEquals("here", RedThreadBraceletMemoryItem.direction(0, 0));
    }

    @Test
    void tetherRangeIsBoundedAndInclusive() {
        double boundary = RedThreadBraceletMemoryItem.TETHER_RANGE * RedThreadBraceletMemoryItem.TETHER_RANGE;

        assertTrue(RedThreadBraceletMemoryItem.withinRangeSqr(0));
        assertTrue(RedThreadBraceletMemoryItem.withinRangeSqr(boundary));
        assertFalse(RedThreadBraceletMemoryItem.withinRangeSqr(boundary + 0.01));
        assertFalse(RedThreadBraceletMemoryItem.withinRangeSqr(-1));
    }

    @Test
    void nonFiniteDirectionFailsClosed() {
        assertThrows(IllegalArgumentException.class,
                () -> RedThreadBraceletMemoryItem.direction(Double.NaN, 0));
        assertThrows(IllegalArgumentException.class,
                () -> RedThreadBraceletMemoryItem.direction(0, Double.POSITIVE_INFINITY));
    }
}
