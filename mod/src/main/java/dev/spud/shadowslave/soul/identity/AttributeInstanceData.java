package dev.spud.shadowslave.soul.identity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/** Persistent Java-owned Attribute identity awarded to a player. */
public record AttributeInstanceData(
        ResourceLocation attributeId,
        String formalName,
        String origin,
        String visibility,
        String provenance
) {
    private static final MapCodec<AttributeInstanceData> RAW_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("attribute_id").forGetter(AttributeInstanceData::attributeId),
            Codec.STRING.fieldOf("formal_name").forGetter(AttributeInstanceData::formalName),
            Codec.STRING.fieldOf("origin").forGetter(AttributeInstanceData::origin),
            Codec.STRING.fieldOf("visibility").forGetter(AttributeInstanceData::visibility),
            Codec.STRING.fieldOf("provenance").forGetter(AttributeInstanceData::provenance)
    ).apply(instance, AttributeInstanceData::new));

    public static final MapCodec<AttributeInstanceData> CODEC = RAW_CODEC.flatXmap(
            value -> {
                try {
                    return DataResult.success(new AttributeInstanceData(
                            value.attributeId(), value.formalName(), value.origin(), value.visibility(), value.provenance()));
                } catch (IllegalArgumentException | NullPointerException exception) {
                    return DataResult.error(() -> "Invalid AttributeInstanceData: " + exception.getMessage());
                }
            },
            DataResult::success
    );

    public AttributeInstanceData {
        attributeId = Objects.requireNonNull(attributeId, "attributeId");
        formalName = requireText(formalName, "formalName");
        origin = requireText(origin, "origin");
        visibility = requireText(visibility, "visibility");
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
