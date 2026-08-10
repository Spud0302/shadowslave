package dev.spud.shadowslave.nightmare;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/** Verifies exact successful-completion recovery authority in the persisted SavedData image. */
final class PersistedNightmareCompletionReceiptVerifier {
    private PersistedNightmareCompletionReceiptVerifier() {
    }

    static void requirePresent(Path receiptFile, NightmareCompletionReceiptData.Receipt expected) {
        Path checkedFile = Objects.requireNonNull(receiptFile, "receiptFile");
        NightmareCompletionReceiptData.Receipt checkedExpected = Objects.requireNonNull(expected, "expected");
        if (!Files.isRegularFile(checkedFile)) {
            throw new IllegalStateException("Successful Nightmare completion receipt file is missing after persistence");
        }

        ListTag receipts = readHealthyReceipts(checkedFile);
        int exactMatches = 0;
        for (Tag rawReceipt : receipts) {
            NightmareCompletionReceiptData.Receipt decoded = decode(rawReceipt);
            if (decoded.equals(checkedExpected)) {
                exactMatches++;
            }
        }
        if (exactMatches != 1) {
            throw new IllegalStateException(
                    "Persisted successful Nightmare completion authority does not contain exactly one expected receipt"
            );
        }
    }

    static void requireAbsent(Path receiptFile, NightmareCompletionReceiptData.Receipt expected) {
        Path checkedFile = Objects.requireNonNull(receiptFile, "receiptFile");
        NightmareCompletionReceiptData.Receipt checkedExpected = Objects.requireNonNull(expected, "expected");
        if (!Files.exists(checkedFile)) {
            return;
        }
        if (!Files.isRegularFile(checkedFile)) {
            throw new IllegalStateException("Successful Nightmare completion receipt path is not a regular file");
        }

        ListTag receipts = readHealthyReceipts(checkedFile);
        for (Tag rawReceipt : receipts) {
            NightmareCompletionReceiptData.Receipt decoded = decode(rawReceipt);
            if (decoded.instance().playerId().equals(checkedExpected.instance().playerId())
                    || decoded.instance().instanceId().equals(checkedExpected.instance().instanceId())) {
                throw new IllegalStateException(
                        "Persisted successful Nightmare completion authority still contains the consumed receipt identity"
                );
            }
        }
    }

    private static ListTag readHealthyReceipts(Path receiptFile) {
        CompoundTag root;
        try {
            root = NbtIo.readCompressed(receiptFile, NbtAccounter.unlimitedHeap());
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read persisted successful Nightmare completion receipt", exception);
        }

        Tag rawData = root.get("data");
        if (!(rawData instanceof CompoundTag data)) {
            throw new IllegalStateException("Persisted successful Nightmare completion receipt file has no data compound");
        }
        Tag rawReceipts = data.get("receipts");
        if (!(rawReceipts instanceof ListTag receipts)) {
            throw new IllegalStateException("Persisted successful Nightmare completion receipt file has no receipt list");
        }
        return receipts;
    }

    private static NightmareCompletionReceiptData.Receipt decode(Tag rawReceipt) {
        if (!(rawReceipt instanceof CompoundTag receiptTag)) {
            throw new IllegalStateException("Persisted successful Nightmare completion receipt entry is malformed");
        }
        try {
            return NightmareCompletionReceiptData.Receipt.load(receiptTag);
        } catch (RuntimeException exception) {
            throw new IllegalStateException(
                    "Persisted successful Nightmare completion receipt is rejected by production loading",
                    exception
            );
        }
    }
}
