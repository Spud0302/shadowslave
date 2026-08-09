package dev.spud.shadowslave.dreamrealm.content;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/** Player-facing DESIGN preparation briefs for already-resolved Dream Realm regions. */
public final class DreamRealmExpeditionBriefCatalog {
    public static final String GENERATOR_VERSION = "dream-realm-expedition-brief-v1";

    private DreamRealmExpeditionBriefCatalog() {}

    public enum BriefFamily {
        ROUTE_RECON,
        HAZARD_PREPARATION,
        LANDMARK_ORIENTATION,
        THREAT_AWARENESS,
        OPPORTUNITY_PLANNING
    }

    public record ExpeditionBrief(
            String id,
            String regionId,
            BriefFamily family,
            DreamRealmRegionContentCatalog.Hazard hazard,
            DreamRealmRegionContentCatalog.Traversal traversal,
            DreamRealmRegionContentCatalog.Opportunity opportunity,
            String landmarkId,
            String creatureAffinityId,
            String heading,
            String situationRead,
            List<String> preparationChecks,
            List<String> departureQuestions,
            List<String> presentationCues,
            String antiOverclaimBoundary
    ) {
        public ExpeditionBrief {
            id = stableId(id, "id");
            regionId = stableId(regionId, "regionId");
            family = Objects.requireNonNull(family, "family");
            hazard = Objects.requireNonNull(hazard, "hazard");
            traversal = Objects.requireNonNull(traversal, "traversal");
            opportunity = Objects.requireNonNull(opportunity, "opportunity");
            landmarkId = stableId(landmarkId, "landmarkId");
            creatureAffinityId = stableId(creatureAffinityId, "creatureAffinityId");
            heading = text(heading, "heading");
            situationRead = text(situationRead, "situationRead");
            preparationChecks = nonEmptyText(preparationChecks, "preparationChecks");
            departureQuestions = nonEmptyText(departureQuestions, "departureQuestions");
            presentationCues = nonEmptyText(presentationCues, "presentationCues");
            if (presentationCues.size() < 2) {
                throw new IllegalArgumentException("presentationCues must contain at least two entries");
            }
            antiOverclaimBoundary = text(antiOverclaimBoundary, "antiOverclaimBoundary");
        }
    }

    public record PreparedBrief(
            String generatorVersion,
            long seed,
            String regionId,
            String regionDisplayName,
            String briefId,
            BriefFamily family,
            DreamRealmRegionContentCatalog.Hazard hazard,
            DreamRealmRegionContentCatalog.Traversal traversal,
            DreamRealmRegionContentCatalog.Opportunity opportunity,
            String landmarkId,
            String creatureAffinityId,
            String heading,
            String situationRead,
            List<String> preparationChecks,
            List<String> departureQuestions,
            String presentationCue,
            String antiOverclaimBoundary
    ) {}

