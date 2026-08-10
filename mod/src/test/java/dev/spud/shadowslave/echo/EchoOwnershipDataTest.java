package dev.spud.shadowslave.echo;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.spud.shadowslave.echo.content.EchoContentCatalog;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EchoOwnershipDataTest {
    private static final ResourceLocation ASH_BURROWER = id("echo/ash_burrower");
    private static final ResourceLocation OVERWORLD = ResourceLocation.fromNamespaceAndPath("minecraft", "overworld");

    @Test void persistentRoundTripRetainsIdentityProvenanceCommandAndManifestationLocation() {
        UUID uuid = UUID.fromString("50c04cf0-709c-4c8d-9b31-595539415342");
        BlockPos pos = new BlockPos(12, 70, -9);
        EchoOwnershipData original = new EchoOwnershipData(List.of(unmanifested()
                .withCommandMode(EchoContentCatalog.CommandMode.FOLLOW).withManifestation(uuid, OVERWORLD, pos)));
        var encoded = EchoOwnershipData.CODEC.codec().encodeStart(JsonOps.INSTANCE, original).getOrThrow();
        EchoOwnershipData decoded = EchoOwnershipData.CODEC.codec().parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertEquals(original, decoded);
        assertEquals(EchoContentCatalog.CommandMode.FOLLOW, decoded.find(ASH_BURROWER).orElseThrow().commandMode());
    }

    @Test void olderStoredEchoWithoutCommandDefaultsToHold() {
        var encoded = EchoInstanceData.CODEC.codec().encodeStart(JsonOps.INSTANCE, unmanifested()).getOrThrow();
        JsonObject object = encoded.getAsJsonObject(); object.remove("command_mode");
        assertEquals(EchoContentCatalog.CommandMode.HOLD,
                EchoInstanceData.CODEC.codec().parse(JsonOps.INSTANCE, object).getOrThrow().commandMode());
    }

    @Test void commandModePreservesManifestationAndDismissalPreservesCommand() {
        UUID uuid = UUID.fromString("fefc4139-2617-4fc0-aa89-514b674fd98a");
        BlockPos pos = new BlockPos(4, 80, 6);
        EchoOwnershipData following = EchoOwnershipData.empty().award(unmanifested())
                .withManifestation(ASH_BURROWER, uuid, OVERWORLD, pos)
                .withCommandMode(ASH_BURROWER, EchoContentCatalog.CommandMode.FOLLOW);
        EchoInstanceData active = following.find(ASH_BURROWER).orElseThrow();
        assertEquals(Optional.of(uuid), active.manifestationUuid());
        assertEquals(Optional.of(pos), active.manifestationPos());
        EchoOwnershipData dismissed = following.withoutManifestation(ASH_BURROWER);
        assertFalse(dismissed.find(ASH_BURROWER).orElseThrow().manifestationUuid().isPresent());
        assertEquals(EchoContentCatalog.CommandMode.FOLLOW, dismissed.find(ASH_BURROWER).orElseThrow().commandMode());
        assertSame(dismissed, dismissed.withCommandMode(ASH_BURROWER, EchoContentCatalog.CommandMode.FOLLOW));
    }

    @Test void guardPointRoundTripKeepsAnchorIndependentFromManifestation() {
        UUID uuid = UUID.fromString("afaa04e6-51b5-4ea0-9f23-a5c769a48e3a");
        BlockPos guardPoint = new BlockPos(24, 72, -11);
        BlockPos manifestation = new BlockPos(31, 72, -4);
        EchoOwnershipData guarding = EchoOwnershipData.empty().award(unmanifested())
                .withGuardPoint(ASH_BURROWER, OVERWORLD, guardPoint)
                .withManifestation(ASH_BURROWER, uuid, OVERWORLD, manifestation);

        var encoded = EchoOwnershipData.CODEC.codec().encodeStart(JsonOps.INSTANCE, guarding).getOrThrow();
        EchoOwnershipData decoded = EchoOwnershipData.CODEC.codec().parse(JsonOps.INSTANCE, encoded).getOrThrow();
        EchoInstanceData active = decoded.find(ASH_BURROWER).orElseThrow();
        assertEquals(EchoContentCatalog.CommandMode.GUARD_POINT, active.commandMode());
        assertEquals(Optional.of(OVERWORLD), active.commandTargetDimension());
        assertEquals(Optional.of(guardPoint), active.commandTargetPos());
        assertEquals(Optional.of(manifestation), active.manifestationPos());

        EchoInstanceData dismissed = decoded.withoutManifestation(ASH_BURROWER).find(ASH_BURROWER).orElseThrow();
        assertEquals(EchoContentCatalog.CommandMode.GUARD_POINT, dismissed.commandMode());
        assertEquals(Optional.of(guardPoint), dismissed.commandTargetPos());
        assertTrue(dismissed.manifestationUuid().isEmpty());
    }

    @Test void leavingGuardPointClearsAnchorAndHalfTargetsFailClosed() {
        EchoInstanceData guarding = unmanifested().withGuardPoint(OVERWORLD, new BlockPos(2, 65, 7));
        EchoInstanceData following = guarding.withCommandMode(EchoContentCatalog.CommandMode.FOLLOW);
        assertEquals(EchoContentCatalog.CommandMode.FOLLOW, following.commandMode());
        assertTrue(following.commandTargetDimension().isEmpty());
        assertTrue(following.commandTargetPos().isEmpty());

        assertThrows(IllegalArgumentException.class, () -> new EchoInstanceData(
                ASH_BURROWER, "Ash Burrower", "test_award", "test",
                EchoContentCatalog.CommandMode.GUARD_POINT,
                Optional.of(OVERWORLD), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
    }

    @Test void targetlessStoredGuardCommandLoadsFailSafeAndCanBeRetargeted() {
        var encoded = EchoInstanceData.CODEC.codec().encodeStart(JsonOps.INSTANCE, unmanifested()).getOrThrow();
        JsonObject object = encoded.getAsJsonObject();
        object.addProperty("command_mode", "guard_point");
        EchoInstanceData decoded = EchoInstanceData.CODEC.codec().parse(JsonOps.INSTANCE, object).getOrThrow();
        assertEquals(EchoContentCatalog.CommandMode.GUARD_POINT, decoded.commandMode());
        assertTrue(decoded.commandTargetPos().isEmpty());

        BlockPos target = new BlockPos(-8, 67, 19);
        EchoInstanceData retargeted = decoded.withGuardPoint(OVERWORLD, target);
        assertEquals(Optional.of(OVERWORLD), retargeted.commandTargetDimension());
        assertEquals(Optional.of(target), retargeted.commandTargetPos());
    }

    @Test void awardAndMalformedStateFailClosed() {
        EchoInstanceData echo = unmanifested();
        EchoOwnershipData once = EchoOwnershipData.empty().award(echo);
        assertSame(once, once.award(echo));
        assertThrows(IllegalArgumentException.class, () -> once.award(new EchoInstanceData(
                ASH_BURROWER, "Different Echo", "test_award", "test", Optional.empty(), Optional.empty(), Optional.empty())));
        assertThrows(IllegalArgumentException.class, () -> new EchoInstanceData(
                ASH_BURROWER, "Ash Burrower", "test_award", "test",
                Optional.of("not-a-uuid"), Optional.of(OVERWORLD), Optional.of(BlockPos.ZERO.asLong())));
    }

    private static EchoInstanceData unmanifested() {
        return new EchoInstanceData(ASH_BURROWER, "Ash Burrower", "test_award", "test",
                Optional.empty(), Optional.empty(), Optional.empty());
    }
    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath("shadowslave", path); }
}
