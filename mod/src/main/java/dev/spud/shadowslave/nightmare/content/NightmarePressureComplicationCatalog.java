package dev.spud.shadowslave.nightmare.content;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * DESIGN: authored pressure/complication primitives for already-resolved
 * Nightmare scenarios.
 *
 * <p>This catalogue never chooses a Nightmare, mutates a {@code ResolutionGraph},
 * calculates difficulty, or decides whether a complication actually occurs.
 * The Java core must first resolve scenario identity and any authoritative event;
 * this class only composes bounded player-facing pressure content from families
 * explicitly allowed by that caller.</p>
 */
public final class NightmarePressureComplicationCatalog {
    public static final String GENERATOR_VERSION = "nightmare-pressure-v1";

    private static final List<Primitive> PRIMITIVES = List.of(
            primitive("shelter_failing", "Shelter Failing", Family.ENVIRONMENTAL_DETERIORATION,
                    "A place that was merely uncomfortable is becoming unsafe to remain in.",
                    "Do you reinforce the position, move before it worsens, or accept exposure to finish the current task?",
                    List.of("reinforce temporarily", "relocate deliberately", "finish the task under worsening conditions"),
                    Set.of("shelter", "exposure", "structure"),
                    List.of("Dust falls from seams that were quiet moments ago.", "The refuge no longer sounds as solid as it first appeared."),
                    "Does not define collapse timing, damage, safe-zone rules, or a canonical environmental difficulty formula."),
            primitive("waterline_rising", "Waterline Rising", Family.ENVIRONMENTAL_DETERIORATION,
                    "Water is taking away usable ground and turning a known route into a changing hazard.",
                    "Do you cross now, seek higher ground, or abandon what lies beyond the water?",
                    List.of("cross while the route remains legible", "move to higher ground", "leave the threatened objective behind"),
                    Set.of("water", "flood", "route"),
                    List.of("A familiar marker is now only partly above the water.", "Debris begins moving where the floor had been still."),
                    "Does not define flood rate, drowning rules, route closure probability, or automatic failure."),
            primitive("air_turning_hostile", "Air Turning Hostile", Family.ENVIRONMENTAL_DETERIORATION,
                    "Dust, smoke, spores, or another local condition is making continued exposure harder to tolerate.",
                    "Do you shorten the task, improve your protection, or find another approach?",
                    List.of("reduce exposure time", "improvise protection", "change approach"),
                    Set.of("air", "visibility", "exposure"),
                    List.of("Each breath carries more of the place with it.", "The distance is disappearing behind a thickening veil."),
                    "Does not establish toxicity, status effects, exposure thresholds, or a universal survival timer."),
            primitive("cold_deepening", "Cold Deepening", Family.ENVIRONMENTAL_DETERIORATION,
                    "The environment is becoming less forgiving while the central conflict remains unresolved.",
                    "Do you spend time finding protection, keep moving, or give up a slower obligation?",
                    List.of("seek protection", "maintain movement", "drop a slower obligation"),
                    Set.of("cold", "weather", "endurance"),
                    List.of("Fine movement is becoming harder to ignore.", "The wind has changed from discomfort into a decision."),
                    "Does not define temperature curves, stamina loss, frostbite mechanics, or canonical weather progression."),

            primitive("false_route_marker", "False Route Marker", Family.MISINFORMATION,
                    "A marker points somewhere plausible, but its placement conflicts with another piece of local evidence.",
                    "Do you trust it, verify it, or deliberately choose the slower known route?",
                    List.of("follow provisionally", "cross-check the marker", "use the slower verified route"),
                    Set.of("route", "marker", "deception"),
                    List.of("The mark is convincing until you notice what is missing beside it.", "Someone wanted this direction to look obvious."),
                    "Does not provide lie detection, truth scoring, perfect route knowledge, or a canonical deception mechanic."),
            primitive("copied_signal", "Copied Signal", Family.MISINFORMATION,
                    "A familiar signal appears in the wrong context, leaving its source or intent uncertain.",
                    "Do you answer, observe for a second confirming sign, or stay silent and reposition?",
                    List.of("answer cautiously", "wait for corroboration", "stay silent and move"),
                    Set.of("signal", "sound", "deception"),
                    List.of("The pattern is right; the circumstances are not.", "Recognition comes before confidence."),
                    "Does not prove an imitator, identify a creature or NPC, or define a canonical signal-authentication rule."),
            primitive("stories_disagree", "Stories Disagree", Family.MISINFORMATION,
                    "Two accounts of the same event cannot both be complete as told.",
                    "Do you confront the disagreement, seek physical evidence, or act on the overlap between both accounts?",
                    List.of("question the contradiction", "seek independent evidence", "act only on shared facts"),
                    Set.of("witness", "social", "evidence"),
                    List.of("The second telling changes one detail that matters.", "Agreement ends exactly where the risk begins."),
                    "Does not decide who is lying, assign guilt, quantify credibility, or grant supernatural truth evaluation."),
            primitive("instruction_outlived_context", "Instruction Outlived Its Context", Family.MISINFORMATION,
                    "An old order or plan may have been reasonable when written but no longer matches the current scene.",
                    "Do you obey the instruction, reinterpret its purpose, or discard it in favour of current evidence?",
                    List.of("follow the written instruction", "preserve the intent but change the method", "act on current conditions"),
                    Set.of("orders", "history", "evidence"),
                    List.of("The instruction is clear; the world around it has changed.", "Nothing on the page says when it stopped being safe."),
                    "Does not reveal historical truth, establish author intent, or define when old information becomes invalid."),

            primitive("warn_or_rescue", "Warn or Rescue", Family.DIVIDED_OBLIGATION,
                    "One obligation calls for immediate help while another requires carrying information farther.",
                    "Which responsibility can you leave unfinished, and what consequence are you willing to accept?",
                    List.of("stay with those in immediate danger", "carry the warning onward", "attempt a costly handoff"),
                    Set.of("warning", "rescue", "duty"),
                    List.of("Both needs are real, and neither waits politely for the other.", "The route ahead and the voices behind demand different answers."),
                    "Does not rank moral choices, guarantee survival, or define canonical appraisal weight for sacrifice or duty."),
            primitive("hold_or_pursue", "Hold or Pursue", Family.DIVIDED_OBLIGATION,
                    "Protecting a position conflicts with chasing a moving threat or opportunity.",
                    "Do you preserve what you have, pursue what is leaving, or entrust one responsibility to someone else?",
                    List.of("hold the position", "pursue the moving objective", "delegate one obligation"),
                    Set.of("defense", "pursuit", "duty"),
                    List.of("The safer choice for the position is the riskier choice for the chase.", "Staying and following can no longer be the same plan."),
                    "Does not determine NPC competence, threat movement, objective value, or a canonical duty hierarchy."),
            primitive("evidence_or_people", "Evidence or People", Family.DIVIDED_OBLIGATION,
                    "Preserving proof or knowledge now competes with moving vulnerable people out of danger.",
                    "Do you save the record, save time for the evacuation, or split the work and accept reduced certainty?",
                    List.of("preserve the evidence", "prioritise evacuation", "divide attention"),
                    Set.of("evidence", "rescue", "history"),
                    List.of("What can prove what happened is not as easy to move as the people who need to leave.", "A complete account and a complete evacuation are pulling in opposite directions."),
                    "Does not make evidence objectively true, guarantee casualties, or assign canonical appraisal value to either choice."),
            primitive("supplies_or_passage", "Supplies or Passage", Family.DIVIDED_OBLIGATION,
                    "Keeping essential material makes a difficult crossing slower or less flexible.",
                    "Do you carry the burden, abandon part of it, or search for another way through?",
                    List.of("keep the supplies", "leave some behind", "seek another passage"),
                    Set.of("supplies", "route", "burden"),
                    List.of("What seemed portable on safe ground is now shaping the route.", "Every useful load has become part of the crossing problem."),
                    "Does not define encumbrance, item value, supply consumption, or a canonical resource-scarcity equation."),

            primitive("light_running_low", "Light Running Low", Family.RESOURCE_LOSS,
                    "A limited source of visibility or signalling can no longer be treated as effectively endless.",
                    "Do you conserve it, spend it to resolve uncertainty now, or continue without it?",
                    List.of("conserve the remaining light", "spend light for immediate information", "continue without dependable light"),
                    Set.of("light", "visibility", "signal"),
                    List.of("The light still works, which makes deciding when to use it harder.", "There is enough illumination for a choice, not enough for complacency."),
                    "Does not define fuel duration, darkness penalties, Memory behaviour, or a canonical resource timer."),
            primitive("tool_damaged", "Tool Damaged", Family.RESOURCE_LOSS,
                    "A useful tool still functions imperfectly, changing which approaches remain practical.",
                    "Do you repair it, improvise around the damage, or abandon the task that depended on it?",
                    List.of("attempt a field repair", "improvise another method", "drop the tool-dependent task"),
                    Set.of("tool", "repair", "precision"),
                    List.of("It has not failed completely; that would be a simpler problem.", "The next use might work, but no longer deserves blind trust."),
                    "Does not establish durability values, repair recipes, break chances, or canonical equipment degradation."),
            primitive("map_lost", "Map Lost", Family.RESOURCE_LOSS,
                    "A carried reference is gone or unusable, leaving remembered landmarks more important than before.",
                    "Do you reconstruct the route, return to the last verified point, or follow local signs provisionally?",
                    List.of("reconstruct from memory", "return to a verified reference", "use provisional local signs"),
                    Set.of("map", "route", "landmark"),
                    List.of("The terrain has not changed, but your confidence in it has.", "What was a glance at a page is now a memory test."),
                    "Does not erase Java-owned discovered knowledge, reveal maps, quantify memory, or define route-finding success."),
            primitive("supplies_compromised", "Supplies Compromised", Family.RESOURCE_LOSS,
                    "Some carried material is wet, spoiled, scattered, or otherwise less useful than expected.",
                    "Do you salvage what remains, redistribute the burden, or change the plan around the loss?",
                    List.of("salvage usable material", "redistribute what remains", "change the plan"),
                    Set.of("supplies", "loss", "adaptation"),
                    List.of("The inventory still contains things; it no longer contains the plan you counted on.", "The loss is partial, which leaves room for argument about what to save."),
                    "Does not determine item destruction, hunger, healing, prices, loot, or a canonical supply-loss probability."),

            primitive("crossing_closing", "Crossing Closing", Family.TIME_SENSITIVE_ROUTE_CHANGE,
                    "A usable crossing is becoming less viable while another task remains unfinished.",
                    "Do you cross before conditions worsen, finish the current obligation, or commit to a different return route?",
                    List.of("cross now", "finish the current obligation", "accept another route"),
                    Set.of("crossing", "route", "timing"),
                    List.of("The path is still open, but no longer feels permanent.", "Waiting has become a route decision."),
                    "Does not define countdowns, travel speed, closure probability, or a canonical time-pressure formula."),
            primitive("return_route_blocked", "Return Route Blocked", Family.TIME_SENSITIVE_ROUTE_CHANGE,
                    "The path used to enter can no longer be assumed to be the path used to leave.",
                    "Do you investigate the blockage, press toward an alternate exit, or create a temporary way back?",
                    List.of("inspect the blockage", "seek an alternate exit", "create temporary access"),
                    Set.of("route", "blockage", "adaptation"),
                    List.of("The familiar way back has become a new problem.", "Retracing your steps ends earlier than expected."),
                    "Does not define terrain mutation, pathfinding, spawn causes, or guarantee that an alternate route exists."),
            primitive("detour_turning_unsafe", "Detour Turning Unsafe", Family.TIME_SENSITIVE_ROUTE_CHANGE,
                    "A route chosen to avoid one danger is accumulating a different kind of risk.",
                    "Do you continue the detour, return toward the known danger, or stop to verify a third option?",
                    List.of("continue the detour", "return toward known ground", "verify another option"),
                    Set.of("detour", "route", "hazard"),
                    List.of("Avoiding the first danger did not make the journey safe.", "The alternate path is beginning to demand its own price."),
                    "Does not compare objective danger numerically, calculate safe paths, or establish canonical route-generation rules."),
            primitive("hazard_front_moving", "Hazard Front Moving", Family.TIME_SENSITIVE_ROUTE_CHANGE,
                    "A broad environmental threat is changing which areas are practical to occupy or traverse.",
                    "Do you move ahead of it, shelter until it passes, or use its movement to alter another actor's options?",
                    List.of("move ahead of the hazard", "seek temporary shelter", "use the changing terrain tactically"),
                    Set.of("weather", "hazard", "route"),
                    List.of("The dangerous part of the landscape is no longer staying put.", "A safe direction now depends on when you take it."),
                    "Does not forecast weather, schedule hazards, set encounter odds, or define canonical environmental AI."),

            primitive("prepared_collapse", "Prepared Collapse", Family.DELAYED_CONSEQUENCE,
                    "Earlier preparation can matter later if an unstable structure is deliberately left ready to fail.",
                    "Do you trigger it now, preserve it as a later option, or dismantle the preparation before someone else is harmed?",
                    List.of("trigger the prepared collapse", "leave it as a delayed option", "make the structure safe again"),
                    Set.of("structure", "delay", "sacrifice"),
                    List.of("The important action happened earlier; the consequence has not happened yet.", "A prepared failure waits for a reason to become real."),
                    "Does not autonomously mutate world state, decide casualties, or bypass Java-owned event acceptance and terminal resolution."),
            primitive("warning_arrives_late", "Warning Arrives Late", Family.DELAYED_CONSEQUENCE,
                    "Information sent earlier reaches someone only after the local situation has changed.",
                    "Do you trust others to act on the old warning, send a correction, or change your own plan around their likely response?",
                    List.of("let the earlier warning stand", "send a correction", "adapt locally"),
                    Set.of("warning", "delay", "communication"),
                    List.of("Your earlier decision is moving through the world without you.", "The message may arrive faithfully and still be out of date."),
                    "Does not simulate communication speed, guarantee NPC action, or decide whether a delayed warning resolves the scenario."),
            primitive("support_compromised_earlier", "Earlier Damage Returns", Family.DELAYED_CONSEQUENCE,
                    "A structure or route weakened by an earlier choice becomes relevant again at a worse moment.",
                    "Do you spend time stabilising it, redirect movement, or accept losing access to that area?",
                    List.of("stabilise the weakened support", "redirect movement", "accept lost access"),
                    Set.of("structure", "route", "delay"),
                    List.of("The earlier shortcut has become part of the present danger.", "A cost deferred is still a cost."),
                    "Does not define structural simulation, damage propagation, route closure timing, or automatic punishment for earlier choices."),
            primitive("bargain_called_in", "Bargain Called In", Family.DELAYED_CONSEQUENCE,
                    "An agreement that solved an earlier problem creates a new obligation before the conflict is over.",
                    "Do you honour the bargain, renegotiate under pressure, or break it and accept the social consequence?",
                    List.of("honour the bargain", "renegotiate", "break the agreement"),
                    Set.of("social", "obligation", "delay"),
                    List.of("The price of earlier cooperation has finally arrived.", "A solved problem returns in the shape of a promise."),
                    "Does not enforce morality, reputation, faction standing, NPC compliance, or canonical social appraisal weights.")
    );

