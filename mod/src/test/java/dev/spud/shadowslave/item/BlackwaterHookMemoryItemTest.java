package dev.spud.shadowslave.item;

import dev.spud.shadowslave.content.memory.MemoryContentCatalog;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlackwaterHookMemoryItemTest {
    @Test
    void runtimeConsumesExistingBlackwaterAndUndertowIdentities() {
        MemoryContentCatalog.MemoryProfile profile = MemoryContentCatalog.waveOne().memories().stream()
                .filter(memory -> memory.id().equals(BlackwaterHookMemoryItem.MEMORY_ID))
                .findFirst()
                .orElseThrow();

        assertEquals("Blackwater Hook", profile.formalName());
        assertTrue(profile.enchantments().stream().anyMatch(enchantment -> enchantment.id().equals(
                ResourceLocation.fromNamespaceAndPath("shadowslave", "memory_enchantment/undertow_line"))));
        assertTrue(profile.enchantments().stream().anyMatch(enchantment -> enchantment.id().equals(
                ResourceLocation.fromNamespaceAndPath("shadowslave", "memory_enchantment/river_grip"))));
    }

    @Test
    void terrainPullHasExplicitArrivalAndMaximumRange() {
        assertFalse(BlackwaterHookMemoryItem.withinRange(2.0));
        assertTrue(BlackwaterHookMemoryItem.withinRange(2.01));
        assertTrue(BlackwaterHookMemoryItem.withinRange(16.0));
        assertFalse(BlackwaterHookMemoryItem.withinRange(16.01));
        assertFalse(BlackwaterHookMemoryItem.withinRange(Double.NaN));
        assertFalse(BlackwaterHookMemoryItem.withinRange(Double.POSITIVE_INFINITY));
    }

    @Test
    void pullVectorIsBoundedAndPointsTowardAnchor() {
        Vec3 horizontal = BlackwaterHookMemoryItem.pullVector(new Vec3(10.0, 0.0, 0.0));
        assertEquals(0.85, horizontal.x, 1.0e-9);
        assertEquals(0.0, horizontal.y, 1.0e-9);
        assertEquals(0.0, horizontal.z, 1.0e-9);

        Vec3 steep = BlackwaterHookMemoryItem.pullVector(new Vec3(1.0, 100.0, 1.0));
        assertTrue(steep.x > 0.0);
        assertEquals(0.35, steep.y, 1.0e-9);
        assertTrue(steep.z > 0.0);
        assertEquals(Vec3.ZERO, BlackwaterHookMemoryItem.pullVector(Vec3.ZERO));
    }
}
