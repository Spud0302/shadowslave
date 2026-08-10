package dev.spud.shadowslave.preview;

import dev.spud.shadowslave.migration.ImportedIdentityData;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PersistedPreviewResetPlayerVerifierTest {
    @TempDir
    Path tempDir;

    @Test
    void acceptsExplicitAllDefaultResetState() throws IOException {
        Path file = writePlayer(new CompoundTag());
        CompoundTag attachments = readAttachments(file);
        attachments.put("shadowslave:soul", encodeSoul(SoulData.uninfected()));
        attachments.put("shadowslave:identity", encodeIdentity(SoulIdentityData.empty()));
        attachments.put("shadowslave:imported_identity", encodeImported(ImportedIdentityData.empty()));
        attachments.put("shadowslave:preview_power", encodePower(PreviewPowerData.empty()));
        rewritePlayer(file, attachments);

        assertDoesNotThrow(() -> PersistedPreviewResetPlayerVerifier.requireReset(file));
    }

    @Test
    void acceptsMissingDefaultAttachmentsAndMissingAttachmentContainer() throws IOException {
        Path emptyAttachments = writePlayer(new CompoundTag());
        assertDoesNotThrow(() -> PersistedPreviewResetPlayerVerifier.requireReset(emptyAttachments));

        CompoundTag root = new CompoundTag();
        Path noAttachments = tempDir.resolve(UUID.randomUUID() + ".dat");
        NbtIo.writeCompressed(root, noAttachments);
        assertDoesNotThrow(() -> PersistedPreviewResetPlayerVerifier.requireReset(noAttachments));
    }

    @Test
    void rejectsStalePlayerStateInAnyResetAttachment() throws IOException {
        CompoundTag attachments = new CompoundTag();
        attachments.put("shadowslave:soul", encodeSoul(SoulTransitions.infect(SoulData.uninfected())));
        Path staleSoul = writePlayer(attachments);
        assertThrows(IllegalStateException.class, () -> PersistedPreviewResetPlayerVerifier.requireReset(staleSoul));

        attachments = new CompoundTag();
        attachments.put("shadowslave:preview_power", encodePower(new PreviewPowerData(200L)));
        Path stalePower = writePlayer(attachments);
        assertThrows(IllegalStateException.class, () -> PersistedPreviewResetPlayerVerifier.requireReset(stalePower));
    }

    @Test
    void rejectsMalformedAttachmentPayloadOrContainer() throws IOException {
        CompoundTag attachments = new CompoundTag();
        attachments.put("shadowslave:imported_identity", StringTag.valueOf("invalid"));
        Path invalidImportedIdentity = writePlayer(attachments);
        assertThrows(
                IllegalStateException.class,
                () -> PersistedPreviewResetPlayerVerifier.requireReset(invalidImportedIdentity)
        );

        CompoundTag malformedRoot = new CompoundTag();
        malformedRoot.put("neoforge:attachments", StringTag.valueOf("invalid"));
        Path malformedContainer = tempDir.resolve(UUID.randomUUID() + ".dat");
        NbtIo.writeCompressed(malformedRoot, malformedContainer);
        assertThrows(
                IllegalStateException.class,
                () -> PersistedPreviewResetPlayerVerifier.requireReset(malformedContainer)
        );
    }

    @Test
    void rejectsMissingPlayerFile() {
        assertThrows(
                IllegalStateException.class,
                () -> PersistedPreviewResetPlayerVerifier.requireReset(tempDir.resolve("missing.dat"))
        );
    }

    private Tag encodeSoul(SoulData soul) {
        return SoulData.CODEC.codec().encodeStart(NbtOps.INSTANCE, soul).getOrThrow();
    }

    private Tag encodeIdentity(SoulIdentityData identity) {
        return SoulIdentityData.CODEC.codec().encodeStart(NbtOps.INSTANCE, identity).getOrThrow();
    }

    private Tag encodeImported(ImportedIdentityData identity) {
        return ImportedIdentityData.CODEC.codec().encodeStart(NbtOps.INSTANCE, identity).getOrThrow();
    }

    private Tag encodePower(PreviewPowerData power) {
        return PreviewPowerData.CODEC.codec().encodeStart(NbtOps.INSTANCE, power).getOrThrow();
    }

    private Path writePlayer(CompoundTag attachments) throws IOException {
        CompoundTag root = new CompoundTag();
        root.put("neoforge:attachments", attachments);
        Path file = tempDir.resolve(UUID.randomUUID() + ".dat");
        NbtIo.writeCompressed(root, file);
        return file;
    }

    private CompoundTag readAttachments(Path file) throws IOException {
        return NbtIo.readCompressed(file, net.minecraft.nbt.NbtAccounter.unlimitedHeap())
                .getCompound("neoforge:attachments");
    }

    private void rewritePlayer(Path file, CompoundTag attachments) throws IOException {
        CompoundTag root = new CompoundTag();
        root.put("neoforge:attachments", attachments);
        NbtIo.writeCompressed(root, file);
    }
}
