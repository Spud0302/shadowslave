package dev.spud.shadowslave.nightmare.content;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Authored DESIGN modules describing how an already-resolved Nightmare Creature
 * can function inside an encounter without turning presentation into spawn or
 * progression authority.
 *
 * <p>The creature is supplied by the caller. Composition can vary only among
 * authored modules for that exact stable creature id. No Rank/Class, spawn,
 * reward, appraisal, Nightmare completion or persistence rule is inferred.</p>
 */
public final class NightmareCreatureEncounterRoleCatalog {
    public static final String GENERATOR_VERSION = "nightmare-creature-encounter-role-v1";

    private NightmareCreatureEncounterRoleCatalog() {
    }

    public enum EncounterRole {
        HUNTER,
        OBSTACLE,
        ENVIRONMENTAL_PRESSURE,
        DECEPTIVE_CONTACT,
        TERRITORIAL_THREAT,
        AVOIDABLE_HAZARD
    }

    public record EncounterRoleModule(
            String id,
            String creatureId,
            EncounterRole role,
            Set<NightmareCreatureContentCatalog.Pressure> requiredPressures,
            Set<String> affinityTags,
            String encounterFrame,
            String escalation,
            String counterplayFrame,
            Set<String> evidenceTags,
            String antiOverclaimBoundary
    ) {
        public EncounterRoleModule {
            id = stableId(id);
            creatureId = stableId(creatureId);
            role = Objects.requireNonNull(role, "role");
            requiredPressures = nonEmptyCopy(requiredPressures, "requiredPressures");
            affinityTags = tags(affinityTags, "affinityTags");
            encounterFrame = text(encounterFrame, "encounterFrame");
            escalation = text(escalation, "escalation");
            counterplayFrame = text(counterplayFrame, "counterplayFrame");
            evidenceTags = tags(evidenceTags, "evidenceTags");
            antiOverclaimBoundary = text(antiOverclaimBoundary, "antiOverclaimBoundary");
            if (affinityTags.isEmpty()) {
                throw new IllegalArgumentException("affinityTags cannot be empty");
            }
            if (evidenceTags.isEmpty()) {
                throw new IllegalArgumentException("evidenceTags cannot be empty");
            }
        }
    }

    public record ResolvedEncounterRole(
            String creatureId,
            String moduleId,
            EncounterRole role,
            String encounterFrame,
            String escalation,
            String counterplayFrame,
            Set<String> matchedEvidence,
            long resolvedSeed,
            String generatorVersion
    ) {
        public ResolvedEncounterRole {
            creatureId = stableId(creatureId);
            moduleId = stableId(moduleId);
            role = Objects.requireNonNull(role, "role");
            encounterFrame = text(encounterFrame, "encounterFrame");
            escalation = text(escalation, "escalation");
            counterplayFrame = text(counterplayFrame, "counterplayFrame");
            matchedEvidence = Set.copyOf(Objects.requireNonNull(matchedEvidence, "matchedEvidence"));
            generatorVersion = text(generatorVersion, "generatorVersion");
        }
    }

