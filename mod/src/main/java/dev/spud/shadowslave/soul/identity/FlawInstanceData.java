package dev.spud.shadowslave.soul.identity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Optional;

/** Persistent drawback identity with a separately revealed formal label. */
public record FlawInstanceData(
        ResourceLocation instanceId,
        Optional<String> formalName,
        ResourceLocation effectId,
        String provenance
) {
    private static final MapCodec<StoredFlawInstanceData> STORED_CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("instance_id").forGetter(StoredFlawInstanceData::instanceId),
                    Codec.STRING.optionalFieldOf("formal_name").forGetter(StoredFlawInstanceData::formalName),
                    ResourceLocation.CODEC.fieldOf("effect_id").forGetter(StoredFlawInstanceData::effectId),
                    Codec.STRING.fieldOf("provenance").forGetter(StoredFlawInstanceData::provenance)
            ).apply(instance, StoredFlawInstanceData::new));

    public static final MapCodec<FlawInstanceData> CODEC = STORED_CODEC.flatXmap(
            FlawInstanceData::construct,
            data -> DataResult.success(StoredFlawInstanceData.from(data))
    );

    public FlawInstanceData {
        instanceId = Objects.requireNonNull(instanceId, "instanceId");
        formalName = normalizeOptionalName(formalName);
        effectId = Objects.requireNonNull(effectId, "effectId");
        provenance = requireText(provenance, "provenance");
    }

    public FlawInstanceData(
            ResourceLocation instanceId,
            String formalName,
            ResourceLocation effectId,
            String provenance
    ) {
        this(instanceId, Optional.ofNullable(formalName), effectId, provenance);
    }

    public String displayedName() {
        return formalName.orElse("");
    }

    private static DataResult<FlawInstanceData> construct(StoredFlawInstanceData stored) {
        try {
            return DataResult.success(new FlawInstanceData(
                    stored.instanceId(),
                    stored.formalName(),
                    stored.effectId(),
                    stored.provenance()
            ));
        } catch (IllegalArgumentException | NullPointerException exception) {
            return DataResult.error(() -> "Invalid FlawInstanceData: " + exception.getMessage());
        }
    }

    private static Optional<String> normalizeOptionalName(Optional<String> value) {
        Optional<String> checked = Objects.requireNonNull(value, "formalName");
        if (checked.isEmpty()) {
            return Optional.empty();
        }
        String normalized = checked.orElseThrow().trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("formalName cannot be blank when present");
        }
        return Optional.of(normalized);
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }

    private record StoredFlawInstanceData(
            ResourceLocation instanceId,
            Optional<String> formalName,
            ResourceLocation effectId,
            String provenance
    ) {
        private static StoredFlawInstanceData from(FlawInstanceData data) {
            return new StoredFlawInstanceData(
                    data.instanceId(),
                    data.formalName(),
                    data.effectId(),
                    data.provenance()
            );
        }
    }
}
