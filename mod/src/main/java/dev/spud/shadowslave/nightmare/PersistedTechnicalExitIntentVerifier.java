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

/** Verifies that the exact technical/admin exit authority is present in the settled registry file. */
final class PersistedTechnicalExitIntentVerifier {
    private PersistedTechnicalExitIntentVerifier() {
    }

    static void requirePresent(
            Path registryFile,
            UUID playerId,
            UUID instanceId,
            NightmareExitReason reason
    ) {
        Path checkedFile = Objects.requireNonNull(registryFile, "registryFile");
        UUID checkedPlayerId = Objects.requireNonNull(playerId, "playerId");
        UUID checkedInstanceId = Objects.requireNonNull(instanceId, "instanceId");
        NightmareExitReason checkedReason = Objects.requireNonNull(reason, "reason");

        if (!Files.isRegularFile(checkedFile)) {
            throw new IllegalStateException("Technical Nightmare exit registry file is missing after persistence");
        }

        CompoundTag root;
        try {
            root = NbtIo.readCompressed(checkedFile, NbtAccounter.unlimitedHeap());
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read persisted technical Nightmare exit registry", exception);
        }

        Tag rawData = root.get("data");
        if (!(rawData instanceof CompoundTag data)) {
            throw new IllegalStateException("Persisted Nightmare registry has no data compound");
        }

        Tag rawTechnicalExits = data.get("technical_exits");
        if (!(rawTechnicalExits instanceof ListTag technicalExits)) {
            throw new IllegalStateException("Persisted Nightmare registry has no technical-exit list");
        }

        for (Tag rawMarker : technicalExits) {
            if (!(rawMarker instanceof CompoundTag marker)) {
                continue;
            }
            if (marker.hasUUID("player_id")
                    && marker.hasUUID("instance_id")
                    && checkedPlayerId.equals(marker.getUUID("player_id"))
                    && checkedInstanceId.equals(marker.getUUID("instance_id"))
                    && checkedReason.name().equals(marker.getString("reason"))) {
                return;
            }
        }

        throw new IllegalStateException(
                "Persisted Nightmare registry does not contain the exact technical exit authority"
        );
    }
}
