package dev.spud.shadowslave.client;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.fieldguide.FieldGuideRegistry;
import dev.spud.shadowslave.network.payload.FieldGuideStatusPayload;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Hold to peek (DESIGN, Jack 2026-10-23): with the Field Guide in the off
 * hand, a small north-up map of the explored ground around the player sits
 * in the top-left corner, with paths, pins, Gates and Seeds on it.
 */
public final class FieldGuideMinimapLayer implements LayeredDraw.Layer {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "field_guide_minimap");
    static final int SIZE = 80;
    static final int MARGIN = 6;
    static final float SCALE = 0.5F;

    @Override
    public void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (minecraft.options.hideGui || player == null || minecraft.level == null || minecraft.getDebugOverlay().showDebugScreen()
                || !player.getOffhandItem().is(FieldGuideRegistry.FIELD_GUIDE_ITEM.get())) {
            return;
        }
        String dimension = player.level().dimension().location().toString();
        int left = MARGIN;
        int top = MARGIN;
        graphics.fill(left - 3, top - 3, left + SIZE + 3, top + SIZE + 3, FieldGuideScreen.LEATHER_EDGE);
        graphics.fill(left - 2, top - 2, left + SIZE + 2, top + SIZE + 2, FieldGuideScreen.LEATHER);
        FieldGuideMapRenderer.View view = new FieldGuideMapRenderer.View(left, top, SIZE, SIZE, player.getX(), player.getZ(), SCALE);
        graphics.enableScissor(left, top, left + SIZE, top + SIZE);
        FieldGuideMapRenderer.drawTerrain(graphics, dimension, view);
        if (ClientFieldGuideState.shown(ClientFieldGuideState.Layer.PATHS)) {
            FieldGuideMapRenderer.drawPaths(graphics, dimension, view);
        }
        for (FieldGuideStatusPayload.Pin pin : ClientFieldGuideState.status().pins()) {
            boolean gateLayer = pin.kind().equals("gate") || pin.kind().equals("seed");
            if (pin.dimension().equals(dimension)
                    && ClientFieldGuideState.shown(gateLayer ? ClientFieldGuideState.Layer.GATES : ClientFieldGuideState.Layer.PINS)) {
                FieldGuideMapRenderer.drawPin(graphics, pin.kind(), view.screenX(pin.x()), view.screenY(pin.z()), pin.active());
            }
        }
        FieldGuideMapRenderer.drawPlayer(graphics, left + SIZE / 2, top + SIZE / 2, player.getYRot());
        graphics.disableScissor();
        graphics.drawString(minecraft.font, "N", left + SIZE / 2 - 2, top + 2, FieldGuideMapRenderer.INK, false);
        String coords = player.getBlockX() + ", " + player.getBlockZ();
        int textWidth = minecraft.font.width(coords);
        graphics.fill(left - 2, top + SIZE + 3, left + Math.max(SIZE + 2, textWidth + 6), top + SIZE + 14, 0xA0000000);
        graphics.drawString(minecraft.font, coords, left + 1, top + SIZE + 5, 0xFFEADFC2, false);
    }
}
