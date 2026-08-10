package dev.spud.shadowslave.nightmare.content;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** DESIGN-only presentation for an already-authorized Nightmare faction re-encounter context. */
public final class NightmareFactionReencounterContextCatalog {
    public static final String GENERATOR_VERSION = "nightmare-faction-reencounter-context-v1";

    private NightmareFactionReencounterContextCatalog() {}

    public enum ContextKind {
        KNOWN_HISTORY,
        CHANGED_CIRCUMSTANCE,
        OPEN_BUSINESS,
        NO_CURRENT_COMMITMENT
    }

    public record Primitive(String id, ContextKind kind, String title, String contextRead,
                            String conversationPrompt, List<String> playerResponses, Set<String> affinityTags,
                            List<String> presentationCues, String antiOverclaimBoundary) {
        public Primitive {
            id = stableId(id);
            kind = Objects.requireNonNull(kind, "kind");
            title = text(title, "title");
            contextRead = text(contextRead, "contextRead");
            conversationPrompt = text(conversationPrompt, "conversationPrompt");
            playerResponses = exactTextList(playerResponses, 3, "playerResponses");
            affinityTags = nonEmptyTags(affinityTags);
            presentationCues = exactTextList(presentationCues, 2, "presentationCues");
            antiOverclaimBoundary = text(antiOverclaimBoundary, "antiOverclaimBoundary");
        }
    }

    public record Selection(String generatorVersion, long seed, String scenarioId, String factionId,
                            String priorHistoryId, String reencounterId, ContextKind kind,
                            Primitive primitive, String presentationCue, Set<String> matchedEvidenceTags) {
        public Selection {
            generatorVersion = text(generatorVersion, "generatorVersion");
            scenarioId = opaqueId(scenarioId, "scenarioId");
            factionId = opaqueId(factionId, "factionId");
            priorHistoryId = opaqueId(priorHistoryId, "priorHistoryId");
            reencounterId = opaqueId(reencounterId, "reencounterId");
            kind = Objects.requireNonNull(kind, "kind");
            primitive = Objects.requireNonNull(primitive, "primitive");
            if (primitive.kind() != kind) throw new IllegalArgumentException("primitive kind must match supplied context kind");
            presentationCue = text(presentationCue, "presentationCue");
            matchedEvidenceTags = Set.copyOf(Objects.requireNonNull(matchedEvidenceTags, "matchedEvidenceTags"));
        }
    }

