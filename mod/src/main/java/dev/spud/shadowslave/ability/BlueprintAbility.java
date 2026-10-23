package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mason, Awakened (craft): the first press learns the 5x5x5 shape in front
 * of you (free); the second press, within a minute, builds it where you look,
 * one block from your pack for each block of the shape. Blocks you don't
 * carry are skipped, nothing is overwritten, and essence is only spent on the
 * build. Block entities (chests, furnaces) are not copied.
 */
public final class BlueprintAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.BLUEPRINT.abilityId();
    static final int ESSENCE_COST = 50;
    static final int SIZE = 5;
    static final int HALF = SIZE / 2;
    static final int HOLD_TICKS = 20 * 60;
    static final double REACH = 24.0;

    record Piece(Vec3i offset, BlockState state) {
    }

    record Plan(ResourceKey<Level> dimension, List<Piece> pieces, long expiresAt) {
    }

    private static final Map<UUID, Plan> PLANS = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.BLUEPRINT.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        Plan plan = PLANS.get(player.getUUID());
        return plan == null ? learn(player) : build(player, plan);
    }

    private static Activation learn(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos corner = learnCorner(player);
        List<Piece> pieces = new ArrayList<>();
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                for (int z = 0; z < SIZE; z++) {
                    BlockPos pos = corner.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (!state.isAir() && !state.hasBlockEntity() && state.getFluidState().isEmpty()
                            && state.getBlock().asItem() instanceof BlockItem) {
                        pieces.add(new Piece(new Vec3i(x, y, z), state));
                    }
                }
            }
        }
        if (pieces.isEmpty()) {
            AbilityService.feedback(player, "There is nothing built in front of you to learn.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        PLANS.put(player.getUUID(), new Plan(level.dimension(), List.copyOf(pieces), level.getGameTime() + HOLD_TICKS));
        outline(level, corner);
        level.playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 0.8F);
        AbilityService.feedback(player, "Blueprint of " + pieces.size() + " blocks learned. Look where to build and press again.",
                ChatFormatting.GOLD);
        return Activation.PRIMED;
    }

    private Activation build(ServerPlayer player, Plan plan) {
        ServerLevel level = player.serverLevel();
        if (!level.dimension().equals(plan.dimension())) {
            PLANS.remove(player.getUUID());
            AbilityService.feedback(player, "The blueprint fades between worlds.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        if (!EssenceService.covers(player, AbilityService.costFor(player, this))) {
            AbilityService.feedback(player, displayName() + " needs " + AbilityService.costFor(player, this) + " essence to build",
                    ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        HitResult hit = player.pick(REACH, 1.0F, false);
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) {
            AbilityService.feedback(player, "Look at the ground where it should stand.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        BlockPos corner = blockHit.getBlockPos().relative(blockHit.getDirection()).offset(-HALF, 0, -HALF);
        int placed = 0;
        int missing = 0;
        for (Piece piece : plan.pieces()) {
            BlockPos pos = corner.offset(piece.offset());
            if (!RaiseWallAbility.fits(level, pos, piece.state())) {
                continue;
            }
            if (!takeOne(player, piece.state())) {
                missing++;
                continue;
            }
            level.setBlockAndUpdate(pos, piece.state());
            MasonWorks.record(player, pos);
            placed++;
        }
        if (placed == 0) {
            AbilityService.feedback(player, missing > 0 ? "You carry none of the blocks this blueprint needs."
                    : "There is no room to build it there.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        PLANS.remove(player.getUUID());
        outline(level, corner);
        level.playSound(null, corner.offset(HALF, 0, HALF), SoundEvents.STONE_PLACE, SoundSource.PLAYERS, 1.0F, 0.7F);
        level.playSound(null, corner.offset(HALF, 0, HALF), SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.4F, 1.6F);
        AbilityService.feedback(player, "Built " + placed + " blocks" + (missing > 0 ? ", " + missing + " missing from your pack" : ""),
                ChatFormatting.GOLD);
        return Activation.USED;
    }

    /** The near-bottom corner of the box learned: centred ahead of the player, its front face one block away. */
    static BlockPos learnCorner(ServerPlayer player) {
        var facing = player.getDirection();
        BlockPos center = player.blockPosition().relative(facing, HALF + 1);
        return center.offset(-HALF, 0, -HALF);
    }

    /** Takes one matching block item from the pack; free in creative. */
    static boolean takeOne(ServerPlayer player, BlockState state) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.getItem() instanceof BlockItem item && item.getBlock() == state.getBlock()) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    private static void outline(ServerLevel level, BlockPos corner) {
        for (int i = 0; i <= SIZE; i++) {
            for (int[] edge : new int[][]{{i, 0, 0}, {i, 0, SIZE}, {0, 0, i}, {SIZE, 0, i}, {i, SIZE, 0}, {i, SIZE, SIZE},
                    {0, SIZE, i}, {SIZE, SIZE, i}, {0, i, 0}, {SIZE, i, 0}, {0, i, SIZE}, {SIZE, i, SIZE}}) {
                Vec3 at = Vec3.atLowerCornerOf(corner).add(edge[0], edge[1], edge[2]);
                level.sendParticles(ParticleTypes.END_ROD, at.x, at.y, at.z, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    public void tick(ServerPlayer player) {
        Plan plan = PLANS.get(player.getUUID());
        if (plan != null && plan.expiresAt() <= player.serverLevel().getGameTime()) {
            PLANS.remove(player.getUUID());
            AbilityService.feedback(player, "The blueprint fades from your mind.", ChatFormatting.GRAY);
            AbilityService.syncStatus(player);
        }
    }

    @Override
    public void clear(ServerPlayer player) {
        PLANS.remove(player.getUUID());
    }

    @Override
    public boolean primed(ServerPlayer player) {
        return PLANS.containsKey(player.getUUID());
    }

    static int learned(ServerPlayer player) {
        Plan plan = PLANS.get(player.getUUID());
        return plan == null ? 0 : plan.pieces().size();
    }
}
