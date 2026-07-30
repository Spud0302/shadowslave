package dev.spud.shadowslave.client;

import dev.spud.shadowslave.network.payload.SoulSnapshot;

import java.util.Optional;

/** Client-only cache for rendering; never treated as authoritative gameplay state. */
public final class ClientSoulState {
    private static SoulSnapshot latest;

    private ClientSoulState() {
    }

    public static void update(SoulSnapshot snapshot) {
        latest = snapshot;
    }

    public static Optional<SoulSnapshot> latest() {
        return Optional.ofNullable(latest);
    }

    public static void clear() {
        latest = null;
    }
}
