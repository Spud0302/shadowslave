package dev.spud.shadowslave.world.entity;

/**
 * Pure timing/contact policy for the Drowned Listener's bounded committed melee strike.
 *
 * <p>All numbers are replaceable DESIGN. The policy exists so the physical executor exposes a
 * readable commitment and an earned recovery window instead of inheriting instantaneous vanilla
 * melee resolution.</p>
 */
final class DrownedListenerStrikeBehavior {
    static final int WINDUP_TICKS = 10;
    static final int CONNECTED_RECOVERY_TICKS = 10;
    static final int EVADED_RECOVERY_TICKS = 18;
    static final double CONNECT_RANGE = 2.75D;
    static final double MAX_VERTICAL_DELTA = 1.6D;

    private DrownedListenerStrikeBehavior() {
    }

    static boolean canConnect(double distanceSquared, double verticalDelta, boolean hasLineOfSight) {
        if (!Double.isFinite(distanceSquared) || !Double.isFinite(verticalDelta)) {
            return false;
        }
        if (distanceSquared < 0.0D || verticalDelta < 0.0D) {
            return false;
        }
        return hasLineOfSight
                && distanceSquared <= CONNECT_RANGE * CONNECT_RANGE
                && verticalDelta <= MAX_VERTICAL_DELTA;
    }

    static int recoveryTicks(boolean connected) {
        return connected ? CONNECTED_RECOVERY_TICKS : EVADED_RECOVERY_TICKS;
    }
}
