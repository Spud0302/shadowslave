package dev.spud.shadowslave.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A candle bound with an amethyst shard. Lit beside a bed, it lets the Spell
 * name the sleeper's Aspect when they wake (see DreamCandleRitual).
 */
public class DreamCandleBlock extends CandleBlock {
    public DreamCandleBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (state.getValue(LIT) && random.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.REVERSE_PORTAL,
                    pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.4,
                    pos.getY() + 0.6,
                    pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.4,
                    0.0, 0.02, 0.0);
        }
    }
}
