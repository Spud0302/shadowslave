package dev.spud.shadowslave.nightmare;

import dev.spud.shadowslave.appraisal.GeneratedAppraisalRecoverySnapshot;
import dev.spud.shadowslave.appraisal.PreviewAppraisalService;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PersistedNightmareCompletionReceiptVerifierTest {
    @TempDir
    Path tempDir;

    @Test
    void acceptsExactlyOneProductionLoadableExpectedReceipt() throws IOException {
        NightmareCompletionReceiptData.Receipt expected = receipt(new UUID(401L, 409L), new UUID(419L, 421L));
        Path file = writeReceipts(expected);

        assertDoesNotThrow(() -> PersistedNightmareCompletionReceiptVerifier.requirePresent(file, expected));
    }

    @Test
    void rejectsMissingExpectedReceipt() throws IOException {
        NightmareCompletionReceiptData.Receipt expected = receipt(new UUID(431L, 433L), new UUID(439L, 443L));
        NightmareCompletionReceiptData.Receipt other = receipt(new UUID(449L, 457L), new UUID(461L, 463L));
        Path file = writeReceipts(other);

        assertThrows(
                IllegalStateException.class,
                () -> PersistedNightmareCompletionReceiptVerifier.requirePresent(file, expected)
        );
    }

    @Test
    void rejectsDuplicateExpectedAuthority() throws IOException {
        NightmareCompletionReceiptData.Receipt expected = receipt(new UUID(467L, 479L), new UUID(487L, 491L));
        Path file = writeReceipts(expected, expected);

        assertThrows(
                IllegalStateException.class,
                () -> PersistedNightmareCompletionReceiptVerifier.requirePresent(file, expected)
        );
    }

    @Test
    void rejectsMalformedReceiptThroughProductionLoader() throws IOException {
        NightmareCompletionReceiptData.Receipt expected = receipt(new UUID(499L, 503L), new UUID(509L, 521L));
        CompoundTag root = new CompoundTag();
        CompoundTag data = new CompoundTag();
        ListTag receipts = new ListTag();
        receipts.add(new CompoundTag());
        data.put("receipts", receipts);
        root.put("data", data);
        Path file = tempDir.resolve("malformed.dat");
        NbtIo.writeCompressed(root, file);

        assertThrows(
                IllegalStateException.class,
                () -> PersistedNightmareCompletionReceiptVerifier.requirePresent(file, expected)
        );
        assertThrows(
                IllegalStateException.class,
                () -> PersistedNightmareCompletionReceiptVerifier.requireAbsent(file, expected)
        );
    }

    @Test
    void acceptsHealthyPersistedAbsenceAndUnrelatedReceipts() throws IOException {
        NightmareCompletionReceiptData.Receipt expected = receipt(new UUID(523L, 541L), new UUID(547L, 557L));
        NightmareCompletionReceiptData.Receipt other = receipt(new UUID(563L, 569L), new UUID(571L, 577L));
        Path file = writeReceipts(other);

        assertDoesNotThrow(() -> PersistedNightmareCompletionReceiptVerifier.requireAbsent(file, expected));
        assertDoesNotThrow(() -> PersistedNightmareCompletionReceiptVerifier.requireAbsent(
                tempDir.resolve("missing-receipts.dat"), expected));
    }

    @Test
    void rejectsPersistedReceiptIdentityAfterConsumption() throws IOException {
        NightmareCompletionReceiptData.Receipt expected = receipt(new UUID(587L, 593L), new UUID(599L, 601L));
        Path exactFile = writeReceipts(expected);

        assertThrows(
                IllegalStateException.class,
                () -> PersistedNightmareCompletionReceiptVerifier.requireAbsent(exactFile, expected)
        );

        NightmareCompletionReceiptData.Receipt samePlayer = receipt(new UUID(607L, 613L), expected.instance().playerId());
        Path samePlayerFile = writeReceipts(samePlayer);
        assertThrows(
                IllegalStateException.class,
                () -> PersistedNightmareCompletionReceiptVerifier.requireAbsent(samePlayerFile, expected)
        );

        NightmareCompletionReceiptData.Receipt sameInstance = receipt(expected.instance().instanceId(), new UUID(617L, 619L));
        Path sameInstanceFile = writeReceipts(sameInstance);
        assertThrows(
                IllegalStateException.class,
                () -> PersistedNightmareCompletionReceiptVerifier.requireAbsent(sameInstanceFile, expected)
        );
    }

    private Path writeReceipts(NightmareCompletionReceiptData.Receipt... receipts) throws IOException {
        CompoundTag root = new CompoundTag();
        CompoundTag data = new CompoundTag();
        ListTag entries = new ListTag();
        for (NightmareCompletionReceiptData.Receipt receipt : receipts) {
            entries.add(receipt.save());
        }
        data.put("receipts", entries);
        root.put("data", data);
        Path file = tempDir.resolve(UUID.randomUUID() + ".dat");
        NbtIo.writeCompressed(root, file);
        return file;
    }

    private static NightmareCompletionReceiptData.Receipt receipt(UUID instanceId, UUID playerId) {
        NightmareInstance instance = new NightmareInstance(
                instanceId,
                playerId,
                2,
                "drowned_bell",
                "cistern_keeper",
                ResourceLocation.parse("minecraft:overworld"),
                12.5,
                70.0,
                -4.5,
                15.0F,
                0.0F,
                new BlockPos(0, 64, 0),
                new BlockPos(3, 64, 3),
                Optional.empty(),
                100L,
                Optional.of("resolved"),
                Optional.of("flood_diverted")
        );
        GeneratedAppraisalRecoverySnapshot snapshot = GeneratedAppraisalRecoverySnapshot.fromPrepared(
                PreviewAppraisalService.prepareWithRewards(instance, "flood_diverted")
        );
        return new NightmareCompletionReceiptData.Receipt(instance, snapshot);
    }
}
