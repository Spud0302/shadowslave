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

    /** Grown by the Thorn Matron, and wild in the Thornwake Basin. */
    public static final DeferredBlock<dev.spud.shadowslave.creature.BlackBriarBlock> BLACK_BRIAR = BLOCKS.registerBlock(
            "black_briar",
            dev.spud.shadowslave.creature.BlackBriarBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(net.minecraft.world.level.material.MapColor.COLOR_BLACK)
                    .strength(0.8F)
                    .sound(net.minecraft.world.level.block.SoundType.SWEET_BERRY_BUSH)
                    .noOcclusion()
                    .isViewBlocking((state, level, pos) -> false)
                    .ignitedByLava()
                    .noLootTable()
                    .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
    );

    private ModBlocks() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
