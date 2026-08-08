package dev.spud.shadowslave.nightmare;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NightmareLoginRecoveryPolicyTest {
    @Test
    void successfulCompletionAlwaysPrecedesActiveOrTechnicalRecovery() {
        for (boolean active : new boolean[]{false, true}) {
            for (boolean inNightmare : new boolean[]{false, true}) {
                assertEquals(
                        NightmareLoginRecoveryPolicy.Action.SUCCESSFUL_COMPLETION,
                        NightmareLoginRecoveryPolicy.select(true, active, inNightmare)
                );
            }
        }
    }

    @Test
    void activeOwnershipWithoutReceiptUsesLocationToChooseRecovery() {
        assertEquals(
                NightmareLoginRecoveryPolicy.Action.ACTIVE_IN_NIGHTMARE,
                NightmareLoginRecoveryPolicy.select(false, true, true)
        );
        assertEquals(
                NightmareLoginRecoveryPolicy.Action.TECHNICAL_RECOVERY,
                NightmareLoginRecoveryPolicy.select(false, true, false)
        );
    }

    @Test
    void noReceiptAndNoActiveOwnershipRequiresNoNightmareRecovery() {
        assertEquals(
                NightmareLoginRecoveryPolicy.Action.NONE,
                NightmareLoginRecoveryPolicy.select(false, false, true)
        );
        assertEquals(
                NightmareLoginRecoveryPolicy.Action.NONE,
                NightmareLoginRecoveryPolicy.select(false, false, false)
        );
    }
}
