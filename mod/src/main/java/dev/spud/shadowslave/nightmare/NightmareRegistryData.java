package dev.spud.shadowslave.nightmare;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Overworld-owned persistent registry for active Nightmare instances and durable completion receipts. */
public final class NightmareRegistryData extends SavedData {
    private static final String DATA_NAME = "shadowslave_nightmares";
    private static final Factory<NightmareRegistryData> FACTORY = new Factory<>(
            NightmareRegistryData::new,
            NightmareRegistryData::load,
            null
    );

    private final Map<UUID, NightmareInstance> instances = new LinkedHashMap<>();
    private final Map<UUID, UUID> instanceByPlayer = new LinkedHashMap<>();
    private final Map<UUID, NightmareCompletionRecord> successfulCompletions = new LinkedHashMap<>();
    private int nextSlot;

    public static NightmareRegistryData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    public Optional<NightmareInstance> findByPlayer(UUID playerId) {
        UUID instanceId = instanceByPlayer.get(playerId);
        return instanceId == null ? Optional.empty() : Optional.ofNullable(instances.get(instanceId));
    }

    public Optional<NightmareCompletionRecord> findSuccessfulCompletionByPlayer(UUID playerId) {
        return Optional.ofNullable(successfulCompletions.get(playerId));
    }

    public Collection<NightmareInstance> activeInstances() {
        return List.copyOf(instances.values());
    }

