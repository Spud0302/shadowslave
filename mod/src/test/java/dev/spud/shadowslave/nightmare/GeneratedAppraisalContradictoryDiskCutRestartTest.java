package dev.spud.shadowslave.nightmare;

import dev.spud.shadowslave.appraisal.GeneratedAppraisalRecoveryService;
import dev.spud.shadowslave.appraisal.GeneratedAppraisalRecoverySnapshot;
import dev.spud.shadowslave.appraisal.PreviewAppraisalService;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Fresh-process evidence that contradictory active ownership cannot consume completion recovery authority.
 *
 * <p>The durable receipt and registry are reconstructed from compressed NBT in a child JVM. The production
 * replay selector must reject an active Nightmare that belongs to the same player but is not the exact receipt
 * instance. The parent then proves the child left both durable images byte-for-byte unchanged.</p>
 */
class GeneratedAppraisalContradictoryDiskCutRestartTest {
    @Test
    void contradictoryPersistedOwnershipFailsClosedAcrossFreshJvm(@TempDir Path tempDir) throws Exception {
        NightmareInstance completed = instance(new UUID(701L, 709L), new UUID(719L, 727L), 2);
        NightmareInstance contradictoryActive = instance(completed.playerId(), new UUID(733L, 739L), 3);
        assertEquals(completed.playerId(), contradictoryActive.playerId(),
                "Fixture must model a different active Nightmare for the same player");
        assertNotEquals(completed.instanceId(), contradictoryActive.instanceId(),
                "Fixture must keep the contradictory active Nightmare instance distinct from the receipt");

        GeneratedAppraisalRecoverySnapshot snapshot = GeneratedAppraisalRecoverySnapshot.fromPrepared(
                PreviewAppraisalService.prepareWithRewards(
                        completed,
                        completed.terminalResolutionId().orElseThrow()
                )
        );
        NightmareCompletionReceiptData.Receipt receipt =
                new NightmareCompletionReceiptData.Receipt(completed, snapshot);

        Path receiptFile = tempDir.resolve("shadowslave_nightmare_completion_receipts.dat");
        Path registryFile = tempDir.resolve("shadowslave_nightmares.dat");
        writeReceipts(receiptFile, List.of(receipt));
        writeRegistry(registryFile, List.of(contradictoryActive));

        byte[] receiptBefore = Files.readAllBytes(receiptFile);
        byte[] registryBefore = Files.readAllBytes(registryFile);

        runFreshJvm(receiptFile, registryFile);

        assertArrayEquals(receiptBefore, Files.readAllBytes(receiptFile),
                "Contradictory replay must retain the durable completion receipt unchanged");
        assertArrayEquals(registryBefore, Files.readAllBytes(registryFile),
                "Contradictory replay must retain unrelated active ownership unchanged");
    }

    /** Entry point used by the parent JUnit process to force a real JVM/static-state boundary. */
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Expected <receipt-file> <registry-file>");
        }
        NightmareCompletionReceiptData.Receipt receipt = readOnlyReceipt(Path.of(args[0]));
        NightmareInstance active = readOnlyActiveInstance(Path.of(args[1]));

        try {
            selectActiveInstanceForReplay(Optional.of(active), receipt);
            fail("Contradictory persisted ownership was accepted by the production replay selector");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("contradicts the completion receipt"),
                    "Unexpected fail-closed reason: " + expected.getMessage());
        }
    }

    /** Calls the package-private production selector without widening its runtime API for test evidence. */
    @SuppressWarnings("unchecked")
    private static Optional<NightmareInstance> selectActiveInstanceForReplay(
            Optional<NightmareInstance> active,
            NightmareCompletionReceiptData.Receipt receipt
    ) throws Exception {
        Method selector = GeneratedAppraisalRecoveryService.class.getDeclaredMethod(
                "activeInstanceForReplay",
                Optional.class,
                NightmareCompletionReceiptData.Receipt.class
        );
        selector.setAccessible(true);
        try {
            return (Optional<NightmareInstance>) selector.invoke(null, active, receipt);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof Exception checked) {
                throw checked;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException("Production replay selector failed", cause);
        }
    }

    private static NightmareCompletionReceiptData.Receipt readOnlyReceipt(Path file) throws Exception {
        CompoundTag root = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
        ListTag receipts = root.getCompound("data").getList("receipts", Tag.TAG_COMPOUND);
        if (receipts.size() != 1) {
            throw new IllegalStateException("Expected exactly one completion receipt at restart boundary");
        }
        return NightmareCompletionReceiptData.Receipt.load(receipts.getCompound(0));
    }

    private static NightmareInstance readOnlyActiveInstance(Path file) throws Exception {
        CompoundTag root = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
        ListTag instances = root.getCompound("data").getList("instances", Tag.TAG_COMPOUND);
        if (instances.size() != 1) {
            throw new IllegalStateException("Expected exactly one active Nightmare at restart boundary");
        }
        return NightmareInstance.load(instances.getCompound(0));
    }

    private static void writeRegistry(Path file, List<NightmareInstance> instances) throws Exception {
        ListTag entries = new ListTag();
        for (NightmareInstance instance : instances) {
            entries.add(instance.save());
        }
        CompoundTag data = new CompoundTag();
        data.put("instances", entries);
        CompoundTag root = new CompoundTag();
        root.put("data", data);
        NbtIo.writeCompressed(root, file);
    }

    private static void writeReceipts(Path file, List<NightmareCompletionReceiptData.Receipt> receipts) throws Exception {
        ListTag entries = new ListTag();
        for (NightmareCompletionReceiptData.Receipt receipt : receipts) {
            entries.add(receipt.save());
        }
        CompoundTag data = new CompoundTag();
        data.put("receipts", entries);
        CompoundTag root = new CompoundTag();
        root.put("data", data);
        NbtIo.writeCompressed(root, file);
    }

    private static NightmareInstance instance(UUID playerId, UUID instanceId, int slot) {
        return new NightmareInstance(
                instanceId,
                playerId,
                slot,
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
    }

    private static void runFreshJvm(Path receiptFile, Path registryFile) throws Exception {
        Path java = Path.of(System.getProperty("java.home"), "bin", isWindows() ? "java.exe" : "java");
        Process process = new ProcessBuilder(
                java.toString(),
                "-cp",
                System.getProperty("java.class.path"),
                GeneratedAppraisalContradictoryDiskCutRestartTest.class.getName(),
                receiptFile.toString(),
                registryFile.toString()
        ).redirectErrorStream(true).start();
        assertTrue(process.waitFor(30, TimeUnit.SECONDS), "Fresh contradiction JVM did not terminate");
        String output = new String(process.getInputStream().readAllBytes());
        assertEquals(0, process.exitValue(), "Fresh contradiction JVM failed:\n" + output);
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
