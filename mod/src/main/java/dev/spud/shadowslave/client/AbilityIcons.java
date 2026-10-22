package dev.spud.shadowslave.client;

import dev.spud.shadowslave.ShadowSlaveMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Map;

/** The 16x16 icons for each kit ability and Flaw, keyed by ability id or Flaw effect id. */
final class AbilityIcons {
    private static final Map<String, ResourceLocation> ICONS = Map.of(
            "shadowslave:generation/ability/kindle", texture("ability/kindle"),
            "shadowslave:generation/ability/mist_passage", texture("ability/mist_passage"),
            "shadowslave:generation/ability/holdfast", texture("ability/holdfast"),
            "shadowslave:generation/ability/returning_tide", texture("ability/returning_tide"),
            "shadowslave:generation/flaw_effect/cold_ash", texture("flaw/cold_ash"),
            "shadowslave:generation/flaw_effect/borrowed_breath", texture("flaw/borrowed_breath"),
            "shadowslave:generation/flaw_effect/stone_sleep", texture("flaw/stone_sleep"),
            "shadowslave:generation/flaw_effect/tidal_debt", texture("flaw/tidal_debt")
    );
    private AbilityIcons() {
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
