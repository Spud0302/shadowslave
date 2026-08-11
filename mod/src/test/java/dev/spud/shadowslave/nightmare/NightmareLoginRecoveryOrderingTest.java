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
        String loginHandler = methodBody(
                Files.readString(source),
                "public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)");

        String replayGuard = "if (GeneratedAppraisalRecoveryService.replayPending(player)) {";
        int replayIndex = loginHandler.indexOf(replayGuard);
        int returnIndex = loginHandler.indexOf("return;", replayIndex);
        int activeIndex = loginHandler.indexOf("NightmareService.activeFor(player).ifPresent", replayIndex);

        assertTrue(replayIndex >= 0, "login must consult durable successful-completion recovery first");
        assertTrue(returnIndex > replayIndex && returnIndex < activeIndex,
                "handled completion replay must return before ordinary active-Nightmare reconciliation");
        assertTrue(activeIndex > replayIndex,
                "ordinary active-instance reconciliation must remain after completion-receipt replay");
    }

    private static String methodBody(String source, String signature) {
        int signatureIndex = source.indexOf(signature);
        assertTrue(signatureIndex >= 0, "login handler signature must remain present");

        int openingBrace = source.indexOf('{', signatureIndex + signature.length());
        assertTrue(openingBrace >= 0, "login handler must have a method body");

        int depth = 0;
        for (int index = openingBrace; index < source.length(); index++) {
            char current = source.charAt(index);
            if (current == '{') {
                depth++;
            } else if (current == '}') {
                depth--;
                if (depth == 0) {
                    return source.substring(openingBrace + 1, index);
                }
            }
        }

        throw new AssertionError("login handler body must have balanced braces");
    }
}
