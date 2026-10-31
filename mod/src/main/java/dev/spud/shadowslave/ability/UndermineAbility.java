package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Saboteur, Dormant: set a slow charge on the block you look at. Ten seconds
 * later it goes: in the wild it cracks the stone around it; at an enemy's Link
 * Anchor it cuts the supply line; on an enemy Citadel's land it damages an
 * upgrade. A member of that clan standing over it stamps it out.
 */
public final class UndermineAbility implements AspectAbility {
    public static final ResourceLocation ID = AspectKitCatalog.SABOTEUR.abilityId();
    static final int ESSENCE_COST = 30;
    static final double REACH = 6.0;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return AspectKitCatalog.SABOTEUR.abilityName();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        if (SaboteurCraft.charge(player).isPresent()) {
            AbilityService.feedback(player, "Your charge is still burning.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        Vec3 eye = player.getEyePosition();
        BlockHitResult hit = player.level().clip(new ClipContext(eye, eye.add(player.getLookAngle().scale(REACH)),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.BLOCK) {
            AbilityService.feedback(player, "Look at the block to set your charge on.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        BlockPos pos = hit.getBlockPos();
        SaboteurCraft.plant(player, pos, hit.getDirection());
        ServerLevel level = player.serverLevel();
        level.playSound(null, pos, SoundEvents.TNT_PRIMED, SoundSource.PLAYERS, 0.6F, 1.2F);
        level.sendParticles(ParticleTypes.LAVA, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 4, 0.2, 0.0, 0.2, 0.0);
        AbilityService.feedback(player, "Charge set: it goes in " + SaboteurCraft.fuse(player) / 20 + " s.", ChatFormatting.GOLD);
        return Activation.USED;
    }

    @Override
    public void clear(ServerPlayer player) {
        SaboteurCraft.forget(player.getUUID());
    }

    @Override
    public boolean primed(ServerPlayer player) {
        return SaboteurCraft.charge(player).isPresent();
    }
}
