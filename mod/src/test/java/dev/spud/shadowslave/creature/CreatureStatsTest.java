package dev.spud.shadowslave.creature;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreatureStatsTest {
    private static final CreatureStats AWAKENED_MONSTER = new CreatureStats(60, 7, 8, 0.27);

    @Test
    void nativeRankAndClassKeepTheStats() {
        assertEquals(AWAKENED_MONSTER, AWAKENED_MONSTER.scaled(CreatureRank.AWAKENED, CreatureClass.MONSTER,
                CreatureRank.AWAKENED, CreatureClass.MONSTER));
    }

    @Test
    void eachRankDoublesHealthAndAddsArmour() {
        CreatureStats fallen = AWAKENED_MONSTER.scaled(CreatureRank.AWAKENED, CreatureClass.MONSTER,
                CreatureRank.FALLEN, CreatureClass.MONSTER);
        assertEquals(120, fallen.health());
        assertEquals(12, fallen.armour());
        assertTrue(fallen.damage() > AWAKENED_MONSTER.damage());
        CreatureStats dormant = AWAKENED_MONSTER.scaled(CreatureRank.AWAKENED, CreatureClass.MONSTER,
                CreatureRank.DORMANT, CreatureClass.MONSTER);
        assertEquals(30, dormant.health());
        assertEquals(4, dormant.armour());
    }

    @Test
    void moreCoresMeanMoreHealthAtTheSameRank() {
        CreatureStats tyrant = AWAKENED_MONSTER.scaled(CreatureRank.AWAKENED, CreatureClass.MONSTER,
                CreatureRank.AWAKENED, CreatureClass.TYRANT);
        CreatureStats beast = AWAKENED_MONSTER.scaled(CreatureRank.AWAKENED, CreatureClass.MONSTER,
                CreatureRank.AWAKENED, CreatureClass.BEAST);
        assertTrue(tyrant.health() > AWAKENED_MONSTER.health());
        assertTrue(beast.health() < AWAKENED_MONSTER.health());
        assertTrue(beast.health() >= 1);
    }

    @Test
    void classesCountTheirCores() {
        assertEquals(1, CreatureClass.BEAST.cores());
        assertEquals(5, CreatureClass.TYRANT.cores());
        assertEquals(7, CreatureClass.TITAN.cores());
    }

    @Test
    void ranksMapToShardsAndStepDown() {
        assertEquals(CreatureRank.DORMANT, CreatureRank.DORMANT.lower());
        assertEquals(CreatureRank.AWAKENED, CreatureRank.FALLEN.lower());
        assertEquals(CreatureRank.AWAKENED, CreatureRank.of(dev.spud.shadowslave.soul.core.ShardRank.AWAKENED));
    }

    @Test
    void statsMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> new CreatureStats(0, 1, 0, 0.2));
    }
}
