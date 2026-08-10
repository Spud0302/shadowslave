package dev.spud.shadowslave.nightmare;

import dev.spud.shadowslave.appraisal.GeneratedAppraisalRecoverySnapshot;
import dev.spud.shadowslave.persistence.SavedDataPersistence;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Durable authority for a resolved Nightmare whose exact generated appraisal still needs to converge. */
public final class NightmareCompletionReceiptData extends SavedData {
    static final String DATA_NAME = "shadowslave_nightmare_completion_receipts";
    private static final Factory<NightmareCompletionReceiptData> FACTORY = new Factory<>(
            NightmareCompletionReceiptData::new,
            NightmareCompletionReceiptData::load,
            null
    );

    private final Map<UUID, Receipt> receipts = new LinkedHashMap<>();
    private boolean corrupt;
    private transient MinecraftServer server;

    public static NightmareCompletionReceiptData get(MinecraftServer server) {
        MinecraftServer checkedServer = Objects.requireNonNull(server, "server");
        NightmareCompletionReceiptData data = checkedServer.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
        data.server = checkedServer;
        return data;
    }

    public Optional<Receipt> find(UUID playerId) {
        requireHealthy();
        return Optional.ofNullable(receipts.get(Objects.requireNonNull(playerId, "playerId")));
    }

    public Receipt begin(NightmareInstance instance, GeneratedAppraisalRecoverySnapshot appraisal) {
        requireHealthy();
        NightmareInstance checkedInstance = Objects.requireNonNull(instance, "instance");
        GeneratedAppraisalRecoverySnapshot checkedAppraisal = Objects.requireNonNull(appraisal, "appraisal");
        Receipt created = new Receipt(checkedInstance, checkedAppraisal);
        Receipt existing = receipts.get(checkedInstance.playerId());
        if (existing != null) {
            if (!existing.equals(created)) {
                throw new IllegalStateException("Player already has a different successful Nightmare completion receipt");
            }
            persistAndVerify(existing);
            return existing;
        }
        boolean duplicateInstance = receipts.values().stream()
                .anyMatch(receipt -> receipt.instance().instanceId().equals(checkedInstance.instanceId()));
        if (duplicateInstance) {
            throw new IllegalStateException("Nightmare instance already belongs to another completion receipt");
        }
        receipts.put(checkedInstance.playerId(), created);
        persistAndVerify(created);
        return created;
    }

    public Optional<Receipt> clear(Receipt expected) {
        requireHealthy();
        Receipt checked = Objects.requireNonNull(expected, "expected");
        Receipt existing = receipts.get(checked.instance().playerId());
        if (existing == null) {
            return Optional.empty();
        }
        if (!existing.equals(checked)) {
            throw new IllegalStateException("Cannot clear a stale successful Nightmare completion receipt");
        }
        receipts.remove(checked.instance().playerId());
        setDirty();
        return Optional.of(existing);
    }

    /**
     * Consumes one receipt only after the persisted SavedData image proves that authority is absent.
     * If the async save or read-back is ambiguous, restore and re-dirty the in-memory receipt so the
     * same process cannot forget recovery authority that may still exist on disk.
     */
    public void clearAndVerify(Receipt expected) {
        Receipt checked = Objects.requireNonNull(expected, "expected");
        Optional<Receipt> removed = clear(checked);
        if (removed.isEmpty() || server == null) {
            return;
        }

        Path receiptFile = receiptFile(server);
        try {
            SavedDataPersistence.saveAndWait(server);
            PersistedNightmareCompletionReceiptVerifier.requireAbsent(receiptFile, checked);
        } catch (RuntimeException exception) {
            receipts.put(checked.instance().playerId(), checked);
            setDirty();
            throw new IllegalStateException(
                    "Could not prove successful Nightmare completion receipt deletion; recovery authority was restored in memory",
                    exception
            );
        }
    }

    boolean isCorrupt() {
        return corrupt;
    }

    private void persistAndVerify(Receipt expected) {
        if (server == null) {
            setDirty();
            return;
        }

        setDirty();
        SavedDataPersistence.saveAndWait(server);
        PersistedNightmareCompletionReceiptVerifier.requirePresent(receiptFile(server), expected);
    }

    private static Path receiptFile(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT)
                .resolve("data")
                .resolve(DATA_NAME + ".dat");
    }

    private void requireHealthy() {
        if (corrupt) {
            throw new IllegalStateException(
                    "Successful Nightmare completion receipt storage is corrupt; recovery and new entry are blocked"
            );
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        requireHealthy();
        ListTag entries = new ListTag();
        for (Receipt receipt : receipts.values()) {
            entries.add(receipt.save());
        }
        tag.put("receipts", entries);
        return tag;
    }

    static NightmareCompletionReceiptData load(CompoundTag tag, HolderLookup.Provider registries) {
        NightmareCompletionReceiptData data = new NightmareCompletionReceiptData();
        Tag rawEntries = tag.get("receipts");
        if (!(rawEntries instanceof ListTag entries)) {
            data.corrupt = true;
            return data;
        }
        for (Tag rawEntry : entries) {
            if (!(rawEntry instanceof CompoundTag receiptTag)) {
                data.corrupt = true;
                return data;
            }
            try {
                data.restore(Receipt.load(receiptTag));
            } catch (RuntimeException exception) {
                data.receipts.clear();
                data.corrupt = true;
                return data;
            }
        }
        return data;
    }

    void restore(Receipt receipt) {
        requireHealthy();
        Receipt checked = Objects.requireNonNull(receipt, "receipt");
        if (receipts.putIfAbsent(checked.instance().playerId(), checked) != null) {
            throw new IllegalStateException("Player has duplicate successful Nightmare completion receipts");
        }
        boolean duplicateInstance = receipts.values().stream()
                .filter(existing -> !existing.instance().playerId().equals(checked.instance().playerId()))
                .anyMatch(existing -> existing.instance().instanceId().equals(checked.instance().instanceId()));
        if (duplicateInstance) {
            throw new IllegalStateException("Nightmare instance is duplicated across completion receipts");
        }
    }

    public record Receipt(NightmareInstance instance, GeneratedAppraisalRecoverySnapshot appraisal) {
        public Receipt {
            instance = Objects.requireNonNull(instance, "instance");
            appraisal = Objects.requireNonNull(appraisal, "appraisal");
        }

        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.put("instance", instance.save());
            tag.put("appraisal", appraisal.save());
            return tag;
        }

        static Receipt load(CompoundTag tag) {
            if (!tag.contains("instance", Tag.TAG_COMPOUND) || !tag.contains("appraisal")) {
                throw new IllegalStateException("Successful Nightmare completion receipt is incomplete");
            }
            return new Receipt(
                    NightmareInstance.load(tag.getCompound("instance")),
                    GeneratedAppraisalRecoverySnapshot.load(tag.get("appraisal"))
            );
        }
    }
}
