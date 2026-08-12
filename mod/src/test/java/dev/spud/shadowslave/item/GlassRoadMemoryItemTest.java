package dev.spud.shadowslave.item;

import dev.spud.shadowslave.content.memory.MemoryContentCatalog;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    @Test
    void commitmentAndRecoveryAreDistinctWindows() {
        GlassRoadCombatData committed = new GlassRoadCombatData(110L, 0L);
        assertTrue(committed.committed(100L));
        assertFalse(committed.readyToResolve(100L));
        assertTrue(committed.readyToResolve(110L));

        GlassRoadCombatData recovering = new GlassRoadCombatData(0L, 126L);
        assertTrue(recovering.recovering(110L));
        assertFalse(recovering.recovering(126L));
        assertEquals(10, GlassRoadMemoryItem.WINDUP_TICKS);
        assertEquals(16, GlassRoadMemoryItem.RECOVERY_TICKS);
    }

    @Test
    void cleanEdgeRequiresAPreciseForwardLine() {
        Vec3 origin = Vec3.ZERO;
        Vec3 forward = new Vec3(0.0D, 0.0D, 1.0D);
        assertTrue(GlassRoadMemoryItem.onPrecisionLine(origin, forward, new Vec3(0.0D, 0.0D, 4.5D)));
        assertTrue(GlassRoadMemoryItem.onPrecisionLine(origin, forward, new Vec3(0.79D, 0.0D, 3.0D)));
        assertFalse(GlassRoadMemoryItem.onPrecisionLine(origin, forward, new Vec3(0.81D, 0.0D, 3.0D)));
        assertFalse(GlassRoadMemoryItem.onPrecisionLine(origin, forward, new Vec3(0.0D, 0.0D, 4.51D)));
        assertFalse(GlassRoadMemoryItem.onPrecisionLine(origin, forward, new Vec3(0.0D, 0.0D, -1.0D)));
        assertFalse(GlassRoadMemoryItem.onPrecisionLine(origin, new Vec3(Double.NaN, 0.0D, 1.0D), new Vec3(0.0D, 0.0D, 1.0D)));
    }
}
