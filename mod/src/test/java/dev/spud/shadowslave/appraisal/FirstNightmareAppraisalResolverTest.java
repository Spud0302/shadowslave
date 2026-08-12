package dev.spud.shadowslave.appraisal;

import dev.spud.shadowslave.appraisal.generation.AttributeContentCatalog;
import dev.spud.shadowslave.nightmare.NightmareInstance;
import dev.spud.shadowslave.nightmare.content.DrownedBellScenarioDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirstNightmareAppraisalResolverTest {
    private static final ResourceLocation KINDLE = ResourceLocation.fromNamespaceAndPath(
            "shadowslave", "generation/ability/kindle");
    private static final ResourceLocation COLD_ASH = ResourceLocation.fromNamespaceAndPath(
            "shadowslave", "generation/flaw/cold_ash");
    private static final ResourceLocation COLD_ASH_EFFECT = ResourceLocation.fromNamespaceAndPath(
            "shadowslave", "generation/flaw_effect/cold_ash");

    @Test
    void identicalCompletedNightmareResolvesExactlyTheSameAward() {
        NightmareInstance completed = instance(new UUID(11L, 29L), "last_signal", "last_watchkeeper");

        var first = FirstNightmareAppraisalResolver.resolve(completed, "signal_restored");
        var second = FirstNightmareAppraisalResolver.resolve(completed, "signal_restored");

        assertEquals(first, second);
        assertTrue(first.identity().aspect().instanceId().getPath().startsWith("generated/aspect/"));
        assertTrue(first.identity().flaw().instanceId().getPath().startsWith("generated/flaw/"));
        assertTrue(AttributeContentCatalog.waveOne().profiles().stream()
                .anyMatch(profile -> profile.id().equals(first.attribute().id())));
    }

    @Test
    void terminalResolutionIsPartOfThePermanentGenerationFingerprint() {
        NightmareInstance completed = instance(new UUID(31L, 47L), DrownedBellScenarioDefinition.SCENARIO_ID, "ferry_deckhand");

        var tower = FirstNightmareAppraisalResolver.resolve(completed, "tower_held");
        var evacuation = FirstNightmareAppraisalResolver.resolve(completed, "villagers_evacuated");

        assertNotEquals(tower.evidence().canonicalText(), evacuation.evidence().canonicalText());
        assertNotEquals(tower.identity().generationFingerprint(), evacuation.identity().generationFingerprint());
    }

    @Test
    void completedInstanceSeedsExploreExecutableAspectNamesAndExistingAttributeBacklog() {
        Set<String> aspectNames = new HashSet<>();
        Set<ResourceLocation> attributes = new HashSet<>();

        for (long seed = 1; seed <= 256; seed++) {
            var award = FirstNightmareAppraisalResolver.resolve(
                    instance(new UUID(seed, ~seed), "last_signal", "last_watchkeeper"),
                    "signal_restored"
            );
            aspectNames.add(award.identity().aspect().formalName());
            attributes.add(award.attribute().id());
            assertEquals(KINDLE, award.identity().aspect().abilityId());
            assertEquals(COLD_ASH, award.identity().flaw().primitiveId());
            assertEquals(COLD_ASH_EFFECT, award.identity().flaw().effectId());
        }

        assertTrue(aspectNames.size() >= 6, () -> "expected generated Aspect naming diversity but got " + aspectNames);
        assertTrue(attributes.size() >= 6, () -> "expected Attribute diversity but got " + attributes);
    }

    @Test
    void drownedBellAwardsAlsoRemainInsideExecutableRuntimeBoundary() {
        for (long seed = 257; seed <= 512; seed++) {
            var award = FirstNightmareAppraisalResolver.resolve(
                    instance(new UUID(seed, ~seed), DrownedBellScenarioDefinition.SCENARIO_ID, "cistern_keeper"),
                    "flood_diverted"
            );

            assertEquals(KINDLE, award.identity().aspect().abilityId());
            assertEquals(COLD_ASH, award.identity().flaw().primitiveId());
            assertEquals(COLD_ASH_EFFECT, award.identity().flaw().effectId());
        }
    }

    @Test
    void roleAndScenarioEvidenceFeedsExistingTagsWithoutClaimingCanonFormula() {
        var weights = FirstNightmareAppraisalResolver.evidenceWeights(
                DrownedBellScenarioDefinition.SCENARIO_ID, "cistern_keeper", "flood_diverted");

        assertTrue(weights.getOrDefault("water", 0) >= 8);
        assertTrue(weights.getOrDefault("preservation", 0) >= 3);
        assertTrue(weights.getOrDefault("adaptation", 0) >= 4);
    }

    private static NightmareInstance instance(UUID id, String scenario, String role) {
        return new NightmareInstance(
                id,
                new UUID(91L, 92L),
                0,
                scenario,
                role,
                ResourceLocation.parse("minecraft:overworld"),
                0.5,
                64.0,
                0.5,
                0.0F,
                0.0F,
                BlockPos.ZERO,
                BlockPos.ZERO,
                Optional.empty(),
                1L
        );
    }
}
