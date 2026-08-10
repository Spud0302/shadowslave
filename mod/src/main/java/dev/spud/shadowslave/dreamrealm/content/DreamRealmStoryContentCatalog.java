package dev.spud.shadowslave.dreamrealm.content;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/** Authored DESIGN settlement, faction, NPC and story content for Dream Realm frontier play. */
public final class DreamRealmStoryContentCatalog {
    private DreamRealmStoryContentCatalog() {}

    public enum FactionRole { WARDENS, GUIDES, SALVAGERS, TRADERS, HUNTERS, KEEPERS, FERRYMEN, SCHOLARS }
    public enum Service { SHELTER, TRADE, RUMORS, REPAIR, GUIDANCE, PROVISIONS, ESCORT, MEDIATION, SALVAGE, SCOUTING }

    public record StoryModule(
            String id,
            String displayName,
            String regionId,
            String settlementName,
            String factionName,
            FactionRole factionRole,
            Set<Service> services,
            Set<String> npcArchetypes,
            Set<String> tensions,
            Set<String> storyHooks,
            String arrivalCue,
            String standingRule
    ) {
        public StoryModule {
            id = stableId(id);
            displayName = text(displayName, "displayName");
            regionId = stableId(regionId);
            settlementName = text(settlementName, "settlementName");
            factionName = text(factionName, "factionName");
            factionRole = Objects.requireNonNull(factionRole, "factionRole");
            services = nonEmptyCopy(services, "services");
            npcArchetypes = nonEmptyTags(npcArchetypes, "npcArchetypes");
            tensions = nonEmptyTags(tensions, "tensions");
            storyHooks = nonEmptyTags(storyHooks, "storyHooks");
            arrivalCue = text(arrivalCue, "arrivalCue");
            standingRule = text(standingRule, "standingRule");
        }
    }

