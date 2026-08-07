package dev.spud.shadowslave.preview;

import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.identity.AspectAbilityData;
import dev.spud.shadowslave.soul.identity.AspectAbilitySetData;
import dev.spud.shadowslave.soul.identity.AspectInstanceData;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PreviewPowerServiceTest {
    private static final ResourceLocation KINDLE = id("preview/ability/kindle");

    @Test
    void authorizesAbilityByMembershipWhenItIsNotFirst() {
        SoulIdentityData identity = identityWithAbilities(
                AspectAbilityData.innate(id("preview/ability/ember_sense"), "test"),
                AspectAbilityData.rankGranted(KINDLE, SoulRank.AWAKENED, "test")
        );

        assertTrue(PreviewPowerService.possessesAspectAbility(identity, KINDLE));
    }

    @Test
    void rejectsMissingAbilityAndMissingAspect() {
        SoulIdentityData identity = identityWithAbilities(
                AspectAbilityData.innate(id("preview/ability/ember_sense"), "test")
        );

        assertFalse(PreviewPowerService.possessesAspectAbility(identity, KINDLE));
        assertFalse(PreviewPowerService.possessesAspectAbility(SoulIdentityData.empty(), KINDLE));
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
        return new SoulIdentityData(Optional.of(aspect), Optional.empty());
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("shadowslave", path);
    }
}
