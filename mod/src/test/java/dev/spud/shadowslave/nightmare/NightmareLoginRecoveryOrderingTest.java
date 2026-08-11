package dev.spud.shadowslave.nightmare;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class NightmareLoginRecoveryOrderingTest {
    @Test
    void durableCompletionReplayOutranksOrdinaryActiveInstanceReconciliation() throws IOException {
        Path source = Path.of("src/main/java/dev/spud/shadowslave/nightmare/NightmareEvents.java");
        String text = Files.readString(source);

        String replayGuard = "if (GeneratedAppraisalRecoveryService.replayPending(player)) {";
        int replayIndex = text.indexOf(replayGuard);
        int returnIndex = text.indexOf("return;", replayIndex);
        int activeIndex = text.indexOf("NightmareService.activeFor(player).ifPresent", replayIndex);

        assertTrue(replayIndex >= 0, "login must consult durable successful-completion recovery first");
        assertTrue(returnIndex > replayIndex && returnIndex < activeIndex,
                "handled completion replay must return before ordinary active-Nightmare reconciliation");
        assertTrue(activeIndex > replayIndex,
                "ordinary active-instance reconciliation must remain after completion-receipt replay");
    }
}
