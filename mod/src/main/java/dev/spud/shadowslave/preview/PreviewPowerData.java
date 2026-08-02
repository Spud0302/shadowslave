package dev.spud.shadowslave.preview;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Persistent preview-only ability cooldown state. */
public record PreviewPowerData(long kindleCooldownUntil) {
    public static final MapCodec<PreviewPowerData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.LONG.optionalFieldOf("kindle_cooldown_until", 0L).forGetter(PreviewPowerData::kindleCooldownUntil)
    ).apply(instance, PreviewPowerData::new));

    public PreviewPowerData {
        if (kindleCooldownUntil < 0) {
            throw new IllegalArgumentException("kindleCooldownUntil cannot be negative");
        }
    }

    public static PreviewPowerData empty() {
        return new PreviewPowerData(0L);
    }
}
