package dev.spud.shadowslave.dreamrealm.content;

import dev.spud.shadowslave.dreamrealm.content.DreamRealmRegionContentCatalog.Hazard;
import dev.spud.shadowslave.dreamrealm.content.DreamRealmRegionContentCatalog.Opportunity;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Authored DESIGN micro-modules for already-resolved Dream Realm landmark hooks.
 *
 * <p>This catalogue never places landmarks, awards salvage, reveals hidden objectives, or mutates
 * Java-owned region state. A deterministic seed may vary only the surfaced approach cue.
 */
public final class DreamRealmLandmarkMicroModuleCatalog {
    public static final String GENERATOR_VERSION = "dream-realm-landmark-micro-module-v1";

    private DreamRealmLandmarkMicroModuleCatalog() {}

    public enum InteractionFamily {
        OBSERVATION,
        TRAVERSAL,
        RECOVERY,
        AVOIDANCE,
        INFORMATION
    }

    public record LandmarkModule(
            String id,
            String regionId,
            String landmarkHook,
            InteractionFamily family,
            Hazard pressureHazard,
            Opportunity opportunity,
            List<String> approachCues,
            String decisionPrompt,
            List<String> decisionOptions,
            String antiOverclaimBoundary
    ) {
        public LandmarkModule {
            id = stableId(id);
            regionId = stableId(regionId);
            landmarkHook = stableId(landmarkHook);
            family = Objects.requireNonNull(family, "family");
            pressureHazard = Objects.requireNonNull(pressureHazard, "pressureHazard");
            opportunity = Objects.requireNonNull(opportunity, "opportunity");
            approachCues = nonEmptyTextList(approachCues, "approachCues", 2);
            decisionPrompt = text(decisionPrompt, "decisionPrompt");
            decisionOptions = nonEmptyTextList(decisionOptions, "decisionOptions", 2);
            antiOverclaimBoundary = text(antiOverclaimBoundary, "antiOverclaimBoundary");
        }
    }

    public record LandmarkPresentation(
            String generatorVersion,
            long seed,
            String moduleId,
            String regionId,
            String landmarkHook,
            InteractionFamily family,
            Hazard pressureHazard,
            Opportunity opportunity,
            String approachCue,
            String decisionPrompt,
            List<String> decisionOptions,
            String antiOverclaimBoundary
    ) {
        public LandmarkPresentation {
            generatorVersion = text(generatorVersion, "generatorVersion");
            moduleId = stableId(moduleId);
            regionId = stableId(regionId);
            landmarkHook = stableId(landmarkHook);
            family = Objects.requireNonNull(family, "family");
            pressureHazard = Objects.requireNonNull(pressureHazard, "pressureHazard");
            opportunity = Objects.requireNonNull(opportunity, "opportunity");
            approachCue = text(approachCue, "approachCue");
            decisionPrompt = text(decisionPrompt, "decisionPrompt");
            decisionOptions = nonEmptyTextList(decisionOptions, "decisionOptions", 2);
            antiOverclaimBoundary = text(antiOverclaimBoundary, "antiOverclaimBoundary");
        }
    }

