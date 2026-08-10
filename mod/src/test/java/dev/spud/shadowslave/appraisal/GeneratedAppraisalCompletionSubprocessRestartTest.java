package dev.spud.shadowslave.appraisal;

import dev.spud.shadowslave.echo.EchoOwnershipData;
import dev.spud.shadowslave.memory.MemoryOwnershipData;
import dev.spud.shadowslave.nightmare.NightmareCompletionReceiptData;
import dev.spud.shadowslave.nightmare.NightmareInstance;
import dev.spud.shadowslave.soul.SoulData;
import dev.spud.shadowslave.soul.SoulTransitions;
import dev.spud.shadowslave.soul.identity.AttributeOwnershipData;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
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
 * Cross-process evidence for successful-completion appraisal recovery.
 *
 * <p>The deterministic restart-cut matrix reconstructs production NBT in one JVM. This test adds a
 * stronger boundary: the originating test process writes only the durable receipt image, then a brand-new
 * JVM reads that file through the production receipt codec and plans recovery without retaining the
 * originating process's receipt, Nightmare instance, generator result, or static state.</p>
 */
public class GeneratedAppraisalCompletionSubprocessRestartTest {
    @Test
    void freshJvmReconstructsExactReceiptAndConvergesEmptyAndAlreadyCompleteCuts(@TempDir Path tempDir)
            throws Exception {
        NightmareInstance instance = instance("flood_diverted");
        GeneratedAppraisalRecoverySnapshot snapshot = GeneratedAppraisalRecoverySnapshot.fromPrepared(
                PreviewAppraisalService.prepareWithRewards(instance, instance.terminalResolutionId().orElseThrow())
        );
        NightmareCompletionReceiptData.Receipt receipt = new NightmareCompletionReceiptData.Receipt(instance, snapshot);
        Path receiptFile = tempDir.resolve("completion-receipt.nbt");
        NbtIo.writeCompressed(receipt.save(), receiptFile);

        Path emptyResult = tempDir.resolve("empty-result.txt");
        runFreshJvm(receiptFile, emptyResult, "empty");
        assertEquals(expectedResult(receipt, false), Files.readString(emptyResult));

        Path completeResult = tempDir.resolve("complete-result.txt");
        runFreshJvm(receiptFile, completeResult, "complete");
        assertEquals(expectedResult(receipt, true), Files.readString(completeResult));
    }

