package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stone, Awakened (shelter): raise a 3x3 wall of stone two blocks ahead. It
 * only fills air and plants, never breaks anything, and crumbles back to air
 * after a while. Blocks someone has since changed are left alone.
 */
public final class RootInStoneAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.ROOT_IN_STONE.abilityId();
    static final int ESSENCE_COST = 35;
    static final int DURATION_TICKS = 20 * 12;
    static final int DISTANCE = 2;

    record Wall(ResourceKey<Level> dimension, List<BlockPos> blocks, long crumblesAt) {
    }

    private static final Map<UUID, Wall> WALLS = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.ROOT_IN_STONE.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockState stone = Blocks.STONE.defaultBlockState();
        crumble(player);
        Direction facing = player.getDirection();
        Direction side = facing.getClockWise();
        BlockPos base = player.blockPosition().relative(facing, DISTANCE);
        List<BlockPos> placed = new ArrayList<>();
        for (int across = -1; across <= 1; across++) {
            for (int up = 0; up <= 2; up++) {
                BlockPos pos = base.relative(side, across).above(up);
                BlockState there = level.getBlockState(pos);
                if (!there.canBeReplaced() || !level.getFluidState(pos).isEmpty()
                        || !level.getEntities(null, new AABB(pos)).isEmpty()) {
                    continue;
                }
                level.setBlockAndUpdate(pos, stone);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, stone), pos.getX() + 0.5, pos.getY() + 0.5,
                        pos.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.05);
                placed.add(pos.immutable());
            }
        }
        if (placed.isEmpty()) {
            AbilityService.feedback(player, "There is no room for stone to rise.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        WALLS.put(player.getUUID(), new Wall(level.dimension(), List.copyOf(placed), level.getGameTime() + DURATION_TICKS));
        level.playSound(null, base, SoundEvents.STONE_PLACE, SoundSource.PLAYERS, 1.0F, 0.6F);
        level.playSound(null, base, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.3F, 1.6F);
        return Activation.USED;
    }

    @Override
    public void tick(ServerPlayer player) {
        Wall wall = WALLS.get(player.getUUID());
        if (wall != null && wall.crumblesAt() <= player.serverLevel().getGameTime()) {
            crumble(player);
        }
    }

    @Override
    public void clear(ServerPlayer player) {
        crumble(player);
    }

    private static void crumble(ServerPlayer player) {
        Wall wall = WALLS.remove(player.getUUID());
        if (wall == null || player.getServer() == null) {
            return;
        }
        ServerLevel level = player.getServer().getLevel(wall.dimension());
        if (level == null) {
            return;
        }
        for (BlockPos pos : wall.blocks()) {
            if (level.isLoaded(pos) && level.getBlockState(pos).is(Blocks.STONE)) {
                level.removeBlock(pos, false);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), pos.getX() + 0.5,
                        pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.3, 0.3, 0.3, 0.05);
            }
        }
    }

    static List<BlockPos> wallOf(ServerPlayer player) {
        Wall wall = WALLS.get(player.getUUID());
        return wall == null ? List.of() : wall.blocks();
    }
}
