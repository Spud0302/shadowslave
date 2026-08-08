package dev.spud.shadowslave.content.acquisition;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Authored player-facing provenance contexts for already-resolved Memory and Echo acquisition.
 *
 * <p>The caller supplies the authoritative subject kind and source. This catalogue may choose
 * among compatible presentation variants, but it cannot decide how an item or Echo was actually
 * obtained. All exact context text, tags and deterministic selection rules are project DESIGN.</p>
 */
public record MemoryEchoAcquisitionContextCatalog(List<AcquisitionContext> contexts) {
    public static final int GENERATOR_VERSION = 1;

    public MemoryEchoAcquisitionContextCatalog {
        ArrayList<AcquisitionContext> canonical = new ArrayList<>(Objects.requireNonNull(contexts, "contexts"));
        canonical.sort(Comparator.comparing(AcquisitionContext::id));
        HashSet<String> seen = new HashSet<>();
        for (AcquisitionContext context : canonical) {
            Objects.requireNonNull(context, "context");
            if (!seen.add(context.id())) {
                throw new IllegalArgumentException("Duplicate acquisition context id: " + context.id());
            }
        }
        contexts = List.copyOf(canonical);
    }

    public static MemoryEchoAcquisitionContextCatalog waveOne() {
        return new MemoryEchoAcquisitionContextCatalog(List.of(
                context("memory_creature_spoils", SubjectKind.MEMORY, AcquisitionSource.SLAIN_CREATURE,
                        ProvenanceVisibility.KNOWN, Set.of("combat", "creature", "spell_reward"),
                        "The Spell's reward arrives in the wake of a slain Nightmare Creature.",
                        "Provenance: creature-slaying reward; exact drop chance and selection rule unknown."),
                context("memory_transfer_gift", SubjectKind.MEMORY, AcquisitionSource.TRANSFER,
                        ProvenanceVisibility.KNOWN, Set.of("gift", "transfer", "social"),
                        "A deliberate handoff leaves the Memory bound to a new owner.",
                        "Provenance: transferred from another owner; transfer motive and value remain contextual."),
                context("memory_weaver_craft", SubjectKind.MEMORY, AcquisitionSource.ARTIFICIAL_CREATION,
                        ProvenanceVisibility.KNOWN, Set.of("crafted", "weaving", "artificial"),
                        "The Memory was made rather than awarded by a creature kill.",
                        "Provenance: artificial Memory creation; the exact craft method belongs to the resolved content."),
                context("memory_scenario_cache", SubjectKind.MEMORY, AcquisitionSource.AUTHORED_DISCOVERY,
                        ProvenanceVisibility.PARTIAL, Set.of("discovery", "scenario", "cache"),
                        "The Memory is discovered through an authored scenario circumstance rather than a random roll.",
                        "Provenance: scenario-authored discovery. This is project DESIGN, not a canonical Spell reward rule."),
                context("memory_unknown_origin", SubjectKind.MEMORY, AcquisitionSource.UNKNOWN,
                        ProvenanceVisibility.HIDDEN, Set.of("mystery", "unknown", "unverified"),
                        "The Memory is present, but its route into the owner's possession is unresolved.",
                        "Provenance: UNKNOWN. Do not infer a creature kill, transfer or crafting event."),
                context("echo_creature_reward", SubjectKind.ECHO, AcquisitionSource.SLAIN_CREATURE,
                        ProvenanceVisibility.KNOWN, Set.of("combat", "creature", "spell_reward"),
                        "The Echo is received as a rare reward after a Nightmare Creature is slain.",
                        "Provenance: creature-slaying reward; canonical probability and selection formula remain unknown."),
                context("echo_transfer_gift", SubjectKind.ECHO, AcquisitionSource.TRANSFER,
                        ProvenanceVisibility.KNOWN, Set.of("gift", "transfer", "social"),
                        "Ownership changes through a deliberate transfer rather than a new creature reward.",
                        "Provenance: transferred Echo; prior owner and reason are supplied by authoritative state."),
                context("echo_artificial_forge", SubjectKind.ECHO, AcquisitionSource.ARTIFICIAL_CREATION,
                        ProvenanceVisibility.KNOWN, Set.of("artificial", "crafted", "enchanter"),
                        "The Echo was deliberately created and carries no slain-creature provenance.",
                        "Provenance: artificial Echo. Do not invent creature Rank, Class or source identity."),
                context("echo_scenario_ally", SubjectKind.ECHO, AcquisitionSource.AUTHORED_DISCOVERY,
                        ProvenanceVisibility.PARTIAL, Set.of("discovery", "scenario", "ally"),
                        "The Echo enters the player's story through an authored scenario handoff or discovery.",
                        "Provenance: scenario-authored acquisition. This is project DESIGN, not a canonical reward formula."),
                context("echo_unknown_origin", SubjectKind.ECHO, AcquisitionSource.UNKNOWN,
                        ProvenanceVisibility.HIDDEN, Set.of("mystery", "unknown", "unverified"),
                        "The Echo's present ownership is known while its acquisition history remains deliberately unresolved.",
                        "Provenance: UNKNOWN. No creature source or artificial creator may be inferred." )
        ));
    }

    public AcquisitionContext find(String id) {
        String stable = stableId(id);
        return contexts.stream()
                .filter(context -> context.id().equals(stable))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown acquisition context id: " + stable));
    }

