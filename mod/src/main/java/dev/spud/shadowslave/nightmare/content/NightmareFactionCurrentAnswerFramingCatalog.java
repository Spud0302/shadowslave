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

/** DESIGN-only presentation for already-authorized Nightmare faction answers. */
public final class NightmareFactionCurrentAnswerFramingCatalog {
    public static final String GENERATOR_VERSION = "nightmare-faction-current-answer-framing-v1";

    private NightmareFactionCurrentAnswerFramingCatalog() {}

    public enum AnswerFrame {
        DIRECT,
        LIMITED,
        CONDITIONAL,
        REFUSED
    }

    public record Primitive(String id, AnswerFrame frame, String title, String answerRead,
                            String factionLine, List<String> playerResponses, Set<String> affinityTags,
                            List<String> presentationCues, String antiOverclaimBoundary) {
        public Primitive {
            id = stableId(id);
            frame = Objects.requireNonNull(frame, "frame");
            title = text(title, "title");
            answerRead = text(answerRead, "answerRead");
            factionLine = text(factionLine, "factionLine");
            playerResponses = exactTextList(playerResponses, 3, "playerResponses");
            affinityTags = nonEmptyTags(affinityTags);
            presentationCues = exactTextList(presentationCues, 2, "presentationCues");
            antiOverclaimBoundary = text(antiOverclaimBoundary, "antiOverclaimBoundary");
        }
    }

    public record Selection(String generatorVersion, long seed, String scenarioId, String factionId,
                            String questionId, String answerId, AnswerFrame frame, Primitive primitive,
                            String presentationCue, Set<String> matchedEvidenceTags) {
        public Selection {
            generatorVersion = text(generatorVersion, "generatorVersion");
            scenarioId = opaqueId(scenarioId, "scenarioId");
            factionId = opaqueId(factionId, "factionId");
            questionId = opaqueId(questionId, "questionId");
            answerId = opaqueId(answerId, "answerId");
            frame = Objects.requireNonNull(frame, "frame");
            primitive = Objects.requireNonNull(primitive, "primitive");
            if (primitive.frame() != frame) throw new IllegalArgumentException("primitive frame must match supplied frame");
            presentationCue = text(presentationCue, "presentationCue");
            matchedEvidenceTags = Set.copyOf(Objects.requireNonNull(matchedEvidenceTags, "matchedEvidenceTags"));
        }
    }

