package dev.spud.shadowslave.echo;

import dev.spud.shadowslave.attachment.ModAttachments;
import dev.spud.shadowslave.echo.content.EchoContentCatalog;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;
import java.util.UUID;

/** Server-side access boundary for canonical Echo ownership, command/cargo state and manifestation identity. */
public final class EchoOwnershipService {
    private EchoOwnershipService() {}

    public static EchoOwnershipData get(ServerPlayer player) { return Objects.requireNonNull(player, "player").getData(ModAttachments.ECHOES); }
    public static boolean owns(ServerPlayer player, ResourceLocation echoId) { return get(player).owns(echoId); }
    public static EchoOwnershipData award(ServerPlayer player, EchoInstanceData echo) { Objects.requireNonNull(player, "player"); EchoOwnershipData before = get(player); EchoOwnershipData after = before.award(echo); if (after != before) player.setData(ModAttachments.ECHOES, after); return after; }
    public static EchoOwnershipData setCommandMode(ServerPlayer player, ResourceLocation echoId, EchoContentCatalog.CommandMode commandMode) { Objects.requireNonNull(player, "player"); EchoOwnershipData before = get(player); EchoOwnershipData after = before.withCommandMode(echoId, commandMode); if (after != before) player.setData(ModAttachments.ECHOES, after); return after; }
    public static EchoOwnershipData setGuardPoint(ServerPlayer player, ResourceLocation echoId, ResourceLocation dimension, BlockPos position) { Objects.requireNonNull(player, "player"); EchoOwnershipData before = get(player); EchoOwnershipData after = before.withGuardPoint(echoId, dimension, position); if (after != before) player.setData(ModAttachments.ECHOES, after); return after; }
    public static EchoOwnershipData setCargo(ServerPlayer player, ResourceLocation echoId, ResourceLocation itemId, int count) { Objects.requireNonNull(player, "player"); EchoOwnershipData before = get(player); EchoOwnershipData after = before.withCargo(echoId, itemId, count); if (after != before) player.setData(ModAttachments.ECHOES, after); return after; }
    public static EchoOwnershipData clearCargo(ServerPlayer player, ResourceLocation echoId) { Objects.requireNonNull(player, "player"); EchoOwnershipData before = get(player); EchoOwnershipData after = before.withoutCargo(echoId); if (after != before) player.setData(ModAttachments.ECHOES, after); return after; }
    public static EchoOwnershipData setManifestation(ServerPlayer player, ResourceLocation echoId, UUID entityUuid, ResourceLocation dimension, BlockPos position) { Objects.requireNonNull(player, "player"); EchoOwnershipData before = get(player); EchoOwnershipData after = before.withManifestation(echoId, entityUuid, dimension, position); if (after != before) player.setData(ModAttachments.ECHOES, after); return after; }
    public static EchoOwnershipData clearManifestation(ServerPlayer player, ResourceLocation echoId) { Objects.requireNonNull(player, "player"); EchoOwnershipData before = get(player); EchoOwnershipData after = before.withoutManifestation(echoId); if (after != before) player.setData(ModAttachments.ECHOES, after); return after; }
    public static void replace(ServerPlayer player, EchoOwnershipData data) { Objects.requireNonNull(player, "player").setData(ModAttachments.ECHOES, Objects.requireNonNull(data, "data")); }
}
