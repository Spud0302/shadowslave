package dev.spud.shadowslave.nightmare.content;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Authored player-facing NPC motive primitives for already-resolved Nightmare roles.
 *
 * <p>This is DESIGN content. The caller supplies the authoritative historical role ID
 * and the motive families that scenario authoring permits. Composition may vary only
 * presentation inside that boundary; it does not decide NPC truthfulness, allegiance,
 * persuasion, scenario outcome, appraisal, progression, or persistent relationship state.</p>
 */
public final class NightmareNpcMotiveCatalog {
    public static final String GENERATOR_VERSION = "nightmare-npc-motive-v1";

    private NightmareNpcMotiveCatalog() {
    }

    public enum MotiveFamily {
        DUTY,
        FEAR,
        CONCEALMENT,
        RIVALRY,
        OBLIGATION,
        DESPERATION,
        CONFLICTING_LOYALTY
    }

    public record MotivePrimitive(
            String id,
            String title,
            MotiveFamily family,
            String motiveRead,
            List<String> dialogueHooks,
            List<String> behaviorHooks,
            Set<String> affinityTags,
            List<String> presentationCues,
            String antiOverclaimBoundary
    ) {
        public MotivePrimitive {
            dialogueHooks = List.copyOf(dialogueHooks);
            behaviorHooks = List.copyOf(behaviorHooks);
            affinityTags = Set.copyOf(affinityTags);
            presentationCues = List.copyOf(presentationCues);
        }
    }

    public record Composition(
            String historicalRoleId,
            MotivePrimitive primitive,
            String presentationCue,
            int matchedEvidenceTags,
            long seed,
            String generatorVersion
    ) {
    }