    public static List<Primitive> waveOne() {
        return List.of(
                p("direct_present_goal", AnswerFrame.DIRECT, "A Present Goal Stated",
                        "The faction gives a bounded answer about what it says it wants in this encounter.",
                        "Our present goal is the matter in front of us. Nothing beyond that is being answered here.",
                        "Record the stated goal.", "Ask about its scope.", "Keep hidden motive unresolved.",
                        Set.of("goal", "current", "scope"),
                        "The current statement is shown separately from older history.", "No truth marker is attached to the line.",
                        "A direct answer records what was said; it cannot prove truth, hidden motive, trust, allegiance, reputation, or future conduct."),
                p("direct_access_status", AnswerFrame.DIRECT, "Current Access Answered",
                        "The faction gives a present statement about access without the presentation changing the route itself.",
                        "For this meeting, the stated access is the one we have named.",
                        "Record the stated access.", "Ask for the boundary.", "Verify world state separately.",
                        Set.of("access", "current", "boundary"),
                        "The statement is displayed beside the current access question.", "World-state presentation remains visually separate.",
                        "A direct access answer cannot unlock terrain, establish ownership, guarantee safety, or prove the speaker has authority to grant passage."),
                p("direct_open_matter_position", AnswerFrame.DIRECT, "A Current Position Stated",
                        "The faction states where it presently stands on one previously open matter.",
                        "This is our position on that point now.",
                        "Compare it with the old record.", "Ask what changed.", "Leave truth unresolved beyond the statement.",
                        Set.of("open", "position", "current"),
                        "Past and present statements appear side by side.", "Differences are highlighted without a deception label.",
                        "A current position cannot establish truth, bad faith, obligation, trust, hostility, allegiance, or reputation."),
                p("direct_decline_acknowledged", AnswerFrame.DIRECT, "Refusal Acknowledged",
                        "The faction directly acknowledges a player's refusal without the presentation inventing a relationship consequence.",
                        "Your refusal is understood for this proposal.",
                        "End this topic.", "Ask whether another issue remains.", "Leave future reaction unresolved.",
                        Set.of("decline", "terms", "scope"),
                        "The declined proposal is marked closed for this exchange only.", "No hostility or reputation indicator is generated.",
                        "Acknowledging a refusal cannot create peace, hostility, betrayal, reputation change, allegiance, punishment, or future willingness."),

                p("limited_partial_goal", AnswerFrame.LIMITED, "Only Part of the Goal Given",
                        "The faction answers part of the question and leaves the rest expressly unstated.",
                        "That is as much of our purpose as we are stating now.",
                        "Record the stated portion.", "Ask what remains undisclosed.", "Do not fill the gap by inference.",
                        Set.of("goal", "limited", "uncertainty"),
                        "The answered portion is bounded with an explicit unresolved remainder.", "No hidden content is generated behind the gap.",
                        "A limited answer cannot reveal withheld motive, prove deception, determine trust, or infer future behavior from silence."),
                p("limited_access_scope", AnswerFrame.LIMITED, "Access Scope Only",
                        "The faction answers only the named access boundary and does not address broader permission.",
                        "We are answering for that route only.",
                        "Keep the answer route-specific.", "Ask about another area separately.", "Treat silence elsewhere as unresolved.",
                        Set.of("access", "limited", "route"),
                        "Only the named route receives an answer marker.", "Other locations remain unclassified rather than closed or open.",
                        "A limited access answer cannot establish ownership, universal permission, route safety, territorial legitimacy, or access elsewhere."),
                p("limited_source_only", AnswerFrame.LIMITED, "Source Named, Claim Unsettled",
                        "The faction identifies the source it relies on but does not thereby establish that source as true or complete.",
                        "That is the source for our answer. We are not adding more to it now.",
                        "Record the source.", "Compare it with other evidence.", "Preserve disagreement if present.",
                        Set.of("source", "evidence", "limited"),
                        "The source is linked to the answer without a verification badge.", "Contradictory evidence can remain visible beside it.",
                        "Naming a source cannot certify authenticity, truth, innocence, guilt, motive, or the reliability of the speaker."),
                p("limited_no_broader_commitment", AnswerFrame.LIMITED, "No Broader Commitment Given",
                        "The faction answers the immediate point but expressly leaves broader commitment unsettled.",
                        "Our answer goes no further than this matter.",
                        "Keep the scope narrow.", "Ask about broader terms separately.", "Do not infer loyalty from cooperation.",
                        Set.of("commitment", "limited", "scope"),
                        "The answer ends at a visible scope boundary.", "Relationship history remains separate from the current statement.",
                        "A narrow answer cannot establish alliance, trust, reputation, friendship, hostility, debt, or future commitment."),

                p("conditional_after_verification", AnswerFrame.CONDITIONAL, "Answer Depends on Verification",
                        "The faction states that its answer depends on a named check without the presentation deciding that check.",
                        "If that point is verified, our answer applies as stated.",
                        "Ask what counts as verification.", "Record the condition.", "Leave fulfillment to current state.",
                        Set.of("condition", "verification", "evidence"),
                        "The condition is displayed beside the answer without a completion mark.", "Relevant evidence can be linked without automatic resolution.",
                        "A conditional answer cannot decide whether evidence is sufficient, make a claim true, unlock access, or accept a scenario event."),
                p("conditional_after_exchange", AnswerFrame.CONDITIONAL, "Answer Tied to an Exchange",
                        "The faction makes its answer contingent on a stated exchange without the presentation performing that exchange.",
                        "Our answer changes only if the named exchange is actually made.",
                        "Ask for the exact exchange.", "Decline the exchange.", "Leave the answer conditional.",
                        Set.of("condition", "exchange", "terms"),
                        "The exchange appears as an unfulfilled condition rather than a completed transaction.", "No inventory or access state is mutated by the line.",
                        "A conditional exchange cannot transfer resources, create debt, calculate leverage, prove consent, or enforce a contract."),
                p("conditional_named_boundary", AnswerFrame.CONDITIONAL, "Answer Holds Within a Boundary",
                        "The faction gives an answer that applies only while a named boundary or circumstance remains current.",
                        "Within that boundary, this is our answer.",
                        "Ask where the boundary ends.", "Ask what changes it.", "Recheck if circumstances change.",
                        Set.of("condition", "boundary", "current"),
                        "The answer is visibly attached to its stated boundary.", "Changing circumstances do not automatically choose a replacement answer.",
                        "A bounded condition cannot forecast future conduct, establish territorial legitimacy, guarantee safety, or create permanent permission."),
                p("conditional_named_actor", AnswerFrame.CONDITIONAL, "Answer Limited to a Named Actor",
                        "The faction states that the answer applies only to a named participant or role supplied by current state.",
                        "This answer applies only to the person or role already named.",
                        "Confirm the named scope.", "Ask whether others are excluded.", "Do not generalize the answer.",
                        Set.of("condition", "actor", "scope"),
                        "The scoped actor is shown as context, not generated by presentation.", "Other actors remain outside the answer unless separately resolved.",
                        "An actor-scoped answer cannot create membership, allegiance, reputation, authority, immunity, or rights for anyone else."),

                p("refused_no_answer", AnswerFrame.REFUSED, "No Answer Given",
                        "The faction refuses the current question and the missing answer remains missing.",
                        "We are not answering that.",
                        "Record the refusal.", "Ask a different bounded question.", "End the exchange.",
                        Set.of("refusal", "uncertainty", "boundary"),
                        "The unanswered field remains explicitly unresolved.", "No motive or guilt marker replaces the missing answer.",
                        "Refusal cannot prove guilt, deception, hostility, hidden motive, allegiance, reputation, or the truth of the underlying claim."),
                p("refused_access_question", AnswerFrame.REFUSED, "Access Question Refused",
                        "The faction refuses to answer whether current access applies; the presentation does not convert that refusal into a closed route.",
                        "We will not answer the access question here.",
                        "Leave access unresolved.", "Seek another authorized source.", "Choose another known option if one exists.",
                        Set.of("refusal", "access", "route"),
                        "The access field remains unresolved rather than denied.", "No route lock or danger marker is created.",
                        "Refusing an access question cannot close terrain, prove ownership, establish hostility, guarantee danger, or remove other valid access."),
                p("refused_open_matter", AnswerFrame.REFUSED, "Open Matter Left Unanswered",
                        "The faction refuses to revisit a previously open point, preserving rather than resolving the gap.",
                        "We are not reopening that matter now.",
                        "Keep the old matter unresolved.", "Return to the present issue.", "End the exchange without a verdict.",
                        Set.of("refusal", "open", "history"),
                        "The old unresolved record remains intact beside the new refusal.", "The refusal is timestamped as current context rather than a final judgment.",
                        "A refusal to revisit history cannot establish blame, truth, bad faith, obligation, hostility, reputation, or permanent closure."),
                p("refused_without_escalation", AnswerFrame.REFUSED, "Refusal Without Escalation",
                        "The faction declines to answer without the presentation inventing a threat or immediate escalation.",
                        "No answer. That is all for this question.",
                        "Accept the refusal for now.", "Ask whether another topic is open.", "Disengage.",
                        Set.of("refusal", "disengage", "relationship"),
                        "The exchange can end without combat or hostility presentation.", "Future stance remains unset until supplied by later state.",
                        "A refused answer cannot create aggression, peace, friendship, betrayal, reputation change, allegiance, punishment, or future behavior."));
    }

