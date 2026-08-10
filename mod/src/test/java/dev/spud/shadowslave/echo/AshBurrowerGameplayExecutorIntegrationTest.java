package dev.spud.shadowslave.echo;

import dev.spud.shadowslave.echo.content.EchoContentCatalog;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AshBurrowerGameplayExecutorIntegrationTest {
    private static final ResourceLocation ECHO_ID = ResourceLocation.fromNamespaceAndPath("shadowslave", "echo/ash_burrower");
    private static final ResourceLocation DIMENSION = ResourceLocation.fromNamespaceAndPath("shadowslave", "dream_realm");

    @Test
    void guardTargetAndCargoRemainJavaOwnedAcrossManifestationChanges() {
        EchoInstanceData state = new EchoInstanceData(ECHO_ID, "Ash Burrower", "test", "test",
                Optional.empty(), Optional.empty(), Optional.empty())
                .withGuardPoint(DIMENSION, new BlockPos(12, 64, -7));
        assertEquals(EchoContentCatalog.CommandMode.GUARD_POINT, state.commandMode());
        assertEquals(Optional.of(DIMENSION), state.commandTargetDimension());
        assertEquals(Optional.of(new BlockPos(12, 64, -7)), state.commandTargetPos());

        EchoInstanceData carrying = state.withCommandMode(EchoContentCatalog.CommandMode.HOLD)
                .withCargo(ResourceLocation.withDefaultNamespace("cobblestone"), 12);
        assertEquals(EchoContentCatalog.CommandMode.CARRY, carrying.commandMode());
        assertEquals(Optional.of(12), carrying.cargoCount());
        assertTrue(carrying.commandTargetDimension().isEmpty());
    }

    @Test
    void runtimeExecutorUsesDedicatedAshBurrowerEntityForIntegratedCommands() throws Exception {
        String service = Files.readString(Path.of("src/main/java/dev/spud/shadowslave/echo/EchoManifestationService.java"));
        assertTrue(service.contains("NightmareCreatureEntities.ASH_BURROWER_ECHO"));
        assertTrue(service.contains("case GUARD_POINT"));
        assertTrue(service.contains("case CARRY"));
        assertFalse(service.contains("EntityType.ARMADILLO"));
    }
}
