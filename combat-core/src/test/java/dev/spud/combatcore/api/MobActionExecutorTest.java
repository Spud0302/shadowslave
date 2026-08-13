package dev.spud.combatcore.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MobActionExecutorTest {
    @Test
    void followsSharedActionTiming() {
        CombatActionDefinition action = new CombatActionDefinition("mob_basic", 3, 1, 4, 2.5);
        MobActionExecutor<String> executor = new MobActionExecutor<>();

        assertTrue(executor.start(action, "entity-7", 100));
        assertEquals(CombatPhase.WINDUP, executor.phaseAt(100));
        assertTrue(executor.movementReserved(100));
        assertFalse(executor.consumeActiveWindow(102));
        assertEquals(CombatPhase.ACTIVE, executor.phaseAt(103));
        assertTrue(executor.consumeActiveWindow(103));
        assertFalse(executor.consumeActiveWindow(103));
        assertEquals(CombatPhase.RECOVERY, executor.phaseAt(104));
        assertFalse(executor.actionReserved(108));
    }
}
