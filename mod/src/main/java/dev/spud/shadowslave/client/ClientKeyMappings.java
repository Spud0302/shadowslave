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
    public static final KeyMapping USE_SECOND_ABILITY = key("key.shadowslave.use_second_ability", GLFW.GLFW_KEY_V);
    /** The Master's ability (Ascended). */
    public static final KeyMapping USE_THIRD_ABILITY = key("key.shadowslave.use_third_ability", GLFW.GLFW_KEY_C);
    /** The Saint's Transformation (Transcendent). */
    public static final KeyMapping USE_FOURTH_ABILITY = key("key.shadowslave.use_fourth_ability", GLFW.GLFW_KEY_X);
    public static final KeyMapping CHANNEL_ESSENCE = key("key.shadowslave.channel_essence", GLFW.GLFW_KEY_Z);
    public static final KeyMapping TOGGLE_MEMORY = key("key.shadowslave.toggle_memory", GLFW.GLFW_KEY_G);
    public static final KeyMapping TOGGLE_ECHO = key("key.shadowslave.toggle_echo", GLFW.GLFW_KEY_H);
    public static final KeyMapping TOGGLE_ECHO_MODE = key("key.shadowslave.toggle_echo_mode", GLFW.GLFW_KEY_J);
    public static final KeyMapping OPEN_DEV_MENU = key("key.shadowslave.open_dev_menu", GLFW.GLFW_KEY_F7);

    private ClientKeyMappings() {
    }

    /** The key that fires the ability in a HUD slot, or null if that slot has no key. */
    public static KeyMapping abilityKey(int slot) {
        return switch (slot) {
            case 0 -> USE_ABILITY;
            case 1 -> USE_SECOND_ABILITY;
            case 2 -> USE_THIRD_ABILITY;
            case 3 -> USE_FOURTH_ABILITY;
            default -> null;
        };
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
        event.register(USE_SECOND_ABILITY);
        event.register(USE_THIRD_ABILITY);
        event.register(USE_FOURTH_ABILITY);
        event.register(CHANNEL_ESSENCE);
        event.register(TOGGLE_MEMORY);
        event.register(TOGGLE_ECHO);
        event.register(TOGGLE_ECHO_MODE);
        event.register(OPEN_DEV_MENU);
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