    public static List<Primitive> waveOne() {
        return List.of(
                p("known_history_recorded", ContextKind.KNOWN_HISTORY, "Prior Contact Recorded",
                        "This faction is not unknown to you: a bounded prior interaction is already part of the record.",
                        "What from that earlier contact is relevant to this meeting without deciding the faction's present stance?",
                        "Recall only what actually happened before.", "Ask what has changed since then.", "Treat current intent as a fresh question.",
                        Set.of("history", "record", "contact"),
                        "A prior-contact marker appears beside the current meeting.", "The old entry can be opened without replacing the current context.",
                        "Known history cannot establish current trust, hostility, allegiance, reputation, motive, access, active terms, or future behavior."),
                p("known_history_scope", ContextKind.KNOWN_HISTORY, "History Has a Scope",
                        "The earlier interaction supplies context, but only within the people, place, and matter that were actually recorded.",
                        "Which part of the old record belongs in this conversation, and which part must be checked again?",
                        "Name the old fact you are relying on.", "Keep broader assumptions out of the record.", "Ask for present confirmation where it matters.",
                        Set.of("history", "scope", "verification"),
                        "The earlier entry is shown with a clear scope boundary.", "Past context and present observation remain visually separate.",
                        "A scoped history entry cannot be generalized into faction-wide loyalty, permanent permission, truth, reputation, ownership, or guaranteed cooperation."),
                p("known_history_context_not_verdict", ContextKind.KNOWN_HISTORY, "Context, Not a Verdict",
                        "What happened before can inform the meeting without deciding what this faction thinks or will do now.",
                        "How do you use the old context while leaving current intent open?",
                        "Refer to the old event precisely.", "Ask for the faction's current position.", "Leave prediction out of the summary.",
                        Set.of("history", "relationship", "uncertainty"),
                        "The old interaction is summarized without a relationship score.", "Current words and actions remain their own entry.",
                        "Past interaction cannot become a trust score, enemy flag, allegiance state, reputation value, truth verdict, or prediction of future conduct."),
                p("known_history_compare_current", ContextKind.KNOWN_HISTORY, "Compare Past and Present",
                        "A prior interaction exists, giving you something concrete to compare with what is happening now.",
                        "What is still the same, what is different, and what remains unknown?",
                        "Compare one present fact with the old record.", "Keep differences descriptive rather than accusatory.", "Record anything that still cannot be established.",
                        Set.of("history", "compare", "uncertainty"),
                        "Past and present notes are placed side by side.", "Differences are highlighted without a guilt or deception marker.",
                        "Comparison cannot prove deception, bad faith, guilt, loyalty, faction-wide policy, reputation change, or which explanation is true."),

                p("changed_circumstance_shifted", ContextKind.CHANGED_CIRCUMSTANCE, "Circumstances Have Shifted",
                        "The current meeting is taking place under circumstances that differ from the previous recorded interaction.",
                        "Which old assumptions need to be checked before you act on them again?",
                        "Identify the changed circumstance that matters now.", "Recheck any decision that depended on the old context.", "Keep unchanged facts separate from changed ones.",
                        Set.of("change", "context", "verification"),
                        "A changed-context marker separates the old meeting from the current one.", "The previous record remains visible as history rather than current state.",
                        "Changed circumstances cannot by themselves establish current hostility, trust, route safety, ownership, scarcity, allegiance, active terms, or blame."),
                p("changed_circumstance_recheck", ContextKind.CHANGED_CIRCUMSTANCE, "Recheck the Old Assumption",
                        "Something relevant to this meeting is no longer represented by the old context alone.",
                        "What did your earlier decision assume that now requires a fresh check?",
                        "State the old assumption narrowly.", "Look for a present observation before relying on it.", "Leave unrelated history unchanged.",
                        Set.of("change", "assumption", "verification"),
                        "The old assumption is flagged for rechecking rather than erased.", "Current evidence can be attached without rewriting the past entry.",
                        "A need to recheck cannot infer a hidden cause, deception, worsening danger, improved safety, faction motive, reputation, or future outcome."),
                p("changed_circumstance_old_context", ContextKind.CHANGED_CIRCUMSTANCE, "Old Context Needs Renewal",
                        "The earlier interaction remains useful history, but it is not sufficient by itself to describe the present meeting.",
                        "What current fact should be established before the old context is used again?",
                        "Ask what is true now.", "Keep the old record intact.", "Treat any new condition as a separate fact.",
                        Set.of("change", "history", "current"),
                        "The current meeting opens a fresh context entry linked to the old one.", "No old state is silently copied forward.",
                        "Renewing context cannot manufacture current permission, current danger, faction membership, ownership, trust, debt, active commitment, or truth."),
                p("changed_circumstance_present_first", ContextKind.CHANGED_CIRCUMSTANCE, "Start From What Is Current",
                        "The recorded change means present conditions must lead this encounter rather than inherited assumptions from the last one.",
                        "What can you establish here and now before relying on the past?",
                        "Observe the current situation first.", "Use history only where it still fits.", "Mark any remaining mismatch as unresolved.",
                        Set.of("change", "current", "observation"),
                        "Current observations are placed before historical context in the meeting summary.", "Unresolved differences remain visible instead of being auto-resolved.",
                        "Present-first framing cannot decide motive, truth, hostility, allegiance, reputation, access legality, resource state, or terminal Nightmare outcome."),

                p("open_business_one_matter", ContextKind.OPEN_BUSINESS, "One Matter Remains Open",
                        "A bounded matter from the prior interaction is still unresolved at the start of this meeting.",
                        "Do you return to that matter now, clarify its scope, or leave it open?",
                        "Ask directly about the unresolved point.", "Confirm whether its scope has changed.", "Leave it open if the present meeting cannot settle it.",
                        Set.of("open", "matter", "terms"),
                        "The unresolved matter is surfaced without reopening unrelated history.", "Its open status is shown separately from any current relationship read.",
                        "Open business cannot establish that an old offer is still valid, set a price, prove obligation, create debt, change reputation, or force agreement."),
                p("open_business_question_returns", ContextKind.OPEN_BUSINESS, "A Prior Question Returns",
                        "The earlier interaction left a question that remains relevant and unsettled in this re-encounter.",
                        "What new information, if any, can narrow the question without pretending it was already answered?",
                        "Restate the unresolved question.", "Ask for a current answer or source.", "Preserve the gap if nothing new establishes it.",
                        Set.of("open", "question", "information"),
                        "The earlier question is linked to the new exchange.", "No answer is prefilled from repetition or confidence.",
                        "A returning question cannot become truth, guilt, confession, motive, reputation, allegiance, or certainty merely because it remains important."),
                p("open_business_unfinished_terms", ContextKind.OPEN_BUSINESS, "Unfinished Terms to Revisit",
                        "A scoped set of terms or conditions remains unfinished rather than completed, rejected, or forgotten.",
                        "Which part is still open, and what would count as a new decision rather than a continuation by assumption?",
                        "Name only the term still open.", "Ask whether either side still wishes to discuss it.", "Record a new decision separately if one occurs.",
                        Set.of("open", "terms", "agreement"),
                        "Unfinished terms are shown as open history, not an active contract by default.", "Any new agreement requires its own current-state entry.",
                        "Unfinished terms cannot automatically create a current commitment, transfer resources, grant access, bind either side, alter allegiance, or score reputation."),
                p("open_business_carry_forward", ContextKind.OPEN_BUSINESS, "Carry Forward Only the Open Point",
                        "The re-encounter inherits one unresolved thread, not the whole emotional or political meaning of the previous interaction.",
                        "How do you keep the open point visible without importing assumptions that were never established?",
                        "Carry forward the exact unresolved thread.", "Leave settled history in the past entry.", "Ask for fresh evidence before expanding its meaning.",
                        Set.of("open", "scope", "uncertainty"),
                        "Only the unresolved thread is pinned to the current meeting.", "Past cooperation and dispute remain separate historical context.",
                        "Carrying one issue forward cannot infer current trust, hostility, allegiance, faction policy, truth, blame, active resources, or future behavior."),

                p("no_commitment_none_active", ContextKind.NO_CURRENT_COMMITMENT, "No Active Commitment",
                        "No current bounded commitment between you and this faction is attached to this meeting.",
                        "What, if anything, do you want to establish now without treating history as an existing promise?",
                        "Begin with the present issue.", "Refer to old history only as context.", "Make any new commitment explicit rather than assumed.",
                        Set.of("no_commitment", "current", "terms"),
                        "The meeting opens with no active-commitment marker.", "Historical agreements remain visible only in their own records.",
                        "No current commitment does not mean hostility, friendship, neutrality, freedom from consequence, no obligations elsewhere, or any particular reputation."),
                p("no_commitment_history_without_terms", ContextKind.NO_CURRENT_COMMITMENT, "History Without Current Terms",
                        "Previous contact exists, but there are no current terms being carried into this encounter as a commitment.",
                        "Which past facts are useful context without becoming present obligations?",
                        "Recall only the relevant history.", "Ask before assuming a prior arrangement continues.", "Keep any new terms separate and explicit.",
                        Set.of("no_commitment", "history", "scope"),
                        "Past entries are available without an active-terms banner.", "The present conversation starts from its own state.",
                        "Historical contact without current terms cannot establish access, debt, ownership, allegiance, trust, reputation, or a promise of future cooperation."),
                p("no_commitment_no_inherited_promise", ContextKind.NO_CURRENT_COMMITMENT, "No Inherited Promise",
                        "Nothing in the current commitment state turns the previous interaction into a standing promise for this meeting.",
                        "What needs to be asked or agreed again before you rely on it now?",
                        "Ask for current consent where it matters.", "Separate courtesy from commitment.", "Record any new promise as a new event.",
                        Set.of("no_commitment", "promise", "consent"),
                        "The interface avoids carrying an old promise badge into the new encounter.", "A fresh promise, if one occurs, receives a separate current entry.",
                        "Absence of an inherited promise cannot decide goodwill, hostility, consent to unrelated actions, allegiance, reputation, access, or future willingness."),
                p("no_commitment_start_current", ContextKind.NO_CURRENT_COMMITMENT, "Start Without Active Terms",
                        "This meeting begins without an active commitment governing what either side must do next.",
                        "What current question should be addressed before any new terms are proposed?",
                        "Clarify the immediate issue.", "Ask what each side is willing to discuss now.", "Leave the meeting uncommitted if no terms are reached.",
                        Set.of("no_commitment", "current", "conversation"),
                        "The conversation opens without a contract or obligation overlay.", "New terms are presented only after a separate current decision.",
                        "Starting without active terms cannot erase history, establish neutrality, waive claims, settle truth, change faction stance, modify reputation, or predict the outcome."));
    }

