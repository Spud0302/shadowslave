package dev.spud.shadowslave.memory;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/** Persistent server-authoritative collection of player-owned Memories. */
public record MemoryOwnershipData(List<MemoryInstanceData> memories) {
    private static final MapCodec<MemoryOwnershipData> RAW_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            MemoryInstanceData.CODEC.codec().listOf().optionalFieldOf("memories", List.of())
                    .forGetter(MemoryOwnershipData::memories)
    ).apply(instance, MemoryOwnershipData::new));

    public static final MapCodec<MemoryOwnershipData> CODEC = RAW_CODEC.flatXmap(
            value -> {
                try {
                    return DataResult.success(new MemoryOwnershipData(value.memories()));
                } catch (IllegalArgumentException | NullPointerException exception) {
                    return DataResult.error(() -> "Invalid MemoryOwnershipData: " + exception.getMessage());
                }
            },
            DataResult::success
    );

    public MemoryOwnershipData {
        memories = List.copyOf(Objects.requireNonNull(memories, "memories"));
        HashSet<ResourceLocation> ids = new HashSet<>();
        for (MemoryInstanceData memory : memories) {
            Objects.requireNonNull(memory, "memory");
            if (!ids.add(memory.memoryId())) {
                throw new IllegalArgumentException("Duplicate owned Memory id: " + memory.memoryId());
            }
        }
    }

    public static MemoryOwnershipData empty() {
        return new MemoryOwnershipData(List.of());
    }

    public boolean owns(ResourceLocation memoryId) {
        Objects.requireNonNull(memoryId, "memoryId");
        return memories.stream().anyMatch(memory -> memory.memoryId().equals(memoryId));
    }

    public MemoryOwnershipData award(MemoryInstanceData memory) {
        Objects.requireNonNull(memory, "memory");
        if (owns(memory.memoryId())) {
            return this;
        }
        ArrayList<MemoryInstanceData> next = new ArrayList<>(memories);
        next.add(memory);
        return new MemoryOwnershipData(next);
    }
}
