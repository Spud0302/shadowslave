package dev.spud.shadowslave.weaving;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A loom strung with soul-thread. Anyone can use it: lay creature materials
 * and soul shards on it and weave a Memory (see {@link WeavingService}).
 */
public class WeavingTableBlock extends Block {
    public WeavingTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player instanceof ServerPlayer serverPlayer) {
            WeavingService.open(serverPlayer, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(6) == 0) {
            level.addParticle(ParticleTypes.ENCHANT,
                    pos.getX() + 0.2 + random.nextDouble() * 0.6,
                    pos.getY() + 1.1,
                    pos.getZ() + 0.2 + random.nextDouble() * 0.6,
                    0.0, 0.05, 0.0);
        }
    }
}
