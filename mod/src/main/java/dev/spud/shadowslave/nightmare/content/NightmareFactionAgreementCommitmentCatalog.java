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

/** DESIGN-only presentation for already-authorized Java-owned Nightmare faction commitments. */
public final class NightmareFactionAgreementCommitmentCatalog {
    public static final String GENERATOR_VERSION = "nightmare-faction-agreement-commitment-v1";

    private NightmareFactionAgreementCommitmentCatalog() {}

    public enum CommitmentState { PROPOSED, ACCEPTED, FULFILLED, BROKEN, EXPIRED }

    public record Primitive(String id, CommitmentState state, String title, String statusRead,
                            String playerPrompt, List<String> playerResponses, Set<String> affinityTags,
                            List<String> presentationCues, String antiOverclaimBoundary) {
        public Primitive {
            id = stableId(id);
            state = Objects.requireNonNull(state, "state");
            title = text(title, "title");
            statusRead = text(statusRead, "statusRead");
            playerPrompt = text(playerPrompt, "playerPrompt");
            playerResponses = exactTextList(playerResponses, 3, "playerResponses");
            affinityTags = nonEmptyTags(affinityTags);
            presentationCues = exactTextList(presentationCues, 2, "presentationCues");
            antiOverclaimBoundary = text(antiOverclaimBoundary, "antiOverclaimBoundary");
        }
    }

    public record Selection(String generatorVersion, long seed, String scenarioId, String factionId,
                            String agreementId, String commitmentId, CommitmentState state,
                            Primitive primitive, String presentationCue, Set<String> matchedEvidenceTags) {
        public Selection {
            generatorVersion = text(generatorVersion, "generatorVersion");
            scenarioId = opaqueId(scenarioId, "scenarioId");
            factionId = opaqueId(factionId, "factionId");
            agreementId = opaqueId(agreementId, "agreementId");
            commitmentId = opaqueId(commitmentId, "commitmentId");
            state = Objects.requireNonNull(state, "state");
            primitive = Objects.requireNonNull(primitive, "primitive");
            if (primitive.state() != state) {
                throw new IllegalArgumentException("primitive state must match caller-owned commitment state");
            }
            presentationCue = text(presentationCue, "presentationCue");
            matchedEvidenceTags = Set.copyOf(Objects.requireNonNull(matchedEvidenceTags, "matchedEvidenceTags"));
        }
    }

