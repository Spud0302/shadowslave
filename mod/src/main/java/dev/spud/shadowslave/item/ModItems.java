package dev.spud.shadowslave.item;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.block.ModBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Physical item registrations. Canonical ownership remains in Java attachments. */
public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ShadowSlaveMod.MOD_ID);

    public static final DeferredItem<Item> ASH_COMPASS_MEMORY = ITEMS.registerItem(
            "ash_compass_memory",
            AshCompassMemoryItem::new,
            new Item.Properties().stacksTo(1)
    );

    public static final DeferredItem<Item> GLASS_ROAD_MEMORY = ITEMS.registerItem(
            "glass_road_memory",
            GlassRoadMemoryItem::new,
            new Item.Properties().stacksTo(1)
    );

    public static final DeferredItem<BlockItem> DREAM_CANDLE = ITEMS.registerSimpleBlockItem(ModBlocks.DREAM_CANDLE);

    private ModItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        modEventBus.addListener(ModItems::addToCreativeTabs);
    }

    private static void addToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(DREAM_CANDLE);
        }
    }
}
