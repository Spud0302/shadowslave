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

/** DESIGN-only outcome presentation for an already-resolved Nightmare faction answer follow-up. */
public final class NightmareFactionAnswerFollowupOutcomeCatalog {
    public static final String GENERATOR_VERSION = "nightmare-faction-answer-followup-outcome-v1";

    private NightmareFactionAnswerFollowupOutcomeCatalog() {}

    public enum OutcomeKind { RECORDED, COMPARED, CHECKED, LEFT_OPEN }

    public record Primitive(String id, OutcomeKind kind, String title, String outcomeRead,
                            String carryForwardPrompt, List<String> playerResponses, Set<String> affinityTags,
                            List<String> presentationCues, String antiOverclaimBoundary) {
        public Primitive {
            id = stableId(id);
            kind = Objects.requireNonNull(kind, "kind");
            title = text(title, "title");
            outcomeRead = text(outcomeRead, "outcomeRead");
            carryForwardPrompt = text(carryForwardPrompt, "carryForwardPrompt");
            playerResponses = exactTextList(playerResponses, 3, "playerResponses");
            affinityTags = Set.copyOf(nonEmptyTags(affinityTags));
            presentationCues = exactTextList(presentationCues, 2, "presentationCues");
            antiOverclaimBoundary = text(antiOverclaimBoundary, "antiOverclaimBoundary");
        }
    }

    public record Selection(String generatorVersion, long seed, String scenarioId, String factionId,
                            String answerId, String followupId, String outcomeId, OutcomeKind kind,
                            Primitive primitive, String presentationCue, Set<String> matchedEvidenceTags) {
        public Selection {
            generatorVersion = text(generatorVersion, "generatorVersion");
            scenarioId = opaqueId(scenarioId, "scenarioId");
            factionId = opaqueId(factionId, "factionId");
            answerId = opaqueId(answerId, "answerId");
            followupId = opaqueId(followupId, "followupId");
            outcomeId = opaqueId(outcomeId, "outcomeId");
            kind = Objects.requireNonNull(kind, "kind");
            primitive = Objects.requireNonNull(primitive, "primitive");
            if (primitive.kind() != kind) throw new IllegalArgumentException("primitive kind must match supplied kind");
            presentationCue = text(presentationCue, "presentationCue");
            matchedEvidenceTags = Set.copyOf(Objects.requireNonNull(matchedEvidenceTags, "matchedEvidenceTags"));
        }
    }

