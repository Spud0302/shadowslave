package dev.spud.shadowslave.dreamrealm.content;

import dev.spud.shadowslave.nightmare.content.NightmareCreatureContentCatalog;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Authored DESIGN ecology bridges between already-defined Dream Realm regions and Nightmare Creatures.
 *
 * <p>This catalogue does not decide whether a creature spawns, how often it occurs, or what Rank/Class means
 * mechanically. The caller supplies an already-resolved region and creature. Composition varies only authored
 * encounter presentation for a pair that the region catalogue already marks as compatible.</p>
 */
public final class DreamRealmCreatureEcologyCatalog {
    public static final String GENERATOR_VERSION = "dream-realm-creature-ecology-v1";

    private DreamRealmCreatureEcologyCatalog() {}

    public record EcologyProfile(
            String id,
            String regionId,
            String creatureId,
            DreamRealmRegionContentCatalog.Hazard localHazard,
            DreamRealmRegionContentCatalog.Traversal traversalPressure,
            Set<String> counterplayTags,
            List<String> approachCues,
            String habitatRead,
            String pressureRead,
            String counterplayRead,
            String boundary
    ) {
        public EcologyProfile {
            id = stableId(id);
            regionId = stableId(regionId);
            creatureId = stableId(creatureId);
            localHazard = Objects.requireNonNull(localHazard, "localHazard");
            traversalPressure = Objects.requireNonNull(traversalPressure, "traversalPressure");
            counterplayTags = nonEmptyTags(counterplayTags, "counterplayTags");
            approachCues = nonEmptyTexts(approachCues, "approachCues");
            habitatRead = text(habitatRead, "habitatRead");
            pressureRead = text(pressureRead, "pressureRead");
            counterplayRead = text(counterplayRead, "counterplayRead");
            boundary = text(boundary, "boundary");
        }
    }

    public record EncounterPresentation(
            String generatorVersion,
            long seed,
            String profileId,
            String regionId,
            String creatureId,
            DreamRealmRegionContentCatalog.Hazard localHazard,
            DreamRealmRegionContentCatalog.Traversal traversalPressure,
            String approachCue,
            String habitatRead,
            String pressureRead,
            String counterplayRead,
            String boundary
    ) {}