    public static List<LandmarkModule> waveOne() {
        List<LandmarkModule> modules = List.of(
                module("ashen_expanse_buried_watchtower", "ashen_expanse", "buried_watchtower", InteractionFamily.OBSERVATION,
                        Hazard.UNSTABLE_GROUND, Opportunity.OBSERVATION,
                        List.of("A slanted tower crown breaks the ash in a place the wind should have leveled.", "Fresh ash lies thinner along one buried wall, exposing a safer line of approach."),
                        "Decide whether the tower is worth exposing yourself long enough to read the flats.",
                        List.of("Climb only high enough to map silhouettes and shelter lines, then descend.", "Circle the buried foundation for marks, caches, or a lower protected entrance.", "Record the tower as a reference point and keep moving before the open ground becomes a trap."),
                        "The watchtower may improve local knowledge, but it does not reveal hidden objectives or guarantee salvage."),
                module("ashen_expanse_black_obelisk", "ashen_expanse", "black_obelisk", InteractionFamily.INFORMATION,
                        Hazard.DEEP_DARKNESS, Opportunity.OBSERVATION,
                        List.of("The black stone keeps a sharper silhouette than the horizon around it.", "Old scratches cluster where travelers could reach the obelisk without crossing the deepest ash."),
                        "Choose how much trust to place in a landmark whose meaning is not yet known.",
                        List.of("Compare visible marks against known routes before treating any of them as guidance.", "Use the obelisk only as a positional reference and preserve uncertainty about its purpose.", "Inspect from cover, then withdraw if the open approach offers no new evidence."),
                        "The obelisk carries no automatic prophecy, map reveal, or canonical supernatural function."),
                module("ashen_expanse_shattered_causeway", "ashen_expanse", "shattered_causeway", InteractionFamily.TRAVERSAL,
                        Hazard.OPEN_EXPOSURE, Opportunity.SHORTCUT,
                        List.of("Broken road slabs point across the flats, but whole sections have vanished into ash.", "The surviving stones form a fast line with almost no nearby cover."),
                        "Decide whether speed is worth committing to an exposed route.",
                        List.of("Follow the intact stones while maintaining a planned retreat to the last shelter.", "Leave the road and accept slower ash travel to reduce predictability.", "Cross only far enough to test whether the next break can be cleared safely."),
                        "The causeway is an authored route hook, not a guaranteed shortcut or fixed world-generation corridor."),
                module("chainfall_reach_severed_chain_root", "chainfall_reach", "severed_chain_root", InteractionFamily.RECOVERY,
                        Hazard.FALLING_DEBRIS, Opportunity.SALVAGE,
                        List.of("A chain thick as a tower lies snapped against the rock, its buried links packed with debris.", "Wind has scoured one side of the break clean while loose fragments collect in its lee."),
                        "Choose whether to work the chain root despite the unstable overhead.",
                        List.of("Search the sheltered side for reusable material without climbing beneath loose stone.", "Use the chain root as temporary cover and leave the debris undisturbed.", "Probe the break for a safer anchor point before committing tools or weight."),
                        "Nothing here guarantees valuable material, a Memory, an Echo, or a canonical crafting resource."),
                module("chainfall_reach_hanging_keep", "chainfall_reach", "hanging_keep", InteractionFamily.AVOIDANCE,
                        Hazard.CRUSHING_PRESSURE, Opportunity.SHELTER,
                        List.of("A ruined keep clings to the underside of an island where the pressure feels wrong.", "Loose masonry traces the arc of earlier falls below its walls."),
                        "Choose whether the keep is shelter, a detour, or a place to avoid entirely.",
                        List.of("Approach only along load-bearing stone and leave before pressure conditions worsen.", "Use the keep's shadow as a navigation marker without entering the ruin.", "Take the longer exterior route rather than trust unsupported chambers."),
                        "The keep is not canonically safe, inhabited, or structurally persistent."),
                module("chainfall_reach_wind_bridge", "chainfall_reach", "wind_bridge", InteractionFamily.TRAVERSAL,
                        Hazard.OPEN_EXPOSURE, Opportunity.SHORTCUT,
                        List.of("A narrow natural span whistles between two islands with nothing below but dark air.", "Crosswinds arrive in pulses strong enough to make loose gear lift."),
                        "Decide whether to cross now, wait for a calmer interval, or find another chain.",
                        List.of("Test the first span while anchored before committing full weight.", "Wait through several gust cycles and cross only if the pattern becomes readable.", "Reject the bridge and trade distance for a broader route."),
                        "No fixed wind cycle, safe interval, or bridge-generation rule is claimed."),
                module("glassmere_flats_mirror_ridge", "glassmere_flats", "mirror_ridge", InteractionFamily.OBSERVATION,
                        Hazard.OPEN_EXPOSURE, Opportunity.OBSERVATION,
                        List.of("A low ridge throws broken reflections far across the white plain.", "Duller facets show tracks that bright glass would hide."),
                        "Decide whether the extra sightline is worth becoming visible in return.",
                        List.of("Use a shaded facet to scan the flats without cresting the entire ridge.", "Compare reflections from two angles before trusting movement in the distance.", "Mark the ridge as a reference and leave before changing light removes concealment."),
                        "Reflections do not provide prophecy, remote vision, or perfect detection."),
                module("glassmere_flats_red_hill", "glassmere_flats", "red_hill", InteractionFamily.RECOVERY,
                        Hazard.UNSTABLE_GROUND, Opportunity.RARE_MATERIAL,
                        List.of("A rust-red rise interrupts the glass plain, its surface mixed with shattered mineral plates.", "Small collapses expose fresh layers and then cover them again."),
                        "Choose whether to inspect the hill without turning a material hook into a guaranteed reward.",
                        List.of("Sample only exposed fragments while keeping weight off fractured plates.", "Use the hill as cover and ignore the material seam if footing deteriorates.", "Circle for older stable cuts before attempting any recovery."),
                        "Rare-material opportunity is DESIGN; quantity, quality, respawn, and value remain unspecified."),
                module("glassmere_flats_singing_fissure", "glassmere_flats", "singing_fissure", InteractionFamily.INFORMATION,
                        Hazard.RESONANCE_STORMS, Opportunity.OBSERVATION,
                        List.of("A narrow crack hums when the wind crosses it, changing pitch as the weather shifts.", "The surrounding glass answers with smaller tones that are difficult to localize."),
                        "Decide whether the sound is useful evidence or simply another hazard.",
                        List.of("Listen from a stable distance and compare changes before moving closer.", "Treat the fissure as a storm-warning reference without assuming what each tone means.", "Leave the resonant ground if the surrounding glass begins answering too strongly."),
                        "The tones are not a canonical language, detector, or universal storm forecast."),
                module("blackwater_steps_drowned_stair", "blackwater_steps", "drowned_stair", InteractionFamily.TRAVERSAL,
                        Hazard.CORROSIVE_WATER, Opportunity.SHORTCUT,
                        List.of("Stone steps descend beneath black water, reappearing farther along the terrace.", "A few upper treads remain dry enough to show old wear."),
                        "Choose whether to test the submerged route or keep to slower high ground.",
                        List.of("Probe depth and surface condition from above before stepping into water.", "Use only the exposed upper stair as a directional clue, then climb around.", "Wait for better visibility or water conditions instead of committing blind."),
                        "The stair does not guarantee a passable submerged path or fixed water chemistry."),
                module("blackwater_steps_rope_harbour", "blackwater_steps", "rope_harbour", InteractionFamily.RECOVERY,
                        Hazard.FLOOD_SURGE, Opportunity.TRADE_ROUTE,
                        List.of("Weathered posts and rope stubs mark a landing above the current waterline.", "Drift collects behind the pilings after each surge."),
                        "Decide whether to recover useful remnants without assuming a stocked settlement cache.",
                        List.of("Inspect stranded drift and rope from above the surge line.", "Use the pilings to judge recent water height, then move on.", "Recover only obvious mundane material and leave sealed or uncertain containers unresolved."),
                        "The harbour provides no guaranteed loot, trade inventory, Memory, Echo, or safe boat."),
                module("blackwater_steps_empty_ferry_house", "blackwater_steps", "empty_ferry_house", InteractionFamily.INFORMATION,
                        Hazard.CONCEALING_MIST, Opportunity.SHELTER,
                        List.of("A roofed ferry house stands empty beside a channel that disappears into fog.", "Scratches on the threshold point both toward and away from the water."),
                        "Choose what information can be trusted before using the house as shelter or route evidence.",
                        List.of("Verify the structure is actually empty before entering.", "Compare threshold marks with current water and weather rather than reading them as instructions.", "Use the roof briefly, but keep an exit that does not depend on the unseen channel."),
                        "The marks do not reveal canonical history, ownership, or supernatural guidance."),
                module("thornwake_basin_root_chapel", "thornwake_basin", "root_chapel", InteractionFamily.INFORMATION,
                        Hazard.HOSTILE_FLORA, Opportunity.OBSERVATION,
                        List.of("Roots have wrapped a small stone chapel without quite crushing its doorway.", "Old carvings survive only where the living growth has not reached."),
                        "Decide whether to inspect the preserved stone without assuming the ruin explains the region.",
                        List.of("Read only exposed carvings and distinguish observation from interpretation.", "Trace the root pressure from outside before entering a confined space.", "Record unanswered symbols rather than invent a local history for them."),
                        "The chapel has no asserted canonical religion, prophecy, or regional backstory."),
                module("thornwake_basin_sunken_garden", "thornwake_basin", "sunken_garden", InteractionFamily.RECOVERY,
                        Hazard.HOSTILE_FLORA, Opportunity.FOOD,
                        List.of("A walled garden lies below the surrounding growth, half flooded with leaves and thorn runners.", "A few plants differ from the aggressive briars around them."),
                        "Choose whether potential food or material is worth entering controlled vegetation.",
                        List.of("Identify plants by prior knowledge before touching or consuming anything.", "Collect only clearly mundane material from the edge and avoid the central beds.", "Treat the garden as a landmark and seek food elsewhere if identification is uncertain."),
                        "Food safety, alchemical value, regrowth, and rarity are not guaranteed."),
                module("thornwake_basin_stone_ring", "thornwake_basin", "stone_ring", InteractionFamily.AVOIDANCE,
                        Hazard.CONCEALING_MIST, Opportunity.DEFENSIBLE_CAMP,
                        List.of("A ring of waist-high stones creates a clear patch inside the briars.", "Mist settles in the center longer than it does on the surrounding path."),
                        "Decide whether an apparently defensible site is worth the reduced visibility.",
                        List.of("Observe the ring through one full mist shift before entering.", "Use the outer stones as cover without camping inside the low visibility pocket.", "Reject the site if the mist removes too many exit lines."),
                        "The ring is not a magical ward, safe zone, spawn suppressor, or guaranteed campsite."),
                module("mistwound_pass_split_peak", "mistwound_pass", "split_peak", InteractionFamily.OBSERVATION,
                        Hazard.OPEN_EXPOSURE, Opportunity.OBSERVATION,
                        List.of("Two broken summits frame a narrow view through the moving mist.", "When the cloud tears open, distant route fragments align for only a moment."),
                        "Decide whether to wait for a useful sightline or keep moving between known references.",
                        List.of("Use brief clear intervals to confirm only already-known landmarks.", "Stay below the crest and trade range of sight for protection from exposure.", "Record uncertain silhouettes separately instead of promoting them to route facts."),
                        "The peak grants no map reveal, divination, or perfect route knowledge."),
                module("mistwound_pass_echo_gate", "mistwound_pass", "echo_gate", InteractionFamily.INFORMATION,
                        Hazard.CONCEALING_MIST, Opportunity.SHORTCUT,
                        List.of("A stone arch returns footsteps with delays that do not match the visible corridor.", "The pass beyond it cannot be seen clearly from either side."),
                        "Choose how to verify the route without treating echoes as trustworthy guides.",
                        List.of("Create a controlled sound and compare its return before crossing.", "Maintain visual or physical markers independent of the echoed sound.", "Avoid the gate entirely if source direction cannot be verified."),
                        "Echo behavior is authored local presentation, not a canonical supernatural navigation system."),
                module("mistwound_pass_weather_cairn", "mistwound_pass", "weather_cairn", InteractionFamily.TRAVERSAL,
                        Hazard.FALLING_DEBRIS, Opportunity.SHELTER,
                        List.of("A stacked cairn stands where the pass narrows beneath a scar of loose rock.", "Its sheltered side is worn smoother than the windward face."),
                        "Decide whether the cairn marks a usable pause point or merely old survival evidence.",
                        List.of("Use the sheltered side briefly while watching the slope above.", "Compare the cairn's wear with current wind before inferring a safe direction.", "Pass without stopping if falling debris is already active."),
                        "The cairn does not guarantee a safe route, maintained trail, or living guide network."),
                module("bonewhite_march_rib_arch", "bonewhite_march", "rib_arch", InteractionFamily.TRAVERSAL,
                        Hazard.OPEN_EXPOSURE, Opportunity.SHELTER,
                        List.of("Enormous pale ribs form a sequence of arches across the plain.", "Each arch offers shade, but the gaps between them are fully exposed."),
                        "Choose whether to travel arch to arch or make a faster open crossing.",
                        List.of("Move between ribs as staged shelter points and reassess at every gap.", "Use one arch only to orient, then take a different line to avoid predictability.", "Cross the open plain only after identifying the next fallback shelter."),
                        "The ribs do not imply a canonical creature species, skeleton history, or guaranteed safe corridor."),
                module("bonewhite_march_hollow_bone", "bonewhite_march", "hollow_bone", InteractionFamily.RECOVERY,
                        Hazard.FALLING_DEBRIS, Opportunity.SALVAGE,
                        List.of("A split hollow bone is large enough to enter, with old fragments caught inside.", "Fine debris shifts whenever the outer shell flexes."),
                        "Decide whether shelter or salvage justifies entering a brittle enclosure.",
                        List.of("Inspect from the opening and recover only material that does not require disturbing the shell.", "Use the cavity as short shelter while staying within one step of the exit.", "Leave intact if new cracks appear or overhead pieces begin shifting."),
                        "The bone contains no guaranteed loot, lineage clue, Memory, Echo, or rare crafting component."),
                module("bonewhite_march_cracked_plain", "bonewhite_march", "cracked_plain", InteractionFamily.AVOIDANCE,
                        Hazard.UNSTABLE_GROUND, Opportunity.OBSERVATION,
                        List.of("Hairline fractures spread across an otherwise featureless white plain.", "Some cracks end abruptly while others vanish beneath dust."),
                        "Choose whether to cross directly, probe a route, or detour around the fractured ground.",
                        List.of("Test ground ahead with expendable pressure before each committed step.", "Follow the edge of the crack field even if it adds distance.", "Use the fracture pattern only as local evidence, not proof of what lies below."),
                        "The cracks do not canonically indicate a creature, ruin, or deterministic collapse timer."),
                module("hollow_causeway_empty_gatehouse", "hollow_causeway", "empty_gatehouse", InteractionFamily.INFORMATION,
                        Hazard.DEEP_DARKNESS, Opportunity.SHELTER,
                        List.of("An empty gatehouse divides the underground road into two nearly identical passages.", "Old fixtures remain, but no surviving sign identifies which road mattered."),
                        "Decide how to learn from the junction without inventing meaning for missing signage.",
                        List.of("Mark both exits before searching the gatehouse for mundane route evidence.", "Use airflow, wear, and known direction together rather than trusting one clue.", "Take shelter only after preserving an unmistakable return mark."),
                        "The gatehouse does not reveal a canonical destination, faction, lock, or quest objective."),
                module("hollow_causeway_buried_milestone", "hollow_causeway", "buried_milestone", InteractionFamily.OBSERVATION,
                        Hazard.UNSTABLE_GROUND, Opportunity.OBSERVATION,
                        List.of("A carved milestone protrudes from collapsed masonry beside the road.", "Only fragments of its face are visible without disturbing the rubble."),
                        "Choose whether to expose more of the marker or preserve the unstable site.",
                        List.of("Record the visible fragments before moving any debris.", "Compare the marker's orientation with the road rather than guessing missing text.", "Leave it buried if uncovering it would compromise the passage."),
                        "Missing text is UNKNOWN; the module does not fabricate distances, names, or history."),
                module("hollow_causeway_collapsed_gallery", "hollow_causeway", "collapsed_gallery", InteractionFamily.RECOVERY,
                        Hazard.UNSTABLE_GROUND, Opportunity.SALVAGE,
                        List.of("A side gallery has folded into the road, leaving pockets between beams and masonry.", "Old wire and fittings are visible where the collapse has not sealed them."),
                        "Decide whether recoverable mundane material is worth entering a collapse zone.",
                        List.of("Take only reachable material that does not support weight above it.", "Search the outer debris for a bypass before entering interior pockets.", "Abandon recovery if the route would become harder to exit than to enter."),
                        "Visible salvage is not guaranteed valuable, renewable, or tied to progression rewards."),
                module("storm_lantern_coast_storm_belfry", "storm_lantern_coast", "storm_belfry", InteractionFamily.INFORMATION,
                        Hazard.RESONANCE_STORMS, Opportunity.OBSERVATION,
                        List.of("A broken belfry faces the sea, its remaining metal answering distant thunder.", "Different pieces resonate at different points in the storm."),
                        "Choose whether to use the ruin as local weather evidence without trusting it as a perfect instrument.",
                        List.of("Observe several thunder cycles before correlating resonance with conditions.", "Mute or avoid loose metal if its noise increases local exposure.", "Record the pattern as provisional and keep a non-auditory fallback route."),
                        "The belfry is not a canonical storm oracle, warning system, or universal safe-timing device."),
                module("storm_lantern_coast_sea_gate", "storm_lantern_coast", "sea_gate", InteractionFamily.TRAVERSAL,
                        Hazard.FLOOD_SURGE, Opportunity.TRADE_ROUTE,
                        List.of("A stone sea gate opens onto a narrow channel cut into the cliffs.", "Water marks climb far above the current surface."),
                        "Decide whether to use the channel now, scout it, or take the high path.",
                        List.of("Read recent water marks and current surge behavior before entering.", "Scout from above and keep the boat route uncommitted until an exit is visible.", "Take the high route if the channel would remove too many escape options."),
                        "The gate does not guarantee navigable water, a maintained trade route, or predictable tides."),
                module("storm_lantern_coast_cliff_lantern", "storm_lantern_coast", "cliff_lantern", InteractionFamily.OBSERVATION,
                        Hazard.FALLING_DEBRIS, Opportunity.SHELTER,
                        List.of("A dead lantern cage projects from a cliff alcove above the spray.", "Its mounting point overlooks both the lower water route and a narrow high path."),
                        "Choose whether the viewpoint is worth climbing beneath weathered stone.",
                        List.of("Climb only to a stable stance that reveals both route options.", "Use the lantern position as a reference without touching the weakened fixture.", "Stay below if falling debris or wind removes a safe descent."),
                        "The lantern does not automatically mark a safe path, active settlement, or canonical signal network."),
                module("red_canopy_canopy_bridge", "red_canopy", "canopy_bridge", InteractionFamily.TRAVERSAL,
                        Hazard.OPEN_EXPOSURE, Opportunity.SHORTCUT,
                        List.of("A woven bridge links two giant branches above the flooded understory.", "Missing sections reveal a long fall through red leaves."),
                        "Decide whether height and speed outweigh the bridge's exposure and uncertain maintenance.",
                        List.of("Test anchors and the next span before transferring full weight.", "Use the bridge only as an observation line, then return to lower routes.", "Reject damaged sections and descend rather than improvising a guaranteed crossing."),
                        "The bridge is not guaranteed maintained, inhabited, or safe because it is visibly constructed."),
                module("red_canopy_flooded_temple", "red_canopy", "flooded_temple", InteractionFamily.INFORMATION,
                        Hazard.FLOOD_SURGE, Opportunity.OBSERVATION,
                        List.of("Stone walls vanish into warm floodwater beneath the roots.", "Relief fragments remain above the waterline, separated from any readable whole."),
                        "Choose what can be learned without inventing a complete story or entering unsafe water.",
                        List.of("Record exposed motifs and their location before interpreting them.", "Inspect water level and exits first if moving closer to submerged sections.", "Preserve unknown meaning instead of assigning the ruin a canonical faith or function."),
                        "The temple's builders, religion, artifacts, and supernatural properties remain unspecified DESIGN/UNKNOWN."),
                module("red_canopy_giant_root", "red_canopy", "giant_root", InteractionFamily.RECOVERY,
                        Hazard.HOSTILE_FLORA, Opportunity.RARE_MATERIAL,
                        List.of("A root wider than a road has split, exposing resinous fibers beneath the bark.", "Rainwater carries loose material into sheltered hollows along its base."),
                        "Decide whether to gather mundane material without provoking or damaging living terrain.",
                        List.of("Collect only already-shed material from the sheltered hollows.", "Use the root as weather cover and leave the living surface untouched.", "Withdraw if nearby growth reacts to disturbance or blocks an exit."),
                        "The root does not guarantee rare loot, harmless flora, regeneration, or a progression ingredient.")
        );
        validateUniqueIds(modules);
        validateAgainstRegions(modules, DreamRealmRegionContentCatalog.waveOne());
        return modules;
    }

