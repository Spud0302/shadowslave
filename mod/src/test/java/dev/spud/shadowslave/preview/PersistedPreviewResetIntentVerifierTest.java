package dev.spud.shadowslave.preview;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StringTag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PersistedPreviewResetIntentVerifierTest {
    @TempDir
    Path tempDir;

    @Test
    void acceptsExactPersistedResetMarker() throws IOException {
        UUID playerId = UUID.randomUUID();
        Path file = writeRegistry(marker(playerId));

        assertDoesNotThrow(() -> PersistedPreviewResetIntentVerifier.requirePresent(file, playerId));
    }

    @Test
    void rejectsMissingExpectedPlayerMarker() throws IOException {
        Path file = writeRegistry(marker(UUID.randomUUID()));

        assertThrows(IllegalStateException.class, () -> PersistedPreviewResetIntentVerifier.requirePresent(
                file,
                UUID.randomUUID()
        ));
    }

    @Test
    void rejectsMalformedOrDuplicatePersistedAuthority() throws IOException {
        UUID playerId = UUID.randomUUID();
        Path malformed = writeRegistry(StringTag.valueOf("not-a-marker"));
        assertThrows(IllegalStateException.class, () -> PersistedPreviewResetIntentVerifier.requirePresent(
                malformed,
                playerId
        ));

        Path duplicate = tempDir.resolve("duplicate.dat");
        ListTag pending = new ListTag();
        pending.add(marker(playerId));
        pending.add(marker(playerId));
        writeRegistry(duplicate, pending);
        assertThrows(IllegalStateException.class, () -> PersistedPreviewResetIntentVerifier.requirePresent(
                duplicate,
                playerId
        ));
    }

    @Test
    void rejectsMissingRegistryFile() {
        assertThrows(IllegalStateException.class, () -> PersistedPreviewResetIntentVerifier.requirePresent(
                tempDir.resolve("missing.dat"),
                UUID.randomUUID()
        ));
    }

    private CompoundTag marker(UUID playerId) {
        CompoundTag entry = new CompoundTag();
        entry.putUUID("player_id", playerId);
        return entry;
    }

    private Path writeRegistry(net.minecraft.nbt.Tag entry) throws IOException {
        ListTag pending = new ListTag();
        pending.add(entry);
        Path file = tempDir.resolve(UUID.randomUUID() + ".dat");
        writeRegistry(file, pending);
        return file;
    }

    private void writeRegistry(Path file, ListTag pending) throws IOException {
        CompoundTag data = new CompoundTag();
        data.put("pending", pending);
        CompoundTag root = new CompoundTag();
        root.put("data", data);
        NbtIo.writeCompressed(root, file);
    }
}