    /**
     * Wave one deliberately gives every existing creature at least two encounter
     * roles. Exact roles and framing are DESIGN, not canonical creature behavior.
     */
    public static List<EncounterRoleModule> waveOne() {
        List<EncounterRoleModule> modules = List.of(
                module("ash_burrower_hunter", "ash_burrower", EncounterRole.HUNTER,
                        Set.of(NightmareCreatureContentCatalog.Pressure.AMBUSH),
                        Set.of("ash", "loose_ground", "pursuit"),
                        "A moving furrow shadows the party's route and waits for repeated footfalls before closing in.",
                        "Repeated movement across loose ground gives the burrower clearer opportunities to strike from below.",
                        "Break the pursuit rhythm with firm surfaces, elevated pauses, or deliberate false vibrations.",
                        Set.of("awareness", "adaptation", "terrain_use"),
                        "This does not make every Ash Burrower a persistent tracker or define a canonical aggro radius."),
                module("ash_burrower_obstacle", "ash_burrower", EncounterRole.OBSTACLE,
                        Set.of(NightmareCreatureContentCatalog.Pressure.DISPLACEMENT),
                        Set.of("ruins", "crossing", "loose_ground"),
                        "The creature occupies the unstable approach to a needed crossing, making careless movement the real danger.",
                        "Panicked attempts to rush the route churn more ground and create additional attack lanes.",
                        "Treat the crossing as a terrain problem: test firm footing, bait movement elsewhere, then pass in controlled bursts.",
                        Set.of("preparation", "timing", "terrain_use"),
                        "The route block is authored encounter framing, not a universal territory rule for the species."),

                module("bell_eater_hunter", "bell_eater", EncounterRole.HUNTER,
                        Set.of(NightmareCreatureContentCatalog.Pressure.PURSUIT),
                        Set.of("tower", "resonance", "signal"),
                        "Every accidental ring turns the tower into a trail the Bell-Eater can follow.",
                        "Louder or repeated resonance narrows the time available before the creature reaches the source.",
                        "Move through soft surfaces, silence loose metal, or plant a false echo away from the objective.",
                        Set.of("restraint", "misdirection", "planning"),
                        "Sound pursuit is DESIGN for this profile and is not a canonical rule for sound-sensitive Nightmare Creatures."),
                module("bell_eater_environment", "bell_eater", EncounterRole.ENVIRONMENTAL_PRESSURE,
                        Set.of(NightmareCreatureContentCatalog.Pressure.AREA_DENIAL),
                        Set.of("tower", "stone", "resonance"),
                        "The creature makes bells, chains, and resonant stone into hazardous pieces of the environment even before it appears.",
                        "Using noisy machinery or signalling devices risks converting useful infrastructure into a pressure source.",
                        "Reconfigure the space for silence or intentionally ring something expendable to displace the danger.",
                        Set.of("resourcefulness", "restraint", "misdirection"),
                        "The module does not imply supernatural control over all sound or structures."),

                module("chainback_hunter", "chainback", EncounterRole.HUNTER,
                        Set.of(NightmareCreatureContentCatalog.Pressure.PURSUIT),
                        Set.of("cliffs", "bridges", "fortifications"),
                        "The Chainback advances along the same constrained route, turning every railing and anchor into something its trailing iron can catch.",
                        "A retreat through clutter gives the creature more opportunities to snag equipment or pull someone off balance.",
                        "Create vertical separation, cut expendable anchors, or pass through gaps its shell and chains cannot negotiate cleanly.",
                        Set.of("mobility", "terrain_use", "cooperation"),
                        "This framing does not establish canonical chain length, pulling force, or pathfinding behavior."),
                module("chainback_obstacle", "chainback", EncounterRole.OBSTACLE,
                        Set.of(NightmareCreatureContentCatalog.Pressure.DISPLACEMENT),
                        Set.of("bridge", "gate", "fortifications"),
                        "A Chainback has fouled a narrow approach with dragging iron, making passage dangerous even if the creature is not directly fought.",
                        "Each failed crossing attempt shifts chains and closes off another safe line.",
                        "Use a narrow bypass, sever a load-bearing snag at the right moment, or climb around the occupied lane.",
                        Set.of("timing", "mobility", "preparation"),
                        "The obstacle state is authored scenario geometry, not a universal behavior of every Chainback."),

                module("drowned_listener_hunter", "drowned_listener", EncounterRole.HUNTER,
                        Set.of(NightmareCreatureContentCatalog.Pressure.PURSUIT),
                        Set.of("harbour", "flooded_caves", "sound"),
                        "The Drowned Listener tracks disturbance through flooded passages, forcing the group to manage who makes noise and where.",
                        "Repeated splashes or shouted coordination pull the pursuit toward the loudest route.",
                        "Throw sound elsewhere, reach dry ground, or collapse a route after the group passes.",
                        Set.of("warning", "rescue", "misdirection"),
                        "The module does not claim perfect echolocation or a canonical detection range."),
                module("drowned_listener_avoidable", "drowned_listener", EncounterRole.AVOIDABLE_HAZARD,
                        Set.of(NightmareCreatureContentCatalog.Pressure.AMBUSH),
                        Set.of("storm", "shallow_water", "crossing"),
                        "Still water marks a route that can be bypassed entirely if the party recognizes the Listener's presence early.",
                        "Entering the water converts a navigational shortcut into an active ambush problem.",
                        "Take the slower dry route, send a sound decoy first, or make the crossing only after securing an exit.",
                        Set.of("caution", "awareness", "choice"),
                        "Avoidability is an authored encounter option, not a statement that canonical Nightmare Creature encounters always permit retreat."),

                module("glasswing_hunter", "glasswing", EncounterRole.HUNTER,
                        Set.of(NightmareCreatureContentCatalog.Pressure.AMBUSH),
                        Set.of("crystal", "canyon", "sunlight"),
                        "Reflected flashes reveal a Glasswing circling above an exposed route before it commits to an attack.",
                        "Crossing bright reflective ground for too long gives the aerial predator repeated approach windows.",
                        "Move under shade, raise smoke, or disrupt reflective surfaces before crossing open ground.",
                        Set.of("observation", "preparation", "timing"),
                        "The profile does not imply invisibility, light magic, or canonical reflection-based senses."),
                module("glasswing_environment", "glasswing", EncounterRole.ENVIRONMENTAL_PRESSURE,
                        Set.of(NightmareCreatureContentCatalog.Pressure.AREA_DENIAL),
                        Set.of("crystal", "sunlight", "open_ground"),
                        "Its presence turns mirrored stone and direct sunlight into unsafe travel conditions rather than a conventional arena fight.",
                        "Damage to shade or smoke cover widens the area from which the Glasswing can threaten the route.",
                        "Alter visibility, wait for safer light, or break the reflections that telegraph and support its approach.",
                        Set.of("resourcefulness", "planning", "terrain_use"),
                        "Environmental pressure is DESIGN and does not establish a universal ecology rule for gliding creatures."),

                module("gutter_choir_deceptive", "gutter_choir", EncounterRole.DECEPTIVE_CONTACT,
                        Set.of(NightmareCreatureContentCatalog.Pressure.DECEPTION),
                        Set.of("city", "sewers", "crowds"),
                        "Stolen voices call from drains and side passages, mixing real pleas with bait until source verification becomes part of survival.",
                        "Answering or following unverified voices lets the Choir split attention and isolate responders.",
                        "Break line of hearing, require visual confirmation, and isolate one source before acting on what it says.",
                        Set.of("discernment", "resolve", "cooperation"),
                        "The module does not grant the creature perfect impersonation of memories, thoughts, or private knowledge."),
                module("gutter_choir_territorial", "gutter_choir", EncounterRole.TERRITORIAL_THREAT,
                        Set.of(NightmareCreatureContentCatalog.Pressure.AREA_DENIAL),
                        Set.of("sewers", "district", "crowds"),
                        "A network of drains carries the Choir's voices across a district, making the territory itself unreliable for coordination.",
                        "Remaining inside the acoustic network increases fatigue and forces more communication mistakes.",
                        "Leave the listening network, create quiet compartments, or identify and bypass the occupied channels.",
                        Set.of("endurance", "discernment", "route_choice"),
                        "This does not establish a canonical territory size or permanent lair behavior."),

                module("hollow_mimic_deceptive", "hollow_mimic", EncounterRole.DECEPTIVE_CONTACT,
                        Set.of(NightmareCreatureContentCatalog.Pressure.DECEPTION),
                        Set.of("settlement", "interiors", "darkness"),
                        "A familiar voice offers directions from an unseen room, creating a social verification problem before combat begins.",
                        "Separating to investigate gives the Mimic a cleaner opportunity to replace trustworthy information with bait.",
                        "Use agreed verification phrases, paired watch, and bright open meeting points before trusting a voice alone.",
                        Set.of("discernment", "trust", "cooperation"),
                        "The authored mimicry does not imply copied memories, identity theft, shapeshifting, or automatic lie detection."),
                module("hollow_mimic_avoidable", "hollow_mimic", EncounterRole.AVOIDABLE_HAZARD,
                        Set.of(NightmareCreatureContentCatalog.Pressure.AMBUSH),
                        Set.of("interiors", "darkness", "alternate_route"),
                        "An interior route contains signs of a Mimic, but the objective can be reached through a slower exposed passage instead.",
                        "Entering alone turns uncertainty into an ambush; staying grouped keeps the threat informational rather than immediate.",
                        "Refuse the suspicious route, keep paired watch, or force any contact into a bright open space.",
                        Set.of("caution", "choice", "trust"),
                        "Avoidability is scenario DESIGN and not a universal property of canonical Mimics."),

                module("mire_runner_hunter", "mire_runner", EncounterRole.HUNTER,
                        Set.of(NightmareCreatureContentCatalog.Pressure.PURSUIT, NightmareCreatureContentCatalog.Pressure.PACK_COORDINATION),
                        Set.of("marsh", "reeds", "shallow_water"),
                        "Multiple reed wakes spread around the route as the pack tries to keep prey moving into poorer footing.",
                        "A straight retreat lets separate runners maintain pressure from both flanks.",
                        "Mask scent, cross deep water, or force the pack through fire and narrow terrain that disrupts coordination.",
                        Set.of("mobility", "resourcefulness", "cooperation"),
                        "The pack behavior is authored for this profile and does not define canonical Beast intelligence or pack size."),
                module("mire_runner_territorial", "mire_runner", EncounterRole.TERRITORIAL_THREAT,
                        Set.of(NightmareCreatureContentCatalog.Pressure.PACK_COORDINATION),
                        Set.of("marsh", "nesting_ground", "reeds"),
                        "A frequently used marsh route cuts through the pack's feeding ground, turning travel timing into the main decision.",
                        "Noise and scent left by repeated crossings draw more runners toward the corridor.",
                        "Travel by deep water, mask scent, or take a longer route before the pack converges.",
                        Set.of("route_choice", "caution", "resourcefulness"),
                        "This module does not assert fixed territorial instincts for canonical Nightmare Beasts."),

                module("pale_ferryman_deceptive", "pale_ferryman", EncounterRole.DECEPTIVE_CONTACT,
                        Set.of(NightmareCreatureContentCatalog.Pressure.DECEPTION),
                        Set.of("river", "fog", "crossing"),
                        "The Ferryman presents itself as the obvious solution to a dangerous crossing without proving that accepting passage is safe.",
                        "Each concession to the offered crossing reduces the party's ability to choose another route cleanly.",
                        "Refuse passage, secure an anchor before approaching, or verify an alternate crossing first.",
                        Set.of("caution", "choice", "discernment"),
                        "The module does not define speech, bargains, souls, tolls, or supernatural contracts unless separately authored."),
                module("pale_ferryman_obstacle", "pale_ferryman", EncounterRole.OBSTACLE,
                        Set.of(NightmareCreatureContentCatalog.Pressure.DISPLACEMENT),
                        Set.of("river", "fog", "crossing"),
                        "The creature occupies the shortest river crossing and pressures the party to choose between delay, exposure, and confrontation.",
                        "Fog and current make improvised crossing attempts progressively harder to coordinate.",
                        "Anchor a rope, find another ford, or wait until visibility makes a controlled crossing possible.",
                        Set.of("planning", "choice", "terrain_use"),
                        "Occupying a crossing is encounter DESIGN, not a canonical Ferryman ecology rule."),

                module("stone_maw_obstacle", "stone_maw", EncounterRole.OBSTACLE,
                        Set.of(NightmareCreatureContentCatalog.Pressure.AREA_DENIAL),
                        Set.of("quarry", "cave", "stone"),
                        "Circular cracks migrate beneath the only broad floor, forcing the party to read safe timing instead of simply charging through.",
                        "Standing still on warm stone gives the Maw a clearer strike opportunity and closes off nearby footing.",
                        "Use reinforced flooring, cold decoys, or cross during the interval after a committed strike.",
                        Set.of("timing", "terrain_use", "preparation"),
                        "The exact crack timing and heat response are DESIGN, not canonical burrower mechanics."),
                module("stone_maw_avoidable", "stone_maw", EncounterRole.AVOIDABLE_HAZARD,
                        Set.of(NightmareCreatureContentCatalog.Pressure.AMBUSH),
                        Set.of("cave", "quarry", "alternate_route"),
                        "The cracked floor marks a hazardous chamber that can be bypassed through a narrower but stable tunnel.",
                        "Rushing the obvious chamber converts a navigation choice into an active underground attack.",
                        "Take the stable tunnel, test the floor with a cold decoy, or reinforce a short crossing before committing weight.",
                        Set.of("caution", "choice", "preparation"),
                        "The bypass is scenario-authored and does not imply Stone Maws always remain confined to one chamber."),

                module("thorn_matron_environment", "thorn_matron", EncounterRole.ENVIRONMENTAL_PRESSURE,
                        Set.of(NightmareCreatureContentCatalog.Pressure.AREA_DENIAL, NightmareCreatureContentCatalog.Pressure.ATTRITION),
                        Set.of("forest", "overgrowth", "ruins"),
                        "Fresh briars steadily rewrite routes around the Matron, turning time spent in the area into shrinking mobility.",
                        "Uncontrolled movement creates more entanglement and leaves fewer clean exits for the group.",
                        "Sever key vines, use stone routes, or commit a controlled burn where the scenario safely permits it.",
                        Set.of("planning", "sacrifice", "terrain_use"),
                        "The Matron's exact plant interaction is DESIGN and does not establish universal vegetation control for Devils."),
                module("thorn_matron_territorial", "thorn_matron", EncounterRole.TERRITORIAL_THREAT,
                        Set.of(NightmareCreatureContentCatalog.Pressure.PACK_COORDINATION),
                        Set.of("forest", "overgrowth", "ruins"),
                        "The party must cross a zone where the Matron's movement and surrounding growth make lingering progressively less safe.",
                        "Repeated attempts through the same corridor let the threat reshape that route against predictable movement.",
                        "Choose a stone approach, cut a fresh line once, or sacrifice an expendable route to keep the main escape open.",
                        Set.of("route_choice", "planning", "sacrifice"),
                        "This is authored territorial framing, not a canonical claim that Devils universally control territory or minions."),

                module("veil_stalker_hunter", "veil_stalker", EncounterRole.HUNTER,
                        Set.of(NightmareCreatureContentCatalog.Pressure.PURSUIT, NightmareCreatureContentCatalog.Pressure.AMBUSH),
                        Set.of("mist", "night", "open_ground"),
                        "Small bends in the mist track alongside the group, making shared observation more valuable than raw speed.",
                        "A lone runner loses corroborating sightlines and gives the Stalker more opportunities to approach unseen.",
                        "Move under crosswind, use cold shelter, and keep overlapping watches so one uncertain sighting can be confirmed.",
                        Set.of("awareness", "cooperation", "mobility"),
                        "The creature is not canonically invisible, teleporting, or guaranteed to hunt solitary targets."),
                module("veil_stalker_deceptive", "veil_stalker", EncounterRole.DECEPTIVE_CONTACT,
                        Set.of(NightmareCreatureContentCatalog.Pressure.DECEPTION),
                        Set.of("mist", "night", "open_ground"),
                        "The encounter is built around uncertain glimpses in fog: the danger comes from acting on a false read as much as from the creature itself.",
                        "Repeated unverified reactions pull the group apart and consume safe routes or shelter.",
                        "Use crosswind, shared watch, and deliberate confirmation before committing to a chase or retreat.",
                        Set.of("discernment", "awareness", "cooperation"),
                        "This module adds no illusions, mind effects, prophecy, or canonical deception power beyond the authored profile pressure.")
        );
        validate(modules);
        return modules;
    }

