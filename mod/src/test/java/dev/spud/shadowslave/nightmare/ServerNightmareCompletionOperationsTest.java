package dev.spud.shadowslave.nightmare;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerNightmareCompletionOperationsTest {
    @Test
    void matchingReceiptRetainsExactResolvedInstanceAuthority() {
        NightmareInstance instance = instance(UUID.randomUUID(), UUID.randomUUID(), 2);
        NightmareCompletionRecord receipt = new NightmareCompletionRecord(
                instance,
                NightmareCompletionPhase.APPRAISAL_COMMITTED,
                1200L
        );

        assertEquals(
                receipt,
                ServerNightmareCompletionOperations.requireMatchingReceipt(instance, Optional.of(receipt))
        );
        assertThrows(
                IllegalStateException.class,
                () -> ServerNightmareCompletionOperations.requireMatchingReceipt(instance, Optional.empty())
        );
    }

    @Test
    void staleOrDifferentReceiptFailsClosed() {
        UUID playerId = UUID.randomUUID();
        NightmareInstance expected = instance(UUID.randomUUID(), playerId, 1);
        NightmareInstance changedSnapshot = new NightmareInstance(
                expected.instanceId(), expected.playerId(), expected.slot(), expected.scenarioId(), expected.historicalRoleId(),
                expected.returnDimension(), expected.returnX(), expected.returnY(), expected.returnZ(), expected.returnYaw(),
                expected.returnPitch(), expected.origin().offset(1, 0, 0), expected.altar(), expected.pursuerId(),
                expected.createdGameTime()
        );
        NightmareInstance differentInstance = instance(UUID.randomUUID(), playerId, 3);

        assertThrows(IllegalStateException.class, () -> ServerNightmareCompletionOperations.requireMatchingReceipt(
                expected,
                Optional.of(new NightmareCompletionRecord(
                        changedSnapshot,
                        NightmareCompletionPhase.TERMINAL_RESOLUTION_RECORDED,
                        1L
                ))
        ));
        assertThrows(IllegalStateException.class, () -> ServerNightmareCompletionOperations.requireMatchingReceipt(
                expected,
                Optional.of(new NightmareCompletionRecord(
                        differentInstance,
                        NightmareCompletionPhase.TERMINAL_RESOLUTION_RECORDED,
                        1L
                ))
        ));
    }

    @Test
    void activeOwnershipRequiresTheExactFrozenSnapshot() {
        UUID playerId = UUID.randomUUID();
        NightmareInstance expected = instance(UUID.randomUUID(), playerId, 4);
        NightmareInstance changedPursuer = new NightmareInstance(
                expected.instanceId(), expected.playerId(), expected.slot(), expected.scenarioId(), expected.historicalRoleId(),
                expected.returnDimension(), expected.returnX(), expected.returnY(), expected.returnZ(), expected.returnYaw(),
                expected.returnPitch(), expected.origin(), expected.altar(), Optional.of(UUID.randomUUID()),
                expected.createdGameTime()
        );
        NightmareInstance differentInstance = instance(UUID.randomUUID(), playerId, 5);

        assertTrue(ServerNightmareCompletionOperations.exactActiveOwnershipPresent(expected, Optional.of(expected)));
        assertFalse(ServerNightmareCompletionOperations.exactActiveOwnershipPresent(expected, Optional.empty()));
        assertThrows(
                IllegalStateException.class,
                () -> ServerNightmareCompletionOperations.exactActiveOwnershipPresent(expected, Optional.of(changedPursuer))
        );
        assertThrows(
                IllegalStateException.class,
                () -> ServerNightmareCompletionOperations.exactActiveOwnershipPresent(expected, Optional.of(differentInstance))
        );
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
                12.5 + slot,
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
