package dev.spud.shadowslave.nightmare;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NightmareCompletionRecoveryPlanTest {
    @Test
    void terminalReceiptReplaysEveryMissingAction() {
        assertEquals(
                new NightmareCompletionRecoveryPlan(true, true, true),
                NightmareCompletionRecoveryPlan.forState(
                        NightmareCompletionPhase.TERMINAL_RESOLUTION_RECORDED,
                        false,
                        true,
                        true
                )
        );
    }

    @Test
    void playerSaveAheadOfReceiptDoesNotDuplicateAppraisalOrReturn() {
        assertEquals(
                new NightmareCompletionRecoveryPlan(false, false, true),
                NightmareCompletionRecoveryPlan.forState(
                        NightmareCompletionPhase.TERMINAL_RESOLUTION_RECORDED,
                        true,
                        false,
                        true
                )
        );
    }

    @Test
    void receiptAheadOfPlayerSaveReappliesAppraisalAndReturn() {
        assertEquals(
                new NightmareCompletionRecoveryPlan(true, true, true),
                NightmareCompletionRecoveryPlan.forState(
                        NightmareCompletionPhase.APPRAISAL_COMMITTED,
                        false,
                        true,
                        true
                )
        );
    }

    @Test
    void returnedPlayerStillNeedsActiveOwnershipTeardown() {
        assertEquals(
                new NightmareCompletionRecoveryPlan(false, false, true),
                NightmareCompletionRecoveryPlan.forState(
                        NightmareCompletionPhase.RETURN_COMMITTED,
                        true,
                        false,
                        true
                )
        );
    }

    @Test
    void teardownReceiptRepairsOnlyTheStateThatIsActuallyStale() {
        assertEquals(
                new NightmareCompletionRecoveryPlan(true, false, false),
                NightmareCompletionRecoveryPlan.forState(
                        NightmareCompletionPhase.TEARDOWN_COMMITTED,
                        false,
                        false,
                        false
                )
        );
        assertEquals(
                new NightmareCompletionRecoveryPlan(false, true, false),
                NightmareCompletionRecoveryPlan.forState(
                        NightmareCompletionPhase.TEARDOWN_COMMITTED,
                        true,
                        true,
                        false
                )
        );
        assertEquals(
                new NightmareCompletionRecoveryPlan(false, false, true),
                NightmareCompletionRecoveryPlan.forState(
                        NightmareCompletionPhase.TEARDOWN_COMMITTED,
                        true,
                        false,
                        true
                )
        );
    }

    @Test
    void fullyCommittedAndConsistentStateRequiresNoReplay() {
        assertEquals(
                new NightmareCompletionRecoveryPlan(false, false, false),
                NightmareCompletionRecoveryPlan.forState(
                        NightmareCompletionPhase.TEARDOWN_COMMITTED,
                        true,
                        false,
                        false
                )
        );
    }
}
