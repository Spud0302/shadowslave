package dev.spud.shadowslave.client;

import dev.spud.shadowslave.awakening.Longing;
import dev.spud.shadowslave.network.payload.AwakenRequestPayload;
import dev.spud.shadowslave.network.payload.IntentionRequestPayload;
import dev.spud.shadowslave.network.payload.RefineRequestPayload;
import dev.spud.shadowslave.network.payload.SoulSeaPayload;
import dev.spud.shadowslave.soul.refinement.RefinementRules;
import dev.spud.shadowslave.soul.refinement.RefinementStat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Locale;

/**
 * The Soul Sea, opened by meditating at a lit Dream Candle. The Rise page
 * shows the core, the next rank and what it asks, and the abilities to choose
 * between on rising; the Refine page places refinement points and respecs.
 */
public final class SoulSeaScreen extends Screen {
    private static final int TAB_HEIGHT = 18;
    private static final int CARD_GAP = 12;
    private static final int BUTTON_WIDTH = 96;
    private static final int RESPEC_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 18;
    private static final int PLUS_SIZE = 14;
    private static final int STAT_ROW = 30;

    enum Page {
        RISE("Rise"),
        REFINE("Refine");

        final String label;

        Page(String label) {
            this.label = label;
        }
    }

    private static final List<RefinementStat> STATS = List.of(RefinementStat.values());

    private SoulSeaPayload sea;
    private Page page = Page.RISE;
    private String selected = "";
    private String longing;
    private int left;
    private int top;
    private int right;
    private int bottom;

    public SoulSeaScreen(SoulSeaPayload sea) {
        super(Component.translatable("screen.shadowslave.soul_sea"));
        this.sea = sea;
        this.longing = sea.intention().longing();
    }

    /** Fresh state from the server while the screen is open. */
    public void update(SoulSeaPayload next) {
        this.sea = next;
        if (next.choices().stream().noneMatch(choice -> choice.abilityId().equals(selected))) {
            selected = "";
        }
    }

    SoulSeaPayload sea() {
        return sea;
    }

