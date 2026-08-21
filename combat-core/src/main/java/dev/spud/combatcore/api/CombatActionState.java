package dev.spud.combatcore.api;

import java.util.Objects;

public final class CombatActionState {
    private CombatActionDefinition definition;
    private long startedTick;
    private boolean resolved;

    public boolean start(CombatActionDefinition next, long nowTick) {
        Objects.requireNonNull(next, "next");
        if (phaseAt(nowTick) != CombatPhase.IDLE) return false;
        definition = next;
        startedTick = nowTick;
        resolved = false;
        return true;
    }

    public CombatPhase phaseAt(long nowTick) {
        if (definition == null) return CombatPhase.IDLE;
        long elapsed = nowTick - startedTick;
        if (elapsed < 0) return CombatPhase.IDLE;
        if (elapsed < definition.windupTicks()) return CombatPhase.WINDUP;
        if (elapsed < definition.windupTicks() + definition.activeTicks()) return CombatPhase.ACTIVE;
        if (elapsed < definition.totalTicks()) return CombatPhase.RECOVERY;
        definition = null;
        resolved = false;
        return CombatPhase.IDLE;
    }

    public boolean cancelDuringWindup(long nowTick) {
        if (phaseAt(nowTick) != CombatPhase.WINDUP) return false;
        definition = null;
        resolved = false;
        return true;
    }

    public boolean movementReserved(long nowTick) {
        return phaseAt(nowTick) != CombatPhase.IDLE;
    }

    public boolean actionReserved(long nowTick) {
        return phaseAt(nowTick) != CombatPhase.IDLE;
    }

    public boolean markResolved(long nowTick) {
        if (phaseAt(nowTick) != CombatPhase.ACTIVE || resolved) return false;
        resolved = true;
        return true;
    }

    public CombatActionDefinition definition() {
        return definition;
    }
}
