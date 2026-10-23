package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rime, Awakened (control): the creature you look at (within {@link #REACH}
 * blocks, no higher in rank than you) is frozen inside a shell of real ice
 * for four seconds. It cannot move or land a blow. The shell only fills
 * open air and melts away after; ice someone has since broken is left alone.
 */
public final class EncaseAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.ENCASE.abilityId();
    static final int ESSENCE_COST = 40;
    static final double REACH = 16.0;
    static final int HOLD_TICKS = 20 * 4;
    /** Anything wider or taller than this does not fit in a shell. */
    static final double MAX_WIDTH = 2.0;
    static final double MAX_HEIGHT = 3.0;

    record Shell(ResourceKey<Level> dimension, List<BlockPos> ice, long until) {
    }

    private static final Map<UUID, Shell> SHELLS = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.ENCASE.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        LivingEntity target = AbilityTargets.lookedAt(player, REACH, entity -> entity instanceof Mob);
        if (!(target instanceof Mob mob)) {
            AbilityService.feedback(player, "There is nothing there to freeze.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        if (!Slumber.canLull(player, mob)) {
            AbilityService.feedback(player, "Its soul burns too hot to freeze.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        if (mob.getBbWidth() > MAX_WIDTH || mob.getBbHeight() > MAX_HEIGHT) {
            AbilityService.feedback(player, "It is too large to encase.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        if (SHELLS.containsKey(mob.getUUID())) {
            AbilityService.feedback(player, "It is already frozen.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        encase(mob, HOLD_TICKS);
        SignatureKitPassives.mark(player, mob, 1);
        player.serverLevel().playSound(null, mob.blockPosition(), SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 1.0F, 0.6F);
        player.serverLevel().playSound(null, mob.blockPosition(), SoundEvents.POWDER_SNOW_PLACE, SoundSource.PLAYERS, 1.0F, 0.8F);
        return Activation.USED;
    }

    /** Builds the shell around the mob: every open block that touches its box. */
    static void encase(Mob mob, int ticks) {
        ServerLevel level = (ServerLevel) mob.level();
        AABB box = mob.getBoundingBox();
        BlockPos min = BlockPos.containing(box.minX, box.minY, box.minZ);
        BlockPos max = BlockPos.containing(box.maxX - 1.0e-4, box.maxY - 1.0e-4, box.maxZ - 1.0e-4);
        BlockState ice = Blocks.ICE.defaultBlockState();
        List<BlockPos> placed = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(min.offset(-1, -1, -1), max.offset(1, 1, 1))) {
            boolean inside = pos.getX() >= min.getX() && pos.getX() <= max.getX() && pos.getY() >= min.getY()
                    && pos.getY() <= max.getY() && pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ();
            if (inside || !level.getBlockState(pos).canBeReplaced() || !level.getFluidState(pos).isEmpty()) {
                continue;
            }
            level.setBlockAndUpdate(pos, ice);
            placed.add(pos.immutable());
        }
        mob.setTarget(null);
        mob.getNavigation().stop();
        mob.setDeltaMovement(0.0, Math.min(0.0, mob.getDeltaMovement().y), 0.0);
        mob.setTicksFrozen(mob.getTicksRequiredToFreeze());
        level.sendParticles(ParticleTypes.SNOWFLAKE, mob.getX(), mob.getY() + mob.getBbHeight() * 0.5, mob.getZ(), 40,
                0.6, 0.6, 0.6, 0.03);
        SHELLS.put(mob.getUUID(), new Shell(level.dimension(), List.copyOf(placed), level.getGameTime() + ticks));
    }

    public static boolean encased(Entity entity) {
        return SHELLS.containsKey(entity.getUUID());
    }

    static List<BlockPos> shellOf(Entity entity) {
        Shell shell = SHELLS.get(entity.getUUID());
        return shell == null ? List.of() : shell.ice();
    }

    /** Holds encased creatures still and melts each shell when its time is up. Called every server tick. */
    static void tickShells(MinecraftServer server) {
        if (SHELLS.isEmpty()) {
            return;
        }
        for (Iterator<Map.Entry<UUID, Shell>> it = SHELLS.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<UUID, Shell> entry = it.next();
            Shell shell = entry.getValue();
            ServerLevel level = server.getLevel(shell.dimension());
            if (level == null) {
                it.remove();
                continue;
            }
            Entity entity = level.getEntity(entry.getKey());
            if (!(entity instanceof Mob mob) || !mob.isAlive() || shell.until() <= level.getGameTime()) {
                it.remove();
                melt(level, shell);
                continue;
            }
            mob.setTarget(null);
            mob.getNavigation().stop();
            mob.setDeltaMovement(0.0, Math.min(0.0, mob.getDeltaMovement().y), 0.0);
            mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 6, false, false));
            mob.setTicksFrozen(Mth.clamp(mob.getTicksFrozen() + 2, 0, mob.getTicksRequiredToFreeze()));
        }
    }

    private static void melt(ServerLevel level, Shell shell) {
        BlockState ice = Blocks.ICE.defaultBlockState();
        for (BlockPos pos : shell.ice()) {
            if (level.isLoaded(pos) && level.getBlockState(pos).is(Blocks.ICE)) {
                level.removeBlock(pos, false);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ice), pos.getX() + 0.5, pos.getY() + 0.5,
                        pos.getZ() + 0.5, 4, 0.3, 0.3, 0.3, 0.05);
            }
        }
        level.playSound(null, shell.ice().isEmpty() ? BlockPos.ZERO : shell.ice().get(0), SoundEvents.GLASS_BREAK,
                SoundSource.PLAYERS, 0.8F, 1.2F);
    }

    /** Melts every shell at once, when the server stops. */
    static void thawAll(MinecraftServer server) {
        SHELLS.values().forEach(shell -> {
            ServerLevel level = server.getLevel(shell.dimension());
            if (level != null) {
                melt(level, shell);
            }
        });
        SHELLS.clear();
    }
}
