package dev.spud.shadowslave.nightmare.content;

import dev.spud.shadowslave.dreamrealm.content.DreamRealmRegionContentCatalog;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Java-owned identity bridge between a Nightmare's reconstructed historical site
 * and the later Dream Realm place that can be explored after that history has
 * become ruin, wilderness, settlement, or some other later state.
 *
 * <p>This catalogue does not claim that a challenger's Nightmare actions rewrite
 * the present Dream Realm. It identifies the same place across two eras so world
 * generation and scenario execution can share geography without sharing mutable
 * authority.</p>
 */
public final class NightmareHistoricalSiteCatalog {
    private NightmareHistoricalSiteCatalog() {
    }

    public record Site(
            String id,
            String scenarioId,
            String dreamRealmRegionId,
            Map<String, String> historicalToFutureLandmarks,
            Map<String, FateAxis> originalHistory,
            String futureStateCue
    ) {
        public Site {
            id = stableId(id, "id");
            scenarioId = stableId(scenarioId, "scenarioId");
            dreamRealmRegionId = stableId(dreamRealmRegionId, "dreamRealmRegionId");
            historicalToFutureLandmarks = immutableIdMap(historicalToFutureLandmarks, "historicalToFutureLandmarks");
            originalHistory = immutableFateMap(originalHistory);
            futureStateCue = text(futureStateCue, "futureStateCue");
            if (historicalToFutureLandmarks.isEmpty()) {
                throw new IllegalArgumentException("historicalToFutureLandmarks cannot be empty");
            }
            if (originalHistory.isEmpty()) {
                throw new IllegalArgumentException("originalHistory cannot be empty");
            }
        }
    }

    /** One historically meaningful state axis used by divergence appraisal. */
    public record FateAxis(String id, String originalValue, int weight) {
        public FateAxis {
            id = stableId(id, "fate axis id");
            originalValue = stableId(originalValue, "originalValue");
            if (weight <= 0) {
                throw new IllegalArgumentException("fate-axis weight must be positive");
            }
        }
    }

    /**
     * First era-linked proof: The Drowned Bell is the historical settlement that
     * later survives only as a ruined site inside Storm Lantern Coast.
     *
     * <p>All exact site names, fate-axis values, weights and later ruin details
     * are project DESIGN. The canonical proposition is only that Nightmares
     * reconstruct conflicts from the ancient history of the Dream Realm.</p>
     */
    public static Site drownedBell() {
        return new Site(
                "drowned_bell_cliff_settlement",
                DrownedBellScenarioDefinition.SCENARIO_ID,
                "storm_lantern_coast",
                Map.of(
                        "bell_tower", "storm_belfry",
                        "sea_gate", "sea_gate",
                        "quarry_tunnels", "collapsed_quarry_cut",
                        "lower_village", "drowned_harbour_terraces"
                ),
                fateAxes(
                        new FateAxis("warning_bell", "silent", 3),
                        new FateAxis("quarry_route", "sealed", 2),
                        new FateAxis("sea_gate", "failed", 3),
                        new FateAxis("lower_village", "inundated", 4),
                        new FateAxis("drowned_listener", "survived", 3)
                ),
                "Storm-blackened foundations, a broken belfry and the remains of a sea gate mark the later age of the same cliff settlement reconstructed by The Drowned Bell."
        );
    }

    public static List<Site> waveOne() {
        List<Site> sites = List.of(drownedBell());
        validateUniqueIds(sites);
        validateRegionLinks(sites);
        return sites;
    }

    public static Site requireScenario(String scenarioId) {
        String checked = stableId(scenarioId, "scenarioId");
        return waveOne().stream()
                .filter(site -> site.scenarioId().equals(checked))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No historical Dream Realm site for scenario " + checked));
    }

    private static Map<String, FateAxis> fateAxes(FateAxis... axes) {
        LinkedHashMap<String, FateAxis> result = new LinkedHashMap<>();
        for (FateAxis axis : axes) {
            FateAxis checked = Objects.requireNonNull(axis, "axis");
            if (result.put(checked.id(), checked) != null) {
                throw new IllegalArgumentException("Duplicate fate axis " + checked.id());
            }
        }
        return Map.copyOf(result);
    }

    private static Map<String, FateAxis> immutableFateMap(Map<String, FateAxis> source) {
        LinkedHashMap<String, FateAxis> result = new LinkedHashMap<>();
        for (Map.Entry<String, FateAxis> entry : Objects.requireNonNull(source, "originalHistory").entrySet()) {
            FateAxis axis = Objects.requireNonNull(entry.getValue(), "fate axis");
            String key = stableId(entry.getKey(), "fate axis key");
            if (!key.equals(axis.id())) {
                throw new IllegalArgumentException("Fate-axis map key must equal axis id: " + key + " != " + axis.id());
            }
            if (result.put(key, axis) != null) {
                throw new IllegalArgumentException("Duplicate fate axis " + key);
            }
        }
        return Map.copyOf(result);
    }

    private static Map<String, String> immutableIdMap(Map<String, String> source, String name) {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : Objects.requireNonNull(source, name).entrySet()) {
            String key = stableId(entry.getKey(), name + " key");
            String value = stableId(entry.getValue(), name + " value");
            if (result.put(key, value) != null) {
                throw new IllegalArgumentException("Duplicate " + name + " key " + key);
            }
        }
        return Map.copyOf(result);
    }

    private static void validateUniqueIds(List<Site> sites) {
        Set<String> ids = new java.util.HashSet<>();
        Set<String> scenarios = new java.util.HashSet<>();
        for (Site site : sites) {
            if (!ids.add(site.id())) {
                throw new IllegalArgumentException("Duplicate historical site id " + site.id());
            }
            if (!scenarios.add(site.scenarioId())) {
                throw new IllegalArgumentException("Scenario maps to more than one historical site " + site.scenarioId());
            }
        }
    }

    private static void validateRegionLinks(List<Site> sites) {
        Set<String> regionIds = DreamRealmRegionContentCatalog.waveOne().stream()
                .map(DreamRealmRegionContentCatalog.RegionProfile::id)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        for (Site site : sites) {
            if (!regionIds.contains(site.dreamRealmRegionId())) {
                throw new IllegalArgumentException("Historical site " + site.id() + " links unknown Dream Realm region " + site.dreamRealmRegionId());
            }
        }
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
