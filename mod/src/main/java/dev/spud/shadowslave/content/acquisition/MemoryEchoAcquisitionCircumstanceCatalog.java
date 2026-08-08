package dev.spud.shadowslave.content.acquisition;

import dev.spud.shadowslave.content.acquisition.MemoryEchoAcquisitionContextCatalog.AcquisitionSource;
import dev.spud.shadowslave.content.acquisition.MemoryEchoAcquisitionContextCatalog.SubjectKind;
import dev.spud.shadowslave.dreamrealm.content.DreamRealmRegionContentCatalog;
import dev.spud.shadowslave.nightmare.content.DrownedBellScenarioDefinition;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Authored DESIGN circumstances for an already-resolved Memory/Echo acquisition source.
 *
 * <p>This layer answers only "what local circumstance frames the known acquisition?". The caller
 * supplies authoritative subject kind and {@link AcquisitionSource}; this catalogue cannot choose,
 * upgrade, replace, or infer either one. Region and Nightmare-resolution anchors reference existing
 * Java-owned content IDs so adapters do not become content authority.</p>
 */
public record MemoryEchoAcquisitionCircumstanceCatalog(List<AcquisitionCircumstance> circumstances) {
    public static final int GENERATOR_VERSION = 1;
    public static final String REGION_CATALOG_ID = "dream_realm_region_catalog";

    public MemoryEchoAcquisitionCircumstanceCatalog {
        ArrayList<AcquisitionCircumstance> canonical = new ArrayList<>(
                Objects.requireNonNull(circumstances, "circumstances"));
        canonical.sort(Comparator.comparing(AcquisitionCircumstance::id));
        HashSet<String> seen = new HashSet<>();
        for (AcquisitionCircumstance circumstance : canonical) {
            Objects.requireNonNull(circumstance, "circumstance");
            if (!seen.add(circumstance.id())) {
                throw new IllegalArgumentException("Duplicate acquisition circumstance id: " + circumstance.id());
            }
        }
        circumstances = List.copyOf(canonical);
        validateAnchors(circumstances);
    }

