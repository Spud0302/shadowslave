package dev.spud.shadowslave.nightmare;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightmareEntryRollbackOwnershipTest {
    @Test
    void preparedSnapshotRemovesTheAuthoritativePreUpdateOwnerAfterUpdateFailure() {
        NightmareInstance created = instance(UUID.randomUUID(), UUID.randomUUID(), 3);
        BlockPos origin = LastSignalScenario.originForSlot(created.slot());
        NightmareInstance prepared = created
                .withLayout(origin, LastSignalScenario.altarForOrigin(origin))
                .withPursuer(UUID.randomUUID());
        NightmareRegistryData registry = new NightmareRegistryData();
        registry.restore(created);

        assertThrows(IllegalStateException.class, () -> registry.remove(prepared),
                "exact-snapshot teardown rejects the prepared copy while the registry still owns the pre-update copy");

        assertEquals(created, NightmareService.removeMatchingEntryOwnership(registry, prepared).orElseThrow());
        assertTrue(registry.findByPlayer(created.playerId()).isEmpty());
    }

    @Test
    void preparedSnapshotRemovesItselfAfterRegistryUpdateSucceeded() {
        NightmareInstance created = instance(UUID.randomUUID(), UUID.randomUUID(), 4);
        BlockPos origin = LastSignalScenario.originForSlot(created.slot());
        NightmareInstance prepared = created
                .withLayout(origin, LastSignalScenario.altarForOrigin(origin))
                .withPursuer(UUID.randomUUID());
        NightmareRegistryData registry = new NightmareRegistryData();
        registry.restore(created);
        registry.update(prepared);

        assertEquals(prepared, NightmareService.removeMatchingEntryOwnership(registry, prepared).orElseThrow());
        assertTrue(registry.findByPlayer(created.playerId()).isEmpty());
    }

    @Test
    void failedEntryRollbackCannotConsumeAnotherInstanceForTheSamePlayer() {
        UUID playerId = UUID.randomUUID();
        NightmareInstance attempted = instance(UUID.randomUUID(), playerId, 5);
        NightmareInstance newer = instance(UUID.randomUUID(), playerId, 6);
        NightmareRegistryData registry = new NightmareRegistryData();
        registry.restore(newer);

        assertTrue(NightmareService.removeMatchingEntryOwnership(registry, attempted).isEmpty());
        assertEquals(newer, registry.findByPlayer(playerId).orElseThrow());
    }

    private static NightmareInstance instance(UUID instanceId, UUID playerId, int slot) {
        return new NightmareInstance(
                instanceId,
                playerId,
                slot,
                LastSignalScenario.SCENARIO_ID,
                LastSignalScenario.ROLE_ID,
                ResourceLocation.parse("minecraft:overworld"),
                12.5,
                70.0,
                -8.5,
                90.0F,
                10.0F,
                BlockPos.ZERO,
                BlockPos.ZERO,
                Optional.empty(),
                1_000L + slot
        );
    }
}
