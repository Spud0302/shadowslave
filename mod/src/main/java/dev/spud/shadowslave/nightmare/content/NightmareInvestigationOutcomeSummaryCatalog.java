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

/** DESIGN-only summaries for already-resolved Java-owned investigation outcomes. */
public final class NightmareInvestigationOutcomeSummaryCatalog {
    public static final String GENERATOR_VERSION = "nightmare-investigation-outcome-summary-v1";

    private NightmareInvestigationOutcomeSummaryCatalog() {}

    public enum OutcomeState { FOUND, LEFT_UNRESOLVED, PRESERVED, ABANDONED }

    public record Primitive(String id, OutcomeState state, String title, String outcomeRead, String carryForward,
                            List<String> playerReflections, Set<String> affinityTags, List<String> presentationCues,
                            String antiOverclaimBoundary) {
        public Primitive {
            id = stableId(id);
            state = Objects.requireNonNull(state, "state");
            title = text(title, "title");
            outcomeRead = text(outcomeRead, "outcomeRead");
            carryForward = text(carryForward, "carryForward");
            playerReflections = exactTextList(playerReflections, 3, "playerReflections");
            affinityTags = nonEmptyTags(affinityTags);
            presentationCues = exactTextList(presentationCues, 2, "presentationCues");
            antiOverclaimBoundary = text(antiOverclaimBoundary, "antiOverclaimBoundary");
        }
    }

    public record Selection(String generatorVersion, long seed, String scenarioId, String actorContextId,
                            String investigationId, String outcomeId, OutcomeState state, Primitive primitive,
                            String presentationCue, Set<String> matchedEvidenceTags) {
        public Selection {
            generatorVersion = text(generatorVersion, "generatorVersion");
            scenarioId = opaqueId(scenarioId, "scenarioId");
            actorContextId = opaqueId(actorContextId, "actorContextId");
            investigationId = opaqueId(investigationId, "investigationId");
            outcomeId = opaqueId(outcomeId, "outcomeId");
            state = Objects.requireNonNull(state, "state");
            primitive = Objects.requireNonNull(primitive, "primitive");
            if (primitive.state() != state) throw new IllegalArgumentException("primitive state must match caller-owned outcome state");
            presentationCue = text(presentationCue, "presentationCue");
            matchedEvidenceTags = Set.copyOf(Objects.requireNonNull(matchedEvidenceTags, "matchedEvidenceTags"));
        }
    }

