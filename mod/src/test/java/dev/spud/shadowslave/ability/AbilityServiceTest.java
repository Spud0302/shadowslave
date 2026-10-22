package dev.spud.shadowslave.ability;

import com.mojang.serialization.JsonOps;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.identity.AspectAbilityData;
import dev.spud.shadowslave.soul.identity.AspectAbilitySetData;
import dev.spud.shadowslave.soul.identity.AspectInstanceData;
import dev.spud.shadowslave.soul.identity.FlawInstanceData;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbilityServiceTest {
    private static final ResourceLocation KINDLE = id("preview/ability/kindle");

    @Test
    void authorizesAbilityByMembershipWhenItIsNotFirst() {
        SoulIdentityData identity = identityWithAbilities(
                AspectAbilityData.innate(id("preview/ability/ember_sense"), "test"),
                AspectAbilityData.rankGranted(KINDLE, SoulRank.AWAKENED, "test")
        );

        assertTrue(AbilityService.possessesAspectAbility(identity, KINDLE));
    }

    @Test
    void rejectsMissingAbilityAndMissingAspect() {
        SoulIdentityData identity = identityWithAbilities(
                AspectAbilityData.innate(id("preview/ability/ember_sense"), "test")
        );

        assertFalse(AbilityService.possessesAspectAbility(identity, KINDLE));
        assertFalse(AbilityService.possessesAspectAbility(SoulIdentityData.empty(), KINDLE));
    }

    @Test
    void abilityKeyPicksTheHighestRankActivatableAbilityAndSkipsInnateRules() {
        ResourceLocation dormant = id("generation/ability/kindle");
        ResourceLocation awakened = id("generation/ability/carry_the_flame");
        SoulIdentityData identity = identityWithAbilities(
                AspectAbilityData.innate(id("kit/innate/ember_warmth"), "test"),
                AspectAbilityData.rankGranted(dormant, SoulRank.DORMANT, "test"),
                AspectAbilityData.rankGranted(awakened, SoulRank.AWAKENED, "test")
        );

        assertEquals(Optional.of(awakened), AbilityService.activeAbilityId(identity, id -> true));
        assertEquals(Optional.of(dormant), AbilityService.activeAbilityId(identity, dormant::equals));
        assertEquals(Optional.empty(), AbilityService.activeAbilityId(identity, id("kit/innate/ember_warmth")::equals));
    }

    @Test
    void legacySavesStillUseTheirSingleAbility() {
        SoulIdentityData identity = identityWithAbilities(
                AspectAbilityData.legacyUnclassified(KindleAbility.ID, "test")
        );
        assertEquals(Optional.of(KindleAbility.ID), AbilityService.activeAbilityId(identity, AbilityRegistry::isActivatable));
    }

    @Test
    void cooldownsCountDownAndRoundUpToWholeSeconds() {
        AbilityCooldownData cooldowns = AbilityCooldownData.empty().start(KINDLE, 1000L, 600);
        assertEquals(600L, cooldowns.remainingTicks(KINDLE, 1000L));
        assertEquals(1L, cooldowns.remainingTicks(KINDLE, 1599L));
        assertEquals(0L, cooldowns.remainingTicks(KINDLE, 1600L));
        assertEquals(0L, cooldowns.remainingTicks(id("other"), 1000L));
        assertEquals(1L, AbilityService.secondsCeil(1L));
        assertEquals(30L, AbilityService.secondsCeil(600L));
    }

    @Test
    void cooldownsSurviveASaveRoundTrip() {
        AbilityCooldownData cooldowns = AbilityCooldownData.empty().start(KINDLE, 40L, 600);
        var encoded = AbilityCooldownData.CODEC.codec().encodeStart(JsonOps.INSTANCE, cooldowns).getOrThrow();
        assertEquals(cooldowns, AbilityCooldownData.CODEC.codec().parse(JsonOps.INSTANCE, encoded).getOrThrow());
    }

    private static SoulIdentityData identityWithAbilities(AspectAbilityData... abilities) {
        AspectInstanceData aspect = new AspectInstanceData(
                id("preview/aspect/last_light"),
                "Last Light",
                SoulRank.AWAKENED,
                id("preview/nature/ember_resolve"),
                new AspectAbilitySetData(List.of(abilities)),
                "test"
        );
        FlawInstanceData flaw = new FlawInstanceData(
                id("preview/flaw/test"),
                "Test Flaw",
                id("preview/flaw_effect/test"),
                "test"
        );
        return new SoulIdentityData(Optional.of(aspect), Optional.of(flaw));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("shadowslave", path);
    }
}
