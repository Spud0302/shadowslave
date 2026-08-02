package dev.spud.shadowslave.migration;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;
import java.util.Optional;

/**
 * Persistent Java-owned metadata for an identity imported from the frozen datapack.
 * SoulData stores the authoritative IDs and ranks; this attachment preserves the
 * names and compatibility metadata required to render and execute the imported
 * identity without depending on legacy scoreboards.
 */
public record ImportedIdentityData(
        Optional<ImportedAspect> aspect,
        Optional<ImportedFlaw> flaw
) {
    private static final MapCodec<StoredImportedIdentityData> STORED_CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ImportedAspect.CODEC.codec().optionalFieldOf("aspect")
                            .forGetter(StoredImportedIdentityData::aspect),
                    ImportedFlaw.CODEC.codec().optionalFieldOf("flaw")
                            .forGetter(StoredImportedIdentityData::flaw)
            ).apply(instance, StoredImportedIdentityData::new));

    public static final MapCodec<ImportedIdentityData> CODEC = STORED_CODEC.flatXmap(
            ImportedIdentityData::construct,
            data -> DataResult.success(StoredImportedIdentityData.from(data))
    );

    public ImportedIdentityData {
        aspect = Objects.requireNonNull(aspect, "aspect");
        flaw = Objects.requireNonNull(flaw, "flaw");
        if (aspect.isPresent() != flaw.isPresent()) {
            throw new IllegalArgumentException("Imported identity requires both Aspect and Flaw metadata");
        }
    }

    public static ImportedIdentityData empty() {
        return new ImportedIdentityData(Optional.empty(), Optional.empty());
    }

    public static ImportedIdentityData fromPlan(DatapackMigrationPlan plan) {
        Objects.requireNonNull(plan, "plan");
        return new ImportedIdentityData(plan.aspect(), plan.flaw());
    }

    public boolean hasIdentity() {
        return aspect.isPresent();
    }

    private static DataResult<ImportedIdentityData> construct(StoredImportedIdentityData stored) {
        try {
            return DataResult.success(new ImportedIdentityData(stored.aspect(), stored.flaw()));
        } catch (IllegalArgumentException | NullPointerException exception) {
            return DataResult.error(() -> "Invalid ImportedIdentityData: " + exception.getMessage());
        }
    }

    private record StoredImportedIdentityData(
            Optional<ImportedAspect> aspect,
            Optional<ImportedFlaw> flaw
    ) {
        private static StoredImportedIdentityData from(ImportedIdentityData data) {
            return new StoredImportedIdentityData(data.aspect(), data.flaw());
        }
    }
}
