package dev.spud.shadowslave.network;

import dev.spud.shadowslave.ability.AbilityService;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Server-side lifecycle hooks for keeping the owning client current. */
public final class SoulPlayerEvents {
    private SoulPlayerEvents() {
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SoulSyncService.sync(player);
            AbilityService.syncStatus(player);
        }
    }
}
