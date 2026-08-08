package dev.spud.shadowslave.appraisal.generation;

import dev.spud.shadowslave.soul.SoulRank;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpandedIdentityContentCatalogTest {
    @Test
    void waveOneProvidesBroadComposableContent() {
        IdentityPrimitiveCatalog catalog = ExpandedIdentityContentCatalog.waveOne();

        assertEquals(12, catalog.natures().size());
        assertEquals(10, catalog.archetypes().size());
        assertEquals(24, catalog.abilities().size());
        assertEquals(17, catalog.flaws().size());

        for (IdentityPrimitiveCatalog.Nature nature : catalog.natures()) {
            long compatibleAbilities = catalog.abilities().stream()
                    .filter(ability -> ability.supports(nature))
                    .count();
            assertTrue(compatibleAbilities >= 2, () -> nature.id() + " should have at least two ability expressions");
        }
    }

    @Test
    void waveOneKeepsFlawsMechanicallyVariedInsteadOfOneDebuffTemplate() {
        IdentityPrimitiveCatalog catalog = ExpandedIdentityContentCatalog.waveOne();
        Set<String> traitFamilies = new HashSet<>();
        catalog.flaws().forEach(flaw -> traitFamilies.addAll(flaw.traitTags()));

        assertTrue(traitFamilies.containsAll(Set.of(
                "attachment",
                "compulsion",
                "conditional",
                "disclosure",
                "environmental",
                "long_horizon",
                "pain",
                "physical",
                "psychological",
                "resource",
                "sensory",
                "social"
        )));
        assertTrue(traitFamilies.size() >= 16);
    }

    @Test
    void waveOneRetainsTheOriginalLastSignalThemeFamilies() {
        IdentityPrimitiveCatalog catalog = ExpandedIdentityContentCatalog.waveOne();
        Set<ResourceLocation> natureIds = new HashSet<>();
        catalog.natures().forEach(nature -> natureIds.add(nature.id()));

        assertTrue(natureIds.contains(id("generation/nature/ash")));
        assertTrue(natureIds.contains(id("generation/nature/ember")));
        assertTrue(natureIds.contains(id("generation/nature/road")));
        assertTrue(natureIds.contains(id("generation/nature/signal")));
    }

    @Test
    void expandedCatalogueCanGenerateResolvedCandidatesAcrossSeeds() {
        DeterministicIdentityGenerator generator = new DeterministicIdentityGenerator(
                ExpandedIdentityContentCatalog.waveOne()
        );
        GenerationEvidence evidence = new GenerationEvidence(
                "the_last_signal",
                "last_watchkeeper",
                "signal_restored",
                Map.of(
                        "duty", 3,
                        "preservation", 2,
                        "warning", 2,
                        "witness", 1
                )
        );

        Set<ResourceLocation> natures = new HashSet<>();
        Set<ResourceLocation> abilities = new HashSet<>();
        Set<ResourceLocation> flaws = new HashSet<>();
        for (long seed = 0; seed < 128; seed++) {
            GeneratedIdentityCandidate candidate = generator.generate(seed, evidence, SoulRank.DORMANT);
            natures.add(candidate.aspect().natureId());
            abilities.add(candidate.aspect().abilityId());
            flaws.add(candidate.flaw().primitiveId());
            assertFalse(candidate.aspect().formalName().isBlank());
            assertFalse(candidate.flaw().formalName().isBlank());
        }

        assertTrue(natures.size() >= 8, "seed sweep should explore several nature families");
        assertTrue(abilities.size() >= 12, "seed sweep should explore several ability expressions");
        assertTrue(flaws.size() >= 10, "seed sweep should explore several Flaw families");
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("shadowslave", path);
    }
}
