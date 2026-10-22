package dev.spud.shadowslave.item;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.block.ModBlocks;
import dev.spud.shadowslave.soul.core.ShardRank;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
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

    public static final DeferredItem<SoulShardItem> DORMANT_SOUL_SHARD = shard("dormant_soul_shard", ShardRank.DORMANT, Rarity.COMMON);
    public static final DeferredItem<SoulShardItem> AWAKENED_SOUL_SHARD = shard("awakened_soul_shard", ShardRank.AWAKENED, Rarity.UNCOMMON);
    public static final DeferredItem<SoulShardItem> FALLEN_SOUL_SHARD = shard("fallen_soul_shard", ShardRank.FALLEN, Rarity.RARE);
    public static final DeferredItem<SoulShardItem> CORRUPTED_SOUL_SHARD = shard("corrupted_soul_shard", ShardRank.CORRUPTED, Rarity.EPIC);

    private ModItems() {
    }

    private static DeferredItem<SoulShardItem> shard(String name, ShardRank rank, Rarity rarity) {
        return ITEMS.registerItem(name, properties -> new SoulShardItem(rank, properties), new Item.Properties().rarity(rarity));
    }

    /** The shard item for a creature rank. */
    public static SoulShardItem shardFor(ShardRank rank) {
        return switch (rank) {
            case DORMANT -> DORMANT_SOUL_SHARD.get();
            case AWAKENED -> AWAKENED_SOUL_SHARD.get();
            case FALLEN -> FALLEN_SOUL_SHARD.get();
            case CORRUPTED -> CORRUPTED_SOUL_SHARD.get();
        };
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        modEventBus.addListener(ModItems::addToCreativeTabs);
    }

    private static void addToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(DREAM_CANDLE);
        }
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(DORMANT_SOUL_SHARD);
            event.accept(AWAKENED_SOUL_SHARD);
            event.accept(FALLEN_SOUL_SHARD);
            event.accept(CORRUPTED_SOUL_SHARD);
        }
    }
}
