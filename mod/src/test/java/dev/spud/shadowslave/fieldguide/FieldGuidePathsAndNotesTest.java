package dev.spud.shadowslave.fieldguide;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Traced paths and Field Notes, run through the pure guide data. */
class FieldGuidePathsAndNotesTest {
    private static final String OVERWORLD = "minecraft:overworld";
    private static final ResourceLocation BURROWER = ResourceLocation.fromNamespaceAndPath("shadowslave", "ash_burrower");

    @Test
    void walkingCountsAndLearningDoesNot() {
        FieldGuideData guide = FieldGuideData.empty();
        assertEquals(1, guide.walk(OVERWORLD, 10L, 100));
        assertEquals(2, guide.walk(OVERWORLD, 10L, 100));
        assertTrue(guide.learnPath(OVERWORLD, 11L, 100), "a path someone else walked is new to this guide");
        assertFalse(guide.learnPath(OVERWORLD, 10L, 100), "but one it has walked is not");
        assertEquals(0, guide.pathCells(OVERWORLD).get(11L), "and learning it is not a walk of one's own");
        assertEquals(2, guide.pathCells(OVERWORLD).get(10L));
    }

    @Test
    void theOldestPathsFadeFirstAndWalkingKeepsOneFresh() {
        FieldGuideData guide = FieldGuideData.empty();
        guide.walk(OVERWORLD, 1L, 2);
        guide.walk(OVERWORLD, 2L, 2);
        guide.walk(OVERWORLD, 1L, 2);
        guide.walk(OVERWORLD, 3L, 2);
        assertTrue(guide.hasPath(OVERWORLD, 1L), "walked again, so it is fresh");
        assertFalse(guide.hasPath(OVERWORLD, 2L), "the stalest cell fades");
        assertTrue(guide.hasPath(OVERWORLD, 3L));
    }

    @Test
    void aTracedLineHasNoGaps() {
        List<Long> line = FieldGuideService.line(0, 0, 5, -3);
        assertEquals(DangerRating.key(5, -3), line.get(line.size() - 1), "it ends where the player is");
        int x = 0;
        int z = 0;
        for (long cell : line) {
            int nx = DangerRating.keyX(cell);
            int nz = DangerRating.keyZ(cell);
            assertTrue(Math.abs(nx - x) <= 1 && Math.abs(nz - z) <= 1, "each step touches the last");
            x = nx;
            z = nz;
        }
    }

    @Test
    void aNoteTeachesPlacesAndPathsButNeverAnAnchor() {
        FieldGuideData note = FieldGuideData.empty();
        note.addPin(new FieldGuideData.Pin(FieldGuideData.PinKind.PLACE, OVERWORLD, 5, 5, "Stash", Optional.empty()));
        note.addPin(new FieldGuideData.Pin(FieldGuideData.PinKind.ANCHOR, OVERWORLD, 9, 9, "Anchor", Optional.empty()));
        note.learnPath(OVERWORLD, 7L, 100);
        note.sight(BURROWER, 0, OVERWORLD, 0, 0, 0, 0, 3);

        FieldGuideData reader = FieldGuideData.empty();
        FieldGuideData.Learned learned = reader.learnFrom(note, 16384, 8192, true);
        assertEquals(new FieldGuideData.Learned(1, 0, 1, 1, 0), learned);
        assertTrue(reader.pins().stream().anyMatch(pin -> pin.label().equals("Stash")), "a copied place passes over");
        assertTrue(reader.pins().stream().noneMatch(pin -> pin.kind() == FieldGuideData.PinKind.ANCHOR), "an anchor never does");
        assertEquals(List.of("1 creature page", "1 marked place", "1 stretch of path"), learned.describe());
        assertFalse(reader.learnFrom(note, 16384, 8192, true).any(), "reading it twice teaches nothing new");
    }

    @Test
    void pathsTracingAndNotesSurviveSaving() {
        FieldGuideData guide = FieldGuideData.empty();
        guide.setTracing(true);
        guide.walk(OVERWORLD, 42L, 100);
        guide.walk(OVERWORLD, 42L, 100);
        FieldGuideData loaded = FieldGuideData.CODEC.parse(JsonOps.INSTANCE,
                FieldGuideData.CODEC.encodeStart(JsonOps.INSTANCE, guide).getOrThrow()).getOrThrow();
        assertTrue(loaded.tracing());
        assertEquals(2, loaded.pathCells(OVERWORLD).get(42L));

        FieldNotePage page = new FieldNotePage(FieldNotePage.Kind.PATH, "Paths near 0, 0", "Jack", List.of("1 stretch of path"),
                guide, UUID.randomUUID());
        FieldNotePage read = FieldNotePage.CODEC.parse(JsonOps.INSTANCE, FieldNotePage.CODEC.encodeStart(JsonOps.INSTANCE, page).getOrThrow())
                .getOrThrow();
        assertEquals(page, read, "a note is the same note after saving");
        assertEquals("Jack", read.author());
        assertTrue(read.knowledge().hasPath(OVERWORLD, 42L));
    }

    @Test
    void theRulesFileSetsPathsNotesAndLostNotes() throws IOException {
        FieldGuideSettings settings = FieldGuideSettings.CODEC.parse(JsonOps.INSTANCE,
                JsonParser.parseString(Files.readString(Path.of("src/main/resources/data/shadowslave/field_guide/rules.json")))).getOrThrow();
        assertEquals(8, settings.paths().establishedWalks());
        assertTrue(settings.paths().walksPerPlayer() >= settings.paths().establishedWalks(),
                "one player alone can still wear a road in single player");
        assertEquals(2, settings.notes().cost().size(), "paper and ink");
        assertFalse(settings.notes().lost().warnings().isEmpty());
        assertFalse(settings.notes().lost().endings().isEmpty());
    }
}
