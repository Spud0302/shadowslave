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

/** DESIGN-only next-step presentation for an already-authorized Nightmare faction information thread. */
public final class NightmareFactionInformationThreadNextStepCatalog {
    public static final String GENERATOR_VERSION = "nightmare-faction-information-thread-next-step-v1";

    private NightmareFactionInformationThreadNextStepCatalog() {}

    public enum Family { RECHECK, SEEK_SOURCE, COMPARE, ARCHIVE }

    public record Primitive(String id, Family family, String title, String situationRead,
                            String actionPrompt, List<String> playerChoices, Set<String> affinityTags,
                            List<String> presentationCues, String antiOverclaimBoundary) {
        public Primitive {
            id = stableId(id);
            family = Objects.requireNonNull(family, "family");
            title = text(title, "title");
            situationRead = text(situationRead, "situationRead");
            actionPrompt = text(actionPrompt, "actionPrompt");
            playerChoices = exactTextList(playerChoices, 3, "playerChoices");
            affinityTags = Set.copyOf(nonEmptyTags(affinityTags));
            presentationCues = exactTextList(presentationCues, 2, "presentationCues");
            antiOverclaimBoundary = text(antiOverclaimBoundary, "antiOverclaimBoundary");
        }
    }

    public record Selection(String generatorVersion, long seed, String scenarioId, String factionId,
                            String threadId, String latestSummaryId, Set<Family> allowedFamilies,
                            Primitive primitive, String presentationCue, Set<String> matchedEvidenceTags) {
        public Selection {
            generatorVersion = text(generatorVersion, "generatorVersion");
            scenarioId = opaqueId(scenarioId, "scenarioId");
            factionId = opaqueId(factionId, "factionId");
            threadId = opaqueId(threadId, "threadId");
            latestSummaryId = opaqueId(latestSummaryId, "latestSummaryId");
            allowedFamilies = Set.copyOf(nonEmptyFamilies(allowedFamilies));
            primitive = Objects.requireNonNull(primitive, "primitive");
            if (!allowedFamilies.contains(primitive.family())) {
                throw new IllegalArgumentException("primitive family must be caller-authorized");
            }
            presentationCue = text(presentationCue, "presentationCue");
            matchedEvidenceTags = Set.copyOf(Objects.requireNonNull(matchedEvidenceTags, "matchedEvidenceTags"));
        }
    }

