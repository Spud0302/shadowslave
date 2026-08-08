package dev.spud.shadowslave.nightmare.content;

import dev.spud.shadowslave.nightmare.resolution.ResolutionGraph;
import dev.spud.shadowslave.nightmare.resolution.ResolutionTransition;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * DESIGN content for a social/deception-centered First Nightmare scenario.
 *
 * <p>The scenario is immutable authored content plus a pure resolution graph.
 * It deliberately has no required boss or creature-kill completion condition.
 * Persistence, progression, appraisal and runtime world state remain owned by
 * the Java core.</p>
 */
public final class HollowTreatyScenarioDefinition {
    public static final String SCENARIO_ID = "the_hollow_treaty";
    public static final String HISTORICAL_ROLE_ID = "hostage_interpreter";

    private static final String INITIAL = "truce_fracturing";
    private static final String LEDGER_FOUND = "ledger_found";
    private static final String WITNESS_SECURED = "witness_secured";
    private static final String COUNCIL_CONVENED = "council_convened";
    private static final String REFUGEES_READY = "refugees_ready";
    private static final String FALSE_COPY_READY = "false_copy_ready";
    private static final String ACCUSATION_TESTED = "accusation_tested";

    private static final String TREATY_RESTORED = "treaty_restored";
    private static final String PASSAGE_BROKERED = "passage_brokered";
    private static final String BRITTLE_PEACE = "brittle_peace";
    private static final String ACCUSER_BROKEN = "accuser_broken";
    private static final String TRUTH_BURIED = "truth_buried";

    private HollowTreatyScenarioDefinition() {
    }

    public static ScenarioContent content() {
        return new ScenarioContent(
                SCENARIO_ID,
                "The Hollow Treaty",
                HISTORICAL_ROLE_ID,
                "Hostage Interpreter",
                "Two exhausted hill settlements are hours from breaking a winter truce after an envoy dies and the signed treaty ledger disappears. The challenger inhabits the interpreter held as a guarantee of good faith: distrusted by both sides, able to understand both dialects, and uniquely positioned to discover whether the peace was betrayed, forged, or merely made impossible by fear.",
                List.of(
                        new Location("oath_hall", "Oath Hall", "A neutral stone hall where both delegations meet under armed watch. Every public accusation made here can harden the conflict."),
                        new Location("scribe_loft", "Scribe Loft", "A cramped archive above the hall containing discarded drafts, seal impressions and the interpreters' working notes."),
                        new Location("guest_quarters", "Guest Quarters", "Locked rooms where servants, witnesses and minor delegates are being held separately until the council decides whom to blame."),
                        new Location("snow_gate", "Snow Gate", "The only usable pass through the ridge. Refugees from both settlements are already gathering here before the weather closes it."),
                        new Location("bell_court", "Bell Court", "A public courtyard used for challenges, testimony and formal repudiation. A lie exposed here cannot easily be hidden again.")
                ),
                List.of(
                        new Character("sera", "Sera", "The senior interpreter who taught the challenger both treaty dialects. She insists the missing ledger contained a clause neither delegation wanted discussed."),
                        new Character("halvek", "Halvek", "Captain of the eastern escort. He wants an immediate arrest and believes delay is simply another western trick."),
                        new Character("maelin", "Maelin", "A western grain factor whose stores will be seized if the truce collapses. She knows who had access to the treaty seals."),
                        new Character("tovan", "Tovan", "A frightened page who carried the final ledger and saw someone replace its wrapping before the envoy's death."),
                        new Character("elder_ress", "Elder Ress", "The neutral keeper of the Snow Gate. Ress cares less about proving guilt than getting civilians through the pass before the storm closes it.")
                ),
                Set.of(
                        "mutual_distrust",
                        "translation_asymmetry",
                        "forged_evidence",
                        "hostage_status",
                        "closing_pass",
                        "civilian_pressure"
                ),
                List.of(
                        new Choice("search_archive", "Search the scribe loft", "Recover draft clauses, seal impressions and interpreter notes before either delegation confiscates them."),
                        new Choice("question_page", "Question the page privately", "Use shared vocabulary and the page's partial trust to reconstruct who handled the treaty ledger."),
                        new Choice("convene_council", "Force a joint council", "Risk your hostage status to bring both delegations into one room before accusations harden into violence."),
                        new Choice("expose_forgery", "Expose the forged clause", "Demonstrate how the treaty copy was altered and identify the contradiction both delegations can verify."),
                        new Choice("prepare_refugees", "Prepare the refugees", "Work with Elder Ress to separate civilians from armed delegations and organize passage before the storm closes the ridge."),
                        new Choice("negotiate_passage", "Broker neutral passage", "Trade face-saving concessions so both sides allow civilians through the Snow Gate without resolving the larger dispute."),
                        new Choice("plant_false_copy", "Create a false clean copy", "Use your knowledge of both dialects to construct a version of the treaty that removes the disputed clause."),
                        new Choice("present_false_copy", "Present the false copy", "Offer the fabricated text as if it were the recovered original, buying immediate peace at the cost of a concealed lie."),
                        new Choice("challenge_accuser", "Challenge the chief accusation", "Demand that Halvek repeat his claim under the Bell Court's public testimony rules."),
                        new Choice("expose_blackmail", "Expose the coercion", "Use contradictions in the accusation to reveal that the witness statement was produced under threat."),
                        new Choice("burn_ledger", "Destroy the recovered ledger", "Erase the strongest proof of the disputed clause so neither faction can use it as immediate justification for war."),
                        new Choice("withdraw_claim", "Withdraw your own testimony", "Refuse to translate the decisive accusation, forcing both delegations to proceed without the one witness they jointly recognize.")
                ),
                Map.of(
                        TREATY_RESTORED, new ResolutionContent(
                                TREATY_RESTORED,
                                "Words Returned to Stone",
                                "The altered clause is exposed in front of both delegations. Neither side gets the victory it wanted, but the immediate pretext for war collapses and the original truce is restored under hostile scrutiny.",
                                Map.of("truth", 4, "interpretation", 4, "courage", 3, "social", 3, "preservation", 2)
                        ),
                        PASSAGE_BROKERED, new ResolutionContent(
                                PASSAGE_BROKERED,
                                "The Gate Before the Verdict",
                                "The larger dispute remains unresolved, but civilians from both settlements cross the Snow Gate under a neutral guarantee before the storm seals the ridge.",
                                Map.of("mediation", 4, "guidance", 4, "preservation", 4, "sacrifice", 2, "pragmatism", 3)
                        ),
                        BRITTLE_PEACE, new ResolutionContent(
                                BRITTLE_PEACE,
                                "A Peace Made of Paper",
                                "A fabricated clean treaty is accepted long enough to stop the delegations from fighting. The bloodshed is prevented, but the settlement now rests on a lie that may later be discovered.",
                                Map.of("deception", 4, "adaptation", 3, "preservation", 3, "risk", 3, "interpretation", 2)
                        ),
                        ACCUSER_BROKEN, new ResolutionContent(
                                ACCUSER_BROKEN,
                                "The Bell Answers Back",
                                "The central accusation collapses under public questioning when the coerced testimony is exposed. The accuser loses the authority to begin hostilities, though the underlying grievance survives.",
                                Map.of("truth", 3, "social", 4, "resolve", 3, "precision", 4, "retaliation", 2)
                        ),
                        TRUTH_BURIED, new ResolutionContent(
                                TRUTH_BURIED,
                                "No Words Left to Fight Over",
                                "The recovered ledger is destroyed and the interpreter refuses to authenticate either surviving copy. With no mutually trusted text left, both delegations postpone war rather than commit to a claim they cannot prove.",
                                Map.of("deception", 3, "sacrifice", 3, "uncertainty", 4, "preservation", 2, "resolve", 2)
                        )
                )
        );
    }

