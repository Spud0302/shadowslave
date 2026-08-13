package dev.spud.combatcore.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class CombatActionStateTest {
    @Test
    void phasesAreDeterministic() {
        var definition = new CombatActionDefinition("basic_action", 4, 2, 5, 3.0);
        var state = new CombatActionState();

        assertTrue(state.start(definition, 100));
        assertEquals(CombatPhase.WINDUP, state.phaseAt(103));
        assertEquals(CombatPhase.ACTIVE, state.phaseAt(104));
        assertTrue(state.markResolved(104));
        assertFalse(state.markResolved(105));
        assertEquals(CombatPhase.RECOVERY, state.phaseAt(106));
        assertEquals(CombatPhase.IDLE, state.phaseAt(111));
    }

    @Test
    void onlyWindupMayCancel() {
        var definition = new CombatActionDefinition("basic_action", 3, 1, 3, 3.0);
        var state = new CombatActionState();
        assertTrue(state.start(definition, 0));
        assertTrue(state.cancelDuringWindup(1));
        assertEquals(CombatPhase.IDLE, state.phaseAt(1));

        assertTrue(state.start(definition, 10));
        assertFalse(state.cancelDuringWindup(13));
    }
}
