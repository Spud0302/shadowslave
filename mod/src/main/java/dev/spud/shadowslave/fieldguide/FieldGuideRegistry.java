package dev.spud.shadowslave.fieldguide;

import dev.spud.shadowslave.ShadowSlaveMod;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** The Field Guide item and what each player's guide knows. Pages, rules and the recipe live in data. */
public final class FieldGuideRegistry {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ShadowSlaveMod.MOD_ID);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ShadowSlaveMod.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<FieldGuideData>> FIELD_GUIDE = ATTACHMENTS.register("field_guide",
            () -> AttachmentType.builder(FieldGuideData::empty).serialize(FieldGuideData.CODEC).copyOnDeath().build());

    public static final DeferredItem<FieldGuideItem> FIELD_GUIDE_ITEM = ITEMS.registerItem("field_guide", FieldGuideItem::new,
            new Item.Properties().stacksTo(1));

    private FieldGuideRegistry() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        ATTACHMENTS.register(modEventBus);
        modEventBus.addListener(FieldGuideRegistry::addToCreativeTabs);
    }

    private static void addToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(FIELD_GUIDE_ITEM);
        }
    }
}
