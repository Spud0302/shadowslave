package dev.spud.shadowslave.client;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.smithing.BilletData;
import dev.spud.shadowslave.smithing.SmithingAnvilBlockEntity;
import dev.spud.shadowslave.smithing.SmithingRegistry;
import dev.spud.shadowslave.smithing.SmithingRules;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * The smith's two gauges under the crosshair. Holding a Smith's Hammer and
 * looking at a billet on an anvil shows the hammer bar: the marker sweeps
 * back and forth, the bright band is a clean blow and the dim one a rough
 * blow, and the billet's heat sits beside it. Holding a blade in a trough
 * shows the quench bar filling toward the hiss. The server judges every
 * blow and quench with the same numbers.
 */
public final class SmithingHudLayer implements LayeredDraw.Layer {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "smithing");

    private static final int BAR_WIDTH = 140;
    private static final int BAR_HEIGHT = 9;
    private static final int TOP_OFFSET = 20;

    @Override
    public void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (minecraft.options.hideGui || player == null || minecraft.level == null || minecraft.screen != null) {
            return;
        }
        double time = minecraft.level.getGameTime() + delta.getGameTimeDeltaPartialTick(false);
        if (player.isUsingItem() && player.getUseItem().is(SmithingRegistry.BILLET_ITEM.get())) {
            renderQuench(graphics, minecraft.font, player.getUseItem(), player.getTicksUsingItem() + delta.getGameTimeDeltaPartialTick(false));
            return;
        }
        if (!player.getMainHandItem().is(SmithingRegistry.SMITHS_HAMMER.get())) {
            return;
        }
        HitResult hit = minecraft.hitResult;
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK
                || !(minecraft.level.getBlockEntity(blockHit.getBlockPos()) instanceof SmithingAnvilBlockEntity anvil)) {
            return;
        }
        BilletData billet = anvil.piece().get(SmithingRegistry.BILLET.get());
        if (billet == null || billet.shaped()) {
            return;
        }
        renderHammer(graphics, minecraft.font, billet, time, (long) time);
    }

    private static void renderHammer(GuiGraphics graphics, Font font, BilletData billet, double time, long now) {
        int left = graphics.guiWidth() / 2 - BAR_WIDTH / 2;
        int top = graphics.guiHeight() / 2 + TOP_OFFSET;
        boolean workable = billet.workable(now);
        graphics.fill(left - 2, top - 2, left + BAR_WIDTH + 2, top + BAR_HEIGHT + 2, SpellStyle.PANEL);
        graphics.renderOutline(left - 2, top - 2, BAR_WIDTH + 4, BAR_HEIGHT + 4, SpellStyle.BORDER);
        float target = billet.target();
        float clean = billet.zone() / 1000.0F;
        float rough = clean * SmithingRules.ROUGH_SCALE;
        fillSpan(graphics, left, top, target - rough, target + rough, workable ? 0x80F2B05E : 0x40808080);
        fillSpan(graphics, left, top, target - clean, target + clean, workable ? 0xFFF6C33C : 0x60A0A0A0);
        float marker = SmithingRules.marker(time, billet.seed());
        int mx = left + Math.round(marker * BAR_WIDTH);
        graphics.fill(mx - 1, top - 4, mx + 1, top + BAR_HEIGHT + 4, workable ? 0xFFFFFFFF : 0xFF808080);

        String blows = Math.min(billet.landed(), billet.blows()) + "/" + billet.blows();
        graphics.drawString(font, blows, left + BAR_WIDTH + 6, top, SpellStyle.TEXT, true);
        if (billet.cold()) {
            graphics.drawString(font, "Cold work", left, top + BAR_HEIGHT + 5, SpellStyle.MUTED, true);
            return;
        }
        int heat = billet.heatAt(now);
        SmithingRules.Glow glow = SmithingRules.glow(heat);
        int rgb = glow.colour();
        int colour = FastColor.ARGB32.color(255, (rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
        String label = workable ? heat + "° · " + glow.label() : heat + "° · too cold, reheat it";
        graphics.drawString(font, label, left, top + BAR_HEIGHT + 5, workable ? colour : SpellStyle.FLAW, true);
        if (heat > billet.heatMax()) {
            String hot = "too hot: blows count but lose heat score";
            graphics.drawString(font, hot, left, top + BAR_HEIGHT + 15, SpellStyle.MUTED, true);
        }
    }

    private static void fillSpan(GuiGraphics graphics, int left, int top, float from, float to, int colour) {
        fillSpan(graphics, left, top, BAR_HEIGHT, from, to, colour);
    }

    private static void fillSpan(GuiGraphics graphics, int left, int top, int height, float from, float to, int colour) {
        int x0 = left + Math.round(Math.max(0.0F, from) * BAR_WIDTH);
        int x1 = left + Math.round(Math.min(1.0F, to) * BAR_WIDTH);
        if (x1 > x0) {
            graphics.fill(x0, top, x1, top + height, colour);
        }
    }

    private static void renderQuench(GuiGraphics graphics, Font font, ItemStack stack, float held) {
        BilletData billet = stack.get(SmithingRegistry.BILLET.get());
        if (billet == null) {
            return;
        }
        int left = graphics.guiWidth() / 2 - BAR_WIDTH / 2;
        int top = graphics.guiHeight() / 2 + TOP_OFFSET;
        float span = billet.quenchTicks() + SmithingRules.QUENCH_ZERO;
        graphics.fill(left - 2, top - 2, left + BAR_WIDTH + 2, top + BAR_HEIGHT + 2, SpellStyle.PANEL);
        graphics.renderOutline(left - 2, top - 2, BAR_WIDTH + 4, BAR_HEIGHT + 4, SpellStyle.BORDER);
        float perfectFrom = (billet.quenchTicks() - SmithingRules.QUENCH_PERFECT) / span;
        float perfectTo = (billet.quenchTicks() + SmithingRules.QUENCH_PERFECT) / span;
        fillSpan(graphics, left, top, perfectFrom, perfectTo, 0x8079DDF2);
        float filled = Math.min(1.0F, held / span);
        fillSpan(graphics, left, top + 2, BAR_HEIGHT - 4, 0.0F, filled, held > billet.quenchTicks() + SmithingRules.QUENCH_ZERO ? 0xFFF08080 : 0xFFF2EDF7);
        int mark = left + Math.round(billet.quenchTicks() / span * BAR_WIDTH);
        graphics.fill(mark, top - 4, mark + 1, top + BAR_HEIGHT + 4, SpellStyle.ASPECT);
        graphics.drawString(font, "Let go at the hiss", left, top + BAR_HEIGHT + 5, SpellStyle.ASPECT, true);
    }
}
