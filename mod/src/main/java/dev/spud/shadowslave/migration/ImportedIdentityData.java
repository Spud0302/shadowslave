package dev.spud.shadowslave.migration;

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
    public static final MapCodec<ImportedIdentityData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ImportedAspect.CODEC.codec().optionalFieldOf("aspect").forGetter(ImportedIdentityData::aspect),
            ImportedFlaw.CODEC.codec().optionalFieldOf("flaw").forGetter(ImportedIdentityData::flaw)
    ).apply(instance, ImportedIdentityData::new));

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
}
