package dev.spud.shadowslave.dreamrealm.content;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Authored DESIGN field-report content for already-resolved Dream Realm regions.
 *
 * <p>This catalogue never discovers a region, reveals a map, confirms a creature spawn, or mutates world/progression
 * state. It composes player-facing notes from Java-owned region primitives that have already been selected elsewhere.</p>
 */
public final class DreamRealmFieldReportCatalog {
    public static final String GENERATOR_VERSION = "dream-realm-field-report-v1";

    private DreamRealmFieldReportCatalog() {}

    public enum KnowledgeState {
        OBSERVED,
        VERIFIED,
        PROVISIONAL
    }

    public enum SubjectKind {
        REGION_HAZARD,
        LANDMARK,
        CREATURE_SIGN
    }

    public record KnowledgeAnchor(
            String id,
            String regionId,
            KnowledgeState knowledgeState,
            SubjectKind subjectKind,
            String subjectId,
            String headline,
            String observation,
            String basis,
            String limitation
    ) {
        public KnowledgeAnchor {
            id = stableId(id, "id");
            regionId = stableId(regionId, "regionId");
            knowledgeState = Objects.requireNonNull(knowledgeState, "knowledgeState");
            subjectKind = Objects.requireNonNull(subjectKind, "subjectKind");
            subjectId = stableId(subjectId, "subjectId");
            headline = text(headline, "headline");
            observation = text(observation, "observation");
            basis = text(basis, "basis");
            limitation = text(limitation, "limitation");
        }
    }

    public record ReportFraming(
            String id,
            KnowledgeState knowledgeState,
            String label,
            String opening,
            String nextAction
    ) {
        public ReportFraming {
            id = stableId(id, "id");
            knowledgeState = Objects.requireNonNull(knowledgeState, "knowledgeState");
            label = text(label, "label");
            opening = text(opening, "opening");
            nextAction = text(nextAction, "nextAction");
        }
    }

    public record ComposedFieldReport(
            String generatorVersion,
            long seed,
            String regionId,
            String regionName,
            KnowledgeState knowledgeState,
            String anchorId,
            String framingId,
            SubjectKind subjectKind,
            String subjectId,
            String title,
            String reportLine,
            String basis,
            String nextAction,
            String limitation
    ) {}

