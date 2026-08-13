package dev.spud.combatcore.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MeleeGeometryTest {
    private static final MeleeGeometry.Vec3 ORIGIN = new MeleeGeometry.Vec3(0.0, 1.0, 0.0);
    private static final MeleeGeometry.Vec3 FORWARD = new MeleeGeometry.Vec3(0.0, 0.0, 1.0);

    @Test
    void acceptsTargetsInsideReachAndArc() {
        assertTrue(MeleeGeometry.withinArc(
                ORIGIN,
                FORWARD,
                new MeleeGeometry.Vec3(0.5, 1.0, 2.0),
                3.0,
                45.0
        ));
    }

    @Test
    void rejectsTargetsOutsideReachOrBehindAttacker() {
        assertFalse(MeleeGeometry.withinArc(
                ORIGIN,
                FORWARD,
                new MeleeGeometry.Vec3(0.0, 1.0, 3.1),
                3.0,
                45.0
        ));
        assertFalse(MeleeGeometry.withinArc(
                ORIGIN,
                FORWARD,
                new MeleeGeometry.Vec3(0.0, 1.0, -1.0),
                3.0,
                90.0
        ));
    }

    @Test
    void rejectsDegenerateOrMalformedGeometry() {
        assertFalse(MeleeGeometry.withinArc(ORIGIN, FORWARD, ORIGIN, 3.0, 45.0));
        assertFalse(MeleeGeometry.withinArc(ORIGIN, new MeleeGeometry.Vec3(0.0, 0.0, 0.0), new MeleeGeometry.Vec3(0.0, 1.0, 1.0), 3.0, 45.0));
        assertFalse(MeleeGeometry.withinArc(ORIGIN, FORWARD, new MeleeGeometry.Vec3(0.0, 1.0, 1.0), Double.NaN, 45.0));
        assertFalse(MeleeGeometry.withinArc(ORIGIN, FORWARD, new MeleeGeometry.Vec3(0.0, 1.0, 1.0), 3.0, 0.0));
        assertThrows(IllegalArgumentException.class, () -> new MeleeGeometry.Vec3(Double.NaN, 0.0, 0.0));
    }
}
