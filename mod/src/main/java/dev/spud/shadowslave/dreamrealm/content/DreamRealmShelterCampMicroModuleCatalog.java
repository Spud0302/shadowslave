package dev.spud.shadowslave.dreamrealm.content;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/** Authored DESIGN shelter/camp decisions for already-resolved Dream Realm regions. */
public final class DreamRealmShelterCampMicroModuleCatalog {
    public static final String GENERATOR_VERSION = "dream-realm-shelter-camp-micro-module-v1";

    private DreamRealmShelterCampMicroModuleCatalog() {}

    public enum CampFamily {
        SHELTER_EVALUATION,
        WATCH_AND_REST,
        CAMP_ABANDONMENT,
        INFORMATION_EXCHANGE,
        TEMPORARY_REFUGE
    }

    public record CampModule(
            String id,
            String regionId,
            CampFamily family,
            DreamRealmRegionContentCatalog.Hazard pressure,
            DreamRealmRegionContentCatalog.Opportunity opportunity,
            List<String> approachCues,
            String situation,
            String decisionPrompt,
            List<String> choices,
            String antiOverclaimBoundary
    ) {
        public CampModule {
            id = stableId(id);
            regionId = stableId(regionId);
            family = Objects.requireNonNull(family, "family");
            pressure = Objects.requireNonNull(pressure, "pressure");
            opportunity = Objects.requireNonNull(opportunity, "opportunity");
            approachCues = textList(approachCues, "approachCues", 2);
            situation = text(situation, "situation");
            decisionPrompt = text(decisionPrompt, "decisionPrompt");
            choices = textList(choices, "choices", 3);
            antiOverclaimBoundary = text(antiOverclaimBoundary, "antiOverclaimBoundary");
        }
    }

    public record ResolvedCampModule(
            String generatorVersion,
            long seed,
            String regionId,
            CampModule module,
            String approachCue
    ) {}