    private static final List<MotivePrimitive> WAVE_ONE = List.of(
            motive("hold_the_post", "Hold the Post", MotiveFamily.DUTY,
                    "They measure every choice against a station, watch, route, or person they believe must be kept.",
                    List.of("Ask what would count as abandoning the duty.", "Offer a way to fulfill the duty without obeying its expected method."),
                    List.of("Returns to the assigned position when pressure eases.", "Checks threatened responsibilities before personal comfort."),
                    Set.of("duty", "watch", "preservation"),
                    List.of("Their attention keeps returning to the responsibility they were given.", "Even while speaking, they track whether their charge is being neglected."),
                    "Duty does not prove obedience, courage, moral correctness, or a canonical role behavior rule."),
            motive("carry_the_message", "Carry the Message", MotiveFamily.DUTY,
                    "They care most about getting a warning, order, testimony, or request to the person meant to receive it.",
                    List.of("Clarify which part of the message cannot be altered.", "Challenge whether delivery matters more than the messenger surviving."),
                    List.of("Protects records or witnesses that preserve the message.", "Prefers routes that improve delivery certainty over prestige."),
                    Set.of("duty", "signal", "warning"),
                    List.of("They repeat key details as if checking that nothing has changed.", "They treat interruption as a threat to the message itself."),
                    "This does not create supernatural message authority, guaranteed honesty, or canonical quest logic."),
            motive("keep_the_names", "Keep the Names", MotiveFamily.DUTY,
                    "They feel responsible for preserving who was present, missing, dead, accused, or owed an answer.",
                    List.of("Ask which name they are most afraid will be erased.", "Offer evidence that changes who must be remembered or accounted for."),
                    List.of("Counts people before supplies.", "Protects ledgers, markers, graves, or witness lists when practical."),
                    Set.of("duty", "witness", "evidence"),
                    List.of("They notice absences before explanations.", "Their questions keep returning to who has not been accounted for."),
                    "This is authored motive texture, not perfect memory, truth detection, or a canonical death-record system."),
            motive("finish_the_shift", "Finish the Shift", MotiveFamily.DUTY,
                    "They define success as bringing an ordinary responsibility to a clean end despite extraordinary danger.",
                    List.of("Ask what unfinished task would keep them from leaving.", "Offer a bounded handoff that lets someone else complete the work."),
                    List.of("Tries to close tools, routes, gates, or records before departure.", "Treats incomplete routine work as future risk."),
                    Set.of("duty", "labor", "closure"),
                    List.of("They talk about catastrophe in the language of unfinished work.", "Their plans include small closing tasks others would ignore."),
                    "This does not mean the Nightmare rewards diligence or that every role has a predetermined duty path."),

            motive("fear_of_repetition", "Fear of Repetition", MotiveFamily.FEAR,
                    "They are driven by the belief that an earlier disaster, betrayal, or mistake is about to happen again.",
                    List.of("Ask what present evidence actually matches the earlier event.", "Propose a safeguard that addresses the fear without accepting its conclusion."),
                    List.of("Overchecks familiar failure points.", "Resists plans that resemble the remembered disaster."),
                    Set.of("fear", "aftermath", "warning"),
                    List.of("They compare the present moment to something that already went wrong.", "A familiar detail makes them noticeably less willing to improvise."),
                    "Fear does not establish prophecy, trauma mechanics, or that the feared event will recur."),
            motive("fear_of_exposure", "Fear of Exposure", MotiveFamily.FEAR,
                    "They fear being seen, identified, blamed, or publicly connected to the current crisis.",
                    List.of("Ask what becomes dangerous if their involvement is known.", "Offer a private route that still requires a concrete contribution."),
                    List.of("Avoids public commitments.", "Prefers indirect help that leaves fewer witnesses."),
                    Set.of("fear", "social", "concealment"),
                    List.of("They become more careful when a third person approaches.", "Their answers grow narrower whenever responsibility is mentioned."),
                    "This does not prove guilt, deception, cowardice, or any universal witness-response rule."),
            motive("fear_for_another", "Fear for Another", MotiveFamily.FEAR,
                    "Their risk tolerance changes sharply when a particular person or group may be endangered.",
                    List.of("Ask whose safety changes their decision.", "Test whether the protected person would accept the proposed cost."),
                    List.of("Redirects danger away from the protected party.", "May reject strategically strong plans that increase another person's exposure."),
                    Set.of("fear", "relationship", "protection"),
                    List.of("Their composure changes more around another person's danger than their own.", "They keep checking one route, room, or group before deciding."),
                    "This does not create a canonical bond, protection bonus, or automatic self-sacrifice rule."),
            motive("fear_of_being_left", "Fear of Being Left", MotiveFamily.FEAR,
                    "They fear abandonment, isolation, or being deliberately excluded from the group's escape or decision.",
                    List.of("State clearly what place they have in the plan.", "Ask what they would do if the group chose a route they could not follow."),
                    List.of("Stays physically close to decision makers.", "Challenges plans that depend on splitting the group."),
                    Set.of("fear", "relationship", "isolation"),
                    List.of("They notice departures immediately.", "A suggestion to split up draws a stronger reaction than the hazard itself."),
                    "This is not a canonical attachment mechanic or proof that separation is objectively dangerous."),

            motive("hide_the_failure", "Hide the Failure", MotiveFamily.CONCEALMENT,
                    "They want a mistake, breach, loss, or dereliction to remain unknown long enough to avoid its consequences.",
                    List.of("Ask what fact they need others not to learn yet.", "Offer a corrective action in exchange for a truthful bounded account."),
                    List.of("Redirects attention from the failure point.", "Repairs visible evidence before discussing causes."),
                    Set.of("concealment", "failure", "evidence"),
                    List.of("They explain consequences before anyone has asked about causes.", "They volunteer solutions that conveniently avoid one location or record."),
                    "Concealment framing does not prove a lie, assign guilt, or grant the player lie detection."),
            motive("protect_the_secret", "Protect the Secret", MotiveFamily.CONCEALMENT,
                    "They believe some information must remain restricted because disclosure would create another danger.",
                    List.of("Ask what concrete harm disclosure could cause.", "Offer a narrower disclosure to the people who need the information."),
                    List.of("Shares partial operational facts while withholding origins.", "Tests who is present before speaking plainly."),
                    Set.of("concealment", "knowledge", "risk"),
                    List.of("They separate what you need to know from what they refuse to explain.", "Their caution is aimed at the audience as much as the question."),
                    "This does not establish that the secret is justified, true, supernatural, or canonically important."),
            motive("protect_someone_else", "Protect Someone Else", MotiveFamily.CONCEALMENT,
                    "They hide another person's involvement, weakness, location, or responsibility rather than their own.",
                    List.of("Ask what would happen to the protected person if the truth surfaced.", "Present evidence that protects the person without preserving the false account."),
                    List.of("Takes blame for details that do not fit cleanly.", "Avoids naming who supplied information or access."),
                    Set.of("concealment", "relationship", "protection"),
                    List.of("Their omissions cluster around one absent person.", "They accept suspicion more readily than they accept a direct question about someone else."),
                    "This does not prove loyalty, innocence, guilt, or a fixed relationship state."),
            motive("buy_time_with_silence", "Buy Time with Silence", MotiveFamily.CONCEALMENT,
                    "They withhold a fact because delay itself may change who can act on it or what options remain.",
                    List.of("Ask what changes if the truth waits another hour.", "Set a clear condition or deadline for disclosure."),
                    List.of("Answers around timing rather than substance.", "Waits for a route, person, or event before committing to a full account."),
                    Set.of("concealment", "delay", "timing"),
                    List.of("They seem to be waiting for something more than deciding what to say.", "Their silence has an endpoint even if they refuse to name it."),
                    "This does not implement hidden timers, persuasion thresholds, or guaranteed future disclosure."),

            motive("prove_the_better_path", "Prove the Better Path", MotiveFamily.RIVALRY,
                    "They want their plan, craft, route, faction, or judgement to outperform a rival alternative.",
                    List.of("Ask what evidence would make them admit the rival plan is better.", "Give them responsibility for a measurable part of a combined plan."),
                    List.of("Volunteers for comparisons that showcase competence.", "Undervalues proposals that strengthen a rival's standing."),
                    Set.of("rivalry", "status", "competence"),
                    List.of("They compare options by who proposed them as well as what they accomplish.", "A rival's success irritates them even when it helps the group."),
                    "Rivalry does not create reputation math, hostility, sabotage, or canonical faction behavior."),
            motive("settle_an_old_score", "Settle an Old Score", MotiveFamily.RIVALRY,
                    "They see the crisis as a chance to answer an earlier humiliation, injury, betrayal, or debt between peers.",
                    List.of("Ask what would actually count as the score being settled.", "Separate immediate survival from the older dispute."),
                    List.of("Takes unnecessary interest in a rival's setbacks.", "May choose personally satisfying leverage over cleaner cooperation."),
                    Set.of("rivalry", "retaliation", "history"),
                    List.of("The current argument carries details that clearly began before today.", "They react to the person before reacting to the proposal."),
                    "This does not establish canonical revenge logic, justified retaliation, or a damage/reward modifier."),
            motive("claim_the_credit", "Claim the Credit", MotiveFamily.RIVALRY,
                    "They want visible ownership of a success because status after the crisis matters to them.",
                    List.of("Offer explicit credit in exchange for concrete cooperation.", "Ask whether they would still support the plan if someone else were praised."),
                    List.of("Places themselves where witnesses can see the decisive work.", "Corrects accounts that understate their contribution."),
                    Set.of("rivalry", "status", "witness"),
                    List.of("They care who will remember the decision, not only whether it works.", "They keep track of who saw them act."),
                    "This is not a canonical reputation system or evidence that appraisal values recognition."),
            motive("deny_the_rival", "Deny the Rival", MotiveFamily.RIVALRY,
                    "They are willing to accept a lesser outcome if it prevents a rival from gaining decisive leverage.",
                    List.of("Ask what minimum outcome they can accept without empowering the rival.", "Propose a neutral custodian, route, or witness that neither side controls."),
                    List.of("Blocks exclusive access even when cooperation would be faster.", "Prefers shared or destroyed leverage over rival ownership."),
                    Set.of("rivalry", "denial", "leverage"),
                    List.of("They evaluate every asset by who would control it afterward.", "A solution becomes unacceptable the moment it gives one person sole leverage."),
                    "This does not assert faction alignment, universal zero-sum behavior, or canonical strategic scoring."),

            motive("debt_to_a_person", "Debt to a Person", MotiveFamily.OBLIGATION,
                    "They believe a past rescue, promise, gift, or kindness created a personal debt they have not repaid.",
                    List.of("Ask what repayment would satisfy the debt.", "Challenge whether the person owed would demand the present sacrifice."),
                    List.of("Accepts disproportionate risk for the person owed.", "Treats requests from that person as weightier than equivalent requests from strangers."),
                    Set.of("obligation", "relationship", "debt"),
                    List.of("They speak about help as repayment rather than generosity.", "One person's request carries unusual weight with them."),
                    "This does not create binding oaths, compulsion, debt currency, or canonical social rules."),
            motive("debt_to_the_dead", "Debt to the Dead", MotiveFamily.OBLIGATION,
                    "They act under a promise, responsibility, or unfinished obligation tied to someone who can no longer enforce it.",
                    List.of("Ask what the dead person actually asked for.", "Distinguish honoring the obligation from repeating the dead person's choices."),
                    List.of("Protects a task or person linked to the dead.", "Rejects easy abandonment when it would make the old promise meaningless."),
                    Set.of("obligation", "memory", "aftermath"),
                    List.of("They answer an absent person's expectations as if they were still present.", "A promise from before the crisis keeps shaping today's route."),
                    "This does not establish spirits, supernatural enforcement, or a canonical promise mechanic."),
            motive("owed_to_the_group", "Owed to the Group", MotiveFamily.OBLIGATION,
                    "They believe previous protection or belonging obliges them to carry an unfair share now.",
                    List.of("Ask what the group actually expects in return.", "Offer a contribution that repays the obligation without making them disposable."),
                    List.of("Volunteers for dangerous communal work.", "Defers personal claims when shared needs are visible."),
                    Set.of("obligation", "group", "duty"),
                    List.of("They frame sacrifice as payment for having been carried before.", "They are quicker to volunteer than to explain why."),
                    "This does not prove healthy loyalty, compulsory sacrifice, or a canonical cohort-bond system."),
            motive("promise_with_conditions", "Promise with Conditions", MotiveFamily.OBLIGATION,
                    "They are bound by a promise whose wording leaves real room for interpretation, conflict, or refusal.",
                    List.of("Ask for the promise's exact practical boundary.", "Find a path that satisfies the promise without assuming its broadest possible reading."),
                    List.of("Distinguishes what was actually promised from what others expect.", "Will cooperate strongly inside the promise and resist demands outside it."),
                    Set.of("obligation", "promise", "interpretation"),
                    List.of("They keep returning to the exact terms of what they agreed to do.", "Their refusal sounds narrower than simple disloyalty."),
                    "This does not implement magical oaths, perfect wording memory, or a canonical contract system."),

            motive("one_way_out", "One Way Out", MotiveFamily.DESPERATION,
                    "They believe only one remaining route, bargain, person, or resource can still save what matters to them.",
                    List.of("Test whether the supposed only option is actually unique.", "Offer a slower alternative that preserves a second fallback."),
                    List.of("Commits resources early to the favored option.", "Dismisses uncertain alternatives faster than they would under ordinary pressure."),
                    Set.of("desperation", "escape", "scarcity"),
                    List.of("They speak as though every alternative has already failed.", "They are ready to spend more than the option is objectively worth."),
                    "Desperation does not establish objective scarcity, hidden success odds, or a canonical last-chance mechanic."),
            motive("save_anyone", "Save Anyone", MotiveFamily.DESPERATION,
                    "They have abandoned the hope of a perfect outcome and now focus on preventing total loss.",
                    List.of("Ask which loss is still preventable right now.", "Offer a bounded rescue that does not pretend the larger disaster is solved."),
                    List.of("Prioritizes reachable survivors over ideal plans.", "Accepts incomplete outcomes that preserve someone or something concrete."),
                    Set.of("desperation", "rescue", "triage"),
                    List.of("They have stopped talking about victory and started counting what can still be saved.", "Their plans are smaller, immediate, and painfully concrete."),
                    "This does not define canonical triage rules, success criteria, or appraisal value for partial rescue."),
            motive("nothing_left_to_lose", "Nothing Left to Lose", MotiveFamily.DESPERATION,
                    "They believe the ordinary costs that once constrained them no longer matter.",
                    List.of("Identify a cost they have overlooked because it belongs to someone else.", "Offer an outcome that restores a reason to survive or preserve leverage."),
                    List.of("Accepts risks they previously rejected.", "Stops protecting status, property, or future relationships that once mattered."),
                    Set.of("desperation", "risk", "loss"),
                    List.of("Threats that once mattered no longer move them.", "They negotiate as though tomorrow has already been taken away."),
                    "This does not grant fear immunity, berserk behavior, or a canonical desperation power state."),
            motive("bargain_before_collapse", "Bargain Before Collapse", MotiveFamily.DESPERATION,
                    "They will accept terms they normally hate because delay may erase the possibility of any bargain at all.",
                    List.of("Separate the terms required now from concessions that can wait.", "Ask what they still refuse even under time pressure."),
                    List.of("Trades long-term advantage for immediate access or safety.", "Pushes for a decision before conditions worsen."),
                    Set.of("desperation", "bargain", "timing"),
                    List.of("They negotiate against the clock more than against you.", "The narrowing window is doing part of the persuading."),
                    "This does not implement persuasion math, guaranteed acceptance, or canonical timed-deal rules."),

            motive("two_people_one_promise", "Two People, One Promise", MotiveFamily.CONFLICTING_LOYALTY,
                    "They owe incompatible forms of loyalty to two people whose immediate interests now diverge.",
                    List.of("Ask what they owe each person separately.", "Find an action that preserves one duty without pretending the conflict disappears."),
                    List.of("Avoids choices that force an explicit side too early.", "May help both sides in limited ways until a decision becomes unavoidable."),
                    Set.of("loyalty", "relationship", "conflict"),
                    List.of("Every answer protects one person while qualifying what it means for the other.", "They delay naming a side even when their actions reveal the strain."),
                    "This does not select allegiance, force betrayal, or define a canonical loyalty-resolution formula."),
            motive("home_against_duty", "Home Against Duty", MotiveFamily.CONFLICTING_LOYALTY,
                    "Their obligation to a role, post, faction, or task conflicts with people or a place they consider home.",
                    List.of("Ask which part of the formal duty is truly non-negotiable.", "Offer a handoff that preserves the task while freeing them to protect home."),
                    List.of("Splits attention between assigned objectives and signs of danger to home.", "May resist orders that make the two loyalties mutually exclusive."),
                    Set.of("loyalty", "duty", "home"),
                    List.of("Formal orders never fully pull their attention away from home.", "They keep measuring the task against what it may cost elsewhere."),
                    "This does not establish faction hierarchy, disobedience rules, or canonical home-bond mechanics."),
            motive("truth_against_kin", "Truth Against Kin", MotiveFamily.CONFLICTING_LOYALTY,
                    "They possess or suspect evidence that could protect the wider group while harming someone close to them.",
                    List.of("Ask what can be verified independently before naming the person.", "Offer a process that separates evidence preservation from immediate punishment."),
                    List.of("Protects evidence but hesitates over attribution.", "Seeks ways to reduce the cost to kin without erasing the underlying fact."),
                    Set.of("loyalty", "evidence", "relationship"),
                    List.of("They handle the evidence carefully but become guarded when attribution begins.", "Their conflict is not whether the fact matters, but what naming it will do."),
                    "This does not prove the evidence true, create lie detection, or force a betrayal outcome."),
            motive("survivors_on_both_sides", "Survivors on Both Sides", MotiveFamily.CONFLICTING_LOYALTY,
                    "They care about people caught on opposing sides and resist plans that treat one entire side as expendable.",
                    List.of("Identify which people are actually part of the immediate threat.", "Design passage, warning, or containment around civilians rather than faction labels."),
                    List.of("Challenges collective punishment.", "Looks for separations between combatants, authorities, and bystanders."),
                    Set.of("loyalty", "civilian", "mercy"),
                    List.of("They keep naming individuals where others speak only about sides.", "They object when a plan turns affiliation into a complete moral category."),
                    "This does not impose a moral canon, guarantee peaceful resolution, or define appraisal consequences." )
    );