    /** One ecology profile for every region/creature affinity currently authored on main. */
    public static List<EcologyProfile> waveOne() {
        List<EcologyProfile> profiles = List.of(
                p("ashen_expanse_ash_burrower", "ashen_expanse", "ash_burrower",
                        DreamRealmRegionContentCatalog.Hazard.UNSTABLE_GROUND, DreamRealmRegionContentCatalog.Traversal.OPEN_GROUND,
                        Set.of("stone_floor", "bait_vibration", "high_ground"),
                        List.of("A moving seam crosses ash that the wind has left untouched.", "Loose ash dimples in a line beneath the next footfall."),
                        "The open ash fields give the burrower long uninterrupted vibration trails between buried ruins.",
                        "Crossing loose flats turns ordinary movement into an invitation for a displacement ambush.",
                        "Broken masonry, elevated foundations and deliberate false vibration let travelers interrupt the approach.",
                        "This pairing is authored regional compatibility, not a claim that Ash Burrowers canonically inhabit ash deserts."),
                p("ashen_expanse_veil_stalker", "ashen_expanse", "veil_stalker",
                        DreamRealmRegionContentCatalog.Hazard.DEEP_DARKNESS, DreamRealmRegionContentCatalog.Traversal.SHELTER_DASH,
                        Set.of("cold_shelter", "crosswind", "shared_watch"),
                        List.of("Dust drifts around an empty shape where no ruin stands.", "A patch of darkness keeps pace with the shelter line."),
                        "Long sightlines and sparse cover make small disturbances in dust and darkness unusually valuable warnings.",
                        "Travel between shelters becomes a contest between exposure and an ambusher that benefits from uncertainty.",
                        "Crosswind, cold cover and shared observation turn the empty flats into readable space instead of safe space.",
                        "The Stalker's exact concealment rules and occurrence rate remain DESIGN/UNKNOWN."),
                p("chainfall_reach_chainback", "chainfall_reach", "chainback",
                        DreamRealmRegionContentCatalog.Hazard.FALLING_DEBRIS, DreamRealmRegionContentCatalog.Traversal.CHAIN_CROSSING,
                        Set.of("narrow_gap", "cut_anchor", "vertical_escape"),
                        List.of("Iron scrapes against iron from the far side of a hanging span.", "A slack bridge chain jerks twice without any change in the wind."),
                        "Bridges, anchors and cliff faces give the Chainback's dragging iron many places to catch and redirect movement.",
                        "On a suspended route, displacement is more dangerous than raw pursuit because one snag can decide the crossing.",
                        "Narrow gaps, sacrificial anchors and vertical separation let players turn the same chain geometry against the threat.",
                        "This does not define universal Chainback pathfinding or falling damage rules."),
                p("chainfall_reach_glasswing", "chainfall_reach", "glasswing",
                        DreamRealmRegionContentCatalog.Hazard.OPEN_EXPOSURE, DreamRealmRegionContentCatalog.Traversal.CLIMBING,
                        Set.of("shade", "smoke", "break_reflection"),
                        List.of("A flash moves below the climbing route without casting a stable shadow.", "Sunlight fractures once across the face of a neighboring island."),
                        "Open aerial gaps and bright cliff faces amplify reflected-light tells while leaving climbers exposed.",
                        "A gliding ambusher can pressure the moment a traveler commits both hands to exposed climbing.",
                        "Shade, smoke and broken reflections create safer windows without implying that the creature must be fought.",
                        "No canonical altitude preference or regional spawn rule is claimed."),
                p("glassmere_flats_glasswing", "glassmere_flats", "glasswing",
                        DreamRealmRegionContentCatalog.Hazard.OPEN_EXPOSURE, DreamRealmRegionContentCatalog.Traversal.OPEN_GROUND,
                        Set.of("shade", "smoke", "break_reflection"),
                        List.of("One reflection crosses the plain against the direction of the pale sky.", "A mirrored ridge flashes in three places, then only two."),
                        "The reflective plain makes the Glasswing easier to notice but gives it countless bright angles from which to approach.",
                        "Open-ground travel becomes a timing problem: every bright crossing risks exposing both traveler and predator.",
                        "Dull surfaces, smoke and temporary shade reduce useful reflections for both sides.",
                        "Reflection behavior here is authored encounter design, not canonical optics."),
                p("glassmere_flats_bell_eater", "glassmere_flats", "bell_eater",
                        DreamRealmRegionContentCatalog.Hazard.RESONANCE_STORMS, DreamRealmRegionContentCatalog.Traversal.NARROW_PATHS,
                        Set.of("silence", "false_echo", "soft_ground"),
                        List.of("A singing fissure stops ringing before the storm itself fades.", "The plain answers one metallic note with a heavier sound beneath it."),
                        "Resonant glass and singing fissures provide a noisy landscape in which meaningful sound must be separated from background resonance.",
                        "A narrow safe path can become area denial when ringing draws pressure toward the route.",
                        "Silence, false echoes and softer footing let players manage the soundscape instead of treating every storm as combat.",
                        "The Bell-Eater is not asserted to canonically feed on resonance storms."),
                p("blackwater_steps_pale_ferryman", "blackwater_steps", "pale_ferryman",
                        DreamRealmRegionContentCatalog.Hazard.CORROSIVE_WATER, DreamRealmRegionContentCatalog.Traversal.BOATING,
                        Set.of("refuse_passage", "anchor_rope", "alternate_crossing"),
                        List.of("A white figure waits at a landing the route map marks as empty.", "A crossing rope goes taut though no boat has reached the terrace."),
                        "Terraced waterways and intermittent landings make an offered crossing plausible without making it trustworthy.",
                        "The creature's displacement pressure matters because the water itself makes a bad destination.",
                        "Refusal, anchored lines and alternate crossings preserve meaningful non-combat choices.",
                        "No universal bargain, sentience or ferry ritual is claimed."),
                p("blackwater_steps_drowned_listener", "blackwater_steps", "drowned_listener",
                        DreamRealmRegionContentCatalog.Hazard.FLOOD_SURGE, DreamRealmRegionContentCatalog.Traversal.BOATING,
                        Set.of("decoy_sound", "dry_ground", "collapsed_route"),
                        List.of("Black water stops lapping against one stair while every other terrace still moves.", "A boat wake vanishes behind the hull instead of spreading outward."),
                        "Flooded terraces offer long connected water paths through which sound can reveal a moving traveler.",
                        "A surge can remove dry escape points while the Listener turns noise into pursuit pressure.",
                        "Decoy sound, secured dry ground and deliberately closed channels can break the approach.",
                        "This does not establish canonical underwater hearing range or flood timing."),
                p("thornwake_basin_thorn_matron", "thornwake_basin", "thorn_matron",
                        DreamRealmRegionContentCatalog.Hazard.HOSTILE_FLORA, DreamRealmRegionContentCatalog.Traversal.NARROW_PATHS,
                        Set.of("controlled_burn", "sever_vines", "stone_route"),
                        List.of("A briar arch closes behind the party faster than the surrounding growth can explain.", "Fresh thorns point inward along a path that was open minutes ago."),
                        "Dense overgrowth lets the Matron's authored area-denial pressure blend into an already hostile landscape.",
                        "Narrow paths can become progressively worse if travelers accept every new corridor the growth offers.",
                        "Old stone, severed vines and carefully bounded fire create counterplay without requiring a boss-style kill.",
                        "The Matron does not canonically control all hostile plants."),
                p("thornwake_basin_mire_runner", "thornwake_basin", "mire_runner",
                        DreamRealmRegionContentCatalog.Hazard.UNSTABLE_GROUND, DreamRealmRegionContentCatalog.Traversal.NARROW_PATHS,
                        Set.of("deep_water", "fire", "mask_scent"),
                        List.of("Low brush parts in several places at once without showing a body.", "Mud beside the old stone path carries parallel tracks that never cross it."),
                        "Wet hollows and broken vegetation give a coordinated pack many parallel approaches around ruined stone.",
                        "Unstable ground punishes a straight sprint while multiple pursuit lines narrow the player's options.",
                        "Deep water, firebreaks and scent masking can divide or confuse the pack long enough to relocate.",
                        "Pack size, tracking range and local population remain unsupported DESIGN details."),
                p("mistwound_pass_veil_stalker", "mistwound_pass", "veil_stalker",
                        DreamRealmRegionContentCatalog.Hazard.CONCEALING_MIST, DreamRealmRegionContentCatalog.Traversal.NARROW_PATHS,
                        Set.of("cold_shelter", "crosswind", "shared_watch"),
                        List.of("The mist bends around a shape that never resolves when the wind opens the pass.", "Two travelers see the same empty gap close from different sides."),
                        "Broken visibility and hard landmarks make absence itself a useful warning when the Stalker crosses the mist.",
                        "A narrow route magnifies deception because stepping aside can be as dangerous as continuing forward.",
                        "Crosswind, shared watch and verified shelter create overlapping observations rather than perfect detection.",
                        "No universal mist invisibility or detection formula is claimed."),
                p("mistwound_pass_hollow_mimic", "mistwound_pass", "hollow_mimic",
                        DreamRealmRegionContentCatalog.Hazard.CONCEALING_MIST, DreamRealmRegionContentCatalog.Traversal.CLIMBING,
                        Set.of("verification_phrase", "paired_watch", "bright_open_space"),
                        List.of("A familiar voice calls from beyond a cairn that has no footprints beside it.", "Someone answers from above the route without pausing to breathe."),
                        "Mist and echoes make familiar speech tempting as a navigation aid even when landmarks should remain authoritative.",
                        "Climbers can be lured into committing to a bad ledge before the source of a voice is verified.",
                        "Verification phrases, paired observation and open sightlines keep social information from becoming automatic truth.",
                        "The Mimic is not granted memory reading, perfect impersonation at range or prophecy."),
                p("bonewhite_march_stone_maw", "bonewhite_march", "stone_maw",
                        DreamRealmRegionContentCatalog.Hazard.UNSTABLE_GROUND, DreamRealmRegionContentCatalog.Traversal.OPEN_GROUND,
                        Set.of("timed_crossing", "cold_decoy", "reinforced_floor"),
                        List.of("A circular crack appears in the white plain where no old fracture ran.", "Loose bone dust jumps once from the ground before settling again."),
                        "Wide hard flats give the burrowing threat few visual obstructions but few safe interruptions once a crossing starts.",
                        "Unstable patches can convert open-ground speed into an area-denial timing problem.",
                        "Timed movement, cooler decoys and reinforced hollow structures give travelers readable options.",
                        "The exact substrate the Stone Maw can burrow through remains DESIGN."),
                p("bonewhite_march_glasswing", "bonewhite_march", "glasswing",
                        DreamRealmRegionContentCatalog.Hazard.OPEN_EXPOSURE, DreamRealmRegionContentCatalog.Traversal.SHELTER_DASH,
                        Set.of("shade", "smoke", "break_reflection"),
                        List.of("A bright point holds still against the sky until the party leaves the rib arch.", "Pale chitin on the ground flashes before anything overhead does."),
                        "The bright plain gives gliding threats long approaches and makes scattered hollow structures tactically important.",
                        "Travel becomes a shelter-to-shelter exposure calculation rather than a simple straight-line march.",
                        "Shade, smoke and disrupted reflections help choose when to leave cover.",
                        "No canonical migration or nesting behavior is asserted."),
                p("hollow_causeway_hollow_mimic", "hollow_causeway", "hollow_mimic",
                        DreamRealmRegionContentCatalog.Hazard.DEEP_DARKNESS, DreamRealmRegionContentCatalog.Traversal.TUNNELS,
                        Set.of("verification_phrase", "paired_watch", "bright_open_space"),
                        List.of("A companion's voice repeats from the junction the group just marked as empty.", "Speech returns from a side passage with the right words and the wrong breathing."),
                        "Repeating underground architecture gives false familiarity unusual leverage over route decisions.",
                        "A deceptive call can split attention or pull a traveler away from the marked path without any direct attack.",
                        "Verification phrases, paired watches and deliberately lit junctions make information handling part of traversal.",
                        "No canonical intelligence level or copied-memory access is claimed."),
                p("hollow_causeway_stone_maw", "hollow_causeway", "stone_maw",
                        DreamRealmRegionContentCatalog.Hazard.UNSTABLE_GROUND, DreamRealmRegionContentCatalog.Traversal.TUNNELS,
                        Set.of("timed_crossing", "cold_decoy", "reinforced_floor"),
                        List.of("Dust falls from a tunnel ceiling as a circular fracture spreads across the floor.", "One buried milestone vibrates while the others remain still."),
                        "Old tunnels constrain movement and make floor integrity an immediate navigation concern.",
                        "Area denial matters more when the safest-looking floor is also the only obvious route forward.",
                        "Reinforced masonry, decoys and timed crossings let players route around the pressure.",
                        "The creature is not claimed to sense through all stone or collapse entire dungeons."),
                p("storm_lantern_coast_bell_eater", "storm_lantern_coast", "bell_eater",
                        DreamRealmRegionContentCatalog.Hazard.RESONANCE_STORMS, DreamRealmRegionContentCatalog.Traversal.CLIMBING,
                        Set.of("silence", "false_echo", "soft_ground"),
                        List.of("A warning bell stops halfway through a swing while thunder continues below.", "Metal rings once from a cliff where no lantern is hanging."),
                        "Storm bells and warning infrastructure make sound both useful information and authored risk.",
                        "Cliff travel becomes harder when necessary signals can also create pursuit or area denial.",
                        "Silence windows, false echoes and softer approaches let the party choose when to create meaningful sound.",
                        "This does not make every bell a canonical lure or define hearing distance."),
                p("storm_lantern_coast_drowned_listener", "storm_lantern_coast", "drowned_listener",
                        DreamRealmRegionContentCatalog.Hazard.FLOOD_SURGE, DreamRealmRegionContentCatalog.Traversal.BOATING,
                        Set.of("decoy_sound", "dry_ground", "collapsed_route"),
                        List.of("Foam vanishes around one rock shelf before the next surge arrives.", "The water beneath the boat goes flat despite rain striking everywhere else."),
                        "Surges repeatedly connect and disconnect pools, coves and low paths along the coast.",
                        "A noisy boat can carry pursuit pressure with it just as the safe shoreline changes.",
                        "Dry ledges, decoy sound and deliberately abandoned channels can sever the Listener's approach.",
                        "No canonical tide schedule or guaranteed aquatic territory is claimed."),
                p("storm_lantern_coast_chainback", "storm_lantern_coast", "chainback",
                        DreamRealmRegionContentCatalog.Hazard.FALLING_DEBRIS, DreamRealmRegionContentCatalog.Traversal.NARROW_PATHS,
                        Set.of("narrow_gap", "cut_anchor", "vertical_escape"),
                        List.of("A cliff rope twitches under a dragging weight from above.", "Loose iron knocks against the rock between thunderclaps."),
                        "Cliff infrastructure supplies anchors, ropes and narrow ledges that make displacement especially consequential.",
                        "A snag at the wrong moment can force a traveler into storm exposure or a lower flooded route.",
                        "Narrow gaps, cut anchors and vertical separation create local escape decisions.",
                        "No canonical association between Chainbacks and coastal settlements is claimed."),
                p("red_canopy_thorn_matron", "red_canopy", "thorn_matron",
                        DreamRealmRegionContentCatalog.Hazard.HOSTILE_FLORA, DreamRealmRegionContentCatalog.Traversal.CLIMBING,
                        Set.of("controlled_burn", "sever_vines", "stone_route"),
                        List.of("Fresh thorns bridge two trunks where rain had cleared the gap moments ago.", "A vine ladder twists away from the route without any wind."),
                        "Vertical vegetation lets authored area denial spread between floor, root and canopy routes.",
                        "Choosing height can avoid floodwater while entering a space increasingly controlled by hostile growth.",
                        "Severed vines, old stone and tightly controlled fire create route choices rather than a mandatory duel.",
                        "The Matron is not claimed to command the entire canopy ecosystem."),
                p("red_canopy_mire_runner", "red_canopy", "mire_runner",
                        DreamRealmRegionContentCatalog.Hazard.FLOOD_SURGE, DreamRealmRegionContentCatalog.Traversal.SWIMMING,
                        Set.of("deep_water", "fire", "mask_scent"),
                        List.of("Several wakes cross the flooded roots without showing what made them.", "Warm rain carries a musky scent just before the undergrowth parts in a V."),
                        "Flooded roots let a mobile pack switch between water and ground approaches around the same target.",
                        "A surge can erase scent-breaking terrain while also opening deeper water the pack may not use as effectively.",
                        "Deep channels, fire and scent masking create different escape tools as water levels change.",
                        "Exact swimming limits, pack counts and flood behavior remain DESIGN."),
                p("red_canopy_gutter_choir", "red_canopy", "gutter_choir",
                        DreamRealmRegionContentCatalog.Hazard.HOSTILE_FLORA, DreamRealmRegionContentCatalog.Traversal.NARROW_PATHS,
                        Set.of("isolation", "broken_line_of_hearing", "identify_source"),
                        List.of("Several familiar voices answer from separate root hollows with the same cadence.", "A call for help moves through the canopy without any matching movement in the leaves."),
                        "Dense roots and layered canopy channels let disembodied sound arrive from many plausible directions.",
                        "Deceptive voices can turn a constrained path into attrition by repeatedly forcing verification and detours.",
                        "Breaking lines of hearing, isolating one source and verifying origin keep information pressure actionable.",
                        "The Choir is not granted canonical mind control, telepathy or unlimited voice range.")
        );
        validateAgainstSourceCatalogues(profiles);
        return profiles;
    }

