package dev.spud.shadowslave.nightmare;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightmareEntryCommitBoundaryTest {
    @Test
    void preTeleportFailureStillRollsBackEntry() {
        assertTrue(NightmareService.shouldRollbackFailedEntry(false));
    }

    @Test
    void postTeleportFailurePreservesCommittedEntry() {
        assertFalse(NightmareService.shouldRollbackFailedEntry(true));
    }
}
