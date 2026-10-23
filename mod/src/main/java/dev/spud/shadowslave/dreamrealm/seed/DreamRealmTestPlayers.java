package dev.spud.shadowslave.dreamrealm.seed;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;

/** Sends a mock player back to its test room so it does not linger in the Dream Realm. */
final class DreamRealmTestPlayers {
    private DreamRealmTestPlayers() {
    }

    static void home(ServerPlayer player, GameTestHelper helper) {
        var room = helper.absoluteVec(new net.minecraft.world.phys.Vec3(2.5, 1, 2.5));
        player.teleportTo(helper.getLevel(), room.x, room.y, room.z, java.util.Set.of(), 0, 0);
    }
}
