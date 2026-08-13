package dev.spud.combatcore.api;

import java.util.Objects;

/** Minimal timing seam for an AI-controlled actor. */
public final class MobActionExecutor<T> {
    private final CombatActionState state = new CombatActionState();
    private T target;

    public boolean start(CombatActionDefinition definition, T nextTarget, long nowTick) {
        Objects.requireNonNull(nextTarget, "nextTarget");
        if (!state.start(definition, nowTick)) return false;
        target = nextTarget;
        return true;
    }

    public boolean consumeActiveWindow(long nowTick) {
        return state.markResolved(nowTick);
    }

    public boolean cancelDuringWindup(long nowTick) {
        boolean canceled = state.cancelDuringWindup(nowTick);
        if (canceled) target = null;
        return canceled;
    }

    public CombatPhase phaseAt(long nowTick) {
        return state.phaseAt(nowTick);
    }

    public boolean movementReserved(long nowTick) {
        return state.movementReserved(nowTick);
    }

    public boolean actionReserved(long nowTick) {
        boolean reserved = state.actionReserved(nowTick);
        if (!reserved) target = null;
        return reserved;
    }

    public T target() {
        return target;
    }
}