    public static List<Primitive> waveOne() {
        return List.of(
                p("recorded_statement_retained", OutcomeKind.RECORDED, "Statement Added to the Record",
                        "The statement is now retained with the speaker and encounter that supplied it.",
                        "Carry the statement forward as a sourced claim, not a settled fact.",
                        List.of("Review the recorded wording.", "Keep its source attached.", "Continue with the unresolved question."),
                        Set.of("record", "statement", "provenance"),
                        List.of("The statement appears with its source and encounter context.", "Its truth remains unmarked."),
                        "A recorded statement cannot establish truth, sincerity, motive, guilt, trust, allegiance, reputation, or future conduct."),
                p("recorded_scope_preserved", OutcomeKind.RECORDED, "Scope Preserved",
                        "The answer's stated boundary is retained without extending it into unmentioned places or circumstances.",
                        "Use the recorded scope only where it actually applies.",
                        List.of("Review the named boundary.", "Leave outside areas unresolved.", "Recheck the scope if circumstances change."),
                        Set.of("record", "scope", "boundary"),
                        List.of("The recorded boundary is displayed beside the answer.", "Unaddressed areas remain visibly outside the record."),
                        "Preserving scope cannot establish ownership, universal access, territorial legitimacy, route safety, or permanent permission."),
                p("recorded_condition_preserved", OutcomeKind.RECORDED, "Condition Preserved",
                        "The answer's condition is retained without marking that condition fulfilled.",
                        "Keep the condition available for a separate current-state check.",
                        List.of("Review the condition as stated.", "Link a later observation separately.", "Leave fulfillment unresolved."),
                        Set.of("record", "condition", "verification"),
                        List.of("The condition appears without a completion mark.", "Any linked observation is shown as separate evidence."),
                        "Recording a condition cannot decide fulfillment, transfer resources, unlock access, enforce terms, or accept a scenario event."),
                p("recorded_gap_preserved", OutcomeKind.RECORDED, "Unanswered Gap Preserved",
                        "The unanswered part of the exchange remains visible instead of being filled with an invented explanation.",
                        "Carry the missing answer forward only as an open question.",
                        List.of("Review what remains unanswered.", "Keep the refusal or omission attached.", "Do not infer a hidden answer."),
                        Set.of("record", "gap", "refusal"),
                        List.of("The unanswered field remains open in the record.", "No accusation or motive marker fills the gap."),
                        "An unanswered gap cannot establish deception, guilt, hostility, hidden motive, denied access, or relationship state."),

                p("compared_prior_difference", OutcomeKind.COMPARED, "Prior Statement Compared",
                        "The earlier and current statements have been placed together and their actual differences retained.",
                        "Carry forward the difference without inventing why it exists.",
                        List.of("Review the changed wording.", "Keep shared points separate from differences.", "Leave the reason unresolved."),
                        Set.of("compare", "history", "statement"),
                        List.of("Earlier and current wording appear side by side.", "Differences are highlighted without a deception verdict."),
                        "A changed statement cannot by itself prove deception, bad faith, motive, betrayal, trust loss, allegiance change, or guilt."),
                p("compared_accounts_result", OutcomeKind.COMPARED, "Accounts Compared",
                        "Shared and conflicting details between the accounts are retained as bounded comparison results.",
                        "Use agreement and disagreement as leads rather than automatic truth scores.",
                        List.of("Review the shared details.", "Review the conflicting details.", "Keep disputed claims unresolved."),
                        Set.of("compare", "account", "evidence"),
                        List.of("Shared and conflicting details are displayed separately.", "Neither source receives an automatic reliability score."),
                        "Agreement cannot certify truth, and contradiction cannot establish lying, guilt, source reliability, persuasion success, or blame."),
                p("compared_record_result", OutcomeKind.COMPARED, "Record and Claim Compared",
                        "The statement has been compared with the available record, including where their details align or diverge.",
                        "Preserve both provenance limits before drawing any further conclusion.",
                        List.of("Review matching details.", "Review any mismatch.", "Keep authenticity as a separate question."),
                        Set.of("compare", "record", "object"),
                        List.of("The compared details are linked across statement and record.", "Record authenticity remains a separate unresolved concern."),
                        "Comparison with a record cannot automatically prove authenticity, truth, ownership, innocence, guilt, authority, or scenario resolution."),
                p("compared_current_conditions", OutcomeKind.COMPARED, "Current Conditions Compared",
                        "The answer has been compared with the current observations that were actually available for the check.",
                        "Carry forward only the observed match, mismatch, or missing detail.",
                        List.of("Review the current observation.", "Mark stale or missing information.", "Request another check only if needed."),
                        Set.of("compare", "current", "condition"),
                        List.of("Current observations appear beside the answer with their context.", "Missing observation remains missing rather than being generated."),
                        "A comparison cannot create world state, guarantee route safety, establish access or ownership, or independently make a statement true or false."),

                p("checked_named_source", OutcomeKind.CHECKED, "Named Source Checked",
                        "The named source check has been completed and its bounded result is available beside the original answer.",
                        "Use the check result without converting one source into universal authority.",
                        List.of("Review what the source actually supplied.", "Preserve any mismatch.", "Leave broader reliability unresolved."),
                        Set.of("check", "source", "provenance"),
                        List.of("The checked source is linked to the original answer.", "No universal trust score is assigned to the source."),
                        "Checking a source cannot automatically establish truth, sincerity, guilt, motive, reputation, allegiance, or universal reliability."),
                p("checked_stated_condition", OutcomeKind.CHECKED, "Stated Condition Checked",
                        "The named condition has been checked and the supplied result is retained separately from any consequence it might authorize.",
                        "Carry forward the check result without inventing what it changes.",
                        List.of("Review the checked condition.", "Keep the result attached to its context.", "Wait for any separate consequence to be established."),
                        Set.of("check", "condition", "current"),
                        List.of("The condition and check result are displayed together.", "No access, inventory, or objective state changes from this summary."),
                        "A condition check cannot decide resource transfer, access mutation, agreement enforcement, relationship change, or scenario-event acceptance."),
                p("checked_access_claim", OutcomeKind.CHECKED, "Access Claim Checked",
                        "The current route or boundary information supplied for the check is now shown beside the faction's claim.",
                        "Keep route state, permission, ownership, and safety as separate questions unless each was supplied.",
                        List.of("Review the current route information.", "Keep the speaker's claim separate.", "Recheck if the route state changes."),
                        Set.of("check", "access", "route"),
                        List.of("The access claim is paired with the supplied route check.", "Safety and ownership remain separate fields."),
                        "An access check cannot invent territorial legitimacy, ownership, route safety, permission, hostility, or rights not established elsewhere."),
                p("checked_shared_detail", OutcomeKind.CHECKED, "One Detail Checked",
                        "One selected detail has been checked while the rest of the answer remains outside that result.",
                        "Use the bounded result only for the detail that was actually checked.",
                        List.of("Review the checked detail.", "Keep the remaining claims separate.", "Choose another check only if needed."),
                        Set.of("check", "detail", "evidence"),
                        List.of("Only the selected detail receives the check result.", "Unchecked portions remain visibly separate."),
                        "Checking one detail cannot certify the whole answer, infer hidden motive, calculate confidence, or decide faction relationship state."),

                p("left_open_missing_information", OutcomeKind.LEFT_OPEN, "Question Left Open",
                        "The follow-up ends with information still missing, and the unanswered point remains available for later context.",
                        "Preserve what is missing without promising that an answer will appear.",
                        List.of("Review the missing information.", "Keep the question open.", "Move on to current priorities."),
                        Set.of("open", "information", "gap"),
                        List.of("The missing-information note remains beside the answer.", "No timer or promised future result is generated."),
                        "Leaving a question open cannot guarantee future evidence, freeze world state, establish truth, preserve access, or predict future faction behavior."),
                p("left_open_unsafe_check", OutcomeKind.LEFT_OPEN, "Unsafe Check Left Open",
                        "The proposed check remains unresolved because it was not carried out under the current circumstances.",
                        "Keep the open point without treating avoided danger as proof of anything.",
                        List.of("Review the unresolved check.", "Seek a safer route if one becomes available.", "Continue without a verdict."),
                        Set.of("open", "risk", "verification"),
                        List.of("The unperformed check remains visible without a failure mark.", "No route receives a safety label from the deferral."),
                        "An unperformed risky check cannot establish cowardice, guilt, route safety, appraisal quality, failure, hostility, or scenario outcome."),
                p("left_open_authority_question", OutcomeKind.LEFT_OPEN, "Authority Question Left Open",
                        "The available exchange did not establish who could settle the disputed point, so that authority question remains open.",
                        "Carry forward the missing authority rather than inventing a replacement.",
                        List.of("Review the unresolved authority question.", "Seek another source only if one becomes available.", "Keep control and legitimacy unsettled."),
                        Set.of("open", "authority", "source"),
                        List.of("The unresolved authority question stays attached to the record.", "No replacement authority is invented."),
                        "Missing authority cannot prove invalidity, legitimacy, ownership, truth, reputation, allegiance, or who should control the outcome."),
                p("left_open_after_refusal", OutcomeKind.LEFT_OPEN, "Refused Question Left Open",
                        "The conversation ended without an answer to this point, and the refusal remains recorded without escalation.",
                        "Keep the question open without turning refusal into a relationship verdict.",
                        List.of("Leave the question open.", "Return to another current matter.", "End this line of inquiry for now."),
                        Set.of("open", "refusal", "disengage"),
                        List.of("The open question remains in the record after the exchange.", "No hostility or relationship change is generated."),
                        "A refused question cannot create peace, hostility, betrayal, trust, reputation, allegiance, punishment, or future willingness."));
    }