    @Override
    protected void init() {
        int panelWidth = Math.min(360, this.width - 32);
        // Tall enough for four rows of longings above the Offer button.
        int panelHeight = Math.min(276, this.height - 40);
        left = (this.width - panelWidth) / 2;
        top = (this.height - panelHeight) / 2;
        right = left + panelWidth;
        bottom = top + panelHeight;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        SpellStyle.veil(graphics, this.width, this.height);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        SpellStyle.panel(graphics, left, top, right, bottom, SpellStyle.RULE);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, top - 12, SpellStyle.TITLE);
        if (!sea.core().hasCore()) {
            renderIntention(graphics, mouseX, mouseY);
            return;
        }
        renderTabs(graphics, mouseX, mouseY);
        if (page == Page.RISE) {
            renderRise(graphics, mouseX, mouseY);
        } else {
            renderRefine(graphics, mouseX, mouseY);
        }
    }

    private void renderTabs(GuiGraphics graphics, int mouseX, int mouseY) {
        Page[] pages = Page.values();
        int tabWidth = (right - left) / pages.length;
        for (int i = 0; i < pages.length; i++) {
            int x0 = left + i * tabWidth;
            boolean chosen = pages[i] == page;
            boolean hovered = inside(mouseX, mouseY, x0, top, x0 + tabWidth, top + TAB_HEIGHT);
            String label = pages[i] == Page.REFINE && sea.refinement().unspent() > 0
                    ? pages[i].label + " (" + sea.refinement().unspent() + ")"
                    : pages[i].label;
            int colour = chosen ? SpellStyle.TITLE : hovered ? SpellStyle.TEXT : SpellStyle.MUTED;
            graphics.drawCenteredString(this.font, label, x0 + tabWidth / 2, top + 5, colour);
            if (chosen) {
                graphics.fill(x0 + 8, top + TAB_HEIGHT - 2, x0 + tabWidth - 8, top + TAB_HEIGHT - 1, SpellStyle.BORDER);
            }
        }
        graphics.fill(left + 1, top + TAB_HEIGHT, right - 1, top + TAB_HEIGHT + 1, SpellStyle.RULE);
    }

    private void renderRise(GuiGraphics graphics, int mouseX, int mouseY) {
        int centre = (left + right) / 2;
        int y = top + TAB_HEIGHT + 10;
        int labelX = left + 24;
        int valueX = left + 110;
        graphics.drawString(this.font, humanize(sea.core().rank()) + " core", labelX, y, SpellStyle.MUTED, false);
        String coreValue = sea.core().saturated() ? "Saturated" : Math.round(sea.core().fraction() * 100) + "%";
        int barRight = right - 24 - this.font.width("Saturated") - 8;
        graphics.fill(valueX, y + 1, barRight, y + 8, SpellStyle.PANEL);
        graphics.renderOutline(valueX, y, barRight - valueX, 9, SpellStyle.RULE);
        int filled = Math.round((barRight - valueX - 2) * sea.core().fraction());
        if (filled > 0) {
            graphics.fillGradient(valueX + 1, y + 1, valueX + 1 + filled, y + 8, SpellStyle.ASPECT,
                    SpellStyle.withAlpha(SpellStyle.ASPECT, 0.55F));
        }
        graphics.drawString(this.font, coreValue, barRight + 8, y, SpellStyle.ASPECT, false);
        y += 16;

        if (sea.nextRank().isEmpty()) {
            body(graphics, "Your soul has risen as far as it can for now. The next rank is not open yet.", y,
                    SpellStyle.MUTED);
            hint(graphics, "screen.shadowslave.soul_sea.hint");
            return;
        }
        graphics.drawCenteredString(this.font, "Rise to " + humanize(sea.nextRank()), centre, y, SpellStyle.TITLE);
        y += 12;
        // Two lines fit above the cards; a requirement with several parts wraps onto the second.
        List<FormattedCharSequence> requirement = this.font.split(Component.literal(sea.requirement()), right - left - 32);
        for (int i = 0; i < Math.min(2, requirement.size()); i++) {
            graphics.drawCenteredString(this.font, requirement.get(i), centre, y + i * 10,
                    sea.ready() ? SpellStyle.TEXT : SpellStyle.MUTED);
        }

        List<SoulSeaPayload.Choice> choices = sea.choices();
        int cardWidth = cardWidth(choices.size());
        int cardTop = cardTop();
        int cardBottom = buttonTop() - 8;
        for (int i = 0; i < choices.size(); i++) {
            int x0 = cardLeft(i, cardWidth);
            renderCard(graphics, choices.get(i), x0, cardTop, x0 + cardWidth, cardBottom,
                    inside(mouseX, mouseY, x0, cardTop, x0 + cardWidth, cardBottom));
        }
        renderButton(graphics, mouseX, mouseY, BUTTON_WIDTH, "Awaken", canAwaken());
        hint(graphics, "screen.shadowslave.soul_sea.choose");
    }

    private void renderRefine(GuiGraphics graphics, int mouseX, int mouseY) {
        int centre = (left + right) / 2;
        int y = top + TAB_HEIGHT + 10;
        SoulSeaPayload.Refinement points = sea.refinement();
        String heading = points.unspent() == 1 ? "1 refinement point to place"
                : points.unspent() + " refinement points to place";
        graphics.drawCenteredString(this.font, heading, centre, y, points.unspent() > 0 ? SpellStyle.TITLE : SpellStyle.MUTED);
        y = statTop();
        for (int i = 0; i < STATS.size(); i++) {
            RefinementStat stat = STATS.get(i);
            int rowY = y + i * STAT_ROW;
            graphics.drawString(this.font, stat.label(), left + 24, rowY, SpellStyle.TEXT, false);
            String value = String.valueOf(placed(points, stat));
            graphics.drawString(this.font, value, left + 84, rowY, SpellStyle.ABILITY, false);
            graphics.drawString(this.font, RefinementRules.perPoint(stat), left + 24, rowY + 11, SpellStyle.HINT, false);
            int plusX = right - 24 - PLUS_SIZE;
            boolean enabled = points.unspent() > 0;
            boolean hovered = enabled && inside(mouseX, mouseY, plusX, rowY - 3, plusX + PLUS_SIZE, rowY - 3 + PLUS_SIZE);
            graphics.fill(plusX, rowY - 3, plusX + PLUS_SIZE, rowY - 3 + PLUS_SIZE,
                    hovered ? SpellStyle.withAlpha(SpellStyle.BORDER, 0.5F) : SpellStyle.PANEL);
            graphics.renderOutline(plusX, rowY - 3, PLUS_SIZE, PLUS_SIZE, enabled ? SpellStyle.BORDER : SpellStyle.RULE);
            graphics.drawCenteredString(this.font, "+", plusX + PLUS_SIZE / 2, rowY, enabled ? SpellStyle.TITLE : SpellStyle.HINT);
        }
        renderButton(graphics, mouseX, mouseY, RESPEC_WIDTH, "Respec (lose 1 point)", points.canRespec());
        hint(graphics, "screen.shadowslave.soul_sea.refine");
    }

    /** Before the roll: name a longing, offer what is in hand, and see how the odds lean. */
    private void renderIntention(GuiGraphics graphics, int mouseX, int mouseY) {
        int centre = (left + right) / 2;
        int y = top + 12;
        graphics.drawCenteredString(this.font, "Before the Spell names you", centre, y, SpellStyle.TITLE);
        y += 14;
        body(graphics, "Offer something you carry and name what you long for. The Spell listens, but it is never bound.",
                y, SpellStyle.MUTED);
        y += 26;
        ItemStack held = this.minecraft.player == null ? ItemStack.EMPTY : this.minecraft.player.getMainHandItem();
        String holding = held.isEmpty() ? "Your hands are empty; you can still name a longing."
                : "In hand: " + held.getHoverName().getString();
        graphics.drawCenteredString(this.font, holding, centre, y, SpellStyle.TEXT);

        Longing[] longings = Longing.values();
        int width = longingWidth();
        List<Integer> odds = sea.intention().odds();
        for (int i = 0; i < longings.length; i++) {
            int x0 = longingLeft(i);
            int y0 = longingTop(i);
            boolean chosen = longings[i].serializedName().equals(longing);
            boolean hovered = inside(mouseX, mouseY, x0, y0, x0 + width, y0 + BUTTON_HEIGHT);
            graphics.fill(x0, y0, x0 + width, y0 + BUTTON_HEIGHT,
                    chosen ? SpellStyle.withAlpha(SpellStyle.BORDER, 0.35F) : hovered ? SpellStyle.withAlpha(SpellStyle.BORDER, 0.2F) : SpellStyle.PANEL);
            graphics.renderOutline(x0, y0, width, BUTTON_HEIGHT, chosen ? SpellStyle.TITLE : SpellStyle.RULE);
            graphics.drawCenteredString(this.font, longings[i].label(), x0 + width / 2, y0 + 5,
                    chosen ? SpellStyle.TITLE : SpellStyle.TEXT);
            if (i < odds.size()) {
                int oddsY = y0 + BUTTON_HEIGHT + 6;
                String label = longings[i].natureName() + " " + odds.get(i) + "%";
                // Eight to a row leaves little room: shrink the longer names to fit their column.
                float scale = Math.min(1.0F, (float) width / this.font.width(label));
                graphics.pose().pushPose();
                graphics.pose().translate(x0 + width / 2.0F, oddsY + (1.0F - scale) * 4.0F, 0.0F);
                graphics.pose().scale(scale, scale, 1.0F);
                graphics.drawCenteredString(this.font, label, 0, 0, SpellStyle.ASPECT);
                graphics.pose().popPose();
                graphics.fill(x0, oddsY + 11, x0 + width, oddsY + 15, SpellStyle.PANEL);
                // A full bar is 20%, a little above what an offering and a longing lift one Nature to, so a lean shows.
                int filled = Math.min(width, Math.round(width * odds.get(i) / 20.0F));
                graphics.fill(x0, oddsY + 11, x0 + filled, oddsY + 15, SpellStyle.withAlpha(SpellStyle.ASPECT, 0.8F));
            }
        }

        int summaryY = longingTop(longings.length - 1) + BUTTON_HEIGHT + 24;
        String offered = sea.intention().offering().isEmpty() ? "nothing offered" : "offered " + sea.intention().offering();
        String named = Longing.parse(sea.intention().longing()).map(value -> "longing to " + value.label().toLowerCase(Locale.ROOT))
                .orElse("no longing named");
        graphics.drawCenteredString(this.font, "So far: " + offered + ", " + named + ".", centre, summaryY, SpellStyle.MUTED);
        renderButton(graphics, mouseX, mouseY, BUTTON_WIDTH, "Offer", !longing.isEmpty());
        hint(graphics, "screen.shadowslave.soul_sea.intention");
    }

    /** Longings sit in rows of eight, one per Nature. */
    private static final int LONGINGS_PER_ROW = 8;
    private static final int LONGING_ROW = BUTTON_HEIGHT + 22;

    private int longingWidth() {
        return (right - left - 32 - (LONGINGS_PER_ROW - 1) * 8) / LONGINGS_PER_ROW;
    }

    private int longingLeft(int index) {
        return left + 16 + (index % LONGINGS_PER_ROW) * (longingWidth() + 8);
    }

    private int longingTop(int index) {
        return top + 70 + (index / LONGINGS_PER_ROW) * LONGING_ROW;
    }

    private static int placed(SoulSeaPayload.Refinement points, RefinementStat stat) {
        return switch (stat) {
            case BODY -> points.body();
            case SOUL -> points.soul();
            case ASPECT -> points.aspect();
        };
    }

    private void renderCard(GuiGraphics graphics, SoulSeaPayload.Choice choice, int x0, int y0, int x1, int y1, boolean hovered) {
        boolean chosen = choice.abilityId().equals(selected);
        graphics.fill(x0, y0, x1, y1, chosen ? SpellStyle.withAlpha(SpellStyle.BORDER, 0.25F) : SpellStyle.PANEL);
        graphics.renderOutline(x0, y0, x1 - x0, y1 - y0, chosen ? SpellStyle.TITLE : hovered ? SpellStyle.BORDER : SpellStyle.RULE);
        int y = y0 + 6;
        int width = 20 + this.font.width(choice.name());
        int x = (x0 + x1) / 2 - width / 2;
        AbilityIcons.draw(graphics, choice.abilityId(), x, y);
        graphics.drawString(this.font, choice.name(), x + 20, y + 4, SpellStyle.ABILITY, false);
        y += 20;
        graphics.drawCenteredString(this.font, choice.role(), (x0 + x1) / 2, y, SpellStyle.HINT);
        y += 12;
        for (FormattedCharSequence line : this.font.split(Component.literal(choice.description()), x1 - x0 - 12)) {
            if (y > y1 - 10) {
                break;
            }
            graphics.drawString(this.font, line, (x0 + x1) / 2 - this.font.width(line) / 2, y, SpellStyle.MUTED, false);
            y += 10;
        }
    }

    private void renderButton(GuiGraphics graphics, int mouseX, int mouseY, int width, String label, boolean enabled) {
        int x0 = (left + right - width) / 2;
        int y0 = buttonTop();
        boolean hovered = enabled && inside(mouseX, mouseY, x0, y0, x0 + width, y0 + BUTTON_HEIGHT);
        graphics.fill(x0, y0, x0 + width, y0 + BUTTON_HEIGHT,
                hovered ? SpellStyle.withAlpha(SpellStyle.BORDER, 0.5F) : SpellStyle.PANEL);
        graphics.renderOutline(x0, y0, width, BUTTON_HEIGHT, enabled ? SpellStyle.BORDER : SpellStyle.RULE);
        graphics.drawCenteredString(this.font, label, x0 + width / 2, y0 + 5, enabled ? SpellStyle.TITLE : SpellStyle.HINT);
    }

    private void body(GuiGraphics graphics, String text, int y, int colour) {
        for (FormattedCharSequence line : this.font.split(Component.literal(text), right - left - 48)) {
            graphics.drawString(this.font, line, (left + right) / 2 - this.font.width(line) / 2, y, colour, false);
            y += 10;
        }
    }

    private void hint(GuiGraphics graphics, String key) {
        graphics.drawCenteredString(this.font, Component.translatable(key), this.width / 2, bottom + 6, SpellStyle.HINT);
    }

    private boolean canAwaken() {
        return sea.ready() && !selected.isEmpty();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!sea.core().hasCore()) {
            return clickIntention(mouseX, mouseY) || super.mouseClicked(mouseX, mouseY, button);
        }
        if (inside(mouseX, mouseY, left, top, right, top + TAB_HEIGHT)) {
            int index = (int) ((mouseX - left) / ((right - left) / Page.values().length));
            select(Page.values()[Math.min(Page.values().length - 1, index)]);
            return true;
        }
        if (page == Page.REFINE) {
            return clickRefine(mouseX, mouseY) || super.mouseClicked(mouseX, mouseY, button);
        }
        if (!sea.nextRank().isEmpty()) {
            int buttonLeft = (left + right - BUTTON_WIDTH) / 2;
            if (canAwaken() && inside(mouseX, mouseY, buttonLeft, buttonTop(), buttonLeft + BUTTON_WIDTH, buttonTop() + BUTTON_HEIGHT)) {
                PacketDistributor.sendToServer(new AwakenRequestPayload(selected));
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F));
                return true;
            }
            List<SoulSeaPayload.Choice> choices = sea.choices();
            int cardWidth = cardWidth(choices.size());
            for (int i = 0; i < choices.size(); i++) {
                int x0 = cardLeft(i, cardWidth);
                if (inside(mouseX, mouseY, x0, cardTop(), x0 + cardWidth, buttonTop() - 8)) {
                    selected = choices.get(i).abilityId();
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean clickIntention(double mouseX, double mouseY) {
        Longing[] longings = Longing.values();
        for (int i = 0; i < longings.length; i++) {
            int x0 = longingLeft(i);
            if (inside(mouseX, mouseY, x0, longingTop(i), x0 + longingWidth(), longingTop(i) + BUTTON_HEIGHT)) {
                longing = longings[i].serializedName();
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
                return true;
            }
        }
        int buttonLeft = (left + right - BUTTON_WIDTH) / 2;
        if (!longing.isEmpty() && inside(mouseX, mouseY, buttonLeft, buttonTop(), buttonLeft + BUTTON_WIDTH, buttonTop() + BUTTON_HEIGHT)) {
            PacketDistributor.sendToServer(new IntentionRequestPayload(longing));
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F));
            return true;
        }
        return false;
    }

    private boolean clickRefine(double mouseX, double mouseY) {
        SoulSeaPayload.Refinement points = sea.refinement();
        int plusX = right - 24 - PLUS_SIZE;
        for (int i = 0; i < STATS.size(); i++) {
            int rowY = statTop() + i * STAT_ROW;
            if (points.unspent() > 0 && inside(mouseX, mouseY, plusX, rowY - 3, plusX + PLUS_SIZE, rowY - 3 + PLUS_SIZE)) {
                PacketDistributor.sendToServer(new RefineRequestPayload(STATS.get(i).serializedName()));
                return true;
            }
        }
        int respecLeft = (left + right - RESPEC_WIDTH) / 2;
        if (points.canRespec() && inside(mouseX, mouseY, respecLeft, buttonTop(), respecLeft + RESPEC_WIDTH, buttonTop() + BUTTON_HEIGHT)) {
            PacketDistributor.sendToServer(new RefineRequestPayload(RefineRequestPayload.RESPEC));
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 0.8F));
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (sea.core().hasCore() && (keyCode == GLFW.GLFW_KEY_1 || keyCode == GLFW.GLFW_KEY_2 || keyCode == GLFW.GLFW_KEY_TAB)) {
            select(keyCode == GLFW.GLFW_KEY_1 ? Page.RISE : keyCode == GLFW.GLFW_KEY_2 ? Page.REFINE
                    : Page.values()[(page.ordinal() + 1) % Page.values().length]);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void select(Page next) {
        if (next != page) {
            page = next;
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
        }
    }

    private int cardWidth(int count) {
        return count <= 0 ? 0 : (right - left - 32 - CARD_GAP * (count - 1)) / count;
    }

    private int cardLeft(int index, int cardWidth) {
        return left + 16 + index * (cardWidth + CARD_GAP);
    }

    /** Where the choice cards start, below the core and requirement lines. */
    private int cardTop() {
        return top + TAB_HEIGHT + 58;
    }

    private int statTop() {
        return top + TAB_HEIGHT + 34;
    }

    private int buttonTop() {
        return bottom - BUTTON_HEIGHT - 10;
    }

    private static boolean inside(double x, double y, int x0, int y0, int x1, int y1) {
        return x >= x0 && x < x1 && y >= y0 && y < y1;
    }

    static String humanize(String serialized) {
        if (serialized.isEmpty()) {
            return "";
        }
        return serialized.substring(0, 1).toUpperCase(Locale.ROOT) + serialized.substring(1);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
