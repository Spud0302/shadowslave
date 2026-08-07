package dev.spud.shadowslave.nightmare;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightmareRegistryCompletionClearTest {
    @Test
    void exactSnapshotClearsItsReceiptOnlyOnce() {
        UUID playerId = UUID.randomUUID();
        NightmareInstance instance = instance(UUID.randomUUID(), playerId, 2, UUID.randomUUID());
        NightmareRegistryData registry = new NightmareRegistryData();
        registry.restore(instance);
        NightmareCompletionRecord receipt = registry.beginSuccessfulCompletion(instance, 1_000L);

        assertEquals(receipt, registry.clearSuccessfulCompletion(instance).orElseThrow());
        assertTrue(registry.clearSuccessfulCompletion(instance).isEmpty());
        assertTrue(registry.findSuccessfulCompletionByPlayer(playerId).isEmpty());
        assertEquals(instance, registry.findByPlayer(playerId).orElseThrow(),
                "clearing a retained receipt must not consume active ownership");
    }

    @Test
    void staleOlderInstanceCannotClearANewerReceiptForTheSamePlayer() {
        UUID playerId = UUID.randomUUID();
        NightmareInstance older = instance(UUID.randomUUID(), playerId, 2, UUID.randomUUID());
        NightmareInstance newer = instance(UUID.randomUUID(), playerId, 3, UUID.randomUUID());
        NightmareRegistryData registry = new NightmareRegistryData();

        registry.restore(older);
        registry.beginSuccessfulCompletion(older, 1_000L);
        registry.clearSuccessfulCompletion(older);
        registry.remove(older);

        registry.restore(newer);
        NightmareCompletionRecord newerReceipt = registry.beginSuccessfulCompletion(newer, 2_000L);

        assertTrue(registry.clearSuccessfulCompletion(older).isEmpty(),
                "a stale instance ID must not consume the player's newer completion receipt");
        assertEquals(newerReceipt, registry.findSuccessfulCompletionByPlayer(playerId).orElseThrow());
        assertEquals(newer, registry.findByPlayer(playerId).orElseThrow());
    }

    @Test
    void modifiedSameIdSnapshotCannotClearAuthoritativeReceipt() {
        UUID instanceId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        NightmareInstance authoritative = instance(instanceId, playerId, 4, UUID.randomUUID());
        NightmareInstance modified = instance(instanceId, playerId, 4, UUID.randomUUID());
        NightmareRegistryData registry = new NightmareRegistryData();
        registry.restore(authoritative);
        NightmareCompletionRecord receipt = registry.beginSuccessfulCompletion(authoritative, 3_000L);

        assertThrows(IllegalStateException.class, () -> registry.clearSuccessfulCompletion(modified));
        assertEquals(receipt, registry.findSuccessfulCompletionByPlayer(playerId).orElseThrow());
        assertEquals(authoritative, registry.findByPlayer(playerId).orElseThrow());
    }

    private static NightmareInstance instance(UUID instanceId, UUID playerId, int slot, UUID pursuerId) {
        BlockPos origin = LastSignalScenario.originForSlot(slot);
        return new NightmareInstance(
                instanceId,
                playerId,
                slot,
                LastSignalScenario.SCENARIO_ID,
                LastSignalScenario.ROLE_ID,
                ResourceLocation.parse("minecraft:overworld"),
                10.5 + slot,
                70.0,
                4.5,
                90.0F,
                10.0F,
                origin,
                LastSignalScenario.altarForOrigin(origin),
                Optional.of(pursuerId),
                900L + slot
        );
    }
}
