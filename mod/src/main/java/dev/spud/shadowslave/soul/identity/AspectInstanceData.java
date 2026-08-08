package dev.spud.shadowslave.soul.identity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.spud.shadowslave.soul.SoulRank;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Persistent Aspect identity, independent from execution providers and name-revelation state. */
public record AspectInstanceData(
        ResourceLocation instanceId,
        Optional<String> formalName,
        SoulRank aspectRank,
        ResourceLocation natureId,
        AspectAbilitySetData abilitySet,
        String provenance
) {
    private static final MapCodec<StoredAspectInstanceData> STORED_CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("instance_id").forGetter(StoredAspectInstanceData::instanceId),
                    Codec.STRING.optionalFieldOf("formal_name").forGetter(StoredAspectInstanceData::formalName),
                    SoulRank.CODEC.fieldOf("aspect_rank").forGetter(StoredAspectInstanceData::aspectRank),
                    ResourceLocation.CODEC.fieldOf("nature_id").forGetter(StoredAspectInstanceData::natureId),
                    AspectAbilityData.CODEC.codec().listOf().optionalFieldOf("abilities").forGetter(StoredAspectInstanceData::abilities),
                    ResourceLocation.CODEC.optionalFieldOf("ability_id").forGetter(StoredAspectInstanceData::legacyAbilityId),
                    Codec.STRING.fieldOf("provenance").forGetter(StoredAspectInstanceData::provenance)
            ).apply(instance, StoredAspectInstanceData::new));

    public static final MapCodec<AspectInstanceData> CODEC = STORED_CODEC.flatXmap(
            AspectInstanceData::construct,
            data -> DataResult.success(StoredAspectInstanceData.from(data))
    );

    public AspectInstanceData {
        instanceId = Objects.requireNonNull(instanceId, "instanceId");
        formalName = normalizeOptionalName(formalName);
        aspectRank = Objects.requireNonNull(aspectRank, "aspectRank");
        natureId = Objects.requireNonNull(natureId, "natureId");
        abilitySet = Objects.requireNonNull(abilitySet, "abilitySet");
        if (abilitySet.abilities().isEmpty()) {
            throw new IllegalArgumentException("abilitySet cannot be empty for an Aspect identity");
        }
        provenance = requireText(provenance, "provenance");
    }

    /** Source-compatible constructor for existing revealed identities using the explicit set model. */
    public AspectInstanceData(
            ResourceLocation instanceId,
            String formalName,
            SoulRank aspectRank,
            ResourceLocation natureId,
            AspectAbilitySetData abilitySet,
            String provenance
    ) {
        this(
                instanceId,
                Optional.of(Objects.requireNonNull(formalName, "formalName")),
                aspectRank,
                natureId,
                abilitySet,
                provenance
        );
    }

    public String displayedName() {
        return formalName.orElse("");
    }

    private static DataResult<AspectInstanceData> construct(StoredAspectInstanceData stored) {
        try {
            boolean hasAbilities = stored.abilities().isPresent();
            boolean hasLegacyAbility = stored.legacyAbilityId().isPresent();
            if (hasAbilities == hasLegacyAbility) {
                return DataResult.error(() -> "Invalid AspectInstanceData: exactly one of abilities or legacy ability_id is required");
            }

            AspectAbilitySetData abilities = hasAbilities
                    ? new AspectAbilitySetData(stored.abilities().orElseThrow())
                    : new AspectAbilitySetData(List.of(AspectAbilityData.legacyUnclassified(
                            stored.legacyAbilityId().orElseThrow(),
                            "compatibility: decoded legacy AspectInstanceData.ability_id"
                    )));

            return DataResult.success(new AspectInstanceData(
                    stored.instanceId(),
                    stored.formalName(),
                    stored.aspectRank(),
                    stored.natureId(),
                    abilities,
                    stored.provenance()
            ));
        } catch (IllegalArgumentException | NullPointerException exception) {
            return DataResult.error(() -> "Invalid AspectInstanceData: " + exception.getMessage());
        }
    }

    private static Optional<String> normalizeOptionalName(Optional<String> value) {
        Optional<String> checked = Objects.requireNonNull(value, "formalName");
        if (checked.isEmpty()) {
            return Optional.empty();
        }
        String normalized = checked.orElseThrow().trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("formalName cannot be blank when present");
        }
        return Optional.of(normalized);
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }

    private record StoredAspectInstanceData(
            ResourceLocation instanceId,
            Optional<String> formalName,
            SoulRank aspectRank,
            ResourceLocation natureId,
            Optional<List<AspectAbilityData>> abilities,
            Optional<ResourceLocation> legacyAbilityId,
            String provenance
    ) {
        private static StoredAspectInstanceData from(AspectInstanceData data) {
            return new StoredAspectInstanceData(
                    data.instanceId(),
                    data.formalName(),
                    data.aspectRank(),
                    data.natureId(),
                    Optional.of(data.abilitySet().abilities()),
                    Optional.empty(),
                    data.provenance()
            );
        }
    }
}
