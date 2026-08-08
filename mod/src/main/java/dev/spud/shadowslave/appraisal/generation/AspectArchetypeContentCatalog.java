package dev.spud.shadowslave.appraisal.generation;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Player-facing presentation for the authored Aspect archetype primitives.
 *
 * <p>The archetype identities themselves already belong to the Java-owned
 * procedural identity catalogue. This class only explains an already-resolved
 * nature/archetype pair; it never chooses, rerolls, awards, persists, ranks or
 * executes an Aspect.</p>
 *
 * <p>All exact role promises, hooks and boundaries in this file are Minecraft
 * DESIGN. They are not claimed to be a canonical Nightmare Spell naming or
 * Aspect-generation taxonomy.</p>
 */
public final class AspectArchetypeContentCatalog {
    public static final String PRESENTATION_VERSION = "aspect-archetype-presentation-v1";

    private static final IdentityPrimitiveCatalog IDENTITY_CATALOG = ExpandedIdentityContentCatalog.waveOne();
    private static final Map<ResourceLocation, IdentityPrimitiveCatalog.Nature> NATURES = indexNatures();
    private static final Map<ResourceLocation, IdentityPrimitiveCatalog.Archetype> ARCHETYPES = indexArchetypes();
    private static final List<ArchetypeProfile> PROFILES = List.of(
            profile(
                    "bearer",
                    "Carrier",
                    "Your Aspect is framed around carrying responsibility, pressure, or a charge through danger rather than merely possessing power.",
                    List.of(
                            "Protect something that cannot protect itself.",
                            "Keep moving while a burden makes the safer choice harder.",
                            "Decide when carrying the obligation farther is worth the personal cost."
                    ),
                    "Bearer is a narrative role cue, not extra inventory capacity, universal durability, or a rule that every power must involve a literal carried object.",
                    Set.of("duty", "endurance", "burden")
            ),
            profile(
                    "keeper",
                    "Custodian",
                    "Your Aspect is framed around preserving, maintaining, sheltering, or refusing to surrender something important.",
                    List.of(
                            "Hold a position or resource together under pressure.",
                            "Choose what deserves preservation when not everything can be saved.",
                            "Create time or safety for others by maintaining a threatened advantage."
                    ),
                    "Keeper does not grant ownership, inviolable wards, absolute protection, or authority over anything merely described as being kept.",
                    Set.of("duty", "preservation", "stability")
            ),
            profile(
                    "last",
                    "Final Holdout",
                    "Your Aspect is framed around resolve after support, certainty, or easier alternatives have been stripped away.",
                    List.of(
                            "Continue when retreat would abandon the remaining objective.",
                            "Turn isolation into a deliberate tactical choice rather than an automatic defeat.",
                            "Decide whether sacrifice preserves something meaningful or merely wastes what remains."
                    ),
                    "Last is not a canonical prophecy of being the final survivor, does not guarantee heroic sacrifice, and does not make solitary play mechanically superior.",
                    Set.of("resolve", "sacrifice", "aftermath")
            ),
            profile(
                    "pilgrim",
                    "Wayfarer",
                    "Your Aspect is framed around a purposeful journey in which reaching, leaving, or enduring the road matters as much as raw confrontation.",
                    List.of(
                            "Commit to a destination despite incomplete safety.",
                            "Read obstacles as route problems instead of default combat encounters.",
                            "Choose what principles or cargo remain worth carrying to the destination."
                    ),
                    "Pilgrim does not reveal unknown destinations, bypass barriers, create roads, or imply a religious identity for the generated character.",
                    Set.of("path", "resolve", "journey")
            ),
            profile(
                    "seeker",
                    "Investigator",
                    "Your Aspect is framed around finding what is hidden, overlooked, weak, missing, or not yet understood.",
                    List.of(
                            "Investigate before committing to a dangerous interpretation.",
                            "Pursue clues that change how a conflict can be approached.",
                            "Trade speed for better information when uncertainty is itself the threat."
                    ),
                    "Seeker does not provide omniscience, prophecy, automatic objective discovery, truth detection, or knowledge the character has not earned.",
                    Set.of("perception", "curiosity", "discovery")
            ),
            profile(
                    "sentinel",
                    "Watchkeeper",
                    "Your Aspect is framed around vigilance, warning, thresholds, and deciding when observation must become intervention.",
                    List.of(
                            "Notice danger early enough to change another person's options.",
                            "Hold or monitor a boundary while pressure builds elsewhere.",
                            "Choose when a warning is more valuable than preserving concealment."
                    ),
                    "Sentinel does not grant perfect detection, immunity to ambush, universal aggro control, or a supernatural right to command those being warned.",
                    Set.of("warning", "duty", "vigilance")
            ),
            profile(
                    "voice",
                    "Herald",
                    "Your Aspect is framed around signals, declarations, coordination, and making information matter at the moment others can still act on it.",
                    List.of(
                            "Turn knowledge into a warning or rallying message.",
                            "Coordinate allies whose choices depend on timing and clarity.",
                            "Decide when speaking openly is worth revealing your position or intent."
                    ),
                    "Voice does not imply mind control, magical persuasion, compulsory obedience, universal communication range, or canonical social authority.",
                    Set.of("signal", "social", "coordination")
            ),
            profile(
                    "wanderer",
                    "Drifter",
                    "Your Aspect is framed around movement, escape, repositioning, and surviving by refusing to be fixed where an enemy expects you.",
                    List.of(
                            "Reposition before pressure turns into entrapment.",
                            "Find value in alternate routes rather than forcing one defended path.",
                            "Choose when leaving a place is adaptation rather than abandonment."
                    ),
                    "Wanderer does not grant teleportation, phasing, infinite stamina, freedom from terrain, or automatic escape from authored constraints.",
                    Set.of("movement", "escape", "adaptation")
            ),
            profile(
                    "weaver",
                    "Binder",
                    "Your Aspect is framed around precise relationships between separate things: links, timing, cooperation, patterns, and deliberate assembly.",
                    List.of(
                            "Create or exploit a meaningful connection between separate resources or actors.",
                            "Solve a problem through sequencing and precision rather than raw force.",
                            "Break a dangerous dependency when preserving the link would be worse."
                    ),
                    "Weaver is a project archetype name only. It does not grant fate manipulation, sorcery associated with Weaver, Memory weaving, mind binding, or ownership over linked actors.",
                    Set.of("connection", "precision", "coordination")
            ),
            profile(
                    "witness",
                    "Observer",
                    "Your Aspect is framed around seeing, remembering, verifying, or preserving information whose value depends on being noticed accurately.",
                    List.of(
                            "Observe before acting when the distinction between appearances matters.",
                            "Preserve evidence that another actor would rather erase or distort.",
                            "Decide when knowledge creates a responsibility to warn, reveal, or remain silent."
                    ),
                    "Witness does not grant omniscience, perfect memory, lie detection, prophecy, remote viewing, or a canonical obligation to disclose everything perceived.",
                    Set.of("witness", "perception", "evidence")
            )
    );
    private static final Map<ResourceLocation, ArchetypeProfile> PROFILE_BY_ID = indexProfiles();

