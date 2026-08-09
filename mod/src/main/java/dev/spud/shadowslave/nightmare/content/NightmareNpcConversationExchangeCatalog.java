package dev.spud.shadowslave.nightmare.content;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Authored DESIGN conversation-exchange modules for an already-resolved Nightmare
 * scenario and actor context.
 *
 * <p>This catalogue presents bounded social beats. It does not determine hidden
 * truth, allegiance, persuasion success, accepted scenario events, appraisal,
 * rewards, progression or persistence.</p>
 */
public final class NightmareNpcConversationExchangeCatalog {
    public static final String GENERATOR_VERSION = "nightmare-npc-conversation-exchange-v1";

    private NightmareNpcConversationExchangeCatalog() {
    }

    public enum ExchangeFamily {
        QUESTION,
        ANSWER,
        COUNTER_QUESTION,
        VERIFICATION,
        DISENGAGEMENT
    }

    public record ExchangePrimitive(
            String id,
            ExchangeFamily family,
            String title,
            String exchangeRead,
            String npcLine,
            List<String> playerOptions,
            Set<String> affinityTags,
            List<String> presentationCues,
            String boundary
    ) {
        public ExchangePrimitive {
            id = stableId(id);
            family = Objects.requireNonNull(family, "family");
            title = text(title, "title");
            exchangeRead = text(exchangeRead, "exchangeRead");
            npcLine = text(npcLine, "npcLine");
            playerOptions = nonEmptyText(playerOptions, "playerOptions");
            if (playerOptions.size() != 3) {
                throw new IllegalArgumentException("playerOptions must contain exactly three authored responses");
            }
            affinityTags = tags(affinityTags, "affinityTags");
            if (affinityTags.isEmpty()) {
                throw new IllegalArgumentException("affinityTags cannot be empty");
            }
            presentationCues = nonEmptyText(presentationCues, "presentationCues");
            if (presentationCues.size() != 2) {
                throw new IllegalArgumentException("presentationCues must contain exactly two cues");
            }
            boundary = text(boundary, "boundary");
        }
    }

    public record ComposedExchange(
            String scenarioId,
            String actorContextId,
            String primitiveId,
            ExchangeFamily family,
            String title,
            String exchangeRead,
            String npcLine,
            List<String> playerOptions,
            String presentationCue,
            String boundary,
            Set<String> matchedEvidenceTags,
            long seed,
            String generatorVersion
    ) {
        public ComposedExchange {
            scenarioId = stableId(scenarioId);
            actorContextId = stableId(actorContextId);
            primitiveId = stableId(primitiveId);
            family = Objects.requireNonNull(family, "family");
            title = text(title, "title");
            exchangeRead = text(exchangeRead, "exchangeRead");
            npcLine = text(npcLine, "npcLine");
            playerOptions = nonEmptyText(playerOptions, "playerOptions");
            presentationCue = text(presentationCue, "presentationCue");
            boundary = text(boundary, "boundary");
            matchedEvidenceTags = Set.copyOf(Objects.requireNonNull(matchedEvidenceTags, "matchedEvidenceTags"));
            generatorVersion = text(generatorVersion, "generatorVersion");
        }
    }

