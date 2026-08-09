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

/** DESIGN-only presentation for already-authorized Nightmare faction interaction history. */
public final class NightmareFactionAftermathHistoryCatalog {
    public static final String GENERATOR_VERSION = "nightmare-faction-aftermath-history-v1";

    private NightmareFactionAftermathHistoryCatalog() {}

    public enum HistoryKind {
        COOPERATED,
        DISPUTED,
        UNRESOLVED,
        SEPARATED
    }

    public record Primitive(String id, HistoryKind kind, String title, String historyRead,
                            String carryForwardPrompt, List<String> playerReflections, Set<String> affinityTags,
                            List<String> presentationCues, String antiOverclaimBoundary) {
        public Primitive {
            id = stableId(id);
            kind = Objects.requireNonNull(kind, "kind");
            title = text(title, "title");
            historyRead = text(historyRead, "historyRead");
            carryForwardPrompt = text(carryForwardPrompt, "carryForwardPrompt");
            playerReflections = exactTextList(playerReflections, 3, "playerReflections");
            affinityTags = nonEmptyTags(affinityTags);
            presentationCues = exactTextList(presentationCues, 2, "presentationCues");
            antiOverclaimBoundary = text(antiOverclaimBoundary, "antiOverclaimBoundary");
        }
    }

    public record Selection(String generatorVersion, long seed, String scenarioId, String factionId,
                            String interactionId, String historyEntryId, HistoryKind kind,
                            Primitive primitive, String presentationCue, Set<String> matchedEvidenceTags) {
        public Selection {
            generatorVersion = text(generatorVersion, "generatorVersion");
            scenarioId = opaqueId(scenarioId, "scenarioId");
            factionId = opaqueId(factionId, "factionId");
            interactionId = opaqueId(interactionId, "interactionId");
            historyEntryId = opaqueId(historyEntryId, "historyEntryId");
            kind = Objects.requireNonNull(kind, "kind");
            primitive = Objects.requireNonNull(primitive, "primitive");
            if (primitive.kind() != kind) throw new IllegalArgumentException("primitive kind must match supplied history kind");
            presentationCue = text(presentationCue, "presentationCue");
            matchedEvidenceTags = Set.copyOf(Objects.requireNonNull(matchedEvidenceTags, "matchedEvidenceTags"));
        }
    }

