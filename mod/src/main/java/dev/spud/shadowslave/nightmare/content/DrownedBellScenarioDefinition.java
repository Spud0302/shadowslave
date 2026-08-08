package dev.spud.shadowslave.nightmare.content;

import dev.spud.shadowslave.nightmare.resolution.ResolutionGraph;
import dev.spud.shadowslave.nightmare.resolution.ResolutionTransition;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * DESIGN content for a second authored First Nightmare scenario.
 *
 * <p>The scenario is intentionally expressed as immutable content data plus a
 * pure resolution graph. It does not own persistence, progression, appraisal,
 * or world state. Those remain responsibilities of the Java core.</p>
 */
public final class DrownedBellScenarioDefinition {
    public static final String SCENARIO_ID = "the_drowned_bell";
    public static final String HISTORICAL_ROLE_ID = "bell_keepers_apprentice";

    private static final String INITIAL = "storm_approaching";
    private static final String WARNING_READY = "warning_ready";
    private static final String TUNNELS_OPEN = "tunnels_open";
    private static final String FLOODGATE_REACHED = "floodgate_reached";
    private static final String CREATURE_LURED = "creature_lured";
    private static final String TOWER_HELD = "tower_held";
    private static final String VILLAGERS_EVACUATED = "villagers_evacuated";
    private static final String FLOOD_DIVERTED = "flood_diverted";
    private static final String CREATURE_BURIED = "creature_buried";

    private DrownedBellScenarioDefinition() {
    }

    public static ScenarioContent content() {
        return new ScenarioContent(
                SCENARIO_ID,
                "The Drowned Bell",
                HISTORICAL_ROLE_ID,
                "Bell-keeper's Apprentice",
                "A storm tide is swallowing an isolated cliff village while its warning bell stands silent. The challenger must decide what to preserve, whom to trust, and how to survive the single creature drawn to the bell's resonance.",
                List.of(
                        new Location("bell_tower", "Bell Tower", "A weathered warning tower overlooking the harbour road. Its cracked bronze bell can still carry across the cliffs if its clapper is repaired."),
                        new Location("quarry_tunnels", "Old Quarry Tunnels", "Abandoned stone-cutting passages above the high-water mark. They form a dangerous but viable evacuation route."),
                        new Location("sea_gate", "Sea Gate", "A timber-and-stone flood barrier that can divert part of the surge toward the abandoned quarry cut."),
                        new Location("lower_village", "Lower Village", "Homes, storehouses and a shrine crowded along the harbour terraces, directly exposed to the incoming tide.")
                ),
                List.of(
                        new Character("mara", "Mara", "The exhausted bell-keeper who knows the warning code but has a crushed hand and cannot repair the bell herself."),
                        new Character("oren", "Oren", "A quarry foreman who insists the old tunnels are unsafe, but knows which support walls can be opened into an escape route."),
                        new Character("vesh", "Vesh", "A harbour reeve focused on saving stores and the sea gate; he withholds labour from the tower unless convinced the warning matters."),
                        new Character("nemi", "Nemi", "A child who saw the creature following sound through the flooded caves and can reveal that it hunts resonance rather than sight.")
                ),
                new Creature(
                        "drowned_listener",
                        "Drowned Listener",
                        "Dormant Monster",
                        "A pale cave predator that tracks strong vibration and resonance. It is the scenario's sole Nightmare Creature; all other opposition is environmental or human.",
                        Set.of("sound_tracking", "ambush", "flood_adapted")
                ),
                List.of(
                        new Choice("repair_bell", "Repair the warning bell", "Restores a village-wide warning option but risks attracting the Drowned Listener."),
                        new Choice("open_quarry_route", "Open the quarry route", "Creates a high-ground evacuation path after negotiating with or bypassing Oren."),
                        new Choice("reach_floodgate", "Reach the sea gate", "Creates a chance to divert the storm surge before the lower village is overwhelmed."),
                        new Choice("ring_bell", "Ring the warning", "Warns the settlement and deliberately reveals the tower to the creature."),
                        new Choice("guide_evacuation", "Guide the evacuation", "Leads civilians through the opened quarry route instead of fighting for the harbour."),
                        new Choice("divert_flood", "Divert the flood", "Commits labour to the sea gate and sacrifices the quarry cut to spare most of the village."),
                        new Choice("lure_creature", "Lure the creature with the bell", "Uses repeated bell strikes to pull the Drowned Listener into the unstable quarry approach."),
                        new Choice("collapse_quarry", "Collapse the quarry mouth", "Buries the creature after it has been deliberately lured into position.")
                ),
                Map.of(
                        "tower_held", new ResolutionContent(
                                "tower_held",
                                "Last Bell Standing",
                                "The warning sounds until the last families reach high ground. The tower survives the creature's assault long enough for the village to scatter.",
                                Map.of("duty", 4, "warning", 4, "resolve", 3, "sacrifice", 2, "preservation", 2)
                        ),
                        "villagers_evacuated", new ResolutionContent(
                                "villagers_evacuated",
                                "Road Above the Tide",
                                "The challenger abandons the harbour and gets the trapped villagers through the quarry tunnels before the surge arrives.",
                                Map.of("guidance", 4, "movement", 3, "preservation", 4, "social", 2, "adaptation", 2)
                        ),
                        "flood_diverted", new ResolutionContent(
                                "flood_diverted",
                                "Break the Water",
                                "The sea gate is forced open toward the abandoned cut, redirecting the worst of the surge away from the lower village.",
                                Map.of("water", 4, "precision", 3, "sacrifice", 2, "preservation", 4, "endurance", 2)
                        ),
                        "creature_buried", new ResolutionContent(
                                "creature_buried",
                                "Silence Below Stone",
                                "The bell is used as bait, the Listener follows the resonance, and the quarry mouth is collapsed over it before the tide reaches the tower.",
                                Map.of("sound", 4, "warning", 2, "precision", 4, "retaliation", 3, "resolve", 2)
                        )
                )
        );
    }

