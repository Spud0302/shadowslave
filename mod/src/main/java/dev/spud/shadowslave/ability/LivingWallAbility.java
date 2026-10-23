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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verdant, Awakened (shelter): a thorny hedge of flowering azalea five wide
 * and two high, two blocks ahead. Hostile creatures pressing against it are
 * cut and slowed; it withers after {@link #DURATION_TICKS}. Like Root in
 * Stone, it only grows into air and plants and never replaces anything.
 */
public final class LivingWallAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.LIVING_WALL.abilityId();
    static final int ESSENCE_COST = 35;
    static final int DURATION_TICKS = 20 * 15;
    static final int DISTANCE = 2;
    static final int HALF_WIDTH = 2;
    static final float THORN_DAMAGE = 2.0F;
    static final int THORN_EVERY_TICKS = 10;

    record Hedge(ResourceKey<Level> dimension, List<BlockPos> blocks, long withersAt) {
    }

    private static final Map<UUID, Hedge> HEDGES = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.LIVING_WALL.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        wither(player);
        BlockState leaves = hedgeBlock();
        Direction facing = player.getDirection();
        Direction side = facing.getClockWise();
        BlockPos base = player.blockPosition().relative(facing, DISTANCE);
        List<BlockPos> grown = new ArrayList<>();
        for (int across = -HALF_WIDTH; across <= HALF_WIDTH; across++) {
            for (int up = 0; up <= 1; up++) {
                BlockPos pos = base.relative(side, across).above(up);
                if (!level.getBlockState(pos).canBeReplaced() || !level.getFluidState(pos).isEmpty()
                        || !level.getEntities(null, new AABB(pos)).isEmpty()) {
                    continue;
                }
                level.setBlockAndUpdate(pos, leaves);
                level.sendParticles(ParticleTypes.CHERRY_LEAVES, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                        4, 0.3, 0.2, 0.3, 0.0);
                grown.add(pos.immutable());
            }
        }
        if (grown.isEmpty()) {
            AbilityService.feedback(player, "There is no room for the hedge to grow.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        HEDGES.put(player.getUUID(), new Hedge(level.dimension(), List.copyOf(grown), level.getGameTime() + DURATION_TICKS));
        level.playSound(null, base, SoundEvents.AZALEA_LEAVES_PLACE, SoundSource.PLAYERS, 1.0F, 0.7F);
        level.playSound(null, base, SoundEvents.SWEET_BERRY_BUSH_PLACE, SoundSource.PLAYERS, 1.0F, 0.8F);
        return Activation.USED;
    }

    static BlockState hedgeBlock() {
        return Blocks.FLOWERING_AZALEA_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
    }

    @Override
    public void tick(ServerPlayer player) {
        Hedge hedge = HEDGES.get(player.getUUID());
        if (hedge == null) {
            return;
        }
        ServerLevel level = player.serverLevel();
        if (hedge.withersAt() <= level.getGameTime()) {
            wither(player);
            return;
        }
        if (level.getGameTime() % THORN_EVERY_TICKS == 0 && level.dimension().equals(hedge.dimension())) {
            prick(player, level, hedge);
        }
    }

    private static void prick(ServerPlayer player, ServerLevel level, Hedge hedge) {
        for (BlockPos pos : hedge.blocks()) {
            if (!level.getBlockState(pos).is(Blocks.FLOWERING_AZALEA_LEAVES)) {
                continue;
            }
            for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(0.35),
                    AbilityTargets::isHostile)) {
                mob.hurt(level.damageSources().thorns(player), THORN_DAMAGE);
                mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, false, true));
            }
        }
    }

    @Override
    public void clear(ServerPlayer player) {
        wither(player);
    }

    private static void wither(ServerPlayer player) {
        Hedge hedge = HEDGES.remove(player.getUUID());
        if (hedge == null || player.getServer() == null) {
            return;
        }
        ServerLevel level = player.getServer().getLevel(hedge.dimension());
        if (level == null) {
            return;
        }
        for (BlockPos pos : hedge.blocks()) {
            if (level.isLoaded(pos) && level.getBlockState(pos).is(Blocks.FLOWERING_AZALEA_LEAVES)) {
                level.removeBlock(pos, false);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.FLOWERING_AZALEA_LEAVES.defaultBlockState()),
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.3, 0.3, 0.3, 0.05);
            }
        }
    }

    static List<BlockPos> hedgeOf(ServerPlayer player) {
        Hedge hedge = HEDGES.get(player.getUUID());
        return hedge == null ? List.of() : hedge.blocks();
    }
}
