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

/** DESIGN-only presentation for bounded questions around already-authorized Nightmare faction context. */
public final class NightmareFactionCurrentIntentCheckCatalog {
    public static final String GENERATOR_VERSION = "nightmare-faction-current-intent-check-v1";

    private NightmareFactionCurrentIntentCheckCatalog() {}

    public enum CheckFamily {
        CLARIFY_PRESENT_GOAL,
        VERIFY_CURRENT_ACCESS,
        REVISIT_OPEN_MATTER,
        DECLINE_RENEWED_TERMS
    }

    public record Primitive(String id, CheckFamily family, String title, String situationRead,
                            String playerAsk, List<String> followUpOptions, Set<String> affinityTags,
                            List<String> presentationCues, String antiOverclaimBoundary) {
        public Primitive {
            id = stableId(id);
            family = Objects.requireNonNull(family, "family");
            title = text(title, "title");
            situationRead = text(situationRead, "situationRead");
            playerAsk = text(playerAsk, "playerAsk");
            followUpOptions = exactTextList(followUpOptions, 3, "followUpOptions");
            affinityTags = nonEmptyTags(affinityTags);
            presentationCues = exactTextList(presentationCues, 2, "presentationCues");
            antiOverclaimBoundary = text(antiOverclaimBoundary, "antiOverclaimBoundary");
        }
    }

    public record Selection(String generatorVersion, long seed, String scenarioId, String factionId,
                            String reencounterContextId, String interactionId, Set<CheckFamily> allowedFamilies,
                            Primitive primitive, String presentationCue, Set<String> matchedEvidenceTags) {
        public Selection {
            generatorVersion = text(generatorVersion, "generatorVersion");
            scenarioId = opaqueId(scenarioId, "scenarioId");
            factionId = opaqueId(factionId, "factionId");
            reencounterContextId = opaqueId(reencounterContextId, "reencounterContextId");
            interactionId = opaqueId(interactionId, "interactionId");
            allowedFamilies = checkedFamilies(allowedFamilies);
            primitive = Objects.requireNonNull(primitive, "primitive");
            if (!allowedFamilies.contains(primitive.family())) {
                throw new IllegalArgumentException("primitive family must be caller-allowed");
            }
            presentationCue = text(presentationCue, "presentationCue");
            matchedEvidenceTags = Set.copyOf(Objects.requireNonNull(matchedEvidenceTags, "matchedEvidenceTags"));
        }
    }

