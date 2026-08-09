package dev.spud.shadowslave.nightmare.content;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** DESIGN-only follow-up plans for already-resolved Nightmare investigation journal identities. */
public final class NightmareClueActionPlanCatalog {
    public static final String GENERATOR_VERSION = "nightmare-clue-action-plan-v1";

    private NightmareClueActionPlanCatalog() {}

    public enum Family { REVISIT, COMPARE, SEEK_SOURCE, TEST_ROUTE, PROTECT_EVIDENCE, DEFER }

    public record Primitive(String id, Family family, String title, String situationRead, String actionPrompt,
                            List<String> playerOptions, Set<String> affinityTags, List<String> presentationCues,
                            String antiOverclaimBoundary) {
        public Primitive {
            id = stableId(id);
            family = Objects.requireNonNull(family, "family");
            title = text(title, "title");
            situationRead = text(situationRead, "situationRead");
            actionPrompt = text(actionPrompt, "actionPrompt");
            playerOptions = exactTextList(playerOptions, 3, "playerOptions");
            affinityTags = nonEmptyTags(affinityTags);
            presentationCues = exactTextList(presentationCues, 2, "presentationCues");
            antiOverclaimBoundary = text(antiOverclaimBoundary, "antiOverclaimBoundary");
        }
    }

    public record Selection(String generatorVersion, long seed, String scenarioId, String actorContextId,
                            String journalEntryId, Primitive primitive, String presentationCue,
                            Set<String> matchedEvidenceTags) {
        public Selection {
            generatorVersion = text(generatorVersion, "generatorVersion");
            scenarioId = stableId(scenarioId);
            actorContextId = stableId(actorContextId);
            journalEntryId = stableId(journalEntryId);
            primitive = Objects.requireNonNull(primitive, "primitive");
            presentationCue = text(presentationCue, "presentationCue");
            matchedEvidenceTags = Set.copyOf(Objects.requireNonNull(matchedEvidenceTags, "matchedEvidenceTags"));
        }
    }

