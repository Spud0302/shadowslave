package dev.spud.shadowslave.fieldguide;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** A clan's Field Guide board: guides learn from each other and nothing is ever lost. */
class FieldGuideBoardTest {
    private static final ResourceLocation BURROWER = ResourceLocation.fromNamespaceAndPath("shadowslave", "ash_burrower");
    private static final ResourceLocation CHAINBACK = ResourceLocation.fromNamespaceAndPath("shadowslave", "chainback");
    private static final String REALM = "shadowslave:dream_realm";

    @Test
    void aGuideLearnsPagesMapAndSharedPinsFromAnother() {
        FieldGuideData board = FieldGuideData.empty();
        FieldGuideData scout = FieldGuideData.empty();
        scout.sight(BURROWER, 0, REALM, 10, 20, 100, 600, 3);
        scout.kill(BURROWER, 1, REALM, 12, 22);
        scout.explore(REALM, 5L, new byte[FieldGuideData.CHUNK_BYTES], 16384);
        UUID seed = UUID.randomUUID();
        scout.addPin(new FieldGuideData.Pin(FieldGuideData.PinKind.SEED, REALM, 40, 40, "Nightmare Seed", Optional.of(seed)));
        scout.addPin(new FieldGuideData.Pin(FieldGuideData.PinKind.PLACE, REALM, 1, 1, "My stash", Optional.empty()));
        scout.markSeen(REALM, 7L, 2);

        FieldGuideData.Learned learned = board.learnFrom(scout, 16384);
        assertEquals(new FieldGuideData.Learned(1, 1, 1, 0, 1), learned, "one page, one chunk, one shared pin, one danger mark");
        assertEquals(3, board.creature(BURROWER).orElseThrow().sightings());
        assertEquals(1, board.creature(BURROWER).orElseThrow().kills());
        assertTrue(board.explored(REALM, 5L));
        assertTrue(board.hasPin(seed), "Seeds are shared");
        assertFalse(board.pins().stream().anyMatch(pin -> pin.label().equals("My stash")), "a player's own marked places stay theirs");
        assertEquals(2, FieldGuideData.strongest(board.dangerMarks(REALM).get(7L)));

        // Learning again changes nothing; the board's copy is its own.
        assertFalse(board.learnFrom(scout, 16384).any());
        scout.kill(BURROWER, 1, REALM, 12, 22);
        assertEquals(1, board.creature(BURROWER).orElseThrow().kills(), "the board keeps its own record");
    }

    @Test
    void countsOnlyGrowAndRanksAddUp() {
        FieldGuideData veteran = FieldGuideData.empty();
        veteran.sight(CHAINBACK, 0, REALM, 0, 0, 0, 600, 5);
        FieldGuideData novice = FieldGuideData.empty();
        novice.sight(CHAINBACK, 2, REALM, 0, 0, 0, 600, 1);

        FieldGuideData.Learned learned = veteran.learnFrom(novice, 16384);
        assertEquals(1, learned.pages(), "a new rank seen fills out the page");
        assertEquals(5, veteran.creature(CHAINBACK).orElseThrow().sightings(), "fewer sightings never lower the count");
        assertEquals(0b101, veteran.creature(CHAINBACK).orElseThrow().ranksSeen());
    }
}
