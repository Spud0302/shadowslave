package dev.spud.shadowslave.citadel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A Citadel's Link Anchor (DESIGN, Run I): the pylon outside the walls that
 * holds its supply line once its Gateway Links are built. An enemy at war who
 * stands at it long enough, unanswered, cuts it, and the Gateway starves until
 * a member mends it here. It cannot be broken.
 */
public final class LinkAnchorBlock extends Block {
    public static final BooleanProperty CUT = BooleanProperty.create("cut");
    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 16, 15);

    public LinkAnchorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(CUT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CUT);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            CitadelWars.useAnchor(serverPlayer, pos);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5;
        double z = pos.getZ() + 0.5;
        if (state.getValue(CUT)) {
            if (random.nextInt(2) == 0) {
                level.addParticle(ParticleTypes.LARGE_SMOKE, x + random.triangle(0, 0.2), pos.getY() + 1.1, z + random.triangle(0, 0.2), 0, 0.03, 0);
            }
            return;
        }
        level.addParticle(ParticleTypes.REVERSE_PORTAL, x, pos.getY() + 1.1, z, random.triangle(0, 0.3), 0.12, random.triangle(0, 0.3));
        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x + random.triangle(0, 0.15), pos.getY() + 1.25, z + random.triangle(0, 0.15), 0, 0.04, 0);
        }
    }
}
