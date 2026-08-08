package dev.spud.shadowslave.appraisal.generation;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Authored Attribute primitives for player-facing identity generation.
 *
 * <p>This catalogue is Minecraft DESIGN content constrained by primary lore. It
 * deliberately does not claim that the Nightmare Spell uses this taxonomy,
 * weighting model, visibility model, or any universal procedural Attribute
 * generation formula.</p>
 */
public final class AttributeContentCatalog {
    private static final String NAMESPACE = "shadowslave";

    private AttributeContentCatalog() {
    }

    public enum OriginKind {
        INNATE,
        NIGHTMARE_ROLE_INHERITED,
        ACQUIRED_TRANSFORMATION
    }

    public enum Visibility {
        REVEALED,
        OBSCURED
    }

    public enum EffectFamily {
        AFFINITY,
        CONSTITUTION,
        FATE_RELATION,
        PERCEPTION,
        PRESENCE,
        RESILIENCE,
        TRAVERSAL,
        UTILITY
    }

    public record AttributeProfile(
            ResourceLocation id,
            String formalName,
            OriginKind origin,
            Visibility visibility,
            EffectFamily effectFamily,
            Set<String> affinityTags,
            Set<String> gameplayHooks,
            int baseWeight,
            ResourceLocation evolvesTo
    ) {
        public AttributeProfile {
            id = Objects.requireNonNull(id, "id");
            formalName = requireText(formalName, "formalName");
            origin = Objects.requireNonNull(origin, "origin");
            visibility = Objects.requireNonNull(visibility, "visibility");
            effectFamily = Objects.requireNonNull(effectFamily, "effectFamily");
            affinityTags = normalizedTags(affinityTags, "affinityTags");
            gameplayHooks = normalizedTags(gameplayHooks, "gameplayHooks");
            if (gameplayHooks.isEmpty()) {
                throw new IllegalArgumentException("Attribute gameplayHooks cannot be empty");
            }
            if (baseWeight <= 0) {
                throw new IllegalArgumentException("baseWeight must be positive");
            }
        }
    }

    public record Catalog(List<AttributeProfile> profiles) {
        public Catalog {
            ArrayList<AttributeProfile> canonical = new ArrayList<>(Objects.requireNonNull(profiles, "profiles"));
            canonical.sort(Comparator.comparing(profile -> profile.id().toString()));
            if (canonical.isEmpty()) {
                throw new IllegalArgumentException("Attribute catalogue cannot be empty");
            }

            HashSet<ResourceLocation> ids = new HashSet<>();
            for (AttributeProfile profile : canonical) {
                Objects.requireNonNull(profile, "profile");
                if (!ids.add(profile.id())) {
                    throw new IllegalArgumentException("Duplicate Attribute id: " + profile.id());
                }
            }

            for (AttributeProfile profile : canonical) {
                if (profile.evolvesTo() != null && !ids.contains(profile.evolvesTo())) {
                    throw new IllegalArgumentException(
                            "Attribute " + profile.id() + " evolves to missing profile " + profile.evolvesTo()
                    );
                }
            }

            detectEvolutionCycles(canonical);
            profiles = List.copyOf(canonical);
        }

        /**
         * Deterministically chooses one authored primitive using appraisal evidence.
         * This is a project DESIGN rule, not a canonical Spell formula.
         */
        public AttributeProfile select(long seed, Map<String, Integer> evidence) {
            Map<String, Integer> normalized = normalizeEvidence(evidence);
            long totalWeight = 0L;
            ArrayList<Long> weights = new ArrayList<>(profiles.size());

            for (AttributeProfile profile : profiles) {
                long weight = profile.baseWeight();
                for (String tag : profile.affinityTags()) {
                    weight += (long) normalized.getOrDefault(tag, 0) * 3L;
                }
                weights.add(weight);
                totalWeight += weight;
            }

            long draw = Math.floorMod(mix(seed ^ evidenceFingerprint(normalized)), totalWeight);
            long cursor = 0L;
            for (int index = 0; index < profiles.size(); index++) {
                cursor += weights.get(index);
                if (draw < cursor) {
                    return profiles.get(index);
                }
            }
            throw new IllegalStateException("Attribute selection cursor escaped weighted catalogue");
        }

        public AttributeProfile require(ResourceLocation id) {
            return profiles.stream()
                    .filter(profile -> profile.id().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown Attribute profile: " + id));
        }
    }

