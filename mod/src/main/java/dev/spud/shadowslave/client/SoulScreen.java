package dev.spud.shadowslave.client;

import dev.spud.shadowslave.network.payload.SoulSnapshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Read-only view of the server's Soul and revealed identity state, in the
 * Spell look: a translucent veil, a thin-ruled panel, and tabs for the Aspect,
 * its abilities, its Flaw and the Soul itself.
 */
public final class SoulScreen extends Screen {
    private static final int TAB_HEIGHT = 18;
    private static final SoulPages.Tab[] TABS = SoulPages.Tab.values();
    /** Remembered between openings, like a bookmark. */
    private static SoulPages.Tab lastTab = SoulPages.Tab.ASPECT;

    private final SoulSnapshot snapshot;
    private SoulPages.Tab tab = lastTab;
    private int left;
    private int top;
    private int right;
    private int bottom;

    public SoulScreen(SoulSnapshot snapshot) {
        super(Component.translatable("screen.shadowslave.soul"));
        this.snapshot = snapshot;
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
        renderTabs(graphics, mouseX, mouseY);

        long now = this.minecraft.level == null ? 0 : this.minecraft.level.getGameTime();
        List<SoulPages.Entry> entries = SoulPages.page(tab, snapshot, ClientAbilityState.status(),
                ClientAbilityState.remainingTicks(now), ClientKeyMappings.USE_ABILITY.getTranslatedKeyMessage().getString());
        renderEntries(graphics, entries);

        graphics.drawCenteredString(this.font, Component.translatable("screen.shadowslave.soul.tabs_hint"),
                this.width / 2, bottom + 6, SpellStyle.HINT);
    }

    private void renderTabs(GuiGraphics graphics, int mouseX, int mouseY) {
        int tabWidth = (right - left) / TABS.length;
        for (int i = 0; i < TABS.length; i++) {
            int x0 = left + i * tabWidth;
            boolean selected = TABS[i] == tab;
            boolean hovered = mouseX >= x0 && mouseX < x0 + tabWidth && mouseY >= top && mouseY < top + TAB_HEIGHT;
            int colour = selected ? SpellStyle.TITLE : hovered ? SpellStyle.TEXT : SpellStyle.MUTED;
            graphics.drawCenteredString(this.font, TABS[i].label, x0 + tabWidth / 2, top + 5, colour);
            if (selected) {
                graphics.fill(x0 + 8, top + TAB_HEIGHT - 2, x0 + tabWidth - 8, top + TAB_HEIGHT - 1, SpellStyle.BORDER);
            }
        }
        graphics.fill(left + 1, top + TAB_HEIGHT, right - 1, top + TAB_HEIGHT + 1, SpellStyle.RULE);
    }

    private void renderEntries(GuiGraphics graphics, List<SoulPages.Entry> entries) {
        int centre = (left + right) / 2;
        int textWidth = right - left - 32;
        int y = top + TAB_HEIGHT + 12;
        int labelX = left + 24;
        int valueX = left + 120;
        for (SoulPages.Entry entry : entries) {
            if (y > bottom - 12) {
                break;
            }
            switch (entry.style()) {
                case NAME -> {
                    float scale = this.font.width(entry.text()) * 1.5F <= textWidth ? 1.5F : 1.0F;
                    float textLeft = centre - this.font.width(entry.text()) * scale / 2.0F;
                    if (!entry.icon().isEmpty()) {
                        AbilityIcons.draw(graphics, entry.icon(), Math.round(textLeft) - 20, y - 2);
                    }
                    graphics.pose().pushPose();
                    graphics.pose().translate(textLeft, y, 0.0F);
                    graphics.pose().scale(scale, scale, 1.0F);
                    graphics.drawString(this.font, entry.text(), 0, 0, entry.colour(), true);
                    graphics.pose().popPose();
                    y += Math.round(9 * scale) + 4;
                }
                case SUBTITLE, HEADING, VOICE -> {
                    if (entry.icon().isEmpty()) {
                        graphics.drawCenteredString(this.font, entry.text(), centre, y, entry.colour());
                        y += 12;
                    } else {
                        // Icon and text are centred together as one unit.
                        int width = 20 + this.font.width(entry.text());
                        int x = centre - width / 2;
                        AbilityIcons.draw(graphics, entry.icon(), x, y);
                        graphics.drawString(this.font, entry.text(), x + 20, y + 4, entry.colour());
                        y += 20;
                    }
                }
                case BODY -> {
                    for (FormattedCharSequence line : this.font.split(Component.literal(entry.text()), textWidth)) {
                        graphics.drawString(this.font, line, centre - this.font.width(line) / 2, y, entry.colour(), false);
                        y += 10;
                    }
                    y += 2;
                }
                case ROW -> {
                    graphics.drawString(this.font, entry.text(), labelX, y, SpellStyle.MUTED, false);
                    graphics.drawString(this.font, entry.value(), valueX, y, entry.colour(), false);
                    y += 16;
                }
                case SPACER -> y += 6;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseY >= top && mouseY < top + TAB_HEIGHT && mouseX >= left && mouseX < right) {
            int index = (int) ((mouseX - left) / ((right - left) / TABS.length));
            select(TABS[Math.min(TABS.length - 1, index)]);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode >= GLFW.GLFW_KEY_1 && keyCode < GLFW.GLFW_KEY_1 + TABS.length) {
            select(TABS[keyCode - GLFW.GLFW_KEY_1]);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_TAB || keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_LEFT) {
            boolean back = keyCode == GLFW.GLFW_KEY_LEFT || (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
            select(TABS[Math.floorMod(tab.ordinal() + (back ? -1 : 1), TABS.length)]);
            return true;
        }
        if (ClientKeyMappings.OPEN_SOUL.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void select(SoulPages.Tab next) {
        if (next != tab) {
            tab = next;
            lastTab = next;
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
