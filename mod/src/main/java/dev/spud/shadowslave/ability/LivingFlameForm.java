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
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.List;

/**
 * Ember, Transformation (DESIGN): a burning shape that can fly. Fire cannot
 * hurt it, its blows set targets alight, and whatever strikes it in melee
 * catches fire too.
 */
public final class LivingFlameForm extends TransformationAbility {
    public static final ResourceLocation ID = KitLadder.LIVING_FLAME.abilityId();
    static final int ESSENCE_COST = 60;
    static final int DRAIN_PER_SECOND = 6;
    static final int FIRE_SECONDS = 5;

    public LivingFlameForm() {
        super(KitLadder.LIVING_FLAME);
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
        return List.of(FormModifier.add(Attributes.ATTACK_DAMAGE, 2.0));
    }

    @Override
    protected ParticleOptions aura() {
        return ParticleTypes.FLAME;
    }

    @Override
    protected SoundEvent shiftSound() {
        return SoundEvents.BLAZE_SHOOT;
    }

    @Override
    protected void onEnter(ServerPlayer player) {
        setFlight(player, true);
    }

    @Override
    protected void onFormTick(ServerPlayer player, int ticksHeld) {
        player.clearFire();
        if (ticksHeld % 20 == 1) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, true, false));
        }
        if (!player.getAbilities().mayfly) {
            setFlight(player, true);
        }
        if (ticksHeld % 3 == 0) {
            player.serverLevel().sendParticles(ParticleTypes.LAVA, player.getX(), player.getY() + 0.8, player.getZ(), 1,
                    0.3, 0.5, 0.3, 0.0);
        }
    }

    @Override
    protected void onLeave(ServerPlayer player) {
        setFlight(player, false);
    }

    @Override
    protected void onIncoming(ServerPlayer player, LivingIncomingDamageEvent event) {
        if (event.getSource().is(DamageTypeTags.IS_FIRE)) {
            event.setCanceled(true);
            return;
        }
        if (event.getSource().getDirectEntity() instanceof LivingEntity attacker && attacker != player) {
            attacker.igniteForSeconds(FIRE_SECONDS);
        }
    }

    @Override
    protected void onHit(ServerPlayer player, LivingEntity target, LivingDamageEvent.Pre event) {
        target.igniteForSeconds(FIRE_SECONDS);
    }

    private static void setFlight(ServerPlayer player, boolean on) {
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        player.getAbilities().mayfly = on;
        if (!on) {
            player.getAbilities().flying = false;
        }
        player.onUpdateAbilities();
    }
}