    public static List<Primitive> waveOne() {
        return List.of(
                p("recheck_current_condition", Family.RECHECK, "Recheck the Current Condition",
                        "The thread still depends on a condition that may have changed since it was last recorded.",
                        "What can be observed again without assuming the earlier condition still holds?",
                        List.of("Repeat the bounded observation.", "Compare only the changed detail.", "Leave the thread open if a recheck is unsafe."),
                        Set.of("recheck", "condition", "current"),
                        List.of("The previous observation remains visible beside the new check.", "Any changed detail is highlighted without supplying a cause."),
                        "A recheck cannot guarantee safety, establish truth, change access, prove motive, or accept a scenario event."),
                p("recheck_route_marker", Family.RECHECK, "Recheck the Route Marker",
                        "A route reference in the thread is old enough that it should be tested against present conditions before use.",
                        "Does the same marker still identify the same bounded route fact?",
                        List.of("Inspect the marker again.", "Compare its present surroundings.", "Keep the old route note as history only."),
                        Set.of("recheck", "route", "marker"),
                        List.of("Old and current marker reads appear side by side.", "No safe-route badge is generated from the comparison."),
                        "Rechecking a route cannot guarantee passability, route safety, ownership, current control, or successful travel."),
                p("recheck_statement_scope", Family.RECHECK, "Recheck the Stated Scope",
                        "A retained statement may still matter, but its scope should be confirmed before it is treated as current context.",
                        "Is the same bounded statement still being made now?",
                        List.of("Ask for the current scope.", "Preserve the older wording.", "Do not inherit broader terms automatically."),
                        Set.of("recheck", "statement", "scope"),
                        List.of("The earlier wording remains attached as history.", "Current wording is displayed separately when supplied."),
                        "A repeated statement cannot prove sincerity, renew a commitment, establish trust, or predict future behavior."),
                p("recheck_observed_detail", Family.RECHECK, "Recheck One Observed Detail",
                        "One concrete observation can be revisited without reopening every claim in the information thread.",
                        "Which single observed detail can be checked again with the least added assumption?",
                        List.of("Repeat one observation.", "Record only what changed.", "Avoid expanding the result beyond that detail."),
                        Set.of("recheck", "observation", "bounded"),
                        List.of("The chosen detail is isolated from the rest of the thread.", "Unrelated claims remain visually unchanged."),
                        "One repeated observation cannot settle unrelated claims, guilt, deception, relationships, or terminal resolution."),

                p("seek_named_source", Family.SEEK_SOURCE, "Seek the Named Source",
                        "The thread identifies a specific source that may narrow the open question if it can actually be reached.",
                        "Can the named source be consulted without treating its answer as automatic truth?",
                        List.of("Find the named source.", "Ask only about the recorded point.", "Keep the source unavailable if contact fails."),
                        Set.of("source", "named", "seek"),
                        List.of("The source is presented as a lead rather than a verdict.", "Availability and reliability remain separate concerns."),
                        "Finding a source cannot certify reliability, truth, innocence, guilt, authority, allegiance, or cooperation."),
                p("seek_original_record", Family.SEEK_SOURCE, "Seek the Original Record",
                        "A copied, relayed, or summarized claim points toward an earlier record that has not yet been inspected directly.",
                        "Is the original record available for a bounded comparison?",
                        List.of("Locate the original record.", "Preserve the copy's provenance.", "Stop if the original cannot be reached."),
                        Set.of("source", "record", "provenance"),
                        List.of("The copied claim keeps its source chain visible.", "The original is not marked authentic merely because it is found."),
                        "An original-looking record cannot automatically prove authenticity, ownership, legitimacy, truth, or scenario progress."),
                p("seek_second_account", Family.SEEK_SOURCE, "Seek a Second Account",
                        "The thread currently depends on one account and can support another bounded perspective without turning agreement into proof.",
                        "Is there an independent account relevant to the same exact detail?",
                        List.of("Ask for an independent account.", "Keep both sources distinct.", "Record disagreement without accusing either source."),
                        Set.of("source", "account", "compare"),
                        List.of("Each account retains its own source marker.", "No majority-truth or confidence score is displayed."),
                        "A second account cannot create truth by agreement, prove deception by disagreement, or calculate source confidence."),
                p("seek_current_witness", Family.SEEK_SOURCE, "Seek a Current Witness",
                        "Older information may need a present observer before the thread can support a current question.",
                        "Who can describe the relevant condition now without inheriting the older conclusion?",
                        List.of("Seek a present observer.", "Ask for direct observations.", "Keep old and new accounts separate."),
                        Set.of("source", "current", "witness"),
                        List.of("Historical and current accounts occupy separate entries.", "Current observation is not promoted into hidden-state knowledge."),
                        "A current witness cannot establish hidden motive, future behavior, truth beyond their observation, or faction relationship state."),

                p("compare_prior_current", Family.COMPARE, "Compare Prior and Current Records",
                        "The thread contains older and newer information that can be placed together without deciding why they differ.",
                        "Which bounded detail changed, stayed the same, or remains unclear?",
                        List.of("Align the same detail across both records.", "Mark only the observed difference.", "Leave causes unresolved."),
                        Set.of("compare", "history", "current"),
                        List.of("Matching fields align across the two records.", "Differences appear without generated explanations."),
                        "A difference cannot prove deception, sabotage, bad faith, guilt, or a relationship change."),
                p("compare_accounts", Family.COMPARE, "Compare the Accounts",
                        "Two retained accounts can be compared at their shared points while each keeps its own provenance.",
                        "Where do the accounts agree, diverge, or describe different scopes?",
                        List.of("Compare shared details first.", "Separate scope differences from contradictions.", "Keep both accounts attached."),
                        Set.of("compare", "account", "provenance"),
                        List.of("Shared and differing details are grouped separately.", "Neither source receives a truth marker from agreement alone."),
                        "Comparison cannot determine who lied, assign guilt, calculate reliability, or settle trust, allegiance, or reputation."),
                p("compare_claim_record", Family.COMPARE, "Compare Claim and Record",
                        "A faction claim and retained record can be checked against the same bounded subject without assuming either is authoritative.",
                        "What exactly aligns or diverges between the claim and record?",
                        List.of("Match the shared subject.", "Keep record provenance visible.", "Preserve any unresolved mismatch."),
                        Set.of("compare", "claim", "record"),
                        List.of("The claim and record are shown in parallel.", "Authenticity and legitimacy remain separate unresolved fields."),
                        "A claim-record match cannot prove authenticity, legitimacy, ownership, truth, access rights, or scenario completion."),
                p("compare_condition_outcome", Family.COMPARE, "Compare Condition and Result",
                        "A stated condition and later observed result can be compared without inventing causation or enforcement.",
                        "Did the observed result match the stated condition, and what remains unexplained?",
                        List.of("Compare only the named condition.", "Record the observed result.", "Leave causation and responsibility unresolved."),
                        Set.of("compare", "condition", "result"),
                        List.of("Condition and result appear as separate entries.", "No causal arrow is generated by presentation alone."),
                        "Matching or diverging condition and result cannot establish causation, blame, contract enforcement, access, or resource transfer."),

                p("archive_closed_thread", Family.ARCHIVE, "Archive the Closed Thread",
                        "The tracked information process is finished, so its records can be retained without generating another objective.",
                        "What context should remain searchable if a genuinely new question appears later?",
                        List.of("Archive the closing summary.", "Keep supporting sources linked.", "Start a new thread only for a new question."),
                        Set.of("archive", "closed", "record"),
                        List.of("The thread moves to history with its source links intact.", "No new task is created from archive status."),
                        "Archiving cannot certify complete truth, erase uncertainty, resolve the Nightmare, or grant appraisal or progression."),
                p("archive_stale_context", Family.ARCHIVE, "Archive Stale Context",
                        "Information that is no longer current can remain useful as dated history without continuing to guide present action.",
                        "Which old details are worth preserving for comparison rather than current use?",
                        List.of("Keep the dated context.", "Remove it from current prompts.", "Link it only when a later question needs history."),
                        Set.of("archive", "stale", "history"),
                        List.of("The record keeps its original date/context marker.", "Archived material is visually distinct from current information."),
                        "Stale context cannot become current truth, route safety, access permission, hostility, or future intent through archival."),
                p("archive_unresolved_gap", Family.ARCHIVE, "Archive an Unresolved Gap",
                        "The thread can end while a bounded question remains unanswered, and that gap can be preserved rather than filled in.",
                        "What unresolved point should remain visible in the historical record?",
                        List.of("Keep the unanswered point.", "Archive attempted checks.", "Do not invent a final verdict."),
                        Set.of("archive", "unresolved", "gap"),
                        List.of("The unanswered point remains explicitly marked.", "No hidden answer appears when the thread is archived."),
                        "An archived gap cannot establish truth, guilt, deception, trust, hostility, allegiance, reputation, or future willingness."),
                p("archive_superseded_thread", Family.ARCHIVE, "Archive a Superseded Thread",
                        "A newer thread now owns the current question, so this one can remain as historical context without transferring conclusions automatically.",
                        "Which sources or observations should be linked forward without copying the old verdict?",
                        List.of("Link the newer thread.", "Preserve original sources here.", "Carry forward only explicitly selected context."),
                        Set.of("archive", "history", "superseded"),
                        List.of("The old thread links forward without being rewritten.", "Current state remains visually separate from historical state."),
                        "Supersession cannot transfer truth, confidence, access, resources, relationship state, world mutation, or progression."));
    }

