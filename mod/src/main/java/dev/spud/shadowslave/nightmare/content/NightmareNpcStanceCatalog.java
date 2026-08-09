package dev.spud.shadowslave.nightmare.content;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Authored player-facing NPC stance/response primitives for an already-resolved Nightmare role/NPC.
 *
 * <p>This is DESIGN content. The caller supplies authoritative role/NPC identity and the response
 * families allowed by scenario authoring. Composition can only choose readable response texture.
 * It cannot decide truth, allegiance, persuasion success, accepted scenario events, appraisal,
 * rewards, progression, or persistent relationship state.</p>
 */
public final class NightmareNpcStanceCatalog {
    public static final String GENERATOR_VERSION = "nightmare-npc-stance-v1";

    private NightmareNpcStanceCatalog() {
    }

    public enum StanceFamily {
        COOPERATION,
        REFUSAL,
        BARGAINING,
        WARNING,
        CONDITIONAL_HELP,
        WITHDRAWAL
    }

    public record StancePrimitive(
            String id,
            String title,
            StanceFamily family,
            String responseRead,
            List<String> spokenHooks,
            List<String> playerLevers,
            Set<String> affinityTags,
            List<String> presentationCues,
            String antiOverclaimBoundary
    ) {
        public StancePrimitive {
            spokenHooks = List.copyOf(spokenHooks);
            playerLevers = List.copyOf(playerLevers);
            affinityTags = Set.copyOf(affinityTags);
            presentationCues = List.copyOf(presentationCues);
        }
    }

    public record Composition(
            String actorContextId,
            StancePrimitive primitive,
            String presentationCue,
            int matchedEvidenceTags,
            long seed,
            String generatorVersion
    ) {
    }

