package dev.spud.shadowslave.client;

import dev.spud.shadowslave.network.payload.AspectRevealPayload;
import dev.spud.shadowslave.network.payload.OpenSoulScreenRequestPayload;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * The Spell names a freshly rolled Aspect: its words type themselves out over
 * a translucent veil, then the Soul screen opens. Click, Space or Enter skips
 * the typing, and again continues; Esc closes.
 */
public final class SpellRevealScreen extends Screen {
    private static final long MS_PER_CHAR = 18;
    private static final long AUTO_CONTINUE_MS = 6000;
    private static final int CHARS_PER_TICK_SOUND = 3;

    private static final int VEIL_TOP = 0xB0040208;
    private static final int VEIL_BOTTOM = 0xD80E0618;
    private static final int RULE_COLOR = 0x806E4A8E;
    private static final int VOICE_COLOR = 0xFFB9A6D6;
    private static final int NAME_COLOR = 0xFF79DDF2;
    private static final int LABEL_COLOR = 0xFFF2EDF7;
    private static final int DETAIL_COLOR = 0xFF9A93A6;
    private static final int ABILITY_COLOR = 0xFFD9B3FF;
    private static final int FLAW_COLOR = 0xFFF08080;
    private static final int HINT_COLOR = 0xFF6F6879;

    private enum Kind {
        VOICE(1.0F, 14),
        NAME(2.0F, 24),
        LABEL(1.0F, 12),
        DETAIL(1.0F, 11),
        GAP(1.0F, 8),
        SPACE(1.0F, 5);

        final float scale;
        final int height;

        Kind(float scale, int height) {
            this.scale = scale;
            this.height = height;
        }
    }

    private record Line(Kind kind, String text, int color, int pauseMs) {
    }

    private record Row(Kind kind, String text, int color) {
    }

    private final RandomSource random = RandomSource.create();
    private final List<Line> lines;
    private final List<Row> rows = new ArrayList<>();
    private RevealTypewriter typewriter;
    private long openedAt = -1;
    private long skipOffset;
    private int soundedChars;
    private boolean continued;

    public SpellRevealScreen(AspectRevealPayload reveal) {
        super(Component.translatable("screen.shadowslave.reveal"));
        this.lines = script(reveal);
    }

    private static List<Line> script(AspectRevealPayload reveal) {
        List<Line> lines = new ArrayList<>();
        lines.add(new Line(Kind.VOICE, "The Spell turns its gaze upon you.", VOICE_COLOR, 400));
        lines.add(new Line(Kind.VOICE, "Dreamer. Your Aspect has been woven.", VOICE_COLOR, 600));
        lines.add(new Line(Kind.GAP, "", 0, 0));
        lines.add(new Line(Kind.NAME, reveal.aspectName(), NAME_COLOR, 250));
        lines.add(new Line(Kind.DETAIL, reveal.rankName() + " " + reveal.natureName() + " Aspect", DETAIL_COLOR, 600));
        lines.add(new Line(Kind.GAP, "", 0, 0));
        lines.add(new Line(Kind.LABEL, "Innate: " + reveal.innateName(), LABEL_COLOR, 120));
        lines.add(new Line(Kind.DETAIL, reveal.innateDescription(), DETAIL_COLOR, 350));
        lines.add(new Line(Kind.SPACE, "", 0, 0));
        lines.add(new Line(Kind.LABEL, "Ability: " + reveal.abilityName(), ABILITY_COLOR, 120));
        lines.add(new Line(Kind.DETAIL, reveal.abilityDescription(), DETAIL_COLOR, 350));
        lines.add(new Line(Kind.SPACE, "", 0, 0));
        lines.add(new Line(Kind.LABEL, "Flaw: " + reveal.flawName(), FLAW_COLOR, 120));
        lines.add(new Line(Kind.DETAIL, reveal.flawDescription(), DETAIL_COLOR, 600));
        lines.add(new Line(Kind.GAP, "", 0, 0));
        lines.add(new Line(Kind.VOICE, "Every gift has its price.", VOICE_COLOR, 0));
        return lines;
    }

