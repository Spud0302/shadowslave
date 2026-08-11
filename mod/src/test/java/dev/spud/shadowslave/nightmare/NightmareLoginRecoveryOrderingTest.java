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
        assertTrue(replayIndex >= 0, "login must consult durable successful-completion recovery first");

        int replayOpeningBrace = loginHandler.indexOf('{', replayIndex);
        int replayClosingBrace = matchingClosingBrace(loginHandler, replayOpeningBrace);
        String replayBody = loginHandler.substring(replayOpeningBrace + 1, replayClosingBrace);
        int activeIndex = loginHandler.indexOf("NightmareService.activeFor(player).ifPresent");

        assertTrue(replayBody.matches("\\s*return;\\s*"),
                "handled completion replay guard must return immediately without conditional fallthrough");
        assertTrue(activeIndex > replayClosingBrace,
                "ordinary active-instance reconciliation must remain after the completed replay guard");
    }

    private static String methodBody(String source, String signature) {
        int signatureIndex = source.indexOf(signature);
        assertTrue(signatureIndex >= 0, "login handler signature must remain present");

        int openingBrace = source.indexOf('{', signatureIndex + signature.length());
        assertTrue(openingBrace >= 0, "login handler must have a method body");
        int closingBrace = matchingClosingBrace(source, openingBrace);
        return source.substring(openingBrace + 1, closingBrace);
    }

    private static int matchingClosingBrace(String source, int openingBrace) {
        assertTrue(openingBrace >= 0 && source.charAt(openingBrace) == '{',
                "balanced block search must begin on an opening brace");

        int depth = 0;
        for (int index = openingBrace; index < source.length(); index++) {
            char current = source.charAt(index);
            if (current == '{') {
                depth++;
            } else if (current == '}') {
                depth--;
                if (depth == 0) {
                    return index;
                }
            }
        }

        throw new AssertionError("source block must have balanced braces");
    }
}
