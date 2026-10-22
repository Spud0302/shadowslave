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
import dev.spud.shadowslave.world.entity.NightmareCreatureEntities;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
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

    public static final DeferredItem<DeferredSpawnEggItem> ASH_BURROWER_SPAWN_EGG = ITEMS.registerItem("ash_burrower_spawn_egg",
            properties -> new DeferredSpawnEggItem(NightmareCreatureEntities.ASH_BURROWER, 0x3a302b, 0xd8662a, properties));
    public static final DeferredItem<DeferredSpawnEggItem> CHAINBACK_SPAWN_EGG = ITEMS.registerItem("chainback_spawn_egg",
            properties -> new DeferredSpawnEggItem(NightmareCreatureEntities.CHAINBACK, 0x2f3236, 0x8a4a22, properties));
    public static final DeferredItem<DeferredSpawnEggItem> HOLLOW_MIMIC_SPAWN_EGG = ITEMS.registerItem("hollow_mimic_spawn_egg",
            properties -> new DeferredSpawnEggItem(NightmareCreatureEntities.HOLLOW_MIMIC, 0xd9d2c3, 0x141218, properties));
    public static final DeferredItem<DeferredSpawnEggItem> ASHEN_TYRANT_SPAWN_EGG = ITEMS.registerItem("ashen_tyrant_spawn_egg",
            properties -> new DeferredSpawnEggItem(NightmareCreatureEntities.ASHEN_TYRANT, 0x26201e, 0xff7a2a, properties));

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
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ASH_BURROWER_SPAWN_EGG);
            event.accept(CHAINBACK_SPAWN_EGG);
            event.accept(HOLLOW_MIMIC_SPAWN_EGG);
            event.accept(ASHEN_TYRANT_SPAWN_EGG);
        }
    }
}
