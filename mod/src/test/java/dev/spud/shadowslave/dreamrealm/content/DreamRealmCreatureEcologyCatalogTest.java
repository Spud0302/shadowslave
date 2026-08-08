package dev.spud.shadowslave.dreamrealm.content;

import dev.spud.shadowslave.nightmare.content.NightmareCreatureContentCatalog;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DreamRealmCreatureEcologyCatalogTest {
    @Test
    void waveOneCoversEveryAuthoredRegionCreatureAffinityExactlyOnce() {
        var profiles = DreamRealmCreatureEcologyCatalog.waveOne();
        Set<String> expectedPairs = new HashSet<>();
        for (var region : DreamRealmRegionContentCatalog.waveOne()) {
            for (String creatureId : region.creatureAffinityIds()) {
                expectedPairs.add(region.id() + "/" + creatureId);
            }
        }
        Set<String> actualPairs = new HashSet<>();
        profiles.forEach(profile -> actualPairs.add(profile.regionId() + "/" + profile.creatureId()));

        assertEquals(22, profiles.size());
        assertEquals(expectedPairs, actualPairs);
        assertEquals(22, profiles.stream().map(DreamRealmCreatureEcologyCatalog.EcologyProfile::id).distinct().count());
    }

    @Test
    void everyProfileUsesOnlySourceRegionAndCreaturePrimitives() {
        var regions = DreamRealmRegionContentCatalog.waveOne();
        var creatures = NightmareCreatureContentCatalog.waveOne();

        for (var profile : DreamRealmCreatureEcologyCatalog.waveOne()) {
            var region = regions.stream().filter(candidate -> candidate.id().equals(profile.regionId())).findFirst().orElseThrow();
            var creature = creatures.stream().filter(candidate -> candidate.id().equals(profile.creatureId())).findFirst().orElseThrow();

            assertTrue(region.creatureAffinityIds().contains(creature.id()), profile.id());
            assertTrue(region.hazards().contains(profile.localHazard()), profile.id());
            assertTrue(region.traversal().contains(profile.traversalPressure()), profile.id());
            assertTrue(creature.counterplayTags().containsAll(profile.counterplayTags()), profile.id());
            assertTrue(profile.approachCues().size() >= 2, profile.id());
            assertFalse(profile.habitatRead().isBlank(), profile.id());
            assertFalse(profile.pressureRead().isBlank(), profile.id());
            assertFalse(profile.counterplayRead().isBlank(), profile.id());
            assertFalse(profile.boundary().isBlank(), profile.id());
        }
    }

    @Test
    void deterministicPresentationCannotChangeCallerSuppliedRegionOrCreature() {
        for (var profile : DreamRealmCreatureEcologyCatalog.waveOne()) {
            for (long seed = 0; seed < 256; seed++) {
                var presentation = DreamRealmCreatureEcologyCatalog.compose(seed, profile.regionId(), profile.creatureId());
                assertEquals(profile.regionId(), presentation.regionId(), profile.id());
                assertEquals(profile.creatureId(), presentation.creatureId(), profile.id());
                assertEquals(profile.id(), presentation.profileId(), profile.id());
                assertEquals(profile.localHazard(), presentation.localHazard(), profile.id());
                assertEquals(profile.traversalPressure(), presentation.traversalPressure(), profile.id());
            }
        }
    }

    @Test
    void sameSeedAndPairIsStableAndAuthoredCueVariationIsReachable() {
        var first = DreamRealmCreatureEcologyCatalog.compose(42L, "blackwater_steps", "drowned_listener");
        var second = DreamRealmCreatureEcologyCatalog.compose(42L, "blackwater_steps", "drowned_listener");
        assertEquals(first, second);

        Set<String> cues = new HashSet<>();
        for (long seed = 0; seed < 512; seed++) {
            cues.add(DreamRealmCreatureEcologyCatalog.compose(seed, "blackwater_steps", "drowned_listener").approachCue());
        }
        assertEquals(2, cues.size());
    }

    @Test
    void compositionFailsClosedForPairsNotAlreadyAuthoredByRegionCatalogue() {
        assertThrows(IllegalArgumentException.class,
                () -> DreamRealmCreatureEcologyCatalog.compose(1L, "ashen_expanse", "pale_ferryman"));
        assertThrows(IllegalArgumentException.class,
                () -> DreamRealmCreatureEcologyCatalog.compose(1L, "missing_region", "ash_burrower"));
        assertThrows(IllegalArgumentException.class,
                () -> DreamRealmCreatureEcologyCatalog.compose(1L, "ashen_expanse", "missing_creature"));
    }

    @Test
    void ecologyPresentationContainsNoSpawnOrRewardAuthority() {
        for (var profile : DreamRealmCreatureEcologyCatalog.waveOne()) {
            String combined = (profile.habitatRead() + " " + profile.pressureRead() + " "
                    + profile.counterplayRead() + " " + profile.boundary()).toLowerCase();
            assertFalse(combined.contains("spawn chance"), profile.id());
            assertFalse(combined.contains("drop chance"), profile.id());
            assertFalse(combined.contains("guaranteed reward"), profile.id());
            assertFalse(combined.contains("canonical habitat"), profile.id());
        }
    }
}