package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import dev.spud.shadowslave.echoes.EchoService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/** Vessel, Awakened (movement): trade places with your summoned Echo. */
public final class SwapPlacesAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.SWAP_PLACES.abilityId();
    static final int ESSENCE_COST = 25;
    static final double RANGE = 64.0;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.SWAP_PLACES.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        Optional<Mob> found = EchoService.summonedBodies(player).stream()
                .filter(echo -> echo.level() == player.level() && echo.distanceToSqr(player) <= RANGE * RANGE)
                .findFirst();
        if (found.isEmpty()) {
            AbilityService.feedback(player, "No Echo of yours stands near enough to trade places with.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        Mob echo = found.get();
        ServerLevel level = player.serverLevel();
        Vec3 mine = player.position();
        Vec3 theirs = echo.position();
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, mine.x, mine.y + 1.0, mine.z, 30, 0.3, 0.6, 0.3, 0.05);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, theirs.x, theirs.y + 1.0, theirs.z, 30, 0.3, 0.6, 0.3, 0.05);
        echo.getNavigation().stop();
        echo.teleportTo(mine.x, mine.y, mine.z);
        player.teleportTo(theirs.x, theirs.y, theirs.z);
        player.resetFallDistance();
        AbilityTargets.beam(level, ParticleTypes.SOUL, mine.add(0, 1.0, 0), theirs.add(0, 1.0, 0));
        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.3F);
        return Activation.USED;
    }
}
