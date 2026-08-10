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

/** DESIGN-only follow-up presentation for an already-authorized Nightmare faction answer. */
public final class NightmareFactionAnswerFollowupCatalog {
    public static final String GENERATOR_VERSION = "nightmare-faction-answer-followup-v1";

    private NightmareFactionAnswerFollowupCatalog() {}

    public enum FollowupFamily { RECORD, COMPARE, VERIFY, DEFER }

    public record Primitive(String id, FollowupFamily family, String title, String situationRead,
                            String playerPrompt, List<String> playerActions, Set<String> affinityTags,
                            List<String> presentationCues, String antiOverclaimBoundary) {
        public Primitive {
            id = stableId(id);
            family = Objects.requireNonNull(family, "family");
            title = text(title, "title");
            situationRead = text(situationRead, "situationRead");
            playerPrompt = text(playerPrompt, "playerPrompt");
            playerActions = exactTextList(playerActions, 3, "playerActions");
            affinityTags = Set.copyOf(nonEmptyTags(affinityTags));
            presentationCues = exactTextList(presentationCues, 2, "presentationCues");
            antiOverclaimBoundary = text(antiOverclaimBoundary, "antiOverclaimBoundary");
        }
    }

    public record Selection(String generatorVersion, long seed, String scenarioId, String factionId,
                            String answerId, String followupId, FollowupFamily family, Primitive primitive,
                            String presentationCue, Set<String> matchedEvidenceTags) {
        public Selection {
            generatorVersion = text(generatorVersion, "generatorVersion");
            scenarioId = opaqueId(scenarioId, "scenarioId");
            factionId = opaqueId(factionId, "factionId");
            answerId = opaqueId(answerId, "answerId");
            followupId = opaqueId(followupId, "followupId");
            family = Objects.requireNonNull(family, "family");
            primitive = Objects.requireNonNull(primitive, "primitive");
            if (primitive.family() != family) throw new IllegalArgumentException("primitive family must match supplied family");
            presentationCue = text(presentationCue, "presentationCue");
            matchedEvidenceTags = Set.copyOf(Objects.requireNonNull(matchedEvidenceTags, "matchedEvidenceTags"));
        }
    }

