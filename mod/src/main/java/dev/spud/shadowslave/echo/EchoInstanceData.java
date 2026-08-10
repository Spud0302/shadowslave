package dev.spud.shadowslave.echo;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.spud.shadowslave.echo.content.EchoContentCatalog;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Persistent Java-owned identity, provenance, command state and current manifestation for one Echo. */
public record EchoInstanceData(
        ResourceLocation echoId,
        String formalName,
        String acquisitionSource,
        String provenance,
        EchoContentCatalog.CommandMode commandMode,
        Optional<String> manifestationEntityUuid,
        Optional<ResourceLocation> manifestationDimension,
        Optional<Long> manifestationBlockPos
) {
    private static final Codec<EchoContentCatalog.CommandMode> COMMAND_MODE_CODEC = Codec.STRING.flatXmap(
            value -> {
                try {
                    return DataResult.success(EchoContentCatalog.CommandMode.valueOf(value.trim().toUpperCase(Locale.ROOT)));
                } catch (IllegalArgumentException | NullPointerException exception) {
                    return DataResult.error(() -> "Unknown Echo command mode: " + value);
                }
            },
            mode -> DataResult.success(mode.name().toLowerCase(Locale.ROOT))
    );

    private static final MapCodec<EchoInstanceData> RAW_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("echo_id").forGetter(EchoInstanceData::echoId),
            Codec.STRING.fieldOf("formal_name").forGetter(EchoInstanceData::formalName),
            Codec.STRING.fieldOf("acquisition_source").forGetter(EchoInstanceData::acquisitionSource),
            Codec.STRING.fieldOf("provenance").forGetter(EchoInstanceData::provenance),
            COMMAND_MODE_CODEC.optionalFieldOf("command_mode", EchoContentCatalog.CommandMode.HOLD)
                    .forGetter(EchoInstanceData::commandMode),
            Codec.STRING.optionalFieldOf("manifestation_entity_uuid").forGetter(EchoInstanceData::manifestationEntityUuid),
            ResourceLocation.CODEC.optionalFieldOf("manifestation_dimension").forGetter(EchoInstanceData::manifestationDimension),
            Codec.LONG.optionalFieldOf("manifestation_block_pos").forGetter(EchoInstanceData::manifestationBlockPos)
    ).apply(instance, EchoInstanceData::new));

    public static final MapCodec<EchoInstanceData> CODEC = RAW_CODEC.flatXmap(
            value -> {
                try {
                    return DataResult.success(new EchoInstanceData(
                            value.echoId(), value.formalName(), value.acquisitionSource(), value.provenance(), value.commandMode(),
                            value.manifestationEntityUuid(), value.manifestationDimension(), value.manifestationBlockPos()));
                } catch (IllegalArgumentException | NullPointerException exception) {
                    return DataResult.error(() -> "Invalid EchoInstanceData: " + exception.getMessage());
                }
            },
            DataResult::success
    );

    /** Backward-compatible constructor for callers created before command state existed. */
    public EchoInstanceData(
            ResourceLocation echoId,
            String formalName,
            String acquisitionSource,
            String provenance,
            Optional<String> manifestationEntityUuid,
            Optional<ResourceLocation> manifestationDimension,
            Optional<Long> manifestationBlockPos
    ) {
        this(echoId, formalName, acquisitionSource, provenance, EchoContentCatalog.CommandMode.HOLD,
                manifestationEntityUuid, manifestationDimension, manifestationBlockPos);
    }

    public EchoInstanceData {
        echoId = Objects.requireNonNull(echoId, "echoId");
        formalName = requireText(formalName, "formalName");
        acquisitionSource = requireText(acquisitionSource, "acquisitionSource");
        provenance = requireText(provenance, "provenance");
        commandMode = Objects.requireNonNull(commandMode, "commandMode");
        manifestationEntityUuid = Objects.requireNonNull(manifestationEntityUuid, "manifestationEntityUuid")
                .map(value -> UUID.fromString(requireText(value, "manifestationEntityUuid")).toString());
        manifestationDimension = Objects.requireNonNull(manifestationDimension, "manifestationDimension");
        manifestationBlockPos = Objects.requireNonNull(manifestationBlockPos, "manifestationBlockPos");
        int populated = (manifestationEntityUuid.isPresent() ? 1 : 0)
                + (manifestationDimension.isPresent() ? 1 : 0)
                + (manifestationBlockPos.isPresent() ? 1 : 0);
        if (populated != 0 && populated != 3) {
            throw new IllegalArgumentException("Echo manifestation UUID, dimension and block position must be stored together");
        }
    }

    public EchoInstanceData withCommandMode(EchoContentCatalog.CommandMode mode) {
        EchoContentCatalog.CommandMode checked = Objects.requireNonNull(mode, "mode");
        if (checked == commandMode) {
            return this;
        }
        return new EchoInstanceData(
                echoId, formalName, acquisitionSource, provenance, checked,
                manifestationEntityUuid, manifestationDimension, manifestationBlockPos);
    }

    public EchoInstanceData withManifestation(UUID entityUuid, ResourceLocation dimension, BlockPos position) {
        return new EchoInstanceData(
                echoId,
                formalName,
                acquisitionSource,
                provenance,
                commandMode,
                Optional.of(Objects.requireNonNull(entityUuid, "entityUuid").toString()),
                Optional.of(Objects.requireNonNull(dimension, "dimension")),
                Optional.of(Objects.requireNonNull(position, "position").asLong())
        );
    }

    public EchoInstanceData withoutManifestation() {
        if (manifestationEntityUuid.isEmpty()) {
            return this;
        }
        return new EchoInstanceData(
                echoId, formalName, acquisitionSource, provenance, commandMode,
                Optional.empty(), Optional.empty(), Optional.empty());
    }

    public Optional<UUID> manifestationUuid() {
        return manifestationEntityUuid.map(UUID::fromString);
    }

    public Optional<BlockPos> manifestationPos() {
        return manifestationBlockPos.map(BlockPos::of);
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }
}
