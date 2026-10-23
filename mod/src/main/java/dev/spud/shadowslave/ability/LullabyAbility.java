package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

/**
 * Lull's Dormant ability: the creature you look at (within
 * {@link #REACH} blocks) falls asleep for five seconds, if its rank is no
 * higher than your own. Damage wakes it.
 */
public final class LullabyAbility implements AspectAbility {
    public static final ResourceLocation ID = AspectKitCatalog.LULL.abilityId();
    static final int ESSENCE_COST = 25;
    static final double REACH = 16.0;
    static final int SLEEP_TICKS = 20 * 5;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return AspectKitCatalog.LULL.abilityName();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        LivingEntity target = AbilityTargets.lookedAt(player, REACH, entity -> entity instanceof Mob && !(entity instanceof Player));
        if (target == null) {
            AbilityService.feedback(player, "Look at a creature to sing it to sleep.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        if (!Slumber.canLull(player, target)) {
            AbilityService.feedback(player, "Its soul is too strong to lull.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        Slumber.put((Mob) target, SLEEP_TICKS);
        ServerLevel level = player.serverLevel();
        AbilityTargets.beam(level, ParticleTypes.NOTE, player.getEyePosition().add(player.getLookAngle().scale(0.8)),
                target.getEyePosition());
        level.playSound(null, target.blockPosition(), SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 0.8F, 0.6F);
        AbilityService.feedback(player, target.getDisplayName().getString() + " falls asleep.", ChatFormatting.LIGHT_PURPLE);
        return Activation.USED;
    }
}
