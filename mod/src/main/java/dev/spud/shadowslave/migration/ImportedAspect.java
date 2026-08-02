package dev.spud.shadowslave.migration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
    public static final MapCodec<ImportedAspect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("instance_id").forGetter(ImportedAspect::instanceId),
            Codec.STRING.fieldOf("formal_name").forGetter(ImportedAspect::formalName),
            SoulRank.CODEC.fieldOf("aspect_rank").forGetter(ImportedAspect::aspectRank),
            Codec.STRING.fieldOf("legacy_nature").forGetter(ImportedAspect::legacyNature),
            Codec.STRING.fieldOf("legacy_archetype").forGetter(ImportedAspect::legacyArchetype),
            ResourceLocation.CODEC.fieldOf("legacy_mechanical_root").forGetter(ImportedAspect::legacyMechanicalRoot),
            Codec.INT.fieldOf("source_score").forGetter(ImportedAspect::sourceScore),
            Codec.BOOL.optionalFieldOf("legacy_prototype", false).forGetter(ImportedAspect::legacyPrototype)
    ).apply(instance, ImportedAspect::new));

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