    public static List<Primitive> waveOne() {
        return List.of(
                p("revisit_fixed_reference", Family.REVISIT, "Return to the Fixed Reference", "A previous observation is useful enough to revisit, but the world may have changed since it was recorded.", "What can be checked again without assuming the old condition still holds?", "Return to the reference.", "Compare only observable changes.", "Withdraw if the route is no longer defensible.", Set.of("location","observation","record"), "The old note becomes a starting point, not a guarantee.", "Fresh observation is kept separate from the earlier record.", "Revisiting does not make an old route safe, current, or correct."),
                p("revisit_object", Family.REVISIT, "Reinspect the Object", "A physical object has enough unresolved detail to justify a second bounded inspection.", "Which property can be checked without assigning cause or culprit?", "Inspect the same feature again.", "Check position and condition.", "Leave cause unresolved.", Set.of("object","damage","verification"), "The second look targets one specific feature.", "The plan avoids turning damage into accusation.", "Reinspection cannot prove sabotage, authenticity, ownership, or guilt."),
                p("revisit_signal", Family.REVISIT, "Listen for the Pattern Again", "A prior signal was recorded, but one occurrence cannot establish sender, meaning, or recurrence.", "Can the pattern be observed again under comparable local conditions?", "Return to the listening point.", "Record recurrence or absence.", "Keep interpretation separate.", Set.of("signal","observation","uncertainty"), "The plan seeks repetition, not prophecy.", "Absence is recorded as absence rather than hidden meaning.", "A repeated or missing signal does not reveal intent or future danger."),
                p("revisit_statement", Family.REVISIT, "Return to the Statement", "A recorded account contains a bounded detail that can be revisited without turning the speaker into a suspect by default.", "Which exact detail deserves a follow-up question?", "Ask about one concrete detail.", "Compare wording without accusation.", "Record any refusal separately.", Set.of("testimony","source","verification"), "The follow-up stays narrow.", "The original statement remains visible beside the new answer.", "Changed wording does not automatically prove deception or guilt."),

                p("compare_sequences", Family.COMPARE, "Compare the Sequences", "Two records describe overlapping events in different orders.", "Where is the first concrete divergence?", "Align shared events.", "Mark the first disagreement.", "Seek one independent anchor.", Set.of("sequence","contradiction","record"), "The comparison narrows the disagreement.", "Shared facts and disputed order are displayed separately.", "Comparison does not decide which sequence is true."),
                p("compare_sources", Family.COMPARE, "Compare Independent Sources", "Two sources touch the same fact from different positions or chains of relay.", "What overlaps without assuming either source is authoritative?", "List shared claims.", "Mark source distance.", "Preserve unmatched details.", Set.of("source","testimony","verification"), "Agreement becomes corroboration rather than proof.", "Differences remain visible for later testing.", "Agreement does not certify truth, and disagreement does not certify lying."),
                p("compare_object_account", Family.COMPARE, "Compare Object and Account", "A physical trace and an account can be set side by side without granting either hidden truth authority.", "Which specific physical feature bears on which specific claim?", "Describe the feature.", "Restate the matching claim.", "Record the unresolved mismatch.", Set.of("object","testimony","contradiction"), "The comparison is limited to one feature and one claim.", "Cause and intent remain outside the card.", "This comparison cannot identify a culprit or certify authenticity."),
                p("compare_route_records", Family.COMPARE, "Compare Route Records", "Two route notes may describe different times, viewpoints, or conditions.", "Which fixed references survive both records?", "Align fixed references.", "Separate condition changes.", "Require fresh travel verification.", Set.of("route","location","record"), "Historical route knowledge is compared without becoming a live map oracle.", "Condition differences stay explicit.", "Matching route records do not guarantee present access or safety."),

                p("seek_second_witness", Family.SEEK_SOURCE, "Seek a Second Witness", "One account matters, but its limits are still visible.", "Who could independently observe the same bounded event?", "Find an independent witness.", "Ask the same narrow question.", "Keep agreement and conflict separate.", Set.of("testimony","source","corroboration"), "The plan looks for another vantage, not a majority vote.", "Secondhand agreement remains labelled by source.", "More witnesses do not automatically create truth or certainty thresholds."),
                p("seek_original_record", Family.SEEK_SOURCE, "Seek the Original Record", "A copied, relayed, or summarized record may be useful while still lacking direct provenance.", "Can the earlier version be located without assuming the copy is false?", "Trace the record backward.", "Preserve each version.", "Compare changes without assigning motive.", Set.of("record","source","preservation"), "Each version remains distinguishable.", "Provenance is improved only when actually found.", "Finding an earlier record does not automatically certify authenticity or authority."),
                p("seek_route_guide", Family.SEEK_SOURCE, "Seek Local Route Knowledge", "A route clue is actionable enough to ask someone with relevant local experience, but not enough to declare a safe path.", "Who has direct experience with this route or landmark?", "Ask for fixed references.", "Ask what conditions invalidate the route.", "Verify the route independently before use.", Set.of("route","location","source"), "Local knowledge becomes another bounded source.", "The plan asks about failure conditions as well as directions.", "A guide's account does not guarantee present safety or access."),
                p("seek_missing_link", Family.SEEK_SOURCE, "Seek the Missing Link", "The journal identifies one missing fact whose absence materially limits the next decision.", "Which source could directly supply that fact rather than another interpretation?", "Name the missing fact.", "Identify a plausible direct source.", "Stop if only speculation is available.", Set.of("gap","source","uncertainty"), "The search targets one missing fact.", "Speculation is not promoted merely because the gap matters.", "Seeking a source does not imply the source exists or will cooperate."),

                p("test_route_marker", Family.TEST_ROUTE, "Test the Route Marker", "A mark or trace suggests a route, but its meaning and current usability remain unresolved.", "Can the next segment be checked while retaining a safe return?", "Verify one segment.", "Keep a return reference.", "Abort on incompatible conditions.", Set.of("route","verification","location"), "The plan advances one bounded segment at a time.", "Retreat remains a valid outcome.", "Testing a route does not guarantee destination, safety, or scenario progress."),
                p("test_alternate_path", Family.TEST_ROUTE, "Test the Alternate Path", "Conflicting route evidence makes an alternate path worth checking without declaring either path correct.", "What is the smallest reversible test of the alternative?", "Check the first defensible landmark.", "Record obstruction or passage.", "Return before committing deeper.", Set.of("route","contradiction","uncertainty"), "The alternative is tested reversibly.", "A blocked segment is evidence about this attempt only.", "The alternate path is not automatically safer, intended, or canonical."),
                p("test_signal_location", Family.TEST_ROUTE, "Test the Signal Location", "A signal may be spatially useful even when its sender and meaning remain unknown.", "Can position or audibility be checked without following the signal blindly?", "Compare from two fixed points.", "Record change in direction or strength.", "Do not infer sender or intent.", Set.of("signal","location","verification"), "Spatial checking stays separate from interpretation.", "The signal remains an observation, not a command.", "This test cannot reveal a hidden sender, objective, or danger probability."),
                p("test_condition_boundary", Family.TEST_ROUTE, "Test the Condition Boundary", "Environmental or structural pressure may make movement possible only under some local conditions.", "Where is the nearest reversible boundary between observed safe-enough footing and unknown ground?", "Mark the current boundary.", "Probe only the next bounded section.", "Withdraw if conditions worsen.", Set.of("route","environment","observation"), "The plan makes uncertainty spatially explicit.", "No global safe-zone claim is created.", "A locally traversable section does not certify the wider route or future conditions."),

                p("protect_original", Family.PROTECT_EVIDENCE, "Protect the Original", "An original record or object may be altered by handling, weather, conflict, or well-meaning repair.", "What must remain unchanged for later comparison?", "Separate the original from working copies.", "Record unavoidable handling.", "Limit further alteration.", Set.of("preservation","record","object"), "The original remains distinguishable from later work.", "Handling changes are made visible.", "Protection does not certify authenticity, ownership, or chain of custody."),
                p("protect_contradiction", Family.PROTECT_EVIDENCE, "Keep Both Sides of the Contradiction", "A contradiction may be lost if later notes overwrite the less convenient version.", "What exact conflict needs to survive future editing?", "Retain both claims.", "Append later findings separately.", "Mark what remains unresolved.", Set.of("preservation","contradiction","record"), "The disagreement is preserved as evidence of uncertainty.", "Later resolution can be added without rewriting history.", "Preserving a contradiction does not decide truth or blame."),
                p("protect_sample", Family.PROTECT_EVIDENCE, "Protect the Bounded Sample", "A small sample is useful only if its limits and collection context survive with it.", "Which property and collection condition need to remain attached to the sample?", "Label the sample boundary.", "Record collection context.", "Avoid generalizing beyond it.", Set.of("preservation","sample","object"), "The sample keeps its limits beside it.", "Later interpretation cannot erase where it came from.", "A preserved sample does not establish rarity, universality, or supernatural properties."),
                p("protect_account", Family.PROTECT_EVIDENCE, "Protect the Original Account", "A witness account may be summarized later, but its original distinctions and omissions can still matter.", "Which parts must remain attributable to the witness rather than the investigator?", "Preserve the original wording or summary boundary.", "Mark firsthand and relayed detail.", "Append later checks separately.", Set.of("preservation","testimony","source"), "The witness account stays separable from analysis.", "Later corrections do not silently rewrite the source.", "Preservation does not certify memory accuracy, truthfulness, or allegiance."),

                p("defer_for_missing_fact", Family.DEFER, "Defer Until the Missing Fact Changes", "The next action would depend on a fact the investigation does not currently have.", "What exact missing fact would justify reopening the decision?", "Name the resume condition.", "Record the cost of waiting.", "Do not invent certainty to force action.", Set.of("gap","uncertainty","defer"), "Deferral is tied to an explicit resume condition.", "Waiting remains a choice with tradeoffs rather than free safety.", "Deferral does not freeze the world or guarantee the missing fact will appear."),
                p("defer_unsafe_test", Family.DEFER, "Defer the Unsafe Test", "A proposed verification would create disproportionate danger or destroy the evidence it is meant to test.", "Can the question be preserved without performing this test now?", "Record why the test is unsafe.", "Seek a lower-risk alternative.", "Leave the claim unresolved.", Set.of("uncertainty","preservation","safety"), "The plan keeps the question open without forcing a hazardous experiment.", "Unresolved remains an acceptable state.", "Declining a test does not prove or disprove the underlying claim."),
                p("defer_social_pressure", Family.DEFER, "Defer Under Social Pressure", "Pressure for an immediate accusation or commitment exceeds what the bounded evidence can support.", "What can be stated now without turning uncertainty into a verdict?", "State only established facts.", "Name the unresolved point.", "Decline a forced conclusion.", Set.of("testimony","uncertainty","authority"), "The card separates urgency from evidentiary authority.", "A refusal to accuse remains a valid investigative action.", "Deferral does not grant immunity, settle allegiance, or block future scenario events."),
                p("defer_stale_route", Family.DEFER, "Defer the Stale Route", "A preserved route record is too old or condition-dependent to justify immediate travel.", "What fresh observation would make this route actionable again?", "Mark the route as stale.", "Seek a current reference.", "Use another plan until reverified.", Set.of("route","record","defer"), "Historical usefulness is retained without pretending freshness.", "The resume condition is current route evidence.", "Deferral does not imply the route is dangerous, closed, or permanently invalid.")
        );
    }

