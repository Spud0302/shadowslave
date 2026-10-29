package dev.spud.shadowslave.client;

import dev.spud.shadowslave.network.payload.MemorySummonPayload;
import dev.spud.shadowslave.network.payload.SoulSnapshot;
import dev.spud.shadowslave.network.payload.WeavingStatusPayload;
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
                ClientAbilityState.essence(now, partialTick), ClientKeyMappings.abilityKeyNames(),
                ClientCoreState.status(), ClientWeavingState.status(), SoulScreen::itemName);
        renderEntries(graphics, entries);
        if (tab == SoulPages.Tab.MEMORIES) {
            renderSea(graphics, mouseX, mouseY, now + partialTick);
        }

        graphics.drawCenteredString(this.font, Component.translatable("screen.shadowslave.soul.tabs_hint"),
                this.width / 2, bottom + 6, SpellStyle.HINT);
    }

    /** "minecraft:iron_ingot" to "Iron Ingot", in the player's language. */
    static String itemName(String id) {
        net.minecraft.resources.ResourceLocation location = net.minecraft.resources.ResourceLocation.tryParse(id);
        if (location == null || !net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(location)) {
            return id;
        }
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.get(location).getDescription().getString();
    }

    /** Where the Soul Sea's spheres circle: below the page's lines, in the panel's lower part. */
    private int seaX() {
        return (left + right) / 2;
    }

    private int seaY() {
        return top + TAB_HEIGHT + 60 + (bottom - top - TAB_HEIGHT - 60) / 2;
    }

    /** Each Memory's place on its orbit at {@code time}, as x, y pairs. */
    private int[][] spheres(int count, double time) {
        int[][] places = new int[count][2];
        int rx = Math.min(130, (right - left) / 2 - 30);
        int ry = Math.max(24, (bottom - seaY()) - 22);
        for (int i = 0; i < count; i++) {
            double angle = time * 0.01 + Math.PI * 2 * i / Math.max(1, count);
            places[i][0] = seaX() + (int) Math.round(Math.cos(angle) * rx);
            places[i][1] = seaY() + (int) Math.round(Math.sin(angle) * ry);
        }
        return places;
    }

    /**
     * The Soul Sea (lore: Memories rest there as spheres circling the soul,
     * ch. 47): a glow at the centre and each dismissed Memory as a sphere
     * around it, its item drawn inside. Hover one to read it; click to summon it.
     */
    private void renderSea(GuiGraphics graphics, int mouseX, int mouseY, double time) {
        List<WeavingStatusPayload.Memory> held = ClientWeavingState.status().soul().held();
        int cx = seaX();
        int cy = seaY();
        float pulse = 0.5F + 0.5F * (float) Math.sin(time * 0.08);
        for (int radius = 14; radius > 0; radius -= 2) {
            graphics.fill(cx - radius, cy - radius / 2, cx + radius, cy + radius / 2,
                    SpellStyle.withAlpha(SpellStyle.ASPECT, 0.05F + 0.04F * pulse));
        }
        graphics.fill(cx - 2, cy - 2, cx + 2, cy + 2, SpellStyle.withAlpha(SpellStyle.TITLE, 0.9F));
        int[][] places = spheres(held.size(), time);
        int hovered = -1;
        for (int i = 0; i < held.size(); i++) {
            int x = places[i][0];
            int y = places[i][1];
            boolean over = Math.abs(mouseX - x) <= 11 && Math.abs(mouseY - y) <= 11;
            if (over) {
                hovered = i;
            }
            orb(graphics, x, y, over ? SpellStyle.TITLE : SpellStyle.ABILITY, over);
            net.minecraft.resources.ResourceLocation item = net.minecraft.resources.ResourceLocation.tryParse(held.get(i).item());
            if (item != null && net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(item)) {
                graphics.renderItem(new net.minecraft.world.item.ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(item)), x - 8, y - 8);
            }
            WeavingStatusPayload.Memory memory = held.get(i);
            if (memory.maxDamage() > 0 && memory.damage() > 0) {
                int width = Math.round(14.0F * (memory.maxDamage() - memory.damage()) / memory.maxDamage());
                graphics.fill(x - 7, y + 10, x + 7, y + 11, 0xFF101010);
                graphics.fill(x - 7, y + 10, x - 7 + width, y + 11, SpellStyle.ASPECT);
            }
        }
        if (hovered >= 0) {
            List<Component> lines = new java.util.ArrayList<>();
            List<String> text = SoulPages.memoryLines(held.get(hovered));
            for (int i = 0; i < text.size(); i++) {
                lines.add(Component.literal(text.get(i)).withStyle(i == 0 ? net.minecraft.ChatFormatting.LIGHT_PURPLE
                        : i == text.size() - 1 ? net.minecraft.ChatFormatting.DARK_GRAY : net.minecraft.ChatFormatting.GRAY));
            }
            graphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
        }
    }

    /** A soft sphere: a faint disc, lighter toward the top, inside a bright rim. */
    private static void orb(GuiGraphics graphics, int x, int y, int colour, boolean bright) {
        int radius = 11;
        for (int dy = -radius; dy <= radius; dy++) {
            int half = (int) Math.round(Math.sqrt(radius * radius - dy * dy));
            float shade = 0.10F + 0.12F * (radius - dy) / (2.0F * radius);
            graphics.fill(x - half, y + dy, x + half + 1, y + dy + 1, SpellStyle.withAlpha(colour, shade));
            int rim = SpellStyle.withAlpha(colour, bright ? 0.95F : 0.65F);
            graphics.fill(x - half, y + dy, x - half + 1, y + dy + 1, rim);
            graphics.fill(x + half, y + dy, x + half + 1, y + dy + 1, rim);
        }
        graphics.fill(x - 4, y - radius, x + 5, y - radius + 1, SpellStyle.withAlpha(colour, bright ? 0.95F : 0.65F));
        graphics.fill(x - 4, y + radius, x + 5, y + radius + 1, SpellStyle.withAlpha(colour, bright ? 0.95F : 0.65F));
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
                case METER -> {
                    graphics.drawString(this.font, entry.text(), labelX, y, SpellStyle.MUTED, false);
                    int barLeft = valueX;
                    int barRight = right - 24 - this.font.width("Saturated") - 8;
                    graphics.fill(barLeft, y + 1, barRight, y + 8, SpellStyle.PANEL);
                    graphics.renderOutline(barLeft, y, barRight - barLeft, 9, SpellStyle.RULE);
                    int filled = Math.round((barRight - barLeft - 2) * entry.fill());
                    if (filled > 0) {
                        graphics.fillGradient(barLeft + 1, y + 1, barLeft + 1 + filled, y + 8, entry.colour(),
                                SpellStyle.withAlpha(entry.colour(), 0.55F));
                    }
                    graphics.drawString(this.font, entry.value(), barRight + 8, y, entry.colour(), false);
                    y += 16;
                }
                case SPACER -> y += 6;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (tab == SoulPages.Tab.MEMORIES && button == 0 && this.minecraft.level != null) {
            List<WeavingStatusPayload.Memory> held = ClientWeavingState.status().soul().held();
            int[][] places = spheres(held.size(), this.minecraft.level.getGameTime() + this.minecraft.getTimer().getGameTimeDeltaPartialTick(false));
            for (int i = 0; i < places.length; i++) {
                if (Math.abs(mouseX - places[i][0]) <= 11 && Math.abs(mouseY - places[i][1]) <= 11) {
                    net.neoforged.neoforge.network.PacketDistributor.sendToServer(new MemorySummonPayload(i));
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F));
                    return true;
                }
            }
        }
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
