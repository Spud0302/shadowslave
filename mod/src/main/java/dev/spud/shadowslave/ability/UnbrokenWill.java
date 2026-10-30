package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The always-on halves of Unbroken and the states its abilities leave behind:
 * Weathered (a long fight hurts less and less), Last Stand's brace, Grit's
 * planted feet, Defiance's dare, and No Retreat (running from a fight costs
 * essence).
 */
public final class UnbrokenWill {
    /** Weathered: a fight ends when neither side has struck for this long. */
    static final long FIGHT_GAP_TICKS = 20 * 6;
    /** Weathered: each second of the fight turns aside this much more, up to the cap. */
    static final float WEATHERED_PER_SECOND = 0.02F;
    static final float WEATHERED_CAP = 0.3F;
    /** Last Stand: the health a killing blow leaves, and how long before the brace can be raised again. */
    static final float LAST_STAND_HEALTH = 2.0F;
    static final long LAST_STAND_REST_TICKS = 20 * 180;
    /** Grit: each stack turns aside this much, up to the cap; stepping this far from where you planted ends it. */
    static final float GRIT_PER_STACK = 0.1F;
    static final int GRIT_MAX_STACKS = 5;
    static final double GRIT_LEASH = 1.5;
    /** No Retreat: essence drained each second you sprint away from a hostile this near. */
    static final int RETREAT_DRAIN = 3;
    static final double RETREAT_RADIUS = 12.0;

    private static final Map<UUID, Fight> FIGHTS = new ConcurrentHashMap<>();
    private static final Set<UUID> BRACED = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Long> RESTING = new ConcurrentHashMap<>();
    private static final Map<UUID, Grit> GRIT = new ConcurrentHashMap<>();
    /** A dared creature to the Unbroken it must fight and until when. */
    private static final Map<UUID, Dare> DARES = new ConcurrentHashMap<>();
    private static final Map<UUID, Vec3> LAST_SEEN = new ConcurrentHashMap<>();

    record Fight(long start, long last) {
    }

    record Grit(Vec3 anchor, long until, int stacks) {
    }

    record Dare(UUID player, long until) {
    }

    private UnbrokenWill() {
    }

    static boolean weathered(ServerPlayer player) {
        return KitRules.hasInnate(player, AspectKitCatalog.UNBROKEN.innateId());
    }

    // ---- Weathered -----------------------------------------------------------------------------------------------

    /** Marks a moment of fighting: a fight that went quiet starts over. Timed in server ticks, which always run. */
    static void fought(ServerPlayer player) {
        long now = player.server.getTickCount();
        FIGHTS.compute(player.getUUID(), (id, fight) -> fight == null || now - fight.last() > FIGHT_GAP_TICKS
                ? new Fight(now, now) : new Fight(fight.start(), now));
    }

    /** The share of damage Weathered turns aside right now. */
    static float weatheredShare(ServerPlayer player) {
        Fight fight = FIGHTS.get(player.getUUID());
        long now = player.server.getTickCount();
        if (fight == null || now - fight.last() > FIGHT_GAP_TICKS) {
            return 0.0F;
        }
        return Math.min(WEATHERED_CAP, WEATHERED_PER_SECOND * ((now - fight.start()) / 20L));
    }

    // ---- Last Stand ----------------------------------------------------------------------------------------------

    static boolean braced(ServerPlayer player) {
        return BRACED.contains(player.getUUID());
    }

    static void brace(ServerPlayer player) {
        BRACED.add(player.getUUID());
    }

    /** Ticks until Last Stand can be braced again, or 0. */
    static long resting(ServerPlayer player) {
        Long until = RESTING.get(player.getUUID());
        return until == null ? 0 : Math.max(0, until - player.serverLevel().getGameTime());
    }

