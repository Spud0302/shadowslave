package dev.spud.shadowslave.soul.identity;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/** Persistent server-authoritative collection of player-owned Attributes. */
public record AttributeOwnershipData(List<AttributeInstanceData> attributes) {
    private static final MapCodec<AttributeOwnershipData> RAW_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            AttributeInstanceData.CODEC.codec().listOf().optionalFieldOf("attributes", List.of())
                    .forGetter(AttributeOwnershipData::attributes)
    ).apply(instance, AttributeOwnershipData::new));

    public static final MapCodec<AttributeOwnershipData> CODEC = RAW_CODEC.flatXmap(
            value -> {
                try {
                    return DataResult.success(new AttributeOwnershipData(value.attributes()));
                } catch (IllegalArgumentException | NullPointerException exception) {
                    return DataResult.error(() -> "Invalid AttributeOwnershipData: " + exception.getMessage());
                }
            },
            DataResult::success
    );

    public AttributeOwnershipData {
        attributes = List.copyOf(Objects.requireNonNull(attributes, "attributes"));
        HashSet<Object> ids = new HashSet<>();
        for (AttributeInstanceData attribute : attributes) {
            Objects.requireNonNull(attribute, "attribute");
            if (!ids.add(attribute.attributeId())) {
                throw new IllegalArgumentException("Duplicate owned Attribute id: " + attribute.attributeId());
            }
        }
    }

    public static AttributeOwnershipData empty() {
        return new AttributeOwnershipData(List.of());
    }

    public AttributeOwnershipData award(AttributeInstanceData attribute) {
        Objects.requireNonNull(attribute, "attribute");
        if (attributes.stream().anyMatch(existing -> existing.attributeId().equals(attribute.attributeId()))) {
            return this;
        }
        java.util.ArrayList<AttributeInstanceData> next = new java.util.ArrayList<>(attributes);
        next.add(attribute);
        return new AttributeOwnershipData(next);
    }
}
