package dev.spud.shadowslave.dreamrealm.content;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DreamRealmExpeditionBriefCatalogTest {
    @Test
    void waveOneProvidesTwoBriefsForEveryMergedRegion() {
        List<DreamRealmExpeditionBriefCatalog.ExpeditionBrief> briefs = DreamRealmExpeditionBriefCatalog.waveOne();
        assertEquals(20, briefs.size());
        assertEquals(20, briefs.stream().map(DreamRealmExpeditionBriefCatalog.ExpeditionBrief::id).distinct().count());

        for (DreamRealmRegionContentCatalog.RegionProfile region : DreamRealmRegionContentCatalog.waveOne()) {
            assertEquals(2, briefs.stream().filter(brief -> brief.regionId().equals(region.id())).count(), region.id());
        }
    }

    @Test
    void everyBriefUsesOnlySourceRegionPrimitives() {
        for (DreamRealmExpeditionBriefCatalog.ExpeditionBrief brief : DreamRealmExpeditionBriefCatalog.waveOne()) {
            DreamRealmRegionContentCatalog.RegionProfile region = DreamRealmRegionContentCatalog.waveOne().stream()
                    .filter(candidate -> candidate.id().equals(brief.regionId()))
                    .findFirst().orElseThrow();
            assertTrue(region.hazards().contains(brief.hazard()), brief.id());
            assertTrue(region.traversal().contains(brief.traversal()), brief.id());
            assertTrue(region.opportunities().contains(brief.opportunity()), brief.id());
            assertTrue(region.landmarkHooks().contains(brief.landmarkId()), brief.id());
            assertTrue(region.creatureAffinityIds().contains(brief.creatureAffinityId()), brief.id());
            assertTrue(brief.preparationChecks().size() >= 2, brief.id());
            assertTrue(brief.departureQuestions().size() >= 2, brief.id());
            assertTrue(brief.presentationCues().size() >= 2, brief.id());
            assertFalse(brief.antiOverclaimBoundary().isBlank(), brief.id());
        }
    }

    @Test
    void waveOneCoversAllPreparationFamilies() {
        EnumSet<DreamRealmExpeditionBriefCatalog.BriefFamily> families = EnumSet.noneOf(DreamRealmExpeditionBriefCatalog.BriefFamily.class);
        DreamRealmExpeditionBriefCatalog.waveOne().forEach(brief -> families.add(brief.family()));
        assertEquals(EnumSet.allOf(DreamRealmExpeditionBriefCatalog.BriefFamily.class), families);
    }

    @Test
    void deterministicSelectionPreservesCallerSuppliedRegionAndAuthoredMechanics() {
        for (DreamRealmRegionContentCatalog.RegionProfile region : DreamRealmRegionContentCatalog.waveOne()) {
            Set<String> reachedBriefs = new HashSet<>();
            Set<String> reachedBriefCues = new HashSet<>();
            for (long seed = 0; seed < 2048; seed++) {
                DreamRealmExpeditionBriefCatalog.PreparedBrief first = DreamRealmExpeditionBriefCatalog.compose(seed, region.id());
                DreamRealmExpeditionBriefCatalog.PreparedBrief second = DreamRealmExpeditionBriefCatalog.compose(seed, region.id());
                assertEquals(first, second);
                assertEquals(region.id(), first.regionId());
                assertEquals(DreamRealmExpeditionBriefCatalog.GENERATOR_VERSION, first.generatorVersion());
                assertTrue(region.hazards().contains(first.hazard()));
                assertTrue(region.traversal().contains(first.traversal()));
                assertTrue(region.opportunities().contains(first.opportunity()));
                assertTrue(region.landmarkHooks().contains(first.landmarkId()));
                assertTrue(region.creatureAffinityIds().contains(first.creatureAffinityId()));
                reachedBriefs.add(first.briefId());
                reachedBriefCues.add(first.briefId() + "|" + first.presentationCue());
            }
            assertEquals(2, reachedBriefs.size(), region.id());
            assertEquals(4, reachedBriefCues.size(), region.id());
        }
    }

    @Test
    void explicitBriefCompositionCannotCrossRegionBoundaries() {
        DreamRealmExpeditionBriefCatalog.PreparedBrief prepared = DreamRealmExpeditionBriefCatalog.compose(
                42L, "mistwound_pass", "mistwound_pass_markers");
        assertEquals("mistwound_pass", prepared.regionId());
        assertEquals("mistwound_pass_markers", prepared.briefId());

        assertThrows(IllegalArgumentException.class, () -> DreamRealmExpeditionBriefCatalog.compose(
                42L, "ashen_expanse", "mistwound_pass_markers"));
        assertThrows(IllegalArgumentException.class, () -> DreamRealmExpeditionBriefCatalog.compose(42L, "unknown_region"));
        assertThrows(IllegalArgumentException.class, () -> DreamRealmExpeditionBriefCatalog.compose(
                42L, "ashen_expanse", "unknown_brief"));
    }

    @Test
    void antiOverclaimBoundariesKeepPreparationQualitative() {
        String allBoundaries = DreamRealmExpeditionBriefCatalog.waveOne().stream()
                .map(DreamRealmExpeditionBriefCatalog.ExpeditionBrief::antiOverclaimBoundary)
                .reduce("", (left, right) -> left + " " + right)
                .toLowerCase();

        assertTrue(allBoundaries.contains("travel time") || allBoundaries.contains("crossing duration"));
        assertTrue(allBoundaries.contains("encounter") || allBoundaries.contains("spawn"));
        assertTrue(allBoundaries.contains("guarantee") || allBoundaries.contains("guaranteed"));
        assertTrue(allBoundaries.contains("probability") || allBoundaries.contains("chance"));
        assertTrue(allBoundaries.contains("forecast") || allBoundaries.contains("predict"));
    }
}