    private NightmarePressureComplicationCatalog() {
    }

    public static List<Primitive> primitives() {
        return PRIMITIVES;
    }

    public static Optional<Primitive> byId(String id) {
        String checked = requireText(id, "id");
        return PRIMITIVES.stream().filter(primitive -> primitive.id().equals(checked)).findFirst();
    }

    public static Composition compose(
            long seed,
            String resolvedScenarioId,
            Set<Family> allowedFamilies,
            Map<String, Integer> evidence
    ) {
        String scenarioId = requireText(resolvedScenarioId, "resolvedScenarioId");
        Set<Family> families = copyFamilies(allowedFamilies);
        Map<String, Integer> checkedEvidence = copyEvidence(evidence);

        List<Primitive> eligible = PRIMITIVES.stream()
                .filter(primitive -> families.contains(primitive.family()))
                .toList();
        if (eligible.isEmpty()) {
            throw new IllegalArgumentException("No authored pressure primitives for allowedFamilies");
        }

        Set<String> positiveEvidence = checkedEvidence.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        int bestMatch = eligible.stream()
                .mapToInt(primitive -> intersectionSize(primitive.affinityTags(), positiveEvidence))
                .max()
                .orElseThrow();
        List<Primitive> candidates = eligible.stream()
                .filter(primitive -> intersectionSize(primitive.affinityTags(), positiveEvidence) == bestMatch)
                .sorted(Comparator.comparing(Primitive::id))
                .toList();

        String context = canonicalContext(scenarioId, families, positiveEvidence);
        Primitive chosen = candidates.get(indexFor(seed, "primitive|" + context, candidates.size()));
        String cue = chosen.presentationCues().get(indexFor(seed, "cue|" + context + "|" + chosen.id(), chosen.presentationCues().size()));
        return new Composition(GENERATOR_VERSION, seed, scenarioId, chosen, cue);
    }