    public static ResolutionGraph resolutionGraph() {
        return new ResolutionGraph(
                INITIAL,
                Set.of(
                        INITIAL,
                        WARNING_READY,
                        TUNNELS_OPEN,
                        FLOODGATE_REACHED,
                        CREATURE_LURED,
                        TOWER_HELD,
                        VILLAGERS_EVACUATED,
                        FLOOD_DIVERTED,
                        CREATURE_BURIED
                ),
                List.of(
                        new ResolutionTransition(INITIAL, "repair_bell", WARNING_READY),
                        new ResolutionTransition(INITIAL, "open_quarry_route", TUNNELS_OPEN),
                        new ResolutionTransition(INITIAL, "reach_floodgate", FLOODGATE_REACHED),

                        new ResolutionTransition(WARNING_READY, "ring_bell", TOWER_HELD),
                        new ResolutionTransition(WARNING_READY, "lure_creature", CREATURE_LURED),
                        new ResolutionTransition(WARNING_READY, "open_quarry_route", TUNNELS_OPEN),

                        new ResolutionTransition(TUNNELS_OPEN, "guide_evacuation", VILLAGERS_EVACUATED),
                        new ResolutionTransition(TUNNELS_OPEN, "repair_bell", WARNING_READY),
                        new ResolutionTransition(TUNNELS_OPEN, "reach_floodgate", FLOODGATE_REACHED),

                        new ResolutionTransition(FLOODGATE_REACHED, "divert_flood", FLOOD_DIVERTED),
                        new ResolutionTransition(FLOODGATE_REACHED, "repair_bell", WARNING_READY),
                        new ResolutionTransition(FLOODGATE_REACHED, "open_quarry_route", TUNNELS_OPEN),

                        new ResolutionTransition(CREATURE_LURED, "collapse_quarry", CREATURE_BURIED)
                ),
                Map.of(
                        TOWER_HELD, "tower_held",
                        VILLAGERS_EVACUATED, "villagers_evacuated",
                        FLOOD_DIVERTED, "flood_diverted",
                        CREATURE_BURIED, "creature_buried"
                )
        );
    }

    public record ScenarioContent(
            String id,
            String displayName,
            String historicalRoleId,
            String historicalRoleName,
            String premise,
            List<Location> locations,
            List<Character> characters,
            Creature creature,
            List<Choice> choices,
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
            creature = Objects.requireNonNull(creature, "creature");
            choices = List.copyOf(Objects.requireNonNull(choices, "choices"));
            resolutions = Map.copyOf(Objects.requireNonNull(resolutions, "resolutions"));
            if (locations.isEmpty() || characters.isEmpty() || choices.isEmpty() || resolutions.size() < 2) {
                throw new IllegalArgumentException("Scenario content must provide locations, characters, choices and multiple resolutions");
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

    public record Creature(String id, String name, String classification, String description, Set<String> traits) {
        public Creature {
            id = requireText(id, "creature id");
            name = requireText(name, "creature name");
            classification = requireText(classification, "creature classification");
            description = requireText(description, "creature description");
            traits = Set.copyOf(Objects.requireNonNull(traits, "creature traits"));
            if (traits.isEmpty()) {
                throw new IllegalArgumentException("Creature must expose at least one content trait");
            }
        }
    }

    public record Choice(String eventId, String name, String description) {
        public Choice {
            eventId = requireText(eventId, "eventId");
            name = requireText(name, "choice name");
            description = requireText(description, "choice description");
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