    private AspectArchetypeContentCatalog() {
    }

    public static List<ArchetypeProfile> all() {
        return PROFILES;
    }

    public static ArchetypeProfile requireProfile(ResourceLocation archetypeId) {
        ArchetypeProfile profile = PROFILE_BY_ID.get(Objects.requireNonNull(archetypeId, "archetypeId"));
        if (profile == null) {
            throw new IllegalArgumentException("Unknown Aspect archetype content id: " + archetypeId);
        }
        return profile;
    }

    /**
     * Composes presentation for an already-resolved nature/archetype pair.
     * The pair is never altered and no procedural selection occurs here.
     */
    public static AspectArchetypePresentation compose(ResourceLocation natureId, ResourceLocation archetypeId) {
        IdentityPrimitiveCatalog.Nature nature = NATURES.get(Objects.requireNonNull(natureId, "natureId"));
        if (nature == null) {
            throw new IllegalArgumentException("Unknown Aspect nature id: " + natureId);
        }
        IdentityPrimitiveCatalog.Archetype archetype = ARCHETYPES.get(Objects.requireNonNull(archetypeId, "archetypeId"));
        if (archetype == null) {
            throw new IllegalArgumentException("Unknown Aspect archetype id: " + archetypeId);
        }
        ArchetypeProfile profile = requireProfile(archetypeId);

        return new AspectArchetypePresentation(
                PRESENTATION_VERSION,
                LoreClassification.DESIGN,
                nature.id(),
                archetype.id(),
                archetype.formatName(nature.nameToken()),
                profile.roleLabel(),
                profile.rolePromise(),
                profile.decisionHooks(),
                profile.powerBoundary(),
                profile.presentationTags()
        );
    }

    public enum LoreClassification {
        DESIGN
    }

