package dev.spud.shadowslave.migration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
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
    private static final MapCodec<StoredImportedAspect> STORED_CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("instance_id").forGetter(StoredImportedAspect::instanceId),
                    Codec.STRING.fieldOf("formal_name").forGetter(StoredImportedAspect::formalName),
                    SoulRank.CODEC.fieldOf("aspect_rank").forGetter(StoredImportedAspect::aspectRank),
                    Codec.STRING.fieldOf("legacy_nature").forGetter(StoredImportedAspect::legacyNature),
                    Codec.STRING.fieldOf("legacy_archetype").forGetter(StoredImportedAspect::legacyArchetype),
                    ResourceLocation.CODEC.fieldOf("legacy_mechanical_root")
                            .forGetter(StoredImportedAspect::legacyMechanicalRoot),
                    Codec.INT.fieldOf("source_score").forGetter(StoredImportedAspect::sourceScore),
                    Codec.BOOL.optionalFieldOf("legacy_prototype", false)
                            .forGetter(StoredImportedAspect::legacyPrototype)
            ).apply(instance, StoredImportedAspect::new));

    public static final MapCodec<ImportedAspect> CODEC = STORED_CODEC.flatXmap(
            ImportedAspect::construct,
            data -> DataResult.success(StoredImportedAspect.from(data))
    );

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

    private static DataResult<ImportedAspect> construct(StoredImportedAspect stored) {
        try {
            return DataResult.success(new ImportedAspect(
                    stored.instanceId(),
                    stored.formalName(),
                    stored.aspectRank(),
                    stored.legacyNature(),
                    stored.legacyArchetype(),
                    stored.legacyMechanicalRoot(),
                    stored.sourceScore(),
                    stored.legacyPrototype()
            ));
        } catch (IllegalArgumentException | NullPointerException exception) {
            return DataResult.error(() -> "Invalid ImportedAspect: " + exception.getMessage());
        }
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }

    private record StoredImportedAspect(
            ResourceLocation instanceId,
            String formalName,
            SoulRank aspectRank,
            String legacyNature,
            String legacyArchetype,
            ResourceLocation legacyMechanicalRoot,
            int sourceScore,
            boolean legacyPrototype
    ) {
        private static StoredImportedAspect from(ImportedAspect data) {
            return new StoredImportedAspect(
                    data.instanceId(),
                    data.formalName(),
                    data.aspectRank(),
                    data.legacyNature(),
                    data.legacyArchetype(),
                    data.legacyMechanicalRoot(),
                    data.sourceScore(),
                    data.legacyPrototype()
            );
        }
    }
}
