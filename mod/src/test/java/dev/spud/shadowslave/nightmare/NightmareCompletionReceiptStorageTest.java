package dev.spud.shadowslave.nightmare;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightmareCompletionReceiptStorageTest {
    @Test
    void receiptRoundTripsAfterActiveOwnershipIsConsumed() {
        NightmareInstance instance = instance(UUID.randomUUID(), UUID.randomUUID(), 3, 12.5);
        NightmareRegistryData registry = new NightmareRegistryData();
        registry.restore(instance);

        NightmareCompletionRecord receipt = registry.beginSuccessfulCompletion(instance, 1200L);
        assertEquals(NightmareCompletionPhase.TERMINAL_RESOLUTION_RECORDED, receipt.phase());
        registry.advanceSuccessfulCompletion(instance, NightmareCompletionPhase.APPRAISAL_COMMITTED);
        registry.advanceSuccessfulCompletion(instance, NightmareCompletionPhase.RETURN_COMMITTED);
        assertEquals(instance, registry.remove(instance).orElseThrow());
        registry.advanceSuccessfulCompletion(instance, NightmareCompletionPhase.TEARDOWN_COMMITTED);

        NightmareRegistryData restored = NightmareRegistryData.load(registry.save(new CompoundTag(), null), null);
        assertTrue(restored.findByPlayer(instance.playerId()).isEmpty());
        NightmareCompletionRecord restoredReceipt = restored.findSuccessfulCompletionByPlayer(instance.playerId()).orElseThrow();
        assertEquals(instance, restoredReceipt.instance());
        assertEquals(NightmareCompletionPhase.TEARDOWN_COMMITTED, restoredReceipt.phase());
        assertEquals(1200L, restoredReceipt.resolvedGameTime());
    }

    @Test
    void receiptRejectsCrossInstanceAndSnapshotMismatchOnRestore() {
        UUID playerId = UUID.randomUUID();
        NightmareInstance active = instance(UUID.randomUUID(), playerId, 1, 10.0);
        NightmareInstance other = instance(UUID.randomUUID(), playerId, 2, 20.0);
        NightmareRegistryData registry = new NightmareRegistryData();
        registry.restore(active);

        assertThrows(IllegalStateException.class, () -> registry.restoreSuccessfulCompletion(
                new NightmareCompletionRecord(other, NightmareCompletionPhase.TERMINAL_RESOLUTION_RECORDED, 5L)
        ));
        assertTrue(registry.findSuccessfulCompletionByPlayer(playerId).isEmpty());
        assertEquals(active, registry.findByPlayer(playerId).orElseThrow());

        NightmareInstance moved = new NightmareInstance(
                active.instanceId(), active.playerId(), active.slot(), active.scenarioId(), active.historicalRoleId(),
                active.returnDimension(), active.returnX(), active.returnY(), active.returnZ(), active.returnYaw(),
                active.returnPitch(), active.origin().offset(1, 0, 0), active.altar(), active.pursuerId(), active.createdGameTime()
        );
        assertThrows(IllegalStateException.class, () -> registry.restoreSuccessfulCompletion(
                new NightmareCompletionRecord(moved, NightmareCompletionPhase.TERMINAL_RESOLUTION_RECORDED, 5L)
        ));
    }

    @Test
    void receiptFreezesSnapshotAndBlocksNewEntryUntilExplicitlyCleared() {
        UUID playerId = UUID.randomUUID();
        NightmareInstance instance = instance(UUID.randomUUID(), playerId, 4, 30.0);
        NightmareRegistryData registry = new NightmareRegistryData();
        registry.restore(instance);
        registry.beginSuccessfulCompletion(instance, 7L);

        NightmareInstance changedPursuer = new NightmareInstance(
                instance.instanceId(), instance.playerId(), instance.slot(), instance.scenarioId(), instance.historicalRoleId(),
                instance.returnDimension(), instance.returnX(), instance.returnY(), instance.returnZ(), instance.returnYaw(),
                instance.returnPitch(), instance.origin(), instance.altar(), Optional.of(UUID.randomUUID()), instance.createdGameTime()
        );
        assertThrows(IllegalStateException.class, () -> registry.update(changedPursuer));
        assertThrows(IllegalStateException.class, () -> registry.ensurePlayerCanCreate(playerId));

        registry.remove(instance);
        assertThrows(IllegalStateException.class, () -> registry.ensurePlayerCanCreate(playerId));
        assertEquals(instance, registry.clearSuccessfulCompletion(instance).orElseThrow().instance());
        registry.ensurePlayerCanCreate(playerId);
    }

    private static NightmareInstance instance(UUID instanceId, UUID playerId, int slot, double returnX) {
        BlockPos origin = LastSignalScenario.originForSlot(slot);
        return new NightmareInstance(
                instanceId,
                playerId,
                slot,
                LastSignalScenario.SCENARIO_ID,
                LastSignalScenario.ROLE_ID,
                ResourceLocation.parse("minecraft:overworld"),
                returnX,
                70.0,
                4.5,
                90.0F,
                10.0F,
                origin,
                LastSignalScenario.altarForOrigin(origin),
                Optional.of(UUID.randomUUID()),
                900L + slot
        );
    }
}
