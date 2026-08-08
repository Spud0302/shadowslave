package dev.spud.shadowslave.nightmare.content;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightmareCreatureEncounterRoleCatalogTest {
    @Test
    void waveOneProvidesTwoRolesForEveryMergedCreatureAndAllRoleFamilies() {
        List<NightmareCreatureEncounterRoleCatalog.EncounterRoleModule> modules =
                NightmareCreatureEncounterRoleCatalog.waveOne();
        List<NightmareCreatureContentCatalog.CreatureProfile> creatures = NightmareCreatureContentCatalog.waveOne();

        assertEquals(24, modules.size());
        assertEquals(24, modules.stream().map(NightmareCreatureEncounterRoleCatalog.EncounterRoleModule::id).distinct().count());

        EnumSet<NightmareCreatureEncounterRoleCatalog.EncounterRole> roles =
                EnumSet.noneOf(NightmareCreatureEncounterRoleCatalog.EncounterRole.class);
        Map<String, Integer> perCreature = new HashMap<>();
        for (NightmareCreatureEncounterRoleCatalog.EncounterRoleModule module : modules) {
            roles.add(module.role());
            perCreature.merge(module.creatureId(), 1, Integer::sum);
            assertFalse(module.encounterFrame().isBlank());
            assertFalse(module.escalation().isBlank());
            assertFalse(module.counterplayFrame().isBlank());
            assertFalse(module.evidenceTags().isEmpty());
            assertFalse(module.antiOverclaimBoundary().isBlank());
        }

        assertEquals(EnumSet.allOf(NightmareCreatureEncounterRoleCatalog.EncounterRole.class), roles);
        for (NightmareCreatureContentCatalog.CreatureProfile creature : creatures) {
            assertEquals(2, perCreature.getOrDefault(creature.id(), 0));
        }
    }

    @Test
    void modulesNeverExceedThePressureVocabularyOfTheirResolvedCreature() {
        Map<String, NightmareCreatureContentCatalog.CreatureProfile> creatures = new HashMap<>();
        NightmareCreatureContentCatalog.waveOne().forEach(profile -> creatures.put(profile.id(), profile));

        for (NightmareCreatureEncounterRoleCatalog.EncounterRoleModule module :
                NightmareCreatureEncounterRoleCatalog.waveOne()) {
            NightmareCreatureContentCatalog.CreatureProfile source = creatures.get(module.creatureId());
            assertTrue(source.pressures().containsAll(module.requiredPressures()));
        }
    }

    @Test
    void compositionCannotChangeCreatureIdentityAcrossSeedsOrEvidence() {
        for (NightmareCreatureContentCatalog.CreatureProfile creature : NightmareCreatureContentCatalog.waveOne()) {
            for (long seed = 0; seed < 256; seed++) {
                NightmareCreatureEncounterRoleCatalog.ResolvedEncounterRole result =
                        NightmareCreatureEncounterRoleCatalog.compose(seed, creature.id(), Map.of(
                                "ash", 1,
                                "crossing", 99,
                                "mist", 2,
                                "city", 4
                        ));
                assertEquals(creature.id(), result.creatureId());
                assertEquals(creature.id(), NightmareCreatureEncounterRoleCatalog.require(result.moduleId()).creatureId());
                assertEquals(NightmareCreatureEncounterRoleCatalog.GENERATOR_VERSION, result.generatorVersion());
            }
        }
    }

    @Test
    void evidenceMagnitudeDoesNotBecomeAnEncounterScoringFormula() {
        NightmareCreatureEncounterRoleCatalog.ResolvedEncounterRole low =
                NightmareCreatureEncounterRoleCatalog.compose(42L, "pale_ferryman", Map.of("crossing", 1, "river", 1));
        NightmareCreatureEncounterRoleCatalog.ResolvedEncounterRole high =
                NightmareCreatureEncounterRoleCatalog.compose(42L, "pale_ferryman", Map.of("crossing", 999, "river", 999));

        assertEquals(low.moduleId(), high.moduleId());
        assertEquals(low.role(), high.role());
        assertEquals(low.matchedEvidence(), high.matchedEvidence());
    }

    @Test
    void positiveLocalEvidenceCanPreferACompatibleAuthoredRoleWithoutRollingCreatureIdentity() {
        NightmareCreatureEncounterRoleCatalog.ResolvedEncounterRole result =
                NightmareCreatureEncounterRoleCatalog.compose(5L, "bell_eater", Map.of(
                        "signal", 1,
                        "tower", 1
                ));

        assertEquals("bell_eater", result.creatureId());
        assertEquals("bell_eater_hunter", result.moduleId());
        assertEquals(NightmareCreatureEncounterRoleCatalog.EncounterRole.HUNTER, result.role());
        assertTrue(result.matchedEvidence().contains("signal"));
    }

    @Test
    void neutralEvidenceLeavesSeededVariationInsideTheSameCreatureOnly() {
        Set<String> modules = new HashSet<>();
        for (long seed = 0; seed < 512; seed++) {
            NightmareCreatureEncounterRoleCatalog.ResolvedEncounterRole result =
                    NightmareCreatureEncounterRoleCatalog.compose(seed, "drowned_listener", Map.of());
            modules.add(result.moduleId());
            assertEquals("drowned_listener", result.creatureId());
        }

        assertEquals(Set.of("drowned_listener_hunter", "drowned_listener_avoidable"), modules);
    }

    @Test
    void deceptiveAndAvoidableRolesRemainBoundedInsteadOfInventingExtraPowers() {
        NightmareCreatureEncounterRoleCatalog.EncounterRoleModule ferryman =
                NightmareCreatureEncounterRoleCatalog.require("pale_ferryman_deceptive");
        NightmareCreatureEncounterRoleCatalog.EncounterRoleModule mimic =
                NightmareCreatureEncounterRoleCatalog.require("hollow_mimic_deceptive");
        NightmareCreatureEncounterRoleCatalog.EncounterRoleModule stalker =
                NightmareCreatureEncounterRoleCatalog.require("veil_stalker_deceptive");
        NightmareCreatureEncounterRoleCatalog.EncounterRoleModule listener =
                NightmareCreatureEncounterRoleCatalog.require("drowned_listener_avoidable");

        assertTrue(ferryman.antiOverclaimBoundary().contains("contracts"));
        assertTrue(mimic.antiOverclaimBoundary().contains("memories"));
        assertTrue(stalker.antiOverclaimBoundary().contains("illusions"));
        assertTrue(listener.antiOverclaimBoundary().contains("Avoidability"));
    }

    @Test
    void invalidEvidenceAndUnknownIdsFailClosed() {
        assertThrows(IllegalArgumentException.class,
                () -> NightmareCreatureEncounterRoleCatalog.compose(1L, "unknown_creature", Map.of()));
        assertThrows(IllegalArgumentException.class,
                () -> NightmareCreatureEncounterRoleCatalog.require("unknown_module"));
        assertThrows(IllegalArgumentException.class,
                () -> NightmareCreatureEncounterRoleCatalog.compose(1L, "ash_burrower", Map.of("ash", -1)));
    }
}
