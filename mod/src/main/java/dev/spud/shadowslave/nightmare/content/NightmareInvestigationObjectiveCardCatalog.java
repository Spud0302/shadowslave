package dev.spud.shadowslave.nightmare.content;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** DESIGN-only presentation cards for Java-resolved investigation objective state. */
public final class NightmareInvestigationObjectiveCardCatalog {
    public static final String GENERATOR_VERSION = "nightmare-investigation-objective-card-v1";

    private NightmareInvestigationObjectiveCardCatalog() {}

    public enum State { ACTIVE, DEFERRED, BLOCKED, COMPLETED }

    public record Primitive(String id, State state, String title, String statusRead, String nextPrompt,
                            List<String> playerOptions, Set<String> affinityTags, List<String> presentationCues,
                            String antiOverclaimBoundary) {
        public Primitive {
            id = stableId(id);
            state = Objects.requireNonNull(state, "state");
            title = text(title, "title");
            statusRead = text(statusRead, "statusRead");
            nextPrompt = text(nextPrompt, "nextPrompt");
            playerOptions = exactTextList(playerOptions, 3, "playerOptions");
            affinityTags = nonEmptyTags(affinityTags);
            presentationCues = exactTextList(presentationCues, 2, "presentationCues");
            antiOverclaimBoundary = text(antiOverclaimBoundary, "antiOverclaimBoundary");
        }
    }

    public record Selection(String generatorVersion, long seed, String scenarioId, String actorContextId,
                            String planId, String objectiveId, State state, Primitive primitive,
                            String presentationCue, Set<String> matchedEvidenceTags) {
        public Selection {
            generatorVersion = text(generatorVersion, "generatorVersion");
            scenarioId = opaqueId(scenarioId, "scenarioId");
            actorContextId = opaqueId(actorContextId, "actorContextId");
            planId = opaqueId(planId, "planId");
            objectiveId = opaqueId(objectiveId, "objectiveId");
            state = Objects.requireNonNull(state, "state");
            primitive = Objects.requireNonNull(primitive, "primitive");
            if (primitive.state() != state) throw new IllegalArgumentException("primitive state must match caller-owned state");
            presentationCue = text(presentationCue, "presentationCue");
            matchedEvidenceTags = Set.copyOf(Objects.requireNonNull(matchedEvidenceTags, "matchedEvidenceTags"));
        }
    }

