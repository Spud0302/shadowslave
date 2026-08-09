package dev.spud.shadowslave.nightmare.content;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DESIGN-only player-facing investigation journal entries built around already-resolved Nightmare evidence identities.
 * This catalogue records presentation, not truth, certainty progression, accepted events, appraisal, or progression.
 */
public final class NightmareInvestigationJournalCatalog {
    public static final String GENERATOR_VERSION = "nightmare-investigation-journal-v1";

    private NightmareInvestigationJournalCatalog() {}

    public enum EntryState {
        OBSERVED,
        CONTRADICTED,
        UNRESOLVED,
        PRESERVED
    }

    public record Primitive(
            String id,
            EntryState state,
            String title,
            String journalRead,
            String nextQuestion,
            List<String> playerActions,
            Set<String> affinityTags,
            List<String> presentationCues,
            String antiOverclaimBoundary
    ) {
        public Primitive {
            id = stableId(id);
            state = Objects.requireNonNull(state, "state");
            title = text(title, "title");
            journalRead = text(journalRead, "journalRead");
            nextQuestion = text(nextQuestion, "nextQuestion");
            playerActions = exactTextList(playerActions, 3, "playerActions");
            affinityTags = nonEmptyTags(affinityTags, "affinityTags");
            presentationCues = exactTextList(presentationCues, 2, "presentationCues");
            antiOverclaimBoundary = text(antiOverclaimBoundary, "antiOverclaimBoundary");
        }
    }

    public record Selection(
            String generatorVersion,
            long seed,
            String scenarioId,
            String actorContextId,
            String evidenceLinkId,
            String verificationExchangeId,
            Primitive primitive,
            String presentationCue,
            Set<String> matchedEvidenceTags
    ) {
        public Selection {
            generatorVersion = text(generatorVersion, "generatorVersion");
            scenarioId = stableId(scenarioId);
            actorContextId = stableId(actorContextId);
            evidenceLinkId = stableId(evidenceLinkId);
            verificationExchangeId = stableId(verificationExchangeId);
            primitive = Objects.requireNonNull(primitive, "primitive");
            presentationCue = text(presentationCue, "presentationCue");
            matchedEvidenceTags = Set.copyOf(Objects.requireNonNull(matchedEvidenceTags, "matchedEvidenceTags"));
        }
    }

