package dev.spud.shadowslave.nightmare;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightmareRegistryCreationGuardTest {
    @Test
    void retainedCompletionBlocksCreatingAnotherNightmareUntilExplicitlyCleared() {
        UUID playerId = UUID.randomUUID();
        NightmareInstance completed = instance(playerId, 4);
        NightmareRegistryData registry = new NightmareRegistryData();
        registry.restore(completed);
        registry.beginSuccessfulCompletion(completed, 1_200L);
        registry.advanceSuccessfulCompletion(completed, NightmareCompletionPhase.APPRAISAL_COMMITTED);
        registry.advanceSuccessfulCompletion(completed, NightmareCompletionPhase.RETURN_COMMITTED);
        assertEquals(completed, registry.remove(completed).orElseThrow());
        registry.advanceSuccessfulCompletion(completed, NightmareCompletionPhase.TEARDOWN_COMMITTED);

        assertTrue(registry.findByPlayer(playerId).isEmpty(),
                "teardown should consume active ownership before the retained receipt is cleared");
        assertThrows(IllegalStateException.class, () -> registry.ensurePlayerCanCreate(playerId));
        assertEquals(completed,
                registry.findSuccessfulCompletionByPlayer(playerId).orElseThrow().instance(),
                "a rejected entry attempt must not consume the recovery receipt");

        registry.clearSuccessfulCompletion(completed);
        assertDoesNotThrow(() -> registry.ensurePlayerCanCreate(playerId));
    }

    @Test
    void activeOwnershipStillBlocksCreationThroughTheSameRegistryGuard() {
        UUID playerId = UUID.randomUUID();
        NightmareInstance active = instance(playerId, 2);
        NightmareRegistryData registry = new NightmareRegistryData();
        registry.restore(active);

        assertThrows(IllegalStateException.class, () -> registry.ensurePlayerCanCreate(playerId));
        assertEquals(active, registry.findByPlayer(playerId).orElseThrow());
    }

    private static NightmareInstance instance(UUID playerId, int slot) {
        BlockPos origin = LastSignalScenario.originForSlot(slot);
        return new NightmareInstance(
                UUID.randomUUID(),
                playerId,
                slot,
                LastSignalScenario.SCENARIO_ID,
                LastSignalScenario.ROLE_ID,
                ResourceLocation.parse("minecraft:overworld"),
                12.5,
                70.0,
                -4.5,
                90.0F,
                10.0F,
                origin,
                LastSignalScenario.altarForOrigin(origin),
                Optional.empty(),
                900L + slot
        );
    }
}
