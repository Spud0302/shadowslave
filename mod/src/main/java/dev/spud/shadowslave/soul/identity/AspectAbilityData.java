package dev.spud.shadowslave.soul.identity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.spud.shadowslave.soul.SoulRank;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Optional;

/** Provider-independent identity for one ability belonging to an Aspect. */
public record AspectAbilityData(
        ResourceLocation abilityId,
        AspectAbilityKind kind,
        Optional<SoulRank> acquisitionRank,
        String provenance
) {
    private static final MapCodec<StoredAspectAbilityData> STORED_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("ability_id").forGetter(StoredAspectAbilityData::abilityId),
            AspectAbilityKind.CODEC.fieldOf("kind").forGetter(StoredAspectAbilityData::kind),
            SoulRank.CODEC.optionalFieldOf("acquisition_rank").forGetter(StoredAspectAbilityData::acquisitionRank),
            Codec.STRING.fieldOf("provenance").forGetter(StoredAspectAbilityData::provenance)
    ).apply(instance, StoredAspectAbilityData::new));

    /** Decode through an unvalidated storage shape so domain failures become DataResult errors. */
    public static final MapCodec<AspectAbilityData> CODEC = STORED_CODEC.flatXmap(
            AspectAbilityData::construct,
            data -> DataResult.success(StoredAspectAbilityData.from(data))
    );

    public AspectAbilityData {
        abilityId = Objects.requireNonNull(abilityId, "abilityId");
        kind = Objects.requireNonNull(kind, "kind");
        acquisitionRank = Objects.requireNonNull(acquisitionRank, "acquisitionRank");
        provenance = requireText(provenance, "provenance");

        if (kind == AspectAbilityKind.INNATE && acquisitionRank.isPresent()) {
            throw new IllegalArgumentException("An innate Aspect ability cannot have an acquisition rank");
        }
        if (kind == AspectAbilityKind.RANK_GRANTED && acquisitionRank.isEmpty()) {
            throw new IllegalArgumentException("A rank-granted Aspect ability requires an acquisition rank");
        }
        if (kind == AspectAbilityKind.LEGACY_UNCLASSIFIED && acquisitionRank.isPresent()) {
            throw new IllegalArgumentException("A legacy-unclassified Aspect ability cannot invent an acquisition rank");
        }
    }

    public static AspectAbilityData innate(ResourceLocation abilityId, String provenance) {
        return new AspectAbilityData(abilityId, AspectAbilityKind.INNATE, Optional.empty(), provenance);
    }

    public static AspectAbilityData rankGranted(
            ResourceLocation abilityId,
            SoulRank acquisitionRank,
            String provenance
    ) {
        return new AspectAbilityData(
                abilityId,
                AspectAbilityKind.RANK_GRANTED,
                Optional.of(Objects.requireNonNull(acquisitionRank, "acquisitionRank")),
                provenance
        );
    }

    public static AspectAbilityData legacyUnclassified(ResourceLocation abilityId, String provenance) {
        return new AspectAbilityData(
                abilityId,
                AspectAbilityKind.LEGACY_UNCLASSIFIED,
                Optional.empty(),
                provenance
        );
    }

    private static DataResult<AspectAbilityData> construct(StoredAspectAbilityData stored) {
        try {
            return DataResult.success(new AspectAbilityData(
                    stored.abilityId(),
                    stored.kind(),
                    stored.acquisitionRank(),
                    stored.provenance()
            ));
        } catch (IllegalArgumentException | NullPointerException exception) {
            return DataResult.error(() -> "Invalid AspectAbilityData: " + exception.getMessage());
        }
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }

    private record StoredAspectAbilityData(
            ResourceLocation abilityId,
            AspectAbilityKind kind,
            Optional<SoulRank> acquisitionRank,
            String provenance
    ) {
        private static StoredAspectAbilityData from(AspectAbilityData data) {
            return new StoredAspectAbilityData(
                    data.abilityId(),
                    data.kind(),
                    data.acquisitionRank(),
                    data.provenance()
            );
        }
    }
}
