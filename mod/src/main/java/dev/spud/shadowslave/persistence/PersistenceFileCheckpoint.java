package dev.spud.shadowslave.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;

/** Verifies that a save boundary produced a new observable file image. */
public final class PersistenceFileCheckpoint {
    private PersistenceFileCheckpoint() {
    }

    public static Snapshot capture(Path path) {
        Path checkedPath = Objects.requireNonNull(path, "path");
        return Files.exists(checkedPath) ? new Snapshot(true, digest(checkedPath)) : Snapshot.missing();
    }

    public static void requireChanged(Path path, Snapshot before, String description) {
        Path checkedPath = Objects.requireNonNull(path, "path");
        Snapshot checkedBefore = Objects.requireNonNull(before, "before");
        String checkedDescription = Objects.requireNonNull(description, "description");
        Snapshot after = capture(checkedPath);
        if (!after.exists()) {
            throw new IllegalStateException(checkedDescription + " was not persisted: " + checkedPath + " does not exist");
        }
        if (checkedBefore.exists() && Arrays.equals(checkedBefore.sha256(), after.sha256())) {
            throw new IllegalStateException(checkedDescription + " was not persisted: " + checkedPath + " did not change");
        }
    }

    private static byte[] digest(Path path) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (var input = Files.newInputStream(path)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) >= 0) {
                    if (read > 0) {
                        digest.update(buffer, 0, read);
                    }
                }
            }
            return digest.digest();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not verify persistence file " + path, exception);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public record Snapshot(boolean exists, byte[] sha256) {
        public Snapshot {
            sha256 = sha256 == null ? null : sha256.clone();
        }

        public static Snapshot missing() {
            return new Snapshot(false, null);
        }

        @Override
        public byte[] sha256() {
            return sha256 == null ? null : sha256.clone();
        }
    }
}
