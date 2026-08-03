package dev.spud.shadowslave.soul.identity;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;
import java.util.Optional;

/** Persistent full Aspect/Flaw instance records for the player's revealed identity. */
public record SoulIdentityData(
        Optional<AspectInstanceData> aspect,
        Optional<FlawInstanceData> flaw
) {
    private static final MapCodec<StoredSoulIdentityData> STORED_CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    AspectInstanceData.CODEC.codec().optionalFieldOf("aspect")
                            .forGetter(StoredSoulIdentityData::aspect),
                    FlawInstanceData.CODEC.codec().optionalFieldOf("flaw")
                            .forGetter(StoredSoulIdentityData::flaw)
            ).apply(instance, StoredSoulIdentityData::new));

    public static final MapCodec<SoulIdentityData> CODEC = STORED_CODEC.flatXmap(
            SoulIdentityData::construct,
            data -> DataResult.success(StoredSoulIdentityData.from(data))
    );

    public SoulIdentityData {
        aspect = Objects.requireNonNull(aspect, "aspect");
        flaw = Objects.requireNonNull(flaw, "flaw");
        if (aspect.isPresent() != flaw.isPresent()) {
            throw new IllegalArgumentException("Revealed identity requires both Aspect and Flaw records");
        }
    }

    public static SoulIdentityData empty() {
        return new SoulIdentityData(Optional.empty(), Optional.empty());
    }

    public boolean isRevealed() {
        return aspect.isPresent();
    }

    private static DataResult<SoulIdentityData> construct(StoredSoulIdentityData stored) {
        try {
            return DataResult.success(new SoulIdentityData(stored.aspect(), stored.flaw()));
        } catch (IllegalArgumentException | NullPointerException exception) {
            return DataResult.error(() -> "Invalid SoulIdentityData: " + exception.getMessage());
        }
    }

    private record StoredSoulIdentityData(
            Optional<AspectInstanceData> aspect,
            Optional<FlawInstanceData> flaw
    ) {
        private static StoredSoulIdentityData from(SoulIdentityData data) {
            return new StoredSoulIdentityData(data.aspect(), data.flaw());
        }
    }
}
