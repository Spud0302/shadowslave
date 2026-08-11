package dev.spud.shadowslave.world.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AshBurrowerAmbushBehaviorTest {
    @Test
    void closeVisibleTargetCanStartAmbush() {
        assertTrue(AshBurrowerAmbushBehavior.canStart(4.0D, 0.5D, true));
    }

    @Test
    void rangeHeightAndOcclusionCanPreventCommitment() {
        assertFalse(AshBurrowerAmbushBehavior.canStart(9.0D, 0.5D, true));
        assertFalse(AshBurrowerAmbushBehavior.canStart(4.0D, 2.0D, true));
        assertFalse(AshBurrowerAmbushBehavior.canStart(4.0D, 0.5D, false));
    }

    @Test
    void targetCanEscapeDuringWindup() {
        assertTrue(AshBurrowerAmbushBehavior.canStart(4.0D, 0.5D, true));
        assertFalse(AshBurrowerAmbushBehavior.canConnect(16.0D, 0.5D, true));
        assertFalse(AshBurrowerAmbushBehavior.canConnect(4.0D, 0.5D, false));
    }

    @Test
    void evadingAmbushEarnsStrictlyLongerPunishWindow() {
        assertTrue(AshBurrowerAmbushBehavior.recoveryTicks(false)
                > AshBurrowerAmbushBehavior.recoveryTicks(true));
    }

    @Test
    void telegraphRepeatsThroughoutCommitment() {
        int pulses = 0;
        for (int tick = AshBurrowerAmbushBehavior.WINDUP_TICKS; tick > 0; tick--) {
            if (AshBurrowerAmbushBehavior.shouldTelegraph(tick)) {
                pulses++;
            }
        }
        assertTrue(pulses >= 4);
    }

    @Test
    void malformedGeometryAndTimelineFailClosed() {
        assertThrows(IllegalArgumentException.class,
                () -> AshBurrowerAmbushBehavior.canStart(Double.NaN, 0.0D, true));
        assertThrows(IllegalArgumentException.class,
                () -> AshBurrowerAmbushBehavior.canConnect(1.0D, -1.0D, true));
        assertThrows(IllegalArgumentException.class,
                () -> AshBurrowerAmbushBehavior.shouldTelegraph(-1));
        assertThrows(IllegalArgumentException.class,
                () -> AshBurrowerAmbushBehavior.shouldTelegraph(AshBurrowerAmbushBehavior.WINDUP_TICKS + 1));
    }
}
