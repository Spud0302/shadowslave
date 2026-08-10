package dev.spud.shadowslave.item;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AshCompassPresentationResourcesTest {
    private static final Path MODEL = Path.of(
            "src/main/resources/assets/shadowslave/models/item/ash_compass_memory.json");

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

        // North/front points toward decreasing Z. The face begins at z=5.5 while
        // every bezel strip begins at z=5.0, leaving the face visibly inset by 0.5.
        assertTrue(model.contains("\"from\": [4, 4, 5.5]"));
        assertTrue(model.contains("\"from\": [3, 12, 5]"));
        assertTrue(model.contains("\"from\": [3, 3, 5]"));
        assertTrue(model.contains("\"from\": [3, 4, 5]"));
        assertTrue(model.contains("\"from\": [12, 4, 5]"));
        assertTrue(model.contains("\"from\": [7.25, 4.75, 5.25]"));
    }

    @Test
    void materialPlaceholdersUseOnlyBundledVanillaResources() throws IOException {
        String model = Files.readString(MODEL);

        assertTrue(model.contains("minecraft:block/polished_blackstone"));
        assertTrue(model.contains("minecraft:block/oxidized_copper"));
        assertTrue(model.contains("minecraft:block/magma"));
        assertFalse(model.contains("textures/entity"));
        assertFalse(model.contains("modrinth"));
        assertFalse(model.contains("curseforge"));
    }
}
