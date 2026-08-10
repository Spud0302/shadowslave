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

class AshBurrowerTextureResourcesTest {
    private static final Path TEXTURE = Path.of(
            "src/main/resources/assets/shadowslave/textures/entity/ash_burrower.png");

    @Test
    void hostileAndEchoUseTheSameProjectOwnedTexture() throws IOException {
        String hostileModel = Files.readString(Path.of(
                "src/main/java/dev/spud/shadowslave/client/model/AshBurrowerModel.java"));
        String echoModel = Files.readString(Path.of(
                "src/main/java/dev/spud/shadowslave/client/model/AshBurrowerEchoModel.java"));

        String projectTexture = "\"textures/entity/ash_burrower.png\"";
        assertTrue(hostileModel.contains(projectTexture));
        assertTrue(echoModel.contains(projectTexture));
        assertFalse(hostileModel.contains("textures/entity/silverfish.png"));
        assertFalse(echoModel.contains("textures/entity/silverfish.png"));
    }

    @Test
    void textureMatchesGeometryAtlasDimensions() throws IOException {
        assertTrue(Files.isRegularFile(TEXTURE));
        BufferedImage image = ImageIO.read(TEXTURE.toFile());
        assertNotNull(image);
        assertEquals(64, image.getWidth());
        assertEquals(32, image.getHeight());

        String geometry = Files.readString(Path.of(
                "src/main/resources/assets/shadowslave/geo/ash_burrower.geo.json"));
        assertTrue(geometry.contains("\"texture_width\": 64"));
        assertTrue(geometry.contains("\"texture_height\": 32"));
    }
}
