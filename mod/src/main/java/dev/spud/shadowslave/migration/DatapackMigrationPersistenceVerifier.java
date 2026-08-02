package dev.spud.shadowslave.migration;

import dev.spud.shadowslave.soul.SoulData;

import java.util.Objects;
import java.util.Optional;

/** Pure equality and cross-reference checks used after each migration write. */
public final class DatapackMigrationPersistenceVerifier {
    private DatapackMigrationPersistenceVerifier() {
    }

    public static void verify(
            SoulData expectedSoul,
            ImportedIdentityData expectedIdentity,
            SoulData actualSoul,
            ImportedIdentityData actualIdentity
    ) {
        Objects.requireNonNull(expectedSoul, "expectedSoul");
        Objects.requireNonNull(expectedIdentity, "expectedIdentity");
        Objects.requireNonNull(actualSoul, "actualSoul");
        Objects.requireNonNull(actualIdentity, "actualIdentity");

        if (!expectedSoul.equals(actualSoul)) {
            throw new IllegalStateException("Persisted SoulData did not match the migration plan");
        }
        if (!expectedIdentity.equals(actualIdentity)) {
            throw new IllegalStateException("Persisted imported identity metadata did not match the migration plan");
        }

        if (actualIdentity.hasIdentity()) {
            ImportedAspect aspect = actualIdentity.aspect().orElseThrow();
            ImportedFlaw flaw = actualIdentity.flaw().orElseThrow();
            if (!Optional.of(aspect.instanceId()).equals(actualSoul.aspectId())) {
                throw new IllegalStateException("Persisted imported Aspect ID does not match SoulData");
            }
            if (!Optional.of(flaw.instanceId()).equals(actualSoul.flawId())) {
                throw new IllegalStateException("Persisted imported Flaw ID does not match SoulData");
            }
        } else if (actualSoul.aspectId().isPresent() || actualSoul.flawId().isPresent()) {
            throw new IllegalStateException("SoulData contains imported IDs without imported identity metadata");
        }
    }
}
