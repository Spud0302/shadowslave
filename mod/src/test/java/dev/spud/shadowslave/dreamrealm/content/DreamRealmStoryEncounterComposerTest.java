package dev.spud.shadowslave.dreamrealm.content;

import dev.spud.shadowslave.nightmare.content.NightmareCreatureContentCatalog;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DreamRealmStoryEncounterComposerTest {
    @Test
    void everyAuthoredRegionComposesOnlyLocalCompatibleContent() {
        Map<String, DreamRealmStoryContentCatalog.StoryModule> storiesByRegion = new HashMap<>();
        DreamRealmStoryContentCatalog.waveOne().forEach(story -> storiesByRegion.put(story.regionId(), story));

        Map<String, NightmareCreatureContentCatalog.CreatureProfile> creaturesById = new HashMap<>();
        NightmareCreatureContentCatalog.waveOne().forEach(creature -> creaturesById.put(creature.id(), creature));

        for (DreamRealmRegionContentCatalog.RegionProfile region : DreamRealmRegionContentCatalog.waveOne()) {
            DreamRealmStoryEncounterComposer.EncounterDefinition encounter =
                    DreamRealmStoryEncounterComposer.compose(17L, region.id());
            DreamRealmStoryContentCatalog.StoryModule story = storiesByRegion.get(region.id());
            NightmareCreatureContentCatalog.CreatureProfile creature = creaturesById.get(encounter.creatureId());

            assertEquals(region.id(), encounter.regionId());
            assertEquals(story.id(), encounter.storyModuleId());
            assertTrue(region.creatureAffinityIds().contains(encounter.creatureId()), region.id());
            assertTrue(region.hazards().contains(encounter.hazard()), region.id());
            assertTrue(story.npcArchetypes().contains(encounter.npcArchetype()), region.id());
            assertTrue(story.storyHooks().contains(encounter.storyHook()), region.id());
            assertTrue(creature.counterplayTags().contains(encounter.counterplayTag()), region.id());
            assertTrue(creature.appraisalEvidenceTags().contains(encounter.appraisalEvidenceTag()), region.id());
        }
    }

    @Test
    void sameSeedAndRegionResolveTheSameCompleteDefinition() {
        DreamRealmStoryEncounterComposer.EncounterDefinition first =
                DreamRealmStoryEncounterComposer.compose(982451653L, "storm_lantern_coast");
        DreamRealmStoryEncounterComposer.EncounterDefinition second =
                DreamRealmStoryEncounterComposer.compose(982451653L, "storm_lantern_coast");

        assertEquals(first, second);
        assertEquals(DreamRealmStoryEncounterComposer.GENERATOR_VERSION, first.generatorVersion());
        assertEquals(982451653L, first.generationSeed());
    }

    @Test
    void seedSweepProducesVariedEncounterCombinationsWithoutEscapingAuthoredCompatibility() {
        Set<String> combinations = new HashSet<>();
        Set<String> creatures = new HashSet<>();
        Set<String> hooks = new HashSet<>();
        Set<DreamRealmRegionContentCatalog.Hazard> hazards = new HashSet<>();

        for (long seed = 0; seed < 512; seed++) {
            DreamRealmStoryEncounterComposer.EncounterDefinition encounter =
                    DreamRealmStoryEncounterComposer.compose(seed, "red_canopy");
            combinations.add(encounter.creatureId() + ":" + encounter.hazard() + ":" + encounter.storyHook()
                    + ":" + encounter.npcArchetype() + ":" + encounter.counterplayTag());
            creatures.add(encounter.creatureId());
            hooks.add(encounter.storyHook());
            hazards.add(encounter.hazard());
        }

        assertTrue(combinations.size() >= 40);
        assertEquals(3, creatures.size());
        assertEquals(3, hooks.size());
        assertEquals(3, hazards.size());
    }

    @Test
    void regionIdentityParticipatesInDeterministicResolution() {
        DreamRealmStoryEncounterComposer.EncounterDefinition coast =
                DreamRealmStoryEncounterComposer.compose(42L, "storm_lantern_coast");
        DreamRealmStoryEncounterComposer.EncounterDefinition pass =
                DreamRealmStoryEncounterComposer.compose(42L, "mistwound_pass");

        assertNotEquals(coast.id(), pass.id());
        assertNotEquals(coast.regionId(), pass.regionId());
        assertNotEquals(coast.storyModuleId(), pass.storyModuleId());
    }

    @Test
    void resolvedDefinitionCarriesEnoughIdentityToPersistInsteadOfRerolling() {
        DreamRealmStoryEncounterComposer.EncounterDefinition encounter =
                DreamRealmStoryEncounterComposer.compose(-7L, "blackwater_steps");

        assertTrue(encounter.id().startsWith("story_encounter/blackwater_steps/"));
        assertTrue(encounter.presentationCue().length() > 80);
        assertTrue(!encounter.creatureId().isBlank());
        assertTrue(!encounter.storyHook().isBlank());
        assertTrue(!encounter.counterplayTag().isBlank());
        assertTrue(!encounter.appraisalEvidenceTag().isBlank());
    }

    @Test
    void unknownRegionFailsClosedInsteadOfInventingContent() {
        assertThrows(IllegalArgumentException.class,
                () -> DreamRealmStoryEncounterComposer.compose(1L, "not_a_region"));
    }
}