    public static EncounterPresentation compose(long seed, String regionId, String creatureId) {
        String wantedRegion = stableId(regionId);
        String wantedCreature = stableId(creatureId);
        EcologyProfile profile = waveOne().stream()
                .filter(candidate -> candidate.regionId().equals(wantedRegion) && candidate.creatureId().equals(wantedCreature))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No authored ecology profile for " + wantedRegion + "/" + wantedCreature));
        int cueIndex = Math.floorMod(mix(seed, profile.id()), profile.approachCues().size());
        return new EncounterPresentation(GENERATOR_VERSION, seed, profile.id(), profile.regionId(), profile.creatureId(),
                profile.localHazard(), profile.traversalPressure(), profile.approachCues().get(cueIndex),
                profile.habitatRead(), profile.pressureRead(), profile.counterplayRead(), profile.boundary());
    }

    private static EcologyProfile p(String id, String regionId, String creatureId,
                                    DreamRealmRegionContentCatalog.Hazard hazard,
                                    DreamRealmRegionContentCatalog.Traversal traversal,
                                    Set<String> counterplay, List<String> cues,
                                    String habitat, String pressure, String counterplayRead, String boundary) {
        return new EcologyProfile(id, regionId, creatureId, hazard, traversal, counterplay, cues,
                habitat, pressure, counterplayRead, boundary);
    }

