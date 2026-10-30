package dev.spud.shadowslave.creature;

import com.mojang.serialization.MapCodec;
import dev.spud.shadowslave.ShadowSlaveMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Black briar (DESIGN, the lore doc's Thorn Matron: "grows briar walls; fire
 * and severing vines counter it"). A thicket you cannot walk through, whose
 * thorns tear at anything but a Nightmare Creature pressed against it. Blades
 * and axes cut it fast, and it burns like dry brush. Briar a Thorn Matron
 * grows ({@link #WITHERING}) dies back on its own after
 * {@link #WITHER_TICKS}; briar that grows wild in the Dream Realm stays.
 */
public final class BlackBriarBlock extends Block {
    /** Its own way to die: "Steve was torn apart by black briars". */
    public static final ResourceKey<DamageType> DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "black_briar"));

    public static final MapCodec<BlackBriarBlock> CODEC = simpleCodec(BlackBriarBlock::new);
    public static final BooleanProperty WITHERING = BooleanProperty.create("withering");
    /** How long grown briar stands before it dies back. */
    public static final int WITHER_TICKS = 20 * 14;
    static final float THORN_DAMAGE = 2.0F;
    private static final VoxelShape COLLISION = Block.box(1.0, 0.0, 1.0, 15.0, 15.0, 15.0);

    public BlackBriarBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(WITHERING, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WITHERING);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        if (state.getValue(WITHERING) && !old.is(this)) {
            level.scheduleTick(pos, this, WITHER_TICKS + level.getRandom().nextInt(40));
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(WITHERING)) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), pos.getX() + 0.5, pos.getY() + 0.5,
                    pos.getZ() + 0.5, 12, 0.3, 0.3, 0.3, 0.05);
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_BREAK, SoundSource.BLOCKS, 0.6F, 0.6F);
            level.removeBlock(pos, false);
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity living && !(entity instanceof NightmareCreature)) {
            living.hurt(new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DAMAGE)), THORN_DAMAGE);
        }
    }

    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return true;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 100;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 60;
    }
}
