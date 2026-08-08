package dev.spud.shadowslave.dreamrealm.content;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/** Authored DESIGN environment content for future Dream Realm world generation. */
public final class DreamRealmRegionContentCatalog {
    private DreamRealmRegionContentCatalog() {}

    public enum Hazard {
        DEEP_DARKNESS,
        CRUSHING_PRESSURE,
        CORROSIVE_WATER,
        FLOOD_SURGE,
        UNSTABLE_GROUND,
        HOSTILE_FLORA,
        OPEN_EXPOSURE,
        CONCEALING_MIST,
        FALLING_DEBRIS,
        RESONANCE_STORMS
    }

    public enum Traversal {
        OPEN_GROUND,
        CLIMBING,
        CHAIN_CROSSING,
        BOATING,
        SWIMMING,
        TUNNELS,
        SHELTER_DASH,
        NARROW_PATHS
    }

    public enum Opportunity {
        SHELTER,
        WATER,
        FOOD,
        SALVAGE,
        SOUL_SHARD_HUNTING,
        OBSERVATION,
        TRADE_ROUTE,
        SHORTCUT,
        DEFENSIBLE_CAMP,
        RARE_MATERIAL
    }

    public record RegionProfile(
            String id,
            String displayName,
            Set<String> themeTags,
            Set<Hazard> hazards,
            Set<Traversal> traversal,
            Set<Opportunity> opportunities,
            Set<String> creatureAffinityIds,
            Set<String> landmarkHooks,
            Set<String> resourceHooks,
            String arrivalCue,
            String travelRule
    ) {
        public RegionProfile {
            id = stableId(id);
            displayName = text(displayName, "displayName");
            themeTags = nonEmptyTags(themeTags, "themeTags");
            hazards = nonEmptyCopy(hazards, "hazards");
            traversal = nonEmptyCopy(traversal, "traversal");
            opportunities = nonEmptyCopy(opportunities, "opportunities");
            creatureAffinityIds = tags(creatureAffinityIds, "creatureAffinityIds");
            landmarkHooks = nonEmptyTags(landmarkHooks, "landmarkHooks");
            resourceHooks = nonEmptyTags(resourceHooks, "resourceHooks");
            arrivalCue = text(arrivalCue, "arrivalCue");
            travelRule = text(travelRule, "travelRule");
        }
    }

