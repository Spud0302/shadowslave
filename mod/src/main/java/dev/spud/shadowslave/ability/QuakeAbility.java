package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.ability.combat.AbilityCombat;
import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.block.Blocks;

/** Stone, Awakened (force): stomp, and nearby hostile mobs are hurt, thrown up and slowed. */
public final class QuakeAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.QUAKE.abilityId();
    static final int ESSENCE_COST = 45;
    static final double RADIUS = 5.0;
    static final float DAMAGE = 5.0F;
    static final int SLOW_TICKS = 20 * 4;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.QUAKE.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        AbilityCombat combat = AbilityCombat.get();
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(RADIUS, 2.0, RADIUS),
                entity -> entity instanceof Enemy && entity.isAlive() && entity.distanceToSqr(player) <= RADIUS * RADIUS)) {
            combat.damage(player, mob, DAMAGE);
            mob.setDeltaMovement(mob.getDeltaMovement().x * 0.2, 0.65, mob.getDeltaMovement().z * 0.2);
            mob.hurtMarked = true;
            mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOW_TICKS, 1, false, true));
        }
        for (int ring = 1; ring <= 5; ring++) {
            double r = RADIUS * ring / 5.0;
            for (int i = 0; i < 8 * ring; i++) {
                double angle = Math.PI * 2.0 * i / (8 * ring);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                        player.getX() + Math.cos(angle) * r, player.getY() + 0.1, player.getZ() + Math.sin(angle) * r,
                        2, 0.1, 0.1, 0.1, 0.1);
            }
        }
        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.7F, 0.5F);
        level.playSound(null, player.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.6F, 0.4F);
        return Activation.USED;
    }
}