    public static List<KnowledgeAnchor> anchors() {
        List<KnowledgeAnchor> anchors = List.of(
                observed("ashen_expanse", "open_exposure", "Open Flats Carry Far",
                        "Fresh ash movement and uninterrupted sightlines show that this crossing is exposed right now.",
                        "direct local observation of the authored open-exposure hazard",
                        "This says nothing about future visibility, travel time, safety, or what may occupy the flats."),
                verified("ashen_expanse", "buried_watchtower", "Buried Watchtower Fixed",
                        "The half-buried watchtower can be matched from two separate sightlines and used as a local orientation reference.",
                        "two independent physical sightings of the same authored landmark",
                        "Verification does not reveal unexplored terrain, hidden routes, history, loot, or a globally accurate map."),
                provisional("ashen_expanse", "ash_burrower", "Disturbance Under the Ash",
                        "A moving seam in otherwise settled ash is consistent with the local Ash Burrower profile, but the source has not been confirmed.",
                        "one indirect sign compatible with an authored regional creature affinity",
                        "The note does not confirm a creature is present, identify its Rank/Class, or predict an attack."),

                observed("chainfall_reach", "falling_debris", "Fresh Debris on the Chain",
                        "New chips and dust crossing the route show that debris is falling through this span at present.",
                        "direct observation of the authored falling-debris hazard",
                        "The observation does not establish a collapse timer, safe interval, damage value, or future route state."),
                verified("chainfall_reach", "wind_bridge", "Wind Bridge Cross-Checked",
                        "The wind bridge has been identified from both its chain root and the opposite ledge, making it a reliable local landmark.",
                        "the same authored landmark confirmed from two physical reference points",
                        "A verified landmark is not a guarantee that the bridge is safe, intact, or currently traversable."),
                provisional("chainfall_reach", "chainback", "Snag Marks on Iron",
                        "Deep fresh scrapes around a narrow chain route are compatible with Chainback movement, but no creature has been seen.",
                        "indirect physical evidence compatible with an authored regional creature affinity",
                        "The report does not infer population, territory, Rank/Class, encounter probability, or current aggro."),

                observed("glassmere_flats", "resonance_storms", "The Plain Is Resonating",
                        "A rising vibration can be felt through nearby glass and stone, establishing local resonance pressure now.",
                        "direct sensory observation of the authored resonance-storm hazard",
                        "The note is not a storm forecast and supplies no canonical threshold, duration, or damage equation."),
                verified("glassmere_flats", "mirror_ridge", "Mirror Ridge Aligned",
                        "Mirror Ridge remains in the same relation to the red hill when checked from separate points on the plain.",
                        "repeat positional comparison between authored landmarks",
                        "This local cross-check does not make reflections truthful, reveal hidden paths, or produce perfect navigation."),
                provisional("glassmere_flats", "bell_eater", "Broken Resonance Pattern",
                        "A localized interruption in otherwise broad resonance is compatible with Bell-Eater pressure, but the cause remains uncertain.",
                        "an anomalous sign compatible with an authored regional creature affinity",
                        "The anomaly is not proof of Bell-Eater presence, a supernatural detection system, or an encounter prediction."),

                observed("blackwater_steps", "flood_surge", "Waterline Rising",
                        "The waterline has climbed against the same terrace marker since the last check, so local flood pressure is increasing.",
                        "direct repeated observation of the authored flood-surge hazard",
                        "This does not establish a tide cycle, exact rate, safe crossing window, or future water level."),
                verified("blackwater_steps", "rope_harbour", "Rope Harbour Confirmed",
                        "The rope harbour's posts and terrace cut have been matched from water and high ground, fixing it as a local reference.",
                        "independent confirmation of the same authored landmark",
                        "The harbour is not thereby safe, occupied, stocked, functional, or guaranteed to remain reachable."),
                provisional("blackwater_steps", "drowned_listener", "Sound Dies Near the Water",
                        "An unusual deadening of small sounds near one flooded route is compatible with Drowned Listener pressure, but unconfirmed.",
                        "one local acoustic sign compatible with an authored regional creature affinity",
                        "The report does not confirm a creature, determine detection range, or reveal a canonical sound mechanic."),

                observed("thornwake_basin", "hostile_flora", "Briars Closing the Gap",
                        "Fresh growth is narrowing a previously open gap, confirming active vegetation pressure at this location.",
                        "direct comparison against the authored hostile-flora hazard",
                        "The observation does not define growth speed, intelligence, damage, or whether every plant is hostile."),
                verified("thornwake_basin", "stone_ring", "Stone Ring Re-Located",
                        "The stone ring has been reached by two different approaches and matches the same old masonry under the briars.",
                        "repeat physical confirmation of the authored landmark",
                        "Verification does not make the ring magical, safe, historically understood, or a guaranteed camp site."),
                provisional("thornwake_basin", "thorn_matron", "Directed Growth Suspected",
                        "Several thorn lanes bend toward the same blocked passage in a pattern compatible with Thorn Matron pressure, but no source is visible.",
                        "pattern evidence compatible with an authored regional creature affinity",
                        "The pattern does not confirm a Thorn Matron, prove command over all vegetation, or predict spawn location."),

                observed("mistwound_pass", "concealing_mist", "Visibility Collapsing",
                        "Nearby fixed rock features are disappearing behind thickening mist, establishing reduced visibility along this section now.",
                        "direct local observation of the authored concealing-mist hazard",
                        "The report does not forecast duration, reveal hidden entities, or make distant silhouettes trustworthy."),
                verified("mistwound_pass", "weather_cairn", "Weather Cairn Cross-Checked",
                        "The weather cairn's stacked profile and nearby split in the ridge have both been matched from separate approaches.",
                        "two physical references confirming the authored landmark",
                        "The cairn is an orientation point, not a canonical weather oracle, safe-zone marker, or automatic map reveal."),
                provisional("mistwound_pass", "hollow_mimic", "Voice Without a Source",
                        "A familiar-sounding call lacks a matching visible speaker and is compatible with Hollow Mimic pressure, but remains unverified.",
                        "an ambiguous sensory sign compatible with an authored regional creature affinity",
                        "The report does not identify the speaker, establish deception as certain, or grant lie detection."),

                observed("bonewhite_march", "open_exposure", "No Cover Ahead",
                        "The next stretch shows no reliable break in the white plain, confirming immediate exposure on the visible route.",
                        "direct local observation of the authored open-exposure hazard",
                        "The visible route is not the entire region, and the report gives no travel-time, weather, or safety guarantee."),
                verified("bonewhite_march", "rib_arch", "Rib Arch Matched",
                        "The rib arch can be identified by the same fracture pattern from both the plain and a hollow approach.",
                        "repeat physical confirmation of the authored landmark",
                        "This does not establish what created the structure, whether it is organic, or whether shelter beneath it is safe."),
                provisional("bonewhite_march", "stone_maw", "Circular Cracking Noted",
                        "A ring of fresh fractures resembles the warning pattern associated with the local Stone Maw profile, but no creature is confirmed.",
                        "terrain evidence compatible with an authored regional creature affinity",
                        "The note does not prove a Stone Maw is below, reveal exact trigger range, or predict when the ground will fail."),

                observed("hollow_causeway", "deep_darkness", "Light Fails Beyond the Bend",
                        "Available light drops sharply beyond the next turn, confirming an immediate deep-darkness section of the route.",
                        "direct local observation of the authored deep-darkness hazard",
                        "This does not reveal what lies beyond the bend, make darkness supernatural by default, or guarantee a route outcome."),
                verified("hollow_causeway", "buried_milestone", "Milestone Verified Twice",
                        "The buried milestone has been matched against two separately marked junctions, making it a reliable local reference.",
                        "repeat route verification of the authored landmark",
                        "The milestone does not expose a complete old road network, hidden destination, history, or safe shortcut."),
                provisional("hollow_causeway", "hollow_mimic", "Repeated Footfall Pattern",
                        "Footsteps repeat after movement stops in a way compatible with Hollow Mimic pressure, but architecture may also be responsible.",
                        "ambiguous acoustic evidence compatible with an authored regional creature affinity",
                        "The report preserves the alternative explanation and does not confirm a creature, trap, or supernatural echo rule."),

                observed("storm_lantern_coast", "flood_surge", "Lower Path Taking Water",
                        "Water has crossed a previously dry mark on the lower path, establishing present flood pressure near the coast.",
                        "direct comparison against the authored flood-surge hazard",
                        "The note does not provide a tide table, storm schedule, safe interval, or universal coastal rule."),
                verified("storm_lantern_coast", "storm_belfry", "Storm Belfry Fixed",
                        "The storm belfry has been matched against the cliff lantern line from two elevations, fixing its local position.",
                        "independent positional checks of the authored landmark",
                        "The belfry's bells do not thereby forecast storms, reveal danger, or provide canonical supernatural guidance."),
                provisional("storm_lantern_coast", "bell_eater", "Missing Ring in the Belfry Line",
                        "One expected ring drops out while nearby bells remain audible, a pattern compatible with Bell-Eater pressure but not proof.",
                        "one acoustic anomaly compatible with an authored regional creature affinity",
                        "The report does not confirm a Bell-Eater, determine its range, or turn bell behavior into an encounter detector."),

                observed("red_canopy", "hostile_flora", "Vines Reaching Into the Route",
                        "Fresh vines have entered a recently open climbing line, confirming active hostile-flora pressure at this point.",
                        "direct local comparison against the authored hostile-flora hazard",
                        "The observation does not define growth speed, plant intelligence, toxicity, or future route closure."),
                verified("red_canopy", "giant_root", "Giant Root Re-Identified",
                        "The same giant root has been matched from both the flooded floor and an elevated crossing by its split crown and red scar.",
                        "two physical perspectives confirming the authored landmark",
                        "The root is not automatically safe, inhabited, resource-rich, historically significant, or a map-reveal device."),
                provisional("red_canopy", "gutter_choir", "Layered Voices in the Rain",
                        "Several overlapping voices persist without visible speakers, compatible with Gutter Choir pressure but still unconfirmed.",
                        "ambiguous acoustic evidence compatible with an authored regional creature affinity",
                        "The report does not prove a Gutter Choir is present, identify how many entities exist, or make the voices truthful or false by rule.")
        );
        validateAnchors(anchors);
        return anchors;
    }

