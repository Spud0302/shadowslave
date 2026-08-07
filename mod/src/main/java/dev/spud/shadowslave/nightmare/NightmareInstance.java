package dev.spud.shadowslave.nightmare;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Persistent ownership and recovery data for one active First Nightmare. */
public record NightmareInstance(
        UUID instanceId,
        UUID playerId,
        int slot,
        String scenarioId,
        String historicalRoleId,
        ResourceLocation returnDimension,
        double returnX,
        double returnY,
        double returnZ,
        float returnYaw,
        float returnPitch,
        BlockPos origin,
        BlockPos altar,
        Optional<UUID> pursuerId,
        long createdGameTime
) {
    public NightmareInstance {
        instanceId = Objects.requireNonNull(instanceId, "instanceId");
        playerId = Objects.requireNonNull(playerId, "playerId");
        scenarioId = requireText(scenarioId, "scenarioId");
        historicalRoleId = requireText(historicalRoleId, "historicalRoleId");
        returnDimension = Objects.requireNonNull(returnDimension, "returnDimension");
        origin = Objects.requireNonNull(origin, "origin");
        altar = Objects.requireNonNull(altar, "altar");
        pursuerId = Objects.requireNonNull(pursuerId, "pursuerId");
        if (slot < 0) {
            throw new IllegalArgumentException("slot cannot be negative");
        }
        if (createdGameTime < 0) {
            throw new IllegalArgumentException("createdGameTime cannot be negative");
        }
    }

    public NightmareInstance withLayout(BlockPos newOrigin, BlockPos newAltar) {
        return copy(newOrigin, newAltar, pursuerId);
    }

    public NightmareInstance withPursuer(UUID entityId) {
        return copy(origin, altar, Optional.of(Objects.requireNonNull(entityId, "entityId")));
    }

    private NightmareInstance copy(BlockPos newOrigin, BlockPos newAltar, Optional<UUID> newPursuerId) {
        return new NightmareInstance(
                instanceId,
                playerId,
                slot,
                scenarioId,
                historicalRoleId,
                returnDimension,
                returnX,
                returnY,
                returnZ,
                returnYaw,
                returnPitch,
                newOrigin,
                newAltar,
                newPursuerId,
                createdGameTime
        );
    }

    public CompoundTag save() {
        LastSignalScenario.validatePersistedLayout(this);

        CompoundTag tag = new CompoundTag();
        tag.putUUID("instance_id", instanceId);
        tag.putUUID("player_id", playerId);
        tag.putInt("slot", slot);
        tag.putString("scenario_id", scenarioId);
        tag.putString("historical_role_id", historicalRoleId);
        tag.putString("return_dimension", returnDimension.toString());
        tag.putDouble("return_x", returnX);
        tag.putDouble("return_y", returnY);
        tag.putDouble("return_z", returnZ);
        tag.putFloat("return_yaw", returnYaw);
        tag.putFloat("return_pitch", returnPitch);
        tag.putLong("origin", origin.asLong());
        tag.putLong("altar", altar.asLong());
        pursuerId.ifPresent(value -> tag.putUUID("pursuer_id", value));
        tag.putLong("created_game_time", createdGameTime);
        return tag;
    }

    public static NightmareInstance load(CompoundTag tag) {
        NightmareInstance loaded = new NightmareInstance(
                tag.getUUID("instance_id"),
                tag.getUUID("player_id"),
                tag.getInt("slot"),
                tag.getString("scenario_id"),
                tag.getString("historical_role_id"),
                ResourceLocation.parse(tag.getString("return_dimension")),
                tag.getDouble("return_x"),
                tag.getDouble("return_y"),
                tag.getDouble("return_z"),
                tag.getFloat("return_yaw"),
                tag.getFloat("return_pitch"),
                BlockPos.of(tag.getLong("origin")),
                BlockPos.of(tag.getLong("altar")),
                tag.hasUUID("pursuer_id") ? Optional.of(tag.getUUID("pursuer_id")) : Optional.empty(),
                tag.getLong("created_game_time")
        );
        LastSignalScenario.validatePersistedLayout(loaded);
        return loaded;
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }
}