    public static List<ExchangePrimitive> waveOne() {
        List<ExchangePrimitive> primitives = List.of(
                primitive("ask_for_sequence", ExchangeFamily.QUESTION, "Ask for the Sequence",
                        "Separate what happened first from what the speaker thinks it meant.",
                        "Start at the first thing you personally saw. Leave the conclusions for later.",
                        List.of("Ask for the next observed step.", "Ask who else was present.", "Stop and compare the account with physical evidence."),
                        Set.of("observation", "timeline", "testimony"),
                        List.of("The question is narrow enough to answer without confessing anything else.", "You keep the wording anchored to what could have been witnessed."),
                        "A chronological answer is still testimony, not automatic proof that the speaker is truthful or complete."),
                primitive("ask_for_source", ExchangeFamily.QUESTION, "Ask for the Source",
                        "Trace an assertion back to where the speaker says it came from.",
                        "Did you see that yourself, hear it from someone else, or infer it afterward?",
                        List.of("Request the original witness or object.", "Record the claim as second-hand.", "Leave the source unresolved and move on."),
                        Set.of("source", "testimony", "evidence"),
                        List.of("The room quiets as the claim is separated from its source.", "You ask for provenance rather than demanding agreement."),
                        "Naming a source does not prove that source accurate, honest or available."),
                primitive("ask_for_limit", ExchangeFamily.QUESTION, "Ask What They Do Not Know",
                        "Make uncertainty explicit before acting on the account.",
                        "What part of this are you least certain about?",
                        List.of("Mark the uncertain part for verification.", "Ask what would change their mind.", "Accept the uncertainty without resolving it."),
                        Set.of("uncertainty", "testimony", "caution"),
                        List.of("The question makes room for an incomplete answer.", "You ask for the edge of the speaker's knowledge, not a declaration of innocence."),
                        "Admitting uncertainty is not evidence of honesty, guilt or innocence."),
                primitive("ask_for_stake", ExchangeFamily.QUESTION, "Ask What Changes",
                        "Expose the practical consequence the speaker is focused on without assuming motive.",
                        "If everyone here believed you, what would you want us to do next?",
                        List.of("Ask who benefits from that action.", "Compare the proposed action with other evidence.", "Decline to act on the request yet."),
                        Set.of("consequence", "obligation", "leverage"),
                        List.of("The question shifts from accusation to consequence.", "You ask about the requested action instead of pretending to know why it matters to them."),
                        "A requested outcome can reveal pressure without proving hidden motive or allegiance."),

                primitive("answer_with_observation", ExchangeFamily.ANSWER, "Answer with Observation",
                        "Offer only what the actor context is allowed to know directly.",
                        "I can tell you what I saw. I cannot tell you what it meant.",
                        List.of("Give the bounded observation.", "Name what remains unknown.", "Refuse to add inference as fact."),
                        Set.of("observation", "restraint", "evidence"),
                        List.of("The answer stays deliberately smaller than the accusation.", "You separate the witnessed detail from every tempting conclusion."),
                        "The module does not create or validate an observation; authoritative scenario state must supply any fact actually known."),
                primitive("answer_with_condition", ExchangeFamily.ANSWER, "Answer with a Condition",
                        "Provide limited information while making the next verification step explicit.",
                        "I will give you the route I used if we first confirm the marker is still there.",
                        List.of("Accept the verification step.", "Offer another way to verify the route.", "Decline the exchange."),
                        Set.of("route", "verification", "bargain"),
                        List.of("The answer arrives with a practical condition instead of a promise of trust.", "Information is offered, but only alongside a check both sides can observe."),
                        "A conditional answer does not calculate persuasion, enforce a bargain or guarantee the information is correct."),
                primitive("answer_with_gap", ExchangeFamily.ANSWER, "Answer with a Gap",
                        "State useful information while preserving a deliberate or unavoidable unknown.",
                        "I know who carried the message to the gate. I do not know who wrote it.",
                        List.of("Follow the known carrier.", "Ask how the message was identified.", "Keep authorship unresolved."),
                        Set.of("message", "uncertainty", "testimony"),
                        List.of("One part of the account becomes usable while another remains open.", "The answer gives you a lead without closing the larger question."),
                        "A gap is not automatically concealment, deception or proof of innocence."),
                primitive("answer_with_refusal", ExchangeFamily.ANSWER, "Answer Without Surrendering the Point",
                        "Respond to the immediate question while refusing a broader conclusion.",
                        "Yes, I was there. No, that does not tell you why it happened.",
                        List.of("Ask for the witnessed sequence.", "Ask what conclusion they reject.", "Move to independent evidence."),
                        Set.of("refusal", "testimony", "boundary"),
                        List.of("The answer concedes one fact and fences off the inference built around it.", "A narrow admission prevents the conversation from pretending the whole dispute is settled."),
                        "A limited admission cannot be promoted into guilt, motive, allegiance or scenario resolution."),

                primitive("counter_question_interest", ExchangeFamily.COUNTER_QUESTION, "Ask Why It Matters",
                        "The speaker redirects attention toward the questioner's immediate interest.",
                        "Before I answer, tell me why that detail matters to you.",
                        List.of("Explain the practical reason.", "Refuse and repeat the original question.", "Offer a narrower question instead."),
                        Set.of("leverage", "interest", "caution"),
                        List.of("The conversation briefly turns back on the questioner.", "The reply asks for context without assuming hostility."),
                        "A counter-question does not prove evasion, manipulation or hostile intent."),
                primitive("counter_question_evidence", ExchangeFamily.COUNTER_QUESTION, "Ask What They Have",
                        "The speaker tests whether the accusation rests on evidence or repetition.",
                        "What have you actually found, apart from what people keep saying?",
                        List.of("Present a bounded piece of evidence.", "Admit the claim is still provisional.", "Decline to reveal the evidence yet."),
                        Set.of("evidence", "accusation", "verification"),
                        List.of("The reply forces the distinction between evidence and consensus.", "The speaker asks to see the foundation of the claim before adding more testimony."),
                        "Requesting evidence does not make the speaker truthful or the accusation false."),
                primitive("counter_question_cost", ExchangeFamily.COUNTER_QUESTION, "Ask Who Pays",
                        "The speaker reframes a proposed action around its immediate cost.",
                        "If we do what you want, who is left exposed?",
                        List.of("Name the people or position at risk.", "Propose a smaller commitment.", "Withdraw the proposed action."),
                        Set.of("sacrifice", "obligation", "consequence"),
                        List.of("The next choice becomes harder to treat as abstract.", "The reply names the cost that the first proposal left outside the frame."),
                        "This does not calculate morality, loyalty, appraisal value or the correct choice."),
                primitive("counter_question_authority", ExchangeFamily.COUNTER_QUESTION, "Ask Who Decided",
                        "The speaker challenges whether an instruction has an identified authority.",
                        "Whose order is that, exactly?",
                        List.of("Name the claimed authority.", "Treat the instruction as unverified.", "Ask for a token, witness or record."),
                        Set.of("authority", "order", "verification"),
                        List.of("The title behind the instruction suddenly matters as much as the words.", "You pause before treating an asserted order as settled authority."),
                        "A named authority or token must still be validated by scenario state; presentation cannot confer command authority."),

                primitive("verify_against_object", ExchangeFamily.VERIFICATION, "Check Against the Object",
                        "Compare testimony with a physical record or object already present in the scenario.",
                        "Put the claim beside the ledger, mark, tool or damage it refers to.",
                        List.of("Compare the visible details.", "Ask another observer to check independently.", "Record the mismatch without deciding why it exists."),
                        Set.of("evidence", "object", "verification"),
                        List.of("The conversation pauses while attention moves to something both sides can inspect.", "Words give way to an object that can at least constrain the argument."),
                        "A match or mismatch does not automatically prove authorship, intent, forgery or truthfulness."),
                primitive("verify_with_second_account", ExchangeFamily.VERIFICATION, "Seek a Second Account",
                        "Cross-check one bounded claim without merging witnesses into a single assumed truth.",
                        "Ask another witness the same narrow question separately.",
                        List.of("Compare only overlapping details.", "Preserve disagreements explicitly.", "Leave the claim provisional if accounts cannot be reconciled."),
                        Set.of("testimony", "witness", "verification"),
                        List.of("The second account is kept separate long enough to reveal where it actually agrees.", "You avoid letting one witness hear the answer they are expected to repeat."),
                        "Agreement is not perfect proof and disagreement is not automatic evidence of deception."),
                primitive("verify_by_repeatable_check", ExchangeFamily.VERIFICATION, "Repeat the Check",
                        "Use a local action that another participant can independently repeat.",
                        "If the route, signal or mechanism is real, let someone else perform the same check.",
                        List.of("Repeat the check with a second observer.", "Change one condition and compare.", "Stop if the check would create unacceptable risk."),
                        Set.of("repeatable", "route", "signal"),
                        List.of("The claim becomes a procedure someone else can attempt.", "You look for a repeatable observation rather than a stronger assertion."),
                        "Repeatability can support a local observation but does not establish a universal rule or hidden supernatural truth."),
                primitive("verify_boundary", ExchangeFamily.VERIFICATION, "Verify the Boundary",
                        "Test the exact limit of a claim rather than treating it as universally true.",
                        "Show me where this stops being true.",
                        List.of("Check one edge case.", "Record the known limit.", "Refuse to generalize beyond the tested condition."),
                        Set.of("boundary", "uncertainty", "verification"),
                        List.of("The useful question becomes where the claim fails, not how confidently it was spoken.", "You look for the edge of the observation before building a plan around it."),
                        "A tested local boundary cannot become a canonical law, universal mechanic or probability formula."),

                primitive("disengage_preserve_question", ExchangeFamily.DISENGAGEMENT, "Leave the Question Open",
                        "End the exchange without pretending the unresolved point has disappeared.",
                        "We are not settling this now. Mark the question and move.",
                        List.of("Record what remains unresolved.", "Name what evidence would justify reopening it.", "Leave without assigning blame."),
                        Set.of("withdrawal", "uncertainty", "caution"),
                        List.of("The conversation ends with the disputed point still visible.", "You close the exchange without converting exhaustion into agreement."),
                        "Ending the conversation does not settle truth, guilt, allegiance, reputation or scenario outcome."),
                primitive("disengage_under_pressure", ExchangeFamily.DISENGAGEMENT, "Break for Immediate Danger",
                        "Suspend the social dispute because an already-authored local pressure requires attention.",
                        "Argue later. Deal with the immediate danger first.",
                        List.of("Name a regroup point.", "Carry the unresolved claim forward.", "Separate without promising cooperation."),
                        Set.of("danger", "withdrawal", "priority"),
                        List.of("The argument is interrupted by a problem neither side can ignore forever.", "Immediate survival pressure overtakes the conversation without resolving it."),
                        "A shared danger does not automatically create trust, alliance or a truce."),
                primitive("disengage_after_refusal", ExchangeFamily.DISENGAGEMENT, "Accept the Refusal for Now",
                        "Stop pressing an answer when continued pressure would add no verified information.",
                        "You have refused. I will work with what can be checked elsewhere.",
                        List.of("Seek another source.", "Preserve the refusal as context only.", "Revisit later if new evidence appears."),
                        Set.of("refusal", "withdrawal", "evidence"),
                        List.of("The refusal remains part of the scene without becoming a confession.", "You stop trying to force certainty out of the same exchange."),
                        "Refusal remains ambiguous; it cannot be converted into guilt, lie detection or failed persuasion."),
                primitive("disengage_with_terms", ExchangeFamily.DISENGAGEMENT, "Leave with Terms Unaccepted",
                        "End a bargain or conditional exchange without silently treating its terms as active.",
                        "Those terms are not accepted. If they change, ask again.",
                        List.of("State which term blocked agreement.", "Offer no counterproposal.", "Leave a bounded counterproposal for later."),
                        Set.of("bargain", "withdrawal", "boundary"),
                        List.of("The exchange ends before ambiguity can masquerade as consent.", "The unaccepted terms remain visible rather than becoming an implied deal."),
                        "No bargain, quest, allegiance change or scenario event exists unless authoritative Java state explicitly records it.")
        );
        validate(primitives);
        return primitives;
    }

