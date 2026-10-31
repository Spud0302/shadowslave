package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.soul.SoulRank;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RankPowersTest {
    @Test
    void abilitiesFromBelowYourRankAreMasteredOnceYouAreAMaster() {
        assertFalse(RankPowers.masteredAt(Optional.of(SoulRank.AWAKENED), Optional.of(SoulRank.DORMANT)));
        assertTrue(RankPowers.masteredAt(Optional.of(SoulRank.ASCENDED), Optional.of(SoulRank.DORMANT)));
        assertTrue(RankPowers.masteredAt(Optional.of(SoulRank.ASCENDED), Optional.of(SoulRank.AWAKENED)));
        assertFalse(RankPowers.masteredAt(Optional.of(SoulRank.ASCENDED), Optional.of(SoulRank.ASCENDED)), "a Master's own ability");
        assertTrue(RankPowers.masteredAt(Optional.of(SoulRank.TRANSCENDENT), Optional.of(SoulRank.ASCENDED)));
        assertFalse(RankPowers.masteredAt(Optional.empty(), Optional.empty()));
    }

    @Test
    void powerGrowsAQuarterForEachRankPastAwakened() {
        assertEquals(1.0F, RankPowers.powerFor(Optional.of(SoulRank.DORMANT)));
        assertEquals(1.0F, RankPowers.powerFor(Optional.of(SoulRank.AWAKENED)));
        assertEquals(1.25F, RankPowers.powerFor(Optional.of(SoulRank.ASCENDED)));
        assertEquals(1.5F, RankPowers.powerFor(Optional.of(SoulRank.TRANSCENDENT)));
        assertEquals(1.0F, RankPowers.powerFor(Optional.empty()));
    }
}