    private static final List<StancePrimitive> WAVE_ONE = List.of(
            stance("open_cooperation", "Open Cooperation", StanceFamily.COOPERATION,
                    "They are willing to work with the player on the immediate problem without demanding control of the whole plan.",
                    List.of("Tell me what you need first.", "I will take one part if you take the other."),
                    List.of("Define a bounded task.", "Offer independently verifiable information."),
                    Set.of("cooperation", "duty", "rescue"),
                    List.of("They answer with concrete tasks instead of testing your intentions.", "Their posture shifts toward the shared problem rather than toward you."),
                    "Cooperation does not prove trust, friendship, truthfulness, allegiance, or scenario success."),
            stance("parallel_effort", "Parallel Effort", StanceFamily.COOPERATION,
                    "They agree to act toward the same local outcome while keeping authority and routes separate.",
                    List.of("We do not have to agree on everything to do this.", "Take your route. I will cover mine."),
                    List.of("Split responsibilities cleanly.", "Choose checkpoints that expose divergence early."),
                    Set.of("cooperation", "independence", "route"),
                    List.of("They cooperate without surrendering their own plan.", "Agreement appears in synchronized actions rather than warm language."),
                    "This does not establish durable alliance state or automatic coordination bonuses."),
            stance("evidence_first", "Evidence First", StanceFamily.COOPERATION,
                    "They will collaborate when the next action can be anchored to evidence both sides can inspect.",
                    List.of("Show me what you saw, then I will show you mine.", "We act on what we can both verify."),
                    List.of("Present physical evidence.", "Invite a neutral witness or repeatable check."),
                    Set.of("cooperation", "evidence", "verification"),
                    List.of("Their willingness rises when the discussion becomes testable.", "They keep cooperation attached to things that can be checked."),
                    "This does not grant truth detection or establish that the evidence is complete or correct."),
            stance("shared_watch", "Shared Watch", StanceFamily.COOPERATION,
                    "They offer practical mutual coverage during a dangerous pause, crossing, or observation window.",
                    List.of("You watch that side. I will watch this one.", "Wake me if the marker changes, not just if you get nervous."),
                    List.of("Agree on observable triggers.", "Use overlapping but independent observation."),
                    Set.of("cooperation", "watch", "hazard"),
                    List.of("They turn cooperation into a watch pattern rather than a promise.", "They define what each person is responsible for noticing."),
                    "This does not create guaranteed safety, perfect detection, or a canonical watch mechanic."),

            stance("flat_refusal", "Flat Refusal", StanceFamily.REFUSAL,
                    "They reject the proposed action directly without pretending that the conversation itself resolved the disagreement.",
                    List.of("No. Not that way.", "Ask me for something else."),
                    List.of("Change the requested action.", "Ask for the concrete reason without assuming it will change the answer."),
                    Set.of("refusal", "boundary", "risk"),
                    List.of("The answer arrives before negotiation can gather momentum.", "They make the boundary clearer than the explanation."),
                    "Refusal does not prove hostility, hidden motive, immunity to persuasion, or permanent allegiance."),
            stance("refusal_without_betrayal", "Refusal Without Betrayal", StanceFamily.REFUSAL,
                    "They refuse one request while explicitly leaving room for other forms of help or coexistence.",
                    List.of("I will not do that, but I am not your enemy.", "I can help another way."),
                    List.of("Ask what assistance remains acceptable.", "Separate the disputed act from the broader relationship."),
                    Set.of("refusal", "relationship", "cooperation"),
                    List.of("They close one door while pointing to another.", "Their refusal is narrow even if their tone is hard."),
                    "This does not prove loyalty or guarantee that an alternative offer is sincere or sufficient."),
            stance("procedural_refusal", "Procedural Refusal", StanceFamily.REFUSAL,
                    "They reject acting until a required witness, handoff, signal, route check, or other authored condition is met.",
                    List.of("Not until someone else sees this.", "I move when the route is checked."),
                    List.of("Meet the stated procedural condition.", "Challenge whether the condition is actually necessary."),
                    Set.of("refusal", "procedure", "evidence"),
                    List.of("They argue about process more than outcome.", "Their refusal has a concrete prerequisite instead of a vague mood."),
                    "This does not invent universal quest prerequisites or prove the stated condition is justified."),
            stance("protective_refusal", "Protective Refusal", StanceFamily.REFUSAL,
                    "They reject a plan because they believe it exposes another person or group to unacceptable danger.",
                    List.of("You are asking someone else to pay for this.", "Find another route for them first."),
                    List.of("Reduce the third party's exposure.", "Ask the affected person rather than speaking for them."),
                    Set.of("refusal", "protection", "civilian"),
                    List.of("They evaluate the cost to absent people before the benefit to themselves.", "Their objection sharpens whenever another person's risk is minimized."),
                    "This does not establish objective danger, moral correctness, or automatic protection behavior."),

            stance("terms_for_access", "Terms for Access", StanceFamily.BARGAINING,
                    "They will provide access, passage, information, or labor only in exchange for a bounded reciprocal action.",
                    List.of("You want through; I want something carried back.", "One favor for one door."),
                    List.of("Narrow the exchange.", "Offer a substitute that preserves the same practical value."),
                    Set.of("bargain", "access", "exchange"),
                    List.of("They turn the disagreement into two explicit asks.", "Their attention moves to what each side can actually deliver."),
                    "This does not implement prices, contract enforcement, persuasion math, or guaranteed acceptance."),
            stance("temporary_truce", "Temporary Truce", StanceFamily.BARGAINING,
                    "They offer a time- or problem-bounded pause in conflict without resolving the larger dispute.",
                    List.of("Until the crossing is clear, we stop this.", "After the wounded are out, we can hate each other again."),
                    List.of("Define the exact stopping condition.", "Create a visible boundary both sides can monitor."),
                    Set.of("bargain", "truce", "timing"),
                    List.of("They negotiate an interval, not reconciliation.", "Every concession is tied to a clear end condition."),
                    "This does not create faction peace, trust, safe zones, or a canonical truce system."),
            stance("information_trade", "Information Trade", StanceFamily.BARGAINING,
                    "They exchange bounded operational knowledge while keeping other facts private.",
                    List.of("One route for one name.", "Tell me what changed at the gate and I will tell you what I found below."),
                    List.of("Trade independently useful facts.", "Verify received information before escalating the exchange."),
                    Set.of("bargain", "information", "verification"),
                    List.of("They measure information by usefulness rather than intimacy.", "Each answer is paired with a request for something comparable."),
                    "This does not prove the information true, complete, secret, or canonically valuable."),
            stance("costly_concession", "Costly Concession", StanceFamily.BARGAINING,
                    "They will accept a disliked compromise when the alternative threatens something they value more.",
                    List.of("I hate this, but I hate the other outcome more.", "You get this much. No more."),
                    List.of("Reduce the concession's collateral cost.", "Make the alternative consequence concrete rather than rhetorical."),
                    Set.of("bargain", "cost", "pressure"),
                    List.of("Agreement arrives with an explicit limit.", "They treat compromise as damage control rather than conversion."),
                    "This does not prove coercion, calculate leverage, or guarantee durable compliance."),

            stance("immediate_warning", "Immediate Warning", StanceFamily.WARNING,
                    "They prioritize communicating a near-term danger over resolving who is right about the larger situation.",
                    List.of("Argue later. Move now.", "The route is changing while we talk."),
                    List.of("Ask what observable sign makes the warning urgent.", "Take a reversible precaution while checking the claim."),
                    Set.of("warning", "hazard", "timing"),
                    List.of("Their language becomes shorter as the perceived window narrows.", "They point to immediate conditions rather than old grievances."),
                    "A warning does not prove danger, prediction, supernatural sensing, or encounter timing."),
            stance("bounded_warning", "Bounded Warning", StanceFamily.WARNING,
                    "They warn only about the part they personally observed or can support, preserving uncertainty elsewhere.",
                    List.of("I saw the lower marker move. I did not see what moved it.", "The west route failed once; I cannot tell you why."),
                    List.of("Preserve the stated uncertainty.", "Cross-check the observation independently."),
                    Set.of("warning", "evidence", "uncertainty"),
                    List.of("They distinguish observation from explanation without being prompted.", "Their warning gets narrower when pressed for certainty."),
                    "This does not grant perfect memory, truthfulness, or a canonical confidence system."),
            stance("warning_with_exit", "Warning with Exit", StanceFamily.WARNING,
                    "They pair a danger report with one practical way to disengage rather than presenting confrontation as mandatory.",
                    List.of("If you do not want this fight, leave by the service road.", "The upper path is exposed, but you can still turn back."),
                    List.of("Verify the exit before relying on it.", "Compare the exit's cost against staying."),
                    Set.of("warning", "escape", "route"),
                    List.of("The warning includes a way not to commit.", "They describe the hazard and the retreat option in the same breath."),
                    "This does not guarantee the exit is safe, open, optimal, or canonically generated."),
            stance("warning_against_assumption", "Warning Against Assumption", StanceFamily.WARNING,
                    "They challenge a confident interpretation because the available evidence supports more than one explanation.",
                    List.of("You know what happened, not why.", "That sign can mean more than one thing."),
                    List.of("List alternative explanations.", "Delay irreversible action until another check is available."),
                    Set.of("warning", "misinformation", "verification"),
                    List.of("They attack certainty rather than the observation itself.", "Their warning is about inference, not necessarily about danger."),
                    "This does not make the NPC an oracle, investigator authority, or automatic detector of false conclusions."),

            stance("help_after_proof", "Help After Proof", StanceFamily.CONDITIONAL_HELP,
                    "They will assist once the player demonstrates a specific claim, identity, route fact, or prior action.",
                    List.of("Show me the marker, then I go with you.", "Bring proof the gate is open and I will move the others."),
                    List.of("Satisfy the narrow evidence condition.", "Offer a different verifiable condition serving the same concern."),
                    Set.of("conditional", "evidence", "help"),
                    List.of("Their help is attached to one checkable condition.", "They describe what would change their answer instead of demanding vague trust."),
                    "This does not define universal quest gating or guarantee that proof changes allegiance."),
            stance("help_after_safety", "Help After Safety", StanceFamily.CONDITIONAL_HELP,
                    "They will help only after a person, route, shelter, or fallback is made acceptably secure by authored scenario terms.",
                    List.of("Get them under cover and I will come.", "Mark the return route first."),
                    List.of("Reduce the named exposure.", "Create a fallback before asking for commitment."),
                    Set.of("conditional", "safety", "route"),
                    List.of("They keep returning to what must be secured first.", "The requested help becomes discussable only after a fallback exists."),
                    "This does not create safe-zone authority, numeric safety thresholds, or guaranteed participation."),
            stance("help_with_limit", "Help with Limit", StanceFamily.CONDITIONAL_HELP,
                    "They agree to assist only within a declared boundary of time, distance, target, or responsibility.",
                    List.of("I will take you to the arch, no farther.", "One trip. After that, you are on your own."),
                    List.of("Respect the stated boundary.", "Negotiate one specific extension instead of assuming unlimited help."),
                    Set.of("conditional", "boundary", "help"),
                    List.of("They agree and define the stopping point in the same sentence.", "Their help comes with a visible edge."),
                    "This does not implement follower duration, command range, loyalty, or AI ownership."),
            stance("help_if_reciprocal", "Help if Reciprocal", StanceFamily.CONDITIONAL_HELP,
                    "They will commit if the player accepts a corresponding risk or responsibility rather than outsourcing all cost.",
                    List.of("I go if you carry your share.", "You want my people exposed; put yourself on the same route."),
                    List.of("Accept a symmetric responsibility.", "Propose a different fair division of risk."),
                    Set.of("conditional", "reciprocity", "risk"),
                    List.of("They test whether the player is willing to pay the same kind of cost.", "Their condition is about shared exposure rather than payment."),
                    "This does not calculate fairness, morality, persuasion, or appraisal value."),

            stance("leave_the_conversation", "Leave the Conversation", StanceFamily.WITHDRAWAL,
                    "They end the exchange because continued argument is no longer worth the immediate cost.",
                    List.of("We are done talking.", "I have somewhere else to be before this gets worse."),
                    List.of("Do not treat departure as agreement.", "Reopen contact later only with materially changed circumstances."),
                    Set.of("withdrawal", "timing", "conflict"),
                    List.of("They stop answering and start preparing to move.", "The conversation ends through action rather than a final argument."),
                    "Withdrawal does not prove hostility, defeat, fear, or a permanent relationship transition."),
            stance("withdraw_to_observe", "Withdraw to Observe", StanceFamily.WITHDRAWAL,
                    "They step back from commitment in order to watch how uncertain conditions develop.",
                    List.of("I am not choosing until I see what changes.", "You act first; I will watch the result."),
                    List.of("Provide new observable evidence.", "Offer a reversible trial action instead of full commitment."),
                    Set.of("withdrawal", "observation", "uncertainty"),
                    List.of("They create distance without leaving the area entirely.", "Their next move is observation, not support or attack."),
                    "This does not define hidden AI states, future allegiance, or probability of re-engagement."),
            stance("protective_withdrawal", "Protective Withdrawal", StanceFamily.WITHDRAWAL,
                    "They disengage from the immediate objective to move another person, group, or resource out of danger.",
                    List.of("I am taking them out. Decide without me.", "This argument can wait; they cannot."),
                    List.of("Offer a safer handoff.", "Separate evacuation from the disputed objective."),
                    Set.of("withdrawal", "protection", "rescue"),
                    List.of("Their attention leaves the dispute and fixes on who can still be moved.", "They choose distance from the conflict as a form of protection."),
                    "This does not prove moral priority, successful rescue, or canonical NPC pathfinding behavior."),
            stance("withdraw_with_information", "Withdraw with Information", StanceFamily.WITHDRAWAL,
                    "They leave after giving one bounded fact, warning, or route detail without joining the player's plan.",
                    List.of("I will tell you this much, then I am gone.", "Use the east marker. Do not ask me to come with you."),
                    List.of("Separate the information from assumed endorsement.", "Verify the fact independently after departure."),
                    Set.of("withdrawal", "information", "warning"),
                    List.of("They contribute one useful thing while making departure non-negotiable.", "The information survives the relationship ending for now."),
                    "This does not prove the information true or convert withdrawal into cooperation." )
    );

