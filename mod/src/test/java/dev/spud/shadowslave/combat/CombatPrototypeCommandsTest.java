package dev.spud.shadowslave.combat;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CombatPrototypeCommandsTest {
    @Test
    void prototypeFixtureUsesExistingChainbackAndOrdinaryIronSwordOnly() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/dev/spud/shadowslave/combat/CombatPrototypeCommands.java"));

        assertTrue(source.contains("NightmareCreatureEntities.CHAINBACK.get().create(level)"));
        assertTrue(source.contains("new ItemStack(Items.IRON_SWORD)"));
        assertTrue(source.contains("chainback.setTarget(player)"));
        assertTrue(source.contains("CHAINBACK_SPAWN_DISTANCE = 6.0D"));
        assertTrue(source.contains("chainback.addTag(PROTOTYPE_CHAINBACK_TAG)"));

        assertFalse(source.contains("SoulService"));
        assertFalse(source.contains("MemoryOwnership"));
        assertFalse(source.contains("Aspect"));
        assertFalse(source.contains("StabilityService"));
        assertFalse(source.contains("bettercombat.api"));
    }

    @Test
    void prototypeStatusReportsExistingRecoveryAsOpeningWithoutOwningCombatState() throws Exception {
        String commandSource = Files.readString(Path.of(
                "src/main/java/dev/spud/shadowslave/combat/CombatPrototypeCommands.java"));
        String chainbackSource = Files.readString(Path.of(
                "src/main/java/dev/spud/shadowslave/world/entity/ChainbackEntity.java"));

        assertTrue(commandSource.contains("Commands.literal(\"status\")"));
        assertTrue(commandSource.contains("chainback.isInDisplacementRecovery()"));
        assertTrue(commandSource.contains("chainback.displacementRecoveryTicks()"));
        assertTrue(commandSource.contains("chainback.getHealth()"));
        assertTrue(commandSource.contains("opening %s"));

        assertTrue(chainbackSource.contains("public boolean isInDisplacementRecovery()"));
        assertTrue(chainbackSource.contains("public int displacementRecoveryTicks()"));
        assertTrue(chainbackSource.contains("return this.displacementRecoveryTicks;"));
        assertFalse(chainbackSource.contains("stability"));
        assertFalse(chainbackSource.contains("bettercombat"));
    }
}