    public static Catalog waveOne() {
        return new Catalog(List.of(
                attribute(
                        "ashen_lungs", "Ashen Lungs", OriginKind.NIGHTMARE_ROLE_INHERITED,
                        Visibility.REVEALED, EffectFamily.CONSTITUTION,
                        Set.of("ash", "endurance", "aftermath"),
                        Set.of("smoke_tolerance", "measured_breathing", "ash_exposure"), 3, "unbroken_breath"
                ),
                attribute(
                        "bell_sense", "Bell Sense", OriginKind.NIGHTMARE_ROLE_INHERITED,
                        Visibility.REVEALED, EffectFamily.PERCEPTION,
                        Set.of("sound", "warning", "resonance"),
                        Set.of("resonance_direction", "distant_warning", "false_echo_detection"), 4, null
                ),
                attribute(
                        "blackwater_blood", "Blackwater Blood", OriginKind.ACQUIRED_TRANSFORMATION,
                        Visibility.REVEALED, EffectFamily.RESILIENCE,
                        Set.of("water", "survival", "adaptation"),
                        Set.of("cold_water_tolerance", "slowed_exhaustion", "flood_survival"), 2, null
                ),
                attribute(
                        "borrowed_compass", "Borrowed Compass", OriginKind.NIGHTMARE_ROLE_INHERITED,
                        Visibility.REVEALED, EffectFamily.TRAVERSAL,
                        Set.of("path", "guidance", "escape"),
                        Set.of("route_memory", "wrong_turn_warning", "landmark_recall"), 4, "roadwise"
                ),
                attribute(
                        "cinder_heart", "Cinder Heart", OriginKind.INNATE,
                        Visibility.REVEALED, EffectFamily.AFFINITY,
                        Set.of("ember", "resolve", "preservation"),
                        Set.of("ember_affinity", "heat_sense", "flame_stability"), 3, "living_ember"
                ),
                attribute(
                        "glass_nerves", "Glass Nerves", OriginKind.INNATE,
                        Visibility.REVEALED, EffectFamily.PERCEPTION,
                        Set.of("glass", "precision", "reflection"),
                        Set.of("fine_motion_sense", "surface_vibration", "reflection_notice"), 3, null
                ),
                attribute(
                        "hollow_presence", "Hollow Presence", OriginKind.INNATE,
                        Visibility.OBSCURED, EffectFamily.PRESENCE,
                        Set.of("hollow", "absence", "concealment"),
                        Set.of("attention_slip", "quiet_footprint", "presence_suppression"), 2, null
                ),
                attribute(
                        "mist_born", "Mist-Born", OriginKind.NIGHTMARE_ROLE_INHERITED,
                        Visibility.REVEALED, EffectFamily.UTILITY,
                        Set.of("mist", "concealment", "adaptation"),
                        Set.of("mist_orientation", "condensation_notice", "concealed_motion"), 3, null
                ),
                attribute(
                        "red_thread", "Red Thread", OriginKind.INNATE,
                        Visibility.OBSCURED, EffectFamily.FATE_RELATION,
                        Set.of("thread", "connection", "witness"),
                        Set.of("bond_awareness", "coincidence_notice", "connection_pressure"), 1, null
                ),
                attribute(
                        "roadwise", "Roadwise", OriginKind.ACQUIRED_TRANSFORMATION,
                        Visibility.REVEALED, EffectFamily.TRAVERSAL,
                        Set.of("path", "movement", "guidance"),
                        Set.of("route_intuition", "terrain_pacing", "safe_detour_notice"), 2, null
                ),
                attribute(
                        "stone_sleeper", "Stone Sleeper", OriginKind.NIGHTMARE_ROLE_INHERITED,
                        Visibility.REVEALED, EffectFamily.RESILIENCE,
                        Set.of("stone", "stability", "endurance"),
                        Set.of("rest_on_rough_ground", "impact_bracing", "long_watch_recovery"), 3, null
                ),
                attribute(
                        "thorn_kin", "Thorn-Kin", OriginKind.ACQUIRED_TRANSFORMATION,
                        Visibility.REVEALED, EffectFamily.AFFINITY,
                        Set.of("thorn", "growth", "retaliation"),
                        Set.of("hostile_flora_notice", "thorn_handling", "growth_response"), 2, null
                ),
                attribute(
                        "tide_listener", "Tide Listener", OriginKind.INNATE,
                        Visibility.REVEALED, EffectFamily.PERCEPTION,
                        Set.of("water", "rhythm", "warning"),
                        Set.of("water_level_notice", "current_reading", "storm_timing"), 3, null
                ),
                attribute(
                        "unbroken_breath", "Unbroken Breath", OriginKind.ACQUIRED_TRANSFORMATION,
                        Visibility.REVEALED, EffectFamily.CONSTITUTION,
                        Set.of("endurance", "aftermath", "resolve"),
                        Set.of("smoke_endurance", "breath_recovery", "exertion_pacing"), 2, null
                ),
                attribute(
                        "veil_touched", "Veil-Touched", OriginKind.ACQUIRED_TRANSFORMATION,
                        Visibility.OBSCURED, EffectFamily.PRESENCE,
                        Set.of("concealment", "absence", "mist"),
                        Set.of("obscured_outline", "attention_break", "hidden_route_affinity"), 1, null
                ),
                attribute(
                        "watchers_mark", "Watcher's Mark", OriginKind.NIGHTMARE_ROLE_INHERITED,
                        Visibility.REVEALED, EffectFamily.UTILITY,
                        Set.of("watch", "warning", "duty"),
                        Set.of("watchfulness", "camp_routine", "approach_notice"), 4, null
                ),
                attribute(
                        "weathered_hands", "Weathered Hands", OriginKind.NIGHTMARE_ROLE_INHERITED,
                        Visibility.REVEALED, EffectFamily.UTILITY,
                        Set.of("craft", "repair", "preservation"),
                        Set.of("field_repair", "tool_familiarity", "material_wear_notice"), 4, null
                ),
                attribute(
                        "living_ember", "Living Ember", OriginKind.ACQUIRED_TRANSFORMATION,
                        Visibility.REVEALED, EffectFamily.AFFINITY,
                        Set.of("ember", "light", "resolve"),
                        Set.of("steady_flame", "warmth_reserve", "kindling_response"), 2, null
                )
        ));
    }

