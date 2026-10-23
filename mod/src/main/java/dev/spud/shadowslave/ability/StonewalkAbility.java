package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * Delve, Awakened (escape): step straight through the wall in front of you,
 * up to {@link #MAX_DEPTH} blocks of it, to the first spot beyond with room
 * to stand. It needs a wall to start: you can't Stonewalk through open air.
 */
public final class StonewalkAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.STONEWALK.abilityId();
    static final int ESSENCE_COST = 30;
    static final int MAX_DEPTH = 8;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.STONEWALK.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Direction facing = player.getDirection();
        BlockPos feet = player.blockPosition();
        if (open(level, feet.relative(facing))) {
            AbilityService.feedback(player, "There is no stone in front of you to walk through.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        Optional<BlockPos> exit = exit(level, feet, facing);
        if (exit.isEmpty()) {
            AbilityService.feedback(player, "The stone runs too deep.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        BlockPos to = exit.get();
        for (int step = 1; step < feet.distManhattan(to); step++) {
            BlockPos through = feet.relative(facing, step);
            BlockState state = level.getBlockState(through);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), through.getX() + 0.5, through.getY() + 1.0,
                    through.getZ() + 0.5, 8, 0.3, 0.6, 0.3, 0.05);
        }
        player.teleportTo(to.getX() + 0.5, to.getY(), to.getZ() + 0.5);
        player.resetFallDistance();
        level.playSound(null, to, SoundEvents.ROOTED_DIRT_BREAK, SoundSource.PLAYERS, 1.0F, 0.6F);
        level.playSound(null, to, SoundEvents.STONE_STEP, SoundSource.PLAYERS, 1.0F, 0.5F);
        return Activation.USED;
    }

    /** The first spot ahead, past the wall, where a player's feet and head both fit. */
    static Optional<BlockPos> exit(ServerLevel level, BlockPos feet, Direction facing) {
        for (int step = 2; step <= MAX_DEPTH + 1; step++) {
            BlockPos at = feet.relative(facing, step);
            if (open(level, at) && open(level, at.above())) {
                return Optional.of(at);
            }
        }
        return Optional.empty();
    }

    static boolean open(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty() && level.getFluidState(pos).isEmpty();
    }
}