    private static StancePrimitive stance(
            String id,
            String title,
            StanceFamily family,
            String responseRead,
            List<String> spokenHooks,
            List<String> playerLevers,
            Set<String> affinityTags,
            List<String> presentationCues,
            String antiOverclaimBoundary
    ) {
        return new StancePrimitive(id, title, family, responseRead, spokenHooks, playerLevers, affinityTags, presentationCues, antiOverclaimBoundary);
    }

    public static List<StancePrimitive> waveOne() {
        return WAVE_ONE;
    }

    public static Composition compose(
            long seed,
            String actorContextId,
            Set<StanceFamily> allowedFamilies,
            Map<String, Integer> evidence
    ) {
        if (actorContextId == null || actorContextId.isBlank()) {
            throw new IllegalArgumentException("actorContextId must be non-blank");
        }
        if (allowedFamilies == null || allowedFamilies.isEmpty()) {
            throw new IllegalArgumentException("allowedFamilies must be non-empty");
        }
        if (evidence == null) {
            throw new IllegalArgumentException("evidence must be non-null");
        }
        evidence.forEach((tag, value) -> {
            if (tag == null || tag.isBlank() || value == null || value < 0) {
                throw new IllegalArgumentException("evidence must use non-blank tags and non-negative values");
            }
        });

        EnumSet<StanceFamily> familyCopy = EnumSet.copyOf(allowedFamilies);
        List<StancePrimitive> candidates = WAVE_ONE.stream()
                .filter(primitive -> familyCopy.contains(primitive.family()))
                .toList();
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("no stance primitives exist for allowed families");
        }

