package dev.spud.shadowslave.world.entity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DrownedListenerVibrationBehaviorTest {
    @Test
    void movingPlayerIsDetectedFartherWhileListenerIsInWater() {
        assertTrue(DrownedListenerVibrationBehavior.detects(100.0D, 0.09D, false, true));
        assertFalse(DrownedListenerVibrationBehavior.detects(100.0D, 0.09D, false, false));
    }

    @Test
    void dryGroundStillAllowsBoundedNearbyDetection() {
        assertTrue(DrownedListenerVibrationBehavior.detects(25.0D, 0.09D, false, false));
        assertFalse(DrownedListenerVibrationBehavior.detects(49.0D, 0.09D, false, false));
    }

    @Test
    void crouchingSuppressesRangedMotionDetection() {
        assertFalse(DrownedListenerVibrationBehavior.detects(25.0D, 0.09D, true, true));
    }

    @Test
    void immediateProximityStillTriggersWhileCrouching() {
        assertTrue(DrownedListenerVibrationBehavior.detects(4.0D, 0.0D, true, false));
    }

    @Test
    void stillPlayerDoesNotTriggerAtRange() {
        assertFalse(DrownedListenerVibrationBehavior.detects(25.0D, 0.0D, false, true));
    }

    @Test
    void cleanupReleasesOnlyExpiredVibrationOwnedTarget() {
        UUID vibrationTarget = UUID.randomUUID();
        assertTrue(DrownedListenerVibrationBehavior.shouldReleaseVibrationTarget(
                vibrationTarget, vibrationTarget, 0, true, 25.0D));
        assertFalse(DrownedListenerVibrationBehavior.shouldReleaseVibrationTarget(
                vibrationTarget, UUID.randomUUID(), 0, true, 25.0D));
        assertFalse(DrownedListenerVibrationBehavior.shouldReleaseVibrationTarget(
                vibrationTarget, vibrationTarget, 20, true, 25.0D));
    }

    @Test
    void malformedNegativeInputsFailClosed() {
        assertThrows(IllegalArgumentException.class,
                () -> DrownedListenerVibrationBehavior.detects(-1.0D, 0.0D, false, true));
        assertThrows(IllegalArgumentException.class,
                () -> DrownedListenerVibrationBehavior.detects(1.0D, -1.0D, false, true));
    }
}
