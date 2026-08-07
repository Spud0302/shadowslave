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
    private final Map<UUID, TechnicalExitMarker> technicalExits = new LinkedHashMap<>();
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

    public Optional<NightmareExitReason> findTechnicalExitReasonByPlayer(UUID playerId) {
        TechnicalExitMarker marker = technicalExits.get(Objects.requireNonNull(playerId, "playerId"));
        return marker == null ? Optional.empty() : Optional.of(marker.reason());
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
        if (technicalExits.containsKey(checkedPlayerId)) {
            throw new IllegalStateException("Player still has a pending technical Nightmare exit");
        }
    }

    public void update(NightmareInstance instance) {
        NightmareInstance existing = instances.get(instance.instanceId());
        if (existing == null || !existing.playerId().equals(instance.playerId())) {
            throw new IllegalStateException("Cannot update an unregistered Nightmare instance");
        }
        if (existing.slot() != instance.slot()) {
            throw new IllegalStateException("Cannot change a Nightmare instance's allocated slot");
        }
        ensureSamePersistentIdentity(existing, instance);
        NightmareCompletionRecord completion = successfulCompletions.get(instance.playerId());
        if (completion != null) {
            if (!completion.instance().instanceId().equals(instance.instanceId())) {
                throw new IllegalStateException("Player retained successful completion belongs to another Nightmare");
            }
            if (!completion.instance().equals(instance)) {
                throw new IllegalStateException(
                        "Cannot mutate a Nightmare instance after its successful completion receipt is recorded"
                );
            }
        }
        TechnicalExitMarker technicalExit = technicalExits.get(instance.playerId());
        if (technicalExit != null && !technicalExit.instanceId().equals(instance.instanceId())) {
            throw new IllegalStateException("Player pending technical exit belongs to another Nightmare");
        }
        ensureSlotOwnedOnlyBy(instance.slot(), instance.instanceId());
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
        NightmareInstance registered = instances.get(registeredInstanceId);
        if (registered == null || !registered.equals(checked)) {
            throw new IllegalStateException("Cannot complete a stale or modified Nightmare instance snapshot");
        }
        if (technicalExits.containsKey(checked.playerId())) {
            throw new IllegalStateException("Cannot complete a Nightmare while a technical exit is pending");
        }

        NightmareCompletionRecord existing = successfulCompletions.get(checked.playerId());
        if (existing != null) {
            if (!existing.instance().instanceId().equals(checked.instanceId())) {
                throw new IllegalStateException("Player already has a completion receipt for another Nightmare");
            }
            return existing;
        }

        NightmareCompletionRecord created = new NightmareCompletionRecord(
                registered,
                NightmareCompletionPhase.TERMINAL_RESOLUTION_RECORDED,
                resolvedGameTime
        );
        successfulCompletions.put(registered.playerId(), created);
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
        if (!existing.instance().equals(checked)) {
            throw new IllegalStateException("Cannot advance completion from a stale or modified Nightmare instance snapshot");
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

    /**
     * Removes only the retained completion receipt owned by the supplied authoritative instance snapshot.
     * A stale instance ID is a no-op; a modified same-ID snapshot is rejected.
     */
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

    /** Records technical/admin exit intent while exact active ownership is still authoritative. */
    public NightmareExitReason beginTechnicalExit(NightmareInstance expected, NightmareExitReason reason) {
        NightmareInstance checked = Objects.requireNonNull(expected, "expected");
        NightmareExitReason checkedReason = requireTechnicalExitReason(reason);
        UUID registeredInstanceId = instanceByPlayer.get(checked.playerId());
        if (!checked.instanceId().equals(registeredInstanceId)) {
            throw new IllegalStateException("Cannot begin technical exit for a Nightmare that is not active");
        }
        NightmareInstance registered = instances.get(registeredInstanceId);
        if (registered == null || !registered.equals(checked)) {
            throw new IllegalStateException("Cannot begin technical exit from a stale or modified Nightmare snapshot");
        }

        TechnicalExitMarker existing = technicalExits.get(checked.playerId());
        if (existing != null) {
            if (!existing.instanceId().equals(checked.instanceId())) {
                throw new IllegalStateException("Player already has a technical exit for another Nightmare");
            }
            if (existing.reason() != checkedReason) {
                throw new IllegalStateException("Cannot change the reason of a pending technical Nightmare exit");
            }
            return existing.reason();
        }

        technicalExits.put(
                checked.playerId(),
                new TechnicalExitMarker(checked.playerId(), checked.instanceId(), checkedReason)
        );
        setDirty();
        return checkedReason;
    }

    /**
     * Completes the exact pending technical/admin exit and consumes active ownership in one SavedData mutation.
     * Any successful-completion receipt must already have been cleared durably by the coordinator.
     */
    public Optional<NightmareInstance> completeTechnicalExit(NightmareInstance expected) {
        NightmareInstance checked = Objects.requireNonNull(expected, "expected");
        TechnicalExitMarker marker = technicalExits.get(checked.playerId());
        if (marker == null || !marker.instanceId().equals(checked.instanceId())) {
            throw new IllegalStateException("No matching pending technical Nightmare exit exists");
        }
        if (successfulCompletions.containsKey(checked.playerId())) {
            throw new IllegalStateException("Cannot consume technical Nightmare ownership while completion receipt remains");
        }

        UUID registeredInstanceId = instanceByPlayer.get(checked.playerId());
        NightmareInstance registered = registeredInstanceId == null ? null : instances.get(registeredInstanceId);
        if (!checked.instanceId().equals(registeredInstanceId) || registered == null || !registered.equals(checked)) {
            throw new IllegalStateException("Cannot complete technical exit from stale or modified active ownership");
        }

        instanceByPlayer.remove(checked.playerId());
        instances.remove(checked.instanceId());
        technicalExits.remove(checked.playerId());
        setDirty();
        return Optional.of(registered);
    }

    /** Removes only the exact ownership record that the caller previously resolved. */
    public Optional<NightmareInstance> remove(NightmareInstance expected) {
        NightmareInstance checked = Objects.requireNonNull(expected, "expected");
        if (technicalExits.containsKey(checked.playerId())) {
            throw new IllegalStateException("Pending technical Nightmare exit must use completeTechnicalExit");
        }
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

        ListTag encodedTechnicalExits = new ListTag();
        for (TechnicalExitMarker marker : technicalExits.values()) {
            encodedTechnicalExits.add(marker.save());
        }
        tag.put("technical_exits", encodedTechnicalExits);
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

        ListTag encodedTechnicalExits = tag.getList("technical_exits", Tag.TAG_COMPOUND);
        for (int index = 0; index < encodedTechnicalExits.size(); index++) {
            data.restoreTechnicalExit(TechnicalExitMarker.load(encodedTechnicalExits.getCompound(index)));
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
        if (completion != null && completion.instance().slot() != checked.slot()) {
            throw new IllegalStateException(
                    "Active Nightmare and retained successful completion disagree on the instance slot"
            );
        }
        if (completion != null) {
            ensureSameInstanceRecoveryIdentity(checked, completion.instance());
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
        if (activeWithSameInstanceId != null && activeWithSameInstanceId.slot() != checked.instance().slot()) {
            throw new IllegalStateException(
                    "Retained successful completion and active Nightmare disagree on the instance slot"
            );
        }
        if (activeWithSameInstanceId != null) {
            ensureSameInstanceRecoveryIdentity(activeWithSameInstanceId, checked.instance());
        }
        UUID activeInstanceId = instanceByPlayer.get(playerId);
        if (activeInstanceId != null && !activeInstanceId.equals(checked.instance().instanceId())) {
            throw new IllegalStateException(
                    "Player retained successful completion does not match active Nightmare in SavedData"
            );
        }
        ensureSlotOwnedOnlyBy(checked.instance().slot(), checked.instance().instanceId());
        successfulCompletions.put(playerId, checked);
        nextSlot = Math.max(nextSlot, checked.instance().slot() + 1);
    }

    private void restoreTechnicalExit(TechnicalExitMarker marker) {
        TechnicalExitMarker checked = Objects.requireNonNull(marker, "marker");
        if (technicalExits.containsKey(checked.playerId())) {
            throw new IllegalStateException("Player has multiple technical Nightmare exits in SavedData");
        }
        NightmareInstance active = findByPlayer(checked.playerId())
                .orElseThrow(() -> new IllegalStateException(
                        "Technical Nightmare exit has no active ownership in SavedData"
                ));
        if (!active.instanceId().equals(checked.instanceId())) {
            throw new IllegalStateException("Technical Nightmare exit does not match active ownership in SavedData");
        }
        NightmareCompletionRecord completion = successfulCompletions.get(checked.playerId());
        if (completion != null && !completion.instance().instanceId().equals(checked.instanceId())) {
            throw new IllegalStateException("Technical Nightmare exit does not match retained completion in SavedData");
        }
        technicalExits.put(checked.playerId(), checked);
    }

    private static NightmareExitReason requireTechnicalExitReason(NightmareExitReason reason) {
        NightmareExitReason checked = Objects.requireNonNull(reason, "reason");
        if (checked != NightmareExitReason.TECHNICAL_RECOVERY && checked != NightmareExitReason.ADMIN_ABORT) {
            throw new IllegalArgumentException("Reason is not a technical/admin Nightmare exit: " + checked);
        }
        return checked;
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

    private static void ensureSameInstanceRecoveryIdentity(NightmareInstance active, NightmareInstance completed) {
        ensureSamePersistentIdentity(active, completed);
        if (!active.origin().equals(completed.origin()) || !active.altar().equals(completed.altar())) {
            throw new IllegalStateException(
                    "Active Nightmare and retained successful completion disagree on persisted scenario layout"
            );
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

    private record TechnicalExitMarker(UUID playerId, UUID instanceId, NightmareExitReason reason) {
        private TechnicalExitMarker {
            Objects.requireNonNull(playerId, "playerId");
            Objects.requireNonNull(instanceId, "instanceId");
            requireTechnicalExitReason(reason);
        }

        private CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("player_id", playerId);
            tag.putUUID("instance_id", instanceId);
            tag.putString("reason", reason.name());
            return tag;
        }

        private static TechnicalExitMarker load(CompoundTag tag) {
            NightmareExitReason reason;
            try {
                reason = NightmareExitReason.valueOf(tag.getString("reason"));
            } catch (IllegalArgumentException exception) {
                throw new IllegalStateException("Unknown technical Nightmare exit reason in SavedData", exception);
            }
            return new TechnicalExitMarker(
                    tag.getUUID("player_id"),
                    tag.getUUID("instance_id"),
                    reason
            );
        }
    }
}
