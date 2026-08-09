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
 * DESIGN-only player-facing verification exchanges for an already-resolved Nightmare evidence link.
 * This catalogue never adjudicates truth, authenticity, guilt, certainty progression, or scenario resolution.
 */
public final class NightmareEvidenceVerificationExchangeCatalog {
    public static final String GENERATOR_VERSION = "nightmare-evidence-verification-exchange-v1";

    private NightmareEvidenceVerificationExchangeCatalog() {}

    public enum Family {
        COMPARE,
        CORROBORATE,
        PRESERVE_UNCERTAINTY,
        PRESERVE_EVIDENCE,
        DISENGAGE
    }

    public record Primitive(
            String id,
            Family family,
            String title,
            String exchangeRead,
            String verificationPrompt,
            List<String> playerResponses,
            Set<String> affinityTags,
            List<String> presentationCues,
            String antiOverclaimBoundary
    ) {
        public Primitive {
            id = stableId(id);
            family = Objects.requireNonNull(family, "family");
            title = text(title, "title");
            exchangeRead = text(exchangeRead, "exchangeRead");
            verificationPrompt = text(verificationPrompt, "verificationPrompt");
            playerResponses = exactTextList(playerResponses, 3, "playerResponses");
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
            Primitive primitive,
            String presentationCue,
            Set<String> matchedEvidenceTags
    ) {
        public Selection {
            generatorVersion = text(generatorVersion, "generatorVersion");
            scenarioId = stableId(scenarioId);
            actorContextId = stableId(actorContextId);
            evidenceLinkId = stableId(evidenceLinkId);
            primitive = Objects.requireNonNull(primitive, "primitive");
            presentationCue = text(presentationCue, "presentationCue");
            matchedEvidenceTags = Set.copyOf(Objects.requireNonNull(matchedEvidenceTags, "matchedEvidenceTags"));
        }
    }

    public static List<Primitive> waveOne() {
        return List.of(
                p("compare_sequence", Family.COMPARE, "Compare the Sequence",
                        "Place two accounts, records, or traces side by side and isolate the first point where their order differs.",
                        "Which step is shared, and where does the sequence first stop matching?",
                        List.of("Mark the common steps.", "Mark the first divergence.", "Leave the reason for the divergence unresolved."),
                        Set.of("sequence", "record", "testimony"),
                        List.of("Agreement first makes the later disagreement easier to see.", "The comparison narrows the question without deciding the answer."),
                        "A sequence mismatch does not identify deception, forgery, guilt, or the correct account."),
                p("compare_time", Family.COMPARE, "Compare the Timing",
                        "Use only relative timing that the available evidence actually supports before comparing two claims.",
                        "Can one event be placed before, after, or during another without inventing an exact clock?",
                        List.of("Keep only supported ordering.", "Separate exact time from relative time.", "Record unresolved overlap."),
                        Set.of("timing", "sequence", "uncertainty"),
                        List.of("Relative order can matter even when exact time is unavailable.", "The useful comparison is smaller than a full timeline."),
                        "This exchange does not create exact timestamps or prove that a timing conflict is intentional."),
                p("compare_location", Family.COMPARE, "Compare the Place",
                        "Cross-check where an account places a person or object against a bounded physical or route reference.",
                        "What location claim is actually testable from the surviving evidence?",
                        List.of("Tie the claim to one fixed reference.", "Check line of sight or access.", "Preserve alternate explanations."),
                        Set.of("location", "route", "object"),
                        List.of("A fixed place constrains a story more reliably than a guess about motive.", "One location check can weaken or strengthen a claim without settling the whole event."),
                        "Location consistency does not prove identity, intent, allegiance, or truthfulness."),
                p("compare_authority", Family.COMPARE, "Compare the Authority",
                        "Separate what an instruction says from whether the speaker or document had authority to issue it.",
                        "What independent fact, if any, supports the claimed authority?",
                        List.of("Check the claimed role.", "Check whether others recognized it at the time.", "Keep legitimacy unresolved if the evidence stops there."),
                        Set.of("authority", "record", "testimony"),
                        List.of("The words of an order and the right to give it are separate questions.", "Authority is something to verify, not infer from confidence."),
                        "This exchange cannot grant authority, identify the legitimate faction, or accept a scenario event."),

                p("seek_second_account", Family.CORROBORATE, "Seek a Second Account",
                        "Ask for an independently obtained account before promoting a single claim beyond its current evidence boundary.",
                        "Can another source address the same concrete detail without copying the first account?",
                        List.of("Ask separately.", "Compare only overlapping details.", "Keep agreement as corroboration rather than proof."),
                        Set.of("testimony", "source", "corroboration"),
                        List.of("Independent agreement is useful precisely because it can still be wrong.", "A second account adds evidence, not omniscience."),
                        "Repeated testimony does not automatically become truth or a numerical confidence score."),
                p("check_physical_anchor", Family.CORROBORATE, "Check the Physical Anchor",
                        "Use a surviving object, route mark, signal point, or damage trace to test one bounded part of an account.",
                        "Which claim can this object support or contradict without being made to speak for everything?",
                        List.of("Inspect the relevant feature.", "Compare it with the stated sequence.", "Limit the conclusion to that feature."),
                        Set.of("object", "route", "signal", "corroboration"),
                        List.of("Physical evidence can answer a small question while leaving the larger story open.", "The object constrains interpretation; it does not narrate motive."),
                        "A physical anchor does not identify a culprit, certify authenticity, or prove hidden intent."),
                p("repeat_signal_check", Family.CORROBORATE, "Repeat the Signal Check",
                        "Reproduce or re-observe a known local signal pattern under comparable conditions when doing so is already permitted by the scenario.",
                        "Does the same observable pattern recur, and what remains unchanged if it does?",
                        List.of("Record the repeated pattern.", "Compare conditions.", "Keep sender and meaning separate from recurrence."),
                        Set.of("signal", "sequence", "corroboration"),
                        List.of("Repeatability can strengthen an observation without revealing who caused it.", "The repeated cue is evidence of recurrence, not prophecy."),
                        "This exchange does not guarantee repeatability, identify a sender, forecast danger, or create world state."),
                p("cross_check_record", Family.CORROBORATE, "Cross-Check the Record",
                        "Test one entry in a record against another source that should be independent of the document itself.",
                        "Which field can be corroborated without assuming the whole record is authentic?",
                        List.of("Choose one checkable field.", "Compare it independently.", "Keep the rest of the record unverified."),
                        Set.of("record", "corroboration", "source"),
                        List.of("One verified detail should remain one verified detail.", "Partial corroboration is useful only if it stays partial."),
                        "A matching field does not authenticate the whole record or prove every statement in it."),

                p("name_the_gap", Family.PRESERVE_UNCERTAINTY, "Name the Gap",
                        "State exactly what the evidence does not establish before choosing a next step.",
                        "What remains unknown even after the strongest available comparison?",
                        List.of("Name the missing fact.", "Avoid substituting motive for evidence.", "Carry the gap forward explicitly."),
                        Set.of("uncertainty", "gap", "testimony"),
                        List.of("A named gap is harder to accidentally fill with assumption.", "The unresolved part remains visible instead of disappearing into confident prose."),
                        "Naming uncertainty does not itself increase certainty or reveal hidden truth."),
                p("retain_two_hypotheses", Family.PRESERVE_UNCERTAINTY, "Retain Two Hypotheses",
                        "Keep two evidence-compatible explanations alive when the current material cannot distinguish them.",
                        "What future observation would separate the alternatives?",
                        List.of("Write both explanations.", "List what each predicts locally.", "Avoid choosing until new evidence arrives."),
                        Set.of("uncertainty", "contradiction", "hypothesis"),
                        List.of("Competing explanations prevent a contradiction from becoming a premature verdict.", "Two plausible accounts can coexist without either becoming canon."),
                        "This exchange does not calculate probabilities, confidence percentages, or a hidden correct hypothesis."),
                p("separate_observation_inference", Family.PRESERVE_UNCERTAINTY, "Separate Observation from Inference",
                        "Split what was directly observed from the interpretation placed on top of it.",
                        "Which words describe the evidence itself, and which describe what someone thinks it means?",
                        List.of("Restate the observation plainly.", "Restate the inference separately.", "Keep both without merging them."),
                        Set.of("observation", "inference", "uncertainty"),
                        List.of("The same observation can support more than one inference.", "Separating layers makes later revision possible."),
                        "An inference remains an inference even when it sounds persuasive; this catalogue never promotes it to CANON."),
                p("preserve_contradiction", Family.PRESERVE_UNCERTAINTY, "Preserve the Contradiction",
                        "Record incompatible details without forcing an immediate winner when neither side has enough support.",
                        "What exactly conflicts, and what evidence would resolve that conflict?",
                        List.of("Write both claims precisely.", "Identify the smallest disputed fact.", "Defer judgment until that fact can be checked."),
                        Set.of("contradiction", "uncertainty", "verification"),
                        List.of("A contradiction is a durable question, not an automatic accusation.", "Keeping both claims visible protects against convenient certainty."),
                        "Contradiction alone does not prove lying, forgery, guilt, sabotage, or hostile allegiance."),

                p("preserve_chain", Family.PRESERVE_EVIDENCE, "Preserve the Chain",
                        "Keep track of who handled, moved, copied, or relayed an evidence object or account after it was first observed.",
                        "Can later readers distinguish the original state from later handling?",
                        List.of("Record each known handoff.", "Mark uncertain handling explicitly.", "Avoid rewriting the original description."),
                        Set.of("source", "record", "preservation"),
                        List.of("A handling history makes later changes easier to notice.", "Preservation protects context without proving authenticity."),
                        "A preserved chain does not certify authenticity, eliminate tampering, or assign guilt."),
                p("record_before_change", Family.PRESERVE_EVIDENCE, "Record Before Change",
                        "Capture the observable state before a scenario-authorized repair, move, opening, or cleanup alters it.",
                        "What detail will become impossible to inspect after the change?",
                        List.of("Record that detail first.", "Mark who observed it.", "Then proceed only if the scenario permits the change."),
                        Set.of("object", "preservation", "alteration"),
                        List.of("The important moment is just before useful action destroys useful evidence.", "Preservation and intervention can both matter if their order is explicit."),
                        "This exchange does not authorize world mutation; scenario/runtime Java authority must permit any change."),
                p("preserve_original_wording", Family.PRESERVE_EVIDENCE, "Preserve the Original Wording",
                        "Keep the first recorded wording of a message or account alongside later summaries and corrections.",
                        "What changed in wording, and was the change factual, interpretive, or merely clearer?",
                        List.of("Keep the first version.", "Append rather than overwrite.", "Mark who made the correction."),
                        Set.of("record", "message", "preservation"),
                        List.of("Revision becomes evidence when the earlier wording survives.", "A cleaner summary should not erase the rougher original."),
                        "Changed wording does not by itself prove deception, coercion, or falsification."),
                p("isolate_sample", Family.PRESERVE_EVIDENCE, "Isolate a Bounded Sample",
                        "Preserve a small scenario-authorized sample or representative trace while leaving the broader site unchanged.",
                        "What minimum sample is enough to compare later without consuming the whole source?",
                        List.of("Take only the permitted sample.", "Record where it came from.", "Leave remaining material undisturbed."),
                        Set.of("object", "sample", "preservation"),
                        List.of("A bounded sample supports later comparison without turning investigation into harvesting.", "The sample remains evidence, not a reward."),
                        "This exchange does not define loot, ownership, crafting value, quantity, rarity, or progression rewards."),

                p("defer_judgment", Family.DISENGAGE, "Defer Judgment",
                        "End the verification attempt without deciding the disputed claim when current evidence is insufficient.",
                        "What should remain explicitly unresolved when the conversation ends?",
                        List.of("State the unresolved claim.", "Record the next useful check.", "Leave without declaring a winner."),
                        Set.of("uncertainty", "disengage", "verification"),
                        List.of("Stopping can preserve more truth than forcing an answer.", "The unresolved claim remains available for later evidence."),
                        "Disengaging does not freeze NPC allegiance, close a quest, or settle scenario truth."),
                p("leave_with_record", Family.DISENGAGE, "Leave with a Record",
                        "End the exchange while preserving the evidence state and the questions already established.",
                        "What must be carried forward so the next exchange does not restart from rumor?",
                        List.of("Record confirmed observations.", "Record unresolved conflicts.", "Record the source boundaries."),
                        Set.of("record", "disengage", "preservation"),
                        List.of("The conversation ends; the evidence ledger does not vanish with it.", "A clean handoff preserves uncertainty as carefully as certainty."),
                        "A retained record does not create persistent canonical knowledge unless a Java-owned system explicitly stores it."),
                p("decline_unsafe_test", Family.DISENGAGE, "Decline the Unsafe Test",
                        "Refuse a proposed verification step when performing it would require unsupported danger, destruction, coercion, or authority.",
                        "Can the claim be checked another way without inventing permission or forcing risk?",
                        List.of("Refuse the unsafe step.", "Propose a bounded alternative.", "Leave the claim unresolved if no safe check exists."),
                        Set.of("disengage", "risk", "authority"),
                        List.of("A test is not automatically valid merely because it could produce information.", "Refusal preserves both uncertainty and the authority boundary."),
                        "This exchange does not calculate safety, grant consent, authorize coercion, or override scenario rules."),
                p("accept_unresolved_refusal", Family.DISENGAGE, "Accept an Unresolved Refusal",
                        "Allow a witness or actor to refuse further cooperation without converting refusal into guilt or deception.",
                        "What can still be verified elsewhere after the refusal?",
                        List.of("Record the refusal neutrally.", "Check independent evidence.", "Do not infer motive from silence alone."),
                        Set.of("testimony", "disengage", "uncertainty"),
                        List.of("Refusal closes one source, not the whole investigation.", "Silence preserves a gap; it does not fill it."),
                        "Refusal is not proof of guilt, lying, hostility, allegiance, or persuasion failure."));
    }

    public static Selection compose(long seed, String scenarioId, String actorContextId, String evidenceLinkId,
                                    Set<Family> allowedFamilies, Map<String, Integer> evidence) {
        String scenario = stableId(scenarioId);
        String actor = stableId(actorContextId);
        String link = stableId(evidenceLinkId);
        Set<Family> families = Set.copyOf(Objects.requireNonNull(allowedFamilies, "allowedFamilies"));
        if (families.isEmpty()) throw new IllegalArgumentException("allowedFamilies must not be empty");
        Map<String, Integer> normalizedEvidence = normalizeEvidence(evidence);

        List<Primitive> compatible = waveOne().stream().filter(p -> families.contains(p.family())).toList();
        if (compatible.isEmpty()) throw new IllegalArgumentException("no compatible verification exchange primitives");
        Set<String> positiveTags = normalizedEvidence.entrySet().stream().filter(e -> e.getValue() > 0).map(Map.Entry::getKey).collect(Collectors.toSet());
        int bestMatch = compatible.stream().mapToInt(p -> matchCount(p.affinityTags(), positiveTags)).max().orElse(0);
        List<Primitive> pool = bestMatch > 0 ? compatible.stream().filter(p -> matchCount(p.affinityTags(), positiveTags) == bestMatch).toList() : compatible;

        int primitiveIndex = index(seed, scenario + "|" + actor + "|" + link + "|primitive|" + stableEvidence(normalizedEvidence), pool.size());
        Primitive primitive = pool.get(primitiveIndex);
        int cueIndex = index(seed, scenario + "|" + actor + "|" + link + "|cue|" + primitive.id(), primitive.presentationCues().size());
        Set<String> matched = primitive.affinityTags().stream().filter(positiveTags::contains).collect(Collectors.toSet());
        return new Selection(GENERATOR_VERSION, seed, scenario, actor, link, primitive, primitive.presentationCues().get(cueIndex), matched);
    }

    private static Primitive p(String id, Family family, String title, String read, String prompt, List<String> responses,
                               Set<String> tags, List<String> cues, String boundary) {
        return new Primitive(id, family, title, read, prompt, responses, tags, cues, boundary);
    }

    private static int matchCount(Set<String> tags, Set<String> evidence) {
        int count = 0;
        for (String tag : tags) if (evidence.contains(tag)) count++;
        return count;
    }

    private static Map<String, Integer> normalizeEvidence(Map<String, Integer> evidence) {
        Objects.requireNonNull(evidence, "evidence");
        return evidence.entrySet().stream().collect(Collectors.toMap(
                e -> stableId(e.getKey()),
                e -> {
                    Integer value = Objects.requireNonNull(e.getValue(), "evidence value");
                    if (value < 0) throw new IllegalArgumentException("evidence values must be non-negative");
                    return value > 0 ? 1 : 0;
                }));
    }

    private static String stableEvidence(Map<String, Integer> evidence) {
        return evidence.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(","));
    }

