package dev.spud.shadowslave.migration;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/** Preserved player-facing and mechanical metadata for one datapack Flaw. */
public record ImportedFlaw(
        ResourceLocation instanceId,
        String formalName,
        String legacyFamily,
        ResourceLocation effectId,
        int sourceScore,
        boolean legacyPrototype
) {
    public ImportedFlaw {
        instanceId = Objects.requireNonNull(instanceId, "instanceId");
        formalName = requireText(formalName, "formalName");
        legacyFamily = requireText(legacyFamily, "legacyFamily");
        effectId = Objects.requireNonNull(effectId, "effectId");
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
