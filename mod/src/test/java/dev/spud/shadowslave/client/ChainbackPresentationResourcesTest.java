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

class ChainbackPresentationResourcesTest {
    private static final Path TEXTURE = Path.of(
            "src/main/resources/assets/shadowslave/textures/entity/chainback.png");

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

    @Test
    void modelUsesProjectOwnedTextureInsteadOfSpiderFallback() throws IOException {
        String model = Files.readString(Path.of(
                "src/main/java/dev/spud/shadowslave/client/model/ChainbackModel.java"));

        assertTrue(model.contains("\"textures/entity/chainback.png\""));
        assertFalse(model.contains("textures/entity/spider/spider.png"));
    }

    @Test
    void textureMatchesGeometryAtlasDimensions() throws IOException {
        assertTrue(Files.isRegularFile(TEXTURE));
        BufferedImage image = ImageIO.read(TEXTURE.toFile());
        assertNotNull(image);
        assertEquals(64, image.getWidth());
        assertEquals(64, image.getHeight());

        String geometry = Files.readString(Path.of(
                "src/main/resources/assets/shadowslave/geo/chainback.geo.json"));
        assertTrue(geometry.contains("\"texture_width\": 64"));
        assertTrue(geometry.contains("\"texture_height\": 64"));
    }
}
