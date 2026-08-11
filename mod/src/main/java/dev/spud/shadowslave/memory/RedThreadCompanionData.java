package dev.spud.shadowslave.memory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Persistent Java-owned target for Red Thread Bracelet's authored tethered_pulse enchantment. */
public record RedThreadCompanionData(Optional<String> companionUuid) {
    private static final MapCodec<RedThreadCompanionData> RAW_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("companion_uuid").forGetter(RedThreadCompanionData::companionUuid)
    ).apply(instance, RedThreadCompanionData::new));

    public static final MapCodec<RedThreadCompanionData> CODEC = RAW_CODEC.flatXmap(
            value -> {
                try {
                    return DataResult.success(new RedThreadCompanionData(value.companionUuid()));
                } catch (IllegalArgumentException | NullPointerException exception) {
                    return DataResult.error(() -> "Invalid RedThreadCompanionData: " + exception.getMessage());
                }
            },
            DataResult::success
    );

    public RedThreadCompanionData {
        companionUuid = Objects.requireNonNull(companionUuid, "companionUuid").map(value -> {
            String checked = Objects.requireNonNull(value, "companionUuid value").trim();
            if (checked.isEmpty()) throw new IllegalArgumentException("companionUuid cannot be blank");
            return UUID.fromString(checked).toString();
        });
    }

    public static RedThreadCompanionData empty() {
        return new RedThreadCompanionData(Optional.empty());
    }

    public static RedThreadCompanionData marked(UUID companionUuid) {
        return new RedThreadCompanionData(Optional.of(Objects.requireNonNull(companionUuid, "companionUuid").toString()));
    }

    public Optional<UUID> companionId() {
        return companionUuid.map(UUID::fromString);
    }

    public boolean hasCompanion() {
        return companionUuid.isPresent();
    }

    public RedThreadCompanionData clear() {
        return empty();
    }
}
