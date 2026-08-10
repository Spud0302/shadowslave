package dev.spud.shadowslave.appraisal;

import com.mojang.serialization.Codec;
import dev.spud.shadowslave.echo.EchoOwnershipData;
import dev.spud.shadowslave.memory.MemoryOwnershipData;
import dev.spud.shadowslave.soul.SoulData;
import dev.spud.shadowslave.soul.identity.AttributeOwnershipData;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Verifies the exact persisted player attachment image before successful-completion recovery authority is consumed.
 */
public final class PersistedGeneratedAppraisalPlayerVerifier {
    private static final String ATTACHMENTS_KEY = "neoforge:attachments";
    private static final String SOUL_KEY = "shadowslave:soul";
    private static final String IDENTITY_KEY = "shadowslave:identity";
    private static final String ATTRIBUTES_KEY = "shadowslave:attributes";
    private static final String MEMORIES_KEY = "shadowslave:memories";
    private static final String ECHOES_KEY = "shadowslave:echoes";

    private PersistedGeneratedAppraisalPlayerVerifier() {
    }

    public static void requireCommitted(
            Path playerFile,
            GeneratedAppraisalRecoveryService.PlayerState expected,
            GeneratedAppraisalRecoverySnapshot snapshot
    ) {
        Path checkedFile = Objects.requireNonNull(playerFile, "playerFile");
        GeneratedAppraisalRecoveryService.PlayerState checkedExpected = Objects.requireNonNull(expected, "expected");
        GeneratedAppraisalRecoverySnapshot checkedSnapshot = Objects.requireNonNull(snapshot, "snapshot");

        GeneratedAppraisalRecoveryService.RecoveryPlan receiptProof =
                GeneratedAppraisalRecoveryService.plan(checkedExpected, checkedSnapshot);
        if (!receiptProof.alreadyComplete() || !receiptProof.target().equals(checkedExpected)) {
            throw new IllegalStateException("Expected player state does not contain the exact generated appraisal receipt award");
        }

        if (!Files.isRegularFile(checkedFile)) {
            throw new IllegalStateException("Generated-appraisal player data file is missing after persistence");
        }

        CompoundTag root;
        try {
            root = NbtIo.readCompressed(checkedFile, NbtAccounter.unlimitedHeap());
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read persisted generated-appraisal player data", exception);
        }

        Tag rawAttachments = root.get(ATTACHMENTS_KEY);
        if (!(rawAttachments instanceof CompoundTag attachments)) {
            throw new IllegalStateException("Persisted generated-appraisal player data has no NeoForge attachments");
        }

        GeneratedAppraisalRecoveryService.PlayerState persisted = new GeneratedAppraisalRecoveryService.PlayerState(
                decodeRequired(attachments, SOUL_KEY, SoulData.CODEC.codec(), "Soul"),
                decodeRequired(attachments, IDENTITY_KEY, SoulIdentityData.CODEC.codec(), "Soul identity"),
                decodeRequired(attachments, ATTRIBUTES_KEY, AttributeOwnershipData.CODEC.codec(), "Attribute ownership"),
                decodeRequired(attachments, MEMORIES_KEY, MemoryOwnershipData.CODEC.codec(), "Memory ownership"),
                decodeRequired(attachments, ECHOES_KEY, EchoOwnershipData.CODEC.codec(), "Echo ownership")
        );

        if (!persisted.equals(checkedExpected)) {
            throw new IllegalStateException("Persisted generated-appraisal player state does not match the committed attachment state");
        }
    }

    private static <T> T decodeRequired(
            CompoundTag attachments,
            String key,
            Codec<T> codec,
            String description
    ) {
        Tag stored = attachments.get(key);
        if (stored == null) {
            throw new IllegalStateException("Persisted generated-appraisal player data has no " + description + " attachment");
        }
        try {
            return codec.parse(NbtOps.INSTANCE, stored).getOrThrow();
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Persisted generated-appraisal " + description + " is invalid", exception);
        }
    }
}
