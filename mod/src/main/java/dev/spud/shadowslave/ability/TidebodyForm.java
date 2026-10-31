package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.List;

/**
 * Tide, Transformation (DESIGN): a body of water. It breathes and swims like
 * the sea, heals while wet, fire cannot touch it, and whatever strikes it in
 * melee is thrown back on the swell.
 */
public final class TidebodyForm extends TransformationAbility {
    public static final ResourceLocation ID = KitLadder.TIDEBODY.abilityId();
    static final int ESSENCE_COST = 50;
    static final int DRAIN_PER_SECOND = 5;
    static final double THROW_BACK = 1.2;

    public TidebodyForm() {
        super(KitLadder.TIDEBODY);
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public int drainPerSecond() {
        return DRAIN_PER_SECOND;
    }

    @Override
    protected List<FormModifier> modifiers() {
        return List.of(FormModifier.multiply(Attributes.MOVEMENT_SPEED, 0.1));
    }

    @Override
    protected ParticleOptions aura() {
        return ParticleTypes.SPLASH;
    }

    @Override
    protected SoundEvent shiftSound() {
        return SoundEvents.CONDUIT_ACTIVATE;
    }

    @Override
    protected void onFormTick(ServerPlayer player, int ticksHeld) {
        player.clearFire();
        if (ticksHeld % 20 != 1) {
            return;
        }
        player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 40, 0, true, false));
        player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 40, 0, true, false));
        if (player.isInWaterOrRain()) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 1, true, false));
        }
    }

    @Override
    protected void onIncoming(ServerPlayer player, LivingIncomingDamageEvent event) {
        if (event.getSource().is(DamageTypeTags.IS_FIRE)) {
            event.setCanceled(true);
            return;
        }
        if (event.getSource().getDirectEntity() instanceof LivingEntity attacker && attacker != player) {
            Vec3 away = attacker.position().subtract(player.position()).multiply(1, 0, 1).normalize().scale(THROW_BACK);
            attacker.push(away.x, 0.4, away.z);
            attacker.hurtMarked = true;
            player.serverLevel().sendParticles(ParticleTypes.SPLASH, attacker.getX(), attacker.getY() + 1.0, attacker.getZ(),
                    20, 0.4, 0.4, 0.4, 0.2);
        }
    }
}
