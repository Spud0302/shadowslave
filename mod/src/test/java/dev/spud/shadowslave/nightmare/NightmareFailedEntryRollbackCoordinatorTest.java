package dev.spud.shadowslave.nightmare;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NightmareFailedEntryRollbackCoordinatorTest {
    @Test
    void restoresSoulOnlyAfterAuthoritativeRollbackSucceeds() {
        List<String> calls = new ArrayList<>();

        NightmareFailedEntryRollbackCoordinator.rollback(new NightmareFailedEntryRollbackCoordinator.Operations() {
            @Override
            public void rollbackAuthoritativeState() {
                calls.add("rollback-authority");
            }

            @Override
            public void restoreSoul() {
                calls.add("restore-soul");
            }
        });

        assertEquals(List.of("rollback-authority", "restore-soul"), calls);
    }

    @Test
    void authoritativeRollbackFailurePreventsSoulRestore() {
        List<String> calls = new ArrayList<>();
        IllegalStateException retainedAuthority = new IllegalStateException("retained technical exit blocks removal");

        IllegalStateException failure = assertThrows(
                IllegalStateException.class,
                () -> NightmareFailedEntryRollbackCoordinator.rollback(
                        new NightmareFailedEntryRollbackCoordinator.Operations() {
                            @Override
                            public void rollbackAuthoritativeState() {
                                calls.add("rollback-authority");
                                throw retainedAuthority;
                            }

                            @Override
                            public void restoreSoul() {
                                calls.add("restore-soul");
                            }
                        }
                )
        );

        assertSame(retainedAuthority, failure);
        assertEquals(List.of("rollback-authority"), calls,
                "Carrier restoration must not run after authoritative rollback fails");
    }
}
