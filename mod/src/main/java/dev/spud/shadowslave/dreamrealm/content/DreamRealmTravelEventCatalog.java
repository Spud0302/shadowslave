package dev.spud.shadowslave.dreamrealm.content;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Authored DESIGN travel decisions for already-resolved Dream Realm regions.
 *
 * <p>This catalogue does not roll a destination, creature, reward, travel time, or encounter chance.
 * The Java caller supplies the authoritative region identity and the seed only varies which compatible
 * authored travel event and presentation cue is returned.</p>
 */
public final class DreamRealmTravelEventCatalog {
    public static final String GENERATOR_VERSION = "dream-realm-travel-event-v1";

    private DreamRealmTravelEventCatalog() {}

    public enum TravelFamily {
        WEATHER_EXPOSURE,
        DETOUR,
        SHELTER_REST,
        CROSSING_VERIFICATION,
        ROUTE_ADAPTATION
    }

    public record TravelEvent(
            String id,
            String regionId,
            TravelFamily family,
            DreamRealmRegionContentCatalog.Hazard hazard,
            DreamRealmRegionContentCatalog.Traversal traversal,
            List<String> approachCues,
            String pressureRead,
            String decisionPrompt,
            List<String> choices,
            String antiOverclaimBoundary
    ) {
        public TravelEvent {
            id = stableId(id, "id");
            regionId = stableId(regionId, "regionId");
            family = Objects.requireNonNull(family, "family");
            hazard = Objects.requireNonNull(hazard, "hazard");
            traversal = Objects.requireNonNull(traversal, "traversal");
            approachCues = textList(approachCues, 2, "approachCues");
            pressureRead = text(pressureRead, "pressureRead");
            decisionPrompt = text(decisionPrompt, "decisionPrompt");
            choices = textList(choices, 3, "choices");
            antiOverclaimBoundary = text(antiOverclaimBoundary, "antiOverclaimBoundary");
        }
    }

    public record ResolvedTravelEvent(
            String generatorVersion,
            long seed,
            String regionId,
            TravelEvent event,
            String approachCue
    ) {
        public ResolvedTravelEvent {
            generatorVersion = text(generatorVersion, "generatorVersion");
            regionId = stableId(regionId, "regionId");
            event = Objects.requireNonNull(event, "event");
            approachCue = text(approachCue, "approachCue");
            if (!event.regionId().equals(regionId)) {
                throw new IllegalArgumentException("Resolved event must preserve caller-supplied region identity");
            }
            if (!event.approachCues().contains(approachCue)) {
                throw new IllegalArgumentException("approachCue must come from the selected event");
            }
        }
    }

