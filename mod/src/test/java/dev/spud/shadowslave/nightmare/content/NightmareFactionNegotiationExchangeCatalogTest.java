package dev.spud.shadowslave.nightmare.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class NightmareFactionNegotiationExchangeCatalogTest {

    @Test
    void waveOneHasTwentyUniqueExchangesAndFourPerFamily() {
        List<NightmareFactionNegotiationExchangeCatalog.Primitive> primitives =
                NightmareFactionNegotiationExchangeCatalog.waveOne();

        assertEquals(20, primitives.size());
        assertEquals(20, primitives.stream().map(NightmareFactionNegotiationExchangeCatalog.Primitive::id)
                .collect(Collectors.toSet()).size());

        Map<NightmareFactionNegotiationExchangeCatalog.ExchangeFamily, Long> byFamily = primitives.stream()
                .collect(Collectors.groupingBy(NightmareFactionNegotiationExchangeCatalog.Primitive::family,
                        Collectors.counting()));
        for (NightmareFactionNegotiationExchangeCatalog.ExchangeFamily family
                : NightmareFactionNegotiationExchangeCatalog.ExchangeFamily.values()) {
            assertEquals(4L, byFamily.getOrDefault(family, 0L), family.name());
        }
    }

    @Test
    void everyExchangeHasBoundedPlayerFacingContent() {
        for (NightmareFactionNegotiationExchangeCatalog.Primitive primitive
                : NightmareFactionNegotiationExchangeCatalog.waveOne()) {
            assertFalse(primitive.title().isBlank());
            assertTrue(primitive.exchangeRead().length() >= 40, primitive.id());
            assertTrue(primitive.factionLine().length() >= 15, primitive.id());
            assertEquals(3, primitive.playerOptions().size(), primitive.id());
            assertEquals(2, primitive.presentationCues().size(), primitive.id());
            assertFalse(primitive.affinityTags().isEmpty(), primitive.id());
            assertTrue(primitive.antiOverclaimBoundary().length() >= 55, primitive.id());
        }

        String boundaries = NightmareFactionNegotiationExchangeCatalog.waveOne().stream()
                .map(NightmareFactionNegotiationExchangeCatalog.Primitive::antiOverclaimBoundary)
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
        String responseId = "Response::AlreadyResolved/CaseSensitive";
        String interactionStateId = "Interaction::Counter/Open";
        Set<NightmareFactionNegotiationExchangeCatalog.ExchangeFamily> allowed =
                EnumSet.of(NightmareFactionNegotiationExchangeCatalog.ExchangeFamily.COUNTER);

        for (long seed = 0; seed < 4096; seed++) {
            var selection = NightmareFactionNegotiationExchangeCatalog.compose(seed, scenarioId, factionId,
                    responseId, interactionStateId, allowed, Map.of());
            assertEquals(scenarioId, selection.scenarioId());
            assertEquals(factionId, selection.factionId());
            assertEquals(responseId, selection.responseId());
            assertEquals(interactionStateId, selection.interactionStateId());
            assertEquals(allowed, selection.allowedFamilies());
            assertEquals(NightmareFactionNegotiationExchangeCatalog.ExchangeFamily.COUNTER,
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

        Set<NightmareFactionNegotiationExchangeCatalog.ExchangeFamily> allowed =
                EnumSet.allOf(NightmareFactionNegotiationExchangeCatalog.ExchangeFamily.class);
        var left = NightmareFactionNegotiationExchangeCatalog.compose(8123L, "Scenario::A", "Faction::B",
                "Response::C", "Interaction::D", allowed, first);
        var right = NightmareFactionNegotiationExchangeCatalog.compose(8123L, "Scenario::A", "Faction::B",
                "Response::C", "Interaction::D", allowed, second);

        assertEquals(left.primitive(), right.primitive());
        assertEquals(left.presentationCue(), right.presentationCue());
        assertEquals(left.matchedEvidenceTags(), right.matchedEvidenceTags());
    }

    @Test
    void compatibleEvidenceCanPreferVerificationWithoutMutatingAuthority() {
        Set<NightmareFactionNegotiationExchangeCatalog.ExchangeFamily> allowed =
                EnumSet.allOf(NightmareFactionNegotiationExchangeCatalog.ExchangeFamily.class);
        for (long seed = 0; seed < 512; seed++) {
            var selection = NightmareFactionNegotiationExchangeCatalog.compose(seed,
                    "Scenario::Opaque", "Faction::Opaque", "Response::Opaque", "State::Opaque",
                    allowed, Map.of("verify", 1));
            assertEquals(NightmareFactionNegotiationExchangeCatalog.ExchangeFamily.VERIFY,
                    selection.primitive().family());
            assertEquals("Scenario::Opaque", selection.scenarioId());
            assertEquals("Faction::Opaque", selection.factionId());
            assertEquals("Response::Opaque", selection.responseId());
            assertEquals("State::Opaque", selection.interactionStateId());
            assertTrue(selection.matchedEvidenceTags().contains("verify"));
        }
    }

    @Test
    void neutralSweepReachesEveryExchangeAndCuePair() {
        Set<NightmareFactionNegotiationExchangeCatalog.ExchangeFamily> allowed =
                EnumSet.allOf(NightmareFactionNegotiationExchangeCatalog.ExchangeFamily.class);
        Set<String> exchangeIds = new HashSet<>();
        Set<String> exchangeCuePairs = new HashSet<>();

        for (long seed = 0; seed < 16384; seed++) {
            var selection = NightmareFactionNegotiationExchangeCatalog.compose(seed,
                    "Scenario::Neutral", "Faction::Neutral", "Response::Neutral", "State::Neutral",
                    allowed, Map.of());
            exchangeIds.add(selection.primitive().id());
            exchangeCuePairs.add(selection.primitive().id() + "|" + selection.presentationCue());
        }

        assertEquals(20, exchangeIds.size());
        assertEquals(40, exchangeCuePairs.size());
    }

    @Test
    void sameInputIsDeterministicAndFamilySetOrderDoesNotMatter() {
        Set<NightmareFactionNegotiationExchangeCatalog.ExchangeFamily> first = new HashSet<>();
        first.add(NightmareFactionNegotiationExchangeCatalog.ExchangeFamily.VERIFY);
        first.add(NightmareFactionNegotiationExchangeCatalog.ExchangeFamily.ACCEPT);
        first.add(NightmareFactionNegotiationExchangeCatalog.ExchangeFamily.DISENGAGE);
        Set<NightmareFactionNegotiationExchangeCatalog.ExchangeFamily> second = new HashSet<>();
        second.add(NightmareFactionNegotiationExchangeCatalog.ExchangeFamily.DISENGAGE);
        second.add(NightmareFactionNegotiationExchangeCatalog.ExchangeFamily.ACCEPT);
        second.add(NightmareFactionNegotiationExchangeCatalog.ExchangeFamily.VERIFY);

        var left = NightmareFactionNegotiationExchangeCatalog.compose(42L, "Scenario::A", "Faction::B",
                "Response::C", "State::D", first, Map.of("verify", 1));
        var right = NightmareFactionNegotiationExchangeCatalog.compose(42L, "Scenario::A", "Faction::B",
                "Response::C", "State::D", second, Map.of("verify", 1));
        assertEquals(left, right);
    }

    @Test
    void malformedAuthorityAndEvidenceFailClosed() {
        Set<NightmareFactionNegotiationExchangeCatalog.ExchangeFamily> all =
                EnumSet.allOf(NightmareFactionNegotiationExchangeCatalog.ExchangeFamily.class);

        assertThrows(IllegalArgumentException.class, () -> NightmareFactionNegotiationExchangeCatalog.compose(
                1L, " ", "Faction", "Response", "State", all, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionNegotiationExchangeCatalog.compose(
                1L, "Scenario", " ", "Response", "State", all, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionNegotiationExchangeCatalog.compose(
                1L, "Scenario", "Faction", " ", "State", all, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionNegotiationExchangeCatalog.compose(
                1L, "Scenario", "Faction", "Response", " ", all, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionNegotiationExchangeCatalog.compose(
                1L, "Scenario", "Faction", "Response", "State", Set.of(), Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionNegotiationExchangeCatalog.compose(
                1L, "Scenario", "Faction", "Response", "State", all, Map.of("verify", -1)));
        assertThrows(IllegalArgumentException.class,
                () -> NightmareFactionNegotiationExchangeCatalog.requirePrimitive("not_an_exchange"));
    }
}
