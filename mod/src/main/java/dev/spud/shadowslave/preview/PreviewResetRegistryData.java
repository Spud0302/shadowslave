package dev.spud.shadowslave.preview;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Durable intent marker for compound preview resets that must replay across restart. */
public final class PreviewResetRegistryData extends SavedData {
    private static final String DATA_NAME = "shadowslave_preview_resets";
    private static final Factory<PreviewResetRegistryData> FACTORY = new Factory<>(
            PreviewResetRegistryData::new,
            PreviewResetRegistryData::load,
            null
    );

    private final Set<UUID> pendingPlayers = new LinkedHashSet<>();
    private final String loadFailure;

    public PreviewResetRegistryData() {
        this(null);
    }

    private PreviewResetRegistryData(String loadFailure) {
        this.loadFailure = loadFailure;
    }

    public static PreviewResetRegistryData get(MinecraftServer server) {
        return Objects.requireNonNull(server, "server")
                .overworld()
                .getDataStorage()
                .computeIfAbsent(FACTORY, DATA_NAME);
    }

    /** A corrupt marker has unknown ownership, so Nightmare recovery must stop globally rather than guess. */
    public boolean recoveryBlocked() {
        return loadFailure != null;
    }

    public Optional<String> loadFailure() {
        return Optional.ofNullable(loadFailure);
    }

    public boolean isPending(UUID playerId) {
        requireHealthy();
        return pendingPlayers.contains(Objects.requireNonNull(playerId, "playerId"));
    }

    /** Idempotently records reset intent before any other durable reset mutation. */
    public void begin(UUID playerId) {
        requireHealthy();
        if (pendingPlayers.add(Objects.requireNonNull(playerId, "playerId"))) {
            setDirty();
        }
    }

    /** Clears reset intent only after player state has been saved and the final snapshot has been published. */
    public void complete(UUID playerId) {
        requireHealthy();
        if (pendingPlayers.remove(Objects.requireNonNull(playerId, "playerId"))) {
            setDirty();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        requireHealthy();
        ListTag encoded = new ListTag();
        for (UUID playerId : pendingPlayers) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("player_id", playerId);
            encoded.add(entry);
        }
        tag.put("pending", encoded);
        return tag;
    }

    static PreviewResetRegistryData load(CompoundTag tag, HolderLookup.Provider registries) {
        Tag pendingTag = tag.get("pending");
        if (!(pendingTag instanceof ListTag encoded)) {
            return blocked("Preview reset marker is missing a pending list");
        }

        PreviewResetRegistryData data = new PreviewResetRegistryData();
        for (int index = 0; index < encoded.size(); index++) {
            Tag rawEntry = encoded.get(index);
            if (!(rawEntry instanceof CompoundTag entry)) {
                return blocked("Preview reset marker entry " + index + " is not a compound");
            }
            if (!entry.hasUUID("player_id")) {
                return blocked("Preview reset marker entry " + index + " is missing player_id");
            }
            UUID playerId = entry.getUUID("player_id");
            if (!data.pendingPlayers.add(playerId)) {
                return blocked("Duplicate preview reset marker for player " + playerId);
            }
        }
        return data;
    }

    private static PreviewResetRegistryData blocked(String loadFailure) {
        return new PreviewResetRegistryData(Objects.requireNonNull(loadFailure, "loadFailure"));
    }

    private void requireHealthy() {
        if (loadFailure != null) {
            throw new IllegalStateException("Preview reset recovery is blocked: " + loadFailure);
        }
    }
}