    public static ComposedExchange compose(
            long seed,
            String scenarioId,
            String actorContextId,
            Set<ExchangeFamily> allowedFamilies,
            Map<String, Integer> evidence
    ) {
        String checkedScenario = stableId(scenarioId);
        String checkedActor = stableId(actorContextId);
        Set<ExchangeFamily> families = Set.copyOf(Objects.requireNonNull(allowedFamilies, "allowedFamilies"));
        if (families.isEmpty()) {
            throw new IllegalArgumentException("allowedFamilies cannot be empty");
        }

        Set<String> positiveEvidence = positiveEvidence(evidence);
        List<ExchangePrimitive> candidates = waveOne().stream()
                .filter(primitive -> families.contains(primitive.family()))
                .sorted(Comparator.comparing(ExchangePrimitive::id))
                .toList();
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No authored exchange primitive matches allowedFamilies");
        }

        List<ExchangePrimitive> matched = candidates.stream()
                .filter(primitive -> primitive.affinityTags().stream().anyMatch(positiveEvidence::contains))
                .toList();
        List<ExchangePrimitive> pool = matched.isEmpty() ? candidates : matched;
        String evidenceKey = positiveEvidence.stream().sorted().reduce("", (left, right) -> left + "," + right);
        int primitiveIndex = index(seed, checkedScenario + "|" + checkedActor + "|" + evidenceKey + "|primitive", pool.size());
        ExchangePrimitive selected = pool.get(primitiveIndex);
        int cueIndex = index(seed, checkedScenario + "|" + checkedActor + "|" + selected.id() + "|cue", selected.presentationCues().size());
        Set<String> matchedTags = selected.affinityTags().stream()
                .filter(positiveEvidence::contains)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());

        return new ComposedExchange(
                checkedScenario,
                checkedActor,
                selected.id(),
                selected.family(),
                selected.title(),
                selected.exchangeRead(),
                selected.npcLine(),
                selected.playerOptions(),
                selected.presentationCues().get(cueIndex),
                selected.boundary(),
                matchedTags,
                seed,
                GENERATOR_VERSION
        );
    }

    private static ExchangePrimitive primitive(
            String id,
            ExchangeFamily family,
            String title,
            String exchangeRead,
            String npcLine,
            List<String> playerOptions,
            Set<String> affinityTags,
            List<String> cues,
            String boundary
    ) {
        return new ExchangePrimitive(id, family, title, exchangeRead, npcLine, playerOptions, affinityTags, cues, boundary);
    }

    private static void validate(List<ExchangePrimitive> primitives) {
        Set<String> ids = new HashSet<>();
        Map<ExchangeFamily, Integer> counts = new EnumMap<>(ExchangeFamily.class);
        for (ExchangePrimitive primitive : primitives) {
            if (!ids.add(primitive.id())) {
                throw new IllegalArgumentException("Duplicate exchange primitive id: " + primitive.id());
            }
            counts.merge(primitive.family(), 1, Integer::sum);
        }
        for (ExchangeFamily family : ExchangeFamily.values()) {
            if (counts.getOrDefault(family, 0) != 4) {
                throw new IllegalArgumentException("Wave one must contain exactly four primitives for " + family);
            }
        }
    }

    private static Set<String> positiveEvidence(Map<String, Integer> evidence) {
        Map<String, Integer> checked = Objects.requireNonNull(evidence, "evidence");
        Set<String> result = new HashSet<>();
        for (Map.Entry<String, Integer> entry : checked.entrySet()) {
            String tag = stableId(entry.getKey());
            int value = Objects.requireNonNull(entry.getValue(), "evidence value");
            if (value < 0) {
                throw new IllegalArgumentException("evidence values cannot be negative");
            }
            if (value > 0) {
                result.add(tag);
            }
        }
        return Set.copyOf(result);
    }

    private static int index(long seed, String salt, int bound) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((GENERATOR_VERSION + "|" + seed + "|" + salt).getBytes(StandardCharsets.UTF_8));
            long value = 0L;
            for (int i = 0; i < Long.BYTES; i++) {
                value = (value << 8) | (bytes[i] & 0xffL);
            }
            return (int) Long.remainderUnsigned(value, bound);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

    private static String stableId(String value) {
        String checked = text(value, "id").toLowerCase(Locale.ROOT);
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

    private static List<String> nonEmptyText(List<String> source, String name) {
        List<String> result = new ArrayList<>();
        for (String value : Objects.requireNonNull(source, name)) {
            result.add(text(value, name + " entry"));
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be empty");
        }
        return List.copyOf(result);
    }

    private static Set<String> tags(Set<String> source, String name) {
        Set<String> result = new HashSet<>();
        for (String value : Objects.requireNonNull(source, name)) {
            result.add(stableId(value));
        }
        return Set.copyOf(result);
    }
}
