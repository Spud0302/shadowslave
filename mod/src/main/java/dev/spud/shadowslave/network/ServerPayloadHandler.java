package dev.spud.shadowslave.network;

import dev.spud.shadowslave.echo.EchoInstanceData;
import dev.spud.shadowslave.echo.EchoManifestationService;
import dev.spud.shadowslave.echo.EchoOwnershipService;
import dev.spud.shadowslave.echo.content.EchoContentCatalog;
import dev.spud.shadowslave.memory.MemoryManifestationService;
import dev.spud.shadowslave.network.payload.ActivateKindleRequestPayload;
import dev.spud.shadowslave.network.payload.OpenSoulScreenRequestPayload;
import dev.spud.shadowslave.network.payload.ToggleAshBurrowerEchoModeRequestPayload;
import dev.spud.shadowslave.network.payload.ToggleAshBurrowerEchoRequestPayload;
import dev.spud.shadowslave.network.payload.ToggleAshCompassRequestPayload;
import dev.spud.shadowslave.preview.PreviewPowerService;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Handles client intent without trusting client-provided character state. */
public final class ServerPayloadHandler {
    private ServerPayloadHandler() {
    }

    public static void handleOpenSoulScreen(OpenSoulScreenRequestPayload payload, IPayloadContext context) {
        ServerPlayer player = serverPlayer(context, "Soul screen");
        if (player != null) {
            SoulSyncService.openScreen(player);
        }
    }

    public static void handleActivateKindle(ActivateKindleRequestPayload payload, IPayloadContext context) {
        ServerPlayer player = serverPlayer(context, "Kindle");
        if (player != null) {
            PreviewPowerService.activateKindle(player);
        }
    }

    public static void handleToggleAshCompass(ToggleAshCompassRequestPayload payload, IPayloadContext context) {
        ServerPlayer player = serverPlayer(context, "Memory toggle");
        if (player == null) {
            return;
        }

        MemoryManifestationService.ManifestResult result = MemoryManifestationService.summonAshCompass(player);
        if (result == MemoryManifestationService.ManifestResult.ALREADY_SUMMONED) {
            result = MemoryManifestationService.dismissAshCompass(player);
        }
        sendMemoryResult(player, result);
    }

    public static void handleToggleAshBurrowerEcho(
            ToggleAshBurrowerEchoRequestPayload payload,
            IPayloadContext context
    ) {
        ServerPlayer player = serverPlayer(context, "Echo toggle");
        if (player == null) {
            return;
        }

        EchoManifestationService.ManifestResult result = EchoManifestationService.summonAshBurrower(player);
        if (result == EchoManifestationService.ManifestResult.ALREADY_SUMMONED) {
            result = EchoManifestationService.dismissAshBurrower(player);
        }
        sendEchoManifestResult(player, result);
    }

    public static void handleToggleAshBurrowerEchoMode(
            ToggleAshBurrowerEchoModeRequestPayload payload,
            IPayloadContext context
    ) {
        ServerPlayer player = serverPlayer(context, "Echo command toggle");
        if (player == null) {
            return;
        }

        EchoInstanceData state = EchoOwnershipService.get(player)
                .find(EchoManifestationService.ASH_BURROWER_ID)
                .orElse(null);
        if (state == null) {
            player.sendSystemMessage(Component.literal("Your soul does not contain [Ash Burrower].")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        EchoContentCatalog.CommandMode nextMode = state.commandMode() == EchoContentCatalog.CommandMode.FOLLOW
                ? EchoContentCatalog.CommandMode.HOLD
                : EchoContentCatalog.CommandMode.FOLLOW;
        EchoManifestationService.CommandResult result = EchoManifestationService.commandAshBurrower(player, nextMode);
        if (result == EchoManifestationService.CommandResult.COMMAND_SET) {
            player.sendSystemMessage(Component.literal("[Ash Burrower] — "
                            + (nextMode == EchoContentCatalog.CommandMode.FOLLOW ? "Follow." : "Hold here."))
                    .withStyle(ChatFormatting.AQUA));
        } else if (result == EchoManifestationService.CommandResult.NOT_OWNED) {
            player.sendSystemMessage(Component.literal("Your soul does not contain [Ash Burrower].")
                    .withStyle(ChatFormatting.RED));
        } else {
            player.sendSystemMessage(Component.literal("[Ash Burrower] cannot execute that command yet.")
                    .withStyle(ChatFormatting.RED));
        }
    }

    private static ServerPlayer serverPlayer(IPayloadContext context, String action) {
        if (context.player() instanceof ServerPlayer player) {
            return player;
        }
        context.disconnect(Component.literal(action + " request reached a non-server player context"));
        return null;
    }

    private static void sendMemoryResult(ServerPlayer player, MemoryManifestationService.ManifestResult result) {
        switch (result) {
            case SUMMONED -> player.sendSystemMessage(Component.literal("[Ash Compass] manifests.")
                    .withStyle(ChatFormatting.AQUA));
            case DISMISSED -> player.sendSystemMessage(Component.literal("[Ash Compass] dissolves back into your soul.")
                    .withStyle(ChatFormatting.DARK_AQUA));
            case INVENTORY_FULL -> player.sendSystemMessage(Component.literal(
                    "[Ash Compass] cannot manifest while your inventory is full.").withStyle(ChatFormatting.RED));
            case NOT_OWNED -> player.sendSystemMessage(Component.literal("Your soul does not contain [Ash Compass].")
                    .withStyle(ChatFormatting.RED));
            case NOT_SUMMONED -> player.sendSystemMessage(Component.literal("[Ash Compass] is not currently manifested.")
                    .withStyle(ChatFormatting.GRAY));
            case ALREADY_SUMMONED -> throw new IllegalStateException("Memory toggle did not resolve manifested state");
        }
    }

    private static void sendEchoManifestResult(ServerPlayer player, EchoManifestationService.ManifestResult result) {
        switch (result) {
            case SUMMONED -> player.sendSystemMessage(Component.literal("[Ash Burrower] takes shape beside you.")
                    .withStyle(ChatFormatting.AQUA));
            case DISMISSED -> player.sendSystemMessage(Component.literal(
                    "[Ash Burrower] dissolves out of the physical world.").withStyle(ChatFormatting.DARK_AQUA));
            case NOT_SUMMONED -> player.sendSystemMessage(Component.literal("[Ash Burrower] is not currently manifested.")
                    .withStyle(ChatFormatting.GRAY));
            case NOT_OWNED -> player.sendSystemMessage(Component.literal("Your soul does not contain [Ash Burrower].")
                    .withStyle(ChatFormatting.RED));
            case SPAWN_FAILED -> player.sendSystemMessage(Component.literal("[Ash Burrower] could not take physical form here.")
                    .withStyle(ChatFormatting.RED));
            case ALREADY_SUMMONED -> throw new IllegalStateException("Echo toggle did not resolve manifested state");
        }
    }
}
