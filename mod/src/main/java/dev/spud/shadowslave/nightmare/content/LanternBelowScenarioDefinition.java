package dev.spud.shadowslave.nightmare.content;

import dev.spud.shadowslave.nightmare.resolution.ResolutionGraph;
import dev.spud.shadowslave.nightmare.resolution.ResolutionTransition;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * DESIGN content for an authored First-Nightmare-style investigation and rescue scenario.
 *
 * <p>The definition owns immutable authored content and a pure resolution graph only.
 * Persistence, challenger outcomes, appraisal, rewards, spawning, NPC decisions and world
 * mutation remain Java-core/runtime concerns outside this content slice.</p>
 */
public final class LanternBelowScenarioDefinition {
    public static final String SCENARIO_ID = "the_lantern_below";
    public static final String HISTORICAL_ROLE_ID = "survey_clerks_assistant";

    private static final String INITIAL = "upper_gallery_after_collapse";
    private static final String TRACE_FOUND = "trace_found";
    private static final String ROUTE_MARKED = "route_marked";
    private static final String LOWER_GALLERY = "lower_gallery_reached";
    private static final String AIR_READY = "air_route_open";
    private static final String TEAM_FOUND = "trapped_team_found";
    private static final String LEDGER_FOUND = "shift_ledger_found";
    private static final String ALL_RESCUED = "all_rescued";
    private static final String INJURED_RESCUED = "injured_rescued";
    private static final String BREACH_SEALED = "breach_sealed";
    private static final String LEDGER_EXPOSED = "ledger_exposed";
    private static final String EVIDENCE_CARRIED = "evidence_carried";

    private LanternBelowScenarioDefinition() {
    }

    public static ScenarioContent content() {
        return new ScenarioContent(
                SCENARIO_ID,
                "The Lantern Below",
                HISTORICAL_ROLE_ID,
                "Survey Clerk's Assistant",
                "A night shift has vanished beneath a mountain archive after a chain of underground collapses. The challenger inherits route ledgers, handwriting knowledge and signal conventions but no command authority. Dust, aftershocks and contradictory testimony make finding the missing workers as dangerous as deciding which evidence to trust.",
                List.of(
                        new Location("upper_gallery", "Upper Gallery", "The surviving work camp beneath the archive, crowded with frightened labourers and conflicting accounts of the collapse."),
                        new Location("marker_stair", "Marker Stair", "A steep survey descent where intact route marks alternate with fresh chalk that does not match the clerk's ledger."),
                        new Location("vent_chamber", "Vent Chamber", "A narrow junction whose old air shaft can clear dust from one lower approach if opened before another aftershock blocks it."),
                        new Location("lower_gallery", "Lower Gallery", "A fractured work level filled with fallen props, abandoned lamps and intermittent knocking from beyond the rubble."),
                        new Location("sealed_archive", "Sealed Archive", "A records room beside the deepest breach, containing the shift ledger and evidence that the missing crew was sent below after the route was declared unsafe.")
                ),
                List.of(
                        new Character("iven", "Iven", "The senior surveyor who remembers the old marker system but is concussed and repeatedly loses the sequence of recent events."),
                        new Character("dassa", "Dassa", "An injured porter trapped near the lower gallery who can identify where the missing crew split after the first collapse."),
                        new Character("halek", "Halek", "The archive overseer who insists the missing shift ignored orders, while pressing the rescue party to protect the sealed records first."),
                        new Character("rui", "Rui", "A missing chalk runner whose distinctive doubled route marks can separate a genuine trail from hurried false markings."),
                        new Character("senn", "Senn", "The rescue foreman willing to commit workers underground once the challenger can give him a route he considers defensible.")
                ),
                List.of(
                        new Pressure("dust_suffocation", "Dust Suffocation", "Fine stone dust thickens in low passages and turns prolonged searching into a survival problem."),
                        new Pressure("aftershocks", "Aftershocks", "Repeated tremors can invalidate a route that looked safe only moments before."),
                        new Pressure("unstable_supports", "Unstable Supports", "Damaged props make speed and careful verification compete with each other."),
                        new Pressure("false_markers", "False Markers", "Fresh chalk points toward a dangerous dead end and does not match Rui's known hand."),
                        new Pressure("failing_air", "Failing Air", "Smoke and dust collect below unless the old vent route is deliberately reopened."),
                        new Pressure("split_testimony", "Split Testimony", "Survivors disagree about who ordered the missing shift below and why the route ledger was altered.")
                ),
                List.of(
                        new Choice("inspect_guide_rope", "Inspect the guide rope", "Compare breaks, knots and dust on the surviving line to establish where the missing shift actually descended."),
                        new Choice("question_survivors", "Question the survivors separately", "Compare contradictory accounts before treating any one witness as authoritative."),
                        new Choice("mark_safe_route", "Mark a defensible return route", "Use stable survey references so rescuers can retreat even if another aftershock changes the lower passages."),
                        new Choice("follow_doubled_chalk", "Follow Rui's doubled chalk", "Reject the fresh false marks and descend along the missing runner's known marking habit."),
                        new Choice("open_old_vent", "Open the old vent", "Clear a bounded lower approach instead of assuming the whole mine becomes safe."),
                        new Choice("verify_knocking", "Verify the knocking", "Match the rhythm against shift signals before committing rescuers through unstable rubble."),
                        new Choice("guide_all_out", "Guide the trapped shift out", "Use the marked route and opened air path to move the surviving crew toward the upper gallery."),
                        new Choice("extract_injured_porter", "Extract the injured porter", "Take the nearest injured survivor out immediately rather than risking the entire rescue party deeper below."),
                        new Choice("recover_shift_ledger", "Recover the shift ledger", "Retrieve the record beside the breach and compare its overwritten route order with the clerk's known hand."),
                        new Choice("seal_deep_breach", "Seal the deep breach", "Close the unstable archive throat before the next tremor can propagate through the occupied galleries."),
                        new Choice("confront_overseer", "Confront the overseer with the ledger", "Force the altered order into the open while witnesses and rescuers are still assembled."),
                        new Choice("carry_evidence_out", "Carry the evidence out", "Leave the deeper search unresolved and preserve proof of the altered order before the archive collapses.")
                ),
                Map.of(
                        "all_rescued", new ResolutionContent(
                                "all_rescued",
                                "Every Lantern Returns",
                                "The false trail is rejected, a return route is marked, the lower air path is opened and the surviving shift is brought back together before the galleries close again.",
                                Map.of("investigation", 4, "guidance", 4, "preservation", 4, "endurance", 2, "adaptation", 3)
                        ),
                        "injured_rescued", new ResolutionContent(
                                "injured_rescued",
                                "One Voice From the Dark",
                                "The nearest injured worker is brought safely out while the deeper search is abandoned before the rescue party is consumed by the same collapse.",
                                Map.of("rescue", 4, "restraint", 3, "preservation", 3, "sacrifice", 2, "judgment", 4)
                        ),
                        "breach_sealed", new ResolutionContent(
                                "breach_sealed",
                                "Stone Over the Deep",
                                "The altered shift order is found, but the unstable breach is sealed instead of pursued. The occupied galleries survive while the fate of anyone deeper remains unresolved.",
                                Map.of("investigation", 3, "containment", 4, "sacrifice", 4, "preservation", 3, "judgment", 3)
                        ),
                        "ledger_exposed", new ResolutionContent(
                                "ledger_exposed",
                                "Names Written Clearly",
                                "The overwritten shift order is exposed before witnesses, breaking the false account of why the missing crew went below and forcing the surviving camp to face what happened.",
                                Map.of("investigation", 4, "truth", 4, "social", 3, "resolve", 2, "consequence", 3)
                        ),
                        "evidence_carried", new ResolutionContent(
                                "evidence_carried",
                                "What the Mountain Could Not Bury",
                                "The challenger leaves the collapsing archive with the altered ledger intact, preserving proof while accepting that the deeper rescue remains unfinished.",
                                Map.of("investigation", 4, "preservation", 3, "sacrifice", 3, "adaptation", 2, "consequence", 4)
                        )
                )
        );
    }

