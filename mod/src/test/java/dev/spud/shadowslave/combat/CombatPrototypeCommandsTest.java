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
    void prototypeFixtureRefusesToProduceVanillaOnlyDependencyEvidence() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/dev/spud/shadowslave/combat/CombatPrototypeCommands.java"));

        assertTrue(source.contains("BETTER_COMBAT_MOD_ID = \"bettercombat\""));
        assertTrue(source.contains("ModList.get().isLoaded(BETTER_COMBAT_MOD_ID)"));
        assertTrue(source.contains("Combat prototype setup refused: Better Combat is not loaded"));
        assertTrue(source.contains("Combat prototype ready: Better Combat is loaded."));

        assertFalse(source.contains("bettercombat.api"));
        assertFalse(source.contains("net.bettercombat"));
    }

    @Test
    void prototypeStatusReportsExistingRecoveryAndHealthWithoutOwningDamageState() throws Exception {
        String commandSource = Files.readString(Path.of(
                "src/main/java/dev/spud/shadowslave/combat/CombatPrototypeCommands.java"));
        String modSource = Files.readString(Path.of(
                "src/main/java/dev/spud/shadowslave/ShadowSlaveMod.java"));
        String chainbackSource = Files.readString(Path.of(
                "src/main/java/dev/spud/shadowslave/world/entity/ChainbackEntity.java"));

        assertTrue(commandSource.contains("Commands.literal(\"status\")"));
        assertTrue(commandSource.contains("chainback.isInDisplacementRecovery()"));
        assertTrue(commandSource.contains("chainback.displacementRecoveryTicks()"));
        assertTrue(commandSource.contains("chainback.getHealth()"));
        assertTrue(commandSource.contains("probe ARMED"));
        assertTrue(commandSource.contains("health delta %.1f since OPEN baseline"));

        assertFalse(commandSource.contains("LivingDamageEvent"));
        assertFalse(commandSource.contains("onLivingDamage"));
        assertFalse(commandSource.contains("PrototypeTelemetry"));
        assertFalse(modSource.contains("CombatPrototypeCommands::onLivingDamage"));

        assertTrue(chainbackSource.contains("public boolean isInDisplacementRecovery()"));
        assertTrue(chainbackSource.contains("public int displacementRecoveryTicks()"));
        assertTrue(chainbackSource.contains("return this.displacementRecoveryTicks;"));
        assertFalse(chainbackSource.contains("stability"));
        assertFalse(chainbackSource.contains("bettercombat"));
    }

    @Test
    void openingHealthProbeIsTransientBoundToTargetAndConsumedAfterOneVerdict() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/dev/spud/shadowslave/combat/CombatPrototypeCommands.java"));

        assertTrue(source.contains("Map<UUID, HealthProbeBaseline> HEALTH_PROBES"));
        assertTrue(source.contains("new ConcurrentHashMap<>()"));
        assertTrue(source.contains("if (opening)"));
        assertTrue(source.contains("new HealthProbeBaseline(chainback.getUUID(), chainback.getHealth())"));
        assertTrue(source.contains("baseline.chainbackId().equals(chainback.getUUID())"));
        assertTrue(source.contains("baseline.health() - chainback.getHealth()"));
        assertTrue(source.contains("healthDelta > 0.0F ? \"DAMAGE OBSERVED\" : \"NO DAMAGE OBSERVED\""));
        assertTrue(source.contains("probe CONSUMED"));
        assertTrue(source.contains("HEALTH_PROBES.remove(player.getUUID())"));
        assertTrue(source.indexOf("float healthDelta = baseline.health() - chainback.getHealth()")
                < source.indexOf("probeStatus = String.format(\n                    \" | health delta"));

        assertFalse(source.contains("SavedData"));
        assertFalse(source.contains("CompoundTag"));
        assertFalse(source.contains("setPersistentData"));
        assertFalse(source.contains("LivingDamageEvent"));
    }

    @Test
    void repeatedPhysicalRunsCannotAccumulateTaggedPrototypeTargets() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/dev/spud/shadowslave/combat/CombatPrototypeCommands.java"));

        assertTrue(source.contains("int removed = removeTaggedPrototypeChainbacks(player)"));
        assertTrue(source.contains("Commands.literal(\"reset\")"));
        assertTrue(source.contains("tagged.forEach(ChainbackEntity::discard)"));
        assertTrue(source.contains("exactly one test target"));
    }

    @Test
    void evadedDisplacementHasAVisibleOpeningCueWithoutChangingCombatAuthority() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/dev/spud/shadowslave/world/entity/ChainbackEntity.java"));

        assertTrue(source.contains("if (!pullConnected)"));
        assertTrue(source.contains("this.emitEvadedRecoveryOpening()"));
        assertTrue(source.contains("ParticleTypes.CLOUD"));
        assertTrue(source.contains("serverLevel.sendParticles("));

        assertFalse(source.contains("bettercombat"));
        assertFalse(source.contains("MemoryOwnership"));
        assertFalse(source.contains("SoulService"));
        assertFalse(source.contains("StabilityService"));
    }

    @Test
    void displacementWarningAndRecoveryReserveInheritedSpiderCombatGoals() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/dev/spud/shadowslave/world/entity/ChainbackEntity.java"));

        assertTrue(source.contains("this.goalSelector.addGoal(0, new DisplacementActionReservationGoal(this))"));
        assertTrue(source.contains("return this.displacementTelegraphTicks > 0 || this.displacementRecoveryTicks > 0"));
        assertTrue(source.contains("EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP)"));
        assertTrue(source.contains("this.getNavigation().stop()"));
        assertTrue(source.contains("this.chainback.setDeltaMovement(0.0D, motion.y, 0.0D)"));

        assertFalse(source.contains("DisplacementRecoveryGoal"));
        assertFalse(source.contains("bettercombat"));
        assertFalse(source.contains("StabilityService"));
    }
}
