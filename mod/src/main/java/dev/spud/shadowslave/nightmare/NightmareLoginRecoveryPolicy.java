package dev.spud.shadowslave.nightmare;

/** Pure precedence policy for reconnect/restart recovery routing. */
final class NightmareLoginRecoveryPolicy {
    private NightmareLoginRecoveryPolicy() {
    }

    static Action select(
            boolean successfulCompletionPresent,
            boolean activeOwnershipPresent,
            boolean playerInNightmare
    ) {
        if (successfulCompletionPresent) {
            return Action.SUCCESSFUL_COMPLETION;
        }
        if (!activeOwnershipPresent) {
            return Action.NONE;
        }
        return playerInNightmare ? Action.ACTIVE_IN_NIGHTMARE : Action.TECHNICAL_RECOVERY;
    }

    enum Action {
        SUCCESSFUL_COMPLETION,
        ACTIVE_IN_NIGHTMARE,
        TECHNICAL_RECOVERY,
        NONE
    }
}