    private static Primitive primitive(
            String id,
            String displayName,
            Family family,
            String pressureRead,
            String playerQuestion,
            List<String> responseHooks,
            Set<String> affinityTags,
            List<String> presentationCues,
            String antiOverclaimBoundary
    ) {
        return new Primitive(id, displayName, family, pressureRead, playerQuestion, responseHooks, affinityTags, presentationCues, antiOverclaimBoundary);
    }

    private static Set<Family> copyFamilies(Set<Family> families) {
        Objects.requireNonNull(families, "allowedFamilies");
        if (families.isEmpty() || families.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("allowedFamilies must be non-empty and contain no nulls");
        }
        return Set.copyOf(EnumSet.copyOf(families));
    }

    private static Map<String, Integer> copyEvidence(Map<String, Integer> evidence) {
        Objects.requireNonNull(evidence, "evidence");
        Map<String, Integer> copy = new LinkedHashMap<>();
        evidence.forEach((key, value) -> {
            String checkedKey = requireText(key, "evidence key");
            if (value == null || value < 0) {
                throw new IllegalArgumentException("Evidence values must be non-negative");
            }
            copy.put(checkedKey, value);
        });
        return Map.copyOf(copy);
    }

    private static int intersectionSize(Set<String> left, Set<String> right) {
        int matches = 0;
        for (String value : left) {
            if (right.contains(value)) {
                matches++;
            }
        }
        return matches;
    }

