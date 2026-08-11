package dev.spud.shadowslave.item;

import dev.spud.shadowslave.ShadowSlaveMod;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Physical item registrations. Canonical ownership remains in Java attachments. */
public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ShadowSlaveMod.MOD_ID);

    public static final DeferredItem<Item> ASH_COMPASS_MEMORY = ITEMS.registerItem("ash_compass_memory", AshCompassMemoryItem::new, new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> BELLGLASS_TOKEN_MEMORY = ITEMS.registerItem("bellglass_token_memory", BellglassTokenMemoryItem::new, new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> RED_THREAD_BRACELET_MEMORY = ITEMS.registerItem("red_thread_bracelet_memory", RedThreadBraceletMemoryItem::new, new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> BORROWED_DAWN_MEMORY = ITEMS.registerItem("borrowed_dawn_memory", BorrowedDawnMemoryItem::new, new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> VEIL_STITCH_CASE_MEMORY = ITEMS.registerItem("veil_stitch_case_memory", VeilStitchCaseMemoryItem::new, new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> MIREWALKER_BOOTS_MEMORY = ITEMS.registerItem("mirewalker_boots_memory", MirewalkerBootsMemoryItem::new, new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> STONEWAKE_SHIELD_MEMORY = ITEMS.registerItem("stonewake_shield_memory", StonewakeShieldMemoryItem::new, new Item.Properties().stacksTo(1));

    private ModItems() {}
    public static void register(IEventBus modEventBus) { ITEMS.register(modEventBus); }
}
