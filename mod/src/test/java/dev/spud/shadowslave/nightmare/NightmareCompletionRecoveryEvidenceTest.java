package dev.spud.shadowslave.nightmare;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightmareCompletionRecoveryEvidenceTest {
    @Test
    void recoveredRequiresAppliedAppraisalNoActiveOwnershipAndOutsideNightmare() {
        UUID nightmare = UUID.fromString("11111111-2222-3333-4444-555555555555");
        UUID player = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");

        NightmareCompletionRecoveryEvidence recovered = new NightmareCompletionRecoveryEvidence(
                nightmare, player, true, false, false
        );
        assertTrue(recovered.recovered());
        assertEquals(
                "COMPLETION RECOVERY EVIDENCE nightmare=" + nightmare
                        + " player_uuid=" + player
                        + " appraisal_applied=true active_present=false in_nightmare=false",
                recovered.logMarker()
        );

        assertFalse(new NightmareCompletionRecoveryEvidence(nightmare, player, false, false, false).recovered());
        assertFalse(new NightmareCompletionRecoveryEvidence(nightmare, player, true, true, false).recovered());
        assertFalse(new NightmareCompletionRecoveryEvidence(nightmare, player, true, false, true).recovered());
    }
}
