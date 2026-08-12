package dev.spud.shadowslave.appraisal;

import dev.spud.shadowslave.nightmare.content.NightmareHistoricalSiteCatalog;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Project-owned appraisal input that measures how strongly a resolved Nightmare
 * departs from the site's authored original history.
 *
 * <p>The novel supports deviation from the original flow of fate as a major
 * appraisal factor, but does not expose the Spell's exact formula or thresholds.
 * This class therefore guarantees only a monotonic weighted divergence score.
 * Grade names and thresholds remain a replaceable calibration layer.</p>
 */
public final class NightmareDivergenceAppraisal {
    private NightmareDivergenceAppraisal() {
    }

    public record Result(
            int score,
            int maximumScore,
            Set<String> changedAxes,
            Set<String> unchangedAxes,
            Set<String> unknownAxes
    ) {
        public Result {
            if (score < 0 || maximumScore <= 0 || score > maximumScore) {
                throw new IllegalArgumentException("Invalid divergence score " + score + "/" + maximumScore);
            }
            changedAxes = Set.copyOf(Objects.requireNonNull(changedAxes, "changedAxes"));
            unchangedAxes = Set.copyOf(Objects.requireNonNull(unchangedAxes, "unchangedAxes"));
            unknownAxes = Set.copyOf(Objects.requireNonNull(unknownAxes, "unknownAxes"));
        }

        public double ratio() {
            return (double) score / (double) maximumScore;
        }
    }

    /**
     * Scores only resolved axes. Missing axes are UNKNOWN and grant no deviation
     * credit instead of assuming a change that the runtime did not prove.
     */
    public static Result score(NightmareHistoricalSiteCatalog.Site site, Map<String, String> resolvedHistory) {
        Objects.requireNonNull(site, "site");
        Map<String, String> checkedHistory = normalizedHistory(resolvedHistory);

        int score = 0;
        int maximum = 0;
        LinkedHashSet<String> changed = new LinkedHashSet<>();
        LinkedHashSet<String> unchanged = new LinkedHashSet<>();
        LinkedHashSet<String> unknown = new LinkedHashSet<>();

        for (NightmareHistoricalSiteCatalog.FateAxis axis : site.originalHistory().values()) {
            maximum = Math.addExact(maximum, axis.weight());
            String resolvedValue = checkedHistory.get(axis.id());
            if (resolvedValue == null) {
                unknown.add(axis.id());
            } else if (resolvedValue.equals(axis.originalValue())) {
                unchanged.add(axis.id());
            } else {
                changed.add(axis.id());
                score = Math.addExact(score, axis.weight());
            }
        }

        for (String suppliedAxis : checkedHistory.keySet()) {
            if (!site.originalHistory().containsKey(suppliedAxis)) {
                throw new IllegalArgumentException("Resolved history contains unknown fate axis " + suppliedAxis);
            }
        }

        return new Result(score, maximum, changed, unchanged, unknown);
    }

    private static Map<String, String> normalizedHistory(Map<String, String> source) {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : Objects.requireNonNull(source, "resolvedHistory").entrySet()) {
            String key = normalizedId(entry.getKey(), "fate axis");
            String value = normalizedId(entry.getValue(), "resolved fate value");
            if (result.put(key, value) != null) {
                throw new IllegalArgumentException("Duplicate resolved fate axis " + key);
            }
        }
        return Map.copyOf(result);
    }

    private static String normalizedId(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim().toLowerCase(java.util.Locale.ROOT);
        if (checked.isEmpty() || !checked.matches("[a-z0-9_]+")) {
            throw new IllegalArgumentException(name + " must be a stable lowercase id");
        }
        return checked;
    }
}
