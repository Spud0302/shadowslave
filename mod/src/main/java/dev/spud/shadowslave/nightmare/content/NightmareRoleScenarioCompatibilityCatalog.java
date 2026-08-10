package dev.spud.shadowslave.nightmare.content;

import dev.spud.shadowslave.nightmare.LastSignalScenario;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * DESIGN compatibility modules between authored Nightmare scenarios and historical roles.
 *
 * <p>Canon supports challengers inhabiting meaningful reconstructed roles, but the Spell's
 * role-assignment principle remains UNKNOWN. These modules only prevent incoherent project
 * combinations and provide deterministic DESIGN selection among authored variants.</p>
 */
public final class NightmareRoleScenarioCompatibilityCatalog {
    private NightmareRoleScenarioCompatibilityCatalog() {
    }

    public record RoleVariant(
            String roleId,
            int baseWeight,
            String entryHook,
            String conflictPressure,
            String leverage
    ) {
        public RoleVariant {
            roleId = stableId(roleId);
            if (baseWeight <= 0) {
                throw new IllegalArgumentException("baseWeight must be positive");
            }
            entryHook = text(entryHook, "entryHook");
            conflictPressure = text(conflictPressure, "conflictPressure");
            leverage = text(leverage, "leverage");
        }
    }

    public record ScenarioCompatibility(
            String scenarioId,
            String scenarioName,
            Set<String> scenarioAffinities,
            List<RoleVariant> variants,
            String compatibilityNote
    ) {
        public ScenarioCompatibility {
            scenarioId = stableId(scenarioId);
            scenarioName = text(scenarioName, "scenarioName");
            scenarioAffinities = tags(scenarioAffinities, "scenarioAffinities");
            variants = List.copyOf(Objects.requireNonNull(variants, "variants"));
            compatibilityNote = text(compatibilityNote, "compatibilityNote");
            if (variants.size() < 3) {
                throw new IllegalArgumentException("A scenario compatibility module needs at least three role variants");
            }
            if (variants.stream().map(RoleVariant::roleId).distinct().count() != variants.size()) {
                throw new IllegalArgumentException("Scenario role variants must use unique role IDs");
            }
        }
    }

    public record ScenarioRoleMatch(
            String scenarioId,
            NightmareRoleContentCatalog.RoleProfile role,
            RoleVariant variant,
            long seed,
            int designWeight,
            List<String> matchedEvidence
    ) {
        public ScenarioRoleMatch {
            scenarioId = stableId(scenarioId);
            role = Objects.requireNonNull(role, "role");
            variant = Objects.requireNonNull(variant, "variant");
            if (!role.id().equals(variant.roleId())) {
                throw new IllegalArgumentException("variant must describe the selected role");
            }
            if (designWeight <= 0) {
                throw new IllegalArgumentException("designWeight must be positive");
            }
            matchedEvidence = List.copyOf(Objects.requireNonNull(matchedEvidence, "matchedEvidence"));
        }
    }

    /** Current authored compatibility wave. Exact combinations and weights are DESIGN. */
    public static List<ScenarioCompatibility> waveOne() {
        return List.of(
                new ScenarioCompatibility(
                        LastSignalScenario.SCENARIO_ID,
                        "The Last Signal",
                        Set.of("warning", "route", "duty", "signals", "survival"),
                        List.of(
                                variant("watch_apprentice", 7,
                                        "You were the junior watcher sent ahead when the road's final relay stopped answering.",
                                        "The ruined post is nominally your responsibility, but nobody remains to confirm what warning should be sent.",
                                        "You understand damaged signal hardware and the old relay code."),
                                variant("wounded_courier", 5,
                                        "You reached the dead road carrying a dispatch that never made the final relay.",
                                        "Your injury makes a direct retreat costly, while the sealed message may matter more than your orders admit.",
                                        "Routing marks and courier procedure help reconstruct what the vanished watch was meant to report."),
                                variant("caravan_scout", 6,
                                        "You were sent ahead of a caravan after travelers stopped returning from the watch road.",
                                        "People behind you will follow the route you mark even if you never make it back.",
                                        "Tracks, terrain and creature spoor let you distinguish a blocked road from an ambush."),
                                variant("border_levy", 3,
                                        "You are a conscript reassigned to a lonely relay after the trained watch abandoned it.",
                                        "Standing orders demand the signal be restored, but the same orders forbid leaving the post unsecured.",
                                        "Checkpoint procedure and local speech give you leverage with any survivors or deserters you find."),
                                variant("archive_novice", 2,
                                        "You accompanied a record-keeper sent to compare the road's signal logs with older reports.",
                                        "The keeper is gone, and the official record contradicts the state of the ruined post.",
                                        "Ledger habits and ritual notation make altered warnings and missing entries easier to spot."),
                                variant("pilgrim_guide", 2,
                                        "You were guiding travelers along a devotional road when the final beacon went dark.",
                                        "The safest route and the route your charges believe sacred are no longer the same.",
                                        "Local path knowledge reveals approaches to the watch that ordinary travelers would miss.")
                        ),
                        "Compatible roles must plausibly have a reason to approach, understand or be affected by the failed warning relay."
                ),
                new ScenarioCompatibility(
                        DrownedBellScenarioDefinition.SCENARIO_ID,
                        "The Drowned Bell",
                        Set.of("warning", "water", "rescue", "signals", "civilians", "route"),
                        List.of(
                                variant("watch_apprentice", 6,
                                        "You are attached to the village warning tower as its least experienced keeper.",
                                        "The senior keeper is injured just as the storm tide cuts off the lower road.",
                                        "You know the warning sequence and enough maintenance to make the cracked bell useful again."),
                                variant("cistern_keeper", 6,
                                        "You maintain the channels feeding the cliff settlement's drinking water and flood drains.",
                                        "Officials want the lower valves sealed even as the rising tide threatens to reverse the flow.",
                                        "Maintenance bypasses give you unusual access to the sea gate and buried waterworks."),
                                variant("quarry_runner", 5,
                                        "You carry work orders between the village and the abandoned quarry above the flood line.",
                                        "The foreman distrusts your warnings, while families below need a route he insists is unsafe.",
                                        "You remember service galleries and weak support walls that can become either escape routes or traps."),
                                variant("healers_aide", 3,
                                        "You are helping an exhausted village healer as storm injuries begin arriving faster than supplies.",
                                        "Moving patients uphill may save them, but abandoning the lower terraces leaves others without aid.",
                                        "Triage knowledge helps identify who can travel, who needs carrying and which local materials can substitute for lost supplies."),
                                variant("pilgrim_guide", 2,
                                        "You brought a small group to the harbour shrine just before the storm closed the coast road.",
                                        "Your charges expect you to protect both them and the shrine's observances while the settlement begins to evacuate.",
                                        "Old service paths link the shrine terraces to higher quarry ground without using the exposed harbour stairs."),
                                variant("ferry_deckhand", 7,
                                        "You were unloading the last ferry when the harbour surge trapped the vessel against the lower quay.",
                                        "Passengers, cargo and villagers all compete for the same few moments before the water rises again.",
                                        "Current, mooring and landing knowledge makes the flooded harbour navigable when streets cease to be roads.")
                        ),
                        "Compatible roles must plausibly connect to the settlement's warning, evacuation, water-control or storm-survival conflict."
                )
        );
    }