    /** Entry point used only by the parent JUnit process to force a real JVM/static-state boundary. */
    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            throw new IllegalArgumentException("Expected <receipt-file> <result-file> <empty|complete>");
        }

        Path receiptFile = Path.of(args[0]);
        Path resultFile = Path.of(args[1]);
        String mode = args[2];
        CompoundTag receiptTag = NbtIo.readCompressed(receiptFile, NbtAccounter.unlimitedHeap());
        NightmareCompletionReceiptData.Receipt receipt = NightmareCompletionReceiptData.Receipt.load(receiptTag);
        GeneratedAppraisalRecoverySnapshot snapshot = receipt.appraisal();

        GeneratedAppraisalRecoveryService.PlayerState source = switch (mode) {
            case "empty" -> emptyAspirant();
            case "complete" -> completeDreamer(snapshot);
            default -> throw new IllegalArgumentException("Unknown subprocess cut: " + mode);
        };

        Optional<NightmareInstance> active = mode.equals("empty")
                ? Optional.of(receipt.instance())
                : Optional.empty();
        Optional<NightmareInstance> teardown = GeneratedAppraisalRecoveryService.activeInstanceForReplay(active, receipt);
        GeneratedAppraisalRecoveryService.RecoveryPlan plan = GeneratedAppraisalRecoveryService.plan(source, snapshot);

        String result = result(plan.target(), receipt, plan.alreadyComplete(), teardown.isPresent());
        Files.writeString(resultFile, result);
    }

    private static void runFreshJvm(Path receiptFile, Path resultFile, String mode) throws Exception {
        Path java = Path.of(System.getProperty("java.home"), "bin", isWindows() ? "java.exe" : "java");
        Process process = new ProcessBuilder(
                java.toString(),
                "-cp",
                System.getProperty("java.class.path"),
                GeneratedAppraisalCompletionSubprocessRestartTest.class.getName(),
                receiptFile.toString(),
                resultFile.toString(),
                mode
        ).redirectErrorStream(true).start();

        assertTrue(process.waitFor(30, TimeUnit.SECONDS), "Fresh recovery JVM did not terminate");
        String output = new String(process.getInputStream().readAllBytes());
        assertEquals(0, process.exitValue(), "Fresh recovery JVM failed:\n" + output);
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    private static String expectedResult(NightmareCompletionReceiptData.Receipt receipt, boolean alreadyComplete) {
        GeneratedAppraisalRecoverySnapshot snapshot = receipt.appraisal();
        GeneratedAppraisalRecoveryService.PlayerState state = alreadyComplete
                ? completeDreamer(snapshot)
                : GeneratedAppraisalRecoveryService.plan(emptyAspirant(), snapshot).target();
        return result(state, receipt, alreadyComplete, !alreadyComplete);
    }

    private static String result(
            GeneratedAppraisalRecoveryService.PlayerState state,
            NightmareCompletionReceiptData.Receipt receipt,
            boolean alreadyComplete,
            boolean teardownPresent
    ) {
        GeneratedAppraisalRecoverySnapshot snapshot = receipt.appraisal();
        return "instance=" + receipt.instance().instanceId() + "\n"
                + "player=" + receipt.instance().playerId() + "\n"
                + "resolution=" + receipt.instance().terminalResolutionId().orElseThrow() + "\n"
                + "snapshotNbt=" + snapshot.save() + "\n"
                + "aspect=" + state.soul().aspectId().orElseThrow() + "\n"
                + "flaw=" + state.soul().flawId().orElseThrow() + "\n"
                + "identity=" + state.identity().equals(snapshot.identity()) + "\n"
                + "attributes=" + state.attributes().attributes().size() + "\n"
                + "memories=" + state.memories().memories().size() + "\n"
                + "echoes=" + state.echoes().echoes().size() + "\n"
                + "alreadyComplete=" + alreadyComplete + "\n"
                + "teardownPresent=" + teardownPresent + "\n";
    }

    private static GeneratedAppraisalRecoveryService.PlayerState emptyAspirant() {
        return new GeneratedAppraisalRecoveryService.PlayerState(
                aspirantSoul(),
                SoulIdentityData.empty(),
                AttributeOwnershipData.empty(),
                MemoryOwnershipData.empty(),
                EchoOwnershipData.empty()
        );
    }

    private static GeneratedAppraisalRecoveryService.PlayerState completeDreamer(
            GeneratedAppraisalRecoverySnapshot snapshot
    ) {
        var aspect = snapshot.identity().aspect().orElseThrow();
        var flaw = snapshot.identity().flaw().orElseThrow();
        SoulData dreamer = aspirantSoul().asDreamer(aspect.instanceId(), aspect.aspectRank(), flaw.instanceId());
        return new GeneratedAppraisalRecoveryService.PlayerState(
                dreamer,
                snapshot.identity(),
                new AttributeOwnershipData(List.of(snapshot.attribute())),
                new MemoryOwnershipData(List.of(snapshot.memory())),
                new EchoOwnershipData(List.of(snapshot.echo()))
        );
    }

    private static SoulData aspirantSoul() {
        return SoulTransitions.beginFirstNightmare(SoulTransitions.infect(SoulData.uninfected()));
    }

    private static NightmareInstance instance(String resolutionId) {
        return new NightmareInstance(
                new UUID(431L, 433L),
                new UUID(439L, 443L),
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
                Optional.of(resolutionId)
        );
    }
}
