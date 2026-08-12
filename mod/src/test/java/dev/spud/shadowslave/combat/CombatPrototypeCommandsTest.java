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
        assertTrue(source.contains("CHAINBACK_SPAWN_DISTANCE = 3.5D"));
        assertTrue(source.contains("Chainback starts inside displacement range"));
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
    void prototypeStatusDistinguishesEarnedOpeningFromConnectedRecovery() throws Exception {
        String commandSource = Files.readString(Path.of(
                "src/main/java/dev/spud/shadowslave/combat/CombatPrototypeCommands.java"));
        String chainbackSource = Files.readString(Path.of(
                "src/main/java/dev/spud/shadowslave/world/entity/ChainbackEntity.java"));

        assertTrue(commandSource.contains("chainback.isInDisplacementTelegraph()"));
        assertTrue(commandSource.contains("chainback.isInDisplacementRecovery()"));
        assertTrue(commandSource.contains("chainback.isInEvadedDisplacementOpening()"));
        assertTrue(commandSource.contains("telegraph ? \"TELEGRAPH\" : opening ? \"OPEN\" : recovery ? \"RECOVERY\" : \"NEUTRAL\""));

        assertTrue(chainbackSource.contains("private boolean displacementRecoveryOpening"));
        assertTrue(chainbackSource.contains("this.displacementRecoveryOpening = !pullConnected"));
        assertTrue(chainbackSource.contains("public boolean isInEvadedDisplacementOpening()"));
        assertTrue(chainbackSource.contains("return this.displacementRecoveryTicks > 0 && this.displacementRecoveryOpening"));
        assertTrue(chainbackSource.contains("this.displacementRecoveryOpening = false"));

        assertFalse(chainbackSource.contains("StabilityService"));
        assertFalse(chainbackSource.contains("bettercombat"));
    }

    @Test
    void earnedOpeningProbeArmsAndResolvesAutomaticallyWithoutDamageEventAuthority() throws Exception {
        String commandSource = Files.readString(Path.of(
                "src/main/java/dev/spud/shadowslave/combat/CombatPrototypeCommands.java"));
        String modSource = Files.readString(Path.of(
                "src/main/java/dev/spud/shadowslave/ShadowSlaveMod.java"));

        assertTrue(commandSource.contains("public static void onPlayerTick(PlayerTickEvent.Post event)"));
        assertTrue(commandSource.contains("chainback.isInEvadedDisplacementOpening()"));
        assertTrue(commandSource.contains("player.getMainHandItem().is(Items.IRON_SWORD)"));
        assertTrue(commandSource.contains("new HealthProbeBaseline(chainback.getUUID(), chainback.getHealth())"));
        assertTrue(commandSource.contains("Combat prototype OPEN: clean evade confirmed and health probe armed."));
        assertTrue(commandSource.contains("if (!opening)"));
        assertTrue(commandSource.contains("String verdict = healthDelta > 0.0F ? \"DAMAGE OBSERVED\" : \"NO DAMAGE OBSERVED\""));
        assertTrue(commandSource.contains("Combat prototype OPEN closed: health delta %.1f | verdict %s | probe CONSUMED."));
        assertTrue(commandSource.contains("the final verdict resolves when OPEN closes"));
        assertTrue(modSource.contains("NeoForge.EVENT_BUS.addListener(CombatPrototypeCommands::onPlayerTick)"));

        assertFalse(commandSource.contains("LivingDamageEvent"));
        assertFalse(commandSource.contains("onLivingDamage"));
        assertFalse(modSource.contains("CombatPrototypeCommands::onLivingDamage"));
    }

    @Test
    void statusCannotConsumeAndRearmTheProbeInsideTheSameOpening() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/dev/spud/shadowslave/combat/CombatPrototypeCommands.java"));

        assertTrue(source.contains("if (opening)"));
        assertTrue(source.contains("health delta %.1f observed during OPEN | final verdict pending until OPEN closes | probe remains ARMED"));
        assertTrue(source.contains("if (!opening)"));
        assertTrue(source.contains("Combat prototype OPEN closed: health delta %.1f | verdict %s | probe CONSUMED."));

        assertFalse(source.contains("LivingDamageEvent"));
        assertFalse(source.contains("bettercombat.api"));
    }

    @Test
    void telegraphConnectedRecoveryAndEarnedOpeningRemainReadableWithoutInterruptingThePhysicalExchange() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/dev/spud/shadowslave/combat/CombatPrototypeCommands.java"));

        assertTrue(source.contains("if (chainback.isInDisplacementTelegraph())"));
        assertTrue(source.contains("\"TELEGRAPH • \" + chainback.displacementTelegraphTicks() + \"t • break range / line of sight\""));
        assertTrue(source.contains("boolean recovery = chainback.isInDisplacementRecovery()"));
        assertTrue(source.contains("if (recovery)"));
        assertTrue(source.contains("\"RECOVERY • \" + chainback.displacementRecoveryTicks() + \"t • Chainback connected • reposition\""));
        assertTrue(source.contains("\"OPEN • \" + chainback.displacementRecoveryTicks() + \"t • commit one iron-sword swing\""));
        assertTrue(source.contains("TELEGRAPH, connected RECOVERY, and earned OPEN stay visible in the action bar"));

        assertFalse(source.contains("StabilityService"));
        assertFalse(source.contains("bettercombat.api"));
        assertFalse(source.contains("LivingDamageEvent"));
    }

    @Test
    void observedOpeningHitStopsPromptingAnotherAttack() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/dev/spud/shadowslave/combat/CombatPrototypeCommands.java"));

        assertTrue(source.contains("float healthDelta = baseline.health() - chainback.getHealth()"));
        assertTrue(source.contains("if (healthDelta > 0.0F)"));
        assertTrue(source.contains("HIT • %.1f damage • recover / reposition"));
        assertTrue(source.contains("after player damage is observed the prompt switches to recovery"));
        assertTrue(source.indexOf("HIT • %.1f damage • recover / reposition")
                < source.indexOf("OPEN • \" + chainback.displacementRecoveryTicks() + \"t • commit one iron-sword swing"));

        assertFalse(source.contains("LivingDamageEvent"));
        assertFalse(source.contains("bettercombat.api"));
    }

    @Test
    void openingClosureSurfacesImmediateHitOrMissHudVerdict() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/dev/spud/shadowslave/combat/CombatPrototypeCommands.java"));

        assertTrue(source.contains("HIT CONFIRMED • %.1f damage • reposition"));
        assertTrue(source.contains("MISS • opening closed • reposition"));
        assertTrue(source.contains("player.displayClientMessage(Component.literal(healthDelta > 0.0F"));
        assertTrue(source.contains("withStyle(verdictColor), true"));

        assertFalse(source.contains("LivingDamageEvent"));
        assertFalse(source.contains("bettercombat.api"));
        assertFalse(source.contains("StabilityService"));
    }

    @Test
    void openingHealthProbeIsTransientTargetBoundAndOneShot() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/dev/spud/shadowslave/combat/CombatPrototypeCommands.java"));

        assertTrue(source.contains("Map<UUID, HealthProbeBaseline> HEALTH_PROBES"));
        assertTrue(source.contains("new ConcurrentHashMap<>()"));
        assertTrue(source.contains("baseline.chainbackId().equals(chainback.getUUID())"));
        assertTrue(source.contains("baseline.health() - chainback.getHealth()"));
        assertTrue(source.contains("verdict %s | probe CONSUMED"));
        assertTrue(source.contains("HEALTH_PROBES.remove(player.getUUID())"));

        assertFalse(source.contains("SavedData"));
        assertFalse(source.contains("CompoundTag"));
        assertFalse(source.contains("setPersistentData"));
        assertFalse(source.contains("LivingDamageEvent"));
    }

    @Test
    void healthProbeFlagsASecondObservedDamageDropWithoutBecomingDamageAuthority() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/dev/spud/shadowslave/combat/CombatPrototypeCommands.java"));

        assertTrue(source.contains("Float firstObservedHealth"));
        assertTrue(source.contains("observeFirstHealthDrop(chainback.getHealth())"));
        assertTrue(source.contains("extraDamageSinceFirstObservation(chainback.getHealth())"));
        assertTrue(source.contains("verdict = \"EXTRA DAMAGE OBSERVED\""));
        assertTrue(source.contains("EXTRA DAMAGE • %.1f after first observed drop • stop / reject spike"));
        assertTrue(source.contains("Math.max(0.0F, firstObservedHealth - currentHealth)"));

        assertFalse(source.contains("LivingDamageEvent"));
        assertFalse(source.contains("onLivingDamage"));
        assertFalse(source.contains("bettercombat.api"));
        assertFalse(source.contains("StabilityService"));
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
