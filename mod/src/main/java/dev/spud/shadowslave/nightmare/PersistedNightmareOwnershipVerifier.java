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
            if (!(rawInstance instanceof CompoundTag instanceTag)) {
                throw new IllegalStateException("Persisted Nightmare registry contains a malformed active-instance entry");
            }

            NightmareInstance instance;
            try {
                instance = NightmareInstance.load(instanceTag);
            } catch (RuntimeException exception) {
                throw new IllegalStateException(
                        "Persisted Nightmare registry contains an active-instance entry rejected by production loading",
                        exception
                );
            }

            if (checkedPlayerId.equals(instance.playerId()) || checkedInstanceId.equals(instance.instanceId())) {
                throw new IllegalStateException(
                        "Persisted Nightmare registry still contains successful-completion active ownership"
                );
            }
        }
    }
}
