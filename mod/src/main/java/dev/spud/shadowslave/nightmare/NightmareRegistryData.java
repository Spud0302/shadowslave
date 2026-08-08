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
        UUID playerId = player.getUUID();
        ensurePlayerCanCreate(playerId);

        int slot = nextSlot++;
        NightmareInstance created = new NightmareInstance(
                UUID.randomUUID(),
                playerId,
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

    void ensurePlayerCanCreate(UUID playerId) {
        UUID checkedPlayerId = Objects.requireNonNull(playerId, "playerId");
        if (findByPlayer(checkedPlayerId).isPresent()) {
            throw new IllegalStateException("Player already owns an active Nightmare instance");
        }
        if (successfulCompletions.containsKey(checkedPlayerId)) {
            throw new IllegalStateException("Player still has a retained successful Nightmare completion receipt");
        }
    }

    public void update(NightmareInstance instance) {
        NightmareInstance checked = Objects.requireNonNull(instance, "instance");
        NightmareInstance existing = instances.get(checked.instanceId());
        if (existing == null || !existing.playerId().equals(checked.playerId())) {
            throw new IllegalStateException("Cannot update an unregistered Nightmare instance");
        }
        if (existing.slot() != checked.slot()) {
            throw new IllegalStateException("Cannot change a Nightmare instance's allocated slot");
        }
        ensureSamePersistentIdentity(existing, checked);

        NightmareCompletionRecord completion = successfulCompletions.get(checked.playerId());
        if (completion != null && !completion.instance().equals(checked)) {
            throw new IllegalStateException(
                    "Cannot mutate a Nightmare instance after its successful completion receipt is recorded"
            );
        }

        ensureSlotOwnedOnlyBy(checked.slot(), checked.instanceId());
        instances.put(checked.instanceId(), checked);
        instanceByPlayer.put(checked.playerId(), checked.instanceId());
        setDirty();
    }

    /** Records terminal success while the exact active instance still exists. */
    public NightmareCompletionRecord beginSuccessfulCompletion(NightmareInstance expected, long resolvedGameTime) {
        NightmareInstance checked = requireExactActive(expected, "complete");
        NightmareCompletionRecord existing = successfulCompletions.get(checked.playerId());
        if (existing != null) {
            if (!existing.instance().equals(checked)) {
                throw new IllegalStateException("Player already has a completion receipt for another or stale Nightmare snapshot");
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

    /** Advances a receipt one durable milestone; replaying an already-reached target is idempotent. */
    public NightmareCompletionRecord advanceSuccessfulCompletion(
            NightmareInstance expected,
            NightmareCompletionPhase target
    ) {
        NightmareInstance checked = Objects.requireNonNull(expected, "expected");
        NightmareCompletionPhase checkedTarget = Objects.requireNonNull(target, "target");
        NightmareCompletionRecord existing = successfulCompletions.get(checked.playerId());
        if (existing == null || !existing.instance().equals(checked)) {
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

    /** Clears only the receipt owned by the supplied authoritative instance snapshot. */
    public Optional<NightmareCompletionRecord> clearSuccessfulCompletion(NightmareInstance expected) {
        NightmareInstance checked = Objects.requireNonNull(expected, "expected");
        NightmareCompletionRecord existing = successfulCompletions.get(checked.playerId());
        if (existing == null || !existing.instance().instanceId().equals(checked.instanceId())) {
            return Optional.empty();
        }
        if (!existing.instance().equals(checked)) {
            throw new IllegalStateException("Cannot clear completion from a stale or modified Nightmare instance snapshot");
        }
        successfulCompletions.remove(checked.playerId());
        setDirty();
        return Optional.of(existing);
    }

    /** Removes only the exact ownership record that the caller previously resolved. */
    public Optional<NightmareInstance> remove(NightmareInstance expected) {
        NightmareInstance checked = Objects.requireNonNull(expected, "expected");
        UUID registeredInstanceId = instanceByPlayer.get(checked.playerId());
        if (!checked.instanceId().equals(registeredInstanceId)) {
            return Optional.empty();
        }
        NightmareInstance registered = instances.get(registeredInstanceId);
        if (registered == null) {
            return Optional.empty();
        }
        if (!registered.equals(checked)) {
            throw new IllegalStateException("Cannot remove a stale or modified Nightmare instance snapshot");
        }
        return removeByPlayer(checked.playerId());
    }

    private Optional<NightmareInstance> removeByPlayer(UUID playerId) {
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

    void restore(NightmareInstance instance) {
        NightmareInstance checked = Objects.requireNonNull(instance, "instance");
        if (instances.containsKey(checked.instanceId())) {
            throw new IllegalStateException("Duplicate Nightmare instance ID in SavedData");
        }
        if (instanceByPlayer.containsKey(checked.playerId())) {
            throw new IllegalStateException("Player owns multiple active Nightmares in SavedData");
        }

        NightmareCompletionRecord completion = successfulCompletions.get(checked.playerId());
        if (completion != null && !completion.instance().equals(checked)) {
            throw new IllegalStateException("Active Nightmare does not exactly match retained successful completion in SavedData");
        }
        boolean completionUsesInstanceForAnotherPlayer = successfulCompletions.values().stream()
                .anyMatch(existing -> existing.instance().instanceId().equals(checked.instanceId())
                        && !existing.instance().playerId().equals(checked.playerId()));
        if (completionUsesInstanceForAnotherPlayer) {
            throw new IllegalStateException(
                    "Nightmare instance ID belongs to another player's retained successful completion in SavedData"
            );
        }
        ensureSlotOwnedOnlyBy(checked.slot(), checked.instanceId());

        instances.put(checked.instanceId(), checked);
        instanceByPlayer.put(checked.playerId(), checked.instanceId());
        nextSlot = Math.max(nextSlot, checked.slot() + 1);
    }

    void restoreSuccessfulCompletion(NightmareCompletionRecord completion) {
        NightmareCompletionRecord checked = Objects.requireNonNull(completion, "completion");
        NightmareInstance completed = checked.instance();
        UUID playerId = completed.playerId();
        if (successfulCompletions.containsKey(playerId)) {
            throw new IllegalStateException("Player has multiple successful Nightmare receipts in SavedData");
        }
        boolean duplicateInstance = successfulCompletions.values().stream()
                .anyMatch(existing -> existing.instance().instanceId().equals(completed.instanceId()));
        if (duplicateInstance) {
            throw new IllegalStateException("Duplicate successful Nightmare instance ID in SavedData");
        }

        NightmareInstance activeWithSameInstanceId = instances.get(completed.instanceId());
        if (activeWithSameInstanceId != null && !activeWithSameInstanceId.equals(completed)) {
            throw new IllegalStateException("Retained successful completion does not exactly match active Nightmare in SavedData");
        }
        UUID activeInstanceId = instanceByPlayer.get(playerId);
        if (activeInstanceId != null && !activeInstanceId.equals(completed.instanceId())) {
            throw new IllegalStateException("Player retained successful completion does not match active Nightmare in SavedData");
        }
        ensureSlotOwnedOnlyBy(completed.slot(), completed.instanceId());

        successfulCompletions.put(playerId, checked);
        nextSlot = Math.max(nextSlot, completed.slot() + 1);
    }

    private NightmareInstance requireExactActive(NightmareInstance expected, String operation) {
        NightmareInstance checked = Objects.requireNonNull(expected, "expected");
        UUID registeredId = instanceByPlayer.get(checked.playerId());
        NightmareInstance registered = registeredId == null ? null : instances.get(registeredId);
        if (registered == null || !registered.equals(checked)) {
            throw new IllegalStateException("Cannot " + operation + " a stale or inactive Nightmare instance snapshot");
        }
        return registered;
    }

    private static void ensureSamePersistentIdentity(NightmareInstance expected, NightmareInstance actual) {
        if (!expected.scenarioId().equals(actual.scenarioId())
                || !expected.historicalRoleId().equals(actual.historicalRoleId())
                || !expected.returnDimension().equals(actual.returnDimension())
                || Double.compare(expected.returnX(), actual.returnX()) != 0
                || Double.compare(expected.returnY(), actual.returnY()) != 0
                || Double.compare(expected.returnZ(), actual.returnZ()) != 0
                || Float.compare(expected.returnYaw(), actual.returnYaw()) != 0
                || Float.compare(expected.returnPitch(), actual.returnPitch()) != 0
                || expected.createdGameTime() != actual.createdGameTime()) {
            throw new IllegalStateException("Cannot change a Nightmare instance's persistent recovery identity");
        }
    }

    private void ensureSlotOwnedOnlyBy(int slot, UUID instanceId) {
        boolean activeConflict = instances.values().stream()
                .anyMatch(existing -> existing.slot() == slot && !existing.instanceId().equals(instanceId));
        boolean completionConflict = successfulCompletions.values().stream()
                .map(NightmareCompletionRecord::instance)
                .anyMatch(existing -> existing.slot() == slot && !existing.instanceId().equals(instanceId));
        if (activeConflict || completionConflict) {
            throw new IllegalStateException("Nightmare slot belongs to another instance in SavedData");
        }
    }
}
