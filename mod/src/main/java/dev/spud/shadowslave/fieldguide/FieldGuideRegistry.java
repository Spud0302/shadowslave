package dev.spud.shadowslave.fieldguide;

import dev.spud.shadowslave.ShadowSlaveMod;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * The Field Guide, Field Note and Field Guide board, what each player's guide
 * knows, and the loot pieces behind lost notes. Pages, rules, recipes and
 * which chests hold lost notes live in data.
 */
public final class FieldGuideRegistry {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ShadowSlaveMod.MOD_ID);
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ShadowSlaveMod.MOD_ID);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ShadowSlaveMod.MOD_ID);
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ShadowSlaveMod.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ShadowSlaveMod.MOD_ID);
    private static final DeferredRegister<LootItemFunctionType<?>> LOOT_FUNCTIONS =
            DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, ShadowSlaveMod.MOD_ID);
    private static final DeferredRegister<LootItemConditionType> LOOT_CONDITIONS =
            DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, ShadowSlaveMod.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<FieldGuideData>> FIELD_GUIDE = ATTACHMENTS.register("field_guide",
            () -> AttachmentType.builder(FieldGuideData::empty).serialize(FieldGuideData.CODEC).copyOnDeath().build());

    public static final DeferredItem<FieldGuideItem> FIELD_GUIDE_ITEM = ITEMS.registerItem("field_guide", FieldGuideItem::new,
            new Item.Properties().stacksTo(1));

    /** A True Name written on a Field Note. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<WrittenName>> WRITTEN_NAME =
            COMPONENTS.registerComponentType("written_name", builder -> builder
                    .persistent(WrittenName.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodec(WrittenName.CODEC)));

    /** A page of a Field Guide copied onto a Field Note. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FieldNotePage>> NOTE_PAGE =
            COMPONENTS.registerComponentType("note_page", builder -> builder
                    .persistent(FieldNotePage.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodec(FieldNotePage.CODEC)));

    public static final DeferredItem<FieldNoteItem> FIELD_NOTE_ITEM = ITEMS.registerItem("field_note", FieldNoteItem::new,
            new Item.Properties().stacksTo(16));

    public static final DeferredBlock<FieldBoardBlock> FIELD_BOARD = BLOCKS.registerBlock("field_board", FieldBoardBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_PLANKS).noOcclusion());
    public static final DeferredItem<BlockItem> FIELD_BOARD_ITEM = ITEMS.registerSimpleBlockItem(FIELD_BOARD);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FieldBoardBlockEntity>> FIELD_BOARD_ENTITY =
            BLOCK_ENTITIES.register("field_board", () -> BlockEntityType.Builder.of(FieldBoardBlockEntity::new, FIELD_BOARD.get()).build(null));

    public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<LostFieldNoteFunction>> LOST_FIELD_NOTE =
            LOOT_FUNCTIONS.register("lost_field_note", () -> new LootItemFunctionType<>(LostFieldNoteFunction.CODEC));
    public static final DeferredHolder<LootItemConditionType, LootItemConditionType> LOST_NOTES_ENABLED =
            LOOT_CONDITIONS.register("lost_notes_enabled", () -> new LootItemConditionType(LostNotesCondition.CODEC));

    private FieldGuideRegistry() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        COMPONENTS.register(modEventBus);
        ATTACHMENTS.register(modEventBus);
        BLOCKS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        LOOT_FUNCTIONS.register(modEventBus);
        LOOT_CONDITIONS.register(modEventBus);
        modEventBus.addListener(FieldGuideRegistry::addToCreativeTabs);
    }

    private static void addToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(FIELD_GUIDE_ITEM);
            event.accept(FIELD_NOTE_ITEM);
        }
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(FIELD_BOARD_ITEM);
        }
    }
}
