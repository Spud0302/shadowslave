package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.awakening.KitLadder;
import dev.spud.shadowslave.echoes.EchoService;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * The always-on halves of Shepherd, and the Echo slot both Echo Natures give a
 * Dormant soul: Herdbond's cheaper upkeep, Second Bond's extra slot, and
 * Lonely Soul's weaker blows while no Echo walks beside you.
 */
public final class ShepherdBond {
    /** Herdbond: the share of upkeep a Shepherd still pays. */
    static final float HERDBOND_UPKEEP = 0.5F;
    /** Lonely Soul: what a Shepherd's blow keeps while no Echo is summoned. */
    static final float LONELY_DAMAGE = 0.8F;

    private ShepherdBond() {
    }

    static boolean herdbond(ServerPlayer player) {
        return KitRules.hasInnate(player, AspectKitCatalog.SHEPHERD.innateId());
    }

    static boolean vessel(ServerPlayer player) {
        return KitRules.hasInnate(player, AspectKitCatalog.VESSEL.innateId());
    }

    /**
     * Echo slots for a soul, from what its rank gives: Shepherd and Vessel souls
     * hold one even while Dormant, and Second Bond adds one more.
     */
    public static int slots(ServerPlayer player, int base) {
        int slots = base;
        if (herdbond(player) || vessel(player)) {
            slots = Math.max(slots, 1);
        }
        if (KitRules.hasInnate(player, KitLadder.SECOND_BOND.abilityId())) {
            slots++;
        }
        return slots;
    }

    /** The share of Echo upkeep this player pays. */
    public static float upkeepShare(ServerPlayer player) {
        return herdbond(player) ? HERDBOND_UPKEEP : 1.0F;
    }

    static boolean lonely(ServerPlayer player) {
        return KitRules.hasFlaw(player, AspectKitCatalog.SHEPHERD.flawEffectId()) && EchoService.summonedCount(player) == 0;
    }

    /** Lonely Soul: a Shepherd with no Echo out hits a fifth weaker. */
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer attacker && attacker != event.getEntity()
                && event.getSource().getDirectEntity() == attacker && lonely(attacker)) {
            event.setAmount(event.getAmount() * LONELY_DAMAGE);
        }
    }
}
