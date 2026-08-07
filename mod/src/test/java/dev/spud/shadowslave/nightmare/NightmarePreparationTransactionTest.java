package dev.spud.shadowslave.nightmare;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightmarePreparationTransactionTest {
    @Test
    void successfulPreparationDoesNotRunRollback() {
        AtomicBoolean rolledBack = new AtomicBoolean(false);

        String result = NightmarePreparationTransaction.run(
                () -> "prepared",
                () -> rolledBack.set(true)
        );

        assertEquals("prepared", result);
        assertFalse(rolledBack.get());
    }

    @Test
    void failedPreparationRunsRollbackAndRethrowsOriginalFailure() {
        AtomicBoolean rolledBack = new AtomicBoolean(false);
        IllegalStateException failure = new IllegalStateException("prepare failed");

        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> NightmarePreparationTransaction.run(
                        () -> {
                            throw failure;
                        },
                        () -> rolledBack.set(true)
                )
        );

        assertSame(failure, thrown);
        assertTrue(rolledBack.get());
    }

    @Test
    void rollbackFailureIsSuppressedWithoutReplacingPreparationFailure() {
        IllegalStateException failure = new IllegalStateException("prepare failed");
        IllegalArgumentException rollbackFailure = new IllegalArgumentException("rollback failed");

        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> NightmarePreparationTransaction.run(
                        () -> {
                            throw failure;
                        },
                        () -> {
                            throw rollbackFailure;
                        }
                )
        );

        assertSame(failure, thrown);
        assertEquals(1, thrown.getSuppressed().length);
        assertSame(rollbackFailure, thrown.getSuppressed()[0]);
    }

    @Test
    void rollbackAllContinuesAfterAnEarlierCleanupStepFails() {
        AtomicInteger stepsRun = new AtomicInteger();
        IllegalStateException firstFailure = new IllegalStateException("discard failed");
        IllegalArgumentException secondFailure = new IllegalArgumentException("clear failed");

        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> NightmarePreparationTransaction.rollbackAll(
                        () -> {
                            stepsRun.incrementAndGet();
                            throw firstFailure;
                        },
                        () -> {
                            stepsRun.incrementAndGet();
                            throw secondFailure;
                        },
                        stepsRun::incrementAndGet
                )
        );

        assertEquals(3, stepsRun.get());
        assertSame(firstFailure, thrown);
        assertEquals(1, thrown.getSuppressed().length);
        assertSame(secondFailure, thrown.getSuppressed()[0]);
    }
}
