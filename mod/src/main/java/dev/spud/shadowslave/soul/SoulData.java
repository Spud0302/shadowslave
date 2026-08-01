package dev.spud.shadowslave.soul;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Optional;

/**
 * Canonical, server-owned player Soul state.
 *
 * <p>The attachment stores permanent identity and progression. Temporary
 * historical roles, trial bodies, objectives and evidence belong to a
 * Nightmare instance rather than this record.</p>
 */
public record SoulData(
        int schemaVersion,
        SpellState spellState,
        AwakeningPath awakeningPath,
        Optional<SoulRank> soulRank,
        Optional<ResourceLocation> aspectId,
        Optional<SoulRank> aspectRank,
        Optional<ResourceLocation> flawId,
        boolean importedFromDatapack,
        int migrationVersion
) {
    public static final int CURRENT_SCHEMA = 2;
    public static final int NO_MIGRATION = 0;

    /**
     * Empty means the person has no ranked Soul Core. The legacy alpha value
     * "mundane" is accepted only as a migration alias for an absent Rank.
     */
    private static final Codec<Optional<SoulRank>> OPTIONAL_SOUL_RANK_CODEC = Codec.STRING.comapFlatMap(
            serialized -> {
                if (serialized.isBlank() || "mundane".equals(serialized)) {
                    return DataResult.success(Optional.empty());
                }
                return SoulRank.decode(serialized).map(Optional::of);
            },
            rank -> rank.map(SoulRank::serializedName).orElse("")
    );

    private static final MapCodec<StoredSoulData> STORED_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.optionalFieldOf("schema_version", CURRENT_SCHEMA).forGetter(StoredSoulData::schemaVersion),
            SpellState.CODEC.optionalFieldOf("spell_state", SpellState.UNINFECTED).forGetter(StoredSoulData::spellState),
            AwakeningPath.CODEC.optionalFieldOf("awakening_path", AwakeningPath.UNDECIDED).forGetter(StoredSoulData::awakeningPath),
            OPTIONAL_SOUL_RANK_CODEC.optionalFieldOf("soul_rank", Optional.empty()).forGetter(StoredSoulData::soulRank),
            ResourceLocation.CODEC.optionalFieldOf("aspect_id").forGetter(StoredSoulData::aspectId),
            OPTIONAL_SOUL_RANK_CODEC.optionalFieldOf("aspect_rank", Optional.empty()).forGetter(StoredSoulData::aspectRank),
            ResourceLocation.CODEC.optionalFieldOf("flaw_id").forGetter(StoredSoulData::flawId),
            Codec.BOOL.optionalFieldOf("imported_from_datapack", false).forGetter(StoredSoulData::importedFromDatapack),
            Codec.INT.optionalFieldOf("migration_version", NO_MIGRATION).forGetter(StoredSoulData::migrationVersion)
    ).apply(instance, StoredSoulData::new));

    /**
     * Decode through an unvalidated storage record so domain invariant failures
     * become {@link DataResult} errors instead of escaping as raw exceptions
     * during player attachment loading.
     */
    public static final MapCodec<SoulData> CODEC = STORED_CODEC.flatXmap(
            SoulData::decodeStored,
            soul -> DataResult.success(StoredSoulData.from(soul))
    );

    public SoulData {
        if (schemaVersion < 1) {
            throw new IllegalArgumentException("schemaVersion must be positive");
        }
        if (schemaVersion > CURRENT_SCHEMA) {
            throw new IllegalArgumentException("Unsupported future SoulData schema: " + schemaVersion);
        }
        if (migrationVersion < 0) {
            throw new IllegalArgumentException("migrationVersion cannot be negative");
        }

        spellState = Objects.requireNonNull(spellState, "spellState");
        awakeningPath = Objects.requireNonNull(awakeningPath, "awakeningPath");
        soulRank = Objects.requireNonNull(soulRank, "soulRank");
        aspectId = Objects.requireNonNull(aspectId, "aspectId");
        aspectRank = Objects.requireNonNull(aspectRank, "aspectRank");
        flawId = Objects.requireNonNull(flawId, "flawId");

        if (!spellState.requiredSoulRank().equals(soulRank)) {
            throw new IllegalArgumentException(
                    "Spell state " + spellState + " requires Soul Rank " + spellState.requiredSoulRank()
            );
        }
        if (aspectId.isPresent() != aspectRank.isPresent()) {
            throw new IllegalArgumentException("Aspect identity and Aspect Rank must be present together");
        }

        switch (spellState) {
            case UNINFECTED -> {
                if (awakeningPath != AwakeningPath.UNDECIDED) {
                    throw new IllegalArgumentException("An uninfected person cannot already have an awakening path");
                }
            }
            case CARRIER, ASPIRANT, DREAMER -> {
                if (awakeningPath != AwakeningPath.NIGHTMARE_SPELL) {
                    throw new IllegalArgumentException(spellState + " is specific to the Nightmare Spell path");
                }
            }
            default -> {
                if (awakeningPath == AwakeningPath.UNDECIDED) {
                    throw new IllegalArgumentException("Ranked progression requires an awakening path");
                }
            }
        }

        if (spellState == SpellState.UNINFECTED
                || spellState == SpellState.CARRIER
                || spellState == SpellState.ASPIRANT) {
            if (aspectId.isPresent() || flawId.isPresent()) {
                throw new IllegalArgumentException(
                        "Permanent Aspect and Flaw identity cannot exist before First Nightmare appraisal"
                );
            }
        }

        if (awakeningPath == AwakeningPath.NIGHTMARE_SPELL && spellState.isAtOrBeyondDreamer()) {
            if (aspectId.isEmpty() || flawId.isEmpty()) {
                throw new IllegalArgumentException(
                        "A post-First-Nightmare Spell user must have an appraised Aspect and Flaw"
                );
            }
        }
    }

    public static SoulData uninfected() {
        return new SoulData(
                CURRENT_SCHEMA,
                SpellState.UNINFECTED,
                AwakeningPath.UNDECIDED,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                false,
                NO_MIGRATION
        );
    }

    /** Temporary source-compatibility alias for the alpha-1 API. */
    @Deprecated(forRemoval = true)
    public static SoulData mundane() {
        return uninfected();
    }

    public SoulData asCarrier() {
        return new SoulData(
                CURRENT_SCHEMA,
                SpellState.CARRIER,
                AwakeningPath.NIGHTMARE_SPELL,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                importedFromDatapack,
                migrationVersion
        );
    }

    public SoulData asAspirant() {
        return new SoulData(
                CURRENT_SCHEMA,
                SpellState.ASPIRANT,
                AwakeningPath.NIGHTMARE_SPELL,
                Optional.of(SoulRank.DORMANT),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                importedFromDatapack,
                migrationVersion
        );
    }

    public SoulData asDreamer(ResourceLocation aspect, SoulRank revealedAspectRank, ResourceLocation flaw) {
        return new SoulData(
                CURRENT_SCHEMA,
                SpellState.DREAMER,
                AwakeningPath.NIGHTMARE_SPELL,
                Optional.of(SoulRank.DORMANT),
                Optional.of(Objects.requireNonNull(aspect, "aspect")),
                Optional.of(Objects.requireNonNull(revealedAspectRank, "revealedAspectRank")),
                Optional.of(Objects.requireNonNull(flaw, "flaw")),
                importedFromDatapack,
                migrationVersion
        );
    }

    public SoulData markImported(int completedMigrationVersion) {
        return new SoulData(
                CURRENT_SCHEMA,
                spellState,
                awakeningPath,
                soulRank,
                aspectId,
                aspectRank,
                flawId,
                true,
                completedMigrationVersion
        );
    }

    private static DataResult<SoulData> decodeStored(StoredSoulData stored) {
        if (stored.schemaVersion() < 1) {
            return DataResult.error(() -> "SoulData schema_version must be positive");
        }
        if (stored.schemaVersion() > CURRENT_SCHEMA) {
            return DataResult.error(() -> "Unsupported future SoulData schema: " + stored.schemaVersion());
        }

        return switch (stored.schemaVersion()) {
            case 1 -> decodeSchemaOne(stored);
            case CURRENT_SCHEMA -> constructCurrent(stored);
            default -> DataResult.error(() -> "Unsupported SoulData schema: " + stored.schemaVersion());
        };
    }

    /** Explicit alpha-1 to schema-2 migration. */
    private static DataResult<SoulData> decodeSchemaOne(StoredSoulData stored) {
        AwakeningPath migratedPath = stored.awakeningPath();
        if (migratedPath == AwakeningPath.UNDECIDED && stored.spellState() != SpellState.UNINFECTED) {
            migratedPath = AwakeningPath.NIGHTMARE_SPELL;
        }

        Optional<SoulRank> migratedAspectRank = stored.aspectRank();
        if (stored.aspectId().isPresent() && migratedAspectRank.isEmpty()) {
            // The frozen datapack imports its prototype Aspect at Dormant rank.
            migratedAspectRank = Optional.of(SoulRank.DORMANT);
        }

        return construct(
                CURRENT_SCHEMA,
                stored.spellState(),
                migratedPath,
                stored.soulRank(),
                stored.aspectId(),
                migratedAspectRank,
                stored.flawId(),
                stored.importedFromDatapack(),
                stored.migrationVersion()
        );
    }

    /** Schema 2 is validated exactly as stored; migration defaults do not repair it silently. */
    private static DataResult<SoulData> constructCurrent(StoredSoulData stored) {
        return construct(
                CURRENT_SCHEMA,
                stored.spellState(),
                stored.awakeningPath(),
                stored.soulRank(),
                stored.aspectId(),
                stored.aspectRank(),
                stored.flawId(),
                stored.importedFromDatapack(),
                stored.migrationVersion()
        );
    }

    private static DataResult<SoulData> construct(
            int schemaVersion,
            SpellState spellState,
            AwakeningPath awakeningPath,
            Optional<SoulRank> soulRank,
            Optional<ResourceLocation> aspectId,
            Optional<SoulRank> aspectRank,
            Optional<ResourceLocation> flawId,
            boolean importedFromDatapack,
            int migrationVersion
    ) {
        try {
            return DataResult.success(new SoulData(
                    schemaVersion,
                    spellState,
                    awakeningPath,
                    soulRank,
                    aspectId,
                    aspectRank,
                    flawId,
                    importedFromDatapack,
                    migrationVersion
            ));
        } catch (IllegalArgumentException | NullPointerException exception) {
            return DataResult.error(() -> "Invalid SoulData: " + exception.getMessage());
        }
    }

    private record StoredSoulData(
            int schemaVersion,
            SpellState spellState,
            AwakeningPath awakeningPath,
            Optional<SoulRank> soulRank,
            Optional<ResourceLocation> aspectId,
            Optional<SoulRank> aspectRank,
            Optional<ResourceLocation> flawId,
            boolean importedFromDatapack,
            int migrationVersion
    ) {
        private static StoredSoulData from(SoulData soul) {
            return new StoredSoulData(
                    soul.schemaVersion(),
                    soul.spellState(),
                    soul.awakeningPath(),
                    soul.soulRank(),
                    soul.aspectId(),
                    soul.aspectRank(),
                    soul.flawId(),
                    soul.importedFromDatapack(),
                    soul.migrationVersion()
            );
        }
    }
}