    public static List<CampModule> waveOne() {
        List<CampModule> modules = List.of(
                module("ash_ruin_lee", "ashen_expanse", CampFamily.SHELTER_EVALUATION,
                        DreamRealmRegionContentCatalog.Hazard.OPEN_EXPOSURE, DreamRealmRegionContentCatalog.Opportunity.SALVAGE,
                        "A broken wall interrupts the flat horizon with just enough height to cast a narrow lee.",
                        "Half-buried masonry forms a shallow pocket where ash has piled instead of blowing through.",
                        "The ruin can break sightlines, but unstable stone and open approaches make it a refuge to inspect rather than trust.",
                        "Use the ruin now, improve it briefly, or keep moving before exposure worsens?",
                        "Check the wall and ash floor before settling.", "Use the lee for a short bounded pause.", "Leave before the ruin becomes a commitment."),
                module("ash_watch_pause", "ashen_expanse", CampFamily.WATCH_AND_REST,
                        DreamRealmRegionContentCatalog.Hazard.DEEP_DARKNESS, DreamRealmRegionContentCatalog.Opportunity.OBSERVATION,
                        "Darkness erases the flats beyond a small circle of familiar ground.",
                        "The exposed horizon is unreadable except for silhouettes that move against the ash.",
                        "Rest trades attention for recovery while the region's broad sightlines remain useful only to someone watching.",
                        "How much attention can be spared without assuming the area is safe?",
                        "Keep a dedicated watch and shorten the pause.", "Rotate observation before anyone fully settles.", "Abandon rest if the horizon cannot be verified."),

                module("chain_anchor_bivouac", "chainfall_reach", CampFamily.TEMPORARY_REFUGE,
                        DreamRealmRegionContentCatalog.Hazard.OPEN_EXPOSURE, DreamRealmRegionContentCatalog.Opportunity.DEFENSIBLE_CAMP,
                        "A chain root creates a pocket of stone screened from one side by iron links.",
                        "A narrow shelf offers firm footing beneath a massive anchor point.",
                        "The site is defensible by geometry, not automatically safe from wind, altitude pressure, falling debris, or creatures.",
                        "Is this anchor worth using as a temporary bivouac?",
                        "Test footing and overhead debris before stopping.", "Use the anchor for a short guarded rest.", "Continue toward lower or less exposed ground."),
                module("chain_high_watch", "chainfall_reach", CampFamily.CAMP_ABANDONMENT,
                        DreamRealmRegionContentCatalog.Hazard.CRUSHING_PRESSURE, DreamRealmRegionContentCatalog.Opportunity.OBSERVATION,
                        "The view improves as the route climbs, but the pressure in the air grows harder to ignore.",
                        "A high shelf sees several crossings at once while leaving little room to descend quickly.",
                        "A useful observation point can become a bad camp when altitude itself is part of the regional danger.",
                        "When should the party give up the view and descend?",
                        "Take only the observations needed for the next leg.", "Set an explicit leave condition before resting.", "Descend immediately if pressure signs intensify."),

                module("glass_dull_shelter", "glassmere_flats", CampFamily.SHELTER_EVALUATION,
                        DreamRealmRegionContentCatalog.Hazard.RESONANCE_STORMS, DreamRealmRegionContentCatalog.Opportunity.OBSERVATION,
                        "A low ridge of dull glass breaks the reflections without enclosing the traveler.",
                        "Clouded shards make one patch of the flats less visually exposed than the surrounding mirror-bright plain.",
                        "Reduced reflection is useful local evidence, not a guarantee that resonance or unstable ground cannot reach the site.",
                        "Is the dull patch useful enough for a pause?",
                        "Listen and inspect the surface before settling.", "Use the ridge only while local conditions remain dull.", "Move on if ringing or reflection begins to build."),
                module("glass_route_notes", "glassmere_flats", CampFamily.INFORMATION_EXCHANGE,
                        DreamRealmRegionContentCatalog.Hazard.UNSTABLE_GROUND, DreamRealmRegionContentCatalog.Opportunity.TRADE_ROUTE,
                        "Old scuffs and recently disturbed dust suggest other travelers have used this stretch.",
                        "A crossing point carries marks from more than one route without proving which marks are current.",
                        "Travel information can improve decisions, but marks, stories, or exchanged notes remain evidence to verify rather than automatic truth.",
                        "What information should be trusted enough to shape the next camp or crossing?",
                        "Compare route reports against the visible surface.", "Record disagreements instead of forcing one answer.", "Keep uncertain claims provisional and choose a reversible next step."),

                module("blackwater_dry_step", "blackwater_steps", CampFamily.TEMPORARY_REFUGE,
                        DreamRealmRegionContentCatalog.Hazard.FLOOD_SURGE, DreamRealmRegionContentCatalog.Opportunity.SHELTER,
                        "A dry terrace rises one level above the current waterline.",
                        "Fog curls around an empty stone recess while the lower steps disappear into black water.",
                        "Elevation and cover make a useful refuge candidate, but current water height does not establish future flood timing or safety.",
                        "Pause on the terrace, prepare to move, or reject it as too uncertain?",
                        "Mark the current waterline before resting.", "Use the recess with an immediate exit route kept clear.", "Continue if water movement cannot be read."),
                module("blackwater_ferry_word", "blackwater_steps", CampFamily.INFORMATION_EXCHANGE,
                        DreamRealmRegionContentCatalog.Hazard.CONCEALING_MIST, DreamRealmRegionContentCatalog.Opportunity.TRADE_ROUTE,
                        "Rope wear and mooring scars show that this crossing has supported repeated traffic.",
                        "A sheltered landing contains old route marks partly hidden by mist and water stains.",
                        "Route knowledge exchanged at a crossing is useful only after it is separated from stale marks, rumor, and present conditions.",
                        "What should be carried forward from this stop?",
                        "Compare route claims with the current water and fog.", "Keep only observations that can be independently checked.", "Record uncertain information without treating it as a safe-route guarantee."),

                module("thorn_stone_camp", "thornwake_basin", CampFamily.SHELTER_EVALUATION,
                        DreamRealmRegionContentCatalog.Hazard.HOSTILE_FLORA, DreamRealmRegionContentCatalog.Opportunity.DEFENSIBLE_CAMP,
                        "Old stone interrupts the briars and leaves a small patch with fewer living stems.",
                        "A ring of masonry gives clear footing while thorn growth presses close beyond it.",
                        "Stone can simplify the immediate perimeter without creating a magical safe zone or proving that the vegetation will stay still.",
                        "Hold the stone patch, trim a way out, or leave before growth closes around it?",
                        "Inspect every exit before settling.", "Use the clear ground while keeping a route open.", "Abandon the site if new growth changes the perimeter."),
                module("thorn_food_pause", "thornwake_basin", CampFamily.WATCH_AND_REST,
                        DreamRealmRegionContentCatalog.Hazard.CONCEALING_MIST, DreamRealmRegionContentCatalog.Opportunity.FOOD,
                        "Bitter fruit hangs near a patch of comparatively open stone while mist gathers between trunks.",
                        "A potential food source sits close to a place where visibility narrows quickly.",
                        "A resource opportunity can justify a pause, but this catalogue does not establish edibility, nutrition, healing, or safe-rest effects.",
                        "Gather information and rest, or keep the pause too short for the mist to become a larger problem?",
                        "Verify the site before handling unfamiliar food.", "Keep one observer focused beyond the gathering area.", "Leave the resource untouched if visibility collapses."),

                module("mist_cairn_lee", "mistwound_pass", CampFamily.TEMPORARY_REFUGE,
                        DreamRealmRegionContentCatalog.Hazard.OPEN_EXPOSURE, DreamRealmRegionContentCatalog.Opportunity.SHELTER,
                        "A cairn and rock shoulder create a narrow pocket out of the strongest wind.",
                        "The mist repeatedly opens on the same stone marker, making the spot easier to relocate than nearby ground.",
                        "A repeatable landmark and windbreak make a practical refuge, not proof against rockfall, creatures, or changing weather.",
                        "Use the landmark as a short refuge or move while visibility is available?",
                        "Confirm the landmark from more than one angle.", "Rest only while the exit route remains visible.", "Move during the next clear interval if debris risk rises."),
                module("mist_false_voice_watch", "mistwound_pass", CampFamily.WATCH_AND_REST,
                        DreamRealmRegionContentCatalog.Hazard.CONCEALING_MIST, DreamRealmRegionContentCatalog.Opportunity.OBSERVATION,
                        "A voice seems to carry from beyond the next ridge without a visible speaker.",
                        "Mist closes around the camp edge while sound arrives from inconsistent directions.",
                        "Sound is a verification problem here; a familiar voice is not automatic proof of identity, direction, or safety.",
                        "How should the camp respond without turning every sound into a chase?",
                        "Verify direction against known landmarks before moving.", "Use paired observation rather than sending one person alone.", "Stay put or abandon camp if the source cannot be established safely."),

                module("bone_hollow_pause", "bonewhite_march", CampFamily.TEMPORARY_REFUGE,
                        DreamRealmRegionContentCatalog.Hazard.OPEN_EXPOSURE, DreamRealmRegionContentCatalog.Opportunity.SALVAGE,
                        "A hollow structure breaks the white horizon and offers shade from the exposed plain.",
                        "Old material lies inside a curved shelter whose entrances remain visible from a distance.",
                        "Cover and salvage make the hollow useful, but its history, structural safety, contents, and future occupants remain unresolved.",
                        "Use the hollow, inspect it first, or cross the plain before conditions change?",
                        "Check the floor and overhead structure before entering.", "Take only a short pause with both exits monitored.", "Leave if the shelter cannot be verified without committing deeply."),
                module("bone_long_watch", "bonewhite_march", CampFamily.CAMP_ABANDONMENT,
                        DreamRealmRegionContentCatalog.Hazard.FALLING_DEBRIS, DreamRealmRegionContentCatalog.Opportunity.OBSERVATION,
                        "A raised bone ridge sees far across the plain but offers little overhead protection.",
                        "The horizon is excellent for observation while scattered fragments shift above the route.",
                        "A good watch point is not necessarily a good rest point; exposure and falling debris remain independent pressures.",
                        "When should observation end and movement resume?",
                        "Take the route observations before anyone settles.", "Set a short watch window and keep packs ready.", "Abandon the ridge when overhead movement becomes uncertain."),

                module("hollow_gatehouse_refuge", "hollow_causeway", CampFamily.SHELTER_EVALUATION,
                        DreamRealmRegionContentCatalog.Hazard.DEEP_DARKNESS, DreamRealmRegionContentCatalog.Opportunity.SHELTER,
                        "An empty gatehouse interrupts the repeating road with walls that can be counted and marked.",
                        "A side chamber offers cover while the main passage remains visible from one position.",
                        "The structure can simplify orientation and provide cover, but darkness does not make it secure or inhabited history knowable.",
                        "Use the gatehouse as a marked refuge or keep moving before the route becomes harder to distinguish?",
                        "Mark the exact entry and exit before resting.", "Keep light and orientation references independent of memory alone.", "Leave if new sounds or changes make the structure ambiguous."),
                module("hollow_junction_record", "hollow_causeway", CampFamily.INFORMATION_EXCHANGE,
                        DreamRealmRegionContentCatalog.Hazard.UNSTABLE_GROUND, DreamRealmRegionContentCatalog.Opportunity.SHORTCUT,
                        "Several nearly identical junctions converge near a dry ledge.",
                        "Old scratches compete with newer marks on walls that repeat the same shapes farther down the road.",
                        "Shared route records are useful only when tied to verifiable junction features; repeating architecture can make confident memory wrong.",
                        "What should the group record before resting or splitting routes?",
                        "Tie each note to two independent physical markers.", "Record uncertain shortcuts separately from confirmed paths.", "Reject any route claim that cannot be relocated from the current junction."),

                module("storm_high_camp", "storm_lantern_coast", CampFamily.SHELTER_EVALUATION,
                        DreamRealmRegionContentCatalog.Hazard.RESONANCE_STORMS, DreamRealmRegionContentCatalog.Opportunity.DEFENSIBLE_CAMP,
                        "A stone shoulder above the surge line has a narrow back wall and one clear approach.",
                        "Broken lantern masonry offers cover from one direction while bells sound somewhere below.",
                        "Defensible terrain reduces some approach angles; it does not guarantee safety from storms, resonance, debris, or changing water.",
                        "Hold the high ground briefly or keep moving between storm windows?",
                        "Inspect the back wall and overhead stone first.", "Use the site only with a clear descent and evacuation route.", "Leave if resonance or debris signs strengthen."),
                module("storm_route_exchange", "storm_lantern_coast", CampFamily.INFORMATION_EXCHANGE,
                        DreamRealmRegionContentCatalog.Hazard.FLOOD_SURGE, DreamRealmRegionContentCatalog.Opportunity.TRADE_ROUTE,
                        "Mooring wear and fresh rope fibers show that travelers have recently used more than one coastal route.",
                        "Marks on the cliff record high-water lines that do not all agree.",
                        "Travel reports and water marks can inform a choice, but this catalogue does not create a tide table, storm forecast, or guaranteed safe passage.",
                        "Which observations are reliable enough to plan the next leg?",
                        "Compare fresh marks with the present surge.", "Keep conflicting reports instead of averaging them into false certainty.", "Choose a route with a reversible retreat rather than assuming timing."),

                module("canopy_root_refuge", "red_canopy", CampFamily.TEMPORARY_REFUGE,
                        DreamRealmRegionContentCatalog.Hazard.FLOOD_SURGE, DreamRealmRegionContentCatalog.Opportunity.WATER,
                        "A giant root rises above the flooded forest floor and leaves a dry saddle between buttresses.",
                        "Rainwater runs around the root instead of directly across its highest surface.",
                        "Elevation can create temporary refuge, but it does not establish future flood level, clean drinking water, or protection from hostile flora and creatures.",
                        "Use the high root briefly, climb farther, or move before water rises?",
                        "Check water movement and vegetation before settling.", "Use the saddle with both vertical and ground exits identified.", "Leave early if water or plant movement changes."),
                module("canopy_food_watch", "red_canopy", CampFamily.WATCH_AND_REST,
                        DreamRealmRegionContentCatalog.Hazard.HOSTILE_FLORA, DreamRealmRegionContentCatalog.Opportunity.FOOD,
                        "A fruiting branch hangs over a comparatively open section of canopy.",
                        "The perch offers access to a potential food source while exposed leaves make distant movement easier to notice.",
                        "Potential food and improved sightlines justify evaluation, not assumptions about edibility, safety, healing, or how long the vegetation stays passive.",
                        "Gather, rest, or keep moving before the canopy changes around the perch?",
                        "Verify the plant and nearby routes before handling fruit.", "Keep a dedicated observer while anyone rests or gathers.", "Abandon the perch if plant movement narrows the exits.")
        );
        validate(modules);
        return modules;
    }

