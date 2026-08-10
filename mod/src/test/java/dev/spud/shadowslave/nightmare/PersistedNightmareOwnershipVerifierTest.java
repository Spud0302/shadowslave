package dev.spud.shadowslave.nightmare;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PersistedNightmareOwnershipVerifierTest {
    @TempDir
    Path tempDir;

    @Test
    void acceptsPersistedRegistryWithoutTargetOwnership() throws IOException {
        UUID playerId = UUID.randomUUID();
        UUID instanceId = UUID.randomUUID();
        ListTag instances = new ListTag();
        instances.add(instance(UUID.randomUUID(), UUID.randomUUID()));

        Path file = writeRegistry(instances);

        assertDoesNotThrow(() -> PersistedNightmareOwnershipVerifier.requireAbsent(file, playerId, instanceId));
    }

    @Test
    void rejectsPersistedOwnershipForTargetPlayerOrInstance() throws IOException {
        UUID playerId = UUID.randomUUID();
        UUID instanceId = UUID.randomUUID();

        ListTag samePlayer = new ListTag();
        samePlayer.add(instance(playerId, UUID.randomUUID()));
        Path samePlayerFile = writeRegistry(samePlayer);
        assertThrows(IllegalStateException.class,
                () -> PersistedNightmareOwnershipVerifier.requireAbsent(samePlayerFile, playerId, instanceId));

        ListTag sameInstance = new ListTag();
        sameInstance.add(instance(UUID.randomUUID(), instanceId));
        Path sameInstanceFile = writeRegistry(sameInstance);
        assertThrows(IllegalStateException.class,
                () -> PersistedNightmareOwnershipVerifier.requireAbsent(sameInstanceFile, playerId, instanceId));
    }

    @Test
    void rejectsEntryThatProductionLoaderWouldRejectEvenWhenOwnershipIdsArePresent() throws IOException {
        CompoundTag malformed = instance(UUID.randomUUID(), UUID.randomUUID());
        malformed.remove("scenario_id");
        ListTag instances = new ListTag();
        instances.add(malformed);
        Path file = writeRegistry(instances);

        assertThrows(IllegalStateException.class, () -> PersistedNightmareOwnershipVerifier.requireAbsent(
                file,
                UUID.randomUUID(),
                UUID.randomUUID()
        ));
    }

    @Test
    void rejectsMalformedOrMissingRegistryAuthoritySurface() throws IOException {
        CompoundTag malformed = new CompoundTag();
        ListTag instances = new ListTag();
        instances.add(malformed);
        Path malformedFile = writeRegistry(instances);

        assertThrows(IllegalStateException.class, () -> PersistedNightmareOwnershipVerifier.requireAbsent(
                malformedFile,
                UUID.randomUUID(),
                UUID.randomUUID()
        ));
        assertThrows(IllegalStateException.class, () -> PersistedNightmareOwnershipVerifier.requireAbsent(
                tempDir.resolve("missing.dat"),
                UUID.randomUUID(),
                UUID.randomUUID()
        ));
    }

    private Path writeRegistry(ListTag instances) throws IOException {
        CompoundTag data = new CompoundTag();
        data.put("instances", instances);
        CompoundTag root = new CompoundTag();
        root.put("data", data);
        Path file = tempDir.resolve("registry-" + UUID.randomUUID() + ".dat");
        NbtIo.writeCompressed(root, file);
        return file;
    }

    private static CompoundTag instance(UUID playerId, UUID instanceId) {
        return new NightmareInstance(
                instanceId,
                playerId,
                0,
                "the_last_signal",
                "last_watchkeeper",
                ResourceLocation.parse("minecraft:overworld"),
                1.0,
                64.0,
                1.0,
                0.0F,
                0.0F,
                BlockPos.ZERO,
                new BlockPos(1, 64, 1),
                Optional.empty(),
                1L
        ).save();
    }
}
