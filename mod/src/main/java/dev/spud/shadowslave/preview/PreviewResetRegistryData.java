package dev.spud.shadowslave.preview;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.LinkedHashSet;
import java.util.Objects;
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

    public static PreviewResetRegistryData get(MinecraftServer server) {
        return Objects.requireNonNull(server, "server")
                .overworld()
                .getDataStorage()
                .computeIfAbsent(FACTORY, DATA_NAME);
    }

    public boolean isPending(UUID playerId) {
        return pendingPlayers.contains(Objects.requireNonNull(playerId, "playerId"));
    }

    /** Idempotently records reset intent before any other durable reset mutation. */
    public void begin(UUID playerId) {
        if (pendingPlayers.add(Objects.requireNonNull(playerId, "playerId"))) {
            setDirty();
        }
    }

    /** Clears reset intent only after player state has been saved and the final snapshot has been published. */
    public void complete(UUID playerId) {
        if (pendingPlayers.remove(Objects.requireNonNull(playerId, "playerId"))) {
            setDirty();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
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
        PreviewResetRegistryData data = new PreviewResetRegistryData();
        ListTag encoded = tag.getList("pending", Tag.TAG_COMPOUND);
        for (int index = 0; index < encoded.size(); index++) {
            CompoundTag entry = encoded.getCompound(index);
            if (!entry.hasUUID("player_id")) {
                throw new IllegalStateException("Preview reset marker is missing player_id");
            }
            UUID playerId = entry.getUUID("player_id");
            if (!data.pendingPlayers.add(playerId)) {
                throw new IllegalStateException("Duplicate preview reset marker for player " + playerId);
            }
        }
        return data;
    }
}
