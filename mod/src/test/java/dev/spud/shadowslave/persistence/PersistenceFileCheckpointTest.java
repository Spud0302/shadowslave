package dev.spud.shadowslave.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersistenceFileCheckpointTest {
    @TempDir
    Path tempDir;

    @Test
    void newlyCreatedPersistenceFileSatisfiesCheckpoint() throws IOException {
        Path file = tempDir.resolve("state.dat");
        PersistenceFileCheckpoint.Snapshot before = PersistenceFileCheckpoint.capture(file);
        Files.writeString(file, "first-image");
        assertDoesNotThrow(() -> PersistenceFileCheckpoint.requireChanged(file, before, "state"));
    }

    @Test
    void changedPersistenceFileSatisfiesCheckpoint() throws IOException {
        Path file = tempDir.resolve("state.dat");
        Files.writeString(file, "before");
        PersistenceFileCheckpoint.Snapshot before = PersistenceFileCheckpoint.capture(file);
        Files.writeString(file, "after");
        assertDoesNotThrow(() -> PersistenceFileCheckpoint.requireChanged(file, before, "state"));
    }

    @Test
    void unchangedPersistenceFileFailsClosed() throws IOException {
        Path file = tempDir.resolve("state.dat");
        Files.writeString(file, "same-image");
        PersistenceFileCheckpoint.Snapshot before = PersistenceFileCheckpoint.capture(file);
        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> PersistenceFileCheckpoint.requireChanged(file, before, "state")
        );
        assertTrue(thrown.getMessage().contains("did not change"));
    }

    @Test
    void missingPersistenceFileFailsClosed() {
        Path file = tempDir.resolve("state.dat");
        PersistenceFileCheckpoint.Snapshot before = PersistenceFileCheckpoint.capture(file);
        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> PersistenceFileCheckpoint.requireChanged(file, before, "state")
        );
        assertTrue(thrown.getMessage().contains("does not exist"));
    }
}
