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

/** DESIGN-only player-facing negotiation responses for already-resolved Java-owned Nightmare factions. */
public final class NightmareFactionNegotiationResponseCatalog {
    public static final String GENERATOR_VERSION = "nightmare-faction-negotiation-response-v1";

    private NightmareFactionNegotiationResponseCatalog() {}

    public enum ResponseFamily { COOPERATION, COUNTEROFFER, REFUSAL, WARNING, CONDITIONAL_ACCESS }

    public record Primitive(String id, ResponseFamily family, String title, String responseRead,
                            String factionLine, List<String> playerOptions, Set<String> affinityTags,
                            List<String> presentationCues, String antiOverclaimBoundary) {
        public Primitive {
            id = stableId(id);
            family = Objects.requireNonNull(family, "family");
            title = text(title, "title");
            responseRead = text(responseRead, "responseRead");
            factionLine = text(factionLine, "factionLine");
            playerOptions = exactTextList(playerOptions, 3, "playerOptions");
            affinityTags = nonEmptyTags(affinityTags);
            presentationCues = exactTextList(presentationCues, 2, "presentationCues");
            antiOverclaimBoundary = text(antiOverclaimBoundary, "antiOverclaimBoundary");
        }
    }

    public record Selection(String generatorVersion, long seed, String scenarioId, String factionId,
                            String pressureId, String interactionStateId, Set<ResponseFamily> allowedFamilies,
                            Primitive primitive, String presentationCue, Set<String> matchedEvidenceTags) {
        public Selection {
            generatorVersion = text(generatorVersion, "generatorVersion");
            scenarioId = opaqueId(scenarioId, "scenarioId");
            factionId = opaqueId(factionId, "factionId");
            pressureId = opaqueId(pressureId, "pressureId");
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
                p("cooperate_narrow_goal", ResponseFamily.COOPERATION, "Share the Narrow Goal",
                        "The faction accepts one bounded immediate aim without declaring broader trust or allegiance.",
                        "We can work toward that much. Nothing beyond it is settled.",
                        "Confirm the exact shared aim.", "Ask what would end the cooperation.", "Proceed without treating agreement as proof of trust.",
                        Set.of("cooperation", "duty", "rescue"),
                        "The shared objective is highlighted separately from the wider dispute.", "A temporary agreement marker avoids any permanent faction color change.",
                        "Cooperation presentation cannot establish trust, allegiance, truth, persuasion success, or Nightmare completion."),
                p("cooperate_parallel_effort", ResponseFamily.COOPERATION, "Work in Parallel",
                        "The faction is willing to pursue compatible tasks beside the player while keeping authority and resources separate.",
                        "Do your part. We will do ours. We compare results afterward.",
                        "Define who is responsible for each task.", "Choose a later verification point.", "Keep resource ownership unchanged unless Java says otherwise.",
                        Set.of("cooperation", "resource", "verification"),
                        "Two task lanes are shown without merging inventories or command structures.", "A later comparison point is visible as a presentation reminder only.",
                        "Parallel effort cannot transfer resources, command NPCs, guarantee task success, or accept scenario events."),
                p("cooperate_escort_observed", ResponseFamily.COOPERATION, "Escort Under Observation",
                        "The faction accepts limited accompaniment while retaining its own watch, route, and withdrawal conditions.",
                        "You may come with us, provided everyone keeps to the agreed route.",
                        "Ask what route and behavior are actually permitted.", "Accept an escort without claiming territorial rights.", "Decline if the boundary is too restrictive.",
                        Set.of("cooperation", "route", "territory"),
                        "The permitted route is shown as bounded access rather than map ownership.", "Watchful posture communicates caution without a hostility meter.",
                        "An escort does not prove safety, loyalty, ownership, legal passage, or successful arrival."),
                p("cooperate_open_record", ResponseFamily.COOPERATION, "Open the Record",
                        "The faction agrees to expose a bounded record, account, or local fact for inspection without conceding every disputed claim.",
                        "You may inspect this part. The rest remains ours to answer for.",
                        "Inspect only the supplied record.", "Ask what remains withheld.", "Seek independent corroboration before drawing a conclusion.",
                        Set.of("cooperation", "information", "secrecy"),
                        "The disclosed record is visually separated from withheld material.", "No authenticity seal is added by presentation alone.",
                        "Disclosure cannot certify authenticity, truthfulness, innocence, guilt, or appraisal relevance."),

                p("counter_time_terms", ResponseFamily.COUNTEROFFER, "Time-Bounded Terms",
                        "The faction rejects the original breadth of the request but offers a narrower window, duration, or sequence.",
                        "Not indefinitely. For one watch, one crossing, or one attempt, we can agree.",
                        "Accept the bounded window.", "Ask what changes when it expires.", "Counter with a different duration without assuming leverage math.",
                        Set.of("counteroffer", "time", "access"),
                        "A clear start and end condition is shown without a countdown prediction.", "The narrower scope is emphasized over theatrical concession.",
                        "Time-bounded terms do not calculate fairness, pressure, success odds, or a canonical bargaining threshold."),
                p("counter_exchange_one", ResponseFamily.COUNTEROFFER, "Exchange One Thing",
                        "The faction offers one bounded resource, fact, service, or permission in return for another specifically named contribution.",
                        "One thing for one thing. No wider debt is implied.",
                        "Clarify both sides of the exchange.", "Offer a substitute contribution.", "Walk away without creating a reputation penalty.",
                        Set.of("counteroffer", "resource", "bargain"),
                        "Both proposed contributions are displayed side by side.", "No value bar claims that the exchange is objectively equal.",
                        "The catalogue cannot spend resources, create debt, alter reputation, or decide whether an exchange succeeds."),
                p("counter_substitute_condition", ResponseFamily.COUNTEROFFER, "Substitute the Condition",
                        "The faction cannot accept the stated condition and proposes another check, guarantor, route, or limit instead.",
                        "That condition is unacceptable. Meet this narrower one and we can continue.",
                        "Ask why the original condition fails.", "Test the substitute condition if Java authorizes it.", "Reject both conditions and preserve the disagreement.",
                        Set.of("counteroffer", "verification", "secrecy"),
                        "Original and substitute conditions are both retained in the exchange summary.", "The proposed substitute is not styled as an automatic quest gate.",
                        "A substitute condition cannot become a hidden prerequisite, truth test, persuasion score, or automatic scenario transition."),
                p("counter_staged_agreement", ResponseFamily.COUNTEROFFER, "Staged Agreement",
                        "The faction proposes a small reversible first step before considering a larger request.",
                        "Start with the part we can undo. If that holds, we discuss the rest.",
                        "Accept only the first stage.", "Ask what evidence would justify a later stage.", "Refuse escalation until the first result is verified.",
                        Set.of("counteroffer", "verification", "cooperation"),
                        "The first stage is visually isolated from later possibilities.", "Future stages remain conditional text rather than unlocked state.",
                        "Staging does not guarantee later cooperation, progression, trust gain, or acceptance of any scenario event."),

                p("refuse_broader_ask", ResponseFamily.REFUSAL, "Refuse the Broader Ask",
                        "The faction rejects the player's requested scope while leaving narrower conversation or alternatives possible.",
                        "No. Not as you asked it.",
                        "Ask what narrower request remains discussable.", "Record the refusal without treating it as hostility.", "Leave and pursue another route.",
                        Set.of("refusal", "boundary", "access"),
                        "The rejected scope is shown precisely instead of turning the whole faction hostile.", "Alternative channels remain visible when supplied by Java.",
                        "Refusal does not prove hostility, deception, permanent lockout, failed persuasion, or terminal scenario failure."),
                p("refuse_preserve_boundary", ResponseFamily.REFUSAL, "Preserve the Boundary",
                        "The faction refuses to cross a territorial, secrecy, duty, or resource boundary it currently treats as non-negotiable.",
                        "That boundary remains. We will not cross it for this request.",
                        "Ask whether the boundary is temporary or contextual.", "Seek another authorized actor.", "Respect the refusal without validating the underlying claim.",
                        Set.of("refusal", "territory", "secrecy"),
                        "The current boundary is emphasized without a permanent world-state lock icon.", "The faction claim and observed physical boundary remain distinct.",
                        "Presentation cannot establish sovereignty, legitimacy, magical compulsion, permanent refusal, or ownership."),
                p("refuse_without_threat", ResponseFamily.REFUSAL, "Decline Without Threat",
                        "The faction declines participation while explicitly not escalating to violence or retaliation in this exchange.",
                        "We will not help. We are not threatening you either.",
                        "Accept the neutral refusal.", "Ask for information that does not require participation.", "Withdraw without creating a hostility state.",
                        Set.of("refusal", "warning", "cooperation"),
                        "Weapons or threat cues remain neutral unless Java supplies an actual hostile state.", "The refusal is presented as a decision, not a combat trigger.",
                        "A neutral refusal cannot guarantee future peace, disable combat, alter aggro, or modify allegiance."),
                p("refuse_close_route", ResponseFamily.REFUSAL, "Close This Route",
                        "The faction rejects passage through one route, crossing, refuge, or channel while leaving the wider scenario unresolved.",
                        "Not through here.",
                        "Ask whether another route exists.", "Ask what condition currently closes this route.", "Leave without treating the closure as universal map truth.",
                        Set.of("refusal", "route", "territory"),
                        "Only the named route is marked unavailable in the presentation layer.", "No unexplored route is automatically revealed.",
                        "The catalogue cannot block terrain, prove all alternatives unsafe, guarantee enforcement, or fail the Nightmare."),

                p("warning_state_consequence", ResponseFamily.WARNING, "State the Consequence",
                        "The faction warns about a consequence it believes will follow from an action, while leaving truth and probability unresolved.",
                        "If you do that, this is what we believe will follow.",
                        "Ask what evidence supports the warning.", "Proceed only if Java allows the action.", "Seek another account before accepting the forecast.",
                        Set.of("warning", "information", "risk"),
                        "The warning is labelled as the faction's stated expectation.", "No probability meter or prophecy styling is added.",
                        "A warning cannot establish future truth, probability, guilt, magical foresight, or a forced consequence."),
                p("warning_name_boundary", ResponseFamily.WARNING, "Name the Boundary",
                        "The faction warns that crossing a stated line will change its response, without presentation deciding what that later response actually becomes.",
                        "Cross that line and our posture changes.",
                        "Clarify the exact boundary.", "Ask what response is being threatened or promised.", "Choose another approach without assuming the warning is bluff or truth.",
                        Set.of("warning", "boundary", "territory"),
                        "The boundary is made readable without pre-authoring an escalation result.", "Current and hypothetical future posture are displayed separately.",
                        "The catalogue cannot trigger hostility, set aggro, prove intent, or decide an escalation transition."),
                p("warning_offer_exit", ResponseFamily.WARNING, "Warn and Offer Exit",
                        "The faction gives a warning while leaving a clear non-escalatory way for the player to disengage.",
                        "Turn back now and this exchange ends here.",
                        "Take the offered exit.", "Ask whether another route or time is acceptable.", "Stay only if Java-owned interaction state permits it.",
                        Set.of("warning", "withdrawal", "access"),
                        "An explicit disengagement option is foregrounded beside the warning.", "No fear, morale, or intimidation score is displayed.",
                        "Offering an exit cannot guarantee safety, prevent pursuit, calculate intimidation, or settle later faction behavior."),
                p("warning_signal_escalation", ResponseFamily.WARNING, "Signal Escalation",
                        "The faction visibly prepares for a more severe response if the current dispute continues, but the escalation has not yet been accepted as world state.",
                        "We are close to ending this discussion another way.",
                        "Ask what action would prevent escalation.", "Disengage before testing the threat.", "Continue only through Java-authorized interaction logic.",
                        Set.of("warning", "escalation", "duty"),
                        "Readiness cues increase without changing Java-owned combat state.", "The UI distinguishes threatened escalation from active violence.",
                        "Presentation cannot spawn combat, change AI, alter allegiance, assign threat level, or accept an escalation event."),

                p("access_after_verification", ResponseFamily.CONDITIONAL_ACCESS, "Access After Verification",
                        "The faction is willing to permit bounded access only after a caller-authorized verification step is satisfied.",
                        "Verify what you claim, and we can discuss entry.",
                        "Ask what exact verification is required.", "Use another approved proof if one exists.", "Decline the condition without creating automatic hostility.",
                        Set.of("conditional_access", "verification", "secrecy"),
                        "The verification requirement is shown as caller-supplied interaction state.", "No hidden progress bar implies partial persuasion.",
                        "This response cannot decide whether verification succeeds, unlock terrain, prove identity, or mutate objective state."),
                p("access_escort_only", ResponseFamily.CONDITIONAL_ACCESS, "Escort Only",
                        "The faction allows access only while one of its own actors accompanies the player under bounded conditions.",
                        "You may pass with an escort. Not alone.",
                        "Accept the escort condition.", "Ask what the escort may restrict or observe.", "Decline and seek another route.",
                        Set.of("conditional_access", "escort", "territory"),
                        "Escort presence is framed as a condition rather than ownership transfer.", "The permitted path remains bounded to Java-supplied state.",
                        "An escort condition cannot move NPCs, guarantee passage, establish surveillance mechanics, or confer legal authority."),
                p("access_one_window", ResponseFamily.CONDITIONAL_ACCESS, "One Passage Window",
                        "The faction offers one bounded opportunity for passage or use without promising repeat access.",
                        "One window. Miss it or exceed it, and the offer ends.",
                        "Clarify the allowed window.", "Use it only if Java marks access active.", "Ask whether another arrangement is possible later.",
                        Set.of("conditional_access", "time", "route"),
                        "The one-time nature of the offer is explicit without inventing a canonical timer.", "Repeat access remains visibly unresolved.",
                        "Presentation cannot start a timer, guarantee route safety, consume access, or decide whether another window appears."),
                p("access_named_purpose", ResponseFamily.CONDITIONAL_ACCESS, "Access for a Named Purpose",
                        "The faction permits a narrow action or visit while withholding broader use of the same place, resource, or information.",
                        "For that purpose only. Nothing else is agreed.",
                        "Confirm the permitted purpose.", "Ask what actions remain outside the agreement.", "Leave if the purpose no longer matches the player's need.",
                        Set.of("conditional_access", "duty", "resource"),
                        "Permitted purpose and excluded actions are shown together.", "The presentation avoids a permanent access badge.",
                        "Purpose-limited access cannot grant ownership, inventory rights, permanent permission, or scenario completion."));
    }

