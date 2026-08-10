package dev.spud.shadowslave.client;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DrownedListenerPresentationResourcesTest {
    @Test
    void geckoResourcesExposeExpectedPresentationContract() throws IOException {
        String geometry = Files.readString(Path.of(
                "src/main/resources/assets/shadowslave/geo/drowned_listener.geo.json"));
        String animations = Files.readString(Path.of(
                "src/main/resources/assets/shadowslave/animations/drowned_listener.animation.json"));
        String entitySource = Files.readString(Path.of(
                "src/main/java/dev/spud/shadowslave/world/entity/DrownedListenerEntity.java"));

        assertTrue(geometry.contains("geometry.shadowslave.drowned_listener"));
        assertTrue(geometry.contains("\"listener_fin_left\""));
        assertTrue(geometry.contains("\"listener_fin_right\""));
        assertTrue(geometry.contains("\"throat_fan\""));

        assertTrue(animations.contains("\"misc.idle\""));
        assertTrue(animations.contains("\"move.walk\""));
        assertTrue(animations.contains("\"move.swim\""));
        assertTrue(animations.contains("\"attack.strike\""));

        // The custom locomotion controller must choose the swim clip from physical water state.
        assertTrue(entitySource.contains("RawAnimation.begin().thenLoop(\"move.swim\")"));
        assertTrue(entitySource.contains("isInWaterOrBubble() && state.isMoving()"));
        assertTrue(entitySource.contains("state.setAndContinue(DefaultAnimations.WALK)"));
        assertTrue(entitySource.contains("state.setAndContinue(DefaultAnimations.IDLE)"));
    }
}
