package dev.spud.shadowslave.nightmare;

import java.util.Objects;
import java.util.function.Supplier;

/** Small fail-closed transaction helper for scenario preparation side effects. */
final class NightmarePreparationTransaction {
    private NightmarePreparationTransaction() {
    }

    static <T> T run(Supplier<T> preparation, Runnable rollback) {
        Supplier<T> checkedPreparation = Objects.requireNonNull(preparation, "preparation");
        Runnable checkedRollback = Objects.requireNonNull(rollback, "rollback");
        try {
            return checkedPreparation.get();
        } catch (RuntimeException failure) {
            try {
                checkedRollback.run();
            } catch (RuntimeException rollbackFailure) {
                failure.addSuppressed(rollbackFailure);
            }
            throw failure;
        }
    }

    static void rollbackAll(Runnable... steps) {
        RuntimeException firstFailure = null;
        for (Runnable step : Objects.requireNonNull(steps, "steps")) {
            Runnable checkedStep = Objects.requireNonNull(step, "rollback step");
            try {
                checkedStep.run();
            } catch (RuntimeException failure) {
                if (firstFailure == null) {
                    firstFailure = failure;
                } else {
                    firstFailure.addSuppressed(failure);
                }
            }
        }
        if (firstFailure != null) {
            throw firstFailure;
        }
    }
}
