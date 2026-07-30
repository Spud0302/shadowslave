package dev.spud.shadowslave.migration;

import dev.spud.shadowslave.soul.SoulData;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.soul.SpellState;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;
import java.util.Optional;

/**
 * Live migration transaction. Source datapack state is read-only. Java state is
 * written provisionally, read back and verified, then the migration marker is
 * committed. Any failure restores the pre-attempt Java attachments.
 */
public final class DatapackMigrationService {
    private DatapackMigrationService() {
    }

    public static DatapackMigrationOutcome migrate(ServerPlayer player) {
        Objects.requireNonNull(player, "player");

        SoulData beforeSoul = SoulService.get(player);
        ImportedIdentityData beforeIdentity = ImportedIdentityService.get(player);

        if (beforeSoul.importedFromDatapack()
                && beforeSoul.migrationVersion() >= DatapackMigrationTranslator.CURRENT_MIGRATION) {
            return new DatapackMigrationOutcome(
                    DatapackMigrationOutcome.Status.ALREADY_MIGRATED,
                    "Java migration version " + beforeSoul.migrationVersion() + " is already complete"
            );
        }
        if (beforeSoul.spellState() != SpellState.UNINFECTED
                || beforeSoul.importedFromDatapack()
                || beforeIdentity.hasIdentity()) {
            throw new IllegalStateException(
                    "Refusing to overwrite established Java Soul or identity state with datapack evidence"
            );
        }

        LegacyDatapackSnapshot snapshot = LegacyDatapackReader.read(player);
        Optional<DatapackMigrationPlan> optionalPlan = DatapackMigrationTranslator.translate(snapshot);
        if (optionalPlan.isEmpty()) {
            return new DatapackMigrationOutcome(
                    DatapackMigrationOutcome.Status.NO_LEGACY_STATE,
                    "No supported frozen-datapack identity was found"
            );
        }

        DatapackMigrationPlan plan = optionalPlan.orElseThrow();
        ImportedIdentityData plannedIdentity = ImportedIdentityData.fromPlan(plan);
        SoulData provisionalSoul = withoutMigrationMarker(plan.soulData());

        try {
            ImportedIdentityService.replace(player, plannedIdentity);
            SoulService.replace(player, provisionalSoul);
            DatapackMigrationPersistenceVerifier.verify(
                    provisionalSoul,
                    plannedIdentity,
                    SoulService.get(player),
                    ImportedIdentityService.get(player)
            );

            SoulService.replace(player, plan.soulData());
            DatapackMigrationPersistenceVerifier.verify(
                    plan.soulData(),
                    plannedIdentity,
                    SoulService.get(player),
                    ImportedIdentityService.get(player)
            );
        } catch (RuntimeException exception) {
            ImportedIdentityService.replace(player, beforeIdentity);
            SoulService.replace(player, beforeSoul);
            throw new IllegalStateException("Datapack migration failed; Java state was rolled back", exception);
        }

        if (plan.soulData().spellState() == SpellState.CARRIER) {
            return new DatapackMigrationOutcome(
                    DatapackMigrationOutcome.Status.MIGRATED_CARRIER,
                    "Imported Carrier state; legacy tags and scores were retained"
            );
        }
        return new DatapackMigrationOutcome(
                DatapackMigrationOutcome.Status.MIGRATED_DREAMER,
                "Imported Dreamer identity; legacy tags and scores were retained"
        );
    }

    private static SoulData withoutMigrationMarker(SoulData soul) {
        return new SoulData(
                soul.schemaVersion(),
                soul.spellState(),
                soul.awakeningPath(),
                soul.soulRank(),
                soul.aspectId(),
                soul.aspectRank(),
                soul.flawId(),
                false,
                SoulData.NO_MIGRATION
        );
    }
}
