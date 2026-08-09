package dev.spud.shadowslave.nightmare.content;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Authored DESIGN primitives for presenting bounded evidence/testimony links inside an already-resolved Nightmare.
 * This catalogue never decides whether a claim is true, forged, sufficient, or accepted by a ResolutionGraph.
 */
public final class NightmareEvidenceLinkCatalog {
    public static final String GENERATOR_VERSION = "nightmare-evidence-link-v1";

    private NightmareEvidenceLinkCatalog() {}

    public enum Family {
        PHYSICAL_RECORD,
        WITNESS_ACCOUNT,
        SIGNAL_TRACE,
        ROUTE_TRACE,
        DAMAGED_OBJECT,
        CONTRADICTION
    }

    public record Primitive(
            String id,
            Family family,
            String title,
            String evidenceRead,
            String verificationQuestion,
            List<String> playerResponses,
            Set<String> affinityTags,
            List<String> presentationCues,
            String antiOverclaimBoundary
    ) {
        public Primitive {
            id = stableId(id);
            family = Objects.requireNonNull(family, "family");
            title = text(title, "title");
            evidenceRead = text(evidenceRead, "evidenceRead");
            verificationQuestion = text(verificationQuestion, "verificationQuestion");
            playerResponses = exactTextList(playerResponses, 3, "playerResponses");
            affinityTags = nonEmptyTags(affinityTags, "affinityTags");
            presentationCues = exactTextList(presentationCues, 2, "presentationCues");
            antiOverclaimBoundary = text(antiOverclaimBoundary, "antiOverclaimBoundary");
        }
    }

    public record Selection(
            String generatorVersion,
            long seed,
            String scenarioId,
            String actorContextId,
            Primitive primitive,
            String presentationCue,
            Set<String> matchedEvidenceTags
    ) {
        public Selection {
            generatorVersion = text(generatorVersion, "generatorVersion");
            scenarioId = stableId(scenarioId);
            actorContextId = stableId(actorContextId);
            primitive = Objects.requireNonNull(primitive, "primitive");
            presentationCue = text(presentationCue, "presentationCue");
            matchedEvidenceTags = Set.copyOf(Objects.requireNonNull(matchedEvidenceTags, "matchedEvidenceTags"));
        }
    }

