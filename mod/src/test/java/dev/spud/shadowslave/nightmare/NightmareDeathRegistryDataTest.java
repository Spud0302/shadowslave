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

class NightmareDeathRegistryDataTest {
    @Test
    void exactDeathIntentRoundTripsAndClearsIdempotently() {
        NightmareInstance instance = instance(UUID.randomUUID());
        NightmareDeathRegistryData data = new NightmareDeathRegistryData();
        data.begin(instance);
        data.begin(instance);

        NightmareDeathRegistryData restarted = NightmareDeathRegistryData.load(
                data.save(new CompoundTag(), null),
                null
        );

        assertEquals(instance, restarted.findByPlayer(instance.playerId()).orElseThrow());
        assertTrue(restarted.isDurablyTrusted(instance));
        restarted.complete(instance);
        restarted.complete(instance);
        assertTrue(restarted.findByPlayer(instance.playerId()).isEmpty());
    }

    @Test
    void unverifiedIntentRetriesAdvancePersistenceRevisionUntilVerified() {
        NightmareInstance instance = instance(UUID.randomUUID());
        NightmareDeathRegistryData data = new NightmareDeathRegistryData();

        assertEquals(0L, data.save(new CompoundTag(), null).getLong("persistence_revision"));

        data.begin(instance);
        assertFalse(data.isDurablyTrusted(instance));
        assertEquals(1L, data.save(new CompoundTag(), null).getLong("persistence_revision"));

        data.begin(instance);
        assertFalse(data.isDurablyTrusted(instance));
        assertEquals(2L, data.save(new CompoundTag(), null).getLong("persistence_revision"),
                "retrying ambiguous authority must force a distinguishable persistence image");

        data.markDurablyTrusted(instance);
        assertTrue(data.isDurablyTrusted(instance));

        data.begin(instance);
        assertEquals(2L, data.save(new CompoundTag(), null).getLong("persistence_revision"),
                "trusted restart authority must not demand another initial persistence checkpoint");
    }

    @Test
    void staleSnapshotCannotRewriteOrClearPendingDeath() {
        NightmareInstance instance = instance(UUID.randomUUID());
        NightmareInstance changed = instance.withLayout(instance.origin().offset(1, 0, 0), instance.altar());
        NightmareDeathRegistryData data = new NightmareDeathRegistryData();
        data.begin(instance);

        assertThrows(IllegalStateException.class, () -> data.begin(changed));
        assertThrows(IllegalStateException.class, () -> data.complete(changed));
        assertEquals(instance, data.findByPlayer(instance.playerId()).orElseThrow());
    }

    @Test
    void malformedOrDuplicatePersistedMarkersBlockRecoveryInsteadOfBecomingEmpty() {
        NightmareInstance instance = instance(UUID.randomUUID());
        CompoundTag malformed = new CompoundTag();
        malformed.putString("pending", "not-a-list");
        NightmareDeathRegistryData malformedLoaded = NightmareDeathRegistryData.load(malformed, null);
        assertTrue(malformedLoaded.recoveryBlocked());
        assertThrows(IllegalStateException.class, () -> malformedLoaded.findByPlayer(instance.playerId()));

        CompoundTag duplicate = new CompoundTag();
        ListTag pending = new ListTag();
        pending.add(instance.save());
        pending.add(instance.save());
        duplicate.put("pending", pending);
        NightmareDeathRegistryData duplicateLoaded = NightmareDeathRegistryData.load(duplicate, null);
        assertTrue(duplicateLoaded.recoveryBlocked());
        assertThrows(IllegalStateException.class, () -> duplicateLoaded.findByPlayer(instance.playerId()));
    }

    private static NightmareInstance instance(UUID playerId) {
        int slot = 7;
        BlockPos origin = LastSignalScenario.originForSlot(slot);
        return new NightmareInstance(
                UUID.randomUUID(),
                playerId,
                slot,
                LastSignalScenario.SCENARIO_ID,
                LastSignalScenario.ROLE_ID,
                ResourceLocation.parse("minecraft:overworld"),
                8.5,
                72.0,
                -4.5,
                0.0F,
                0.0F,
                origin,
                LastSignalScenario.altarForOrigin(origin),
                Optional.of(UUID.randomUUID()),
                1_337L
        );
    }
}
