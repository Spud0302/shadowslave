package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/**
 * Threshold, Awakened (control): the creature you look at (within
 * {@link #REACH} blocks, no higher in rank than you) drops out of the world
 * into your pocket of space for five seconds, then returns where it was.
 */
public final class BanishAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.BANISH.abilityId();
    static final int ESSENCE_COST = 45;
    static final double REACH = 16.0;
    static final int AWAY_TICKS = 20 * 5;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.BANISH.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        LivingEntity target = AbilityTargets.lookedAt(player, REACH, entity -> entity instanceof Mob);
        if (!(target instanceof Mob mob)) {
            AbilityService.feedback(player, "There is nothing there to banish.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        if (!Slumber.canLull(player, mob) || mob.isVehicle()) {
            AbilityService.feedback(player, "It holds too fast to this world.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        player.serverLevel().playSound(null, mob.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.5F);
        if (!PocketSpace.banish(mob, AWAY_TICKS)) {
            return Activation.FAILED;
        }
        PocketSpace.doorOpened(player);
        return Activation.USED;
    }
}
