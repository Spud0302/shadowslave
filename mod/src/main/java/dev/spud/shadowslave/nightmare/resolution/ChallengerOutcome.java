package dev.spud.shadowslave.nightmare.resolution;

/** Per-challenger result after one shared scenario reaches a terminal resolution. */
public enum ChallengerOutcome {
    COMPLETED,
    FAILED_DEATH,
    TECHNICAL_RECOVERY,
    ADMIN_ABORT,
    INELIGIBLE_OR_INVALIDATED
}