    public Collection<NightmareCompletionRecord> successfulCompletionRecords() {
        return List.copyOf(successfulCompletions.values());
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

    /**
     * Records the terminal success while the active instance still exists.
     * Repeating the call for the same instance is idempotent.
     */
    public NightmareCompletionRecord beginSuccessfulCompletion(
            NightmareInstance expected,
            long resolvedGameTime
    ) {
        NightmareInstance checked = Objects.requireNonNull(expected, "expected");
        UUID registeredInstanceId = instanceByPlayer.get(checked.playerId());
        if (!checked.instanceId().equals(registeredInstanceId)) {
            throw new IllegalStateException("Cannot complete a Nightmare that is not the player's active instance");
        }

        NightmareCompletionRecord existing = successfulCompletions.get(checked.playerId());
        if (existing != null) {
            if (!existing.instance().instanceId().equals(checked.instanceId())) {
                throw new IllegalStateException("Player already has a completion receipt for another Nightmare");
            }
            return existing;
        }

        NightmareCompletionRecord created = new NightmareCompletionRecord(
                checked,
                NightmareCompletionPhase.TERMINAL_RESOLUTION_RECORDED,
                resolvedGameTime
        );
        successfulCompletions.put(checked.playerId(), created);
        setDirty();
        return created;
    }

    /**
     * Advances a receipt one durable milestone. Replaying an already-reached
     * target is idempotent; skipping a milestone is rejected.
     */
    public NightmareCompletionRecord advanceSuccessfulCompletion(
            NightmareInstance expected,
            NightmareCompletionPhase target
    ) {
        NightmareInstance checked = Objects.requireNonNull(expected, "expected");
        NightmareCompletionPhase checkedTarget = Objects.requireNonNull(target, "target");
        NightmareCompletionRecord existing = successfulCompletions.get(checked.playerId());
        if (existing == null || !existing.instance().instanceId().equals(checked.instanceId())) {
            throw new IllegalStateException("No matching successful Nightmare completion receipt exists");
        }
        if (checkedTarget.ordinal() <= existing.phase().ordinal()) {
            return existing;
        }
        if (checkedTarget.ordinal() != existing.phase().ordinal() + 1) {
            throw new IllegalStateException(
                    "Cannot skip Nightmare completion phase from " + existing.phase() + " to " + checkedTarget
            );
        }

        NightmareCompletionRecord advanced = existing.advanceTo(checkedTarget);
        successfulCompletions.put(checked.playerId(), advanced);
        setDirty();
        return advanced;
    }

    /** Removes a retained completion receipt during an explicit development reset. */
    public Optional<NightmareCompletionRecord> clearSuccessfulCompletionByPlayer(UUID playerId) {
        NightmareCompletionRecord removed = successfulCompletions.remove(playerId);
        if (removed != null) {
            setDirty();
        }
        return Optional.ofNullable(removed);
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

        ListTag encodedInstances = new ListTag();
        for (NightmareInstance instance : instances.values()) {
            encodedInstances.add(instance.save());
        }
        tag.put("instances", encodedInstances);

        ListTag encodedCompletions = new ListTag();
        for (NightmareCompletionRecord completion : successfulCompletions.values()) {
            encodedCompletions.add(completion.save());
        }
        tag.put("successful_completions", encodedCompletions);
        return tag;
    }

    static NightmareRegistryData load(CompoundTag tag, HolderLookup.Provider registries) {
        NightmareRegistryData data = new NightmareRegistryData();
        data.nextSlot = Math.max(0, tag.getInt("next_slot"));

        ListTag encodedInstances = tag.getList("instances", Tag.TAG_COMPOUND);
        for (int index = 0; index < encodedInstances.size(); index++) {
            data.restore(NightmareInstance.load(encodedInstances.getCompound(index)));
        }

        ListTag encodedCompletions = tag.getList("successful_completions", Tag.TAG_COMPOUND);
        for (int index = 0; index < encodedCompletions.size(); index++) {
            data.restoreSuccessfulCompletion(NightmareCompletionRecord.load(encodedCompletions.getCompound(index)));
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
        NightmareCompletionRecord completion = successfulCompletions.get(checked.playerId());
        if (completion != null && !completion.instance().instanceId().equals(checked.instanceId())) {
            throw new IllegalStateException(
                    "Player active Nightmare does not match retained successful completion in SavedData"
            );
        }
        boolean completionUsesInstanceForAnotherPlayer = successfulCompletions.values().stream()
                .anyMatch(existing -> existing.instance().instanceId().equals(checked.instanceId())
                        && !existing.instance().playerId().equals(checked.playerId()));
        if (completionUsesInstanceForAnotherPlayer) {
            throw new IllegalStateException(
                    "Nightmare instance ID belongs to another player's retained successful completion in SavedData"
            );
        }

        instances.put(checked.instanceId(), checked);
        instanceByPlayer.put(checked.playerId(), checked.instanceId());
        nextSlot = Math.max(nextSlot, checked.slot() + 1);
    }

    /** Package-visible restart hook for direct persistence tests. */
    void restoreSuccessfulCompletion(NightmareCompletionRecord completion) {
        NightmareCompletionRecord checked = Objects.requireNonNull(completion, "completion");
        UUID playerId = checked.instance().playerId();
        if (successfulCompletions.containsKey(playerId)) {
            throw new IllegalStateException("Player has multiple successful Nightmare receipts in SavedData");
        }
        boolean duplicateInstance = successfulCompletions.values().stream()
                .anyMatch(existing -> existing.instance().instanceId().equals(checked.instance().instanceId()));
        if (duplicateInstance) {
            throw new IllegalStateException("Duplicate successful Nightmare instance ID in SavedData");
        }
        NightmareInstance activeWithSameInstanceId = instances.get(checked.instance().instanceId());
        if (activeWithSameInstanceId != null && !activeWithSameInstanceId.playerId().equals(playerId)) {
            throw new IllegalStateException(
                    "Nightmare instance ID belongs to another player's active Nightmare in SavedData"
            );
        }
        UUID activeInstanceId = instanceByPlayer.get(playerId);
        if (activeInstanceId != null && !activeInstanceId.equals(checked.instance().instanceId())) {
            throw new IllegalStateException(
                    "Player retained successful completion does not match active Nightmare in SavedData"
            );
        }
        successfulCompletions.put(playerId, checked);
        nextSlot = Math.max(nextSlot, checked.instance().slot() + 1);
    }
}
