package dev.spud.shadowslave.item;

import dev.spud.shadowslave.content.memory.MemoryContentCatalog;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class BellglassTokenRuntimeBindingTest {
    @Test
    void consumesExistingBellglassDefinitionsWithoutInventingEnchantments() {
        MemoryContentCatalog.MemoryProfile bellglass = MemoryContentCatalog.waveOne().memories().stream()
                .filter(memory -> memory.id().equals(BellglassTokenMemoryItem.MEMORY_ID))
                .findFirst()
                .orElseThrow();

        assertEquals("Bellglass Token", bellglass.formalName());
        assertTrue(bellglass.enchantments().stream().anyMatch(enchantment ->
                enchantment.id().toString().equals("shadowslave:memory_enchantment/clear_warning")));
        assertTrue(bellglass.enchantments().stream().anyMatch(enchantment ->
                enchantment.id().toString().equals("shadowslave:memory_enchantment/held_note")));
    }

    @Test
    void warningExecutorIsBoundedToExistingPhysicalNightmareCreaturesAndHiddenMovement() throws Exception {
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
    void heldNoteExecutorUsesNoteBlockStateAndJavaOwnedPayload() throws Exception {
        String source = Files.readString(Path.of("src/main/java/dev/spud/shadowslave/item/BellglassTokenMemoryItem.java"));

        assertTrue(source.contains("Blocks.NOTE_BLOCK"));
        assertTrue(source.contains("NoteBlock.INSTRUMENT"));
        assertTrue(source.contains("NoteBlock.NOTE"));
        assertTrue(source.contains("BellglassHeldNoteService.capture"));
        assertTrue(source.contains("BellglassHeldNoteService.get"));
        assertTrue(source.contains("BellglassHeldNoteService.clear"));
        assertFalse(source.contains("PlayLevelSoundEvent"));
    }

    @Test
    void warningRangeRemainsExplicitlyBounded() {
        assertEquals(10.0D, BellglassTokenMemoryItem.WARNING_RANGE);
    }

    @Test
    void vanillaNotePitchMappingIsBoundedAndCentered() {
        assertEquals(1.0F, BellglassTokenMemoryItem.notePitch(12), 0.0001F);
        assertEquals(0.5F, BellglassTokenMemoryItem.notePitch(0), 0.0001F);
        assertEquals(2.0F, BellglassTokenMemoryItem.notePitch(24), 0.0001F);
        assertThrows(IllegalArgumentException.class, () -> BellglassTokenMemoryItem.notePitch(-1));
        assertThrows(IllegalArgumentException.class, () -> BellglassTokenMemoryItem.notePitch(25));
    }
}
