package dev.spud.shadowslave.item;

import dev.spud.shadowslave.content.memory.MemoryContentCatalog;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlassRoadMemoryItemTest {
    @Test
    void consumesExistingGlassRoadCleanEdgeDefinitionWithoutImplementingMirrorStep() {
        var profile = MemoryContentCatalog.waveOne().memories().stream()
                .filter(memory -> memory.id().equals(GlassRoadMemoryItem.MEMORY_ID))
                .findFirst().orElseThrow();
        assertEquals("Glass Road", profile.formalName());
        assertTrue(profile.enchantments().stream().anyMatch(enchantment -> enchantment.id().equals(GlassRoadMemoryItem.CLEAN_EDGE_ID)));
        assertTrue(profile.enchantments().stream().anyMatch(enchantment -> enchantment.id().getPath().endsWith("mirror_step")));
        assertTrue(GlassRoadMemoryItem.bindsExistingCatalogueDefinition());
    }
}
