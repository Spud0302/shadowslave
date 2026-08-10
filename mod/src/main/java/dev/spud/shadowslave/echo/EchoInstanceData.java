package dev.spud.shadowslave.echo;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Persistent Java-owned identity, provenance and current manifestation for one Echo. */
public record EchoInstanceData(
        ResourceLocation echoId,
        String formalName,
        String acquisitionSource,
        String provenance,
        Optional<String> manifestationEntityUuid
) {
    private static final MapCodec<EchoInstanceData> RAW_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("echo_id").forGetter(EchoInstanceData::echoId),
            Codec.STRING.fieldOf("formal_name").forGetter(EchoInstanceData::formalName),
            Codec.STRING.fieldOf("acquisition_source").forGetter(EchoInstanceData::acquisitionSource),
            Codec.STRING.fieldOf("provenance").forGetter(EchoInstanceData::provenance),
            Codec.STRING.optionalFieldOf("manifestation_entity_uuid").forGetter(EchoInstanceData::manifestationEntityUuid)
    ).apply(instance, EchoInstanceData::new));

    public static final MapCodec<EchoInstanceData> CODEC = RAW_CODEC.flatXmap(
            value -> {
                try {
                    return DataResult.success(new EchoInstanceData(
                            value.echoId(), value.formalName(), value.acquisitionSource(), value.provenance(),
                            value.manifestationEntityUuid()));
                } catch (IllegalArgumentException | NullPointerException exception) {
                    return DataResult.error(() -> "Invalid EchoInstanceData: " + exception.getMessage());
                }
            },
            DataResult::success
    );

    public EchoInstanceData {
        echoId = Objects.requireNonNull(echoId, "echoId");
        formalName = requireText(formalName, "formalName");
        acquisitionSource = requireText(acquisitionSource, "acquisitionSource");
        provenance = requireText(provenance, "provenance");
        manifestationEntityUuid = Objects.requireNonNull(manifestationEntityUuid, "manifestationEntityUuid")
                .map(value -> UUID.fromString(requireText(value, "manifestationEntityUuid")).toString());
    }

    public EchoInstanceData withManifestation(UUID entityUuid) {
        return new EchoInstanceData(echoId, formalName, acquisitionSource, provenance,
                Optional.of(Objects.requireNonNull(entityUuid, "entityUuid").toString()));
    }

    public EchoInstanceData withoutManifestation() {
        if (manifestationEntityUuid.isEmpty()) {
            return this;
        }
        return new EchoInstanceData(echoId, formalName, acquisitionSource, provenance, Optional.empty());
    }

    public Optional<UUID> manifestationUuid() {
        return manifestationEntityUuid.map(UUID::fromString);
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }
}
