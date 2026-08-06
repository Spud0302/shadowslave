package dev.spud.shadowslave.appraisal.generation;

import dev.spud.shadowslave.soul.SoulRank;
import net.minecraft.resources.ResourceLocation;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToLongFunction;

/**
 * Deterministically composes a bounded procedural Aspect/Flaw candidate from
 * curated primitives, compatibility rules and recorded evidence.
 *
 * <p>This generator is intentionally not wired into the live preview appraisal
 * until successful Nightmare completion is restart-recoverable.</p>
 */
public final class DeterministicIdentityGenerator {
    public static final String GENERATOR_VERSION = "identity-v1";
    private static final String NAMESPACE = "shadowslave";
    private static final long EVIDENCE_MULTIPLIER = 8L;
    private static final long COHERENCE_BONUS = 3L;

    private final IdentityPrimitiveCatalog catalog;

    public DeterministicIdentityGenerator(IdentityPrimitiveCatalog catalog) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
    }

    public GeneratedIdentityCandidate generate(
            long seed,
            GenerationEvidence evidence,
            SoulRank aspectRank
    ) {
        Objects.requireNonNull(evidence, "evidence");
        Objects.requireNonNull(aspectRank, "aspectRank");

        IdentityPrimitiveCatalog.Nature nature = select(
                "nature",
                seed,
                evidence,
                catalog.natures(),
                candidate -> score(candidate.baseWeight(), candidate.tags(), Set.of(), evidence),
                IdentityPrimitiveCatalog.Nature::id
        );

        IdentityPrimitiveCatalog.Archetype archetype = select(
                "archetype",
                seed,
                evidence,
                catalog.archetypes(),
                candidate -> score(candidate.baseWeight(), candidate.affinityTags(), nature.tags(), evidence),
                IdentityPrimitiveCatalog.Archetype::id
        );

        Set<String> aspectContext = union(nature.tags(), archetype.affinityTags());
        List<IdentityPrimitiveCatalog.Ability> compatibleAbilities = catalog.abilities().stream()
                .filter(candidate -> candidate.supports(nature))
                .toList();
        IdentityPrimitiveCatalog.Ability ability = select(
                "ability",
                seed,
                evidence,
                compatibleAbilities,
                candidate -> score(candidate.baseWeight(), candidate.affinityTags(), aspectContext, evidence),
                IdentityPrimitiveCatalog.Ability::id
        );

        Set<String> completeAspectContext = union(aspectContext, ability.affinityTags());
        List<IdentityPrimitiveCatalog.Flaw> compatibleFlaws = catalog.flaws().stream()
                .filter(candidate -> candidate.supports(nature))
                .toList();
        IdentityPrimitiveCatalog.Flaw flaw = select(
                "flaw",
                seed,
                evidence,
                compatibleFlaws,
                candidate -> score(candidate.baseWeight(), candidate.affinityTags(), completeAspectContext, evidence),
                IdentityPrimitiveCatalog.Flaw::id
        );

        String fingerprint = sha256Hex(String.join(
                "|",
                GENERATOR_VERSION,
                Long.toString(seed),
                aspectRank.serializedName(),
                evidence.canonicalText(),
                nature.id().toString(),
                archetype.id().toString(),
                ability.id().toString(),
                flaw.id().toString()
        ));
        String provenance = "procedural_identity_design/" + GENERATOR_VERSION + "/" + fingerprint;

        return new GeneratedIdentityCandidate(
                GENERATOR_VERSION,
                seed,
                fingerprint,
                provenance,
                new GeneratedIdentityCandidate.Aspect(
                        generatedId("aspect", fingerprint.substring(0, 24)),
                        archetype.formatName(nature.nameToken()),
                        aspectRank,
                        nature.id(),
                        archetype.id(),
                        ability.id(),
                        ability.displayName()
                ),
                new GeneratedIdentityCandidate.Flaw(
                        generatedId("flaw", fingerprint.substring(24, 48)),
                        flaw.formalName(),
                        flaw.id(),
                        flaw.effectId(),
                        flaw.traitTags()
                )
        );
    }

    private static long score(
            int baseWeight,
            Set<String> affinityTags,
            Set<String> contextTags,
            GenerationEvidence evidence
    ) {
        long score = baseWeight;
        for (String tag : affinityTags) {
            score += (long) evidence.weightFor(tag) * EVIDENCE_MULTIPLIER;
            if (contextTags.contains(tag)) {
                score += COHERENCE_BONUS;
            }
        }
        return Math.max(1L, score);
    }

    private static <T> T select(
            String stage,
            long seed,
            GenerationEvidence evidence,
            List<T> candidates,
            ToLongFunction<T> scoreFunction,
            Function<T, ResourceLocation> idFunction
    ) {
        if (candidates.isEmpty()) {
            throw new IllegalStateException("No compatible candidates for generation stage " + stage);
        }

        ArrayList<Long> weights = new ArrayList<>(candidates.size());
        long totalWeight = 0L;
        for (T candidate : candidates) {
            long weight = scoreFunction.applyAsLong(candidate);
            if (weight <= 0L) {
                throw new IllegalStateException("Non-positive weight for " + idFunction.apply(candidate));
            }
            totalWeight = Math.addExact(totalWeight, weight);
            weights.add(weight);
        }

        long roll = Math.floorMod(
                hashToLong(GENERATOR_VERSION + "|" + seed + "|" + evidence.canonicalText() + "|" + stage),
                totalWeight
        );
        long cursor = 0L;
        for (int index = 0; index < candidates.size(); index++) {
            cursor += weights.get(index);
            if (roll < cursor) {
                return candidates.get(index);
            }
        }

        throw new IllegalStateException("Weighted selection exhausted without choosing a " + stage);
    }

    @SafeVarargs
    private static Set<String> union(Collection<String>... collections) {
        HashSet<String> values = new HashSet<>();
        for (Collection<String> collection : collections) {
            values.addAll(collection);
        }
        return Set.copyOf(values);
    }

    private static ResourceLocation generatedId(String family, String suffix) {
        return ResourceLocation.fromNamespaceAndPath(NAMESPACE, "generated/" + family + "/" + suffix);
    }

    private static long hashToLong(String value) {
        return ByteBuffer.wrap(sha256(value)).getLong();
    }

    private static String sha256Hex(String value) {
        byte[] digest = sha256(value);
        StringBuilder builder = new StringBuilder(digest.length * 2);
        for (byte current : digest) {
            builder.append(Character.forDigit((current >>> 4) & 0xF, 16));
            builder.append(Character.forDigit(current & 0xF, 16));
        }
        return builder.toString();
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("JVM does not provide SHA-256", exception);
        }
    }
}
