package dev.spud.shadowslave.nightmare;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class NightmareEntryDurabilityCoordinatorTest {
    @Test
    void preparedOwnershipBecomesDurableBeforeAnyPlayerMutation() {
        List<String> calls = new ArrayList<>();

        NightmareEntryDurabilityCoordinator.commit(new NightmareEntryDurabilityCoordinator.Operations() {
            @Override
            public void persistPreparedOwnership() {
                calls.add("registry");
            }

            @Override
            public void applyPlayerEntry() {
                calls.add("player-entry");
            }

            @Override
            public void persistCommittedPlayer() {
                calls.add("player-save");
            }
        });

        assertEquals(List.of("registry", "player-entry", "player-save"), calls);
    }

    @Test
    void ownershipPersistenceFailurePreventsPlayerMutation() {
        List<String> calls = new ArrayList<>();
        RuntimeException failure = new RuntimeException("registry persistence failed");

        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                NightmareEntryDurabilityCoordinator.commit(new NightmareEntryDurabilityCoordinator.Operations() {
                    @Override
                    public void persistPreparedOwnership() {
                        calls.add("registry");
                        throw failure;
                    }

                    @Override
                    public void applyPlayerEntry() {
                        calls.add("player-entry");
                    }

                    @Override
                    public void persistCommittedPlayer() {
                        calls.add("player-save");
                    }
                })
        );

        assertSame(failure, thrown);
        assertEquals(List.of("registry"), calls);
    }

    @Test
    void failedPlayerEntryNeverPublishesACommittedPlayerSave() {
        List<String> calls = new ArrayList<>();
        RuntimeException failure = new RuntimeException("entry failed");

        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                NightmareEntryDurabilityCoordinator.commit(new NightmareEntryDurabilityCoordinator.Operations() {
                    @Override
                    public void persistPreparedOwnership() {
                        calls.add("registry");
                    }

                    @Override
                    public void applyPlayerEntry() {
                        calls.add("player-entry");
                        throw failure;
                    }

                    @Override
                    public void persistCommittedPlayer() {
                        calls.add("player-save");
                    }
                })
        );

        assertSame(failure, thrown);
        assertEquals(List.of("registry", "player-entry"), calls);
    }

    @Test
    void committedPlayerPersistenceFailureIsSurfacedAfterEntryMutation() {
        List<String> calls = new ArrayList<>();
        RuntimeException failure = new RuntimeException("player persistence failed");

        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                NightmareEntryDurabilityCoordinator.commit(new NightmareEntryDurabilityCoordinator.Operations() {
                    @Override
                    public void persistPreparedOwnership() {
                        calls.add("registry");
                    }

                    @Override
                    public void applyPlayerEntry() {
                        calls.add("player-entry");
                    }

                    @Override
                    public void persistCommittedPlayer() {
                        calls.add("player-save");
                        throw failure;
                    }
                })
        );

        assertSame(failure, thrown);
        assertEquals(List.of("registry", "player-entry", "player-save"), calls);
    }
}