    private static int index(long seed, String salt, int bound) {
        if (bound <= 0) throw new IllegalArgumentException("bound must be positive");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((seed + "|" + salt).getBytes(StandardCharsets.UTF_8));
            long value = 0;
            for (int i = 0; i < Long.BYTES; i++) value = (value << 8) | (hash[i] & 0xffL);
            return Math.floorMod(value, bound);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }

    private static String stableId(String value) {
        String normalized = text(value, "id").toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z0-9][a-z0-9_:-]*")) throw new IllegalArgumentException("invalid stable id: " + value);
        return normalized;
    }

    private static String text(String value, String field) {
        Objects.requireNonNull(value, field);
        String trimmed = value.trim();
        if (trimmed.isEmpty()) throw new IllegalArgumentException(field + " must not be blank");
        return trimmed;
    }

    private static List<String> exactTextList(List<String> values, int size, String field) {
        Objects.requireNonNull(values, field);
        if (values.size() != size) throw new IllegalArgumentException(field + " must contain exactly " + size + " entries");
        List<String> normalized = new ArrayList<>(size);
        for (String value : values) normalized.add(text(value, field));
        return List.copyOf(normalized);
    }

    private static Set<String> nonEmptyTags(Set<String> tags, String field) {
        Objects.requireNonNull(tags, field);
        if (tags.isEmpty()) throw new IllegalArgumentException(field + " must not be empty");
        return tags.stream().map(NightmareEvidenceVerificationExchangeCatalog::stableId).collect(Collectors.toUnmodifiableSet());
    }
}