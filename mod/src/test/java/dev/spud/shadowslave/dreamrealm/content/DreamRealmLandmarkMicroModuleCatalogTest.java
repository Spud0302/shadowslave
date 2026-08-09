package dev.spud.shadowslave.dreamrealm.content;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DreamRealmLandmarkMicroModuleCatalogTest {
    @Test
    void waveOneCoversEveryMergedLandmarkExactlyOnce() {
        List<DreamRealmLandmarkMicroModuleCatalog.LandmarkModule> modules =
                DreamRealmLandmarkMicroModuleCatalog.waveOne();
        Set<String> expectedAnchors = new HashSet<>();
        DreamRealmRegionContentCatalog.waveOne().forEach(region ->
                region.landmarkHooks().forEach(landmark -> expectedAnchors.add(region.id() + "/" + landmark)));

        Set<String> actualAnchors = new HashSet<>();
        modules.forEach(module -> actualAnchors.add(module.regionId() + "/" + module.landmarkHook()));

        assertEquals(30, modules.size());
        assertEquals(expectedAnchors, actualAnchors);
        assertEquals(30, modules.stream().map(DreamRealmLandmarkMicroModuleCatalog.LandmarkModule::id).distinct().count());
    }

    @Test
    void everyModuleUsesOnlyItsSourceRegionsHazardAndOpportunityVocabulary() {
        for (DreamRealmRegionContentCatalog.RegionProfile region : DreamRealmRegionContentCatalog.waveOne()) {
            DreamRealmLandmarkMicroModuleCatalog.waveOne().stream()
                    .filter(module -> module.regionId().equals(region.id()))
                    .forEach(module -> {
                        assertTrue(region.landmarkHooks().contains(module.landmarkHook()), module.id());
                        assertTrue(region.hazards().contains(module.pressureHazard()), module.id());
                        assertTrue(region.opportunities().contains(module.opportunity()), module.id());
                        assertTrue(module.approachCues().size() >= 2, module.id());
                        assertTrue(module.decisionOptions().size() >= 2, module.id());
                        assertTrue(module.decisionPrompt().length() >= 40, module.id());
                        assertTrue(module.antiOverclaimBoundary().length() >= 45, module.id());
                    });
        }
    }

    @Test
    void catalogueUsesAllFivePlayerFacingInteractionFamilies() {
        EnumSet<DreamRealmLandmarkMicroModuleCatalog.InteractionFamily> families =
                EnumSet.noneOf(DreamRealmLandmarkMicroModuleCatalog.InteractionFamily.class);
        DreamRealmLandmarkMicroModuleCatalog.waveOne().forEach(module -> families.add(module.family()));

        assertEquals(EnumSet.allOf(DreamRealmLandmarkMicroModuleCatalog.InteractionFamily.class), families);
    }

    @Test
    void seedMayVaryOnlyApproachCueNotResolvedLandmarkIdentityOrDecisionContent() {
        for (DreamRealmLandmarkMicroModuleCatalog.LandmarkModule module :
                DreamRealmLandmarkMicroModuleCatalog.waveOne()) {
            for (long seed = 0; seed < 256; seed++) {
                DreamRealmLandmarkMicroModuleCatalog.LandmarkPresentation presentation =
                        DreamRealmLandmarkMicroModuleCatalog.compose(seed, module.regionId(), module.landmarkHook());

                assertEquals(DreamRealmLandmarkMicroModuleCatalog.GENERATOR_VERSION,
                        presentation.generatorVersion(), module.id());
                assertEquals(module.id(), presentation.moduleId(), module.id());
                assertEquals(module.regionId(), presentation.regionId(), module.id());
                assertEquals(module.landmarkHook(), presentation.landmarkHook(), module.id());
                assertEquals(module.family(), presentation.family(), module.id());
                assertEquals(module.pressureHazard(), presentation.pressureHazard(), module.id());
                assertEquals(module.opportunity(), presentation.opportunity(), module.id());
                assertEquals(module.decisionPrompt(), presentation.decisionPrompt(), module.id());
                assertEquals(module.decisionOptions(), presentation.decisionOptions(), module.id());
                assertEquals(module.antiOverclaimBoundary(), presentation.antiOverclaimBoundary(), module.id());
                assertTrue(module.approachCues().contains(presentation.approachCue()), module.id());
            }
        }
    }

    @Test
    void bothAuthoredApproachCuesAreReachableForEveryLandmark() {
        for (DreamRealmLandmarkMicroModuleCatalog.LandmarkModule module :
                DreamRealmLandmarkMicroModuleCatalog.waveOne()) {
            Set<String> surfaced = new HashSet<>();
            for (long seed = 0; seed < 512; seed++) {
                surfaced.add(DreamRealmLandmarkMicroModuleCatalog.compose(
                        seed, module.regionId(), module.landmarkHook()).approachCue());
            }
            assertEquals(new HashSet<>(module.approachCues()), surfaced, module.id());
        }
    }

    @Test
    void sameSeedAndAnchorAreDeterministic() {
        var first = DreamRealmLandmarkMicroModuleCatalog.compose(
                42L, "mistwound_pass", "echo_gate");
        var second = DreamRealmLandmarkMicroModuleCatalog.compose(
                42L, "mistwound_pass", "echo_gate");

        assertEquals(first, second);
        assertNotEquals("", first.approachCue());
    }

    @Test
    void unknownOrCrossRegionLandmarksFailClosed() {
        assertThrows(IllegalArgumentException.class, () ->
                DreamRealmLandmarkMicroModuleCatalog.compose(1L, "ashen_expanse", "not_a_landmark"));
        assertThrows(IllegalArgumentException.class, () ->
                DreamRealmLandmarkMicroModuleCatalog.compose(1L, "ashen_expanse", "echo_gate"));
        assertThrows(IllegalArgumentException.class, () ->
                DreamRealmLandmarkMicroModuleCatalog.compose(1L, "not_a_region", "echo_gate"));
    }

    @Test
    void highRiskLandmarksKeepExplicitNegativeBoundaries() {
        var obelisk = DreamRealmLandmarkMicroModuleCatalog.find("ashen_expanse", "black_obelisk").orElseThrow();
        var temple = DreamRealmLandmarkMicroModuleCatalog.find("red_canopy", "flooded_temple").orElseThrow();
        var ring = DreamRealmLandmarkMicroModuleCatalog.find("thornwake_basin", "stone_ring").orElseThrow();
        var harbour = DreamRealmLandmarkMicroModuleCatalog.find("blackwater_steps", "rope_harbour").orElseThrow();

        assertTrue(obelisk.antiOverclaimBoundary().contains("prophecy"));
        assertTrue(temple.antiOverclaimBoundary().contains("religion"));
        assertTrue(ring.antiOverclaimBoundary().contains("safe zone"));
        assertTrue(harbour.antiOverclaimBoundary().contains("guaranteed loot"));
    }
}