    private static MotivePrimitive motive(
            String id,
            String title,
            MotiveFamily family,
            String motiveRead,
            List<String> dialogueHooks,
            List<String> behaviorHooks,
            Set<String> affinityTags,
            List<String> presentationCues,
            String antiOverclaimBoundary
    ) {
        return new MotivePrimitive(id, title, family, motiveRead, dialogueHooks, behaviorHooks, affinityTags, presentationCues, antiOverclaimBoundary);
    }

    public static List<MotivePrimitive> waveOne() {
        return WAVE_ONE;
    }

    public static Composition compose(
            long seed,
            String historicalRoleId,
            Set<MotiveFamily> allowedFamilies,
            Map<String, Integer> evidence
    ) {
        if (historicalRoleId == null || historicalRoleId.isBlank()) {
            throw new IllegalArgumentException("historicalRoleId must be non-blank");
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

        EnumSet<MotiveFamily> familyCopy = EnumSet.copyOf(allowedFamilies);
        List<MotivePrimitive> candidates = WAVE_ONE.stream()
                .filter(primitive -> familyCopy.contains(primitive.family()))
                .toList();
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("no motive primitives exist for allowed families");
        }

        Set<String> positiveTags = evidence.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());

        int bestMatch = candidates.stream()
                .mapToInt(primitive -> matchedTags(primitive, positiveTags))
                .max()
                .orElse(0);
        List<MotivePrimitive> preferred = bestMatch == 0
                ? candidates
                : candidates.stream().filter(primitive -> matchedTags(primitive, positiveTags) == bestMatch).toList();

