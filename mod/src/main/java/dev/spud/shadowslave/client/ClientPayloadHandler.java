package dev.spud.shadowslave.client;

import dev.spud.shadowslave.network.payload.SoulSnapshotPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client-only application of read-only Soul snapshots. */
public final class ClientPayloadHandler {
    private ClientPayloadHandler() {
    }

    public static void handleSoulSnapshot(SoulSnapshotPayload payload, IPayloadContext context) {
        ClientSoulState.update(payload.snapshot());
        if (payload.openScreen()) {
            Minecraft.getInstance().setScreen(new SoulScreen(payload.snapshot()));
        }
    }
}
