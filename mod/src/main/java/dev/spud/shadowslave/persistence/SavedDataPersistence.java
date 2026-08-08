package dev.spud.shadowslave.persistence;

import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.common.IOUtilities;

import java.util.Objects;

/** Explicit durability barrier for SavedData checkpoints used by recovery transactions. */
public final class SavedDataPersistence {
    private SavedDataPersistence() {
    }

    public static void saveAndWait(MinecraftServer server) {
        MinecraftServer checkedServer = Objects.requireNonNull(server, "server");
        saveAndWait(
                () -> checkedServer.overworld().getDataStorage().save(),
                IOUtilities::waitUntilIOWorkerComplete
        );
    }

    static void saveAndWait(Runnable scheduleSave, Runnable awaitWrites) {
        Objects.requireNonNull(scheduleSave, "scheduleSave").run();
        Objects.requireNonNull(awaitWrites, "awaitWrites").run();
    }
}