    private static AttributeProfile attribute(
            String path,
            String name,
            OriginKind origin,
            Visibility visibility,
            EffectFamily effectFamily,
            Set<String> affinityTags,
            Set<String> gameplayHooks,
            int baseWeight,
            String evolvesTo
    ) {
        return new AttributeProfile(
                id("generation/attribute/" + path),
                name,
                origin,
                visibility,
                effectFamily,
                affinityTags,
                gameplayHooks,
                baseWeight,
                evolvesTo == null ? null : id("generation/attribute/" + evolvesTo)
        );
    }

    private static Map<String, Integer> normalizeEvidence(Map<String, Integer> evidence) {
        HashMap<String, Integer> normalized = new HashMap<>();
        Objects.requireNonNull(evidence, "evidence").forEach((rawTag, rawValue) -> {
            String tag = requireText(rawTag, "evidence tag").toLowerCase(Locale.ROOT).replace(' ', '_');
            int value = Objects.requireNonNull(rawValue, "evidence value");
            if (value < 0) {
                throw new IllegalArgumentException("Evidence weights cannot be negative");
            }
            normalized.merge(tag, value, Integer::sum);
        });
        return Map.copyOf(normalized);
    }

    private static long evidenceFingerprint(Map<String, Integer> evidence) {
        long hash = 0x9e3779b97f4a7c15L;
        for (Map.Entry<String, Integer> entry : evidence.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            hash = mix(hash ^ entry.getKey().hashCode());
            hash = mix(hash ^ entry.getValue());
        }
        return hash;
    }

    private static long mix(long value) {
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdL;
        value ^= value >>> 33;
        value *= 0xc4ceb9fe1a85ec53L;
        value ^= value >>> 33;
        return value;
    }

    private static void detectEvolutionCycles(List<AttributeProfile> profiles) {
        HashMap<ResourceLocation, ResourceLocation> edges = new HashMap<>();
        profiles.forEach(profile -> {
            if (profile.evolvesTo() != null) {
                edges.put(profile.id(), profile.evolvesTo());
            }
        });

        for (AttributeProfile start : profiles) {
            HashSet<ResourceLocation> visited = new HashSet<>();
            ResourceLocation cursor = start.id();
            while (cursor != null) {
                if (!visited.add(cursor)) {
                    throw new IllegalArgumentException("Attribute evolution cycle contains " + cursor);
                }
                cursor = edges.get(cursor);
            }
        }
    }

    private static Set<String> normalizedTags(Set<String> source, String name) {
        HashSet<String> normalized = new HashSet<>();
        for (String value : Objects.requireNonNull(source, name)) {
            normalized.add(requireText(value, name).toLowerCase(Locale.ROOT).replace(' ', '_'));
        }
        return Set.copyOf(normalized);
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(NAMESPACE, path);
    }
}
