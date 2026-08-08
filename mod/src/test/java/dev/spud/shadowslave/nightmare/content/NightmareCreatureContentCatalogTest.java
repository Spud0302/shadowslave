package dev.spud.shadowslave.nightmare.content;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightmareCreatureContentCatalogTest {
    @Test
    void waveOneProvidesBroadEncounterVocabulary() {
        List<NightmareCreatureContentCatalog.CreatureProfile> profiles =
                NightmareCreatureContentCatalog.waveOne();

        assertEquals(12, profiles.size());
        assertEquals(12, profiles.stream().map(NightmareCreatureContentCatalog.CreatureProfile::id).distinct().count());

        EnumSet<NightmareCreatureContentCatalog.Rank> ranks = EnumSet.noneOf(NightmareCreatureContentCatalog.Rank.class);
        EnumSet<NightmareCreatureContentCatalog.CreatureClass> classes =
                EnumSet.noneOf(NightmareCreatureContentCatalog.CreatureClass.class);
        EnumSet<NightmareCreatureContentCatalog.Sense> senses =
                EnumSet.noneOf(NightmareCreatureContentCatalog.Sense.class);
        EnumSet<NightmareCreatureContentCatalog.Locomotion> locomotion =
                EnumSet.noneOf(NightmareCreatureContentCatalog.Locomotion.class);
        EnumSet<NightmareCreatureContentCatalog.Pressure> pressures =
                EnumSet.noneOf(NightmareCreatureContentCatalog.Pressure.class);

        for (NightmareCreatureContentCatalog.CreatureProfile profile : profiles) {
            ranks.add(profile.rank());
            classes.add(profile.creatureClass());
            senses.addAll(profile.senses());
            locomotion.addAll(profile.locomotion());
            pressures.addAll(profile.pressures());
            assertFalse(profile.counterplayTags().isEmpty());
            assertFalse(profile.appraisalEvidenceTags().isEmpty());
            assertFalse(profile.presentationCue().isBlank());
        }

        assertEquals(EnumSet.allOf(NightmareCreatureContentCatalog.Rank.class), ranks);
        assertEquals(EnumSet.allOf(NightmareCreatureContentCatalog.CreatureClass.class), classes);
        assertEquals(EnumSet.allOf(NightmareCreatureContentCatalog.Sense.class), senses);
        assertEquals(EnumSet.allOf(NightmareCreatureContentCatalog.Locomotion.class), locomotion);
        assertEquals(EnumSet.allOf(NightmareCreatureContentCatalog.Pressure.class), pressures);
    }

    @Test
    void everyCreatureHasMoreThanDirectDamageAsCounterplay() {
        List<NightmareCreatureContentCatalog.CreatureProfile> profiles =
                NightmareCreatureContentCatalog.waveOne();

        Set<String> allCounterplay = new HashSet<>();
        profiles.forEach(profile -> allCounterplay.addAll(profile.counterplayTags()));

        assertTrue(allCounterplay.contains("bait_vibration"));
        assertTrue(allCounterplay.contains("decoy_sound"));
        assertTrue(allCounterplay.contains("verification_phrase"));
        assertTrue(allCounterplay.contains("alternate_crossing"));
        assertTrue(allCounterplay.contains("controlled_burn"));
        assertTrue(allCounterplay.contains("shared_watch"));
        assertTrue(allCounterplay.size() >= 30);
    }

    @Test
    void drownedListenerIsReusableOutsideItsScenarioDefinition() {
        NightmareCreatureContentCatalog.CreatureProfile listener = NightmareCreatureContentCatalog.waveOne().stream()
                .filter(profile -> profile.id().equals("drowned_listener"))
                .findFirst()
                .orElseThrow();

        assertEquals("Drowned Listener", listener.displayName());
        assertEquals(NightmareCreatureContentCatalog.Rank.DORMANT, listener.rank());
        assertEquals(NightmareCreatureContentCatalog.CreatureClass.MONSTER, listener.creatureClass());
        assertTrue(listener.senses().contains(NightmareCreatureContentCatalog.Sense.SOUND));
        assertTrue(listener.locomotion().contains(NightmareCreatureContentCatalog.Locomotion.SWIM));
        assertTrue(listener.counterplayTags().contains("decoy_sound"));
    }

    @Test
    void authoredPressuresSupportNonBossEncounterDesign() {
        List<NightmareCreatureContentCatalog.CreatureProfile> profiles =
                NightmareCreatureContentCatalog.waveOne();

        long deception = profiles.stream()
                .filter(profile -> profile.pressures().contains(NightmareCreatureContentCatalog.Pressure.DECEPTION))
                .count();
        long areaDenial = profiles.stream()
                .filter(profile -> profile.pressures().contains(NightmareCreatureContentCatalog.Pressure.AREA_DENIAL))
                .count();
        long displacement = profiles.stream()
                .filter(profile -> profile.pressures().contains(NightmareCreatureContentCatalog.Pressure.DISPLACEMENT))
                .count();

        assertTrue(deception >= 3);
        assertTrue(areaDenial >= 4);
        assertTrue(displacement >= 3);
    }
}
