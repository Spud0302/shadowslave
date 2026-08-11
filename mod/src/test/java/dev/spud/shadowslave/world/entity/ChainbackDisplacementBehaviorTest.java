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
        assertFalse(ChainbackDisplacementBehavior.canPull(Double.NaN, 0.0D, 0));
        assertFalse(ChainbackDisplacementBehavior.canPull(4.0D, Double.POSITIVE_INFINITY, 0));
    }

    @Test
    void telegraphPulsesAtBoundedReadableCadence() {
        assertTrue(ChainbackDisplacementBehavior.shouldTelegraphPulse(12));
        assertTrue(ChainbackDisplacementBehavior.shouldTelegraphPulse(9));
        assertTrue(ChainbackDisplacementBehavior.shouldTelegraphPulse(6));
        assertTrue(ChainbackDisplacementBehavior.shouldTelegraphPulse(3));
        assertFalse(ChainbackDisplacementBehavior.shouldTelegraphPulse(11));
        assertFalse(ChainbackDisplacementBehavior.shouldTelegraphPulse(1));
        assertFalse(ChainbackDisplacementBehavior.shouldTelegraphPulse(0));
        assertFalse(ChainbackDisplacementBehavior.shouldTelegraphPulse(-3));
    }

    @Test
    void pullVectorPointsTowardChainbackWithFixedBoundedMagnitude() {
        ChainbackDisplacementBehavior.PullVector pull = ChainbackDisplacementBehavior.pullToward(3.0D, 4.0D);

        assertEquals(0.33D, pull.x(), 1.0E-9D);
        assertEquals(ChainbackDisplacementBehavior.LIFT, pull.y(), 1.0E-9D);
        assertEquals(0.44D, pull.z(), 1.0E-9D);
        assertEquals(ChainbackDisplacementBehavior.HORIZONTAL_PULL,
                Math.sqrt(pull.x() * pull.x() + pull.z() * pull.z()), 1.0E-9D);
    }

    @Test
    void malformedOrZeroDirectionFailsClosed() {
        assertTrue(ChainbackDisplacementBehavior.pullToward(0.0D, 0.0D).isZero());
        assertTrue(ChainbackDisplacementBehavior.pullToward(Double.NaN, 1.0D).isZero());
        assertTrue(ChainbackDisplacementBehavior.pullToward(1.0D, Double.NEGATIVE_INFINITY).isZero());
    }

    @Test
    void tuningConstantsRemainExplicitDesignBounds() {
        assertEquals(4.0D, ChainbackDisplacementBehavior.MAX_HORIZONTAL_RANGE);
        assertEquals(1.5D, ChainbackDisplacementBehavior.MAX_VERTICAL_DELTA);
        assertEquals(50, ChainbackDisplacementBehavior.COOLDOWN_TICKS);
        assertEquals(12, ChainbackDisplacementBehavior.TELEGRAPH_TICKS);
        assertEquals(3, ChainbackDisplacementBehavior.TELEGRAPH_PULSE_INTERVAL);
        assertEquals(0.55D, ChainbackDisplacementBehavior.HORIZONTAL_PULL);
        assertEquals(0.10D, ChainbackDisplacementBehavior.LIFT);
    }
}
