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

/** DESIGN-only player-facing exchange beats for already-resolved Java-owned Nightmare faction negotiations. */
public final class NightmareFactionNegotiationExchangeCatalog {
    public static final String GENERATOR_VERSION = "nightmare-faction-negotiation-exchange-v1";

    private NightmareFactionNegotiationExchangeCatalog() {}

    public enum ExchangeFamily { ACCEPT, CLARIFY, COUNTER, VERIFY, DISENGAGE }

    public record Primitive(String id, ExchangeFamily family, String title, String exchangeRead,
                            String factionLine, List<String> playerOptions, Set<String> affinityTags,
                            List<String> presentationCues, String antiOverclaimBoundary) {
        public Primitive {
            id = stableId(id);
            family = Objects.requireNonNull(family, "family");
            title = text(title, "title");
            exchangeRead = text(exchangeRead, "exchangeRead");
            factionLine = text(factionLine, "factionLine");
            playerOptions = exactTextList(playerOptions, 3, "playerOptions");
            affinityTags = nonEmptyTags(affinityTags);
            presentationCues = exactTextList(presentationCues, 2, "presentationCues");
            antiOverclaimBoundary = text(antiOverclaimBoundary, "antiOverclaimBoundary");
        }
    }

    public record Selection(String generatorVersion, long seed, String scenarioId, String factionId,
                            String responseId, String interactionStateId, Set<ExchangeFamily> allowedFamilies,
                            Primitive primitive, String presentationCue, Set<String> matchedEvidenceTags) {
        public Selection {
            generatorVersion = text(generatorVersion, "generatorVersion");
            scenarioId = opaqueId(scenarioId, "scenarioId");
            factionId = opaqueId(factionId, "factionId");
            responseId = opaqueId(responseId, "responseId");
            interactionStateId = opaqueId(interactionStateId, "interactionStateId");
            allowedFamilies = nonEmptyFamilies(allowedFamilies);
            primitive = Objects.requireNonNull(primitive, "primitive");
            if (!allowedFamilies.contains(primitive.family())) {
                throw new IllegalArgumentException("primitive family must be allowed by caller-owned interaction state");
            }
            presentationCue = text(presentationCue, "presentationCue");
            matchedEvidenceTags = Set.copyOf(Objects.requireNonNull(matchedEvidenceTags, "matchedEvidenceTags"));
        }
    }