    public static List<ExpeditionBrief> waveOne() {
        List<ExpeditionBrief> briefs = List.of(
                brief("ashen_expanse_sightline", "ashen_expanse", BriefFamily.ROUTE_RECON,
                        DreamRealmRegionContentCatalog.Hazard.OPEN_EXPOSURE, DreamRealmRegionContentCatalog.Traversal.OPEN_GROUND,
                        DreamRealmRegionContentCatalog.Opportunity.OBSERVATION, "buried_watchtower", "veil_stalker",
                        "Sightlines Before Speed", "The flats make distance easy to cover and difficult to hide across.",
                        List.of("Mark the next verified shelter before leaving cover.", "Identify at least one observation point that does not require crossing unstable ground."),
                        List.of("What will break line of sight if something notices the group?", "Which landmark will confirm that the route has not drifted?"),
                        List.of("Grey dust leaves the horizon brutally clear.", "The ruined tower is visible long before its base is safe to approach."),
                        "This brief does not calculate detection range, creature placement, travel time, or safe-route probability."),
                brief("ashen_expanse_footing", "ashen_expanse", BriefFamily.HAZARD_PREPARATION,
                        DreamRealmRegionContentCatalog.Hazard.UNSTABLE_GROUND, DreamRealmRegionContentCatalog.Traversal.SHELTER_DASH,
                        DreamRealmRegionContentCatalog.Opportunity.SALVAGE, "shattered_causeway", "ash_burrower",
                        "Test the Ground", "Broken surfaces and ash conceal whether a direct line is actually traversable.",
                        List.of("Choose a short test segment before committing the whole crossing.", "Keep the salvage stop optional if footing worsens."),
                        List.of("Where can the group retreat if the surface gives way?", "Is the salvage worth leaving the tested line?"),
                        List.of("A causeway edge disappears beneath loose ash.", "Small collapses appear where the surface looked flat a moment ago."),
                        "The module does not assign collapse chances, Ash Burrower spawn odds, salvage quantity, or guaranteed safe footing."),

                brief("chainfall_reach_anchor", "chainfall_reach", BriefFamily.ROUTE_RECON,
                        DreamRealmRegionContentCatalog.Hazard.OPEN_EXPOSURE, DreamRealmRegionContentCatalog.Traversal.CHAIN_CROSSING,
                        DreamRealmRegionContentCatalog.Opportunity.SHORTCUT, "severed_chain_root", "chainback",
                        "Anchor the Crossing", "A shorter crossing can expose the expedition to wind, height, and limited retreat choices.",
                        List.of("Verify both ends of the intended chain crossing before the first member commits.", "Name an alternate route that does not depend on the same span."),
                        List.of("What happens if the crossing becomes one-way?", "Which anchor confirms the intended island?"),
                        List.of("The severed root hangs above an empty drop.", "Iron groans carry farther than voices between the islands."),
                        "No wind threshold, chain strength, altitude safety curve, encounter probability, or shortcut time saving is defined."),
                brief("chainfall_reach_high_ground", "chainfall_reach", BriefFamily.THREAT_AWARENESS,
                        DreamRealmRegionContentCatalog.Hazard.FALLING_DEBRIS, DreamRealmRegionContentCatalog.Traversal.CLIMBING,
                        DreamRealmRegionContentCatalog.Opportunity.DEFENSIBLE_CAMP, "hanging_keep", "glasswing",
                        "High Ground Has a Cost", "A defensible perch may trade ground access for exposure to falling material and aerial sightlines.",
                        List.of("Inspect overhead stone before treating the keep as a pause point.", "Keep one descent route that remains usable without crossing open ledges."),
                        List.of("What evidence would make the group abandon the perch?", "Can the watch position observe without silhouetting everyone?"),
                        List.of("Loose fragments tick down the keep wall.", "Reflected movement flashes across the higher stone."),
                        "The brief does not guarantee a safe camp, predict Glasswing presence, or define falling-debris damage."),

                brief("glassmere_flats_dull_route", "glassmere_flats", BriefFamily.ROUTE_RECON,
                        DreamRealmRegionContentCatalog.Hazard.RESONANCE_STORMS, DreamRealmRegionContentCatalog.Traversal.OPEN_GROUND,
                        DreamRealmRegionContentCatalog.Opportunity.TRADE_ROUTE, "mirror_ridge", "bell_eater",
                        "Prefer Dull Sightlines", "Open reflective ground can make route reading and local warning cues harder to trust.",
                        List.of("Select a route segment with multiple physical reference points.", "Agree on a fallback landmark before resonance or glare disrupts orientation."),
                        List.of("Which marker remains useful if reflections become confusing?", "Where can the group leave the open route without guessing?"),
                        List.of("The mirror ridge throws pale light across the flats.", "A distant vibration seems to come from more than one direction."),
                        "This does not turn resonance into a forecast, define Bell-Eater range, or establish a canonical trade-route safety level."),
                brief("glassmere_flats_material_stop", "glassmere_flats", BriefFamily.OPPORTUNITY_PLANNING,
                        DreamRealmRegionContentCatalog.Hazard.UNSTABLE_GROUND, DreamRealmRegionContentCatalog.Traversal.NARROW_PATHS,
                        DreamRealmRegionContentCatalog.Opportunity.RARE_MATERIAL, "singing_fissure", "glasswing",
                        "Make the Material Stop Optional", "Useful material is a reason to investigate, not proof that leaving the route is worth the risk.",
                        List.of("Decide before departure what observation would cancel the material stop.", "Keep collection subordinate to route integrity."),
                        List.of("Can the group inspect the fissure without committing to recovery?", "What route evidence must remain visible during the stop?"),
                        List.of("Thin glass edges hum around the fissure.", "The narrow approach gives little room to move around unstable sections."),
                        "No rarity, quantity, crafting value, Glasswing encounter rate, or guaranteed recovery is assigned."),

                brief("blackwater_steps_crossing", "blackwater_steps", BriefFamily.HAZARD_PREPARATION,
                        DreamRealmRegionContentCatalog.Hazard.FLOOD_SURGE, DreamRealmRegionContentCatalog.Traversal.BOATING,
                        DreamRealmRegionContentCatalog.Opportunity.TRADE_ROUTE, "rope_harbour", "drowned_listener",
                        "Observe Before Launch", "A familiar water route still needs current local observation before a crossing begins.",
                        List.of("Compare the waterline against a fixed harbour reference.", "Keep an abort point reachable before the boat enters the longest exposed section."),
                        List.of("What would make the current crossing evidence stale?", "Where can the expedition land without continuing forward?"),
                        List.of("The harbour rope hangs at a different angle against the water.", "Fog erases the next terrace until the boat is already near it."),
                        "The module does not predict floods, calculate currents, guarantee trade-route patrols, or determine Drowned Listener presence."),
                brief("blackwater_steps_mist", "blackwater_steps", BriefFamily.LANDMARK_ORIENTATION,
                        DreamRealmRegionContentCatalog.Hazard.CONCEALING_MIST, DreamRealmRegionContentCatalog.Traversal.NARROW_PATHS,
                        DreamRealmRegionContentCatalog.Opportunity.SHELTER, "empty_ferry_house", "pale_ferryman",
                        "Keep a Physical Reference", "Mist makes an apparently simple terrace sequence unreliable when visual continuity disappears.",
                        List.of("Choose a physical landmark for every change of level.", "Treat a shelter as provisional until its exits are checked."),
                        List.of("What confirms the route if voices or silhouettes disagree?", "Which exit remains available if the water rises?"),
                        List.of("The ferry house appears only when the fog thins.", "A narrow terrace vanishes behind the group after a few steps."),
                        "No voice is treated as trustworthy navigation, no ferry service is guaranteed, and no shelter duration is defined."),

                brief("thornwake_basin_old_stone", "thornwake_basin", BriefFamily.LANDMARK_ORIENTATION,
                        DreamRealmRegionContentCatalog.Hazard.HOSTILE_FLORA, DreamRealmRegionContentCatalog.Traversal.NARROW_PATHS,
                        DreamRealmRegionContentCatalog.Opportunity.DEFENSIBLE_CAMP, "stone_ring", "thorn_matron",
                        "Find the Stone Under the Growth", "Old masonry can provide better route evidence than the most obvious opening in changing vegetation.",
                        List.of("Record the last verified stone marker before entering dense growth.", "Keep an exit line open instead of assuming the same gap will remain."),
                        List.of("What feature proves this is the intended route and not a new opening?", "At what point does the camp geometry stop being defensible?"),
                        List.of("Briars curl over a line of older stone.", "The visible path narrows where the vegetation looks newest."),
                        "The brief does not define plant growth rates, Thorn Matron control, safe-camp duration, or route permanence."),
                brief("thornwake_basin_food", "thornwake_basin", BriefFamily.OPPORTUNITY_PLANNING,
                        DreamRealmRegionContentCatalog.Hazard.CONCEALING_MIST, DreamRealmRegionContentCatalog.Traversal.CLIMBING,
                        DreamRealmRegionContentCatalog.Opportunity.FOOD, "sunken_garden", "mire_runner",
                        "Do Not Plan Around Untested Food", "An authored food opportunity is a place to investigate, not automatic safe provisions.",
                        List.of("Carry the route plan as though local food is unavailable.", "Separate observation/testing from consumption decisions."),
                        List.of("What evidence would justify collecting rather than passing by?", "Can the group leave the garden without retracing the same concealed approach?"),
                        List.of("Bitter fruit hangs above mist pooled in the ruined garden.", "Tracks vanish where roots break the stone."),
                        "Edibility, nourishment, toxicity, buffs, Mire Runner occurrence, and supply replacement values remain unknown."),

                brief("mistwound_pass_markers", "mistwound_pass", BriefFamily.LANDMARK_ORIENTATION,
                        DreamRealmRegionContentCatalog.Hazard.CONCEALING_MIST, DreamRealmRegionContentCatalog.Traversal.NARROW_PATHS,
                        DreamRealmRegionContentCatalog.Opportunity.OBSERVATION, "weather_cairn", "hollow_mimic",
                        "Move Between Verified Markers", "The pass rewards physical verification when sound and brief visual impressions are unreliable.",
                        List.of("Agree on the next physical marker before leaving the current one.", "Use paired confirmation when a voice proposes a route change."),
                        List.of("What can be touched or rechecked if visibility collapses?", "Who independently confirms a changed instruction?"),
                        List.of("The cairn appears through a brief hole in the mist.", "A familiar voice carries from somewhere the group cannot see."),
                        "The module does not make cairns magical, voices false by default, Hollow Mimics guaranteed, or mist duration predictable."),
                brief("mistwound_pass_rockfall", "mistwound_pass", BriefFamily.HAZARD_PREPARATION,
                        DreamRealmRegionContentCatalog.Hazard.FALLING_DEBRIS, DreamRealmRegionContentCatalog.Traversal.CLIMBING,
                        DreamRealmRegionContentCatalog.Opportunity.SHELTER, "split_peak", "veil_stalker",
                        "Keep the Climb Reversible", "A climbing shortcut is less useful if falling stone or lost visibility removes the return line.",
                        List.of("Identify the nearest shelter before starting the exposed climb.", "Do not commit the whole group until the first segment has a known retreat."),
                        List.of("Can the route be reversed if debris blocks the upper section?", "Which shelter remains visible from the climb?"),
                        List.of("Pebbles skip down from the split peak.", "Wind opens and closes a view of the next ledge."),
                        "No rockfall frequency, Veil Stalker spawn rule, climbing speed, or shelter guarantee is defined."),

                brief("bonewhite_march_shelter", "bonewhite_march", BriefFamily.ROUTE_RECON,
                        DreamRealmRegionContentCatalog.Hazard.OPEN_EXPOSURE, DreamRealmRegionContentCatalog.Traversal.SHELTER_DASH,
                        DreamRealmRegionContentCatalog.Opportunity.OBSERVATION, "rib_arch", "glasswing",
                        "Plan Shelter to Shelter", "Fast open travel is useful only while the expedition knows where the next break in exposure will be.",
                        List.of("Mark the next hollow or arch before leaving the current break in exposure.", "Keep observation stops shorter than the route decision they support."),
                        List.of("What is the fallback if the next shelter is unusable?", "Can the route be confirmed without lingering in the open?"),
                        List.of("The rib arch is the only interruption in a white horizon.", "Light flashes across the open plain with nothing nearby to block it."),
                        "The module does not calculate exposure tolerance, Glasswing range, shelter safety, or crossing duration."),
                brief("bonewhite_march_hollow", "bonewhite_march", BriefFamily.THREAT_AWARENESS,
                        DreamRealmRegionContentCatalog.Hazard.UNSTABLE_GROUND, DreamRealmRegionContentCatalog.Traversal.TUNNELS,
                        DreamRealmRegionContentCatalog.Opportunity.SALVAGE, "hollow_bone", "stone_maw",
                        "Inspect the Hollow Before Entry", "A hollow structure can offer cover and salvage while also concealing unstable footing.",
                        List.of("Test the entrance before moving supplies inside.", "Keep salvage secondary to maintaining a clear exit."),
                        List.of("What sign would make the group abandon the hollow?", "Can the exit be found without relying on remembered turns alone?"),
                        List.of("Fine white fragments gather inside the hollow bone.", "A circular crack interrupts the otherwise flat floor."),
                        "No Stone Maw presence, salvage quality, structural collapse chance, or safe-rest rule is implied."),

                brief("hollow_causeway_junctions", "hollow_causeway", BriefFamily.LANDMARK_ORIENTATION,
                        DreamRealmRegionContentCatalog.Hazard.DEEP_DARKNESS, DreamRealmRegionContentCatalog.Traversal.TUNNELS,
                        DreamRealmRegionContentCatalog.Opportunity.SHORTCUT, "buried_milestone", "hollow_mimic",
                        "Mark Every Junction Twice", "Repeating road architecture makes a remembered turn weaker evidence than a physical marker.",
                        List.of("Use two independent marks at every consequential junction.", "Record which landmark invalidates the shortcut if it is missed."),
                        List.of("How will the group detect that it has looped?", "Which marker is independent of voice or memory?"),
                        List.of("The same arch shape repeats beyond the buried milestone.", "Darkness swallows the previous junction almost immediately."),
                        "The brief does not guarantee loops, make Hollow Mimics ubiquitous, reveal maps, or define shortcut distance."),
                brief("hollow_causeway_gatehouse", "hollow_causeway", BriefFamily.OPPORTUNITY_PLANNING,
                        DreamRealmRegionContentCatalog.Hazard.UNSTABLE_GROUND, DreamRealmRegionContentCatalog.Traversal.CLIMBING,
                        DreamRealmRegionContentCatalog.Opportunity.SHELTER, "empty_gatehouse", "stone_maw",
                        "Treat the Gatehouse as Provisional", "A ruin can provide cover without being structurally safe or strategically secure.",
                        List.of("Check floor continuity and both exits before moving the group inside.", "Do not make the shelter a required route anchor until it has been reverified."),
                        List.of("What evidence would force immediate abandonment?", "Can the expedition continue if this shelter is lost?"),
                        List.of("The gatehouse roof survives while parts of the floor do not.", "Loose masonry shifts under a climbing handhold."),
                        "No safe-zone, respawn, Stone Maw encounter, structural durability, or rest benefit is guaranteed."),

                brief("storm_lantern_coast_high_path", "storm_lantern_coast", BriefFamily.ROUTE_RECON,
                        DreamRealmRegionContentCatalog.Hazard.RESONANCE_STORMS, DreamRealmRegionContentCatalog.Traversal.CLIMBING,
                        DreamRealmRegionContentCatalog.Opportunity.DEFENSIBLE_CAMP, "storm_belfry", "bell_eater",
                        "High Paths Trade Water for Storm", "Leaving surge-prone ground can increase exposure to resonance, cliffs, and falling debris.",
                        List.of("Choose the high route only with a known descent point.", "Keep bell observations descriptive rather than treating them as a forecast."),
                        List.of("What local sign would trigger a descent?", "Where can the group pause without relying on the belfry as a safe zone?"),
                        List.of("The belfry rings under a sky that changes faster than the route.", "Spray reaches the lower ledges while the higher path remains exposed."),
                        "Bell behavior is not prophecy, storm timing is not calculated, and neither camp safety nor Bell-Eater presence is guaranteed."),
                brief("storm_lantern_coast_sea_gate", "storm_lantern_coast", BriefFamily.HAZARD_PREPARATION,
                        DreamRealmRegionContentCatalog.Hazard.FLOOD_SURGE, DreamRealmRegionContentCatalog.Traversal.BOATING,
                        DreamRealmRegionContentCatalog.Opportunity.WATER, "sea_gate", "drowned_listener",
                        "Verify the Sea Gate Twice", "A water route can change between observation and commitment, especially near narrow coastal access points.",
                        List.of("Recheck the gate immediately before launch.", "Keep enough route flexibility to abandon the water approach without completing it."),
                        List.of("What physical marker shows the current water state?", "Which landing remains possible if the gate becomes unusable?"),
                        List.of("Water surges through the sea gate and leaves new debris behind.", "The next landing disappears behind spray."),
                        "No tide table, surge probability, Drowned Listener occurrence, water purity, or boating-duration formula is supplied."),

                brief("red_canopy_vertical", "red_canopy", BriefFamily.ROUTE_RECON,
                        DreamRealmRegionContentCatalog.Hazard.OPEN_EXPOSURE, DreamRealmRegionContentCatalog.Traversal.CLIMBING,
                        DreamRealmRegionContentCatalog.Opportunity.OBSERVATION, "canopy_bridge", "gutter_choir",
                        "Choose a Vertical Layer Deliberately", "Canopy and ground routes trade different visibility, flood, and height pressures.",
                        List.of("Select the intended travel layer before the route branches vertically.", "Name a descent or ascent point that does not depend on the same bridge."),
                        List.of("What does the expedition gain by changing height?", "How will the group verify a call heard from another layer?"),
                        List.of("The canopy bridge reveals distance while exposing the route from below.", "Calls echo between trunks without showing who made them."),
                        "The brief does not define Gutter Choir placement, fall damage, canopy safety, or a universally optimal travel layer."),
                brief("red_canopy_flood", "red_canopy", BriefFamily.HAZARD_PREPARATION,
                        DreamRealmRegionContentCatalog.Hazard.FLOOD_SURGE, DreamRealmRegionContentCatalog.Traversal.SWIMMING,
                        DreamRealmRegionContentCatalog.Opportunity.WATER, "giant_root", "mire_runner",
                        "Keep a Dry Exit", "Water travel can be efficient only while the expedition preserves a reachable route out of the current flow.",
                        List.of("Identify a dry or elevated exit before entering a long water segment.", "Treat local water as a navigation surface, not automatically as safe drinking supply."),
                        List.of("Where can the group leave the water if pursuit begins?", "What evidence would cancel the swimming route?"),
                        List.of("Rainwater curls around the giant root and accelerates through the low ground.", "Tracks disappear where the flooded route deepens."),
                        "No flood schedule, Mire Runner encounter chance, swimming stamina rule, water safety, or supply value is defined.")
        );
        validate(briefs);
        return briefs;
    }

