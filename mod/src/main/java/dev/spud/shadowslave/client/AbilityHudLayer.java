package dev.spud.shadowslave.client;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.network.payload.AbilityStatusPayload;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

import java.util.List;

/**
 * The Aspect ability slots left of the hotbar, one per ability key with the
 * first key nearest the hotbar, and the essence bar they share on the far
 * left. A slot dims from the top while the pool is short of its cost and
 * clears as essence returns; it glows while a press is primed and flashes
 * once when it can be used again.
 */
public final class AbilityHudLayer implements LayeredDraw.Layer {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "ability_slot");

    static final int SIZE = 22;
    static final int BAR_WIDTH = 6;
    private static final int GAP = 4;
    private static final int SLOT_GAP = 2;
    private static final int OFFHAND_WIDTH = 29;
    private static final int READY_FLASH_TICKS = 10;
    private static final int SHADE = 0x90000000;
    private static final int PRIMED = 0xFF79DDF2;
    private static final int READY = 0xFFFFFFFF;
    static final int ESSENCE_TOP = 0xFFB98CF0;
    static final int ESSENCE_BOTTOM = 0xFF5B3A9A;
    private static final int COST_MARK = 0xFFF2EDF7;

    @Override
    public void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (minecraft.options.hideGui || player == null || player.isSpectator() || minecraft.level == null) {
            return;
        }
        AbilityStatusPayload status = ClientAbilityState.status();
        List<String> owned = ClientSoulState.latest().map(soul -> soul.abilityIds()).orElse(List.of());
        // The Soul snapshot is the source of truth for what the player holds; a reset hides the slots at once.
        if (!status.hasAbility() || !owned.contains(status.slots().get(0).abilityId())) {
            return;
        }

        boolean offhandShown = !player.getOffhandItem().isEmpty() && player.getMainArm() == HumanoidArm.RIGHT;
        int right = graphics.guiWidth() / 2 - 91 - GAP - (offhandShown ? OFFHAND_WIDTH : 0);
        int y = graphics.guiHeight() - SIZE - 1;
        long now = minecraft.level.getGameTime();
        float partialTick = delta.getGameTimeDeltaPartialTick(false);

        int x = right;
        for (int i = 0; i < status.slots().size(); i++) {
            x -= SIZE;
            renderSlot(graphics, minecraft.font, status.slots().get(i), i, x, y, now, partialTick);
            x -= SLOT_GAP;
        }
        renderEssence(graphics, status, x - BAR_WIDTH, y, now, partialTick);
    }

    private static void renderSlot(GuiGraphics graphics, Font font, AbilityStatusPayload.Slot slot, int index,
                                   int x, int y, long now, float partialTick) {
        SpellStyle.panel(graphics, x, y, x + SIZE, y + SIZE, border(slot, index, now, partialTick));
        AbilityIcons.draw(graphics, slot.abilityId(), x + 3, y + 3);

        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 200.0F);
        float shortfall = ClientAbilityState.shortfall(index, now, partialTick);
        if (shortfall > 0.0F) {
            int shadeBottom = y + 1 + Mth.ceil((SIZE - 2) * shortfall);
            graphics.fill(x + 1, y + 1, x + SIZE - 1, shadeBottom, SHADE);
        }
        KeyMapping key = ClientKeyMappings.abilityKey(index);
        if (key != null) {
            String label = key.getTranslatedKeyMessage().getString();
            label = label.length() <= 3 ? label : label.substring(0, 3);
            int colour = shortfall > 0.0F ? SpellStyle.MUTED : SpellStyle.TITLE;
            graphics.drawString(font, label, x + SIZE - 1 - font.width(label), y + SIZE - 9, colour, true);
        }
        graphics.pose().popPose();
    }

    /** A vertical bar that fills from the bottom, with a tick at the first slot's cost. */
    private static void renderEssence(GuiGraphics graphics, AbilityStatusPayload status, int x, int y,
                                      long now, float partialTick) {
        SpellStyle.panel(graphics, x, y, x + BAR_WIDTH, y + SIZE, SpellStyle.BORDER);
        int inner = SIZE - 2;
        int filled = Math.round(inner * ClientAbilityState.essenceFraction(now, partialTick));
        if (filled > 0) {
            graphics.fillGradient(x + 1, y + 1 + inner - filled, x + BAR_WIDTH - 1, y + 1 + inner,
                    ESSENCE_TOP, ESSENCE_BOTTOM);
        }
        int cost = status.slots().get(0).cost();
        if (status.capacity() > 0 && cost > 0 && cost < status.capacity()) {
            int markY = y + 1 + inner - Math.round(inner * (float) cost / status.capacity());
            graphics.fill(x + 1, markY, x + BAR_WIDTH - 1, markY + 1, SpellStyle.withAlpha(COST_MARK, 0.7F));
        }
    }

    private static int border(AbilityStatusPayload.Slot slot, int index, long now, float partialTick) {
        if (slot.primed()) {
            float pulse = 0.55F + 0.45F * Mth.sin((now + partialTick) * 0.3F);
            return SpellStyle.withAlpha(PRIMED, pulse);
        }
        long sinceReady = ClientAbilityState.ticksSinceAffordable(index, now);
        if (sinceReady >= 0 && sinceReady < READY_FLASH_TICKS) {
            float fade = Math.min(1.0F, (sinceReady + partialTick) / READY_FLASH_TICKS);
            return FastColor.ARGB32.lerp(fade, READY, SpellStyle.BORDER);
        }
        return SpellStyle.BORDER;
    }
}
