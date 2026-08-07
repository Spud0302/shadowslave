package dev.spud.shadowslave.nightmare;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightmareRegistryLayoutRecoveryTest {
    @Test
    void corruptRestartDataRejectsOriginMismatchForSameActiveAndCompletedInstanceInBothOrders() {
        UUID instanceId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        NightmareInstance active = instance(instanceId, playerId, 4, new BlockPos(768, 96, 0));
        NightmareInstance completedSnapshot = instance(instanceId, playerId, 4, new BlockPos(769, 96, 0));
        NightmareCompletionRecord receipt = receipt(completedSnapshot);

        assertRejectedInBothOrders(active, receipt);
    }

    @Test
    void corruptRestartDataRejectsAltarMismatchForSameActiveAndCompletedInstanceInBothOrders() {
        UUID instanceId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        BlockPos origin = new BlockPos(960, 96, 0);
        NightmareInstance active = instance(instanceId, playerId, 5, origin);
        NightmareInstance completedSnapshot = new NightmareInstance(
                instanceId,
                playerId,
                5,
                LastSignalScenario.SCENARIO_ID,
                LastSignalScenario.ROLE_ID,
                ResourceLocation.parse("minecraft:overworld"),
                10.0,
                70.0,
                4.5,
                90.0F,
                10.0F,
                origin,
                LastSignalScenario.altarForOrigin(origin).offset(1, 0, 0),
                Optional.of(UUID.randomUUID()),
                905L
        );
        NightmareCompletionRecord receipt = receipt(completedSnapshot);

        assertRejectedInBothOrders(active, receipt);
    }

    private static void assertRejectedInBothOrders(
            NightmareInstance active,
            NightmareCompletionRecord receipt
    ) {
        UUID playerId = active.playerId();

        NightmareRegistryData activeFirst = new NightmareRegistryData();
        activeFirst.restore(active);
        assertThrows(IllegalStateException.class, () -> activeFirst.restoreSuccessfulCompletion(receipt));
        assertEquals(active, activeFirst.findByPlayer(playerId).orElseThrow());
        assertTrue(activeFirst.findSuccessfulCompletionByPlayer(playerId).isEmpty());

        NightmareRegistryData receiptFirst = new NightmareRegistryData();
        receiptFirst.restoreSuccessfulCompletion(receipt);
        assertThrows(IllegalStateException.class, () -> receiptFirst.restore(active));
        assertEquals(receipt, receiptFirst.findSuccessfulCompletionByPlayer(playerId).orElseThrow());
        assertTrue(receiptFirst.findByPlayer(playerId).isEmpty());
    }

    private static NightmareCompletionRecord receipt(NightmareInstance instance) {
        return new NightmareCompletionRecord(
                instance,
                NightmareCompletionPhase.TERMINAL_RESOLUTION_RECORDED,
                100L
        );
    }

    private static NightmareInstance instance(
            UUID instanceId,
            UUID playerId,
            int slot,
            BlockPos origin
    ) {
        return new NightmareInstance(
                instanceId,
                playerId,
                slot,
                LastSignalScenario.SCENARIO_ID,
                LastSignalScenario.ROLE_ID,
                ResourceLocation.parse("minecraft:overworld"),
                10.0,
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
