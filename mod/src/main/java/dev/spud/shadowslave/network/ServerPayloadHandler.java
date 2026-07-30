package dev.spud.shadowslave.network;

import dev.spud.shadowslave.network.payload.OpenSoulScreenRequestPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Handles client intent without trusting client-provided character state. */
public final class ServerPayloadHandler {
    private ServerPayloadHandler() {
    }

    public static void handleOpenSoulScreen(
            OpenSoulScreenRequestPayload payload,
            IPayloadContext context
    ) {
        if (context.player() instanceof ServerPlayer player) {
            SoulSyncService.openScreen(player);
            return;
        }

        context.disconnect(Component.literal("Soul screen request reached a non-server player context"));
    }
}