    @Override
    protected void init() {
        rows.clear();
        List<Integer> lengths = new ArrayList<>();
        List<Integer> pauses = new ArrayList<>();
        int wrapWidth = Math.min(320, this.width - 40);
        for (Line line : lines) {
            List<String> wrapped = wrap(line.text(), (int) (wrapWidth / line.kind().scale));
            for (int i = 0; i < wrapped.size(); i++) {
                rows.add(new Row(line.kind(), wrapped.get(i), line.color()));
                lengths.add(wrapped.get(i).length());
                pauses.add(i == wrapped.size() - 1 ? line.pauseMs() : 0);
            }
        }
        // Re-initialising on resize rebuilds the rows but keeps the clock running.
        typewriter = new RevealTypewriter(lengths, pauses, MS_PER_CHAR);
        if (openedAt < 0) {
            openedAt = Util.getMillis();
        }
    }

    private List<String> wrap(String text, int width) {
        if (text.isEmpty()) {
            return List.of("");
        }
        List<String> out = new ArrayList<>();
        for (FormattedText part : this.font.getSplitter().splitLines(text, Math.max(40, width), Style.EMPTY)) {
            out.add(part.getString());
        }
        return out.isEmpty() ? List.of("") : out;
    }

    private long elapsed() {
        return Util.getMillis() - openedAt + skipOffset;
    }

    @Override
    public void tick() {
        long elapsed = elapsed();
        int shown = typewriter.totalVisible(elapsed);
        if (shown - soundedChars >= CHARS_PER_TICK_SOUND && !typewriter.finished(elapsed)) {
            soundedChars = shown;
            float pitch = 1.6F + random.nextFloat() * 0.3F;
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_CHIME, pitch, 0.25F));
        }
        if (elapsed >= typewriter.endMs() + AUTO_CONTINUE_MS) {
            advance();
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillGradient(0, 0, this.width, this.height, VEIL_TOP, VEIL_BOTTOM);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        long elapsed = elapsed();
        int typing = typewriter.typingRow(elapsed);
        boolean cursorOn = (Util.getMillis() / 400) % 2 == 0;

        int total = rows.stream().mapToInt(row -> row.kind().height).sum();
        int y = Math.max(16, (this.height - total) / 2 - 8);
        int centre = this.width / 2;
        graphics.fill(centre - 60, y - 8, centre + 60, y - 7, RULE_COLOR);

        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            int shown = typewriter.visible(i, elapsed);
            if (shown > 0 || i == typing) {
                String text = row.text().substring(0, shown) + (i == typing && cursorOn ? "_" : "");
                drawCentred(graphics, row, text, centre, y);
            }
            y += row.kind().height;
        }
        graphics.fill(centre - 60, y + 2, centre + 60, y + 3, RULE_COLOR);

        Component hint = Component.translatable(typewriter.finished(elapsed)
                ? "screen.shadowslave.reveal.continue"
                : "screen.shadowslave.reveal.skip");
        graphics.drawCenteredString(this.font, hint, centre, this.height - 20, HINT_COLOR);
    }

    private void drawCentred(GuiGraphics graphics, Row row, String text, int centre, int y) {
        float scale = row.kind().scale;
        // Centre on the full row so the text grows rightwards as it types.
        int fullWidth = this.font.width(row.text());
        graphics.pose().pushPose();
        graphics.pose().translate(centre - fullWidth * scale / 2.0F, y, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(this.font, text, 0, 0, row.color(), row.kind() != Kind.VOICE);
        graphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        advance();
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            advance();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /** First press finishes the typing; the next one opens the Soul screen. */
    private void advance() {
        long elapsed = elapsed();
        if (!typewriter.finished(elapsed)) {
            skipOffset += typewriter.endMs() - elapsed;
            soundedChars = typewriter.totalVisible(typewriter.endMs());
            return;
        }
        if (!continued) {
            continued = true;
            // The server answers with a Soul snapshot that replaces this screen.
            PacketDistributor.sendToServer(OpenSoulScreenRequestPayload.INSTANCE);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
