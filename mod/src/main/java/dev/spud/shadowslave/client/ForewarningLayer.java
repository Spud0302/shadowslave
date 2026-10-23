package dev.spud.shadowslave.client;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.network.payload.ForewarningPayload;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

/**
 * Seer's Forewarning on screen: a violet glow along the screen's edges and a
 * marker around the crosshair pointing at the creature winding up, for as
 * long as the windup lasts. A warning passed on by Shared Sight looks the same
 * in a paler shade.
 */
public final class ForewarningLayer implements LayeredDraw.Layer {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "forewarning");
    static final int OWN = 0xB46CF0;
    static final int SHARED = 0xD6C2F5;
    static final int EDGE = 28;
    static final int MARKER_RADIUS = 40;
    /** The flash lingers a little past the hit so it is not gone before you react. */
    static final int LINGER_TICKS = 8;

    private static int attackerId = -1;
    private static long startedAt;
    private static long until;
    private static boolean shared;

    static void warn(ForewarningPayload payload, long now) {
        attackerId = payload.attackerId();
        startedAt = now;
        until = now + payload.ticks() + LINGER_TICKS;
        shared = payload.shared();
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BELL.value(), shared ? 1.6F : 1.2F, 0.6F));
    }

    /** How strong the flash is now: bright at first, pulsing, fading at the end; 0 when there is none. */
    static float strength(long now, float partialTick, long startedAt, long until) {
        float time = now + partialTick;
        if (time >= until || time < startedAt) {
            return 0.0F;
        }
        float rise = Mth.clamp((time - startedAt) / 3.0F, 0.0F, 1.0F);
        float fall = Mth.clamp((until - time) / LINGER_TICKS, 0.0F, 1.0F);
        float pulse = 0.75F + 0.25F * Mth.sin(time * 1.2F);
        return rise * fall * pulse;
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (minecraft.options.hideGui || player == null || minecraft.level == null) {
            return;
        }
        long now = minecraft.level.getGameTime();
        float strength = strength(now, delta.getGameTimeDeltaPartialTick(false), startedAt, until);
        if (strength <= 0.0F) {
            return;
        }
        int rgb = shared ? SHARED : OWN;
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        int solid = ((int) (strength * 150) << 24) | rgb;
        int clear = rgb;
        graphics.fillGradient(0, 0, width, EDGE, solid, clear);
        graphics.fillGradient(0, height - EDGE, width, height, clear, solid);
        sideGradient(graphics, 0, EDGE, height, solid, clear);
        sideGradient(graphics, width - EDGE, width, height, clear, solid);

        Entity attacker = minecraft.level.getEntity(attackerId);
        if (attacker == null) {
            return;
        }
        double dx = attacker.getX() - player.getX();
        double dz = attacker.getZ() - player.getZ();
        float yawTo = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
        float offset = Mth.wrapDegrees(yawTo - player.getYRot()) * Mth.DEG_TO_RAD;
        int cx = width / 2 + Math.round(Mth.sin(offset) * MARKER_RADIUS);
        int cy = height / 2 - Math.round(Mth.cos(offset) * MARKER_RADIUS);
        int marker = ((int) (Math.min(1.0F, strength * 1.4F) * 255) << 24) | rgb;
        for (int row = 0; row < 13; row++) {
            int half = row < 7 ? row : 12 - row;
            graphics.fill(cx - half - 1, cy - 6 + row, cx + half + 2, cy - 5 + row, marker);
        }
        graphics.fill(cx, cy - 4, cx + 1, cy + 2, 0xFF1A0E26);
        graphics.fill(cx, cy + 3, cx + 1, cy + 5, 0xFF1A0E26);
    }

    /** A left-to-right gradient strip, drawn as thin columns since GuiGraphics only fades top to bottom. */
    private static void sideGradient(GuiGraphics graphics, int x0, int x1, int height, int left, int right) {
        int columns = x1 - x0;
        for (int i = 0; i < columns; i++) {
            float t = columns <= 1 ? 0.0F : (float) i / (columns - 1);
            int alpha = Math.round(Mth.lerp(t, left >>> 24, right >>> 24));
            graphics.fill(x0 + i, 0, x0 + i + 1, height, (alpha << 24) | (left & 0xFFFFFF));
        }
    }
}
