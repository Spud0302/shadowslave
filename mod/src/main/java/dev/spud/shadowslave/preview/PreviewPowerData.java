package dev.spud.shadowslave.preview;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Persistent preview-only ability cooldown state. */
public record PreviewPowerData(long kindleCooldownUntil) {
    private static final MapCodec<StoredPreviewPowerData> STORED_CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.LONG.optionalFieldOf("kindle_cooldown_until", 0L)
                            .forGetter(StoredPreviewPowerData::kindleCooldownUntil)
            ).apply(instance, StoredPreviewPowerData::new));

    public static final MapCodec<PreviewPowerData> CODEC = STORED_CODEC.flatXmap(
            stored -> construct(stored.kindleCooldownUntil()),
            data -> DataResult.success(new StoredPreviewPowerData(data.kindleCooldownUntil()))
    );

    public PreviewPowerData {
        if (kindleCooldownUntil < 0) {
            throw new IllegalArgumentException("kindleCooldownUntil cannot be negative");
        }
    }

    public static PreviewPowerData empty() {
        return new PreviewPowerData(0L);
    }

    private static DataResult<PreviewPowerData> construct(long kindleCooldownUntil) {
        try {
            return DataResult.success(new PreviewPowerData(kindleCooldownUntil));
        } catch (IllegalArgumentException exception) {
            return DataResult.error(() -> "Invalid PreviewPowerData: " + exception.getMessage());
        }
    }

    private record StoredPreviewPowerData(long kindleCooldownUntil) {
    }
}
