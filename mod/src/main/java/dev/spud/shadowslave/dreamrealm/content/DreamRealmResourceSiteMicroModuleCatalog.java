package dev.spud.shadowslave.dreamrealm.content;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Player-facing DESIGN micro-modules for already-authored Dream Realm resource hooks. */
public final class DreamRealmResourceSiteMicroModuleCatalog {
    private DreamRealmResourceSiteMicroModuleCatalog() {}

    public enum InteractionFamily {
        GATHERING,
        RECOVERY,
        VERIFICATION
    }

    public record ResourceSiteModule(
            String id,
            String regionId,
            String resourceId,
            String displayName,
            InteractionFamily family,
            List<String> approachCues,
            String decisionPrompt,
            List<String> decisionOptions,
            String negativeBoundary
    ) {
        public ResourceSiteModule {
            id = stableId(id);
            regionId = stableId(regionId);
            resourceId = stableId(resourceId);
            displayName = text(displayName, "displayName");
            family = Objects.requireNonNull(family, "family");
            approachCues = nonEmptyText(approachCues, "approachCues");
            if (approachCues.size() < 2) throw new IllegalArgumentException("approachCues must contain at least two cues");
            decisionPrompt = text(decisionPrompt, "decisionPrompt");
            decisionOptions = nonEmptyText(decisionOptions, "decisionOptions");
            if (decisionOptions.size() < 3) throw new IllegalArgumentException("decisionOptions must contain at least three options");
            negativeBoundary = text(negativeBoundary, "negativeBoundary");
        }
    }

    public record ResolvedResourceSite(
            String generatorVersion,
            long seed,
            String regionId,
            String resourceId,
            String moduleId,
            InteractionFamily family,
            String approachCue,
            String decisionPrompt,
            List<String> decisionOptions,
            String negativeBoundary
    ) {}

