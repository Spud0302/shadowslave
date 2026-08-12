package dev.spud.shadowslave.item;

import dev.spud.shadowslave.content.memory.MemoryContentCatalog;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StonewakeShieldMemoryItemTest {
    @Test
    void consumesExistingStonewakeAndSettleDefinitions() {
        var profile = MemoryContentCatalog.waveOne().memories().stream()
                .filter(memory -> memory.id().equals(StonewakeShieldMemoryItem.MEMORY_ID))
                .findFirst().orElseThrow();
        assertEquals("Stonewake Shield", profile.formalName());
        assertTrue(profile.enchantments().stream().anyMatch(enchantment -> enchantment.id().equals(StonewakeShieldMemoryItem.SETTLE_ID)));
        assertTrue(profile.enchantments().stream().anyMatch(enchantment -> enchantment.id().getPath().endsWith("wake")));
        assertTrue(StonewakeShieldMemoryItem.bindsExistingCatalogueDefinition());
    }

    @Test
    void settleRequiresBoundedHorizontalForce() {
        assertFalse(StonewakeShieldMemoryItem.canSettle(0.0D, 0.0D));
        assertFalse(StonewakeShieldMemoryItem.canSettle(0.079D, 0.0D));
        assertTrue(StonewakeShieldMemoryItem.canSettle(0.08D, 0.0D));
        assertTrue(StonewakeShieldMemoryItem.canSettle(0.06D, 0.06D));
        assertFalse(StonewakeShieldMemoryItem.canSettle(Double.NaN, 0.0D));
        assertFalse(StonewakeShieldMemoryItem.canSettle(Double.POSITIVE_INFINITY, 0.0D));
    }

    @Test
    void settleTurnsACommittedHorizontalShoveIntoAWeakerFollowThrough() {
        assertEquals(0.25D, StonewakeShieldMemoryItem.SETTLED_HORIZONTAL_MULTIPLIER);
        assertEquals(0.25D, StonewakeShieldMemoryItem.settledVelocity(1.0D));
        assertEquals(-0.5D, StonewakeShieldMemoryItem.settledVelocity(-2.0D));
        assertThrows(IllegalArgumentException.class, () -> StonewakeShieldMemoryItem.settledVelocity(Double.NaN));
    }
}
