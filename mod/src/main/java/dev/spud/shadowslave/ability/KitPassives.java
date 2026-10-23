package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.attachment.ModAttachments;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.creature.NightmareCreature;
import dev.spud.shadowslave.gate.GateEvent;
import dev.spud.shadowslave.gate.GateService;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.entity.living.LivingBreatheEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Optional;

/**
 * The always-on half of the kits: each Nature's innate rule and its Flaw.
 * All server-side; Stone Sleep's sinking runs on the client (ClientKitEffects)
 * because player movement is client-driven.
 */
public final class KitPassives {
    static final float EMBER_FIRE_DAMAGE_FACTOR = 0.5F;
    static final double STILL_SPEED_SQR = 0.0001;
    static final int STONE_SLEEP_TICKS = 20 * 20;
    static final long TIDAL_DEBT_TICKS = 24000L;
    private static final int WEAKNESS_REFRESH_TICKS = 40;
    static final float QUARRY_LOCK_FACTOR = 0.5F;
    /** Gate Sense: how far a Warden feels a Gate, and how often the pull shows. */
    static final double GATE_SENSE_RANGE = 512.0;
    static final int GATE_SENSE_TICKS = 20;
    /** Soul Feast: essence for any hostile kill, and per core and rank step for a Nightmare Creature. */
    static final int FEAST_BASE = 8;
    static final int FEAST_PER_CORE_RANK = 6;
    static final int FEAST_MAX = 60;
    /** Brimming Vessel: a hit of at least this much (after armour) spills this share of held essence. */
    static final float BRIMMING_HIT = 6.0F;
    static final float BRIMMING_SPILL = 0.25F;

    private KitPassives() {
    }

