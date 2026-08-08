package dev.spud.shadowslave.dreamrealm.content;

import dev.spud.shadowslave.nightmare.content.NightmareCreatureContentCatalog;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * DESIGN-only deterministic composition of player-facing Dream Realm story encounters.
 *
 * <p>This class does not claim that the Nightmare Spell, Dream Realm, factions, or
 * creatures use a canonical encounter-generation formula. It combines authored Java
 * primitives using explicit compatibility constraints so a resolved definition can be
 * persisted by a later canonical-state owner instead of being rerolled on restart.</p>
 */
public final class DreamRealmStoryEncounterComposer {
    public static final int GENERATOR_VERSION = 1;

    private DreamRealmStoryEncounterComposer() {}

    public record EncounterDefinition(
            String id,
            int generatorVersion,
            long generationSeed,
            String regionId,
            String storyModuleId,
            String creatureId,
            DreamRealmRegionContentCatalog.Hazard hazard,
            String npcArchetype,
            String storyHook,
            String counterplayTag,
            String appraisalEvidenceTag,
            String presentationCue
    ) {
        public EncounterDefinition {
            id = requireText(id, "id");
            if (generatorVersion < 1) {
                throw new IllegalArgumentException("generatorVersion must be positive");
            }
            regionId = requireText(regionId, "regionId");
            storyModuleId = requireText(storyModuleId, "storyModuleId");
            creatureId = requireText(creatureId, "creatureId");
            hazard = Objects.requireNonNull(hazard, "hazard");
            npcArchetype = requireText(npcArchetype, "npcArchetype");
            storyHook = requireText(storyHook, "storyHook");
            counterplayTag = requireText(counterplayTag, "counterplayTag");
            appraisalEvidenceTag = requireText(appraisalEvidenceTag, "appraisalEvidenceTag");
            presentationCue = requireText(presentationCue, "presentationCue");
        }
    }

    /**
     * Resolves one encounter for an authored region using only compatible authored content.
     * Callers should persist the returned complete definition if it becomes player state.
     */
    public static EncounterDefinition compose(long seed, String regionId) {
        Objects.requireNonNull(regionId, "regionId");

        DreamRealmRegionContentCatalog.RegionProfile region = DreamRealmRegionContentCatalog.waveOne().stream()
                .filter(candidate -> candidate.id().equals(regionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown Dream Realm region: " + regionId));

        DreamRealmStoryContentCatalog.StoryModule story = DreamRealmStoryContentCatalog.waveOne().stream()
                .filter(candidate -> candidate.regionId().equals(region.id()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No story module for region: " + region.id()));

        Map<String, NightmareCreatureContentCatalog.CreatureProfile> creaturesById = new HashMap<>();
        for (NightmareCreatureContentCatalog.CreatureProfile creature : NightmareCreatureContentCatalog.waveOne()) {
            creaturesById.put(creature.id(), creature);
        }

        List<NightmareCreatureContentCatalog.CreatureProfile> compatibleCreatures = region.creatureAffinityIds().stream()
                .map(creaturesById::get)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(NightmareCreatureContentCatalog.CreatureProfile::id))
                .toList();
        if (compatibleCreatures.isEmpty()) {
            throw new IllegalStateException("Region has no available compatible creature: " + region.id());
        }

        long state = mix64(seed ^ stableHash(region.id()) ^ ((long) GENERATOR_VERSION << 48));
        NightmareCreatureContentCatalog.CreatureProfile creature = choose(compatibleCreatures, state);
        state = mix64(state + 0x9E3779B97F4A7C15L);
        DreamRealmRegionContentCatalog.Hazard hazard = chooseSorted(region.hazards(), state);
        state = mix64(state + 0x9E3779B97F4A7C15L);
        String npc = chooseSorted(story.npcArchetypes(), state);
        state = mix64(state + 0x9E3779B97F4A7C15L);
        String hook = chooseSorted(story.storyHooks(), state);
        state = mix64(state + 0x9E3779B97F4A7C15L);
        String counterplay = chooseSorted(creature.counterplayTags(), state);
        state = mix64(state + 0x9E3779B97F4A7C15L);
        String evidence = chooseSorted(creature.appraisalEvidenceTags(), state);

        String encounterId = "story_encounter/" + region.id() + "/" + Long.toUnsignedString(mix64(seed ^ stableHash(story.id())), 36);
        String cue = region.arrivalCue() + " " + story.arrivalCue() + " " + creature.presentationCue();

        return new EncounterDefinition(
                encounterId,
                GENERATOR_VERSION,
                seed,
                region.id(),
                story.id(),
                creature.id(),
                hazard,
                npc,
                hook,
                counterplay,
                evidence,
                cue
        );
    }

    private static <T> T choose(List<T> values, long state) {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Cannot choose from an empty list");
        }
        return values.get(Math.floorMod(state, values.size()));
    }

    private static <T extends Enum<T>> T chooseSorted(Set<T> values, long state) {
        List<T> sorted = values.stream().sorted(Comparator.comparing(Enum::name)).toList();
        return choose(sorted, state);
    }

    private static String chooseSorted(Set<String> values, long state) {
        List<String> sorted = new ArrayList<>(values);
        sorted.sort(String::compareTo);
        return choose(sorted, state);
    }

    private static long stableHash(String value) {
        long hash = 0xcbf29ce484222325L;
        for (byte b : value.getBytes(StandardCharsets.UTF_8)) {
            hash ^= Byte.toUnsignedInt(b);
            hash *= 0x100000001b3L;
        }
        return hash;
    }

    private static long mix64(long value) {
        value ^= value >>> 30;
        value *= 0xbf58476d1ce4e5b9L;
        value ^= value >>> 27;
        value *= 0x94d049bb133111ebL;
        return value ^ (value >>> 31);
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }
}
