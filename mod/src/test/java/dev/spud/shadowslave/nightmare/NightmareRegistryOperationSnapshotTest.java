package dev.spud.shadowslave.nightmare;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NightmareRegistryOperationSnapshotTest {
    @Test
    void completionPhaseCannotAdvanceFromModifiedSnapshotWithMatchingIdentity() {
        NightmareInstance active = baseInstance();
        NightmareRegistryData registry = new NightmareRegistryData();
        registry.restore(active);
        NightmareCompletionRecord terminal = registry.beginSuccessfulCompletion(active, 100L);
        NightmareInstance modified = active.withPursuer(UUID.randomUUID());

        assertThrows(
                IllegalStateException.class,
                () -> registry.advanceSuccessfulCompletion(modified, NightmareCompletionPhase.APPRAISAL_COMMITTED)
        );
        assertEquals(terminal, registry.findSuccessfulCompletionByPlayer(active.playerId()).orElseThrow());
        assertEquals(active, registry.findByPlayer(active.playerId()).orElseThrow());
    }

    @Test
    void teardownCannotConsumeOwnershipFromModifiedSnapshotWithMatchingInstanceId() {
        NightmareInstance active = baseInstance();
        NightmareRegistryData registry = new NightmareRegistryData();
        registry.restore(active);
        NightmareInstance modified = active.withPursuer(UUID.randomUUID());

        assertThrows(IllegalStateException.class, () -> registry.remove(modified));
        assertEquals(active, registry.findByPlayer(active.playerId()).orElseThrow());
    }

    @Test
    void publicApiDoesNotExposePlayerOnlyTeardownBypass() {
        boolean exposesPlayerOnlyRemoval = Arrays.stream(NightmareRegistryData.class.getDeclaredMethods())
                .filter(method -> method.getName().equals("removeByPlayer"))
                .anyMatch(method -> Modifier.isPublic(method.getModifiers()));

        assertFalse(
                exposesPlayerOnlyRemoval,
                "teardown must remain scoped to an authoritative NightmareInstance snapshot"
        );
    }

    private static NightmareInstance baseInstance() {
        UUID instanceId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        BlockPos origin = LastSignalScenario.originForSlot(3);
        return new NightmareInstance(
                instanceId,
                playerId,
                3,
                LastSignalScenario.SCENARIO_ID,
                LastSignalScenario.ROLE_ID,
                ResourceLocation.parse("minecraft:overworld"),
                10.0,
                70.0,
                4.5,
                90.0F,
                10.0F,
                origin,
                LastSignalScenario.altarForOrigin(origin),
                Optional.of(UUID.randomUUID()),
                903L
        );
    }
}
