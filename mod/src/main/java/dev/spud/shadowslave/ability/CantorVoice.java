package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.fieldguide.FieldGuideService;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The always-on halves of the Cantor: Attuned (the Spell's Callings come
 * twice as often and say a little more) and Spoken Self (each ability a
 * Cantor uses shows where they stand on everyone else's Field Guide map for
 * ten seconds).
 */
public final class CantorVoice {
    /** Attuned: the wait between Callings, as a share of the player's chosen gap. */
    public static final double ATTUNED_GAP = 0.5;
    static final int SPOKEN_TICKS = 20 * 10;

    /** Where a Cantor's voice was last heard, and until when the maps show it. */
    public record Spoken(UUID speaker, String name, String dimension, int x, int z, long until) {
    }

    private static final Map<UUID, Spoken> SPOKEN = new ConcurrentHashMap<>();

    private CantorVoice() {
    }

    public static boolean attuned(ServerPlayer player) {
        return KitRules.hasInnate(player, AspectKitCatalog.CANTOR.innateId());
    }

    static boolean spokenSelf(ServerPlayer player) {
        return KitRules.hasFlaw(player, AspectKitCatalog.CANTOR.flawEffectId());
    }

    /** Spoken Self: an ability was used, so the voice carries to every other Field Guide. */
    static void onAbilityUsed(ServerPlayer player) {
        if (!spokenSelf(player)) {
            return;
        }
        SPOKEN.put(player.getUUID(), new Spoken(player.getUUID(), player.getGameProfile().getName(),
                FieldGuideService.dimension(player.level()), player.getBlockX(), player.getBlockZ(),
                player.serverLevel().getGameTime() + SPOKEN_TICKS));
        refreshOthers(player.server, player.getUUID());
    }

    /** The voices a viewer's map shows right now: everyone's but their own. */
    public static List<Spoken> heard(ServerPlayer viewer) {
        long now = viewer.server.overworld().getGameTime();
        return SPOKEN.values().stream()
                .filter(spoken -> !spoken.speaker().equals(viewer.getUUID()) && spoken.until() > now)
                .toList();
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        if (SPOKEN.isEmpty() || event.getServer().getTickCount() % 20 != 0) {
            return;
        }
        long now = event.getServer().overworld().getGameTime();
        for (Spoken spoken : List.copyOf(SPOKEN.values())) {
            if (spoken.until() <= now && SPOKEN.remove(spoken.speaker(), spoken)) {
                refreshOthers(event.getServer(), spoken.speaker());
            }
        }
    }

    private static void refreshOthers(MinecraftServer server, UUID speaker) {
        for (ServerPlayer other : server.getPlayerList().getPlayers()) {
            if (!other.getUUID().equals(speaker)) {
                FieldGuideService.markDirty(other);
            }
        }
    }

    public static void forget(UUID player) {
        SPOKEN.remove(player);
    }

    public static void onServerStopping(ServerStoppingEvent event) {
        SPOKEN.clear();
    }
}
