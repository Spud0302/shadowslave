package dev.spud.shadowslave.combat;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CombatPrototypeCommandsTest {
    private static String commandSource() throws Exception {
        return Files.readString(Path.of(
                "src/main/java/dev/spud/shadowslave/combat/CombatPrototypeCommands.java"));
    }

    @Test
    void prototypeFixtureUsesExistingChainbackAndOrdinaryIronSwordOnly() throws Exception {
        String source = commandSource();

        assertTrue(source.contains("NightmareCreatureEntities.CHAINBACK.get().create(level)"));
        assertTrue(source.contains("new ItemStack(Items.IRON_SWORD)"));
        assertTrue(source.contains("chainback.setTarget(player)"));
        assertTrue(source.contains("CHAINBACK_SPAWN_DISTANCE = 3.5D"));
        assertTrue(source.contains("chainback.addTag(PROTOTYPE_CHAINBACK_TAG)"));

        assertFalse(source.contains("SoulService"));
        assertFalse(source.contains("MemoryOwnership"));
        assertFalse(source.contains("AspectService"));
        assertFalse(source.contains("StabilityService"));
        assertFalse(source.contains("net.bettercombat"));
    }

    @Test
    void prototypeFixtureRefusesVanillaOnlyOrExplicitlyDisabledBetterCombatEvidence() throws Exception {
        String source = commandSource();

        assertTrue(source.contains("BETTER_COMBAT_MOD_ID = \"bettercombat\""));
        assertTrue(source.contains("BETTER_COMBAT_DISABLED_TAG = \"bettercombat_disabled\""));
        assertTrue(source.contains("ModList.get().isLoaded(BETTER_COMBAT_MOD_ID)"));
        assertTrue(source.contains("player.getTags().contains(BETTER_COMBAT_DISABLED_TAG)"));
        assertTrue(source.contains("BetterCombatSpikeAdapter.isAttackDisabled(player)"));
        assertTrue(source.contains("Combat prototype setup refused: Better Combat is not loaded"));
        assertTrue(source.contains("this player has Better Combat's persistent bettercombat_disabled tag"));
        assertTrue(source.contains("CombatFlags API reports attacks disabled for this player by another mod/runtime flag"));
        assertTrue(source.contains("CombatFlags confirms attacks are enabled for this player"));

        assertFalse(source.contains("net.bettercombat.api"));
        assertFalse(source.contains("setAttacksDisabled"));
    }

    @Test
    void betterCombatReadOnlyApiStaysBehindSpikeAdapter() throws Exception {
        String adapterSource = Files.readString(Path.of(
                "src/main/java/dev/spud/shadowslave/combat/BetterCombatSpikeAdapter.java"));
        String buildSource = Files.readString(Path.of("build.gradle"));

        assertTrue(adapterSource.contains("import net.bettercombat.api.CombatFlags"));
        assertTrue(adapterSource.contains("return CombatFlags.isAttackDisabled(player)"));
        assertTrue(buildSource.contains("implementation \"maven.modrinth:5sy6g3kz:${bettercombat_version_id}\""));

        assertFalse(adapterSource.contains("setAttacksDisabled"));
        assertFalse(adapterSource.contains("WeaponAttributes"));
        assertFalse(adapterSource.contains("AttackHandler"));
        assertFalse(adapterSource.contains("LivingDamageEvent"));
        assertFalse(adapterSource.contains("SoulService"));
        assertFalse(adapterSource.contains("MemoryOwnership"));
        assertFalse(adapterSource.contains("AspectService"));
        assertFalse(adapterSource.contains("StabilityService"));
    }

    @Test
    void prototypeStatusDistinguishesTelegraphConnectedRecoveryAndEarnedOpening() throws Exception {
        String commandSource = commandSource();
        String chainbackSource = Files.readString(Path.of(
                "src/main/java/dev/spud/shadowslave/world/entity/ChainbackEntity.java"));

        assertTrue(commandSource.contains("chainback.isInDisplacementTelegraph()"));
        assertTrue(commandSource.contains("chainback.isInDisplacementRecovery()"));
        assertTrue(commandSource.contains("chainback.isInEvadedDisplacementOpening()"));
        assertTrue(commandSource.contains("telegraph ? \"TELEGRAPH\" : opening ? \"OPEN\" : recovery ? \"RECOVERY\" : \"NEUTRAL\""));

        assertTrue(chainbackSource.contains("this.displacementRecoveryOpening = !pullConnected"));
        assertTrue(chainbackSource.contains("public boolean isInEvadedDisplacementOpening()"));
        assertFalse(chainbackSource.contains("bettercombat"));
        assertFalse(chainbackSource.contains("StabilityService"));
    }

    @Test
    void earnedOpeningProbeRemainsTransientAndBelowDamageAuthority() throws Exception {
        String source = commandSource();

        assertTrue(source.contains("Map<UUID, HealthProbeBaseline> HEALTH_PROBES"));
        assertTrue(source.contains("player.getMainHandItem().is(Items.IRON_SWORD)"));
        assertTrue(source.contains("new HealthProbeBaseline("));
        assertTrue(source.contains("chainback.getUUID()"));
        assertTrue(source.contains("chainback.getHealth()"));
        assertTrue(source.contains("openingDistance"));
        assertTrue(source.contains("DAMAGE OBSERVED"));
        assertTrue(source.contains("NO DAMAGE OBSERVED"));
        assertTrue(source.contains("EXTRA DAMAGE OBSERVED"));
        assertTrue(source.contains("TARGET LOST • verdict invalid • reset / repeat"));
        assertTrue(source.contains("HEALTH_PROBES.remove(player.getUUID())"));

        assertFalse(source.contains("LivingDamageEvent"));
        assertFalse(source.contains("SavedData"));
        assertFalse(source.contains("CompoundTag"));
        assertFalse(source.contains("setPersistentData"));
        assertFalse(source.contains("net.bettercombat"));
    }

    @Test
    void uninterruptedExchangePresentationStopsPromptingAfterObservedHit() throws Exception {
        String source = commandSource();

        assertTrue(source.contains("TELEGRAPH • "));
        assertTrue(source.contains("RECOVERY • "));
        assertTrue(source.contains("OPEN • %dt • %.1f blocks • commit one iron-sword swing"));
        assertTrue(source.contains("HIT • %.1f damage • recover / reposition"));
        assertTrue(source.contains("HIT CONFIRMED • %.1f damage • reposition"));
        assertTrue(source.contains("MISS • opening closed • reposition"));
        assertTrue(source.contains("EXTRA DAMAGE • %.1f after first observed drop • stop / reject spike"));

        assertFalse(source.contains("StabilityService"));
        assertFalse(source.contains("LivingDamageEvent"));
    }

    @Test
    void repeatedRunsKeepExactlyOneTaggedPrototypeTarget() throws Exception {
        String source = commandSource();

        assertTrue(source.contains("int removed = removeTaggedPrototypeChainbacks(player)"));
        assertTrue(source.contains("Commands.literal(\"reset\")"));
        assertTrue(source.contains("tagged.forEach(ChainbackEntity::discard)"));
        assertTrue(source.contains("exactly one test target"));
    }

    @Test
    void evadedDisplacementCueAndActionReservationRemainCreatureOwned() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/dev/spud/shadowslave/world/entity/ChainbackEntity.java"));

        assertTrue(source.contains("this.emitEvadedRecoveryOpening()"));
        assertTrue(source.contains("ParticleTypes.CLOUD"));
        assertTrue(source.contains("this.goalSelector.addGoal(0, new DisplacementActionReservationGoal(this))"));
        assertTrue(source.contains("return this.displacementTelegraphTicks > 0 || this.displacementRecoveryTicks > 0"));
        assertTrue(source.contains("EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP)"));
        assertTrue(source.contains("this.chainback.setDeltaMovement(0.0D, motion.y, 0.0D)"));

        assertFalse(source.contains("bettercombat"));
        assertFalse(source.contains("MemoryOwnership"));
        assertFalse(source.contains("SoulService"));
        assertFalse(source.contains("StabilityService"));
    }
}
