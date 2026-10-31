package dev.spud.shadowslave.client;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.devtools.DevTools;
import dev.spud.shadowslave.network.payload.ChannelRequestPayload;
import dev.spud.shadowslave.network.payload.EchoActionPayload;
import dev.spud.shadowslave.network.payload.OpenSoulScreenRequestPayload;
import dev.spud.shadowslave.network.payload.ToggleAshBurrowerEchoModeRequestPayload;
import dev.spud.shadowslave.network.payload.ToggleAshBurrowerEchoRequestPayload;
import dev.spud.shadowslave.network.payload.MemoryKeyPayload;
import dev.spud.shadowslave.network.payload.UseAbilityRequestPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
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
        consume(ClientKeyMappings.USE_ABILITY, new UseAbilityRequestPayload(0), minecraft);
        consume(ClientKeyMappings.USE_SECOND_ABILITY, new UseAbilityRequestPayload(1), minecraft);
        consume(ClientKeyMappings.USE_THIRD_ABILITY, new UseAbilityRequestPayload(2), minecraft);
        consume(ClientKeyMappings.USE_FOURTH_ABILITY, new UseAbilityRequestPayload(3), minecraft);
        // Sneak while channeling to gather the essence into the next blow instead of leaping.
        consume(ClientKeyMappings.CHANNEL_ESSENCE,
                new ChannelRequestPayload(minecraft.player != null && minecraft.player.isShiftKeyDown()), minecraft);
        // Sneak with the Memory key to turn which dismissed Memory answers next.
        consume(ClientKeyMappings.TOGGLE_MEMORY,
                new MemoryKeyPayload(minecraft.player != null && minecraft.player.isShiftKeyDown()), minecraft);
        // Echoes bound at the Weaving Table answer the Echo keys; a soul with none keeps the older Ash Burrower Echo on them.
        boolean echoes = !ClientEchoState.status().held().isEmpty();
        consume(ClientKeyMappings.TOGGLE_ECHO, echoes ? EchoActionPayload.of(EchoActionPayload.CALL)
                : ToggleAshBurrowerEchoRequestPayload.INSTANCE, minecraft);
        if (echoes) {
            while (ClientKeyMappings.TOGGLE_ECHO_MODE.consumeClick()) {
                if (minecraft.player != null && minecraft.screen == null) {
                    minecraft.setScreen(new EchoWheelScreen());
                }
            }
        } else {
            consume(ClientKeyMappings.TOGGLE_ECHO_MODE, ToggleAshBurrowerEchoModeRequestPayload.INSTANCE, minecraft);
        }
        while (ClientKeyMappings.OPEN_DEV_MENU.consumeClick()) {
            if (minecraft.player != null && minecraft.screen == null) {
                if (minecraft.player.hasPermissions(DevTools.PERMISSION)) {
                    minecraft.setScreen(new DevMenuScreen());
                } else {
                    minecraft.player.displayClientMessage(Component.literal("The dev menu needs operator rights (or cheats on).")
                            .withStyle(ChatFormatting.GRAY), true);
                }
            }
        }
        if (minecraft.level != null && ClientAbilityState.consumeReadyCue(minecraft.level.getGameTime())) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_CHIME, 1.4F, 0.6F));
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientSoulState.clear();
        ClientTrueNameState.clear();
        ClientAbilityState.clear();
        ClientCoreState.clear();
        ClientWeavingState.clear();
        ClientEchoState.clear();
        ClientFieldGuideState.clear();
        ClientCallingsState.reset();
    }

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        SpellClientConfig.sync();
    }

    private static void consume(KeyMapping mapping, CustomPacketPayload payload, Minecraft minecraft) {
        while (mapping.consumeClick()) {
            if (minecraft.player != null && minecraft.screen == null) {
                PacketDistributor.sendToServer(payload);
            }
        }
    }
}
