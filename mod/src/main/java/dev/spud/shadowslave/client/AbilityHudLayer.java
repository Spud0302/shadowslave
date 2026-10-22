package dev.spud.shadowslave.client;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.network.payload.AbilityStatusPayload;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

/**
 * The Aspect ability slot left of the hotbar: the ability's icon, a sweep that
 * drains as the cooldown runs, the seconds left, and the key that fires it.
 * It glows while a press is primed and flashes once when the ability is ready.
 */
public final class AbilityHudLayer implements LayeredDraw.Layer {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "ability_slot");

    static final int SIZE = 22;
    private static final int GAP = 4;
    private static final int OFFHAND_WIDTH = 29;
    private static final int READY_FLASH_TICKS = 10;
    private static final int SHADE = 0x90000000;
    private static final int PRIMED = 0xFF79DDF2;
    private static final int READY = 0xFFFFFFFF;

    @Override
    public void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (minecraft.options.hideGui || player == null || player.isSpectator() || minecraft.level == null) {
            return;
        }
        AbilityStatusPayload status = ClientAbilityState.status();
        // The Soul snapshot is the source of truth for what the player holds; a reset hides the slot at once.
        boolean held = status.hasAbility() && ClientSoulState.latest()
                .map(soul -> soul.abilityIds().contains(status.abilityId()))
                .orElse(false);
        if (!held) {
            return;
        }

        boolean offhandShown = !player.getOffhandItem().isEmpty() && player.getMainArm() == HumanoidArm.RIGHT;
        int x = graphics.guiWidth() / 2 - 91 - GAP - SIZE - (offhandShown ? OFFHAND_WIDTH : 0);
        int y = graphics.guiHeight() - SIZE - 1;
        long now = minecraft.level.getGameTime();
        float partialTick = delta.getGameTimeDeltaPartialTick(false);

        SpellStyle.panel(graphics, x, y, x + SIZE, y + SIZE, border(status, now, partialTick));
        graphics.renderItem(AbilityIcons.forAbility(status.abilityId()), x + 3, y + 3);

        Font font = minecraft.font;
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 200.0F);
        float fraction = ClientAbilityState.cooldownFraction(now, partialTick);
        if (fraction > 0.0F) {
            int shadeTop = y + 1 + Mth.floor((SIZE - 2) * (1.0F - fraction));
            graphics.fill(x + 1, shadeTop, x + SIZE - 1, y + SIZE - 1, SHADE);
            String seconds = Long.toString((ClientAbilityState.remainingTicks(now) + 19) / 20);
            graphics.drawString(font, seconds, x + (SIZE - font.width(seconds)) / 2 + 1, y + 7, SpellStyle.TEXT, true);
        } else {
            Component key = ClientKeyMappings.USE_ABILITY.getTranslatedKeyMessage();
            String label = key.getString().length() <= 3 ? key.getString() : key.getString().substring(0, 3);
            graphics.drawString(font, label, x + SIZE - 1 - font.width(label), y + SIZE - 9, SpellStyle.TITLE, true);
        }
        graphics.pose().popPose();
    }

    private static int border(AbilityStatusPayload status, long now, float partialTick) {
        if (status.primed()) {
            float pulse = 0.55F + 0.45F * Mth.sin((now + partialTick) * 0.3F);
            return SpellStyle.withAlpha(PRIMED, pulse);
        }
        long sinceReady = ClientAbilityState.ticksSinceReady(now);
        if (sinceReady >= 0 && sinceReady < READY_FLASH_TICKS) {
            float fade = Math.min(1.0F, (sinceReady + partialTick) / READY_FLASH_TICKS);
            return FastColor.ARGB32.lerp(fade, READY, SpellStyle.BORDER);
        }
        return SpellStyle.BORDER;
    }
}