    /**
     * Selects a presentation variant only after authoritative source resolution.
     * Positive evidence may bias equally valid authored variants; evidence magnitude is ignored.
     */
    public ResolvedContext compose(long seed, SubjectKind subjectKind, AcquisitionSource source, Map<String, Integer> evidence) {
        Objects.requireNonNull(subjectKind, "subjectKind");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(evidence, "evidence");

        Set<String> positiveEvidence = normalizePositiveEvidence(evidence);
        List<AcquisitionContext> compatible = contexts.stream()
                .filter(context -> context.subjectKind() == subjectKind && context.source() == source)
                .toList();
        if (compatible.isEmpty()) {
            throw new IllegalArgumentException("No authored acquisition context for " + subjectKind + " / " + source);
        }

        int bestMatches = compatible.stream()
                .mapToInt(context -> intersectionSize(context.evidenceTags(), positiveEvidence))
                .max()
                .orElseThrow();
        List<AcquisitionContext> finalists = compatible.stream()
                .filter(context -> intersectionSize(context.evidenceTags(), positiveEvidence) == bestMatches)
                .sorted(Comparator.comparing(AcquisitionContext::id))
                .toList();
        int index = Math.floorMod(mix(seed, subjectKind, source, positiveEvidence), finalists.size());
        AcquisitionContext selected = finalists.get(index);
        Set<String> matched = selected.evidenceTags().stream()
                .filter(positiveEvidence::contains)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        return new ResolvedContext(GENERATOR_VERSION, seed, selected.id(), subjectKind, source,
                selected.visibility(), matched, selected.acquisitionCue(), selected.provenanceLine());
    }

    public enum SubjectKind { MEMORY, ECHO }

    public enum AcquisitionSource {
        SLAIN_CREATURE,
        TRANSFER,
        ARTIFICIAL_CREATION,
        AUTHORED_DISCOVERY,
        UNKNOWN
    }

    public enum ProvenanceVisibility { KNOWN, PARTIAL, HIDDEN }

    public record AcquisitionContext(
            String id,
            SubjectKind subjectKind,
            AcquisitionSource source,
            ProvenanceVisibility visibility,
            Set<String> evidenceTags,
            String acquisitionCue,
            String provenanceLine,
            String evidenceClassification
    ) {
        public AcquisitionContext {
            id = stableId(id);
            subjectKind = Objects.requireNonNull(subjectKind, "subjectKind");
            source = Objects.requireNonNull(source, "source");
            visibility = Objects.requireNonNull(visibility, "visibility");
            evidenceTags = normalizedTags(evidenceTags, "evidenceTags");
            acquisitionCue = text(acquisitionCue, "acquisitionCue");
            provenanceLine = text(provenanceLine, "provenanceLine");
            evidenceClassification = text(evidenceClassification, "evidenceClassification");
        }
    }

    public record ResolvedContext(
            int generatorVersion,
            long seed,
            String contextId,
            SubjectKind subjectKind,
            AcquisitionSource source,
            ProvenanceVisibility visibility,
            Set<String> matchedEvidence,
            String acquisitionCue,
            String provenanceLine
    ) {
        public ResolvedContext {
            if (generatorVersion < 1) {
                throw new IllegalArgumentException("generatorVersion must be positive");
            }
            contextId = stableId(contextId);
            subjectKind = Objects.requireNonNull(subjectKind, "subjectKind");
            source = Objects.requireNonNull(source, "source");
            visibility = Objects.requireNonNull(visibility, "visibility");
            matchedEvidence = Set.copyOf(Objects.requireNonNull(matchedEvidence, "matchedEvidence"));
            acquisitionCue = text(acquisitionCue, "acquisitionCue");
            provenanceLine = text(provenanceLine, "provenanceLine");
        }
    }

    private static AcquisitionContext context(
            String id,
            SubjectKind subjectKind,
            AcquisitionSource source,
            ProvenanceVisibility visibility,
            Set<String> tags,
            String cue,
            String provenance
    ) {
        return new AcquisitionContext(id, subjectKind, source, visibility, tags, cue, provenance, "DESIGN");
    }

    private static Set<String> normalizePositiveEvidence(Map<String, Integer> evidence) {
        HashSet<String> positive = new HashSet<>();
        for (Map.Entry<String, Integer> entry : evidence.entrySet()) {
            String tag = stableId(entry.getKey());
            Integer magnitude = Objects.requireNonNull(entry.getValue(), "evidence magnitude");
            if (magnitude < 0) {
                throw new IllegalArgumentException("Evidence magnitude cannot be negative: " + tag);
            }
            if (magnitude > 0) {
                positive.add(tag);
            }
        }
        return Set.copyOf(positive);
    }

    private static int intersectionSize(Set<String> left, Set<String> right) {
        int matches = 0;
        for (String value : left) {
            if (right.contains(value)) {
                matches++;
            }
        }
        return matches;
    }

    private static int mix(long seed, SubjectKind subjectKind, AcquisitionSource source, Set<String> evidence) {
        long mixed = seed ^ 0x9E3779B97F4A7C15L;
        mixed = Long.rotateLeft(mixed ^ subjectKind.name().hashCode(), 17);
        mixed = Long.rotateLeft(mixed ^ source.name().hashCode(), 23);
        for (String tag : evidence.stream().sorted().toList()) {
            mixed = Long.rotateLeft(mixed ^ tag.hashCode(), 11) * 0xBF58476D1CE4E5B9L;
        }
        return (int) (mixed ^ (mixed >>> 32));
    }

    private static Set<String> normalizedTags(Set<String> source, String name) {
        HashSet<String> normalized = new HashSet<>();
        for (String value : Objects.requireNonNull(source, name)) {
            normalized.add(stableId(value));
        }
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be empty");
        }
        return Set.copyOf(normalized);
    }

    private static String stableId(String value) {
        String checked = text(value, "id").toLowerCase(Locale.ROOT).replace(' ', '_');
        if (!checked.matches("[a-z0-9_]+")) {
            throw new IllegalArgumentException("id must contain only lowercase letters, numbers and underscores");
        }
        return checked;
    }

    private static String text(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }
}
