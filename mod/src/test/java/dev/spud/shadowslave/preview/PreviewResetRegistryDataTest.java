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
        assertTrue(restored.isPending(playerId));

        restored.complete(playerId);
        restored.complete(playerId);
        assertFalse(restored.isPending(playerId));
    }

    @Test
    void duplicatePersistedMarkersFailClosed() {
        UUID playerId = UUID.randomUUID();
        CompoundTag saved = new CompoundTag();
        ListTag pending = new ListTag();
        pending.add(marker(playerId));
        pending.add(marker(playerId));
        saved.put("pending", pending);

        assertThrows(IllegalStateException.class, () -> PreviewResetRegistryData.load(saved, null));
    }

    @Test
    void malformedPersistedMarkerFailsClosed() {
        CompoundTag saved = new CompoundTag();
        ListTag pending = new ListTag();
        pending.add(new CompoundTag());
        saved.put("pending", pending);

        assertThrows(IllegalStateException.class, () -> PreviewResetRegistryData.load(saved, null));
    }

    private static CompoundTag marker(UUID playerId) {
        CompoundTag marker = new CompoundTag();
        marker.putUUID("player_id", playerId);
        return marker;
    }
}
