package dev.spud.shadowslave.nightmare.content;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** DESIGN-only journal presentation for an already-resolved Nightmare faction information thread. */
public final class NightmareFactionInformationThreadSummaryCatalog {
    public static final String GENERATOR_VERSION = "nightmare-faction-information-thread-summary-v1";

    private NightmareFactionInformationThreadSummaryCatalog() {}

    public enum ThreadState { ACTIVE, CONTRADICTED, STALE, CLOSED }

    public record Primitive(String id, ThreadState state, String title, String threadRead,
                            String nextQuestion, List<String> playerActions, Set<String> affinityTags,
                            List<String> presentationCues, String antiOverclaimBoundary) {
        public Primitive {
            id = stableId(id);
            state = Objects.requireNonNull(state, "state");
            title = text(title, "title");
            threadRead = text(threadRead, "threadRead");
            nextQuestion = text(nextQuestion, "nextQuestion");
            playerActions = exactTextList(playerActions, 3, "playerActions");
            affinityTags = Set.copyOf(nonEmptyTags(affinityTags));
            presentationCues = exactTextList(presentationCues, 2, "presentationCues");
            antiOverclaimBoundary = text(antiOverclaimBoundary, "antiOverclaimBoundary");
        }
    }

    public record Selection(String generatorVersion, long seed, String scenarioId, String factionId,
                            String threadId, String latestOutcomeId, ThreadState state,
                            Primitive primitive, String presentationCue, Set<String> matchedEvidenceTags) {
        public Selection {
            generatorVersion = text(generatorVersion, "generatorVersion");
            scenarioId = opaqueId(scenarioId, "scenarioId");
            factionId = opaqueId(factionId, "factionId");
            threadId = opaqueId(threadId, "threadId");
            latestOutcomeId = opaqueId(latestOutcomeId, "latestOutcomeId");
            state = Objects.requireNonNull(state, "state");
            primitive = Objects.requireNonNull(primitive, "primitive");
            if (primitive.state() != state) throw new IllegalArgumentException("primitive state must match supplied state");
            presentationCue = text(presentationCue, "presentationCue");
            matchedEvidenceTags = Set.copyOf(Objects.requireNonNull(matchedEvidenceTags, "matchedEvidenceTags"));
        }
    }

