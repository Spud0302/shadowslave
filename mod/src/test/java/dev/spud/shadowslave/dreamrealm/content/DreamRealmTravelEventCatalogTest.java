package dev.spud.shadowslave.dreamrealm.content;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DreamRealmTravelEventCatalogTest {
    @Test
    void waveOneProvidesExactlyTwoBoundedEventsPerCurrentRegion() {
        List<DreamRealmTravelEventCatalog.TravelEvent> events = DreamRealmTravelEventCatalog.waveOne();
        List<DreamRealmRegionContentCatalog.RegionProfile> regions = DreamRealmRegionContentCatalog.waveOne();

        assertEquals(20, events.size());
        assertEquals(20, events.stream().map(DreamRealmTravelEventCatalog.TravelEvent::id).distinct().count());

        Map<String, Long> counts = new HashMap<>();
        events.forEach(event -> counts.merge(event.regionId(), 1L, Long::sum));
        assertEquals(regions.size(), counts.size());
        for (DreamRealmRegionContentCatalog.RegionProfile region : regions) {
            assertEquals(2L, counts.getOrDefault(region.id(), 0L), region.id());
        }
    }

    @Test
    void everyEventUsesOnlyHazardAndTraversalFromItsSourceRegion() {
        Map<String, DreamRealmRegionContentCatalog.RegionProfile> regions = new HashMap<>();
        DreamRealmRegionContentCatalog.waveOne().forEach(region -> regions.put(region.id(), region));

        for (DreamRealmTravelEventCatalog.TravelEvent event : DreamRealmTravelEventCatalog.waveOne()) {
            DreamRealmRegionContentCatalog.RegionProfile region = regions.get(event.regionId());
            assertTrue(region.hazards().contains(event.hazard()), event.id());
            assertTrue(region.traversal().contains(event.traversal()), event.id());
            assertTrue(event.approachCues().size() >= 2, event.id());
            assertEquals(3, event.choices().size(), event.id());
            assertFalse(event.pressureRead().isBlank(), event.id());
            assertFalse(event.decisionPrompt().isBlank(), event.id());
            assertFalse(event.antiOverclaimBoundary().isBlank(), event.id());
        }
    }

    @Test
    void catalogueUsesEveryAuthoredTravelFamily() {
        EnumSet<DreamRealmTravelEventCatalog.TravelFamily> families = EnumSet.noneOf(DreamRealmTravelEventCatalog.TravelFamily.class);
        DreamRealmTravelEventCatalog.waveOne().forEach(event -> families.add(event.family()));
        assertEquals(EnumSet.allOf(DreamRealmTravelEventCatalog.TravelFamily.class), families);
    }

    @Test
    void sameSeedAndRegionAlwaysResolveTheSamePresentation() {
        for (DreamRealmRegionContentCatalog.RegionProfile region : DreamRealmRegionContentCatalog.waveOne()) {
            assertEquals(
                    DreamRealmTravelEventCatalog.compose(123456789L, region.id()),
                    DreamRealmTravelEventCatalog.compose(123456789L, region.id()),
                    region.id()
            );
        }
    }

    @Test
    void seedCanVaryPresentationButNeverCallerSuppliedRegionOrAuthoredMechanics() {
        Map<String, List<DreamRealmTravelEventCatalog.TravelEvent>> eventsByRegion = new HashMap<>();
        for (DreamRealmTravelEventCatalog.TravelEvent event : DreamRealmTravelEventCatalog.waveOne()) {
            eventsByRegion.computeIfAbsent(event.regionId(), ignored -> new java.util.ArrayList<>()).add(event);
        }

        for (DreamRealmRegionContentCatalog.RegionProfile region : DreamRealmRegionContentCatalog.waveOne()) {
            Set<String> reachableEvents = new HashSet<>();
            Set<String> reachableCues = new HashSet<>();
            for (long seed = 0; seed < 2048; seed++) {
                DreamRealmTravelEventCatalog.ResolvedTravelEvent resolved = DreamRealmTravelEventCatalog.compose(seed, region.id());
                DreamRealmTravelEventCatalog.TravelEvent source = DreamRealmTravelEventCatalog.byId(resolved.event().id());

                assertEquals(region.id(), resolved.regionId(), region.id());
                assertEquals(region.id(), resolved.event().regionId(), region.id());
                assertEquals(DreamRealmTravelEventCatalog.GENERATOR_VERSION, resolved.generatorVersion(), region.id());
                assertEquals(source.family(), resolved.event().family(), region.id());
                assertEquals(source.hazard(), resolved.event().hazard(), region.id());
                assertEquals(source.traversal(), resolved.event().traversal(), region.id());
                assertEquals(source.decisionPrompt(), resolved.event().decisionPrompt(), region.id());
                assertEquals(source.choices(), resolved.event().choices(), region.id());
                assertEquals(source.antiOverclaimBoundary(), resolved.event().antiOverclaimBoundary(), region.id());
                assertTrue(source.approachCues().contains(resolved.approachCue()), region.id());

                reachableEvents.add(resolved.event().id());
                reachableCues.add(resolved.event().id() + "::" + resolved.approachCue());
            }

            assertEquals(2, reachableEvents.size(), region.id());
            int expectedCueCount = eventsByRegion.get(region.id()).stream().mapToInt(event -> event.approachCues().size()).sum();
            assertEquals(expectedCueCount, reachableCues.size(), region.id());
        }
    }

    @Test
    void highRiskEvocativeEventsRetainExplicitNegativeBoundaries() {
        assertTrue(DreamRealmTravelEventCatalog.byId("glassmere_flats_resonance_lull")
                .antiOverclaimBoundary().toLowerCase().contains("forecast"));
        assertTrue(DreamRealmTravelEventCatalog.byId("storm_lantern_coast_bell_weather")
                .antiOverclaimBoundary().toLowerCase().contains("prediction"));
        assertTrue(DreamRealmTravelEventCatalog.byId("mistwound_pass_landmark_check")
                .antiOverclaimBoundary().toLowerCase().contains("truthful"));
        assertTrue(DreamRealmTravelEventCatalog.byId("hollow_causeway_dark_junction")
                .antiOverclaimBoundary().toLowerCase().contains("map reveal"));
    }

    @Test
    void eventsDoNotOwnRewardsSpawnsOrTravelProbabilities() {
        String allBoundaries = DreamRealmTravelEventCatalog.waveOne().stream()
                .map(DreamRealmTravelEventCatalog.TravelEvent::antiOverclaimBoundary)
                .map(String::toLowerCase)
                .reduce("", (left, right) -> left + " " + right);

        assertTrue(allBoundaries.contains("probability") || allBoundaries.contains("frequency"));
        assertFalse(DreamRealmTravelEventCatalog.waveOne().stream()
                .map(DreamRealmTravelEventCatalog.TravelEvent::decisionPrompt)
                .anyMatch(prompt -> prompt.toLowerCase().contains("reward")));
    }

    @Test
    void unknownRegionsAndEventsFailClosed() {
        assertThrows(IllegalArgumentException.class,
                () -> DreamRealmTravelEventCatalog.compose(1L, "unknown_region"));
        assertThrows(IllegalArgumentException.class,
                () -> DreamRealmTravelEventCatalog.byId("unknown_event"));
        assertThrows(IllegalArgumentException.class,
                () -> DreamRealmTravelEventCatalog.compose(1L, "Bad Region"));
    }
}