    public static List<ReportFraming> framings() {
        List<ReportFraming> framings = List.of(
                framing("observed_field_note", KnowledgeState.OBSERVED, "Field Note",
                        "Observed on site:", "Treat this as a current local condition and re-check before committing to it later."),
                framing("observed_immediate_read", KnowledgeState.OBSERVED, "Immediate Read",
                        "Current read:", "Use the observation for the next decision only; do not promote it into a regional rule."),
                framing("observed_travel_note", KnowledgeState.OBSERVED, "Travel Note",
                        "On the present route:", "Mark where the condition was seen and compare it again if the route is revisited."),
                framing("verified_cross_check", KnowledgeState.VERIFIED, "Cross-Check",
                        "Cross-checked locally:", "Keep the reference, but verify again if the environment or route materially changes."),
                framing("verified_confirmed_marker", KnowledgeState.VERIFIED, "Confirmed Marker",
                        "Confirmed from independent local evidence:", "Use this as a bounded reference point, not as perfect knowledge of the surrounding region."),
                framing("verified_repeat_observation", KnowledgeState.VERIFIED, "Repeated Observation",
                        "Repeated checks agree:", "Record what was actually confirmed and leave untested implications unresolved."),
                framing("provisional_working_hypothesis", KnowledgeState.PROVISIONAL, "Working Hypothesis",
                        "One plausible explanation is:", "Seek a second independent sign before treating the interpretation as verified."),
                framing("provisional_unconfirmed_sign", KnowledgeState.PROVISIONAL, "Unconfirmed Sign",
                        "A sign may indicate:", "Preserve alternative explanations and avoid acting as though the suspected source is definitely present."),
                framing("provisional_cautionary_note", KnowledgeState.PROVISIONAL, "Cautionary Note",
                        "The evidence is suggestive, not decisive:", "Use the possibility to guide caution while keeping the underlying claim explicitly provisional.")
        );
        validateFramings(framings);
        return framings;
    }

