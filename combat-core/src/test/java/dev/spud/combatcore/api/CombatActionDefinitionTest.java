package dev.spud.combatcore.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CombatActionDefinitionTest {
    @Test
    void reportsTotalDuration() {
        CombatActionDefinition action = new CombatActionDefinition("basic_swing", 4, 2, 6, 3.0);
        assertEquals(12, action.totalTicks());
    }

    @Test
    void rejectsMalformedDefinitions() {
        assertThrows(IllegalArgumentException.class, () -> new CombatActionDefinition("", 4, 2, 6, 3.0));
        assertThrows(IllegalArgumentException.class, () -> new CombatActionDefinition("basic_swing", -1, 2, 6, 3.0));
        assertThrows(IllegalArgumentException.class, () -> new CombatActionDefinition("basic_swing", 4, 0, 6, 3.0));
        assertThrows(IllegalArgumentException.class, () -> new CombatActionDefinition("basic_swing", 4, 2, -1, 3.0));
        assertThrows(IllegalArgumentException.class, () -> new CombatActionDefinition("basic_swing", 4, 2, 6, 0.0));
        assertThrows(IllegalArgumentException.class, () -> new CombatActionDefinition("basic_swing", 4, 2, 6, Double.POSITIVE_INFINITY));
    }
}
