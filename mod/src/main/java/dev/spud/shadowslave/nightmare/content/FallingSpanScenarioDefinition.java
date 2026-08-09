package dev.spud.shadowslave.nightmare.content;

import dev.spud.shadowslave.nightmare.resolution.ResolutionGraph;
import dev.spud.shadowslave.nightmare.resolution.ResolutionTransition;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * DESIGN content for an authored First-Nightmare-style scenario focused on
 * evacuation, timing, sacrifice and world-driven resolution rather than a boss.
 *
 * <p>The Java core owns the immutable scenario definition and pure resolution
 * graph. World, NPC, structure and presentation adapters may execute resolved
 * events later, but cannot redefine terminal resolution or appraisal evidence.</p>
 */
public final class FallingSpanScenarioDefinition {
    public static final String SCENARIO_ID = "the_falling_span";
    public static final String HISTORICAL_ROLE_ID = "span_ward_runner";

    private static final String INITIAL = "column_stalled";
    private static final String ANCHORS_CHECKED = "anchors_checked";
    private static final String LOWER_PATH_FOUND = "lower_path_found";
    private static final String CIVILIANS_MOVING = "civilians_moving";
    private static final String BRIDGE_CLEARED = "bridge_cleared";
    private static final String PURSUERS_DELAYED = "pursuers_delayed";
    private static final String CLIFF_PREPARED = "cliff_prepared";
    private static final String PARLEY_OPEN = "parley_open";

    private static final String LAST_CROSSING = "last_crossing";
    private static final String PATH_BELOW = "path_below";
    private static final String ROAD_DENIED = "road_denied";
    private static final String MOUNTAIN_DECIDES = "mountain_decides";
    private static final String PASSAGE_BARGAINED = "passage_bargained";

    private FallingSpanScenarioDefinition() {
    }