    public static MemoryEchoAcquisitionCircumstanceCatalog waveOne() {
        Set<SubjectKind> both = Set.of(SubjectKind.MEMORY, SubjectKind.ECHO);
        return new MemoryEchoAcquisitionCircumstanceCatalog(List.of(
                circumstance("ashen_buried_cache", both, Set.of(AcquisitionSource.AUTHORED_DISCOVERY),
                        regionAnchor("ashen_expanse"), Set.of("salvage", "ruins", "discovery"),
                        "Ash-Buried Find",
                        "A marked ruin pocket yields the already-resolved acquisition after careful salvage beneath the ash."),
                circumstance("chainfall_guide_exchange", both, Set.of(AcquisitionSource.TRANSFER),
                        regionAnchor("chainfall_reach"), Set.of("transfer", "guide", "route"),
                        "Guide's Exchange",
                        "The already-resolved transfer changes hands at a chain crossing after route work earns a guide's trust."),
                circumstance("glassmere_brokered_exchange", both, Set.of(AcquisitionSource.TRANSFER),
                        regionAnchor("glassmere_flats"), Set.of("transfer", "trade", "verification"),
                        "Dullglass Exchange",
                        "A brokered handoff is completed only after both parties verify the object away from the deceptive glare."),
                circumstance("blackwater_recovered_cache", both, Set.of(AcquisitionSource.AUTHORED_DISCOVERY),
                        regionAnchor("blackwater_steps"), Set.of("discovery", "water", "salvage"),
                        "Low-Water Recovery",
                        "A retreating waterline exposes a scenario-authored cache long enough for the resolved acquisition to be recovered."),
                circumstance("thornwake_hunt_aftermath", both, Set.of(AcquisitionSource.SLAIN_CREATURE),
                        regionAnchor("thornwake_basin"), Set.of("creature", "hunt", "flora"),
                        "Briar Hunt Aftermath",
                        "The known creature-derived acquisition is framed by a hunt whose tracks vanish quickly beneath hostile growth."),
                circumstance("mistwound_verified_handoff", both, Set.of(AcquisitionSource.TRANSFER),
                        regionAnchor("mistwound_pass"), Set.of("transfer", "verification", "mist"),
                        "Verified Handoff",
                        "The transfer is completed at a checked cairn after both travelers prove they followed the same verified route."),
                circumstance("bonewhite_hunt_aftermath", both, Set.of(AcquisitionSource.SLAIN_CREATURE),
                        regionAnchor("bonewhite_march"), Set.of("creature", "hunt", "tracking"),
                        "White-March Quarry",
                        "The known creature-derived acquisition follows a tracked hunt across open ground where the approach mattered as much as the clash."),
                circumstance("causeway_sealed_cache", both, Set.of(AcquisitionSource.AUTHORED_DISCOVERY),
                        regionAnchor("hollow_causeway"), Set.of("discovery", "archive", "salvage"),
                        "Sealed Road Cache",
                        "A mapped recess in the buried road reveals an authored cache after its repeating passages are correctly distinguished."),
                circumstance("storm_lantern_commission", both, Set.of(AcquisitionSource.ARTIFICIAL_CREATION),
                        regionAnchor("storm_lantern_coast"), Set.of("artificial", "commission", "craft"),
                        "Cliffside Commission",
                        "A finished artificial acquisition is delivered under a local commission; its actual construction method remains authoritative elsewhere."),
                circumstance("red_canopy_flooded_cache", both, Set.of(AcquisitionSource.AUTHORED_DISCOVERY),
                        regionAnchor("red_canopy"), Set.of("discovery", "flood", "salvage"),
                        "Flooded Root Cache",
                        "Falling water reveals a root-bound cache during an authored expedition window before the lower path disappears again."),
                circumstance("red_canopy_hunt_aftermath", both, Set.of(AcquisitionSource.SLAIN_CREATURE),
                        regionAnchor("red_canopy"), Set.of("creature", "hunt", "rain"),
                        "Canopy Hunt Aftermath",
                        "The known creature-derived acquisition follows a hunt resolved across wet vertical terrain rather than an abstract loot roll."),
                circumstance("drowned_bell_tower_handoff", both, Set.of(AcquisitionSource.AUTHORED_DISCOVERY),
                        drownedBellResolutionAnchor("tower_held"), Set.of("warning", "duty", "preservation"),
                        "After the Last Bell",
                        "Following the authored tower-held resolution, a surviving local handoff can frame an already-authorized scenario acquisition."),
                circumstance("drowned_bell_quarry_handoff", both, Set.of(AcquisitionSource.AUTHORED_DISCOVERY),
                        drownedBellResolutionAnchor("villagers_evacuated"), Set.of("guidance", "movement", "preservation"),
                        "Above the Tide",
                        "Following the authored evacuation resolution, a survivor handoff can frame an already-authorized scenario acquisition."),
                circumstance("drowned_bell_floodgate_recovery", both, Set.of(AcquisitionSource.AUTHORED_DISCOVERY),
                        drownedBellResolutionAnchor("flood_diverted"), Set.of("water", "precision", "preservation"),
                        "What the Flood Spared",
                        "Following the authored flood-diversion resolution, recovered stores can frame an already-authorized scenario acquisition."),
                circumstance("drowned_bell_quarry_recovery", both, Set.of(AcquisitionSource.AUTHORED_DISCOVERY),
                        drownedBellResolutionAnchor("creature_buried"), Set.of("sound", "precision", "retaliation"),
                        "Silence in the Rubble",
                        "Following the authored quarry-collapse resolution, a deliberate recovery can frame an already-authorized scenario acquisition." )
        ));
    }

    public AcquisitionCircumstance find(String id) {
        String stable = stableId(id);
        return circumstances.stream()
                .filter(circumstance -> circumstance.id().equals(stable))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown acquisition circumstance id: " + stable));
    }

