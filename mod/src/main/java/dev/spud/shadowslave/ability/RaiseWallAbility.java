package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Mason's Dormant ability: raise a 3x3 wall two blocks ahead from the blocks
 * in your hand, one item per block. Unlike Stone's Root in Stone it stays:
 * it is your wall. It only fills air and plants, never anything with a block
 * entity, and the blocks count as your own work for Far From Home.
 */
public final class RaiseWallAbility implements AspectAbility {
    public static final ResourceLocation ID = AspectKitCatalog.MASON.abilityId();
    static final int ESSENCE_COST = 20;
    static final int DISTANCE = 2;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return AspectKitCatalog.MASON.abilityName();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof BlockItem blockItem) || blockItem.getBlock() instanceof EntityBlock) {
            AbilityService.feedback(player, "Hold the blocks you want the wall made of.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        ServerLevel level = player.serverLevel();
        BlockState state = blockItem.getBlock().defaultBlockState();
        Direction facing = player.getDirection();
        Direction side = facing.getClockWise();
        BlockPos base = player.blockPosition().relative(facing, DISTANCE);
        int placed = 0;
        for (int up = 0; up <= 2; up++) {
            for (int across = -1; across <= 1; across++) {
                if (held.isEmpty()) {
                    break;
                }
                BlockPos pos = base.relative(side, across).above(up);
                if (!fits(level, pos, state)) {
                    continue;
                }
                level.setBlockAndUpdate(pos, state);
                MasonWorks.record(player, pos);
                held.consume(1, player);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), pos.getX() + 0.5, pos.getY() + 0.5,
                        pos.getZ() + 0.5, 5, 0.3, 0.3, 0.3, 0.05);
                placed++;
            }
        }
        if (placed == 0) {
            AbilityService.feedback(player, "There is no room for a wall there.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        level.playSound(null, base, state.getSoundType(level, base, player).getPlaceSound(), SoundSource.PLAYERS, 1.0F, 0.8F);
        return Activation.USED;
    }

    static boolean fits(ServerLevel level, BlockPos pos, BlockState state) {
        return level.getBlockState(pos).canBeReplaced() && level.getFluidState(pos).isEmpty()
                && level.getEntities(null, new AABB(pos)).isEmpty() && state.canSurvive(level, pos);
    }
}
