package dev.spud.shadowslave.memory;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RedThreadCompanionDataTest {
    private static final UUID COMPANION = UUID.fromString("0f3c96e6-cffe-4db5-8a69-54d769ef2e68");

    @Test
    void markedTargetOwnsExactNormalizedUuid() {
        RedThreadCompanionData data = RedThreadCompanionData.marked(COMPANION);

        assertTrue(data.hasCompanion());
        assertEquals(Optional.of(COMPANION), data.companionId());
        assertEquals(Optional.of(COMPANION.toString()), data.companionUuid());
    }

    @Test
    void clearReturnsEmptyTarget() {
        RedThreadCompanionData cleared = RedThreadCompanionData.marked(COMPANION).clear();

        assertFalse(cleared.hasCompanion());
        assertEquals(RedThreadCompanionData.empty(), cleared);
    }

    @Test
    void malformedUuidFailsClosed() {
        assertThrows(IllegalArgumentException.class,
                () -> new RedThreadCompanionData(Optional.of("not-a-uuid")));
        assertThrows(IllegalArgumentException.class,
                () -> new RedThreadCompanionData(Optional.of("   ")));
    }
}
