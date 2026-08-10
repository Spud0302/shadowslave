package dev.spud.shadowslave.client;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DrownedListenerPresentationResourcesTest {
    private static final Path TEXTURE = Path.of(
            "src/main/resources/assets/shadowslave/textures/entity/drowned_listener.png");

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

        assertTrue(entitySource.contains("RawAnimation.begin().thenLoop(\"move.swim\")"));
        assertTrue(entitySource.contains("isInWaterOrBubble() && state.isMoving()"));
        assertTrue(entitySource.contains("state.setAndContinue(DefaultAnimations.WALK)"));
        assertTrue(entitySource.contains("state.setAndContinue(DefaultAnimations.IDLE)"));
    }

    @Test
    void modelUsesProjectOwnedTextureInsteadOfDrownedFallback() throws IOException {
        String model = Files.readString(Path.of(
                "src/main/java/dev/spud/shadowslave/client/model/DrownedListenerModel.java"));

        assertTrue(model.contains("\"textures/entity/drowned_listener.png\""));
        assertFalse(model.contains("textures/entity/zombie/drowned.png"));
    }

    @Test
    void textureMatchesGeometryAtlasDimensions() throws IOException {
        assertTrue(Files.isRegularFile(TEXTURE));
        BufferedImage image = ImageIO.read(TEXTURE.toFile());
        assertNotNull(image);
        assertEquals(64, image.getWidth());
        assertEquals(64, image.getHeight());

        String geometry = Files.readString(Path.of(
                "src/main/resources/assets/shadowslave/geo/drowned_listener.geo.json"));
        assertTrue(geometry.contains("\"texture_width\": 64"));
        assertTrue(geometry.contains("\"texture_height\": 64"));
    }
}
