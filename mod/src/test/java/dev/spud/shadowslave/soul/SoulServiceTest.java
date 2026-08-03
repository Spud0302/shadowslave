package dev.spud.shadowslave.soul;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SoulServiceTest {
    @Test
    void ordinaryReplacementWritesThenSynchronizesExactlyOnce() {
        FakeOperations operations = new FakeOperations();
        SoulData next = SoulTransitions.infect(SoulData.uninfected());

        SoulData result = SoulService.replace(operations, next);

        assertEquals(next, result);
        assertEquals(next, operations.written);
        assertEquals(next, operations.synced);
        assertEquals(List.of("write", "sync"), operations.calls);
    }

    private static final class FakeOperations implements SoulService.Operations {
        private final List<String> calls = new ArrayList<>();
        private SoulData written;
        private SoulData synced;

        @Override
        public void write(SoulData next) {
            calls.add("write");
            written = next;
        }

        @Override
        public void sync(SoulData next) {
            calls.add("sync");
            synced = next;
        }
    }
}
