package dev.spud.shadowslave.weaving;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.memory.MemoryInstanceData;
import net.minecraft.Util;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.core.Holder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.SimpleTier;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The Weaving table, its menu, the creature materials it weaves with, the
 * Forging Memories it weaves, and the player's Weaving progress. Which items
 * a pattern needs and makes lives in data ({@link WeavingContentLoader});
 * only what an item is lives here.
 */
public final class WeavingRegistry {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ShadowSlaveMod.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ShadowSlaveMod.MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ShadowSlaveMod.MOD_ID);
    private static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, ShadowSlaveMod.MOD_ID);
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ShadowSlaveMod.MOD_ID);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ShadowSlaveMod.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<WeavingData>> WEAVING = ATTACHMENTS.register("weaving",
            () -> AttachmentType.builder(WeavingData::empty).serialize(WeavingData.CODEC.codec()).copyOnDeath().build());

    /** Who wove a Memory and from what, stamped on the item when it leaves the table. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MemoryInstanceData>> WOVEN_MEMORY =
            COMPONENTS.registerComponentType("woven_memory", builder -> builder
                    .persistent(MemoryInstanceData.CODEC.codec())
                    .networkSynchronized(ByteBufCodecs.fromCodec(MemoryInstanceData.CODEC.codec())));

    /** The strands laid into a piece, or the single strand a Woven Thread carries. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<WovenStrands>> WOVEN_STRANDS =
            COMPONENTS.registerComponentType("woven_strands", builder -> builder
                    .persistent(WovenStrands.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodec(WovenStrands.CODEC)));

    /** Who a Bound Memory has bonded to. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BoundTo>> BOUND_TO =
            COMPONENTS.registerComponentType("bound_to", builder -> builder
                    .persistent(BoundTo.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodec(BoundTo.CODEC)));

    /** The Memories a player has dismissed into their soul. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<SoulMemories>> SOUL_MEMORIES = ATTACHMENTS.register("soul_memories",
            () -> AttachmentType.builder(() -> SoulMemories.EMPTY).serialize(SoulMemories.CODEC).copyOnDeath().build());

    public static final DeferredBlock<WeavingTableBlock> WEAVING_TABLE = BLOCKS.registerBlock("weaving_table",
            WeavingTableBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.LOOM));
    public static final DeferredItem<BlockItem> WEAVING_TABLE_ITEM = ITEMS.registerSimpleBlockItem(WEAVING_TABLE);

    public static final DeferredHolder<MenuType<?>, MenuType<WeavingMenu>> WEAVING_MENU = MENUS.register("weaving_table",
            () -> new MenuType<>(WeavingMenu::new, FeatureFlags.DEFAULT_FLAGS));

    // Creature materials (loot tables drop them; patterns name them).
    public static final DeferredItem<Item> ASHEN_HIDE = ITEMS.registerSimpleItem("ashen_hide");
    public static final DeferredItem<Item> CHAINBACK_LINK = ITEMS.registerSimpleItem("chainback_link",
            new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> MIMIC_HUSK = ITEMS.registerSimpleItem("mimic_husk",
            new Item.Properties().rarity(Rarity.UNCOMMON));

    /** A strand unravelled out of one piece, ready to lay into another. */
    public static final DeferredItem<WovenThreadItem> WOVEN_THREAD = ITEMS.registerItem("woven_thread",
            WovenThreadItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    // Woven weapons get sturdier with Tier; they never take a repair material.
    private static final Tier WOVEN_I = new SimpleTier(BlockTags.INCORRECT_FOR_IRON_TOOL, 300, 6.0F, 2.0F, 14, () -> Ingredient.EMPTY);
    private static final Tier WOVEN_II = new SimpleTier(BlockTags.INCORRECT_FOR_IRON_TOOL, 600, 6.5F, 3.0F, 14, () -> Ingredient.EMPTY);
    private static final Tier WOVEN_III = new SimpleTier(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 900, 7.0F, 4.0F, 14, () -> Ingredient.EMPTY);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> WOVEN_CLOTH = armour("woven_cloth",
            new int[]{1, 2, 3, 1}, 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F);
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ASHEN_HIDE_MATERIAL = armour("ashen_hide",
            new int[]{1, 3, 4, 2}, 12, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F);
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> CHAINBACK_MATERIAL = armour("chainback",
            new int[]{2, 5, 6, 2}, 12, SoundEvents.ARMOR_EQUIP_CHAIN, 1.0F, 0.05F);
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> HOLLOW_MATERIAL = armour("hollow",
            new int[]{2, 5, 6, 3}, 14, SoundEvents.ARMOR_EQUIP_IRON, 1.0F, 0.0F);

    public static final DeferredItem<WovenMemoryItems.Sword> SHARD_KNIFE = sword("shard_knife", WOVEN_I, 2, -2.0F);
    public static final DeferredItem<WovenMemoryItems.Armour> SLEEPERS_HOOD = armourPiece("sleepers_hood", WOVEN_CLOTH, ArmorItem.Type.HELMET, 8);
    public static final DeferredItem<WovenMemoryItems.Armour> WANDERERS_BOOTS = armourPiece("wanderers_boots", WOVEN_CLOTH, ArmorItem.Type.BOOTS, 8);
    public static final DeferredItem<WovenMemoryItems.Armour> ASHEN_HIDE_VEST = armourPiece("ashen_hide_vest", ASHEN_HIDE_MATERIAL, ArmorItem.Type.CHESTPLATE, 10);
    public static final DeferredItem<WovenMemoryItems.Sword> LINKED_CLEAVER = sword("linked_cleaver", WOVEN_II, 4, -3.0F);
    public static final DeferredItem<WovenMemoryItems.Armour> CHAINBACK_MAIL = armourPiece("chainback_mail", CHAINBACK_MATERIAL, ArmorItem.Type.CHESTPLATE, 16);
    public static final DeferredItem<WovenMemoryItems.Armour> HOLLOW_VISAGE = armourPiece("hollow_visage", HOLLOW_MATERIAL, ArmorItem.Type.HELMET, 20);
    public static final DeferredItem<WovenMemoryItems.Sword> HOLLOW_EDGE = sword("hollow_edge", WOVEN_III, 3, -2.4F);

    private static final List<DeferredItem<? extends Item>> MEMORIES = List.of(
            SHARD_KNIFE, SLEEPERS_HOOD, WANDERERS_BOOTS, ASHEN_HIDE_VEST, LINKED_CLEAVER, CHAINBACK_MAIL, HOLLOW_VISAGE, HOLLOW_EDGE);

    private WeavingRegistry() {
    }

    private static DeferredItem<WovenMemoryItems.Sword> sword(String name, Tier tier, int damage, float speed) {
        return ITEMS.registerItem(name, properties -> new WovenMemoryItems.Sword(memoryId(name), tier, properties),
                new Item.Properties().rarity(Rarity.UNCOMMON).attributes(SwordItem.createAttributes(tier, damage, speed)));
    }

    private static DeferredItem<WovenMemoryItems.Armour> armourPiece(String name, Holder<ArmorMaterial> material,
                                                                      ArmorItem.Type type, int durability) {
        return ITEMS.registerItem(name, properties -> new WovenMemoryItems.Armour(memoryId(name), material, type, properties),
                new Item.Properties().rarity(Rarity.UNCOMMON).durability(type.getDurability(durability)));
    }

    /** Defence for boots, leggings, chestplate and helmet, in that order. */
    private static DeferredHolder<ArmorMaterial, ArmorMaterial> armour(String name, int[] defence, int enchantability,
                                                                      Holder<net.minecraft.sounds.SoundEvent> sound,
                                                                      float toughness, float knockback) {
        return ARMOR_MATERIALS.register(name, () -> new ArmorMaterial(
                Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                    map.put(ArmorItem.Type.BOOTS, defence[0]);
                    map.put(ArmorItem.Type.LEGGINGS, defence[1]);
                    map.put(ArmorItem.Type.CHESTPLATE, defence[2]);
                    map.put(ArmorItem.Type.HELMET, defence[3]);
                    map.put(ArmorItem.Type.BODY, defence[2]);
                }),
                enchantability, sound, () -> Ingredient.EMPTY,
                List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, name))),
                toughness, knockback));
    }

    static ResourceLocation memoryId(String name) {
        return ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "memory/" + name);
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        MENUS.register(modEventBus);
        ARMOR_MATERIALS.register(modEventBus);
        COMPONENTS.register(modEventBus);
        ATTACHMENTS.register(modEventBus);
        modEventBus.addListener(WeavingRegistry::addToCreativeTabs);
    }

    private static void addToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(WEAVING_TABLE_ITEM);
        }
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ASHEN_HIDE);
            event.accept(CHAINBACK_LINK);
            event.accept(MIMIC_HUSK);
        }
        // Woven Threads only exist with a strand in them, so they stay out of the creative tabs.
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            MEMORIES.forEach(event::accept);
        }
    }

    /** Every woven Memory item, keyed by its Memory id. */
    public static Map<ResourceLocation, Item> memoryItems() {
        java.util.LinkedHashMap<ResourceLocation, Item> items = new java.util.LinkedHashMap<>();
        MEMORIES.forEach(item -> items.put(memoryId(item.getId().getPath()), item.get()));
        return items;
    }
}