    public static List<Primitive> waveOne() {
        return List.of(
                p("goal_name_immediate", CheckFamily.CLARIFY_PRESENT_GOAL, "Name the Immediate Goal",
                        "The faction is present again, but its current purpose is not supplied by the old record alone.",
                        "What are you trying to accomplish here, right now?",
                        "Ask what must happen first.", "Ask what is outside that goal.", "Leave motive open beyond the answer.",
                        Set.of("goal", "current", "scope"),
                        "The question is framed around the present meeting rather than the old one.", "Any answer is recorded as a current statement, not a relationship verdict.",
                        "A stated goal cannot prove hidden motive, truth, trust, allegiance, reputation, future behavior, or scenario resolution."),
                p("goal_scope_boundary", CheckFamily.CLARIFY_PRESENT_GOAL, "Clarify the Goal's Boundary",
                        "A current aim can be useful without being treated as the faction's complete purpose or policy.",
                        "What does this goal include, and what does it not include?",
                        "Ask who the goal concerns.", "Ask where its scope ends.", "Keep unstated aims unresolved.",
                        Set.of("goal", "scope", "boundary"),
                        "The current goal is shown with a visible scope boundary.", "Unanswered scope remains marked as open rather than inferred.",
                        "Scope clarification cannot establish faction-wide policy, hidden intent, deception, allegiance, reputation, access, ownership, or future conduct."),
                p("goal_priority_now", CheckFamily.CLARIFY_PRESENT_GOAL, "Ask What Matters First",
                        "Several concerns may exist, but only the faction's current stated priority is being checked.",
                        "Which matter comes first for you in this encounter?",
                        "Ask what can wait.", "Ask what would change the priority.", "Do not turn priority into permanent loyalty.",
                        Set.of("goal", "priority", "current"),
                        "The answer is presented as a current priority rather than a permanent faction trait.", "Other concerns remain visible as unresolved context.",
                        "A current priority cannot create obligation, loyalty, hostility, trust, reputation, resource authority, or a guaranteed later decision."),
                p("goal_reason_without_truth", CheckFamily.CLARIFY_PRESENT_GOAL, "Ask for the Stated Reason",
                        "The faction can give a reason for its present goal without that reason becoming verified truth.",
                        "Why is that your goal now?",
                        "Record the reason as their statement.", "Ask what evidence they rely on.", "Preserve disagreement if the reason is contested.",
                        Set.of("goal", "reason", "evidence"),
                        "The stated reason is visually separated from verified observations.", "Contradicting evidence can sit beside it without an automatic lie marker.",
                        "A stated reason cannot certify truth, innocence, guilt, legitimacy, persuasion success, allegiance, reputation, or hidden motive."),

                p("access_still_current", CheckFamily.VERIFY_CURRENT_ACCESS, "Verify Access Is Still Current",
                        "Prior passage or permission may exist in history, but current access must be supplied by present state.",
                        "Does that access still apply now?",
                        "Ask which route or place it covers.", "Ask whether any condition changed.", "Treat silence as unresolved, not permission.",
                        Set.of("access", "current", "verification"),
                        "Historical access and current access are shown as separate entries.", "No route unlock is implied by the question itself.",
                        "Verification text cannot grant access, establish ownership or territorial legitimacy, guarantee route safety, or change world state."),
                p("access_scope_now", CheckFamily.VERIFY_CURRENT_ACCESS, "Verify the Current Scope",
                        "An access statement may be bounded by place, purpose, people, or time without defining broader authority.",
                        "Exactly what may be entered or used under the current terms?",
                        "Ask for the named boundary.", "Ask who is included.", "Keep unrelated areas outside the assumption.",
                        Set.of("access", "scope", "boundary"),
                        "The access statement is displayed beside a bounded scope marker.", "Unmentioned areas remain visually outside the current permission.",
                        "A bounded access statement cannot prove ownership, universal permission, safe passage, faction control, reputation, allegiance, or future access."),
                p("access_condition_check", CheckFamily.VERIFY_CURRENT_ACCESS, "Check the Access Condition",
                        "Current access can depend on a stated condition without the presentation deciding whether that condition has been met.",
                        "What condition applies before access is recognized?",
                        "Ask who evaluates the condition.", "Ask what evidence is relevant.", "Leave fulfillment to the actual current state.",
                        Set.of("access", "condition", "evidence"),
                        "The stated condition appears without a completed check mark.", "Relevant evidence can be linked without converting it into automatic authorization.",
                        "A displayed condition cannot decide fulfillment, unlock terrain, transfer resources, prove truth, calculate persuasion, or accept a scenario event."),
                p("access_decline_assumption", CheckFamily.VERIFY_CURRENT_ACCESS, "Do Not Assume the Old Route",
                        "A previously available route is history until current access is confirmed for this encounter.",
                        "Should we treat the old route as available now, or verify it again first?",
                        "Ask for current confirmation.", "Choose another known option if available.", "Leave the route status unresolved if no answer exists.",
                        Set.of("access", "route", "history"),
                        "The old route is marked as historical rather than currently open.", "Alternative planning remains available without declaring the route closed.",
                        "Refusing to assume access cannot establish that a route is blocked, unsafe, hostile, owned, trapped, or permanently unavailable."),

                p("open_matter_exact_point", CheckFamily.REVISIT_OPEN_MATTER, "Return to One Open Point",
                        "One bounded matter can be revisited without importing the whole previous relationship into the new exchange.",
                        "Can we return to the one point that remained unsettled?",
                        "Restate only that point.", "Ask whether new information exists.", "Leave unrelated history outside the discussion.",
                        Set.of("open", "matter", "scope"),
                        "Only the selected open point is pinned into the current conversation.", "Other prior disputes remain in history rather than becoming active by association.",
                        "Revisiting one matter cannot reactivate old terms, prove obligation, establish blame, change reputation, or settle unrelated disputes."),
                p("open_matter_new_information", CheckFamily.REVISIT_OPEN_MATTER, "Ask What Changed the Open Matter",
                        "An unresolved matter may have new context, but the presentation does not invent that context on its own.",
                        "Has anything changed that bears directly on the unresolved point?",
                        "Ask for the specific change.", "Ask for its source.", "Keep the matter open if nothing new is established.",
                        Set.of("open", "change", "evidence"),
                        "New information is attached to the existing open point without rewriting its history.", "No change is assumed until a current statement or observation exists.",
                        "A claimed change cannot prove truth, guilt, motive, obligation, access, allegiance, reputation, or the final meaning of the dispute."),
                p("open_matter_current_position", CheckFamily.REVISIT_OPEN_MATTER, "Ask the Current Position",
                        "The old record tells you the matter was unresolved, not what either side's position must be now.",
                        "What is your position on that matter now?",
                        "Record the current answer separately.", "Compare it with the old record.", "Keep intent unresolved beyond what was actually said.",
                        Set.of("open", "position", "current"),
                        "Past and present positions are shown side by side.", "A changed answer is highlighted without an automatic deception label.",
                        "A current position cannot establish truth, bad faith, trust, hostility, allegiance, reputation, binding terms, or future action."),
                p("open_matter_leave_open", CheckFamily.REVISIT_OPEN_MATTER, "Leave the Matter Open",
                        "Re-encounter does not require every inherited question to be settled immediately.",
                        "Do we leave this point unresolved for now?",
                        "Record that no new decision was reached.", "Preserve any new evidence separately.", "Return to the present issue if needed.",
                        Set.of("open", "defer", "uncertainty"),
                        "The open point remains visible without becoming a failure marker.", "The current conversation can continue on another bounded issue.",
                        "Leaving a matter open cannot imply guilt, agreement, refusal, hostility, surrender, reputation change, or a guaranteed future chance to settle it."),

                p("decline_terms_no_assumption", CheckFamily.DECLINE_RENEWED_TERMS, "Decline Renewed Terms",
                        "A new or renewed proposal can be refused without automatically redefining the whole relationship.",
                        "No. I am not accepting those terms now.",
                        "Ask whether the conversation can continue without them.", "State which term you are declining.", "End the exchange if no other matter remains.",
                        Set.of("decline", "terms", "boundary"),
                        "The declined terms are marked separately from the broader faction history.", "No hostility meter or betrayal marker is added by presentation.",
                        "Declining terms cannot itself create hostility, betrayal, reputation loss, allegiance change, punishment, combat, or terminal resolution."),
                p("decline_access_exchange", CheckFamily.DECLINE_RENEWED_TERMS, "Decline the Access Exchange",
                        "Access tied to a proposed exchange can be declined without the presentation deciding the route's physical state.",
                        "I am not accepting that exchange for access.",
                        "Ask whether another current route exists.", "Leave access unresolved.", "Disengage from the access discussion.",
                        Set.of("decline", "access", "exchange"),
                        "The proposed exchange is shown as declined while route state remains separate.", "Alternative options remain unfilled unless current state supplies them.",
                        "Declining an access exchange cannot close terrain, prove no alternative exists, establish hostility, spend resources, or mutate world access."),
                p("decline_obligation_scope", CheckFamily.DECLINE_RENEWED_TERMS, "Decline the Broader Obligation",
                        "A proposal can reach beyond the bounded matter the player is willing to discuss.",
                        "I will not take on that broader obligation.",
                        "Offer to discuss a narrower matter.", "Ask what happens if no agreement is reached.", "Leave without accepting an obligation.",
                        Set.of("decline", "obligation", "scope"),
                        "The declined obligation is shown with its stated scope.", "Any narrower discussion is treated as a separate current proposal.",
                        "Refusing an obligation cannot establish debt, guilt, dishonor, hostility, reputation change, allegiance, coercion success, or scenario failure."),
                p("decline_without_verdict", CheckFamily.DECLINE_RENEWED_TERMS, "Decline Without a Verdict",
                        "The player can refuse current terms without pretending to know the faction's hidden motive or future reaction.",
                        "I am declining this offer; I am not making a claim about your intent.",
                        "Keep the refusal scoped to the offer.", "Ask whether another issue remains.", "Leave future reaction unresolved.",
                        Set.of("decline", "uncertainty", "relationship"),
                        "The refusal is logged as a current decision rather than a relationship verdict.", "Future stance remains blank until supplied by later state.",
                        "A scoped refusal cannot prove motive, friendship, hostility, trust, allegiance, reputation, future behavior, or whether another offer will exist."));
    }