    public static Selection compose(long seed, String scenarioId, String factionId, String answerId,
                                    String followupId, String outcomeId, OutcomeKind kind,
                                    Map<String, Integer> evidence) {
        String scenario = opaqueId(scenarioId, "scenarioId");
        String faction = opaqueId(factionId, "factionId");
        String answer = opaqueId(answerId, "answerId");
        String followup = opaqueId(followupId, "followupId");
        String outcome = opaqueId(outcomeId, "outcomeId");
        OutcomeKind checkedKind = Objects.requireNonNull(kind, "kind");
        Set<String> positive = positiveEvidence(evidence);
        List<Primitive> candidates = waveOne().stream().filter(p -> p.kind() == checkedKind)
                .sorted(Comparator.comparing(Primitive::id)).toList();
        int best = candidates.stream().mapToInt(p -> overlap(p.affinityTags(), positive)).max().orElse(0);
        List<Primitive> preferred = best > 0
                ? candidates.stream().filter(p -> overlap(p.affinityTags(), positive) == best).toList()
                : candidates;
        String key = scenario + "|" + faction + "|" + answer + "|" + followup + "|" + outcome + "|"
                + checkedKind.name() + "|" + positive.stream().sorted().collect(Collectors.joining(","));
        Primitive primitive = preferred.get(index(seed, key + "|primitive", preferred.size()));
        String cue = primitive.presentationCues().get(index(seed, key + "|" + primitive.id() + "|cue", 2));
        Set<String> matched = primitive.affinityTags().stream().filter(positive::contains).collect(Collectors.toSet());
        return new Selection(GENERATOR_VERSION, seed, scenario, faction, answer, followup, outcome, checkedKind,
                primitive, cue, matched);
    }

    public static Primitive requirePrimitive(String id) {
        String checked = stableId(id);
        return waveOne().stream().filter(p -> p.id().equals(checked)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown faction answer followup outcome: " + id));
    }

    private static Primitive p(String id, OutcomeKind kind, String title, String read, String prompt,
                               List<String> responses, Set<String> tags, List<String> cues, String boundary) {
        return new Primitive(id, kind, title, read, prompt, responses, tags, cues, boundary);
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
