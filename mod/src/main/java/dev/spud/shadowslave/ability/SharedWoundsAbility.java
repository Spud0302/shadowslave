package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;

import java.util.Optional;

/**
 * Vessel, Awakened (shelter): for twenty seconds your summoned Echo takes two
 * fifths of every blow that falls on you, while it stands near enough.
 */
public final class SharedWoundsAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.SHARED_WOUNDS.abilityId();
    static final int ESSENCE_COST = 30;
    static final int SHARE_TICKS = 20 * 20;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.SHARED_WOUNDS.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        if (VesselBond.sharing(player)) {
            AbilityService.feedback(player, "Your Echo already shares your wounds.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        Optional<Mob> echo = VesselBond.woundTaker(player);
        if (echo.isEmpty()) {
            AbilityService.feedback(player, "No Echo of yours stands near enough to share your wounds.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        VesselBond.shareWounds(player, SHARE_TICKS);
        ServerLevel level = player.serverLevel();
        AbilityTargets.beam(level, ParticleTypes.SOUL, player.position().add(0, 1.0, 0),
                echo.get().position().add(0, echo.get().getBbHeight() * 0.6, 0));
        level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, player.getX(), player.getY() + 1.2, player.getZ(), 6, 0.3, 0.3, 0.3, 0.1);
        level.playSound(null, player.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0F, 0.6F);
        return Activation.USED;
    }

    @Override
    public void tick(ServerPlayer player) {
        if (VesselBond.sharing(player) && player.tickCount % 10 == 0) {
            VesselBond.woundTaker(player).ifPresent(echo -> AbilityTargets.beam(player.serverLevel(), ParticleTypes.SOUL,
                    player.position().add(0, 1.0, 0), echo.position().add(0, echo.getBbHeight() * 0.6, 0)));
        }
    }

    @Override
    public void clear(ServerPlayer player) {
        VesselBond.unshare(player.getUUID());
    }

    @Override
    public boolean primed(ServerPlayer player) {
        return VesselBond.sharing(player);
    }
}
