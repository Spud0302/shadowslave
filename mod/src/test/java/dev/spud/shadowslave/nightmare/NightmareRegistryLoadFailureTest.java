package dev.spud.shadowslave.nightmare;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightmareRegistryLoadFailureTest {
    @Test
    void legacySaveWithoutCompletionListRemainsHealthy() {
        NightmareInstance active = instance(UUID.randomUUID(), UUID.randomUUID(), 2);
        CompoundTag saved = new CompoundTag();
        saved.putInt("next_slot", 3);
        ListTag instances = new ListTag();
        instances.add(active.save());
        saved.put("instances", instances);

        NightmareRegistryData restored = NightmareRegistryData.load(saved, null);

        assertFalse(restored.recoveryBlocked());
        assertTrue(restored.successfulCompletionRecords().isEmpty());
        assertEquals(active, restored.findByPlayer(active.playerId()).orElseThrow());
    }

    @Test
    void wrongCompletionListTypeLoadsAsBlockedInsteadOfEmptyHealthyRegistry() {
        CompoundTag saved = new CompoundTag();
        saved.putString("successful_completions", "not-a-list");

        NightmareRegistryData restored = NightmareRegistryData.load(saved, null);

        assertTrue(restored.recoveryBlocked());
        assertTrue(restored.loadFailure().orElseThrow().contains("successful_completions"));
        assertThrows(IllegalStateException.class, restored::activeInstances);
        assertThrows(IllegalStateException.class, () -> restored.findSuccessfulCompletionByPlayer(UUID.randomUUID()));
        assertThrows(IllegalStateException.class, () -> restored.save(new CompoundTag(), null));
    }

    @Test
    void contradictoryPersistedActiveAndReceiptLoadAsBlockedRecoveryState() {
        UUID playerId = UUID.randomUUID();
        UUID instanceId = UUID.randomUUID();
        NightmareInstance active = instance(instanceId, playerId, 1);
        NightmareInstance divergent = new NightmareInstance(
                active.instanceId(), active.playerId(), active.slot(), active.scenarioId(), active.historicalRoleId(),
                active.returnDimension(), active.returnX(), active.returnY(), active.returnZ(), active.returnYaw(),
                active.returnPitch(), active.origin(), active.altar(), Optional.of(UUID.randomUUID()), active.createdGameTime()
        );

        CompoundTag saved = new CompoundTag();
        ListTag instances = new ListTag();
        instances.add(active.save());
        saved.put("instances", instances);
        ListTag completions = new ListTag();
        completions.add(new NightmareCompletionRecord(
                divergent,
                NightmareCompletionPhase.TERMINAL_RESOLUTION_RECORDED,
                100L
        ).save());
        saved.put("successful_completions", completions);

        NightmareRegistryData restored = NightmareRegistryData.load(saved, null);

        assertTrue(restored.recoveryBlocked());
        assertTrue(restored.loadFailure().orElseThrow().contains("does not exactly match"));
        assertThrows(IllegalStateException.class, () -> restored.findByPlayer(playerId));
    }

    private static NightmareInstance instance(UUID instanceId, UUID playerId, int slot) {
        BlockPos origin = LastSignalScenario.originForSlot(slot);
        return new NightmareInstance(
                instanceId,
                playerId,
                slot,
                LastSignalScenario.SCENARIO_ID,
                LastSignalScenario.ROLE_ID,
                ResourceLocation.parse("minecraft:overworld"),
                10.5,
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
