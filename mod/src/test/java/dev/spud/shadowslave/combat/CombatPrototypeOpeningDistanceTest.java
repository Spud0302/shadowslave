package dev.spud.shadowslave.combat;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CombatPrototypeOpeningDistanceTest {
    @Test
    void earnedOpeningReportsDistanceWithoutOwningAttackRangeOrDamage() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/dev/spud/shadowslave/combat/CombatPrototypeCommands.java"));

        assertTrue(source.contains("double currentDistance = Math.sqrt(player.distanceToSqr(chainback))"));
        assertTrue(source.contains("OPEN • %dt • %.1f blocks • commit one iron-sword swing"));
        assertTrue(source.contains("opened at %.1f blocks"));
        assertTrue(source.contains("double openingDistance"));
        assertTrue(source.contains("baseline.openingDistance()"));
        assertTrue(source.contains("distance %.1f blocks | phase %s"));

        assertFalse(source.contains("BETTER_COMBAT_ATTACK_RANGE"));
        assertFalse(source.contains("bettercombat.api"));
        assertFalse(source.contains("LivingDamageEvent"));
        assertFalse(source.contains("StabilityService"));
        assertFalse(source.contains("MemoryOwnership"));
        assertFalse(source.contains("SoulService"));
    }
}
