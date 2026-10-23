package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
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

import java.util.Optional;

/**
 * Gloam, Awakened (movement): step through the shadows to the spot you look
 * at, up to {@link #REACH} blocks away, as long as it is dim or dark there.
 * Bright spots refuse you and the step costs nothing.
 */
public final class ShadeStepAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.SHADE_STEP.abilityId();
    static final int ESSENCE_COST = 20;
    static final double REACH = 24.0;
    /** The darkest a landing spot may be lit: anything up to this is shadow enough. */
    static final int MAX_LIGHT = 7;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.SHADE_STEP.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        Optional<Vec3> landing = landing(player);
        if (landing.isEmpty()) {
            AbilityService.feedback(player, "There is no ground to step to there.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        ServerLevel level = player.serverLevel();
        Vec3 to = landing.get();
        if (level.getMaxLocalRawBrightness(BlockPos.containing(to.add(0, 0.5, 0))) > MAX_LIGHT) {
            AbilityService.feedback(player, "That spot is too bright to step into.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        level.sendParticles(ParticleTypes.SQUID_INK, player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.3, 0.6, 0.3, 0.02);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, player.getX(), player.getY() + 1.0, player.getZ(), 12, 0.3, 0.6, 0.3, 0.01);
        player.teleportTo(to.x, to.y, to.z);
        player.resetFallDistance();
        level.sendParticles(ParticleTypes.SQUID_INK, to.x, to.y + 1.0, to.z, 30, 0.3, 0.6, 0.3, 0.02);
        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.6F, 0.5F);
        return Activation.USED;
    }

    /** Where the step lands: on top of the block looked at, or on the ground below the spot looked at. */
    static Optional<Vec3> landing(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        BlockHitResult hit = level.clip(new ClipContext(eye, eye.add(look.scale(REACH)), ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, player));
        if (hit.getType() == HitResult.Type.MISS) {
            return Optional.empty();
        }
        // Back off half a block from the face so the search starts in open air.
        BlockPos start = BlockPos.containing(hit.getLocation().subtract(look.scale(0.5)));
        for (int down = 0; down <= 4; down++) {
            BlockPos feet = start.below(down);
            if (!level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), net.minecraft.core.Direction.UP)) {
                continue;
            }
            Vec3 spot = Vec3.atBottomCenterOf(feet);
            if (level.noCollision(player, player.getDimensions(player.getPose()).makeBoundingBox(spot))) {
                return Optional.of(spot);
            }
            return Optional.empty();
        }
        return Optional.empty();
    }
}
