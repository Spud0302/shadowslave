package dev.spud.combatcore.api;

public final class MeleeGeometry {
    private MeleeGeometry() {}

    public record Vec3(double x, double y, double z) {
        public Vec3 {
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
                throw new IllegalArgumentException("vector components must be finite");
            }
        }
    }

    public static boolean withinArc(Vec3 origin, Vec3 facing, Vec3 target, double reach, double halfArcDegrees) {
        if (!Double.isFinite(reach) || reach <= 0.0) return false;
        if (!Double.isFinite(halfArcDegrees) || halfArcDegrees <= 0.0 || halfArcDegrees > 180.0) return false;

        double dx = target.x() - origin.x();
        double dy = target.y() - origin.y();
        double dz = target.z() - origin.z();
        if (dx * dx + dy * dy + dz * dz > reach * reach) return false;

        double facingLength = Math.sqrt(facing.x() * facing.x() + facing.z() * facing.z());
        double targetLength = Math.sqrt(dx * dx + dz * dz);
        if (facingLength == 0.0 || targetLength == 0.0) return false;

        double dot = (facing.x() * dx + facing.z() * dz) / (facingLength * targetLength);
        double clamped = Math.max(-1.0, Math.min(1.0, dot));
        double angle = Math.toDegrees(Math.acos(clamped));
        return angle <= halfArcDegrees;
    }
}
