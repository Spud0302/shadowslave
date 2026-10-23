package dev.spud.shadowslave.client;

import dev.spud.shadowslave.ShadowSlaveMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The 16x16 icons for each kit ability and Flaw, keyed by ability id or Flaw effect id. */
final class AbilityIcons {
    private static final Map<String, ResourceLocation> ICONS = icons(
            List.of("kindle", "mist_passage", "holdfast", "returning_tide",
                    "carry_the_flame", "ember_ward", "through_the_haze", "veil",
                    "root_in_stone", "quake", "low_tide", "undertow",
                    "mark_prey", "foresight", "soul_siphon", "essence_gift",
                    "snare_sigil", "relentless_hunt", "fate_thread", "shared_sight",
                    "harvest", "reaping_arc", "soul_ward", "overflow",
                    "quench", "tunnel_strike", "raise_wall", "bloom",
                    "masterwork", "molten_edge", "vein_pull", "stonewalk",
                    "blueprint", "scaffold", "living_wall", "wild_call",
                    "waymark", "lullaby", "anchor_recall", "trailblaze", "dreamfall", "sleepwalker"),
            List.of("cold_ash", "borrowed_breath", "stone_sleep", "tidal_debt",
                    "quarry_lock", "clouded_eyes", "hunger", "brimming_vessel",
                    "cold_hearth", "open_sky", "far_from_home", "rootbound", "wanderlust", "insomnia"));

    private AbilityIcons() {
    }

    private static Map<String, ResourceLocation> icons(List<String> abilities, List<String> flaws) {
        Map<String, ResourceLocation> icons = new HashMap<>();
        abilities.forEach(name -> icons.put("shadowslave:generation/ability/" + name, texture("ability/" + name)));
        flaws.forEach(name -> icons.put("shadowslave:generation/flaw_effect/" + name, texture("flaw/" + name)));
        return Map.copyOf(icons);
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "textures/gui/" + name + ".png");
    }

    static boolean has(String id) {
        return ICONS.containsKey(id);
    }

    /** Draws the icon for an ability or Flaw effect id at 16x16. */
    static void draw(GuiGraphics graphics, String id, int x, int y) {
        ResourceLocation icon = ICONS.get(id);
        if (icon != null) {
            graphics.blit(icon, x, y, 0.0F, 0.0F, 16, 16, 16, 16);
        } else {
            // Abilities from older or imported identities have no drawn icon yet.
            graphics.renderItem(new ItemStack(Items.AMETHYST_SHARD), x, y);
        }
    }
}
