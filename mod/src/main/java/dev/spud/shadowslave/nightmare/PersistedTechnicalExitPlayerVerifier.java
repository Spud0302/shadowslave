package dev.spud.shadowslave.nightmare;

import com.mojang.serialization.Codec;
import dev.spud.shadowslave.soul.SoulData;
import dev.spud.shadowslave.soul.SoulTransitions;
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

/** Verifies the exact persisted player-side result required before technical-exit authority is consumed. */
final class PersistedTechnicalExitPlayerVerifier {
    private static final String ATTACHMENTS_KEY = "neoforge:attachments";
    private static final String SOUL_KEY = "shadowslave:soul";
    private static final String IDENTITY_KEY = "shadowslave:identity";
    private static final SoulData RECOVERED_SOUL = SoulTransitions.infect(SoulData.uninfected());
    private static final SoulIdentityData RECOVERED_IDENTITY = SoulIdentityData.empty();

    private PersistedTechnicalExitPlayerVerifier() {
    }

    static void requireRecovered(Path playerFile) {
        Path checkedFile = Objects.requireNonNull(playerFile, "playerFile");
        if (!Files.isRegularFile(checkedFile)) {
            throw new IllegalStateException("Technical-exit player data file is missing after persistence");
        }

        CompoundTag root;
        try {
            root = NbtIo.readCompressed(checkedFile, NbtAccounter.unlimitedHeap());
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read persisted technical-exit player data", exception);
        }

        Tag rawAttachments = root.get(ATTACHMENTS_KEY);
        if (!(rawAttachments instanceof CompoundTag attachments)) {
            throw new IllegalStateException("Persisted technical-exit player data has no NeoForge attachments");
        }

        Tag rawSoul = attachments.get(SOUL_KEY);
        if (rawSoul == null) {
            throw new IllegalStateException("Persisted technical-exit player data has no Soul attachment");
        }
        SoulData persistedSoul = decode(SoulData.CODEC.codec(), rawSoul, "Soul");
        if (!RECOVERED_SOUL.equals(persistedSoul)) {
            throw new IllegalStateException("Persisted technical-exit Soul is not the recovered Carrier state");
        }

        Tag rawIdentity = attachments.get(IDENTITY_KEY);
        if (rawIdentity == null) {
            // Absence reconstructs the attachment's registered empty/default identity and is therefore equivalent
            // to the explicitly cleared state required by technical recovery.
            return;
        }
        SoulIdentityData persistedIdentity = decode(
                SoulIdentityData.CODEC.codec(),
                rawIdentity,
                "Soul identity"
        );
        if (!RECOVERED_IDENTITY.equals(persistedIdentity)) {
            throw new IllegalStateException("Persisted technical-exit Soul identity was not cleared");
        }
    }

    private static <T> T decode(Codec<T> codec, Tag stored, String description) {
        try {
            return codec.parse(NbtOps.INSTANCE, stored).getOrThrow();
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Persisted technical-exit " + description + " is invalid", exception);
        }
    }
}
