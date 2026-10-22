package dev.spud.shadowslave.client;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RevealTypewriterTest {
    // Two rows: 4 chars then a 100 ms pause, then 2 chars. 10 ms per char.
    private final RevealTypewriter typewriter = new RevealTypewriter(List.of(4, 2), List.of(100, 0), 10);

    @Test
    void rowsTypeInOrderWithPauses() {
        assertEquals(0, typewriter.visible(0, 0));
        assertEquals(2, typewriter.visible(0, 25));
        assertEquals(4, typewriter.visible(0, 40));
        // Second row waits for the first row (40 ms) plus its pause (100 ms).
        assertEquals(0, typewriter.visible(1, 139));
        assertEquals(1, typewriter.visible(1, 150));
        assertEquals(2, typewriter.visible(1, 500));
    }

    @Test
    void reportsTheRowBeingTyped() {
        assertEquals(0, typewriter.typingRow(15));
        assertEquals(-1, typewriter.typingRow(90));
        assertEquals(1, typewriter.typingRow(145));
        assertEquals(-1, typewriter.typingRow(160));
    }

    @Test
    void finishesWhenTheLastRowIsShown() {
        assertEquals(160, typewriter.endMs());
        assertFalse(typewriter.finished(159));
        assertTrue(typewriter.finished(160));
        assertEquals(6, typewriter.totalVisible(typewriter.endMs()));
    }

    @Test
    void rejectsMismatchedPauses() {
        assertThrows(IllegalArgumentException.class, () -> new RevealTypewriter(List.of(1, 2), List.of(0), 10));
    }
}
