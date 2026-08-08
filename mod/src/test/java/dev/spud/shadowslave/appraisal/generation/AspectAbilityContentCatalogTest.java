package dev.spud.shadowslave.appraisal.generation;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AspectAbilityContentCatalogTest {
    @Test
    void waveOneExactlyCoversExistingGeneratedAspectPrimitives() {
        AspectAbilityContentCatalog content = AspectAbilityContentCatalog.waveOne();
        IdentityPrimitiveCatalog generated = ExpandedIdentityContentCatalog.waveOne();

        assertEquals(12, content.natures().size());
        assertEquals(24, content.abilities().size());
        assertEquals(
                generated.natures().stream().map(IdentityPrimitiveCatalog.Nature::id).collect(java.util.stream.Collectors.toSet()),
                content.natures().stream().map(AspectAbilityContentCatalog.NatureProfile::id).collect(java.util.stream.Collectors.toSet())
        );
        assertEquals(
                generated.abilities().stream().map(IdentityPrimitiveCatalog.Ability::id).collect(java.util.stream.Collectors.toSet()),
                content.abilities().stream().map(AspectAbilityContentCatalog.AbilityProfile::id).collect(java.util.stream.Collectors.toSet())
        );
    }

    @Test
    void everyPrimitiveHasSubstantialPlayerFacingContent() {
        AspectAbilityContentCatalog content = AspectAbilityContentCatalog.waveOne();

        for (AspectAbilityContentCatalog.NatureProfile nature : content.natures()) {
            assertFalse(nature.summary().isBlank());
            assertFalse(nature.playerPromise().isBlank());
            assertFalse(nature.presentationTags().isEmpty());
        }

        for (AspectAbilityContentCatalog.AbilityProfile ability : content.abilities()) {
            assertFalse(ability.summary().isBlank());
            assertFalse(ability.boundary().isBlank());
            assertTrue(ability.gameplayHooks().size() >= 2);
            assertTrue(ability.gameplayHooks().stream().noneMatch(String::isBlank));
            assertFalse(ability.presentationTags().isEmpty());
        }
    }

    @Test
    void catalogueUsesSeveralAbilityFamiliesInsteadOfOneCombatTemplate() {
        AspectAbilityContentCatalog content = AspectAbilityContentCatalog.waveOne();
        Set<AspectAbilityContentCatalog.AbilityFamily> families = new HashSet<>();
        content.abilities().forEach(ability -> families.add(ability.family()));

        assertTrue(families.contains(AspectAbilityContentCatalog.AbilityFamily.OFFENSE));
        assertTrue(families.contains(AspectAbilityContentCatalog.AbilityFamily.DEFENSE));
        assertTrue(families.contains(AspectAbilityContentCatalog.AbilityFamily.PERCEPTION));
        assertTrue(families.contains(AspectAbilityContentCatalog.AbilityFamily.MOVEMENT));
        assertTrue(families.contains(AspectAbilityContentCatalog.AbilityFamily.SUPPORT));
        assertTrue(families.contains(AspectAbilityContentCatalog.AbilityFamily.CONCEALMENT));
        assertTrue(families.size() >= 9);
    }

    @Test
    void compositionPreservesAlreadyResolvedIdentityAndMergedDisplayNames() {
        AspectAbilityContentCatalog content = AspectAbilityContentCatalog.waveOne();
        IdentityPrimitiveCatalog generated = ExpandedIdentityContentCatalog.waveOne();

        for (IdentityPrimitiveCatalog.Nature nature : generated.natures()) {
            for (IdentityPrimitiveCatalog.Ability ability : generated.abilities()) {
                if (!ability.supports(nature)) {
                    continue;
                }
                AspectAbilityContentCatalog.AspectAbilityPresentation presentation = content.compose(
                        nature.id(),
                        ability.id()
                );
                assertEquals(AspectAbilityContentCatalog.PRESENTATION_VERSION, presentation.version());
                assertEquals(AspectAbilityContentCatalog.PROVENANCE, presentation.provenance());
                assertEquals(nature.id(), presentation.natureId());
                assertEquals(nature.nameToken(), presentation.natureName());
                assertEquals(ability.id(), presentation.abilityId());
                assertEquals(ability.displayName(), presentation.abilityName());
            }
        }
    }

    @Test
    void incompatibleNatureAbilityPairsFailClosedInsteadOfReinterpretingIdentity() {
        AspectAbilityContentCatalog content = AspectAbilityContentCatalog.waveOne();

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> content.compose(id("generation/nature/ash"), id("generation/ability/kindle"))
        );
        assertTrue(error.getMessage().contains("not compatible"));
    }

    @Test
    void unknownPrimitiveIdsFailClosed() {
        AspectAbilityContentCatalog content = AspectAbilityContentCatalog.waveOne();

        assertThrows(
                IllegalArgumentException.class,
                () -> content.nature(id("generation/nature/not_real"))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> content.ability(id("generation/ability/not_real"))
        );
    }

    @Test
    void authoredBoundariesRejectUniversalPowerClaims() {
        AspectAbilityContentCatalog content = AspectAbilityContentCatalog.waveOne();

        AspectAbilityContentCatalog.AbilityProfile mirror = content.ability(id("generation/ability/mirror_glimpse"));
        AspectAbilityContentCatalog.AbilityProfile hollow = content.ability(id("generation/ability/hollow_step"));
        AspectAbilityContentCatalog.AbilityProfile lowTide = content.ability(id("generation/ability/low_tide"));
        AspectAbilityContentCatalog.AbilityProfile waymark = content.ability(id("generation/ability/waymark"));

        assertTrue(mirror.boundary().contains("not prophecy"));
        assertTrue(hollow.boundary().contains("not true invisibility"));
        assertTrue(lowTide.boundary().contains("does not command arbitrary bodies of water"));
        assertTrue(waymark.boundary().contains("does not discover unknown objectives"));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("shadowslave", path);
    }
}
