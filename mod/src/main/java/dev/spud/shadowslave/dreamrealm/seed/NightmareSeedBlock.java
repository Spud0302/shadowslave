package dev.spud.shadowslave.dreamrealm.seed;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

/**
 * A Nightmare Seed growing in the Dream Realm (DESIGN, after ch. 458-459).
 * The block only shows the Seed: its growth, region and owner live in
 * {@link SeedData}, so a Seed keeps growing while its chunk is unloaded.
 * Using it reads the Seed; sneak-using it challenges it.
 */
public final class NightmareSeedBlock extends Block {
    public static final MapCodec<NightmareSeedBlock> CODEC = simpleCodec(NightmareSeedBlock::new);
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 3);
    private static final VoxelShape[] SHAPES = {
            box(5, 0, 5, 11, 7, 11),
            box(4, 0, 4, 12, 10, 12),
            box(3, 0, 3, 13, 13, 13),
            box(2, 0, 2, 14, 16, 14)
    };
    private static final DustParticleOptions SPORE = new DustParticleOptions(new Vector3f(0.55F, 0.12F, 0.75F), 1.0F);

    public NightmareSeedBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(STAGE, 0));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(STAGE)];
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player instanceof ServerPlayer serverPlayer) {
            if (player.isShiftKeyDown()) {
                SeedService.challenge(serverPlayer, pos);
            } else {
                SeedService.describe(serverPlayer, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        int stage = state.getValue(STAGE);
        for (int i = 0; i <= stage; i++) {
            if (random.nextInt(3) != 0) {
                continue;
            }
            double spread = 0.25 + 0.12 * stage;
            level.addParticle(SPORE,
                    pos.getX() + 0.5 + (random.nextDouble() - 0.5) * spread * 2,
                    pos.getY() + 0.2 + random.nextDouble() * (0.4 + 0.2 * stage),
                    pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * spread * 2,
                    0.0, 0.0, 0.0);
        }
        if (stage >= 2 && random.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.REVERSE_PORTAL, pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5,
                    (random.nextDouble() - 0.5) * 0.1, 0.05 + 0.03 * stage, (random.nextDouble() - 0.5) * 0.1);
        }
        if (stage == 3 && random.nextInt(10) == 0) {
            level.addParticle(ParticleTypes.SCULK_SOUL, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0.0, 0.04, 0.0);
        }
    }
}
