package dev.spud.shadowslave.appraisal.generation;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttributeContentCatalogTest {
    @Test
    void waveOneProvidesBroadNonStatAttributeContent() {
        AttributeContentCatalog.Catalog catalog = AttributeContentCatalog.waveOne();

        assertEquals(18, catalog.profiles().size());
        assertEquals(Set.of(AttributeContentCatalog.OriginKind.values()),
                catalog.profiles().stream().map(AttributeContentCatalog.AttributeProfile::origin).collect(java.util.stream.Collectors.toSet()));
        assertEquals(Set.of(AttributeContentCatalog.EffectFamily.values()),
                catalog.profiles().stream().map(AttributeContentCatalog.AttributeProfile::effectFamily).collect(java.util.stream.Collectors.toSet()));

        Set<String> forbiddenGenericStats = Set.of("strength", "dexterity", "agility", "intelligence", "vitality");
        for (AttributeContentCatalog.AttributeProfile profile : catalog.profiles()) {
            assertFalse(forbiddenGenericStats.contains(profile.formalName().toLowerCase()));
            assertFalse(profile.gameplayHooks().isEmpty());
        }
    }

    @Test
    void evolutionHooksResolveToStableAuthoredProfiles() {
        AttributeContentCatalog.Catalog catalog = AttributeContentCatalog.waveOne();
        Map<ResourceLocation, ResourceLocation> expected = Map.of(
                id("generation/attribute/ashen_lungs"), id("generation/attribute/unbroken_breath"),
                id("generation/attribute/borrowed_compass"), id("generation/attribute/roadwise"),
                id("generation/attribute/cinder_heart"), id("generation/attribute/living_ember")
        );

        assertEquals(expected.size(), catalog.profiles().stream().filter(profile -> profile.evolvesTo() != null).count());
        expected.forEach((source, target) -> assertEquals(target, catalog.require(source).evolvesTo()));
    }

    @Test
    void deterministicSelectionUsesEvidenceWithoutClaimingACanonFormula() {
        AttributeContentCatalog.Catalog catalog = AttributeContentCatalog.waveOne();
        Map<String, Integer> evidence = Map.of(
                "warning", 4,
                "sound", 3,
                "duty", 2,
                "preservation", 1
        );

        AttributeContentCatalog.AttributeProfile first = catalog.select(42L, evidence);
        AttributeContentCatalog.AttributeProfile second = catalog.select(42L, Map.of(
                "preservation", 1,
                "duty", 2,
                "sound", 3,
                "warning", 4
        ));

        assertEquals(first.id(), second.id(), "map iteration order must not affect deterministic generation");
    }

    @Test
    void seedSweepExploresAuthoredPrimitiveSpace() {
        AttributeContentCatalog.Catalog catalog = AttributeContentCatalog.waveOne();
        Map<String, Integer> evidence = Map.of(
                "path", 2,
                "warning", 2,
                "water", 1,
                "resolve", 1
        );

        Set<ResourceLocation> selected = new HashSet<>();
        for (long seed = 0; seed < 256; seed++) {
            selected.add(catalog.select(seed, evidence).id());
        }

        assertTrue(selected.size() >= 12, "deterministic seed sweep should not collapse into a tiny fixed catalogue");
    }

    @Test
    void evidenceBiasChangesSelectionDistribution() {
        AttributeContentCatalog.Catalog catalog = AttributeContentCatalog.waveOne();
        Map<String, Integer> warningEvidence = Map.of("warning", 8, "sound", 6);
        Map<String, Integer> pathEvidence = Map.of("path", 8, "guidance", 6);
        Set<ResourceLocation> warningProfiles = Set.of(
                id("generation/attribute/bell_sense"),
                id("generation/attribute/watchers_mark"),
                id("generation/attribute/tide_listener")
        );
        Set<ResourceLocation> pathProfiles = Set.of(
                id("generation/attribute/borrowed_compass"),
                id("generation/attribute/roadwise")
        );

        long warningUnderWarningEvidence = countSelections(catalog, warningEvidence, warningProfiles);
        long warningUnderPathEvidence = countSelections(catalog, pathEvidence, warningProfiles);
        long pathUnderPathEvidence = countSelections(catalog, pathEvidence, pathProfiles);
        long pathUnderWarningEvidence = countSelections(catalog, warningEvidence, pathProfiles);

        assertTrue(warningUnderWarningEvidence > warningUnderPathEvidence * 3,
                "warning evidence should materially favor warning-affinity Attributes");
        assertTrue(pathUnderPathEvidence > pathUnderWarningEvidence * 3,
                "path evidence should materially favor path-affinity Attributes");
    }

    @Test
    void invalidEvidenceFailsClosed() {
        AttributeContentCatalog.Catalog catalog = AttributeContentCatalog.waveOne();
        assertThrows(IllegalArgumentException.class, () -> catalog.select(1L, Map.of("warning", -1)));
    }

    @Test
    void obscuredAttributesRemainExplicitInsteadOfImplicitNullState() {
        AttributeContentCatalog.Catalog catalog = AttributeContentCatalog.waveOne();
        Set<ResourceLocation> obscured = new HashSet<>();
        catalog.profiles().stream()
                .filter(profile -> profile.visibility() == AttributeContentCatalog.Visibility.OBSCURED)
                .forEach(profile -> obscured.add(profile.id()));

        assertEquals(Set.of(
                id("generation/attribute/hollow_presence"),
                id("generation/attribute/red_thread"),
                id("generation/attribute/veil_touched")
        ), obscured);
    }

    private static long countSelections(
            AttributeContentCatalog.Catalog catalog,
            Map<String, Integer> evidence,
            Set<ResourceLocation> targets
    ) {
        long count = 0L;
        for (long seed = 0; seed < 4096; seed++) {
            if (targets.contains(catalog.select(seed, evidence).id())) {
                count++;
            }
        }
        return count;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("shadowslave", path);
    }
}
