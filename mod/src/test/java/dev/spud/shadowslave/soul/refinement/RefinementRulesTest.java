package dev.spud.shadowslave.soul.refinement;

import dev.spud.shadowslave.soul.SoulRank;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RefinementRulesTest {
    private static final Optional<SoulRank> DORMANT = Optional.of(SoulRank.DORMANT);
    private static final Optional<SoulRank> AWAKENED = Optional.of(SoulRank.AWAKENED);

    @Test
    void oneAtNamingAndThreeMoreAtAwakening() {
        assertEquals(0, RefinementRules.pointsFor(Optional.empty()));
        assertEquals(1, RefinementRules.pointsFor(DORMANT));
        assertEquals(4, RefinementRules.pointsFor(AWAKENED));
    }

    @Test
    void placedPointsAndRespecsComeOutOfWhatWasEarned() {
        RefinementData data = RefinementData.empty().plus(RefinementStat.BODY).plus(RefinementStat.SOUL);
        assertEquals(2, RefinementRules.unspent(AWAKENED, data));
        assertTrue(RefinementRules.canRespec(AWAKENED, data));
        RefinementData respecced = data.respecced();
        assertEquals(3, RefinementRules.unspent(AWAKENED, respecced), "a respec loses one point");
        assertFalse(RefinementRules.canRespec(AWAKENED, respecced), "nothing placed, nothing to respec");
    }

    @Test
    void aspectPointsCutCostsUpToThirtyPercent() {
        assertEquals(35, RefinementRules.cost(35, RefinementData.empty()));
        assertEquals(33, RefinementRules.cost(35, new RefinementData(0, 0, 1, 0)));
        assertEquals(25, RefinementRules.cost(35, new RefinementData(0, 0, 9, 0)));
        assertEquals(1, RefinementRules.cost(1, new RefinementData(0, 0, 9, 0)));
    }

    @Test
    void soulPointsGrowThePool() {
        RefinementData data = new RefinementData(0, 2, 0, 0);
        assertEquals(30, RefinementRules.capacityBonus(data));
        assertEquals(1.2, RefinementRules.regenMultiplier(data), 1.0e-9);
    }
}