    public static Selection compose(long seed, String scenarioId, String factionId, String pressureId,
                                    String interactionStateId, Set<ResponseFamily> allowedFamilies,
                                    Map<String, Integer> evidence) {
        String checkedScenario = opaqueId(scenarioId, "scenarioId");
        String checkedFaction = opaqueId(factionId, "factionId");
        String checkedPressure = opaqueId(pressureId, "pressureId");
        String checkedState = opaqueId(interactionStateId, "interactionStateId");
        Set<ResponseFamily> checkedFamilies = nonEmptyFamilies(allowedFamilies);
        Set<String> positiveEvidence = positiveEvidence(evidence);

        List<Primitive> candidates = waveOne().stream()
                .filter(primitive -> checkedFamilies.contains(primitive.family()))
                .sorted(Comparator.comparing(Primitive::id))
                .toList();
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("no negotiation responses available for allowed families");
        }

        int bestMatch = candidates.stream()
                .mapToInt(primitive -> overlap(primitive.affinityTags(), positiveEvidence))
                .max()
                .orElse(0);
        List<Primitive> preferred = bestMatch > 0
                ? candidates.stream().filter(primitive -> overlap(primitive.affinityTags(), positiveEvidence) == bestMatch).toList()
                : candidates;

        String authorityKey = checkedScenario + "|" + checkedFaction + "|" + checkedPressure + "|" + checkedState
                + "|" + checkedFamilies.stream().map(Enum::name).sorted().collect(Collectors.joining(","))
                + "|" + positiveEvidence.stream().sorted().collect(Collectors.joining(","));
        Primitive primitive = preferred.get(index(seed, authorityKey + "|primitive", preferred.size()));
        String cue = primitive.presentationCues().get(index(seed, authorityKey + "|" + primitive.id() + "|cue",
                primitive.presentationCues().size()));
        Set<String> matched = primitive.affinityTags().stream()
                .filter(positiveEvidence::contains)
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return new Selection(GENERATOR_VERSION, seed, checkedScenario, checkedFaction, checkedPressure, checkedState,
                checkedFamilies, primitive, cue, matched);
    }

    public static Primitive requirePrimitive(String id) {
        String checked = stableId(id);
        return waveOne().stream().filter(primitive -> primitive.id().equals(checked)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown negotiation response primitive: " + checked));
    }

    private static Primitive p(String id, ResponseFamily family, String title, String responseRead, String factionLine,
                               String optionOne, String optionTwo, String optionThree, Set<String> tags,
                               String cueOne, String cueTwo, String boundary) {
        return new Primitive(id, family, title, responseRead, factionLine, List.of(optionOne, optionTwo, optionThree),
                tags, List.of(cueOne, cueTwo), boundary);
    }

    private static int overlap(Set<String> left, Set<String> right) {
        int matches = 0;
        for (String value : left) {
            if (right.contains(value)) {
                matches++;
            }
        }
        return matches;
    }

    private static int index(long seed, String key, int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("bound must be positive");
        }
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
            if (magnitude < 0) {
                throw new IllegalArgumentException("negative evidence is not supported");
            }
            if (magnitude > 0) {
                tags.add(tag);
            }
        });
        return Set.copyOf(tags);
    }

    private static Set<ResponseFamily> nonEmptyFamilies(Set<ResponseFamily> families) {
        Objects.requireNonNull(families, "allowedFamilies");
        if (families.isEmpty()) {
            throw new IllegalArgumentException("allowedFamilies must not be empty");
        }
        if (families.contains(null)) {
            throw new IllegalArgumentException("allowedFamilies must not contain null");
        }
        return Set.copyOf(families);
    }

    private static Set<String> nonEmptyTags(Set<String> tags) {
        Objects.requireNonNull(tags, "tags");
        if (tags.isEmpty()) {
            throw new IllegalArgumentException("affinityTags must not be empty");
        }
        Set<String> normalized = tags.stream().map(NightmareFactionNegotiationResponseCatalog::stableId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return Set.copyOf(normalized);
    }

    private static List<String> exactTextList(List<String> values, int expected, String field) {
        Objects.requireNonNull(values, field);
        if (values.size() != expected) {
            throw new IllegalArgumentException(field + " must contain exactly " + expected + " entries");
        }
        List<String> checked = new ArrayList<>(expected);
        for (String value : values) {
            checked.add(text(value, field));
        }
        return List.copyOf(checked);
    }

    private static String opaqueId(String value, String field) {
        Objects.requireNonNull(value, field);
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }

    private static String text(String value, String field) {
        Objects.requireNonNull(value, field);
        String checked = value.trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
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