    public static List<Primitive> waveOne() {
        return List.of(
                p("record_exact_statement", FollowupFamily.RECORD, "Record the Exact Statement",
                        "The current answer can be retained as a bounded statement without turning it into a fact.",
                        "Keep what was said separate from what is known.",
                        List.of("Record the answer as stated.", "Attach the current speaker and context.", "Leave truth unresolved."),
                        Set.of("statement", "record", "provenance"),
                        List.of("The answer is shown with its current source and encounter context.", "No truth badge is added to the record."),
                        "Recording a statement cannot prove truth, sincerity, motive, guilt, trust, allegiance, reputation, or future conduct."),
                p("record_scope_boundary", FollowupFamily.RECORD, "Record the Stated Scope",
                        "The answer includes a boundary that can be preserved without extending it beyond what was said.",
                        "Keep the answer attached to the scope it actually named.",
                        List.of("Record the named boundary.", "Keep unaddressed areas unresolved.", "Recheck if the context changes."),
                        Set.of("scope", "boundary", "record"),
                        List.of("The boundary appears beside the recorded answer.", "Outside the boundary remains unclassified."),
                        "A recorded scope cannot establish ownership, universal access, route safety, territorial legitimacy, or permanent permission."),
                p("record_condition_unmet", FollowupFamily.RECORD, "Record the Condition",
                        "A conditional answer can be retained with its condition still visibly unsettled.",
                        "Preserve the condition without marking it fulfilled.",
                        List.of("Record the condition.", "Link any relevant observation separately.", "Do not assume completion."),
                        Set.of("condition", "record", "verification"),
                        List.of("The condition is displayed without a completion marker.", "Linked observations remain separate from fulfillment state."),
                        "Recording a condition cannot decide fulfillment, transfer resources, unlock access, enforce terms, or accept a scenario event."),
                p("record_refusal_gap", FollowupFamily.RECORD, "Record the Unanswered Gap",
                        "A refusal can be retained as an unanswered point instead of being replaced with an invented explanation.",
                        "Record the gap and leave the missing answer missing.",
                        List.of("Record the refusal.", "Name the unanswered question.", "Avoid inferring a hidden answer."),
                        Set.of("refusal", "gap", "record"),
                        List.of("The unanswered field remains visibly open.", "No accusation or motive marker fills the gap."),
                        "A recorded refusal cannot establish deception, guilt, hostility, hidden motive, denied access, or relationship state."),

                p("compare_with_prior_statement", FollowupFamily.COMPARE, "Compare with the Prior Statement",
                        "A present answer can be compared with an older recorded statement without deciding why they differ.",
                        "Show what changed in the wording, then preserve the reason as a question.",
                        List.of("Place the statements side by side.", "Mark only the actual difference.", "Ask what accounts for the change."),
                        Set.of("history", "compare", "statement"),
                        List.of("Past and current wording appear side by side.", "Differences are highlighted without a lie marker."),
                        "A changed statement cannot by itself prove deception, bad faith, motive, betrayal, trust loss, allegiance change, or guilt."),
                p("compare_with_other_account", FollowupFamily.COMPARE, "Compare with Another Account",
                        "The answer can be placed beside another account while agreement or contradiction remains bounded evidence.",
                        "Compare the accounts without turning majority agreement into truth.",
                        List.of("Line up the shared points.", "Mark the conflicting points.", "Keep unresolved claims unresolved."),
                        Set.of("account", "compare", "evidence"),
                        List.of("Agreement and disagreement are displayed separately.", "No source receives an automatic reliability score."),
                        "Agreement cannot certify truth, and contradiction cannot establish lying, guilt, source reliability, persuasion success, or blame."),
                p("compare_with_physical_record", FollowupFamily.COMPARE, "Compare with a Physical Record",
                        "The faction answer can be checked against an already-recorded object or document without granting that record automatic authenticity.",
                        "Put the statement beside the record and keep both provenance limits visible.",
                        List.of("Compare the named details.", "Mark what the record does not establish.", "Preserve any contradiction."),
                        Set.of("record", "compare", "object"),
                        List.of("Statement and record are linked by the compared detail.", "Authenticity remains a separate unresolved concern."),
                        "A physical record cannot automatically prove authenticity, truth, ownership, innocence, guilt, authority, or scenario resolution."),
                p("compare_with_current_world_state", FollowupFamily.COMPARE, "Compare with Current Conditions",
                        "The answer can be compared with already-observed current conditions without allowing presentation to mutate those conditions.",
                        "Check whether the statement matches what is presently observed.",
                        List.of("Compare only known current details.", "Mark stale or missing observations.", "Request a new check if needed."),
                        Set.of("current", "compare", "condition"),
                        List.of("Current observations are timestamped beside the answer.", "Missing observation stays missing instead of being generated."),
                        "A comparison cannot create world state, guarantee route safety, establish access, prove ownership, or make a statement true or false."),

                p("verify_named_source", FollowupFamily.VERIFY, "Verify the Named Source",
                        "An answer that names a source can expose a bounded verification step without deciding the source's reliability.",
                        "Check the named source before treating the claim as settled.",
                        List.of("Seek the named source.", "Preserve the answer if the source is unavailable.", "Record any mismatch without a verdict."),
                        Set.of("source", "verify", "provenance"),
                        List.of("The source appears as a check target rather than a trust score.", "Unavailable sources remain explicitly unavailable."),
                        "Source verification cannot automatically establish truth, sincerity, guilt, motive, reputation, allegiance, or universal reliability."),
                p("verify_stated_condition", FollowupFamily.VERIFY, "Verify the Stated Condition",
                        "A conditional answer can point toward an already-authorized check while fulfillment remains outside presentation authority.",
                        "Check the named condition without assuming what passing the check will cause.",
                        List.of("Inspect the condition through an available check.", "Record the result separately.", "Leave consequences to current state."),
                        Set.of("condition", "verify", "current"),
                        List.of("The condition and its check are visually linked.", "No access, inventory, or objective state changes from the prompt."),
                        "Verification presentation cannot decide fulfillment, unlock access, transfer resources, enforce an agreement, or accept a scenario event."),
                p("verify_access_claim", FollowupFamily.VERIFY, "Verify the Access Claim",
                        "A statement about passage can be sent to a current access check without treating the speaker as the route authority.",
                        "Verify the present route state separately from what was said.",
                        List.of("Check the current route state.", "Check any named boundary.", "Keep authority and safety unresolved unless supplied elsewhere."),
                        Set.of("access", "verify", "route"),
                        List.of("The access statement is shown beside current route information.", "Safety and ownership remain separate fields."),
                        "An access check cannot invent territorial legitimacy, ownership, safety, permission, hostility, or rights not supplied by current state."),
                p("verify_shared_detail", FollowupFamily.VERIFY, "Verify One Shared Detail",
                        "A broad answer can be narrowed to one testable detail rather than forcing an all-or-nothing truth verdict.",
                        "Choose one bounded detail that can actually be checked.",
                        List.of("Select one shared detail.", "Compare the resulting observation.", "Leave the rest of the answer unresolved."),
                        Set.of("detail", "verify", "evidence"),
                        List.of("Only the selected detail receives a verification link.", "Unverified portions remain visually separate."),
                        "Checking one detail cannot certify the whole answer, infer hidden motive, calculate confidence, or decide faction relationship state."),

                p("defer_until_new_information", FollowupFamily.DEFER, "Defer Until New Information",
                        "The answer can remain unresolved when the next useful step depends on information that is not yet available.",
                        "Keep the answer open until a relevant new observation exists.",
                        List.of("Defer the check.", "Record what information is missing.", "Return only if the missing context changes."),
                        Set.of("defer", "information", "gap"),
                        List.of("The answer remains available with a visible missing-information note.", "No timer or promised future result is generated."),
                        "Deferral cannot guarantee future evidence, freeze world state, establish truth, preserve access, or predict faction behavior."),
                p("defer_unsafe_check", FollowupFamily.DEFER, "Defer an Unsafe Check",
                        "A proposed verification can be left pending when current state does not authorize or support taking that risk.",
                        "Leave the question open rather than turning danger into compulsory proof.",
                        List.of("Defer the risky check.", "Seek a safer authorized route.", "Preserve the unanswered point."),
                        Set.of("defer", "risk", "verification"),
                        List.of("The pending check remains visible without a failure mark.", "No route is labelled safe merely because another check is deferred."),
                        "Deferring a risky check cannot establish cowardice, guilt, route safety, appraisal quality, failure, hostility, or scenario outcome."),
                p("defer_without_current_authority", FollowupFamily.DEFER, "Defer Without Current Authority",
                        "The answer can be retained when the available participant or source cannot presently settle the disputed point.",
                        "Do not force a verdict from a source that has not been established as able to give one.",
                        List.of("Keep the answer on record.", "Seek an appropriate source if one becomes available.", "Leave authority unresolved."),
                        Set.of("defer", "authority", "source"),
                        List.of("The unresolved authority question stays attached to the answer.", "No replacement authority is invented."),
                        "Missing authority cannot prove invalidity, legitimacy, ownership, truth, reputation, allegiance, or who should control the outcome."),
                p("defer_after_refusal", FollowupFamily.DEFER, "Defer After Refusal",
                        "A refused answer can be left for later without silently converting the refusal into escalation or closure.",
                        "End this line of inquiry for now and preserve the open question.",
                        List.of("Leave the question open.", "Return to another current matter.", "Disengage from the exchange."),
                        Set.of("defer", "refusal", "disengage"),
                        List.of("The open question remains in the record after the conversation ends.", "No hostility or relationship change is generated."),
                        "Deferring after refusal cannot create peace, hostility, betrayal, trust, reputation, allegiance, punishment, or future willingness."));
    }

