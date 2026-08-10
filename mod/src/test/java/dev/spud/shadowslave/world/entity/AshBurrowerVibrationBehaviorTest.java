package dev.spud.shadowslave.world.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AshBurrowerVibrationBehaviorTest {
    @Test
    void movingPlayerInsideDetectionRangeTriggersVibrationSense() {
        assertTrue(AshBurrowerVibrationBehavior.detects(36.0D, 0.09D, false));
    }

    @Test
    void crouchMovementSuppressesOrdinaryVibrationDetection() {
        assertFalse(AshBurrowerVibrationBehavior.detects(36.0D, 0.09D, true));
    }

    @Test
    void stillPlayerDoesNotTriggerAtRange() {
        assertFalse(AshBurrowerVibrationBehavior.detects(36.0D, 0.0D, false));
    }

    @Test
    void immediateProximityStillTriggersEvenWhenCrouching() {
        assertTrue(AshBurrowerVibrationBehavior.detects(4.0D, 0.0D, true));
    }

    @Test
    void movementOutsideDetectionRangeDoesNotTrigger() {
        assertFalse(AshBurrowerVibrationBehavior.detects(169.0D, 4.0D, false));
    }

    @Test
    void malformedNegativeInputsFailClosed() {
        assertThrows(IllegalArgumentException.class,
                () -> AshBurrowerVibrationBehavior.detects(-1.0D, 0.0D, false));
        assertThrows(IllegalArgumentException.class,
                () -> AshBurrowerVibrationBehavior.detects(1.0D, -1.0D, false));
    }
}
