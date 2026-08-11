package dev.spud.shadowslave.world.block;

import dev.spud.shadowslave.ShadowSlaveMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Physical world registrations. Java content catalogs remain authoritative for resource identity. */
public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(
            BuiltInRegistries.BLOCK, ShadowSlaveMod.MOD_ID);

    /** DESIGN presentation block for the existing Ashen Expanse ruin_metal resource hook. */
    public static final DeferredHolder<Block, Block> RUIN_METAL = BLOCKS.register(
            "ruin_metal",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(3.0F, 6.0F)
                    .sound(SoundType.METAL))
    );

    private ModBlocks() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