    public static List<Primitive> waveOne() {
        return List.of(
                p("observed_sequence", EntryState.OBSERVED, "Sequence Recorded",
                        "The journal keeps only the order that was actually observed or independently established, without filling the missing steps.",
                        "Which part of the sequence is direct observation, and where does inference begin?",
                        List.of("Mark the observed order.", "Separate later interpretation.", "Leave unobserved steps blank."),
                        Set.of("sequence", "observation", "record"),
                        List.of("A short ordered note keeps observation separate from reconstruction.", "The entry stops exactly where the evidence stops."),
                        "An observed sequence does not prove motive, identity, hidden cause, or a complete timeline."),
                p("observed_object", EntryState.OBSERVED, "Object Condition",
                        "A physical object's present condition is recorded before anyone assigns a cause to the damage, repair, placement, or absence.",
                        "What can be described about the object without claiming who changed it or why?",
                        List.of("Record visible condition.", "Record location and orientation.", "Keep cause separate."),
                        Set.of("object", "damage", "observation"),
                        List.of("The object is described before the story around it.", "Physical condition becomes a reference point rather than a verdict."),
                        "Object condition cannot identify sabotage, guilt, authenticity, ownership, or intent."),
                p("observed_signal", EntryState.OBSERVED, "Signal Heard",
                        "A signal pattern is recorded as an event that occurred, while sender, meaning, and consequence remain separate questions.",
                        "What exact part of the signal was perceived, and what interpretation is merely proposed?",
                        List.of("Record the pattern.", "Record the local conditions.", "Leave sender and meaning unassigned."),
                        Set.of("signal", "observation", "uncertainty"),
                        List.of("The journal captures the cue without turning it into prophecy.", "One heard pattern remains one bounded observation."),
                        "A signal entry does not identify a sender, predict danger, or create a scenario event."),
                p("observed_route", EntryState.OBSERVED, "Route Trace",
                        "Marks, wear, blockage, or recent passage are noted as route evidence without declaring the route safe, correct, or intended.",
                        "Which route fact can be seen now, and which travel conclusion still needs verification?",
                        List.of("Record the trace.", "Mark the nearest fixed reference.", "Keep safety and destination unresolved."),
                        Set.of("route", "location", "observation"),
                        List.of("A route note records what the ground says, not what the player hopes it means.", "The entry remains useful even when the destination is unknown."),
                        "Route traces do not guarantee safety, destination, ownership, or future accessibility."),
                p("observed_testimony", EntryState.OBSERVED, "Statement Recorded",
                        "A witness statement is preserved as something a person said, not automatically as something that happened.",
                        "Which words are the witness's claim, and which facts can be checked separately?",
                        List.of("Record the claim faithfully.", "Mark firsthand versus relayed detail.", "List one checkable point."),
                        Set.of("testimony", "source", "observation"),
                        List.of("The journal distinguishes speech from fact.", "A clean transcript makes later comparison possible."),
                        "Recording testimony does not certify truthfulness, reliability, guilt, or allegiance."),

                p("contradicted_sequence", EntryState.CONTRADICTED, "Sequence Conflict",
                        "Two surviving accounts disagree about the order of events, so the journal preserves the first concrete divergence instead of choosing a winner.",
                        "What is the smallest ordering conflict that can still be tested?",
                        List.of("Write both orders.", "Mark the first divergence.", "Seek an independent anchor."),
                        Set.of("sequence", "contradiction", "testimony"),
                        List.of("The conflict is narrowed to one disputed transition.", "Both accounts remain visible until another fact constrains them."),
                        "A sequence conflict is not proof of lying, forgery, guilt, or the correct account."),
                p("contradicted_time", EntryState.CONTRADICTED, "Timing Conflict",
                        "Available evidence places the same event in incompatible relative positions, while exact clock time remains unavailable or unreliable.",
                        "Can the contradiction be resolved with relative order rather than invented timestamps?",
                        List.of("Keep supported relative times.", "Reject invented precision.", "Record unresolved overlap."),
                        Set.of("timing", "contradiction", "uncertainty"),
                        List.of("The journal keeps the conflict without pretending the world supplied a clock.", "Relative timing remains useful even when exact time is unknown."),
                        "This entry does not create canonical timestamps or imply intentional deception."),
                p("contradicted_authority", EntryState.CONTRADICTED, "Authority Disputed",
                        "An instruction or record claims authority that another source contests, so wording and legitimacy are logged as separate questions.",
                        "What supports the claimed authority independently of the order itself?",
                        List.of("Record the claimed role.", "Record the competing claim.", "Seek independent recognition."),
                        Set.of("authority", "contradiction", "record"),
                        List.of("The order can be authentic text while its authority remains disputed.", "Confidence and legitimacy stay separate in the entry."),
                        "The journal cannot grant authority, choose a legitimate faction, or accept a scenario event."),
                p("contradicted_location", EntryState.CONTRADICTED, "Location Conflict",
                        "A claimed location does not fit another bounded route, object, or line-of-sight reference, but the conflict alone does not explain why.",
                        "Which location fact is incompatible, and what alternative explanation remains possible?",
                        List.of("Mark the fixed reference.", "Write the conflicting claim.", "Preserve alternate explanations."),
                        Set.of("location", "route", "contradiction"),
                        List.of("The journal shows exactly where the geography stops agreeing with the account.", "A place conflict becomes a question rather than an accusation."),
                        "Location inconsistency does not establish identity, deception, intent, or guilt."),
                p("contradicted_object_account", EntryState.CONTRADICTED, "Object and Account Diverge",
                        "A physical trace and a spoken or written account cannot both be accepted literally as currently described.",
                        "Which feature of the object conflicts with which exact claim?",
                        List.of("Describe the feature.", "Quote the claim in summary.", "Keep cause and culprit unresolved."),
                        Set.of("object", "testimony", "contradiction"),
                        List.of("The object and account are held side by side without forcing either into automatic truth.", "The contradiction stays bounded to the feature actually inspected."),
                        "A physical contradiction does not prove forgery, sabotage, guilt, or deliberate lying."),

                p("unresolved_gap", EntryState.UNRESOLVED, "Known Gap",
                        "The journal names a missing fact that current evidence cannot establish instead of silently filling it with assumption.",
                        "What single missing fact would most change the current interpretation?",
                        List.of("Name the missing fact.", "Record why it matters.", "Do not substitute motive for evidence."),
                        Set.of("gap", "uncertainty", "inference"),
                        List.of("A named gap remains visible on later review.", "The absence of evidence is recorded without being converted into evidence."),
                        "Naming a gap does not increase certainty or reveal the hidden answer."),
                p("unresolved_hypotheses", EntryState.UNRESOLVED, "Competing Explanations",
                        "Two or more explanations remain compatible with the known evidence, so the journal keeps them separate and testable.",
                        "What future observation would distinguish the leading explanations?",
                        List.of("Write each explanation briefly.", "List one local prediction for each.", "Delay selection until evidence changes."),
                        Set.of("hypothesis", "uncertainty", "inference"),
                        List.of("Alternative explanations prevent a convenient guess from becoming hidden canon.", "The entry turns uncertainty into a future verification target."),
                        "The journal does not calculate probabilities, confidence percentages, or a secretly correct hypothesis."),
                p("unresolved_source", EntryState.UNRESOLVED, "Source Unclear",
                        "Useful information survives, but its original source, chain of relay, or authority cannot currently be established.",
                        "What can still be used safely if provenance remains unknown?",
                        List.of("Keep the information bounded.", "Mark provenance as unresolved.", "Seek an independent source before escalation."),
                        Set.of("source", "uncertainty", "record"),
                        List.of("Information can remain operationally relevant without invented provenance.", "The journal makes the missing source explicit."),
                        "Unknown provenance is not evidence of forgery, deception, or low reliability by itself."),
                p("unresolved_meaning", EntryState.UNRESOLVED, "Meaning Unsettled",
                        "A signal, mark, phrase, or object feature is real enough to record but supports more than one plausible interpretation.",
                        "What is directly present, and which meanings remain interpretations?",
                        List.of("Record the sign itself.", "List plausible meanings separately.", "Avoid treating familiarity as proof."),
                        Set.of("signal", "inference", "uncertainty"),
                        List.of("The sign stays concrete while its meaning stays open.", "Interpretation remains detachable from observation."),
                        "This entry does not reveal hidden messages, prophecy, intent, or supernatural truth."),
                p("unresolved_refusal", EntryState.UNRESOLVED, "Refusal Left Open",
                        "A person refuses, delays, or limits an answer, and the journal preserves that social fact without assigning guilt or hostility.",
                        "What did the refusal actually prevent from being learned?",
                        List.of("Record the refused question.", "Record any stated boundary.", "Leave motive unresolved."),
                        Set.of("refusal", "testimony", "uncertainty"),
                        List.of("Silence becomes a recorded limit, not a confession.", "The unanswered question remains available for another route."),
                        "Refusal does not prove guilt, hostility, deception, allegiance, or future non-cooperation."),

                p("preserved_original", EntryState.PRESERVED, "Original Preserved",
                        "The original wording, mark, layout, or object state is retained before any summary, copy, repair, or reinterpretation is made.",
                        "What must remain unchanged so later comparison is still possible?",
                        List.of("Preserve the original.", "Create a separate working copy.", "Record any unavoidable change."),
                        Set.of("preservation", "record", "object"),
                        List.of("The journal separates the original from later handling.", "Future comparison stays possible because alteration is not hidden."),
                        "Preservation does not certify authenticity, ownership, chain of custody, or truth."),
                p("preserved_contradiction", EntryState.PRESERVED, "Contradiction Preserved",
                        "Two incompatible details are carried forward together so later evidence can resolve them without rewriting the earlier record.",
                        "Which precise contradiction must remain visible on the next review?",
                        List.of("Keep both claims.", "Mark the disputed fact.", "Append later findings instead of overwriting."),
                        Set.of("preservation", "contradiction", "verification"),
                        List.of("The contradiction becomes durable evidence of uncertainty.", "Later resolution can be added without pretending the conflict never existed."),
                        "Preserving a contradiction does not decide which side is true or assign blame."),
                p("preserved_sample", EntryState.PRESERVED, "Bounded Sample Preserved",
                        "A small allowed sample or representation is retained for later comparison while the journal refuses to generalize beyond it.",
                        "What exact property does this sample preserve, and what does it fail to represent?",
                        List.of("Label the sample boundary.", "Record collection conditions.", "Avoid generalizing beyond the sample."),
                        Set.of("preservation", "sample", "object"),
                        List.of("A bounded sample stays useful because its limits are written beside it.", "The record keeps collection context separate from later interpretation."),
                        "A preserved sample does not establish rarity, universality, authenticity, or supernatural properties."),
                p("preserved_testimony", EntryState.PRESERVED, "Account Preserved",
                        "A witness account is retained in its own terms before later summaries merge it with other evidence.",
                        "Which parts are firsthand, relayed, uncertain, or explicitly omitted?",
                        List.of("Preserve the original account.", "Mark source distance.", "Append checks separately."),
                        Set.of("preservation", "testimony", "source"),
                        List.of("The witness's words remain distinguishable from later analysis.", "Corrections can be appended without silently rewriting the account."),
                        "Preserving testimony does not certify truthfulness, memory accuracy, allegiance, or guilt."),
                p("preserved_route_record", EntryState.PRESERVED, "Route Record Preserved",
                        "A route state, marker layout, or access note is retained as a historical observation even if later conditions change.",
                        "What was true when this route note was made, and what must be rechecked before reuse?",
                        List.of("Date the observation context.", "Keep fixed references.", "Require fresh verification before acting."),
                        Set.of("preservation", "route", "record"),
                        List.of("Old route knowledge stays useful without masquerading as current world state.", "The entry makes freshness a visible limitation."),
                        "A preserved route record does not guarantee current safety, access, travel time, or unchanged conditions.")
        );
    }