    public static ResolvedCampModule compose(long seed, String regionId) {
        String checkedRegion = stableId(regionId);
        List<CampModule> compatible = new ArrayList<>();
        for (CampModule module : waveOne()) {
            if (module.regionId().equals(checkedRegion)) {
                compatible.add(module);
            }
        }
        if (compatible.isEmpty()) {
            throw new IllegalArgumentException("Unknown Dream Realm region id: " + checkedRegion);
        }
        int moduleIndex = Math.floorMod(mix(seed, checkedRegion.hashCode()), compatible.size());
        CampModule module = compatible.get(moduleIndex);
        int cueIndex = Math.floorMod(mix(seed ^ 0x6A09E667F3BCC909L, module.id().hashCode()), module.approachCues().size());
        return new ResolvedCampModule(GENERATOR_VERSION, seed, checkedRegion, module, module.approachCues().get(cueIndex));
    }

    public static CampModule require(String moduleId) {
        String checked = stableId(moduleId);
        return waveOne().stream().filter(module -> module.id().equals(checked)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown shelter/camp module id: " + checked));
    }

    private static CampModule module(String id, String regionId, CampFamily family,
                                     DreamRealmRegionContentCatalog.Hazard pressure,
                                     DreamRealmRegionContentCatalog.Opportunity opportunity,
                                     String cueA, String cueB, String situation, String prompt,
                                     String choiceA, String choiceB, String choiceC) {
        return new CampModule(id, regionId, family, pressure, opportunity, List.of(cueA, cueB), situation, prompt,
                List.of(choiceA, choiceB, choiceC), boundaryFor(id));
    }

    private static String boundaryFor(String id) {
        return switch (id) {
            case "thorn_food_pause", "canopy_food_watch" -> "Food opportunity does not establish edibility, nutrition, healing, buffs, toxicity, quantity, respawn, or ownership.";
            case "blackwater_dry_step", "canopy_root_refuge", "storm_route_exchange" -> "Present water conditions do not establish a canonical flood/tide forecast, future safe duration, water quality, or guaranteed route safety.";
            case "mist_false_voice_watch" -> "Sound does not establish identity, truth, direction, prophecy, objective revelation, or mandatory pursuit.";
            case "glass_dull_shelter" -> "Local visual or resonance conditions do not establish a universal forecast, magical safe zone, or immunity from regional hazards.";
            case "glass_route_notes", "blackwater_ferry_word", "hollow_junction_record" -> "Exchanged or recorded route information remains evidence to verify; it does not reveal hidden objectives or guarantee current safety.";
            default -> "A refuge, camp, watch point, or defensible position is not a guaranteed safe zone and does not define recovery values, encounter odds, respawn behavior, or rewards.";
        };
    }

    private static void validate(List<CampModule> modules) {
        Set<String> ids = new HashSet<>();
        for (CampModule module : modules) {
            if (!ids.add(module.id())) {
                throw new IllegalArgumentException("Duplicate shelter/camp module id: " + module.id());
            }
        }
    }

    private static int mix(long seed, int salt) {
        long value = seed ^ (long) salt * 0x9E3779B97F4A7C15L;
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdl;
        value ^= value >>> 33;
        value *= 0xc4ceb9fe1a85ec53l;
        value ^= value >>> 33;
        return (int) (value ^ (value >>> 32));
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

    private static List<String> textList(List<String> source, String name, int minimum) {
        List<String> result = Objects.requireNonNull(source, name).stream().map(value -> text(value, name)).toList();
        if (result.size() < minimum) {
            throw new IllegalArgumentException(name + " must contain at least " + minimum + " entries");
        }
        return List.copyOf(result);
    }
}