    public static Selection compose(long seed, String scenarioId, String factionId, String reencounterContextId,
                                    String interactionId, Set<CheckFamily> allowedFamilies, Map<String, Integer> evidence) {
        String checkedScenario = opaqueId(scenarioId, "scenarioId");
        String checkedFaction = opaqueId(factionId, "factionId");
        String checkedContext = opaqueId(reencounterContextId, "reencounterContextId");
        String checkedInteraction = opaqueId(interactionId, "interactionId");
        Set<CheckFamily> checkedAllowed = checkedFamilies(allowedFamilies);
        Set<String> positiveEvidence = positiveEvidence(evidence);

        List<Primitive> candidates = waveOne().stream()
                .filter(primitive -> checkedAllowed.contains(primitive.family()))
                .sorted(Comparator.comparing(Primitive::id))
                .toList();
        if (candidates.isEmpty()) throw new IllegalArgumentException("no faction current-intent check for supplied families");

        int bestMatch = candidates.stream().mapToInt(p -> overlap(p.affinityTags(), positiveEvidence)).max().orElse(0);
        List<Primitive> preferred = bestMatch > 0
                ? candidates.stream().filter(p -> overlap(p.affinityTags(), positiveEvidence) == bestMatch).toList()
                : candidates;

        String authorityKey = checkedScenario + "|" + checkedFaction + "|" + checkedContext + "|"
                + checkedInteraction + "|" + checkedAllowed.stream().map(Enum::name).sorted().collect(Collectors.joining(",")) + "|"
                + positiveEvidence.stream().sorted().collect(Collectors.joining(","));
        Primitive primitive = preferred.get(index(seed, authorityKey + "|primitive", preferred.size()));
        String cue = primitive.presentationCues().get(index(seed,
                authorityKey + "|" + primitive.id() + "|cue", primitive.presentationCues().size()));
        Set<String> matched = primitive.affinityTags().stream().filter(positiveEvidence::contains).sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new Selection(GENERATOR_VERSION, seed, checkedScenario, checkedFaction, checkedContext,
                checkedInteraction, checkedAllowed, primitive, cue, matched);
    }

