package dev.spud.shadowslave.echo;

import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EchoOwnershipDataTest {
    private static final ResourceLocation ASH_BURROWER = id("echo/ash_burrower");
    private static final ResourceLocation OVERWORLD = ResourceLocation.fromNamespaceAndPath("minecraft", "overworld");

    @Test
    void persistentRoundTripRetainsIdentityProvenanceAndManifestationLocation() {
        UUID entityUuid = UUID.fromString("50c04cf0-709c-4c8d-9b31-595539415342");
        BlockPos position = new BlockPos(12, 70, -9);
        EchoInstanceData echo = unmanifested().withManifestation(entityUuid, OVERWORLD, position);
        EchoOwnershipData original = new EchoOwnershipData(List.of(echo));

        var encoded = EchoOwnershipData.CODEC.codec().encodeStart(JsonOps.INSTANCE, original).getOrThrow();
        EchoOwnershipData decoded = EchoOwnershipData.CODEC.codec().parse(JsonOps.INSTANCE, encoded).getOrThrow();

        assertEquals(original, decoded);
        EchoInstanceData restored = decoded.find(ASH_BURROWER).orElseThrow();
        assertEquals(Optional.of(entityUuid), restored.manifestationUuid());
        assertEquals(Optional.of(OVERWORLD), restored.manifestationDimension());
        assertEquals(Optional.of(position), restored.manifestationPos());
    }

    @Test
    void awardIsIdempotentAndContradictoryIdentityFailsClosed() {
        EchoInstanceData echo = unmanifested();
        EchoOwnershipData once = EchoOwnershipData.empty().award(echo);

        assertSame(once, once.award(echo));
        assertTrue(once.owns(ASH_BURROWER));
        assertThrows(IllegalArgumentException.class, () -> once.award(new EchoInstanceData(
                ASH_BURROWER, "Different Echo", "test_award", "test",
                Optional.empty(), Optional.empty(), Optional.empty())));
        assertThrows(IllegalArgumentException.class, () -> new EchoOwnershipData(List.of(echo, echo)));
    }

    @Test
    void manifestationCanBeRecordedAndClearedWithoutChangingOwnership() {
        EchoOwnershipData owned = EchoOwnershipData.empty().award(unmanifested());
        UUID entityUuid = UUID.fromString("fefc4139-2617-4fc0-aa89-514b674fd98a");
        BlockPos position = new BlockPos(4, 80, 6);

        EchoOwnershipData manifested = owned.withManifestation(ASH_BURROWER, entityUuid, OVERWORLD, position);
        EchoInstanceData active = manifested.find(ASH_BURROWER).orElseThrow();
        assertEquals(Optional.of(entityUuid), active.manifestationUuid());
        assertEquals(Optional.of(position), active.manifestationPos());

        EchoOwnershipData dismissed = manifested.withoutManifestation(ASH_BURROWER);
        assertTrue(dismissed.owns(ASH_BURROWER));
        assertFalse(dismissed.find(ASH_BURROWER).orElseThrow().manifestationUuid().isPresent());
    }

    @Test
    void malformedOrPartialStoredManifestationFailsClosed() {
        assertThrows(IllegalArgumentException.class, () -> new EchoInstanceData(
                ASH_BURROWER, "Ash Burrower", "test_award", "test",
                Optional.of("not-a-uuid"), Optional.of(OVERWORLD), Optional.of(BlockPos.ZERO.asLong())));
        assertThrows(IllegalArgumentException.class, () -> new EchoInstanceData(
                ASH_BURROWER, "Ash Burrower", "test_award", "test",
                Optional.of(UUID.randomUUID().toString()), Optional.empty(), Optional.empty()));
    }

    private static EchoInstanceData unmanifested() {
        return new EchoInstanceData(
                ASH_BURROWER,
                "Ash Burrower",
                "test_award",
                "test",
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("shadowslave", path);
    }
}
