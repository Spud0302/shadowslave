package dev.spud.shadowslave.client;

import dev.spud.shadowslave.network.payload.NightmareAppraisalPayload;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * The Spell appraises a conquered Nightmare (after ch. 15-16): over a glimpse of
 * its inner workings, a web of stars with runes drifting through it (after the
 * manhwa's look), it types out every deed, then the Spell's comment ("You have
 * achieved the impossible!"), then the grade with its comment, then the boons. Click, Space or Enter skips the typing, and again closes; long
 * appraisals scroll to follow the typing, and the mouse wheel scrolls back up.
 */
public class NightmareAppraisalScreen extends Screen {
    private static final long MS_PER_CHAR = 16;
    private static final int CHARS_PER_TICK_SOUND = 3;
    private static final int STARS = 140;
    private static final int RUNES = 18;
    private static final int LINK_DISTANCE = 46;

    static final int GRADE_COLOR = 0xFFFFD27A;

    enum Kind {
        VOICE(1.0F, 13),
        NAME(2.0F, 24),
        GRADE(2.0F, 22),
        LABEL(1.0F, 13),
        DETAIL(1.0F, 11),
        GAP(1.0F, 8);

        final float scale;
        final int height;

        Kind(float scale, int height) {
            this.scale = scale;
            this.height = height;
        }
    }

    record Line(Kind kind, String text, int color, int pauseMs) {
    }

    private record Row(Kind kind, String text, int color) {
    }

    private final RandomSource random = RandomSource.create();
    private final List<Line> lines;
    private final List<Row> rows = new ArrayList<>();
    private final SpellWeb web;
    private RevealTypewriter typewriter;
    private long openedAt = -1;
    private long skipOffset;
    private int soundedChars;
    private int scrollBack;
    private int maxScroll;

    public NightmareAppraisalScreen(NightmareAppraisalPayload appraisal) {
        this(Component.translatable("screen.shadowslave.appraisal"), script(appraisal), appraisal.nightmare().hashCode());
    }

    /** Any of the Spell's typed announcements over its web, such as a True Name's reveal. */
    NightmareAppraisalScreen(Component title, List<Line> lines, int seed) {
        super(title);
        this.lines = lines;
        this.web = new SpellWeb(seed, STARS, RUNES, LINK_DISTANCE);
    }

    static List<Line> script(NightmareAppraisalPayload appraisal) {
        List<Line> lines = new ArrayList<>();
        lines.add(new Line(Kind.VOICE, "The Nightmare is over. The Spell weighs what you did in it.", SpellStyle.VOICE, 450));
        lines.add(new Line(Kind.GAP, "", 0, 0));
        lines.add(new Line(Kind.NAME, appraisal.nightmare(), SpellStyle.ASPECT, 200));
        if (!appraisal.setting().isEmpty()) {
            lines.add(new Line(Kind.DETAIL, appraisal.setting(), SpellStyle.MUTED, 400));
        }
        if (!appraisal.ending().isEmpty()) {
            lines.add(new Line(Kind.GAP, "", 0, 0));
            lines.add(new Line(Kind.VOICE, appraisal.ending(), SpellStyle.VOICE, 450));
        }
        lines.add(new Line(Kind.GAP, "", 0, 0));
        for (String deed : appraisal.deeds()) {
            lines.add(new Line(Kind.DETAIL, "[" + deed + "]", SpellStyle.TEXT, 140));
        }
        lines.add(new Line(Kind.GAP, "", 0, 0));
        if (!appraisal.acclaim().isEmpty()) {
            // The Spell's comment comes before the grade (ch. 15: "You have achieved the impossible!").
            lines.add(new Line(Kind.VOICE, appraisal.acclaim(), GRADE_COLOR, 600));
        }
        lines.add(new Line(Kind.LABEL, "Your performance has been appraised:", SpellStyle.TEXT, 300));
        lines.add(new Line(Kind.GRADE, appraisal.grade(), GRADE_COLOR, 300));
        for (String comment : appraisal.comments()) {
            lines.add(new Line(Kind.VOICE, comment, SpellStyle.VOICE, 500));
        }
        lines.add(new Line(Kind.GAP, "", 0, 0));
        lines.add(new Line(Kind.LABEL, "You have received:", SpellStyle.TEXT, 150));
        for (String boon : appraisal.boons()) {
            lines.add(new Line(Kind.DETAIL, boon, SpellStyle.ABILITY, 140));
        }
        if (!appraisal.trueName().isEmpty()) {
            lines.add(new Line(Kind.GAP, "", 0, 0));
            lines.add(new Line(Kind.LABEL, "You have received a True Name.", SpellStyle.TEXT, 300));
            if (!appraisal.trueNameLine().isEmpty()) {
                lines.add(new Line(Kind.DETAIL, "[" + appraisal.trueNameLine() + "]", SpellStyle.TEXT, 300));
            }
            lines.add(new Line(Kind.LABEL, "Your True Name is:", SpellStyle.TEXT, 300));
            lines.add(new Line(Kind.GRADE, appraisal.trueName(), GRADE_COLOR, 500));
        }
        if (!appraisal.lore().isEmpty()) {
            lines.add(new Line(Kind.GAP, "", 0, 0));
            lines.add(new Line(Kind.DETAIL, appraisal.lore(), SpellStyle.HINT, 0));
        }
        return lines;
    }

    @Override
    protected void init() {
        rows.clear();
        List<Integer> lengths = new ArrayList<>();
        List<Integer> pauses = new ArrayList<>();
        int wrapWidth = Math.min(340, this.width - 40);
        for (Line line : lines) {
            List<String> wrapped = wrap(line.text(), (int) (wrapWidth / line.kind().scale));
            for (int i = 0; i < wrapped.size(); i++) {
                rows.add(new Row(line.kind(), wrapped.get(i), line.color()));
                lengths.add(wrapped.get(i).length());
                pauses.add(i == wrapped.size() - 1 ? line.pauseMs() : 0);
            }
        }
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
            float pitch = 1.5F + random.nextFloat() * 0.3F;
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_CHIME, pitch, 0.22F));
        }
    }

    // ---- the Spell's inner workings ------------------------------------------------------------------------------

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int w = this.width;
        int h = this.height;
        float time = (Util.getMillis() - Math.max(0, openedAt)) / 1000.0F;
        web.render(graphics, this.font, 0, 0, w, h, time, 1.0F, 1.0F);
        // A veil behind the text so the words stay readable.
        int panel = Math.min(380, w - 20);
        graphics.fillGradient(w / 2 - panel / 2, 0, w / 2 + panel / 2, h, 0x70020308, 0xA0020308);
    }

    // ---- the words -----------------------------------------------------------------------------------------------

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        long elapsed = elapsed();
        int typing = typewriter.typingRow(elapsed);
        boolean cursorOn = (Util.getMillis() / 400) % 2 == 0;

        int total = rows.stream().mapToInt(row -> row.kind().height).sum();
        int top = Math.max(18, (this.height - total) / 2 - 8);
        // Follow the typing once the appraisal runs off the bottom of the screen.
        int last = typing >= 0 ? typing : lastVisibleRow(elapsed);
        int lastBottom = top;
        for (int i = 0; i <= last && i < rows.size(); i++) {
            lastBottom += rows.get(i).kind().height;
        }
        maxScroll = Math.max(0, lastBottom - (this.height - 34));
        scrollBack = Math.min(scrollBack, maxScroll);
        int scroll = maxScroll - scrollBack;
        int y = top - scroll;
        int centre = this.width / 2;
        // Keep the words clear of the hint at the bottom while scrolled back.
        graphics.enableScissor(0, 0, this.width, this.height - 26);
        graphics.fill(centre - 70, y - 8, centre + 70, y - 7, SpellStyle.RULE);
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            int shown = typewriter.visible(i, elapsed);
            if ((shown > 0 || i == typing) && y > -30 && y < this.height) {
                String text = row.text().substring(0, shown) + (i == typing && cursorOn ? "_" : "");
                drawCentred(graphics, row, text, centre, y);
            }
            y += row.kind().height;
        }
        if (typewriter.finished(elapsed)) {
            graphics.fill(centre - 70, y + 2, centre + 70, y + 3, SpellStyle.RULE);
        }
        graphics.disableScissor();
        Component hint = Component.translatable(typewriter.finished(elapsed)
                ? "screen.shadowslave.appraisal.close"
                : "screen.shadowslave.reveal.skip");
        graphics.drawCenteredString(this.font, hint, centre, this.height - 16, SpellStyle.HINT);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scrollBack = Math.max(0, Math.min(maxScroll, scrollBack + (int) (scrollY * 12)));
        return true;
    }

    private int lastVisibleRow(long elapsed) {
        int last = 0;
        for (int i = 0; i < rows.size(); i++) {
            if (typewriter.visible(i, elapsed) > 0) {
                last = i;
            }
        }
        return last;
    }

    private void drawCentred(GuiGraphics graphics, Row row, String text, int centre, int y) {
        float scale = row.kind().scale;
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

    /** First press finishes the typing; the next one closes the appraisal. */
    private void advance() {
        long elapsed = elapsed();
        if (!typewriter.finished(elapsed)) {
            skipOffset += typewriter.endMs() - elapsed;
            soundedChars = typewriter.totalVisible(typewriter.endMs());
            return;
        }
        onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
