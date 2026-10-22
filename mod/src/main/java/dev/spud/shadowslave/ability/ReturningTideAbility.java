package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tide's Dormant ability: the first press marks where you stand; press again
 * within 10 seconds, or wait, and the tide pulls you back to the mark.
 */
public final class ReturningTideAbility implements AspectAbility {
    public static final ResourceLocation ID = AspectKitCatalog.TIDE.abilityId();
    static final int MARK_TICKS = 20 * 10;
    static final int COOLDOWN_TICKS = 20 * 25;

    record Mark(ResourceKey<Level> dimension, Vec3 position, long expiresAt) {
    }

    /** Live marks. Short-lived, so they are not saved; a restart simply drops the mark. */
    private static final Map<UUID, Mark> MARKS = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return "Returning Tide";
    }

    @Override
    public int cooldownTicks() {
        return COOLDOWN_TICKS;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        Mark mark = MARKS.remove(player.getUUID());
        if (mark == null) {
            ServerLevel level = player.serverLevel();
            MARKS.put(player.getUUID(), new Mark(level.dimension(), player.position(), level.getGameTime() + MARK_TICKS));
            level.sendParticles(ParticleTypes.SPLASH, player.getX(), player.getY() + 0.2, player.getZ(), 30, 0.4, 0.1, 0.4, 0.1);
            level.playSound(null, player.blockPosition(), SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 0.8F, 0.8F);
            AbilityService.feedback(player, "The tide marks this place. Press again to return.", ChatFormatting.AQUA);
            return Activation.PRIMED;
        }
        return pullBack(player, mark) ? Activation.USED : Activation.FAILED;
    }

    @Override
    public void tick(ServerPlayer player) {
        Mark mark = MARKS.get(player.getUUID());
        if (mark != null && mark.expiresAt() <= player.serverLevel().getGameTime()) {
            MARKS.remove(player.getUUID());
            if (pullBack(player, mark)) {
                AbilityService.startCooldown(player, this);
            }
        }
    }

    private static boolean pullBack(ServerPlayer player, Mark mark) {
        if (!player.serverLevel().dimension().equals(mark.dimension())) {
            AbilityService.feedback(player, "The tide cannot reach you here.", ChatFormatting.GRAY);
            return false;
        }
        ServerLevel level = player.serverLevel();
        Vec3 from = player.position();
        level.sendParticles(ParticleTypes.BUBBLE_POP, from.x, from.y + 1.0, from.z, 25, 0.3, 0.5, 0.3, 0.05);
        player.teleportTo(mark.position().x, mark.position().y, mark.position().z);
        player.resetFallDistance();
        level.sendParticles(ParticleTypes.SPLASH, mark.position().x, mark.position().y + 0.5, mark.position().z,
                40, 0.4, 0.4, 0.4, 0.2);
        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 0.6F, 1.0F);
        return true;
    }

    static boolean hasMark(ServerPlayer player) {
        return MARKS.containsKey(player.getUUID());
    }
}
