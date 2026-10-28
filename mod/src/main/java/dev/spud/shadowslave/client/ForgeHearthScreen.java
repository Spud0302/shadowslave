package dev.spud.shadowslave.client;

import dev.spud.shadowslave.smithing.BilletData;
import dev.spud.shadowslave.smithing.ForgeHearthMenu;
import dev.spud.shadowslave.smithing.SmithingRegistry;
import dev.spud.shadowslave.smithing.SmithingRules;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/**
 * The Forge Hearth in the Spell look. Left: the two by two crucible and the
 * Fuse button, with what the last fuse said beneath. Middle: the billet over
 * the fire and the fuel feeding it. Right: a heat gauge in glow colours with
 * the fire's heat, the billet's heat and the pattern's working band marked.
 */
public final class ForgeHearthScreen extends AbstractContainerScreen<ForgeHearthMenu> {
    private static final int[] FUSE = {66, 30, 104, 44};
    private static final int GAUGE_X = 146;
    private static final int GAUGE_TOP = 18;
    private static final int GAUGE_BOTTOM = 92;
    private static final int GAUGE_WIDTH = 10;
    private static final int GAUGE_MAX = 1500;

    public ForgeHearthScreen(ForgeHearthMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = ForgeHearthMenu.INVENTORY_Y + 82;
        this.inventoryLabelY = ForgeHearthMenu.INVENTORY_Y - 11;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        int gx = this.leftPos + GAUGE_X;
        if (mouseX >= gx - 2 && mouseX < gx + GAUGE_WIDTH + 2 && mouseY >= this.topPos + GAUGE_TOP && mouseY < this.topPos + GAUGE_BOTTOM) {
            java.util.List<Component> lines = new java.util.ArrayList<>();
            lines.add(Component.literal("Fire: " + this.menu.temperature() + "°" + (this.menu.bellows() ? " (bellows)" : "")));
            BilletData billet = this.menu.billet().get(SmithingRegistry.BILLET.get());
            if (billet != null && !billet.cold()) {
                lines.add(Component.literal("Billet: " + this.menu.billetHeat() + "° · " + SmithingRules.glow(this.menu.billetHeat()).label()));
                lines.add(Component.literal("Works " + billet.heatMin() + "° to " + billet.heatMax() + "°"));
            }
            lines.add(Component.literal("Sneak and use the hearth to work the bellows."));
            graphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        SpellStyle.panel(graphics, x, y, x + this.imageWidth, y + this.imageHeight, SpellStyle.RULE);
        for (Slot slot : this.menu.slots) {
            boolean billet = slot.index == 5;
            graphics.fill(x + slot.x - 1, y + slot.y - 1, x + slot.x + 17, y + slot.y + 17, 0xE0080610);
            graphics.renderOutline(x + slot.x - 2, y + slot.y - 2, 20, 20, billet ? glowColour(this.menu.billetHeat(), 0.9F) : SpellStyle.RULE);
        }
        renderFlame(graphics, x + ForgeHearthMenu.FUEL_X + 2, y + ForgeHearthMenu.BILLET_Y + 20);
        renderGauge(graphics, x + GAUGE_X, y);
        graphics.fill(x + 8, y + ForgeHearthMenu.INVENTORY_Y - 14, x + this.imageWidth - 8, y + ForgeHearthMenu.INVENTORY_Y - 13, SpellStyle.RULE);
        renderButton(graphics, mouseX, mouseY, FUSE, "Fuse", this.menu.billet().isEmpty());
    }

    /** A small flame that shrinks as the fuel burns down. */
    private void renderFlame(GuiGraphics graphics, int left, int top) {
        float left01 = this.menu.burnLeft();
        int height = 12;
        int lit = Math.round(height * left01);
        graphics.fill(left + 3, top, left + 11, top + height, 0x60000000);
        if (lit > 0) {
            int colour = this.menu.bellows() ? 0xFFFFE08A : 0xFFF08A2A;
            graphics.fill(left + 3, top + height - lit, left + 11, top + height, colour);
            graphics.fill(left + 5, top + height - lit, left + 9, top + height, 0xFFFFF2B0);
        }
    }

    private void renderGauge(GuiGraphics graphics, int gx, int y) {
        int top = y + GAUGE_TOP;
        int bottom = y + GAUGE_BOTTOM;
        for (int py = top; py < bottom; py++) {
            int heat = Math.round(GAUGE_MAX * (bottom - py) / (float) (bottom - top));
            graphics.fill(gx, py, gx + GAUGE_WIDTH, py + 1, glowColour(heat, 0.55F));
        }
        graphics.renderOutline(gx - 1, top - 1, GAUGE_WIDTH + 2, bottom - top + 2, SpellStyle.BORDER);
        BilletData billet = this.menu.billet().get(SmithingRegistry.BILLET.get());
        if (billet != null && !billet.cold()) {
            int bandTop = heatY(billet.heatMax(), top, bottom);
            int bandBottom = heatY(billet.heatMin(), top, bottom);
            graphics.renderOutline(gx - 3, bandTop, GAUGE_WIDTH + 6, bandBottom - bandTop, SpellStyle.ASPECT);
            int by = heatY(this.menu.billetHeat(), top, bottom);
            graphics.fill(gx - 6, by - 1, gx - 1, by + 2, 0xFFFFFFFF);
        }
        int fy = heatY(this.menu.temperature(), top, bottom);
        graphics.fill(gx, fy, gx + GAUGE_WIDTH, fy + 1, 0xFFFFFFFF);
    }

    private static int heatY(int heat, int top, int bottom) {
        return Mth.clamp(bottom - Math.round((bottom - top) * heat / (float) GAUGE_MAX), top, bottom);
    }

    private static int glowColour(int heat, float alpha) {
        int rgb = SmithingRules.glow(heat).colour();
        return FastColor.ARGB32.color(Math.round(alpha * 255), (rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, 8, 7, SpellStyle.TITLE, false);
        graphics.drawString(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, SpellStyle.MUTED, false);
        String heat = this.menu.temperature() + "°";
        graphics.drawString(this.font, heat, GAUGE_X + GAUGE_WIDTH / 2 - this.font.width(heat) / 2, 7, SpellStyle.MUTED, false);
        String last = ClientWeavingState.status().lastResult();
        int y = 62;
        int width = 104;
        if (last.isEmpty()) {
            last = "Lay a pattern's materials in the crucible, then fuse.";
        }
        for (FormattedCharSequence line : this.font.split(Component.literal(last), width)) {
            if (y > ForgeHearthMenu.INVENTORY_Y - 22) {
                break;
            }
            graphics.drawString(this.font, line, 8, y, SpellStyle.VOICE, false);
            y += 9;
        }
    }

    private void renderButton(GuiGraphics graphics, int mouseX, int mouseY, int[] box, String label, boolean enabled) {
        int x0 = this.leftPos + box[0];
        int y0 = this.topPos + box[1];
        int x1 = this.leftPos + box[2];
        int y1 = this.topPos + box[3];
        boolean hovered = enabled && mouseX >= x0 && mouseX < x1 && mouseY >= y0 && mouseY < y1;
        graphics.fill(x0, y0, x1, y1, hovered ? SpellStyle.withAlpha(SpellStyle.BORDER, 0.5F) : SpellStyle.PANEL);
        graphics.renderOutline(x0, y0, x1 - x0, y1 - y0, enabled ? SpellStyle.BORDER : SpellStyle.RULE);
        graphics.drawCenteredString(this.font, label, (x0 + x1) / 2, y0 + 3, enabled ? SpellStyle.TEXT : SpellStyle.HINT);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= this.leftPos + FUSE[0] && mouseX < this.leftPos + FUSE[2]
                && mouseY >= this.topPos + FUSE[1] && mouseY < this.topPos + FUSE[3]) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, ForgeHearthMenu.BUTTON_FUSE);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
