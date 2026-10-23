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
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gloam's Dormant ability: for 20 seconds shadow wreathes your blows. Each
 * melee hit cuts deeper the darker it is where it lands: a little in bright
 * light, more in the dim, most in the dark (SignatureKitPassives adds it).
 */
public final class GloamEdgeAbility implements AspectAbility {
    public static final ResourceLocation ID = AspectKitCatalog.GLOAM.abilityId();
    static final int ESSENCE_COST = 30;
    static final int DURATION_TICKS = 20 * 20;
    static final float BRIGHT_CUT = 2.0F;
    static final float DIM_CUT = 4.0F;
    static final float DARK_CUT = 6.0F;

    private static final Map<UUID, Long> ACTIVE_UNTIL = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return AspectKitCatalog.GLOAM.abilityName();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        if (active(player)) {
            AbilityService.feedback(player, "Your edge is already wreathed in shadow.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        ServerLevel level = player.serverLevel();
        ACTIVE_UNTIL.put(player.getUUID(), level.getGameTime() + DURATION_TICKS);
        level.sendParticles(ParticleTypes.SQUID_INK, player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.4, 0.5, 0.4, 0.02);
        level.playSound(null, player.blockPosition(), SoundEvents.WARDEN_ATTACK_IMPACT, SoundSource.PLAYERS, 0.6F, 1.4F);
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
            AbilityService.feedback(player, "The shadow slips from your edge.", ChatFormatting.GRAY);
            AbilityService.syncStatus(player);
        } else if (level.getGameTime() % 3 == 0) {
            Vec3 hand = player.getEyePosition().add(player.getLookAngle().scale(0.5)).add(0, -0.45, 0);
            level.sendParticles(ParticleTypes.SMOKE, hand.x, hand.y, hand.z, 2, 0.08, 0.08, 0.08, 0.0);
        }
    }

    /** The extra damage a wreathed blow deals, by how dark it is where it lands, with a puff of shadow. */
    static float cut(ServerPlayer player, LivingEntity target) {
        float cut = switch (SignatureKitPassives.lightAt(target)) {
            case DARK -> DARK_CUT;
            case DIM -> DIM_CUT;
            case BRIGHT -> BRIGHT_CUT;
        };
        player.serverLevel().sendParticles(ParticleTypes.SQUID_INK, target.getX(), target.getY() + target.getBbHeight() * 0.6,
                target.getZ(), (int) cut * 3, 0.25, 0.3, 0.25, 0.02);
        return cut;
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
}