    public static List<Primitive> waveOne() {
        return List.of(
                p("proposed_bounded_exchange", CommitmentState.PROPOSED, "Bounded Exchange Proposed",
                        "A faction has put forward a local exchange whose scope is visible, but Java has not authorized it as accepted.",
                        "Review what is actually being proposed before treating any consequence as real.",
                        "Restate the proposed contribution and return.", "Ask what remains outside the proposal.", "Leave the proposal pending without changing state.",
                        Set.of("proposal", "terms", "resource"),
                        "The proposed terms are shown with a pending marker rather than a success seal.", "No inventory, access, or allegiance change is animated.",
                        "A proposed commitment cannot transfer resources, create access, alter allegiance or reputation, bind either side, or accept a scenario event."),
                p("proposed_conditional_passage", CommitmentState.PROPOSED, "Conditional Passage Proposed",
                        "A faction has offered passage under named conditions, but the route and permission remain Java-owned state.",
                        "Check the exact actor, purpose, route, and condition before relying on the offer.",
                        "Clarify the route covered by the proposal.", "Clarify who the proposal applies to.", "Keep movement and permission unchanged until Java accepts it.",
                        Set.of("proposal", "access", "territory"),
                        "The claimed passage scope is displayed separately from the actual map state.", "No gate, barrier, or route visibly unlocks from presentation alone.",
                        "A proposed passage cannot unlock terrain, prove territorial legitimacy, guarantee safety, or establish a right of access."),
                p("proposed_information_terms", CommitmentState.PROPOSED, "Information Terms Proposed",
                        "A faction offers bounded information in exchange for a named action or reciprocal disclosure without certifying either side's claims.",
                        "Separate what is offered, what is requested, and what remains unknown.",
                        "Record the exact information scope.", "Ask what would remain withheld.", "Seek verification without converting the proposal into truth.",
                        Set.of("proposal", "information", "secrecy"),
                        "Offered information and requested consideration are listed in separate fields.", "No truth or authenticity badge accompanies the offer.",
                        "Information terms cannot certify truth, detect deception, create debt, calculate leverage, or settle faction intent."),
                p("proposed_shared_task", CommitmentState.PROPOSED, "Shared Task Proposed",
                        "A faction proposes a bounded joint action while broader trust, command, and future cooperation remain unsettled.",
                        "Decide whether the task itself is acceptable without treating it as an alliance.",
                        "Restate the shared task only.", "Ask who owns each part of the work.", "Keep future cooperation explicitly unresolved.",
                        Set.of("proposal", "cooperation", "duty"),
                        "The shared task is highlighted without an alliance or trust meter.", "Future stages remain absent until separately authorized.",
                        "A shared-task proposal cannot create allegiance, trust, command authority, guaranteed cooperation, or terminal Nightmare progress."),

                p("accepted_local_terms", CommitmentState.ACCEPTED, "Local Terms Accepted",
                        "Java has authorized this bounded agreement as accepted; presentation records the scope without inventing fulfillment or wider loyalty.",
                        "Confirm what acceptance now means and what it still does not mean.",
                        "Review the accepted scope.", "Review the exclusions and unresolved points.", "Proceed only through Java-owned actions.",
                        Set.of("accepted", "terms", "cooperation"),
                        "Accepted scope is emphasized while unresolved subjects remain visible.", "No completion, loyalty, or reward effect is shown.",
                        "Accepted terms cannot imply fulfillment, transfer resources, alter allegiance or reputation, prove trust, or resolve the Nightmare."),
                p("accepted_conditional_access", CommitmentState.ACCEPTED, "Conditional Access Accepted",
                        "Java has authorized a bounded access agreement, while actual movement, enforcement, and safety remain separate authoritative concerns.",
                        "Treat the accepted permission as no broader than its supplied scope.",
                        "Review the named route or place.", "Review the actor or purpose restriction.", "Wait for Java-owned world state before crossing.",
                        Set.of("accepted", "access", "territory"),
                        "Accepted access is presented as scoped permission rather than a global map unlock.", "Physical barriers remain controlled by runtime authority.",
                        "Accepted access cannot itself move actors, unlock terrain, guarantee safety, establish ownership, or spend an access token."),
                p("accepted_resource_commitment", CommitmentState.ACCEPTED, "Resource Commitment Accepted",
                        "Java has authorized the commitment to provide or exchange a named resource or service, but no transfer is inferred from acceptance alone.",
                        "Keep the promise distinct from the later act of fulfillment.",
                        "Review what was committed.", "Review what counterpart obligation exists.", "Do not treat inventory as changed until Java records transfer.",
                        Set.of("accepted", "resource", "bargain"),
                        "Committed resources are shown as obligations rather than inventory deltas.", "No quantity or value is invented beyond caller-owned data.",
                        "Acceptance cannot transfer or reserve resources, calculate fair value, create hidden debt, change reputation, or prove future compliance."),
                p("accepted_truce_scope", CommitmentState.ACCEPTED, "Bounded Truce Accepted",
                        "Java has authorized a local non-escalation agreement without establishing friendship, allegiance, permanent peace, or truthfulness.",
                        "Preserve the distinction between temporary restraint and deeper relationship state.",
                        "Review what actions are restrained.", "Review when or where the truce applies.", "Leave allegiance and hostility to Java-owned state.",
                        Set.of("accepted", "truce", "warning"),
                        "The truce scope is shown without a friendship or allegiance icon.", "No AI or combat state is changed by the card itself.",
                        "A truce cannot create permanent peace, allegiance, trust, AI ceasefire behavior, or a guaranteed future negotiation outcome."),

                p("fulfilled_resource_delivered", CommitmentState.FULFILLED, "Committed Resource Delivered",
                        "Java has authorized this bounded resource or service obligation as fulfilled; presentation reports completion without inventing value or reward.",
                        "Record what was completed and avoid extending it into broader social meaning.",
                        "Acknowledge the completed obligation.", "Record any remaining independent obligations.", "Do not infer reputation or appraisal changes.",
                        Set.of("fulfilled", "resource", "bargain"),
                        "The specific obligation receives a completed marker while unrelated terms remain separate.", "No reward, reputation, or appraisal animation is attached.",
                        "Fulfillment cannot calculate value, grant rewards, alter reputation or allegiance, prove goodwill, or accept a ResolutionGraph event."),
                p("fulfilled_passage_honored", CommitmentState.FULFILLED, "Passage Commitment Honored",
                        "Java has authorized the bounded access commitment as fulfilled for the supplied context without creating permanent territorial rights.",
                        "Record that this access obligation was honored, not that the route is universally open.",
                        "Acknowledge the honored passage.", "Record the exact context in which it applied.", "Treat future access as fresh Java-owned state.",
                        Set.of("fulfilled", "access", "territory"),
                        "The honored commitment is recorded as historical context rather than a permanent unlock.", "No ownership or future-access badge persists automatically.",
                        "Honored passage cannot establish ownership, permanent access, future safety, faction allegiance, or continuing permission."),
                p("fulfilled_information_provided", CommitmentState.FULFILLED, "Promised Information Provided",
                        "Java has authorized the disclosure obligation as fulfilled while the information's truth, completeness, and interpretation remain separate questions.",
                        "Record that the disclosure occurred without certifying its contents.",
                        "Record exactly what was disclosed.", "Note what remains unverified or withheld.", "Seek corroboration without reopening fulfillment state.",
                        Set.of("fulfilled", "information", "verification"),
                        "Disclosure completion and truth status are shown as separate concepts.", "No certainty percentage is generated from fulfillment.",
                        "Providing promised information cannot make it true, complete, authentic, innocent, guilty, or canonically decisive."),
                p("fulfilled_shared_task_completed", CommitmentState.FULFILLED, "Shared Task Completed",
                        "Java has authorized the agreed local task as fulfilled, while broader scenario success and relationship consequences remain independent.",
                        "Close the bounded task without turning it into total cooperation or Nightmare completion.",
                        "Acknowledge the completed task.", "Record any unresolved consequences.", "Wait for Java to authorize any later event or outcome.",
                        Set.of("fulfilled", "cooperation", "duty"),
                        "The local task closes while the wider scenario remains visibly separate.", "No terminal-resolution or progression effect is attached.",
                        "Completing a shared task cannot resolve the Nightmare, establish trust or allegiance, award progression, or determine appraisal."),

                p("broken_resource_not_delivered", CommitmentState.BROKEN, "Resource Commitment Broken",
                        "Java has authorized that the supplied resource or service commitment was not honored; presentation does not assign motive or blame.",
                        "Record the broken obligation without deciding why it happened or what punishment follows.",
                        "Review the exact unmet obligation.", "Ask whether cause is known or still unresolved.", "Leave retaliation and reputation to Java-owned state.",
                        Set.of("broken", "resource", "bargain"),
                        "The unmet obligation is marked separately from any explanation or accusation.", "No automatic hostility or reputation penalty appears.",
                        "A broken commitment cannot prove betrayal, bad faith, guilt, deception, hostility, or determine reputation, punishment, or appraisal."),
                p("broken_access_denied", CommitmentState.BROKEN, "Accepted Passage Not Honored",
                        "Java has authorized that a previously accepted passage commitment was not honored in the supplied context without declaring territorial legitimacy.",
                        "Record the mismatch between agreement and outcome without inventing enforcement rights.",
                        "Review what access was accepted.", "Record how the supplied outcome differed.", "Leave enforcement or escalation to Java-owned state.",
                        Set.of("broken", "access", "territory"),
                        "Accepted scope and observed denial are shown side by side.", "No legal ownership or retaliation marker is inferred.",
                        "Denied passage cannot prove legitimacy, betrayal, hostile intent, permanent exclusion, or authorize retaliation or world mutation."),
                p("broken_information_withheld", CommitmentState.BROKEN, "Promised Disclosure Withheld",
                        "Java has authorized that a promised disclosure was not completed, while motive, truth, and concealment remain unresolved unless separately established.",
                        "Record the missing disclosure without turning silence into confession.",
                        "Record what was promised.", "Record what was not supplied.", "Preserve uncertainty about motive and truth.",
                        Set.of("broken", "information", "secrecy"),
                        "The missing disclosure is shown as an unmet obligation rather than a lie indicator.", "No guilt or deception label appears automatically.",
                        "Withholding promised information cannot prove lying, guilt, conspiracy, faction intent, or justify certainty, reputation, or appraisal changes."),
                p("broken_truce_violated", CommitmentState.BROKEN, "Bounded Truce Broken",
                        "Java has authorized that the supplied local non-escalation commitment was violated, without allowing presentation to decide actor intent or wider war state.",
                        "Record the violated term and keep cause, intent, and response separate.",
                        "Review which restraint was broken.", "Record the supplied actor or event context.", "Leave hostility escalation and combat state to Java.",
                        Set.of("broken", "truce", "warning"),
                        "The violated term is highlighted without automatically changing faction relationship UI.", "No combat or pursuit behavior is triggered by presentation.",
                        "A broken truce cannot infer intent, start war, alter AI, assign collective blame, change allegiance, or resolve the scenario."),

                p("expired_time_window", CommitmentState.EXPIRED, "Commitment Window Expired",
                        "Java has authorized that a bounded commitment no longer applies because its caller-owned validity condition ended.",
                        "Treat expiration as the end of this commitment, not as betrayal or punishment.",
                        "Review what condition ended the commitment.", "Record which terms no longer apply.", "Seek fresh state rather than reviving the old agreement.",
                        Set.of("expired", "time", "terms"),
                        "The old commitment is archived rather than shown as active or broken.", "No hostility or blame effect accompanies expiration.",
                        "Expiration cannot prove bad faith, create a canonical timer, impose reputation loss, or decide future willingness to negotiate."),
                p("expired_context_changed", CommitmentState.EXPIRED, "Context No Longer Applies",
                        "Java has authorized that a commitment ended because the actor, route, purpose, or scenario context it covered is no longer the current one.",
                        "Do not reuse old permission or obligations outside the context that authorized them.",
                        "Review the original context.", "Record what changed.", "Require fresh Java-owned authority before reuse.",
                        Set.of("expired", "access", "context"),
                        "Original and current contexts are shown distinctly.", "No stale permission is silently carried forward.",
                        "Context expiration cannot infer ownership, permanent denial, safe passage, hostility, or automatically create replacement terms."),
                p("expired_condition_unmet", CommitmentState.EXPIRED, "Condition Passed Unmet",
                        "Java has authorized that the commitment ceased to apply because its supplied condition was not met within the relevant bounded context.",
                        "Record the expired condition without turning it into moral failure or scenario defeat.",
                        "Review the unmet condition.", "Record whether later reconsideration is unknown.", "Leave consequences to Java-owned state.",
                        Set.of("expired", "condition", "verification"),
                        "The unmet condition is archived without a failure-score meter.", "No future retry, cooldown, or penalty is invented.",
                        "An unmet expired condition cannot create guilt, reputation loss, a retry timer, permanent refusal, or terminal Nightmare failure."),
                p("expired_offer_withdrawn", CommitmentState.EXPIRED, "Offer No Longer Current",
                        "Java has authorized that an earlier accepted or proposed commitment is no longer current, without presentation deciding whether it was revoked, superseded, or simply ended.",
                        "Treat the old commitment as historical state and ask for a current one if needed.",
                        "Archive the old terms.", "Ask for the current position if Java permits interaction.", "Do not assume the old terms can be restored.",
                        Set.of("expired", "terms", "counter"),
                        "The prior commitment remains readable as history but not active authority.", "No restore button implies the prior state is recoverable.",
                        "An expired offer cannot guarantee renewed terms, preserve prices or access, imply hostility, or create a canonical negotiation cooldown."));
    }

