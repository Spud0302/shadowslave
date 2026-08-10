package dev.spud.shadowslave.preview;

import com.mojang.serialization.Codec;
import dev.spud.shadowslave.migration.ImportedIdentityData;
import dev.spud.shadowslave.soul.SoulData;
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

/** Verifies the persisted all-default player baseline before preview-reset authority is consumed. */
final class PersistedPreviewResetPlayerVerifier {
    private static final String ATTACHMENTS_KEY = "neoforge:attachments";
    private static final String SOUL_KEY = "shadowslave:soul";
    private static final String IDENTITY_KEY = "shadowslave:identity";
    private static final String IMPORTED_IDENTITY_KEY = "shadowslave:imported_identity";
    private static final String PREVIEW_POWER_KEY = "shadowslave:preview_power";

    private PersistedPreviewResetPlayerVerifier() {
    }

    static void requireReset(Path playerFile) {
        Path checkedFile = Objects.requireNonNull(playerFile, "playerFile");
        if (!Files.isRegularFile(checkedFile)) {
            throw new IllegalStateException("Preview-reset player data file is missing after persistence");
        }

        CompoundTag root;
        try {
            root = NbtIo.readCompressed(checkedFile, NbtAccounter.unlimitedHeap());
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read persisted preview-reset player data", exception);
        }

        Tag rawAttachments = root.get(ATTACHMENTS_KEY);
        if (rawAttachments == null) {
            // All four reset targets are the registered attachment defaults, so an absent
            // attachment container reconstructs to the exact desired reset state.
            return;
        }
        if (!(rawAttachments instanceof CompoundTag attachments)) {
            throw new IllegalStateException("Persisted preview-reset NeoForge attachments are malformed");
        }

        requireDefault(
                attachments.get(SOUL_KEY),
                SoulData.CODEC.codec(),
                SoulData.uninfected(),
                "Soul"
        );
        requireDefault(
                attachments.get(IDENTITY_KEY),
                SoulIdentityData.CODEC.codec(),
                SoulIdentityData.empty(),
                "Soul identity"
        );
        requireDefault(
                attachments.get(IMPORTED_IDENTITY_KEY),
                ImportedIdentityData.CODEC.codec(),
                ImportedIdentityData.empty(),
                "imported identity"
        );
        requireDefault(
                attachments.get(PREVIEW_POWER_KEY),
                PreviewPowerData.CODEC.codec(),
                PreviewPowerData.empty(),
                "preview power"
        );
    }

    private static <T> void requireDefault(Tag stored, Codec<T> codec, T expected, String description) {
        if (stored == null) {
            // Missing serialized data reconstructs through the registered default supplier.
            return;
        }

        T decoded;
        try {
            decoded = codec.parse(NbtOps.INSTANCE, stored).getOrThrow();
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Persisted preview-reset " + description + " is invalid", exception);
        }
        if (!expected.equals(decoded)) {
            throw new IllegalStateException("Persisted preview-reset " + description + " was not cleared");
        }
    }
}
