package dev.spud.shadowslave.world.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChainbackDisplacementBehaviorTest {
    @Test
    void pullRequiresBoundedRangeVerticalBandAndExpiredCooldown() {
        assertTrue(ChainbackDisplacementBehavior.canPull(16.0D, 1.5D, 0));
        assertFalse(ChainbackDisplacementBehavior.canPull(16.0001D, 0.0D, 0));
        assertFalse(ChainbackDisplacementBehavior.canPull(4.0D, 1.5001D, 0));
        assertFalse(ChainbackDisplacementBehavior.canPull(4.0D, 0.0D, 1));
        assertFalse(ChainbackDisplacementBehavior.canPull(0.0D, 0.0D, 0));
    }

    @Test
    void telegraphHasReadablePulseCadence() {
        assertTrue(ChainbackDisplacementBehavior.shouldTelegraphPulse(12));
        assertTrue(ChainbackDisplacementBehavior.shouldTelegraphPulse(9));
        assertTrue(ChainbackDisplacementBehavior.shouldTelegraphPulse(6));
        assertTrue(ChainbackDisplacementBehavior.shouldTelegraphPulse(3));
        assertFalse(ChainbackDisplacementBehavior.shouldTelegraphPulse(11));
    }

    @Test
    void evadeEarnsLongerRecoveryThanConnectedPull() {
        assertEquals(8, ChainbackDisplacementBehavior.recoveryTicks(true));
        assertEquals(18, ChainbackDisplacementBehavior.recoveryTicks(false));
        assertTrue(ChainbackDisplacementBehavior.recoveryTicks(false)
                > ChainbackDisplacementBehavior.recoveryTicks(true));
    }

    @Test
    void pullVectorIsBoundedAndFailsClosed() {
        var pull = ChainbackDisplacementBehavior.pullToward(3.0D, 4.0D);
        assertEquals(0.33D, pull.x(), 1.0E-9D);
        assertEquals(0.44D, pull.z(), 1.0E-9D);
        assertEquals(ChainbackDisplacementBehavior.HORIZONTAL_PULL,
                Math.sqrt(pull.x() * pull.x() + pull.z() * pull.z()), 1.0E-9D);
        assertTrue(ChainbackDisplacementBehavior.pullToward(0.0D, 0.0D).isZero());
        assertTrue(ChainbackDisplacementBehavior.pullToward(Double.NaN, 1.0D).isZero());
    }
}
