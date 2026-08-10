package dev.spud.shadowslave.memory;

import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MemoryOwnershipDataTest {
    @Test
    void persistentRoundTripRetainsIdentityAndAcquisitionProvenance() {
        MemoryInstanceData memory = new MemoryInstanceData(id("memory/ash_compass"), "Ash Compass", "first_nightmare_appraisal_design", "nightmare/example/resolution/signal_restored");
        MemoryOwnershipData original = new MemoryOwnershipData(List.of(memory));
        var encoded = MemoryOwnershipData.CODEC.codec().encodeStart(JsonOps.INSTANCE, original).getOrThrow();
        MemoryOwnershipData decoded = MemoryOwnershipData.CODEC.codec().parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertEquals(original, decoded);
    }

    @Test
    void duplicateAwardsAreIdempotentButContradictoryStoredDuplicatesFailClosed() {
        MemoryInstanceData memory = new MemoryInstanceData(id("memory/ash_compass"), "Ash Compass", "test", "first");
        MemoryOwnershipData once = MemoryOwnershipData.empty().award(memory);
        assertSame(once, once.award(memory));
        assertEquals(1, once.memories().size());
        assertThrows(IllegalArgumentException.class, () -> new MemoryOwnershipData(List.of(memory,
                new MemoryInstanceData(id("memory/ash_compass"), "Ash Compass", "other", "second"))));
    }

    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath("shadowslave", path); }
}
