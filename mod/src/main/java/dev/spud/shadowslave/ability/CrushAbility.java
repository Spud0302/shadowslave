package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Anchor, Awakened (force): for ten seconds, every landing from
 * {@link #MIN_FALL} blocks or more sends out a shockwave that grows with the
 * fall, half again as strong while Heavy, and the fall itself cannot hurt you.
 */
public final class CrushAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.CRUSH.abilityId();
    static final int ESSENCE_COST = 35;
    static final int DURATION_TICKS = 20 * 10;
    static final float MIN_FALL = 3.0F;
    static final float MAX_DAMAGE = 14.0F;

    private static final Map<UUID, Long> ACTIVE_UNTIL = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.CRUSH.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        if (active(player)) {
            AbilityService.feedback(player, "You already fall like a stone.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        ACTIVE_UNTIL.put(player.getUUID(), player.serverLevel().getGameTime() + DURATION_TICKS);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.5F, 0.5F);
        return Activation.USED;
    }

    @Override
    public void tick(ServerPlayer player) {
        Long until = ACTIVE_UNTIL.get(player.getUUID());
        if (until != null && until <= player.serverLevel().getGameTime()) {
            ACTIVE_UNTIL.remove(player.getUUID());
            AbilityService.feedback(player, "Your weight settles.", ChatFormatting.GRAY);
            AbilityService.syncStatus(player);
        }
    }

    /** The landing: no fall damage, and a shockwave if the drop was high enough. */
    public static void onFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !active(player) || event.getDistance() < MIN_FALL) {
            return;
        }
        event.setDamageMultiplier(0.0F);
        shockwave(player, event.getDistance());
    }

    static float damageFor(float fall, boolean heavy) {
        return Math.min(MAX_DAMAGE, (2.0F + fall * 0.75F) * (heavy ? 1.5F : 1.0F));
    }

    static void shockwave(ServerPlayer player, float fall) {
        ServerLevel level = player.serverLevel();
        boolean heavy = AnchorWeight.weight(player) == AnchorWeight.Weight.HEAVY;
        float damage = damageFor(fall, heavy);
        double radius = Math.min(6.0, 2.0 + fall / 3.0);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius, 2.0, radius),
                entity -> entity != player && AbilityTargets.isHostile(entity))) {
            foe.hurt(level.damageSources().indirectMagic(player, player), damage);
            Vec3 away = foe.position().subtract(player.position()).multiply(1, 0, 1);
            Vec3 push = away.lengthSqr() < 1.0e-4 ? Vec3.ZERO : away.normalize().scale(0.8);
            foe.setDeltaMovement(push.x, 0.4, push.z);
            foe.hurtMarked = true;
        }
        BlockState ground = level.getBlockState(player.getOnPos());
        if (!ground.isAir()) {
            int points = (int) (radius * 8);
            for (int i = 0; i < points; i++) {
                float angle = i * Mth.TWO_PI / points;
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), player.getX() + Mth.cos(angle) * radius,
                        player.getY() + 0.1, player.getZ() + Mth.sin(angle) * radius, 4, 0.2, 0.1, 0.2, 0.1);
            }
        }
        level.sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY() + 0.2, player.getZ(), 2, 0.5, 0.1, 0.5, 0.0);
        level.playSound(null, player.blockPosition(), SoundEvents.MACE_SMASH_GROUND_HEAVY, SoundSource.PLAYERS, 1.0F, 0.8F);
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