    public static Selection compose(long seed, String scenarioId, String factionId, String answerId,
                                    String followupId, FollowupFamily family, Map<String, Integer> evidence) {
        String scenario = opaqueId(scenarioId, "scenarioId");
        String faction = opaqueId(factionId, "factionId");
        String answer = opaqueId(answerId, "answerId");
        String followup = opaqueId(followupId, "followupId");
        FollowupFamily checkedFamily = Objects.requireNonNull(family, "family");
        Set<String> positive = positiveEvidence(evidence);
        List<Primitive> candidates = waveOne().stream().filter(p -> p.family() == checkedFamily)
                .sorted(Comparator.comparing(Primitive::id)).toList();
        int best = candidates.stream().mapToInt(p -> overlap(p.affinityTags(), positive)).max().orElse(0);
        List<Primitive> preferred = best > 0
                ? candidates.stream().filter(p -> overlap(p.affinityTags(), positive) == best).toList()
                : candidates;
        String key = scenario + "|" + faction + "|" + answer + "|" + followup + "|" + checkedFamily.name()
                + "|" + positive.stream().sorted().collect(Collectors.joining(","));
        Primitive primitive = preferred.get(index(seed, key + "|primitive", preferred.size()));
        String cue = primitive.presentationCues().get(index(seed, key + "|" + primitive.id() + "|cue", 2));
        Set<String> matched = primitive.affinityTags().stream().filter(positive::contains).collect(Collectors.toSet());
        return new Selection(GENERATOR_VERSION, seed, scenario, faction, answer, followup, checkedFamily,
                primitive, cue, matched);
    }

