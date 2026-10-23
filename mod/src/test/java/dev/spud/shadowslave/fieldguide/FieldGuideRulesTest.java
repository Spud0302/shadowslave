package dev.spud.shadowslave.fieldguide;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.spud.shadowslave.creature.CreatureRank;
import dev.spud.shadowslave.dreamrealm.DreamRegion;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The Field Guide data in the mod's resources, run through the pure guide rules. */
class FieldGuideRulesTest {
    private static final Path DATA = Path.of("src/main/resources/data/shadowslave");
    private static final Path ASSETS = Path.of("src/main/resources/assets/shadowslave");

    private static <T> T parse(Path file, Codec<T> codec) throws IOException {
        return codec.parse(JsonOps.INSTANCE, JsonParser.parseString(Files.readString(file))).getOrThrow();
    }

    private static FieldGuideContent content() throws IOException {
        Map<ResourceLocation, CreaturePage> pages = new HashMap<>();
        try (Stream<Path> files = Files.list(DATA.resolve("field_guide/creature"))) {
            for (Path file : files.toList()) {
                pages.put(ResourceLocation.fromNamespaceAndPath("shadowslave", file.getFileName().toString().replace(".json", "")),
                        parse(file, CreaturePage.CODEC));
            }
        }
        return FieldGuideContent.build(parse(DATA.resolve("field_guide/rules.json"), FieldGuideSettings.CODEC), pages);
    }

    @Test
    void everyRosterCreatureHasAPageThatOpensWithASighting() throws IOException {
        FieldGuideContent content = content();
        for (String creature : new String[]{"ash_burrower", "chainback", "hollow_mimic", "ashen_tyrant"}) {
            CreaturePage page = content.page(ResourceLocation.fromNamespaceAndPath("shadowslave", creature)).orElseThrow();
            assertEquals(1, page.revealed(1, 0).size(), creature + ": one sighting shows rank and class");
            assertEquals(page.sections().size(), page.revealed(100, 100).size(), creature + ": everything opens in the end");
        }
    }

    @Test
    void sectionsOpenBySightingsOrKillsWhicheverComesFirst() {
        CreaturePage.Section section = new CreaturePage.Section("Weakness", "Stone stops it.", Optional.of(6), Optional.of(2));
        assertFalse(section.unlocked(5, 1));
        assertTrue(section.unlocked(6, 0));
        assertTrue(section.unlocked(0, 2));
        assertEquals("Sight it once more or slay 1 more", section.hint(5, 1));
        CreaturePage.Section drops = new CreaturePage.Section("Drops", "Hide.", Optional.empty(), Optional.of(3));
        assertEquals("Slay 3 more", drops.hint(40, 0));
    }

    @Test
    void rulesFileMatchesTheDefaults() throws IOException {
        assertEquals(FieldGuideSettings.DEFAULT, parse(DATA.resolve("field_guide/rules.json"), FieldGuideSettings.CODEC));
    }

    @Test
    void dangerRisesWithWhatIsSeenAndWhoDied() {
        assertEquals(DangerRating.NONE, DangerRating.rate(DangerRating.NONE, DangerRating.NONE, 0, 1));
        assertEquals(CreatureRank.DORMANT.ordinal(), DangerRating.rate(CreatureRank.DORMANT.ordinal(), DangerRating.NONE, 0, 1));
        assertEquals(CreatureRank.FALLEN.ordinal(), DangerRating.rate(CreatureRank.DORMANT.ordinal(), CreatureRank.FALLEN.ordinal(), 0, 1));
        assertEquals(CreatureRank.CORRUPTED.ordinal(), DangerRating.rate(CreatureRank.DORMANT.ordinal(), CreatureRank.FALLEN.ordinal(), 2, 1));
        assertEquals(CreatureRank.UNHOLY.ordinal(), DangerRating.rate(CreatureRank.CURSED.ordinal(), DangerRating.NONE, 1, 3));
        long key = DangerRating.key(-3, 7);
        assertEquals(-3, DangerRating.keyX(key));
        assertEquals(7, DangerRating.keyZ(key));
    }

    @Test
    void dreamRegionsCarryABaselineDanger() throws IOException {
        DreamRegion expanse = parse(DATA.resolve("dream_realm/region/ashen_expanse.json"), DreamRegion.CODEC);
        assertEquals(CreatureRank.DORMANT, expanse.dangerRank());
    }

    @Test
    void sightingsRespectTheCooldownAndExploredChunksSurviveSaving() {
        FieldGuideData data = FieldGuideData.empty();
        ResourceLocation type = ResourceLocation.fromNamespaceAndPath("shadowslave", "chainback");
        assertTrue(data.sight(type, 1, "minecraft:overworld", 10, 20, 100, 600, 1));
        assertFalse(data.sight(type, 2, "minecraft:overworld", 12, 22, 300, 600, 1));
        assertTrue(data.sight(type, 1, "minecraft:overworld", 12, 22, 700, 600, 2));
        assertEquals(3, data.creature(type).orElseThrow().sightings());
        assertEquals(0b110, data.creature(type).orElseThrow().ranksSeen());

        byte[] colours = new byte[FieldGuideData.CHUNK_BYTES];
        for (int i = 0; i < colours.length; i++) {
            colours[i] = (byte) (i * 17 - 100);
        }
        assertTrue(data.explore("minecraft:overworld", 42L, colours, 64));
        assertFalse(data.explore("minecraft:overworld", 42L, colours, 64), "unchanged colours are not news");
        data.markSeen("minecraft:overworld", DangerRating.key(1, 1), 2);
        data.markDeath("minecraft:overworld", DangerRating.key(1, 1));
        data.addPin(new FieldGuideData.Pin(FieldGuideData.PinKind.WARNING, "minecraft:overworld", 5, 6, "Den", Optional.empty()));

        FieldGuideData loaded = FieldGuideData.CODEC.parse(JsonOps.INSTANCE,
                FieldGuideData.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow()).getOrThrow();
        assertArrayEquals(colours, loaded.exploredChunks("minecraft:overworld").get(42L));
        int marks = loaded.dangerMarks("minecraft:overworld").get(DangerRating.key(1, 1));
        assertEquals(2, FieldGuideData.strongest(marks));
        assertEquals(1, FieldGuideData.deaths(marks));
        assertEquals("Den", loaded.pins().get(0).label());
        assertEquals(3, loaded.creature(type).orElseThrow().sightings());
    }

    @Test
    void theOldestChunksFadeBackIntoFogPastTheCap() {
        FieldGuideData data = FieldGuideData.empty();
        byte[] colours = new byte[FieldGuideData.CHUNK_BYTES];
        for (long chunk = 0; chunk < 70; chunk++) {
            data.explore("minecraft:overworld", chunk, colours, 64);
        }
        assertEquals(64, data.exploredChunks("minecraft:overworld").size());
        assertFalse(data.explored("minecraft:overworld", 0L));
        assertTrue(data.explored("minecraft:overworld", 69L));
    }

    @Test
    void theGuideHasItsAssets() {
        assertTrue(Files.exists(ASSETS.resolve("textures/item/field_guide.png")));
        assertTrue(Files.exists(ASSETS.resolve("models/item/field_guide.json")));
        assertTrue(Files.exists(DATA.resolve("recipe/field_guide.json")));
    }
}
