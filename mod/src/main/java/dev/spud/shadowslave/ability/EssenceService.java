package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.attachment.ModAttachments;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.awakening.SoulSeaService;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.soul.refinement.RefinementRules;
import dev.spud.shadowslave.soul.refinement.RefinementService;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Essence, the one pool every Aspect ability draws on (DESIGN). There are no
 * cooldowns: the pool refills over time, so choosing what to spend it on is
 * the skill. A Sleeper already spends essence on their Dormant ability; the
 * pool grows with Soul Rank.
 */
public final class EssenceService {
    public static final int DORMANT_CAPACITY = 100;
    public static final int AWAKENED_CAPACITY = 200;
    /** An empty pool refills in this many ticks. */
    static final int FULL_REFILL_TICKS = 20 * 60;
    /** How often a quiet pool is resent to the client to correct drift in its prediction. */
    static final int RESYNC_TICKS = 20 * 5;
    /** Deep Well (Wellspring): refill speed while meditating at the Dream Candle, and while sneaking still. */
    static final float DEEP_WELL_MEDITATING = 3.0F;
    static final float DEEP_WELL_GATHERING = 2.0F;

    /** The refill rate each player's client last heard, so a change of pace is sent straight away. */
    private static final Map<UUID, Float> LAST_SENT_REGEN = new ConcurrentHashMap<>();

    private EssenceService() {
    }

    /** Essence the player's core holds when full; 0 before the Spell names an Aspect. */
    public static int capacity(ServerPlayer player) {
        int base = capacityFor(SoulService.get(player).soulRank());
        return base <= 0 ? 0 : base + RefinementRules.capacityBonus(RefinementService.get(player));
    }

    static int capacityFor(Optional<SoulRank> rank) {
        return rank.map(EssenceService::baseCapacity).orElse(0);
    }

    static int baseCapacity(SoulRank rank) {
        return switch (rank) {
            case DORMANT -> DORMANT_CAPACITY;
            case AWAKENED -> AWAKENED_CAPACITY;
            // Higher ranks are not playable yet; keep the curve going so admin tools do not break.
            default -> AWAKENED_CAPACITY * (rank.ordinal());
        };
    }

    /** Essence regained per tick, after refinement and the kit's own rules (Hunger, Deep Well, Rootbound, Open Sky). */
    public static float regenPerTick(ServerPlayer player) {
        float base = (float) (regenPerTickFor(capacity(player)) * RefinementRules.regenMultiplier(RefinementService.get(player)));
        float factor = kitRegenFactor(KitRules.hasFlaw(player, AspectKitCatalog.REAPER.flawEffectId()),
                KitRules.hasInnate(player, AspectKitCatalog.WELLSPRING.innateId()),
                SoulSeaService.meditating(player), gathering(player));
        if (KitRules.hasFlaw(player, AspectKitCatalog.VERDANT.flawEffectId()) && !KitPassives.onLivingGround(player)) {
            factor = 0.0F;
        }
        if (KitRules.hasFlaw(player, AspectKitCatalog.DELVE.flawEffectId()) && KitPassives.underOpenSky(player)) {
            factor *= KitPassives.OPEN_SKY_REGEN;
        }
        factor *= DreamKitPassives.wanderlust(player);
        return base * factor;
    }

    /**
     * Hunger (Reaper): no refill at all, except while meditating at the
     * Candle. Deep Well (Wellspring): three times as fast while meditating,
     * twice while sneaking still.
     */
    static float kitRegenFactor(boolean hunger, boolean deepWell, boolean meditating, boolean gathering) {
        float factor = hunger && !meditating ? 0.0F : 1.0F;
        if (deepWell) {
            factor *= meditating ? DEEP_WELL_MEDITATING : gathering ? DEEP_WELL_GATHERING : 1.0F;
        }
        return factor;
    }

    /** Sneaking and standing still: a Wellspring gathering essence. */
    static boolean gathering(ServerPlayer player) {
        return player.isCrouching() && player.getKnownMovement().horizontalDistanceSqr() < KitPassives.STILL_SPEED_SQR;
    }

    static float regenPerTickFor(int capacity) {
        return (float) capacity / FULL_REFILL_TICKS;
    }

    public static float current(ServerPlayer player) {
        return Math.min(capacity(player), player.getData(ModAttachments.ESSENCE).current());
    }

    public static boolean covers(ServerPlayer player, int cost) {
        return new EssenceData(current(player)).covers(cost);
    }

    /** Takes up to {@code amount} essence; the pool never goes below zero. */
    public static void spend(ServerPlayer player, int amount) {
        player.setData(ModAttachments.ESSENCE, new EssenceData(current(player)).spend(amount));
    }

    /** Adds essence, never past the pool's size; kills, drains and gifts use this. */
    public static void gain(ServerPlayer player, float amount) {
        int capacity = capacity(player);
        if (capacity > 0 && amount > 0) {
            player.setData(ModAttachments.ESSENCE, new EssenceData(current(player)).refill(amount, capacity));
        }
    }

    /** Fills the pool, as on a fresh roll or a rank-up. */
    public static void fill(ServerPlayer player) {
        player.setData(ModAttachments.ESSENCE, new EssenceData(capacity(player)));
    }

    /** Refills a little each tick; returns true when the client should get a fresh reading. */
    static boolean tick(ServerPlayer player) {
        int capacity = capacity(player);
        if (capacity <= 0) {
            return false;
        }
        EssenceData before = player.getData(ModAttachments.ESSENCE);
        float regen = regenPerTick(player);
        Float sent = LAST_SENT_REGEN.put(player.getUUID(), regen);
        boolean paceChanged = sent != null && Math.abs(sent - regen) > 1.0e-6F;
        if (before.current() < capacity) {
            player.setData(ModAttachments.ESSENCE, before.refill(regen, capacity));
            return paceChanged || player.tickCount % RESYNC_TICKS == 0;
        }
        if (before.current() > capacity) {
            player.setData(ModAttachments.ESSENCE, new EssenceData(capacity));
            return true;
        }
        return paceChanged;
    }

    static void forget(UUID player) {
        LAST_SENT_REGEN.remove(player);
    }
}
