package dev.spud.shadowslave.world.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DrownedListenerStrikeBehaviorTest {
    @Test
    void contactRequiresRangeVerticalBandAndLineOfSight() {
        double edge = DrownedListenerStrikeBehavior.CONNECT_RANGE * DrownedListenerStrikeBehavior.CONNECT_RANGE;

        assertTrue(DrownedListenerStrikeBehavior.canConnect(
                edge,
                DrownedListenerStrikeBehavior.MAX_VERTICAL_DELTA,
                true));
        assertFalse(DrownedListenerStrikeBehavior.canConnect(Math.nextUp(edge), 0.0D, true));
        assertFalse(DrownedListenerStrikeBehavior.canConnect(
                1.0D,
                Math.nextUp(DrownedListenerStrikeBehavior.MAX_VERTICAL_DELTA),
                true));
        assertFalse(DrownedListenerStrikeBehavior.canConnect(1.0D, 0.0D, false));
    }

    @Test
    void malformedContactEvidenceFailsClosed() {
        assertFalse(DrownedListenerStrikeBehavior.canConnect(Double.NaN, 0.0D, true));
        assertFalse(DrownedListenerStrikeBehavior.canConnect(Double.POSITIVE_INFINITY, 0.0D, true));
        assertFalse(DrownedListenerStrikeBehavior.canConnect(-1.0D, 0.0D, true));
        assertFalse(DrownedListenerStrikeBehavior.canConnect(1.0D, Double.NaN, true));
        assertFalse(DrownedListenerStrikeBehavior.canConnect(1.0D, -0.1D, true));
    }

    @Test
    void cleanEvadeEarnsStrictlyLongerRecoveryThanConnectedStrike() {
        assertTrue(DrownedListenerStrikeBehavior.WINDUP_TICKS > 0);
        assertTrue(DrownedListenerStrikeBehavior.CONNECTED_RECOVERY_TICKS > 0);
        assertTrue(DrownedListenerStrikeBehavior.recoveryTicks(false)
                > DrownedListenerStrikeBehavior.recoveryTicks(true));
    }
}
