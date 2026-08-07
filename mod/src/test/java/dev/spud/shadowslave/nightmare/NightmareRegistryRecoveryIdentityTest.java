package dev.spud.shadowslave.nightmare;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightmareRegistryRecoveryIdentityTest {
    @Test
    void corruptRestartDataRejectsScenarioMismatchInBothOrders() {
        NightmareInstance active = baseInstance();
        NightmareInstance completed = copy(active, "other_scenario", active.historicalRoleId(), active.returnDimension(),
                active.returnX(), active.returnY(), active.returnZ(), active.returnYaw(), active.returnPitch(), active.createdGameTime());
        assertRejectedInBothOrders(active, receipt(completed));
    }

    @Test
    void corruptRestartDataRejectsHistoricalRoleMismatchInBothOrders() {
        NightmareInstance active = baseInstance();
        NightmareInstance completed = copy(active, active.scenarioId(), "other_role", active.returnDimension(),
                active.returnX(), active.returnY(), active.returnZ(), active.returnYaw(), active.returnPitch(), active.createdGameTime());
        assertRejectedInBothOrders(active, receipt(completed));
    }

    @Test
    void corruptRestartDataRejectsReturnDestinationMismatchInBothOrders() {
        NightmareInstance active = baseInstance();
        NightmareInstance completed = copy(active, active.scenarioId(), active.historicalRoleId(),
                ResourceLocation.parse("minecraft:the_nether"), active.returnX() + 1.0, active.returnY(), active.returnZ(),
                active.returnYaw(), active.returnPitch(), active.createdGameTime());
        assertRejectedInBothOrders(active, receipt(completed));
    }

    @Test
    void corruptRestartDataRejectsCreationTimeMismatchInBothOrders() {
        NightmareInstance active = baseInstance();
        NightmareInstance completed = copy(active, active.scenarioId(), active.historicalRoleId(), active.returnDimension(),
                active.returnX(), active.returnY(), active.returnZ(), active.returnYaw(), active.returnPitch(), active.createdGameTime() + 1L);
        assertRejectedInBothOrders(active, receipt(completed));
    }

    @Test
    void runtimeUpdateCannotRewritePersistentRecoveryIdentity() {
        NightmareInstance active = baseInstance();
        NightmareRegistryData registry = new NightmareRegistryData();
        registry.restore(active);
        NightmareInstance rewritten = copy(active, "other_scenario", active.historicalRoleId(), active.returnDimension(),
                active.returnX(), active.returnY(), active.returnZ(), active.returnYaw(), active.returnPitch(), active.createdGameTime());

        assertThrows(IllegalStateException.class, () -> registry.update(rewritten));
        assertEquals(active, registry.findByPlayer(active.playerId()).orElseThrow());
    }

    private static void assertRejectedInBothOrders(NightmareInstance active, NightmareCompletionRecord receipt) {
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
        return new NightmareCompletionRecord(instance, NightmareCompletionPhase.TERMINAL_RESOLUTION_RECORDED, 100L);
    }

    private static NightmareInstance baseInstance() {
        UUID instanceId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        BlockPos origin = new BlockPos(1152, 96, 0);
        return new NightmareInstance(
                instanceId, playerId, 6, LastSignalScenario.SCENARIO_ID, LastSignalScenario.ROLE_ID,
                ResourceLocation.parse("minecraft:overworld"), 10.0, 70.0, 4.5, 90.0F, 10.0F,
                origin, LastSignalScenario.altarForOrigin(origin), Optional.of(UUID.randomUUID()), 906L
        );
    }

    private static NightmareInstance copy(
            NightmareInstance source,
            String scenarioId,
            String historicalRoleId,
            ResourceLocation returnDimension,
            double returnX,
            double returnY,
            double returnZ,
            float returnYaw,
            float returnPitch,
            long createdGameTime
    ) {
        return new NightmareInstance(
                source.instanceId(), source.playerId(), source.slot(), scenarioId, historicalRoleId,
                returnDimension, returnX, returnY, returnZ, returnYaw, returnPitch,
                source.origin(), source.altar(), source.pursuerId(), createdGameTime
        );
    }
}