    private static String canonicalContext(String scenarioId, Set<Family> families, Set<String> evidence) {
        String familyText = families.stream().map(Enum::name).sorted().reduce((left, right) -> left + "," + right).orElseThrow();
        String evidenceText = evidence.stream().sorted().reduce((left, right) -> left + "," + right).orElse("");
        return GENERATOR_VERSION + "|" + scenarioId + "|" + familyText + "|" + evidenceText;
    }

    private static int indexFor(long seed, String context, int size) {
        long value = ByteBuffer.wrap(sha256(seed + "|" + context)).getLong();
        return (int) Math.floorMod(value, (long) size);
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("JVM does not provide SHA-256", exception);
        }
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }

    public enum Family {
        ENVIRONMENTAL_DETERIORATION,
        MISINFORMATION,
        DIVIDED_OBLIGATION,
        RESOURCE_LOSS,
        TIME_SENSITIVE_ROUTE_CHANGE,
        DELAYED_CONSEQUENCE
    }

    public record Primitive(
            String id,
            String displayName,
            Family family,
            String pressureRead,
            String playerQuestion,
            List<String> responseHooks,
            Set<String> affinityTags,
            List<String> presentationCues,
            String antiOverclaimBoundary
    ) {
        public Primitive {
            id = requireText(id, "id");
            displayName = requireText(displayName, "displayName");
            family = Objects.requireNonNull(family, "family");
            pressureRead = requireText(pressureRead, "pressureRead");
            playerQuestion = requireText(playerQuestion, "playerQuestion");
            responseHooks = List.copyOf(Objects.requireNonNull(responseHooks, "responseHooks"));
            affinityTags = Set.copyOf(Objects.requireNonNull(affinityTags, "affinityTags"));
            presentationCues = List.copyOf(Objects.requireNonNull(presentationCues, "presentationCues"));
            antiOverclaimBoundary = requireText(antiOverclaimBoundary, "antiOverclaimBoundary");
            if (responseHooks.size() < 3 || affinityTags.isEmpty() || presentationCues.size() < 2) {
                throw new IllegalArgumentException("Pressure primitives require three responses, affinity tags, and two presentation cues");
            }
            responseHooks.forEach(value -> requireText(value, "response hook"));
            affinityTags.forEach(value -> requireText(value, "affinity tag"));
            presentationCues.forEach(value -> requireText(value, "presentation cue"));
        }
    }

    public record Composition(
            String generatorVersion,
            long seed,
            String resolvedScenarioId,
            Primitive primitive,
            String presentationCue
    ) {
        public Composition {
            generatorVersion = requireText(generatorVersion, "generatorVersion");
            resolvedScenarioId = requireText(resolvedScenarioId, "resolvedScenarioId");
            primitive = Objects.requireNonNull(primitive, "primitive");
            presentationCue = requireText(presentationCue, "presentationCue");
        }
    }
}
