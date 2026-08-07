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
}
