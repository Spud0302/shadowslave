package dev.spud.combatcore.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
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

    @Test
    void resolvesConsumerEffectOnlyOnceInsideActiveWindow() {
        CombatActionDefinition action = new CombatActionDefinition("special", 2, 1, 3, 4.0);
        MobActionExecutor<String> executor = new MobActionExecutor<>();
        AtomicInteger calls = new AtomicInteger();
        AtomicReference<String> resolvedTarget = new AtomicReference<>();
        AtomicReference<CombatActionDefinition> resolvedDefinition = new AtomicReference<>();

        assertTrue(executor.start(action, "target-a", 20));
        assertFalse(executor.resolveActiveWindow(21, (target, definition) -> calls.incrementAndGet()));
        assertTrue(executor.resolveActiveWindow(22, (target, definition) -> {
            calls.incrementAndGet();
            resolvedTarget.set(target);
            resolvedDefinition.set(definition);
        }));
        assertFalse(executor.resolveActiveWindow(22, (target, definition) -> calls.incrementAndGet()));

        assertEquals(1, calls.get());
        assertEquals("target-a", resolvedTarget.get());
        assertEquals(action, resolvedDefinition.get());
    }

    @Test
    void publishesOnlyPhaseTransitionsAndClearsTargetOnIdle() {
        CombatActionDefinition action = new CombatActionDefinition("telegraphed", 2, 1, 2, 3.0);
        MobActionExecutor<String> executor = new MobActionExecutor<>();
        List<CombatPhase> phases = new ArrayList<>();

        assertTrue(executor.start(action, "target-b", 40));
        assertTrue(executor.publishPhase(40, (target, definition, phase) -> phases.add(phase)));
        assertFalse(executor.publishPhase(41, (target, definition, phase) -> phases.add(phase)));
        assertTrue(executor.publishPhase(42, (target, definition, phase) -> phases.add(phase)));
        assertTrue(executor.publishPhase(43, (target, definition, phase) -> phases.add(phase)));
        assertFalse(executor.publishPhase(44, (target, definition, phase) -> phases.add(phase)));
        assertTrue(executor.publishPhase(45, (target, definition, phase) -> phases.add(phase)));

        assertEquals(List.of(
                CombatPhase.WINDUP,
                CombatPhase.ACTIVE,
                CombatPhase.RECOVERY,
                CombatPhase.IDLE), phases);
        assertNull(executor.target());
    }
}
