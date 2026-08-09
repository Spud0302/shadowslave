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
    void normalTeleportReturnWithoutDimensionChangeDoesNotCommitEntry() {
        assertFalse(NightmareService.entryTeleportCommitted(Level.OVERWORLD));
        assertTrue(NightmareService.shouldRollbackFailedEntry(false));
    }

    @Test
    void playerActuallyInNightmareCommitsEntryAndProtectsOwnershipFromLaterPresentationFailure() {
        assertTrue(NightmareService.entryTeleportCommitted(NightmareService.NIGHTMARE_LEVEL));
        assertFalse(NightmareService.shouldRollbackFailedEntry(true));
    }

    @Test
    void teleportFailureBeforeDimensionSwitchRemainsRollbackEligible() {
        boolean committed = NightmareService.entryCommittedAtFailure(false, Level.OVERWORLD);

        assertFalse(committed);
        assertTrue(NightmareService.shouldRollbackFailedEntry(committed));
    }

    @Test
    void teleportFailureAfterDimensionSwitchRetainsNightmareOwnership() {
        boolean committed = NightmareService.entryCommittedAtFailure(false, NightmareService.NIGHTMARE_LEVEL);

        assertTrue(committed);
        assertFalse(NightmareService.shouldRollbackFailedEntry(committed));
    }

    @Test
    void alreadyObservedCommitIsNotErasedByLaterFailureObservation() {
        assertTrue(NightmareService.entryCommittedAtFailure(true, Level.OVERWORLD));
    }
}