    public static List<Primitive> waveOne() {
        return List.of(
                p("cooperated_shared_task", HistoryKind.COOPERATED, "Worked Toward One Task",
                        "You and this faction acted toward the same bounded task during the recorded interaction.",
                        "Remember the shared action without turning it into a promise about the future.",
                        "Recall what the shared task actually covered.", "Keep later cooperation as a separate question.", "Separate one faction member's action from the whole faction.",
                        Set.of("cooperation", "task", "duty"),
                        "The shared action is recorded as one dated history entry.", "No friendship or loyalty meter is inferred.",
                        "Past cooperation cannot prove trust, allegiance, friendship, future help, faction-wide agreement, reputation change, or Nightmare success."),
                p("cooperated_information", HistoryKind.COOPERATED, "Information Was Shared",
                        "Useful information changed hands during the interaction, but the record does not make every claim true or complete.",
                        "Carry forward what was exchanged together with its limits and source.",
                        "Recall what information was actually shared.", "Keep unverified claims marked separately.", "Do not infer loyalty from disclosure alone.",
                        Set.of("cooperation", "information", "verification"),
                        "Shared information is shown with its source context.", "No truth seal or allegiance badge is added.",
                        "Information sharing cannot certify truth or completeness, prove loyalty, establish motive, alter reputation, or guarantee later disclosure."),
                p("cooperated_access", HistoryKind.COOPERATED, "Passage Was Coordinated",
                        "A local passage or access step was coordinated during the interaction, within the scope that actually occurred.",
                        "Remember the successful coordination without treating access as permanent.",
                        "Recall which route or place was involved.", "Recall who the access covered.", "Treat future passage as a fresh state question.",
                        Set.of("cooperation", "access", "route"),
                        "The historical route is marked as a past interaction, not a current unlock.", "Current access remains visually separate.",
                        "Past coordinated access cannot establish ownership, permanent permission, route safety, territorial legitimacy, trust, or future access."),
                p("cooperated_mutual_aid", HistoryKind.COOPERATED, "Aid Was Exchanged",
                        "A bounded act of aid occurred between the player and faction during this interaction.",
                        "Keep the act itself distinct from debt, generosity, loyalty, or obligation unless those are separately established.",
                        "Recall what aid actually occurred.", "Keep any remaining obligation separate.", "Avoid assigning motives that were never established.",
                        Set.of("cooperation", "aid", "resource"),
                        "The aid is logged as a completed historical act.", "No debt or goodwill score is synthesized.",
                        "Past aid cannot calculate debt, generosity, fair value, allegiance, reputation, obligation, or future behavior."),

                p("disputed_terms", HistoryKind.DISPUTED, "Terms Were Disputed",
                        "The interaction included an unresolved disagreement about terms or obligations.",
                        "Remember the point of disagreement without deciding who was right from the history entry alone.",
                        "Recall the term each side contested.", "Separate the dispute from later consequences.", "Leave blame unresolved unless another record establishes it.",
                        Set.of("dispute", "terms", "obligation"),
                        "Both sides of the disputed term remain visible in the history entry.", "No guilt or hostility marker is inferred.",
                        "A dispute cannot prove blame, bad faith, deception, hostility, guilt, reputation loss, or which account is true."),
                p("disputed_access", HistoryKind.DISPUTED, "Access Was Contested",
                        "The interaction included disagreement over local access, passage, or a boundary.",
                        "Keep the contested access history separate from current permission and territorial legitimacy.",
                        "Recall which access point was contested.", "Check current access independently.", "Do not infer ownership from the dispute itself.",
                        Set.of("dispute", "access", "territory"),
                        "The contested place is shown as historical context rather than a current map state.", "No owner or enemy state is generated.",
                        "Contested access cannot establish ownership, territorial legitimacy, hostility, permanent denial, route danger, or future enforcement."),
                p("disputed_account", HistoryKind.DISPUTED, "Accounts Diverged",
                        "The parties left the interaction with materially different accounts of what happened or what mattered.",
                        "Preserve the disagreement without converting disagreement into deception.",
                        "Recall where the accounts diverged.", "Keep corroborated points separate from disputed ones.", "Leave motive and truth open where evidence is incomplete.",
                        Set.of("dispute", "information", "uncertainty"),
                        "Divergent accounts are displayed side by side.", "No lie indicator is inferred from contradiction.",
                        "Different accounts cannot prove lying, guilt, forgery, motive, bad faith, faction hostility, or a canonical truth score."),
                p("disputed_resource", HistoryKind.DISPUTED, "Resource Use Was Disputed",
                        "The interaction left a disagreement about a bounded resource, contribution, or exchange.",
                        "Record the disagreement without inventing quantity, price, ownership, or debt.",
                        "Recall what resource issue was disputed.", "Separate known transfers from claimed ones.", "Leave value and fairness unresolved unless established elsewhere.",
                        Set.of("dispute", "resource", "bargain"),
                        "Known resource facts and disputed claims remain separate.", "No price, debt, or scarcity meter is generated.",
                        "A resource dispute cannot invent quantities, ownership, fair value, debt, theft, scarcity, reputation, or appraisal consequences."),

                p("unresolved_intent", HistoryKind.UNRESOLVED, "Intent Remained Unclear",
                        "The interaction ended without establishing the faction's deeper intent beyond the actions actually observed.",
                        "Carry forward the uncertainty instead of filling it with trust or suspicion.",
                        "Recall only the actions that were observed.", "Name what intent remains unknown.", "Wait for later evidence before assigning motive.",
                        Set.of("uncertainty", "intent", "relationship"),
                        "The history entry keeps an explicit unresolved-intent marker.", "No trust or threat prediction is generated.",
                        "Unclear intent cannot be converted into trust, hostility, allegiance, betrayal probability, motive, reputation, or future behavior."),
                p("unresolved_offer", HistoryKind.UNRESOLVED, "Offer Was Left Open",
                        "An offer or condition remained unsettled when the interaction ended.",
                        "Remember the open question without treating the old offer as still valid now.",
                        "Recall what was left unanswered.", "Check whether the offer is still current before acting on it.", "Treat changed conditions as a new interaction.",
                        Set.of("uncertainty", "offer", "terms"),
                        "The offer is shown as historically open, not currently active.", "No timer or preserved price is invented.",
                        "An unresolved offer cannot guarantee future availability, preserve prices, create a cooldown, imply goodwill, or bind either side."),
                p("unresolved_claim", HistoryKind.UNRESOLVED, "A Claim Stayed Unsettled",
                        "A consequential claim remained neither established nor disproved within the recorded interaction.",
                        "Keep the claim attached to its source and unresolved status.",
                        "Recall the exact scope of the claim.", "Recall what evidence was missing.", "Do not turn repetition or confidence into proof.",
                        Set.of("uncertainty", "claim", "verification"),
                        "The claim remains visibly unresolved in the history entry.", "No confidence percentage or truth verdict is generated.",
                        "An unsettled claim cannot become truth, guilt, authenticity, forgery, motive, ownership, or a numeric confidence score from presentation alone."),
                p("unresolved_relationship", HistoryKind.UNRESOLVED, "Relationship Stayed Unsettled",
                        "The interaction ended without establishing a durable relationship beyond the bounded events that occurred.",
                        "Carry event history forward while leaving the relationship itself open.",
                        "Recall what actually happened.", "Separate cooperation or dispute from lasting allegiance.", "Let later conduct provide new evidence.",
                        Set.of("uncertainty", "relationship", "allegiance"),
                        "Events remain visible while the relationship summary stays unresolved.", "No reputation or allegiance score is synthesized.",
                        "An unresolved relationship cannot become trust, hostility, friendship, allegiance, reputation, or a prediction of future cooperation."),

                p("separated_without_agreement", HistoryKind.SEPARATED, "Parted Without Agreement",
                        "The interaction ended with the parties going separate ways and no bounded agreement recorded.",
                        "Remember the separation without treating it as permanent hostility or peace.",
                        "Recall what remained unsettled at departure.", "Keep future contact as a separate question.", "Do not infer an enemy state from separation alone.",
                        Set.of("separation", "disengage", "terms"),
                        "The interaction closes with no agreement marker.", "Future stance remains unpredicted.",
                        "Separation without agreement cannot establish hostility, alliance, permanent refusal, reputation change, future violence, or future willingness."),
                p("separated_after_warning", HistoryKind.SEPARATED, "Separated After a Warning",
                        "The parties separated after a warning or stated boundary, with no further consequence inferred by the history record.",
                        "Carry forward the warning as something said, not a guaranteed future event.",
                        "Recall the stated warning or boundary.", "Separate it from what actually happened later.", "Do not treat the warning as prophecy.",
                        Set.of("separation", "warning", "boundary"),
                        "The warning is archived as a historical statement.", "No forecast, hostility meter, or mandatory escalation path appears.",
                        "A past warning cannot guarantee danger, prove hostility, predict retaliation, force escalation, change reputation, or establish future intent."),
                p("separated_after_dispute", HistoryKind.SEPARATED, "Dispute Ended in Separation",
                        "The parties stopped the interaction after a dispute instead of reaching terms.",
                        "Record both the dispute and the separation without turning either into proof of betrayal.",
                        "Recall the disputed point.", "Recall how the interaction ended.", "Keep blame and later relationship state separate.",
                        Set.of("separation", "dispute", "relationship"),
                        "The dispute and departure are shown as two historical facts.", "No betrayal or enemy icon is generated.",
                        "Ending a disputed interaction cannot prove betrayal, guilt, hostility, bad faith, permanent enmity, reputation loss, or future retaliation."),
                p("separated_by_circumstance", HistoryKind.SEPARATED, "Circumstances Ended the Contact",
                        "The interaction ended because the parties could no longer continue it under the recorded circumstances.",
                        "Keep the cause of separation bounded to what is actually known.",
                        "Recall which circumstance ended contact.", "Keep motive separate from circumstance.", "Treat any later meeting as a new interaction state.",
                        Set.of("separation", "circumstance", "survival"),
                        "The external circumstance is recorded separately from relationship interpretation.", "No blame or permanent relationship outcome is synthesized.",
                        "Circumstantial separation cannot prove abandonment, hostility, cowardice, loyalty, guilt, reputation change, or whether contact will resume."));
    }

