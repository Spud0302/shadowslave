package dev.spud.shadowslave.awakening;

import dev.spud.shadowslave.block.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;

import java.util.Optional;

/**
 * The opt-in roll trigger: sleep (at least the few seconds vanilla needs to
 * skip the night) with a lit Dream Candle beside the bed, and the Spell names
 * your Aspect when you wake. Without a candle, sleep stays vanilla.
 */
public final class DreamCandleRitual {
    /** Horizontal reach from the bed's head block; covers both sides and the foot. */
    static final int HORIZONTAL_REACH = 2;
    static final int VERTICAL_REACH = 1;

    private DreamCandleRitual() {
    }

    public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.isSleepingLongEnough()) {
            return;
        }
        Optional<BlockPos> bed = player.getSleepingPos();
        if (bed.isEmpty()) {
            return;
        }
        ServerLevel level = player.serverLevel();
        Optional<BlockPos> candle = findLitCandle(level, bed.get());
        if (candle.isEmpty()) {
            return;
        }

        if (AspectRollService.hasAspect(player)) {
            player.displayClientMessage(Component.literal("The Dream Candle flickers. Your Aspect is already known.")
                    .withStyle(ChatFormatting.GRAY), true);
            return;
        }

        AspectRoller.Roll roll;
        try {
            roll = AspectRollService.roll(player);
        } catch (RuntimeException exception) {
            player.displayClientMessage(Component.literal(exception.getMessage()).withStyle(ChatFormatting.RED), true);
            return;
        }
        burnDown(level, candle.get());
        AspectRollService.reveal(player, roll);
    }

    public static Optional<BlockPos> findLitCandle(ServerLevel level, BlockPos bed) {
        for (BlockPos pos : BlockPos.betweenClosed(
                bed.offset(-HORIZONTAL_REACH, -VERTICAL_REACH, -HORIZONTAL_REACH),
                bed.offset(HORIZONTAL_REACH, VERTICAL_REACH, HORIZONTAL_REACH))) {
            BlockState state = level.getBlockState(pos);
            if (state.is(ModBlocks.DREAM_CANDLE.get()) && state.getValue(CandleBlock.LIT)) {
                return Optional.of(pos.immutable());
            }
        }
        return Optional.empty();
    }

    /** The ritual spends one candle. */
    private static void burnDown(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        int candles = state.getValue(CandleBlock.CANDLES);
        if (candles > 1) {
            level.setBlockAndUpdate(pos, state.setValue(CandleBlock.CANDLES, candles - 1).setValue(CandleBlock.LIT, false));
        } else {
            level.removeBlock(pos, false);
        }
        level.playSound(null, pos, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 0.7F);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                30, 0.3, 0.4, 0.3, 0.05);
    }
}
