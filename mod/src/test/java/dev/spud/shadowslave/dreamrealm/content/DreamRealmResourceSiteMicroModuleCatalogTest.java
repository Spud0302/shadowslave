package dev.spud.shadowslave.dreamrealm.content;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class DreamRealmResourceSiteMicroModuleCatalogTest {
    @Test
    void coversEveryAuthoredResourceHookExactlyOnce() {
        var modules = DreamRealmResourceSiteMicroModuleCatalog.waveOne();
        assertEquals(30, modules.size());
        Set<String> anchors = new HashSet<>();
        for (var module : modules) {
            assertTrue(anchors.add(module.regionId() + "/" + module.resourceId()));
            var region = DreamRealmRegionContentCatalog.waveOne().stream()
                    .filter(candidate -> candidate.id().equals(module.regionId()))
                    .findFirst().orElseThrow();
            assertTrue(region.resourceHooks().contains(module.resourceId()));
            assertTrue(module.approachCues().size() >= 2);
            assertTrue(module.decisionOptions().size() >= 3);
            assertFalse(module.negativeBoundary().isBlank());
        }
        int expected = DreamRealmRegionContentCatalog.waveOne().stream()
                .mapToInt(region -> region.resourceHooks().size()).sum();
        assertEquals(expected, anchors.size());
    }

    @Test
    void representsAllInteractionFamilies() {
        var families = DreamRealmResourceSiteMicroModuleCatalog.waveOne().stream()
                .map(DreamRealmResourceSiteMicroModuleCatalog.ResourceSiteModule::family)
                .collect(java.util.stream.Collectors.toSet());
        assertEquals(Set.of(
                DreamRealmResourceSiteMicroModuleCatalog.InteractionFamily.GATHERING,
                DreamRealmResourceSiteMicroModuleCatalog.InteractionFamily.RECOVERY,
                DreamRealmResourceSiteMicroModuleCatalog.InteractionFamily.VERIFICATION), families);
    }

    @Test
    void seedMayOnlyVaryApproachCue() {
        for (var module : DreamRealmResourceSiteMicroModuleCatalog.waveOne()) {
            var baseline = DreamRealmResourceSiteMicroModuleCatalog.compose(0L, module.regionId(), module.resourceId());
            Set<String> cues = new HashSet<>();
            for (long seed = 0; seed < 256; seed++) {
                var resolved = DreamRealmResourceSiteMicroModuleCatalog.compose(seed, module.regionId(), module.resourceId());
                assertEquals(baseline.regionId(), resolved.regionId());
                assertEquals(baseline.resourceId(), resolved.resourceId());
                assertEquals(baseline.moduleId(), resolved.moduleId());
                assertEquals(baseline.family(), resolved.family());
                assertEquals(baseline.decisionPrompt(), resolved.decisionPrompt());
                assertEquals(baseline.decisionOptions(), resolved.decisionOptions());
                assertEquals(baseline.negativeBoundary(), resolved.negativeBoundary());
                assertTrue(module.approachCues().contains(resolved.approachCue()));
                cues.add(resolved.approachCue());
            }
            assertEquals(new HashSet<>(module.approachCues()), cues);
        }
    }

    @Test
    void sameSeedIsDeterministic() {
        var first = DreamRealmResourceSiteMicroModuleCatalog.compose(73L, "glassmere_flats", "resonant_glass");
        var second = DreamRealmResourceSiteMicroModuleCatalog.compose(73L, "glassmere_flats", "resonant_glass");
        assertEquals(first, second);
    }

    @Test
    void crossRegionAndUnknownAnchorsFailClosed() {
        assertThrows(IllegalArgumentException.class,
                () -> DreamRealmResourceSiteMicroModuleCatalog.compose(1L, "ashen_expanse", "rainfruit"));
        assertThrows(IllegalArgumentException.class,
                () -> DreamRealmResourceSiteMicroModuleCatalog.compose(1L, "unknown_region", "rainfruit"));
        assertThrows(IllegalArgumentException.class,
                () -> DreamRealmResourceSiteMicroModuleCatalog.compose(1L, "red_canopy", "unknown_resource"));
    }

    @Test
    void highRiskResourcesPreserveAntiOverclaimBoundaries() {
        String joined = DreamRealmResourceSiteMicroModuleCatalog.waveOne().stream()
                .map(DreamRealmResourceSiteMicroModuleCatalog.ResourceSiteModule::negativeBoundary)
                .reduce("", (left, right) -> left + "\n" + right)
                .toLowerCase();
        assertTrue(joined.contains("memory") || joined.contains("echo"));
        assertTrue(joined.contains("unknown"));
        assertTrue(joined.contains("guarantee") || joined.contains("guaranteed"));
        assertTrue(joined.contains("respawn") || joined.contains("regrowth") || joined.contains("replenishment"));
    }
}
