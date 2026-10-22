package dev.spud.shadowslave.block;

import dev.spud.shadowslave.ShadowSlaveMod;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Block registrations. */
public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ShadowSlaveMod.MOD_ID);

    public static final DeferredBlock<Block> DREAM_CANDLE = BLOCKS.registerBlock(
            "dream_candle",
            DreamCandleBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.PURPLE_CANDLE)
    );

    private ModBlocks() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
