package dev.spud.shadowslave.nightmare.content;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
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

class NightmareNpcConversationExchangeCatalogTest {
    @Test
    void waveOneHasFourSubstantivePrimitivesPerFamily() {
        List<NightmareNpcConversationExchangeCatalog.ExchangePrimitive> primitives =
                NightmareNpcConversationExchangeCatalog.waveOne();

        assertEquals(20, primitives.size());
        assertEquals(20, primitives.stream().map(NightmareNpcConversationExchangeCatalog.ExchangePrimitive::id).distinct().count());

        Map<NightmareNpcConversationExchangeCatalog.ExchangeFamily, Integer> counts =
                new EnumMap<>(NightmareNpcConversationExchangeCatalog.ExchangeFamily.class);
        for (NightmareNpcConversationExchangeCatalog.ExchangePrimitive primitive : primitives) {
            counts.merge(primitive.family(), 1, Integer::sum);
            assertFalse(primitive.title().isBlank());
            assertFalse(primitive.exchangeRead().isBlank());
            assertFalse(primitive.npcLine().isBlank());
            assertEquals(3, primitive.playerOptions().size());
            assertEquals(2, primitive.presentationCues().size());
            assertFalse(primitive.affinityTags().isEmpty());
            assertTrue(primitive.boundary().length() >= 45);
        }

        for (NightmareNpcConversationExchangeCatalog.ExchangeFamily family :
                NightmareNpcConversationExchangeCatalog.ExchangeFamily.values()) {
            assertEquals(4, counts.getOrDefault(family, 0));
        }
    }

    @Test
    void sameInputIsDeterministicAndMapOrderIndependent() {
        Map<String, Integer> first = new HashMap<>();
        first.put("testimony", 1);
        first.put("verification", 1);
        Map<String, Integer> second = new HashMap<>();
        second.put("verification", 1);
        second.put("testimony", 1);

        var a = NightmareNpcConversationExchangeCatalog.compose(
                77L,
                "hollow_treaty",
                "hostage_interpreter",
                EnumSet.allOf(NightmareNpcConversationExchangeCatalog.ExchangeFamily.class),
                first
        );
        var b = NightmareNpcConversationExchangeCatalog.compose(
                77L,
                "hollow_treaty",
                "hostage_interpreter",
                EnumSet.allOf(NightmareNpcConversationExchangeCatalog.ExchangeFamily.class),
                second
        );

        assertEquals(a, b);
    }

    @Test
    void positiveEvidenceMagnitudeDoesNotBecomeAHiddenScoringFormula() {
        var low = NightmareNpcConversationExchangeCatalog.compose(
                18L,
                "lantern_below",
                "survey_clerk_assistant",
                EnumSet.allOf(NightmareNpcConversationExchangeCatalog.ExchangeFamily.class),
                Map.of("evidence", 1, "testimony", 1)
        );
        var high = NightmareNpcConversationExchangeCatalog.compose(
                18L,
                "lantern_below",
                "survey_clerk_assistant",
                EnumSet.allOf(NightmareNpcConversationExchangeCatalog.ExchangeFamily.class),
                Map.of("evidence", 999, "testimony", 999)
        );

        assertEquals(low, high);
    }

    @Test
    void seedCannotChangeCallerOwnedScenarioActorOrAllowedFamily() {
        Set<NightmareNpcConversationExchangeCatalog.ExchangeFamily> allowed =
                EnumSet.of(NightmareNpcConversationExchangeCatalog.ExchangeFamily.VERIFICATION);

        for (long seed = 0; seed < 4096; seed++) {
            var result = NightmareNpcConversationExchangeCatalog.compose(
                    seed,
                    "falling_span",
                    "span_ward_runner",
                    allowed,
                    Map.of()
            );
            assertEquals("falling_span", result.scenarioId());
            assertEquals("span_ward_runner", result.actorContextId());
            assertEquals(NightmareNpcConversationExchangeCatalog.ExchangeFamily.VERIFICATION, result.family());
            assertEquals(NightmareNpcConversationExchangeCatalog.GENERATOR_VERSION, result.generatorVersion());
        }
    }

    @Test
    void compatibleEvidenceCanPreferMatchingAuthoredContentWithoutTruthInference() {
        var result = NightmareNpcConversationExchangeCatalog.compose(
                4L,
                "drowned_bell",
                "cistern_keeper",
                EnumSet.allOf(NightmareNpcConversationExchangeCatalog.ExchangeFamily.class),
                Map.of("authority", 1)
        );

        assertTrue(result.matchedEvidenceTags().contains("authority"));
        assertEquals("counter_question_authority", result.primitiveId());
        assertTrue(result.boundary().toLowerCase().contains("authority"));
    }

    @Test
    void neutralSweepReachesEveryPrimitiveAndCuePair() {
        Set<String> primitiveIds = new HashSet<>();
        Set<String> primitiveCuePairs = new HashSet<>();

        for (long seed = 0; seed < 16384; seed++) {
            var result = NightmareNpcConversationExchangeCatalog.compose(
                    seed,
                    "generic_authored_nightmare",
                    "resolved_historical_actor",
                    EnumSet.allOf(NightmareNpcConversationExchangeCatalog.ExchangeFamily.class),
                    Map.of()
            );
            primitiveIds.add(result.primitiveId());
            primitiveCuePairs.add(result.primitiveId() + "|" + result.presentationCue());
        }

        assertEquals(20, primitiveIds.size());
        assertEquals(40, primitiveCuePairs.size());
    }

    @Test
    void explicitBoundariesRejectCommonSocialOverclaims() {
        String allBoundaries = NightmareNpcConversationExchangeCatalog.waveOne().stream()
                .map(NightmareNpcConversationExchangeCatalog.ExchangePrimitive::boundary)
                .map(String::toLowerCase)
                .reduce("", (left, right) -> left + " " + right);

        assertTrue(allBoundaries.contains("truth"));
        assertTrue(allBoundaries.contains("guilt"));
        assertTrue(allBoundaries.contains("allegiance"));
        assertTrue(allBoundaries.contains("persuasion"));
        assertTrue(allBoundaries.contains("scenario"));
        assertTrue(allBoundaries.contains("canonical"));
    }

    @Test
    void invalidAuthorityInputsFailClosed() {
        assertThrows(IllegalArgumentException.class, () -> NightmareNpcConversationExchangeCatalog.compose(
                1L,
                " ",
                "actor",
                EnumSet.of(NightmareNpcConversationExchangeCatalog.ExchangeFamily.QUESTION),
                Map.of()
        ));
        assertThrows(IllegalArgumentException.class, () -> NightmareNpcConversationExchangeCatalog.compose(
                1L,
                "scenario",
                " ",
                EnumSet.of(NightmareNpcConversationExchangeCatalog.ExchangeFamily.QUESTION),
                Map.of()
        ));
        assertThrows(IllegalArgumentException.class, () -> NightmareNpcConversationExchangeCatalog.compose(
                1L,
                "scenario",
                "actor",
                Set.of(),
                Map.of()
        ));
        assertThrows(IllegalArgumentException.class, () -> NightmareNpcConversationExchangeCatalog.compose(
                1L,
                "scenario",
                "actor",
                EnumSet.of(NightmareNpcConversationExchangeCatalog.ExchangeFamily.QUESTION),
                Map.of("testimony", -1)
        ));
    }
}
