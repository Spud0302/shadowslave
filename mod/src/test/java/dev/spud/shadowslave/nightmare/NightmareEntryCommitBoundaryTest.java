package dev.spud.shadowslave.nightmare;

import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightmareEntryCommitBoundaryTest {
    @Test
    void preTeleportFailureStillRollsBackEntry() {
        assertTrue(NightmareService.shouldRollbackFailedEntry(false));
    }

    @Test
    void entryFromNightmareDimensionIsRejectedBeforeCreatingReturnState() {
        assertFalse(NightmareService.entryOriginAllowed(NightmareService.NIGHTMARE_LEVEL));
    }

    @Test
    void entryFromOrdinaryDimensionIsAllowed() {
        assertTrue(NightmareService.entryOriginAllowed(Level.OVERWORLD));
    }

    @Test
    void normalTeleportReturnWithoutDimensionChangeDoesNotCommitEntry() {
        assertFalse(NightmareService.entryTeleportCommitted(Level.OVERWORLD));
        assertTrue(NightmareService.shouldRollbackFailedEntry(false));
    }

    @Test
    void playerActuallyInNightmareCommitsEntry() {
        assertTrue(NightmareService.entryTeleportCommitted(NightmareService.NIGHTMARE_LEVEL));
        assertFalse(NightmareService.shouldRollbackFailedEntry(true));
    }
}
