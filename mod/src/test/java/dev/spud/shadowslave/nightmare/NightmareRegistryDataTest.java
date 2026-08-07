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
                "the active ownership used by teardown must be consumable only once");
        assertTrue(afterRestart.findByPlayer(aliceId).isEmpty());
        assertEquals(bob, afterRestart.findByPlayer(bobId).orElseThrow(),
                "Alice teardown must not remove Bob's active Nightmare");

        CompoundTag afterAliceTeardown = afterRestart.save(new CompoundTag(), null);
        assertEquals(8, afterAliceTeardown.getInt("next_slot"),
                "restart must not reuse a slot that belonged to a persisted instance");
    }

    @Test
    void completedReceiptSurvivesAfterActiveOwnershipIsConsumed() {
        UUID playerId = UUID.randomUUID();
        NightmareInstance completed = instance(playerId, 3, 18.0, UUID.randomUUID());
        NightmareRegistryData registry = new NightmareRegistryData();
        registry.restore(completed);

        NightmareCompletionRecord terminal = registry.beginSuccessfulCompletion(completed, 1_200L);
        assertEquals(NightmareCompletionPhase.TERMINAL_RESOLUTION_RECORDED, terminal.phase());
        registry.advanceSuccessfulCompletion(completed, NightmareCompletionPhase.APPRAISAL_COMMITTED);
        registry.advanceSuccessfulCompletion(completed, NightmareCompletionPhase.RETURN_COMMITTED);
        assertEquals(completed, registry.remove(completed).orElseThrow());
        NightmareCompletionRecord finished = registry.advanceSuccessfulCompletion(
                completed,
                NightmareCompletionPhase.TEARDOWN_COMMITTED
        );

        CompoundTag persisted = registry.save(new CompoundTag(), null);
        NightmareRegistryData afterRestart = NightmareRegistryData.load(persisted, null);

        assertTrue(afterRestart.findByPlayer(playerId).isEmpty());
        assertEquals(finished, afterRestart.findSuccessfulCompletionByPlayer(playerId).orElseThrow());
        assertEquals(List.of(finished), List.copyOf(afterRestart.successfulCompletionRecords()));
    }

    @Test
    void everyDurableCompletionBoundaryRoundTripsForRecovery() {
        for (NightmareCompletionPhase target : NightmareCompletionPhase.values()) {
            UUID playerId = UUID.randomUUID();
            NightmareInstance completed = instance(playerId, target.ordinal(), 30.0, UUID.randomUUID());
            NightmareRegistryData registry = new NightmareRegistryData();
            registry.restore(completed);
            registry.beginSuccessfulCompletion(completed, 2_000L + target.ordinal());

            if (target.ordinal() >= NightmareCompletionPhase.APPRAISAL_COMMITTED.ordinal()) {
                registry.advanceSuccessfulCompletion(completed, NightmareCompletionPhase.APPRAISAL_COMMITTED);
            }
            if (target.ordinal() >= NightmareCompletionPhase.RETURN_COMMITTED.ordinal()) {
                registry.advanceSuccessfulCompletion(completed, NightmareCompletionPhase.RETURN_COMMITTED);
            }
            if (target == NightmareCompletionPhase.TEARDOWN_COMMITTED) {
                registry.remove(completed);
                registry.advanceSuccessfulCompletion(completed, NightmareCompletionPhase.TEARDOWN_COMMITTED);
            }

            NightmareRegistryData afterRestart = NightmareRegistryData.load(
                    registry.save(new CompoundTag(), null),
                    null
            );
            NightmareCompletionRecord restored = afterRestart
                    .findSuccessfulCompletionByPlayer(playerId)
                    .orElseThrow();

            assertEquals(target, restored.phase());
            assertEquals(completed, restored.instance());
            assertEquals(
                    target != NightmareCompletionPhase.TEARDOWN_COMMITTED,
                    afterRestart.findByPlayer(playerId).isPresent(),
                    "active ownership remains available until teardown is committed"
            );
        }
    }

    @Test
    void completionProgressionIsOrderedAndReplayIsIdempotent() {
        UUID playerId = UUID.randomUUID();
        NightmareInstance completed = instance(playerId, 0, 10.0, UUID.randomUUID());
        NightmareRegistryData registry = new NightmareRegistryData();
        registry.restore(completed);

        NightmareCompletionRecord first = registry.beginSuccessfulCompletion(completed, 500L);
        assertEquals(first, registry.beginSuccessfulCompletion(completed, 999L),
                "repeated terminal resolution must retain the original receipt");
        assertThrows(
                IllegalStateException.class,
                () -> registry.advanceSuccessfulCompletion(completed, NightmareCompletionPhase.RETURN_COMMITTED)
        );

        NightmareCompletionRecord appraised = registry.advanceSuccessfulCompletion(
                completed,
                NightmareCompletionPhase.APPRAISAL_COMMITTED
        );
        assertEquals(appraised, registry.advanceSuccessfulCompletion(
                completed,
                NightmareCompletionPhase.APPRAISAL_COMMITTED
        ));
        assertEquals(appraised, registry.advanceSuccessfulCompletion(
                completed,
                NightmareCompletionPhase.TERMINAL_RESOLUTION_RECORDED
        ), "replayed earlier milestones must not downgrade the receipt");
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
    void registeredInstanceCannotMoveToAnotherAllocatedSlot() {
        UUID instanceId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        NightmareInstance original = instance(instanceId, playerId, 4, 10.0, UUID.randomUUID());
        NightmareInstance moved = instance(instanceId, playerId, 5, 10.0, UUID.randomUUID());
        NightmareRegistryData registry = new NightmareRegistryData();
        registry.restore(original);

        assertThrows(IllegalStateException.class, () -> registry.update(moved));
        assertEquals(original, registry.findByPlayer(playerId).orElseThrow());
    }

    @Test
    void corruptRestartDataCannotReplaceAnExistingOwnerInstanceOrReceipt() {
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

        NightmareCompletionRecord receipt = new NightmareCompletionRecord(
                original,
                NightmareCompletionPhase.TERMINAL_RESOLUTION_RECORDED,
                100L
        );
        registry.restoreSuccessfulCompletion(receipt);
        assertThrows(IllegalStateException.class, () -> registry.restoreSuccessfulCompletion(receipt));
        assertEquals(receipt, registry.findSuccessfulCompletionByPlayer(aliceId).orElseThrow());
    }

    @Test
    void corruptRestartDataRejectsDifferentActiveAndCompletedInstancesForOnePlayer() {
        UUID playerId = UUID.randomUUID();
        NightmareInstance completed = instance(playerId, 0, 10.0, UUID.randomUUID());
        NightmareInstance newer = instance(playerId, 1, 20.0, UUID.randomUUID());
        NightmareCompletionRecord receipt = new NightmareCompletionRecord(
                completed,
                NightmareCompletionPhase.TERMINAL_RESOLUTION_RECORDED,
                100L
        );

        NightmareRegistryData activeFirst = new NightmareRegistryData();
        activeFirst.restore(newer);
        assertThrows(IllegalStateException.class, () -> activeFirst.restoreSuccessfulCompletion(receipt));
        assertEquals(newer, activeFirst.findByPlayer(playerId).orElseThrow());
        assertTrue(activeFirst.findSuccessfulCompletionByPlayer(playerId).isEmpty());

        NightmareRegistryData receiptFirst = new NightmareRegistryData();
        receiptFirst.restoreSuccessfulCompletion(receipt);
        assertThrows(IllegalStateException.class, () -> receiptFirst.restore(newer));
        assertEquals(receipt, receiptFirst.findSuccessfulCompletionByPlayer(playerId).orElseThrow());
        assertTrue(receiptFirst.findByPlayer(playerId).isEmpty());
    }

    @Test
    void corruptRestartDataRejectsOneInstanceIdAssignedToDifferentPlayersAcrossActiveAndCompletedState() {
        UUID sharedInstanceId = UUID.randomUUID();
        UUID aliceId = UUID.randomUUID();
        UUID bobId = UUID.randomUUID();
        NightmareInstance aliceActive = instance(sharedInstanceId, aliceId, 0, 10.0, UUID.randomUUID());
        NightmareInstance bobCompleted = instance(sharedInstanceId, bobId, 1, 20.0, UUID.randomUUID());
        NightmareCompletionRecord bobReceipt = new NightmareCompletionRecord(
                bobCompleted,
                NightmareCompletionPhase.TEARDOWN_COMMITTED,
                200L
        );

        NightmareRegistryData activeFirst = new NightmareRegistryData();
        activeFirst.restore(aliceActive);
        assertThrows(IllegalStateException.class, () -> activeFirst.restoreSuccessfulCompletion(bobReceipt));
        assertEquals(aliceActive, activeFirst.findByPlayer(aliceId).orElseThrow());
        assertTrue(activeFirst.findSuccessfulCompletionByPlayer(bobId).isEmpty());

        NightmareRegistryData receiptFirst = new NightmareRegistryData();
        receiptFirst.restoreSuccessfulCompletion(bobReceipt);
        assertThrows(IllegalStateException.class, () -> receiptFirst.restore(aliceActive));
        assertEquals(bobReceipt, receiptFirst.findSuccessfulCompletionByPlayer(bobId).orElseThrow());
        assertTrue(receiptFirst.findByPlayer(aliceId).isEmpty());
    }

    @Test
    void corruptRestartDataRejectsOnePhysicalSlotAssignedToDifferentInstances() {
        int sharedSlot = 6;
        UUID aliceId = UUID.randomUUID();
        UUID bobId = UUID.randomUUID();
        NightmareInstance alice = instance(UUID.randomUUID(), aliceId, sharedSlot, 10.0, UUID.randomUUID());
        NightmareInstance bob = instance(UUID.randomUUID(), bobId, sharedSlot, 20.0, UUID.randomUUID());
        NightmareCompletionRecord aliceReceipt = new NightmareCompletionRecord(
                alice,
                NightmareCompletionPhase.TEARDOWN_COMMITTED,
                100L
        );
        NightmareCompletionRecord bobReceipt = new NightmareCompletionRecord(
                bob,
                NightmareCompletionPhase.TEARDOWN_COMMITTED,
                200L
        );

        NightmareRegistryData activeOnly = new NightmareRegistryData();
        activeOnly.restore(alice);
        assertThrows(IllegalStateException.class, () -> activeOnly.restore(bob));
        assertEquals(alice, activeOnly.findByPlayer(aliceId).orElseThrow());
        assertTrue(activeOnly.findByPlayer(bobId).isEmpty());

        NightmareRegistryData completionOnly = new NightmareRegistryData();
        completionOnly.restoreSuccessfulCompletion(aliceReceipt);
        assertThrows(IllegalStateException.class, () -> completionOnly.restoreSuccessfulCompletion(bobReceipt));
        assertEquals(aliceReceipt, completionOnly.findSuccessfulCompletionByPlayer(aliceId).orElseThrow());
        assertTrue(completionOnly.findSuccessfulCompletionByPlayer(bobId).isEmpty());

        NightmareRegistryData activeFirst = new NightmareRegistryData();
        activeFirst.restore(alice);
        assertThrows(IllegalStateException.class, () -> activeFirst.restoreSuccessfulCompletion(bobReceipt));
        assertEquals(alice, activeFirst.findByPlayer(aliceId).orElseThrow());
        assertTrue(activeFirst.findSuccessfulCompletionByPlayer(bobId).isEmpty());

        NightmareRegistryData receiptFirst = new NightmareRegistryData();
        receiptFirst.restoreSuccessfulCompletion(bobReceipt);
        assertThrows(IllegalStateException.class, () -> receiptFirst.restore(alice));
        assertEquals(bobReceipt, receiptFirst.findSuccessfulCompletionByPlayer(bobId).orElseThrow());
        assertTrue(receiptFirst.findByPlayer(aliceId).isEmpty());
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
