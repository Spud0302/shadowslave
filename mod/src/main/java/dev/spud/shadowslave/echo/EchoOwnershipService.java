package dev.spud.shadowslave.echo;

import dev.spud.shadowslave.attachment.ModAttachments;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Server-side access boundary for canonical Echo ownership and manifestation identity. */
public final class EchoOwnershipService {
    private EchoOwnershipService() {
    }

    public static EchoOwnershipData get(ServerPlayer player) {
        return Objects.requireNonNull(player, "player").getData(ModAttachments.ECHOES);
    }

    public static boolean owns(ServerPlayer player, ResourceLocation echoId) {
        return get(player).owns(echoId);
    }

    public static EchoOwnershipData award(ServerPlayer player, EchoInstanceData echo) {
        Objects.requireNonNull(player, "player");
        EchoOwnershipData before = get(player);
        EchoOwnershipData after = before.award(echo);
        if (after != before) {
            player.setData(ModAttachments.ECHOES, after);
        }
        return after;
    }

    public static EchoOwnershipData setManifestation(ServerPlayer player, ResourceLocation echoId, Optional<UUID> entityUuid) {
        Objects.requireNonNull(player, "player");
        EchoOwnershipData after = get(player).withManifestation(echoId, entityUuid);
        player.setData(ModAttachments.ECHOES, after);
        return after;
    }

    public static void replace(ServerPlayer player, EchoOwnershipData data) {
        Objects.requireNonNull(player, "player").setData(ModAttachments.ECHOES, Objects.requireNonNull(data, "data"));
    }
}
