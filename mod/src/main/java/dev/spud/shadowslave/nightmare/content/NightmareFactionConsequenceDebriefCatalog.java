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

/** DESIGN-only presentation for already-authorized Java-owned local faction consequences. */
public final class NightmareFactionConsequenceDebriefCatalog {
    public static final String GENERATOR_VERSION = "nightmare-faction-consequence-debrief-v1";

    private NightmareFactionConsequenceDebriefCatalog() {}

    public enum ConsequenceKind {
        ACCESS_CHANGED,
        OBLIGATION_CLOSED,
        RESOURCE_STATE_CHANGED,
        RELATIONSHIP_UNRESOLVED
    }

    public record Primitive(String id, ConsequenceKind kind, String title, String consequenceRead,
                            String carryForwardPrompt, List<String> playerResponses, Set<String> affinityTags,
                            List<String> presentationCues, String antiOverclaimBoundary) {
        public Primitive {
            id = stableId(id);
            kind = Objects.requireNonNull(kind, "kind");
            title = text(title, "title");
            consequenceRead = text(consequenceRead, "consequenceRead");
            carryForwardPrompt = text(carryForwardPrompt, "carryForwardPrompt");
            playerResponses = exactTextList(playerResponses, 3, "playerResponses");
            affinityTags = nonEmptyTags(affinityTags);
            presentationCues = exactTextList(presentationCues, 2, "presentationCues");
            antiOverclaimBoundary = text(antiOverclaimBoundary, "antiOverclaimBoundary");
        }
    }

    public record Selection(String generatorVersion, long seed, String scenarioId, String factionId,
                            String agreementId, String consequenceId, ConsequenceKind kind,
                            Primitive primitive, String presentationCue, Set<String> matchedEvidenceTags) {
        public Selection {
            generatorVersion = text(generatorVersion, "generatorVersion");
            scenarioId = opaqueId(scenarioId, "scenarioId");
            factionId = opaqueId(factionId, "factionId");
            agreementId = opaqueId(agreementId, "agreementId");
            consequenceId = opaqueId(consequenceId, "consequenceId");
            kind = Objects.requireNonNull(kind, "kind");
            primitive = Objects.requireNonNull(primitive, "primitive");
            if (primitive.kind() != kind) {
                throw new IllegalArgumentException("primitive kind must match caller-owned consequence kind");
            }
            presentationCue = text(presentationCue, "presentationCue");
            matchedEvidenceTags = Set.copyOf(Objects.requireNonNull(matchedEvidenceTags, "matchedEvidenceTags"));
        }
    }

