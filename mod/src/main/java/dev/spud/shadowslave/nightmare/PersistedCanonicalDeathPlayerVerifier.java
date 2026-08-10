package dev.spud.shadowslave.nightmare;

import com.mojang.serialization.Codec;
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

/** Verifies the exact persisted player-side reset required before canonical-death authority is consumed. */
final class PersistedCanonicalDeathPlayerVerifier {
    private static final String ATTACHMENTS_KEY = "neoforge:attachments";
    private static final String SOUL_KEY = "shadowslave:soul";
    private static final String IDENTITY_KEY = "shadowslave:identity";
    private static final SoulData DEATH_RESET_SOUL = SoulData.uninfected();
    private static final SoulIdentityData DEATH_RESET_IDENTITY = SoulIdentityData.empty();

    private PersistedCanonicalDeathPlayerVerifier() {
    }

    static void requireReset(Path playerFile) {
        Path checkedFile = Objects.requireNonNull(playerFile, "playerFile");
        if (!Files.isRegularFile(checkedFile)) {
            throw new IllegalStateException("Canonical-death player data file is missing after persistence");
        }

        CompoundTag root;
        try {
            root = NbtIo.readCompressed(checkedFile, NbtAccounter.unlimitedHeap());
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read persisted canonical-death player data", exception);
        }

        Tag rawAttachments = root.get(ATTACHMENTS_KEY);
        if (!(rawAttachments instanceof CompoundTag attachments)) {
            throw new IllegalStateException("Persisted canonical-death player data has no NeoForge attachments");
        }

        Tag rawSoul = attachments.get(SOUL_KEY);
        if (rawSoul == null) {
            throw new IllegalStateException("Persisted canonical-death player data has no Soul attachment");
        }
        SoulData persistedSoul = decode(SoulData.CODEC.codec(), rawSoul, "Soul");
        if (!DEATH_RESET_SOUL.equals(persistedSoul)) {
            throw new IllegalStateException("Persisted canonical-death Soul is not the required uninfected reset state");
        }

        Tag rawIdentity = attachments.get(IDENTITY_KEY);
        if (rawIdentity == null) {
            // Absence reconstructs the attachment's registered empty/default identity and is therefore equivalent
            // to the explicitly cleared state required by canonical-death recovery.
            return;
        }
        SoulIdentityData persistedIdentity = decode(
                SoulIdentityData.CODEC.codec(),
                rawIdentity,
                "Soul identity"
        );
        if (!DEATH_RESET_IDENTITY.equals(persistedIdentity)) {
            throw new IllegalStateException("Persisted canonical-death Soul identity was not cleared");
        }
    }

    private static <T> T decode(Codec<T> codec, Tag stored, String description) {
        try {
            return codec.parse(NbtOps.INSTANCE, stored).getOrThrow();
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Persisted canonical-death " + description + " is invalid", exception);
        }
    }
}