    /** A braced Unbroken refuses a killing blow once. */
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !BRACED.remove(player.getUUID())
                || event.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }
        event.setCanceled(true);
        player.setHealth(LAST_STAND_HEALTH);
        player.clearFire();
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 3, 1, false, true, true));
        RESTING.put(player.getUUID(), player.serverLevel().getGameTime() + LAST_STAND_REST_TICKS);
        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0, player.getZ(), 60, 0.5, 0.8, 0.5, 0.4);
        level.playSound(null, player.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.8F, 0.7F);
        AbilityService.feedback(player, "Last Stand: you will not fall. Not yet.", ChatFormatting.GOLD);
        AbilityService.syncStatus(player);
    }

    // ---- Grit ----------------------------------------------------------------------------------------------------

    static void plant(ServerPlayer player, int ticks) {
        GRIT.put(player.getUUID(), new Grit(player.position(), player.serverLevel().getGameTime() + ticks, 0));
    }

    static int gritStacks(ServerPlayer player) {
        Grit grit = GRIT.get(player.getUUID());
        return grit == null ? 0 : grit.stacks();
    }

    static boolean planted(ServerPlayer player) {
        return GRIT.containsKey(player.getUUID());
    }

    static void unplant(UUID player) {
        GRIT.remove(player);
    }

    // ---- Defiance ------------------------------------------------------------------------------------------------

    static void dare(Mob mob, ServerPlayer player, int ticks) {
        mob.setTarget(player);
        DARES.put(mob.getUUID(), new Dare(player.getUUID(), mob.level().getGameTime() + ticks));
    }

    static boolean dared(Mob mob, ServerPlayer player) {
        Dare dare = DARES.get(mob.getUUID());
        return dare != null && dare.player().equals(player.getUUID()) && dare.until() > mob.level().getGameTime();
    }

    /** A dared creature can't turn away from the one who dared it while they still stand. */
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide) {
            return;
        }
        Dare dare = DARES.get(mob.getUUID());
        if (dare == null) {
            return;
        }
        if (dare.until() <= mob.level().getGameTime()) {
            DARES.remove(mob.getUUID());
            return;
        }
        LivingEntity next = event.getNewAboutToBeSetTarget();
        net.minecraft.world.entity.player.Player darer = mob.level().getPlayerByUUID(dare.player());
        if (darer != null && darer.isAlive() && next != darer) {
            event.setCanceled(true);
        }
    }

    // ---- Damage --------------------------------------------------------------------------------------------------

    /** Marks the fight on both sides of a blow. */
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer attacker && attacker != event.getEntity() && weathered(attacker)) {
            fought(attacker);
        }
        if (event.getEntity() instanceof ServerPlayer player && weathered(player) && event.getSource().getEntity() != null) {
            fought(player);
        }
    }

    /**
     * Weathered and Grit turn their share aside at the last step, after armour
     * and every other change, so nothing later can undo it.
     */
    public static void onDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        float kept = 1.0F;
        if (weathered(player) && event.getSource().getEntity() != null) {
            kept -= weatheredShare(player);
        }
        Grit grit = GRIT.get(player.getUUID());
        if (grit != null && grit.until() > player.serverLevel().getGameTime()
                && player.position().distanceTo(grit.anchor()) <= GRIT_LEASH) {
            kept -= GRIT_PER_STACK * grit.stacks();
            GRIT.put(player.getUUID(), new Grit(grit.anchor(), grit.until(), Math.min(GRIT_MAX_STACKS, grit.stacks() + 1)));
            player.serverLevel().sendParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 1.0, player.getZ(), 8, 0.3, 0.5, 0.3, 0.1);
        }
        if (kept < 1.0F) {
            event.setNewDamage(event.getNewDamage() * Math.max(0.2F, kept));
        }
    }

    // ---- Every second --------------------------------------------------------------------------------------------

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        Grit grit = GRIT.get(player.getUUID());
        if (grit != null && (grit.until() <= player.serverLevel().getGameTime() || player.position().distanceTo(grit.anchor()) > GRIT_LEASH)) {
            GRIT.remove(player.getUUID());
            AbilityService.feedback(player, "You give ground, and your grit goes with it.", ChatFormatting.GRAY);
            AbilityService.syncStatus(player);
        }
        if (player.tickCount % 20 == 0) {
            retreat(player);
        }
    }

    /** No Retreat: a second spent sprinting away from a hostile creature costs essence. */
    static void retreat(ServerPlayer player) {
        Vec3 now = player.position();
        Vec3 before = LAST_SEEN.put(player.getUUID(), now);
        if (before == null || !player.isSprinting() || !KitRules.hasFlaw(player, AspectKitCatalog.UNBROKEN.flawEffectId())) {
            return;
        }
        if (fleeing(player, now.subtract(before))) {
            EssenceService.spend(player, RETREAT_DRAIN);
            player.serverLevel().sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 0.8, player.getZ(), 6, 0.2, 0.3, 0.2, 0.02);
            AbilityService.syncStatus(player);
        }
    }

    /** Whether moving by {@code moved} carries the player away from some hostile creature nearby. */
    static boolean fleeing(ServerPlayer player, Vec3 moved) {
        Vec3 flat = new Vec3(moved.x, 0, moved.z);
        if (flat.lengthSqr() < 0.25) {
            return false;
        }
        return player.serverLevel().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(RETREAT_RADIUS), AbilityTargets::isHostile)
                .stream().anyMatch(foe -> flat.dot(player.position().subtract(foe.position())) > 0);
    }

    // ---- Cleanup -------------------------------------------------------------------------------------------------

    static void forget(UUID player) {
        FIGHTS.remove(player);
        GRIT.remove(player);
        LAST_SEEN.remove(player);
    }

    public static void onServerStopping(ServerStoppingEvent event) {
        FIGHTS.clear();
        BRACED.clear();
        RESTING.clear();
        GRIT.clear();
        DARES.clear();
        LAST_SEEN.clear();
    }
}