    public static List<TravelEvent> waveOne() {
        List<TravelEvent> events = List.of(
                event("ashen_expanse_open_sky_check", "ashen_expanse", TravelFamily.WEATHER_EXPOSURE,
                        DreamRealmRegionContentCatalog.Hazard.OPEN_EXPOSURE,
                        DreamRealmRegionContentCatalog.Traversal.OPEN_GROUND,
                        List.of("The ash thins enough to expose the whole flat at once.",
                                "A long break in the dust leaves no nearby shape tall enough to hide behind."),
                        "Speed across the flat competes with the risk of being visible from far away.",
                        "Do you commit to the exposed crossing now or spend time changing the approach?",
                        List.of("Cross while the sightline is clear and keep moving.",
                                "Wait for thicker ash or darker conditions before crossing.",
                                "Detour toward broken ground even if the route is longer."),
                        "This event does not define canonical weather cycles, detection ranges, or travel-time values."),
                event("ashen_expanse_broken_ground", "ashen_expanse", TravelFamily.ROUTE_ADAPTATION,
                        DreamRealmRegionContentCatalog.Hazard.UNSTABLE_GROUND,
                        DreamRealmRegionContentCatalog.Traversal.SHELTER_DASH,
                        List.of("A crust of ash has split into shallow plates ahead.",
                                "The next patch of cover is separated by ground that sags under loose debris."),
                        "The shortest dash may cross footing that cannot be trusted at full speed.",
                        "Do you test the unstable stretch or give up the direct shelter-to-shelter line?",
                        List.of("Probe the edge and advance only over firm sections.",
                                "Make one quick crossing before the surface shifts further.",
                                "Backtrack and search for a slower route around the broken ground."),
                        "Unstable ground here is authored regional DESIGN; no universal collapse probability or damage rule is implied."),

                event("chainfall_reach_falling_route", "chainfall_reach", TravelFamily.SHELTER_REST,
                        DreamRealmRegionContentCatalog.Hazard.FALLING_DEBRIS,
                        DreamRealmRegionContentCatalog.Traversal.CLIMBING,
                        List.of("Pebbles begin ticking down the face above the next climb.",
                                "Fresh chips of stone lie beneath an otherwise usable climbing line."),
                        "Continuing upward preserves progress but may keep the traveler beneath an active fall line.",
                        "Do you climb through, wait under cover, or surrender height for a different route?",
                        List.of("Climb quickly between protected ledges.",
                                "Hold position under cover and watch for another fall.",
                                "Descend to a less direct route with fewer overhead faces."),
                        "The catalogue does not establish a canonical debris cadence, safe waiting duration, or stamina formula."),
                event("chainfall_reach_chain_test", "chainfall_reach", TravelFamily.CROSSING_VERIFICATION,
                        DreamRealmRegionContentCatalog.Hazard.CRUSHING_PRESSURE,
                        DreamRealmRegionContentCatalog.Traversal.CHAIN_CROSSING,
                        List.of("The chain ahead rises toward a higher island before dropping again.",
                                "The next crossing climbs enough that the upper links disappear into thin cloud."),
                        "A shorter high crossing may expose travelers to the region's dangerous altitude pressure.",
                        "Do you verify the high route, take the longer lower chain, or stop before committing?",
                        List.of("Advance only far enough to test whether the climb remains tolerable.",
                                "Choose the longer chain that keeps more altitude in reserve.",
                                "Hold at the anchor and reassess the route before stepping onto either chain."),
                        "No exact altitude threshold, pressure curve, or canonical chain-safety formula is asserted."),

                event("glassmere_flats_resonance_lull", "glassmere_flats", TravelFamily.WEATHER_EXPOSURE,
                        DreamRealmRegionContentCatalog.Hazard.RESONANCE_STORMS,
                        DreamRealmRegionContentCatalog.Traversal.OPEN_GROUND,
                        List.of("A faint hum passes through the glass plain before the next exposed stretch.",
                                "Loose shards begin to answer one another with thin, uneven tones."),
                        "The route is open and fast, but local resonance may make the crossing harder to read.",
                        "Do you move during the present lull, wait for the sound to settle, or hug broken terrain?",
                        List.of("Cross the open section before the resonance grows stronger.",
                                "Wait and compare several sound changes before moving.",
                                "Follow fractured ground that offers a slower but less exposed line."),
                        "Resonance is not a canonical forecast language and does not reveal guaranteed storm timing."),
                event("glassmere_flats_reflection_detour", "glassmere_flats", TravelFamily.DETOUR,
                        DreamRealmRegionContentCatalog.Hazard.UNSTABLE_GROUND,
                        DreamRealmRegionContentCatalog.Traversal.NARROW_PATHS,
                        List.of("A bright reflected band hides cracks along the direct line.",
                                "The safest-looking strip flashes too strongly to judge its surface from a distance."),
                        "The direct path is difficult to verify because reflection masks the footing.",
                        "Do you test the reflective strip, take a duller narrow route, or wait for the angle to change?",
                        List.of("Probe the direct strip one section at a time.",
                                "Use the narrow dull-glass edge where cracks are easier to see.",
                                "Delay until the changing light exposes more of the surface."),
                        "Reflections do not provide prophecy, hidden-object revelation, or a canonical hazard-detection mechanic."),

                event("blackwater_steps_surge_crossing", "blackwater_steps", TravelFamily.CROSSING_VERIFICATION,
                        DreamRealmRegionContentCatalog.Hazard.FLOOD_SURGE,
                        DreamRealmRegionContentCatalog.Traversal.BOATING,
                        List.of("The waterline creeps over one more terrace while the boat is still tied off.",
                                "A strand of debris reverses direction near the planned crossing."),
                        "A familiar water route may become unsafe when the local flow changes.",
                        "Do you launch now, test the current first, or postpone the crossing?",
                        List.of("Make the crossing while the visible route remains open.",
                                "Send or drop a harmless marker to read the current before launching.",
                                "Keep the boat tied and wait for a more stable waterline."),
                        "No canonical tide schedule, flood predictor, boat-speed rule, or guaranteed safe window is defined."),
                event("blackwater_steps_bad_water_detour", "blackwater_steps", TravelFamily.DETOUR,
                        DreamRealmRegionContentCatalog.Hazard.CORROSIVE_WATER,
                        DreamRealmRegionContentCatalog.Traversal.NARROW_PATHS,
                        List.of("The lower steps are wet with water that has left pale marks on the stone.",
                                "A shallow shortcut cuts through pooled black water beneath the dry ledge."),
                        "The shortest route trades distance for direct contact with a known regional hazard.",
                        "Do you test the wet shortcut, stay on the dry ledge, or retreat to a higher terrace?",
                        List.of("Test a small exposed surface before committing to the wet route.",
                                "Keep to the narrow dry ledge despite the slower pace.",
                                "Climb back and search for a higher crossing."),
                        "The event does not define exact corrosion damage, equipment immunity, or a universal safe-contact duration."),

                event("thornwake_basin_briar_detour", "thornwake_basin", TravelFamily.ROUTE_ADAPTATION,
                        DreamRealmRegionContentCatalog.Hazard.HOSTILE_FLORA,
                        DreamRealmRegionContentCatalog.Traversal.NARROW_PATHS,
                        List.of("Fresh thorns have narrowed yesterday's passage to a shoulder-wide slit.",
                                "A familiar gap is now stitched with new red growth."),
                        "The obvious path still exists, but local growth has changed how safely it can be used.",
                        "Do you force the known passage, search for old stone beneath the growth, or turn back?",
                        List.of("Clear only enough space to move through the known gap.",
                                "Search for a buried stone line that may offer another route.",
                                "Retreat before becoming committed inside the narrowed passage."),
                        "Hostile flora does not imply a universal growth rate, intelligence, aggro rule, or harvest reward."),
                event("thornwake_basin_mist_pause", "thornwake_basin", TravelFamily.SHELTER_REST,
                        DreamRealmRegionContentCatalog.Hazard.CONCEALING_MIST,
                        DreamRealmRegionContentCatalog.Traversal.TUNNELS,
                        List.of("Mist pools across the next opening while the old stone tunnel remains clear behind you.",
                                "Visibility drops outside the ruin just as the tunnel reaches a branching exit."),
                        "Leaving shelter now trades a verified enclosed route for poor visibility in overgrowth.",
                        "Do you wait in the tunnel, mark the exit and proceed, or choose another branch?",
                        List.of("Hold in the tunnel until nearby reference points become visible again.",
                                "Mark the exit carefully and move into the mist with a retreat line.",
                                "Use another tunnel branch instead of entering the obscured ground."),
                        "This does not establish a canonical mist duration, creature spawn rule, or universal shelter safety."),

                event("mistwound_pass_landmark_check", "mistwound_pass", TravelFamily.CROSSING_VERIFICATION,
                        DreamRealmRegionContentCatalog.Hazard.CONCEALING_MIST,
                        DreamRealmRegionContentCatalog.Traversal.NARROW_PATHS,
                        List.of("The next cairn appears only when the wind tears a hole through the mist.",
                                "A familiar ridge vanishes, then reappears several steps off the expected line."),
                        "The pass rewards movement between verified landmarks rather than trusting voices or silhouettes.",
                        "Do you wait for another visual confirmation, advance to the last verified marker, or backtrack?",
                        List.of("Wait for a second independent view of the next landmark.",
                                "Move only to the last position that can still be verified visually.",
                                "Return to the previous marker and choose a different line."),
                        "The event does not make echoes, voices, or silhouettes truthful navigation aids."),
                event("mistwound_pass_rockfall_route", "mistwound_pass", TravelFamily.DETOUR,
                        DreamRealmRegionContentCatalog.Hazard.FALLING_DEBRIS,
                        DreamRealmRegionContentCatalog.Traversal.CLIMBING,
                        List.of("A sharp crack sounds above the climbing route and then stops.",
                                "New fragments lie across the handholds beneath an exposed face."),
                        "The climb remains possible, but recent movement overhead changes the risk of committing to it.",
                        "Do you inspect the face, take the longer sheltered traverse, or wait before climbing?",
                        List.of("Inspect the first section and retreat if more debris moves.",
                                "Use the longer traverse that spends less time beneath the face.",
                                "Wait from cover and watch for another movement cycle."),
                        "No exact rockfall probability, waiting interval, or canonical route rating is claimed."),

                event("bonewhite_march_exposure_rest", "bonewhite_march", TravelFamily.SHELTER_REST,
                        DreamRealmRegionContentCatalog.Hazard.OPEN_EXPOSURE,
                        DreamRealmRegionContentCatalog.Traversal.OPEN_GROUND,
                        List.of("The next hollow structure is visible, but the plain between offers no cover.",
                                "A long white stretch separates the current shade from the next break in exposure."),
                        "Fast open travel preserves time but reduces opportunities to stop or hide.",
                        "Do you cross immediately, rest before the exposed stretch, or search for another shelter line?",
                        List.of("Cross the open ground in one committed movement.",
                                "Rest and prepare before leaving the current shelter.",
                                "Spend time searching for a route with another intermediate hollow."),
                        "This event does not define a canonical exhaustion meter, visibility radius, or shelter spacing."),
                event("bonewhite_march_hollow_test", "bonewhite_march", TravelFamily.ROUTE_ADAPTATION,
                        DreamRealmRegionContentCatalog.Hazard.UNSTABLE_GROUND,
                        DreamRealmRegionContentCatalog.Traversal.TUNNELS,
                        List.of("The hollow ahead gives back a dull crack when loose bone shifts underfoot.",
                                "A tunnel through the white remains opens beneath a ceiling crossed by fresh fractures."),
                        "A sheltered shortcut may exchange exposure for uncertain structural footing.",
                        "Do you test the hollow route, remain above ground, or use the tunnel only as temporary cover?",
                        List.of("Probe the tunnel entrance and advance only while the structure stays quiet.",
                                "Stay on the exposed surface rather than commit underground.",
                                "Use the entrance for a brief stop, then continue by another route."),
                        "The catalogue does not infer the creature origin, age, contents, or collapse probability of any hollow structure."),

                event("hollow_causeway_dark_junction", "hollow_causeway", TravelFamily.CROSSING_VERIFICATION,
                        DreamRealmRegionContentCatalog.Hazard.DEEP_DARKNESS,
                        DreamRealmRegionContentCatalog.Traversal.TUNNELS,
                        List.of("Two identical archways leave the road where the light fails completely.",
                                "Repeated masonry makes the next underground junction look like one already passed."),
                        "The route is threatened less by speed than by losing confidence in which junction is which.",
                        "Do you verify the junction, mark it before moving, or backtrack to the last certain point?",
                        List.of("Compare the junction against existing marks before choosing a branch.",
                                "Add a distinct physical marker before advancing.",
                                "Return to the last verified junction and rebuild the route from there."),
                        "No automatic map reveal, supernatural route truth, or canonical darkness-navigation rule is provided."),
                event("hollow_causeway_broken_climb", "hollow_causeway", TravelFamily.ROUTE_ADAPTATION,
                        DreamRealmRegionContentCatalog.Hazard.UNSTABLE_GROUND,
                        DreamRealmRegionContentCatalog.Traversal.CLIMBING,
                        List.of("Part of the old road has dropped away, leaving a climb over cracked masonry.",
                                "The direct gallery ends at a fresh break with exposed stone ribs above it."),
                        "Continuing on the known road requires turning a horizontal route into a fragile climb.",
                        "Do you climb the break, search for a tunnel bypass, or retreat to another junction?",
                        List.of("Test the masonry and climb only across sections that hold.",
                                "Search nearby side passages for a bypass around the break.",
                                "Return to the previous junction and abandon this branch."),
                        "This does not establish a canonical ruin layout, collapse schedule, or hidden-shortcut guarantee."),

                event("storm_lantern_coast_surge_boat", "storm_lantern_coast", TravelFamily.WEATHER_EXPOSURE,
                        DreamRealmRegionContentCatalog.Hazard.FLOOD_SURGE,
                        DreamRealmRegionContentCatalog.Traversal.BOATING,
                        List.of("Foam reaches a higher mark on the cliff just before launch.",
                                "The protected water below the ledge begins pulling hard toward the open coast."),
                        "The lower route may avoid exposed heights, but a surge can change it while the crossing is underway.",
                        "Do you launch before conditions worsen, wait above the waterline, or abandon the boat route?",
                        List.of("Launch while the current approach remains readable.",
                                "Wait from higher ground for the surge pattern to change.",
                                "Switch to a cliff route despite greater storm exposure."),
                        "No canonical tide table, surge frequency, safe launch threshold, or travel-time equation is asserted."),
                event("storm_lantern_coast_bell_weather", "storm_lantern_coast", TravelFamily.CROSSING_VERIFICATION,
                        DreamRealmRegionContentCatalog.Hazard.RESONANCE_STORMS,
                        DreamRealmRegionContentCatalog.Traversal.NARROW_PATHS,
                        List.of("A broken bell answers thunder from somewhere below the cliff path.",
                                "Metal fittings along the path begin to vibrate before the next exposed bend."),
                        "Local sound may be useful evidence of changing conditions without being a guaranteed forecast.",
                        "Do you treat the vibration as a reason to wait, verify it against visible conditions, or continue?",
                        List.of("Pause and compare the sound with wind, water, and sky before deciding.",
                                "Move to the next protected bend while keeping a retreat route.",
                                "Take a different path that avoids the resonant structures."),
                        "Bells and vibration do not constitute a canonical storm-prediction language or magical warning system."),

                event("red_canopy_flooded_floor", "red_canopy", TravelFamily.ROUTE_ADAPTATION,
                        DreamRealmRegionContentCatalog.Hazard.FLOOD_SURGE,
                        DreamRealmRegionContentCatalog.Traversal.SWIMMING,
                        List.of("Warm rain turns the forest floor into a fast brown channel.",
                                "A ground route disappears beneath water while higher roots remain reachable."),
                        "The region's lowest route can become a water route without warning from the traveller's perspective.",
                        "Do you swim the flooded line, climb above it, or wait for the flow to ease?",
                        List.of("Use the water route only while entry and exit points remain visible.",
                                "Climb onto roots and continue above the flooded floor.",
                                "Hold on stable ground and wait before committing to either route."),
                        "No canonical rainfall cycle, current strength, flood duration, or swimming-speed rule is defined."),
                event("red_canopy_living_climb", "red_canopy", TravelFamily.DETOUR,
                        DreamRealmRegionContentCatalog.Hazard.HOSTILE_FLORA,
                        DreamRealmRegionContentCatalog.Traversal.CLIMBING,
                        List.of("The branch used as a handhold is wrapped in fresh thorny growth.",
                                "A canopy route remains intact, but new red vines cross the easiest climb."),
                        "The high route offers distance from floodwater but requires negotiating dangerous vegetation.",
                        "Do you clear a narrow climbing line, descend toward water, or search for another tree connection?",
                        List.of("Clear only the handholds needed for a careful climb.",
                                "Descend and accept the lower route rather than force the growth.",
                                "Traverse sideways until another canopy connection can be verified."),
                        "Hostile flora does not imply universal sentience, regrowth rate, harvesting value, or combat behavior.")
        );
        validate(events);
        return events;
    }

