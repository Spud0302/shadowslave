package dev.spud.shadowslave.nightmare.content;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * DESIGN-authored player-facing Nightmare deed primitives.
 *
 * <p>Canon establishes that appraisal can recount what a challenger actually did before
 * presenting a verdict. Canon does not provide a universal deed taxonomy, weighting formula,
 * or generation algorithm. This catalogue therefore provides reusable authored language and
 * deterministic presentation composition only. It does not calculate completion, appraisal
 * grade, progression, rewards, or any canonical Spell score.</p>
 */
public final class NightmareDeedContentCatalog {
    public static final int GENERATOR_VERSION = 1;

    private static final List<DeedDefinition> DEFINITIONS = List.of(
            deed("discovery", "Uncovered What Was Hidden", DeedFamily.DISCOVERY,
                    Set.of("discovery", "knowledge", "truth", "investigation"),
                    "The challenger uncovered knowledge that the conflict had concealed."),
            deed("investigation", "Followed the Broken Thread", DeedFamily.DISCOVERY,
                    Set.of("investigation", "evidence", "truth", "precision"),
                    "The challenger assembled scattered evidence before acting."),
            deed("warning", "Carried the Warning", DeedFamily.PRESERVATION,
                    Set.of("warning", "duty", "guidance", "preservation"),
                    "The challenger delivered a warning while there was still time to answer it."),
            deed("rescue", "Brought Others Through", DeedFamily.PRESERVATION,
                    Set.of("rescue", "guidance", "movement", "preservation"),
                    "The challenger turned personal survival into a path for others."),
            deed("preservation", "Kept What Could Be Saved", DeedFamily.PRESERVATION,
                    Set.of("preservation", "duty", "resolve", "endurance"),
                    "The challenger preserved something the conflict was poised to destroy."),
            deed("sacrifice", "Paid the Necessary Price", DeedFamily.SACRIFICE,
                    Set.of("sacrifice", "duty", "resolve", "preservation"),
                    "The challenger accepted a meaningful cost to change the outcome."),
            deed("negotiation", "Made Enemies Speak", DeedFamily.SOCIAL,
                    Set.of("negotiation", "social", "guidance", "truth"),
                    "The challenger created room for an outcome that force alone could not reach."),
            deed("deception", "Won by False Appearance", DeedFamily.SOCIAL,
                    Set.of("deception", "social", "adaptation", "precision"),
                    "The challenger used deception as a deliberate instrument of the conflict."),
            deed("exposure", "Dragged the Lie Into Light", DeedFamily.SOCIAL,
                    Set.of("exposure", "truth", "evidence", "social"),
                    "The challenger exposed a lie, coercion, or hidden cause at a decisive moment."),
            deed("mercy", "Refused the Easy Cruelty", DeedFamily.CHOICE,
                    Set.of("mercy", "restraint", "social", "preservation"),
                    "The challenger refused a simpler destructive choice when another path remained."),
            deed("retaliation", "Answered Violence With Violence", DeedFamily.CONFLICT,
                    Set.of("retaliation", "combat", "resolve", "precision"),
                    "The challenger met a direct threat and broke its ability to continue the conflict."),
            deed("counterplay", "Turned the Threat Against Itself", DeedFamily.CONFLICT,
                    Set.of("counterplay", "adaptation", "precision", "creatures"),
                    "The challenger learned a threat's nature and exploited that knowledge instead of relying on force alone."),
            deed("adaptation", "Changed Course Before Breaking", DeedFamily.ENDURANCE,
                    Set.of("adaptation", "movement", "endurance", "survival"),
                    "The challenger abandoned a failing plan and survived by changing approach."),
            deed("endurance", "Remained When Retreat Was Easier", DeedFamily.ENDURANCE,
                    Set.of("endurance", "resolve", "duty", "survival"),
                    "The challenger endured pressure long enough for an intended consequence to become possible."),
            deed("defiance", "Refused the Expected Ending", DeedFamily.CHOICE,
                    Set.of("defiance", "adaptation", "resolve", "fate"),
                    "The challenger pursued an outcome that the reconstructed situation did not make easy."),
            deed("destruction", "Removed the Thing Everyone Needed", DeedFamily.CHOICE,
                    Set.of("destruction", "denial", "sacrifice", "adaptation"),
                    "The challenger destroyed or denied a decisive resource so no side could use it as intended.")
    );

    private NightmareDeedContentCatalog() {
    }

    public static List<DeedDefinition> definitions() {
        return DEFINITIONS;
    }

    public static DeedDefinition require(String id) {
        String checked = requireText(id, "id");
        return DEFINITIONS.stream()
                .filter(definition -> definition.id().equals(checked))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown deed definition: " + checked));
    }

