package dev.spud.shadowslave.appraisal.generation;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
        Set<ResourceLocation> warningSelections = selections(catalog, Map.of("warning", 8, "sound", 6));
        Set<ResourceLocation> pathSelections = selections(catalog, Map.of("path", 8, "guidance", 6));

        assertNotEquals(warningSelections, pathSelections);
        assertTrue(warningSelections.contains(id("generation/attribute/bell_sense"))
                || warningSelections.contains(id("generation/attribute/watchers_mark"))
                || warningSelections.contains(id("generation/attribute/tide_listener")));
        assertTrue(pathSelections.contains(id("generation/attribute/borrowed_compass"))
                || pathSelections.contains(id("generation/attribute/roadwise")));
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

    private static Set<ResourceLocation> selections(AttributeContentCatalog.Catalog catalog, Map<String, Integer> evidence) {
        HashSet<ResourceLocation> selected = new HashSet<>();
        for (long seed = 0; seed < 256; seed++) {
            selected.add(catalog.select(seed, evidence).id());
        }
        return Set.copyOf(selected);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("shadowslave", path);
    }
}
