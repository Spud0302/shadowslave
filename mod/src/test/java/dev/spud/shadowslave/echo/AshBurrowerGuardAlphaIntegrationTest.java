package dev.spud.shadowslave.echo;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AshBurrowerGuardAlphaIntegrationTest {
    @Test
    void guardExecutionUsesPersistentAnchorAndOnlyExistingNightmareCreatureThreats() throws Exception {
        String service = Files.readString(Path.of("src/main/java/dev/spud/shadowslave/echo/EchoManifestationService.java"));
        assertTrue(service.contains("EchoOwnershipService.setGuardPoint"));
        assertTrue(service.contains("findGuardThreat(entity, target)"));
        assertTrue(service.contains("new AABB(guardPoint).inflate(GUARD_THREAT_RADIUS)"));
        assertTrue(service.contains("candidate.distanceToSqr(centerX, centerY, centerZ) <= radiusSquared"));
        assertTrue(service.contains("NightmareCreatureEntities.ASH_BURROWER.get()"));
        assertTrue(service.contains("NightmareCreatureEntities.CHAINBACK.get()"));
        assertTrue(service.contains("NightmareCreatureEntities.DROWNED_LISTENER.get()"));
        assertTrue(service.contains("mob.setTarget(selected)"));
        assertFalse(service.contains("getEntitiesOfClass(Monster.class"));
        assertFalse(service.contains("NearestAttackableTargetGoal"));
    }

    @Test
    void ownedEchoCombatDoesNotReplaceMergedSmartBrainLibHostileExecutor() throws Exception {
        String echo = Files.readString(Path.of("src/main/java/dev/spud/shadowslave/echo/AshBurrowerEchoEntity.java"));
        String hostile = Files.readString(Path.of("src/main/java/dev/spud/shadowslave/world/entity/AshBurrowerEntity.java"));
        String registrations = Files.readString(Path.of("src/main/java/dev/spud/shadowslave/world/entity/NightmareCreatureEntities.java"));
        assertTrue(echo.contains("new MeleeAttackGoal(this, 1.1D, true)"));
        assertTrue(echo.contains("DefaultAnimations.ATTACK_STRIKE"));
        assertTrue(registrations.contains("Attributes.ATTACK_DAMAGE, 3.0D"));
        assertTrue(hostile.contains("SmartBrainOwner"));
        assertTrue(hostile.contains("brainProvider()"));
        assertFalse(echo.contains("SmartBrainOwner"));
    }
}