    public static Selection compose(long seed, String scenarioId, String factionId, String questionId,
                                    String answerId, AnswerFrame frame, Map<String, Integer> evidence) {
        String checkedScenario = opaqueId(scenarioId, "scenarioId");
        String checkedFaction = opaqueId(factionId, "factionId");
        String checkedQuestion = opaqueId(questionId, "questionId");
        String checkedAnswer = opaqueId(answerId, "answerId");
        AnswerFrame checkedFrame = Objects.requireNonNull(frame, "frame");
        Set<String> positiveEvidence = positiveEvidence(evidence);

        List<Primitive> candidates = waveOne().stream()
                .filter(p -> p.frame() == checkedFrame)
                .sorted(Comparator.comparing(Primitive::id))
                .toList();
        if (candidates.isEmpty()) throw new IllegalArgumentException("no faction answer framing for supplied frame");

        int bestMatch = candidates.stream().mapToInt(p -> overlap(p.affinityTags(), positiveEvidence)).max().orElse(0);
        List<Primitive> preferred = bestMatch > 0
                ? candidates.stream().filter(p -> overlap(p.affinityTags(), positiveEvidence) == bestMatch).toList()
                : candidates;

        String authorityKey = checkedScenario + "|" + checkedFaction + "|" + checkedQuestion + "|"
                + checkedAnswer + "|" + checkedFrame.name() + "|"
                + positiveEvidence.stream().sorted().collect(Collectors.joining(","));
        Primitive primitive = preferred.get(index(seed, authorityKey + "|primitive", preferred.size()));
        String cue = primitive.presentationCues().get(index(seed,
                authorityKey + "|" + primitive.id() + "|cue", primitive.presentationCues().size()));
        Set<String> matched = primitive.affinityTags().stream().filter(positiveEvidence::contains).sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new Selection(GENERATOR_VERSION, seed, checkedScenario, checkedFaction, checkedQuestion,
                checkedAnswer, checkedFrame, primitive, cue, matched);
    }

