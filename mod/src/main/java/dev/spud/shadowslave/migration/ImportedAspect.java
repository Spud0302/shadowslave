package dev.spud.shadowslave.migration;

import dev.spud.shadowslave.soul.SoulRank;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/** Preserved player-facing and mechanical metadata for one datapack Aspect. */
public record ImportedAspect(
        ResourceLocation instanceId,
        String formalName,
        SoulRank aspectRank,
        String legacyNature,
        String legacyArchetype,
        ResourceLocation legacyMechanicalRoot,
        int sourceScore,
        boolean legacyPrototype
) {
    public ImportedAspect {
        instanceId = Objects.requireNonNull(instanceId, "instanceId");
        formalName = requireText(formalName, "formalName");
        aspectRank = Objects.requireNonNull(aspectRank, "aspectRank");
        legacyNature = requireText(legacyNature, "legacyNature");
        legacyArchetype = requireText(legacyArchetype, "legacyArchetype");
        legacyMechanicalRoot = Objects.requireNonNull(legacyMechanicalRoot, "legacyMechanicalRoot");
        if (sourceScore <= 0) {
            throw new IllegalArgumentException("sourceScore must be positive");
        }
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }
}
