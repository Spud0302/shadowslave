package dev.spud.shadowslave.client;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.network.payload.ActivateKindleRequestPayload;
import dev.spud.shadowslave.network.payload.OpenSoulScreenRequestPayload;
import dev.spud.shadowslave.network.payload.ToggleAshBurrowerEchoModeRequestPayload;
import dev.spud.shadowslave.network.payload.ToggleAshBurrowerEchoRequestPayload;
import dev.spud.shadowslave.network.payload.ToggleAshCompassRequestPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Physical-client-only input handling. The server remains authoritative for every requested action. */
@EventBusSubscriber(modid = ShadowSlaveMod.MOD_ID, value = Dist.CLIENT)
public final class ClientGameEvents {
    private ClientGameEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        consume(ClientKeyMappings.OPEN_SOUL, OpenSoulScreenRequestPayload.INSTANCE, minecraft);
        consume(ClientKeyMappings.ACTIVATE_KINDLE, ActivateKindleRequestPayload.INSTANCE, minecraft);
        consume(ClientKeyMappings.TOGGLE_MEMORY, ToggleAshCompassRequestPayload.INSTANCE, minecraft);
        consume(ClientKeyMappings.TOGGLE_ECHO, ToggleAshBurrowerEchoRequestPayload.INSTANCE, minecraft);
        consume(ClientKeyMappings.TOGGLE_ECHO_MODE, ToggleAshBurrowerEchoModeRequestPayload.INSTANCE, minecraft);
    }

    private static void consume(KeyMapping mapping, CustomPacketPayload payload, Minecraft minecraft) {
        while (mapping.consumeClick()) {
            if (minecraft.player != null && minecraft.screen == null) {
                PacketDistributor.sendToServer(payload);
            }
        }
    }
}
