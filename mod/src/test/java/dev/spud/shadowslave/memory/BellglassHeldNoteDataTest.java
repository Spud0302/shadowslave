package dev.spud.shadowslave.memory;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BellglassHeldNoteDataTest {
    @Test
    void capturedNoteOwnsExactInstrumentAndPitchIndex() {
        BellglassHeldNoteData data = BellglassHeldNoteData.captured("HARP", 12);

        assertTrue(data.hasNote());
        assertEquals(Optional.of("HARP"), data.instrument());
        assertEquals(Optional.of(12), data.note());
    }

    @Test
    void clearReturnsEmptyPayload() {
        BellglassHeldNoteData cleared = BellglassHeldNoteData.captured("BASS", 7).clear();

        assertFalse(cleared.hasNote());
        assertEquals(BellglassHeldNoteData.empty(), cleared);
    }

    @Test
    void halfPopulatedPayloadFailsClosed() {
        assertThrows(IllegalArgumentException.class,
                () -> new BellglassHeldNoteData(Optional.of("HARP"), Optional.empty()));
        assertThrows(IllegalArgumentException.class,
                () -> new BellglassHeldNoteData(Optional.empty(), Optional.of(12)));
    }

    @Test
    void invalidNotesAndBlankInstrumentFailClosed() {
        assertThrows(IllegalArgumentException.class, () -> BellglassHeldNoteData.captured(" ", 12));
        assertThrows(IllegalArgumentException.class, () -> BellglassHeldNoteData.captured("HARP", -1));
        assertThrows(IllegalArgumentException.class, () -> BellglassHeldNoteData.captured("HARP", 25));
    }
}
