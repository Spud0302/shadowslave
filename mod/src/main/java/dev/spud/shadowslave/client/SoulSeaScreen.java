package dev.spud.shadowslave.client;

import dev.spud.shadowslave.network.payload.AwakenRequestPayload;
import dev.spud.shadowslave.network.payload.SoulSeaPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Locale;

/**
 * The Soul Sea, opened by meditating at a lit Dream Candle: the core, the next
 * rank and what it asks, and the abilities to choose between on rising.
 */
public final class SoulSeaScreen extends Screen {
    private static final int CARD_GAP = 12;
    private static final int BUTTON_WIDTH = 96;
    private static final int BUTTON_HEIGHT = 18;

    private SoulSeaPayload sea;
    private String selected = "";
    private int left;
    private int top;
    private int right;
    private int bottom;

    public SoulSeaScreen(SoulSeaPayload sea) {
        super(Component.translatable("screen.shadowslave.soul_sea"));
        this.sea = sea;
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
        int panelHeight = Math.min(240, this.height - 40);
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

        int centre = (left + right) / 2;
        int y = top + 14;
        if (!sea.core().hasCore()) {
            body(graphics, "Your soul has no Aspect yet. Sleep beside a lit Dream Candle and the Spell will name one.",
                    y, SpellStyle.MUTED);
            hint(graphics);
            return;
        }
        int labelX = left + 24;
        int valueX = left + 110;
        graphics.drawString(this.font, "Soul Rank", labelX, y, SpellStyle.MUTED, false);
        graphics.drawString(this.font, humanize(sea.core().rank()), valueX, y, SpellStyle.TEXT, false);
        y += 16;
        graphics.drawString(this.font, "Core", labelX, y, SpellStyle.MUTED, false);
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
        y += 14;
        graphics.fill(left + 16, y, right - 16, y + 1, SpellStyle.RULE);
        y += 8;

        if (sea.nextRank().isEmpty()) {
            body(graphics, "Your soul has risen as far as it can for now. The next rank is not open yet.", y,
                    SpellStyle.MUTED);
            hint(graphics);
            return;
        }
        graphics.drawCenteredString(this.font, "Rise to " + humanize(sea.nextRank()), centre, y, SpellStyle.TITLE);
        y += 12;
        graphics.drawCenteredString(this.font, sea.requirement(), centre, y, sea.ready() ? SpellStyle.TEXT : SpellStyle.MUTED);

        List<SoulSeaPayload.Choice> choices = sea.choices();
        int cardWidth = cardWidth(choices.size());
        int cardTop = cardTop();
        int cardBottom = buttonTop() - 8;
        for (int i = 0; i < choices.size(); i++) {
            int x0 = cardLeft(i, cardWidth);
            renderCard(graphics, choices.get(i), x0, cardTop, x0 + cardWidth, cardBottom,
                    inside(mouseX, mouseY, x0, cardTop, x0 + cardWidth, cardBottom));
        }
        renderButton(graphics, mouseX, mouseY);
        hint(graphics);
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

    private void renderButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x0 = (left + right - BUTTON_WIDTH) / 2;
        int y0 = buttonTop();
        boolean enabled = canAwaken();
        boolean hovered = enabled && inside(mouseX, mouseY, x0, y0, x0 + BUTTON_WIDTH, y0 + BUTTON_HEIGHT);
        graphics.fill(x0, y0, x0 + BUTTON_WIDTH, y0 + BUTTON_HEIGHT,
                hovered ? SpellStyle.withAlpha(SpellStyle.BORDER, 0.5F) : SpellStyle.PANEL);
        graphics.renderOutline(x0, y0, BUTTON_WIDTH, BUTTON_HEIGHT, enabled ? SpellStyle.BORDER : SpellStyle.RULE);
        graphics.drawCenteredString(this.font, "Awaken", x0 + BUTTON_WIDTH / 2, y0 + 5,
                enabled ? SpellStyle.TITLE : SpellStyle.HINT);
    }

    private void body(GuiGraphics graphics, String text, int y, int colour) {
        for (FormattedCharSequence line : this.font.split(Component.literal(text), right - left - 48)) {
            graphics.drawString(this.font, line, (left + right) / 2 - this.font.width(line) / 2, y, colour, false);
            y += 10;
        }
    }

    private void hint(GuiGraphics graphics) {
        graphics.drawCenteredString(this.font, Component.translatable(sea.choices().isEmpty()
                        ? "screen.shadowslave.soul_sea.hint" : "screen.shadowslave.soul_sea.choose"),
                this.width / 2, bottom + 6, SpellStyle.HINT);
    }

    private boolean canAwaken() {
        return sea.ready() && !selected.isEmpty();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!sea.nextRank().isEmpty() && sea.core().hasCore()) {
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
                if (mouseX >= x0 && mouseX < x0 + cardWidth && mouseY >= cardTop() && mouseY < buttonTop() - 8) {
                    selected = choices.get(i).abilityId();
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private int cardWidth(int count) {
        return count <= 0 ? 0 : (right - left - 32 - CARD_GAP * (count - 1)) / count;
    }

    private int cardLeft(int index, int cardWidth) {
        return left + 16 + index * (cardWidth + CARD_GAP);
    }

    /** Where the choice cards start, below the rank, core and requirement lines. */
    private int cardTop() {
        return top + 76;
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
