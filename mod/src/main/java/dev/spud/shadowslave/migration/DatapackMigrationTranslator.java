package dev.spud.shadowslave.migration;

import dev.spud.shadowslave.soul.SoulData;
import dev.spud.shadowslave.soul.SoulTransitions;

import java.util.Optional;

/**
 * Pure fail-safe translator from frozen datapack evidence into Java records.
 * It never reads a player, writes an attachment or clears a legacy value.
 */
public final class DatapackMigrationTranslator {
    public static final int CURRENT_MIGRATION = 1;

    private DatapackMigrationTranslator() {
    }

    public static Optional<DatapackMigrationPlan> translate(LegacyDatapackSnapshot snapshot) {
        if (snapshot.completedMigrationVersion() >= CURRENT_MIGRATION) {
            return Optional.empty();
        }
        if (!snapshot.hasAnyLegacyState()) {
            return Optional.empty();
        }
        if (snapshot.activeNightmare()) {
            throw new IllegalStateException(
                    "Datapack player is still marked inside a Nightmare; recover or defer before migration"
            );
        }

        if (snapshot.rankScore() == 0) {
            if (snapshot.aspectScore() != 0 || snapshot.flawScore() != 0) {
                throw new IllegalArgumentException("Incomplete datapack player has orphaned Aspect/Flaw scores");
            }
            if (!snapshot.carrier()) {
                return Optional.empty();
            }

            SoulData carrier = SoulTransitions.infect(SoulData.uninfected())
                    .markImported(CURRENT_MIGRATION);
            return Optional.of(new DatapackMigrationPlan(carrier, Optional.empty(), Optional.empty()));
        }

        if (snapshot.rankScore() != 1) {
            throw new IllegalArgumentException("Unsupported frozen datapack rank score: " + snapshot.rankScore());
        }
        if (snapshot.aspectScore() == 0 || snapshot.flawScore() == 0) {
            throw new IllegalArgumentException("Completed datapack player is missing Aspect or Flaw identity");
        }

        ImportedAspect aspect = DatapackIdentityMappings.aspect(snapshot);
        ImportedFlaw flaw = DatapackIdentityMappings.flaw(snapshot);
        SoulData dreamer = SoulTransitions.completeFirstNightmare(
                SoulTransitions.beginFirstNightmare(
                        SoulTransitions.infect(SoulData.uninfected())
                ),
                aspect.instanceId(),
                aspect.aspectRank(),
                flaw.instanceId()
        ).markImported(CURRENT_MIGRATION);

        return Optional.of(new DatapackMigrationPlan(
                dreamer,
                Optional.of(aspect),
                Optional.of(flaw)
        ));
    }
}
