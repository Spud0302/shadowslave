package dev.spud.shadowslave.citadel;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A Citadel's Gateway (CANON, ch. 23 and 358: Awakened anchor themselves to
 * a Citadel's Gateway, and an unclaimed one can be conquered). It is sealed
 * while the Guardian lives, open once the Guardian falls, and claimed once a
 * clan takes it. Using it does what its state allows: read who holds it,
 * claim it, or anchor to it and share the Field Guide board. It cannot be
 * broken.
 */
public final class GatewayBlock extends BaseEntityBlock {
    public static final MapCodec<GatewayBlock> CODEC = simpleCodec(GatewayBlock::new);
    public static final EnumProperty<State> STATE = EnumProperty.create("state", State.class);
    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 16, 15);

    /** What the Gateway shows: held by its Guardian, conquered and free to claim, or a clan's. */
    public enum State implements StringRepresentable {
        SEALED("sealed", 5),
        OPEN("open", 11),
        CLAIMED("claimed", 15);

        private final String name;
        private final int light;

        State(String name, int light) {
            this.name = name;
            this.light = light;
        }

        public int light() {
            return light;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public GatewayBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(STATE, State.SEALED));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STATE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GatewayBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, CitadelRegistry.GATEWAY_ENTITY.get(), GatewayBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            CitadelService.useGateway(serverPlayer, pos);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.7;
        double z = pos.getZ() + 0.5;
        switch (state.getValue(STATE)) {
            case SEALED -> {
                if (random.nextInt(3) == 0) {
                    level.addParticle(ParticleTypes.SMOKE, x + random.triangle(0, 0.3), y, z + random.triangle(0, 0.3), 0, 0.02, 0);
                }
            }
            case OPEN -> level.addParticle(ParticleTypes.REVERSE_PORTAL, x, y, z,
                    random.triangle(0, 0.5), 0.15, random.triangle(0, 0.5));
            case CLAIMED -> {
                level.addParticle(ParticleTypes.REVERSE_PORTAL, x, y, z, random.triangle(0, 0.4), 0.1, random.triangle(0, 0.4));
                if (random.nextInt(4) == 0) {
                    level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x + random.triangle(0, 0.3), pos.getY() + 1.05,
                            z + random.triangle(0, 0.3), 0, 0.03, 0);
                }
            }
        }
    }
}