    public static List<Primitive> waveOne() {
        return List.of(
                p("active_open_question", ThreadState.ACTIVE, "Open Question in Play",
                        "This information thread still has a current unresolved question and at least one recorded point worth carrying forward.",
                        "What specific fact would meaningfully narrow the open question?",
                        List.of("Review the latest recorded point.", "Choose one bounded follow-up.", "Leave the thread active without guessing."),
                        Set.of("active", "question", "open"),
                        List.of("The latest entry is shown beside the remaining question.", "Unresolved fields stay visibly open."),
                        "An active thread cannot establish truth, motive, guilt, trust, allegiance, reputation, access, or scenario progress."),
                p("active_source_pending", ThreadState.ACTIVE, "Source Still Worth Checking",
                        "The thread remains current because a named source or record has not yet been checked against the existing claim.",
                        "Is that source still available and relevant to this exact claim?",
                        List.of("Keep the source attached.", "Check only the named point.", "Defer if the source is no longer reachable."),
                        Set.of("active", "source", "verification"),
                        List.of("The pending source appears as a linked lead, not a verdict.", "No reliability score is assigned before a check."),
                        "A pending source cannot prove reliability, truth, innocence, guilt, authority, or future cooperation."),
                p("active_condition_current", ThreadState.ACTIVE, "Condition Still Current",
                        "A previously stated condition still belongs to the current information thread, but its effect is not decided here.",
                        "Has the condition changed, been checked, or remained untested?",
                        List.of("Review the stated condition.", "Compare it with current observations.", "Keep its consequence separate."),
                        Set.of("active", "condition", "current"),
                        List.of("The condition is displayed with its last known context.", "Consequences remain separate from the journal summary."),
                        "A current condition cannot by itself grant access, transfer resources, enforce an agreement, or accept a scenario event."),
                p("active_multiple_leads", ThreadState.ACTIVE, "Several Leads, One Thread",
                        "Multiple bounded leads still point at the same unresolved topic, so they remain grouped without being merged into one conclusion.",
                        "Which lead can be checked without assuming the others are true?",
                        List.of("Compare the leads side by side.", "Select one next check.", "Preserve the others as separate leads."),
                        Set.of("active", "lead", "compare"),
                        List.of("Each lead keeps its own source marker.", "No automatic consensus or confidence score is shown."),
                        "Several leads cannot become truth by accumulation, calculate confidence, prove deception, or decide faction relationship state."),

                p("contradicted_accounts", ThreadState.CONTRADICTED, "Accounts Conflict",
                        "Two retained accounts disagree on at least one bounded detail, and the disagreement is preserved without choosing a winner.",
                        "Which disputed detail can be checked independently?",
                        List.of("Review the shared details first.", "Isolate the disputed point.", "Keep both sources attached."),
                        Set.of("contradicted", "account", "compare"),
                        List.of("Shared and conflicting details are separated visually.", "Neither account is marked false by the summary."),
                        "Contradiction cannot establish lying, guilt, bad faith, source reliability, reputation loss, or hostility."),
                p("contradicted_record_claim", ThreadState.CONTRADICTED, "Record and Claim Diverge",
                        "A retained record and a faction claim do not align on one or more bounded details.",
                        "Can the mismatch be narrowed without assuming the record is authentic or the claim is false?",
                        List.of("Review the exact mismatch.", "Keep record provenance visible.", "Seek another bounded check if available."),
                        Set.of("contradicted", "record", "claim"),
                        List.of("The record and claim appear together with provenance labels.", "Authenticity remains a separate unresolved question."),
                        "A record-claim mismatch cannot automatically prove forgery, deception, ownership, guilt, authority, or scenario resolution."),
                p("contradicted_condition_result", ThreadState.CONTRADICTED, "Condition and Observation Diverge",
                        "The stated condition and a later supplied observation no longer align cleanly, so the thread carries both without inventing a cause.",
                        "Is the condition stale, the observation incomplete, or another explanation still open?",
                        List.of("Review when each entry was recorded.", "Preserve the mismatch.", "Recheck only if current evidence allows."),
                        Set.of("contradicted", "condition", "current"),
                        List.of("Each entry retains its own time/context marker.", "No cause is generated for the mismatch."),
                        "Divergence cannot establish sabotage, bad faith, motive, route safety, access rights, or a relationship change."),
                p("contradicted_authority", ThreadState.CONTRADICTED, "Authority Claims Conflict",
                        "Different sources claim authority over the same bounded matter, but the journal does not decide legitimacy.",
                        "What evidence would distinguish who can actually settle this matter?",
                        List.of("Review each claimed scope.", "Keep legitimacy unresolved.", "Avoid extending either claim beyond its evidence."),
                        Set.of("contradicted", "authority", "scope"),
                        List.of("Competing scopes are displayed without a control marker.", "Ownership and legitimacy remain unset."),
                        "Competing authority claims cannot decide ownership, territorial legitimacy, access, allegiance, truth, or who should control an outcome."),

                p("stale_old_route", ThreadState.STALE, "Route Information Is Old",
                        "The route information is retained for history, but its recorded conditions are no longer current enough to guide action safely by themselves.",
                        "What must be observed again before relying on this route?",
                        List.of("Review the last known conditions.", "Seek a current route check.", "Keep the old record for comparison only."),
                        Set.of("stale", "route", "current"),
                        List.of("The entry is visibly marked as old context.", "No current safety marker is inferred from it."),
                        "Stale route information cannot guarantee safety, access, passability, ownership, or current faction control."),
                p("stale_old_statement", ThreadState.STALE, "Statement Needs Renewal",
                        "A prior statement remains in the record, but changed circumstances mean it should not be treated as the faction's current position.",
                        "Does the speaker still stand by the same bounded statement now?",
                        List.of("Keep the old wording for history.", "Ask for a current position if appropriate.", "Do not inherit old terms automatically."),
                        Set.of("stale", "statement", "history"),
                        List.of("The older statement remains readable with its original context.", "Current position remains blank until supplied."),
                        "An old statement cannot establish current intent, renewed terms, trust, hostility, allegiance, or future behavior."),
                p("stale_changed_conditions", ThreadState.STALE, "Conditions Have Changed",
                        "The thread's last useful observation belongs to circumstances that have since changed, so the journal preserves it as past context only.",
                        "Which current condition matters before this thread becomes actionable again?",
                        List.of("Review what changed.", "Request a current observation.", "Leave the old entry intact for comparison."),
                        Set.of("stale", "condition", "history"),
                        List.of("Past and current context slots are kept separate.", "The old entry is not silently rewritten."),
                        "Changed conditions cannot by themselves prove danger, safety, deception, access loss, scarcity, or scenario failure."),
                p("stale_missing_context", ThreadState.STALE, "Context No Longer Sufficient",
                        "The surviving record lacks enough current context to support the same question it once did.",
                        "What missing context would make this thread useful again?",
                        List.of("Identify the missing context.", "Retain the historical record.", "Avoid drawing a current verdict."),
                        Set.of("stale", "context", "gap"),
                        List.of("Missing current context is shown explicitly.", "The archived details remain unchanged."),
                        "Insufficient context cannot establish truth, motive, guilt, access, relationship state, or a forecast of future events."),

                p("closed_answered_scope", ThreadState.CLOSED, "Scoped Question Closed",
                        "The bounded question this thread tracked has received an already-authorized closing outcome, so no further journal action is implied.",
                        "What part of the record should remain available for future context?",
                        List.of("Review the closing outcome.", "Archive the supporting entries.", "Open a new thread only for a new question."),
                        Set.of("closed", "answer", "record"),
                        List.of("The closing outcome appears at the end of the thread.", "Supporting entries remain accessible as history."),
                        "Closing one question cannot certify every related claim, settle faction relationship state, or resolve the Nightmare."),
                p("closed_no_further_check", ThreadState.CLOSED, "No Further Check Authorized",
                        "This thread is closed because the current information process has ended, not because every hidden fact is known.",
                        "Is a genuinely new question present, or is this thread simply finished?",
                        List.of("Keep the final record.", "Do not invent another check.", "Start separately if new authority later exists."),
                        Set.of("closed", "check", "boundary"),
                        List.of("The thread is marked finished without a truth-complete badge.", "No new objective is generated from closure."),
                        "Closure cannot create omniscience, guarantee truth, manufacture authority, accept a scenario event, or grant appraisal credit."),
                p("closed_left_unresolved", ThreadState.CLOSED, "Closed With Uncertainty Retained",
                        "The information process has ended while one or more facts remain unresolved, and that uncertainty is preserved in the final summary.",
                        "Which unresolved point should remain visible as historical context?",
                        List.of("Keep the unresolved point visible.", "Archive the attempted checks.", "Avoid converting closure into a verdict."),
                        Set.of("closed", "open", "uncertainty"),
                        List.of("The final summary lists unresolved points separately.", "No hidden answer fills them after closure."),
                        "A closed unresolved thread cannot establish truth, guilt, deception, trust, hostility, allegiance, or future willingness."),
                p("closed_superseded_thread", ThreadState.CLOSED, "Thread Superseded",
                        "A newer already-authorized information thread now owns the current question, so this older thread remains only as history.",
                        "What historical context should be carried into the newer thread without copying old conclusions forward?",
                        List.of("Link the newer thread.", "Preserve this thread's sources.", "Carry forward only explicitly relevant context."),
                        Set.of("closed", "history", "current"),
                        List.of("The old thread links forward without being rewritten.", "Current state remains owned by the newer record."),
                        "Supersession cannot prove the old thread wrong, transfer truth or confidence, or mutate access, relationships, world state, or progression."));
    }