    public static Selection compose(long seed, String scenarioId, String factionId, String priorHistoryId,
                                    String reencounterId, ContextKind kind, Map<String, Integer> evidence) {
        String checkedScenario = opaqueId(scenarioId, "scenarioId");
        String checkedFaction = opaqueId(factionId, "factionId");
        String checkedHistory = opaqueId(priorHistoryId, "priorHistoryId");
        String checkedReencounter = opaqueId(reencounterId, "reencounterId");
        ContextKind checkedKind = Objects.requireNonNull(kind, "kind");
        Set<String> positiveEvidence = positiveEvidence(evidence);

        List<Primitive> candidates = waveOne().stream()
                .filter(primitive -> primitive.kind() == checkedKind)
                .sorted(Comparator.comparing(Primitive::id))
                .toList();
        if (candidates.isEmpty()) throw new IllegalArgumentException("no faction reencounter presentation for supplied kind");

        int bestMatch = candidates.stream().mapToInt(p -> overlap(p.affinityTags(), positiveEvidence)).max().orElse(0);
        List<Primitive> preferred = bestMatch > 0
                ? candidates.stream().filter(p -> overlap(p.affinityTags(), positiveEvidence) == bestMatch).toList()
                : candidates;

        String authorityKey = checkedScenario + "|" + checkedFaction + "|" + checkedHistory + "|"
                + checkedReencounter + "|" + checkedKind.name() + "|"
                + positiveEvidence.stream().sorted().collect(Collectors.joining(","));
        Primitive primitive = preferred.get(index(seed, authorityKey + "|primitive", preferred.size()));
        String cue = primitive.presentationCues().get(index(seed,
                authorityKey + "|" + primitive.id() + "|cue", primitive.presentationCues().size()));
        Set<String> matched = primitive.affinityTags().stream().filter(positiveEvidence::contains).sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new Selection(GENERATOR_VERSION, seed, checkedScenario, checkedFaction, checkedHistory,
                checkedReencounter, checkedKind, primitive, cue, matched);
    }