    public static List<Primitive> waveOne() {
        return List.of(
                p("active_recheck_anchor", State.ACTIVE, "Recheck the Anchor", "The objective remains open because one fixed reference can still be checked directly.", "What can be verified next without deciding the whole investigation?", "Return to the fixed reference.", "Record only observable changes.", "Keep an exit route available.", Set.of("observation", "location", "verification"), "The card highlights one bounded next check.", "Earlier notes remain visible beside the new observation.", "ACTIVE presentation does not guarantee the route is safe, current, or completable."),
                p("active_compare_records", State.ACTIVE, "Compare the Records", "Two retained records overlap enough to justify comparison, but neither is granted hidden authority.", "Where is the first concrete agreement or divergence?", "Align shared details.", "Mark the first conflict.", "Seek one independent anchor.", Set.of("record", "contradiction", "verification"), "Shared facts and disputed details are separated visually.", "The card keeps source identity attached to each claim.", "Comparison does not adjudicate truth, guilt, authenticity, or scenario outcome."),
                p("active_seek_source", State.ACTIVE, "Seek Another Source", "A missing fact is narrow enough to pursue through another bounded source.", "Who could directly observe or preserve the missing fact?", "Find a direct witness or record.", "Ask one narrow question.", "Stop if only speculation is available.", Set.of("source", "gap", "testimony"), "The card names the missing fact instead of a presumed answer.", "Potential sources remain suggestions rather than quest authority.", "ACTIVE does not imply the source exists, will cooperate, or knows the truth."),
                p("active_test_route", State.ACTIVE, "Test One Route Segment", "A route clue is actionable enough for one reversible check, not enough for a safety verdict.", "What is the smallest segment that can be tested while preserving retreat?", "Check the next fixed marker.", "Record obstruction or passage.", "Withdraw on incompatible conditions.", Set.of("route", "location", "uncertainty"), "Only the next segment is emphasized.", "Retreat remains a first-class option on the card.", "A successful local check does not guarantee the wider route, destination, or future conditions."),

                p("deferred_missing_fact", State.DEFERRED, "Waiting on a Missing Fact", "The objective is intentionally paused until a named piece of information changes or becomes available.", "What exact observation should reopen this objective?", "Keep the resume condition visible.", "Pursue another objective.", "Record the cost of waiting.", Set.of("gap", "defer", "uncertainty"), "The resume condition is shown beside the paused objective.", "The card distinguishes waiting from abandonment.", "DEFERRED does not freeze the world or guarantee the missing fact will appear."),
                p("deferred_unsafe_test", State.DEFERRED, "Unsafe to Test Now", "The current verification method would create disproportionate danger or destroy the evidence it is meant to examine.", "What lower-risk condition would justify reopening the test?", "Preserve the question.", "Seek a lower-risk method.", "Work another lead meanwhile.", Set.of("safety", "preservation", "defer"), "The hazard is shown as a reason for pause, not as proof of the claim.", "The unresolved question remains attached to the card.", "Declining a hazardous test neither proves nor disproves the underlying claim."),
                p("deferred_stale_route", State.DEFERRED, "Route Information Is Stale", "The available route record is too condition-dependent or old to justify immediate travel.", "What fresh reference would make the route actionable again?", "Seek a current landmark check.", "Use another route plan.", "Retain the old record for comparison.", Set.of("route", "record", "defer"), "Historical usefulness remains visible without masquerading as live navigation.", "The card names freshness as the missing condition.", "DEFERRED does not mean the route is closed, dangerous, or permanently invalid."),
                p("deferred_social_pressure", State.DEFERRED, "Conclusion Deferred", "Pressure for an accusation or commitment exceeds what the recorded evidence can support.", "What can be stated now without turning uncertainty into a verdict?", "State only established facts.", "Name the unresolved point.", "Return when new evidence exists.", Set.of("testimony", "authority", "uncertainty"), "The card separates urgency from evidentiary authority.", "No suspect or faction is visually marked as guilty.", "Deferral does not settle allegiance, protect anyone from future consequences, or block Java-owned events."),

                p("blocked_missing_access", State.BLOCKED, "Access Not Available", "Java-owned scenario state says this objective cannot currently proceed through the required place, person, or object.", "What alternative can be pursued without inventing access?", "Seek another route or source.", "Protect existing evidence.", "Leave the blocker recorded.", Set.of("access", "route", "blocked"), "The blocker is presented as a current constraint, not a permanent world rule.", "Alternative leads remain visible when authored by the caller.", "BLOCKED presentation cannot create, remove, or predict access conditions."),
                p("blocked_source_unavailable", State.BLOCKED, "Source Unavailable", "The required witness, record, or signal source is not currently reachable under Java-owned scenario state.", "What can be preserved or compared while the source remains unavailable?", "Preserve current notes.", "Seek an independent source.", "Wait for an authoritative state change.", Set.of("source", "blocked", "preservation"), "The unavailable source remains identified without implying why it is unavailable.", "The card keeps alternative evidence separate.", "Source unavailability does not imply death, guilt, refusal, deception, or future availability."),
                p("blocked_condition", State.BLOCKED, "Condition Prevents Progress", "An authored environmental or structural condition currently prevents the next investigation step.", "Which observable change would allow Java-owned scenario logic to reconsider progress?", "Observe the condition.", "Pursue another lead.", "Preserve a resume note.", Set.of("environment", "blocked", "observation"), "The card names the present condition without forecasting its duration.", "A resume note can be shown without becoming a timer.", "BLOCKED does not predict weather, collapse timing, creature movement, or automatic reopening."),
                p("blocked_authority", State.BLOCKED, "Authority Has Not Accepted the Step", "The attempted investigation step has not been accepted by Java-owned scenario authority.", "What prerequisite or alternative can be examined without forcing progress?", "Review known prerequisites.", "Try another authored lead.", "Keep the rejected step recorded.", Set.of("authority", "blocked", "verification"), "The card presents rejection without inventing hidden reasons.", "Known prerequisites can be displayed only when supplied by Java state.", "Presentation cannot accept a ResolutionGraph event, infer a hidden prerequisite, or override scenario state."),

                p("completed_observation", State.COMPLETED, "Observation Recorded", "Java-owned objective state marks this bounded observation step complete.", "What should the player carry forward from this completed step?", "Review the recorded observation.", "Follow an authored next lead.", "Leave the record unchanged.", Set.of("observation", "record", "completed"), "The completed step is visually settled while its evidence remains inspectable.", "Follow-up leads are shown separately from completion.", "COMPLETED means this objective step ended; it does not certify truth, scenario resolution, appraisal, or reward."),
                p("completed_comparison", State.COMPLETED, "Comparison Completed", "The requested bounded comparison has been performed and retained by Java-owned investigation state.", "Which agreement, conflict, or remaining gap should inform the next authored objective?", "Review agreements.", "Review contradictions.", "Preserve unresolved gaps.", Set.of("record", "contradiction", "completed"), "Results are grouped as agreements, conflicts, and gaps rather than winners and losers.", "The card preserves source labels after completion.", "Completing a comparison does not determine which source is true or who is responsible."),
                p("completed_route_check", State.COMPLETED, "Route Check Recorded", "Java-owned state marks the requested route check complete for the conditions that were actually observed.", "What route fact is now recorded, and what remains outside its scope?", "Review the checked segment.", "Record condition limits.", "Choose another authored plan.", Set.of("route", "location", "completed"), "The checked segment is separated from unchecked terrain.", "Condition limits remain attached to the completed record.", "A completed route check does not guarantee future safety, wider access, or Nightmare completion."),
                p("completed_preservation", State.COMPLETED, "Evidence Preserved", "The requested preservation step is complete under Java-owned investigation state.", "What provenance or handling limit should remain visible with the preserved material?", "Review the preserved item or account.", "Keep handling notes attached.", "Continue with another authored objective.", Set.of("preservation", "record", "completed"), "The card confirms the preservation action without upgrading evidentiary authority.", "Handling limits remain visible after completion.", "Preservation completion does not certify authenticity, ownership, chain of custody, guilt, or appraisal credit.")
        );
    }

