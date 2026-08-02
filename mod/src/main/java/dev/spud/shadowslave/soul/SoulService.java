package dev.spud.shadowslave.soul;

import dev.spud.shadowslave.attachment.ModAttachments;
import dev.spud.shadowslave.network.SoulSyncService;
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

    public static SoulData beginFirstNightmare(ServerPlayer player) {
        return replace(player, SoulTransitions.beginFirstNightmare(get(player)));
    }

    public static SoulData completeFirstNightmare(
            ServerPlayer player,
            ResourceLocation aspectId,
            SoulRank aspectRank,
            ResourceLocation flawId
    ) {
        return replace(
                player,
                SoulTransitions.completeFirstNightmare(get(player), aspectId, aspectRank, flawId)
        );
    }

    public static SoulData reset(ServerPlayer player) {
        return replace(player, SoulTransitions.reset());
    }

    /**
     * Resets only the Soul attachment. The caller must complete every related
     * attachment mutation and send one final authoritative snapshot.
     */
    public static SoulData resetWithoutSync(ServerPlayer player) {
        return replaceWithoutSync(player, SoulTransitions.reset());
    }

    public static SoulData replace(ServerPlayer player, SoulData next) {
        ServerPlayer checkedPlayer = Objects.requireNonNull(player, "player");
        SoulData replaced = replaceWithoutSync(checkedPlayer, next);
        SoulSyncService.sync(checkedPlayer, replaced, false);
        return replaced;
    }

    private static SoulData replaceWithoutSync(ServerPlayer player, SoulData next) {
        ServerPlayer checkedPlayer = Objects.requireNonNull(player, "player");
        SoulData checkedNext = Objects.requireNonNull(next, "next");
        checkedPlayer.setData(ModAttachments.SOUL, checkedNext);
        return checkedNext;
    }
}