    public static ScenarioContent content() {
        return new ScenarioContent(
                SCENARIO_ID,
                "The Falling Span",
                HISTORICAL_ROLE_ID,
                "Span-Ward Runner",
                "A refugee column is trapped at a failing suspension bridge while an armed pursuit closes from the pass behind. The challenger inherits a runner's knowledge of the bridge, cliff paths and signal code, but no authority to command everyone. The conflict can end through evacuation, denial, negotiation or a delayed collapse prepared earlier and completed by the mountain itself.",
                List.of(
                        new Location("wind_span", "Wind Span", "A rope-and-timber suspension bridge stretched over a deep gorge. Several anchor lashings have begun to slip under storm strain."),
                        new Location("west_anchor", "West Anchor House", "A stone recess containing spare line, old wedges and the bridge ward's maintenance marks."),
                        new Location("goat_stair", "Goat Stair", "A narrow maintenance descent below the main road. It is slower and exposed, but bypasses the bridge for small groups."),
                        new Location("scree_shelf", "Scree Shelf", "A fractured slope above the pursuit road where loose stone can be destabilised without knowing exactly when the storm will finish the work."),
                        new Location("east_gate", "East Gate", "A cramped refuge beyond the gorge where the fleeing column can regroup if enough people cross before the weather worsens.")
                ),
                List.of(
                        new Character("sera", "Sera", "The senior bridge ward, injured during the first anchor failure. She knows which repairs are temporary and refuses to call the span safe."),
                        new Character("halven", "Halven", "A merchant elder demanding that wagons and ledgers cross before refugees on foot, because much of the settlement's winter stock is loaded behind them."),
                        new Character("toma", "Toma", "A shepherd who used the Goat Stair as a child and can confirm where the lower route rejoins the eastern road."),
                        new Character("captain_rusk", "Captain Rusk", "The leader of the pursuing levy. He wants fugitives returned and supplies surrendered, but is not written as automatically suicidal or immune to negotiation."),
                        new Character("ivi", "Ivi", "A young signal keeper carrying the bridge ward's horn codes. She can relay a crossing order or a warning, but cannot make the crowd obey it by magic.")
                ),
                List.of(
                        new Pressure("storm_strain", "Storm Strain", "Crosswinds and rain worsen the bridge condition over time, but this module defines no canonical timer or failure probability."),
                        new Pressure("crowd_panic", "Crowd Panic", "Refugees, carts and competing priorities make movement difficult without turning NPC behaviour into a fixed universal morale formula."),
                        new Pressure("armed_pursuit", "Armed Pursuit", "The levy approaching from the pass creates urgency without making combat or killing its captain a required resolution."),
                        new Pressure("limited_authority", "Limited Authority", "The inherited runner role provides local knowledge and signals, not unquestioned command over either side."),
                        new Pressure("unstable_cliff", "Unstable Cliff", "The scree shelf can be prepared for a later collapse, while exact timing and physical simulation remain runtime DESIGN.")
                ),
                List.of(
                        new ScenarioEvent("inspect_anchors", "Inspect the failing anchors", EventActor.PLAYER, "Use the ward marks to identify which bridge lines can support an organised crossing."),
                        new ScenarioEvent("scout_lower_path", "Scout the Goat Stair", EventActor.PLAYER, "Verify whether the maintenance descent can carry people who abandon carts and heavy loads."),
                        new ScenarioEvent("organize_crossing", "Organise a foot crossing", EventActor.PLAYER, "Use the signal code and local knowledge to move people in bounded groups without declaring the bridge universally safe."),
                        new ScenarioEvent("clear_bridge", "Clear the final group", EventActor.PLAYER, "Get the last organised group beyond the eastern anchor before deciding the span's fate."),
                        new ScenarioEvent("cut_span", "Cut the span", EventActor.PLAYER, "Sever the bridge. The same act has a different outcome depending on whether the crossing was completed first."),
                        new ScenarioEvent("guide_lower_path", "Guide the lower route", EventActor.PLAYER, "Lead a reduced column down the Goat Stair and abandon the main crossing."),
                        new ScenarioEvent("delay_pursuers", "Delay the pursuing levy", EventActor.PLAYER, "Use gates, debris and false movement cues to buy time without requiring a kill."),
                        new ScenarioEvent("prepare_rockfall", "Prepare the scree shelf", EventActor.PLAYER, "Loosen selected supports so a later storm-driven collapse can block the road."),
                        new ScenarioEvent("storm_breaks_cliff", "The storm breaks the shelf", EventActor.WORLD, "A delayed world event completes the prepared rockfall; the player does not personally perform the final terminal action."),
                        new ScenarioEvent("open_parley", "Open a parley", EventActor.PLAYER, "Use the gorge and damaged bridge as leverage to force a pause long enough for terms to be heard."),
                        new ScenarioEvent("accept_passage_terms", "Accept bounded passage terms", EventActor.NPC, "The opposing captain accepts a limited crossing arrangement. This is authored scenario logic, not a universal persuasion system.")
                ),
                Map.of(
                        "last_crossing", new ResolutionContent(
                                "last_crossing",
                                "Last Feet Across",
                                "The refugee column crosses on foot, the final group clears the eastern anchor, and only then is the failing span cut behind them.",
                                Map.of("preservation", 4, "guidance", 4, "timing", 3, "sacrifice", 2, "resolve", 2)
                        ),
                        "path_below", new ResolutionContent(
                                "path_below",
                                "Road Beneath the Road",
                                "The column abandons wagons and the obvious crossing, descending the maintenance stair to escape by a slower route below the span.",
                                Map.of("discovery", 3, "adaptation", 4, "guidance", 4, "sacrifice", 2, "movement", 3)
                        ),
                        "road_denied", new ResolutionContent(
                                "road_denied",
                                "Nothing Crosses",
                                "The bridge is cut before the evacuation is complete, denying immediate pursuit at the cost of stranding people and supplies on the western side.",
                                Map.of("denial", 4, "consequence", 4, "sacrifice", 4, "resolve", 2, "preservation", 1)
                        ),
                        "mountain_decides", new ResolutionContent(
                                "mountain_decides",
                                "When the Mountain Moves",
                                "Earlier preparation leaves the scree shelf ready. Later, the storm releases the slope and buries the pursuit road without requiring the challenger to perform the final act.",
                                Map.of("preparation", 4, "adaptation", 3, "indirect_action", 4, "endurance", 2, "consequence", 3)
                        ),
                        "passage_bargained", new ResolutionContent(
                                "passage_bargained",
                                "Terms at the Gorge",
                                "The damaged crossing becomes leverage for a temporary passage agreement, ending the immediate pursuit without requiring either side to be destroyed.",
                                Map.of("negotiation", 4, "social", 4, "restraint", 3, "preservation", 2, "consequence", 2)
                        )
                )
        );
    }