    public static List<StoryModule> waveOne() {
        List<StoryModule> modules = List.of(
                module("ashen_watch", "Ashen Watch", "ashen_expanse", "Cinder Rest", "Grey Lanterns", FactionRole.WARDENS,
                        Set.of(Service.SHELTER, Service.RUMORS, Service.SCOUTING),
                        Set.of("watch_captain", "ash_mapper", "ruin_scavenger"),
                        Set.of("shrinking_safe_route", "missing_patrol", "disputed_salvage"),
                        Set.of("trace_missing_patrol", "mark_new_shelter_line", "mediate_ruin_claim"),
                        "A ring of hooded lamps burns behind a wall built from scavenged black stone.",
                        "Standing rises when travelers improve shared warning routes instead of merely extracting salvage."),
                module("chainward_station", "Chainward Station", "chainfall_reach", "Anchor House", "Iron Pilgrims", FactionRole.GUIDES,
                        Set.of(Service.GUIDANCE, Service.ESCORT, Service.REPAIR),
                        Set.of("chain_guide", "rigging_smith", "height_scout"),
                        Set.of("broken_crossing", "guide_rivalry", "unsafe_shortcut"),
                        Set.of("repair_chain_crossing", "escort_supply_team", "survey_lower_route"),
                        "Signal flags snap above a stone house bolted directly into a colossal chain root.",
                        "Standing follows reliable route work; reckless shortcuts that endanger later travelers reduce trust."),
                module("mirror_exchange", "Mirror Exchange", "glassmere_flats", "Dullglass Market", "Veiled Ledger", FactionRole.TRADERS,
                        Set.of(Service.TRADE, Service.RUMORS, Service.MEDIATION),
                        Set.of("market_factor", "glass_appraiser", "route_broker"),
                        Set.of("false_material", "storm_price_spike", "caravan_dispute"),
                        Set.of("verify_resonant_glass", "settle_caravan_claim", "open_storm_bypass"),
                        "Canvas roofs are dusted to kill reflections, turning the market into a patch of deliberate dullness.",
                        "Standing rewards fair verification and dependable deliveries more than raw purchase volume."),
                module("blackwater_ferry", "Blackwater Ferry", "blackwater_steps", "Rope Harbour", "Stillwater Hands", FactionRole.FERRYMEN,
                        Set.of(Service.GUIDANCE, Service.ESCORT, Service.PROVISIONS),
                        Set.of("ferry_master", "reed_crafter", "water_reader"),
                        Set.of("lost_boat", "changing_channel", "fare_debt"),
                        Set.of("recover_lost_boat", "sound_safe_channel", "escort_debt_caravan"),
                        "Low boats hang beneath rope gantries while pole marks record yesterday's waterline.",
                        "Standing is earned by returning people and boats alive; abandoning passengers for cargo is remembered."),
                module("thornward_commune", "Thornward Commune", "thornwake_basin", "Stone Ring", "Briar Keepers", FactionRole.KEEPERS,
                        Set.of(Service.SHELTER, Service.PROVISIONS, Service.REPAIR),
                        Set.of("garden_keeper", "resin_worker", "path_cutter"),
                        Set.of("encroaching_briar", "food_shortage", "forbidden_burn"),
                        Set.of("clear_old_stone_path", "harvest_resin_safely", "contain_spreading_fire"),
                        "Ancient stones form the only open circle in a basin otherwise claimed by red thorns.",
                        "Standing favors careful stewardship; indiscriminate burning solves immediate problems but damages trust."),
                module("mist_cairn_house", "Mist Cairn House", "mistwound_pass", "Cairnhouse", "True Markers", FactionRole.GUIDES,
                        Set.of(Service.SHELTER, Service.GUIDANCE, Service.RUMORS),
                        Set.of("cairn_keeper", "echo_tester", "mountain_runner"),
                        Set.of("false_marker", "missing_runner", "route_fraud"),
                        Set.of("audit_cairn_line", "find_missing_runner", "expose_false_guide"),
                        "Every wall bears a different carved route mark, each checked by hand before dusk.",
                        "Standing depends on verified information; confident guesses are treated as dangerous misconduct."),
                module("bonefield_post", "Bonefield Post", "bonewhite_march", "Rib Shelter", "White Trackers", FactionRole.HUNTERS,
                        Set.of(Service.SCOUTING, Service.PROVISIONS, Service.SALVAGE),
                        Set.of("track_reader", "bone_worker", "hunt_coordinator"),
                        Set.of("migrating_predator", "spoiled_cache", "hunt_boundary"),
                        Set.of("map_predator_migration", "recover_cache", "enforce_hunt_boundary"),
                        "A shelter nestles under a rib-like arch whose shade can be seen from half a day's march away.",
                        "Standing rewards useful field knowledge and restraint when a hunt would threaten the post's survival."),
                module("causeway_archive", "Causeway Archive", "hollow_causeway", "Milestone Vault", "Road Rememberers", FactionRole.SCHOLARS,
                        Set.of(Service.RUMORS, Service.GUIDANCE, Service.SALVAGE),
                        Set.of("route_archivist", "lamp_keeper", "rubble_reader"),
                        Set.of("contradictory_map", "sealed_gallery", "stolen_record"),
                        Set.of("compare_old_routes", "open_sealed_gallery", "recover_stolen_record"),
                        "Numbered lamps lead to a vault where every surviving road fragment is copied onto slate.",
                        "Standing comes from preserving verifiable route knowledge, including evidence that disproves an old map."),
                module("storm_belfry_town", "Storm Belfry Town", "storm_lantern_coast", "High Bell", "Cliff Wardens", FactionRole.WARDENS,
                        Set.of(Service.SHELTER, Service.REPAIR, Service.ESCORT),
                        Set.of("bell_warden", "cliff_rigger", "surge_runner"),
                        Set.of("silent_warning_bell", "damaged_sea_gate", "evacuation_argument"),
                        Set.of("restore_warning_line", "brace_sea_gate", "choose_evacuation_route"),
                        "Ropes, bells and shuttered lamps connect stone houses stepped into the cliff face.",
                        "Standing favors warnings that protect the whole route network, even when they disrupt profitable crossings."),
                module("red_canopy_camp", "Red Canopy Camp", "red_canopy", "Root Market", "Raincutters", FactionRole.SALVAGERS,
                        Set.of(Service.TRADE, Service.PROVISIONS, Service.SCOUTING),
                        Set.of("canopy_scout", "sap_broker", "rope_climber"),
                        Set.of("flooded_cache", "canopy_claim", "predator_pressure"),
                        Set.of("recover_flooded_cache", "negotiate_canopy_claim", "open_high_route"),
                        "Platforms circle a giant root above flood level, linked by wet rope bridges and cargo nets.",
                        "Standing improves when salvage work leaves usable routes and warnings for those who follow."));
        validateUniqueIds(modules);
        return modules;
    }

    private static StoryModule module(String id, String displayName, String regionId, String settlementName,
                                      String factionName, FactionRole factionRole, Set<Service> services,
                                      Set<String> npcArchetypes, Set<String> tensions, Set<String> storyHooks,
                                      String arrivalCue, String standingRule) {
        return new StoryModule(id, displayName, regionId, settlementName, factionName, factionRole, services,
                npcArchetypes, tensions, storyHooks, arrivalCue, standingRule);
    }

    private static void validateUniqueIds(List<StoryModule> modules) {
        HashSet<String> ids = new HashSet<>();
        for (StoryModule module : modules) {
            if (!ids.add(module.id())) throw new IllegalArgumentException("Duplicate story module id: " + module.id());
        }
    }

    private static String stableId(String value) {
        String checked = text(value, "id").toLowerCase(Locale.ROOT);
        if (!checked.matches("[a-z0-9_]+")) throw new IllegalArgumentException("id must be stable lowercase snake_case");
        return checked;
    }

    private static String text(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) throw new IllegalArgumentException(name + " cannot be blank");
        return checked;
    }

    private static Set<String> nonEmptyTags(Set<String> source, String name) {
        HashSet<String> result = new HashSet<>();
        for (String value : Objects.requireNonNull(source, name)) result.add(stableId(value));
        if (result.isEmpty()) throw new IllegalArgumentException(name + " cannot be empty");
        return Set.copyOf(result);
    }

    private static <T> Set<T> nonEmptyCopy(Set<T> source, String name) {
        Set<T> result = Set.copyOf(Objects.requireNonNull(source, name));
        if (result.isEmpty()) throw new IllegalArgumentException(name + " cannot be empty");
        return result;
    }
}
