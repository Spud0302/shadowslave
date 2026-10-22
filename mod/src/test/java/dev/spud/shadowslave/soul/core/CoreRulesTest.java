package dev.spud.shadowslave.soul.core;

import dev.spud.shadowslave.soul.SoulRank;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoreRulesTest {
    @Test
    void sameOrStrongerShardsFillAtFullValue() {
        assertEquals(4, CoreRules.absorbValue(ShardRank.DORMANT, ShardRank.DORMANT.value(), SoulRank.DORMANT));
        assertEquals(240, CoreRules.absorbValue(ShardRank.CORRUPTED, ShardRank.CORRUPTED.value(), SoulRank.DORMANT));
        assertEquals(20, CoreRules.absorbValue(ShardRank.AWAKENED, ShardRank.AWAKENED.value(), SoulRank.AWAKENED));
    }

    @Test
    void weakerShardsHelpAStrongerCoreAQuarterPerRank() {
        assertEquals(1, CoreRules.absorbValue(ShardRank.DORMANT, ShardRank.DORMANT.value(), SoulRank.AWAKENED));
        assertEquals(5, CoreRules.absorbValue(ShardRank.AWAKENED, 20, SoulRank.ASCENDED));
        assertEquals(1, CoreRules.absorbValue(ShardRank.DORMANT, 4, SoulRank.ASCENDED), "never less than one");
    }

    @Test
    void aDormantCoreSaturatesAtItsLimit() {
        assertEquals(40, CoreRules.limit(SoulRank.DORMANT) / ShardRank.DORMANT.value(), "forty Dormant shards");
        assertFalse(CoreRules.saturated(159, SoulRank.DORMANT));
        assertTrue(CoreRules.saturated(160, SoulRank.DORMANT));
        assertEquals(0.5F, CoreRules.fraction(80, SoulRank.DORMANT));
    }

    @Test
    void healthBonusGrowsWithFillAndKeepsPassedRanks() {
        assertEquals(0.0, CoreRules.healthBonus(0, SoulRank.DORMANT));
        assertEquals(2.0, CoreRules.healthBonus(80, SoulRank.DORMANT));
        assertEquals(4.0, CoreRules.healthBonus(0, SoulRank.AWAKENED));
        assertEquals(8.0, CoreRules.healthBonus(600, SoulRank.AWAKENED));
    }

    @Test
    void pvpTakesAQuarterRoundedUp() {
        assertEquals(0, CoreRules.pvpLoss(0));
        assertEquals(1, CoreRules.pvpLoss(1));
        assertEquals(40, CoreRules.pvpLoss(160));
        assertEquals(26, CoreRules.pvpLoss(101));
    }
}
