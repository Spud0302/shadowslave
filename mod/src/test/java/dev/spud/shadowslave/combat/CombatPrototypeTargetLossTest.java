package dev.spud.shadowslave.combat;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CombatPrototypeTargetLossTest {
    @Test
    void armedProbeInvalidatesRunIfTaggedChainbackDisappears() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/dev/spud/shadowslave/combat/CombatPrototypeCommands.java"));

        assertTrue(source.contains("if (chainback == null)"));
        assertTrue(source.contains("if (baseline != null)"));
        assertTrue(source.contains("Combat prototype verdict INVALID: the tagged Chainback disappeared while the OPEN health probe was armed."));
        assertTrue(source.contains("TARGET LOST • verdict invalid • reset / repeat"));
        assertTrue(source.contains("HEALTH_PROBES.remove(player.getUUID())"));

        assertFalse(source.contains("LivingDamageEvent"));
        assertFalse(source.contains("bettercombat.api"));
        assertFalse(source.contains("StabilityService"));
        assertFalse(source.contains("SavedData"));
    }
}
