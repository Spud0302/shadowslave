package dev.spud.shadowslave.nightmare;

import com.mojang.serialization.Codec;
import dev.spud.shadowslave.appraisal.GeneratedAppraisalRecoveryService;
import dev.spud.shadowslave.appraisal.GeneratedAppraisalRecoverySnapshot;
import dev.spud.shadowslave.appraisal.PersistedGeneratedAppraisalPlayerVerifier;
import dev.spud.shadowslave.appraisal.PreviewAppraisalService;
import dev.spud.shadowslave.echo.EchoOwnershipData;
import dev.spud.shadowslave.memory.MemoryOwnershipData;
import dev.spud.shadowslave.soul.SoulData;
import dev.spud.shadowslave.soul.SoulTransitions;
import dev.spud.shadowslave.soul.identity.AttributeOwnershipData;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Cross-process evidence for reconstruction from the three persisted successful-completion authority surfaces.
 *
 * <p>This is deliberately below a live NeoForge restart: it materializes compressed SavedData-style receipt and
 * active-ownership images plus a compressed player attachment image, then discards the originating JVM. A fresh
 * JVM reconstructs those files, uses the production receipt codec and recovery planner to converge the player
 * award, and writes the post-replay disk images. A second fresh JVM proves the exact award is committed while
 * active ownership and completion authority are both absent.</p>
 */
class GeneratedAppraisalCompletionDiskImageRestartTest {
    private static final String ATTACHMENTS = "neoforge:attachments";

    @Test
    void persistedThreeSurfaceCutConvergesAcrossTwoFreshJvms(@TempDir Path tempDir) throws Exception {
        NightmareInstance instance = instance();
        GeneratedAppraisalRecoverySnapshot snapshot = GeneratedAppraisalRecoverySnapshot.fromPrepared(
                PreviewAppraisalService.prepareWithRewards(instance, instance.terminalResolutionId().orElseThrow())
        );
        NightmareCompletionReceiptData.Receipt receipt = new NightmareCompletionReceiptData.Receipt(instance, snapshot);

        Path receiptFile = tempDir.resolve("shadowslave_nightmare_completion_receipts.dat");
        Path registryFile = tempDir.resolve("shadowslave_nightmares.dat");
        Path playerFile = tempDir.resolve(instance.playerId() + ".dat");
        writeReceipts(receiptFile, List.of(receipt));
        writeRegistry(registryFile, List.of(instance));
        writePlayer(playerFile, emptyAspirant());

        runFreshJvm("recover", receiptFile, registryFile, playerFile);
        runFreshJvm("verify", receiptFile, registryFile, playerFile);
    }

