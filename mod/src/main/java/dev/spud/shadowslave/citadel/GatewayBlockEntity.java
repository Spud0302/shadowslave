package dev.spud.shadowslave.citadel;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Wakes a generated Citadel the first time its Gateway ticks, and keeps its
 * Guardian at its post while the Citadel is held. The Citadel itself lives
 * in {@link CitadelData}, keyed by the Gateway's position.
 */
public final class GatewayBlockEntity extends BlockEntity {
    private static final int INTERVAL = 20;

    public GatewayBlockEntity(BlockPos pos, BlockState state) {
        super(CitadelRegistry.GATEWAY_ENTITY.get(), pos, state);
    }

    static void serverTick(Level level, BlockPos pos, BlockState state, GatewayBlockEntity gateway) {
        if (level instanceof ServerLevel serverLevel && (level.getGameTime() + pos.asLong()) % INTERVAL == 0) {
            CitadelService.tickGateway(serverLevel, pos);
        }
    }
}
