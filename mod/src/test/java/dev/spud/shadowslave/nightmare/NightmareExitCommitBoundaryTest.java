package dev.spud.shadowslave.nightmare;

import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightmareExitCommitBoundaryTest {
    @Test
    void cancelledReturnWhileStillInNightmareDoesNotCommitExit() {
        assertFalse(NightmareService.returnTeleportCommitted(
                NightmareService.NIGHTMARE_LEVEL,
                Level.OVERWORLD
        ));
    }

    @Test
    void selectedReturnDimensionCommitsExit() {
        assertTrue(NightmareService.returnTeleportCommitted(Level.OVERWORLD, Level.OVERWORLD));
    }

    @Test
    void redirectToDifferentNonNightmareDimensionDoesNotCommitExit() {
        assertFalse(NightmareService.returnTeleportCommitted(Level.END, Level.OVERWORLD));
    }
}