    public static Selection compose(
            long seed,
            String scenarioId,
            String actorContextId,
            String evidenceLinkId,
            String verificationExchangeId,
            Set<EntryState> allowedStates,
            Map<String, Integer> evidence
    ) {
        String stableScenarioId = stableId(scenarioId);
        String stableActorContextId = stableId(actorContextId);
        String stableEvidenceLinkId = stableId(evidenceLinkId);
        String stableVerificationExchangeId = stableId(verificationExchangeId);
        Set<EntryState> states = Set.copyOf(Objects.requireNonNull(allowedStates, "allowedStates"));
        if (states.isEmpty()) {
            throw new IllegalArgumentException("allowedStates must not be empty");
        }
        Set<String> positiveTags = positiveEvidenceTags(evidence);
        List<Primitive> eligible = waveOne().stream()
                .filter(primitive -> states.contains(primitive.state()))
                .toList();
        if (eligible.isEmpty()) {
            throw new IllegalArgumentException("No journal primitives are compatible with allowedStates");
        }
        List<Primitive> preferred = eligible.stream()
                .filter(primitive -> primitive.affinityTags().stream().anyMatch(positiveTags::contains))
                .toList();
        List<Primitive> pool = preferred.isEmpty() ? eligible : preferred;
        int primitiveIndex = deterministicIndex(seed, stableScenarioId, stableActorContextId,
                stableEvidenceLinkId, stableVerificationExchangeId, "primitive", pool.size());
        Primitive primitive = pool.get(primitiveIndex);
        int cueIndex = deterministicIndex(seed, stableScenarioId, stableActorContextId,
                stableEvidenceLinkId, stableVerificationExchangeId, primitive.id(), primitive.presentationCues().size());
        Set<String> matchedTags = primitive.affinityTags().stream()
                .filter(positiveTags::contains)
                .collect(Collectors.toUnmodifiableSet());
        return new Selection(GENERATOR_VERSION, seed, stableScenarioId, stableActorContextId,
                stableEvidenceLinkId, stableVerificationExchangeId, primitive,
                primitive.presentationCues().get(cueIndex), matchedTags);
    }

