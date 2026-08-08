package dev.spud.shadowslave.appraisal.generation;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * Curated primitives and compatibility rules used by procedural identity
 * generation. A generated Aspect is composed from a nature, archetype and
 * compatible ability instead of selected from one finite finished-Aspect list.
 */
public record IdentityPrimitiveCatalog(
        List<Nature> natures,
        List<Archetype> archetypes,
        List<Ability> abilities,
        List<Flaw> flaws
) {
    private static final String NAMESPACE = "shadowslave";

    public IdentityPrimitiveCatalog {
        natures = canonicalList(natures, Nature::id, "nature");
        archetypes = canonicalList(archetypes, Archetype::id, "archetype");
        abilities = canonicalList(abilities, Ability::id, "ability");
        flaws = canonicalList(flaws, Flaw::id, "flaw");

        if (natures.isEmpty() || archetypes.isEmpty() || abilities.isEmpty() || flaws.isEmpty()) {
            throw new IllegalArgumentException("Every procedural identity primitive family must be non-empty");
        }

        for (Nature nature : natures) {
            if (abilities.stream().noneMatch(ability -> ability.supports(nature))) {
                throw new IllegalArgumentException("Nature " + nature.id() + " has no compatible ability");
            }
            if (flaws.stream().noneMatch(flaw -> flaw.supports(nature))) {
                throw new IllegalArgumentException("Nature " + nature.id() + " has no compatible Flaw");
            }
        }
    }

    /** Initial bounded catalogue used to prove the architecture around The Last Signal. */
    public static IdentityPrimitiveCatalog lastSignalPrototype() {
        return new IdentityPrimitiveCatalog(
                List.of(
                        new Nature(id("generation/nature/ash"), "Ash", Set.of("ash", "endurance", "aftermath"), 3),
                        new Nature(id("generation/nature/ember"), "Ember", Set.of("ember", "light", "preservation"), 5),
                        new Nature(id("generation/nature/road"), "Road", Set.of("path", "movement", "guidance"), 4),
                        new Nature(id("generation/nature/signal"), "Signal", Set.of("signal", "warning", "perception"), 4)
                ),
                List.of(
                        new Archetype(id("generation/archetype/keeper"), "Keeper of %s", Set.of("duty", "preservation"), 4),
                        new Archetype(id("generation/archetype/last"), "Last %s", Set.of("resolve", "sacrifice"), 2),
                        new Archetype(id("generation/archetype/wanderer"), "%s Wanderer", Set.of("movement", "escape"), 3),
                        new Archetype(id("generation/archetype/witness"), "%s Witness", Set.of("witness", "perception"), 3)
                ),
                List.of(
                        new Ability(id("generation/ability/ashen_guard"), "Ashen Guard", Set.of("ash"), Set.of("endurance", "duty"), 5),
                        new Ability(id("generation/ability/carry_the_flame"), "Carry the Flame", Set.of("ember"), Set.of("duty", "sacrifice"), 3),
                        new Ability(id("generation/ability/endure_the_ruin"), "Endure the Ruin", Set.of("ash"), Set.of("preservation", "aftermath"), 3),
                        new Ability(id("generation/ability/hear_the_call"), "Hear the Distant Call", Set.of("signal"), Set.of("witness", "warning"), 3),
                        new Ability(id("generation/ability/kindle"), "Kindle", Set.of("ember"), Set.of("preservation", "resolve"), 5),
                        new Ability(id("generation/ability/shorten_the_road"), "Shorten the Road", Set.of("path"), Set.of("escape", "movement"), 3),
                        new Ability(id("generation/ability/warning_pulse"), "Warning Pulse", Set.of("signal"), Set.of("warning", "perception"), 5),
                        new Ability(id("generation/ability/waymark"), "Waymark", Set.of("path"), Set.of("guidance", "movement"), 5)
                ),
                List.of(
                        new Flaw(
                                id("generation/flaw/burden_of_the_last"),
                                "Burden of the Last",
                                id("generation/flaw_effect/burden_of_the_last"),
                                Set.of("sacrifice", "aftermath"),
                                Set.of("resource", "long_horizon"),
                                Set.of(),
                                3
                        ),
                        new Flaw(
                                id("generation/flaw/cold_ash"),
                                "Cold Ash",
                                id("generation/flaw_effect/cold_ash"),
                                Set.of("ember", "preservation"),
                                Set.of("environmental", "water_vulnerability"),
                                Set.of(),
                                4
                        ),
                        new Flaw(
                                id("generation/flaw/open_flame"),
                                "Open Flame",
                                id("generation/flaw_effect/open_flame"),
                                Set.of("witness", "warning"),
                                Set.of("social", "disclosure"),
                                Set.of(),
                                3
                        ),
                        new Flaw(
                                id("generation/flaw/rooted_step"),
                                "Rooted Step",
                                id("generation/flaw_effect/rooted_step"),
                                Set.of("duty", "endurance"),
                                Set.of("immobility", "physical"),
                                Set.of("movement"),
                                3
                        ),
                        new Flaw(
                                id("generation/flaw/unanswered_call"),
                                "Unanswered Call",
                                id("generation/flaw_effect/unanswered_call"),
                                Set.of("signal", "duty"),
                                Set.of("compulsion", "social"),
                                Set.of(),
                                3
                        )
                )
        );
    }

    public record Nature(ResourceLocation id, String nameToken, Set<String> tags, int baseWeight) {
        public Nature {
            id = Objects.requireNonNull(id, "id");
            nameToken = requireText(nameToken, "nameToken");
            tags = normalizedTags(tags, "nature tags");
            requirePositive(baseWeight, "baseWeight");
        }
    }

    public record Archetype(
            ResourceLocation id,
            String namePattern,
            Set<String> affinityTags,
            int baseWeight
    ) {
        public Archetype {
            id = Objects.requireNonNull(id, "id");
            namePattern = requireText(namePattern, "namePattern");
            int placeholder = namePattern.indexOf("%s");
            if (placeholder < 0 || namePattern.indexOf("%s", placeholder + 2) >= 0) {
                throw new IllegalArgumentException("namePattern must contain exactly one %s placeholder");
            }
            affinityTags = normalizedTags(affinityTags, "archetype affinityTags");
            requirePositive(baseWeight, "baseWeight");
        }

        public String formatName(String natureNameToken) {
            return namePattern.replace("%s", requireText(natureNameToken, "natureNameToken"));
        }
    }

    public record Ability(
            ResourceLocation id,
            String displayName,
            Set<String> requiredNatureTags,
            Set<String> affinityTags,
            int baseWeight
    ) {
        public Ability {
            id = Objects.requireNonNull(id, "id");
            displayName = requireText(displayName, "displayName");
            requiredNatureTags = normalizedTags(requiredNatureTags, "requiredNatureTags");
            affinityTags = normalizedTags(affinityTags, "ability affinityTags");
            requirePositive(baseWeight, "baseWeight");
        }

        public boolean supports(Nature nature) {
            return nature.tags().containsAll(requiredNatureTags);
        }
    }

    public record Flaw(
            ResourceLocation id,
            String formalName,
            ResourceLocation effectId,
            Set<String> affinityTags,
            Set<String> traitTags,
            Set<String> incompatibleNatureTags,
            int baseWeight
    ) {
        public Flaw {
            id = Objects.requireNonNull(id, "id");
            formalName = requireText(formalName, "formalName");
            effectId = Objects.requireNonNull(effectId, "effectId");
            affinityTags = normalizedTags(affinityTags, "flaw affinityTags");
            traitTags = normalizedTags(traitTags, "traitTags");
            incompatibleNatureTags = normalizedTags(incompatibleNatureTags, "incompatibleNatureTags");
            requirePositive(baseWeight, "baseWeight");
        }

        public boolean supports(Nature nature) {
            return Collections.disjoint(incompatibleNatureTags, nature.tags());
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(NAMESPACE, path);
    }

    private static <T> List<T> canonicalList(
            List<T> source,
            Function<T, ResourceLocation> idExtractor,
            String familyName
    ) {
        ArrayList<T> canonical = new ArrayList<>(Objects.requireNonNull(source, familyName + "s"));
        canonical.sort(Comparator.comparing(value -> idExtractor.apply(value).toString()));

        HashSet<ResourceLocation> seen = new HashSet<>();
        for (T value : canonical) {
            Objects.requireNonNull(value, familyName);
            ResourceLocation id = Objects.requireNonNull(idExtractor.apply(value), familyName + " id");
            if (!seen.add(id)) {
                throw new IllegalArgumentException("Duplicate " + familyName + " id: " + id);
            }
        }
        return List.copyOf(canonical);
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

    private static void requirePositive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }
}
