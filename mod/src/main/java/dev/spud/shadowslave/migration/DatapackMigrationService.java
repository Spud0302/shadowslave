package dev.spud.shadowslave.migration;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.soul.SoulData;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.soul.SpellState;
import dev.spud.shadowslave.soul.identity.AspectInstanceData;
import dev.spud.shadowslave.soul.identity.FlawInstanceData;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import dev.spud.shadowslave.soul.identity.SoulIdentityService;
import net.minecraft.resources.ResourceLocation;
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
        SoulIdentityData beforeIdentity = SoulIdentityService.get(player);
        ImportedIdentityData beforeImportedIdentity = ImportedIdentityService.get(player);

        if (beforeSoul.importedFromDatapack()
                && beforeSoul.migrationVersion() >= DatapackMigrationTranslator.CURRENT_MIGRATION) {
            return new DatapackMigrationOutcome(
                    DatapackMigrationOutcome.Status.ALREADY_MIGRATED,
                    "Java migration version " + beforeSoul.migrationVersion() + " is already complete"
            );
        }
        if (beforeSoul.spellState() != SpellState.UNINFECTED
                || beforeSoul.importedFromDatapack()
                || beforeIdentity.isRevealed()
                || beforeImportedIdentity.hasIdentity()) {
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
        ImportedIdentityData plannedImportedIdentity = ImportedIdentityData.fromPlan(plan);
        SoulIdentityData plannedIdentity = toSoulIdentity(plannedImportedIdentity);
        SoulData provisionalSoul = withoutMigrationMarker(plan.soulData());

        try {
            ImportedIdentityService.replace(player, plannedImportedIdentity);
            SoulIdentityService.replace(player, plannedIdentity);
            SoulService.replace(player, provisionalSoul);
            verifyAll(player, provisionalSoul, plannedIdentity, plannedImportedIdentity);

            SoulService.replace(player, plan.soulData());
            verifyAll(player, plan.soulData(), plannedIdentity, plannedImportedIdentity);
        } catch (RuntimeException exception) {
            ImportedIdentityService.replace(player, beforeImportedIdentity);
            SoulIdentityService.replace(player, beforeIdentity);
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

    private static void verifyAll(
            ServerPlayer player,
            SoulData expectedSoul,
            SoulIdentityData expectedIdentity,
            ImportedIdentityData expectedImportedIdentity
    ) {
        DatapackMigrationPersistenceVerifier.verify(
                expectedSoul,
                expectedImportedIdentity,
                SoulService.get(player),
                ImportedIdentityService.get(player)
        );
        if (!expectedIdentity.equals(SoulIdentityService.get(player))) {
            throw new IllegalStateException("Persisted revealed identity did not match the migration plan");
        }
    }

    private static SoulIdentityData toSoulIdentity(ImportedIdentityData imported) {
        if (!imported.hasIdentity()) {
            return SoulIdentityData.empty();
        }
        ImportedAspect aspect = imported.aspect().orElseThrow();
        ImportedFlaw flaw = imported.flaw().orElseThrow();
        AspectInstanceData persistentAspect = new AspectInstanceData(
                aspect.instanceId(),
                aspect.formalName(),
                aspect.aspectRank(),
                aspect.legacyMechanicalRoot(),
                id("compat/datapack/" + aspect.legacyNature()),
                "datapack-v1.0.0"
        );
        FlawInstanceData persistentFlaw = new FlawInstanceData(
                flaw.instanceId(),
                flaw.formalName(),
                flaw.effectId(),
                "datapack-v1.0.0"
        );
        return new SoulIdentityData(Optional.of(persistentAspect), Optional.of(persistentFlaw));
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

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, path);
    }
}