    public static List<Primitive> waveOne() {
        return List.of(
                p("found_fixed_fact", OutcomeState.FOUND, "A Fixed Fact", "The investigation closes with one bounded fact established by Java-owned state.", "Carry the established fact forward without extending it into a wider verdict.", "Review what was directly established.", "Keep surrounding questions separate.", "Use the fact only where another Java rule accepts it.", Set.of("observation", "verification", "found"), "The established fact is separated from surrounding uncertainty.", "Source and scope remain visible beside the conclusion.", "FOUND does not mean every related claim is true, every culprit is known, or the Nightmare has resolved."),
                p("found_route_answer", OutcomeState.FOUND, "A Route Answer", "A bounded route question has an authoritative investigation outcome for the conditions actually checked.", "Carry forward the checked route fact and its condition limits.", "Review the checked segment.", "Retain the conditions under which it was checked.", "Leave unchecked terrain unresolved.", Set.of("route", "location", "found"), "Checked and unchecked route segments are visually distinct.", "Condition limits remain attached to the route answer.", "FOUND does not guarantee future safety, wider access, destination state, or successful travel."),
                p("found_source_identity", OutcomeState.FOUND, "Source Identified", "Java-owned investigation state has resolved which bounded source produced a retained record, signal, or account.", "Carry provenance forward without turning source identity into truth authority.", "Review the source identity.", "Keep the source claim separate from the claim's truth.", "Preserve any remaining contradiction.", Set.of("source", "record", "found"), "Provenance is foregrounded while truth remains a separate field.", "Contradictions stay visible after source identification.", "Identifying a source does not prove authenticity, honesty, guilt, allegiance, or correctness."),
                p("found_condition_change", OutcomeState.FOUND, "Condition Established", "A local environmental or structural condition has been established for the investigation's recorded moment.", "Carry forward the observed condition as time-bounded information.", "Review when and where it was observed.", "Keep later changes open.", "Use it only for compatible authored decisions.", Set.of("environment", "observation", "found"), "Time and place are displayed with the condition.", "No forecast is inferred from the recorded observation.", "FOUND does not make a condition permanent or predict weather, collapse, creatures, or future access."),

                p("unresolved_competing_accounts", OutcomeState.LEFT_UNRESOLVED, "Accounts Still Diverge", "The investigation ends with materially different accounts still unsupported by enough authority to choose between them.", "Carry forward the disagreement itself rather than a guessed winner.", "Preserve each account separately.", "Name the shared facts.", "Leave the disputed claim open.", Set.of("testimony", "contradiction", "unresolved"), "Each account keeps its own source label.", "Shared facts and disputes are shown in separate sections.", "LEFT_UNRESOLVED does not imply both accounts are equally true, equally false, or morally equivalent."),
                p("unresolved_missing_link", OutcomeState.LEFT_UNRESOLVED, "The Missing Link Remains", "A necessary connection between observations was never established before this investigation ended.", "Carry forward the exact missing link as an open question.", "Record what would have connected the evidence.", "Keep alternative explanations visible.", "Do not manufacture the missing step.", Set.of("gap", "uncertainty", "unresolved"), "The absent connection is displayed explicitly instead of being silently filled.", "Competing explanations remain provisional.", "An unresolved gap cannot be converted into hidden certainty, guilt, or a ResolutionGraph event by presentation."),
                p("unresolved_source_unavailable", OutcomeState.LEFT_UNRESOLVED, "Source Never Reached", "A relevant source remained unavailable through the end of this bounded investigation.", "Carry forward what was sought and why it remains missing.", "Record the unavailable source.", "Separate absence from refusal.", "Retain any independent evidence.", Set.of("source", "access", "unresolved"), "The unavailable source is named without assigning a reason.", "Independent evidence remains inspectable beside the gap.", "Unavailability does not imply death, deception, refusal, guilt, or future availability."),
                p("unresolved_route_question", OutcomeState.LEFT_UNRESOLVED, "Route Question Open", "The investigation did not establish whether a wider route was usable beyond the bounded checks completed.", "Carry forward only the segments and conditions actually known.", "Keep tested segments recorded.", "Mark the untested boundary.", "Avoid a whole-route verdict.", Set.of("route", "gap", "unresolved"), "Known segments fade into an explicit unknown boundary.", "No destination state is implied beyond the checked area.", "LEFT_UNRESOLVED does not mean the route is safe, unsafe, open, closed, or impossible."),

                p("preserved_original", OutcomeState.PRESERVED, "Original Preserved", "The investigation ends with an original object, record, or account retained in the state authorized by Java-owned investigation logic.", "Carry the preserved material and its handling limits forward.", "Review what was preserved.", "Keep handling notes attached.", "Separate preservation from interpretation.", Set.of("preservation", "record", "preserved"), "The preserved item is marked as retained, not verified.", "Handling notes remain visible next to its description.", "PRESERVED does not certify authenticity, ownership, chain of custody, truth, or appraisal credit."),
                p("preserved_contradiction", OutcomeState.PRESERVED, "Contradiction Preserved", "A conflict between sources remains deliberately retained so later Java-owned logic can examine it without this summary choosing a side.", "Carry both sides and the exact point of conflict forward.", "Review the conflicting claims.", "Retain source identity for each side.", "Keep the contradiction open.", Set.of("contradiction", "preservation", "preserved"), "Both sides remain equally inspectable in the presentation.", "The disputed point is highlighted without an accusation marker.", "Preserving a contradiction does not establish deception, culpability, or equal reliability."),
                p("preserved_bounded_sample", OutcomeState.PRESERVED, "Bounded Sample Retained", "A limited sample has been retained under Java-owned state without expanding its meaning beyond what was actually collected.", "Carry forward provenance and scope before any later examination.", "Review sample origin.", "Keep quantity and scope limits visible.", "Leave wider conditions unresolved.", Set.of("sample", "preservation", "preserved"), "Sample provenance and scope appear before interpretation.", "The wider site remains explicitly outside the sample's authority.", "PRESERVED does not make a sample representative, safe, rare, magical, or authentic."),
                p("preserved_route_record", OutcomeState.PRESERVED, "Route Record Retained", "A route note, marker sequence, or checked segment has been preserved as historical investigation material.", "Carry forward when, where, and under what conditions the route information was recorded.", "Review the recorded conditions.", "Keep stale/current status separate.", "Retain alternate route notes if present.", Set.of("route", "record", "preserved"), "The record date or investigation moment remains prominent.", "Current-route authority is not implied by preservation styling.", "A preserved route record is not a live navigation guarantee or forecast."),

                p("abandoned_risk_exceeded", OutcomeState.ABANDONED, "Risk Exceeded the Question", "Java-owned investigation state records that this line was left behind because continuing it was not accepted or justified under the current conditions.", "Carry forward the abandoned question and the known reason without converting withdrawal into an answer.", "Record the stopping condition.", "Preserve what was already learned.", "Move to another authored concern.", Set.of("risk", "defer", "abandoned"), "The stopping condition is shown beside the unfinished question.", "Existing evidence remains available after abandonment.", "ABANDONED does not prove the line was wrong, impossible, unsafe forever, or irrelevant to the Nightmare."),
                p("abandoned_access_lost", OutcomeState.ABANDONED, "Access Was Lost", "The investigation ended this line after the required place, person, or object was no longer available to pursue.", "Carry forward the access boundary and any evidence obtained before it closed.", "Record the last available observation.", "Keep missing evidence explicit.", "Do not infer why access was lost.", Set.of("access", "source", "abandoned"), "The last known access state is separated from speculation about its cause.", "Evidence collected before loss remains inspectable.", "Lost access does not establish destruction, death, refusal, betrayal, or permanent closure."),
                p("abandoned_cost_choice", OutcomeState.ABANDONED, "Another Obligation Took Priority", "Java-owned investigation state records that this line was left unfinished in favor of another authorized obligation or survival concern.", "Carry forward what was knowingly left unanswered and what was protected instead.", "Review the abandoned question.", "Record the competing obligation.", "Keep moral judgment outside the summary.", Set.of("obligation", "choice", "abandoned"), "The tradeoff is presented without assigning virtue or blame.", "The unfinished question remains visible beside the chosen obligation.", "ABANDONED does not define the morally correct choice or determine appraisal, guilt, allegiance, or scenario success."),
                p("abandoned_evidence_destroyed", OutcomeState.ABANDONED, "No Further Check Was Possible", "The bounded investigation ended after the relevant evidence could no longer support further examination under Java-owned state.", "Carry forward what was known before the verification path ended.", "Record the last verified observation.", "Name what can no longer be checked.", "Avoid reconstructing missing evidence from flavor text.", Set.of("evidence", "loss", "abandoned"), "The final verified point is separated from the unavailable remainder.", "No reconstructed answer is displayed in place of missing evidence.", "ABANDONED does not prove deliberate destruction, forgery, guilt, or the truth of the surviving account.")
        );
    }

