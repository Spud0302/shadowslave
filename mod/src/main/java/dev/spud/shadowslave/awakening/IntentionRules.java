package dev.spud.shadowslave.awakening;

import dev.spud.shadowslave.appraisal.generation.GenerationEvidence;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * How an intention nudges the roll (DESIGN): the offering and the longing each
 * add evidence to one Nature's tag, so offering and longing for the same Nature
 * roughly doubles its chance, from about 7% to 15% of the fourteen. Per-tag evidence is
 * capped so playstyle plus intention never makes a Nature certain.
 */
public final class IntentionRules {
    static final int OFFERING_WEIGHT = 2;
    static final int LONGING_WEIGHT = 2;
    static final int MAX_TAG_WEIGHT = 4;
    /** Mirrors the identity generator: each point of evidence adds this much to a Nature's weight. */
    static final int EVIDENCE_WEIGHT = 8;

    private IntentionRules() {
    }

    public static GenerationEvidence apply(GenerationEvidence base, IntentionData intention) {
        Map<String, Integer> weights = new LinkedHashMap<>(base.weights());
        if (!intention.offeringTag().isEmpty()) {
            weights.merge(intention.offeringTag(), OFFERING_WEIGHT, Integer::sum);
        }
        intention.parsedLonging().ifPresent(longing -> weights.merge(longing.evidenceTag(), LONGING_WEIGHT, Integer::sum));
        weights.replaceAll((tag, weight) -> Math.min(MAX_TAG_WEIGHT, weight));
        return new GenerationEvidence(base.scenarioId(), base.historicalRoleId(), base.resolutionId(), weights);
    }

    /** Chance of each Nature, in Longing order (one Longing per Nature), as whole percentages. */
    public static List<Integer> odds(GenerationEvidence evidence) {
        int[] weights = new int[Longing.values().length];
        int total = 0;
        for (Longing longing : Longing.values()) {
            weights[longing.ordinal()] = AspectRoller.NATURE_BASE_WEIGHT + EVIDENCE_WEIGHT * evidence.weightFor(longing.evidenceTag());
            total += weights[longing.ordinal()];
        }
        int sum = total;
        return Arrays.stream(weights).map(weight -> Math.round(100.0F * weight / sum)).boxed().toList();
    }
}
