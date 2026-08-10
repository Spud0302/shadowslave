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

class PersistedCanonicalDeathPlayerVerifierTest {
    @TempDir
    Path tempDir;

    @Test
    void acceptsExactUninfectedResetAndClearedIdentity() throws IOException {
        Path file = writePlayer(
                encodeSoul(SoulData.uninfected()),
                encodeIdentity(SoulIdentityData.empty())
        );

        assertDoesNotThrow(() -> PersistedCanonicalDeathPlayerVerifier.requireReset(file));
    }

    @Test
    void acceptsMissingIdentityBecauseReloadDefaultsToEmptyIdentity() throws IOException {
        Path file = writePlayer(encodeSoul(SoulData.uninfected()), null);

        assertDoesNotThrow(() -> PersistedCanonicalDeathPlayerVerifier.requireReset(file));
    }

    @Test
    void rejectsCarrierAndStaleAspirantSoulStates() throws IOException {
        SoulData carrier = SoulTransitions.infect(SoulData.uninfected());
        Path carrierFile = writePlayer(encodeSoul(carrier), encodeIdentity(SoulIdentityData.empty()));
        assertThrows(IllegalStateException.class, () -> PersistedCanonicalDeathPlayerVerifier.requireReset(carrierFile));

        SoulData aspirant = SoulTransitions.beginFirstNightmare(carrier);
        Path aspirantFile = writePlayer(encodeSoul(aspirant), encodeIdentity(SoulIdentityData.empty()));
        assertThrows(IllegalStateException.class, () -> PersistedCanonicalDeathPlayerVerifier.requireReset(aspirantFile));
    }

    @Test
    void rejectsMissingOrInvalidRecoveryAttachments() throws IOException {
        Path missingSoul = writePlayer(null, encodeIdentity(SoulIdentityData.empty()));
        assertThrows(IllegalStateException.class, () -> PersistedCanonicalDeathPlayerVerifier.requireReset(missingSoul));

        Path invalidIdentity = writePlayer(
                encodeSoul(SoulData.uninfected()),
                StringTag.valueOf("not-an-identity")
        );
        assertThrows(IllegalStateException.class, () -> PersistedCanonicalDeathPlayerVerifier.requireReset(invalidIdentity));
    }

    @Test
    void rejectsMissingPlayerFile() {
        assertThrows(IllegalStateException.class, () -> PersistedCanonicalDeathPlayerVerifier.requireReset(
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
