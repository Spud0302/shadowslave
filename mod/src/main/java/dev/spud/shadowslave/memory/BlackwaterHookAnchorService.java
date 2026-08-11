package dev.spud.shadowslave.memory;

import dev.spud.shadowslave.attachment.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

/** Server-side access boundary for the Blackwater Hook's Java-owned anchor payload. */
public final class BlackwaterHookAnchorService {
    private BlackwaterHookAnchorService() {}

    public static BlackwaterHookAnchorData get(ServerPlayer player) {
        return Objects.requireNonNull(player, "player").getData(ModAttachments.BLACKWATER_HOOK_ANCHOR);
    }

    public static void anchor(ServerPlayer player, String dimension, BlockPos pos) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(pos, "pos");
        player.setData(ModAttachments.BLACKWATER_HOOK_ANCHOR, get(player).anchored(dimension, pos.asLong()));
    }

    public static void clear(ServerPlayer player) {
        Objects.requireNonNull(player, "player").setData(ModAttachments.BLACKWATER_HOOK_ANCHOR, BlackwaterHookAnchorData.empty());
    }
}
