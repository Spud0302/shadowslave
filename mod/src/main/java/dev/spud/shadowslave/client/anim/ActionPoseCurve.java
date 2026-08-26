package dev.spud.shadowslave.client.anim;

import dev.spud.combatcore.api.CombatPhase;

/**
 * Arm rotations for a committed player action, as a pure function of phase and progress.
 *
 * <p>Combat Core owns the phase timing; this only decides what the arm looks like at a
 * given point inside a phase. It deliberately imports nothing from Minecraft so the
 * motion can be reasoned about and unit-tested without a client, and so the class that
 * does touch Minecraft stays a thin adapter over these numbers.
 *
 * <p>The shape is chosen so the action reads: the arm rises quickly and then settles into
 * a held cocked pose for the rest of the wind-up, which is the part a defender needs to
 * see; the active window snaps through; recovery eases back to rest, which is the part
 * that communicates the attacker is briefly committed and cannot act.
 *
 * <p>Rotations are radians in Minecraft's humanoid convention, where a more negative
 * {@code xRot} lifts the arm forward and over the head.
 */
public final class ActionPoseCurve {

    /** Arm rest position, matching the vanilla idle arm. */
    public static final float REST_X = 0.0F;
    public static final float REST_Z = 0.0F;

    /** Held pose at the end of wind-up: arm raised and slightly outward. */
    public static final float COCKED_X = -2.55F;
    public static final float COCKED_Z = -0.35F;

    /** Pose at the end of the active window: swung through, just past vertical. */
    public static final float STRIKE_X = -0.25F;
    public static final float STRIKE_Z = 0.10F;

    private ActionPoseCurve() {
    }

    /** Arm rotation in radians. Yaw is unused today but keeps the record honest for later. */
    public record ArmRotation(float xRot, float yRot, float zRot) {
        public static final ArmRotation REST = new ArmRotation(REST_X, 0.0F, REST_Z);
    }

    /** Whether this phase overrides the vanilla arm at all. */
    public static boolean overridesArm(CombatPhase phase) {
        return phase != null && phase != CombatPhase.IDLE;
    }

    /**
     * Pose for a point inside a phase.
     *
     * @param phase             current combat phase; {@code null} or IDLE yields rest
     * @param ticksIntoPhase    ticks elapsed inside this phase, clamped into range
     * @param phaseDurationTicks total length of this phase; non-positive means "finished"
     */
    public static ArmRotation poseFor(CombatPhase phase, float ticksIntoPhase, int phaseDurationTicks) {
        if (!overridesArm(phase)) {
            return ArmRotation.REST;
        }

        float progress = progress(ticksIntoPhase, phaseDurationTicks);
        return switch (phase) {
            // Rise fast, then hold. The hold is the telegraph.
            case WINDUP -> lerp(REST_X, REST_Z, COCKED_X, COCKED_Z, easeOutCubic(progress));
            // One tick in practice, so this is effectively a snap.
            case ACTIVE -> lerp(COCKED_X, COCKED_Z, STRIKE_X, STRIKE_Z, progress);
            // Occupy the whole recovery. easeOutCubic would finish the return in the
            // first third and leave the player looking idle while still committed,
            // which is the opposite of what recovery needs to communicate.
            case RECOVERY -> lerp(STRIKE_X, STRIKE_Z, REST_X, REST_Z, easeInOutCubic(progress));
            case IDLE -> ArmRotation.REST;
        };
    }

    /** Normalised 0..1 position inside a phase. */
    static float progress(float ticksIntoPhase, int phaseDurationTicks) {
        if (phaseDurationTicks <= 0) {
            return 1.0F;
        }
        return clamp01(ticksIntoPhase / phaseDurationTicks);
    }

    static float clamp01(float value) {
        if (Float.isNaN(value) || value < 0.0F) {
            return 0.0F;
        }
        return Math.min(value, 1.0F);
    }

    /** Fast departure, slow arrival, so the pose settles rather than snapping at the end. */
    static float easeOutCubic(float progress) {
        float inverted = 1.0F - clamp01(progress);
        return 1.0F - (inverted * inverted * inverted);
    }

    /** Slow at both ends, so the return occupies the full window instead of snapping home. */
    static float easeInOutCubic(float progress) {
        float p = clamp01(progress);
        if (p < 0.5F) {
            return 4.0F * p * p * p;
        }
        float shifted = (-2.0F * p) + 2.0F;
        return 1.0F - ((shifted * shifted * shifted) / 2.0F);
    }

    private static ArmRotation lerp(float fromX, float fromZ, float toX, float toZ, float t) {
        return new ArmRotation(
                fromX + ((toX - fromX) * t),
                0.0F,
                fromZ + ((toZ - fromZ) * t)
        );
    }
}
