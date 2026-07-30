package dev.spud.shadowslave.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.spud.shadowslave.ShadowSlaveMod;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

/** Physical-client-only registration of the Soul interface key. */
@EventBusSubscriber(modid = ShadowSlaveMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ClientKeyMappings {
    public static final KeyMapping OPEN_SOUL = new KeyMapping(
            "key.shadowslave.open_soul",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            "key.categories.shadowslave"
    );

    private ClientKeyMappings() {
    }

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_SOUL);
    }
}
