package dev.spud.shadowslave.soul.identity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.spud.shadowslave.soul.SoulRank;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/** Persistent revealed Aspect identity, independent from any execution provider. */
public record AspectInstanceData(
        ResourceLocation instanceId,
        String formalName,
        SoulRank aspectRank,
        ResourceLocation natureId,
        ResourceLocation abilityId,
        String provenance
) {
    private static final MapCodec<StoredAspectInstanceData> STORED_CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("instance_id").forGetter(StoredAspectInstanceData::instanceId),
                    Codec.STRING.fieldOf("formal_name").forGetter(StoredAspectInstanceData::formalName),
                    SoulRank.CODEC.fieldOf("aspect_rank").forGetter(StoredAspectInstanceData::aspectRank),
                    ResourceLocation.CODEC.fieldOf("nature_id").forGetter(StoredAspectInstanceData::natureId),
                    ResourceLocation.CODEC.fieldOf("ability_id").forGetter(StoredAspectInstanceData::abilityId),
                    Codec.STRING.fieldOf("provenance").forGetter(StoredAspectInstanceData::provenance)
            ).apply(instance, StoredAspectInstanceData::new));

    public static final MapCodec<AspectInstanceData> CODEC = STORED_CODEC.flatXmap(
            AspectInstanceData::construct,
            data -> DataResult.success(StoredAspectInstanceData.from(data))
    );

    public AspectInstanceData {
        instanceId = Objects.requireNonNull(instanceId, "instanceId");
        formalName = requireText(formalName, "formalName");
        aspectRank = Objects.requireNonNull(aspectRank, "aspectRank");
        natureId = Objects.requireNonNull(natureId, "natureId");
        abilityId = Objects.requireNonNull(abilityId, "abilityId");
        provenance = requireText(provenance, "provenance");
    }

    private static DataResult<AspectInstanceData> construct(StoredAspectInstanceData stored) {
        try {
            return DataResult.success(new AspectInstanceData(
                    stored.instanceId(),
                    stored.formalName(),
                    stored.aspectRank(),
                    stored.natureId(),
                    stored.abilityId(),
                    stored.provenance()
            ));
        } catch (IllegalArgumentException | NullPointerException exception) {
            return DataResult.error(() -> "Invalid AspectInstanceData: " + exception.getMessage());
        }
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }

    private record StoredAspectInstanceData(
            ResourceLocation instanceId,
            String formalName,
            SoulRank aspectRank,
            ResourceLocation natureId,
            ResourceLocation abilityId,
            String provenance
    ) {
        private static StoredAspectInstanceData from(AspectInstanceData data) {
            return new StoredAspectInstanceData(
                    data.instanceId(),
                    data.formalName(),
                    data.aspectRank(),
                    data.natureId(),
                    data.abilityId(),
                    data.provenance()
            );
        }
    }
}
