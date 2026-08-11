package dev.spud.shadowslave.world.entity;

/**
 * Pure Minecraft execution policy for the Ash Burrower's authored AMBUSH pressure.
 *
 * <p>The creature catalogue owns the AMBUSH identity. These concrete distances and timings are
 * DESIGN values that create a readable commit/evade/punish exchange without becoming creature
 * canon or progression authority.</p>
 */
public final class AshBurrowerAmbushBehavior {
    static final double START_RANGE = 2.8D;
    static final double CONNECT_RANGE = 3.1D;
    static final double MAX_VERTICAL_DELTA = 1.75D;
    static final int WINDUP_TICKS = 10;
    static final int CONNECTED_RECOVERY_TICKS = 9;
    static final int EVADED_RECOVERY_TICKS = 22;
    static final int COOLDOWN_TICKS = 34;
    static final int TELEGRAPH_INTERVAL_TICKS = 2;

    private AshBurrowerAmbushBehavior() {
    }

    public static boolean canStart(double distanceSquared, double verticalDelta, boolean hasLineOfSight) {
        validate(distanceSquared, verticalDelta);
        return hasLineOfSight
                && distanceSquared <= START_RANGE * START_RANGE
                && verticalDelta <= MAX_VERTICAL_DELTA;
    }

    public static boolean canConnect(double distanceSquared, double verticalDelta, boolean hasLineOfSight) {
        validate(distanceSquared, verticalDelta);
        return hasLineOfSight
                && distanceSquared <= CONNECT_RANGE * CONNECT_RANGE
                && verticalDelta <= MAX_VERTICAL_DELTA;
    }

    public static int recoveryTicks(boolean connected) {
        return connected ? CONNECTED_RECOVERY_TICKS : EVADED_RECOVERY_TICKS;
    }

    public static boolean shouldTelegraph(int remainingWindupTicks) {
        if (remainingWindupTicks < 0 || remainingWindupTicks > WINDUP_TICKS) {
            throw new IllegalArgumentException("remainingWindupTicks outside wind-up boundary");
        }
        return remainingWindupTicks > 0 && remainingWindupTicks % TELEGRAPH_INTERVAL_TICKS == 0;
    }

    private static void validate(double distanceSquared, double verticalDelta) {
        if (!Double.isFinite(distanceSquared) || !Double.isFinite(verticalDelta)
                || distanceSquared < 0.0D || verticalDelta < 0.0D) {
            throw new IllegalArgumentException("ambush geometry must be finite and non-negative");
        }
    }
}