    private static void validateAgainstSourceCatalogues(List<EcologyProfile> profiles) {
        var regions = DreamRealmRegionContentCatalog.waveOne();
        var creatures = NightmareCreatureContentCatalog.waveOne();
        HashSet<String> profileIds = new HashSet<>();
        HashSet<String> pairs = new HashSet<>();
        for (EcologyProfile profile : profiles) {
            if (!profileIds.add(profile.id())) {
                throw new IllegalArgumentException("Duplicate ecology profile id: " + profile.id());
            }
            String pair = profile.regionId() + "/" + profile.creatureId();
            if (!pairs.add(pair)) {
                throw new IllegalArgumentException("Duplicate ecology pair: " + pair);
            }
            var region = regions.stream().filter(r -> r.id().equals(profile.regionId())).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown region: " + profile.regionId()));
            var creature = creatures.stream().filter(c -> c.id().equals(profile.creatureId())).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown creature: " + profile.creatureId()));
            if (!region.creatureAffinityIds().contains(creature.id())) {
                throw new IllegalArgumentException("Ecology pair is not an authored region affinity: " + pair);
            }
            if (!region.hazards().contains(profile.localHazard())) {
                throw new IllegalArgumentException("Ecology hazard is not present on source region: " + profile.id());
            }
            if (!region.traversal().contains(profile.traversalPressure())) {
                throw new IllegalArgumentException("Ecology traversal is not present on source region: " + profile.id());
            }
            if (!creature.counterplayTags().containsAll(profile.counterplayTags())) {
                throw new IllegalArgumentException("Ecology counterplay is not present on source creature: " + profile.id());
            }
        }
        for (var region : regions) {
            for (String creatureId : region.creatureAffinityIds()) {
                if (!pairs.contains(region.id() + "/" + creatureId)) {
                    throw new IllegalArgumentException("Missing ecology profile for authored affinity: " + region.id() + "/" + creatureId);
                }
            }
        }
    }

    private static int mix(long seed, String id) {
        long value = seed ^ (long) id.hashCode() * 0x9E3779B97F4A7C15L;
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdl;
        value ^= value >>> 33;
        return (int) value;
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

    private static List<String> nonEmptyTexts(List<String> source, String name) {
        List<String> result = Objects.requireNonNull(source, name).stream().map(value -> text(value, name)).toList();
        if (result.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be empty");
        }
        return List.copyOf(result);
    }
}