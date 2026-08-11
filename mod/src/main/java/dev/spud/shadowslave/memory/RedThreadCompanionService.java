package dev.spud.shadowslave.memory;

import dev.spud.shadowslave.attachment.ModAttachments;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;
import java.util.UUID;

/** Java authority seam for Red Thread Bracelet companion targeting. */
public final class RedThreadCompanionService {
    private RedThreadCompanionService() {}

    public static RedThreadCompanionData get(ServerPlayer player) {
        return Objects.requireNonNull(player, "player").getData(ModAttachments.RED_THREAD_COMPANION.get());
    }

    public static void mark(ServerPlayer player, UUID companionUuid) {
        Objects.requireNonNull(player, "player").setData(
                ModAttachments.RED_THREAD_COMPANION.get(),
                RedThreadCompanionData.marked(Objects.requireNonNull(companionUuid, "companionUuid"))
        );
    }

    public static void clear(ServerPlayer player) {
        Objects.requireNonNull(player, "player").setData(ModAttachments.RED_THREAD_COMPANION.get(), RedThreadCompanionData.empty());
    }
}
