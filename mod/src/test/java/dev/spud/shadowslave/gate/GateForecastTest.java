package dev.spud.shadowslave.gate;

import dev.spud.shadowslave.creature.CreatureRank;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GateForecastTest {
    @Test
    void oddsAlwaysAddUpToOneHundred() {
        for (int pull = GateForecast.MIN_PULL; pull < 600; pull++) {
            int total = GateForecast.fromPull(pull).odds().values().stream().mapToInt(Integer::intValue).sum();
            assertEquals(100, total, "pull " + pull);
        }
        assertThrows(IllegalArgumentException.class, () -> new GateForecast(Map.of(1, 60, 2, 30)));
    }

    @Test
    void morePullNeverLowersTheLikeliestCategory() {
        int last = 1;
        for (int pull = GateForecast.MIN_PULL; pull < 600; pull++) {
            int likeliest = GateForecast.fromPull(pull).likeliest();
            assertTrue(likeliest >= last, "pull " + pull);
            last = likeliest;
        }
        assertEquals(1, GateForecast.fromPull(GateForecast.MIN_PULL).likeliest());
        assertEquals(GateCategory.MAX, last);
    }

    @Test
    void shardRankSetsThePull() {
        assertEquals(4, GateForecast.pullQuarters(0, false), "a Dormant shard is one point");
        assertEquals(1, GateForecast.pullQuarters(0, true), "a faint one is a quarter");
        assertEquals(16, GateForecast.pullQuarters(1, false), "an Awakened shard is four Dormant ones");
        // Four Dormant shards just open a Category 1; a handful of Awakened ones reach Category 3.
        assertEquals(1, GateForecast.fromPull(4).likeliest());
        assertEquals(3, GateForecast.fromPull(6 * 4).likeliest());
    }

    @Test
    void describesLikeTheCanonWarning() {
        GateForecast forecast = new GateForecast(Map.of(2, 89, 3, 10, 1, 1));
        assertEquals("Category 2 (89%), 3 (10%), 1 (1%), higher (undefined)", forecast.describe());
        assertEquals("Category 2 (89%)", forecast.describeLikeliest());
        assertEquals("Category 4 (100%), higher (undefined)", GateForecast.certain(4).describe());
    }

    @Test
    void rollFollowsTheOdds() {
        GateForecast forecast = new GateForecast(Map.of(1, 10, 2, 85, 3, 5));
        assertEquals(1, forecast.roll(bound -> 0));
        assertEquals(1, forecast.roll(bound -> 9));
        assertEquals(2, forecast.roll(bound -> 10));
        assertEquals(2, forecast.roll(bound -> 94));
        assertEquals(3, forecast.roll(bound -> 95));
        assertEquals(3, forecast.roll(bound -> 99));
    }

    @Test
    void higherCategoriesBringMoreAndStrongerWaves() {
        int lastWaves = 0;
        for (int category = GateCategory.MIN; category <= GateCategory.MAX; category++) {
            int waves = GateCategory.waves(category).size();
            assertTrue(waves >= lastWaves, "Category " + category);
            lastWaves = waves;
            assertFalse(GateCategory.guardianRank(category).ordinal()
                    < GateCategory.guardianRank(Math.max(GateCategory.MIN, category - 1)).ordinal());
        }
        assertEquals(CreatureRank.DORMANT, GateCategory.guardianRank(1));
        assertEquals(CreatureRank.CORRUPTED, GateCategory.guardianRank(5));
    }
}
