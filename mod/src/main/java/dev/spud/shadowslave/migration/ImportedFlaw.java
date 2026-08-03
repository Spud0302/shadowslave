package dev.spud.shadowslave.migration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
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
    private static final MapCodec<StoredImportedFlaw> STORED_CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("instance_id").forGetter(StoredImportedFlaw::instanceId),
                    Codec.STRING.fieldOf("formal_name").forGetter(StoredImportedFlaw::formalName),
                    Codec.STRING.fieldOf("legacy_family").forGetter(StoredImportedFlaw::legacyFamily),
                    ResourceLocation.CODEC.fieldOf("effect_id").forGetter(StoredImportedFlaw::effectId),
                    Codec.INT.fieldOf("source_score").forGetter(StoredImportedFlaw::sourceScore),
                    Codec.BOOL.optionalFieldOf("legacy_prototype", false)
                            .forGetter(StoredImportedFlaw::legacyPrototype)
            ).apply(instance, StoredImportedFlaw::new));

    public static final MapCodec<ImportedFlaw> CODEC = STORED_CODEC.flatXmap(
            ImportedFlaw::construct,
            data -> DataResult.success(StoredImportedFlaw.from(data))
    );

    public ImportedFlaw {
        instanceId = Objects.requireNonNull(instanceId, "instanceId");
        formalName = requireText(formalName, "formalName");
        legacyFamily = requireText(legacyFamily, "legacyFamily");
        effectId = Objects.requireNonNull(effectId, "effectId");
        if (sourceScore <= 0) {
            throw new IllegalArgumentException("sourceScore must be positive");
        }
    }

    private static DataResult<ImportedFlaw> construct(StoredImportedFlaw stored) {
        try {
            return DataResult.success(new ImportedFlaw(
                    stored.instanceId(),
                    stored.formalName(),
                    stored.legacyFamily(),
                    stored.effectId(),
                    stored.sourceScore(),
                    stored.legacyPrototype()
            ));
        } catch (IllegalArgumentException | NullPointerException exception) {
            return DataResult.error(() -> "Invalid ImportedFlaw: " + exception.getMessage());
        }
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }

    private record StoredImportedFlaw(
            ResourceLocation instanceId,
            String formalName,
            String legacyFamily,
            ResourceLocation effectId,
            int sourceScore,
            boolean legacyPrototype
    ) {
        private static StoredImportedFlaw from(ImportedFlaw data) {
            return new StoredImportedFlaw(
                    data.instanceId(),
                    data.formalName(),
                    data.legacyFamily(),
                    data.effectId(),
                    data.sourceScore(),
                    data.legacyPrototype()
            );
        }
    }
}