    public static Selection compose(long seed, String scenarioId, String actorContextId, String investigationId,
                                    String outcomeId, OutcomeState state, Map<String, Integer> evidence) {
        String scenario = opaqueId(scenarioId, "scenarioId");
        String actor = opaqueId(actorContextId, "actorContextId");
        String investigation = opaqueId(investigationId, "investigationId");
        String outcome = opaqueId(outcomeId, "outcomeId");
        OutcomeState resolvedState = Objects.requireNonNull(state, "state");
        Set<String> positive = positiveEvidenceTags(evidence);
        List<Primitive> eligible = waveOne().stream().filter(p -> p.state() == resolvedState).toList();
        List<Primitive> preferred = eligible.stream().filter(p -> p.affinityTags().stream().anyMatch(positive::contains)).toList();
        List<Primitive> pool = preferred.isEmpty() ? eligible : preferred;
        Primitive primitive = pool.get(index(seed, scenario, actor, investigation, outcome, resolvedState.name(), "primitive", pool.size()));
        String cue = primitive.presentationCues().get(index(seed, scenario, actor, investigation, outcome, primitive.id(), "cue", 2));
        Set<String> matched = primitive.affinityTags().stream().filter(positive::contains).collect(Collectors.toUnmodifiableSet());
        return new Selection(GENERATOR_VERSION, seed, scenario, actor, investigation, outcome, resolvedState, primitive, cue, matched);
    }

    public static Primitive byId(String id) {
        String stable = stableId(id);
        return waveOne().stream().filter(p -> p.id().equals(stable)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown investigation outcome summary: " + stable));
    }

    private static Primitive p(String id, OutcomeState state, String title, String read, String carryForward,
                               String a, String b, String c, Set<String> tags, String cue1, String cue2, String boundary) {
        return new Primitive(id, state, title, read, carryForward, List.of(a, b, c), tags, List.of(cue1, cue2), boundary);
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
        Set<String> normalized = tags.stream().map(NightmareInvestigationOutcomeSummaryCatalog::stableId).collect(Collectors.toUnmodifiableSet());
        if (normalized.isEmpty()) throw new IllegalArgumentException("affinityTags must not be empty");
        return normalized;
    }
}
