package dev.spud.shadowslave.nightmare.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class NightmareClueActionPlanCatalogTest {
    @Test
    void waveOneHasTwentyFourUniquePlansAndFourPerFamily() {
        var plans = NightmareClueActionPlanCatalog.waveOne();
        assertEquals(24, plans.size());
        assertEquals(24, plans.stream().map(NightmareClueActionPlanCatalog.Primitive::id).distinct().count());
        for (var family : NightmareClueActionPlanCatalog.Family.values()) {
            assertEquals(4, plans.stream().filter(plan -> plan.family() == family).count(), family.name());
        }
        for (var plan : plans) {
            assertEquals(3, plan.playerOptions().size());
            assertEquals(2, plan.presentationCues().size());
            assertFalse(plan.affinityTags().isEmpty());
            assertFalse(plan.antiOverclaimBoundary().isBlank());
        }
    }

    @Test
    void sameInputsAreDeterministicAndEvidenceMapOrderDoesNotMatter() {
        Map<String, Integer> first = new HashMap<>();
        first.put("route", 1);
        first.put("record", 2);
        Map<String, Integer> second = new HashMap<>();
        second.put("record", 2);
        second.put("route", 1);
        var families = EnumSet.allOf(NightmareClueActionPlanCatalog.Family.class);
        assertEquals(
                NightmareClueActionPlanCatalog.compose(81L, "scenario_a", "actor_a", "journal_a", families, first),
                NightmareClueActionPlanCatalog.compose(81L, "scenario_a", "actor_a", "journal_a", families, second));
    }

    @Test
    void positiveEvidenceMagnitudeDoesNotBecomeAWeightFormula() {
        var families = EnumSet.allOf(NightmareClueActionPlanCatalog.Family.class);
        var low = NightmareClueActionPlanCatalog.compose(19L, "scenario_a", "actor_a", "journal_a", families, Map.of("route", 1));
        var high = NightmareClueActionPlanCatalog.compose(19L, "scenario_a", "actor_a", "journal_a", families, Map.of("route", 999));
        assertEquals(low, high);
    }

    @Test
    void compatibleEvidencePrefersACompatiblePlanWithoutClaimingTruth() {
        var selection = NightmareClueActionPlanCatalog.compose(7L, "scenario_a", "actor_a", "journal_a",
                EnumSet.allOf(NightmareClueActionPlanCatalog.Family.class), Map.of("route", 1));
        assertTrue(selection.primitive().affinityTags().contains("route"));
        assertTrue(selection.matchedEvidenceTags().contains("route"));
        String boundary = selection.primitive().antiOverclaimBoundary().toLowerCase();
        assertTrue(boundary.contains("not") || boundary.contains("does not") || boundary.contains("cannot"));
    }

    @Test
    void seedCannotMutateCallerOwnedAuthorityOrEscapeAllowedFamilies() {
        Set<NightmareClueActionPlanCatalog.Family> allowed = EnumSet.of(
                NightmareClueActionPlanCatalog.Family.REVISIT,
                NightmareClueActionPlanCatalog.Family.DEFER);
        for (long seed = 0; seed < 4096; seed++) {
            var selection = NightmareClueActionPlanCatalog.compose(seed, "fixed_scenario", "fixed_actor", "fixed_journal", allowed, Map.of());
            assertEquals("fixed_scenario", selection.scenarioId());
            assertEquals("fixed_actor", selection.actorContextId());
            assertEquals("fixed_journal", selection.journalEntryId());
            assertTrue(allowed.contains(selection.primitive().family()));
        }
    }

    @Test
    void neutralSweepReachesEveryPlanAndPresentationCue() {
        Set<String> planIds = new HashSet<>();
        Set<String> pairs = new HashSet<>();
        var families = EnumSet.allOf(NightmareClueActionPlanCatalog.Family.class);
        for (long seed = 0; seed < 16384; seed++) {
            var selection = NightmareClueActionPlanCatalog.compose(seed, "scenario_a", "actor_a", "journal_a", families, Map.of());
            planIds.add(selection.primitive().id());
            pairs.add(selection.primitive().id() + "|" + selection.presentationCue());
        }
        assertEquals(24, planIds.size());
        assertEquals(48, pairs.size());
    }

    @Test
    void failClosedInputsDoNotInventPlanningAuthority() {
        var all = EnumSet.allOf(NightmareClueActionPlanCatalog.Family.class);
        assertThrows(IllegalArgumentException.class, () -> NightmareClueActionPlanCatalog.compose(1L, "", "actor", "journal", all, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareClueActionPlanCatalog.compose(1L, "scenario", "", "journal", all, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareClueActionPlanCatalog.compose(1L, "scenario", "actor", "", all, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareClueActionPlanCatalog.compose(1L, "scenario", "actor", "journal", Set.of(), Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareClueActionPlanCatalog.compose(1L, "scenario", "actor", "journal", all, Map.of("route", -1)));
        assertThrows(IllegalArgumentException.class, () -> NightmareClueActionPlanCatalog.byId("missing_plan"));
    }

    @Test
    void boundariesRejectTruthWorldMutationAndScenarioAuthorityOverclaims() {
        String all = NightmareClueActionPlanCatalog.waveOne().stream()
                .map(NightmareClueActionPlanCatalog.Primitive::antiOverclaimBoundary)
                .reduce("", (left, right) -> left + " " + right).toLowerCase();
        assertTrue(all.contains("truth"));
        assertTrue(all.contains("guilt"));
        assertTrue(all.contains("safe") || all.contains("safety"));
        assertTrue(all.contains("scenario"));
        assertTrue(all.contains("future") || all.contains("current"));
    }
}
