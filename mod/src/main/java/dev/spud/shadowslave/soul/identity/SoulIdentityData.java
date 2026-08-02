package dev.spud.shadowslave.soul.identity;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;
import java.util.Optional;

/** Persistent full Aspect/Flaw instance records for the player's revealed identity. */
public record SoulIdentityData(
        Optional<AspectInstanceData> aspect,
        Optional<FlawInstanceData> flaw
) {
    public static final MapCodec<SoulIdentityData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            AspectInstanceData.CODEC.codec().optionalFieldOf("aspect").forGetter(SoulIdentityData::aspect),
            FlawInstanceData.CODEC.codec().optionalFieldOf("flaw").forGetter(SoulIdentityData::flaw)
    ).apply(instance, SoulIdentityData::new));

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
}
