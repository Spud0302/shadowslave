package dev.spud.shadowslave.item;

import dev.spud.shadowslave.content.memory.MemoryContentCatalog;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VeilStitchCaseMemoryItemTest {
    @Test
    void consumesExistingVeilStitchAndQuietMendingDefinitions() {
        var profile = MemoryContentCatalog.waveOne().memories().stream()
                .filter(memory -> memory.id().equals(VeilStitchCaseMemoryItem.MEMORY_ID))
                .findFirst().orElseThrow();
        assertEquals("Veil-Stitch Case", profile.formalName());
        assertTrue(profile.enchantments().stream().anyMatch(enchantment -> enchantment.id().equals(VeilStitchCaseMemoryItem.QUIET_MENDING_ID)));
        assertTrue(profile.enchantments().stream().anyMatch(enchantment -> enchantment.id().getPath().endsWith("dull_seam")));
        assertTrue(VeilStitchCaseMemoryItem.bindsExistingCatalogueDefinition());
    }

    @Test
    void repairAndCombatClearanceAreExplicitlyBounded() {
        assertEquals(16, VeilStitchCaseMemoryItem.REPAIR_AMOUNT);
        assertEquals(8.0D, VeilStitchCaseMemoryItem.COMBAT_CLEARANCE);
    }
}
