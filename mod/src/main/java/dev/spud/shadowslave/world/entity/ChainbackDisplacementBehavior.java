package dev.spud.shadowslave.world.entity;

/**
 * Pure policy for the bounded physical execution of Chainback's authored DISPLACEMENT pressure.
 *
 * <p>The Java creature profile remains authority for the creature identity and legal pressure.
 * This class only turns that already-authored pressure into replaceable Minecraft motion and
 * readability values.</p>
 */
public final class ChainbackDisplacementBehavior {
    public static final double MAX_HORIZONTAL_RANGE = 4.0D;
    public static final double MAX_VERTICAL_DELTA = 1.5D;
    public static final int COOLDOWN_TICKS = 50;
    public static final int TELEGRAPH_TICKS = 12;
    public static final int TELEGRAPH_PULSE_INTERVAL = 3;
    public static final double HORIZONTAL_PULL = 0.55D;
    public static final double LIFT = 0.10D;

    private ChainbackDisplacementBehavior() {
    }

    public static boolean canPull(double horizontalDistanceSquared, double verticalDelta, int cooldownTicksRemaining) {
        if (!Double.isFinite(horizontalDistanceSquared) || !Double.isFinite(verticalDelta)) {
            return false;
        }
        return cooldownTicksRemaining <= 0
                && horizontalDistanceSquared > 0.0D
                && horizontalDistanceSquared <= MAX_HORIZONTAL_RANGE * MAX_HORIZONTAL_RANGE
                && Math.abs(verticalDelta) <= MAX_VERTICAL_DELTA;
    }

    public static boolean shouldTelegraphPulse(int ticksRemaining) {
        return ticksRemaining > 0 && ticksRemaining % TELEGRAPH_PULSE_INTERVAL == 0;
    }

    public static PullVector pullToward(double deltaX, double deltaZ) {
        if (!Double.isFinite(deltaX) || !Double.isFinite(deltaZ)) {
            return PullVector.NONE;
        }
        double lengthSquared = deltaX * deltaX + deltaZ * deltaZ;
        if (lengthSquared <= 1.0E-8D) {
            return PullVector.NONE;
        }
        double inverseLength = 1.0D / Math.sqrt(lengthSquared);
        return new PullVector(
                deltaX * inverseLength * HORIZONTAL_PULL,
                LIFT,
                deltaZ * inverseLength * HORIZONTAL_PULL);
    }

    public record PullVector(double x, double y, double z) {
        public static final PullVector NONE = new PullVector(0.0D, 0.0D, 0.0D);

        public boolean isZero() {
            return x == 0.0D && y == 0.0D && z == 0.0D;
        }
    }
}
