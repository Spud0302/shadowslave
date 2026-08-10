package dev.spud.shadowslave.item;

import dev.spud.shadowslave.content.memory.MemoryContentCatalog;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class BellglassTokenRuntimeBindingTest {
    @Test
    void consumesExistingBellglassAndClearWarningDefinitions() {
        MemoryContentCatalog.MemoryProfile bellglass = MemoryContentCatalog.waveOne().memories().stream()
                .filter(memory -> memory.id().equals(BellglassTokenMemoryItem.MEMORY_ID))
                .findFirst()
                .orElseThrow();

        assertEquals("Bellglass Token", bellglass.formalName());
        assertTrue(bellglass.enchantments().stream().anyMatch(enchantment ->
                enchantment.id().toString().equals("shadowslave:memory_enchantment/clear_warning")));
    }

    @Test
    void executorIsBoundedToExistingPhysicalNightmareCreaturesAndHiddenMovement() throws Exception {
        String source = Files.readString(Path.of("src/main/java/dev/spud/shadowslave/item/BellglassTokenMemoryItem.java"));

        assertTrue(source.contains("AshBurrowerEntity"));
        assertTrue(source.contains("ChainbackEntity"));
        assertTrue(source.contains("DrownedListenerEntity"));
        assertTrue(source.contains("!player.hasLineOfSight(entity)"));
        assertTrue(source.contains("isMoving(entity)"));
        assertFalse(source.contains("Monster.class"));
        assertFalse(source.contains("AshBurrowerEchoEntity"));
    }

    @Test
    void warningRangeRemainsExplicitlyBounded() {
        assertEquals(10.0D, BellglassTokenMemoryItem.WARNING_RANGE);
    }
}