    public static ResolutionGraph resolutionGraph() {
        return new ResolutionGraph(
                INITIAL,
                Set.of(
                        INITIAL,
                        ANCHORS_CHECKED,
                        LOWER_PATH_FOUND,
                        CIVILIANS_MOVING,
                        BRIDGE_CLEARED,
                        PURSUERS_DELAYED,
                        CLIFF_PREPARED,
                        PARLEY_OPEN,
                        LAST_CROSSING,
                        PATH_BELOW,
                        ROAD_DENIED,
                        MOUNTAIN_DECIDES,
                        PASSAGE_BARGAINED
                ),
                List.of(
                        new ResolutionTransition(INITIAL, "inspect_anchors", ANCHORS_CHECKED),
                        new ResolutionTransition(INITIAL, "scout_lower_path", LOWER_PATH_FOUND),
                        new ResolutionTransition(INITIAL, "cut_span", ROAD_DENIED),
                        new ResolutionTransition(INITIAL, "delay_pursuers", PURSUERS_DELAYED),
                        new ResolutionTransition(INITIAL, "open_parley", PARLEY_OPEN),

                        new ResolutionTransition(ANCHORS_CHECKED, "organize_crossing", CIVILIANS_MOVING),
                        new ResolutionTransition(ANCHORS_CHECKED, "scout_lower_path", LOWER_PATH_FOUND),
                        new ResolutionTransition(ANCHORS_CHECKED, "delay_pursuers", PURSUERS_DELAYED),
                        new ResolutionTransition(ANCHORS_CHECKED, "open_parley", PARLEY_OPEN),

                        new ResolutionTransition(CIVILIANS_MOVING, "clear_bridge", BRIDGE_CLEARED),
                        new ResolutionTransition(BRIDGE_CLEARED, "cut_span", LAST_CROSSING),

                        new ResolutionTransition(LOWER_PATH_FOUND, "guide_lower_path", PATH_BELOW),
                        new ResolutionTransition(LOWER_PATH_FOUND, "open_parley", PARLEY_OPEN),

                        new ResolutionTransition(PURSUERS_DELAYED, "prepare_rockfall", CLIFF_PREPARED),
                        new ResolutionTransition(PURSUERS_DELAYED, "inspect_anchors", ANCHORS_CHECKED),
                        new ResolutionTransition(PURSUERS_DELAYED, "open_parley", PARLEY_OPEN),

                        new ResolutionTransition(CLIFF_PREPARED, "storm_breaks_cliff", MOUNTAIN_DECIDES),

                        new ResolutionTransition(PARLEY_OPEN, "accept_passage_terms", PASSAGE_BARGAINED),
                        new ResolutionTransition(PARLEY_OPEN, "inspect_anchors", ANCHORS_CHECKED)
                ),
                Map.of(
                        LAST_CROSSING, "last_crossing",
                        PATH_BELOW, "path_below",
                        ROAD_DENIED, "road_denied",
                        MOUNTAIN_DECIDES, "mountain_decides",
                        PASSAGE_BARGAINED, "passage_bargained"
                )
        );
    }

    public enum EventActor {
        PLAYER,
        NPC,
        WORLD
    }

    public record ScenarioContent(
            String id,
            String displayName,
            String historicalRoleId,
            String historicalRoleName,
            String premise,
            List<Location> locations,
            List<Character> characters,
            List<Pressure> pressures,
            List<ScenarioEvent> events,
            Map<String, ResolutionContent> resolutions
    ) {
        public ScenarioContent {
            id = requireText(id, "id");
            displayName = requireText(displayName, "displayName");
            historicalRoleId = requireText(historicalRoleId, "historicalRoleId");
            historicalRoleName = requireText(historicalRoleName, "historicalRoleName");
            premise = requireText(premise, "premise");
            locations = List.copyOf(Objects.requireNonNull(locations, "locations"));
            characters = List.copyOf(Objects.requireNonNull(characters, "characters"));
            pressures = List.copyOf(Objects.requireNonNull(pressures, "pressures"));
            events = List.copyOf(Objects.requireNonNull(events, "events"));
            resolutions = Map.copyOf(Objects.requireNonNull(resolutions, "resolutions"));
            if (locations.size() < 4 || characters.size() < 4 || pressures.size() < 4 || events.isEmpty() || resolutions.size() < 2) {
                throw new IllegalArgumentException("Scenario content must provide locations, characters, pressures, events and multiple resolutions");
            }
        }
    }

    public record Location(String id, String name, String description) {
        public Location {
            id = requireText(id, "location id");
            name = requireText(name, "location name");
            description = requireText(description, "location description");
        }
    }

    public record Character(String id, String name, String description) {
        public Character {
            id = requireText(id, "character id");
            name = requireText(name, "character name");
            description = requireText(description, "character description");
        }
    }

    public record Pressure(String id, String name, String description) {
        public Pressure {
            id = requireText(id, "pressure id");
            name = requireText(name, "pressure name");
            description = requireText(description, "pressure description");
        }
    }

    public record ScenarioEvent(String eventId, String name, EventActor actor, String description) {
        public ScenarioEvent {
            eventId = requireText(eventId, "eventId");
            name = requireText(name, "event name");
            actor = Objects.requireNonNull(actor, "actor");
            description = requireText(description, "event description");
        }
    }

    public record ResolutionContent(String id, String name, String description, Map<String, Integer> evidenceWeights) {
        public ResolutionContent {
            id = requireText(id, "resolution id");
            name = requireText(name, "resolution name");
            description = requireText(description, "resolution description");
            evidenceWeights = Map.copyOf(Objects.requireNonNull(evidenceWeights, "evidenceWeights"));
            if (evidenceWeights.isEmpty() || evidenceWeights.values().stream().anyMatch(value -> value == null || value <= 0)) {
                throw new IllegalArgumentException("Resolution evidence weights must be non-empty and positive");
            }
        }
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }
}
