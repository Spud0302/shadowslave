package dev.spud.shadowslave.echo;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Persistent server-authoritative collection of player-owned Echoes. */
public record EchoOwnershipData(List<EchoInstanceData> echoes) {
    private static final MapCodec<EchoOwnershipData> RAW_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            EchoInstanceData.CODEC.codec().listOf().optionalFieldOf("echoes", List.of())
                    .forGetter(EchoOwnershipData::echoes)
    ).apply(instance, EchoOwnershipData::new));

    public static final MapCodec<EchoOwnershipData> CODEC = RAW_CODEC.flatXmap(
            value -> {
                try {
                    return DataResult.success(new EchoOwnershipData(value.echoes()));
                } catch (IllegalArgumentException | NullPointerException exception) {
                    return DataResult.error(() -> "Invalid EchoOwnershipData: " + exception.getMessage());
                }
            },
            DataResult::success
    );

    public EchoOwnershipData {
        echoes = List.copyOf(Objects.requireNonNull(echoes, "echoes"));
        HashSet<ResourceLocation> ids = new HashSet<>();
        for (EchoInstanceData echo : echoes) {
            Objects.requireNonNull(echo, "echo");
            if (!ids.add(echo.echoId())) {
                throw new IllegalArgumentException("Duplicate owned Echo id: " + echo.echoId());
            }
        }
    }

    public static EchoOwnershipData empty() {
        return new EchoOwnershipData(List.of());
    }

    public Optional<EchoInstanceData> find(ResourceLocation echoId) {
        Objects.requireNonNull(echoId, "echoId");
        return echoes.stream().filter(echo -> echo.echoId().equals(echoId)).findFirst();
    }

    public boolean owns(ResourceLocation echoId) {
        return find(echoId).isPresent();
    }

    public EchoOwnershipData award(EchoInstanceData echo) {
        Objects.requireNonNull(echo, "echo");
        Optional<EchoInstanceData> existing = find(echo.echoId());
        if (existing.isPresent()) {
            EchoInstanceData owned = existing.get();
            if (!owned.formalName().equals(echo.formalName())
                    || !owned.acquisitionSource().equals(echo.acquisitionSource())
                    || !owned.provenance().equals(echo.provenance())) {
                throw new IllegalArgumentException("Contradictory Echo identity for " + echo.echoId());
            }
            return this;
        }
        ArrayList<EchoInstanceData> next = new ArrayList<>(echoes);
        next.add(echo);
        return new EchoOwnershipData(next);
    }

    public EchoOwnershipData withManifestation(
            ResourceLocation echoId,
            UUID entityUuid,
            ResourceLocation dimension,
            BlockPos position
    ) {
        return replaceManifestation(echoId, echo -> echo.withManifestation(entityUuid, dimension, position));
    }

    public EchoOwnershipData withoutManifestation(ResourceLocation echoId) {
        return replaceManifestation(echoId, EchoInstanceData::withoutManifestation);
    }

    private EchoOwnershipData replaceManifestation(
            ResourceLocation echoId,
            java.util.function.UnaryOperator<EchoInstanceData> update
    ) {
        Objects.requireNonNull(echoId, "echoId");
        Objects.requireNonNull(update, "update");
        ArrayList<EchoInstanceData> next = new ArrayList<>(echoes.size());
        boolean found = false;
        for (EchoInstanceData echo : echoes) {
            if (echo.echoId().equals(echoId)) {
                found = true;
                next.add(update.apply(echo));
            } else {
                next.add(echo);
            }
        }
        if (!found) {
            throw new IllegalArgumentException("Cannot update unowned Echo: " + echoId);
        }
        return new EchoOwnershipData(next);
    }
}
