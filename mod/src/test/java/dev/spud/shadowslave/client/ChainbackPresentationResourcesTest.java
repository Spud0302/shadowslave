package dev.spud.shadowslave.client;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ChainbackPresentationResourcesTest {
    @Test
    void geckoResourcesExposeExpectedPresentationContract() throws IOException {
        String geometry = Files.readString(Path.of(
                "src/main/resources/assets/shadowslave/geo/chainback.geo.json"));
        String animations = Files.readString(Path.of(
                "src/main/resources/assets/shadowslave/animations/chainback.animation.json"));

        assertTrue(geometry.contains("geometry.shadowslave.chainback"));
        assertTrue(geometry.contains("\"chain_left\""));
        assertTrue(geometry.contains("\"chain_right\""));
        assertTrue(geometry.contains("\"back_chain\""));

        // GeckoLib DefaultAnimations resolves these exact controller keys.
        assertTrue(animations.contains("\"misc.idle\""));
        assertTrue(animations.contains("\"move.walk\""));
        assertTrue(animations.contains("\"attack.strike\""));
    }
}