    public static Selection compose(long seed, String scenarioId, String factionId, String threadId,
                                    String latestSummaryId, Set<Family> allowedFamilies,
                                    Map<String, Integer> evidence) {
        String scenario = opaqueId(scenarioId, "scenarioId");
        String faction = opaqueId(factionId, "factionId");
        String thread = opaqueId(threadId, "threadId");
        String summary = opaqueId(latestSummaryId, "latestSummaryId");
        Set<Family> families = Set.copyOf(nonEmptyFamilies(allowedFamilies));
        Set<String> positive = positiveEvidence(evidence);
        List<Primitive> candidates = waveOne().stream().filter(p -> families.contains(p.family()))
                .sorted(Comparator.comparing(Primitive::id)).toList();
        int best = candidates.stream().mapToInt(p -> overlap(p.affinityTags(), positive)).max().orElse(0);
        List<Primitive> preferred = best > 0
                ? candidates.stream().filter(p -> overlap(p.affinityTags(), positive) == best).toList()
                : candidates;
        String familyKey = families.stream().map(Family::name).sorted().collect(Collectors.joining(","));
        String key = scenario + "|" + faction + "|" + thread + "|" + summary + "|" + familyKey + "|"
                + positive.stream().sorted().collect(Collectors.joining(","));
        Primitive primitive = preferred.get(index(seed, key + "|primitive", preferred.size()));
        String cue = primitive.presentationCues().get(index(seed, key + "|" + primitive.id() + "|cue", 2));
        Set<String> matched = primitive.affinityTags().stream().filter(positive::contains).collect(Collectors.toSet());
        return new Selection(GENERATOR_VERSION, seed, scenario, faction, thread, summary, families,
                primitive, cue, matched);
    }

    public static Primitive requirePrimitive(String id) {
        String checked = stableId(id);
        return waveOne().stream().filter(p -> p.id().equals(checked)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown faction information thread next step: " + id));
    }

    private static Primitive p(String id, Family family, String title, String read, String prompt,
                               List<String> choices, Set<String> tags, List<String> cues, String boundary) {
        return new Primitive(id, family, title, read, prompt, choices, tags, cues, boundary);
    }

    private static int overlap(Set<String> a, Set<String> b) {
        int count = 0;
        for (String value : a) if (b.contains(value)) count++;
        return count;
    }

    private static Set<Family> nonEmptyFamilies(Set<Family> families) {
        Objects.requireNonNull(families, "allowedFamilies");
        if (families.isEmpty()) throw new IllegalArgumentException("allowedFamilies must not be empty");
        if (families.contains(null)) throw new NullPointerException("allowedFamilies must not contain null");
        return new LinkedHashSet<>(families);
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
        Objects.requireNonNull(value, field);
        if (value.trim().isEmpty()) throw new IllegalArgumentException(field + " must not be blank");
        return value;
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
