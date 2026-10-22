package dev.spud.shadowslave.awakening;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/**
 * One hand-made Aspect kit for a Nature: an always-on innate rule, the Dormant
 * ability granted with the Aspect, and the Flaw that comes with it.
 *
 * <p>DESIGN data from the abilities rework plan. The Archetype half of the
 * Aspect name stays procedural; everything else about the kit is fixed.</p>
 */
public record AspectKit(
        ResourceLocation natureId,
        String natureName,
        ResourceLocation innateId,
        String innateName,
        String innateDescription,
        ResourceLocation abilityId,
        String abilityName,
        String abilityDescription,
        ResourceLocation flawId,
        ResourceLocation flawEffectId,
        String flawName,
        String flawDescription
) {
    public AspectKit {
        Objects.requireNonNull(natureId, "natureId");
        Objects.requireNonNull(natureName, "natureName");
        Objects.requireNonNull(innateId, "innateId");
        Objects.requireNonNull(innateName, "innateName");
        Objects.requireNonNull(innateDescription, "innateDescription");
        Objects.requireNonNull(abilityId, "abilityId");
        Objects.requireNonNull(abilityName, "abilityName");
        Objects.requireNonNull(abilityDescription, "abilityDescription");
        Objects.requireNonNull(flawId, "flawId");
        Objects.requireNonNull(flawEffectId, "flawEffectId");
        Objects.requireNonNull(flawName, "flawName");
        Objects.requireNonNull(flawDescription, "flawDescription");
    }
}