        Set<String> positiveTags = evidence.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        int bestMatch = candidates.stream().mapToInt(p -> matchedTags(p, positiveTags)).max().orElse(0);
        List<StancePrimitive> preferred = bestMatch == 0
                ? candidates
                : candidates.stream().filter(p -> matchedTags(p, positiveTags) == bestMatch).toList();

        String familyKey = familyCopy.stream().map(Enum::name).sorted().reduce((a, b) -> a + "," + b).orElse("");
        String evidenceKey = positiveTags.stream().sorted().reduce((a, b) -> a + "," + b).orElse("");
        long primitiveHash = hash64(GENERATOR_VERSION + "|primitive|" + seed + "|" + actorContextId + "|" + familyKey + "|" + evidenceKey);
        StancePrimitive selected = preferred.get(index(primitiveHash, preferred.size()));
        long cueHash = hash64(GENERATOR_VERSION + "|cue|" + seed + "|" + actorContextId + "|" + selected.id());
        String cue = selected.presentationCues().get(index(cueHash, selected.presentationCues().size()));

        return new Composition(actorContextId, selected, cue, matchedTags(selected, positiveTags), seed, GENERATOR_VERSION);
    }

    private static int matchedTags(StancePrimitive primitive, Set<String> positiveTags) {
        return (int) primitive.affinityTags().stream().filter(positiveTags::contains).count();
    }

    private static int index(long value, int size) {
        return Math.floorMod(value, size);
    }

    private static long hash64(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return ByteBuffer.wrap(bytes, 0, Long.BYTES).getLong();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }
}