    public static List<Primitive> waveOne() {
        List<Primitive> primitives = List.of(
                p("dated_shift_ledger", Family.PHYSICAL_RECORD, "Dated Shift Ledger",
                        "A written roster fixes names, duties, and an order of work to a particular recorded sequence.",
                        "Which parts can be checked against people, places, or surviving marks?",
                        List.of("Compare names against those present.", "Check the sequence against physical traces.", "Record the mismatch without deciding why it exists."),
                        Set.of("record", "sequence", "names"),
                        List.of("The page is creased where someone kept returning to the same line.", "Several hands touched the ledger; only some marks are part of the writing."),
                        "A written record is evidence, not automatic proof that its author was truthful or that the document is authentic."),
                p("amended_order", Family.PHYSICAL_RECORD, "Amended Order",
                        "A formal instruction contains a visible correction that changes who was sent where or when.",
                        "Was the correction made before events, after them, or by someone without authority?",
                        List.of("Compare ink, pressure, or material differences.", "Ask who witnessed the amendment.", "Treat timing and authority as unresolved until corroborated."),
                        Set.of("record", "authority", "alteration"),
                        List.of("The correction is careful enough to invite inspection rather than hide from it.", "The changed line is obvious; its legitimacy is not."),
                        "The catalogue does not detect forgery, establish authorship, or decide whether an amendment was lawful."),
                p("sealed_message", Family.PHYSICAL_RECORD, "Sealed Message",
                        "A closed or previously sealed message can link a sender, recipient, and stated intention without proving any of them acted accordingly.",
                        "What does the message actually establish, and what remains only a claim?",
                        List.of("Preserve the seal or damage state.", "Compare its details with independent testimony.", "Separate stated intent from observed action."),
                        Set.of("record", "message", "intent"),
                        List.of("The seal tells a history of handling before the words are considered.", "The message is specific enough to test but not enough to settle events by itself."),
                        "A message cannot prove hidden motive, successful delivery, obedience, or the truth of its contents."),
                p("inventory_tally", Family.PHYSICAL_RECORD, "Inventory Tally",
                        "A count of tools, rations, weapons, or cargo creates a bounded comparison point for later losses and claims.",
                        "Does the remaining stock fit the recorded count and the known sequence of use?",
                        List.of("Count what remains.", "Ask who had access between counts.", "Note unexplained differences without assigning guilt."),
                        Set.of("record", "resource", "access"),
                        List.of("The numbers are mundane, which makes deviations easier to notice and harder to interpret.", "Missing stock narrows a question; it does not answer it."),
                        "A tally does not identify a thief, prove sabotage, or establish the reason an item is missing."),

                p("firsthand_sequence", Family.WITNESS_ACCOUNT, "Firsthand Sequence",
                        "A witness describes what they personally saw in order, including where their view began and ended.",
                        "Which details are direct observation and which were learned afterward?",
                        List.of("Ask for the sequence without interpretation.", "Mark gaps in line of sight or time.", "Compare one concrete detail with another source."),
                        Set.of("testimony", "sequence", "observation"),
                        List.of("The account becomes clearer when the witness separates seeing from assuming.", "The strongest part of the testimony is its stated boundary."),
                        "Firsthand testimony can still be mistaken, incomplete, biased, coerced, or false; this primitive does not adjudicate truth."),
                p("relayed_account", Family.WITNESS_ACCOUNT, "Relayed Account",
                        "The speaker reports information received from someone else rather than claiming direct observation.",
                        "Can the chain of speakers be identified and checked without silently upgrading hearsay into fact?",
                        List.of("Ask who first observed it.", "Preserve the exact relay chain.", "Keep the claim provisional until another source supports it."),
                        Set.of("testimony", "hearsay", "source"),
                        List.of("Each retelling adds a person who can be checked and another place meaning can drift.", "The useful fact may be who passed the claim onward, not whether the claim is already true."),
                        "A relayed account is not automatically false and is never automatically verified by repetition."),
                p("limited_view", Family.WITNESS_ACCOUNT, "Limited View",
                        "A witness is explicit about obstruction, distance, darkness, noise, injury, or another limit on perception.",
                        "What can still be supported inside that admitted limit?",
                        List.of("Keep only details the witness could plausibly perceive.", "Seek another angle on the obscured portion.", "Preserve uncertainty instead of filling the gap."),
                        Set.of("testimony", "uncertainty", "observation"),
                        List.of("Admitted uncertainty makes the usable portion of an account easier to isolate.", "The missing view remains missing even when the rest sounds convincing."),
                        "Perception limits do not prove honesty, deception, supernatural interference, or exact sensory capability."),
                p("interested_witness", Family.WITNESS_ACCOUNT, "Interested Witness",
                        "The witness has a visible stake in how events are understood: duty, kinship, fear, debt, rivalry, or survival.",
                        "Which parts can be checked independently without assuming the stake makes the speaker truthful or deceptive?",
                        List.of("Name the visible stake without moral judgment.", "Verify one neutral detail first.", "Keep motive and factual accuracy as separate questions."),
                        Set.of("testimony", "motive", "relationship"),
                        List.of("The speaker has something to lose, but that fact does not decide the account.", "Interest is context for verification, not a built-in lie detector."),
                        "This primitive never converts motive, fear, loyalty, or rivalry into a truthfulness score."),

                p("repeated_bell_code", Family.SIGNAL_TRACE, "Repeated Bell Code",
                        "A repeated audible signal follows a recognizable local pattern closely enough to compare with known conventions.",
                        "Was the pattern heard completely, and does anyone else recognize the same sequence?",
                        List.of("Repeat the sequence back before interpreting it.", "Ask another listener to identify it independently.", "Treat meaning as provisional if part of the signal was missed."),
                        Set.of("signal", "sound", "sequence"),
                        List.of("The pattern is distinct from ordinary noise but still needs a local interpretation.", "A familiar rhythm invites recognition; it does not guarantee the sender."),
                        "Recognizing a signal pattern does not prove sender identity, authenticity, danger, prophecy, or successful receipt."),
                p("smoke_pattern", Family.SIGNAL_TRACE, "Smoke Pattern",
                        "Timed smoke, light, or another visible signal creates a sequence that can be recorded from a distance.",
                        "Could weather, obstruction, or an interrupted sender have changed what was seen?",
                        List.of("Record timing before assigning meaning.", "Compare from a second viewpoint.", "Separate the visible pattern from assumptions about the sender."),
                        Set.of("signal", "visual", "distance"),
                        List.of("The signal can be copied accurately even when its source remains uncertain.", "Distance preserves the pattern better than the surrounding context."),
                        "A visible pattern cannot by itself prove who sent it, why, or whether it corresponds to current world state."),
                p("broken_signal", Family.SIGNAL_TRACE, "Broken Signal",
                        "A known signal starts but ends early, skips a segment, or arrives in an unexpected form.",
                        "Does the break indicate interruption, damage, error, deliberate alteration, or something still unknown?",
                        List.of("Preserve the incomplete sequence exactly.", "Inspect the signaling point if reachable.", "Do not treat absence of a segment as a coded message by default."),
                        Set.of("signal", "interruption", "uncertainty"),
                        List.of("What is missing is noticeable because the expected pattern is known.", "The break creates a question, not an answer."),
                        "An incomplete signal is not automatic evidence of attack, betrayal, death, sabotage, or supernatural interference."),
                p("counter_signal", Family.SIGNAL_TRACE, "Counter-Signal",
                        "Two nearby sources produce signals that appear to answer or contradict one another.",
                        "Are they part of one exchange, competing instructions, imitation, or unrelated activity?",
                        List.of("Separate each source and sequence.", "Check timing before assuming response.", "Ask which convention each source claims to use."),
                        Set.of("signal", "contradiction", "source"),
                        List.of("The second signal changes how the first must be investigated, not what it automatically means.", "Two messages can conflict without either being fabricated."),
                        "The catalogue does not identify the legitimate signal, select an authority, or infer hostile spoofing."),

                p("double_marked_route", Family.ROUTE_TRACE, "Double-Marked Route",
                        "A route carries two independent marking systems that overlap for part of the path and diverge later.",
                        "Which marks are older, which are local, and which can be tied to known travelers?",
                        List.of("Map the overlap before following either divergence.", "Check wear and placement rather than color alone.", "Keep both possible routes open until another reference resolves them."),
                        Set.of("route", "marking", "contradiction"),
                        List.of("The path itself preserves more than one travel history.", "Agreement along the easy stretch makes the later divergence more important."),
                        "Route marks do not reveal a correct quest path, safe destination, creator, or supernatural guidance."),
                p("fresh_return_marks", Family.ROUTE_TRACE, "Fresh Return Marks",
                        "Tracks, chalk, moved stones, or other mundane signs indicate that someone recently traversed a route in both directions.",
                        "Can direction, age, or identity be constrained without pretending the trace names the traveler?",
                        List.of("Compare outbound and return signs.", "Look for a second fixed reference.", "Record recency as relative rather than exact if evidence is weak."),
                        Set.of("route", "trace", "movement"),
                        List.of("The useful information is that the path saw recent movement, not who survived it.", "A return trace can challenge a witness account without identifying the traveler."),
                        "This primitive does not provide supernatural tracking, exact timestamps, identity recognition, or guaranteed route safety."),
                p("blocked_detour_trace", Family.ROUTE_TRACE, "Blocked Detour Trace",
                        "A normally secondary path shows recent use around a blocked, watched, flooded, or damaged primary route.",
                        "Was the detour chosen for safety, secrecy, necessity, or another reason?",
                        List.of("Inspect why the primary route became unattractive.", "Compare the detour with known local movement.", "Do not infer intent from route choice alone."),
                        Set.of("route", "detour", "pressure"),
                        List.of("Movement around an obstacle is easy to see; the reason for it is not.", "The detour is evidence of choice under pressure, not proof of conspiracy."),
                        "Using a detour does not prove wrongdoing, allegiance, pursuit, escape, or guaranteed passage."),
                p("erased_waymark", Family.ROUTE_TRACE, "Erased Waymark",
                        "A route marker has been scraped, covered, moved, or otherwise made harder to read while leaving signs that alteration occurred.",
                        "Can the previous mark be reconstructed at all, and who could have changed it?",
                        List.of("Preserve the alteration before restoring anything.", "Check nearby markers for the same pattern.", "Treat deliberate alteration as separate from its purpose."),
                        Set.of("route", "alteration", "marking"),
                        List.of("Erasure leaves its own trace even when the original instruction is gone.", "The missing mark may matter more than any guessed reconstruction."),
                        "Visible alteration does not identify the editor, reveal the original message, or prove malicious intent."),

                p("tool_broken_under_load", Family.DAMAGED_OBJECT, "Tool Broken Under Load",
                        "A damaged tool, fastener, weapon, or support can be inspected for where and how it failed during use.",
                        "Does the break fit ordinary strain, poor maintenance, prior damage, deliberate weakening, or an unresolved cause?",
                        List.of("Preserve the break surfaces.", "Compare with an undamaged equivalent.", "Ask when the object was last known intact."),
                        Set.of("object", "damage", "maintenance"),
                        List.of("The fracture records a failure more clearly than it records a cause.", "Damage can contradict a story without naming the person responsible."),
                        "Object damage does not automatically prove sabotage, exact force, attacker identity, or supernatural cause."),
                p("cut_binding", Family.DAMAGED_OBJECT, "Cut Binding",
                        "Rope, cloth, straps, seals, or bindings show a localized severing distinct enough to compare with wear elsewhere.",
                        "Was the cut made before, during, or after the event being investigated?",
                        List.of("Compare the severed area with ordinary wear.", "Check whether the object was still functional afterward.", "Keep timing unresolved if no independent reference exists."),
                        Set.of("object", "alteration", "timing"),
                        List.of("The cut is real; its place in the sequence still needs evidence.", "A severed binding narrows the mechanism without assigning motive."),
                        "This primitive does not identify a tool, culprit, exact timing, or intent from the cut alone."),
                p("repaired_damage", Family.DAMAGED_OBJECT, "Repaired Damage",
                        "An object carries a repair over older damage, showing that failure and continued use were separated in time.",
                        "Who knew the object had failed, and what changed after the repair?",
                        List.of("Identify repair material and local availability.", "Ask who could perform the repair.", "Compare later claims with the fact that the damage was known to someone."),
                        Set.of("object", "repair", "knowledge"),
                        List.of("The repair proves someone responded to damage; it does not reveal their identity by itself.", "Continued use creates a second chapter in the object's history."),
                        "A repair does not establish ownership, competence, guilt, or whether the repaired object later failed again."),
                p("misplaced_component", Family.DAMAGED_OBJECT, "Misplaced Component",
                        "A component is present where it should not be, absent from its usual assembly, or fitted in a way that changes ordinary operation.",
                        "Is this damage, emergency adaptation, maintenance error, deliberate alteration, or a configuration not yet understood?",
                        List.of("Document position before moving it.", "Compare with another assembly or written record.", "Ask who last used or serviced the object."),
                        Set.of("object", "configuration", "access"),
                        List.of("The component creates a concrete discrepancy without supplying its explanation.", "A wrong-looking arrangement may still have been intentional for reasons not yet known."),
                        "The catalogue cannot infer sabotage, correct engineering, ownership, or intent from configuration alone."),

                p("time_conflict", Family.CONTRADICTION, "Time Conflict",
                        "Two accounts or records place the same person, signal, or event in incompatible portions of the sequence.",
                        "Is one timestamp approximate, one account mistaken, or is the contradiction genuine?",
                        List.of("Reduce both claims to the narrowest stated timing.", "Find a fixed event both sources reference.", "Preserve the conflict if it cannot yet be resolved."),
                        Set.of("contradiction", "time", "testimony"),
                        List.of("The contradiction is useful even before either side is judged.", "A fixed shared event can turn vague timing into something testable."),
                        "Conflicting timing does not identify a liar, establish intent, or provide exact chronology without corroboration."),
                p("location_conflict", Family.CONTRADICTION, "Location Conflict",
                        "A record, trace, or witness places a person or object somewhere that conflicts with another source.",
                        "Can travel time, route access, or observation limits make both accounts possible?",
                        List.of("Check the route between claimed locations.", "Separate direct sighting from inferred presence.", "Do not collapse uncertainty into guilt."),
                        Set.of("contradiction", "location", "route"),
                        List.of("The map of claims is inconsistent before the people are judged.", "A location conflict may expose a mistaken assumption rather than a deliberate lie."),
                        "This primitive does not calculate travel time, prove impossibility, or identify deception."),
                p("authority_conflict", Family.CONTRADICTION, "Authority Conflict",
                        "Two instructions, witnesses, or records claim different people had authority to make the same decision.",
                        "What evidence establishes scope of authority at that moment rather than rank in general?",
                        List.of("Ask each source what authority they rely on.", "Look for a dated delegation or local custom.", "Keep command legitimacy separate from whether the order was wise."),
                        Set.of("contradiction", "authority", "record"),
                        List.of("Competing authority claims can both be sincere and still be incompatible.", "A title may be relevant without settling who controlled this specific decision."),
                        "The catalogue does not decide lawful command, faction legitimacy, obedience, guilt, or social reputation."),
                p("object_account_conflict", Family.CONTRADICTION, "Object-Account Conflict",
                        "The physical condition of an object does not fit a straightforward reading of a witness or written account.",
                        "Does the object disprove the account, expose an omitted step, or reveal that the object itself is being misunderstood?",
                        List.of("State the physical discrepancy without accusation.", "Ask the witness to account for it.", "Seek another object or observer before escalating certainty."),
                        Set.of("contradiction", "object", "testimony"),
                        List.of("Material evidence can challenge a story while still requiring interpretation.", "The strongest next step is often another comparison rather than a verdict."),
                        "A discrepancy does not automatically prove a lie, forgery, guilt, exact causation, or a canonical truth-detection mechanic.")
        );
        validate(primitives);
        return primitives;
    }