    public static List<Primitive> waveOne() {
        return List.of(
                p("access_scope_changed", ConsequenceKind.ACCESS_CHANGED, "Access Scope Changed",
                        "The terms of local access have changed, but the change says nothing by itself about ownership, safety, or wider permission.",
                        "Carry forward only the access scope that is actually known.",
                        "Review which place or route changed.", "Review who the change applies to.", "Treat every other route as a separate question.",
                        Set.of("access", "territory", "route"),
                        "The changed access scope is highlighted without a global map reveal.", "Unaffected routes remain visually neutral.",
                        "An access change cannot prove ownership, territorial legitimacy, route safety, permanent permission, hostility, or Nightmare resolution."),
                p("access_window_changed", ConsequenceKind.ACCESS_CHANGED, "Passage Window Changed",
                        "The condition or window under which passage applies has changed, without implying a universal schedule or travel rule.",
                        "Record the current condition rather than extrapolating a schedule.",
                        "Review the active condition.", "Note what condition ended or replaced it.", "Treat a future opening as a fresh question.",
                        Set.of("access", "condition", "time"),
                        "Current and previous passage conditions are shown side by side.", "No countdown appears unless an actual duration is known.",
                        "A changed passage window cannot invent timers, guarantee future access, forecast safety, establish ownership, or impose reputation consequences."),
                p("access_actor_scope_changed", ConsequenceKind.ACCESS_CHANGED, "Who May Pass Changed",
                        "The people covered by this local permission have changed, while everyone outside that scope remains a separate case.",
                        "Keep the consequence attached to the people it actually covers.",
                        "Review who is covered now.", "Review who is explicitly outside the scope.", "Do not generalize one person's access to the whole faction.",
                        Set.of("access", "actor", "scope"),
                        "Covered and uncovered actor scopes are separated in presentation.", "No allegiance icon is inferred from permission.",
                        "Actor-scoped access cannot establish allegiance, membership, trust, collective permission, guilt, or permanent territorial rights."),
                p("access_route_closed_locally", ConsequenceKind.ACCESS_CHANGED, "One Route Is No Longer Assumed",
                        "One route can no longer be relied upon under the present access terms, while every alternative remains a separate question.",
                        "Treat this as one changed route fact, not a global travel verdict.",
                        "Mark the affected route.", "Check whether an alternative is actually available.", "Leave route safety and travel success unresolved.",
                        Set.of("access", "route", "warning"),
                        "The affected route is marked without declaring every alternative open.", "No danger or encounter probability is generated.",
                        "A route access change cannot prove danger, guarantee alternatives, establish a travel probability, trigger hostility, or decide scenario success."),

                p("obligation_closed_fulfilled", ConsequenceKind.OBLIGATION_CLOSED, "Scoped Obligation Closed",
                        "One bounded obligation is no longer pending, but its closure does not settle loyalty, reward, or the rest of the agreement.",
                        "Record exactly which obligation is no longer pending.",
                        "Acknowledge the closed obligation.", "Review any independent obligations still open.", "Keep wider faction intent unresolved.",
                        Set.of("obligation", "fulfilled", "terms"),
                        "The closed obligation receives a local completion marker.", "Other terms remain separate rather than collapsing into one success state.",
                        "Closing an obligation cannot alter allegiance or reputation, grant rewards, prove goodwill, accept a ResolutionGraph event, or resolve the Nightmare."),
                p("obligation_closed_broken", ConsequenceKind.OBLIGATION_CLOSED, "Unmet Obligation Closed",
                        "An unmet obligation is no longer pending, but the reason it went unmet and what follows from that remain separate questions.",
                        "Carry forward the fact of closure separately from why the obligation was unmet.",
                        "Record the unmet obligation.", "Record whether the cause is known or unknown.", "Leave blame and response unresolved until they are established.",
                        Set.of("obligation", "broken", "terms"),
                        "Closure and explanation appear as separate fields.", "No betrayal or hostility badge is inferred.",
                        "An unmet closed obligation cannot prove betrayal, bad faith, guilt, deception, hostility, reputation loss, or appraisal consequence."),
                p("obligation_closed_expired", ConsequenceKind.OBLIGATION_CLOSED, "Expired Obligation Archived",
                        "An obligation has ended with the context that supported it, without turning expiration into failure, blame, or punishment.",
                        "Archive the old obligation and treat any replacement as new terms.",
                        "Review the context that ended.", "Archive the old terms.", "Ask for current terms instead of reviving the old ones.",
                        Set.of("obligation", "expired", "context"),
                        "Expired terms remain readable as history but not active authority.", "No retry timer or penalty is invented.",
                        "Expiration cannot create a canonical cooldown, imply hostility, reduce reputation, preserve prices, restore old access, or determine future willingness."),
                p("obligation_closed_shared_task", ConsequenceKind.OBLIGATION_CLOSED, "Shared Task No Longer Pending",
                        "A shared local task is no longer pending, while broader cooperation and the larger conflict remain independent.",
                        "Treat the closed task as one finished or ended concern rather than an alliance result.",
                        "Record the task as no longer pending.", "List any unresolved consequences that remain.", "Keep the larger objective separate from this task.",
                        Set.of("obligation", "cooperation", "duty"),
                        "The task card closes while the broader scenario display remains unchanged.", "No faction relationship meter moves from presentation alone.",
                        "Closing a shared task cannot establish trust or allegiance, complete another objective, resolve the scenario, determine appraisal, or award progression."),

                p("resource_state_changed_local", ConsequenceKind.RESOURCE_STATE_CHANGED, "Local Resource State Changed",
                        "A bounded resource situation has changed in this local context, without implying an unknown quantity, value, rarity, or wider shortage.",
                        "Carry forward only the resource fact that is actually known.",
                        "Review which resource changed.", "Check whether quantity or quality is actually known.", "Do not infer scarcity from one local change.",
                        Set.of("resource", "state", "local"),
                        "The affected resource is highlighted without a fabricated inventory delta.", "No rarity, price, or scarcity meter is generated.",
                        "A resource-state change cannot invent quantities, prices, scarcity, ownership, debt, reward value, progression, or a canonical economy formula."),
                p("resource_commitment_settled", ConsequenceKind.RESOURCE_STATE_CHANGED, "Committed Resource State Settled",
                        "The local resource consequence tied to an earlier commitment is settled, but fairness, generosity, blame, and social credit remain separate.",
                        "Separate the resource result from any later relationship interpretation.",
                        "Record the resource result.", "Record any independent obligation still open.", "Leave fairness and reputation unresolved.",
                        Set.of("resource", "commitment", "bargain"),
                        "Resource consequence and social interpretation are displayed separately.", "No fairness score or reputation reward appears.",
                        "A settled resource consequence cannot calculate fair value, infer generosity or betrayal, alter allegiance or reputation, or determine appraisal."),
                p("resource_access_link_changed", ConsequenceKind.RESOURCE_STATE_CHANGED, "Resource and Access Context Changed",
                        "A local resource condition relevant to access has changed, while permission and territorial claims remain separate matters.",
                        "Do not convert resource context into automatic passage or exclusion.",
                        "Review the resource fact.", "Review access separately.", "Avoid inferring ownership from possession or absence.",
                        Set.of("resource", "access", "territory"),
                        "Resource and access indicators remain separate.", "No gate or map state changes from the debrief itself.",
                        "Resource state cannot establish territorial ownership, unlock or close routes, guarantee passage, prove theft, trigger hostility, or mutate world state."),
                p("resource_information_changed", ConsequenceKind.RESOURCE_STATE_CHANGED, "Resource Record Updated",
                        "A resource record or report has changed, while its truth, authenticity, and completeness remain separate questions unless already established.",
                        "Preserve provenance when carrying the resource information forward.",
                        "Record what changed in the report.", "Record its source if that is known.", "Keep authenticity and truth separate.",
                        Set.of("resource", "information", "verification"),
                        "The updated record is shown with provenance rather than a truth seal.", "No hidden quantity is inferred from wording.",
                        "An updated resource record cannot certify truth or authenticity, detect theft or deception, infer hidden stock, or determine guilt, reputation, or rewards."),

                p("relationship_question_open", ConsequenceKind.RELATIONSHIP_UNRESOLVED, "Relationship Question Remains Open",
                        "This local outcome settles no deeper relationship question; cooperation, hostility, allegiance, and trust remain unresolved here.",
                        "Carry forward the local consequence without filling the relationship gap.",
                        "Record what happened locally.", "Name which relationship question remains open.", "Wait for later evidence before deciding more.",
                        Set.of("relationship", "uncertainty", "cooperation"),
                        "The debrief explicitly displays an unresolved relationship marker.", "No trust, hostility, or allegiance score is synthesized.",
                        "An unresolved relationship cannot be converted into trust, hostility, allegiance, reputation, betrayal probability, or a future behavior guarantee."),
                p("relationship_after_cooperation_open", ConsequenceKind.RELATIONSHIP_UNRESOLVED, "Cooperation Did Not Settle Loyalty",
                        "A bounded act of cooperation occurred, but loyalty and future intent remain unresolved beyond that act.",
                        "Distinguish one cooperative act from durable allegiance.",
                        "Record the cooperative act.", "Keep future cooperation uncertain.", "Do not infer faction-wide loyalty from one person or exchange.",
                        Set.of("relationship", "cooperation", "allegiance"),
                        "Cooperation is shown as an event history rather than a loyalty meter.", "Future interaction remains unpredicted.",
                        "Cooperation cannot prove trust or allegiance, bind future behavior, erase conflicting interests, change reputation, or guarantee another agreement."),
                p("relationship_after_break_open", ConsequenceKind.RELATIONSHIP_UNRESOLVED, "Broken Terms Did Not Settle Intent",
                        "A bounded agreement problem exists, but betrayal, hostility, and the faction's future stance remain unresolved unless separately established.",
                        "Retain the mismatch while keeping cause and relationship conclusions separate.",
                        "Record the broken term.", "Record whether intent is actually known.", "Keep retaliation and future stance as separate decisions.",
                        Set.of("relationship", "broken", "uncertainty"),
                        "Broken-term history is visible without an automatic enemy state.", "No retaliation prompt is presented as mandatory.",
                        "Broken terms cannot prove betrayal or hostile intent, assign collective blame, force retaliation, change allegiance or reputation, or predict future conduct."),
                p("relationship_after_access_open", ConsequenceKind.RELATIONSHIP_UNRESOLVED, "Access Did Not Settle Relationship",
                        "A local access consequence exists, but permission or denial alone does not establish trust, hostility, allegiance, or moral standing.",
                        "Carry access state forward without using it as a relationship verdict.",
                        "Record the access consequence.", "Keep the relationship question separate.", "Seek later evidence rather than inferring motive.",
                        Set.of("relationship", "access", "territory"),
                        "Access and relationship indicators are deliberately separated.", "No friendship or enemy marker is inferred from a gate state.",
                        "Access or denial cannot by itself prove trust, hostility, territorial legitimacy, allegiance, guilt, reputation change, or future negotiation behavior."));
    }

