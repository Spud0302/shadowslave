package dev.spud.shadowslave.nightmare;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NightmareStatusReportTest {
    @Test
    void reportsCleanCommittedReceiptWithoutActiveOwnership() {
        NightmareInstance instance = instance(UUID.randomUUID(), UUID.randomUUID(), 2);
        NightmareCompletionRecord receipt = new NightmareCompletionRecord(
                instance,
                NightmareCompletionPhase.TEARDOWN_COMMITTED,
                1200L
        );

        NightmareStatusReport report = NightmareStatusReport.from(Optional.empty(), Optional.of(receipt));

        assertTrue(report.lines().contains("No active Nightmare instance."));
        assertTrue(report.lines().contains("Completion receipt: teardown_committed"));
        assertTrue(report.lines().contains("Ownership consistency: teardown committed; active ownership absent"));
    }

    @Test
    void reportsPendingRecoveryWhenReceiptExistsBeforeTeardown() {
        NightmareInstance instance = instance(UUID.randomUUID(), UUID.randomUUID(), 1);
        NightmareCompletionRecord receipt = new NightmareCompletionRecord(
                instance,
                NightmareCompletionPhase.APPRAISAL_COMMITTED,
                800L
        );

        NightmareStatusReport report = NightmareStatusReport.from(Optional.empty(), Optional.of(receipt));

        assertTrue(report.lines().contains("Ownership consistency: recovery pending; active ownership absent"));
    }

    @Test
    void reportsMatchingAndConflictingActiveOwnership() {
        UUID playerId = UUID.randomUUID();
        NightmareInstance completed = instance(UUID.randomUUID(), playerId, 0);
        NightmareCompletionRecord receipt = new NightmareCompletionRecord(
                completed,
                NightmareCompletionPhase.RETURN_COMMITTED,
                900L
        );

        NightmareStatusReport matching = NightmareStatusReport.from(
                Optional.of(completed),
                Optional.of(receipt)
        );
        assertTrue(matching.lines().contains(
                "Ownership consistency: active ownership matches retained receipt"
        ));

        NightmareInstance different = instance(UUID.randomUUID(), playerId, 3);
        NightmareStatusReport conflict = NightmareStatusReport.from(
                Optional.of(different),
                Optional.of(receipt)
        );
        assertTrue(conflict.lines().contains(
                "Ownership consistency: CONFLICT: active ownership belongs to another instance"
        ));
    }

    private static NightmareInstance instance(UUID instanceId, UUID playerId, int slot) {
        BlockPos origin = LastSignalScenario.originForSlot(slot);
        return new NightmareInstance(
                instanceId,
                playerId,
                slot,
                LastSignalScenario.SCENARIO_ID,
                LastSignalScenario.ROLE_ID,
                ResourceLocation.parse("minecraft:overworld"),
                1.5,
                70.0,
                2.5,
                0.0F,
                0.0F,
                origin,
                LastSignalScenario.altarForOrigin(origin),
                Optional.empty(),
                500L
        );
    }
}