    public static Selection compose(long seed, String scenarioId, String factionId, String agreementId,
                                    String commitmentId, CommitmentState state, Map<String, Integer> evidence) {
        String checkedScenario = opaqueId(scenarioId, "scenarioId");
        String checkedFaction = opaqueId(factionId, "factionId");
        String checkedAgreement = opaqueId(agreementId, "agreementId");
        String checkedCommitment = opaqueId(commitmentId, "commitmentId");
        CommitmentState checkedState = Objects.requireNonNull(state, "state");
        Set<String> positiveEvidence = positiveEvidence(evidence);

        List<Primitive> candidates = waveOne().stream()
                .filter(primitive -> primitive.state() == checkedState)
                .sorted(Comparator.comparing(Primitive::id))
                .toList();
        if (candidates.isEmpty()) throw new IllegalArgumentException("no commitment presentation for supplied state");

        int bestMatch = candidates.stream().mapToInt(p -> overlap(p.affinityTags(), positiveEvidence)).max().orElse(0);
        List<Primitive> preferred = bestMatch > 0
                ? candidates.stream().filter(p -> overlap(p.affinityTags(), positiveEvidence) == bestMatch).toList()
                : candidates;

        String authorityKey = checkedScenario + "|" + checkedFaction + "|" + checkedAgreement + "|"
                + checkedCommitment + "|" + checkedState.name() + "|"
                + positiveEvidence.stream().sorted().collect(Collectors.joining(","));
        Primitive primitive = preferred.get(index(seed, authorityKey + "|primitive", preferred.size()));
        String cue = primitive.presentationCues().get(index(seed, authorityKey + "|" + primitive.id() + "|cue",
                primitive.presentationCues().size()));
        Set<String> matched = primitive.affinityTags().stream().filter(positiveEvidence::contains).sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new Selection(GENERATOR_VERSION, seed, checkedScenario, checkedFaction, checkedAgreement,
                checkedCommitment, checkedState, primitive, cue, matched);
    }

    public static Primitive requirePrimitive(String id) {
        String checked = stableId(id);
        return waveOne().stream().filter(p -> p.id().equals(checked)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown faction commitment primitive: " + checked));
    }

    private static Primitive p(String id, CommitmentState state, String title, String statusRead, String playerPrompt,
                               String r1, String r2, String r3, Set<String> tags, String c1, String c2, String boundary) {
        return new Primitive(id, state, title, statusRead, playerPrompt, List.of(r1, r2, r3), tags, List.of(c1, c2), boundary);
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

    private static Set<String> nonEmptyTags(Set<String> tags) {
        Objects.requireNonNull(tags, "tags");
        if (tags.isEmpty()) throw new IllegalArgumentException("affinityTags must not be empty");
        return Set.copyOf(tags.stream().map(NightmareFactionAgreementCommitmentCatalog::stableId)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
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
