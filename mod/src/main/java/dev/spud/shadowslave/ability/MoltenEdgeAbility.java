package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Forge, Awakened (force): for 15 seconds your melee blows burn. Each hit
 * deals a little extra and sets the target alight (KitPassives applies it).
 */
public final class MoltenEdgeAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.MOLTEN_EDGE.abilityId();
    static final int ESSENCE_COST = 35;
    static final int DURATION_TICKS = 20 * 15;
    static final float EXTRA_DAMAGE = 3.0F;
    static final int FIRE_SECONDS = 4;

    private static final Map<UUID, Long> ACTIVE_UNTIL = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.MOLTEN_EDGE.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        if (active(player)) {
            AbilityService.feedback(player, "Your edge is already burning.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        ServerLevel level = player.serverLevel();
        ACTIVE_UNTIL.put(player.getUUID(), level.getGameTime() + DURATION_TICKS);
        level.sendParticles(ParticleTypes.LAVA, player.getX(), player.getY() + 1.0, player.getZ(), 8, 0.3, 0.3, 0.3, 0.0);
        level.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.4, 0.5, 0.4, 0.03);
        level.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.9F, 0.8F);
        return Activation.USED;
    }

    @Override
    public void tick(ServerPlayer player) {
        Long until = ACTIVE_UNTIL.get(player.getUUID());
        if (until == null) {
            return;
        }
        ServerLevel level = player.serverLevel();
        if (until <= level.getGameTime()) {
            ACTIVE_UNTIL.remove(player.getUUID());
            AbilityService.feedback(player, "Your edge cools.", ChatFormatting.GRAY);
            AbilityService.syncStatus(player);
        } else if (level.getGameTime() % 4 == 0) {
            var hand = player.getEyePosition().add(player.getLookAngle().scale(0.5)).add(0, -0.45, 0);
            level.sendParticles(ParticleTypes.SMALL_FLAME, hand.x, hand.y, hand.z, 2, 0.08, 0.08, 0.08, 0.0);
        }
    }

    @Override
    public void clear(ServerPlayer player) {
        ACTIVE_UNTIL.remove(player.getUUID());
    }

    @Override
    public boolean primed(ServerPlayer player) {
        return active(player);
    }

    static boolean active(ServerPlayer player) {
        Long until = ACTIVE_UNTIL.get(player.getUUID());
        return until != null && until > player.serverLevel().getGameTime();
    }

    /** A burning blow: sets the target alight. The extra damage is added by the caller. */
    static void sear(ServerPlayer player, LivingEntity target) {
        target.igniteForSeconds(FIRE_SECONDS);
        player.serverLevel().sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + target.getBbHeight() * 0.6,
                target.getZ(), 12, 0.25, 0.3, 0.25, 0.03);
    }
}
