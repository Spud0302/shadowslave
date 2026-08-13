package dev.spud.shadowslave.nightmare;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class NightmareEntryDurabilityCoordinatorTest {
    @Test
    void ordersOwnershipThenPlayerMutationThenPlayerSave() {
        List<String> calls = new ArrayList<>();
        NightmareEntryDurabilityCoordinator.commit(new NightmareEntryDurabilityCoordinator.Operations() {
            public void persistPreparedOwnership() { calls.add("ownership"); }
            public void applyPlayerEntry() { calls.add("entry"); }
            public void persistCommittedPlayer() { calls.add("player-save"); }
        });
        assertEquals(List.of("ownership", "entry", "player-save"), calls);
    }
}