    public static List<ResourceSiteModule> waveOne() {
        List<ResourceSiteModule> modules = List.of(
                module("ashen_expanse_bone_char", "ashen_expanse", "bone_char", "Char-Bone Patch", InteractionFamily.RECOVERY,
                        "Brittle blackened bone protrudes where ash has thinned.", "Brush ash aside before lifting fragments.",
                        "Take only intact pieces; leave unstable ground undisturbed.", "This is mundane salvage DESIGN, not a guaranteed soul-shard, Memory, Echo, or rare-material source."),
                module("ashen_expanse_ruin_metal", "ashen_expanse", "ruin_metal", "Half-Buried Ruin Metal", InteractionFamily.RECOVERY,
                        "A dull metal edge catches beneath a collapsed wall line.", "Expose attachment points before pulling.",
                        "Recover loose pieces rather than destabilizing the ruin.", "Material quality, quantity, value, and respawn are UNKNOWN."),
                module("ashen_expanse_dry_fungus", "ashen_expanse", "dry_fungus", "Dry Shelf Fungus", InteractionFamily.GATHERING,
                        "Pale shelves cling to the lee side of buried stone.", "Check texture and smell before harvesting.",
                        "Cut a small sample and leave the bed intact.", "Edibility, alchemical use, yield, and regrowth are DESIGN/UNKNOWN, not canon."),
                module("chainfall_reach_chain_iron", "chainfall_reach", "chain_iron", "Weathered Chain Iron", InteractionFamily.RECOVERY,
                        "Flakes and small spalls collect where old chain links grind.", "Work from stable footing and avoid load-bearing sections.",
                        "Gather only detached pieces.", "No module authorizes cutting structural chains or assigns canonical material rarity."),
                module("chainfall_reach_sky_moss", "chainfall_reach", "sky_moss", "Sky Moss Mat", InteractionFamily.GATHERING,
                        "Blue-grey moss grips stone in wind-sheltered seams.", "Test footing before reaching toward the mat.",
                        "Harvest a narrow strip and preserve anchor growth.", "Use, yield, and regrowth are DESIGN/UNKNOWN."),
                module("chainfall_reach_cliff_nests", "chainfall_reach", "cliff_nests", "Abandoned Cliff Nest", InteractionFamily.VERIFICATION,
                        "Twigs and shell fragments sit in a recess above the drop.", "Observe for fresh movement before approaching.",
                        "Verify abandonment, then recover only loose mundane material.", "The site does not guarantee eggs, creatures, Echoes, Memories, or rare loot."),
                module("glassmere_flats_mirror_shard", "glassmere_flats", "mirror_shard", "Dull Mirror Shard", InteractionFamily.RECOVERY,
                        "A reflection flashes once beneath powdery glass.", "Probe around the shard before touching it.",
                        "Lift from the edges and wrap before travel.", "No shard is automatically magical, valuable, or a Memory component."),
                module("glassmere_flats_resonant_glass", "glassmere_flats", "resonant_glass", "Resonant Glass Seam", InteractionFamily.VERIFICATION,
                        "A buried seam hums when distant wind crosses the plain.", "Test with a light tap from cover.",
                        "Mark the seam and recover only detached fragments.", "Resonance is local DESIGN presentation; supernatural function and value remain UNKNOWN."),
                module("glassmere_flats_heatstone", "glassmere_flats", "heatstone", "Warm Heatstone", InteractionFamily.GATHERING,
                        "A dark stone remains warm after the surrounding glass cools.", "Compare its heat to nearby stones before taking it.",
                        "Collect only if handling remains safe.", "Heat retention, duration, uses, rarity, and recharge are DESIGN/UNKNOWN."),
                module("blackwater_steps_reed_fiber", "blackwater_steps", "reed_fiber", "Dry Reed Fiber", InteractionFamily.GATHERING,
                        "Pale reed strands hang above the current line.", "Check water movement before stepping down.",
                        "Cut dry upper fibers and retreat before the water rises.", "Fiber yield, crafting stats, and regrowth are DESIGN."),
                module("blackwater_steps_salt_crystal", "blackwater_steps", "salt_crystal", "Terrace Salt Crystal", InteractionFamily.RECOVERY,
                        "White crystals crust a sheltered stone lip.", "Test the surrounding water and stone before reaching in.",
                        "Chip only loose surface growth.", "Purity, food use, trade value, and replenishment are UNKNOWN."),
                module("blackwater_steps_driftwood", "blackwater_steps", "driftwood", "High-Water Driftwood", InteractionFamily.RECOVERY,
                        "Water-smoothed branches wedge above the flooded steps.", "Watch one full surge before descending.",
                        "Recover light pieces that can be carried without delaying retreat.", "No driftwood pile is a loot container or guaranteed rare resource."),
                module("thornwake_basin_thorn_resin", "thornwake_basin", "thorn_resin", "Thorn Resin Bead", InteractionFamily.GATHERING,
                        "Amber beads harden along old cuts in the briars.", "Confirm the vine is still before approaching.",
                        "Scrape hardened resin without opening fresh wounds in the growth.", "Toxicity, crafting use, rarity, and regrowth remain DESIGN/UNKNOWN."),
                module("thornwake_basin_bitter_fruit", "thornwake_basin", "bitter_fruit", "Bitter Basin Fruit", InteractionFamily.VERIFICATION,
                        "Dark fruit hangs behind a screen of hooked thorns.", "Inspect signs of feeding before tasting or carrying it.",
                        "Take a sample only after a safe path in and out is marked.", "Edibility and effects are UNKNOWN; this catalogue never treats visual similarity as safety."),
                module("thornwake_basin_living_fiber", "thornwake_basin", "living_fiber", "Living Briar Fiber", InteractionFamily.GATHERING,
                        "Flexible inner strands show where a vine has already split.", "Use existing breaks rather than cutting a fresh corridor.",
                        "Take a short length and leave the rooted mass intact.", "No canonical magical property, durability tier, or respawn rate is asserted."),
                module("mistwound_pass_cold_moss", "mistwound_pass", "cold_moss", "Cold Moss", InteractionFamily.GATHERING,
                        "Dark moss survives in a windless crack beneath the mist.", "Anchor yourself before kneeling near the edge.",
                        "Lift a small patch with its substrate intact.", "Medicinal use, cold resistance, yield, and regrowth are UNKNOWN."),
                module("mistwound_pass_echo_stone", "mistwound_pass", "echo_stone", "Echo Stone Chip", InteractionFamily.VERIFICATION,
                        "A loose pebble returns a second tap from the wrong direction.", "Test it against another known stone before collecting.",
                        "Record the response, then bag the loose chip.", "The echo does not reveal truth, paths, prophecy, or hidden objectives."),
                module("mistwound_pass_snowmelt", "mistwound_pass", "snowmelt", "Sheltered Snowmelt", InteractionFamily.VERIFICATION,
                        "Clear meltwater beads beneath an overhang between gusts.", "Check flow, debris, and local danger before filling a vessel.",
                        "Take only what can be carried immediately.", "Potability is not guaranteed by appearance; replenishment and purity are UNKNOWN."),
                module("bonewhite_march_bone_dust", "bonewhite_march", "bone_dust", "Bone Dust Pocket", InteractionFamily.RECOVERY,
                        "Fine white powder gathers in wind shadows beneath hollow ribs.", "Approach from the sheltered side and avoid breathing the plume.",
                        "Collect a sealed sample without disturbing supporting bone.", "Origin, potency, use, and value remain DESIGN/UNKNOWN."),
                module("bonewhite_march_pale_chitin", "bonewhite_march", "pale_chitin", "Pale Chitin Plate", InteractionFamily.RECOVERY,
                        "A weathered plate lies half-buried in the white plain.", "Check for tracks and recent disturbance first.",
                        "Lift only loose plate material and leave carcass excavation optional.", "No creature identity, Rank/Class, crafting tier, or drop ownership is inferred."),
                module("bonewhite_march_fossil_resin", "bonewhite_march", "fossil_resin", "Fossil Resin Nodule", InteractionFamily.RECOVERY,
                        "A dull amber nodule shows through cracked mineral crust.", "Test the crust before levering it free.",
                        "Recover exposed nodules without widening unstable cracks.", "Age, origin, rarity, value, and supernatural function are UNKNOWN."),
                module("hollow_causeway_old_wire", "hollow_causeway", "old_wire", "Old Road Wire", InteractionFamily.RECOVERY,
                        "Tarnished wire loops from a collapsed wall conduit.", "Trace both ends before pulling.",
                        "Take only disconnected lengths.", "The wire has no guaranteed power, trap, map, or artifact function."),
                module("hollow_causeway_lamp_oil", "hollow_causeway", "lamp_oil", "Sealed Lamp Oil", InteractionFamily.VERIFICATION,
                        "A stoppered clay lamp still holds dark liquid.", "Inspect the seal and vessel before opening it.",
                        "Carry the sealed vessel until a safe testing place is available.", "Fuel quality, age, toxicity, quantity, and trade value are UNKNOWN."),
                module("hollow_causeway_masonry", "hollow_causeway", "masonry", "Loose Masonry", InteractionFamily.RECOVERY,
                        "Squared stone blocks have fallen clear of the old road wall.", "Check the ceiling and wall before moving any block.",
                        "Recover only pieces already detached from structural support.", "No module authorizes damaging intact ruins or grants building progression."),
                module("storm_lantern_coast_stormglass", "storm_lantern_coast", "stormglass", "Stormglass Fragment", InteractionFamily.VERIFICATION,
                        "A translucent fragment ticks softly before distant thunder.", "Observe it through one weather change before handling.",
                        "Wrap and mark the fragment for later comparison.", "Its response is DESIGN; forecasting accuracy, magical use, rarity, and recharge are UNKNOWN."),
                module("storm_lantern_coast_rope", "storm_lantern_coast", "rope", "Weathered Rope Coil", InteractionFamily.RECOVERY,
                        "A salt-stiff coil lies caught above the surge line.", "Check anchor points and strand condition before trusting it.",
                        "Recover usable loose lengths but do not assume load-bearing strength.", "No canonical durability or guaranteed safety is assigned."),
                module("storm_lantern_coast_salted_fish", "storm_lantern_coast", "salted_fish", "Abandoned Salted Fish", InteractionFamily.VERIFICATION,
                        "A wrapped bundle smells of salt beneath a dry ledge.", "Check packaging, pests, and age before deciding whether to carry it.",
                        "Prefer sealed portions; discard doubtful food.", "Edibility, ownership, quantity, and respawn are UNKNOWN."),
                module("red_canopy_red_sap", "red_canopy", "red_sap", "Red Sap", InteractionFamily.GATHERING,
                        "Bright sap beads where rain has reopened an old bark scar.", "Watch the trunk and surrounding vines before approaching.",
                        "Collect runoff from an existing wound rather than cutting a new one.", "Toxicity, alchemy, rarity, and regrowth are DESIGN/UNKNOWN."),
                module("red_canopy_rainfruit", "red_canopy", "rainfruit", "Rainfruit Cluster", InteractionFamily.VERIFICATION,
                        "Heavy fruit cups rainwater high above the flooded floor.", "Check bite marks, smell, and route stability before harvesting.",
                        "Take a small sample and keep a retreat route open.", "Edibility, hydration value, buffs, and regrowth are UNKNOWN."),
                module("red_canopy_bark_fiber", "red_canopy", "bark_fiber", "Canopy Bark Fiber", InteractionFamily.GATHERING,
                        "Long fibers peel from already-shed strips of red bark.", "Use fallen or naturally loosened bark first.",
                        "Take only what can be dried and carried without slowing travel.", "Strength, crafting recipes, rarity, and replenishment are DESIGN/UNKNOWN.")
        );
        validate(modules);
        return modules;
    }

