package dev.spud.shadowslave.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

/**
 * A glimpse of the Spell's inner workings (after the manhwa's look, "the
 * celestial equivalent of a neural network"): a web of twinkling stars joined
 * by faint threads, soft teal and ember nebulae, and runes drifting through it.
 * Drawn into any rectangle, so the appraisal fills the screen with it and a
 * Calling shows a band of it.
 */
public final class SpellWeb {
    private static final ResourceLocation RUNE_FONT = ResourceLocation.withDefaultNamespace("alt");
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
    static final int RUNE_COLOR = 0xFFB8E4FF;
    static final int STAR_COLOR = 0xFFE8F2FF;
    static final int WEB_COLOR = 0x30A8C8FF;

    private record Star(float x, float y, float size, float twinkle) {
    }

    private record Rune(float x, float y, String glyph, float phase, float drift, float scale) {
    }

    private final List<Star> stars = new ArrayList<>();
    private final List<Rune> runes = new ArrayList<>();
    private final int linkDistance;
    private final boolean band;

    public SpellWeb(long seed, int starCount, int runeCount, int linkDistance) {
        this(seed, starCount, runeCount, linkDistance, false);
    }

    /**
     * @param band a narrow strip behind words: no nebulae (they read as boxes
     *             when squashed) and runes only at the ends, clear of the text
     */
    public SpellWeb(long seed, int starCount, int runeCount, int linkDistance, boolean band) {
        this.linkDistance = linkDistance;
        this.band = band;
        RandomSource sky = RandomSource.create(seed);
        for (int i = 0; i < starCount; i++) {
            stars.add(new Star(sky.nextFloat(), sky.nextFloat(), sky.nextFloat() < 0.15F ? 2.0F : 1.0F, sky.nextFloat() * 6.28F));
        }
        for (int i = 0; i < runeCount; i++) {
            float rx = sky.nextFloat();
            if (band) {
                rx = rx < 0.5F ? 0.02F + rx * 0.2F : 0.88F + (rx - 0.5F) * 0.2F;
            }
            runes.add(new Rune(rx, sky.nextFloat(), String.valueOf(ALPHABET.charAt(sky.nextInt(ALPHABET.length()))),
                    sky.nextFloat() * 6.28F, 0.3F + sky.nextFloat() * 0.7F, 1.5F + sky.nextFloat() * 1.5F));
        }
    }

    /**
     * Draws the web into {@code [x, y, x + w, y + h]}.
     *
     * @param time  seconds since it opened, for the twinkle and drift
     * @param alpha how visible it is, for fading in and out
     */
    public void render(GuiGraphics graphics, Font font, int x, int y, int w, int h, float time, float alpha, float runeScale) {
        graphics.fill(x, y, x + w, y + h, SpellStyle.withAlpha(0xFF02030A, alpha));
        if (band) {
            // A soft teal-to-ember glow along the strip instead of nebulae.
            graphics.fillGradient(x, y, x + w, y + h, SpellStyle.withAlpha(0x302A6E8C, alpha), SpellStyle.withAlpha(0x208C5A2A, alpha));
        } else {
            int small = Math.min(w, h);
            nebula(graphics, x + w * 0.32F, y + h * 0.42F, small * 0.55F, SpellStyle.withAlpha(0x0E2A6E8C, alpha));
            nebula(graphics, x + w * 0.70F, y + h * 0.36F, small * 0.38F, SpellStyle.withAlpha(0x0C8C5A2A, alpha));
            nebula(graphics, x + w * 0.55F, y + h * 0.75F, small * 0.30F, SpellStyle.withAlpha(0x0A2A3E8C, alpha));
        }

        int web = SpellStyle.withAlpha(WEB_COLOR, alpha);
        for (int i = 0; i < stars.size(); i++) {
            Star a = stars.get(i);
            for (int j = i + 1; j < stars.size(); j++) {
                Star b = stars.get(j);
                float dx = (a.x - b.x) * w;
                float dy = (a.y - b.y) * h;
                if (dx * dx + dy * dy < linkDistance * linkDistance) {
                    line(graphics, x + a.x * w, y + a.y * h, x + b.x * w, y + b.y * h, web);
                }
            }
        }
        for (Star star : stars) {
            float glow = (0.55F + 0.45F * Mth.sin(time * 1.7F + star.twinkle)) * alpha;
            int sx = x + (int) (star.x * w);
            int sy = y + (int) (star.y * h);
            int size = (int) star.size;
            graphics.fill(sx, sy, sx + size, sy + size, SpellStyle.withAlpha(STAR_COLOR, glow));
            if (size > 1) {
                graphics.fill(sx - 1, sy, sx + size + 1, sy + size, SpellStyle.withAlpha(STAR_COLOR, glow * 0.25F));
            }
        }
        for (Rune rune : runes) {
            float rise = (rune.y * h - time * 6.0F * rune.drift * (band ? 0.4F : 1.0F)) % h;
            if (rise < 0) {
                rise += h;
            }
            float pulse = (0.35F + 0.65F * (0.5F + 0.5F * Mth.sin(time * 0.9F + rune.phase))) * alpha;
            float scale = rune.scale * runeScale;
            Component glyph = Component.literal(rune.glyph).withStyle(style -> style.withFont(RUNE_FONT));
            graphics.pose().pushPose();
            graphics.pose().translate(x + rune.x * w, y + rise, 0.0F);
            graphics.pose().scale(scale, scale, 1.0F);
            graphics.drawString(font, glyph, -1, -1, SpellStyle.withAlpha(RUNE_COLOR, pulse * 0.25F), false);
            graphics.drawString(font, glyph, 0, 0, SpellStyle.withAlpha(RUNE_COLOR, pulse * 0.8F), false);
            graphics.pose().popPose();
        }
    }

    private static void nebula(GuiGraphics graphics, float cx, float cy, float radius, int color) {
        for (int ring = 6; ring >= 1; ring--) {
            float r = radius * ring / 6.0F;
            graphics.fill((int) (cx - r), (int) (cy - r * 0.6F), (int) (cx + r), (int) (cy + r * 0.6F), color);
        }
    }

    /** A one-pixel line, drawn as a run of dots. */
    static void line(GuiGraphics graphics, float x0, float y0, float x1, float y1, int color) {
        int steps = (int) Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
        for (int i = 0; i <= steps; i += 2) {
            float t = steps == 0 ? 0 : (float) i / steps;
            int x = (int) (x0 + (x1 - x0) * t);
            int y = (int) (y0 + (y1 - y0) * t);
            graphics.fill(x, y, x + 1, y + 1, color);
        }
    }
}
