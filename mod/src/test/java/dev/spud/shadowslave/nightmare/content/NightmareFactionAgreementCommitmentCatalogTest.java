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

class NightmareFactionAgreementCommitmentCatalogTest {

    @Test
    void waveOneHasTwentyUniqueCommitmentsAndFourPerState() {
        List<NightmareFactionAgreementCommitmentCatalog.Primitive> primitives =
                NightmareFactionAgreementCommitmentCatalog.waveOne();
        assertEquals(20, primitives.size());
        assertEquals(20, primitives.stream().map(NightmareFactionAgreementCommitmentCatalog.Primitive::id)
                .collect(Collectors.toSet()).size());
        Map<NightmareFactionAgreementCommitmentCatalog.CommitmentState, Long> byState = primitives.stream()
                .collect(Collectors.groupingBy(NightmareFactionAgreementCommitmentCatalog.Primitive::state,
                        Collectors.counting()));
        for (NightmareFactionAgreementCommitmentCatalog.CommitmentState state
                : NightmareFactionAgreementCommitmentCatalog.CommitmentState.values()) {
            assertEquals(4L, byState.getOrDefault(state, 0L), state.name());
        }
    }

    @Test
    void everyCommitmentHasBoundedPlayerFacingContent() {
        for (var primitive : NightmareFactionAgreementCommitmentCatalog.waveOne()) {
            assertFalse(primitive.title().isBlank());
            assertTrue(primitive.statusRead().length() >= 40, primitive.id());
            assertTrue(primitive.playerPrompt().length() >= 30, primitive.id());
            assertEquals(3, primitive.playerResponses().size(), primitive.id());
            assertEquals(2, primitive.presentationCues().size(), primitive.id());
            assertFalse(primitive.affinityTags().isEmpty(), primitive.id());
            assertTrue(primitive.antiOverclaimBoundary().length() >= 60, primitive.id());
        }
        String boundaries = NightmareFactionAgreementCommitmentCatalog.waveOne().stream()
                .map(NightmareFactionAgreementCommitmentCatalog.Primitive::antiOverclaimBoundary)
                .collect(Collectors.joining(" ")).toLowerCase();
        assertTrue(boundaries.contains("allegiance"));
        assertTrue(boundaries.contains("reputation"));
        assertTrue(boundaries.contains("truth"));
        assertTrue(boundaries.contains("scenario"));
        assertTrue(boundaries.contains("appraisal"));
        assertTrue(boundaries.contains("resource"));
    }

    @Test
    void composePreservesOpaqueAuthorityAndExactJavaOwnedStateAcrossSeeds() {
        String scenarioId = "Scenario::Mictlan/CaseSensitive";
        String factionId = "Faction::NorthWatch/CaseSensitive";
        String agreementId = "Agreement::Terms/CaseSensitive";
        String commitmentId = "Commitment::One/CaseSensitive";
        for (NightmareFactionAgreementCommitmentCatalog.CommitmentState state
                : NightmareFactionAgreementCommitmentCatalog.CommitmentState.values()) {
            for (long seed = 0; seed < 4096; seed++) {
                var selection = NightmareFactionAgreementCommitmentCatalog.compose(seed, scenarioId, factionId,
                        agreementId, commitmentId, state, Map.of());
                assertEquals(scenarioId, selection.scenarioId());
                assertEquals(factionId, selection.factionId());
                assertEquals(agreementId, selection.agreementId());
                assertEquals(commitmentId, selection.commitmentId());
                assertEquals(state, selection.state());
                assertEquals(state, selection.primitive().state());
            }
        }
    }

    @Test
    void evidenceMapOrderAndPositiveMagnitudeDoNotChangeSelection() {
        Map<String, Integer> first = new LinkedHashMap<>();
        first.put("resource", 1);
        first.put("bargain", 1);
        Map<String, Integer> second = new LinkedHashMap<>();
        second.put("bargain", 999);
        second.put("resource", 999);
        var left = NightmareFactionAgreementCommitmentCatalog.compose(8123L, "Scenario::A", "Faction::B",
                "Agreement::C", "Commitment::D", NightmareFactionAgreementCommitmentCatalog.CommitmentState.ACCEPTED,
                first);
        var right = NightmareFactionAgreementCommitmentCatalog.compose(8123L, "Scenario::A", "Faction::B",
                "Agreement::C", "Commitment::D", NightmareFactionAgreementCommitmentCatalog.CommitmentState.ACCEPTED,
                second);
        assertEquals(left.primitive(), right.primitive());
        assertEquals(left.presentationCue(), right.presentationCue());
        assertEquals(left.matchedEvidenceTags(), right.matchedEvidenceTags());
    }

