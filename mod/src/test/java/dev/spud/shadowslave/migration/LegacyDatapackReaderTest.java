package dev.spud.shadowslave.migration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LegacyDatapackReaderTest {
    @Test
    void absentScoreMapsToZeroDeliberately() {
        assertEquals(0, LegacyDatapackReader.normalizeOptionalScore("ss_rank", null));
    }

    @Test
    void explicitZeroFailsClosedInsteadOfMasqueradingAsAbsent() {
        assertThrows(
                IllegalStateException.class,
                () -> LegacyDatapackReader.normalizeOptionalScore("ss_rank", 0)
        );
    }

    @Test
    void positiveScoreIsPreservedExactly() {
        assertEquals(43, LegacyDatapackReader.normalizeOptionalScore("ss_flaw", 43));
    }

    @Test
    void negativeScoreReachesSnapshotValidationRatherThanBecomingAbsent() {
        assertEquals(-1, LegacyDatapackReader.normalizeOptionalScore("ss_rank", -1));
    }
}
