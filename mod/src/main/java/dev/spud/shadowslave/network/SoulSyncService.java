package dev.spud.shadowslave.network;

import dev.spud.shadowslave.network.payload.SoulSnapshot;
import dev.spud.shadowslave.network.payload.SoulSnapshotPayload;
import dev.spud.shadowslave.soul.SoulData;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.soul.identity.SoulIdentityService;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Objects;

/** Sends intentionally limited snapshots of authoritative server Soul state. */
public final class SoulSyncService {
    private SoulSyncService() {
    }

    public static void sync(ServerPlayer player) {
        sync(player, SoulService.get(player), false);
    }

    public static void openScreen(ServerPlayer player) {
        sync(player, SoulService.get(player), true);
    }

    public static void sync(ServerPlayer player, SoulData soul, boolean openScreen) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(soul, "soul");
        PacketDistributor.sendToPlayer(
                player,
                new SoulSnapshotPayload(
                        SoulSnapshot.from(soul, SoulIdentityService.get(player)),
                        openScreen
                )
        );
    }
}
