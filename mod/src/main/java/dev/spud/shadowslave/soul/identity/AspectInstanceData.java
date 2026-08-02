package dev.spud.shadowslave.soul.identity;

import com.mojang.serialization.Codec;
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
    public static final MapCodec<AspectInstanceData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("instance_id").forGetter(AspectInstanceData::instanceId),
            Codec.STRING.fieldOf("formal_name").forGetter(AspectInstanceData::formalName),
            SoulRank.CODEC.fieldOf("aspect_rank").forGetter(AspectInstanceData::aspectRank),
            ResourceLocation.CODEC.fieldOf("nature_id").forGetter(AspectInstanceData::natureId),
            ResourceLocation.CODEC.fieldOf("ability_id").forGetter(AspectInstanceData::abilityId),
            Codec.STRING.fieldOf("provenance").forGetter(AspectInstanceData::provenance)
    ).apply(instance, AspectInstanceData::new));

    public AspectInstanceData {
        instanceId = Objects.requireNonNull(instanceId, "instanceId");
        formalName = requireText(formalName, "formalName");
        aspectRank = Objects.requireNonNull(aspectRank, "aspectRank");
        natureId = Objects.requireNonNull(natureId, "natureId");
        abilityId = Objects.requireNonNull(abilityId, "abilityId");
        provenance = requireText(provenance, "provenance");
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }
}
