package dev.spud.shadowslave.preview;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/** Verifies that an exact preview-reset recovery marker is present in a healthy persisted registry image. */
final class PersistedPreviewResetIntentVerifier {
    private PersistedPreviewResetIntentVerifier() {
    }

    static void requirePresent(Path registryFile, UUID playerId) {
        Path checkedFile = Objects.requireNonNull(registryFile, "registryFile");
        UUID checkedPlayerId = Objects.requireNonNull(playerId, "playerId");

        if (!Files.isRegularFile(checkedFile)) {
            throw new IllegalStateException("Preview reset registry file is missing after persistence");
        }

        CompoundTag root;
        try {
            root = NbtIo.readCompressed(checkedFile, NbtAccounter.unlimitedHeap());
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read persisted preview reset registry", exception);
        }

        Tag rawData = root.get("data");
        if (!(rawData instanceof CompoundTag data)) {
            throw new IllegalStateException("Persisted preview reset registry has no data compound");
        }

        Tag rawPending = data.get("pending");
        if (!(rawPending instanceof ListTag pending)) {
            throw new IllegalStateException("Persisted preview reset registry has no pending list");
        }

        boolean found = false;
        Set<UUID> seenPlayers = new HashSet<>();
        for (int index = 0; index < pending.size(); index++) {
            Tag rawEntry = pending.get(index);
            if (!(rawEntry instanceof CompoundTag entry)) {
                throw new IllegalStateException("Persisted preview reset entry " + index + " is not a compound");
            }
            if (!entry.hasUUID("player_id")) {
                throw new IllegalStateException("Persisted preview reset entry " + index + " is missing player_id");
            }
            UUID persistedPlayerId = entry.getUUID("player_id");
            if (!seenPlayers.add(persistedPlayerId)) {
                throw new IllegalStateException("Persisted preview reset registry contains a duplicate player marker");
            }
            if (checkedPlayerId.equals(persistedPlayerId)) {
                found = true;
            }
        }

        if (!found) {
            throw new IllegalStateException("Persisted preview reset registry does not contain the expected player marker");
        }
    }
}
