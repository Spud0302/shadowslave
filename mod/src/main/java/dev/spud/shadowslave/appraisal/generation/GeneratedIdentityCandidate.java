package dev.spud.shadowslave.appraisal.generation;

import dev.spud.shadowslave.soul.SoulRank;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Fully resolved procedural candidate ready to be persisted by a future
 * crash-safe appraisal transaction.
 *
 * <p>The selected primitive identifiers are retained so later generator
 * versions do not need to reconstruct an existing player's identity.</p>
 */
public record GeneratedIdentityCandidate(
        String generatorVersion,
        long seed,
        String generationFingerprint,
        String provenance,
        Aspect aspect,
        Flaw flaw
) {
    public GeneratedIdentityCandidate {
        generatorVersion = requireText(generatorVersion, "generatorVersion");
        generationFingerprint = requireFingerprint(generationFingerprint);
        provenance = requireText(provenance, "provenance");
        aspect = Objects.requireNonNull(aspect, "aspect");
        flaw = Objects.requireNonNull(flaw, "flaw");
    }

    public record Aspect(
            ResourceLocation instanceId,
            String formalName,
            SoulRank aspectRank,
            ResourceLocation natureId,
            ResourceLocation archetypeId,
            ResourceLocation abilityId,
            String abilityName
    ) {
        public Aspect {
            instanceId = Objects.requireNonNull(instanceId, "instanceId");
            formalName = requireText(formalName, "formalName");
            aspectRank = Objects.requireNonNull(aspectRank, "aspectRank");
            natureId = Objects.requireNonNull(natureId, "natureId");
            archetypeId = Objects.requireNonNull(archetypeId, "archetypeId");
            abilityId = Objects.requireNonNull(abilityId, "abilityId");
            abilityName = requireText(abilityName, "abilityName");
        }
    }

    public record Flaw(
            ResourceLocation instanceId,
            String formalName,
            ResourceLocation primitiveId,
            ResourceLocation effectId,
            Set<String> traitTags
    ) {
        public Flaw {
            instanceId = Objects.requireNonNull(instanceId, "instanceId");
            formalName = requireText(formalName, "formalName");
            primitiveId = Objects.requireNonNull(primitiveId, "primitiveId");
            effectId = Objects.requireNonNull(effectId, "effectId");
            traitTags = Set.copyOf(Objects.requireNonNull(traitTags, "traitTags"));
        }
    }

    private static String requireFingerprint(String value) {
        String checked = requireText(value, "generationFingerprint").toLowerCase(Locale.ROOT);
        if (checked.length() != 64 || !checked.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("generationFingerprint must be a lowercase SHA-256 hex digest");
        }
        return checked;
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }
}
