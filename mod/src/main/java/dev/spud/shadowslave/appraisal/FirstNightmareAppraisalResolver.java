package dev.spud.shadowslave.appraisal;

import dev.spud.shadowslave.appraisal.generation.AttributeContentCatalog;
import dev.spud.shadowslave.appraisal.generation.DeterministicIdentityGenerator;
import dev.spud.shadowslave.appraisal.generation.ExpandedIdentityContentCatalog;
import dev.spud.shadowslave.appraisal.generation.GeneratedIdentityCandidate;
import dev.spud.shadowslave.appraisal.generation.GenerationEvidence;
import dev.spud.shadowslave.nightmare.NightmareInstance;
import dev.spud.shadowslave.soul.SoulRank;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Resolves the already-authored procedural identity content from one completed
 * First Nightmare. The weighting and seed mixing are Minecraft DESIGN, not a
 * claim about the Nightmare Spell's canonical appraisal algorithm.
 */
public final class FirstNightmareAppraisalResolver {
    private static final long ATTRIBUTE_SEED_SALT = 0x6A09E667F3BCC909L;

    private FirstNightmareAppraisalResolver() {
    }

    public record Award(
            GeneratedIdentityCandidate identity,
            AttributeContentCatalog.AttributeProfile attribute,
            GenerationEvidence evidence
    ) {
        public Award {
            identity = Objects.requireNonNull(identity, "identity");
            attribute = Objects.requireNonNull(attribute, "attribute");
            evidence = Objects.requireNonNull(evidence, "evidence");
        }
    }

    public static Award resolve(NightmareInstance completedInstance, String resolutionId) {
        Objects.requireNonNull(completedInstance, "completedInstance");
        String checkedResolution = requireText(resolutionId, "resolutionId");
        Map<String, Integer> weights = evidenceWeights(completedInstance.scenarioId(), completedInstance.historicalRoleId(), checkedResolution);
        GenerationEvidence evidence = new GenerationEvidence(
                completedInstance.scenarioId(),
                completedInstance.historicalRoleId(),
                checkedResolution,
                weights
        );
        long seed = seedFrom(completedInstance.instanceId());
        GeneratedIdentityCandidate identity = new DeterministicIdentityGenerator(ExpandedIdentityContentCatalog.waveOne())
                .generate(seed, evidence, SoulRank.AWAKENED);
        AttributeContentCatalog.AttributeProfile attribute = AttributeContentCatalog.waveOne()
                .select(seed ^ ATTRIBUTE_SEED_SALT, weights);
        return new Award(identity, attribute, evidence);
    }

    static Map<String, Integer> evidenceWeights(String scenarioId, String historicalRoleId, String resolutionId) {
        HashMap<String, Integer> weights = new HashMap<>();
        String scenario = requireText(scenarioId, "scenarioId").toLowerCase(Locale.ROOT);
        String role = requireText(historicalRoleId, "historicalRoleId").toLowerCase(Locale.ROOT);
        String resolution = requireText(resolutionId, "resolutionId").toLowerCase(Locale.ROOT);

        if (scenario.equals("last_signal")) {
            add(weights, "warning", 5);
            add(weights, "signal", 4);
            add(weights, "duty", 3);
            add(weights, "preservation", 2);
            add(weights, "resolve", 2);
        } else if (scenario.equals("drowned_bell")) {
            add(weights, "water", 5);
            add(weights, "warning", 3);
            add(weights, "resonance", 3);
            add(weights, "survival", 2);
            add(weights, "adaptation", 2);
        }

        if (role.contains("watch")) {
            add(weights, "warning", 3);
            add(weights, "duty", 3);
        }
        if (role.contains("courier") || role.contains("scout") || role.contains("guide")) {
            add(weights, "path", 3);
            add(weights, "guidance", 2);
        }
        if (role.contains("keeper") || role.contains("attendant")) {
            add(weights, "preservation", 3);
            add(weights, "duty", 2);
        }
        if (role.contains("interpreter") || role.contains("archive")) {
            add(weights, "witness", 2);
            add(weights, "perception", 2);
        }
        if (role.contains("ferry") || role.contains("cistern")) {
            add(weights, "water", 3);
        }

        if (resolution.contains("signal") || resolution.contains("tower")) {
            add(weights, "warning", 2);
            add(weights, "preservation", 2);
        }
        if (resolution.contains("evacuat") || resolution.contains("escape")) {
            add(weights, "guidance", 3);
            add(weights, "movement", 2);
        }
        if (resolution.contains("flood") || resolution.contains("water")) {
            add(weights, "water", 3);
            add(weights, "adaptation", 2);
        }
        if (resolution.contains("creature") || resolution.contains("buried")) {
            add(weights, "resolve", 2);
            add(weights, "aftermath", 2);
        }

        if (weights.isEmpty()) {
            add(weights, "resolve", 1);
        }
        return Map.copyOf(weights);
    }

    private static long seedFrom(UUID instanceId) {
        UUID checked = Objects.requireNonNull(instanceId, "instanceId");
        return checked.getMostSignificantBits() ^ Long.rotateLeft(checked.getLeastSignificantBits(), 23);
    }

    private static void add(Map<String, Integer> weights, String tag, int value) {
        weights.merge(tag, value, Math::addExact);
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }
}
