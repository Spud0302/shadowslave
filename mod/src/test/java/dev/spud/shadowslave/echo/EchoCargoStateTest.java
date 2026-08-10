package dev.spud.shadowslave.echo;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.spud.shadowslave.echo.content.EchoContentCatalog;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EchoCargoStateTest {
    private static final ResourceLocation ASH_BURROWER = id("echo/ash_burrower");
    private static final ResourceLocation COBBLESTONE = ResourceLocation.fromNamespaceAndPath("minecraft", "cobblestone");

    @Test void cargoRoundTripPersistsExactPlainStackIdentityAndCount() {
        EchoOwnershipData carrying = EchoOwnershipData.empty().award(unmanifested())
                .withCargo(ASH_BURROWER, COBBLESTONE, 32);
        var encoded = EchoOwnershipData.CODEC.codec().encodeStart(JsonOps.INSTANCE, carrying).getOrThrow();
        EchoOwnershipData decoded = EchoOwnershipData.CODEC.codec().parse(JsonOps.INSTANCE, encoded).getOrThrow();
        EchoInstanceData echo = decoded.find(ASH_BURROWER).orElseThrow();
        assertEquals(EchoContentCatalog.CommandMode.CARRY, echo.commandMode());
        assertEquals(Optional.of(COBBLESTONE), echo.cargoItemId());
        assertEquals(Optional.of(32), echo.cargoCount());
    }

    @Test void dismissalAndMovementCommandsDoNotDeleteCargo() {
        EchoInstanceData carrying = unmanifested().withCargo(COBBLESTONE, 16);
        EchoInstanceData following = carrying.withCommandMode(EchoContentCatalog.CommandMode.FOLLOW);
        assertEquals(Optional.of(COBBLESTONE), following.cargoItemId());
        assertEquals(Optional.of(16), following.cargoCount());
        assertSame(following, following.withoutManifestation());
    }

    @Test void unloadingClearsCargoAndLeavesCarryModeSafelyAtHold() {
        EchoInstanceData unloaded = unmanifested().withCargo(COBBLESTONE, 8).withoutCargo();
        assertEquals(EchoContentCatalog.CommandMode.HOLD, unloaded.commandMode());
        assertTrue(unloaded.cargoItemId().isEmpty());
        assertTrue(unloaded.cargoCount().isEmpty());
    }

    @Test void contradictoryCargoShapesAndCountsFailClosed() {
        assertThrows(IllegalArgumentException.class, () -> new EchoInstanceData(
                ASH_BURROWER, "Ash Burrower", "test_award", "test", EchoContentCatalog.CommandMode.CARRY,
                Optional.empty(), Optional.empty(), Optional.of(COBBLESTONE), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty()));
        assertThrows(IllegalArgumentException.class, () -> unmanifested().withCargo(COBBLESTONE, 0));
        assertThrows(IllegalArgumentException.class, () -> unmanifested().withCargo(COBBLESTONE, 100));
        assertThrows(IllegalStateException.class, () -> unmanifested().withCargo(COBBLESTONE, 1).withCargo(COBBLESTONE, 1));
    }

    @Test void olderStoredEchoWithoutCargoFieldsLoadsEmpty() {
        var encoded = EchoInstanceData.CODEC.codec().encodeStart(JsonOps.INSTANCE, unmanifested()).getOrThrow();
        JsonObject object = encoded.getAsJsonObject();
        object.remove("cargo_item_id");
        object.remove("cargo_count");
        EchoInstanceData decoded = EchoInstanceData.CODEC.codec().parse(JsonOps.INSTANCE, object).getOrThrow();
        assertTrue(decoded.cargoItemId().isEmpty());
        assertTrue(decoded.cargoCount().isEmpty());
    }

    private static EchoInstanceData unmanifested() {
        return new EchoInstanceData(ASH_BURROWER, "Ash Burrower", "test_award", "test",
                Optional.empty(), Optional.empty(), Optional.empty());
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("shadowslave", path);
    }
}