package dev.spud.shadowslave.awakening;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/**
 * A Dreamer's intention before the roll: the evidence tag of what they offered
 * (and the item's name, for display), and the longing they named. Empty
 * strings mean nothing offered or named. The roll uses it once and clears it.
 */
public record IntentionData(String offeringTag, String offeringName, String longing) {
    public static final MapCodec<IntentionData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("offering_tag", "").forGetter(IntentionData::offeringTag),
            Codec.STRING.optionalFieldOf("offering_name", "").forGetter(IntentionData::offeringName),
            Codec.STRING.optionalFieldOf("longing", "").forGetter(IntentionData::longing)
    ).apply(instance, IntentionData::new));

    public static IntentionData empty() {
        return new IntentionData("", "", "");
    }

    public boolean isEmpty() {
        return offeringTag.isEmpty() && longing.isEmpty();
    }

    public Optional<Longing> parsedLonging() {
        return Longing.parse(longing);
    }
}
