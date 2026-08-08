package dev.spud.shadowslave.preview;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PreviewResetRegistryDataTest {
    @Test
    void pendingResetRoundTripsAndCompletesIdempotently() {
        UUID playerId = UUID.randomUUID();
        PreviewResetRegistryData data = new PreviewResetRegistryData();

        data.begin(playerId);
        data.begin(playerId);
        assertTrue(data.isPending(playerId));

        CompoundTag saved = data.save(new CompoundTag(), null);
        PreviewResetRegistryData restored = PreviewResetRegistryData.load(saved, null);
        assertFalse(restored.recoveryBlocked());
        assertTrue(restored.isPending(playerId));

        restored.complete(playerId);
        restored.complete(playerId);
        assertFalse(restored.isPending(playerId));
    }

    @Test
    void duplicatePersistedMarkersRemainLoadedAsGloballyBlockedRecoveryState() {
        UUID playerId = UUID.randomUUID();
        CompoundTag saved = new CompoundTag();
        ListTag pending = new ListTag();
        pending.add(marker(playerId));
        pending.add(marker(playerId));
        saved.put("pending", pending);

        PreviewResetRegistryData restored = PreviewResetRegistryData.load(saved, null);

        assertTrue(restored.recoveryBlocked());
        assertTrue(restored.loadFailure().orElseThrow().contains("Duplicate"));
        assertThrows(IllegalStateException.class, () -> restored.isPending(playerId));
        assertThrows(IllegalStateException.class, () -> restored.begin(UUID.randomUUID()));
    }

    @Test
    void malformedPersistedMarkerRemainsLoadedAsGloballyBlockedRecoveryState() {
        CompoundTag saved = new CompoundTag();
        ListTag pending = new ListTag();
        pending.add(new CompoundTag());
        saved.put("pending", pending);

        PreviewResetRegistryData restored = PreviewResetRegistryData.load(saved, null);

        assertTrue(restored.recoveryBlocked());
        assertTrue(restored.loadFailure().orElseThrow().contains("missing player_id"));
        assertThrows(IllegalStateException.class, () -> restored.complete(UUID.randomUUID()));
    }

    @Test
    void wrongPersistedMarkerTypeRemainsLoadedAsBlockedInsteadOfBecomingAnEmptyRegistry() {
        CompoundTag saved = new CompoundTag();
        saved.putString("pending", "not-a-list");

        PreviewResetRegistryData restored = PreviewResetRegistryData.load(saved, null);

        assertTrue(restored.recoveryBlocked());
        assertTrue(restored.loadFailure().orElseThrow().contains("pending list"));
    }

    private static CompoundTag marker(UUID playerId) {
        CompoundTag marker = new CompoundTag();
        marker.putUUID("player_id", playerId);
        return marker;
    }
}
