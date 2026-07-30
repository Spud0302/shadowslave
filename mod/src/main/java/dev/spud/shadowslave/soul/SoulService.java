package dev.spud.shadowslave.soul;

import dev.spud.shadowslave.attachment.ModAttachments;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

/**
 * The only application-facing entry point for mutating a player's Soul.
 * Commands, migration and Nightmare services must call this class instead of
 * writing attachments directly.
 */
public final class SoulService {
    private SoulService() {
    }

    public static SoulData get(ServerPlayer player) {
        return Objects.requireNonNull(player, "player").getData(ModAttachments.SOUL);
    }

    public static SoulData infect(ServerPlayer player) {
        return replace(player, SoulTransitions.infect(get(player)));
    }

    public static SoulData completeFirstNightmare(
            ServerPlayer player,
            ResourceLocation aspectId,
            ResourceLocation flawId
    ) {
        return replace(player, SoulTransitions.completeFirstNightmare(get(player), aspectId, flawId));
    }

    public static SoulData reset(ServerPlayer player) {
        return replace(player, SoulTransitions.reset());
    }

    public static SoulData replace(ServerPlayer player, SoulData next) {
        Objects.requireNonNull(player, "player").setData(ModAttachments.SOUL, Objects.requireNonNull(next, "next"));
        return next;
    }
}
