package dev.spud.shadowslave.item;

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

class AshCompassPresentationResourcesTest {
    private static final Path MODEL = Path.of(
            "src/main/resources/assets/shadowslave/models/item/ash_compass_memory.json");
    private static final Path TEXTURE_ROOT = Path.of(
            "src/main/resources/assets/shadowslave/textures/item");

    @Test
    void ashCompassUsesDedicatedThreeDimensionalProjectGeometry() throws IOException {
        String model = Files.readString(MODEL);

        assertTrue(model.contains("\"parent\": \"minecraft:block/block\""));
        assertTrue(model.contains("\"name\": \"compass_body\""));
        assertTrue(model.contains("\"name\": \"face_recess\""));
        assertTrue(model.contains("\"name\": \"bezel_top\""));
        assertTrue(model.contains("\"name\": \"bezel_bottom\""));
        assertTrue(model.contains("\"name\": \"bezel_left\""));
        assertTrue(model.contains("\"name\": \"bezel_right\""));
        assertTrue(model.contains("\"name\": \"needle_spine\""));
        assertTrue(model.contains("\"name\": \"needle_tip\""));
        assertTrue(model.contains("\"name\": \"crown\""));
        assertTrue(model.contains("\"firstperson_righthand\""));
        assertTrue(model.contains("\"thirdperson_righthand\""));
        assertTrue(model.contains("\"gui\""));
        assertFalse(model.contains("minecraft:item/echo_shard"));
        assertFalse(model.contains("minecraft:item/generated"));
    }

    @Test
    void emberFaceIsRecessedBehindForwardBezel() throws IOException {
        String model = Files.readString(MODEL);

        assertTrue(model.contains("\"from\": [4, 4, 5.5]"));
        assertTrue(model.contains("\"from\": [3, 12, 5]"));
        assertTrue(model.contains("\"from\": [3, 3, 5]"));
        assertTrue(model.contains("\"from\": [3, 4, 5]"));
        assertTrue(model.contains("\"from\": [12, 4, 5]"));
        assertTrue(model.contains("\"from\": [7.25, 4.75, 5.25]"));
    }

    @Test
    void projectTexturesReplaceVanillaMaterialPlaceholders() throws IOException {
        String model = Files.readString(MODEL);

        assertTrue(model.contains("shadowslave:item/ash_compass_case"));
        assertTrue(model.contains("shadowslave:item/ash_compass_needle"));
        assertTrue(model.contains("shadowslave:item/ash_compass_ember"));
        assertFalse(model.contains("minecraft:block/polished_blackstone"));
        assertFalse(model.contains("minecraft:block/oxidized_copper"));
        assertFalse(model.contains("minecraft:block/magma"));
        assertFalse(model.contains("modrinth"));
        assertFalse(model.contains("curseforge"));
    }

    @Test
    void projectMaterialTexturesAreReadableSixteenPixelPngs() throws IOException {
        assertTexture("ash_compass_case.png");
        assertTexture("ash_compass_needle.png");
        assertTexture("ash_compass_ember.png");
    }

    private static void assertTexture(String fileName) throws IOException {
        BufferedImage image = ImageIO.read(TEXTURE_ROOT.resolve(fileName).toFile());
        assertNotNull(image, fileName + " must decode as PNG");
        assertEquals(16, image.getWidth(), fileName + " width");
        assertEquals(16, image.getHeight(), fileName + " height");
    }
}
