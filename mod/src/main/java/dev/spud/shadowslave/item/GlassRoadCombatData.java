package dev.spud.shadowslave.item;

import dev.spud.combatcore.api.CombatActionDefinition;
import dev.spud.combatcore.api.CombatPhase;
import dev.spud.combatcore.api.MobActionExecutor;

/**
 * Transient Shadow Slave adapter for Glass Road's clean-edge action.
 *
 * Combat Core owns generic phase timing and the one-resolution guard. Shadow
 * Slave still owns the Memory identity, ownership checks, targeting, damage,
 * messages and all supernatural semantics.
 */
public final class GlassRoadCombatData {
    static final CombatActionDefinition CLEAN_EDGE_ACTION = new CombatActionDefinition(
            "shadowslave:glass_road/clean_edge",
            GlassRoadMemoryItem.WINDUP_TICKS,
            1,
            GlassRoadMemoryItem.RECOVERY_TICKS,
            GlassRoadMemoryItem.REACH
    );

    private static final Object ACTION_CONTEXT = new Object();
    private final MobActionExecutor<Object> executor = new MobActionExecutor<>();

    public static GlassRoadCombatData empty() {
        return new GlassRoadCombatData();
    }

    public boolean start(long now) {
        return executor.start(CLEAN_EDGE_ACTION, ACTION_CONTEXT, now);
    }

    public CombatPhase phaseAt(long now) {
        return executor.phaseAt(now);
    }

    public boolean resolve(long now, Runnable resolution) {
        return executor.resolveActiveWindow(now, (ignored, definition) -> resolution.run());
    }

    public boolean actionReserved(long now) {
        return executor.actionReserved(now);
    }
}
