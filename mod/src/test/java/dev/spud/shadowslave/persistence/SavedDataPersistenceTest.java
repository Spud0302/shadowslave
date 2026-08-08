package dev.spud.shadowslave.persistence;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SavedDataPersistenceTest {
    @Test
    void saveAndWaitDoesNotReturnBeforeQueuedSavedDataWritesAreJoined() {
        List<String> calls = new ArrayList<>();

        SavedDataPersistence.saveAndWait(
                () -> calls.add("schedule_save"),
                () -> calls.add("await_io_worker")
        );

        assertEquals(List.of("schedule_save", "await_io_worker"), calls);
    }

    @Test
    void everyDurabilityCheckpointJoinsItsQueuedWriteBeforeTheNextCheckpointBegins() {
        List<String> calls = new ArrayList<>();

        SavedDataPersistence.saveAndWait(
                () -> calls.add("schedule_first"),
                () -> calls.add("await_first")
        );
        calls.add("between_checkpoints");
        SavedDataPersistence.saveAndWait(
                () -> calls.add("schedule_second"),
                () -> calls.add("await_second")
        );

        assertEquals(List.of(
                "schedule_first",
                "await_first",
                "between_checkpoints",
                "schedule_second",
                "await_second"
        ), calls);
    }
}
