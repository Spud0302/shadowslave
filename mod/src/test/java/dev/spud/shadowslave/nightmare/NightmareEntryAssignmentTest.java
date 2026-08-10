package dev.spud.shadowslave.nightmare;

import dev.spud.shadowslave.nightmare.content.DrownedBellScenarioDefinition;
import dev.spud.shadowslave.nightmare.content.NightmareRoleScenarioCompatibilityCatalog;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightmareEntryAssignmentTest {
    private static final UUID PLAYER = UUID.fromString("8f7b0da4-4a83-4a79-8f25-9dbe26304927");

    @Test
    void playableAssignmentUsesImplementedScenarioAndCompatibleAuthoredRole() {
        for (long gameTime = 0; gameTime < 512; gameTime++) {
            NightmareEntryAssignment.Assignment assignment = NightmareEntryAssignment.resolveFirstNightmare(PLAYER, gameTime);
            assertTrue(Set.of(LastSignalScenario.SCENARIO_ID, DrownedBellScenarioDefinition.SCENARIO_ID)
                    .contains(assignment.scenarioId()));
            assertEquals(assignment.roleMatch().role().id(), assignment.historicalRoleId());
            assertEquals(assignment.scenarioId(), assignment.roleMatch().scenarioId());

            Set<String> compatibleRoleIds = NightmareRoleScenarioCompatibilityCatalog.waveOne().stream()
                    .filter(module -> module.scenarioId().equals(assignment.scenarioId()))
                    .findFirst()
                    .orElseThrow()
                    .variants().stream()
                    .map(NightmareRoleScenarioCompatibilityCatalog.RoleVariant::roleId)
                    .collect(java.util.stream.Collectors.toSet());
            assertTrue(compatibleRoleIds.contains(assignment.historicalRoleId()));
        }
    }

    @Test
    void sameEntryEvidenceResolvesExactlyTheSameAssignment() {
        NightmareEntryAssignment.Assignment first = NightmareEntryAssignment.resolveFirstNightmare(PLAYER, 8123L);
        NightmareEntryAssignment.Assignment second = NightmareEntryAssignment.resolveFirstNightmare(PLAYER, 8123L);

        assertEquals(first, second);
    }

    @Test
    void freshEntriesReachBothPlayableScenariosAndMultipleRoles() {
        Set<String> scenarios = new HashSet<>();
        Set<String> roles = new HashSet<>();
        for (long gameTime = 0; gameTime < 2048; gameTime++) {
            NightmareEntryAssignment.Assignment assignment = NightmareEntryAssignment.resolveFirstNightmare(PLAYER, gameTime);
            scenarios.add(assignment.scenarioId());
            roles.add(assignment.historicalRoleId());
        }

        assertEquals(Set.of(LastSignalScenario.SCENARIO_ID, DrownedBellScenarioDefinition.SCENARIO_ID), scenarios);
        assertTrue(roles.size() >= 6, "fresh entry seeds should expose authored role variety: " + roles);
    }

    @Test
    void persistedInstanceRoundTripKeepsResolvedScenarioRoleAndGraphState() {
        NightmareEntryAssignment.Assignment assignment = NightmareEntryAssignment.resolveFirstNightmare(PLAYER, 99L);
        NightmareInstance instance = new NightmareInstance(
                UUID.fromString("c2ee7085-ea47-4ebd-8189-8d68fa22c872"),
                PLAYER,
                3,
                assignment.scenarioId(),
                assignment.historicalRoleId(),
                net.minecraft.resources.ResourceLocation.parse("minecraft:overworld"),
                1.0,
                64.0,
                -2.0,
                90.0F,
                0.0F,
                net.minecraft.core.BlockPos.ZERO,
                net.minecraft.core.BlockPos.ZERO,
                java.util.Optional.empty(),
                99L
        ).withResolutionState("warning_ready", java.util.Optional.empty());

        NightmareInstance restored = NightmareInstance.load(instance.save());
        assertEquals(assignment.scenarioId(), restored.scenarioId());
        assertEquals(assignment.historicalRoleId(), restored.historicalRoleId());
        assertEquals(java.util.Optional.of("warning_ready"), restored.resolutionStateId());
        assertEquals(java.util.Optional.empty(), restored.terminalResolutionId());
    }
}
