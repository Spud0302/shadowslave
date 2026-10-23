package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rime, Awakened (movement): for 20 seconds still water within
 * {@link #RADIUS} blocks freezes under your feet (frosted ice, which melts
 * on its own like Frost Walker's) and you run faster on any ice.
 */
public final class RimepathAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.RIMEPATH.abilityId();
    static final int ESSENCE_COST = 25;
    static final int DURATION_TICKS = 20 * 20;
    static final int RADIUS = 3;

    private static final Map<UUID, Long> ACTIVE_UNTIL = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.RIMEPATH.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        if (active(player)) {
            AbilityService.feedback(player, "The frost is already in your steps.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        ServerLevel level = player.serverLevel();
        ACTIVE_UNTIL.put(player.getUUID(), level.getGameTime() + DURATION_TICKS);
        freezeAround(player);
        level.sendParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 0.3, player.getZ(), 40, 1.0, 0.2, 1.0, 0.02);
        level.playSound(null, player.blockPosition(), SoundEvents.POWDER_SNOW_STEP, SoundSource.PLAYERS, 1.0F, 0.6F);
        return Activation.USED;
    }

    @Override
    public void tick(ServerPlayer player) {
        Long until = ACTIVE_UNTIL.get(player.getUUID());
        if (until == null) {
            return;
        }
        ServerLevel level = player.serverLevel();
        if (until <= level.getGameTime()) {
            ACTIVE_UNTIL.remove(player.getUUID());
            AbilityService.feedback(player, "The frost leaves your steps.", ChatFormatting.GRAY);
            AbilityService.syncStatus(player);
            return;
        }
        if (player.onGround()) {
            freezeAround(player);
        }
        if (level.getBlockState(player.getOnPos()).is(BlockTags.ICE)) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 10, 1, false, false, true));
        }
        if (level.getGameTime() % 4 == 0) {
            level.sendParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 0.1, player.getZ(), 2, 0.3, 0.05, 0.3, 0.0);
        }
    }

    /** Freezes the still water just under the player's feet, as Frost Walker does. */
    static void freezeAround(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos below = player.blockPosition().below();
        BlockState frosted = Blocks.FROSTED_ICE.defaultBlockState();
        for (BlockPos pos : BlockPos.betweenClosed(below.offset(-RADIUS, 0, -RADIUS), below.offset(RADIUS, 0, RADIUS))) {
            if (pos.distToCenterSqr(player.getX(), pos.getY() + 0.5, player.getZ()) > (RADIUS + 0.5) * (RADIUS + 0.5)
                    || !level.getBlockState(pos.above()).isAir()) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.WATER) && state.getValue(LiquidBlock.LEVEL) == 0
                    && level.getFluidState(pos).is(Fluids.WATER) && frosted.canSurvive(level, pos)
                    && level.isUnobstructed(frosted, pos, net.minecraft.world.phys.shapes.CollisionContext.empty())) {
                level.setBlockAndUpdate(pos, frosted);
                level.scheduleTick(pos, Blocks.FROSTED_ICE, Mth.nextInt(player.getRandom(), 60, 120));
            }
        }
    }

    @Override
    public void clear(ServerPlayer player) {
        ACTIVE_UNTIL.remove(player.getUUID());
    }

    @Override
    public boolean primed(ServerPlayer player) {
        return active(player);
    }

    static boolean active(ServerPlayer player) {
        Long until = ACTIVE_UNTIL.get(player.getUUID());
        return until != null && until > player.serverLevel().getGameTime();
    }
}