    public static Optional<LandmarkModule> find(String regionId, String landmarkHook) {
        String checkedRegion = stableId(regionId);
        String checkedLandmark = stableId(landmarkHook);
        return waveOne().stream()
                .filter(module -> module.regionId().equals(checkedRegion)
                        && module.landmarkHook().equals(checkedLandmark))
                .findFirst();
    }

    public static LandmarkPresentation compose(long seed, String regionId, String landmarkHook) {
        LandmarkModule module = find(regionId, landmarkHook)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown Dream Realm landmark module: " + regionId + "/" + landmarkHook));
        int cueIndex = Math.floorMod(mix(seed, module.id()).hashCode(), module.approachCues().size());
        return new LandmarkPresentation(
                GENERATOR_VERSION,
                seed,
                module.id(),
                module.regionId(),
                module.landmarkHook(),
                module.family(),
                module.pressureHazard(),
                module.opportunity(),
                module.approachCues().get(cueIndex),
                module.decisionPrompt(),
                module.decisionOptions(),
                module.antiOverclaimBoundary()
        );
    }

    private static LandmarkModule module(
            String id,
            String regionId,
            String landmarkHook,
            InteractionFamily family,
            Hazard pressureHazard,
            Opportunity opportunity,
            List<String> approachCues,
            String decisionPrompt,
            List<String> decisionOptions,
            String antiOverclaimBoundary
    ) {
        return new LandmarkModule(id, regionId, landmarkHook, family, pressureHazard, opportunity,
                approachCues, decisionPrompt, decisionOptions, antiOverclaimBoundary);
    }

