package dev.spud.shadowslave.nightmare.content;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightmareDeedContentCatalogTest {
    @Test
    void catalogueProvidesBroadAuthoredDeedVocabulary() {
        List<NightmareDeedContentCatalog.DeedDefinition> definitions = NightmareDeedContentCatalog.definitions();

        assertEquals(16, definitions.size());
        assertEquals(16, definitions.stream().map(NightmareDeedContentCatalog.DeedDefinition::id).distinct().count());
        assertEquals(Set.of(
                        NightmareDeedContentCatalog.DeedFamily.DISCOVERY,
                        NightmareDeedContentCatalog.DeedFamily.PRESERVATION,
                        NightmareDeedContentCatalog.DeedFamily.SACRIFICE,
                        NightmareDeedContentCatalog.DeedFamily.SOCIAL,
                        NightmareDeedContentCatalog.DeedFamily.CHOICE,
                        NightmareDeedContentCatalog.DeedFamily.CONFLICT,
                        NightmareDeedContentCatalog.DeedFamily.ENDURANCE
                ),
                definitions.stream().map(NightmareDeedContentCatalog.DeedDefinition::family).collect(java.util.stream.Collectors.toSet()));
        assertTrue(definitions.stream().allMatch(definition -> definition.classification() == NightmareDeedContentCatalog.EvidenceClassification.DESIGN));
        assertTrue(definitions.stream().allMatch(definition -> !definition.evidenceTags().isEmpty()));
        assertTrue(definitions.stream().allMatch(definition -> definition.presentationCue().length() > 25));
    }

    @Test
    void currentDrownedBellEvidenceCanProduceDistinctNarrativeDeeds() {
        Map<String, Integer> towerHeld = Map.of("duty", 4, "warning", 4, "resolve", 3, "sacrifice", 2, "preservation", 2);
        Map<String, Integer> evacuated = Map.of("guidance", 4, "movement", 3, "preservation", 4, "social", 2, "adaptation", 2);
        Map<String, Integer> floodDiverted = Map.of("water", 4, "precision", 3, "sacrifice", 2, "preservation", 4, "endurance", 2);
        Map<String, Integer> creatureBuried = Map.of("sound", 4, "warning", 2, "precision", 4, "retaliation", 3, "resolve", 2);

        Set<List<String>> shapes = new HashSet<>();
        for (Map<String, Integer> evidence : List.of(towerHeld, evacuated, floodDiverted, creatureBuried)) {
            List<NightmareDeedContentCatalog.ComposedDeed> deeds = NightmareDeedContentCatalog.compose(91L, evidence, 3);
            assertEquals(3, deeds.size());
            assertTrue(deeds.stream().allMatch(deed -> evidence.keySet().containsAll(deed.matchedEvidenceTags())));
            shapes.add(deeds.stream().map(NightmareDeedContentCatalog.ComposedDeed::definitionId).toList());
        }

        assertEquals(4, shapes.size());
    }

    @Test
    void compositionIsDeterministicAndMapOrderIndependent() {
        Map<String, Integer> first = new HashMap<>();
        first.put("truth", 4);
        first.put("social", 3);
        first.put("evidence", 2);
        first.put("adaptation", 1);

        Map<String, Integer> second = new HashMap<>();
        second.put("adaptation", 1);
        second.put("evidence", 2);
        second.put("social", 3);
        second.put("truth", 4);

        assertEquals(
                NightmareDeedContentCatalog.compose(771L, first, 4),
                NightmareDeedContentCatalog.compose(771L, second, 4)
        );
    }

    @Test
    void evidenceMagnitudeDoesNotBecomeAnAppraisalScore() {
        Map<String, Integer> low = Map.of("warning", 1, "duty", 1, "preservation", 1);
        Map<String, Integer> high = Map.of("warning", 999, "duty", 42, "preservation", 77);

        assertEquals(
                NightmareDeedContentCatalog.compose(12L, low, 3),
                NightmareDeedContentCatalog.compose(12L, high, 3)
        );
    }

    @Test
    void zeroEvidenceIsIgnoredAndUnknownPositiveTagsDoNotInventDeeds() {
        assertTrue(NightmareDeedContentCatalog.compose(5L, Map.of("warning", 0), 3).isEmpty());
        assertTrue(NightmareDeedContentCatalog.compose(5L, Map.of("unmapped_scenario_fact", 4), 3).isEmpty());
    }

    @Test
    void invalidInputsFailClosed() {
        assertThrows(IllegalArgumentException.class, () -> NightmareDeedContentCatalog.compose(1L, Map.of("warning", -1), 2));
        assertThrows(IllegalArgumentException.class, () -> NightmareDeedContentCatalog.compose(1L, Map.of("warning", 1), 0));
        assertThrows(IllegalArgumentException.class, () -> NightmareDeedContentCatalog.require("missing"));
    }

    @Test
    void seededTieBreakCanVaryPresentationWithoutChangingEvidenceAuthority() {
        Map<String, Integer> evidence = Map.of("social", 1, "adaptation", 1, "precision", 1, "truth", 1);
        Set<List<String>> observed = new HashSet<>();

        for (long seed = 0; seed < 256; seed++) {
            List<NightmareDeedContentCatalog.ComposedDeed> deeds = NightmareDeedContentCatalog.compose(seed, evidence, 3);
            assertFalse(deeds.isEmpty());
            assertTrue(deeds.stream().allMatch(deed -> evidence.keySet().containsAll(deed.matchedEvidenceTags())));
            observed.add(deeds.stream().map(NightmareDeedContentCatalog.ComposedDeed::definitionId).toList());
        }

        assertTrue(observed.size() >= 2, "Expected presentation tie-break variation across seeds");
    }
}
