package dev.spud.shadowslave.persistence;

import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.common.IOUtilities;

import java.util.Objects;

/** Explicit durability barrier for recovery-critical SavedData checkpoints. */
public final class SavedDataPersistence {
    private SavedDataPersistence() {}

    public static void saveAndWait(MinecraftServer server) {
        MinecraftServer checked = Objects.requireNonNull(server, "server");
        checked.overworld().getDataStorage().save();
        IOUtilities.waitUntilIOWorkerComplete();
    }
}
