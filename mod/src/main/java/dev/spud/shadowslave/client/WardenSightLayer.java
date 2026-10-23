package dev.spud.shadowslave.client;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.creature.NightmareCreature;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Warden's Gate Sense, second half: looking at a Nightmare Creature shows its
 * Rank and Class and how much health it has left, just under the crosshair.
 * Everyone else has to learn a creature by fighting it.
 */
public final class WardenSightLayer implements LayeredDraw.Layer {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "warden_sight");
    static final double SIGHT_RANGE = 32.0;
    private static final String INNATE = AspectKitCatalog.WARDEN.innateId().toString();
    private static final int RANK_COLOUR = 0xFFB4E68C;

    @Override
    public void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (minecraft.options.hideGui || player == null || minecraft.level == null || minecraft.screen != null) {
            return;
        }
        List<String> owned = ClientSoulState.latest().map(soul -> soul.abilityIds()).orElse(List.of());
        if (!owned.contains(INNATE)) {
            return;
        }
        NightmareCreature creature = lookedAt(player, delta.getGameTimeDeltaPartialTick(false));
        if (creature == null) {
            return;
        }
        String rank = creature.rankAndClassName().getString();
        String health = Math.round(creature.getHealth()) + " / " + Math.round(creature.getMaxHealth());
        int cx = graphics.guiWidth() / 2;
        int y = graphics.guiHeight() / 2 + 10;
        int width = Math.max(minecraft.font.width(rank), minecraft.font.width(health)) + 8;
        graphics.fill(cx - width / 2, y - 2, cx + width / 2, y + 20, 0x90000000);
        graphics.drawCenteredString(minecraft.font, rank, cx, y, RANK_COLOUR);
        graphics.drawCenteredString(minecraft.font, health, cx, y + 10, SpellStyle.MUTED);
    }

    private static NightmareCreature lookedAt(LocalPlayer player, float partialTick) {
        Vec3 eye = player.getEyePosition(partialTick);
        Vec3 end = eye.add(player.getViewVector(partialTick).scale(SIGHT_RANGE));
        HitResult block = player.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (block.getType() != HitResult.Type.MISS) {
            end = block.getLocation();
        }
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player.level(), player, eye, end,
                player.getBoundingBox().expandTowards(end.subtract(eye)).inflate(1.0),
                entity -> entity instanceof NightmareCreature creature && creature.isAlive());
        return hit != null ? (NightmareCreature) hit.getEntity() : null;
    }
}
