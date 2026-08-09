package dev.spud.shadowslave.dreamrealm.content;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DreamRealmFieldReportCatalogTest {
    @Test
    void anchorsCoverEveryRegionAndKnowledgeStateExactlyOnce() {
        var regions = DreamRealmRegionContentCatalog.waveOne();
        var anchors = DreamRealmFieldReportCatalog.anchors();

        assertEquals(30, anchors.size());
        assertEquals(30, anchors.stream().map(DreamRealmFieldReportCatalog.KnowledgeAnchor::id).distinct().count());

        Map<String, EnumMap<DreamRealmFieldReportCatalog.KnowledgeState, Integer>> counts = new HashMap<>();
        anchors.forEach(anchor -> counts
                .computeIfAbsent(anchor.regionId(), ignored -> new EnumMap<>(DreamRealmFieldReportCatalog.KnowledgeState.class))
                .merge(anchor.knowledgeState(), 1, Integer::sum));

        for (var region : regions) {
            assertEquals(3, counts.get(region.id()).size(), region.id());
            for (var state : DreamRealmFieldReportCatalog.KnowledgeState.values()) {
                assertEquals(1, counts.get(region.id()).get(state), region.id() + "/" + state);
            }
        }
    }

    @Test
    void everyAnchorUsesOnlyAPrimitiveAlreadyAuthoredForItsSourceRegion() {
        Map<String, DreamRealmRegionContentCatalog.RegionProfile> regions = new HashMap<>();
        DreamRealmRegionContentCatalog.waveOne().forEach(region -> regions.put(region.id(), region));

        for (var anchor : DreamRealmFieldReportCatalog.anchors()) {
            var region = regions.get(anchor.regionId());
            assertTrue(region != null, anchor.id());

            switch (anchor.subjectKind()) {
                case REGION_HAZARD -> assertTrue(
                        region.hazards().contains(DreamRealmRegionContentCatalog.Hazard.valueOf(anchor.subjectId().toUpperCase())),
                        anchor.id());
                case LANDMARK -> assertTrue(region.landmarkHooks().contains(anchor.subjectId()), anchor.id());
                case CREATURE_SIGN -> assertTrue(region.creatureAffinityIds().contains(anchor.subjectId()), anchor.id());
            }

            assertFalse(anchor.headline().isBlank(), anchor.id());
            assertFalse(anchor.observation().isBlank(), anchor.id());
            assertFalse(anchor.basis().isBlank(), anchor.id());
            assertFalse(anchor.limitation().isBlank(), anchor.id());
        }
    }

    @Test
    void framingsProvideThreeReusablePresentationsForEveryKnowledgeState() {
        var framings = DreamRealmFieldReportCatalog.framings();
        assertEquals(9, framings.size());
        assertEquals(9, framings.stream().map(DreamRealmFieldReportCatalog.ReportFraming::id).distinct().count());

        EnumMap<DreamRealmFieldReportCatalog.KnowledgeState, Integer> counts =
                new EnumMap<>(DreamRealmFieldReportCatalog.KnowledgeState.class);
        framings.forEach(framing -> counts.merge(framing.knowledgeState(), 1, Integer::sum));

        for (var state : DreamRealmFieldReportCatalog.KnowledgeState.values()) {
            assertEquals(3, counts.get(state), state.name());
        }
    }

    @Test
    void deterministicCompositionPreservesCallerSuppliedRegionAndKnowledgeState() {
        for (var region : DreamRealmRegionContentCatalog.waveOne()) {
            for (var state : DreamRealmFieldReportCatalog.KnowledgeState.values()) {
                var first = DreamRealmFieldReportCatalog.compose(918273645L, region.id(), state);
                var second = DreamRealmFieldReportCatalog.compose(918273645L, region.id(), state);

                assertEquals(first, second);
                assertEquals(DreamRealmFieldReportCatalog.GENERATOR_VERSION, first.generatorVersion());
                assertEquals(region.id(), first.regionId());
                assertEquals(region.displayName(), first.regionName());
                assertEquals(state, first.knowledgeState());
                assertEquals(region.id() + "_" + state.name().toLowerCase(), first.anchorId());
            }
        }
    }

    @Test
    void seedCanVaryPresentationButNeverKnowledgeIdentityOrSubject() {
        for (var region : DreamRealmRegionContentCatalog.waveOne()) {
            for (var state : DreamRealmFieldReportCatalog.KnowledgeState.values()) {
                var baseline = DreamRealmFieldReportCatalog.compose(0L, region.id(), state);
                Set<String> reachedFramings = new HashSet<>();

                for (long seed = 0; seed < 2048; seed++) {
                    var report = DreamRealmFieldReportCatalog.compose(seed, region.id(), state);
                    reachedFramings.add(report.framingId());

                    assertEquals(baseline.regionId(), report.regionId());
                    assertEquals(baseline.regionName(), report.regionName());
                    assertEquals(baseline.knowledgeState(), report.knowledgeState());
                    assertEquals(baseline.anchorId(), report.anchorId());
                    assertEquals(baseline.subjectKind(), report.subjectKind());
                    assertEquals(baseline.subjectId(), report.subjectId());
                    assertEquals(baseline.basis(), report.basis());
                    assertEquals(baseline.limitation(), report.limitation());
                }

                assertEquals(3, reachedFramings.size(), region.id() + "/" + state);
            }
        }
    }

    @Test
    void certaintySemanticsRemainBoundedAndDoNotBecomeWorldTruth() {
        for (var region : DreamRealmRegionContentCatalog.waveOne()) {
            var observed = DreamRealmFieldReportCatalog.compose(7L, region.id(), DreamRealmFieldReportCatalog.KnowledgeState.OBSERVED);
            var verified = DreamRealmFieldReportCatalog.compose(7L, region.id(), DreamRealmFieldReportCatalog.KnowledgeState.VERIFIED);
            var provisional = DreamRealmFieldReportCatalog.compose(7L, region.id(), DreamRealmFieldReportCatalog.KnowledgeState.PROVISIONAL);

            assertEquals(DreamRealmFieldReportCatalog.SubjectKind.REGION_HAZARD, observed.subjectKind());
            assertEquals(DreamRealmFieldReportCatalog.SubjectKind.LANDMARK, verified.subjectKind());
            assertEquals(DreamRealmFieldReportCatalog.SubjectKind.CREATURE_SIGN, provisional.subjectKind());

            String provisionalBoundary = provisional.limitation().toLowerCase();
            assertTrue(provisionalBoundary.contains("not") || provisionalBoundary.contains("does not"), region.id());
            assertTrue(
                    provisionalBoundary.contains("confirm")
                            || provisionalBoundary.contains("proof")
                            || provisionalBoundary.contains("prove"),
                    region.id());
        }
    }

    @Test
    void highRiskReportsExplicitlyRejectMapRevealPredictionAndCreatureConfirmation() {
        String cairn = DreamRealmFieldReportCatalog.anchor("mistwound_pass_verified").limitation().toLowerCase();
        String belfry = DreamRealmFieldReportCatalog.anchor("storm_lantern_coast_verified").limitation().toLowerCase();
        String bellSign = DreamRealmFieldReportCatalog.anchor("storm_lantern_coast_provisional").limitation().toLowerCase();
        String mimicSign = DreamRealmFieldReportCatalog.anchor("mistwound_pass_provisional").limitation().toLowerCase();

        assertTrue(cairn.contains("map reveal") || cairn.contains("map-reveal"));
        assertTrue(cairn.contains("weather oracle"));
        assertTrue(belfry.contains("forecast"));
        assertTrue(bellSign.contains("does not confirm"));
        assertTrue(mimicSign.contains("does not identify"));
    }

    @Test
    void unknownRegionStateAndAnchorInputsFailClosed() {
        assertThrows(IllegalArgumentException.class, () ->
                DreamRealmFieldReportCatalog.compose(1L, "unknown_region", DreamRealmFieldReportCatalog.KnowledgeState.OBSERVED));
        assertThrows(NullPointerException.class, () ->
                DreamRealmFieldReportCatalog.compose(1L, "ashen_expanse", null));
        assertThrows(IllegalArgumentException.class, () -> DreamRealmFieldReportCatalog.anchor("missing_anchor"));
        assertThrows(IllegalArgumentException.class, () ->
                DreamRealmFieldReportCatalog.compose(1L, "Ashen Expanse", DreamRealmFieldReportCatalog.KnowledgeState.OBSERVED));
    }
}
