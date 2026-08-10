package dev.spud.shadowslave.world.entity;

import java.util.UUID;

/**
 * Pure execution policy for the Drowned Listener's authored VIBRATION sense.
 *
 * <p>The Java-owned creature profile establishes VIBRATION and the {@code dry_ground}
 * counterplay tag. Exact Minecraft radii, sampling thresholds and pursuit duration are DESIGN
 * values. This policy is replaceable execution and never owns creature identity, rewards,
 * progression or persistent state.</p>
 */
public final class DrownedListenerVibrationBehavior {
    static final double WATER_DETECTION_RANGE = 14.0D;
    static final double DRY_GROUND_DETECTION_RANGE = 6.0D;
    static final double PROXIMITY_RANGE = 2.5D;
    static final double MIN_SAMPLE_DISPLACEMENT_SQUARED = 0.0225D;
    static final int PURSUIT_TICKS = 80;
    static final double PURSUIT_SPEED = 1.2D;

    private DrownedListenerVibrationBehavior() {
    }

    public static boolean detects(
            double distanceSquared,
            double sampledDisplacementSquared,
            boolean crouching,
            boolean listenerInWater) {
        if (distanceSquared < 0.0D || sampledDisplacementSquared < 0.0D) {
            throw new IllegalArgumentException("Distances cannot be negative");
        }
        if (distanceSquared <= PROXIMITY_RANGE * PROXIMITY_RANGE) {
            return true;
        }
        if (crouching) {
            return false;
        }

        double detectionRange = listenerInWater ? WATER_DETECTION_RANGE : DRY_GROUND_DETECTION_RANGE;
        return distanceSquared <= detectionRange * detectionRange
                && sampledDisplacementSquared >= MIN_SAMPLE_DISPLACEMENT_SQUARED;
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
        return !targetAttackable || distanceSquared > PROXIMITY_RANGE * PROXIMITY_RANGE;
    }
}
