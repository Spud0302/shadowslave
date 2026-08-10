package dev.spud.shadowslave.memory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/** Persistent Java-owned identity and provenance for one owned Memory. */
public record MemoryInstanceData(
        ResourceLocation memoryId,
        String formalName,
        String acquisitionSource,
        String provenance
) {
    private static final MapCodec<MemoryInstanceData> RAW_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("memory_id").forGetter(MemoryInstanceData::memoryId),
            Codec.STRING.fieldOf("formal_name").forGetter(MemoryInstanceData::formalName),
            Codec.STRING.fieldOf("acquisition_source").forGetter(MemoryInstanceData::acquisitionSource),
            Codec.STRING.fieldOf("provenance").forGetter(MemoryInstanceData::provenance)
    ).apply(instance, MemoryInstanceData::new));

    public static final MapCodec<MemoryInstanceData> CODEC = RAW_CODEC.flatXmap(
            value -> {
                try {
                    return DataResult.success(new MemoryInstanceData(
                            value.memoryId(), value.formalName(), value.acquisitionSource(), value.provenance()));
                } catch (IllegalArgumentException | NullPointerException exception) {
                    return DataResult.error(() -> "Invalid MemoryInstanceData: " + exception.getMessage());
                }
            },
            DataResult::success
    );

    public MemoryInstanceData {
        memoryId = Objects.requireNonNull(memoryId, "memoryId");
        formalName = requireText(formalName, "formalName");
        acquisitionSource = requireText(acquisitionSource, "acquisitionSource");
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