        String familyKey = familyCopy.stream().map(Enum::name).sorted().reduce((left, right) -> left + "," + right).orElse("");
        String evidenceKey = positiveTags.stream().sorted().reduce((left, right) -> left + "," + right).orElse("");
        long primitiveHash = hash64(GENERATOR_VERSION + "|primitive|" + seed + "|" + historicalRoleId + "|" + familyKey + "|" + evidenceKey);
        MotivePrimitive selected = preferred.get(index(primitiveHash, preferred.size()));
        long cueHash = hash64(GENERATOR_VERSION + "|cue|" + seed + "|" + historicalRoleId + "|" + selected.id());
        String cue = selected.presentationCues().get(index(cueHash, selected.presentationCues().size()));

        return new Composition(
                historicalRoleId,
                selected,
                cue,
                matchedTags(selected, positiveTags),
                seed,
                GENERATOR_VERSION
        );
    }

    public static Map<MotiveFamily, List<MotivePrimitive>> byFamily() {
        Map<MotiveFamily, List<MotivePrimitive>> result = new LinkedHashMap<>();
        for (MotiveFamily family : MotiveFamily.values()) {
            List<MotivePrimitive> primitives = new ArrayList<>(WAVE_ONE.stream().filter(value -> value.family() == family).toList());
            primitives.sort(Comparator.comparing(MotivePrimitive::id));
            result.put(family, List.copyOf(primitives));
        }
        return Map.copyOf(result);
    }

    private static int matchedTags(MotivePrimitive primitive, Set<String> positiveTags) {
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
