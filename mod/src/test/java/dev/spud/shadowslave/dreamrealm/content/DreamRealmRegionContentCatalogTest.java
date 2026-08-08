package dev.spud.shadowslave.dreamrealm.content;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DreamRealmRegionContentCatalogTest {
    @Test
    void waveOneProvidesDistinctRegionsAndCompleteEnvironmentVocabulary() {
        List<DreamRealmRegionContentCatalog.RegionProfile> regions = DreamRealmRegionContentCatalog.waveOne();

        assertEquals(10, regions.size());
        assertEquals(10, regions.stream().map(DreamRealmRegionContentCatalog.RegionProfile::id).distinct().count());

        EnumSet<DreamRealmRegionContentCatalog.Hazard> hazards = EnumSet.noneOf(DreamRealmRegionContentCatalog.Hazard.class);
        EnumSet<DreamRealmRegionContentCatalog.Traversal> traversal = EnumSet.noneOf(DreamRealmRegionContentCatalog.Traversal.class);
        EnumSet<DreamRealmRegionContentCatalog.Opportunity> opportunities = EnumSet.noneOf(DreamRealmRegionContentCatalog.Opportunity.class);
        regions.forEach(region -> {
            hazards.addAll(region.hazards());
            traversal.addAll(region.traversal());
            opportunities.addAll(region.opportunities());
        });

        assertEquals(EnumSet.allOf(DreamRealmRegionContentCatalog.Hazard.class), hazards);
        assertEquals(EnumSet.allOf(DreamRealmRegionContentCatalog.Traversal.class), traversal);
        assertEquals(EnumSet.allOf(DreamRealmRegionContentCatalog.Opportunity.class), opportunities);
    }

    @Test
    void everyRegionHasNavigationCounterplayAndUsefulReasonsToExplore() {
        for (DreamRealmRegionContentCatalog.RegionProfile region : DreamRealmRegionContentCatalog.waveOne()) {
            assertTrue(region.hazards().size() >= 3, region.id());
            assertTrue(region.traversal().size() >= 2, region.id());
            assertTrue(region.opportunities().size() >= 3, region.id());
            assertTrue(region.landmarkHooks().size() >= 3, region.id());
            assertTrue(region.resourceHooks().size() >= 3, region.id());
            assertFalse(region.arrivalCue().isBlank(), region.id());
            assertFalse(region.travelRule().isBlank(), region.id());
        }
    }

    @Test
    void regionAffinitiesCoverTheWholeFirstCreatureContentWave() {
        Set<String> affinities = new HashSet<>();
        DreamRealmRegionContentCatalog.waveOne().forEach(region -> affinities.addAll(region.creatureAffinityIds()));

        assertTrue(affinities.containsAll(Set.of(
                "ash_burrower",
                "bell_eater",
                "chainback",
                "drowned_listener",
                "glasswing",
                "gutter_choir",
                "hollow_mimic",
                "mire_runner",
                "pale_ferryman",
                "stone_maw",
                "thorn_matron",
                "veil_stalker"
        )));
    }

    @Test
    void catalogueProvidesEnoughLandmarksAndResourcesForRepeatedExploration() {
        Set<String> landmarks = new HashSet<>();
        Set<String> resources = new HashSet<>();
        Set<String> arrivalCues = new HashSet<>();
        Set<String> travelRules = new HashSet<>();

        DreamRealmRegionContentCatalog.waveOne().forEach(region -> {
            landmarks.addAll(region.landmarkHooks());
            resources.addAll(region.resourceHooks());
            arrivalCues.add(region.arrivalCue());
            travelRules.add(region.travelRule());
        });

        assertTrue(landmarks.size() >= 30);
        assertTrue(resources.size() >= 30);
        assertEquals(10, arrivalCues.size());
        assertEquals(10, travelRules.size());
    }
}