    public static Primitive requirePrimitive(String id) {
        String checked = stableId(id);
        return waveOne().stream().filter(p -> p.id().equals(checked)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown faction answer followup: " + id));
    }

    private static Primitive p(String id, FollowupFamily family, String title, String read, String prompt,
                               List<String> actions, Set<String> tags, List<String> cues, String boundary) {
        return new Primitive(id, family, title, read, prompt, actions, tags, cues, boundary);
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

    private static int index(long seed, String key, int bound) {
        if (bound <= 0) throw new IllegalArgumentException("bound must be positive");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(ByteBuffer.allocate(Long.BYTES).putLong(seed).array());
            digest.update(key.getBytes(StandardCharsets.UTF_8));
            long value = ByteBuffer.wrap(digest.digest()).getLong();
            return (int) Math.floorMod(value, bound);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 unavailable", impossible);
        }
    }

    private static String stableId(String value) {
        String checked = text(value, "id").toLowerCase(Locale.ROOT);
        if (!checked.matches("[a-z0-9_]+")) throw new IllegalArgumentException("catalogue id must match [a-z0-9_]+");
        return checked;
    }

    private static String opaqueId(String value, String field) {
        return text(value, field);
    }

    private static String text(String value, String field) {
        String checked = Objects.requireNonNull(value, field).trim();
        if (checked.isEmpty()) throw new IllegalArgumentException(field + " must not be blank");
        return checked;
    }

    private static List<String> exactTextList(List<String> values, int size, String field) {
        Objects.requireNonNull(values, field);
        if (values.size() != size) throw new IllegalArgumentException(field + " must contain exactly " + size + " values");
        return values.stream().map(value -> text(value, field)).toList();
    }

    private static Set<String> nonEmptyTags(Set<String> values) {
        Objects.requireNonNull(values, "affinityTags");
        if (values.isEmpty()) throw new IllegalArgumentException("affinityTags must not be empty");
        return values.stream().map(NightmareFactionAnswerFollowupCatalog::stableId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