    public static ComposedFieldReport compose(long seed, String regionId, KnowledgeState state) {
        String stableRegionId = stableId(regionId, "regionId");
        KnowledgeState checkedState = Objects.requireNonNull(state, "state");
        DreamRealmRegionContentCatalog.RegionProfile region = regionById(stableRegionId);

        KnowledgeAnchor anchor = anchors().stream()
                .filter(candidate -> candidate.regionId().equals(stableRegionId))
                .filter(candidate -> candidate.knowledgeState() == checkedState)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No field-report anchor for " + stableRegionId + "/" + checkedState));

        List<ReportFraming> compatible = framings().stream()
                .filter(framing -> framing.knowledgeState() == checkedState)
                .toList();
        int framingIndex = Math.floorMod(mix(seed, stableRegionId, checkedState.name()), compatible.size());
        ReportFraming framing = compatible.get(framingIndex);

        return new ComposedFieldReport(
                GENERATOR_VERSION,
                seed,
                stableRegionId,
                region.displayName(),
                checkedState,
                anchor.id(),
                framing.id(),
                anchor.subjectKind(),
                anchor.subjectId(),
                framing.label() + ": " + anchor.headline(),
                framing.opening() + " " + anchor.observation(),
                anchor.basis(),
                framing.nextAction(),
                anchor.limitation()
        );
    }

