package dev.spud.shadowslave.fieldguide;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A Field Guide board (DESIGN, the Shared Field Guide plan: "a public lectern
 * or board where players post pages for everyone"). Use a Field Note on it to
 * pin the note up; use it with anything else to read every note pinned there
 * into your guide; sneak-use it to take back the newest note you pinned.
 * Unlike a clan's board in a Citadel, anyone may read it.
 */
public final class FieldBoardBlock extends BaseEntityBlock {
    public static final MapCodec<FieldBoardBlock> CODEC = simpleCodec(FieldBoardBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final int MAX_SHOWN = 4;
    /** How many notes the board shows pinned to it, up to {@link #MAX_SHOWN}. */
    public static final IntegerProperty NOTES = IntegerProperty.create("notes", 0, MAX_SHOWN);
    private static final VoxelShape X_SHAPE = Shapes.or(Block.box(6, 0, 0, 10, 16, 16));
    private static final VoxelShape Z_SHAPE = Shapes.or(Block.box(0, 0, 6, 16, 16, 10));

    public FieldBoardBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(NOTES, 0));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, NOTES);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? X_SHAPE : Z_SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FieldBoardBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        if (!stack.has(FieldGuideRegistry.NOTE_PAGE.get()) || player.isShiftKeyDown()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof FieldBoardBlockEntity board) {
            post(serverPlayer, board, stack);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof FieldBoardBlockEntity board) {
            if (player.isShiftKeyDown()) {
                takeBack(serverPlayer, board);
            } else {
                read(serverPlayer, board);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static boolean open(ServerPlayer player) {
        if (FieldGuideRules.publicBoards(player.server)) {
            return true;
        }
        player.displayClientMessage(Component.literal("Field Guide boards are closed on this server.").withStyle(ChatFormatting.GRAY), true);
        return false;
    }

    public static void post(ServerPlayer player, FieldBoardBlockEntity board, ItemStack stack) {
        if (!open(player)) {
            return;
        }
        if (board.notes().size() >= FieldGuideContent.get().settings().notes().boardCapacity()) {
            player.displayClientMessage(Component.literal("The board is full. Someone has to take a note down first.")
                    .withStyle(ChatFormatting.GRAY), true);
            return;
        }
        board.post(new FieldBoardBlockEntity.Posted(player.getUUID(), player.getGameProfile().getName(), stack.copyWithCount(1)));
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        player.displayClientMessage(Component.literal("You pin the note to the board for anyone to read.").withStyle(ChatFormatting.GOLD), true);
        player.serverLevel().playSound(null, board.getBlockPos(), SoundEvents.BOOK_PUT, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    public static void read(ServerPlayer player, FieldBoardBlockEntity board) {
        if (!open(player)) {
            return;
        }
        List<FieldBoardBlockEntity.Posted> notes = board.notes();
        if (notes.isEmpty()) {
            player.displayClientMessage(Component.literal("Nothing is pinned here yet. Use a Field Note on the board to post it for everyone.")
                    .withStyle(ChatFormatting.GRAY), true);
            return;
        }
        player.sendSystemMessage(Component.literal(notes.size() == 1 ? "One note is pinned to the board:" : notes.size() + " notes are pinned to the board:")
                .withStyle(ChatFormatting.GOLD));
        List<FieldGuideData> knowledge = new ArrayList<>();
        for (FieldBoardBlockEntity.Posted posted : notes) {
            FieldNotePage page = posted.note().get(FieldGuideRegistry.NOTE_PAGE.get());
            if (page == null) {
                continue;
            }
            knowledge.add(page.knowledge());
            String detail = page.lines().isEmpty() ? page.kind().label() : page.kind().label() + ", " + page.lines().get(0);
            player.sendSystemMessage(Component.literal(" • " + page.title()).withStyle(ChatFormatting.WHITE)
                    .append(Component.literal(" (" + detail + "), pinned by " + posted.posterName()).withStyle(ChatFormatting.GRAY)));
        }
        FieldGuideData.Learned learned = FieldNotes.learn(player, knowledge);
        player.sendSystemMessage(learned.any()
                ? Component.literal("Your Field Guide learns " + String.join(", ", learned.describe()) + ".").withStyle(ChatFormatting.GOLD)
                : Component.literal("Your Field Guide already knows everything here.").withStyle(ChatFormatting.GRAY));
        player.serverLevel().playSound(null, board.getBlockPos(), SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    public static void takeBack(ServerPlayer player, FieldBoardBlockEntity board) {
        Optional<FieldBoardBlockEntity.Posted> taken = board.takeBack(player.getUUID(), player.hasPermissions(2) && board.notes().stream()
                .noneMatch(posted -> posted.poster().equals(player.getUUID())));
        if (taken.isEmpty()) {
            player.displayClientMessage(Component.literal("None of the notes here are yours to take down.").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        player.getInventory().placeItemBackInInventory(taken.get().note());
        player.displayClientMessage(Component.literal("You take your note down from the board.").withStyle(ChatFormatting.GRAY), true);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof FieldBoardBlockEntity board) {
            for (ItemStack note : board.takeAll()) {
                Containers.dropItemStack(level, pos.getX(), pos.getY() + 0.5, pos.getZ(), note);
            }
        }
        super.onRemove(state, level, pos, newState, moved);
    }
}