    /**
     * Deterministic DESIGN role selection constrained by the scenario module.
     * Evidence only biases compatible authored variants; it is not a canonical Spell formula.
     */
    public static ScenarioRoleMatch match(String scenarioId, long seed, Map<String, Integer> evidence) {
        String checkedScenario = stableId(scenarioId);
        Objects.requireNonNull(evidence, "evidence");

        ScenarioCompatibility module = waveOne().stream()
                .filter(candidate -> candidate.scenarioId().equals(checkedScenario))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown scenario compatibility: " + checkedScenario));

        List<Map.Entry<String, Integer>> normalizedEvidence = evidence.entrySet().stream()
                .map(entry -> Map.entry(stableId(entry.getKey()), Objects.requireNonNull(entry.getValue(), "evidence value")))
                .sorted(Map.Entry.comparingByKey())
                .toList();
        normalizedEvidence.forEach(entry -> {
            if (entry.getValue() < 0) {
                throw new IllegalArgumentException("evidence values cannot be negative");
            }
        });

        Map<String, NightmareRoleContentCatalog.RoleProfile> roles = new LinkedHashMap<>();
        NightmareRoleContentCatalog.waveOne().stream()
                .sorted(Comparator.comparing(NightmareRoleContentCatalog.RoleProfile::id))
                .forEach(role -> roles.put(role.id(), role));

        List<WeightedVariant> weighted = new ArrayList<>();
        int total = 0;
        for (RoleVariant variant : module.variants().stream().sorted(Comparator.comparing(RoleVariant::roleId)).toList()) {
            NightmareRoleContentCatalog.RoleProfile role = roles.get(variant.roleId());
            if (role == null) {
                throw new IllegalStateException("Compatibility references unknown role: " + variant.roleId());
            }
            int weight = variant.baseWeight() * 100;
            List<String> matched = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : normalizedEvidence) {
                if (entry.getValue() > 0 && role.evidenceAffinities().contains(entry.getKey())) {
                    weight = Math.addExact(weight, Math.multiplyExact(entry.getValue(), 30));
                    matched.add(entry.getKey());
                }
            }
            total = Math.addExact(total, weight);
            weighted.add(new WeightedVariant(role, variant, weight, List.copyOf(matched)));
        }

        int pick = Math.floorMod(mix(seed, checkedScenario, normalizedEvidence), total);
        for (WeightedVariant candidate : weighted) {
            if (pick < candidate.weight()) {
                return new ScenarioRoleMatch(checkedScenario, candidate.role(), candidate.variant(), seed,
                        candidate.weight(), candidate.matchedEvidence());
            }
            pick -= candidate.weight();
        }
        throw new IllegalStateException("scenario role selection exhausted unexpectedly");
    }

    private record WeightedVariant(
            NightmareRoleContentCatalog.RoleProfile role,
            RoleVariant variant,
            int weight,
            List<String> matchedEvidence
    ) {
    }

    private static RoleVariant variant(String roleId, int baseWeight, String entryHook, String conflictPressure, String leverage) {
        return new RoleVariant(roleId, baseWeight, entryHook, conflictPressure, leverage);
    }

    private static long mix(long seed, String scenarioId, List<Map.Entry<String, Integer>> evidence) {
        long value = seed ^ ((long) scenarioId.hashCode() << 32) ^ 0x9E3779B97F4A7C15L;
        for (Map.Entry<String, Integer> entry : evidence) {
            value ^= entry.getKey().hashCode();
            value = Long.rotateLeft(value * 0xBF58476D1CE4E5B9L, 29);
            value ^= entry.getValue();
            value *= 0x94D049BB133111EBL;
        }
        return value ^ (value >>> 31);
    }

    private static Set<String> tags(Set<String> values, String name) {
        Objects.requireNonNull(values, name);
        if (values.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be empty");
        }
        return values.stream().map(NightmareRoleScenarioCompatibilityCatalog::stableId).collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    private static String stableId(String value) {
        String checked = text(value, "id").toLowerCase(Locale.ROOT);
        if (!checked.matches("[a-z0-9_]+")) {
            throw new IllegalArgumentException("Stable IDs must use lowercase letters, numbers and underscores: " + value);
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
}