    public static ResolvedTravelEvent compose(long seed, String regionId) {
        String stableRegionId = stableId(regionId, "regionId");
        requireRegion(stableRegionId);
        List<TravelEvent> candidates = waveOne().stream()
                .filter(event -> event.regionId().equals(stableRegionId))
                .toList();
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No authored travel events for region: " + stableRegionId);
        }
        int eventIndex = index(mix(seed, stableRegionId.hashCode()), candidates.size());
        TravelEvent selected = candidates.get(eventIndex);
        int cueIndex = index(mix(seed ^ 0x9E3779B97F4A7C15L, selected.id().hashCode()), selected.approachCues().size());
        return new ResolvedTravelEvent(GENERATOR_VERSION, seed, stableRegionId, selected, selected.approachCues().get(cueIndex));
    }

    public static TravelEvent byId(String eventId) {
        String stableEventId = stableId(eventId, "eventId");
        return waveOne().stream()
                .filter(event -> event.id().equals(stableEventId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown travel event: " + stableEventId));
    }

    private static TravelEvent event(String id, String regionId, TravelFamily family,
                                     DreamRealmRegionContentCatalog.Hazard hazard,
                                     DreamRealmRegionContentCatalog.Traversal traversal,
                                     List<String> cues, String pressure, String prompt,
                                     List<String> choices, String boundary) {
        return new TravelEvent(id, regionId, family, hazard, traversal, cues, pressure, prompt, choices, boundary);
    }

    private static void validate(List<TravelEvent> events) {
        HashSet<String> ids = new HashSet<>();
        for (TravelEvent event : events) {
            if (!ids.add(event.id())) {
                throw new IllegalArgumentException("Duplicate travel event id: " + event.id());
            }
            DreamRealmRegionContentCatalog.RegionProfile region = requireRegion(event.regionId());
            if (!region.hazards().contains(event.hazard())) {
                throw new IllegalArgumentException(event.id() + " uses hazard absent from region " + event.regionId());
            }
            if (!region.traversal().contains(event.traversal())) {
                throw new IllegalArgumentException(event.id() + " uses traversal absent from region " + event.regionId());
            }
        }
    }

    private static DreamRealmRegionContentCatalog.RegionProfile requireRegion(String regionId) {
        return DreamRealmRegionContentCatalog.waveOne().stream()
                .filter(region -> region.id().equals(regionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown Dream Realm region: " + regionId));
    }

    private static int index(long value, int size) {
        return Math.floorMod((int) (value ^ (value >>> 32)), size);
    }

    private static long mix(long seed, long salt) {
        long value = seed ^ (salt * 0x9E3779B97F4A7C15L);
        value ^= value >>> 30;
        value *= 0xBF58476D1CE4E5B9L;
        value ^= value >>> 27;
        value *= 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
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

    private static List<String> textList(List<String> source, int minimum, String name) {
        List<String> result = new ArrayList<>();
        for (String value : Objects.requireNonNull(source, name)) {
            result.add(text(value, name));
        }
        if (result.size() < minimum) {
            throw new IllegalArgumentException(name + " must contain at least " + minimum + " entries");
        }
        if (new HashSet<>(result).size() != result.size()) {
            throw new IllegalArgumentException(name + " must not contain duplicates");
        }
        return List.copyOf(result);
    }
}