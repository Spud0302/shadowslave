package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.echoes.EchoService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The always-on halves of Vessel and the states its abilities leave behind:
 * Borrowed Trait (a small gift from the kind of Echo you have out), Echo
 * Merge (your Echo folded into you, its strength, hide and speed yours),
 * Hollowed (your own abilities locked while merged) and Shared Wounds (your
 * Echo taking part of each blow).
 */
public final class VesselBond {
    static final ResourceLocation ATTACK_ID = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "echo_merge_attack");
    static final ResourceLocation ARMOR_ID = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "echo_merge_armor");
    static final ResourceLocation SPEED_ID = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "echo_merge_speed");
    /** Borrowed Trait is renewed once a second and lasts a little past that. */
    static final int TRAIT_TICKS = 20 * 4;
    /** Night Vision flickers in its last ten seconds, so it is laid down longer. */
    static final int NIGHT_VISION_TICKS = 20 * 13;
    /** A player's walking speed against a typical creature's: the ratio turns an Echo's speed into a player's. */
    static final double CREATURE_TO_PLAYER_SPEED = 0.25;
    static final double MAX_SPEED_BONUS = 0.5;
    /** Shared Wounds: the share of each blow the Echo takes, and how near it must stand. */
    static final float SHARED_SHARE = 0.4F;
    static final double SHARED_RANGE = 32.0;

    private static final Map<UUID, Merge> MERGES = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> WOUNDS = new ConcurrentHashMap<>();

    /** One merge: which Echo body is folded in, until when, and how its name tag stood before. */
    record Merge(UUID body, long until, boolean nameShown) {
    }

    private VesselBond() {
    }

    static boolean vessel(ServerPlayer player) {
        return KitRules.hasInnate(player, AspectKitCatalog.VESSEL.innateId());
    }

    // ---- Borrowed Trait --------------------------------------------------------------------------------------------

    /** The small gift an Echo of this kind gives its Vessel: the first that fits, in this order. */
    static Holder<MobEffect> trait(Mob echo) {
        if (echo.fireImmune()) {
            return MobEffects.FIRE_RESISTANCE;
        }
        if (echo.canBreatheUnderwater() && !echo.isInvertedHealAndHarm()) {
            return MobEffects.WATER_BREATHING;
        }
        if (echo instanceof FlyingAnimal || echo instanceof net.minecraft.world.entity.FlyingMob) {
            return MobEffects.SLOW_FALLING;
        }
        if (echo.isInvertedHealAndHarm()) {
            return MobEffects.NIGHT_VISION;
        }
        AttributeInstance speed = echo.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && speed.getBaseValue() >= 0.3) {
            return MobEffects.MOVEMENT_SPEED;
        }
        return echo instanceof Monster ? MobEffects.DAMAGE_BOOST : MobEffects.DIG_SPEED;
    }

    /** The Echo whose trait a Vessel carries: the merged one, else the first summoned in this level. */
    static Optional<Mob> traitSource(ServerPlayer player) {
        Optional<Mob> merged = merged(player);
        if (merged.isPresent()) {
            return merged;
        }
        return EchoService.summonedBodies(player).stream().filter(echo -> echo.level() == player.level()).findFirst();
    }

    static void applyTrait(ServerPlayer player) {
        Optional<Mob> source = traitSource(player);
        if (source.isEmpty()) {
            return;
        }
        Holder<MobEffect> effect = trait(source.get());
        int ticks = effect == MobEffects.NIGHT_VISION ? NIGHT_VISION_TICKS : TRAIT_TICKS;
        player.addEffect(new MobEffectInstance(effect, ticks, merging(player) ? 1 : 0, true, false, true));
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        Merge merge = MERGES.get(player.getUUID());
        if (merge != null) {
            holdMerge(player, merge);
        }
        Long wounds = WOUNDS.get(player.getUUID());
        if (wounds != null && wounds <= player.serverLevel().getGameTime()) {
            WOUNDS.remove(player.getUUID());
            AbilityService.feedback(player, "Your wounds are your own again.", ChatFormatting.GRAY);
            AbilityService.syncStatus(player);
        }
        if (player.tickCount % 20 == 0 && vessel(player)) {
            applyTrait(player);
        }
    }

    // ---- Echo Merge ------------------------------------------------------------------------------------------------

    static boolean merging(ServerPlayer player) {
        return MERGES.containsKey(player.getUUID());
    }

    static Optional<Mob> merged(ServerPlayer player) {
        Merge merge = MERGES.get(player.getUUID());
        if (merge == null) {
            return Optional.empty();
        }
        Entity body = player.serverLevel().getEntity(merge.body());
        return body instanceof Mob mob && mob.isAlive() ? Optional.of(mob) : Optional.empty();
    }

    /** Folds {@code echo} into the player for {@code ticks}. */
    static void merge(ServerPlayer player, Mob echo, int ticks) {
        MERGES.put(player.getUUID(), new Merge(echo.getUUID(), player.serverLevel().getGameTime() + ticks, echo.isCustomNameVisible()));
        echo.getNavigation().stop();
        echo.setInvisible(true);
        echo.setInvulnerable(true);
        echo.setNoAi(true);
        echo.setSilent(true);
        echo.setCustomNameVisible(false);
        follow(player, echo);
        double attack = echo.getAttribute(Attributes.ATTACK_DAMAGE) == null ? 0.0 : echo.getAttributeValue(Attributes.ATTACK_DAMAGE);
        modify(player, Attributes.ATTACK_DAMAGE, ATTACK_ID, Math.max(0.0, attack - 1.0), AttributeModifier.Operation.ADD_VALUE);
        modify(player, Attributes.ARMOR, ARMOR_ID, echo.getArmorValue(), AttributeModifier.Operation.ADD_VALUE);
        AttributeInstance speed = echo.getAttribute(Attributes.MOVEMENT_SPEED);
        double bonus = speed == null ? 0.0 : Math.min(MAX_SPEED_BONUS, Math.max(0.0, speed.getBaseValue() / CREATURE_TO_PLAYER_SPEED - 1.0));
        modify(player, Attributes.MOVEMENT_SPEED, SPEED_ID, bonus, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        applyTrait(player);
        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 1.0, player.getZ(), 40, 0.4, 0.8, 0.4, 0.05);
        level.playSound(null, player.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.2F, 0.8F);
    }

    /**
     * Keeps the folded Echo inside its Vessel: just under the player's feet,
     * where it can neither be seen nor caught by the player's own swing.
     */
    static void follow(ServerPlayer player, Mob echo) {
        echo.teleportTo(player.getX(), player.getY() - echo.getBbHeight() - 0.2, player.getZ());
        echo.setDeltaMovement(Vec3.ZERO);
        echo.clearFire();
    }

    static void holdMerge(ServerPlayer player, Merge merge) {
        Optional<Mob> echo = merged(player);
        if (echo.isEmpty() || merge.until() <= player.serverLevel().getGameTime() || !player.isAlive()) {
            unmerge(player);
            return;
        }
        follow(player, echo.get());
        if (player.tickCount % 4 == 0) {
            player.serverLevel().sendParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 1.0, player.getZ(),
                    2, 0.3, 0.6, 0.3, 0.01);
        }
    }

    /** Ends a merge: the Echo steps back out beside its Vessel and the borrowed strength goes with it. */
    static void unmerge(ServerPlayer player) {
        Merge merge = MERGES.remove(player.getUUID());
        unmodify(player, Attributes.ATTACK_DAMAGE, ATTACK_ID);
        unmodify(player, Attributes.ARMOR, ARMOR_ID);
        unmodify(player, Attributes.MOVEMENT_SPEED, SPEED_ID);
        if (merge == null) {
            return;
        }
        Entity body = player.serverLevel().getEntity(merge.body());
        if (body instanceof Mob echo && echo.isAlive()) {
            echo.setInvisible(false);
            echo.setInvulnerable(false);
            echo.setNoAi(false);
            echo.setSilent(false);
            echo.setCustomNameVisible(merge.nameShown());
            Vec3 side = Vec3.directionFromRotation(0, player.getYRot() + 90).scale(1.2);
            echo.teleportTo(player.getX() + side.x, player.getY(), player.getZ() + side.z);
            player.serverLevel().sendParticles(ParticleTypes.SOUL, echo.getX(), echo.getY() + 0.5, echo.getZ(), 25, 0.3, 0.5, 0.3, 0.04);
        }
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0F, 1.3F);
        AbilityService.feedback(player, "Your Echo steps back out of you.", ChatFormatting.GRAY);
        AbilityService.syncStatus(player);
    }

    private static void modify(ServerPlayer player, Holder<Attribute> attribute, ResourceLocation id, double amount,
                               AttributeModifier.Operation operation) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            instance.addOrUpdateTransientModifier(new AttributeModifier(id, amount, operation));
        }
    }

    private static void unmodify(ServerPlayer player, Holder<Attribute> attribute, ResourceLocation id) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(id);
        }
    }

    // ---- Hollowed --------------------------------------------------------------------------------------------------

    /** Hollowed: true when the player's own abilities are locked because they are merged. */
    public static boolean hollowed(ServerPlayer player) {
        return merging(player) && KitRules.hasFlaw(player, AspectKitCatalog.VESSEL.flawEffectId());
    }

    // ---- Shared Wounds ---------------------------------------------------------------------------------------------

    static void shareWounds(ServerPlayer player, int ticks) {
        WOUNDS.put(player.getUUID(), player.serverLevel().getGameTime() + ticks);
    }

    static boolean sharing(ServerPlayer player) {
        Long until = WOUNDS.get(player.getUUID());
        return until != null && until > player.serverLevel().getGameTime();
    }

    static void unshare(UUID player) {
        WOUNDS.remove(player);
    }

    /** The Echo that takes a share of the blow: the first summoned one near enough that can be hurt. */
    static Optional<Mob> woundTaker(ServerPlayer player) {
        return EchoService.summonedBodies(player).stream()
                .filter(echo -> echo.level() == player.level() && !echo.isInvulnerable()
                        && echo.distanceToSqr(player) <= SHARED_RANGE * SHARED_RANGE)
                .findFirst();
    }

    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !sharing(player)
                || event.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }
        Optional<Mob> taker = woundTaker(player).filter(echo -> echo != event.getSource().getEntity());
        if (taker.isEmpty()) {
            return;
        }
        float share = event.getAmount() * SHARED_SHARE;
        LivingEntity echo = taker.get();
        echo.hurt(player.damageSources().magic(), share);
        event.setAmount(event.getAmount() - share);
        player.serverLevel().sendParticles(ParticleTypes.SOUL, echo.getX(), echo.getY() + echo.getBbHeight() * 0.6, echo.getZ(),
                6, 0.2, 0.3, 0.2, 0.02);
    }

    // ---- Cleanup ---------------------------------------------------------------------------------------------------

    static void clear(ServerPlayer player) {
        unmerge(player);
        unshare(player.getUUID());
    }

    public static void onServerStopping(ServerStoppingEvent event) {
        MERGES.clear();
        WOUNDS.clear();
    }
}
