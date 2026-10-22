package dev.spud.shadowslave.client;

import net.minecraft.client.gui.GuiGraphics;

/** Shared colours and frames for the Spell look: pale text on a dark, translucent veil with thin violet rules. */
public final class SpellStyle {
    public static final int VEIL_TOP = 0xB0040208;
    public static final int VEIL_BOTTOM = 0xD80E0618;
    public static final int PANEL = 0xC0120F1C;
    public static final int RULE = 0x806E4A8E;
    public static final int BORDER = 0xFF6E4A8E;
    public static final int VOICE = 0xFFB9A6D6;
    public static final int TITLE = 0xFFD9B3FF;
    public static final int TEXT = 0xFFF2EDF7;
    public static final int MUTED = 0xFF9A93A6;
    public static final int HINT = 0xFF6F6879;
    public static final int ASPECT = 0xFF79DDF2;
    public static final int ABILITY = 0xFFD9B3FF;
    public static final int FLAW = 0xFFF08080;

    private SpellStyle() {
    }

    public static void veil(GuiGraphics graphics, int width, int height) {
        graphics.fillGradient(0, 0, width, height, VEIL_TOP, VEIL_BOTTOM);
    }

    /** A translucent panel with a one-pixel border. */
    public static void panel(GuiGraphics graphics, int left, int top, int right, int bottom, int border) {
        graphics.fill(left, top, right, bottom, PANEL);
        graphics.renderOutline(left, top, right - left, bottom - top, border);
    }

    /** Scales a colour's alpha channel, keeping its hue. */
    public static int withAlpha(int colour, float alpha) {
        int a = Math.round(((colour >>> 24) & 0xFF) * Math.max(0.0F, Math.min(1.0F, alpha)));
        return (a << 24) | (colour & 0x00FFFFFF);
    }
}