    public static Primitive byId(String id) {
        String stableId = stableId(id);
        return waveOne().stream()
                .filter(primitive -> primitive.id().equals(stableId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown journal primitive: " + stableId));
    }

    private static Primitive p(String id, EntryState state, String title, String journalRead, String nextQuestion,
                               List<String> actions, Set<String> tags, List<String> cues, String boundary) {
        return new Primitive(id, state, title, journalRead, nextQuestion, actions, tags, cues, boundary);
    }

    private static Set<String> positiveEvidenceTags(Map<String, Integer> evidence) {
        Objects.requireNonNull(evidence, "evidence");
        List<String> tags = new ArrayList<>();
        evidence.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String tag = stableId(entry.getKey());
                    int value = Objects.requireNonNull(entry.getValue(), "evidence value");
                    if (value < 0) {
                        throw new IllegalArgumentException("Evidence values must not be negative");
                    }
                    if (value > 0) {
                        tags.add(tag);
                    }
                });
        return Set.copyOf(tags);
    }

    private static int deterministicIndex(long seed, String scenarioId, String actorContextId,
                                          String evidenceLinkId, String verificationExchangeId,
                                          String salt, int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("bound must be positive");
        }
        String material = GENERATOR_VERSION + "|" + seed + "|" + scenarioId + "|" + actorContextId
                + "|" + evidenceLinkId + "|" + verificationExchangeId + "|" + salt;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(material.getBytes(StandardCharsets.UTF_8));
            long value = 0L;
            for (int i = 0; i < Long.BYTES; i++) {
                value = (value << 8) | (digest[i] & 0xffL);
            }
            return (int) Math.floorMod(value, bound);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

    private static String stableId(String value) {
        String normalized = text(value, "id").toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z0-9][a-z0-9_-]*")) {
            throw new IllegalArgumentException("Invalid stable id: " + value);
        }
        return normalized;
    }

    private static String text(String value, String field) {
        String normalized = Objects.requireNonNull(value, field).trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return normalized;
    }

    private static List<String> exactTextList(List<String> values, int size, String field) {
        Objects.requireNonNull(values, field);
        if (values.size() != size) {
            throw new IllegalArgumentException(field + " must contain exactly " + size + " entries");
        }
        return values.stream().map(value -> text(value, field)).toList();
    }

    private static Set<String> nonEmptyTags(Set<String> tags, String field) {
        Objects.requireNonNull(tags, field);
        if (tags.isEmpty()) {
            throw new IllegalArgumentException(field + " must not be empty");
        }
        return tags.stream().map(NightmareInvestigationJournalCatalog::stableId)
                .sorted(Comparator.naturalOrder()).collect(Collectors.toUnmodifiableSet());
    }
}
