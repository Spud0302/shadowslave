package dev.spud.shadowslave.client.anim;

import dev.spud.combatcore.api.CombatPhase;
import dev.spud.shadowslave.client.anim.ActionPoseCurve.ArmRotation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The pose curve is pure and Minecraft-free precisely so it can be tested here rather
 * than only being judged by eye in a running client.
 */
class ActionPoseCurveTest {
    private static final float EPSILON = 1.0e-4F;

    @Test
    void idleDoesNotOverrideTheArm() {
        assertFalse(ActionPoseCurve.overridesArm(CombatPhase.IDLE));
        assertFalse(ActionPoseCurve.overridesArm(null));
        assertTrue(ActionPoseCurve.overridesArm(CombatPhase.WINDUP));
        assertTrue(ActionPoseCurve.overridesArm(CombatPhase.ACTIVE));
        assertTrue(ActionPoseCurve.overridesArm(CombatPhase.RECOVERY));
    }

    @Test
    void windupRunsFromRestToCocked() {
        assertEquals(ActionPoseCurve.REST_X, ActionPoseCurve.poseFor(CombatPhase.WINDUP, 0, 10).xRot(), EPSILON);
        assertEquals(ActionPoseCurve.COCKED_X, ActionPoseCurve.poseFor(CombatPhase.WINDUP, 10, 10).xRot(), EPSILON);
    }

    @Test
    void windupLiftsEarlyAndThenHolds() {
        // The held pose is the telegraph. If the lift were linear the defender would only
        // see the readable shape on the last tick, which is too late to react to.
        float halfway = ActionPoseCurve.poseFor(CombatPhase.WINDUP, 5, 10).xRot();
        float completed = halfway / ActionPoseCurve.COCKED_X;
        assertTrue(completed > 0.6F, "expected most of the lift by mid-windup, got " + completed);
    }

    @Test
    void recoveryStillMovesLateInTheWindow() {
        // Regression guard. An ease-out curve here returned the arm to rest in the first
        // third of recovery, leaving the player looking idle while still committed, which
        // is the opposite of what recovery is meant to communicate.
        float twoThirds = ActionPoseCurve.poseFor(CombatPhase.RECOVERY, 11, 16).xRot();
        float atEnd = ActionPoseCurve.poseFor(CombatPhase.RECOVERY, 16, 16).xRot();
        assertTrue(
                Math.abs(twoThirds - atEnd) > 0.01F,
                "recovery should still be moving two thirds through, was " + twoThirds
        );
    }

    @Test
    void recoveryRunsFromStrikeBackToRest() {
        assertEquals(ActionPoseCurve.STRIKE_X, ActionPoseCurve.poseFor(CombatPhase.RECOVERY, 0, 16).xRot(), EPSILON);
        assertEquals(ActionPoseCurve.REST_X, ActionPoseCurve.poseFor(CombatPhase.RECOVERY, 16, 16).xRot(), EPSILON);
    }

    @Test
    void phaseBoundariesAreContinuous() {
        // A discontinuity here reads as the arm teleporting mid-swing.
        ArmRotation windupEnd = ActionPoseCurve.poseFor(CombatPhase.WINDUP, 10, 10);
        ArmRotation activeStart = ActionPoseCurve.poseFor(CombatPhase.ACTIVE, 0, 1);
        assertEquals(windupEnd.xRot(), activeStart.xRot(), EPSILON);
        assertEquals(windupEnd.zRot(), activeStart.zRot(), EPSILON);

        ArmRotation activeEnd = ActionPoseCurve.poseFor(CombatPhase.ACTIVE, 1, 1);
        ArmRotation recoveryStart = ActionPoseCurve.poseFor(CombatPhase.RECOVERY, 0, 16);
        assertEquals(activeEnd.xRot(), recoveryStart.xRot(), EPSILON);
        assertEquals(activeEnd.zRot(), recoveryStart.zRot(), EPSILON);
    }

    @Test
    void windupIsMonotonic() {
        float previous = ActionPoseCurve.poseFor(CombatPhase.WINDUP, 0, 10).xRot();
        for (int tick = 1; tick <= 10; tick++) {
            float current = ActionPoseCurve.poseFor(CombatPhase.WINDUP, tick, 10).xRot();
            assertTrue(current <= previous + EPSILON, "windup reversed at tick " + tick);
            previous = current;
        }
    }

    @Test
    void progressIsClampedAtBothEnds() {
        assertEquals(
                ActionPoseCurve.COCKED_X,
                ActionPoseCurve.poseFor(CombatPhase.WINDUP, 999, 10).xRot(),
                EPSILON
        );
        assertEquals(
                ActionPoseCurve.REST_X,
                ActionPoseCurve.poseFor(CombatPhase.WINDUP, -5, 10).xRot(),
                EPSILON
        );
    }

    @Test
    void degenerateDurationIsTreatedAsFinished() {
        // A zero-length phase must not divide by zero; it is simply already over.
        assertEquals(
                ActionPoseCurve.COCKED_X,
                ActionPoseCurve.poseFor(CombatPhase.WINDUP, 0, 0).xRot(),
                EPSILON
        );
    }

    @Test
    void nanTickCountDoesNotPoisonThePose() {
        float pose = ActionPoseCurve.poseFor(CombatPhase.WINDUP, Float.NaN, 10).xRot();
        assertFalse(Float.isNaN(pose), "a NaN tick count must not reach the renderer");
        assertEquals(ActionPoseCurve.REST_X, pose, EPSILON);
    }

    @Test
    void easingCurvesStayInRange() {
        for (int step = -2; step <= 12; step++) {
            float raw = step / 10.0F;
            assertTrue(ActionPoseCurve.easeOutCubic(raw) >= 0.0F);
            assertTrue(ActionPoseCurve.easeOutCubic(raw) <= 1.0F);
            assertTrue(ActionPoseCurve.easeInOutCubic(raw) >= 0.0F);
            assertTrue(ActionPoseCurve.easeInOutCubic(raw) <= 1.0F);
        }
        assertEquals(0.5F, ActionPoseCurve.easeInOutCubic(0.5F), EPSILON);
    }
}
