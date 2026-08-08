package dev.spud.shadowslave.appraisal.generation;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlawContentCatalogTest {
    @Test
    void waveOneCoversEveryGeneratedFlawExactlyOnce() {
        Set<ResourceLocation> generated = ExpandedIdentityContentCatalog.waveOne().flaws().stream()
                .map(IdentityPrimitiveCatalog.Flaw::id)
                .collect(Collectors.toSet());
        Set<ResourceLocation> presented = FlawContentCatalog.waveOne().stream()
                .map(FlawContentCatalog.FlawProfile::flawId)
                .collect(Collectors.toSet());

        assertEquals(17, FlawContentCatalog.waveOne().size());
        assertEquals(generated, presented);
    }

    @Test
    void everyProfileIsExplicitDesignWithActionablePlayerFacingContent() {
        Set<FlawContentCatalog.FlawFamily> families = new HashSet<>();

        for (FlawContentCatalog.FlawProfile profile : FlawContentCatalog.waveOne()) {
            families.add(profile.family());
            assertEquals(FlawContentCatalog.Classification.DESIGN, profile.classification());
            assertFalse(profile.formalName().isBlank());
            assertTrue(profile.playerSummary().length() >= 40);
            assertTrue(profile.trigger().length() >= 25);
            assertTrue(profile.consequence().length() >= 40);
            assertTrue(profile.copingHooks().size() >= 2);
            assertTrue(profile.copingHooks().stream().allMatch(hook -> hook.length() >= 25));
            assertFalse(profile.presentationTags().isEmpty());
        }

        assertEquals(Set.of(
                FlawContentCatalog.FlawFamily.COMPULSION,
                FlawContentCatalog.FlawFamily.SENSORY,
                FlawContentCatalog.FlawFamily.PHYSICAL,
                FlawContentCatalog.FlawFamily.RESOURCE,
                FlawContentCatalog.FlawFamily.SOCIAL,
                FlawContentCatalog.FlawFamily.EMOTIONAL,
                FlawContentCatalog.FlawFamily.ENVIRONMENTAL,
                FlawContentCatalog.FlawFamily.ATTACHMENT
        ), families);
    }

    @Test
    void compositionNeverChangesAlreadyResolvedFlawIdentity() {
        for (FlawContentCatalog.FlawProfile profile : FlawContentCatalog.waveOne()) {
            for (long seed = 0; seed < 256; seed++) {
                FlawContentCatalog.FlawPresentation presentation = FlawContentCatalog.compose(seed, profile.flawId());
                assertEquals(profile.flawId(), presentation.flawId());
                assertEquals(profile.formalName(), presentation.formalName());
                assertEquals(profile.family(), presentation.family());
                assertEquals(profile.playerSummary(), presentation.playerSummary());
                assertEquals(profile.trigger(), presentation.trigger());
                assertEquals(profile.consequence(), presentation.consequence());
                assertTrue(profile.copingHooks().contains(presentation.copingHook()));
                assertEquals(profile.presentationTags(), presentation.presentationTags());
                assertEquals(FlawContentCatalog.Classification.DESIGN, presentation.classification());
            }
        }
    }

    @Test
    void sameSeedAndIdentityProduceIdenticalPresentation() {
        ResourceLocation flaw = id("generation/flaw/open_flame");

        assertEquals(
                FlawContentCatalog.compose(9321L, flaw),
                FlawContentCatalog.compose(9321L, flaw)
        );
    }

    @Test
    void seededPresentationCanSurfaceEveryAuthoredCopingHook() {
        FlawContentCatalog.FlawProfile profile = FlawContentCatalog.require(id("generation/flaw/witness_burden"));
        Set<String> seen = new HashSet<>();

        for (long seed = 0; seed < 256; seed++) {
            seen.add(FlawContentCatalog.compose(seed, profile.flawId()).copingHook());
        }

        assertEquals(new HashSet<>(profile.copingHooks()), seen);
    }

    @Test
    void unknownFlawIdentityFailsClosed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> FlawContentCatalog.compose(1L, id("generation/flaw/not_authored"))
        );
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("shadowslave", path);
    }
}
