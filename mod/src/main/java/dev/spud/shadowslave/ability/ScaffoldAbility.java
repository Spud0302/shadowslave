package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.core.BlockPos;
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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mason, Awakened (movement): for ten seconds, a plank of bamboo mosaic
 * appears under your feet whenever you step out over a gap or a drop, so
 * you can walk across it. Sneak to stop it catching you. Each plank
 * falls away {@link #PLANK_TICKS} after it was laid.
 */
public final class ScaffoldAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.SCAFFOLD.abilityId();
    static final int ESSENCE_COST = 25;
    static final int DURATION_TICKS = 20 * 10;
    static final int PLANK_TICKS = 20 * 8;
    static final int MAX_PLANKS = 64;

    record Plank(ResourceKey<Level> dimension, BlockPos pos, long fallsAt) {
    }

    private static final Map<UUID, Long> ACTIVE_UNTIL = new ConcurrentHashMap<>();
    private static final Map<UUID, Deque<Plank>> PLANKS = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.SCAFFOLD.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        ACTIVE_UNTIL.put(player.getUUID(), level.getGameTime() + DURATION_TICKS);
        level.playSound(null, player.blockPosition(), SoundEvents.BAMBOO_WOOD_PLACE, SoundSource.PLAYERS, 1.0F, 0.8F);
        lay(player);
        return Activation.USED;
    }

    @Override
    public void tick(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        long now = level.getGameTime();
        Long until = ACTIVE_UNTIL.get(player.getUUID());
        if (until != null) {
            if (until <= now) {
                ACTIVE_UNTIL.remove(player.getUUID());
                AbilityService.syncStatus(player);
            } else if (!player.isShiftKeyDown()) {
                lay(player);
            }
        }
        Deque<Plank> planks = PLANKS.get(player.getUUID());
        while (planks != null && !planks.isEmpty() && planks.peekFirst().fallsAt() <= now) {
            fall(player, planks.pollFirst());
        }
    }

    private static void lay(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos below = BlockPos.containing(player.getX(), player.getY() - 0.5, player.getZ());
        // Only over a real gap: two blocks of air below, so a jump on flat ground doesn't leave a plank.
        if (!level.getBlockState(below).isAir() || !level.getBlockState(below.below()).isAir()
                || !level.getFluidState(below).isEmpty()) {
            return;
        }
        Deque<Plank> planks = PLANKS.computeIfAbsent(player.getUUID(), id -> new ArrayDeque<>());
        if (planks.size() >= MAX_PLANKS) {
            return;
        }
        BlockState plank = Blocks.BAMBOO_MOSAIC.defaultBlockState();
        level.setBlockAndUpdate(below, plank);
        planks.addLast(new Plank(level.dimension(), below.immutable(), level.getGameTime() + PLANK_TICKS));
        player.resetFallDistance();
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, plank), below.getX() + 0.5, below.getY() + 1.0,
                below.getZ() + 0.5, 4, 0.3, 0.05, 0.3, 0.02);
        level.playSound(null, below, SoundEvents.BAMBOO_WOOD_PLACE, SoundSource.PLAYERS, 0.5F, 1.2F);
    }

    private static void fall(ServerPlayer player, Plank plank) {
        ServerLevel level = player.getServer() == null ? null : player.getServer().getLevel(plank.dimension());
        if (level != null && level.isLoaded(plank.pos()) && level.getBlockState(plank.pos()).is(Blocks.BAMBOO_MOSAIC)) {
            level.removeBlock(plank.pos(), false);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BAMBOO_MOSAIC.defaultBlockState()),
                    plank.pos().getX() + 0.5, plank.pos().getY() + 0.5, plank.pos().getZ() + 0.5, 4, 0.3, 0.3, 0.3, 0.05);
        }
    }

    @Override
    public void clear(ServerPlayer player) {
        ACTIVE_UNTIL.remove(player.getUUID());
        Deque<Plank> planks = PLANKS.remove(player.getUUID());
        if (planks != null) {
            planks.forEach(plank -> fall(player, plank));
        }
    }

    @Override
    public boolean primed(ServerPlayer player) {
        return active(player);
    }

    static boolean active(ServerPlayer player) {
        Long until = ACTIVE_UNTIL.get(player.getUUID());
        return until != null && until > player.serverLevel().getGameTime();
    }

    static int planks(ServerPlayer player) {
        Deque<Plank> planks = PLANKS.get(player.getUUID());
        return planks == null ? 0 : planks.size();
    }
}