    public record ArchetypeProfile(
            ResourceLocation archetypeId,
            LoreClassification loreClassification,
            String roleLabel,
            String rolePromise,
            List<String> decisionHooks,
            String powerBoundary,
            Set<String> presentationTags
    ) {
        public ArchetypeProfile {
            archetypeId = Objects.requireNonNull(archetypeId, "archetypeId");
            loreClassification = Objects.requireNonNull(loreClassification, "loreClassification");
            roleLabel = requireText(roleLabel, "roleLabel");
            rolePromise = requireText(rolePromise, "rolePromise");
            decisionHooks = List.copyOf(Objects.requireNonNull(decisionHooks, "decisionHooks"));
            if (decisionHooks.size() < 2 || decisionHooks.stream().anyMatch(value -> value == null || value.isBlank())) {
                throw new IllegalArgumentException("decisionHooks must contain at least two non-blank entries");
            }
            powerBoundary = requireText(powerBoundary, "powerBoundary");
            presentationTags = Set.copyOf(Objects.requireNonNull(presentationTags, "presentationTags"));
            if (presentationTags.isEmpty() || presentationTags.stream().anyMatch(value -> value == null || value.isBlank())) {
                throw new IllegalArgumentException("presentationTags must be non-empty and non-blank");
            }
        }
    }

    public record AspectArchetypePresentation(
            String presentationVersion,
            LoreClassification loreClassification,
            ResourceLocation natureId,
            ResourceLocation archetypeId,
            String formalName,
            String roleLabel,
            String rolePromise,
            List<String> decisionHooks,
            String powerBoundary,
            Set<String> presentationTags
    ) {
        public AspectArchetypePresentation {
            presentationVersion = requireText(presentationVersion, "presentationVersion");
            loreClassification = Objects.requireNonNull(loreClassification, "loreClassification");
            natureId = Objects.requireNonNull(natureId, "natureId");
            archetypeId = Objects.requireNonNull(archetypeId, "archetypeId");
            formalName = requireText(formalName, "formalName");
            roleLabel = requireText(roleLabel, "roleLabel");
            rolePromise = requireText(rolePromise, "rolePromise");
            decisionHooks = List.copyOf(Objects.requireNonNull(decisionHooks, "decisionHooks"));
            powerBoundary = requireText(powerBoundary, "powerBoundary");
            presentationTags = Set.copyOf(Objects.requireNonNull(presentationTags, "presentationTags"));
        }
    }

    private static ArchetypeProfile profile(
            String path,
            String roleLabel,
            String rolePromise,
            List<String> decisionHooks,
            String powerBoundary,
            Set<String> presentationTags
    ) {
        return new ArchetypeProfile(
                id("generation/archetype/" + path),
                LoreClassification.DESIGN,
                roleLabel,
                rolePromise,
                decisionHooks,
                powerBoundary,
                presentationTags
        );
    }

    private static Map<ResourceLocation, IdentityPrimitiveCatalog.Nature> indexNatures() {
        HashMap<ResourceLocation, IdentityPrimitiveCatalog.Nature> values = new HashMap<>();
        for (IdentityPrimitiveCatalog.Nature nature : IDENTITY_CATALOG.natures()) {
            if (values.put(nature.id(), nature) != null) {
                throw new IllegalStateException("Duplicate generated nature id: " + nature.id());
            }
        }
        return Map.copyOf(values);
    }

    private static Map<ResourceLocation, IdentityPrimitiveCatalog.Archetype> indexArchetypes() {
        HashMap<ResourceLocation, IdentityPrimitiveCatalog.Archetype> values = new HashMap<>();
        for (IdentityPrimitiveCatalog.Archetype archetype : IDENTITY_CATALOG.archetypes()) {
            if (values.put(archetype.id(), archetype) != null) {
                throw new IllegalStateException("Duplicate generated archetype id: " + archetype.id());
            }
        }
        return Map.copyOf(values);
    }

    private static Map<ResourceLocation, ArchetypeProfile> indexProfiles() {
        HashMap<ResourceLocation, ArchetypeProfile> values = new HashMap<>();
        for (ArchetypeProfile profile : PROFILES) {
            if (!ARCHETYPES.containsKey(profile.archetypeId())) {
                throw new IllegalStateException("Presentation references unknown generated archetype: " + profile.archetypeId());
            }
            if (values.put(profile.archetypeId(), profile) != null) {
                throw new IllegalStateException("Duplicate Aspect archetype presentation id: " + profile.archetypeId());
            }
        }
        if (!values.keySet().equals(ARCHETYPES.keySet())) {
            throw new IllegalStateException("Aspect archetype presentation must cover every generated archetype exactly once");
        }
        return Map.copyOf(values);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("shadowslave", path);
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }
}
