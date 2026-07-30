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
 * historical roles, trial bodies, objectives and evidence belong to a future
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

    public static final MapCodec<SoulData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.optionalFieldOf("schema_version", CURRENT_SCHEMA).forGetter(SoulData::schemaVersion),
            SpellState.CODEC.optionalFieldOf("spell_state", SpellState.UNINFECTED).forGetter(SoulData::spellState),
            AwakeningPath.CODEC.optionalFieldOf("awakening_path", AwakeningPath.UNDECIDED).forGetter(SoulData::awakeningPath),
            OPTIONAL_SOUL_RANK_CODEC.optionalFieldOf("soul_rank", Optional.empty()).forGetter(SoulData::soulRank),
            ResourceLocation.CODEC.optionalFieldOf("aspect_id").forGetter(SoulData::aspectId),
            OPTIONAL_SOUL_RANK_CODEC.optionalFieldOf("aspect_rank", Optional.empty()).forGetter(SoulData::aspectRank),
            ResourceLocation.CODEC.optionalFieldOf("flaw_id").forGetter(SoulData::flawId),
            Codec.BOOL.optionalFieldOf("imported_from_datapack", false).forGetter(SoulData::importedFromDatapack),
            Codec.INT.optionalFieldOf("migration_version", NO_MIGRATION).forGetter(SoulData::migrationVersion)
    ).apply(instance, SoulData::decode));

    public SoulData {
        if (schemaVersion < 1) {
            throw new IllegalArgumentException("schemaVersion must be positive");
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

        if (spellState == SpellState.DREAMER && awakeningPath == AwakeningPath.NIGHTMARE_SPELL) {
            if (aspectId.isEmpty() || flawId.isEmpty()) {
                throw new IllegalArgumentException("A Spell-path Dreamer must have an appraised Aspect and Flaw");
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

    private static SoulData decode(
            int storedSchema,
            SpellState spellState,
            AwakeningPath storedPath,
            Optional<SoulRank> soulRank,
            Optional<ResourceLocation> aspectId,
            Optional<SoulRank> storedAspectRank,
            Optional<ResourceLocation> flawId,
            boolean importedFromDatapack,
            int migrationVersion
    ) {
        AwakeningPath path = storedPath;
        if (path == AwakeningPath.UNDECIDED && spellState != SpellState.UNINFECTED) {
            path = AwakeningPath.NIGHTMARE_SPELL;
        }

        Optional<SoulRank> aspectRank = storedAspectRank;
        if (aspectId.isPresent() && aspectRank.isEmpty()) {
            // The frozen datapack imports its prototype Aspect at Dormant rank.
            aspectRank = Optional.of(SoulRank.DORMANT);
        }

        return new SoulData(
                CURRENT_SCHEMA,
                spellState,
                path,
                soulRank,
                aspectId,
                aspectRank,
                flawId,
                importedFromDatapack,
                migrationVersion
        );
    }
}