    public static Selection compose(long seed, String scenarioId, String factionId, String threadId,
                                    String latestOutcomeId, ThreadState state, Map<String, Integer> evidence) {
        String scenario = opaqueId(scenarioId, "scenarioId");
        String faction = opaqueId(factionId, "factionId");
        String thread = opaqueId(threadId, "threadId");
        String outcome = opaqueId(latestOutcomeId, "latestOutcomeId");
        ThreadState checkedState = Objects.requireNonNull(state, "state");
        Set<String> positive = positiveEvidence(evidence);
        List<Primitive> candidates = waveOne().stream().filter(p -> p.state() == checkedState)
                .sorted(Comparator.comparing(Primitive::id)).toList();
        int best = candidates.stream().mapToInt(p -> overlap(p.affinityTags(), positive)).max().orElse(0);
        List<Primitive> preferred = best > 0
                ? candidates.stream().filter(p -> overlap(p.affinityTags(), positive) == best).toList()
                : candidates;
        String key = scenario + "|" + faction + "|" + thread + "|" + outcome + "|" + checkedState.name() + "|"
                + positive.stream().sorted().collect(Collectors.joining(","));
        Primitive primitive = preferred.get(index(seed, key + "|primitive", preferred.size()));
        String cue = primitive.presentationCues().get(index(seed, key + "|" + primitive.id() + "|cue", 2));
        Set<String> matched = primitive.affinityTags().stream().filter(positive::contains).collect(Collectors.toSet());
        return new Selection(GENERATOR_VERSION, seed, scenario, faction, thread, outcome, checkedState,
                primitive, cue, matched);
    }

