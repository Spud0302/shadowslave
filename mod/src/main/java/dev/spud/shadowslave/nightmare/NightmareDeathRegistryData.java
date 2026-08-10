package dev.spud.shadowslave.nightmare;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Durable canonical-death intent retained until player reset and Nightmare teardown are both committed. */
public final class NightmareDeathRegistryData extends SavedData {
    private static final String DATA_NAME = "shadowslave_nightmare_deaths";
    private static final Factory<NightmareDeathRegistryData> FACTORY = new Factory<>(
            NightmareDeathRegistryData::new,
            NightmareDeathRegistryData::load,
            null
    );

    private final Map<UUID, NightmareInstance> pendingByPlayer = new LinkedHashMap<>();
    private final Set<UUID> unverifiedByPlayer = new HashSet<>();
    private final String loadFailure;
    private long persistenceRevision;

    public NightmareDeathRegistryData() {
        this(null, 0L);
    }

    private NightmareDeathRegistryData(String loadFailure, long persistenceRevision) {
        this.loadFailure = loadFailure;
        this.persistenceRevision = persistenceRevision;
    }

    public static NightmareDeathRegistryData get(MinecraftServer server) {
        return Objects.requireNonNull(server, "server")
                .overworld()
                .getDataStorage()
                .computeIfAbsent(FACTORY, DATA_NAME);
    }

    public boolean recoveryBlocked() {
        return loadFailure != null;
    }

    public Optional<String> loadFailure() {
        return Optional.ofNullable(loadFailure);
    }

    public Optional<NightmareInstance> findByPlayer(UUID playerId) {
        requireHealthy();
        return Optional.ofNullable(pendingByPlayer.get(Objects.requireNonNull(playerId, "playerId")));
    }

    /** Idempotently records the exact active instance before any death-side durable mutation. */
    void begin(NightmareInstance instance) {
        requireHealthy();
        NightmareInstance checked = Objects.requireNonNull(instance, "instance");
        NightmareInstance existing = pendingByPlayer.get(checked.playerId());
        if (existing != null) {
            if (!existing.equals(checked)) {
                throw new IllegalStateException("Player already has canonical death intent for another Nightmare snapshot");
            }
            if (unverifiedByPlayer.contains(checked.playerId())) {
                advancePersistenceRevision();
            }
            return;
        }
        pendingByPlayer.put(checked.playerId(), checked);
        unverifiedByPlayer.add(checked.playerId());
        advancePersistenceRevision();
    }

    /** Returns true only for an exact marker already proven durable in this process or loaded from disk. */
    boolean isDurablyTrusted(NightmareInstance instance) {
        requireHealthy();
        NightmareInstance checked = Objects.requireNonNull(instance, "instance");
        NightmareInstance existing = pendingByPlayer.get(checked.playerId());
        return checked.equals(existing) && !unverifiedByPlayer.contains(checked.playerId());
    }

    /** Promotes only the exact live marker after its settled persistence checkpoint succeeds. */
    void markDurablyTrusted(NightmareInstance instance) {
        requireHealthy();
        NightmareInstance checked = Objects.requireNonNull(instance, "instance");
        NightmareInstance existing = pendingByPlayer.get(checked.playerId());
        if (!checked.equals(existing)) {
            throw new IllegalStateException("Cannot trust canonical death intent without the exact live marker");
        }
        unverifiedByPlayer.remove(checked.playerId());
    }

    /** Clears only the exact marker after player reset and Nightmare teardown are durably committed. */
    void complete(NightmareInstance instance) {
        requireHealthy();
        NightmareInstance checked = Objects.requireNonNull(instance, "instance");
        NightmareInstance existing = pendingByPlayer.get(checked.playerId());
        if (existing == null) {
            return;
        }
        if (!existing.equals(checked)) {
            throw new IllegalStateException("Cannot clear canonical death intent with a stale or modified snapshot");
        }
        pendingByPlayer.remove(checked.playerId());
        unverifiedByPlayer.remove(checked.playerId());
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        requireHealthy();
        ListTag encoded = new ListTag();
        for (NightmareInstance instance : pendingByPlayer.values()) {
            encoded.add(instance.save());
        }
        tag.put("pending", encoded);
        tag.putLong("persistence_revision", persistenceRevision);
        return tag;
    }

    static NightmareDeathRegistryData load(CompoundTag tag, HolderLookup.Provider registries) {
        Tag pendingTag = tag.get("pending");
        if (!(pendingTag instanceof ListTag encoded)) {
            return blocked("Canonical death marker is missing a pending list");
        }

        NightmareDeathRegistryData data = new NightmareDeathRegistryData(null, tag.getLong("persistence_revision"));
        for (int index = 0; index < encoded.size(); index++) {
            Tag rawEntry = encoded.get(index);
            if (!(rawEntry instanceof CompoundTag entry)) {
                return blocked("Canonical death marker entry " + index + " is not a compound");
            }
            NightmareInstance instance;
            try {
                instance = NightmareInstance.load(entry);
            } catch (RuntimeException exception) {
                return blocked("Canonical death marker entry " + index + " is malformed: " + exception.getMessage());
            }
            NightmareInstance previous = data.pendingByPlayer.putIfAbsent(instance.playerId(), instance);
            if (previous != null) {
                return blocked("Duplicate canonical death marker for player " + instance.playerId());
            }
        }
        return data;
    }

    private void advancePersistenceRevision() {
        persistenceRevision++;
        setDirty();
    }

    private static NightmareDeathRegistryData blocked(String loadFailure) {
        return new NightmareDeathRegistryData(Objects.requireNonNull(loadFailure, "loadFailure"), 0L);
    }

    private void requireHealthy() {
        if (loadFailure != null) {
            throw new IllegalStateException("Canonical Nightmare death recovery is blocked: " + loadFailure);
        }
    }
}
