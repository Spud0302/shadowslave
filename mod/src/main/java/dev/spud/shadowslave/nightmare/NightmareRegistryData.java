package dev.spud.shadowslave.nightmare;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Overworld-owned persistent registry for active Nightmare instances. */
public final class NightmareRegistryData extends SavedData {
    static final String DATA_NAME = "shadowslave_nightmares";
    private static final Factory<NightmareRegistryData> FACTORY = new Factory<>(
            NightmareRegistryData::new,
            NightmareRegistryData::load,
            null
    );

    private final Map<UUID, NightmareInstance> instances = new LinkedHashMap<>();
    private final Map<UUID, UUID> instanceByPlayer = new LinkedHashMap<>();
    private int nextSlot;

    public static NightmareRegistryData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    static Path persistedFile(MinecraftServer server) {
        return Objects.requireNonNull(server, "server")
                .getWorldPath(LevelResource.ROOT)
                .resolve("data")
                .resolve(DATA_NAME + ".dat");
    }

    public Optional<NightmareInstance> findByPlayer(UUID playerId) {
        UUID instanceId = instanceByPlayer.get(playerId);
        return instanceId == null ? Optional.empty() : Optional.ofNullable(instances.get(instanceId));
    }

    public Collection<NightmareInstance> activeInstances() {
        return List.copyOf(instances.values());
    }

    public NightmareInstance create(ServerPlayer player, String scenarioId, String historicalRoleId) {
        if (findByPlayer(player.getUUID()).isPresent()) {
            throw new IllegalStateException("Player already owns an active Nightmare instance");
        }

        int slot = nextSlot++;
        NightmareInstance created = new NightmareInstance(
                UUID.randomUUID(),
                player.getUUID(),
                slot,
                scenarioId,
                historicalRoleId,
                player.serverLevel().dimension().location(),
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getYRot(),
                player.getXRot(),
                BlockPos.ZERO,
                BlockPos.ZERO,
                Optional.empty(),
                player.serverLevel().getGameTime()
        );
        instances.put(created.instanceId(), created);
        instanceByPlayer.put(created.playerId(), created.instanceId());
        setDirty();
        return created;
    }

    public void update(NightmareInstance instance) {
        NightmareInstance existing = instances.get(instance.instanceId());
        if (existing == null || !existing.playerId().equals(instance.playerId())) {
            throw new IllegalStateException("Cannot update an unregistered Nightmare instance");
        }
        instances.put(instance.instanceId(), instance);
        instanceByPlayer.put(instance.playerId(), instance.instanceId());
        setDirty();
    }

    /** Removes only the exact ownership record that the caller previously resolved. */
    public Optional<NightmareInstance> remove(NightmareInstance expected) {
        NightmareInstance checked = Objects.requireNonNull(expected, "expected");
        UUID registeredInstanceId = instanceByPlayer.get(checked.playerId());
        if (!checked.instanceId().equals(registeredInstanceId)) {
            return Optional.empty();
        }
        return removeByPlayer(checked.playerId());
    }

    public Optional<NightmareInstance> removeByPlayer(UUID playerId) {
        UUID instanceId = instanceByPlayer.remove(playerId);
        if (instanceId == null) {
            return Optional.empty();
        }
        NightmareInstance removed = instances.remove(instanceId);
        setDirty();
        return Optional.ofNullable(removed);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("next_slot", nextSlot);
        ListTag encoded = new ListTag();
        for (NightmareInstance instance : instances.values()) {
            encoded.add(instance.save());
        }
        tag.put("instances", encoded);
        return tag;
    }

    static NightmareRegistryData load(CompoundTag tag, HolderLookup.Provider registries) {
        NightmareRegistryData data = new NightmareRegistryData();
        data.nextSlot = Math.max(0, tag.getInt("next_slot"));
        ListTag encoded = tag.getList("instances", Tag.TAG_COMPOUND);
        for (int index = 0; index < encoded.size(); index++) {
            data.restore(NightmareInstance.load(encoded.getCompound(index)));
        }
        return data;
    }

    /**
     * Rebuilds one persisted ownership record without exposing a second runtime entry path.
     * Package visibility keeps the restart boundary directly testable.
     */
    void restore(NightmareInstance instance) {
        NightmareInstance checked = Objects.requireNonNull(instance, "instance");
        if (instances.containsKey(checked.instanceId())) {
            throw new IllegalStateException("Duplicate Nightmare instance ID in SavedData");
        }
        if (instanceByPlayer.containsKey(checked.playerId())) {
            throw new IllegalStateException("Player owns multiple active Nightmares in SavedData");
        }

        instances.put(checked.instanceId(), checked);
        instanceByPlayer.put(checked.playerId(), checked.instanceId());
        nextSlot = Math.max(nextSlot, checked.slot() + 1);
    }
}
