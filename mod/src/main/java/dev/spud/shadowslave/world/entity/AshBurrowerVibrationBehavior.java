package dev.spud.shadowslave.world.entity;

import java.util.UUID;

/**
 * Pure execution policy for the placeholder Ash Burrower vibration sense.
 *
 * <p>The authored creature profile owns the VIBRATION/AMBUSH identity. These concrete Minecraft
 * thresholds are DESIGN values so the physical executor can expose counterplay without becoming
 * canonical creature state.</p>
 */
public final class AshBurrowerVibrationBehavior {
    static final double DETECTION_RANGE = 12.0D;
    static final double PROXIMITY_RANGE = 2.75D;
    static final double MIN_SAMPLE_DISPLACEMENT_SQUARED = 0.0225D;
    static final int PURSUIT_TICKS = 60;
    static final double PURSUIT_SPEED = 1.35D;

    private AshBurrowerVibrationBehavior() {
    }

    public static boolean detects(double distanceSquared, double sampledDisplacementSquared, boolean crouching) {
        if (distanceSquared < 0.0D || sampledDisplacementSquared < 0.0D) {
            throw new IllegalArgumentException("Distances cannot be negative");
        }
        if (distanceSquared <= PROXIMITY_RANGE * PROXIMITY_RANGE) {
            return true;
        }
        if (distanceSquared > DETECTION_RANGE * DETECTION_RANGE || crouching) {
            return false;
        }
        return sampledDisplacementSquared >= MIN_SAMPLE_DISPLACEMENT_SQUARED;
    }

    public static boolean shouldReleaseVibrationTarget(
            UUID vibrationTargetId,
            UUID currentTargetId,
            int pursuitTicks,
            boolean targetAttackable,
            double distanceSquared) {
        if (pursuitTicks > 0 || vibrationTargetId == null || !vibrationTargetId.equals(currentTargetId)) {
            return false;
        }
        double proximitySquared = PROXIMITY_RANGE * PROXIMITY_RANGE;
        return !targetAttackable || distanceSquared > proximitySquared;
    }
}