    @Test
    void compatibleEvidenceCanPreferResourcePrimitiveWithoutMutatingState() {
        for (long seed = 0; seed < 512; seed++) {
            var selection = NightmareFactionAgreementCommitmentCatalog.compose(seed,
                    "Scenario::Opaque", "Faction::Opaque", "Agreement::Opaque", "Commitment::Opaque",
                    NightmareFactionAgreementCommitmentCatalog.CommitmentState.ACCEPTED, Map.of("resource", 1));
            assertEquals("accepted_resource_commitment", selection.primitive().id());
            assertEquals(NightmareFactionAgreementCommitmentCatalog.CommitmentState.ACCEPTED, selection.state());
            assertEquals("Scenario::Opaque", selection.scenarioId());
            assertEquals("Faction::Opaque", selection.factionId());
            assertEquals("Agreement::Opaque", selection.agreementId());
            assertEquals("Commitment::Opaque", selection.commitmentId());
            assertTrue(selection.matchedEvidenceTags().contains("resource"));
        }
    }

    @Test
    void neutralSweepReachesEveryCommitmentAndCuePair() {
        Set<String> ids = new HashSet<>();
        Set<String> pairs = new HashSet<>();
        for (NightmareFactionAgreementCommitmentCatalog.CommitmentState state
                : EnumSet.allOf(NightmareFactionAgreementCommitmentCatalog.CommitmentState.class)) {
            for (long seed = 0; seed < 16384; seed++) {
                var selection = NightmareFactionAgreementCommitmentCatalog.compose(seed,
                        "Scenario::Neutral", "Faction::Neutral", "Agreement::Neutral", "Commitment::Neutral",
                        state, Map.of());
                ids.add(selection.primitive().id());
                pairs.add(selection.primitive().id() + "|" + selection.presentationCue());
            }
        }
        assertEquals(20, ids.size());
        assertEquals(40, pairs.size());
    }

    @Test
    void sameInputIsDeterministic() {
        var left = NightmareFactionAgreementCommitmentCatalog.compose(42L, "Scenario::A", "Faction::B",
                "Agreement::C", "Commitment::D", NightmareFactionAgreementCommitmentCatalog.CommitmentState.BROKEN,
                Map.of("information", 1));
        var right = NightmareFactionAgreementCommitmentCatalog.compose(42L, "Scenario::A", "Faction::B",
                "Agreement::C", "Commitment::D", NightmareFactionAgreementCommitmentCatalog.CommitmentState.BROKEN,
                Map.of("information", 1));
        assertEquals(left, right);
    }

    @Test
    void malformedAuthorityAndEvidenceFailClosed() {
        var state = NightmareFactionAgreementCommitmentCatalog.CommitmentState.PROPOSED;
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionAgreementCommitmentCatalog.compose(
                1L, " ", "Faction", "Agreement", "Commitment", state, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionAgreementCommitmentCatalog.compose(
                1L, "Scenario", " ", "Agreement", "Commitment", state, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionAgreementCommitmentCatalog.compose(
                1L, "Scenario", "Faction", " ", "Commitment", state, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionAgreementCommitmentCatalog.compose(
                1L, "Scenario", "Faction", "Agreement", " ", state, Map.of()));
        assertThrows(NullPointerException.class, () -> NightmareFactionAgreementCommitmentCatalog.compose(
                1L, "Scenario", "Faction", "Agreement", "Commitment", null, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> NightmareFactionAgreementCommitmentCatalog.compose(
                1L, "Scenario", "Faction", "Agreement", "Commitment", state, Map.of("resource", -1)));
        assertThrows(IllegalArgumentException.class,
                () -> NightmareFactionAgreementCommitmentCatalog.requirePrimitive("not_a_commitment"));
    }
}
