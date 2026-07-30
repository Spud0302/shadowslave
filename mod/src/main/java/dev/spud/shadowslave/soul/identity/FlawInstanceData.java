package dev.spud.shadowslave.soul.identity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/** Persistent revealed Flaw identity; formal name and enforced effect remain separate. */
public record FlawInstanceData(
        ResourceLocation instanceId,
        String formalName,
        ResourceLocation effectId,
        String provenance
) {
    public static final MapCodec<FlawInstanceData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("instance_id").forGetter(FlawInstanceData::instanceId),
            Codec.STRING.fieldOf("formal_name").forGetter(FlawInstanceData::formalName),
            ResourceLocation.CODEC.fieldOf("effect_id").forGetter(FlawInstanceData::effectId),
            Codec.STRING.fieldOf("provenance").forGetter(FlawInstanceData::provenance)
    ).apply(instance, FlawInstanceData::new));

    public FlawInstanceData {
        instanceId = Objects.requireNonNull(instanceId, "instanceId");
        formalName = requireText(formalName, "formalName");
        effectId = Objects.requireNonNull(effectId, "effectId");
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