    public static List<RegionProfile> waveOne() {
        List<RegionProfile> regions = List.of(
                region("ashen_expanse", "Ashen Expanse",
                        Set.of("ash", "darkness", "ruins", "open_sky"),
                        Set.of(Hazard.DEEP_DARKNESS, Hazard.OPEN_EXPOSURE, Hazard.UNSTABLE_GROUND),
                        Set.of(Traversal.OPEN_GROUND, Traversal.SHELTER_DASH),
                        Set.of(Opportunity.SALVAGE, Opportunity.OBSERVATION, Opportunity.SOUL_SHARD_HUNTING),
                        Set.of("ash_burrower", "veil_stalker"),
                        Set.of("buried_watchtower", "black_obelisk", "shattered_causeway"),
                        Set.of("bone_char", "ruin_metal", "dry_fungus"),
                        "Fine grey dust swallows every footstep beneath a lightless horizon.",
                        "Travel is fastest across exposed flats, but distant silhouettes share the same view."),
                region("chainfall_reach", "Chainfall Reach",
                        Set.of("floating_islands", "iron", "wind", "abyss"),
                        Set.of(Hazard.CRUSHING_PRESSURE, Hazard.FALLING_DEBRIS, Hazard.OPEN_EXPOSURE),
                        Set.of(Traversal.CHAIN_CROSSING, Traversal.CLIMBING, Traversal.NARROW_PATHS),
                        Set.of(Opportunity.SHORTCUT, Opportunity.OBSERVATION, Opportunity.DEFENSIBLE_CAMP),
                        Set.of("chainback", "glasswing"),
                        Set.of("severed_chain_root", "hanging_keep", "wind_bridge"),
                        Set.of("chain_iron", "sky_moss", "cliff_nests"),
                        "Gargantuan chains groan between islands suspended above an endless dark below.",
                        "Altitude is a resource: routes that climb too high become unsafe even when shorter."),
                region("glassmere_flats", "Glassmere Flats",
                        Set.of("glass", "reflection", "white_plain", "heat"),
                        Set.of(Hazard.OPEN_EXPOSURE, Hazard.UNSTABLE_GROUND, Hazard.RESONANCE_STORMS),
                        Set.of(Traversal.OPEN_GROUND, Traversal.NARROW_PATHS),
                        Set.of(Opportunity.RARE_MATERIAL, Opportunity.OBSERVATION, Opportunity.TRADE_ROUTE),
                        Set.of("glasswing", "bell_eater"),
                        Set.of("mirror_ridge", "red_hill", "singing_fissure"),
                        Set.of("mirror_shard", "resonant_glass", "heatstone"),
                        "The plain flashes like a broken mirror whenever the pale sky shifts.",
                        "Crossings are safest when reflections are dull and sightlines are short."),
                region("blackwater_steps", "Blackwater Steps",
                        Set.of("water", "terraces", "fog", "crossings"),
                        Set.of(Hazard.CORROSIVE_WATER, Hazard.CONCEALING_MIST, Hazard.FLOOD_SURGE),
                        Set.of(Traversal.BOATING, Traversal.NARROW_PATHS, Traversal.CLIMBING),
                        Set.of(Opportunity.TRADE_ROUTE, Opportunity.SHORTCUT, Opportunity.SHELTER),
                        Set.of("pale_ferryman", "drowned_listener"),
                        Set.of("drowned_stair", "rope_harbour", "empty_ferry_house"),
                        Set.of("reed_fiber", "salt_crystal", "driftwood"),
                        "Stone terraces vanish one by one beneath motionless black water and low white fog.",
                        "Water routes must be observed and tested before a crossing is committed."),
                region("thornwake_basin", "Thornwake Basin",
                        Set.of("forest", "thorns", "overgrowth", "ruins"),
                        Set.of(Hazard.HOSTILE_FLORA, Hazard.CONCEALING_MIST, Hazard.UNSTABLE_GROUND),
                        Set.of(Traversal.NARROW_PATHS, Traversal.CLIMBING, Traversal.TUNNELS),
                        Set.of(Opportunity.FOOD, Opportunity.RARE_MATERIAL, Opportunity.DEFENSIBLE_CAMP),
                        Set.of("thorn_matron", "mire_runner"),
                        Set.of("root_chapel", "sunken_garden", "stone_ring"),
                        Set.of("thorn_resin", "bitter_fruit", "living_fiber"),
                        "Dense scarlet briars stitch ruined stone into a single breathing wall.",
                        "The obvious path is noisy; patient travelers search for old stone under the growth."),
                region("mistwound_pass", "Mistwound Pass",
                        Set.of("mist", "mountain", "wind", "echo"),
                        Set.of(Hazard.CONCEALING_MIST, Hazard.FALLING_DEBRIS, Hazard.OPEN_EXPOSURE),
                        Set.of(Traversal.CLIMBING, Traversal.NARROW_PATHS, Traversal.SHELTER_DASH),
                        Set.of(Opportunity.SHORTCUT, Opportunity.OBSERVATION, Opportunity.SHELTER),
                        Set.of("veil_stalker", "hollow_mimic"),
                        Set.of("split_peak", "echo_gate", "weather_cairn"),
                        Set.of("cold_moss", "echo_stone", "snowmelt"),
                        "Wind tears holes in the mist just long enough to reveal fragments of the pass.",
                        "Move between verified landmarks; voices and silhouettes are not navigation aids."),
                region("bonewhite_march", "Bonewhite March",
                        Set.of("bone", "white_plain", "sun", "vast"),
                        Set.of(Hazard.OPEN_EXPOSURE, Hazard.FALLING_DEBRIS, Hazard.UNSTABLE_GROUND),
                        Set.of(Traversal.OPEN_GROUND, Traversal.SHELTER_DASH, Traversal.TUNNELS),
                        Set.of(Opportunity.SOUL_SHARD_HUNTING, Opportunity.OBSERVATION, Opportunity.SALVAGE),
                        Set.of("stone_maw", "glasswing"),
                        Set.of("rib_arch", "hollow_bone", "cracked_plain"),
                        Set.of("bone_dust", "pale_chitin", "fossil_resin"),
                        "A white plain curves toward the horizon beneath an unforgiving sky.",
                        "Open travel is fast; hollow structures are the only dependable breaks in exposure."),
                region("hollow_causeway", "Hollow Causeway",
                        Set.of("ruins", "absence", "road", "underground"),
                        Set.of(Hazard.DEEP_DARKNESS, Hazard.UNSTABLE_GROUND, Hazard.CONCEALING_MIST),
                        Set.of(Traversal.TUNNELS, Traversal.NARROW_PATHS, Traversal.CLIMBING),
                        Set.of(Opportunity.SALVAGE, Opportunity.SHORTCUT, Opportunity.SHELTER),
                        Set.of("hollow_mimic", "stone_maw"),
                        Set.of("empty_gatehouse", "buried_milestone", "collapsed_gallery"),
                        Set.of("old_wire", "lamp_oil", "masonry"),
                        "An ancient road continues underground after the city above it has vanished.",
                        "Mark every junction twice because repeating architecture makes backtracking unreliable."),
                region("storm_lantern_coast", "Storm Lantern Coast",
                        Set.of("coast", "storm", "bells", "cliffs"),
                        Set.of(Hazard.FLOOD_SURGE, Hazard.RESONANCE_STORMS, Hazard.FALLING_DEBRIS),
                        Set.of(Traversal.CLIMBING, Traversal.NARROW_PATHS, Traversal.BOATING),
                        Set.of(Opportunity.WATER, Opportunity.TRADE_ROUTE, Opportunity.DEFENSIBLE_CAMP),
                        Set.of("bell_eater", "drowned_listener", "chainback"),
                        Set.of("storm_belfry", "sea_gate", "cliff_lantern"),
                        Set.of("stormglass", "rope", "salted_fish"),
                        "Broken warning lanterns blink along cliffs while thunder answers bells below.",
                        "Routes change with each surge; high paths trade water safety for storm exposure."),
                region("red_canopy", "Red Canopy",
                        Set.of("jungle", "scarlet", "rain", "verticality"),
                        Set.of(Hazard.HOSTILE_FLORA, Hazard.FLOOD_SURGE, Hazard.OPEN_EXPOSURE),
                        Set.of(Traversal.CLIMBING, Traversal.SWIMMING, Traversal.NARROW_PATHS),
                        Set.of(Opportunity.FOOD, Opportunity.WATER, Opportunity.RARE_MATERIAL, Opportunity.SOUL_SHARD_HUNTING),
                        Set.of("thorn_matron", "mire_runner", "gutter_choir"),
                        Set.of("canopy_bridge", "flooded_temple", "giant_root"),
                        Set.of("red_sap", "rainfruit", "bark_fiber"),
                        "Scarlet leaves hide the sky while warm rain turns every hollow into a rushing stream.",
                        "The forest floor favors water routes; the canopy trades cover for dangerous height.")
        );
        validateUniqueIds(regions);
        return regions;
    }

