package dev.spud.shadowslave.echo;

import com.mojang.serialization.JsonOps;
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

    @Test
    void persistentRoundTripRetainsIdentityProvenanceAndManifestation() {
        UUID entityUuid = UUID.fromString("50c04cf0-709c-4c8d-9b31-595539415342");
        EchoInstanceData echo = new EchoInstanceData(
                ASH_BURROWER,
                "Ash Burrower",
                "first_nightmare_appraisal_design",
                "nightmare/example/resolution/signal_restored",
                Optional.of(entityUuid.toString())
        );
        EchoOwnershipData original = new EchoOwnershipData(List.of(echo));

        var encoded = EchoOwnershipData.CODEC.codec().encodeStart(JsonOps.INSTANCE, original).getOrThrow();
        EchoOwnershipData decoded = EchoOwnershipData.CODEC.codec().parse(JsonOps.INSTANCE, encoded).getOrThrow();

        assertEquals(original, decoded);
        assertEquals(Optional.of(entityUuid), decoded.find(ASH_BURROWER).orElseThrow().manifestationUuid());
    }

    @Test
    void awardIsIdempotentAndContradictoryIdentityFailsClosed() {
        EchoInstanceData echo = new EchoInstanceData(
                ASH_BURROWER, "Ash Burrower", "test_award", "test", Optional.empty());
        EchoOwnershipData once = EchoOwnershipData.empty().award(echo);

        assertSame(once, once.award(echo));
        assertTrue(once.owns(ASH_BURROWER));
        assertThrows(IllegalArgumentException.class, () -> once.award(new EchoInstanceData(
                ASH_BURROWER, "Different Echo", "test_award", "test", Optional.empty())));
        assertThrows(IllegalArgumentException.class, () -> new EchoOwnershipData(List.of(echo, echo)));
    }

    @Test
    void manifestationCanBeRecordedAndClearedWithoutChangingOwnership() {
        EchoOwnershipData owned = EchoOwnershipData.empty().award(new EchoInstanceData(
                ASH_BURROWER, "Ash Burrower", "test_award", "test", Optional.empty()));
        UUID entityUuid = UUID.fromString("fefc4139-2617-4fc0-aa89-514b674fd98a");

        EchoOwnershipData manifested = owned.withManifestation(ASH_BURROWER, Optional.of(entityUuid));
        assertEquals(Optional.of(entityUuid), manifested.find(ASH_BURROWER).orElseThrow().manifestationUuid());

        EchoOwnershipData dismissed = manifested.withManifestation(ASH_BURROWER, Optional.empty());
        assertTrue(dismissed.owns(ASH_BURROWER));
        assertFalse(dismissed.find(ASH_BURROWER).orElseThrow().manifestationUuid().isPresent());
    }

    @Test
    void malformedStoredManifestationUuidFailsClosed() {
        assertThrows(IllegalArgumentException.class, () -> new EchoInstanceData(
                ASH_BURROWER, "Ash Burrower", "test_award", "test", Optional.of("not-a-uuid")));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("shadowslave", path);
    }
}
