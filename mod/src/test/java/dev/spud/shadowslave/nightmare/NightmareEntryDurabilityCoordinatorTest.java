package dev.spud.shadowslave.nightmare;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    void preparedOwnershipPersistenceFailurePreventsPlayerMutation() {
        List<String> calls = new ArrayList<>();
        RuntimeException failure = new RuntimeException("ownership save failed");

        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                NightmareEntryDurabilityCoordinator.commit(new NightmareEntryDurabilityCoordinator.Operations() {
                    public void persistPreparedOwnership() {
                        calls.add("ownership");
                        throw failure;
                    }
                    public void applyPlayerEntry() { calls.add("entry"); }
                    public void persistCommittedPlayer() { calls.add("player-save"); }
                })
        );

        assertSame(failure, thrown);
        assertEquals(List.of("ownership"), calls);
    }

    @Test
    void playerEntryFailurePreventsCommittedPlayerSave() {
        List<String> calls = new ArrayList<>();
        RuntimeException failure = new RuntimeException("entry failed");

        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                NightmareEntryDurabilityCoordinator.commit(new NightmareEntryDurabilityCoordinator.Operations() {
                    public void persistPreparedOwnership() { calls.add("ownership"); }
                    public void applyPlayerEntry() {
                        calls.add("entry");
                        throw failure;
                    }
                    public void persistCommittedPlayer() { calls.add("player-save"); }
                })
        );

        assertSame(failure, thrown);
        assertEquals(List.of("ownership", "entry"), calls);
    }
}
