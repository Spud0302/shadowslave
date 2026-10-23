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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Warden's Dormant ability: mark the creature you look at. The mark glows
 * for everyone and takes half again as much damage from every source, so a
 * group knows what to focus. One mark per Warden; marking again moves it.
 */
public final class MarkPreyAbility implements AspectAbility {
    public static final ResourceLocation ID = AspectKitCatalog.WARDEN.abilityId();
    static final int ESSENCE_COST = 20;
    static final double REACH = 32.0;
    static final int MARK_TICKS = 20 * 30;
    static final float MARKED_DAMAGE_FACTOR = 1.5F;

    record Mark(UUID target, ResourceKey<Level> dimension, long expiresAt) {
    }

    /** Live marks by Warden. Short-lived, so they are not saved. */
    private static final Map<UUID, Mark> MARKS = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return AspectKitCatalog.WARDEN.abilityName();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        LivingEntity prey = AbilityTargets.lookedAt(player, REACH, entity -> entity != player && !(entity instanceof ServerPlayer));
        if (prey == null) {
            AbilityService.feedback(player, "Look at the creature you want to mark.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        mark(player, prey);
        return Activation.USED;
    }

    /** Marks {@code prey} for {@code player}, replacing any earlier mark. */
    public static void mark(ServerPlayer player, LivingEntity prey) {
        ServerLevel level = player.serverLevel();
        Optional<LivingEntity> previous = markOf(player);
        previous.filter(old -> old != prey).ifPresent(old -> old.removeEffect(MobEffects.GLOWING));
        MARKS.put(player.getUUID(), new Mark(prey.getUUID(), level.dimension(), level.getGameTime() + MARK_TICKS));
        prey.addEffect(new MobEffectInstance(MobEffects.GLOWING, MARK_TICKS, 0, false, false));
        AbilityTargets.beam(level, ParticleTypes.CRIT, player.getEyePosition(), prey.position().add(0, prey.getBbHeight() * 0.6, 0));
        level.sendParticles(ParticleTypes.ENCHANTED_HIT, prey.getX(), prey.getY() + prey.getBbHeight() + 0.3, prey.getZ(),
                12, 0.3, 0.2, 0.3, 0.05);
        level.playSound(null, prey.blockPosition(), SoundEvents.CROSSBOW_LOADING_END.value(), SoundSource.PLAYERS, 1.0F, 0.6F);
        AbilityService.feedback(player, "Marked: " + prey.getDisplayName().getString(), ChatFormatting.GREEN);
    }

    @Override
    public void tick(ServerPlayer player) {
        Mark mark = MARKS.get(player.getUUID());
        if (mark == null) {
            return;
        }
        ServerLevel level = player.serverLevel();
        Entity prey = level.getEntity(mark.target());
        if (mark.expiresAt() <= level.getGameTime() || !level.dimension().equals(mark.dimension())
                || !(prey instanceof LivingEntity living) || !living.isAlive()) {
            MARKS.remove(player.getUUID());
            if (prey instanceof LivingEntity living) {
                living.removeEffect(MobEffects.GLOWING);
            }
            return;
        }
        if (level.getGameTime() % 10 == 0) {
            level.sendParticles(player, ParticleTypes.CRIT, true, prey.getX(), prey.getY() + prey.getBbHeight() + 0.4, prey.getZ(),
                    3, 0.15, 0.05, 0.15, 0.0);
        }
    }

    @Override
    public void clear(ServerPlayer player) {
        Optional<LivingEntity> prey = markOf(player);
        MARKS.remove(player.getUUID());
        prey.ifPresent(living -> living.removeEffect(MobEffects.GLOWING));
    }

    /** The living creature this Warden has marked, if the mark still holds. */
    public static Optional<LivingEntity> markOf(ServerPlayer player) {
        Mark mark = MARKS.get(player.getUUID());
        if (mark == null || !player.serverLevel().dimension().equals(mark.dimension())) {
            return Optional.empty();
        }
        return player.serverLevel().getEntity(mark.target()) instanceof LivingEntity living && living.isAlive()
                ? Optional.of(living) : Optional.empty();
    }

    static boolean hasMark(ServerPlayer player) {
        return markOf(player).isPresent();
    }

    /** Whether any Warden has marked this entity. */
    static boolean isMarked(LivingEntity entity) {
        UUID id = entity.getUUID();
        return MARKS.values().stream().anyMatch(mark -> mark.target().equals(id));
    }
}
