package dev.spud.shadowslave.item;

import dev.spud.shadowslave.ShadowSlaveMod;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
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

    public static final DeferredItem<Item> BELLGLASS_TOKEN_MEMORY = ITEMS.registerItem(
            "bellglass_token_memory",
            BellglassTokenMemoryItem::new,
            new Item.Properties().stacksTo(1)
    );

    private ModItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}