    public static Selection compose(long seed, String scenarioId, String factionId, String interactionId,
                                    String historyEntryId, HistoryKind kind, Map<String, Integer> evidence) {
        String checkedScenario = opaqueId(scenarioId, "scenarioId");
        String checkedFaction = opaqueId(factionId, "factionId");
        String checkedInteraction = opaqueId(interactionId, "interactionId");
        String checkedHistoryEntry = opaqueId(historyEntryId, "historyEntryId");
        HistoryKind checkedKind = Objects.requireNonNull(kind, "kind");
        Set<String> positiveEvidence = positiveEvidence(evidence);

        List<Primitive> candidates = waveOne().stream()
                .filter(primitive -> primitive.kind() == checkedKind)
                .sorted(Comparator.comparing(Primitive::id))
                .toList();
        if (candidates.isEmpty()) throw new IllegalArgumentException("no faction history presentation for supplied kind");

        int bestMatch = candidates.stream().mapToInt(p -> overlap(p.affinityTags(), positiveEvidence)).max().orElse(0);
        List<Primitive> preferred = bestMatch > 0
                ? candidates.stream().filter(p -> overlap(p.affinityTags(), positiveEvidence) == bestMatch).toList()
                : candidates;

        String authorityKey = checkedScenario + "|" + checkedFaction + "|" + checkedInteraction + "|"
                + checkedHistoryEntry + "|" + checkedKind.name() + "|"
                + positiveEvidence.stream().sorted().collect(Collectors.joining(","));
        Primitive primitive = preferred.get(index(seed, authorityKey + "|primitive", preferred.size()));
        String cue = primitive.presentationCues().get(index(seed,
                authorityKey + "|" + primitive.id() + "|cue", primitive.presentationCues().size()));
        Set<String> matched = primitive.affinityTags().stream().filter(positiveEvidence::contains).sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new Selection(GENERATOR_VERSION, seed, checkedScenario, checkedFaction, checkedInteraction,
                checkedHistoryEntry, checkedKind, primitive, cue, matched);
    }

    public static Primitive requirePrimitive(String id) {
        String checked = stableId(id);
        return waveOne().stream().filter(p -> p.id().equals(checked)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown faction aftermath history primitive: " + checked));
    }

    private static Primitive p(String id, HistoryKind kind, String title, String historyRead,
                               String carryForwardPrompt, String reflection1, String reflection2, String reflection3,
                               Set<String> affinityTags, String cue1, String cue2, String antiOverclaimBoundary) {
        return new Primitive(id, kind, title, historyRead, carryForwardPrompt,
                List.of(reflection1, reflection2, reflection3), affinityTags, List.of(cue1, cue2), antiOverclaimBoundary);
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
        return values.stream().map(NightmareFactionAftermathHistoryCatalog::stableId)
                .sorted().collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
