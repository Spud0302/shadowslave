package dev.spud.shadowslave.gate;

import dev.spud.shadowslave.ShadowSlaveMod;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The Gate Lure block, its item, block entity and charge component. The rift entity is in NightmareCreatureEntities. */
public final class GateRegistry {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ShadowSlaveMod.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ShadowSlaveMod.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ShadowSlaveMod.MOD_ID);
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ShadowSlaveMod.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<LureCharge>> LURE_CHARGE =
            COMPONENTS.registerComponentType("lure_charge", builder -> builder
                    .persistent(LureCharge.CODEC)
                    .networkSynchronized(LureCharge.STREAM_CODEC));

    public static final DeferredBlock<GateLureBlock> GATE_LURE = BLOCKS.registerBlock("gate_lure", GateLureBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.CRYING_OBSIDIAN)
                    .strength(8.0F, 1200.0F)
                    .noOcclusion()
                    .lightLevel(state -> 3 * state.getValue(GateLureBlock.CHARGE)));
    public static final DeferredItem<BlockItem> GATE_LURE_ITEM = ITEMS.registerSimpleBlockItem(GATE_LURE,
            new Item.Properties().rarity(Rarity.RARE));
    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GateLureBlockEntity>> GATE_LURE_ENTITY =
            BLOCK_ENTITIES.register("gate_lure", () -> BlockEntityType.Builder.of(GateLureBlockEntity::new, GATE_LURE.get()).build(null));

    private GateRegistry() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        COMPONENTS.register(modEventBus);
        modEventBus.addListener(GateRegistry::addToCreativeTabs);
    }

    private static void addToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(GATE_LURE_ITEM);
        }
    }
}
