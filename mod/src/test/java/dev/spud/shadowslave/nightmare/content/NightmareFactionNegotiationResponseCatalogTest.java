package dev.spud.shadowslave.nightmare.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class NightmareFactionNegotiationResponseCatalogTest {

    @Test
    void waveOneHasTwentyUniqueResponsesAndFourPerFamily() {
        List<NightmareFactionNegotiationResponseCatalog.Primitive> primitives =
                NightmareFactionNegotiationResponseCatalog.waveOne();

        assertEquals(20, primitives.size());
        assertEquals(20, primitives.stream().map(NightmareFactionNegotiationResponseCatalog.Primitive::id)
                .collect(Collectors.toSet()).size());

        Map<NightmareFactionNegotiationResponseCatalog.ResponseFamily, Long> byFamily = primitives.stream()
                .collect(Collectors.groupingBy(NightmareFactionNegotiationResponseCatalog.Primitive::family,
                        Collectors.counting()));
        for (NightmareFactionNegotiationResponseCatalog.ResponseFamily family
                : NightmareFactionNegotiationResponseCatalog.ResponseFamily.values()) {
            assertEquals(4L, byFamily.getOrDefault(family, 0L), family.name());
        }
    }

    @Test
    void everyResponseHasBoundedPlayerFacingContent() {
        for (NightmareFactionNegotiationResponseCatalog.Primitive primitive
                : NightmareFactionNegotiationResponseCatalog.waveOne()) {
            assertFalse(primitive.title().isBlank());
            assertTrue(primitive.responseRead().length() >= 40, primitive.id());
            assertTrue(primitive.factionLine().length() >= 15, primitive.id());
            assertEquals(3, primitive.playerOptions().size(), primitive.id());
            assertEquals(2, primitive.presentationCues().size(), primitive.id());
            assertFalse(primitive.affinityTags().isEmpty(), primitive.id());
            assertTrue(primitive.antiOverclaimBoundary().length() >= 55, primitive.id());
        }

        String boundaries = NightmareFactionNegotiationResponseCatalog.waveOne().stream()
                .map(NightmareFactionNegotiationResponseCatalog.Primitive::antiOverclaimBoundary)
                .collect(Collectors.joining(" ")).toLowerCase();
        assertTrue(boundaries.contains("allegiance"));
        assertTrue(boundaries.contains("reputation"));
        assertTrue(boundaries.contains("truth"));
        assertTrue(boundaries.contains("scenario"));
        assertTrue(boundaries.contains("appraisal"));
        assertTrue(boundaries.contains("resource"));
    }

    @Test
    void composePreservesOpaqueAuthorityAndAllowedFamilyAcrossSeeds() {
        String scenarioId = "Scenario::Mictlan/CaseSensitive";
        String factionId = "Faction::NorthWatch/CaseSensitive";
        String pressureId = "Pressure::AlreadyResolved/CaseSensitive";
        String interactionStateId = "Interaction::Counteroffer/Open";
        Set<NightmareFactionNegotiationResponseCatalog.ResponseFamily> allowed =
                EnumSet.of(NightmareFactionNegotiationResponseCatalog.ResponseFamily.COUNTEROFFER);

        for (long seed = 0; seed < 4096; seed++) {
            var selection = NightmareFactionNegotiationResponseCatalog.compose(seed, scenarioId, factionId,
                    pressureId, interactionStateId, allowed, Map.of());
            assertEquals(scenarioId, selection.scenarioId());
            assertEquals(factionId, selection.factionId());
            assertEquals(pressureId, selection.pressureId());
            assertEquals(interactionStateId, selection.interactionStateId());
            assertEquals(allowed, selection.allowedFamilies());
            assertEquals(NightmareFactionNegotiationResponseCatalog.ResponseFamily.COUNTEROFFER,
                    selection.primitive().family());
        }
    }

    @Test
    void evidenceMapOrderAndPositiveMagnitudeDoNotChangeSelection() {
        Map<String, Integer> first = new LinkedHashMap<>();
        first.put("verification", 1);
        first.put("access", 1);

        Map<String, Integer> second = new LinkedHashMap<>();
        second.put("access", 999);
        second.put("verification", 999);

        Set<NightmareFactionNegotiationResponseCatalog.ResponseFamily> allowed =
                EnumSet.allOf(NightmareFactionNegotiationResponseCatalog.ResponseFamily.class);
        var left = NightmareFactionNegotiationResponseCatalog.compose(8123L, "Scenario::A", "Faction::B",
                "Pressure::C", "Interaction::D", allowed, first);
        var right = NightmareFactionNegotiationResponseCatalog.compose(8123L, "Scenario::A", "Faction::B",
                "Pressure::C", "Interaction::D", allowed, second);

        assertEquals(left.primitive(), right.primitive());
        assertEquals(left.presentationCue(), right.presentationCue());
        assertEquals(left.matchedEvidenceTags(), right.matchedEvidenceTags());
    }

    @Test
    void compatibleEvidenceCanPreferConditionalAccessWithoutMutatingAuthority() {
        Set<NightmareFactionNegotiationResponseCatalog.ResponseFamily> allowed =
                EnumSet.allOf(NightmareFactionNegotiationResponseCatalog.ResponseFamily.class);
        for (long seed = 0; seed < 512; seed++) {
            var selection = NightmareFactionNegotiationResponseCatalog.compose(seed,
                    "Scenario::Opaque", "Faction::Opaque", "Pressure::Opaque", "State::Opaque",
                    allowed, Map.of("conditional_access", 1));
            assertEquals(NightmareFactionNegotiationResponseCatalog.ResponseFamily.CONDITIONAL_ACCESS,
                    selection.primitive().family());
            assertEquals("Scenario::Opaque", selection.scenarioId());
            assertEquals("Faction::Opaque", selection.factionId());
            assertEquals("Pressure::Opaque", selection.pressureId());
            assertEquals("State::Opaque", selection.interactionStateId());
            assertTrue(selection.matchedEvidenceTags().contains("conditional_access"));
        }
    }

    @Test
    void neutralSweepReachesEveryResponseAndCuePair() {
        Set<NightmareFactionNegotiationResponseCatalog.ResponseFamily> allowed =
                EnumSet.allOf(NightmareFactionNegotiationResponseCatalog.ResponseFamily.class);
        Set<String> responseIds = new HashSet<>();
        Set<String> responseCuePairs = new HashSet<>();

        for (long seed = 0; seed < 16384; seed++) {
            var selection = NightmareFactionNegotiationResponseCatalog.compose(seed,
                    "Scenario::Neutral", "Faction::Neutral", "Pressure::Neutral", "State::Neutral",
                    allowed, Map.of());
            responseIds.add(selection.primitive().id());
            responseCuePairs.add(selection.primitive().id() + "|" + selection.presentationCue());
        }

        assertEquals(20, responseIds.size());
        assertEquals(40, responseCuePairs.size());
    }

    @Test
    void sameInputIsDeterministicAndFamilySetOrderDoesNotMatter() {
        List<NightmareFactionNegotiationResponseCatalog.ResponseFamily> values = new ArrayList<>(List.of(
                NightmareFactionNegotiationResponseCatalog.ResponseFamily.WARNING,
                NightmareFactionNegotiationResponseCatalog.ResponseFamily.COOPERATION,
                NightmareFactionNegotiationResponseCatalog.ResponseFamily.REFUSAL));
        Set<NightmareFactionNegotiationResponseCatalog.ResponseFamily> first = new HashSet<>(values);
        values.reversed();
        Set<NightmareFactionNegotiationResponseCatalog.ResponseFamily> second = new HashSet<>();
        second.add(NightmareFactionNegotiationResponseCatalog.ResponseFamily.REFUSAL);
        second.add(NightmareFactionNegotiationResponseCatalog.ResponseFamily.COOPERATION);
        second.add(NightmareFactionNegotiationResponseCatalog.ResponseFamily.WARNING);

        var left = NightmareFactionNegotiationResponseCatalog.compose(42L, "Scenario::A", "Faction::B",
                "Pressure::C", "State::D", first, Map.of("warning", 1));
        var right = NightmareFactionNegotiationResponseCatalog.compose(42L, "Scenario::A", "Faction::B",
                "Pressure::C", "State::D", second, Map.of("warning", 1));
        assertEquals(left, right);
    }

    @Test
    void malformedAuthorityAndEvidenceFailClosed() {
        Set<NightmareFactionNegotiationResponseCatalog.ResponseFamily> all =
                EnumSet.allOf(NightmareFactionNegotiationResponseCatalog.ResponseFamily.class);

        assertThrows(IllegalArgumentException.class, () -> NightmareFactionNegotiationResponseCatalog.compose(
                1L, " ", "Faction", "Pressure", "State", all, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionNegotiationResponseCatalog.compose(
                1L, "Scenario", " ", "Pressure", "State", all, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionNegotiationResponseCatalog.compose(
                1L, "Scenario", "Faction", " ", "State", all, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionNegotiationResponseCatalog.compose(
                1L, "Scenario", "Faction", "Pressure", " ", all, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionNegotiationResponseCatalog.compose(
                1L, "Scenario", "Faction", "Pressure", "State", Set.of(), Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionNegotiationResponseCatalog.compose(
                1L, "Scenario", "Faction", "Pressure", "State", all, Map.of("warning", -1)));
        assertThrows(IllegalArgumentException.class,
                () -> NightmareFactionNegotiationResponseCatalog.requirePrimitive("not_a_response"));
    }
}
