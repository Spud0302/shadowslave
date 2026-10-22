package dev.spud.shadowslave.gate;

import com.mojang.serialization.MapCodec;
import dev.spud.shadowslave.item.SoulShardItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

/**
 * Gate Lure (DESIGN, Jack 2026-10-22: Gates are summoned, so players choose
 * when to face one). Feed it soul shards: their rank and number set its pull,
 * and the pull sets the Category forecast. Use it empty-handed to read the
 * forecast; sneak and use it empty-handed to light it, which consumes the
 * Lure and starts a Gate where it stood. Faint shards from ordinary mobs
 * count a quarter. Broken, it keeps what it was fed.
 */
public final class GateLureBlock extends BaseEntityBlock {
    public static final MapCodec<GateLureBlock> CODEC = simpleCodec(GateLureBlock::new);
    /** How lit the runes are: 0 empty, 1 to 4 rising with the forecast Category. */
    public static final IntegerProperty CHARGE = IntegerProperty.create("charge", 0, 4);
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 15, 14);

    public GateLureBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(CHARGE, 0));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CHARGE);
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
        return new GateLureBlockEntity(pos, state);
    }

    /** Rune level for a pull: none below the minimum, then one per Category band, capped at 4. */
    static int chargeFor(int pull) {
        if (pull < GateForecast.MIN_PULL) {
            return 0;
        }
        return Math.min(4, GateForecast.fromPull(pull).likeliest());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        LureCharge charge = stack.getOrDefault(GateRegistry.LURE_CHARGE.get(), LureCharge.EMPTY);
        if (charge.shards() > 0) {
            level.setBlock(pos, state.setValue(CHARGE, chargeFor(charge.pull())), Block.UPDATE_ALL);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        LureCharge charge = stack.getOrDefault(GateRegistry.LURE_CHARGE.get(), LureCharge.EMPTY);
        if (charge.shards() == 0) {
            tooltip.add(Component.literal("Feed it soul shards to call a Gate").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.literal(charge.shards() + " shards · " + (charge.pull() < GateForecast.MIN_PULL
                    ? "too weak to call a Gate" : GateForecast.fromPull(charge.pull()).describeOdds()))
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        tooltip.add(Component.literal("Sneak and use it empty-handed to light it").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        if (!(stack.getItem() instanceof SoulShardItem shard)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!(level.getBlockEntity(pos) instanceof GateLureBlockEntity lure)) {
            return ItemInteractionResult.FAIL;
        }
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        if (lure.full()) {
            player.displayClientMessage(Component.literal("The Lure can hold no more.").withStyle(ChatFormatting.GRAY), true);
            return ItemInteractionResult.FAIL;
        }
        lure.feed(GateForecast.pullQuarters(shard.rank().tier(), SoulShardItem.isFaint(stack)));
        stack.consume(1, player);
        level.setBlock(pos, state.setValue(CHARGE, chargeFor(lure.pull())), Block.UPDATE_ALL);
        level.playSound(null, pos, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 1.0F, 0.6F + lure.shards() * 0.02F);
        ((ServerLevel) level).sendParticles(ParticleTypes.SOUL, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                6, 0.2, 0.2, 0.2, 0.02);
        showForecast(player, lure);
        return ItemInteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof GateLureBlockEntity lure)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!player.isSecondaryUseActive()) {
            showForecast(player, lure);
            return InteractionResult.CONSUME;
        }
        if (lure.pull() < GateForecast.MIN_PULL) {
            player.displayClientMessage(Component.literal("The Lure is too weak to call a Gate. Feed it more shards.")
                    .withStyle(ChatFormatting.GRAY), true);
            return InteractionResult.CONSUME;
        }
        BlockPos ground = pos.below();
        if (!GateService.canOpen((ServerLevel) level, ground)) {
            player.displayClientMessage(Component.literal("Another Gate is already open nearby.").withStyle(ChatFormatting.GRAY), true);
            return InteractionResult.CONSUME;
        }
        GateForecast forecast = GateForecast.fromPull(lure.pull());
        level.removeBlock(pos, false);
        GateService.open((ServerLevel) level, ground, GateTrigger.LURE, forecast);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(Component.literal("The Lure burns away. Something answers.")
                    .withStyle(ChatFormatting.DARK_PURPLE), true);
        }
        return InteractionResult.CONSUME;
    }

    private static void showForecast(Player player, GateLureBlockEntity lure) {
        Component text = lure.pull() < GateForecast.MIN_PULL
                ? Component.literal("Lure: " + lure.shards() + " shards · too weak to call a Gate")
                : Component.literal("Lure: " + lure.shards() + " shards · " + GateForecast.fromPull(lure.pull()).describeOdds());
        player.displayClientMessage(text.copy().withStyle(ChatFormatting.LIGHT_PURPLE), true);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        int charge = state.getValue(CHARGE);
        if (charge > 0 && random.nextInt(5 - charge + 1) == 0) {
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5 + random.triangle(0, 0.25),
                    pos.getY() + 0.95, pos.getZ() + 0.5 + random.triangle(0, 0.25), 0, 0.02, 0);
        }
        if (charge >= 3 && random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.REVERSE_PORTAL, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
                    random.triangle(0, 0.4), 0.1, random.triangle(0, 0.4));
        }
    }
}
