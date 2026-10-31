package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.List;

/**
 * Stone, Transformation (DESIGN): a colossus half again your size. Heavy,
 * armoured and immovable, slower on its feet, and every blow staggers.
 */
public final class ColossusForm extends TransformationAbility {
    public static final ResourceLocation ID = KitLadder.COLOSSUS.abilityId();
    static final int ESSENCE_COST = 60;
    static final int DRAIN_PER_SECOND = 6;
    static final int STAGGER_TICKS = 40;

    public ColossusForm() {
        super(KitLadder.COLOSSUS);
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
        return List.of(
                FormModifier.add(Attributes.SCALE, 0.5),
                FormModifier.add(Attributes.ARMOR, 8.0),
                FormModifier.add(Attributes.ARMOR_TOUGHNESS, 4.0),
                FormModifier.add(Attributes.KNOCKBACK_RESISTANCE, 1.0),
                FormModifier.add(Attributes.ATTACK_DAMAGE, 3.0),
                FormModifier.add(Attributes.ATTACK_KNOCKBACK, 1.0),
                FormModifier.multiply(Attributes.MOVEMENT_SPEED, -0.15));
    }

    @Override
    protected ParticleOptions aura() {
        return new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState());
    }

    @Override
    protected SoundEvent shiftSound() {
        return SoundEvents.IRON_GOLEM_REPAIR;
    }

    @Override
    protected void onHit(ServerPlayer player, LivingEntity target, LivingDamageEvent.Pre event) {
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, STAGGER_TICKS, 2));
    }
}
