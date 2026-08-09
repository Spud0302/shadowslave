package dev.spud.shadowslave.nightmare.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class NightmareFactionAftermathHistoryCatalogTest {
    private static final String SCENARIO = "Scenario:Ash/Trial-A";
    private static final String FACTION = "Faction:North-Watch/A";
    private static final String INTERACTION = "Interaction:Terms/Step-7";
    private static final String HISTORY = "History:Record/A-19";

    @Test
    void waveOneHasExactCoverageAndPlayerFacingShape() {
        List<NightmareFactionAftermathHistoryCatalog.Primitive> primitives = NightmareFactionAftermathHistoryCatalog.waveOne();
        assertEquals(16, primitives.size());
        assertEquals(16, primitives.stream().map(NightmareFactionAftermathHistoryCatalog.Primitive::id).distinct().count());

        Map<NightmareFactionAftermathHistoryCatalog.HistoryKind, Integer> counts =
                new EnumMap<>(NightmareFactionAftermathHistoryCatalog.HistoryKind.class);
        for (NightmareFactionAftermathHistoryCatalog.Primitive primitive : primitives) {
            counts.merge(primitive.kind(), 1, Integer::sum);
            assertEquals(3, primitive.playerReflections().size());
            assertEquals(2, primitive.presentationCues().size());
            assertFalse(primitive.historyRead().isBlank());
            assertFalse(primitive.carryForwardPrompt().isBlank());
            assertTrue(primitive.antiOverclaimBoundary().length() > 60);
            assertPlayerFacing(primitive.title());
            assertPlayerFacing(primitive.historyRead());
            assertPlayerFacing(primitive.carryForwardPrompt());
            primitive.playerReflections().forEach(this::assertPlayerFacing);
            primitive.presentationCues().forEach(this::assertPlayerFacing);
        }
        for (NightmareFactionAftermathHistoryCatalog.HistoryKind kind : NightmareFactionAftermathHistoryCatalog.HistoryKind.values()) {
            assertEquals(4, counts.getOrDefault(kind, 0));
        }
    }

    @Test
    void opaqueAuthorityAndExactHistoryKindSurvive4096SeedsPerKind() {
        for (NightmareFactionAftermathHistoryCatalog.HistoryKind kind : NightmareFactionAftermathHistoryCatalog.HistoryKind.values()) {
            for (long seed = 0; seed < 4096; seed++) {
                NightmareFactionAftermathHistoryCatalog.Selection selection = NightmareFactionAftermathHistoryCatalog.compose(
                        seed, SCENARIO, FACTION, INTERACTION, HISTORY, kind, Map.of());
                assertEquals(SCENARIO, selection.scenarioId());
                assertEquals(FACTION, selection.factionId());
                assertEquals(INTERACTION, selection.interactionId());
                assertEquals(HISTORY, selection.historyEntryId());
                assertEquals(kind, selection.kind());
                assertEquals(kind, selection.primitive().kind());
                assertEquals(NightmareFactionAftermathHistoryCatalog.GENERATOR_VERSION, selection.generatorVersion());
            }
        }
    }

    @Test
    void evidenceOrderAndMagnitudeCannotChangeSelection() {
        LinkedHashMap<String, Integer> first = new LinkedHashMap<>();
        first.put("relationship", 1);
        first.put("uncertainty", 999);
        LinkedHashMap<String, Integer> second = new LinkedHashMap<>();
        second.put("uncertainty", 1);
        second.put("relationship", 999);

        for (long seed = 0; seed < 1024; seed++) {
            NightmareFactionAftermathHistoryCatalog.Selection a = NightmareFactionAftermathHistoryCatalog.compose(
                    seed, SCENARIO, FACTION, INTERACTION, HISTORY,
                    NightmareFactionAftermathHistoryCatalog.HistoryKind.UNRESOLVED, first);
            NightmareFactionAftermathHistoryCatalog.Selection b = NightmareFactionAftermathHistoryCatalog.compose(
                    seed, SCENARIO, FACTION, INTERACTION, HISTORY,
                    NightmareFactionAftermathHistoryCatalog.HistoryKind.UNRESOLVED, second);
            assertEquals(a.primitive().id(), b.primitive().id());
            assertEquals(a.presentationCue(), b.presentationCue());
            assertEquals(Set.of("relationship", "uncertainty"), a.matchedEvidenceTags());
            assertEquals(a.matchedEvidenceTags(), b.matchedEvidenceTags());
        }
    }

    @Test
    void compatibleEvidencePrefersTaggedContentWithoutMutatingAuthority() {
        for (long seed = 0; seed < 1024; seed++) {
            NightmareFactionAftermathHistoryCatalog.Selection selection = NightmareFactionAftermathHistoryCatalog.compose(
                    seed, SCENARIO, FACTION, INTERACTION, HISTORY,
                    NightmareFactionAftermathHistoryCatalog.HistoryKind.COOPERATED,
                    Map.of("access", 1, "route", 9));
            assertEquals("cooperated_access", selection.primitive().id());
            assertEquals(Set.of("access", "route"), selection.matchedEvidenceTags());
            assertEquals(SCENARIO, selection.scenarioId());
            assertEquals(FACTION, selection.factionId());
            assertEquals(INTERACTION, selection.interactionId());
            assertEquals(HISTORY, selection.historyEntryId());
        }
    }

    @Test
    void neutralSweepReachesEveryPrimitiveAndCuePair() {
        Set<String> primitives = new HashSet<>();
        Set<String> pairs = new HashSet<>();
        for (NightmareFactionAftermathHistoryCatalog.HistoryKind kind : NightmareFactionAftermathHistoryCatalog.HistoryKind.values()) {
            for (long seed = 0; seed < 16384; seed++) {
                NightmareFactionAftermathHistoryCatalog.Selection selection = NightmareFactionAftermathHistoryCatalog.compose(
                        seed, SCENARIO, FACTION, INTERACTION, HISTORY, kind, Map.of());
                primitives.add(selection.primitive().id());
                pairs.add(selection.primitive().id() + "|" + selection.presentationCue());
            }
        }
        assertEquals(16, primitives.size());
        assertEquals(32, pairs.size());
    }

    @Test
    void antiOverclaimCoverageNamesCriticalNonAuthorities() {
        String allBoundaries = NightmareFactionAftermathHistoryCatalog.waveOne().stream()
                .map(NightmareFactionAftermathHistoryCatalog.Primitive::antiOverclaimBoundary)
                .reduce("", (left, right) -> left + " " + right)
                .toLowerCase();
        for (String required : List.of("trust", "allegiance", "reputation", "truth", "future", "ownership")) {
            assertTrue(allBoundaries.contains(required), () -> "missing anti-overclaim topic: " + required);
        }
    }

    @Test
    void malformedInputsFailClosed() {
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionAftermathHistoryCatalog.compose(
                1, " ", FACTION, INTERACTION, HISTORY,
                NightmareFactionAftermathHistoryCatalog.HistoryKind.COOPERATED, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionAftermathHistoryCatalog.compose(
                1, SCENARIO, " ", INTERACTION, HISTORY,
                NightmareFactionAftermathHistoryCatalog.HistoryKind.COOPERATED, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionAftermathHistoryCatalog.compose(
                1, SCENARIO, FACTION, " ", HISTORY,
                NightmareFactionAftermathHistoryCatalog.HistoryKind.COOPERATED, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionAftermathHistoryCatalog.compose(
                1, SCENARIO, FACTION, INTERACTION, " ",
                NightmareFactionAftermathHistoryCatalog.HistoryKind.COOPERATED, Map.of()));
        assertThrows(NullPointerException.class, () -> NightmareFactionAftermathHistoryCatalog.compose(
                1, SCENARIO, FACTION, INTERACTION, HISTORY, null, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionAftermathHistoryCatalog.compose(
                1, SCENARIO, FACTION, INTERACTION, HISTORY,
                NightmareFactionAftermathHistoryCatalog.HistoryKind.COOPERATED, Map.of("cooperation", -1)));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionAftermathHistoryCatalog.requirePrimitive("unknown_history"));
    }

    private void assertPlayerFacing(String text) {
        String lower = text.toLowerCase();
        for (String backend : List.of("java", "caller-owned", "authoritative", "resolutiongraph")) {
            assertFalse(lower.contains(backend), () -> "backend term leaked into player-facing copy: " + backend + " in " + text);
        }
    }
}
