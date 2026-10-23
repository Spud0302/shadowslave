package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Anchor, Awakened (control): a well of weight opens where you look (within
 * {@link #REACH} blocks). For four seconds hostiles within {@link #RADIUS}
 * blocks of it are dragged in and pinned there, slowed to a crawl.
 */
public final class GravityWellAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.GRAVITY_WELL.abilityId();
    static final int ESSENCE_COST = 40;
    static final double REACH = 24.0;
    static final double RADIUS = 8.0;
    static final int DURATION_TICKS = 20 * 4;

    record Well(ResourceKey<Level> dimension, Vec3 centre, long until) {
    }

    private static final Map<UUID, Well> WELLS = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.GRAVITY_WELL.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 centre = Slumber.lookPoint(player);
        if (caught(level, centre).isEmpty()) {
            AbilityService.feedback(player, "Nothing there to drag down.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        WELLS.put(player.getUUID(), new Well(level.dimension(), centre, level.getGameTime() + DURATION_TICKS));
        pull(level, centre);
        level.playSound(null, net.minecraft.core.BlockPos.containing(centre), SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(),
                SoundSource.PLAYERS, 1.0F, 0.6F);
        return Activation.USED;
    }

    private static List<Mob> caught(ServerLevel level, Vec3 centre) {
        return level.getEntitiesOfClass(Mob.class, new AABB(centre, centre).inflate(RADIUS), AbilityTargets::isHostile);
    }

    /** Drags everything caught toward the centre and pins what has arrived. */
    static void pull(ServerLevel level, Vec3 centre) {
        for (Mob mob : caught(level, centre)) {
            Vec3 toward = centre.subtract(mob.position());
            double distance = toward.length();
            Vec3 move = distance < 1.0 ? Vec3.ZERO : toward.normalize().scale(Math.min(0.6, distance * 0.25));
            mob.setDeltaMovement(move.x, Math.min(mob.getDeltaMovement().y, 0.0) - 0.1, move.z);
            mob.hurtMarked = true;
            mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 3, false, true));
        }
    }

    @Override
    public void tick(ServerPlayer player) {
        Well well = WELLS.get(player.getUUID());
        if (well == null) {
            return;
        }
        ServerLevel level = player.serverLevel();
        long now = level.getGameTime();
        if (well.until() <= now || !well.dimension().equals(level.dimension())) {
            WELLS.remove(player.getUUID());
            return;
        }
        if (now % 3 == 0) {
            pull(level, well.centre());
        }
        for (int i = 0; i < 10; i++) {
            float angle = i * Mth.TWO_PI / 10 + now * 0.3F;
            double radius = 0.5 + (now % 20) / 20.0 * 3.0;
            level.sendParticles(ParticleTypes.PORTAL, well.centre().x + Mth.cos(angle) * (3.5 - radius), well.centre().y + 0.2,
                    well.centre().z + Mth.sin(angle) * (3.5 - radius), 1, 0.0, 0.0, 0.0, 0.0);
        }
        level.sendParticles(ParticleTypes.SQUID_INK, well.centre().x, well.centre().y + 0.3, well.centre().z, 1, 0.2, 0.1, 0.2, 0.0);
    }

    @Override
    public void clear(ServerPlayer player) {
        WELLS.remove(player.getUUID());
    }
}
