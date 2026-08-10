package dev.spud.shadowslave.nightmare;

import dev.spud.shadowslave.soul.SoulData;
import dev.spud.shadowslave.soul.SoulTransitions;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PersistedTechnicalExitPlayerVerifierTest {
    @TempDir
    Path tempDir;

    @Test
    void acceptsExactRecoveredCarrierAndClearedIdentity() throws IOException {
        Path file = writePlayer(
                encodeSoul(SoulTransitions.infect(SoulData.uninfected())),
                encodeIdentity(SoulIdentityData.empty())
        );

        assertDoesNotThrow(() -> PersistedTechnicalExitPlayerVerifier.requireRecovered(file));
    }

    @Test
    void acceptsMissingIdentityBecauseReloadDefaultsToEmptyIdentity() throws IOException {
        Path file = writePlayer(encodeSoul(SoulTransitions.infect(SoulData.uninfected())), null);

        assertDoesNotThrow(() -> PersistedTechnicalExitPlayerVerifier.requireRecovered(file));
    }

    @Test
    void rejectsStaleAspirantSoul() throws IOException {
        SoulData aspirant = SoulTransitions.beginFirstNightmare(SoulTransitions.infect(SoulData.uninfected()));
        Path file = writePlayer(encodeSoul(aspirant), encodeIdentity(SoulIdentityData.empty()));

        assertThrows(IllegalStateException.class, () -> PersistedTechnicalExitPlayerVerifier.requireRecovered(file));
    }

    @Test
    void rejectsMissingOrInvalidRecoveryAttachments() throws IOException {
        Path missingSoul = writePlayer(null, encodeIdentity(SoulIdentityData.empty()));
        assertThrows(IllegalStateException.class, () -> PersistedTechnicalExitPlayerVerifier.requireRecovered(missingSoul));

        Path invalidIdentity = writePlayer(
                encodeSoul(SoulTransitions.infect(SoulData.uninfected())),
                StringTag.valueOf("not-an-identity")
        );
        assertThrows(IllegalStateException.class, () -> PersistedTechnicalExitPlayerVerifier.requireRecovered(invalidIdentity));
    }

    @Test
    void rejectsMissingPlayerFile() {
        assertThrows(IllegalStateException.class, () -> PersistedTechnicalExitPlayerVerifier.requireRecovered(
                tempDir.resolve("missing.dat")
        ));
    }

    private Tag encodeSoul(SoulData soul) {
        return SoulData.CODEC.codec().encodeStart(NbtOps.INSTANCE, soul).getOrThrow();
    }

    private Tag encodeIdentity(SoulIdentityData identity) {
        return SoulIdentityData.CODEC.codec().encodeStart(NbtOps.INSTANCE, identity).getOrThrow();
    }

    private Path writePlayer(Tag soul, Tag identity) throws IOException {
        CompoundTag attachments = new CompoundTag();
        if (soul != null) {
            attachments.put("shadowslave:soul", soul);
        }
        if (identity != null) {
            attachments.put("shadowslave:identity", identity);
        }

        CompoundTag root = new CompoundTag();
        root.put("neoforge:attachments", attachments);
        Path file = tempDir.resolve(java.util.UUID.randomUUID() + ".dat");
        NbtIo.writeCompressed(root, file);
        return file;
    }
}
