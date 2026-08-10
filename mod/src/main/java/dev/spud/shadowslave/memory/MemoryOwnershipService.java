package dev.spud.shadowslave.memory;

import dev.spud.shadowslave.attachment.ModAttachments;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

/** Server-side access boundary for canonical Memory ownership. */
public final class MemoryOwnershipService {
    private MemoryOwnershipService() {
    }

    public static MemoryOwnershipData get(ServerPlayer player) {
        return Objects.requireNonNull(player, "player").getData(ModAttachments.MEMORIES);
    }

    public static boolean owns(ServerPlayer player, ResourceLocation memoryId) {
        return get(player).owns(memoryId);
    }

    public static MemoryOwnershipData award(ServerPlayer player, MemoryInstanceData memory) {
        Objects.requireNonNull(player, "player");
        MemoryOwnershipData before = get(player);
        MemoryOwnershipData after = before.award(memory);
        if (after != before) {
            player.setData(ModAttachments.MEMORIES, after);
        }
        return after;
    }

    public static void replace(ServerPlayer player, MemoryOwnershipData data) {
        Objects.requireNonNull(player, "player").setData(ModAttachments.MEMORIES, Objects.requireNonNull(data, "data"));
    }
}