    public static Primitive requirePrimitive(String id) {
        String checked = stableId(id);
        return waveOne().stream().filter(p -> p.id().equals(checked)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown faction current-intent check primitive: " + checked));
    }

    private static Primitive p(String id, CheckFamily family, String title, String situationRead,
                               String playerAsk, String follow1, String follow2, String follow3,
                               Set<String> affinityTags, String cue1, String cue2, String antiOverclaimBoundary) {
        return new Primitive(id, family, title, situationRead, playerAsk,
                List.of(follow1, follow2, follow3), affinityTags, List.of(cue1, cue2), antiOverclaimBoundary);
    }

    private static Set<CheckFamily> checkedFamilies(Set<CheckFamily> families) {
        Objects.requireNonNull(families, "allowedFamilies");
        if (families.isEmpty()) throw new IllegalArgumentException("allowedFamilies must not be empty");
        if (families.contains(null)) throw new NullPointerException("allowedFamilies contains null");
        return Set.copyOf(families);
    }

    private static Set<String> positiveEvidence(Map<String, Integer> evidence) {
        Objects.requireNonNull(evidence, "evidence");
        List<String> tags = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : evidence.entrySet()) {
            String tag = stableId(entry.getKey());
            Integer value = Objects.requireNonNull(entry.getValue(), "evidence value");
            if (value < 0) throw new IllegalArgumentException("evidence cannot be negative");
            if (value > 0) tags.add(tag);
        }
        tags.sort(String::compareTo);
        return new LinkedHashSet<>(tags);
    }

    private static int overlap(Set<String> left, Set<String> right) {
        int count = 0;
        for (String tag : left) if (right.contains(tag)) count++;
        return count;
    }

    private static int index(long seed, String key, int bound) {
        if (bound <= 0) throw new IllegalArgumentException("bound must be positive");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(ByteBuffer.allocate(Long.BYTES).putLong(seed).array());
            byte[] bytes = digest.digest(key.getBytes(StandardCharsets.UTF_8));
            int raw = ByteBuffer.wrap(bytes, 0, Integer.BYTES).getInt();
            return Math.floorMod(raw, bound);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

    private static String stableId(String value) {
        String normalized = text(value, "id").toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z0-9][a-z0-9_-]*")) throw new IllegalArgumentException("invalid stable id: " + value);
        return normalized;
    }

    private static String opaqueId(String value, String field) {
        return text(value, field);
    }

    private static String text(String value, String field) {
        Objects.requireNonNull(value, field);
        if (value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
        return value;
    }

    private static List<String> exactTextList(List<String> values, int expected, String field) {
        Objects.requireNonNull(values, field);
        if (values.size() != expected) throw new IllegalArgumentException(field + " must contain exactly " + expected + " entries");
        List<String> checked = values.stream().map(value -> text(value, field + " entry")).toList();
        if (new LinkedHashSet<>(checked).size() != checked.size()) throw new IllegalArgumentException(field + " entries must be unique");
        return checked;
    }

    private static Set<String> nonEmptyTags(Set<String> values) {
        Objects.requireNonNull(values, "affinityTags");
        if (values.isEmpty()) throw new IllegalArgumentException("affinityTags must not be empty");
        return values.stream().map(NightmareFactionCurrentIntentCheckCatalog::stableId)
                .sorted().collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