    public static Selection compose(long seed, String scenarioId, String actorContextId, String journalEntryId,
                                    Set<Family> allowedFamilies, Map<String, Integer> evidence) {
        String scenario = stableId(scenarioId);
        String actor = stableId(actorContextId);
        String journal = stableId(journalEntryId);
        Set<Family> families = Set.copyOf(Objects.requireNonNull(allowedFamilies, "allowedFamilies"));
        if (families.isEmpty()) throw new IllegalArgumentException("allowedFamilies must not be empty");
        Set<String> positive = positiveEvidenceTags(evidence);
        List<Primitive> eligible = waveOne().stream().filter(p -> families.contains(p.family())).toList();
        List<Primitive> preferred = eligible.stream().filter(p -> p.affinityTags().stream().anyMatch(positive::contains)).toList();
        List<Primitive> pool = preferred.isEmpty() ? eligible : preferred;
        if (pool.isEmpty()) throw new IllegalArgumentException("No compatible clue action plans");
        Primitive primitive = pool.get(index(seed, scenario, actor, journal, "primitive", pool.size()));
        String cue = primitive.presentationCues().get(index(seed, scenario, actor, journal, primitive.id(), 2));
        Set<String> matched = primitive.affinityTags().stream().filter(positive::contains).collect(Collectors.toUnmodifiableSet());
        return new Selection(GENERATOR_VERSION, seed, scenario, actor, journal, primitive, cue, matched);
    }

