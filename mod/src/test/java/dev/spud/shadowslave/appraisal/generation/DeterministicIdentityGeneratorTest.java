package dev.spud.shadowslave.appraisal.generation;

import dev.spud.shadowslave.soul.SoulRank;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeterministicIdentityGeneratorTest {
    private final DeterministicIdentityGenerator generator = new DeterministicIdentityGenerator(
            IdentityPrimitiveCatalog.lastSignalPrototype()
    );

    @Test
    void identicalCanonicalInputProducesIdenticalCandidate() {
        GenerationEvidence evidence = evidence(Map.of(
                "preservation", 7,
                "duty", 4,
                "warning", 2
        ));

        GeneratedIdentityCandidate first = generator.generate(914_211L, evidence, SoulRank.AWAKENED);
        GeneratedIdentityCandidate second = generator.generate(914_211L, evidence, SoulRank.AWAKENED);

        assertEquals(first, second);
    }

    @Test
    void evidenceInsertionOrderDoesNotChangeGeneration() {
        LinkedHashMap<String, Integer> forward = new LinkedHashMap<>();
        forward.put("preservation", 7);
        forward.put("duty", 4);
        forward.put("warning", 2);

        LinkedHashMap<String, Integer> reverse = new LinkedHashMap<>();
        reverse.put("warning", 2);
        reverse.put("duty", 4);
        reverse.put("preservation", 7);

        GeneratedIdentityCandidate first = generator.generate(77L, evidence(forward), SoulRank.AWAKENED);
        GeneratedIdentityCandidate second = generator.generate(77L, evidence(reverse), SoulRank.AWAKENED);

        assertEquals(first, second);
    }

    @Test
    void differentSeedsExploreMoreThanOneCombination() {
        HashSet<String> combinations = new HashSet<>();
        GenerationEvidence evidence = evidence(Map.of());

        for (long seed = 0L; seed < 128L; seed++) {
            GeneratedIdentityCandidate candidate = generator.generate(seed, evidence, SoulRank.AWAKENED);
            combinations.add(String.join(
                    "|",
                    candidate.aspect().natureId().toString(),
                    candidate.aspect().archetypeId().toString(),
                    candidate.aspect().abilityId().toString(),
                    candidate.flaw().primitiveId().toString()
            ));
        }

        assertTrue(combinations.size() > 8, "Expected the prototype primitives to produce varied combinations");
    }

    @Test
    void strongEvidenceBiasesTheMatchingNature() {
        int baselineEmberCount = 0;
        int biasedEmberCount = 0;
        GenerationEvidence baseline = evidence(Map.of());
        GenerationEvidence biased = evidence(Map.of(
                "ember", 1_000,
                "preservation", 1_000
        ));

        for (long seed = 0L; seed < 256L; seed++) {
            if (isNature(generator.generate(seed, baseline, SoulRank.AWAKENED), "generation/nature/ember")) {
                baselineEmberCount++;
            }
            if (isNature(generator.generate(seed, biased, SoulRank.AWAKENED), "generation/nature/ember")) {
                biasedEmberCount++;
            }
        }

        assertTrue(biasedEmberCount > baselineEmberCount);
        assertTrue(biasedEmberCount > 240, "Strong evidence should dominate without becoming a hard-coded result");
    }

    @Test
    void incompatibleFlawIsFilteredFromMovementNature() {
        GenerationEvidence movementEvidence = evidence(Map.of(
                "path", 1_000,
                "movement", 1_000
        ));
        int roadCandidates = 0;

        for (long seed = 0L; seed < 256L; seed++) {
            GeneratedIdentityCandidate candidate = generator.generate(seed, movementEvidence, SoulRank.AWAKENED);
            if (isNature(candidate, "generation/nature/road")) {
                roadCandidates++;
                assertFalse(candidate.flaw().traitTags().contains("immobility"));
            }
        }

        assertTrue(roadCandidates > 240, "Movement evidence should make the Road nature common in this bounded test");
    }

    @Test
    void candidateCarriesPersistenceReadyProvenance() {
        long seed = 123_456L;
        GeneratedIdentityCandidate candidate = generator.generate(
                seed,
                evidence(Map.of("resolve", 5)),
                SoulRank.ASCENDED
        );

        assertEquals(DeterministicIdentityGenerator.GENERATOR_VERSION, candidate.generatorVersion());
        assertEquals(seed, candidate.seed());
        assertEquals(64, candidate.evidenceFingerprint().length());
        assertTrue(candidate.provenance().contains(candidate.evidenceFingerprint()));
        assertEquals(SoulRank.ASCENDED, candidate.aspect().aspectRank());
        assertTrue(candidate.aspect().instanceId().getPath().startsWith("generated/aspect/"));
        assertTrue(candidate.flaw().instanceId().getPath().startsWith("generated/flaw/"));
        assertFalse(candidate.aspect().formalName().isBlank());
        assertFalse(candidate.aspect().abilityName().isBlank());
    }

    private static boolean isNature(GeneratedIdentityCandidate candidate, String path) {
        return candidate.aspect().natureId().getPath().equals(path);
    }

    private static GenerationEvidence evidence(Map<String, Integer> weights) {
        return new GenerationEvidence(
                "last_signal",
                "last_watchkeeper",
                "signal_restored",
                weights
        );
    }
}