    /**
     * Resolves descriptive circumstance only inside the caller-supplied authoritative source family.
     * Evidence magnitude is ignored; only positive authored tags participate in deterministic tie-breaking.
     */
    public ResolvedCircumstance compose(
            long seed,
            SubjectKind subjectKind,
            AcquisitionSource authoritativeSource,
            AnchorRef anchor,
            Map<String, Integer> evidence
    ) {
        Objects.requireNonNull(subjectKind, "subjectKind");
        Objects.requireNonNull(authoritativeSource, "authoritativeSource");
        Objects.requireNonNull(anchor, "anchor");
        Objects.requireNonNull(evidence, "evidence");

        Set<String> positive = normalizePositiveEvidence(evidence);
        List<AcquisitionCircumstance> compatible = circumstances.stream()
                .filter(circumstance -> circumstance.subjectKinds().contains(subjectKind))
                .filter(circumstance -> circumstance.sources().contains(authoritativeSource))
                .filter(circumstance -> circumstance.anchor().equals(anchor))
                .toList();
        if (compatible.isEmpty()) {
            throw new IllegalArgumentException("No authored acquisition circumstance for "
                    + subjectKind + " / " + authoritativeSource + " / " + anchor);
        }

        int bestMatches = compatible.stream()
                .mapToInt(circumstance -> intersectionSize(circumstance.evidenceTags(), positive))
                .max()
                .orElseThrow();
        List<AcquisitionCircumstance> finalists = compatible.stream()
                .filter(circumstance -> intersectionSize(circumstance.evidenceTags(), positive) == bestMatches)
                .sorted(Comparator.comparing(AcquisitionCircumstance::id))
                .toList();
        int index = Math.floorMod(mix(seed, subjectKind, authoritativeSource, anchor, positive), finalists.size());
        AcquisitionCircumstance selected = finalists.get(index);
        Set<String> matched = selected.evidenceTags().stream()
                .filter(positive::contains)
                .collect(Collectors.toUnmodifiableSet());

        return new ResolvedCircumstance(
                GENERATOR_VERSION,
                seed,
                selected.id(),
                subjectKind,
                authoritativeSource,
                anchor,
                matched,
                selected.title(),
                selected.description());
    }

    public static AnchorRef regionAnchor(String regionId) {
        return new AnchorRef(AnchorKind.DREAM_REALM_REGION, REGION_CATALOG_ID, regionId);
    }

    public static AnchorRef drownedBellResolutionAnchor(String resolutionId) {
        return new AnchorRef(AnchorKind.NIGHTMARE_RESOLUTION, DrownedBellScenarioDefinition.SCENARIO_ID, resolutionId);
    }

    public enum AnchorKind { DREAM_REALM_REGION, NIGHTMARE_RESOLUTION }

    public record AnchorRef(AnchorKind kind, String containerId, String entryId) {
        public AnchorRef {
            kind = Objects.requireNonNull(kind, "kind");
            containerId = stableId(containerId);
            entryId = stableId(entryId);
        }
    }

    public record AcquisitionCircumstance(
            String id,
            Set<SubjectKind> subjectKinds,
            Set<AcquisitionSource> sources,
            AnchorRef anchor,
            Set<String> evidenceTags,
            String title,
            String description,
            String evidenceClassification
    ) {
        public AcquisitionCircumstance {
            id = stableId(id);
            subjectKinds = nonEmptyCopy(subjectKinds, "subjectKinds");
            sources = nonEmptyCopy(sources, "sources");
            anchor = Objects.requireNonNull(anchor, "anchor");
            evidenceTags = nonEmptyTags(evidenceTags, "evidenceTags");
            title = text(title, "title");
            description = text(description, "description");
            evidenceClassification = text(evidenceClassification, "evidenceClassification");
        }
    }