    public static Selection compose(long seed, String scenarioId, String factionId, String agreementId,
                                    String consequenceId, ConsequenceKind kind, Map<String, Integer> evidence) {
        String checkedScenario = opaqueId(scenarioId, "scenarioId");
        String checkedFaction = opaqueId(factionId, "factionId");
        String checkedAgreement = opaqueId(agreementId, "agreementId");
        String checkedConsequence = opaqueId(consequenceId, "consequenceId");
        ConsequenceKind checkedKind = Objects.requireNonNull(kind, "kind");
        Set<String> positiveEvidence = positiveEvidence(evidence);

        List<Primitive> candidates = waveOne().stream()
                .filter(primitive -> primitive.kind() == checkedKind)
                .sorted(Comparator.comparing(Primitive::id))
                .toList();
        if (candidates.isEmpty()) throw new IllegalArgumentException("no faction consequence presentation for supplied kind");

        int bestMatch = candidates.stream().mapToInt(p -> overlap(p.affinityTags(), positiveEvidence)).max().orElse(0);
        List<Primitive> preferred = bestMatch > 0
                ? candidates.stream().filter(p -> overlap(p.affinityTags(), positiveEvidence) == bestMatch).toList()
                : candidates;

        String authorityKey = checkedScenario + "|" + checkedFaction + "|" + checkedAgreement + "|"
                + checkedConsequence + "|" + checkedKind.name() + "|"
                + positiveEvidence.stream().sorted().collect(Collectors.joining(","));
        Primitive primitive = preferred.get(index(seed, authorityKey + "|primitive", preferred.size()));
        String cue = primitive.presentationCues().get(index(seed,
                authorityKey + "|" + primitive.id() + "|cue", primitive.presentationCues().size()));
        Set<String> matched = primitive.affinityTags().stream().filter(positiveEvidence::contains).sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new Selection(GENERATOR_VERSION, seed, checkedScenario, checkedFaction, checkedAgreement,
                checkedConsequence, checkedKind, primitive, cue, matched);
    }

    public static Primitive requirePrimitive(String id) {
        String checked = stableId(id);
        return waveOne().stream().filter(p -> p.id().equals(checked)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown faction consequence primitive: " + checked));
    }

    private static Primitive p(String id, ConsequenceKind kind, String title, String consequenceRead,
                               String carryForwardPrompt, String r1, String r2, String r3, Set<String> tags,
                               String c1, String c2, String boundary) {
        return new Primitive(id, kind, title, consequenceRead, carryForwardPrompt, List.of(r1, r2, r3), tags,
                List.of(c1, c2), boundary);
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
        return Set.copyOf(tags.stream().map(NightmareFactionConsequenceDebriefCatalog::stableId)
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
