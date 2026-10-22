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

import java.util.ArrayList;
import java.util.List;

/** Physical-client-only registration of normal Shadow Slave player controls. */
@EventBusSubscriber(modid = ShadowSlaveMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ClientKeyMappings {
    private static final String CATEGORY = "key.categories.shadowslave";

    public static final KeyMapping OPEN_SOUL = key("key.shadowslave.open_soul", GLFW.GLFW_KEY_O);
    public static final KeyMapping USE_ABILITY = key("key.shadowslave.use_ability", GLFW.GLFW_KEY_R);
    public static final KeyMapping TOGGLE_MEMORY = key("key.shadowslave.toggle_memory", GLFW.GLFW_KEY_G);
    public static final KeyMapping TOGGLE_ECHO = key("key.shadowslave.toggle_echo", GLFW.GLFW_KEY_H);
    public static final KeyMapping TOGGLE_ECHO_MODE = key("key.shadowslave.toggle_echo_mode", GLFW.GLFW_KEY_J);

    private ClientKeyMappings() {
    }

    /** The key that fires the ability in a HUD slot, or null if that slot has no key. */
    public static KeyMapping abilityKey(int slot) {
        return slot == 0 ? USE_ABILITY : null;
    }

    /** The names of the ability keys, in slot order, as the player has bound them. */
    public static List<String> abilityKeyNames() {
        List<String> names = new ArrayList<>();
        for (int slot = 0; abilityKey(slot) != null; slot++) {
            names.add(abilityKey(slot).getTranslatedKeyMessage().getString());
        }
        return names;
    }

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_SOUL);
        event.register(USE_ABILITY);
        event.register(TOGGLE_MEMORY);
        event.register(TOGGLE_ECHO);
        event.register(TOGGLE_ECHO_MODE);
    }

    private static KeyMapping key(String translationKey, int keyCode) {
        return new KeyMapping(
                translationKey,
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                keyCode,
                CATEGORY
        );
    }
}
