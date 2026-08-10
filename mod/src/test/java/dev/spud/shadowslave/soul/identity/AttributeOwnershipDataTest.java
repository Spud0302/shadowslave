package dev.spud.shadowslave.soul.identity;

import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class AttributeOwnershipDataTest {
    @Test
    void persistentRoundTripRetainsResolvedIdentityWithoutCatalogueLookup() {
        AttributeInstanceData attribute = new AttributeInstanceData(
                id("generation/attribute/bell_sense"),
                "Bell Sense",
                "nightmare_role_inherited",
                "revealed",
                "procedural_identity_design/identity-v1/fingerprint/attribute-selection"
        );
        AttributeOwnershipData original = new AttributeOwnershipData(List.of(attribute));

        var encoded = AttributeOwnershipData.CODEC.codec().encodeStart(JsonOps.INSTANCE, original).getOrThrow();
        AttributeOwnershipData decoded = AttributeOwnershipData.CODEC.codec().parse(JsonOps.INSTANCE, encoded).getOrThrow();

        assertEquals(original, decoded);
    }

    @Test
    void awardingTheSameStableAttributeTwiceIsIdempotent() {
        AttributeInstanceData attribute = new AttributeInstanceData(
                id("generation/attribute/watchers_mark"),
                "Watcher's Mark",
                "nightmare_role_inherited",
                "revealed",
                "test"
        );
        AttributeOwnershipData once = AttributeOwnershipData.empty().award(attribute);
        AttributeOwnershipData twice = once.award(attribute);

        assertSame(once, twice);
        assertEquals(1, twice.attributes().size());
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("shadowslave", path);
    }
}
