package dev.spud.shadowslave.client;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.network.payload.SpellCallingsPayload;
import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/**
 * A Calling (DESIGN, Spell Callings section): a band of the Spell's starry
 * thread-web opens near the top of the screen, the line types itself out in
 * the Spell's voice, lingers, and fades.
 */
public final class CallingLayer implements LayeredDraw.Layer {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "spell_calling");
    static final long MS_PER_CHAR = 28;
    static final long FADE_MS = 600;
    static final long HOLD_MS = 5000;
    private static final int MAX_WIDTH = 300;

    private SpellCallingsPayload.Entry webFor;
    private SpellWeb web;

    @Override
    public void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui || minecraft.player == null) {
            return;
        }
        ClientCallingsState.showing().ifPresent(entry -> {
            long elapsed = Util.getMillis() - ClientCallingsState.shownAt();
            if (!draw(graphics, minecraft.font, entry, elapsed, graphics.guiWidth())) {
                ClientCallingsState.clearShowing();
            }
        });
    }

    /** Draws a Calling {@code elapsed} ms after it came; false once it has faded away. */
    boolean draw(GuiGraphics graphics, Font font, SpellCallingsPayload.Entry entry, long elapsed, int screenWidth) {
        int width = Math.min(MAX_WIDTH, screenWidth - 40);
        List<FormattedCharSequence> wrapped = font.split(Component.literal(entry.text()), width - 24);
        long typing = entry.text().length() * MS_PER_CHAR;
        long total = FADE_MS + typing + HOLD_MS + FADE_MS;
        if (elapsed >= total) {
            return false;
        }
        float alpha = elapsed < FADE_MS ? elapsed / (float) FADE_MS
                : elapsed > total - FADE_MS ? (total - elapsed) / (float) FADE_MS : 1.0F;
        alpha = Math.max(0.0F, Math.min(1.0F, alpha));
        if (web == null || webFor != entry) {
            web = new SpellWeb(entry.text().hashCode(), 54, 4, 34, true);
            webFor = entry;
        }
        int height = 24 + wrapped.size() * 10;
        int left = (screenWidth - width) / 2;
        int top = 30;
        float time = elapsed / 1000.0F;
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 200.0F);
        web.render(graphics, font, left, top, width, height, time, alpha * 0.92F, 0.5F);
        graphics.fill(left, top, left + width, top + height, SpellStyle.withAlpha(0x60020308, alpha));
        graphics.fill(left, top, left + width, top + 1, SpellStyle.withAlpha(SpellStyle.BORDER, alpha));
        graphics.fill(left, top + height - 1, left + width, top + height, SpellStyle.withAlpha(SpellStyle.BORDER, alpha));
        String heading = entry.title().isEmpty() ? "The Spell calls" : "The Spell calls · " + entry.title();
        graphics.drawCenteredString(font, heading, screenWidth / 2, top + 5, SpellStyle.withAlpha(SpellStyle.MUTED, alpha));
        int shown = (int) Math.max(0, Math.min(entry.text().length(), (elapsed - FADE_MS / 2) / MS_PER_CHAR));
        int y = top + 17;
        int consumed = 0;
        for (FormattedCharSequence line : wrapped) {
            String text = plain(line);
            int visible = Math.max(0, Math.min(text.length(), shown - consumed));
            consumed += text.length() + 1;
            if (visible > 0) {
                int lineWidth = font.width(text);
                graphics.drawString(font, text.substring(0, visible), screenWidth / 2 - lineWidth / 2, y,
                        SpellStyle.withAlpha(SpellStyle.VOICE, Math.max(0.05F, alpha)), true);
            }
            y += 10;
        }
        graphics.pose().popPose();
        return true;
    }

    private static String plain(FormattedCharSequence sequence) {
        StringBuilder out = new StringBuilder();
        sequence.accept((index, style, codePoint) -> {
            out.appendCodePoint(codePoint);
            return true;
        });
        return out.toString();
    }
}
