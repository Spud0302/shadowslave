package dev.spud.shadowslave.smithing;

import dev.spud.shadowslave.ShadowSlaveMod;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Physical Smithing (DESIGN, Jack 2026-10-28): the Forge Hearth, Smithing
 * Anvil and Quench Trough, the Smith's Hammer, the billet a piece is worked
 * as, and the components that carry the work and the finished grade. Which
 * patterns exist lives in data ({@code smithing/pattern}).
 */
public final class SmithingRegistry {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ShadowSlaveMod.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ShadowSlaveMod.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ShadowSlaveMod.MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ShadowSlaveMod.MOD_ID);
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ShadowSlaveMod.MOD_ID);

    /** A piece still being worked. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BilletData>> BILLET =
            COMPONENTS.registerComponentType("billet", builder -> builder
                    .persistent(BilletData.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodec(BilletData.CODEC)));
    /** A finished piece's grade and smith. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ForgedData>> FORGED =
            COMPONENTS.registerComponentType("forged", builder -> builder
                    .persistent(ForgedData.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodec(ForgedData.CODEC)));

    public static final DeferredBlock<ForgeHearthBlock> FORGE_HEARTH = BLOCKS.registerBlock("forge_hearth",
            ForgeHearthBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.BLAST_FURNACE)
                    .lightLevel(state -> state.getValue(ForgeHearthBlock.LIT) ? 13 : 0));
    public static final DeferredItem<BlockItem> FORGE_HEARTH_ITEM = ITEMS.registerSimpleBlockItem(FORGE_HEARTH);

    public static final DeferredBlock<SmithingAnvilBlock> SMITHING_ANVIL = BLOCKS.registerBlock("smithing_anvil",
            SmithingAnvilBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.ANVIL));
    public static final DeferredItem<BlockItem> SMITHING_ANVIL_ITEM = ITEMS.registerSimpleBlockItem(SMITHING_ANVIL);

    public static final DeferredBlock<QuenchTroughBlock> QUENCH_TROUGH = BLOCKS.registerBlock("quench_trough",
            QuenchTroughBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL).noOcclusion());
    public static final DeferredItem<BlockItem> QUENCH_TROUGH_ITEM = ITEMS.registerSimpleBlockItem(QUENCH_TROUGH);

    public static final DeferredItem<SmithsHammerItem> SMITHS_HAMMER = ITEMS.registerItem("smiths_hammer",
            SmithsHammerItem::new, new Item.Properties().durability(250));
    public static final DeferredItem<BilletItem> BILLET_ITEM = ITEMS.registerItem("billet",
            BilletItem::new, new Item.Properties().stacksTo(1));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ForgeHearthBlockEntity>> FORGE_HEARTH_ENTITY =
            BLOCK_ENTITIES.register("forge_hearth",
                    () -> BlockEntityType.Builder.of(ForgeHearthBlockEntity::new, FORGE_HEARTH.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SmithingAnvilBlockEntity>> SMITHING_ANVIL_ENTITY =
            BLOCK_ENTITIES.register("smithing_anvil",
                    () -> BlockEntityType.Builder.of(SmithingAnvilBlockEntity::new, SMITHING_ANVIL.get()).build(null));

    public static final DeferredHolder<MenuType<?>, MenuType<ForgeHearthMenu>> FORGE_HEARTH_MENU = MENUS.register("forge_hearth",
            () -> new MenuType<>(ForgeHearthMenu::new, FeatureFlags.DEFAULT_FLAGS));

    private SmithingRegistry() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENUS.register(modEventBus);
        COMPONENTS.register(modEventBus);
        modEventBus.addListener(SmithingRegistry::addToCreativeTabs);
    }

    private static void addToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(FORGE_HEARTH_ITEM);
            event.accept(SMITHING_ANVIL_ITEM);
            event.accept(QUENCH_TROUGH_ITEM);
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(SMITHS_HAMMER);
        }
    }
}
