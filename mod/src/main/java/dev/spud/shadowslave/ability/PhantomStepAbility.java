package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mist, Ascended (movement): blink through mist where you look, then twice
 * more for free within five seconds.
 */
public final class PhantomStepAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.PHANTOM_STEP.abilityId();
    static final int ESSENCE_COST = 40;
    static final int FREE_STEPS = 2;
    static final int WINDOW_TICKS = 20 * 5;

    private record Steps(int left, long until) {
    }

    private static final Map<UUID, Steps> STEPS = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.PHANTOM_STEP.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        Vec3 destination = MistPassageAbility.findDestination(player);
        if (destination == null) {
            AbilityService.feedback(player, "No room to step there.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        ServerLevel level = player.serverLevel();
        Vec3 origin = player.position();
        level.sendParticles(ParticleTypes.CLOUD, origin.x, origin.y + 1.0, origin.z, 20, 0.3, 0.6, 0.3, 0.02);
        player.teleportTo(destination.x, destination.y, destination.z);
        player.fallDistance = 0.0F;
        level.sendParticles(ParticleTypes.CLOUD, destination.x, destination.y + 1.0, destination.z, 20, 0.3, 0.6, 0.3, 0.02);
        level.playSound(null, player.blockPosition(), SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 0.7F, 1.4F);
        Steps steps = STEPS.get(player.getUUID());
        if (steps != null) {
            if (steps.left() <= 1) {
                STEPS.remove(player.getUUID());
            } else {
                STEPS.put(player.getUUID(), new Steps(steps.left() - 1, steps.until()));
            }
            // A free step: nothing more to pay.
            return Activation.PRIMED;
        }
        STEPS.put(player.getUUID(), new Steps(FREE_STEPS, level.getGameTime() + WINDOW_TICKS));
        return Activation.USED;
    }

    @Override
    public void tick(ServerPlayer player) {
        Steps steps = STEPS.get(player.getUUID());
        if (steps != null && steps.until() <= player.serverLevel().getGameTime()) {
            STEPS.remove(player.getUUID());
            AbilityService.syncStatus(player);
        }
    }

    @Override
    public boolean primed(ServerPlayer player) {
        return STEPS.containsKey(player.getUUID());
    }

    @Override
    public void clear(ServerPlayer player) {
        STEPS.remove(player.getUUID());
    }
}
