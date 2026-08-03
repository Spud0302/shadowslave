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
     *
     * @apiNote This method is public only for {@code PreviewResetService}, which
     * lives in a different package. Ordinary Soul mutations must use
     * {@link #reset(ServerPlayer)} or another auto-synchronizing method in this
     * service. Calling this method anywhere else can leave the client stale.
     */
    public static SoulData resetWithoutSync(ServerPlayer player) {
        return replaceWithoutSync(player, SoulTransitions.reset());
    }

    public static SoulData replace(ServerPlayer player, SoulData next) {
        return replace(new ServerOperations(Objects.requireNonNull(player, "player")), next);
    }

    static SoulData replace(Operations operations, SoulData next) {
        Operations checkedOperations = Objects.requireNonNull(operations, "operations");
        SoulData checkedNext = Objects.requireNonNull(next, "next");
        checkedOperations.write(checkedNext);
        checkedOperations.sync(checkedNext);
        return checkedNext;
    }

    private static SoulData replaceWithoutSync(ServerPlayer player, SoulData next) {
        ServerPlayer checkedPlayer = Objects.requireNonNull(player, "player");
        SoulData checkedNext = Objects.requireNonNull(next, "next");
        checkedPlayer.setData(ModAttachments.SOUL, checkedNext);
        return checkedNext;
    }

    interface Operations {
        void write(SoulData next);

        void sync(SoulData next);
    }

    private record ServerOperations(ServerPlayer player) implements Operations {
        @Override
        public void write(SoulData next) {
            player.setData(ModAttachments.SOUL, next);
        }

        @Override
        public void sync(SoulData next) {
            SoulSyncService.sync(player, next, false);
        }
    }
}