    private static void validateAgainstRegions(
            List<LandmarkModule> modules,
            List<DreamRealmRegionContentCatalog.RegionProfile> regions
    ) {
        for (DreamRealmRegionContentCatalog.RegionProfile region : regions) {
            Set<String> covered = new HashSet<>();
            for (LandmarkModule module : modules) {
                if (!module.regionId().equals(region.id())) {
                    continue;
                }
                if (!region.landmarkHooks().contains(module.landmarkHook())) {
                    throw new IllegalArgumentException("Unknown landmark hook for region "
                            + region.id() + ": " + module.landmarkHook());
                }
                if (!region.hazards().contains(module.pressureHazard())) {
                    throw new IllegalArgumentException("Landmark hazard does not belong to region "
                            + region.id() + ": " + module.pressureHazard());
                }
                if (!region.opportunities().contains(module.opportunity())) {
                    throw new IllegalArgumentException("Landmark opportunity does not belong to region "
                            + region.id() + ": " + module.opportunity());
                }
                covered.add(module.landmarkHook());
            }
            if (!covered.equals(region.landmarkHooks())) {
                throw new IllegalArgumentException("Landmark module coverage mismatch for region " + region.id());
            }
        }
    }

    private static void validateUniqueIds(List<LandmarkModule> modules) {
        Set<String> ids = new HashSet<>();
        Set<String> anchors = new HashSet<>();
        for (LandmarkModule module : modules) {
            if (!ids.add(module.id())) {
                throw new IllegalArgumentException("Duplicate landmark module id: " + module.id());
            }
            String anchor = module.regionId() + "/" + module.landmarkHook();
            if (!anchors.add(anchor)) {
                throw new IllegalArgumentException("Duplicate landmark module anchor: " + anchor);
            }
        }
    }

    private static String mix(long seed, String stableId) {
        long mixed = seed ^ 0x9E3779B97F4A7C15L;
        mixed = Long.rotateLeft(mixed, 17) ^ stableId.hashCode();
        mixed *= 0xC2B2AE3D27D4EB4FL;
        return Long.toUnsignedString(mixed, 36);
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

    private static List<String> nonEmptyTextList(List<String> source, String name, int minimumSize) {
        List<String> result = Objects.requireNonNull(source, name).stream()
                .map(value -> text(value, name))
                .toList();
        if (result.size() < minimumSize) {
            throw new IllegalArgumentException(name + " must contain at least " + minimumSize + " entries");
        }
        return List.copyOf(result);
    }
}
