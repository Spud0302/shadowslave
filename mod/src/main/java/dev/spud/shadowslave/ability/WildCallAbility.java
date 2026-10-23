package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

/**
 * Verdant, Awakened (support): the wild answers. The tameable animal you
 * look at (a wolf, cat, parrot, horse and so on) becomes yours on the spot,
 * and every pet of yours within {@link #PET_RANGE} blocks is roused:
 * regeneration and strength for 20 seconds.
 */
public final class WildCallAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.WILD_CALL.abilityId();
    static final int ESSENCE_COST = 30;
    static final double REACH = 12.0;
    static final double PET_RANGE = 16.0;
    static final int ROUSE_TICKS = 20 * 20;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.WILD_CALL.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        LivingEntity wild = AbilityTargets.lookedAt(player, REACH, WildCallAbility::tameable);
        if (wild != null) {
            tame(player, wild);
        }
        int roused = 0;
        for (LivingEntity pet : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(PET_RANGE),
                pet -> pet instanceof OwnableEntity owned && player.getUUID().equals(owned.getOwnerUUID()))) {
            pet.addEffect(new MobEffectInstance(MobEffects.REGENERATION, ROUSE_TICKS, 1, false, true));
            pet.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, ROUSE_TICKS, 0, false, true));
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pet.getX(), pet.getY() + pet.getBbHeight(), pet.getZ(),
                    6, 0.3, 0.3, 0.3, 0.0);
            roused++;
        }
        if (wild == null && roused == 0) {
            AbilityService.feedback(player, "No animal answers your call.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        level.playSound(null, player.blockPosition(), SoundEvents.WOLF_HOWL, SoundSource.PLAYERS, 0.5F, 1.4F);
        level.playSound(null, player.blockPosition(), SoundEvents.AZALEA_LEAVES_HIT, SoundSource.PLAYERS, 1.0F, 0.8F);
        if (wild != null) {
            AbilityService.feedback(player, wild.getDisplayName().getString() + " answers your call", ChatFormatting.GREEN);
        }
        return Activation.USED;
    }

    static boolean tameable(LivingEntity entity) {
        return entity instanceof TamableAnimal animal && !animal.isTame()
                || entity instanceof AbstractHorse horse && !horse.isTamed();
    }

    private static void tame(ServerPlayer player, LivingEntity wild) {
        if (wild instanceof TamableAnimal animal) {
            animal.tame(player);
            animal.getNavigation().stop();
            animal.setTarget(null);
            animal.setOrderedToSit(false);
        } else if (wild instanceof AbstractHorse horse) {
            horse.tameWithName(player);
        }
        wild.level().broadcastEntityEvent(wild, (byte) 7);
    }
}