    public static List<Primitive> waveOne() {
        return List.of(
                p("accept_bounded_terms", ExchangeFamily.ACCEPT, "Accept the Bounded Terms",
                        "The player accepts the stated local terms without expanding them into trust, alliance, ownership, or a completed bargain.",
                        "Then we proceed on those terms, and only those terms.",
                        "Restate the exact accepted scope.", "Ask what remains explicitly unsettled.", "Proceed only after Java records any authoritative consequence.",
                        Set.of("accept", "terms", "cooperation"),
                        "Accepted terms are highlighted without a permanent alliance badge.", "Unsettled subjects remain listed beside the local agreement.",
                        "Acceptance presentation cannot transfer resources, alter allegiance or reputation, prove trust, or accept a scenario event."),
                p("accept_one_step", ExchangeFamily.ACCEPT, "Accept One Step",
                        "The player agrees to one reversible or bounded first step while leaving later stages unresolved.",
                        "One step. After that, we look again.",
                        "Confirm the first step only.", "Ask what evidence will be reviewed afterward.", "Refuse any automatic escalation beyond the supplied state.",
                        Set.of("accept", "staged", "verification"),
                        "Only the immediate step is emphasized as current.", "Later stages remain text rather than unlocked progression.",
                        "A first-step acceptance cannot guarantee later cooperation, unlock progression, or create a hidden success threshold."),
                p("accept_escort_condition", ExchangeFamily.ACCEPT, "Accept the Escort Condition",
                        "The player accepts accompaniment or observation as a local condition without conceding broader territorial or command authority.",
                        "Stay within the agreed bounds and we continue.",
                        "Confirm where the escort applies.", "Ask what the escort may restrict or observe.", "Keep route ownership and NPC movement Java-owned.",
                        Set.of("accept", "escort", "access"),
                        "Escort scope is shown as a bounded condition.", "No map ownership or command-state change is implied visually.",
                        "Accepting an escort cannot move NPCs, grant legal passage, establish surveillance mechanics, or guarantee safe arrival."),
                p("accept_information_scope", ExchangeFamily.ACCEPT, "Accept the Information Scope",
                        "The player accepts a deliberately limited disclosure while preserving the distinction between what was said and what is true.",
                        "That is what we are willing to disclose now.",
                        "Record the disclosed scope.", "Ask what is explicitly withheld.", "Seek corroboration without treating disclosure as authenticity.",
                        Set.of("accept", "information", "secrecy"),
                        "Disclosed and withheld material are displayed separately.", "No truth seal is attached to the faction statement.",
                        "Accepting disclosure cannot certify truth, authenticity, innocence, guilt, or appraisal relevance."),

                p("clarify_exact_terms", ExchangeFamily.CLARIFY, "Clarify the Exact Terms",
                        "The player asks the faction to turn a broad agreement or refusal into a bounded statement of scope, duration, actors, or purpose.",
                        "Ask precisely, and we will answer precisely.",
                        "Clarify the named action.", "Clarify who is included.", "Clarify when the condition ends without inventing a timer.",
                        Set.of("clarify", "terms", "time"),
                        "Ambiguous language is replaced by a bounded summary panel.", "No countdown or probability is inferred from stated duration.",
                        "Clarification cannot manufacture missing authority, create a canonical timer, or convert vague language into guaranteed outcome."),
                p("clarify_boundary", ExchangeFamily.CLARIFY, "Clarify the Boundary",
                        "The player asks what exact route, place, resource, subject, or behavior the faction currently treats as outside the agreement.",
                        "This is the line. Beyond it, nothing has been agreed.",
                        "Identify the exact excluded area or action.", "Ask whether the boundary is contextual.", "Preserve uncertainty about legitimacy and enforcement.",
                        Set.of("clarify", "boundary", "territory"),
                        "The claimed boundary is displayed separately from observed terrain.", "No sovereignty or ownership icon is added by presentation.",
                        "Clarifying a boundary cannot prove territorial legitimacy, ownership, magical enforcement, or permanent exclusion."),
                p("clarify_expected_consequence", ExchangeFamily.CLARIFY, "Clarify the Expected Consequence",
                        "The player asks the faction to separate an observed fact from the consequence it predicts or threatens.",
                        "This is what we observed. This is what we think follows.",
                        "Ask which part is directly observed.", "Ask which part is prediction or threat.", "Seek another account before treating the consequence as certain.",
                        Set.of("clarify", "warning", "information"),
                        "Observation and expectation are shown in different presentation fields.", "No prophecy or certainty meter accompanies the warning.",
                        "Clarification cannot make a warning true, assign probability, trigger hostility, or force the predicted consequence."),
                p("clarify_authority", ExchangeFamily.CLARIFY, "Clarify Who Speaks",
                        "The player asks whether the current speaker is describing personal intent, faction policy, delegated authority, or an unresolved claim.",
                        "You want to know whose word this is. That is fair.",
                        "Ask what authority the speaker actually holds.", "Ask whether another actor can override the answer.", "Record the claim without validating hierarchy.",
                        Set.of("clarify", "authority", "duty"),
                        "Speaker identity and claimed authority are presented as separate facts.", "No command-chain truth is inferred from titles alone.",
                        "Presentation cannot prove rank legitimacy, bind the whole faction, change NPC authority, or establish who is telling the truth."),

                p("counter_narrow_scope", ExchangeFamily.COUNTER, "Counter with a Narrower Scope",
                        "The player responds to a faction position by proposing a smaller action that preserves the central disagreement.",
                        "That is narrower. We can consider it without settling the rest.",
                        "Offer one bounded action.", "State what the counteroffer does not include.", "Leave acceptance entirely to Java-owned interaction state.",
                        Set.of("counter", "terms", "cooperation"),
                        "Original and narrowed scopes remain visible side by side.", "The counteroffer is not styled as accepted until authority says so.",
                        "A counteroffer cannot calculate leverage, force acceptance, create reputation gain, or accept a ResolutionGraph event."),
                p("counter_substitute_proof", ExchangeFamily.COUNTER, "Counter with Substitute Proof",
                        "The player proposes another bounded verification method instead of accepting the faction's original requested proof.",
                        "If your alternative establishes what we need, we can discuss it.",
                        "Name the substitute proof.", "Ask what claim it would actually verify.", "Reject any implication that evidence automatically equals truth.",
                        Set.of("counter", "verification", "access"),
                        "Original and substitute verification paths are both retained.", "Neither path receives an automatic completion marker.",
                        "Substitute proof cannot adjudicate truth, identity, persuasion, hidden prerequisites, or objective completion by itself."),
                p("counter_different_exchange", ExchangeFamily.COUNTER, "Counter with a Different Exchange",
                        "The player proposes a different bounded contribution instead of the resource, service, or information originally requested.",
                        "Different terms may still be terms. Name them clearly.",
                        "State the alternative contribution.", "Ask what the faction would provide in return.", "Preserve resource quantities until Java authorizes transfer.",
                        Set.of("counter", "resource", "bargain"),
                        "Proposed contributions are displayed without a value-equivalence meter.", "Inventory state does not change with the conversation card.",
                        "A different exchange cannot spend inventory, create debt, decide fair value, mutate reputation, or guarantee agreement."),
                p("counter_pause_and_return", ExchangeFamily.COUNTER, "Counter with Time to Reconsider",
                        "The player proposes suspending the exchange and returning after a named fact, actor, or condition can be checked.",
                        "Return when that question has an answer. Until then, nothing changes.",
                        "Name the unresolved prerequisite.", "Choose a later verification point.", "Do not freeze world state while the conversation is paused.",
                        Set.of("counter", "time", "verification"),
                        "The unresolved question is retained as the reason for pausing.", "No automatic reminder implies the world waits unchanged.",
                        "Pausing cannot freeze NPCs or world state, guarantee the same offer later, or create a canonical negotiation timer."),

                p("verify_repeat_terms", ExchangeFamily.VERIFY, "Repeat the Terms Back",
                        "The player restates the current local terms so disagreement about wording can be exposed before any authoritative action occurs.",
                        "Yes. That is what we said — or correct the part you misunderstood.",
                        "Repeat scope and exclusions.", "Ask the faction to correct one disputed phrase.", "Proceed only after Java owns the resulting state.",
                        Set.of("verify", "terms", "information"),
                        "Player and faction summaries are presented together for comparison.", "A wording match is not shown as truth verification.",
                        "Matching terms cannot prove intent, truthfulness, future compliance, persuasion success, or scenario completion."),
                p("verify_named_actor", ExchangeFamily.VERIFY, "Verify the Named Actor",
                        "The player checks whether a promised escort, witness, guarantor, negotiator, or authority is actually the actor the faction named.",
                        "Verify who you are dealing with before relying on their role.",
                        "Confirm the supplied actor identity.", "Ask what authority that actor has.", "Keep allegiance and reputation unchanged until Java says otherwise.",
                        Set.of("verify", "authority", "escort"),
                        "Named actor and claimed role are shown as separate fields.", "No faction-membership inference is drawn from visual proximity alone.",
                        "Verification presentation cannot create membership, allegiance, command authority, trust, or NPC state."),
                p("verify_resource_commitment", ExchangeFamily.VERIFY, "Verify the Resource Commitment",
                        "The player checks exactly what resource, service, access, or information is being proposed before any transfer occurs.",
                        "Know what is being promised before anyone hands anything over.",
                        "Confirm the named contribution.", "Confirm the named return contribution.", "Leave all inventory and ownership mutation to Java.",
                        Set.of("verify", "resource", "bargain"),
                        "Promised items or services are shown as proposed commitments, not inventory deltas.", "No quantity is invented when authority did not supply one.",
                        "Verification cannot transfer resources, reserve inventory, guarantee availability, create debt, or establish fair exchange value."),
                p("verify_access_condition", ExchangeFamily.VERIFY, "Verify the Access Condition",
                        "The player checks whether the stated condition applies to this actor, route, purpose, and moment before treating passage as available.",
                        "Check the condition first. Permission is not broader than what was actually granted.",
                        "Confirm the applicable route or place.", "Confirm the named purpose or actor.", "Wait for Java-owned access state before moving through restricted space.",
                        Set.of("verify", "access", "territory"),
                        "Condition scope is displayed beside current authority state.", "No terrain unlock animation is triggered by the card itself.",
                        "Verification cannot unlock terrain, guarantee safe passage, establish territorial legitimacy, or consume an access token."),

                p("disengage_accept_no_agreement", ExchangeFamily.DISENGAGE, "Leave Without Agreement",
                        "The player ends the exchange while preserving the fact that no local agreement was reached and no wider hostility is inferred.",
                        "Then we are done for now.",
                        "End the conversation.", "Record the unresolved point.", "Leave without inventing reputation or hostility changes.",
                        Set.of("disengage", "refusal", "boundary"),
                        "The exchange closes with the unresolved issue retained.", "No hostile-color transition occurs without Java-owned state.",
                        "Disengagement cannot create hostility, reputation loss, pursuit, permanent refusal, or terminal Nightmare failure."),
                p("disengage_after_warning", ExchangeFamily.DISENGAGE, "Withdraw After the Warning",
                        "The player takes a non-escalatory exit after a warning without treating the warning as proven or the withdrawal as surrender.",
                        "Leave now, and this discussion ends here.",
                        "Take the offered exit.", "Record what consequence was claimed.", "Seek another route or account later if Java permits it.",
                        Set.of("disengage", "warning", "withdrawal"),
                        "The warning remains recorded as a claim after the exchange closes.", "No fear, surrender, or morale state is inferred.",
                        "Withdrawal cannot prove the warning true, guarantee safety, prevent pursuit, alter morale, or decide later faction behavior."),
                p("disengage_preserve_option", ExchangeFamily.DISENGAGE, "Preserve the Option to Return",
                        "The exchange ends without claiming that the faction will necessarily repeat the same terms later.",
                        "You may return to ask again. Do not assume the answer will be unchanged.",
                        "Leave the current terms unresolved.", "Record what would need rechecking later.", "Treat any future offer as fresh Java-owned state.",
                        Set.of("disengage", "time", "counter"),
                        "A future-return note is shown as possibility rather than promise.", "No quest marker guarantees the same negotiation state later.",
                        "This beat cannot freeze an offer, guarantee future access, preserve prices, or create a canonical cooldown or retry rule."),
                p("disengage_protect_uncertainty", ExchangeFamily.DISENGAGE, "Leave the Claim Unsettled",
                        "The player ends the exchange while explicitly retaining uncertainty around truth, authority, motive, or consequence.",
                        "Then leave it unsettled. We do not have to pretend certainty.",
                        "Record the unresolved claim.", "Record who made it and under what context.", "Avoid converting uncertainty into guilt or innocence.",
                        Set.of("disengage", "information", "verification"),
                        "Unresolved claims remain visibly marked as unresolved.", "No confidence percentage or truth label is generated.",
                        "Preserving uncertainty cannot adjudicate truth, guilt, innocence, allegiance, appraisal, or accepted scenario state."));
    }

