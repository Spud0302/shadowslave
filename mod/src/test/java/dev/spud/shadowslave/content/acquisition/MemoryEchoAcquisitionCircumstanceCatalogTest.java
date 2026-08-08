package dev.spud.shadowslave.content.acquisition;

import dev.spud.shadowslave.content.acquisition.MemoryEchoAcquisitionContextCatalog.AcquisitionSource;
import dev.spud.shadowslave.content.acquisition.MemoryEchoAcquisitionContextCatalog.SubjectKind;
import dev.spud.shadowslave.dreamrealm.content.DreamRealmRegionContentCatalog;
import dev.spud.shadowslave.nightmare.content.DrownedBellScenarioDefinition;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.spud.shadowslave.content.acquisition.MemoryEchoAcquisitionCircumstanceCatalog.drownedBellResolutionAnchor;
import static dev.spud.shadowslave.content.acquisition.MemoryEchoAcquisitionCircumstanceCatalog.regionAnchor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemoryEchoAcquisitionCircumstanceCatalogTest {
    @Test
    void waveOneCoversEveryMergedDreamRealmRegionAndEveryDrownedBellResolution() {
        MemoryEchoAcquisitionCircumstanceCatalog catalog = MemoryEchoAcquisitionCircumstanceCatalog.waveOne();
        assertEquals(15, catalog.circumstances().size());

        Set<String> expectedRegions = DreamRealmRegionContentCatalog.waveOne().stream()
                .map(DreamRealmRegionContentCatalog.RegionProfile::id)
                .collect(Collectors.toSet());
        Set<String> actualRegions = catalog.circumstances().stream()
                .filter(value -> value.anchor().kind()
                        == MemoryEchoAcquisitionCircumstanceCatalog.AnchorKind.DREAM_REALM_REGION)
                .map(value -> value.anchor().entryId())
                .collect(Collectors.toSet());
        assertEquals(expectedRegions, actualRegions);

        Set<String> expectedResolutions = DrownedBellScenarioDefinition.content().resolutions().keySet();
        Set<String> actualResolutions = catalog.circumstances().stream()
                .filter(value -> value.anchor().kind()
                        == MemoryEchoAcquisitionCircumstanceCatalog.AnchorKind.NIGHTMARE_RESOLUTION)
                .map(value -> value.anchor().entryId())
                .collect(Collectors.toSet());
        assertEquals(expectedResolutions, actualResolutions);
    }

    @Test
    void everyCircumstanceIsExplicitDesignAndSupportsBothCurrentSubjectKinds() {
        MemoryEchoAcquisitionCircumstanceCatalog catalog = MemoryEchoAcquisitionCircumstanceCatalog.waveOne();
        Set<String> ids = new HashSet<>();

        catalog.circumstances().forEach(value -> {
            assertTrue(ids.add(value.id()));
            assertEquals("DESIGN", value.evidenceClassification());
            assertEquals(Set.of(SubjectKind.MEMORY, SubjectKind.ECHO), value.subjectKinds());
            assertTrue(value.evidenceTags().size() >= 3);
            assertTrue(value.title().length() >= 8);
            assertTrue(value.description().length() >= 50);
        });
    }

    @Test
    void authoritativeSourceCannotBeChangedBySeedEvidenceOrAnchor() {
        MemoryEchoAcquisitionCircumstanceCatalog catalog = MemoryEchoAcquisitionCircumstanceCatalog.waveOne();

        for (long seed = 0; seed < 256; seed++) {
            var transfer = catalog.compose(seed, SubjectKind.MEMORY, AcquisitionSource.TRANSFER,
                    regionAnchor("chainfall_reach"), Map.of("creature", 1, "guide", 2));
            assertEquals(SubjectKind.MEMORY, transfer.subjectKind());
            assertEquals(AcquisitionSource.TRANSFER, transfer.authoritativeSource());
            assertEquals(regionAnchor("chainfall_reach"), transfer.anchor());
            assertEquals("chainfall_guide_exchange", transfer.circumstanceId());

            var creature = catalog.compose(seed, SubjectKind.ECHO, AcquisitionSource.SLAIN_CREATURE,
                    regionAnchor("bonewhite_march"), Map.of("discovery", 9, "tracking", 1));
            assertEquals(SubjectKind.ECHO, creature.subjectKind());
            assertEquals(AcquisitionSource.SLAIN_CREATURE, creature.authoritativeSource());
            assertEquals(regionAnchor("bonewhite_march"), creature.anchor());
            assertEquals("bonewhite_hunt_aftermath", creature.circumstanceId());
        }
    }

    @Test
    void incompatibleSourceAnchorPairsFailClosedInsteadOfInventingProvenance() {
        MemoryEchoAcquisitionCircumstanceCatalog catalog = MemoryEchoAcquisitionCircumstanceCatalog.waveOne();

        assertThrows(IllegalArgumentException.class, () -> catalog.compose(
                1L, SubjectKind.MEMORY, AcquisitionSource.SLAIN_CREATURE,
                regionAnchor("chainfall_reach"), Map.of("creature", 1)));
        assertThrows(IllegalArgumentException.class, () -> catalog.compose(
                1L, SubjectKind.ECHO, AcquisitionSource.UNKNOWN,
                regionAnchor("ashen_expanse"), Map.of("mystery", 1)));
        assertThrows(IllegalArgumentException.class, () -> catalog.compose(
                1L, SubjectKind.MEMORY, AcquisitionSource.TRANSFER,
                drownedBellResolutionAnchor("tower_held"), Map.of("transfer", 1)));
    }

    @Test
    void deterministicCompositionIgnoresMapOrderAndPositiveMagnitude() {
        MemoryEchoAcquisitionCircumstanceCatalog catalog = MemoryEchoAcquisitionCircumstanceCatalog.waveOne();
        Map<String, Integer> first = new HashMap<>();
        first.put("discovery", 1);
        first.put("flood", 7);
        first.put("salvage", 999);

        Map<String, Integer> reordered = new HashMap<>();
        reordered.put("salvage", 1);
        reordered.put("discovery", 88);
        reordered.put("flood", 2);

        var a = catalog.compose(4815162342L, SubjectKind.MEMORY, AcquisitionSource.AUTHORED_DISCOVERY,
                regionAnchor("red_canopy"), first);
        var b = catalog.compose(4815162342L, SubjectKind.MEMORY, AcquisitionSource.AUTHORED_DISCOVERY,
                regionAnchor("red_canopy"), reordered);

        assertEquals(a, b);
        assertEquals(Set.of("discovery", "flood", "salvage"), a.matchedEvidence());
        assertEquals("red_canopy_flooded_cache", a.circumstanceId());
    }

    @Test
    void redCanopyCanFrameDifferentAlreadyResolvedSourcesWithoutRollingBetweenThem() {
        MemoryEchoAcquisitionCircumstanceCatalog catalog = MemoryEchoAcquisitionCircumstanceCatalog.waveOne();

        var discovered = catalog.compose(55L, SubjectKind.MEMORY, AcquisitionSource.AUTHORED_DISCOVERY,
                regionAnchor("red_canopy"), Map.of("discovery", 1, "flood", 1));
        var creature = catalog.compose(55L, SubjectKind.MEMORY, AcquisitionSource.SLAIN_CREATURE,
                regionAnchor("red_canopy"), Map.of("creature", 1, "rain", 1));

        assertEquals("red_canopy_flooded_cache", discovered.circumstanceId());
        assertEquals(AcquisitionSource.AUTHORED_DISCOVERY, discovered.authoritativeSource());
        assertEquals("red_canopy_hunt_aftermath", creature.circumstanceId());
        assertEquals(AcquisitionSource.SLAIN_CREATURE, creature.authoritativeSource());
    }

    @Test
    void drownedBellCircumstancesReferenceExactResolutionEvidenceWithoutBecomingRewards() {
        MemoryEchoAcquisitionCircumstanceCatalog catalog = MemoryEchoAcquisitionCircumstanceCatalog.waveOne();
        Map<String, Set<String>> expectedEvidence = Map.of(
                "tower_held", Set.of("warning", "duty", "preservation"),
                "villagers_evacuated", Set.of("guidance", "movement", "preservation"),
                "flood_diverted", Set.of("water", "precision", "preservation"),
                "creature_buried", Set.of("sound", "precision", "retaliation"));

        for (Map.Entry<String, Set<String>> entry : expectedEvidence.entrySet()) {
            Map<String, Integer> evidence = entry.getValue().stream()
                    .collect(Collectors.toMap(tag -> tag, tag -> 1));
            var resolved = catalog.compose(91L, SubjectKind.ECHO, AcquisitionSource.AUTHORED_DISCOVERY,
                    drownedBellResolutionAnchor(entry.getKey()), evidence);
            assertEquals(AcquisitionSource.AUTHORED_DISCOVERY, resolved.authoritativeSource());
            assertEquals(entry.getValue(), resolved.matchedEvidence());
            assertEquals(entry.getKey(), resolved.anchor().entryId());
        }
    }

    @Test
    void negativeEvidenceAndUnknownAnchorsFailClosed() {
        MemoryEchoAcquisitionCircumstanceCatalog catalog = MemoryEchoAcquisitionCircumstanceCatalog.waveOne();

        assertThrows(IllegalArgumentException.class, () -> catalog.compose(
                1L, SubjectKind.MEMORY, AcquisitionSource.AUTHORED_DISCOVERY,
                regionAnchor("ashen_expanse"), Map.of("salvage", -1)));
        assertThrows(IllegalArgumentException.class, () -> new MemoryEchoAcquisitionCircumstanceCatalog(
                java.util.List.of(new MemoryEchoAcquisitionCircumstanceCatalog.AcquisitionCircumstance(
                        "bad_anchor",
                        Set.of(SubjectKind.MEMORY),
                        Set.of(AcquisitionSource.AUTHORED_DISCOVERY),
                        regionAnchor("not_a_region"),
                        Set.of("discovery"),
                        "Bad Anchor",
                        "This test-only circumstance must be rejected because its region identity is not authoritative.",
                        "DESIGN"))));
        assertThrows(IllegalArgumentException.class, () -> catalog.find("missing_circumstance"));
    }
}