    public static Primitive byId(String id) {
        String stable = stableId(id);
        return waveOne().stream().filter(p -> p.id().equals(stable)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown clue action plan: " + stable));
    }

    private static Primitive p(String id, Family family, String title, String read, String prompt,
                               String a, String b, String c, Set<String> tags, String cue1, String cue2, String boundary) {
        return new Primitive(id, family, title, read, prompt, List.of(a,b,c), tags, List.of(cue1,cue2), boundary);
    }

    private static Set<String> positiveEvidenceTags(Map<String, Integer> evidence) {
        Objects.requireNonNull(evidence, "evidence");
        List<String> tags = new ArrayList<>();
        evidence.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> {
            String tag = stableId(e.getKey());
            int value = Objects.requireNonNull(e.getValue(), "evidence value");
            if (value < 0) throw new IllegalArgumentException("Evidence values must not be negative");
            if (value > 0) tags.add(tag);
        });
        return Set.copyOf(tags);
    }

    private static int index(long seed, String scenario, String actor, String journal, String salt, int bound) {
        String material = GENERATOR_VERSION + "|" + seed + "|" + scenario + "|" + actor + "|" + journal + "|" + salt;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(material.getBytes(StandardCharsets.UTF_8));
            long value = 0L;
            for (int i = 0; i < Long.BYTES; i++) value = (value << 8) | (digest[i] & 0xffL);
            return (int)Math.floorMod(value, bound);
        } catch (NoSuchAlgorithmException e) { throw new IllegalStateException("SHA-256 unavailable", e); }
    }

    private static String stableId(String value) {
        String normalized = text(value, "id").toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z0-9][a-z0-9_-]*")) throw new IllegalArgumentException("Invalid stable id: " + value);
        return normalized;
    }
    private static String text(String value, String field) {
        String normalized = Objects.requireNonNull(value, field).trim();
        if (normalized.isEmpty()) throw new IllegalArgumentException(field + " must not be blank");
        return normalized;
    }
    private static List<String> exactTextList(List<String> values, int size, String field) {
        Objects.requireNonNull(values, field);
        if (values.size() != size) throw new IllegalArgumentException(field + " must contain exactly " + size + " entries");
        return values.stream().map(v -> text(v, field)).toList();
    }
    private static Set<String> nonEmptyTags(Set<String> tags) {
        Objects.requireNonNull(tags, "affinityTags");
        if (tags.isEmpty()) throw new IllegalArgumentException("affinityTags must not be empty");
        return tags.stream().map(NightmareClueActionPlanCatalog::stableId).sorted(Comparator.naturalOrder()).collect(Collectors.toUnmodifiableSet());
    }
}
