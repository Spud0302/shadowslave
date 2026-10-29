package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.citadel.CitadelService;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Keeper's tie to clan land: Hearthhold (abilities cost half on land the
 * clan has claimed) and Homebound (away from it, the essence pool holds a
 * third less). Whether a Keeper stands on clan land is checked once a second.
 */
public final class KeeperHold {
    static final float HOMEBOUND_CAPACITY = 2.0F / 3.0F;
    static final int CHECK_TICKS = 20;

    /** Whether each Keeper stood on clan land at the last check. */
    private static final Map<UUID, Boolean> ON_LAND = new ConcurrentHashMap<>();

    private KeeperHold() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % CHECK_TICKS != 0) {
            return;
        }
        if (!keeper(player)) {
            ON_LAND.remove(player.getUUID());
            return;
        }
        boolean now = CitadelService.onClanLand(player);
        Boolean before = ON_LAND.put(player.getUUID(), now);
        if (before != null && before != now) {
            AbilityService.feedback(player, now ? "You are home. The Citadel's walls steady your soul."
                    : "You leave your clan's land; your soul feels thin.", net.minecraft.ChatFormatting.GRAY);
            AbilityService.syncStatus(player);
        }
    }

    static boolean keeper(ServerPlayer player) {
        return KitRules.hasInnate(player, AspectKitCatalog.KEEPER.innateId())
                || KitRules.hasFlaw(player, AspectKitCatalog.KEEPER.flawEffectId());
    }

    public static boolean onLand(ServerPlayer player) {
        return ON_LAND.computeIfAbsent(player.getUUID(), id -> CitadelService.onClanLand(player));
    }

    /** Hearthhold: half price on clan land, rounded up. */
    public static int costFor(ServerPlayer player, int cost) {
        return KitRules.hasInnate(player, AspectKitCatalog.KEEPER.innateId()) && onLand(player) ? halved(cost) : cost;
    }

    static int halved(int cost) {
        return (cost + 1) / 2;
    }

    /** Homebound: away from clan land the pool holds a third less. */
    public static int capacityFor(ServerPlayer player, int capacity) {
        return KitRules.hasFlaw(player, AspectKitCatalog.KEEPER.flawEffectId()) && !onLand(player)
                ? homebound(capacity) : capacity;
    }

    static int homebound(int capacity) {
        return Math.round(capacity * HOMEBOUND_CAPACITY);
    }

    /** Checks again now, for tests and after a claim. */
    public static void recheck(ServerPlayer player) {
        ON_LAND.put(player.getUUID(), CitadelService.onClanLand(player));
    }

    public static void forget(UUID player) {
        ON_LAND.remove(player);
    }
}
