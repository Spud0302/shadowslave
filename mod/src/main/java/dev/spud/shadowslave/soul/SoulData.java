package dev.spud.shadowslave.soul;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Optional;

/**
 * Canonical, server-owned player Soul state.
 *
 * <p>The attachment stores internal Shadow Slave identifiers. Compatibility
 * adapters may map those identifiers to third-party spells or equipment, but
 * external mod IDs never become the only record of the player's identity.</p>
 */
public record SoulData(
        int schemaVersion,
        SpellState spellState,
        SoulRank soulRank,
        Optional<ResourceLocation> aspectId,
        Optional<ResourceLocation> flawId,
        boolean importedFromDatapack,
        int migrationVersion
) {
    public static final int CURRENT_SCHEMA = 1;
    public static final int NO_MIGRATION = 0;

    public static final MapCodec<SoulData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.optionalFieldOf("schema_version", CURRENT_SCHEMA).forGetter(SoulData::schemaVersion),
            SpellState.CODEC.optionalFieldOf("spell_state", SpellState.MUNDANE).forGetter(SoulData::spellState),
            SoulRank.CODEC.optionalFieldOf("soul_rank", SoulRank.MUNDANE).forGetter(SoulData::soulRank),
            ResourceLocation.CODEC.optionalFieldOf("aspect_id").forGetter(SoulData::aspectId),
            ResourceLocation.CODEC.optionalFieldOf("flaw_id").forGetter(SoulData::flawId),
            Codec.BOOL.optionalFieldOf("imported_from_datapack", false).forGetter(SoulData::importedFromDatapack),
            Codec.INT.optionalFieldOf("migration_version", NO_MIGRATION).forGetter(SoulData::migrationVersion)
    ).apply(instance, SoulData::new));

    public SoulData {
        if (schemaVersion < 1) {
            throw new IllegalArgumentException("schemaVersion must be positive");
        }
        if (migrationVersion < 0) {
            throw new IllegalArgumentException("migrationVersion cannot be negative");
        }
        spellState = Objects.requireNonNull(spellState, "spellState");
        soulRank = Objects.requireNonNull(soulRank, "soulRank");
        aspectId = Objects.requireNonNull(aspectId, "aspectId");
        flawId = Objects.requireNonNull(flawId, "flawId");
    }

    public static SoulData mundane() {
        return new SoulData(
                CURRENT_SCHEMA,
                SpellState.MUNDANE,
                SoulRank.MUNDANE,
                Optional.empty(),
                Optional.empty(),
                false,
                NO_MIGRATION
        );
    }

    public SoulData asCarrier() {
        return new SoulData(
                CURRENT_SCHEMA,
                SpellState.CARRIER,
                SoulRank.MUNDANE,
                Optional.empty(),
                Optional.empty(),
                importedFromDatapack,
                migrationVersion
        );
    }

    public SoulData asSleeper(ResourceLocation aspect, ResourceLocation flaw) {
        return new SoulData(
                CURRENT_SCHEMA,
                SpellState.SLEEPER,
                SoulRank.DORMANT,
                Optional.of(Objects.requireNonNull(aspect, "aspect")),
                Optional.of(Objects.requireNonNull(flaw, "flaw")),
                importedFromDatapack,
                migrationVersion
        );
    }

    public SoulData markImported(int completedMigrationVersion) {
        return new SoulData(
                CURRENT_SCHEMA,
                spellState,
                soulRank,
                aspectId,
                flawId,
                true,
                completedMigrationVersion
        );
    }
}
