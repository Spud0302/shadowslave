package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Delve, Awakened (craft): tear out the whole vein of the ore you look at,
 * up to {@link #MAX_BLOCKS} touching blocks of the same ore, and pull what
 * drops straight into your pack. You still need a tool that can mine it, and
 * the tool wears for every block.
 */
public final class VeinPullAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.VEIN_PULL.abilityId();
    static final int ESSENCE_COST = 40;
    static final int MAX_BLOCKS = 32;
    static final double REACH = 6.0;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.VEIN_PULL.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        HitResult hit = player.pick(REACH, 1.0F, false);
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK
                || !level.getBlockState(blockHit.getBlockPos()).is(Tags.Blocks.ORES)) {
            AbilityService.feedback(player, "Look at an ore to pull its vein.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        BlockState ore = level.getBlockState(blockHit.getBlockPos());
        ItemStack tool = player.getMainHandItem();
        if (!player.hasCorrectToolForDrops(ore)) {
            AbilityService.feedback(player, "Your tool can't pull this ore. Hold one that can mine it.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        List<BlockPos> vein = vein(level, blockHit.getBlockPos(), ore.getBlock(), MAX_BLOCKS);
        int xp = 0;
        for (BlockPos pos : vein) {
            BlockState state = level.getBlockState(pos);
            for (ItemStack drop : Block.getDrops(state, level, pos, null, player, tool)) {
                player.getInventory().placeItemBackInInventory(drop);
            }
            xp += state.getExpDrop(level, pos, null, player, tool);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), pos.getX() + 0.5, pos.getY() + 0.5,
                    pos.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.05);
            AbilityTargets.beam(level, ParticleTypes.WAX_OFF, pos.getCenter(), player.position().add(0, 1.0, 0));
            level.removeBlock(pos, false);
        }
        if (xp > 0) {
            ExperienceOrb.award(level, player.position(), xp);
        }
        if (!player.getAbilities().instabuild) {
            tool.hurtAndBreak(vein.size(), player, EquipmentSlot.MAINHAND);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.DEEPSLATE_BREAK, SoundSource.PLAYERS, 1.0F, 0.7F);
        level.playSound(null, player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.6F, 0.8F);
        AbilityService.feedback(player, "Pulled a vein of " + vein.size(), ChatFormatting.GOLD);
        return Activation.USED;
    }

    /** Blocks of {@code ore} touching the start, edges and corners included, nearest first, at most {@code max}. */
    static List<BlockPos> vein(ServerLevel level, BlockPos start, Block ore, int max) {
        List<BlockPos> found = new ArrayList<>();
        Set<BlockPos> seen = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start.immutable());
        seen.add(start.immutable());
        while (!queue.isEmpty() && found.size() < max) {
            BlockPos pos = queue.poll();
            found.add(pos);
            for (BlockPos next : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
                BlockPos fixed = next.immutable();
                if (!seen.contains(fixed) && level.getBlockState(fixed).is(ore)) {
                    seen.add(fixed);
                    queue.add(fixed);
                }
            }
        }
        return found;
    }
}