    public static Primitive requirePrimitive(String id) {
        String checked = stableId(id);
        return waveOne().stream().filter(p -> p.id().equals(checked)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown faction answer framing: " + id));
    }

    private static Primitive p(String id, AnswerFrame frame, String title, String answerRead, String factionLine,
                               String option1, String option2, String option3, Set<String> tags,
                               String cue1, String cue2, String boundary) {
        return new Primitive(id, frame, title, answerRead, factionLine, List.of(option1, option2, option3),
                tags, List.of(cue1, cue2), boundary);
    }

    private static int overlap(Set<String> left, Set<String> right) {
        int count = 0;
        for (String value : left) if (right.contains(value)) count++;
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
            long value = ByteBuffer.wrap(digest.digest(), 0, Long.BYTES).getLong();
            return (int) Math.floorMod(value, bound);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 unavailable", impossible);
        }
    }

    private static String stableId(String value) {
        String checked = text(value, "id").toLowerCase(Locale.ROOT);
        if (!checked.matches("[a-z0-9][a-z0-9_-]*")) {
            throw new IllegalArgumentException("catalogue ids must use lowercase letters, numbers, underscore, or hyphen");
        }
        return checked;
    }

    private static String opaqueId(String value, String name) {
        return text(value, name);
    }

    private static String text(String value, String name) {
        Objects.requireNonNull(value, name);
        String checked = value.trim();
        if (checked.isEmpty()) throw new IllegalArgumentException(name + " must not be blank");
        return checked;
    }

    private static List<String> exactTextList(List<String> values, int size, String name) {
        Objects.requireNonNull(values, name);
        if (values.size() != size) throw new IllegalArgumentException(name + " must contain exactly " + size + " values");
        return values.stream().map(value -> text(value, name + " entry")).toList();
    }

    private static Set<String> nonEmptyTags(Set<String> values) {
        Objects.requireNonNull(values, "affinityTags");
        if (values.isEmpty()) throw new IllegalArgumentException("affinityTags must not be empty");
        return values.stream().map(NightmareFactionCurrentAnswerFramingCatalog::stableId)
                .sorted().collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