    public static Selection compose(long seed, String scenarioId, String actorContextId,
                                    Set<Family> allowedFamilies, Map<String, Integer> evidence) {
        String checkedScenario = stableId(scenarioId);
        String checkedActor = stableId(actorContextId);
        Set<Family> checkedFamilies = nonEmptyFamilies(allowedFamilies);
        Set<String> positiveEvidence = positiveEvidenceTags(evidence);

        List<Primitive> candidates = waveOne().stream()
                .filter(p -> checkedFamilies.contains(p.family()))
                .sorted(Comparator.comparing(Primitive::id))
                .toList();
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No evidence-link primitives for allowed families");
        }

        List<Primitive> preferred = candidates.stream()
                .filter(p -> p.affinityTags().stream().anyMatch(positiveEvidence::contains))
                .toList();
        List<Primitive> pool = preferred.isEmpty() ? candidates : preferred;

        String context = checkedScenario + "|" + checkedActor + "|" + checkedFamilies.stream()
                .map(Enum::name).sorted().collect(Collectors.joining(",")) + "|" + positiveEvidence.stream()
                .sorted().collect(Collectors.joining(","));
        Primitive primitive = pool.get(index(seed, context + "|primitive", pool.size()));
        String cue = primitive.presentationCues().get(index(seed, context + "|cue|" + primitive.id(), primitive.presentationCues().size()));
        Set<String> matched = primitive.affinityTags().stream().filter(positiveEvidence::contains).collect(Collectors.toUnmodifiableSet());
        return new Selection(GENERATOR_VERSION, seed, checkedScenario, checkedActor, primitive, cue, matched);
    }

    private static Primitive p(String id, Family family, String title, String read, String question,
                               List<String> responses, Set<String> tags, List<String> cues, String boundary) {
        return new Primitive(id, family, title, read, question, responses, tags, cues, boundary);
    }

    private static void validate(List<Primitive> primitives) {
        HashSet<String> ids = new HashSet<>();
        for (Primitive primitive : primitives) {
            if (!ids.add(primitive.id())) {
                throw new IllegalArgumentException("Duplicate evidence-link primitive id: " + primitive.id());
            }
        }
    }

    private static Set<Family> nonEmptyFamilies(Set<Family> source) {
        Set<Family> result = EnumSet.copyOf(Objects.requireNonNull(source, "allowedFamilies"));
        if (result.isEmpty()) {
            throw new IllegalArgumentException("allowedFamilies cannot be empty");
        }
        return Set.copyOf(result);
    }

    private static Set<String> positiveEvidenceTags(Map<String, Integer> evidence) {
        Objects.requireNonNull(evidence, "evidence");
        HashSet<String> result = new HashSet<>();
        for (Map.Entry<String, Integer> entry : evidence.entrySet()) {
            String tag = stableId(entry.getKey());
            Integer value = Objects.requireNonNull(entry.getValue(), "evidence value");
            if (value < 0) {
                throw new IllegalArgumentException("evidence cannot be negative");
            }
            if (value > 0) {
                result.add(tag);
            }
        }
        return Set.copyOf(result);
    }

    private static int index(long seed, String context, int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("bound must be positive");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((GENERATOR_VERSION + "|" + seed + "|" + context).getBytes(StandardCharsets.UTF_8));
            long value = 0L;
            for (int i = 0; i < Long.BYTES; i++) {
                value = (value << 8) | (hash[i] & 0xffL);
            }
            return Math.floorMod(value, bound);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required by the Java runtime", exception);
        }
    }

    private static String stableId(String value) {
        String checked = text(value, "id").toLowerCase(Locale.ROOT);
        if (!checked.matches("[a-z0-9_]+")) {
            throw new IllegalArgumentException("id must contain only lowercase letters, numbers and underscores");
        }
        return checked;
    }

    private static String text(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }

    private static Set<String> nonEmptyTags(Set<String> source, String name) {
        HashSet<String> result = new HashSet<>();
        for (String value : Objects.requireNonNull(source, name)) {
            result.add(stableId(value));
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be empty");
        }
        return Set.copyOf(result);
    }

    private static List<String> exactTextList(List<String> source, int expected, String name) {
        Objects.requireNonNull(source, name);
        if (source.size() != expected) {
            throw new IllegalArgumentException(name + " must contain exactly " + expected + " entries");
        }
        ArrayList<String> result = new ArrayList<>(expected);
        for (String value : source) {
            result.add(text(value, name));
        }
        return List.copyOf(result);
    }
}