    public static KnowledgeAnchor anchor(String anchorId) {
        String stableAnchorId = stableId(anchorId, "anchorId");
        return anchors().stream()
                .filter(anchor -> anchor.id().equals(stableAnchorId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown field-report anchor: " + stableAnchorId));
    }

    private static KnowledgeAnchor observed(String regionId, String hazardId, String headline,
                                            String observation, String basis, String limitation) {
        return anchor(regionId + "_observed", regionId, KnowledgeState.OBSERVED, SubjectKind.REGION_HAZARD,
                hazardId, headline, observation, basis, limitation);
    }

    private static KnowledgeAnchor verified(String regionId, String landmarkId, String headline,
                                            String observation, String basis, String limitation) {
        return anchor(regionId + "_verified", regionId, KnowledgeState.VERIFIED, SubjectKind.LANDMARK,
                landmarkId, headline, observation, basis, limitation);
    }

    private static KnowledgeAnchor provisional(String regionId, String creatureId, String headline,
                                               String observation, String basis, String limitation) {
        return anchor(regionId + "_provisional", regionId, KnowledgeState.PROVISIONAL, SubjectKind.CREATURE_SIGN,
                creatureId, headline, observation, basis, limitation);
    }

    private static KnowledgeAnchor anchor(String id, String regionId, KnowledgeState state, SubjectKind kind,
                                          String subjectId, String headline, String observation, String basis,
                                          String limitation) {
        return new KnowledgeAnchor(id, regionId, state, kind, subjectId, headline, observation, basis, limitation);
    }

    private static ReportFraming framing(String id, KnowledgeState state, String label, String opening,
                                         String nextAction) {
        return new ReportFraming(id, state, label, opening, nextAction);
    }

    private static DreamRealmRegionContentCatalog.RegionProfile regionById(String regionId) {
        return DreamRealmRegionContentCatalog.waveOne().stream()
                .filter(region -> region.id().equals(regionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown Dream Realm region: " + regionId));
    }

    private static void validateAnchors(List<KnowledgeAnchor> anchors) {
        HashSet<String> ids = new HashSet<>();
        Map<String, EnumMap<KnowledgeState, Integer>> stateCounts = new HashMap<>();
        Set<String> regionIds = new HashSet<>();
        DreamRealmRegionContentCatalog.waveOne().forEach(region -> regionIds.add(region.id()));

        for (KnowledgeAnchor anchor : anchors) {
            if (!ids.add(anchor.id())) {
                throw new IllegalArgumentException("Duplicate field-report anchor id: " + anchor.id());
            }
            if (!regionIds.contains(anchor.regionId())) {
                throw new IllegalArgumentException("Unknown field-report region: " + anchor.regionId());
            }
            DreamRealmRegionContentCatalog.RegionProfile region = regionById(anchor.regionId());
            switch (anchor.subjectKind()) {
                case REGION_HAZARD -> {
                    DreamRealmRegionContentCatalog.Hazard hazard;
                    try {
                        hazard = DreamRealmRegionContentCatalog.Hazard.valueOf(anchor.subjectId().toUpperCase(Locale.ROOT));
                    } catch (IllegalArgumentException exception) {
                        throw new IllegalArgumentException("Unknown field-report hazard: " + anchor.subjectId(), exception);
                    }
                    if (!region.hazards().contains(hazard)) {
                        throw new IllegalArgumentException(anchor.id() + " uses hazard outside source region: " + anchor.subjectId());
                    }
                }
                case LANDMARK -> {
                    if (!region.landmarkHooks().contains(anchor.subjectId())) {
                        throw new IllegalArgumentException(anchor.id() + " uses landmark outside source region: " + anchor.subjectId());
                    }
                }
                case CREATURE_SIGN -> {
                    if (!region.creatureAffinityIds().contains(anchor.subjectId())) {
                        throw new IllegalArgumentException(anchor.id() + " uses creature outside source region affinity: " + anchor.subjectId());
                    }
                }
            }
            stateCounts.computeIfAbsent(anchor.regionId(), ignored -> new EnumMap<>(KnowledgeState.class))
                    .merge(anchor.knowledgeState(), 1, Integer::sum);
        }

        if (anchors.size() != regionIds.size() * KnowledgeState.values().length) {
            throw new IllegalArgumentException("Expected exactly one field-report anchor per region/knowledge-state pair");
        }
        for (String regionId : regionIds) {
            EnumMap<KnowledgeState, Integer> counts = stateCounts.get(regionId);
            for (KnowledgeState state : KnowledgeState.values()) {
                if (counts == null || counts.getOrDefault(state, 0) != 1) {
                    throw new IllegalArgumentException("Expected exactly one " + state + " field-report anchor for " + regionId);
                }
            }
        }
    }

    private static void validateFramings(List<ReportFraming> framings) {
        HashSet<String> ids = new HashSet<>();
        EnumMap<KnowledgeState, Integer> counts = new EnumMap<>(KnowledgeState.class);
        for (ReportFraming framing : framings) {
            if (!ids.add(framing.id())) {
                throw new IllegalArgumentException("Duplicate field-report framing id: " + framing.id());
            }
            counts.merge(framing.knowledgeState(), 1, Integer::sum);
        }
        for (KnowledgeState state : KnowledgeState.values()) {
            if (counts.getOrDefault(state, 0) < 3) {
                throw new IllegalArgumentException("Expected at least three field-report framings for " + state);
            }
        }
    }

    private static int mix(long seed, String regionId, String stateName) {
        long value = seed;
        value ^= (long) regionId.hashCode() * 0x9E3779B97F4A7C15L;
        value = Long.rotateLeft(value, 21);
        value ^= (long) stateName.hashCode() * 0xC2B2AE3D27D4EB4FL;
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdL;
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
}