    public record ResolvedCircumstance(
            int generatorVersion,
            long seed,
            String circumstanceId,
            SubjectKind subjectKind,
            AcquisitionSource authoritativeSource,
            AnchorRef anchor,
            Set<String> matchedEvidence,
            String title,
            String description
    ) {
        public ResolvedCircumstance {
            if (generatorVersion < 1) throw new IllegalArgumentException("generatorVersion must be positive");
            circumstanceId = stableId(circumstanceId);
            subjectKind = Objects.requireNonNull(subjectKind, "subjectKind");
            authoritativeSource = Objects.requireNonNull(authoritativeSource, "authoritativeSource");
            anchor = Objects.requireNonNull(anchor, "anchor");
            matchedEvidence = Set.copyOf(Objects.requireNonNull(matchedEvidence, "matchedEvidence"));
            title = text(title, "title");
            description = text(description, "description");
        }
    }

    private static AcquisitionCircumstance circumstance(
            String id,
            Set<SubjectKind> subjectKinds,
            Set<AcquisitionSource> sources,
            AnchorRef anchor,
            Set<String> tags,
            String title,
            String description
    ) {
        return new AcquisitionCircumstance(id, subjectKinds, sources, anchor, tags, title, description, "DESIGN");
    }

    private static void validateAnchors(List<AcquisitionCircumstance> authored) {
        Set<String> regionIds = DreamRealmRegionContentCatalog.waveOne().stream()
                .map(DreamRealmRegionContentCatalog.RegionProfile::id)
                .collect(Collectors.toUnmodifiableSet());
        Set<String> drownedBellResolutionIds = DrownedBellScenarioDefinition.content().resolutions().keySet();

        for (AcquisitionCircumstance circumstance : authored) {
            AnchorRef anchor = circumstance.anchor();
            if (anchor.kind() == AnchorKind.DREAM_REALM_REGION) {
                if (!anchor.containerId().equals(REGION_CATALOG_ID) || !regionIds.contains(anchor.entryId())) {
                    throw new IllegalArgumentException("Unknown Dream Realm region acquisition anchor: " + anchor);
                }
            } else if (anchor.kind() == AnchorKind.NIGHTMARE_RESOLUTION) {
                if (!anchor.containerId().equals(DrownedBellScenarioDefinition.SCENARIO_ID)
                        || !drownedBellResolutionIds.contains(anchor.entryId())) {
                    throw new IllegalArgumentException("Unknown Drowned Bell resolution acquisition anchor: " + anchor);
                }
            }
        }
    }

    private static Set<String> normalizePositiveEvidence(Map<String, Integer> evidence) {
        HashSet<String> positive = new HashSet<>();
        for (Map.Entry<String, Integer> entry : evidence.entrySet()) {
            String tag = stableId(entry.getKey());
            Integer magnitude = Objects.requireNonNull(entry.getValue(), "evidence magnitude");
            if (magnitude < 0) {
                throw new IllegalArgumentException("Evidence magnitude cannot be negative: " + tag);
            }
            if (magnitude > 0) positive.add(tag);
        }
        return Set.copyOf(positive);
    }

    private static int intersectionSize(Set<String> left, Set<String> right) {
        int matches = 0;
        for (String value : left) if (right.contains(value)) matches++;
        return matches;
    }

    private static int mix(long seed, SubjectKind subjectKind, AcquisitionSource source, AnchorRef anchor, Set<String> evidence) {
        long mixed = seed ^ ((long) GENERATOR_VERSION << 32);
        mixed = 31L * mixed + subjectKind.name().hashCode();
        mixed = 31L * mixed + source.name().hashCode();
        mixed = 31L * mixed + anchor.kind().name().hashCode();
        mixed = 31L * mixed + anchor.containerId().hashCode();
        mixed = 31L * mixed + anchor.entryId().hashCode();
        for (String tag : evidence.stream().sorted().toList()) mixed = 31L * mixed + tag.hashCode();
        return Long.hashCode(mixed);
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
