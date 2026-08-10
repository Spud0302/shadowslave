package dev.spud.shadowslave.nightmare.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class NightmareFactionReencounterContextCatalogTest {
    private static final String SCENARIO = "Scenario:Ash/Trial-A";
    private static final String FACTION = "Faction:North-Watch/A";
    private static final String HISTORY = "History:Record/A-19";
    private static final String REENCOUNTER = "ReEncounter:Gate/Visit-2";

    @Test
    void waveOneHasExactCoverageAndPlayerFacingShape() {
        List<NightmareFactionReencounterContextCatalog.Primitive> primitives = NightmareFactionReencounterContextCatalog.waveOne();
        assertEquals(16, primitives.size());
        assertEquals(16, primitives.stream().map(NightmareFactionReencounterContextCatalog.Primitive::id).distinct().count());

        Map<NightmareFactionReencounterContextCatalog.ContextKind, Integer> counts =
                new EnumMap<>(NightmareFactionReencounterContextCatalog.ContextKind.class);
        for (NightmareFactionReencounterContextCatalog.Primitive primitive : primitives) {
            counts.merge(primitive.kind(), 1, Integer::sum);
            assertEquals(3, primitive.playerResponses().size());
            assertEquals(2, primitive.presentationCues().size());
            assertFalse(primitive.contextRead().isBlank());
            assertFalse(primitive.conversationPrompt().isBlank());
            assertTrue(primitive.antiOverclaimBoundary().length() > 60);
            assertPlayerFacing(primitive.title());
            assertPlayerFacing(primitive.contextRead());
            assertPlayerFacing(primitive.conversationPrompt());
            primitive.playerResponses().forEach(this::assertPlayerFacing);
            primitive.presentationCues().forEach(this::assertPlayerFacing);
        }
        for (NightmareFactionReencounterContextCatalog.ContextKind kind : NightmareFactionReencounterContextCatalog.ContextKind.values()) {
            assertEquals(4, counts.getOrDefault(kind, 0));
        }
    }

    @Test
    void opaqueAuthorityAndExactContextKindSurvive4096SeedsPerKind() {
        for (NightmareFactionReencounterContextCatalog.ContextKind kind : NightmareFactionReencounterContextCatalog.ContextKind.values()) {
            for (long seed = 0; seed < 4096; seed++) {
                NightmareFactionReencounterContextCatalog.Selection selection = NightmareFactionReencounterContextCatalog.compose(
                        seed, SCENARIO, FACTION, HISTORY, REENCOUNTER, kind, Map.of());
                assertEquals(SCENARIO, selection.scenarioId());
                assertEquals(FACTION, selection.factionId());
                assertEquals(HISTORY, selection.priorHistoryId());
                assertEquals(REENCOUNTER, selection.reencounterId());
                assertEquals(kind, selection.kind());
                assertEquals(kind, selection.primitive().kind());
                assertEquals(NightmareFactionReencounterContextCatalog.GENERATOR_VERSION, selection.generatorVersion());
            }
        }
    }

    @Test
    void evidenceOrderAndMagnitudeCannotChangeSelection() {
        LinkedHashMap<String, Integer> first = new LinkedHashMap<>();
        first.put("terms", 1);
        first.put("agreement", 999);
        LinkedHashMap<String, Integer> second = new LinkedHashMap<>();
        second.put("agreement", 1);
        second.put("terms", 999);

        for (long seed = 0; seed < 1024; seed++) {
            NightmareFactionReencounterContextCatalog.Selection a = NightmareFactionReencounterContextCatalog.compose(
                    seed, SCENARIO, FACTION, HISTORY, REENCOUNTER,
                    NightmareFactionReencounterContextCatalog.ContextKind.OPEN_BUSINESS, first);
            NightmareFactionReencounterContextCatalog.Selection b = NightmareFactionReencounterContextCatalog.compose(
                    seed, SCENARIO, FACTION, HISTORY, REENCOUNTER,
                    NightmareFactionReencounterContextCatalog.ContextKind.OPEN_BUSINESS, second);
            assertEquals(a.primitive().id(), b.primitive().id());
            assertEquals(a.presentationCue(), b.presentationCue());
            assertEquals(Set.of("agreement", "terms"), a.matchedEvidenceTags());
            assertEquals(a.matchedEvidenceTags(), b.matchedEvidenceTags());
        }
    }

    @Test
    void compatibleEvidencePrefersTaggedContentWithoutMutatingAuthority() {
        for (long seed = 0; seed < 1024; seed++) {
            NightmareFactionReencounterContextCatalog.Selection selection = NightmareFactionReencounterContextCatalog.compose(
                    seed, SCENARIO, FACTION, HISTORY, REENCOUNTER,
                    NightmareFactionReencounterContextCatalog.ContextKind.OPEN_BUSINESS,
                    Map.of("agreement", 1, "terms", 9));
            assertEquals("open_business_unfinished_terms", selection.primitive().id());
            assertEquals(Set.of("agreement", "terms"), selection.matchedEvidenceTags());
            assertEquals(SCENARIO, selection.scenarioId());
            assertEquals(FACTION, selection.factionId());
            assertEquals(HISTORY, selection.priorHistoryId());
            assertEquals(REENCOUNTER, selection.reencounterId());
        }
    }

    @Test
    void neutralSweepReachesEveryPrimitiveAndCuePair() {
        Set<String> primitives = new HashSet<>();
        Set<String> pairs = new HashSet<>();
        for (NightmareFactionReencounterContextCatalog.ContextKind kind : NightmareFactionReencounterContextCatalog.ContextKind.values()) {
            for (long seed = 0; seed < 16384; seed++) {
                NightmareFactionReencounterContextCatalog.Selection selection = NightmareFactionReencounterContextCatalog.compose(
                        seed, SCENARIO, FACTION, HISTORY, REENCOUNTER, kind, Map.of());
                primitives.add(selection.primitive().id());
                pairs.add(selection.primitive().id() + "|" + selection.presentationCue());
            }
        }
        assertEquals(16, primitives.size());
        assertEquals(32, pairs.size());
    }

    @Test
    void antiOverclaimCoverageNamesCriticalNonAuthorities() {
        String allBoundaries = NightmareFactionReencounterContextCatalog.waveOne().stream()
                .map(NightmareFactionReencounterContextCatalog.Primitive::antiOverclaimBoundary)
                .reduce("", (left, right) -> left + " " + right)
                .toLowerCase();
        for (String required : List.of("trust", "allegiance", "reputation", "truth", "access", "commitment", "future")) {
            assertTrue(allBoundaries.contains(required), () -> "missing anti-overclaim topic: " + required);
        }
    }

    @Test
    void malformedInputsFailClosed() {
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionReencounterContextCatalog.compose(
                1, " ", FACTION, HISTORY, REENCOUNTER,
                NightmareFactionReencounterContextCatalog.ContextKind.KNOWN_HISTORY, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionReencounterContextCatalog.compose(
                1, SCENARIO, " ", HISTORY, REENCOUNTER,
                NightmareFactionReencounterContextCatalog.ContextKind.KNOWN_HISTORY, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionReencounterContextCatalog.compose(
                1, SCENARIO, FACTION, " ", REENCOUNTER,
                NightmareFactionReencounterContextCatalog.ContextKind.KNOWN_HISTORY, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionReencounterContextCatalog.compose(
                1, SCENARIO, FACTION, HISTORY, " ",
                NightmareFactionReencounterContextCatalog.ContextKind.KNOWN_HISTORY, Map.of()));
        assertThrows(NullPointerException.class, () -> NightmareFactionReencounterContextCatalog.compose(
                1, SCENARIO, FACTION, HISTORY, REENCOUNTER, null, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionReencounterContextCatalog.compose(
                1, SCENARIO, FACTION, HISTORY, REENCOUNTER,
                NightmareFactionReencounterContextCatalog.ContextKind.KNOWN_HISTORY, Map.of("history", -1)));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionReencounterContextCatalog.requirePrimitive("unknown_context"));
    }

    private void assertPlayerFacing(String text) {
        String lower = text.toLowerCase();
        for (String backend : List.of("java", "caller-owned", "authoritative", "resolutiongraph")) {
            assertFalse(lower.contains(backend), () -> "backend term leaked into player-facing copy: " + backend + " in " + text);
        }
    }
}