    public static ResolvedEncounterRole compose(long seed, String creatureId, Map<String, Integer> evidence) {
        String stableCreatureId = stableId(creatureId);
        Map<String, Integer> checkedEvidence = checkedEvidence(evidence);

        List<EncounterRoleModule> candidates = waveOne().stream()
                .filter(module -> module.creatureId().equals(stableCreatureId))
                .sorted(Comparator.comparing(EncounterRoleModule::id))
                .toList();
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("Unknown creature id: " + stableCreatureId);
        }

        int bestScore = candidates.stream().mapToInt(module -> evidenceScore(module, checkedEvidence)).max().orElse(0);
        List<EncounterRoleModule> best = candidates.stream()
                .filter(module -> evidenceScore(module, checkedEvidence) == bestScore)
                .toList();

        long mixed = mix(seed, stableCreatureId);
        EncounterRoleModule selected = best.get(Math.floorMod(mixed, best.size()));
        Set<String> matched = selected.affinityTags().stream()
                .filter(tag -> checkedEvidence.getOrDefault(tag, 0) > 0)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());

        return new ResolvedEncounterRole(
                stableCreatureId,
                selected.id(),
                selected.role(),
                selected.encounterFrame(),
                selected.escalation(),
                selected.counterplayFrame(),
                matched,
                mixed,
                GENERATOR_VERSION
        );
    }

    public static EncounterRoleModule require(String moduleId) {
        String checked = stableId(moduleId);
        return waveOne().stream()
                .filter(module -> module.id().equals(checked))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown encounter role module: " + checked));
    }

    private static EncounterRoleModule module(
            String id,
            String creatureId,
            EncounterRole role,
            Set<NightmareCreatureContentCatalog.Pressure> requiredPressures,
            Set<String> affinityTags,
            String encounterFrame,
            String escalation,
            String counterplayFrame,
            Set<String> evidenceTags,
            String antiOverclaimBoundary
    ) {
        return new EncounterRoleModule(id, creatureId, role, requiredPressures, affinityTags,
                encounterFrame, escalation, counterplayFrame, evidenceTags, antiOverclaimBoundary);
    }

    private static void validate(List<EncounterRoleModule> modules) {
        Map<String, NightmareCreatureContentCatalog.CreatureProfile> creatures = new HashMap<>();
        for (NightmareCreatureContentCatalog.CreatureProfile profile : NightmareCreatureContentCatalog.waveOne()) {
            creatures.put(profile.id(), profile);
        }

        Set<String> ids = new HashSet<>();
        Map<String, Integer> counts = new HashMap<>();
        for (EncounterRoleModule module : modules) {
            if (!ids.add(module.id())) {
                throw new IllegalArgumentException("Duplicate encounter role module id: " + module.id());
            }
            NightmareCreatureContentCatalog.CreatureProfile profile = creatures.get(module.creatureId());
            if (profile == null) {
                throw new IllegalArgumentException("Unknown source creature: " + module.creatureId());
            }
            if (!profile.pressures().containsAll(module.requiredPressures())) {
                throw new IllegalArgumentException("Encounter role exceeds source creature pressures: " + module.id());
            }
            counts.merge(module.creatureId(), 1, Integer::sum);
        }
        for (String creatureId : creatures.keySet()) {
            if (counts.getOrDefault(creatureId, 0) < 2) {
                throw new IllegalArgumentException("Every source creature needs at least two encounter roles: " + creatureId);
            }
        }
    }

    private static int evidenceScore(EncounterRoleModule module, Map<String, Integer> evidence) {
        int score = 0;
        for (String tag : module.affinityTags()) {
            if (evidence.getOrDefault(tag, 0) > 0) {
                score++;
            }
        }
        return score;
    }

    private static Map<String, Integer> checkedEvidence(Map<String, Integer> source) {
        Map<String, Integer> result = new HashMap<>();
        for (Map.Entry<String, Integer> entry : Objects.requireNonNull(source, "evidence").entrySet()) {
            String tag = stableId(entry.getKey());
            int value = Objects.requireNonNull(entry.getValue(), "evidence value");
            if (value < 0) {
                throw new IllegalArgumentException("Negative encounter evidence is not supported: " + tag);
            }
            result.put(tag, value);
        }
        return Map.copyOf(result);
    }

    private static long mix(long seed, String id) {
        long value = seed ^ 0x9E3779B97F4A7C15L;
        for (int i = 0; i < id.length(); i++) {
            value ^= id.charAt(i);
            value *= 0x100000001B3L;
            value ^= value >>> 32;
        }
        return value;
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

    private static Set<String> tags(Set<String> source, String name) {
        Set<String> result = new HashSet<>();
        for (String tag : Objects.requireNonNull(source, name)) {
            result.add(stableId(tag));
        }
        return Set.copyOf(result);
    }

    private static <T> Set<T> nonEmptyCopy(Set<T> source, String name) {
        Set<T> result = Set.copyOf(Objects.requireNonNull(source, name));
        if (result.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be empty");
        }
        return result;
    }
}
