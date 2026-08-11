package dev.spud.shadowslave.item;

import dev.spud.shadowslave.content.memory.MemoryContentCatalog;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BorrowedDawnMemoryItemTest {
    @Test
    void consumesExistingBorrowedDawnAndFirstLightDefinitions() {
        var profile = MemoryContentCatalog.waveOne().memories().stream()
                .filter(memory -> memory.id().equals(BorrowedDawnMemoryItem.MEMORY_ID))
                .findFirst().orElseThrow();
        assertEquals("Borrowed Dawn", profile.formalName());
        assertTrue(profile.enchantments().stream().anyMatch(enchantment -> enchantment.id().equals(BorrowedDawnMemoryItem.FIRST_LIGHT_ID)));
        assertTrue(profile.enchantments().stream().anyMatch(enchantment -> enchantment.id().getPath().endsWith("night_debt")));
    }

    @Test
    void ambientLightCaptureHasAnExplicitBound() {
        assertFalse(BorrowedDawnMemoryItem.canCapture(-1));
        assertFalse(BorrowedDawnMemoryItem.canCapture(11));
        assertTrue(BorrowedDawnMemoryItem.canCapture(12));
        assertTrue(BorrowedDawnMemoryItem.canCapture(15));
        assertFalse(BorrowedDawnMemoryItem.canCapture(16));
    }

    @Test
    void restorativeAmountIsBoundedAndNotAFullHeal() {
        assertEquals(4.0F, BorrowedDawnMemoryItem.RESTORATIVE_HEAL);
    }
}
