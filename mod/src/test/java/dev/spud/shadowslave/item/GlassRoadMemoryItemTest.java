package dev.spud.shadowslave.item;

import dev.spud.combatcore.api.CombatPhase;
import dev.spud.shadowslave.content.memory.MemoryContentCatalog;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

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
    void cleanEdgeDelegatesCommitmentResolutionAndRecoveryToCombatCore() {
        GlassRoadCombatData state = GlassRoadCombatData.empty();
        AtomicInteger resolutions = new AtomicInteger();

        assertTrue(state.start(100L));
        assertEquals(CombatPhase.WINDUP, state.phaseAt(100L));
        assertEquals(CombatPhase.WINDUP, state.phaseAt(109L));
        assertFalse(state.resolve(109L, resolutions::incrementAndGet));

        assertEquals(CombatPhase.ACTIVE, state.phaseAt(110L));
        assertTrue(state.resolve(110L, resolutions::incrementAndGet));
        assertFalse(state.resolve(110L, resolutions::incrementAndGet));
        assertEquals(1, resolutions.get());

        assertEquals(CombatPhase.RECOVERY, state.phaseAt(111L));
        assertFalse(state.start(111L));
        assertEquals(CombatPhase.RECOVERY, state.phaseAt(126L));
        assertEquals(CombatPhase.IDLE, state.phaseAt(127L));
        assertTrue(state.start(127L));

        assertEquals(10, GlassRoadCombatData.CLEAN_EDGE_ACTION.windupTicks());
        assertEquals(1, GlassRoadCombatData.CLEAN_EDGE_ACTION.activeTicks());
        assertEquals(16, GlassRoadCombatData.CLEAN_EDGE_ACTION.recoveryTicks());
        assertEquals(GlassRoadMemoryItem.REACH, GlassRoadCombatData.CLEAN_EDGE_ACTION.reach());
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
