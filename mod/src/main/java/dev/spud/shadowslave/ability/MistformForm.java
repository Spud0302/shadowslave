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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.List;

/**
 * Mist, Transformation (DESIGN): drifting mist. Unseen and swift, blows and
 * falls pass through it, and creatures hunting it lose the trail. Mist cannot
 * strike: its own blows pass through too.
 */
public final class MistformForm extends TransformationAbility {
    public static final ResourceLocation ID = KitLadder.MISTFORM.abilityId();
    static final int ESSENCE_COST = 50;
    static final int DRAIN_PER_SECOND = 5;
    static final double LOSE_RADIUS = 24.0;

    public MistformForm() {
        super(KitLadder.MISTFORM);
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
        return List.of(FormModifier.multiply(Attributes.MOVEMENT_SPEED, 0.3));
    }

    @Override
    protected ParticleOptions aura() {
        return ParticleTypes.CLOUD;
    }

    @Override
    protected SoundEvent shiftSound() {
        return SoundEvents.ILLUSIONER_MIRROR_MOVE;
    }

    @Override
    protected void onFormTick(ServerPlayer player, int ticksHeld) {
        if (ticksHeld % 20 != 1) {
            return;
        }
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, true, false));
        for (Mob mob : player.serverLevel().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(LOSE_RADIUS),
                candidate -> candidate.getTarget() == player)) {
            mob.setTarget(null);
        }
    }

    @Override
    protected void onLeave(ServerPlayer player) {
        player.removeEffect(MobEffects.INVISIBILITY);
    }

    @Override
    protected void onIncoming(ServerPlayer player, LivingIncomingDamageEvent event) {
        boolean fall = event.getSource().is(DamageTypeTags.IS_FALL);
        boolean blow = event.getSource().getDirectEntity() instanceof LivingEntity
                && !event.getSource().is(DamageTypeTags.IS_PROJECTILE);
        if (fall || blow) {
            event.setCanceled(true);
        }
    }

    @Override
    protected void onOutgoing(ServerPlayer player, LivingIncomingDamageEvent event) {
        if (event.getSource().getDirectEntity() == player) {
            event.setCanceled(true);
        }
    }
}
