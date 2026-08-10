package dev.spud.shadowslave.nightmare;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PersistedTechnicalExitIntentVerifierTest {
    @TempDir
    Path tempDir;

    @Test
    void acceptsExactPersistedTechnicalExitMarker() throws IOException {
        UUID playerId = UUID.randomUUID();
        UUID instanceId = UUID.randomUUID();
        Path file = writeRegistry(playerId, instanceId, NightmareExitReason.ADMIN_ABORT);

        assertDoesNotThrow(() -> PersistedTechnicalExitIntentVerifier.requirePresent(
                file,
                playerId,
                instanceId,
                NightmareExitReason.ADMIN_ABORT
        ));
    }

    @Test
    void rejectsMissingOrMismatchedAuthority() throws IOException {
        UUID playerId = UUID.randomUUID();
        UUID instanceId = UUID.randomUUID();
        Path file = writeRegistry(playerId, instanceId, NightmareExitReason.TECHNICAL_RECOVERY);

        assertThrows(IllegalStateException.class, () -> PersistedTechnicalExitIntentVerifier.requirePresent(
                file,
                playerId,
                instanceId,
                NightmareExitReason.ADMIN_ABORT
        ));
        assertThrows(IllegalStateException.class, () -> PersistedTechnicalExitIntentVerifier.requirePresent(
                file,
                playerId,
                UUID.randomUUID(),
                NightmareExitReason.TECHNICAL_RECOVERY
        ));
    }

    @Test
    void rejectsMissingRegistryFile() {
        assertThrows(IllegalStateException.class, () -> PersistedTechnicalExitIntentVerifier.requirePresent(
                tempDir.resolve("missing.dat"),
                UUID.randomUUID(),
                UUID.randomUUID(),
                NightmareExitReason.ADMIN_ABORT
        ));
    }

    private Path writeRegistry(UUID playerId, UUID instanceId, NightmareExitReason reason) throws IOException {
        CompoundTag marker = new CompoundTag();
        marker.putUUID("player_id", playerId);
        marker.putUUID("instance_id", instanceId);
        marker.putString("reason", reason.name());

        ListTag technicalExits = new ListTag();
        technicalExits.add(marker);
        CompoundTag data = new CompoundTag();
        data.put("technical_exits", technicalExits);
        CompoundTag root = new CompoundTag();
        root.put("data", data);

        Path file = tempDir.resolve("shadowslave_nightmares.dat");
        NbtIo.writeCompressed(root, file);
        return file;
    }
}
