package dev.spud.shadowslave.nightmare.content;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightmareRoleContentCatalogTest {
    @Test
    void waveOneProvidesBroadAuthoredHistoricalRoles() {
        List<NightmareRoleContentCatalog.RoleProfile> roles = NightmareRoleContentCatalog.waveOne();

        assertEquals(12, roles.size());
        Set<String> ids = new HashSet<>();
        Set<NightmareRoleContentCatalog.SocialPosition> positions = new HashSet<>();
        Set<NightmareRoleContentCatalog.Condition> conditions = new HashSet<>();
        Set<NightmareRoleContentCatalog.Relationship> relationships = new HashSet<>();
        Set<NightmareRoleContentCatalog.Knowledge> knowledge = new HashSet<>();
        Set<NightmareRoleContentCatalog.AlignmentPressure> pressures = new HashSet<>();

        roles.forEach(role -> {
            assertTrue(ids.add(role.id()), () -> "duplicate id " + role.id());
            positions.add(role.socialPosition());
            conditions.addAll(role.conditions());
            relationships.addAll(role.relationships());
            knowledge.addAll(role.knowledge());
            pressures.addAll(role.pressures());
            assertFalse(role.obligations().isEmpty());
            assertFalse(role.evidenceAffinities().isEmpty());
            assertFalse(role.startingConstraint().isBlank());
            assertFalse(role.hiddenOpportunity().isBlank());
            assertFalse(role.presentationCue().isBlank());
        });

        assertEquals(Set.of(
                NightmareRoleContentCatalog.SocialPosition.BOUND,
                NightmareRoleContentCatalog.SocialPosition.DEPENDENT,
                NightmareRoleContentCatalog.SocialPosition.OUTSIDER,
                NightmareRoleContentCatalog.SocialPosition.LABOURER,
                NightmareRoleContentCatalog.SocialPosition.RETAINER,
                NightmareRoleContentCatalog.SocialPosition.INITIATE,
                NightmareRoleContentCatalog.SocialPosition.AUXILIARY
        ), positions);
        assertTrue(conditions.containsAll(Set.of(NightmareRoleContentCatalog.Condition.values())));
        assertTrue(relationships.containsAll(Set.of(NightmareRoleContentCatalog.Relationship.values())));
        assertTrue(knowledge.containsAll(Set.of(NightmareRoleContentCatalog.Knowledge.values())));
        assertTrue(pressures.containsAll(Set.of(NightmareRoleContentCatalog.AlignmentPressure.values())));
    }

    @Test
    void matcherIsDeterministicAndIndependentOfEvidenceMapOrder() {
        Map<String, Integer> first = new HashMap<>();
        first.put("warning", 4);
        first.put("rescue", 2);
        first.put("route", 3);

        Map<String, Integer> second = new HashMap<>();
        second.put("route", 3);
        second.put("warning", 4);
        second.put("rescue", 2);

        NightmareRoleContentCatalog.RoleMatch a = NightmareRoleContentCatalog.match(4421L, first);
        NightmareRoleContentCatalog.RoleMatch b = NightmareRoleContentCatalog.match(4421L, second);

        assertEquals(a, b);
    }

    @Test
    void evidenceChangesWeightsWithoutClaimingAUniversalFormula() {
        int warningMatches = 0;
        int neutralMatches = 0;

        for (long seed = 0; seed < 4096; seed++) {
            NightmareRoleContentCatalog.RoleMatch warning = NightmareRoleContentCatalog.match(seed, Map.of("warning", 8));
            NightmareRoleContentCatalog.RoleMatch neutral = NightmareRoleContentCatalog.match(seed, Map.of());
            if (warning.role().evidenceAffinities().contains("warning")) {
                warningMatches++;
            }
            if (neutral.role().evidenceAffinities().contains("warning")) {
                neutralMatches++;
            }
        }

        assertTrue(warningMatches > neutralMatches * 2,
                "warning evidence should materially bias authored matching: " + warningMatches + " vs " + neutralMatches);
    }

    @Test
    void seedSweepReachesDiverseRolesRatherThanAFixedTinyResultSet() {
        Set<String> ids = new HashSet<>();
        for (long seed = 0; seed < 1024; seed++) {
            ids.add(NightmareRoleContentCatalog.match(seed, Map.of("route", 2, "truth", 1)).role().id());
        }
        assertTrue(ids.size() >= 10, () -> "expected broad role diversity but got " + ids);
    }

    @Test
    void matchedEvidenceComesOnlyFromPositiveMatchingEvidence() {
        NightmareRoleContentCatalog.RoleMatch match = NightmareRoleContentCatalog.match(
                81L,
                Map.of("warning", 4, "water", 0, "unrelated", 9)
        );

        List<String> expected = new ArrayList<>();
        for (String affinity : match.role().evidenceAffinities()) {
            if (affinity.equals("warning")) {
                expected.add(affinity);
            }
        }
        expected.sort(String::compareTo);

        List<String> actual = new ArrayList<>(match.matchedEvidence());
        actual.sort(String::compareTo);
        assertEquals(expected, actual);
    }

    @Test
    void negativeEvidenceFailsClosed() {
        assertThrows(IllegalArgumentException.class,
                () -> NightmareRoleContentCatalog.match(1L, Map.of("warning", -1)));
    }
}
