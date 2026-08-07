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

class NightmareRegistryTechnicalExitTest {
    @Test
    void technicalExitIntentRoundTripsWithActiveOwnershipAndCompletionReceipt() {
        UUID playerId = UUID.randomUUID();
        NightmareInstance instance = instance(playerId);
        NightmareRegistryData registry = new NightmareRegistryData();
        registry.restore(instance);
        registry.beginSuccessfulCompletion(instance, 1_200L);
        registry.beginTechnicalExit(instance, NightmareExitReason.ADMIN_ABORT);

        NightmareRegistryData restarted = NightmareRegistryData.load(
                registry.save(new CompoundTag(), null),
                null
        );

        assertEquals(instance, restarted.findByPlayer(playerId).orElseThrow());
        assertTrue(restarted.findSuccessfulCompletionByPlayer(playerId).isPresent());
        assertEquals(
                NightmareExitReason.ADMIN_ABORT,
                restarted.findTechnicalExitReasonByPlayer(playerId).orElseThrow()
        );
        assertThrows(
                IllegalStateException.class,
                () -> restarted.remove(instance),
                "ordinary teardown must not bypass a durable technical-exit transaction"
        );
    }

    @Test
    void exactTechnicalExitClearsIntentOnlyAfterCompletionReceiptIsGone() {
        UUID playerId = UUID.randomUUID();
        NightmareInstance instance = instance(playerId);
        NightmareRegistryData registry = new NightmareRegistryData();
        registry.restore(instance);
        registry.beginSuccessfulCompletion(instance, 1_200L);
        registry.beginTechnicalExit(instance, NightmareExitReason.TECHNICAL_RECOVERY);

        assertThrows(IllegalStateException.class, () -> registry.completeTechnicalExit(instance));
        assertEquals(instance, registry.findByPlayer(playerId).orElseThrow());
        assertEquals(
                NightmareExitReason.TECHNICAL_RECOVERY,
                registry.findTechnicalExitReasonByPlayer(playerId).orElseThrow()
        );

        registry.clearSuccessfulCompletion(instance);
        assertEquals(instance, registry.completeTechnicalExit(instance).orElseThrow());
        assertTrue(registry.findByPlayer(playerId).isEmpty());
        assertTrue(registry.findSuccessfulCompletionByPlayer(playerId).isEmpty());
        assertTrue(registry.findTechnicalExitReasonByPlayer(playerId).isEmpty());

        NightmareRegistryData restarted = NightmareRegistryData.load(
                registry.save(new CompoundTag(), null),
                null
        );
        assertTrue(restarted.findByPlayer(playerId).isEmpty());
        assertTrue(restarted.findTechnicalExitReasonByPlayer(playerId).isEmpty());
    }

    @Test
    void pendingTechnicalExitReasonCannotBeRewritten() {
        UUID playerId = UUID.randomUUID();
        NightmareInstance instance = instance(playerId);
        NightmareRegistryData registry = new NightmareRegistryData();
        registry.restore(instance);
        registry.beginTechnicalExit(instance, NightmareExitReason.ADMIN_ABORT);

        assertThrows(
                IllegalStateException.class,
                () -> registry.beginTechnicalExit(instance, NightmareExitReason.TECHNICAL_RECOVERY)
        );
        assertEquals(
                NightmareExitReason.ADMIN_ABORT,
                registry.findTechnicalExitReasonByPlayer(playerId).orElseThrow()
        );
    }

    private static NightmareInstance instance(UUID playerId) {
        int slot = 4;
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
                -3.5,
                90.0F,
                10.0F,
                origin,
                LastSignalScenario.altarForOrigin(origin),
                Optional.of(UUID.randomUUID()),
                904L
        );
    }
}
