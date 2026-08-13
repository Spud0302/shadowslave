package dev.spud.combatcore.api;

import java.util.Objects;

/**
 * Minimal timing seam for an AI-controlled actor or consumer-owned special
 * action. Combat Core owns timing/commitment only; callers own target meaning,
 * damage, supernatural effects, AI and presentation.
 */
public final class MobActionExecutor<T> {
    @FunctionalInterface
    public interface Resolution<T> {
        void resolve(T target, CombatActionDefinition definition);
    }

    @FunctionalInterface
    public interface Presentation<T> {
        void onPhaseChanged(T target, CombatActionDefinition definition, CombatPhase phase);
    }

    private final CombatActionState state = new CombatActionState();
    private T target;
    private CombatPhase lastPublishedPhase = CombatPhase.IDLE;

    public boolean start(CombatActionDefinition definition, T nextTarget, long nowTick) {
        Objects.requireNonNull(nextTarget, "nextTarget");
        if (!state.start(definition, nowTick)) return false;
        target = nextTarget;
        lastPublishedPhase = CombatPhase.IDLE;
        return true;
    }

    public boolean consumeActiveWindow(long nowTick) {
        return state.markResolved(nowTick);
    }

    /**
     * Runs a consumer-owned effect at most once during the active window.
     * Returning false means the action was not active or had already resolved.
     */
    public boolean resolveActiveWindow(long nowTick, Resolution<? super T> resolution) {
        Objects.requireNonNull(resolution, "resolution");
        if (!state.markResolved(nowTick)) return false;
        resolution.resolve(target, state.definition());
        return true;
    }

    /**
     * Emits presentation-only phase changes. The callback cannot alter canonical
     * timing state and may be ignored entirely by dedicated servers.
     */
    public boolean publishPhase(long nowTick, Presentation<? super T> presentation) {
        Objects.requireNonNull(presentation, "presentation");
        CombatActionDefinition definition = state.definition();
        CombatPhase phase = state.phaseAt(nowTick);
        if (phase == lastPublishedPhase) return false;
        presentation.onPhaseChanged(target, definition, phase);
        lastPublishedPhase = phase;
        if (phase == CombatPhase.IDLE) target = null;
        return true;
    }

    public boolean cancelDuringWindup(long nowTick) {
        boolean canceled = state.cancelDuringWindup(nowTick);
        if (canceled) {
            target = null;
            lastPublishedPhase = CombatPhase.IDLE;
        }
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
