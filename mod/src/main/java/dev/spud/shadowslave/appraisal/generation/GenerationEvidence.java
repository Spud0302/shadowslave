package dev.spud.shadowslave.appraisal.generation;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.TreeMap;

/**
 * Canonical input evidence for a procedural identity appraisal.
 *
 * <p>This is DESIGN data rather than a claim that canon supplies a numerical
 * appraisal formula. The sorted normalized map makes generation independent of
 * insertion order and therefore safe to reproduce from a saved seed.</p>
 */
public record GenerationEvidence(
        String scenarioId,
        String historicalRoleId,
        String resolutionId,
        Map<String, Integer> weights
) {
    public GenerationEvidence {
        scenarioId = requireText(scenarioId, "scenarioId");
        historicalRoleId = requireText(historicalRoleId, "historicalRoleId");
        resolutionId = requireText(resolutionId, "resolutionId");

        TreeMap<String, Integer> canonicalWeights = new TreeMap<>();
        Objects.requireNonNull(weights, "weights").forEach((tag, weight) -> {
            String normalizedTag = normalizeTag(tag);
            if (weight == null || weight <= 0) {
                throw new IllegalArgumentException("Evidence weight must be positive for tag " + normalizedTag);
            }
            canonicalWeights.merge(normalizedTag, weight, Math::addExact);
        });
        weights = Collections.unmodifiableMap(canonicalWeights);
    }

    public int weightFor(String tag) {
        return weights.getOrDefault(normalizeTag(tag), 0);
    }

    /** Stable text used as an input to deterministic generation fingerprints. */
    public String canonicalText() {
        StringJoiner joiner = new StringJoiner("|");
        joiner.add(scenarioId);
        joiner.add(historicalRoleId);
        joiner.add(resolutionId);
        weights.forEach((tag, weight) -> joiner.add(tag + "=" + weight));
        return joiner.toString();
    }

    private static String normalizeTag(String value) {
        return requireText(value, "tag").toLowerCase(Locale.ROOT).replace(' ', '_');
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }
}
