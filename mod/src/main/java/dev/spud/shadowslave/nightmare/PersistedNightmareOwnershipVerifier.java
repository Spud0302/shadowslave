package dev.spud.shadowslave.nightmare;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

/** Verifies that exact active Nightmare ownership is absent from the persisted registry image. */
final class PersistedNightmareOwnershipVerifier {
    private PersistedNightmareOwnershipVerifier() {
    }

    static void requireAbsent(Path registryFile, UUID playerId, UUID instanceId) {
        Path checkedFile = Objects.requireNonNull(registryFile, "registryFile");
        UUID checkedPlayerId = Objects.requireNonNull(playerId, "playerId");
        UUID checkedInstanceId = Objects.requireNonNull(instanceId, "instanceId");

        if (!Files.isRegularFile(checkedFile)) {
            throw new IllegalStateException("Nightmare registry file is missing after ownership teardown persistence");
        }

        CompoundTag root;
        try {
            root = NbtIo.readCompressed(checkedFile, NbtAccounter.unlimitedHeap());
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read persisted Nightmare registry after ownership teardown", exception);
        }

        Tag rawData = root.get("data");
        if (!(rawData instanceof CompoundTag data)) {
            throw new IllegalStateException("Persisted Nightmare registry has no data compound");
        }

        Tag rawInstances = data.get("instances");
        if (!(rawInstances instanceof ListTag instances)) {
            throw new IllegalStateException("Persisted Nightmare registry has no active-instance list");
        }

        for (Tag rawInstance : instances) {
            if (!(rawInstance instanceof CompoundTag instance)) {
                throw new IllegalStateException("Persisted Nightmare registry contains a malformed active-instance entry");
            }
            boolean samePlayer = instance.hasUUID("player_id")
                    && checkedPlayerId.equals(instance.getUUID("player_id"));
            boolean sameInstance = instance.hasUUID("instance_id")
                    && checkedInstanceId.equals(instance.getUUID("instance_id"));
            if (samePlayer || sameInstance) {
                throw new IllegalStateException(
                        "Persisted Nightmare registry still contains canonical-death active ownership"
                );
            }
        }
    }
}
