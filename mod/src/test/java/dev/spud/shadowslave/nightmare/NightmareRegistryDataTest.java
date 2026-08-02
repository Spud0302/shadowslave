package dev.spud.shadowslave.nightmare;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightmareRegistryDataTest {
    @Test
    void restartRoundTripPreservesTwoOwnersAndConsumesOnlyOneTeardownToken() {
        UUID aliceId = UUID.randomUUID();
        UUID bobId = UUID.randomUUID();
        NightmareInstance alice = instance(aliceId, 2, 120.5, UUID.randomUUID());
        NightmareInstance bob = instance(bobId, 7, -48.5, UUID.randomUUID());

        NightmareRegistryData beforeRestart = new NightmareRegistryData();
        beforeRestart.restore(alice);
        beforeRestart.restore(bob);

        CompoundTag persisted = beforeRestart.save(new CompoundTag(), null);
        NightmareRegistryData afterRestart = NightmareRegistryData.load(persisted, null);

        assertEquals(List.of(alice, bob), List.copyOf(afterRestart.activeInstances()));
        assertEquals(alice, afterRestart.findByPlayer(aliceId).orElseThrow());
        assertEquals(bob, afterRestart.findByPlayer(bobId).orElseThrow());

        assertEquals(alice, afterRestart.remove(alice).orElseThrow());
        assertTrue(afterRestart.remove(alice).isEmpty(),
                "the ownership record used to gate teardown/appraisal must be consumable only once");
        assertTrue(afterRestart.findByPlayer(aliceId).isEmpty());
        assertEquals(bob, afterRestart.findByPlayer(bobId).orElseThrow(),
                "Alice teardown must not remove Bob's active Nightmare");

        CompoundTag afterAliceTeardown = afterRestart.save(new CompoundTag(), null);
        assertEquals(8, afterAliceTeardown.getInt("next_slot"),
                "restart must not reuse a slot that belonged to a persisted instance");
    }

    @Test
    void staleTeardownCannotRemoveANewerInstanceForTheSamePlayer() {
        UUID aliceId = UUID.randomUUID();
        NightmareInstance completed = instance(aliceId, 0, 10.0, UUID.randomUUID());
        NightmareInstance newer = instance(aliceId, 1, 20.0, UUID.randomUUID());
        NightmareRegistryData registry = new NightmareRegistryData();

        registry.restore(completed);
        assertEquals(completed, registry.remove(completed).orElseThrow());
        registry.restore(newer);

        assertTrue(registry.remove(completed).isEmpty());
        assertEquals(newer, registry.findByPlayer(aliceId).orElseThrow());
    }

    @Test
    void corruptRestartDataCannotReplaceAnExistingOwnerOrInstance() {
        UUID aliceId = UUID.randomUUID();
        UUID sharedInstanceId = UUID.randomUUID();
        NightmareInstance original = instance(sharedInstanceId, aliceId, 0, 10.0, UUID.randomUUID());
        NightmareRegistryData registry = new NightmareRegistryData();
        registry.restore(original);

        NightmareInstance duplicateOwner = instance(UUID.randomUUID(), aliceId, 1, 20.0, UUID.randomUUID());
        assertThrows(IllegalStateException.class, () -> registry.restore(duplicateOwner));
        assertEquals(original, registry.findByPlayer(aliceId).orElseThrow());
        assertEquals(1, registry.activeInstances().size());

        NightmareInstance duplicateInstance = instance(
                sharedInstanceId,
                UUID.randomUUID(),
                2,
                30.0,
                UUID.randomUUID()
        );
        assertThrows(IllegalStateException.class, () -> registry.restore(duplicateInstance));
        assertEquals(original, registry.findByPlayer(aliceId).orElseThrow());
        assertEquals(1, registry.activeInstances().size());
    }

    private static NightmareInstance instance(UUID playerId, int slot, double returnX, UUID pursuerId) {
        return instance(UUID.randomUUID(), playerId, slot, returnX, pursuerId);
    }

    private static NightmareInstance instance(
            UUID instanceId,
            UUID playerId,
            int slot,
            double returnX,
            UUID pursuerId
    ) {
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
                Optional.of(pursuerId),
                900L + slot
        );
    }
}
