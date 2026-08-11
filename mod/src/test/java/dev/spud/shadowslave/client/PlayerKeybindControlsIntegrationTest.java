package dev.spud.shadowslave.client;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerKeybindControlsIntegrationTest {
    private static final Path KEY_MAPPINGS = Path.of(
            "src/main/java/dev/spud/shadowslave/client/ClientKeyMappings.java");
    private static final Path CLIENT_EVENTS = Path.of(
            "src/main/java/dev/spud/shadowslave/client/ClientGameEvents.java");
    private static final Path PAYLOADS = Path.of(
            "src/main/java/dev/spud/shadowslave/network/ModPayloads.java");
    private static final Path SERVER_HANDLER = Path.of(
            "src/main/java/dev/spud/shadowslave/network/ServerPayloadHandler.java");

    @Test
    void normalGameplayActionsHaveDedicatedRemappableKeybinds() throws IOException {
        String mappings = Files.readString(KEY_MAPPINGS);

        assertTrue(mappings.contains("OPEN_SOUL"));
        assertTrue(mappings.contains("ACTIVATE_KINDLE"));
        assertTrue(mappings.contains("TOGGLE_MEMORY"));
        assertTrue(mappings.contains("TOGGLE_ECHO"));
        assertTrue(mappings.contains("TOGGLE_ECHO_MODE"));
        assertTrue(mappings.contains("GLFW.GLFW_KEY_O"));
        assertTrue(mappings.contains("GLFW.GLFW_KEY_R"));
        assertTrue(mappings.contains("GLFW.GLFW_KEY_G"));
        assertTrue(mappings.contains("GLFW.GLFW_KEY_H"));
        assertTrue(mappings.contains("GLFW.GLFW_KEY_J"));
    }

    @Test
    void clientSendsOnlyIntentAndDoesNotRunCommands() throws IOException {
        String events = Files.readString(CLIENT_EVENTS);

        assertTrue(events.contains("PacketDistributor.sendToServer(payload)"));
        assertTrue(events.contains("minecraft.screen == null"));
        assertTrue(events.contains("ActivateKindleRequestPayload.INSTANCE"));
        assertTrue(events.contains("ToggleAshCompassRequestPayload.INSTANCE"));
        assertTrue(events.contains("ToggleAshBurrowerEchoRequestPayload.INSTANCE"));
        assertTrue(events.contains("ToggleAshBurrowerEchoModeRequestPayload.INSTANCE"));
    }

    @Test
    void dedicatedServerRegistersAndAuthoritativelyExecutesEveryIntent() throws IOException {
        String payloads = Files.readString(PAYLOADS);
        String handler = Files.readString(SERVER_HANDLER);

        assertTrue(payloads.contains("NETWORK_VERSION = \"3\""));
        assertTrue(payloads.contains("ServerPayloadHandler::handleActivateKindle"));
        assertTrue(payloads.contains("ServerPayloadHandler::handleToggleAshCompass"));
        assertTrue(payloads.contains("ServerPayloadHandler::handleToggleAshBurrowerEcho"));
        assertTrue(payloads.contains("ServerPayloadHandler::handleToggleAshBurrowerEchoMode"));
        assertTrue(handler.contains("PreviewPowerService.activateKindle(player)"));
        assertTrue(handler.contains("MemoryManifestationService.summonAshCompass(player)"));
        assertTrue(handler.contains("MemoryManifestationService.dismissAshCompass(player)"));
        assertTrue(handler.contains("EchoManifestationService.summonAshBurrower(player)"));
        assertTrue(handler.contains("EchoManifestationService.dismissAshBurrower(player)"));
        assertTrue(handler.contains("EchoOwnershipService.get(player)"));
        assertTrue(handler.contains("EchoManifestationService.commandAshBurrower(player, nextMode)"));
    }
}