    public static Selection compose(long seed, String scenarioId, String actorContextId, String planId,
                                    String objectiveId, State state, Map<String, Integer> evidence) {
        String scenario = opaqueId(scenarioId, "scenarioId");
        String actor = opaqueId(actorContextId, "actorContextId");
        String plan = opaqueId(planId, "planId");
        String objective = opaqueId(objectiveId, "objectiveId");
        State resolvedState = Objects.requireNonNull(state, "state");
        Set<String> positive = positiveEvidenceTags(evidence);
        List<Primitive> eligible = waveOne().stream().filter(p -> p.state() == resolvedState).toList();
        List<Primitive> preferred = eligible.stream().filter(p -> p.affinityTags().stream().anyMatch(positive::contains)).toList();
        List<Primitive> pool = preferred.isEmpty() ? eligible : preferred;
        Primitive primitive = pool.get(index(seed, scenario, actor, plan, objective, resolvedState.name(), "primitive", pool.size()));
        String cue = primitive.presentationCues().get(index(seed, scenario, actor, plan, objective, primitive.id(), "cue", 2));
        Set<String> matched = primitive.affinityTags().stream().filter(positive::contains).collect(Collectors.toUnmodifiableSet());
        return new Selection(GENERATOR_VERSION, seed, scenario, actor, plan, objective, resolvedState, primitive, cue, matched);
    }

    public static Primitive byId(String id) {
        String stable = stableId(id);
        return waveOne().stream().filter(p -> p.id().equals(stable)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown investigation objective card: " + stable));
    }

    private static Primitive p(String id, State state, String title, String read, String prompt,
                               String a, String b, String c, Set<String> tags, String cue1, String cue2, String boundary) {
        return new Primitive(id, state, title, read, prompt, List.of(a, b, c), tags, List.of(cue1, cue2), boundary);
    }

    private static Set<String> positiveEvidenceTags(Map<String, Integer> evidence) {
        Objects.requireNonNull(evidence, "evidence");
        return evidence.entrySet().stream().map(entry -> {
            String key = stableId(entry.getKey());
            Integer value = Objects.requireNonNull(entry.getValue(), "evidence value");
            if (value < 0) throw new IllegalArgumentException("evidence values must not be negative");
            return Map.entry(key, value);
        }).filter(entry -> entry.getValue() > 0).map(Map.Entry::getKey).collect(Collectors.toUnmodifiableSet());
    }

    private static int index(long seed, String a, String b, String c, String d, String e, String discriminator, int bound) {
        if (bound <= 0) throw new IllegalArgumentException("bound must be positive");
        String material = GENERATOR_VERSION + "|" + seed + "|" + a + "|" + b + "|" + c + "|" + d + "|" + e + "|" + discriminator;
        byte[] digest;
        try {
            digest = MessageDigest.getInstance("SHA-256").digest(material.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 unavailable", impossible);
        }
        long value = 0L;
        for (int i = 0; i < 8; i++) value = (value << 8) | Byte.toUnsignedLong(digest[i]);
        return (int) Long.remainderUnsigned(value, bound);
    }

    private static String opaqueId(String value, String field) {
        return text(value, field);
    }

    private static String stableId(String value) {
        String normalized = text(value, "id").toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z0-9][a-z0-9_-]*")) throw new IllegalArgumentException("invalid stable id: " + value);
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
        return values.stream().map(value -> text(value, field)).toList();
    }

    private static Set<String> nonEmptyTags(Set<String> tags) {
        Objects.requireNonNull(tags, "affinityTags");
        Set<String> normalized = tags.stream().map(NightmareInvestigationObjectiveCardCatalog::stableId).collect(Collectors.toUnmodifiableSet());
        if (normalized.isEmpty()) throw new IllegalArgumentException("affinityTags must not be empty");
        return normalized;
    }
}
