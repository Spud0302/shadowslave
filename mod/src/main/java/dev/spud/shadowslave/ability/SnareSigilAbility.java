package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Warden, Awakened (control): draw a rune on the ground where you look. The
 * first hostiles to cross it are rooted in place for a few seconds. One
 * sigil at a time; it fades after a minute.
 */
public final class SnareSigilAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.SNARE_SIGIL.abilityId();
    static final int ESSENCE_COST = 35;
    static final double REACH = 12.0;
    static final double RADIUS = 1.8;
    static final int SIGIL_TICKS = 20 * 60;
    static final int ROOT_TICKS = 20 * 4;

    record Sigil(ResourceKey<Level> dimension, Vec3 centre, long expiresAt) {
    }

    private static final Map<UUID, Sigil> SIGILS = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.SNARE_SIGIL.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 centre = groundAt(player);
        SIGILS.put(player.getUUID(), new Sigil(level.dimension(), centre, level.getGameTime() + SIGIL_TICKS));
        ring(level, centre, 32, ParticleTypes.ENCHANT);
        level.playSound(null, BlockPos.containing(centre), SoundEvents.EVOKER_PREPARE_ATTACK, SoundSource.PLAYERS, 0.8F, 1.4F);
        return Activation.USED;
    }

    /** The top of the block you look at within reach, or your own feet. */
    static Vec3 groundAt(ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        BlockHitResult hit = player.level().clip(new ClipContext(eye, eye.add(player.getLookAngle().scale(REACH)),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = hit.getBlockPos();
            return new Vec3(hit.getLocation().x, pos.getY() + 1.0, hit.getLocation().z);
        }
        return player.position();
    }

    @Override
    public void tick(ServerPlayer player) {
        Sigil sigil = SIGILS.get(player.getUUID());
        if (sigil == null) {
            return;
        }
        ServerLevel level = player.serverLevel();
        if (sigil.expiresAt() <= level.getGameTime() || !level.dimension().equals(sigil.dimension())) {
            SIGILS.remove(player.getUUID());
            return;
        }
        if (level.getGameTime() % 4 == 0) {
            ring(level, sigil.centre(), 12, ParticleTypes.HAPPY_VILLAGER);
        }
        List<LivingEntity> caught = level.getEntitiesOfClass(LivingEntity.class,
                new AABB(sigil.centre(), sigil.centre()).inflate(RADIUS, 1.0, RADIUS),
                entity -> AbilityTargets.isHostile(entity) && entity.position().distanceTo(sigil.centre()) <= RADIUS + 0.3);
        if (caught.isEmpty()) {
            return;
        }
        SIGILS.remove(player.getUUID());
        for (LivingEntity prey : caught) {
            root(prey);
        }
        ring(level, sigil.centre(), 40, ParticleTypes.TOTEM_OF_UNDYING);
        level.playSound(null, BlockPos.containing(sigil.centre()), SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 1.2F, 0.6F);
    }

    static void root(LivingEntity prey) {
        prey.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ROOT_TICKS, 9, false, true));
        prey.addEffect(new MobEffectInstance(MobEffects.JUMP, ROOT_TICKS, 128, false, false));
        prey.setDeltaMovement(Vec3.ZERO);
        prey.hurtMarked = true;
    }

    private static void ring(ServerLevel level, Vec3 centre, int points, net.minecraft.core.particles.ParticleOptions particle) {
        for (int i = 0; i < points; i++) {
            double angle = Mth.TWO_PI * i / points;
            level.sendParticles(particle, centre.x + Math.cos(angle) * RADIUS, centre.y + 0.1, centre.z + Math.sin(angle) * RADIUS,
                    1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public void clear(ServerPlayer player) {
        SIGILS.remove(player.getUUID());
    }

    static boolean hasSigil(ServerPlayer player) {
        return SIGILS.containsKey(player.getUUID());
    }
}
