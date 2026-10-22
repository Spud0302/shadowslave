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
    void abilityKeysListTheLowestRankFirstAndSkipInnateRules() {
        ResourceLocation dormant = id("generation/ability/kindle");
        ResourceLocation awakened = id("generation/ability/carry_the_flame");
        SoulIdentityData identity = identityWithAbilities(
                AspectAbilityData.rankGranted(awakened, SoulRank.AWAKENED, "test"),
                AspectAbilityData.innate(id("kit/innate/ember_warmth"), "test"),
                AspectAbilityData.rankGranted(dormant, SoulRank.DORMANT, "test")
        );

        assertEquals(List.of(dormant, awakened), AbilityService.abilityIds(identity, id -> true));
        assertEquals(List.of(dormant), AbilityService.abilityIds(identity, dormant::equals));
        assertEquals(List.of(), AbilityService.abilityIds(identity, id("kit/innate/ember_warmth")::equals));
    }

    @Test
    void legacySavesStillUseTheirSingleAbility() {
        SoulIdentityData identity = identityWithAbilities(
                AspectAbilityData.legacyUnclassified(KindleAbility.ID, "test")
        );
        assertEquals(List.of(KindleAbility.ID), AbilityService.abilityIds(identity, AbilityRegistry::isActivatable));
    }

    @Test
    void essencePoolGrowsWithRankAndRefillsInAMinute() {
        assertEquals(0, EssenceService.capacityFor(Optional.empty()));
        assertEquals(EssenceService.DORMANT_CAPACITY, EssenceService.capacityFor(Optional.of(SoulRank.DORMANT)));
        assertEquals(EssenceService.AWAKENED_CAPACITY, EssenceService.capacityFor(Optional.of(SoulRank.AWAKENED)));
        assertEquals(100.0F, EssenceService.regenPerTickFor(100) * EssenceService.FULL_REFILL_TICKS, 1.0e-3F);
    }

    @Test
    void essenceRefillsToTheCapAndNeverGoesNegative() {
        EssenceData pool = new EssenceData(90.0F).refill(25.0F, 100);
        assertEquals(100.0F, pool.current());
        assertTrue(pool.covers(100));
        assertFalse(pool.covers(101));
        assertEquals(0.0F, pool.spend(150).current());
        assertEquals(0.0F, new EssenceData(Float.NaN).current());
    }

    @Test
    void essenceSurvivesASaveRoundTrip() {
        EssenceData pool = new EssenceData(42.5F);
        var encoded = EssenceData.CODEC.codec().encodeStart(JsonOps.INSTANCE, pool).getOrThrow();
        assertEquals(pool, EssenceData.CODEC.codec().parse(JsonOps.INSTANCE, encoded).getOrThrow());
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