    public static ResolvedResourceSite compose(long seed, String regionId, String resourceId) {
        String checkedRegion = stableId(regionId);
        String checkedResource = stableId(resourceId);
        ResourceSiteModule module = waveOne().stream()
                .filter(candidate -> candidate.regionId().equals(checkedRegion) && candidate.resourceId().equals(checkedResource))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown Dream Realm resource anchor: "
                        + checkedRegion + "/" + checkedResource));
        int cueIndex = Math.floorMod(mix(seed, module.id()), module.approachCues().size());
        return new ResolvedResourceSite(
                "dream-realm-resource-site-micro-module-v1",
                seed,
                module.regionId(),
                module.resourceId(),
                module.id(),
                module.family(),
                module.approachCues().get(cueIndex),
                module.decisionPrompt(),
                module.decisionOptions(),
                module.negativeBoundary()
        );
    }

    private static ResourceSiteModule module(
            String id, String regionId, String resourceId, String displayName, InteractionFamily family,
            String firstCue, String secondCue, String decisionPrompt, String negativeBoundary
    ) {
        return new ResourceSiteModule(
                id, regionId, resourceId, displayName, family,
                List.of(firstCue, secondCue),
                decisionPrompt,
                List.of("inspect_or_test", "take_a_bounded_sample", "leave_site_unchanged"),
                negativeBoundary
        );
    }

    private static void validate(List<ResourceSiteModule> modules) {
        Map<String, DreamRealmRegionContentCatalog.RegionProfile> regions = new HashMap<>();
        for (DreamRealmRegionContentCatalog.RegionProfile region : DreamRealmRegionContentCatalog.waveOne()) {
            regions.put(region.id(), region);
        }
        Set<String> ids = new HashSet<>();
        Set<String> anchors = new HashSet<>();
        for (ResourceSiteModule module : modules) {
            if (!ids.add(module.id())) throw new IllegalArgumentException("Duplicate module id: " + module.id());
            if (!anchors.add(module.regionId() + "/" + module.resourceId())) {
                throw new IllegalArgumentException("Duplicate resource anchor: " + module.regionId() + "/" + module.resourceId());
            }
            DreamRealmRegionContentCatalog.RegionProfile region = regions.get(module.regionId());
            if (region == null || !region.resourceHooks().contains(module.resourceId())) {
                throw new IllegalArgumentException("Module references an unauthored region resource hook: "
                        + module.regionId() + "/" + module.resourceId());
            }
        }
        int expected = regions.values().stream().mapToInt(region -> region.resourceHooks().size()).sum();
        if (modules.size() != expected || anchors.size() != expected) {
            throw new IllegalArgumentException("Resource-site modules must cover every authored resource hook exactly once");
        }
    }

    private static int mix(long seed, String salt) {
        long value = seed ^ ((long) salt.hashCode() << 32) ^ salt.hashCode();
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdl;
        value ^= value >>> 33;
        return (int) (value ^ (value >>> 32));
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

    private static List<String> nonEmptyText(List<String> values, String name) {
        List<String> copy = List.copyOf(Objects.requireNonNull(values, name));
        if (copy.isEmpty()) throw new IllegalArgumentException(name + " cannot be empty");
        for (String value : copy) text(value, name);
        return copy;
    }
}