    public static ResolutionGraph resolutionGraph() {
        return new ResolutionGraph(
                INITIAL,
                Set.of(
                        INITIAL,
                        TRACE_FOUND,
                        ROUTE_MARKED,
                        LOWER_GALLERY,
                        AIR_READY,
                        TEAM_FOUND,
                        LEDGER_FOUND,
                        ALL_RESCUED,
                        INJURED_RESCUED,
                        BREACH_SEALED,
                        LEDGER_EXPOSED,
                        EVIDENCE_CARRIED
                ),
                List.of(
                        new ResolutionTransition(INITIAL, "inspect_guide_rope", TRACE_FOUND),
                        new ResolutionTransition(INITIAL, "question_survivors", TRACE_FOUND),
                        new ResolutionTransition(TRACE_FOUND, "mark_safe_route", ROUTE_MARKED),
                        new ResolutionTransition(TRACE_FOUND, "recover_shift_ledger", LEDGER_FOUND),
                        new ResolutionTransition(ROUTE_MARKED, "follow_doubled_chalk", LOWER_GALLERY),
                        new ResolutionTransition(ROUTE_MARKED, "recover_shift_ledger", LEDGER_FOUND),
                        new ResolutionTransition(LOWER_GALLERY, "open_old_vent", AIR_READY),
                        new ResolutionTransition(LOWER_GALLERY, "recover_shift_ledger", LEDGER_FOUND),
                        new ResolutionTransition(AIR_READY, "verify_knocking", TEAM_FOUND),
                        new ResolutionTransition(AIR_READY, "recover_shift_ledger", LEDGER_FOUND),
                        new ResolutionTransition(TEAM_FOUND, "guide_all_out", ALL_RESCUED),
                        new ResolutionTransition(TEAM_FOUND, "extract_injured_porter", INJURED_RESCUED),
                        new ResolutionTransition(LEDGER_FOUND, "seal_deep_breach", BREACH_SEALED),
                        new ResolutionTransition(LEDGER_FOUND, "confront_overseer", LEDGER_EXPOSED),
                        new ResolutionTransition(LEDGER_FOUND, "carry_evidence_out", EVIDENCE_CARRIED)
                ),
                Map.of(
                        ALL_RESCUED, "all_rescued",
                        INJURED_RESCUED, "injured_rescued",
                        BREACH_SEALED, "breach_sealed",
                        LEDGER_EXPOSED, "ledger_exposed",
                        EVIDENCE_CARRIED, "evidence_carried"
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
            List<Pressure> pressures,
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
            pressures = List.copyOf(Objects.requireNonNull(pressures, "pressures"));
            choices = List.copyOf(Objects.requireNonNull(choices, "choices"));
            resolutions = Map.copyOf(Objects.requireNonNull(resolutions, "resolutions"));
            if (locations.isEmpty() || characters.isEmpty() || pressures.isEmpty() || choices.isEmpty() || resolutions.size() < 2) {
                throw new IllegalArgumentException("Scenario content must provide locations, characters, pressures, choices and multiple resolutions");
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