    public static Primitive requirePrimitive(String id) {
        String checked = stableId(id);
        return waveOne().stream().filter(p -> p.id().equals(checked)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown faction information thread summary: " + id));
    }

    private static Primitive p(String id, ThreadState state, String title, String read, String question,
                               List<String> actions, Set<String> tags, List<String> cues, String boundary) {
        return new Primitive(id, state, title, read, question, actions, tags, cues, boundary);
    }

    private static int overlap(Set<String> a, Set<String> b) {
        int count = 0;
        for (String value : a) if (b.contains(value)) count++;
        return count;
    }

    private static Set<String> positiveEvidence(Map<String, Integer> evidence) {
        Objects.requireNonNull(evidence, "evidence");
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        evidence.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            String tag = stableId(entry.getKey());
            Integer value = Objects.requireNonNull(entry.getValue(), "evidence value");
            if (value < 0) throw new IllegalArgumentException("evidence values must be nonnegative");
            if (value > 0) tags.add(tag);
        });
        return Set.copyOf(tags);
    }

    private static Set<String> nonEmptyTags(Set<String> tags) {
        Objects.requireNonNull(tags, "affinityTags");
        if (tags.isEmpty()) throw new IllegalArgumentException("affinityTags must not be empty");
        LinkedHashSet<String> checked = new LinkedHashSet<>();
        for (String tag : tags) checked.add(stableId(tag));
        return checked;
    }

    private static List<String> exactTextList(List<String> values, int count, String field) {
        Objects.requireNonNull(values, field);
        if (values.size() != count) throw new IllegalArgumentException(field + " must contain exactly " + count + " entries");
        return values.stream().map(value -> text(value, field + " entry")).toList();
    }

    private static String stableId(String value) {
        String checked = text(value, "id").toLowerCase(Locale.ROOT);
        if (!checked.matches("[a-z0-9][a-z0-9_:-]*")) throw new IllegalArgumentException("invalid stable id: " + value);
        return checked;
    }

    private static String opaqueId(String value, String field) {
        return text(value, field);
    }

    private static String text(String value, String field) {
        Objects.requireNonNull(value, field);
        String checked = value.trim();
        if (checked.isEmpty()) throw new IllegalArgumentException(field + " must not be blank");
        return checked;
    }

    private static int index(long seed, String key, int bound) {
        if (bound <= 0) throw new IllegalArgumentException("bound must be positive");
        byte[] digest = sha256(GENERATOR_VERSION + "|" + seed + "|" + key);
        long value = ByteBuffer.wrap(digest).getLong();
        return (int) Math.floorMod(value, bound);
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
