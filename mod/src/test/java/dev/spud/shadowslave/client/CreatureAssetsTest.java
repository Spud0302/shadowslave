package dev.spud.shadowslave.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** The creature assets drawn by mod/tools/draw_creatures.py line up with what the entities play. */
class CreatureAssetsTest {
    private static final Path ASSETS = Path.of("src/main/resources/assets/shadowslave");
    private static final Map<String, List<String>> ANIMATIONS = Map.of(
            "ash_burrower", List.of("idle", "walk", "burrowed", "emerge", "bite"),
            "chainback", List.of("idle", "walk", "climb", "slam", "chain_throw"),
            "hollow_mimic", List.of("idle", "walk", "speak", "claw"),
            "ashen_tyrant", List.of("idle", "walk", "slam", "swipe", "brood"));

    @Test
    void everyCreatureHasGeometryTextureAndTheAnimationsItPlays() throws IOException {
        for (var entry : ANIMATIONS.entrySet()) {
            String name = entry.getKey();
            JsonObject geo = read("geo/entity/" + name + ".geo.json");
            JsonObject anims = read("animations/entity/" + name + ".animation.json").getAsJsonObject("animations");
            assertTrue(Files.exists(ASSETS.resolve("textures/entity/" + name + ".png")), name + " texture");

            Set<String> bones = new HashSet<>();
            geo.getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject().getAsJsonArray("bones")
                    .forEach(bone -> bones.add(bone.getAsJsonObject().get("name").getAsString()));
            assertTrue(bones.contains("head"), name + " has a head bone for look rotation");

            for (String animation : entry.getValue()) {
                assertTrue(anims.has(animation), name + " plays " + animation);
                for (String bone : anims.getAsJsonObject(animation).getAsJsonObject("bones").keySet()) {
                    assertTrue(bones.contains(bone), name + "/" + animation + " animates a real bone: " + bone);
                }
            }
        }
    }

    private static JsonObject read(String path) throws IOException {
        return JsonParser.parseString(Files.readString(ASSETS.resolve(path))).getAsJsonObject();
    }
}
