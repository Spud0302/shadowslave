package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import dev.spud.shadowslave.echoes.EchoService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

/**
 * Surveyor, Awakened: crack the ground in a line ahead of you. Every creature
 * on the line takes a blow and is slowed. It splits only the ground's
 * surface: no blocks are broken.
 */
public final class FaultLineAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.FAULT_LINE.abilityId();
    static final int ESSENCE_COST = 35;
    static final int LENGTH = 12;
    static final double WIDTH = 1.5;
    static final float DAMAGE = 4.0F;
    static final int SLOW_TICKS = 100;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.FAULT_LINE.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 flat = player.getLookAngle().multiply(1.0, 0.0, 1.0);
        Vec3 direction = flat.lengthSqr() < 1.0E-4 ? Vec3.directionFromRotation(0.0F, player.getYRot()) : flat.normalize();
        Set<LivingEntity> struck = new HashSet<>();
        for (int step = 1; step <= LENGTH; step++) {
            Vec3 at = player.position().add(direction.scale(step));
            BlockPos ground = BlockPos.containing(at.x, player.getY() - 0.5, at.z);
            BlockState state = level.getBlockState(ground);
            if (!state.isAir()) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), at.x, player.getY() + 0.1, at.z,
                        8, 0.3, 0.1, 0.3, 0.1);
            }
            level.sendParticles(ParticleTypes.CRIT, at.x, player.getY() + 0.2, at.z, 2, 0.2, 0.0, 0.2, 0.1);
            AABB box = new AABB(at.x - WIDTH / 2, player.getY() - 1.0, at.z - WIDTH / 2, at.x + WIDTH / 2, player.getY() + 2.0, at.z + WIDTH / 2);
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box,
                    entity -> entity != player && entity.isAlive() && !CarryTheFlameAbility.isAlly(player, entity)
                            && !EchoService.isAlly(player, entity))) {
                if (struck.add(target)) {
                    target.hurt(level.damageSources().playerAttack(player), DAMAGE);
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOW_TICKS, 1));
                }
            }
        }
        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.6F, 0.6F);
        AbilityService.feedback(player, "Fault Line: " + (struck.isEmpty() ? "the ground splits, empty."
                : struck.size() + (struck.size() == 1 ? " creature" : " creatures") + " caught in the crack."), ChatFormatting.GOLD);
        return Activation.USED;
    }
}
