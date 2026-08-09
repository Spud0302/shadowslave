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
                        "Java has authorized a local access change for this faction context; presentation records the changed scope without inventing ownership or safety.",
                        "Carry forward only the access state Java actually supplied.",
                        "Review which place or route changed.", "Review who the change applies to.", "Treat every other route as separately authoritative.",
                        Set.of("access", "territory", "route"),
                        "The changed access scope is highlighted without a global map reveal.", "Unaffected routes remain visually neutral.",
                        "An access change cannot prove ownership, territorial legitimacy, route safety, permanent permission, hostility, or Nightmare resolution."),
                p("access_window_changed", ConsequenceKind.ACCESS_CHANGED, "Passage Window Changed",
                        "Java has authorized a change to when or under what bounded condition passage applies, without defining a universal timer or travel rule.",
                        "Record the supplied condition rather than extrapolating a schedule.",
                        "Review the active condition.", "Note what condition ended or replaced it.", "Require fresh authority before treating the window as open again.",
                        Set.of("access", "condition", "time"),
                        "Current and previous passage conditions are shown side by side.", "No countdown is generated unless Java supplies one elsewhere.",
                        "A changed passage window cannot invent timers, guarantee future access, forecast safety, establish ownership, or impose reputation consequences."),
                p("access_actor_scope_changed", ConsequenceKind.ACCESS_CHANGED, "Who May Pass Changed",
                        "Java has authorized a change in which supplied actor or group scope is covered by local access state.",
                        "Keep the consequence attached to the exact caller-owned actor scope.",
                        "Review who is covered now.", "Review who is explicitly outside the supplied scope.", "Do not generalize one actor's access to a faction-wide rule.",
                        Set.of("access", "actor", "scope"),
                        "Covered and uncovered actor scopes are separated in presentation.", "No allegiance icon is inferred from permission.",
                        "Actor-scoped access cannot establish allegiance, membership, trust, collective permission, guilt, or permanent territorial rights."),
                p("access_route_closed_locally", ConsequenceKind.ACCESS_CHANGED, "One Route Is No Longer Assumed",
                        "Java has authorized a local access change that removes or alters reliance on one route while other routes remain separate state.",
                        "Treat this as one changed route fact, not a global travel verdict.",
                        "Mark the affected route.", "Check whether an alternative is separately authorized.", "Leave route safety and travel success unresolved.",
                        Set.of("access", "route", "warning"),
                        "The affected route is marked without declaring every alternative open.", "No danger or encounter probability is generated.",
                        "A route access change cannot prove danger, guarantee alternatives, establish a travel probability, trigger hostility, or decide scenario success."),

                p("obligation_closed_fulfilled", ConsequenceKind.OBLIGATION_CLOSED, "Scoped Obligation Closed",
                        "Java has authorized one bounded obligation as closed; presentation does not convert closure into loyalty, reward, or total agreement completion.",
                        "Record exactly which obligation is no longer pending.",
                        "Acknowledge the closed obligation.", "Review any independent obligations still open.", "Keep wider faction intent unresolved.",
                        Set.of("obligation", "fulfilled", "terms"),
                        "The closed obligation receives a local completion marker.", "Other terms remain separate rather than collapsing into one success state.",
                        "Closing an obligation cannot alter allegiance or reputation, grant rewards, prove goodwill, accept a ResolutionGraph event, or resolve the Nightmare."),
                p("obligation_closed_broken", ConsequenceKind.OBLIGATION_CLOSED, "Unmet Obligation Closed",
                        "Java has authorized that a bounded obligation is no longer pending after an unmet outcome, without presentation assigning blame or motive.",
                        "Carry forward the fact of closure separately from why the obligation was unmet.",
                        "Record the unmet obligation.", "Record whether cause is known or unknown.", "Leave blame and response to Java-owned state.",
                        Set.of("obligation", "broken", "terms"),
                        "Closure and explanation appear as separate fields.", "No betrayal or hostility badge is inferred.",
                        "An unmet closed obligation cannot prove betrayal, bad faith, guilt, deception, hostility, reputation loss, or appraisal consequence."),
                p("obligation_closed_expired", ConsequenceKind.OBLIGATION_CLOSED, "Expired Obligation Archived",
                        "Java has authorized a bounded obligation as closed because its supplied context ended, without turning expiration into failure or punishment.",
                        "Archive the old obligation and require fresh authority for any replacement.",
                        "Review the context that ended.", "Archive the old terms.", "Ask for current terms only through a new Java-owned interaction.",
                        Set.of("obligation", "expired", "context"),
                        "Expired terms remain readable as history but not active authority.", "No retry timer or penalty is invented.",
                        "Expiration cannot create a canonical cooldown, imply hostility, reduce reputation, preserve prices, restore old access, or determine future willingness."),
                p("obligation_closed_shared_task", ConsequenceKind.OBLIGATION_CLOSED, "Shared Task No Longer Pending",
                        "Java has authorized a shared local task as closed while broader cooperation and scenario state remain independent.",
                        "Treat the closed task as one finished or ended concern rather than an alliance result.",
                        "Record the task as no longer pending.", "List any supplied unresolved consequences.", "Wait for Java before advancing a larger objective.",
                        Set.of("obligation", "cooperation", "duty"),
                        "The task card closes while the broader scenario display remains unchanged.", "No faction relationship meter moves from presentation alone.",
                        "Closing a shared task cannot establish trust or allegiance, complete another objective, resolve the scenario, determine appraisal, or award progression."),

                p("resource_state_changed_local", ConsequenceKind.RESOURCE_STATE_CHANGED, "Local Resource State Changed",
                        "Java has authorized a change to a bounded resource state associated with this faction context; presentation reports that change without inventing quantity or value.",
                        "Carry forward only the exact resource fact supplied by authoritative state.",
                        "Review which resource changed.", "Review whether quantity or quality is actually supplied elsewhere.", "Do not infer scarcity from one local change.",
                        Set.of("resource", "state", "local"),
                        "The affected resource is highlighted without a fabricated inventory delta.", "No rarity, price, or scarcity meter is generated.",
                        "A resource-state change cannot invent quantities, prices, scarcity, ownership, debt, reward value, progression, or a canonical economy formula."),
                p("resource_commitment_settled", ConsequenceKind.RESOURCE_STATE_CHANGED, "Committed Resource State Settled",
                        "Java has authorized the local resource consequence associated with a prior commitment, without presentation calculating fairness or social credit.",
                        "Separate the resource result from any later relationship interpretation.",
                        "Record the supplied resource result.", "Record any independent obligation still open.", "Leave fairness and reputation unresolved.",
                        Set.of("resource", "commitment", "bargain"),
                        "Resource consequence and social interpretation are displayed separately.", "No fairness score or reputation reward appears.",
                        "A settled resource consequence cannot calculate fair value, infer generosity or betrayal, alter allegiance or reputation, or determine appraisal."),
                p("resource_access_link_changed", ConsequenceKind.RESOURCE_STATE_CHANGED, "Resource and Access Context Changed",
                        "Java has authorized a local resource-state change relevant to access, while actual permission and territorial state remain separately authoritative.",
                        "Do not convert resource context into automatic passage or exclusion.",
                        "Review the resource fact.", "Review access state separately.", "Avoid inferring ownership from possession or absence.",
                        Set.of("resource", "access", "territory"),
                        "Resource and access indicators remain separate.", "No gate or map state changes from the debrief itself.",
                        "Resource state cannot establish territorial ownership, unlock or close routes, guarantee passage, prove theft, trigger hostility, or mutate world state."),
                p("resource_information_changed", ConsequenceKind.RESOURCE_STATE_CHANGED, "Resource Record Updated",
                        "Java has authorized a changed resource record or reported state, while truth, authenticity, and completeness remain separate questions unless already resolved elsewhere.",
                        "Preserve provenance when carrying the resource information forward.",
                        "Record what changed in the supplied record.", "Record the source of that state if available.", "Keep authenticity and truth separate.",
                        Set.of("resource", "information", "verification"),
                        "The updated record is shown with provenance rather than a truth seal.", "No hidden quantity is inferred from wording.",
                        "An updated resource record cannot certify truth or authenticity, detect theft or deception, infer hidden stock, or determine guilt, reputation, or rewards."),

                p("relationship_question_open", ConsequenceKind.RELATIONSHIP_UNRESOLVED, "Relationship Question Remains Open",
                        "Java has authorized no deeper faction relationship conclusion from this local outcome; cooperation, hostility, allegiance, and trust remain unresolved here.",
                        "Carry forward the local consequence without filling the relationship gap.",
                        "Record what happened locally.", "Name which relationship question remains open.", "Wait for later Java-owned evidence or state.",
                        Set.of("relationship", "uncertainty", "cooperation"),
                        "The debrief explicitly displays an unresolved relationship marker.", "No trust, hostility, or allegiance score is synthesized.",
                        "An unresolved relationship cannot be converted into trust, hostility, allegiance, reputation, betrayal probability, or a future behavior guarantee."),
                p("relationship_after_cooperation_open", ConsequenceKind.RELATIONSHIP_UNRESOLVED, "Cooperation Did Not Settle Loyalty",
                        "A bounded cooperative outcome occurred in Java-owned state, but presentation retains loyalty and future intent as unresolved.",
                        "Distinguish one cooperative act from durable allegiance.",
                        "Record the cooperative act.", "Keep future cooperation uncertain.", "Do not infer faction-wide loyalty from one actor or exchange.",
                        Set.of("relationship", "cooperation", "allegiance"),
                        "Cooperation is shown as an event history rather than a loyalty meter.", "Future interaction remains unpredicted.",
                        "Cooperation cannot prove trust or allegiance, bind future behavior, erase conflicting interests, change reputation, or guarantee another agreement."),
                p("relationship_after_break_open", ConsequenceKind.RELATIONSHIP_UNRESOLVED, "Broken Terms Did Not Settle Intent",
                        "A bounded agreement problem exists in Java-owned state, but presentation does not infer betrayal, hostility, or the faction's future stance.",
                        "Retain the mismatch while keeping cause and relationship conclusions separate.",
                        "Record the broken term.", "Record whether intent is actually known.", "Leave retaliation and future stance to Java authority.",
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