    public static ResolutionGraph resolutionGraph() {
        return new ResolutionGraph(
                INITIAL,
                Set.of(
                        INITIAL,
                        LEDGER_FOUND,
                        WITNESS_SECURED,
                        COUNCIL_CONVENED,
                        REFUGEES_READY,
                        FALSE_COPY_READY,
                        ACCUSATION_TESTED,
                        TREATY_RESTORED,
                        PASSAGE_BROKERED,
                        BRITTLE_PEACE,
                        ACCUSER_BROKEN,
                        TRUTH_BURIED
                ),
                List.of(
                        new ResolutionTransition(INITIAL, "search_archive", LEDGER_FOUND),
                        new ResolutionTransition(INITIAL, "prepare_refugees", REFUGEES_READY),
                        new ResolutionTransition(INITIAL, "plant_false_copy", FALSE_COPY_READY),
                        new ResolutionTransition(INITIAL, "challenge_accuser", ACCUSATION_TESTED),

                        new ResolutionTransition(LEDGER_FOUND, "question_page", WITNESS_SECURED),
                        new ResolutionTransition(LEDGER_FOUND, "burn_ledger", TRUTH_BURIED),
                        new ResolutionTransition(LEDGER_FOUND, "prepare_refugees", REFUGEES_READY),

                        new ResolutionTransition(WITNESS_SECURED, "convene_council", COUNCIL_CONVENED),
                        new ResolutionTransition(WITNESS_SECURED, "burn_ledger", TRUTH_BURIED),

                        new ResolutionTransition(COUNCIL_CONVENED, "expose_forgery", TREATY_RESTORED),
                        new ResolutionTransition(COUNCIL_CONVENED, "withdraw_claim", TRUTH_BURIED),

                        new ResolutionTransition(REFUGEES_READY, "negotiate_passage", PASSAGE_BROKERED),
                        new ResolutionTransition(REFUGEES_READY, "search_archive", LEDGER_FOUND),

                        new ResolutionTransition(FALSE_COPY_READY, "present_false_copy", BRITTLE_PEACE),
                        new ResolutionTransition(FALSE_COPY_READY, "prepare_refugees", REFUGEES_READY),

                        new ResolutionTransition(ACCUSATION_TESTED, "expose_blackmail", ACCUSER_BROKEN),
                        new ResolutionTransition(ACCUSATION_TESTED, "search_archive", LEDGER_FOUND)
                ),
                Map.of(
                        TREATY_RESTORED, TREATY_RESTORED,
                        PASSAGE_BROKERED, PASSAGE_BROKERED,
                        BRITTLE_PEACE, BRITTLE_PEACE,
                        ACCUSER_BROKEN, ACCUSER_BROKEN,
                        TRUTH_BURIED, TRUTH_BURIED
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
            Set<String> pressures,
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
            pressures = Set.copyOf(Objects.requireNonNull(pressures, "pressures"));
            choices = List.copyOf(Objects.requireNonNull(choices, "choices"));
            resolutions = Map.copyOf(Objects.requireNonNull(resolutions, "resolutions"));
            if (locations.size() < 3 || characters.size() < 3 || pressures.size() < 3 || choices.isEmpty() || resolutions.size() < 2) {
                throw new IllegalArgumentException("Scenario content must provide multiple locations, characters, pressures, choices and resolutions");
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