    /** Entry point used by the parent JUnit process to force real JVM/static-state boundaries. */
    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            throw new IllegalArgumentException("Expected <recover|verify> <receipt-file> <registry-file> <player-file>");
        }
        String mode = args[0];
        Path receiptFile = Path.of(args[1]);
        Path registryFile = Path.of(args[2]);
        Path playerFile = Path.of(args[3]);

        NightmareCompletionReceiptData.Receipt receipt = readOnlyReceipt(receiptFile);
        switch (mode) {
            case "recover" -> recover(receipt, receiptFile, registryFile, playerFile);
            case "verify" -> verify(receipt, receiptFile, registryFile, playerFile);
            default -> throw new IllegalArgumentException("Unknown disk-image restart mode: " + mode);
        }
    }

    private static void recover(
            NightmareCompletionReceiptData.Receipt receipt,
            Path receiptFile,
            Path registryFile,
            Path playerFile
    ) throws Exception {
        PersistedNightmareCompletionReceiptVerifier.requirePresent(receiptFile, receipt);
        NightmareInstance active = readOnlyActiveInstance(registryFile);
        Optional<NightmareInstance> teardown = GeneratedAppraisalRecoveryService.activeInstanceForReplay(
                Optional.of(active), receipt);
        assertEquals(receipt.instance(), teardown.orElseThrow());

        GeneratedAppraisalRecoveryService.PlayerState current = readPlayer(playerFile);
        GeneratedAppraisalRecoveryService.RecoveryPlan plan = GeneratedAppraisalRecoveryService.plan(
                current, receipt.appraisal());
        writePlayer(playerFile, plan.target());
        writeRegistry(registryFile, List.of());
        writeReceipts(receiptFile, List.of());
    }

    private static void verify(
            NightmareCompletionReceiptData.Receipt receipt,
            Path receiptFile,
            Path registryFile,
            Path playerFile
    ) {
        GeneratedAppraisalRecoveryService.PlayerState expected = GeneratedAppraisalRecoveryService.plan(
                emptyAspirant(), receipt.appraisal()).target();
        PersistedGeneratedAppraisalPlayerVerifier.requireCommitted(playerFile, expected, receipt.appraisal());
        PersistedNightmareOwnershipVerifier.requireAbsent(
                registryFile, receipt.instance().playerId(), receipt.instance().instanceId());
        PersistedNightmareCompletionReceiptVerifier.requireAbsent(receiptFile, receipt);
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

    private static GeneratedAppraisalRecoveryService.PlayerState readPlayer(Path file) throws Exception {
        CompoundTag root = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
        CompoundTag attachments = root.getCompound(ATTACHMENTS);
        return new GeneratedAppraisalRecoveryService.PlayerState(
                decode(attachments, "shadowslave:soul", SoulData.CODEC.codec()),
                decode(attachments, "shadowslave:identity", SoulIdentityData.CODEC.codec()),
                decode(attachments, "shadowslave:attributes", AttributeOwnershipData.CODEC.codec()),
                decode(attachments, "shadowslave:memories", MemoryOwnershipData.CODEC.codec()),
                decode(attachments, "shadowslave:echoes", EchoOwnershipData.CODEC.codec())
        );
    }

    private static <T> T decode(CompoundTag attachments, String key, Codec<T> codec) {
        Tag value = attachments.get(key);
        if (value == null) {
            throw new IllegalStateException("Missing persisted attachment " + key);
        }
        return codec.parse(NbtOps.INSTANCE, value).getOrThrow();
    }

    private static void writePlayer(Path file, GeneratedAppraisalRecoveryService.PlayerState state) throws Exception {
        CompoundTag attachments = new CompoundTag();
        attachments.put("shadowslave:soul", encode(SoulData.CODEC.codec(), state.soul()));
        attachments.put("shadowslave:identity", encode(SoulIdentityData.CODEC.codec(), state.identity()));
        attachments.put("shadowslave:attributes", encode(AttributeOwnershipData.CODEC.codec(), state.attributes()));
        attachments.put("shadowslave:memories", encode(MemoryOwnershipData.CODEC.codec(), state.memories()));
        attachments.put("shadowslave:echoes", encode(EchoOwnershipData.CODEC.codec(), state.echoes()));
        CompoundTag root = new CompoundTag();
        root.put(ATTACHMENTS, attachments);
        NbtIo.writeCompressed(root, file);
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

    private static <T> Tag encode(Codec<T> codec, T value) {
        return codec.encodeStart(NbtOps.INSTANCE, value).getOrThrow();
    }

    private static GeneratedAppraisalRecoveryService.PlayerState emptyAspirant() {
        return new GeneratedAppraisalRecoveryService.PlayerState(
                SoulTransitions.beginFirstNightmare(SoulTransitions.infect(SoulData.uninfected())),
                SoulIdentityData.empty(),
                AttributeOwnershipData.empty(),
                MemoryOwnershipData.empty(),
                EchoOwnershipData.empty()
        );
    }

    private static NightmareInstance instance() {
        return new NightmareInstance(
                new UUID(701L, 709L),
                new UUID(719L, 727L),
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
    }

    private static void runFreshJvm(String mode, Path receiptFile, Path registryFile, Path playerFile) throws Exception {
        Path java = Path.of(System.getProperty("java.home"), "bin", isWindows() ? "java.exe" : "java");
        Process process = new ProcessBuilder(
                java.toString(), "-cp", System.getProperty("java.class.path"),
                GeneratedAppraisalCompletionDiskImageRestartTest.class.getName(),
                mode, receiptFile.toString(), registryFile.toString(), playerFile.toString()
        ).redirectErrorStream(true).start();
        assertTrue(process.waitFor(30, TimeUnit.SECONDS), "Fresh disk-image recovery JVM did not terminate");
        String output = new String(process.getInputStream().readAllBytes());
        assertEquals(0, process.exitValue(), "Fresh disk-image recovery JVM failed:\n" + output);
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
