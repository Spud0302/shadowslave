package dev.spud.shadowslave.migration;

import dev.spud.shadowslave.attachment.ModAttachments;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

/** Mutation boundary for the persistent imported Aspect/Flaw metadata attachment. */
public final class ImportedIdentityService {
    private ImportedIdentityService() {
    }

    public static ImportedIdentityData get(ServerPlayer player) {
        return Objects.requireNonNull(player, "player").getData(ModAttachments.IMPORTED_IDENTITY);
    }

    public static ImportedIdentityData replace(ServerPlayer player, ImportedIdentityData next) {
        ServerPlayer checkedPlayer = Objects.requireNonNull(player, "player");
        ImportedIdentityData checkedNext = Objects.requireNonNull(next, "next");
        checkedPlayer.setData(ModAttachments.IMPORTED_IDENTITY, checkedNext);
        return checkedNext;
    }
}
