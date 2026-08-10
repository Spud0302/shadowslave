package dev.spud.shadowslave.nightmare.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class NightmareFactionCurrentIntentCheckCatalogTest {
    private static final String SCENARIO = "Scenario:Ash/Trial-A";
    private static final String FACTION = "Faction:North-Watch/A";
    private static final String CONTEXT = "ReEncounter:Context/Visit-2";
    private static final String INTERACTION = "Interaction:Intent/Now-3";

    @Test
    void waveOneHasExactCoverageAndPlayerFacingShape() {
        List<NightmareFactionCurrentIntentCheckCatalog.Primitive> primitives = NightmareFactionCurrentIntentCheckCatalog.waveOne();
        assertEquals(16, primitives.size());
        assertEquals(16, primitives.stream().map(NightmareFactionCurrentIntentCheckCatalog.Primitive::id).distinct().count());

        Map<NightmareFactionCurrentIntentCheckCatalog.CheckFamily, Integer> counts =
                new EnumMap<>(NightmareFactionCurrentIntentCheckCatalog.CheckFamily.class);
        for (NightmareFactionCurrentIntentCheckCatalog.Primitive primitive : primitives) {
            counts.merge(primitive.family(), 1, Integer::sum);
            assertEquals(3, primitive.followUpOptions().size());
            assertEquals(2, primitive.presentationCues().size());
            assertFalse(primitive.situationRead().isBlank());
            assertFalse(primitive.playerAsk().isBlank());
            assertTrue(primitive.antiOverclaimBoundary().length() > 60);
            assertPlayerFacing(primitive.title());
            assertPlayerFacing(primitive.situationRead());
            assertPlayerFacing(primitive.playerAsk());
            primitive.followUpOptions().forEach(this::assertPlayerFacing);
            primitive.presentationCues().forEach(this::assertPlayerFacing);
        }
        for (NightmareFactionCurrentIntentCheckCatalog.CheckFamily family : NightmareFactionCurrentIntentCheckCatalog.CheckFamily.values()) {
            assertEquals(4, counts.getOrDefault(family, 0));
        }
    }

    @Test
    void opaqueAuthorityAndAllowedFamilySurvive4096SeedsPerFamily() {
        for (NightmareFactionCurrentIntentCheckCatalog.CheckFamily family : NightmareFactionCurrentIntentCheckCatalog.CheckFamily.values()) {
            Set<NightmareFactionCurrentIntentCheckCatalog.CheckFamily> allowed = Set.of(family);
            for (long seed = 0; seed < 4096; seed++) {
                NightmareFactionCurrentIntentCheckCatalog.Selection selection = NightmareFactionCurrentIntentCheckCatalog.compose(
                        seed, SCENARIO, FACTION, CONTEXT, INTERACTION, allowed, Map.of());
                assertEquals(SCENARIO, selection.scenarioId());
                assertEquals(FACTION, selection.factionId());
                assertEquals(CONTEXT, selection.reencounterContextId());
                assertEquals(INTERACTION, selection.interactionId());
                assertEquals(allowed, selection.allowedFamilies());
                assertEquals(family, selection.primitive().family());
                assertEquals(NightmareFactionCurrentIntentCheckCatalog.GENERATOR_VERSION, selection.generatorVersion());
            }
        }
    }

    @Test
    void allowedFamilyOrderCannotChangeSelection() {
        LinkedHashSet<NightmareFactionCurrentIntentCheckCatalog.CheckFamily> first = new LinkedHashSet<>();
        first.add(NightmareFactionCurrentIntentCheckCatalog.CheckFamily.REVISIT_OPEN_MATTER);
        first.add(NightmareFactionCurrentIntentCheckCatalog.CheckFamily.CLARIFY_PRESENT_GOAL);
        LinkedHashSet<NightmareFactionCurrentIntentCheckCatalog.CheckFamily> second = new LinkedHashSet<>();
        second.add(NightmareFactionCurrentIntentCheckCatalog.CheckFamily.CLARIFY_PRESENT_GOAL);
        second.add(NightmareFactionCurrentIntentCheckCatalog.CheckFamily.REVISIT_OPEN_MATTER);

        for (long seed = 0; seed < 1024; seed++) {
            NightmareFactionCurrentIntentCheckCatalog.Selection a = NightmareFactionCurrentIntentCheckCatalog.compose(
                    seed, SCENARIO, FACTION, CONTEXT, INTERACTION, first, Map.of());
            NightmareFactionCurrentIntentCheckCatalog.Selection b = NightmareFactionCurrentIntentCheckCatalog.compose(
                    seed, SCENARIO, FACTION, CONTEXT, INTERACTION, second, Map.of());
            assertEquals(a.primitive().id(), b.primitive().id());
            assertEquals(a.presentationCue(), b.presentationCue());
        }
    }

    @Test
    void evidenceOrderAndMagnitudeCannotChangeSelection() {
        LinkedHashMap<String, Integer> first = new LinkedHashMap<>();
        first.put("goal", 1);
        first.put("current", 999);
        LinkedHashMap<String, Integer> second = new LinkedHashMap<>();
        second.put("current", 1);
        second.put("goal", 999);

        Set<NightmareFactionCurrentIntentCheckCatalog.CheckFamily> allowed =
                Set.of(NightmareFactionCurrentIntentCheckCatalog.CheckFamily.CLARIFY_PRESENT_GOAL);
        for (long seed = 0; seed < 1024; seed++) {
            NightmareFactionCurrentIntentCheckCatalog.Selection a = NightmareFactionCurrentIntentCheckCatalog.compose(
                    seed, SCENARIO, FACTION, CONTEXT, INTERACTION, allowed, first);
            NightmareFactionCurrentIntentCheckCatalog.Selection b = NightmareFactionCurrentIntentCheckCatalog.compose(
                    seed, SCENARIO, FACTION, CONTEXT, INTERACTION, allowed, second);
            assertEquals(a.primitive().id(), b.primitive().id());
            assertEquals(a.presentationCue(), b.presentationCue());
            assertEquals(a.matchedEvidenceTags(), b.matchedEvidenceTags());
        }
    }

    @Test
    void compatibleEvidencePrefersTaggedContentWithoutMutatingAuthority() {
        Set<NightmareFactionCurrentIntentCheckCatalog.CheckFamily> allowed =
                Set.of(NightmareFactionCurrentIntentCheckCatalog.CheckFamily.CLARIFY_PRESENT_GOAL);
        for (long seed = 0; seed < 1024; seed++) {
            NightmareFactionCurrentIntentCheckCatalog.Selection selection = NightmareFactionCurrentIntentCheckCatalog.compose(
                    seed, SCENARIO, FACTION, CONTEXT, INTERACTION, allowed,
                    Map.of("reason", 1, "evidence", 999));
            assertEquals("goal_reason_without_truth", selection.primitive().id());
            assertEquals(Set.of("reason", "evidence"), selection.matchedEvidenceTags());
            assertEquals(SCENARIO, selection.scenarioId());
            assertEquals(FACTION, selection.factionId());
            assertEquals(CONTEXT, selection.reencounterContextId());
            assertEquals(INTERACTION, selection.interactionId());
        }
    }

    @Test
    void neutralSweepReachesEveryPrimitiveAndCuePair() {
        Set<String> primitives = new HashSet<>();
        Set<String> pairs = new HashSet<>();
        Set<NightmareFactionCurrentIntentCheckCatalog.CheckFamily> allFamilies =
                Set.of(NightmareFactionCurrentIntentCheckCatalog.CheckFamily.values());
        for (long seed = 0; seed < 16384; seed++) {
            NightmareFactionCurrentIntentCheckCatalog.Selection selection = NightmareFactionCurrentIntentCheckCatalog.compose(
                    seed, SCENARIO, FACTION, CONTEXT, INTERACTION, allFamilies, Map.of());
            primitives.add(selection.primitive().id());
            pairs.add(selection.primitive().id() + "|" + selection.presentationCue());
        }
        assertEquals(16, primitives.size());
        assertEquals(32, pairs.size());
    }

    @Test
    void antiOverclaimCoverageNamesCriticalNonAuthorities() {
        String allBoundaries = NightmareFactionCurrentIntentCheckCatalog.waveOne().stream()
                .map(NightmareFactionCurrentIntentCheckCatalog.Primitive::antiOverclaimBoundary)
                .reduce("", (left, right) -> left + " " + right)
                .toLowerCase();
        for (String required : List.of("truth", "trust", "allegiance", "reputation", "access", "future", "scenario")) {
            assertTrue(allBoundaries.contains(required), () -> "missing anti-overclaim topic: " + required);
        }
    }

    @Test
    void malformedInputsFailClosed() {
        Set<NightmareFactionCurrentIntentCheckCatalog.CheckFamily> allowed =
                Set.of(NightmareFactionCurrentIntentCheckCatalog.CheckFamily.CLARIFY_PRESENT_GOAL);
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionCurrentIntentCheckCatalog.compose(
                1, " ", FACTION, CONTEXT, INTERACTION, allowed, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionCurrentIntentCheckCatalog.compose(
                1, SCENARIO, " ", CONTEXT, INTERACTION, allowed, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionCurrentIntentCheckCatalog.compose(
                1, SCENARIO, FACTION, " ", INTERACTION, allowed, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionCurrentIntentCheckCatalog.compose(
                1, SCENARIO, FACTION, CONTEXT, " ", allowed, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionCurrentIntentCheckCatalog.compose(
                1, SCENARIO, FACTION, CONTEXT, INTERACTION, Set.of(), Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionCurrentIntentCheckCatalog.compose(
                1, SCENARIO, FACTION, CONTEXT, INTERACTION, allowed, Map.of("goal", -1)));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionCurrentIntentCheckCatalog.requirePrimitive("unknown_check"));
    }

    private void assertPlayerFacing(String text) {
        String lower = text.toLowerCase();
        for (String backend : List.of("java", "caller-owned", "authoritative", "resolutiongraph")) {
            assertFalse(lower.contains(backend), () -> "backend term leaked into player-facing copy: " + backend + " in " + text);
        }
    }
}
