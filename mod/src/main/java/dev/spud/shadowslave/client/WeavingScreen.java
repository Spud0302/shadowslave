package dev.spud.shadowslave.client;

import dev.spud.shadowslave.network.payload.WeavingStatusPayload;
import dev.spud.shadowslave.weaving.WeavingMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.List;

/**
 * The Weaving table in the Spell look. Left: four material slots, the Weave
 * button and the woven Memory. Right: the branch's level, points and next
 * step, Raise and Respec, and the patterns the player knows (hover one for
 * its materials). Everything shown comes from the server's Weaving reading.
 */
public final class WeavingScreen extends AbstractContainerScreen<WeavingMenu> {
    private static final int LEFT_WIDTH = 176;
    private static final int RIGHT_LEFT = 180;
    private static final int BUTTON_HEIGHT = 14;
    private static final int[] WEAVE = {100, 62, 160, 62 + BUTTON_HEIGHT};
    private static final int PATTERN_TOP = 104;
    private static final int PATTERN_ROW = 11;

    private int branch;

    public WeavingScreen(WeavingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 320;
        this.imageHeight = 222;
        this.inventoryLabelY = WeavingMenu.INVENTORY_Y - 11;
    }

    private WeavingStatusPayload status() {
        return ClientWeavingState.status();
    }

    private WeavingStatusPayload.Branch currentBranch() {
        List<WeavingStatusPayload.Branch> branches = status().branches();
        if (branches.isEmpty()) {
            return null;
        }
        branch = Math.min(branch, branches.size() - 1);
        return branches.get(branch);
    }

    private List<WeavingStatusPayload.Pattern> patterns() {
        WeavingStatusPayload.Branch current = currentBranch();
        return current == null ? List.of() : status().patterns().stream().filter(pattern -> pattern.branch().equals(current.name())).toList();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderPatternTooltip(graphics, mouseX, mouseY);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        SpellStyle.panel(graphics, x, y, x + LEFT_WIDTH, y + this.imageHeight, SpellStyle.RULE);
        SpellStyle.panel(graphics, x + RIGHT_LEFT, y, x + this.imageWidth, y + this.imageHeight, SpellStyle.RULE);
        for (Slot slot : this.menu.slots) {
            boolean output = slot.index == 4;
            int colour = output ? SpellStyle.ASPECT : SpellStyle.RULE;
            graphics.fill(x + slot.x - 1, y + slot.y - 1, x + slot.x + 17, y + slot.y + 17, 0xE0080610);
            graphics.renderOutline(x + slot.x - 2, y + slot.y - 2, 20, 20, output ? SpellStyle.withAlpha(colour, 0.8F) : colour);
        }
        // Threads run from the material slots to the woven Memory.
        int threadY = y + WeavingMenu.OUTPUT_Y + 8;
        long time = this.minecraft.level == null ? 0 : this.minecraft.level.getGameTime();
        for (int i = 0; i < 3; i++) {
            int offset = (i - 1) * 4;
            int startX = x + 74;
            int endX = x + WeavingMenu.OUTPUT_X - 4;
            for (int px = startX; px < endX; px++) {
                float t = (px - startX) / (float) (endX - startX);
                int wave = Math.round((float) Math.sin(t * Math.PI * 3 + time * 0.15F + i) * 2.0F * (1.0F - t));
                int py = threadY + Math.round(offset * (1.0F - t)) + wave;
                graphics.fill(px, py, px + 1, py + 1, SpellStyle.withAlpha(i == 1 ? SpellStyle.ASPECT : SpellStyle.ABILITY, 0.35F + 0.5F * t));
            }
        }
        graphics.fill(x + 8, y + WeavingMenu.INVENTORY_Y - 14, x + LEFT_WIDTH - 8, y + WeavingMenu.INVENTORY_Y - 13, SpellStyle.RULE);
        renderButton(graphics, mouseX, mouseY, WEAVE, "Weave", true);
        renderBranch(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, 8, 7, SpellStyle.TITLE, false);
        graphics.drawString(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, SpellStyle.MUTED, false);
        String last = status().lastResult();
        if (!last.isEmpty()) {
            int y = 84;
            for (FormattedCharSequence line : this.font.split(Component.literal(last), LEFT_WIDTH - 16)) {
                if (y > WeavingMenu.INVENTORY_Y - 24) {
                    break;
                }
                graphics.drawString(this.font, line, 8, y, SpellStyle.VOICE, false);
                y += 10;
            }
        } else {
            graphics.drawString(this.font, "Lay materials, then weave.", 8, 84, SpellStyle.HINT, false);
        }
    }

