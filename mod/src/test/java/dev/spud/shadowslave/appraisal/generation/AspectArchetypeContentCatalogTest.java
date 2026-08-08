package dev.spud.shadowslave.appraisal.generation;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AspectArchetypeContentCatalogTest {
    @Test
    void profilesCoverEveryGeneratedArchetypeExactlyOnce() {
        IdentityPrimitiveCatalog generated = ExpandedIdentityContentCatalog.waveOne();
        Set<ResourceLocation> expected = new HashSet<>();
        generated.archetypes().forEach(archetype -> expected.add(archetype.id()));

        Set<ResourceLocation> actual = new HashSet<>();
        Set<String> roleLabels = new HashSet<>();
        Set<String> decisionHooks = new HashSet<>();
        for (AspectArchetypeContentCatalog.ArchetypeProfile profile : AspectArchetypeContentCatalog.all()) {
            actual.add(profile.archetypeId());
            roleLabels.add(profile.roleLabel());
            decisionHooks.addAll(profile.decisionHooks());

            assertEquals(AspectArchetypeContentCatalog.LoreClassification.DESIGN, profile.loreClassification());
            assertFalse(profile.rolePromise().isBlank());
            assertFalse(profile.powerBoundary().isBlank());
            assertTrue(profile.decisionHooks().size() >= 3);
            assertFalse(profile.presentationTags().isEmpty());
        }

        assertEquals(10, AspectArchetypeContentCatalog.all().size());
        assertEquals(expected, actual);
        assertEquals(10, roleLabels.size(), "each archetype should expose a distinct player-facing role cue");
        assertTrue(decisionHooks.size() >= 30, "wave one should not collapse archetypes into repeated generic hooks");
    }

    @Test
    void everyNatureArchetypePairPreservesResolvedIdentityAndExistingFormalName() {
        IdentityPrimitiveCatalog generated = ExpandedIdentityContentCatalog.waveOne();
        int composed = 0;

        for (IdentityPrimitiveCatalog.Nature nature : generated.natures()) {
            for (IdentityPrimitiveCatalog.Archetype archetype : generated.archetypes()) {
                AspectArchetypeContentCatalog.AspectArchetypePresentation presentation =
                        AspectArchetypeContentCatalog.compose(nature.id(), archetype.id());

                assertEquals(AspectArchetypeContentCatalog.PRESENTATION_VERSION, presentation.presentationVersion());
                assertEquals(AspectArchetypeContentCatalog.LoreClassification.DESIGN, presentation.loreClassification());
                assertEquals(nature.id(), presentation.natureId());
                assertEquals(archetype.id(), presentation.archetypeId());
                assertEquals(archetype.formatName(nature.nameToken()), presentation.formalName());
                assertFalse(presentation.rolePromise().isBlank());
                assertFalse(presentation.powerBoundary().isBlank());
                composed++;
            }
        }

        assertEquals(120, composed, "12 natures x 10 archetypes should remain composable presentation identities");
    }

    @Test
    void evocativeArchetypesKeepExplicitAntiOverclaimBoundaries() {
        String weaver = boundary("weaver");
        assertTrue(weaver.contains("fate manipulation"));
        assertTrue(weaver.contains("memory weaving"));

        String witness = boundary("witness");
        assertTrue(witness.contains("omniscience"));
        assertTrue(witness.contains("prophecy"));

        String voice = boundary("voice");
        assertTrue(voice.contains("mind control"));
        assertTrue(voice.contains("obedience"));

        String wanderer = boundary("wanderer");
        assertTrue(wanderer.contains("teleportation"));
        assertTrue(wanderer.contains("terrain"));

        String pilgrim = boundary("pilgrim");
        assertTrue(pilgrim.contains("religious identity"));
    }

    @Test
    void compositionFailsClosedForUnknownResolvedIds() {
        ResourceLocation validNature = id("generation/nature/ash");
        ResourceLocation validArchetype = id("generation/archetype/bearer");

        assertThrows(
                IllegalArgumentException.class,
                () -> AspectArchetypeContentCatalog.compose(id("generation/nature/unknown"), validArchetype)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> AspectArchetypeContentCatalog.compose(validNature, id("generation/archetype/unknown"))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> AspectArchetypeContentCatalog.requireProfile(id("generation/archetype/unknown"))
        );
    }

    private static String boundary(String archetypePath) {
        return AspectArchetypeContentCatalog.requireProfile(id("generation/archetype/" + archetypePath))
                .powerBoundary()
                .toLowerCase();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("shadowslave", path);
    }
}