    public static Primitive requirePrimitive(String id) {
        String checked = stableId(id);
        return waveOne().stream().filter(p -> p.id().equals(checked)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown faction reencounter context primitive: " + checked));
    }

    private static Primitive p(String id, ContextKind kind, String title, String contextRead,
                               String conversationPrompt, String response1, String response2, String response3,
                               Set<String> affinityTags, String cue1, String cue2, String antiOverclaimBoundary) {
        return new Primitive(id, kind, title, contextRead, conversationPrompt,
                List.of(response1, response2, response3), affinityTags, List.of(cue1, cue2), antiOverclaimBoundary);
    }

    private static Set<String> positiveEvidence(Map<String, Integer> evidence) {
        Objects.requireNonNull(evidence, "evidence");
        List<String> tags = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : evidence.entrySet()) {
            String tag = stableId(entry.getKey());
            Integer value = Objects.requireNonNull(entry.getValue(), "evidence value");
            if (value < 0) throw new IllegalArgumentException("evidence cannot be negative");
            if (value > 0) tags.add(tag);
        }
        tags.sort(String::compareTo);
        return new LinkedHashSet<>(tags);
    }

    private static int overlap(Set<String> left, Set<String> right) {
        int count = 0;
        for (String tag : left) if (right.contains(tag)) count++;
        return count;
    }

    private static int index(long seed, String key, int bound) {
        if (bound <= 0) throw new IllegalArgumentException("bound must be positive");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(ByteBuffer.allocate(Long.BYTES).putLong(seed).array());
            byte[] bytes = digest.digest(key.getBytes(StandardCharsets.UTF_8));
            int raw = ByteBuffer.wrap(bytes, 0, Integer.BYTES).getInt();
            return Math.floorMod(raw, bound);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

    private static String stableId(String value) {
        String normalized = text(value, "id").toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z0-9][a-z0-9_-]*")) throw new IllegalArgumentException("invalid stable id: " + value);
        return normalized;
    }

    private static String opaqueId(String value, String field) {
        return text(value, field);
    }

    private static String text(String value, String field) {
        Objects.requireNonNull(value, field);
        if (value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
        return value;
    }

    private static List<String> exactTextList(List<String> values, int expected, String field) {
        Objects.requireNonNull(values, field);
        if (values.size() != expected) throw new IllegalArgumentException(field + " must contain exactly " + expected + " entries");
        List<String> checked = values.stream().map(value -> text(value, field + " entry")).toList();
        if (new LinkedHashSet<>(checked).size() != checked.size()) throw new IllegalArgumentException(field + " entries must be unique");
        return checked;
    }

    private static Set<String> nonEmptyTags(Set<String> values) {
        Objects.requireNonNull(values, "affinityTags");
        if (values.isEmpty()) throw new IllegalArgumentException("affinityTags must not be empty");
        return values.stream().map(NightmareFactionReencounterContextCatalog::stableId)
                .sorted().collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
