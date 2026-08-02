package dev.spud.shadowslave.migration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
    public static final MapCodec<ImportedFlaw> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("instance_id").forGetter(ImportedFlaw::instanceId),
            Codec.STRING.fieldOf("formal_name").forGetter(ImportedFlaw::formalName),
            Codec.STRING.fieldOf("legacy_family").forGetter(ImportedFlaw::legacyFamily),
            ResourceLocation.CODEC.fieldOf("effect_id").forGetter(ImportedFlaw::effectId),
            Codec.INT.fieldOf("source_score").forGetter(ImportedFlaw::sourceScore),
            Codec.BOOL.optionalFieldOf("legacy_prototype", false).forGetter(ImportedFlaw::legacyPrototype)
    ).apply(instance, ImportedFlaw::new));

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
