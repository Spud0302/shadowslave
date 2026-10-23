package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Delve's Dormant ability: one blow breaks a tunnel three blocks wide, three
 * high and three deep in the direction you face (straight up or down when you
 * look steeply). Blocks break as if you mined them yourself, so what drops
 * depends on the tool in your hand, and nothing harder than {@link #MAX_HARDNESS}
 * or holding a block entity is touched. Delve's way out of a bad fight.
 */
public final class TunnelStrikeAbility implements AspectAbility {
    public static final ResourceLocation ID = AspectKitCatalog.DELVE.abilityId();
    static final int ESSENCE_COST = 20;
    static final int DEPTH = 3;
    static final float MAX_HARDNESS = 5.0F;
    static final float STEEP_PITCH = 50.0F;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return AspectKitCatalog.DELVE.abilityName();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        int broken = 0;
        for (BlockPos pos : tunnel(player)) {
            if (breakable(level, pos) && player.gameMode.destroyBlock(pos)) {
                broken++;
            }
        }
        if (broken == 0) {
            AbilityService.feedback(player, "Nothing here gives way to your strike.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.5F, 1.4F);
        level.playSound(null, player.blockPosition(), SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 1.0F, 0.6F);
        return Activation.USED;
    }

    /** The 27 blocks the strike reaches, nearest layer first. */
    static List<BlockPos> tunnel(ServerPlayer player) {
        Direction facing = facing(player.getXRot(), player.getDirection());
        BlockPos start = facing == Direction.DOWN ? player.blockPosition()
                : facing == Direction.UP ? player.blockPosition().above() : player.blockPosition().above();
        Direction across = facing.getAxis() == Direction.Axis.Y ? Direction.EAST : facing.getClockWise();
        Direction second = facing.getAxis() == Direction.Axis.Y ? Direction.SOUTH : Direction.UP;
        List<BlockPos> blocks = new ArrayList<>();
        for (int depth = 1; depth <= DEPTH; depth++) {
            BlockPos center = start.relative(facing, depth);
            for (int a = -1; a <= 1; a++) {
                for (int b = -1; b <= 1; b++) {
                    blocks.add(center.relative(across, a).relative(second, b));
                }
            }
        }
        return blocks;
    }

    /** Down or up when looking steeply, otherwise the horizontal way the player faces. */
    static Direction facing(float pitch, Direction horizontal) {
        if (pitch >= STEEP_PITCH) {
            return Direction.DOWN;
        }
        if (pitch <= -STEEP_PITCH) {
            return Direction.UP;
        }
        return horizontal;
    }

    static boolean breakable(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        float hardness = state.getDestroySpeed(level, pos);
        return !state.isAir() && hardness >= 0.0F && hardness <= MAX_HARDNESS && !state.hasBlockEntity()
                && state.getFluidState().isEmpty();
    }
}
