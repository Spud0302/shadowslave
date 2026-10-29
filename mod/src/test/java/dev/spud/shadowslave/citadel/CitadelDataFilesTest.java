package dev.spud.shadowslave.citadel;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The Citadel data files parse, and the structure's palette and placement are sane. */
class CitadelDataFilesTest {
    private static final Path DATA = Path.of("src/main/resources/data/shadowslave");

    private static JsonObject json(String file) throws IOException {
        return JsonParser.parseString(Files.readString(DATA.resolve(file))).getAsJsonObject();
    }

    @Test
    void settingsParse() throws IOException {
        CitadelSettings settings = CitadelSettings.CODEC.parse(JsonOps.INSTANCE, json("citadel/settings.json")).getOrThrow();
        assertEquals("shadowslave:ashen_tyrant", settings.guardian().toString());
        assertTrue(settings.guardRadius() >= 4);
        assertFalse(settings.names().isEmpty());
    }

    @Test
    void structureAndSetAgree() throws IOException {
        JsonObject structure = json("worldgen/structure/citadel.json");
        assertEquals("shadowslave:citadel", structure.get("type").getAsString());
        assertEquals("#shadowslave:has_structure/citadel", structure.get("biomes").getAsString());
        int half = structure.get("half_size").getAsInt();
        assertTrue(half >= 8 && half <= 20);
        JsonObject placement = json("worldgen/structure_set/citadels.json").getAsJsonObject("placement");
        assertTrue(placement.get("separation").getAsInt() < placement.get("spacing").getAsInt(), "separation must be below spacing");
        // A Citadel spans 2 * (half + 1) + 1 blocks; neighbours must never overlap.
        assertTrue(placement.get("separation").getAsInt() * 16 > 2 * (half + 1) + 1);
        assertTrue(Files.exists(DATA.resolve("tags/worldgen/biome/has_structure/citadel.json")));
    }

    @Test
    void wearIsFixedByPlace() {
        net.minecraft.core.BlockPos centre = new net.minecraft.core.BlockPos(100, 70, -300);
        net.minecraft.core.BlockPos block = centre.offset(12, 3, 5);
        double first = CitadelLayout.chance(block, centre);
        assertEquals(first, CitadelLayout.chance(block, centre), "every chunk sees the same wear");
        assertTrue(first >= 0 && first < 1);
    }
}
