package dev.spud.shadowslave.nightmare;

/** Why the one shared Nightmare teardown path was invoked. */
public enum NightmareExitReason {
    SUCCESS,
    TECHNICAL_RECOVERY,
    ADMIN_ABORT,
    CANONICAL_DEATH
}
