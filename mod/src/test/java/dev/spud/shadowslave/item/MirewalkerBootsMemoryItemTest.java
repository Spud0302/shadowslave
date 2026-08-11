package dev.spud.shadowslave.item;

import dev.spud.shadowslave.content.memory.MemoryContentCatalog;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MirewalkerBootsMemoryItemTest {
    @Test
    void consumesExistingMirewalkerAndSureFootDefinitions() {
        var profile = MemoryContentCatalog.waveOne().memories().stream()
                .filter(memory -> memory.id().equals(MirewalkerBootsMemoryItem.MEMORY_ID))
                .findFirst().orElseThrow();
        assertEquals("Mirewalker Boots", profile.formalName());
        assertTrue(profile.enchantments().stream().anyMatch(enchantment -> enchantment.id().equals(MirewalkerBootsMemoryItem.SURE_FOOT_ID)));
        assertTrue(profile.enchantments().stream().anyMatch(enchantment -> enchantment.id().getPath().endsWith("light_trace")));
        assertTrue(MirewalkerBootsMemoryItem.bindsExistingCatalogueDefinition());
    }

    @Test
    void sureFootExecutionIsExplicitlyBounded() {
        assertEquals(160, MirewalkerBootsMemoryItem.SURE_FOOT_DURATION_TICKS);
        assertEquals(0, MirewalkerBootsMemoryItem.SURE_FOOT_AMPLIFIER);
        assertTrue(MirewalkerBootsMemoryItem.isDraggingTerrain(Blocks.MUD.defaultBlockState()));
        assertTrue(MirewalkerBootsMemoryItem.isDraggingTerrain(Blocks.SOUL_SAND.defaultBlockState()));
        assertFalse(MirewalkerBootsMemoryItem.isDraggingTerrain(Blocks.STONE.defaultBlockState()));
    }
}
