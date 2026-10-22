package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.attachment.ModAttachments;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.entity.living.LivingBreatheEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * The always-on half of the four kits: each Nature's innate rule and its Flaw.
 * All server-side; Stone Sleep's sinking runs on the client (ClientKitEffects)
 * because player movement is client-driven.
 */
public final class KitPassives {
    static final float EMBER_FIRE_DAMAGE_FACTOR = 0.5F;
    static final double STILL_SPEED_SQR = 0.0001;
    static final int STONE_SLEEP_TICKS = 20 * 20;
    static final long TIDAL_DEBT_TICKS = 24000L;
    private static final int WEAKNESS_REFRESH_TICKS = 40;

    private KitPassives() {
    }

    /** Ember's Warmth: fire and lava hurt half as much. */
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && event.getSource().is(DamageTypeTags.IS_FIRE)
                && KitRules.hasInnate(player, AspectKitCatalog.EMBER.innateId())) {
            event.setAmount(event.getAmount() * EMBER_FIRE_DAMAGE_FACTOR);
        }
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
