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
    private final String loadFailure;
    private int nextSlot;

    public NightmareRegistryData() {
        this(null);
    }

    private NightmareRegistryData(String loadFailure) {
        this.loadFailure = loadFailure;
    }

    public static NightmareRegistryData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    /** Corrupt ownership/completion metadata has uncertain authority, so recovery must stop rather than guess. */
    public boolean recoveryBlocked() {
        return loadFailure != null;
    }

    public Optional<String> loadFailure() {
        return Optional.ofNullable(loadFailure);
    }

    public Optional<NightmareInstance> findByPlayer(UUID playerId) {
        requireHealthy();
        UUID instanceId = instanceByPlayer.get(Objects.requireNonNull(playerId, "playerId"));
        return instanceId == null ? Optional.empty() : Optional.ofNullable(instances.get(instanceId));
    }

    public Optional<NightmareCompletionRecord> findSuccessfulCompletionByPlayer(UUID playerId) {
        requireHealthy();
        return Optional.ofNullable(successfulCompletions.get(Objects.requireNonNull(playerId, "playerId")));
    }

    public Optional<NightmareExitReason> findTechnicalExitReasonByPlayer(UUID playerId) {
        requireHealthy();
        TechnicalExitMarker marker = technicalExits.get(Objects.requireNonNull(playerId, "playerId"));
        return marker == null ? Optional.empty() : Optional.of(marker.reason());
    }

    public Collection<NightmareInstance> activeInstances() {
        requireHealthy();
        return List.copyOf(instances.values());
    }

    public Collection<NightmareCompletionRecord> successfulCompletionRecords() {
        requireHealthy();
        return List.copyOf(successfulCompletions.values());
    }

    public NightmareInstance create(ServerPlayer player, String scenarioId, String historicalRoleId) {
        requireHealthy();
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
        requireHealthy();
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
        requireHealthy();
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
        TechnicalExitMarker technicalExit = technicalExits.get(checked.playerId());
        if (technicalExit != null && !technicalExit.instanceId().equals(checked.instanceId())) {
            throw new IllegalStateException("Player pending technical exit belongs to another Nightmare");
        }

        ensureSlotOwnedOnlyBy(checked.slot(), checked.instanceId());
        instances.put(checked.instanceId(), checked);
        instanceByPlayer.put(checked.playerId(), checked.instanceId());
        setDirty();
    }

    /** Records terminal success while the exact active instance still exists. */
    public NightmareCompletionRecord beginSuccessfulCompletion(NightmareInstance expected, long resolvedGameTime) {
        requireHealthy();
        NightmareInstance checked = requireExactActive(expected, "complete");
        if (technicalExits.containsKey(checked.playerId())) {
            throw new IllegalStateException("Cannot complete a Nightmare while a technical exit is pending");
        }
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
        requireHealthy();
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
        requireHealthy();
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
    NightmareExitReason beginTechnicalExit(NightmareInstance expected, NightmareExitReason reason) {
        requireHealthy();
        NightmareInstance checked = requireExactActive(expected, "begin technical exit for");
        NightmareExitReason checkedReason = requireTechnicalExitReason(reason);
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
    Optional<NightmareInstance> completeTechnicalExit(NightmareInstance expected) {
        requireHealthy();
        NightmareInstance checked = Objects.requireNonNull(expected, "expected");
        TechnicalExitMarker marker = technicalExits.get(checked.playerId());
        if (marker == null || !marker.instanceId().equals(checked.instanceId())) {
            throw new IllegalStateException("No matching pending technical Nightmare exit exists");
        }
        if (successfulCompletions.containsKey(checked.playerId())) {
            throw new IllegalStateException("Cannot consume technical Nightmare ownership while completion receipt remains");
        }

        NightmareInstance registered = requireExactActive(checked, "complete technical exit from");
        instanceByPlayer.remove(checked.playerId());
        instances.remove(checked.instanceId());
        technicalExits.remove(checked.playerId());
        setDirty();
        return Optional.of(registered);
    }

    /** Removes only the exact ownership record that the caller previously resolved. */
    public Optional<NightmareInstance> remove(NightmareInstance expected) {
        requireHealthy();
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
        requireHealthy();
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
        try {
            NightmareRegistryData data = new NightmareRegistryData();
            data.nextSlot = Math.max(0, tag.getInt("next_slot"));

            ListTag encodedInstances = optionalCompoundList(tag, "instances");
            for (int index = 0; index < encodedInstances.size(); index++) {
                Tag rawEntry = encodedInstances.get(index);
                if (!(rawEntry instanceof CompoundTag entry)) {
                    throw new IllegalStateException("Nightmare instance entry " + index + " is not a compound");
                }
                data.restore(NightmareInstance.load(entry));
            }

            ListTag encodedCompletions = optionalCompoundList(tag, "successful_completions");
            for (int index = 0; index < encodedCompletions.size(); index++) {
                Tag rawEntry = encodedCompletions.get(index);
                if (!(rawEntry instanceof CompoundTag entry)) {
                    throw new IllegalStateException("Nightmare completion entry " + index + " is not a compound");
                }
                data.restoreSuccessfulCompletion(NightmareCompletionRecord.load(entry));
            }

            ListTag encodedTechnicalExits = optionalCompoundList(tag, "technical_exits");
            for (int index = 0; index < encodedTechnicalExits.size(); index++) {
                Tag rawEntry = encodedTechnicalExits.get(index);
                if (!(rawEntry instanceof CompoundTag entry)) {
                    throw new IllegalStateException("Technical Nightmare exit entry " + index + " is not a compound");
                }
                data.restoreTechnicalExit(TechnicalExitMarker.load(entry));
            }
            return data;
        } catch (RuntimeException failure) {
            String detail = failure.getMessage();
            return blocked("Nightmare registry decode failed" + (detail == null ? "" : ": " + detail));
        }
    }

    void restore(NightmareInstance instance) {
        requireHealthy();
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
        requireHealthy();
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

    private void restoreTechnicalExit(TechnicalExitMarker marker) {
        requireHealthy();
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
        if (completion != null && !completion.instance().equals(active)) {
            throw new IllegalStateException("Technical Nightmare exit does not match retained completion in SavedData");
        }
        technicalExits.put(checked.playerId(), checked);
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

    private static ListTag optionalCompoundList(CompoundTag tag, String key) {
        Tag raw = tag.get(key);
        if (raw == null) {
            return new ListTag();
        }
        if (!(raw instanceof ListTag list)) {
            throw new IllegalStateException("Nightmare registry " + key + " is not a list");
        }
        return list;
    }

    private static NightmareRegistryData blocked(String loadFailure) {
        return new NightmareRegistryData(Objects.requireNonNull(loadFailure, "loadFailure"));
    }

    private void requireHealthy() {
        if (loadFailure != null) {
            throw new IllegalStateException("Nightmare recovery is blocked: " + loadFailure);
        }
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