    public static Selection compose(long seed, String scenarioId, String factionId, String responseId,
                                    String interactionStateId, Set<ExchangeFamily> allowedFamilies,
                                    Map<String, Integer> evidence) {
        String checkedScenario = opaqueId(scenarioId, "scenarioId");
        String checkedFaction = opaqueId(factionId, "factionId");
        String checkedResponse = opaqueId(responseId, "responseId");
        String checkedState = opaqueId(interactionStateId, "interactionStateId");
        Set<ExchangeFamily> checkedFamilies = nonEmptyFamilies(allowedFamilies);
        Set<String> positiveEvidence = positiveEvidence(evidence);

        List<Primitive> candidates = waveOne().stream()
                .filter(primitive -> checkedFamilies.contains(primitive.family()))
                .sorted(Comparator.comparing(Primitive::id))
                .toList();
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("no negotiation exchanges available for allowed families");
        }

        int bestMatch = candidates.stream()
                .mapToInt(primitive -> overlap(primitive.affinityTags(), positiveEvidence))
                .max()
                .orElse(0);
        List<Primitive> preferred = bestMatch > 0
                ? candidates.stream().filter(primitive -> overlap(primitive.affinityTags(), positiveEvidence) == bestMatch).toList()
                : candidates;

        String authorityKey = checkedScenario + "|" + checkedFaction + "|" + checkedResponse + "|" + checkedState
                + "|" + checkedFamilies.stream().map(Enum::name).sorted().collect(Collectors.joining(","))
                + "|" + positiveEvidence.stream().sorted().collect(Collectors.joining(","));
        Primitive primitive = preferred.get(index(seed, authorityKey + "|primitive", preferred.size()));
        String cue = primitive.presentationCues().get(index(seed, authorityKey + "|" + primitive.id() + "|cue",
                primitive.presentationCues().size()));
        Set<String> matched = primitive.affinityTags().stream()
                .filter(positiveEvidence::contains)
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return new Selection(GENERATOR_VERSION, seed, checkedScenario, checkedFaction, checkedResponse, checkedState,
                checkedFamilies, primitive, cue, matched);
    }

    public static Primitive requirePrimitive(String id) {
        String checked = stableId(id);
        return waveOne().stream().filter(primitive -> primitive.id().equals(checked)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown negotiation exchange primitive: " + checked));
    }

    private static Primitive p(String id, ExchangeFamily family, String title, String exchangeRead, String factionLine,
                               String optionOne, String optionTwo, String optionThree, Set<String> tags,
                               String cueOne, String cueTwo, String boundary) {
        return new Primitive(id, family, title, exchangeRead, factionLine, List.of(optionOne, optionTwo, optionThree),
                tags, List.of(cueOne, cueTwo), boundary);
    }

    private static int overlap(Set<String> left, Set<String> right) {
        int matches = 0;
        for (String value : left) if (right.contains(value)) matches++;
        return matches;
    }

    private static int index(long seed, String key, int bound) {
        if (bound <= 0) throw new IllegalArgumentException("bound must be positive");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(GENERATOR_VERSION.getBytes(StandardCharsets.UTF_8));
            digest.update((byte) 0);
            digest.update(ByteBuffer.allocate(Long.BYTES).putLong(seed).array());
            digest.update((byte) 0);
            digest.update(key.getBytes(StandardCharsets.UTF_8));
            long value = ByteBuffer.wrap(digest.digest(), 0, Long.BYTES).getLong();
            return Math.floorMod(value, bound);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

    private static Set<String> positiveEvidence(Map<String, Integer> evidence) {
        Objects.requireNonNull(evidence, "evidence");
        Set<String> tags = new LinkedHashSet<>();
        evidence.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            String tag = stableId(entry.getKey());
            Integer magnitude = Objects.requireNonNull(entry.getValue(), "evidence magnitude");
            if (magnitude < 0) throw new IllegalArgumentException("negative evidence is not supported");
            if (magnitude > 0) tags.add(tag);
        });
        return Set.copyOf(tags);
    }

    private static Set<ExchangeFamily> nonEmptyFamilies(Set<ExchangeFamily> families) {
        Objects.requireNonNull(families, "allowedFamilies");
        if (families.isEmpty()) throw new IllegalArgumentException("allowedFamilies must not be empty");
        if (families.contains(null)) throw new IllegalArgumentException("allowedFamilies must not contain null");
        return Set.copyOf(families);
    }

    private static Set<String> nonEmptyTags(Set<String> tags) {
        Objects.requireNonNull(tags, "tags");
        if (tags.isEmpty()) throw new IllegalArgumentException("affinityTags must not be empty");
        Set<String> normalized = tags.stream().map(NightmareFactionNegotiationExchangeCatalog::stableId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return Set.copyOf(normalized);
    }

    private static List<String> exactTextList(List<String> values, int expected, String field) {
        Objects.requireNonNull(values, field);
        if (values.size() != expected) throw new IllegalArgumentException(field + " must contain exactly " + expected + " entries");
        List<String> checked = new ArrayList<>(expected);
        for (String value : values) checked.add(text(value, field));
        return List.copyOf(checked);
    }

    private static String opaqueId(String value, String field) {
        Objects.requireNonNull(value, field);
        if (value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
        return value;
    }

    private static String text(String value, String field) {
        Objects.requireNonNull(value, field);
        String checked = value.trim();
        if (checked.isEmpty()) throw new IllegalArgumentException(field + " must not be blank");
        return checked;
    }

    private static String stableId(String value) {
        String checked = text(value, "id").toLowerCase(Locale.ROOT);
        if (!checked.matches("[a-z0-9_]+")) {
            throw new IllegalArgumentException("catalogue IDs/tags must match [a-z0-9_]+: " + value);
        }
        return checked;
    }
}
