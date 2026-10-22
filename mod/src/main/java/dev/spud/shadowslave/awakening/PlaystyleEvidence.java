package dev.spud.shadowslave.awakening;

import dev.spud.shadowslave.appraisal.generation.GenerationEvidence;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reads how a player has played before the roll from vanilla statistics and
 * turns it into evidence tags that nudge the Nature roll.
 *
 * <ul>
 *   <li>Ember: torches and lanterns placed, furnaces, smokers and campfires used.</li>
 *   <li>Mist: distance sneaked.</li>
 *   <li>Stone: blocks broken with a pickaxe.</li>
 *   <li>Tide: distance swum, walked underwater or rowed.</li>
 * </ul>
 */
public final class PlaystyleEvidence {
    static final String SCENARIO = "dream_candle";
    static final String ROLE = "dreamer";
    static final String RESOLUTION = "ritual";

    /** Points needed for evidence 1 and 2 respectively, per tag. */
    static final long EMBER_LOW = 30, EMBER_HIGH = 200;
    static final long MIST_LOW_CM = 50 * 100, MIST_HIGH_CM = 300 * 100;
    static final long STONE_LOW = 200, STONE_HIGH = 1500;
    static final long TIDE_LOW_CM = 200 * 100, TIDE_HIGH_CM = 1000 * 100;

    private PlaystyleEvidence() {
    }

    public record Totals(long lightActions, long sneakCm, long pickaxeBlocks, long waterCm) {
    }

    public static GenerationEvidence fromTotals(Totals totals) {
        Map<String, Integer> weights = new LinkedHashMap<>();
        put(weights, "ember", tier(totals.lightActions(), EMBER_LOW, EMBER_HIGH));
        put(weights, "mist", tier(totals.sneakCm(), MIST_LOW_CM, MIST_HIGH_CM));
        put(weights, "stone", tier(totals.pickaxeBlocks(), STONE_LOW, STONE_HIGH));
        put(weights, "water", tier(totals.waterCm(), TIDE_LOW_CM, TIDE_HIGH_CM));
        return new GenerationEvidence(SCENARIO, ROLE, RESOLUTION, weights);
    }

    public static GenerationEvidence from(ServerPlayer player) {
        return fromTotals(totals(player));
    }

    static Totals totals(ServerPlayer player) {
        ServerStatsCounter stats = player.getStats();
        long light = used(stats, Items.TORCH) + used(stats, Items.SOUL_TORCH)
                + used(stats, Items.LANTERN) + used(stats, Items.SOUL_LANTERN)
                + 5L * (custom(stats, Stats.INTERACT_WITH_FURNACE)
                + custom(stats, Stats.INTERACT_WITH_SMOKER)
                + custom(stats, Stats.INTERACT_WITH_BLAST_FURNACE)
                + custom(stats, Stats.INTERACT_WITH_CAMPFIRE));
        long sneak = custom(stats, Stats.CROUCH_ONE_CM);
        long pickaxe = 0L;
        for (Item item : BuiltInRegistries.ITEM) {
            if (item.builtInRegistryHolder().is(ItemTags.PICKAXES)) {
                pickaxe += used(stats, item);
            }
        }
        long water = custom(stats, Stats.SWIM_ONE_CM)
                + custom(stats, Stats.WALK_UNDER_WATER_ONE_CM)
                + custom(stats, Stats.WALK_ON_WATER_ONE_CM)
                + custom(stats, Stats.BOAT_ONE_CM);
        return new Totals(light, sneak, pickaxe, water);
    }

    static int tier(long value, long low, long high) {
        if (value >= high) {
            return 2;
        }
        return value >= low ? 1 : 0;
    }

    private static void put(Map<String, Integer> weights, String tag, int weight) {
        if (weight > 0) {
            weights.put(tag, weight);
        }
    }

    private static long used(ServerStatsCounter stats, Item item) {
        return stats.getValue(Stats.ITEM_USED.get(item));
    }

    private static long custom(ServerStatsCounter stats, ResourceLocation stat) {
        return stats.getValue(Stats.CUSTOM.get(stat));
    }
}