    public static PreparedBrief compose(long seed, String regionId) {
        String checkedRegion = stableId(regionId, "regionId");
        DreamRealmRegionContentCatalog.RegionProfile region = region(checkedRegion);
        List<ExpeditionBrief> compatible = waveOne().stream().filter(brief -> brief.regionId().equals(checkedRegion)).toList();
        if (compatible.isEmpty()) {
            throw new IllegalArgumentException("No expedition briefs for region: " + checkedRegion);
        }
        int briefIndex = Math.floorMod(mix(seed, checkedRegion.hashCode()), compatible.size());
        ExpeditionBrief brief = compatible.get(briefIndex);
        int cueIndex = Math.floorMod(mix(seed ^ 0x6a09e667f3bcc909L, brief.id().hashCode()), brief.presentationCues().size());
        return composeResolved(seed, region, brief, cueIndex);
    }

    public static PreparedBrief compose(long seed, String regionId, String briefId) {
        String checkedRegion = stableId(regionId, "regionId");
        String checkedBrief = stableId(briefId, "briefId");
        DreamRealmRegionContentCatalog.RegionProfile region = region(checkedRegion);
        ExpeditionBrief brief = waveOne().stream().filter(candidate -> candidate.id().equals(checkedBrief)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown expedition brief: " + checkedBrief));
        if (!brief.regionId().equals(checkedRegion)) {
            throw new IllegalArgumentException("Expedition brief does not belong to region: " + checkedBrief);
        }
        int cueIndex = Math.floorMod(mix(seed ^ 0xbb67ae8584caa73bL, brief.id().hashCode()), brief.presentationCues().size());
        return composeResolved(seed, region, brief, cueIndex);
    }

    private static PreparedBrief composeResolved(long seed, DreamRealmRegionContentCatalog.RegionProfile region,
                                                  ExpeditionBrief brief, int cueIndex) {
        requireSourceMembership(region, brief);
        return new PreparedBrief(GENERATOR_VERSION, seed, region.id(), region.displayName(), brief.id(), brief.family(),
                brief.hazard(), brief.traversal(), brief.opportunity(), brief.landmarkId(), brief.creatureAffinityId(),
                brief.heading(), brief.situationRead(), brief.preparationChecks(), brief.departureQuestions(),
                brief.presentationCues().get(cueIndex), brief.antiOverclaimBoundary());
    }

    private static ExpeditionBrief brief(String id, String regionId, BriefFamily family,
                                         DreamRealmRegionContentCatalog.Hazard hazard,
                                         DreamRealmRegionContentCatalog.Traversal traversal,
                                         DreamRealmRegionContentCatalog.Opportunity opportunity,
                                         String landmarkId, String creatureAffinityId, String heading, String situationRead,
                                         List<String> preparationChecks, List<String> departureQuestions,
                                         List<String> presentationCues, String antiOverclaimBoundary) {
        return new ExpeditionBrief(id, regionId, family, hazard, traversal, opportunity, landmarkId, creatureAffinityId,
                heading, situationRead, preparationChecks, departureQuestions, presentationCues, antiOverclaimBoundary);
    }

    private static DreamRealmRegionContentCatalog.RegionProfile region(String id) {
        return DreamRealmRegionContentCatalog.waveOne().stream().filter(region -> region.id().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown Dream Realm region: " + id));
    }

    private static void validate(List<ExpeditionBrief> briefs) {
        HashSet<String> ids = new HashSet<>();
        for (ExpeditionBrief brief : briefs) {
            if (!ids.add(brief.id())) {
                throw new IllegalArgumentException("Duplicate expedition brief id: " + brief.id());
            }
            requireSourceMembership(region(brief.regionId()), brief);
        }
    }

    private static void requireSourceMembership(DreamRealmRegionContentCatalog.RegionProfile region, ExpeditionBrief brief) {
        if (!region.hazards().contains(brief.hazard())) {
            throw new IllegalArgumentException("Brief hazard not authored for region: " + brief.id());
        }
        if (!region.traversal().contains(brief.traversal())) {
            throw new IllegalArgumentException("Brief traversal not authored for region: " + brief.id());
        }
        if (!region.opportunities().contains(brief.opportunity())) {
            throw new IllegalArgumentException("Brief opportunity not authored for region: " + brief.id());
        }
        if (!region.landmarkHooks().contains(brief.landmarkId())) {
            throw new IllegalArgumentException("Brief landmark not authored for region: " + brief.id());
        }
        if (!region.creatureAffinityIds().contains(brief.creatureAffinityId())) {
            throw new IllegalArgumentException("Brief creature affinity not authored for region: " + brief.id());
        }
    }

    private static int mix(long seed, int salt) {
        long value = seed ^ (Integer.toUnsignedLong(salt) * 0x9e3779b97f4a7c15L);
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdL;
        value ^= value >>> 33;
        value *= 0xc4ceb9fe1a85ec53L;
        value ^= value >>> 33;
        return (int) (value ^ (value >>> 32));
    }

    private static String stableId(String value, String name) {
        String checked = text(value, name).toLowerCase(Locale.ROOT);
        if (!checked.matches("[a-z0-9_]+")) {
            throw new IllegalArgumentException(name + " must contain only lowercase letters, numbers and underscores");
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

    private static List<String> nonEmptyText(List<String> values, String name) {
        ArrayList<String> checked = new ArrayList<>();
        for (String value : Objects.requireNonNull(values, name)) {
            checked.add(text(value, name + " entry"));
        }
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be empty");
        }
        return List.copyOf(checked);
    }
}