    /**
     * Composes presentation-ready deeds from already-recorded scenario evidence.
     *
     * <p>The integer evidence values are used only as a positive/absent signal here. Their
     * magnitudes are deliberately ignored so this helper cannot become an accidental appraisal
     * scoring formula. The seed only breaks ties between equally compatible authored primitives.</p>
     */
    public static List<ComposedDeed> compose(long seed, Map<String, Integer> evidence, int limit) {
        Objects.requireNonNull(evidence, "evidence");
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be positive");
        }
        evidence.forEach((tag, value) -> {
            requireText(tag, "evidence tag");
            if (value == null || value < 0) {
                throw new IllegalArgumentException("evidence values must be non-negative");
            }
        });

        Set<String> positiveTags = new LinkedHashSet<>();
        evidence.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(Map.Entry::getKey)
                .map(String::trim)
                .sorted()
                .forEach(positiveTags::add);

        if (positiveTags.isEmpty()) {
            return List.of();
        }

        List<Candidate> candidates = new ArrayList<>();
        for (DeedDefinition definition : DEFINITIONS) {
            Set<String> matched = new LinkedHashSet<>();
            definition.evidenceTags().stream()
                    .filter(positiveTags::contains)
                    .sorted()
                    .forEach(matched::add);
            if (!matched.isEmpty()) {
                candidates.add(new Candidate(definition, Set.copyOf(matched), tieBreak(seed, definition.id())));
            }
        }

        candidates.sort(Comparator
                .comparingInt((Candidate candidate) -> candidate.matchedTags().size()).reversed()
                .thenComparingLong(Candidate::tieBreak)
                .thenComparing(candidate -> candidate.definition().id()));

        List<ComposedDeed> result = new ArrayList<>();
        for (Candidate candidate : candidates) {
            if (result.size() >= limit) {
                break;
            }
            result.add(new ComposedDeed(
                    candidate.definition().id(),
                    candidate.definition().displayName(),
                    candidate.definition().family(),
                    candidate.matchedTags(),
                    candidate.definition().presentationCue(),
                    GENERATOR_VERSION,
                    seed
            ));
        }
        return List.copyOf(result);
    }

    private static DeedDefinition deed(String id, String displayName, DeedFamily family, Set<String> tags, String cue) {
        return new DeedDefinition(id, displayName, family, tags, cue, EvidenceClassification.DESIGN);
    }

    private static long tieBreak(long seed, String id) {
        long value = seed ^ 0x9E3779B97F4A7C15L;
        for (int i = 0; i < id.length(); i++) {
            value ^= id.charAt(i);
            value *= 0x100000001B3L;
            value ^= value >>> 32;
        }
        return value;
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }

    public enum DeedFamily {
        DISCOVERY,
        PRESERVATION,
        SACRIFICE,
        SOCIAL,
        CHOICE,
        CONFLICT,
        ENDURANCE
    }

    public enum EvidenceClassification {
        DESIGN
    }

    public record DeedDefinition(
            String id,
            String displayName,
            DeedFamily family,
            Set<String> evidenceTags,
            String presentationCue,
            EvidenceClassification classification
    ) {
        public DeedDefinition {
            id = requireText(id, "id");
            displayName = requireText(displayName, "displayName");
            family = Objects.requireNonNull(family, "family");
            evidenceTags = Set.copyOf(Objects.requireNonNull(evidenceTags, "evidenceTags"));
            presentationCue = requireText(presentationCue, "presentationCue");
            classification = Objects.requireNonNull(classification, "classification");
            if (evidenceTags.isEmpty()) {
                throw new IllegalArgumentException("A deed definition must expose evidence tags");
            }
            evidenceTags.forEach(tag -> requireText(tag, "evidence tag"));
        }
    }

    public record ComposedDeed(
            String definitionId,
            String displayName,
            DeedFamily family,
            Set<String> matchedEvidenceTags,
            String presentationCue,
            int generatorVersion,
            long seed
    ) {
        public ComposedDeed {
            definitionId = requireText(definitionId, "definitionId");
            displayName = requireText(displayName, "displayName");
            family = Objects.requireNonNull(family, "family");
            matchedEvidenceTags = Set.copyOf(Objects.requireNonNull(matchedEvidenceTags, "matchedEvidenceTags"));
            presentationCue = requireText(presentationCue, "presentationCue");
            if (matchedEvidenceTags.isEmpty()) {
                throw new IllegalArgumentException("A composed deed must explain which evidence matched it");
            }
            if (generatorVersion < 1) {
                throw new IllegalArgumentException("generatorVersion must be positive");
            }
        }
    }

    private record Candidate(DeedDefinition definition, Set<String> matchedTags, long tieBreak) {
    }
}