    private void renderBranch(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + RIGHT_LEFT + 8;
        int y = this.topPos + 7;
        int width = this.imageWidth - RIGHT_LEFT - 16;
        WeavingStatusPayload status = status();
        WeavingStatusPayload.Branch current = currentBranch();
        if (current == null) {
            graphics.drawString(this.font, "No Weaving branches are loaded.", x, y, SpellStyle.MUTED, false);
            return;
        }
        if (status.branches().size() > 1) {
            graphics.drawString(this.font, "‹ " + current.name() + " ›", x, y, SpellStyle.TITLE, false);
        } else {
            graphics.drawString(this.font, current.name(), x, y, SpellStyle.TITLE, false);
        }
        String level = "Level " + current.level() + "/" + current.maxLevel();
        graphics.drawString(this.font, level, x + width - this.font.width(level), y, SpellStyle.ASPECT, false);
        y += 13;
        graphics.drawString(this.font, current.xp() + " XP  ·  " + status.points() + (status.points() == 1 ? " point" : " points"),
                x, y, SpellStyle.TEXT, false);
        y += 11;
        int barRight = x + width;
        graphics.fill(x, y, barRight, y + 4, SpellStyle.PANEL);
        graphics.renderOutline(x, y, width, 4, SpellStyle.RULE);
        int filled = Math.round((width - 2) * status.xpTowardPoint() / (float) Math.max(1, status.xpPerPoint()));
        if (filled > 0) {
            graphics.fill(x + 1, y + 1, x + 1 + filled, y + 3, SpellStyle.ABILITY);
        }
        y += 8;
        for (FormattedCharSequence line : this.font.split(Component.literal(current.next()), width)) {
            graphics.drawString(this.font, line, x, y, SpellStyle.MUTED, false);
            y += 10;
        }
        renderButton(graphics, mouseX, mouseY, raiseBox(), "Raise", current.canRaise());
        renderButton(graphics, mouseX, mouseY, respecBox(), "Respec", status.canRespec());

        int listY = this.topPos + PATTERN_TOP;
        graphics.drawString(this.font, "Patterns you know", x, listY - 12, SpellStyle.ASPECT, false);
        List<WeavingStatusPayload.Pattern> patterns = patterns();
        for (int i = 0; i < patterns.size(); i++) {
            WeavingStatusPayload.Pattern pattern = patterns.get(i);
            int rowY = listY + i * PATTERN_ROW;
            if (rowY > this.topPos + this.imageHeight - 12) {
                break;
            }
            boolean hovered = mouseX >= x && mouseX < x + width && mouseY >= rowY - 1 && mouseY < rowY + PATTERN_ROW - 1;
            if (hovered) {
                graphics.fill(x - 2, rowY - 2, x + width + 2, rowY + PATTERN_ROW - 2, SpellStyle.withAlpha(SpellStyle.BORDER, 0.25F));
            }
            int colour = pattern.lock().isEmpty() ? SpellStyle.TEXT : SpellStyle.HINT;
            graphics.drawString(this.font, pattern.name(), x, rowY, colour, false);
            String tier = SoulPages.tier(pattern.tier());
            graphics.drawString(this.font, tier, x + width - this.font.width(tier), rowY,
                    pattern.lock().isEmpty() ? SpellStyle.ABILITY : SpellStyle.HINT, false);
        }
        if (patterns.isEmpty()) {
            graphics.drawString(this.font, "None yet.", x, listY, SpellStyle.HINT, false);
        }
    }

    private void renderPatternTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + RIGHT_LEFT + 8;
        int width = this.imageWidth - RIGHT_LEFT - 16;
        List<WeavingStatusPayload.Pattern> patterns = patterns();
        for (int i = 0; i < patterns.size(); i++) {
            int rowY = this.topPos + PATTERN_TOP + i * PATTERN_ROW;
            if (mouseX >= x && mouseX < x + width && mouseY >= rowY - 1 && mouseY < rowY + PATTERN_ROW - 1) {
                WeavingStatusPayload.Pattern pattern = patterns.get(i);
                List<Component> lines = new ArrayList<>();
                lines.add(Component.literal(pattern.name()).withStyle(ChatFormatting.LIGHT_PURPLE));
                lines.add(Component.literal(pattern.rank() + " Memory, Tier " + SoulPages.tier(pattern.tier())).withStyle(ChatFormatting.AQUA));
                for (WeavingStatusPayload.Material material : pattern.materials()) {
                    lines.add(Component.literal(material.count() + " × " + SoulScreen.itemName(material.item())).withStyle(ChatFormatting.GRAY));
                }
                if (!pattern.lock().isEmpty()) {
                    lines.add(Component.literal(pattern.lock()).withStyle(ChatFormatting.RED));
                }
                if (pattern.woven() > 0) {
                    lines.add(Component.literal("Woven " + pattern.woven() + (pattern.woven() == 1 ? " time" : " times"))
                            .withStyle(ChatFormatting.DARK_GRAY));
                }
                graphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
                return;
            }
        }
    }

    private int[] raiseBox() {
        int x = RIGHT_LEFT + 8;
        int width = (this.imageWidth - RIGHT_LEFT - 16 - 6) / 2;
        return new int[]{x, 72, x + width, 72 + BUTTON_HEIGHT};
    }

    private int[] respecBox() {
        int[] raise = raiseBox();
        int width = raise[2] - raise[0];
        return new int[]{raise[2] + 6, raise[1], raise[2] + 6 + width, raise[3]};
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

    private boolean inside(double mouseX, double mouseY, int[] box) {
        return mouseX >= this.leftPos + box[0] && mouseX < this.leftPos + box[2]
                && mouseY >= this.topPos + box[1] && mouseY < this.topPos + box[3];
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        WeavingStatusPayload.Branch current = currentBranch();
        if (inside(mouseX, mouseY, WEAVE)) {
            press(WeavingMenu.BUTTON_WEAVE);
            return true;
        }
        if (current != null && current.canRaise() && inside(mouseX, mouseY, raiseBox())) {
            press(WeavingMenu.BUTTON_RAISE + branch);
            return true;
        }
        if (status().canRespec() && inside(mouseX, mouseY, respecBox())) {
            press(WeavingMenu.BUTTON_RESPEC);
            return true;
        }
        int branches = status().branches().size();
        if (branches > 1 && mouseX >= this.leftPos + RIGHT_LEFT && mouseY >= this.topPos + 4 && mouseY < this.topPos + 18) {
            branch = (branch + 1) % branches;
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void press(int id) {
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
    }
}
