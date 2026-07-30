package dev.spud.shadowslave.soul.identity;

import dev.spud.shadowslave.attachment.ModAttachments;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

/** Mutation boundary for persistent revealed Aspect and Flaw instance data. */
public final class SoulIdentityService {
    private SoulIdentityService() {
    }

    public static SoulIdentityData get(ServerPlayer player) {
        return Objects.requireNonNull(player, "player").getData(ModAttachments.IDENTITY);
    }

    public static SoulIdentityData replace(ServerPlayer player, SoulIdentityData next) {
        ServerPlayer checkedPlayer = Objects.requireNonNull(player, "player");
        SoulIdentityData checkedNext = Objects.requireNonNull(next, "next");
        checkedPlayer.setData(ModAttachments.IDENTITY, checkedNext);
        return checkedNext;
    }
}
