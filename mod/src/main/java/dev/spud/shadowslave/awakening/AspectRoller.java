package dev.spud.shadowslave.awakening;

import dev.spud.shadowslave.appraisal.generation.DeterministicIdentityGenerator;
import dev.spud.shadowslave.appraisal.generation.ExpandedIdentityContentCatalog;
import dev.spud.shadowslave.appraisal.generation.GeneratedIdentityCandidate;
import dev.spud.shadowslave.appraisal.generation.GenerationEvidence;
import dev.spud.shadowslave.appraisal.generation.IdentityPrimitiveCatalog;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.identity.AspectAbilityData;
import dev.spud.shadowslave.soul.identity.AspectAbilitySetData;
import dev.spud.shadowslave.soul.identity.AspectInstanceData;
import dev.spud.shadowslave.soul.identity.FlawInstanceData;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Rolls an Aspect from the hand-made kits.
 *
 * <p>The existing generator picks the Nature (weighted lightly by playstyle
 * evidence) and the Archetype that names the Aspect. The Nature then fixes the
 * whole kit, so a second pass runs against a catalogue holding only that kit.
 * Pure and deterministic for a given seed and evidence.</p>
 */
public final class AspectRoller {
    /**
     * Every kit Nature starts from the same weight. The generator adds 8 per
     * evidence point and evidence tops out at 2 per tag, so with twenty Natures
     * playstyle moves one from about 5% to at most about 8%: it tips the odds
     * but never decides.
     */
    static final int NATURE_BASE_WEIGHT = 24;
    public static final SoulRank STARTING_RANK = SoulRank.DORMANT;
    private static final String PROVENANCE_PREFIX = "dream_candle_ritual/";

    private AspectRoller() {
    }

    public record Roll(AspectKit kit, GeneratedIdentityCandidate candidate, SoulIdentityData identity) {
        public Roll {
            Objects.requireNonNull(kit, "kit");
            Objects.requireNonNull(candidate, "candidate");
            Objects.requireNonNull(identity, "identity");
        }

        public String aspectName() {
            return candidate.aspect().formalName();
        }
    }

    public static Roll roll(long seed, GenerationEvidence evidence) {
        Objects.requireNonNull(evidence, "evidence");
        IdentityPrimitiveCatalog wave = ExpandedIdentityContentCatalog.kitWave();

        IdentityPrimitiveCatalog allKits = catalogFor(wave, AspectKitCatalog.all());
        ResourceLocation natureId = new DeterministicIdentityGenerator(allKits)
                .generate(seed, evidence, STARTING_RANK).aspect().natureId();
        AspectKit kit = AspectKitCatalog.byNature(natureId)
                .orElseThrow(() -> new IllegalStateException("Rolled a Nature without a kit: " + natureId));
        return rollKit(seed, evidence, kit);
    }

    /** Rolls an Aspect of one given kit; only the Archetype that names it is left to chance. For operators and tests. */
    public static Roll rollKit(long seed, GenerationEvidence evidence, AspectKit kit) {
        Objects.requireNonNull(kit, "kit");
        GeneratedIdentityCandidate candidate = new DeterministicIdentityGenerator(
                catalogFor(ExpandedIdentityContentCatalog.kitWave(), List.of(kit)))
                .generate(seed, evidence, STARTING_RANK);
        return new Roll(kit, candidate, identityFor(kit, candidate));
    }

    static IdentityPrimitiveCatalog catalogFor(IdentityPrimitiveCatalog wave, List<AspectKit> kits) {
        List<IdentityPrimitiveCatalog.Nature> natures = kits.stream()
                .map(kit -> find(wave.natures(), IdentityPrimitiveCatalog.Nature::id, kit.natureId()))
                .map(nature -> new IdentityPrimitiveCatalog.Nature(
                        nature.id(), nature.nameToken(), nature.tags(), NATURE_BASE_WEIGHT))
                .toList();
        List<IdentityPrimitiveCatalog.Ability> abilities = kits.stream()
                .map(kit -> find(wave.abilities(), IdentityPrimitiveCatalog.Ability::id, kit.abilityId()))
                .toList();
        List<IdentityPrimitiveCatalog.Flaw> flaws = kits.stream()
                .map(kit -> find(wave.flaws(), IdentityPrimitiveCatalog.Flaw::id, kit.flawId()))
                .toList();
        return new IdentityPrimitiveCatalog(natures, wave.archetypes(), abilities, flaws);
    }

    static SoulIdentityData identityFor(AspectKit kit, GeneratedIdentityCandidate candidate) {
        String provenance = PROVENANCE_PREFIX + candidate.provenance();
        GeneratedIdentityCandidate.Aspect aspect = candidate.aspect();
        GeneratedIdentityCandidate.Flaw flaw = candidate.flaw();
        AspectInstanceData aspectData = new AspectInstanceData(
                aspect.instanceId(),
                aspect.formalName(),
                aspect.aspectRank(),
                kit.natureId(),
                new AspectAbilitySetData(List.of(
                        AspectAbilityData.innate(kit.innateId(), provenance + "/innate"),
                        AspectAbilityData.rankGranted(kit.abilityId(), STARTING_RANK, provenance + "/dormant")
                )),
                provenance
        );
        FlawInstanceData flawData = new FlawInstanceData(flaw.instanceId(), kit.flawName(), kit.flawEffectId(), provenance);
        return new SoulIdentityData(Optional.of(aspectData), Optional.of(flawData));
    }

    private static <T> T find(List<T> values, Function<T, ResourceLocation> idOf, ResourceLocation id) {
        return values.stream()
                .filter(value -> idOf.apply(value).equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Kit references an id missing from the catalogue: " + id));
    }
}