    private static RegionProfile region(String id, String name, Set<String> themes, Set<Hazard> hazards,
                                        Set<Traversal> traversal, Set<Opportunity> opportunities,
                                        Set<String> creatureAffinityIds, Set<String> landmarks,
                                        Set<String> resources, String arrivalCue, String travelRule) {
        return new RegionProfile(id, name, themes, hazards, traversal, opportunities,
                creatureAffinityIds, landmarks, resources, arrivalCue, travelRule);
    }

    private static void validateUniqueIds(List<RegionProfile> regions) {
        HashSet<String> ids = new HashSet<>();
        for (RegionProfile region : regions) {
            if (!ids.add(region.id())) {
                throw new IllegalArgumentException("Duplicate Dream Realm region id: " + region.id());
            }
        }
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
        HashSet<String> result = new HashSet<>();
        for (String value : Objects.requireNonNull(source, name)) {
            result.add(stableId(value));
        }
        return Set.copyOf(result);
    }

    private static Set<String> nonEmptyTags(Set<String> source, String name) {
        Set<String> result = tags(source, name);
        if (result.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be empty");
        }
        return result;
    }

    private static <T> Set<T> nonEmptyCopy(Set<T> source, String name) {
        Set<T> result = Set.copyOf(Objects.requireNonNull(source, name));
        if (result.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be empty");
        }
        return result;
    }
}