    /**
     * Ember's Warmth: fire and lava hurt half as much. Mark Prey: a marked
     * creature takes half again as much from everyone. Quarry Lock: a Warden
     * with a mark up deals half damage to anything else.
     */
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && event.getSource().is(DamageTypeTags.IS_FIRE)
                && KitRules.hasInnate(player, AspectKitCatalog.EMBER.innateId())) {
            event.setAmount(event.getAmount() * EMBER_FIRE_DAMAGE_FACTOR);
        }
        LivingEntity target = event.getEntity();
        if (MarkPreyAbility.isMarked(target)) {
            event.setAmount(event.getAmount() * MarkPreyAbility.MARKED_DAMAGE_FACTOR);
        }
        if (event.getSource().getEntity() instanceof ServerPlayer warden
                && KitRules.hasFlaw(warden, AspectKitCatalog.WARDEN.flawEffectId())) {
            Optional<LivingEntity> mark = MarkPreyAbility.markOf(warden);
            if (mark.isPresent() && mark.get() != target) {
                event.setAmount(event.getAmount() * QUARRY_LOCK_FACTOR);
            }
        }
    }

    /** Soul Feast (Reaper): every hostile kill restores essence, more for stronger Nightmare Creatures. */
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer reaper) || !(event.getEntity() instanceof Enemy)
                || !KitRules.hasInnate(reaper, AspectKitCatalog.REAPER.innateId())) {
            return;
        }
        int feast = event.getEntity() instanceof NightmareCreature creature
                ? feastFor(creature.creatureClass().cores(), creature.rank().ordinal()) : FEAST_BASE;
        EssenceService.gain(reaper, feast);
        LivingEntity dead = event.getEntity();
        Vec3 from = dead.position().add(0, dead.getBbHeight() * 0.5, 0);
        AbilityTargets.beam(reaper.serverLevel(), ParticleTypes.SOUL, from, reaper.position().add(0, 1.0, 0));
        AbilityService.syncStatus(reaper);
    }

    static int feastFor(int cores, int rankOrdinal) {
        return Math.min(FEAST_MAX, FEAST_BASE + FEAST_PER_CORE_RANK * cores * (rankOrdinal + 1));
    }

    /** Brimming Vessel (Wellspring): a heavy hit spills a quarter of the essence you hold. */
    public static void onDamaged(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getNewDamage() < BRIMMING_HIT
                || !KitRules.hasFlaw(player, AspectKitCatalog.WELLSPRING.flawEffectId())) {
            return;
        }
        float spilled = EssenceService.current(player) * BRIMMING_SPILL;
        if (spilled < 1.0F) {
            return;
        }
        EssenceService.spend(player, (int) Math.ceil(spilled));
        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.DRIPPING_HONEY, player.getX(), player.getY() + 1.0, player.getZ(),
                14, 0.4, 0.4, 0.4, 0.0);
        level.sendParticles(ParticleTypes.WAX_OFF, player.getX(), player.getY() + 1.0, player.getZ(), 10, 0.5, 0.5, 0.5, 0.1);
        level.playSound(null, player.blockPosition(), SoundEvents.BOTTLE_EMPTY, SoundSource.PLAYERS, 1.0F, 0.7F);
        AbilityService.syncStatus(player);
    }

    /** Mist's Hush: a sneaking Mist player sets off no vibrations, so sculk and wardens stay quiet. */
    public static void onGameEvent(VanillaGameEvent event) {
        if (event.getCause() instanceof ServerPlayer player
                && player.isCrouching()
                && KitRules.hasInnate(player, AspectKitCatalog.MIST.innateId())) {
            event.setCanceled(true);
        }
    }

    /** Stone's Footing (standing still) and Holdfast (while braced): no knockback. */
    public static void onKnockBack(LivingKnockBackEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        boolean still = player.getKnownMovement().horizontalDistanceSqr() < STILL_SPEED_SQR;
        if (HoldfastAbility.isBraced(player)
                || (still && KitRules.hasInnate(player, AspectKitCatalog.STONE.innateId()))) {
            event.setCanceled(true);
        }
    }

    /** Tide's Breath: breathe underwater. Borrowed Breath: air runs out half again as fast. */
    public static void onBreathe(LivingBreatheEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.canBreathe()) {
            return;
        }
        if (KitRules.hasInnate(player, AspectKitCatalog.TIDE.innateId())) {
            event.setCanBreathe(true);
            event.setRefillAirAmount(4);
        } else if (event.getConsumeAirAmount() > 0
                && player.tickCount % 2 == 0
                && KitRules.hasFlaw(player, AspectKitCatalog.MIST.flawEffectId())) {
            event.setConsumeAirAmount(event.getConsumeAirAmount() + 1);
        }
    }

    /** Stone Sleep: waking from a full sleep leaves you slow and heavy. */
    public static void onWakeUp(PlayerWakeUpEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && player.isSleepingLongEnough()
                && KitRules.hasFlaw(player, AspectKitCatalog.STONE.flawEffectId())) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, STONE_SLEEP_TICKS, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, STONE_SLEEP_TICKS, 0, false, true));
        }
    }

    /** Once a second: Cold Ash (weak in water or rain) and Tidal Debt (weak if a day passes without water). */
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 20 != 0) {
            return;
        }
        if (KitRules.hasFlaw(player, AspectKitCatalog.EMBER.flawEffectId()) && player.isInWaterRainOrBubble()) {
            weaken(player);
        }
        if (KitRules.hasFlaw(player, AspectKitCatalog.TIDE.flawEffectId())) {
            tickTidalDebt(player);
        }
        if (player.tickCount % GATE_SENSE_TICKS == 0 && KitRules.hasInnate(player, AspectKitCatalog.WARDEN.innateId())) {
            senseGate(player);
        }
    }

    /** Twice a second for Seers: watch for creepers starting to hiss. */
    public static void onSeerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 10 == 0 && ForewarningService.isSeer(player)) {
            ForewarningService.tickSeer(player);
        }
    }

    /**
     * Gate Sense (Warden): a faint trail of light, seen only by the Warden,
     * drifting from them toward the nearest Gate that has not closed.
     */
    private static void senseGate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Optional<GateEvent> gate = GateService.nearest(level, player.blockPosition())
                .filter(found -> found.phase() != GateEvent.Phase.CLOSED
                        && found.center().distSqr(player.blockPosition()) <= GATE_SENSE_RANGE * GATE_SENSE_RANGE);
        if (gate.isEmpty()) {
            return;
        }
        Vec3 eye = player.getEyePosition();
        Vec3 toward = Vec3.atCenterOf(gate.get().center()).subtract(eye);
        if (toward.lengthSqr() < 9.0) {
            return;
        }
        Vec3 step = toward.normalize();
        for (int i = 3; i <= 10; i++) {
            Vec3 at = eye.add(step.scale(i * 0.8)).add(0, -0.3, 0);
            level.sendParticles(player, ParticleTypes.END_ROD, false, at.x, at.y, at.z, 1, 0.03, 0.03, 0.03, 0.0);
        }
    }

    private static void tickTidalDebt(ServerPlayer player) {
        long now = player.serverLevel().getGameTime();
        FlawStateData state = player.getData(ModAttachments.FLAW_STATE);
        if (player.isInWaterRainOrBubble() || state.lastWaterTouch() == FlawStateData.NEVER) {
            player.setData(ModAttachments.FLAW_STATE, state.withWaterTouch(now));
            return;
        }
        if (tidalDebtDue(state.lastWaterTouch(), now)) {
            weaken(player);
        }
    }

    static boolean tidalDebtDue(long lastWaterTouch, long now) {
        return now - lastWaterTouch >= TIDAL_DEBT_TICKS;
    }

    private static void weaken(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, WEAKNESS_REFRESH_TICKS, 0, false, true));
    }
}
