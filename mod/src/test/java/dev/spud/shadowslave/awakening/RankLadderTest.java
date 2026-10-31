package dev.spud.shadowslave.awakening;

import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SpellState;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RankLadderTest {
    @Test
    void eachStageRisesToTheNextUpToSaint() {
        assertEquals(Optional.empty(), AwakeningService.nextRank(SpellState.DREAMER, false), "no Aspect named yet");
        assertEquals(Optional.of(SoulRank.AWAKENED), AwakeningService.nextRank(SpellState.DREAMER, true));
        assertEquals(Optional.of(SoulRank.ASCENDED), AwakeningService.nextRank(SpellState.AWAKENED, true));
        assertEquals(Optional.of(SoulRank.TRANSCENDENT), AwakeningService.nextRank(SpellState.MASTER, true));
        assertEquals(Optional.empty(), AwakeningService.nextRank(SpellState.SAINT, true), "Sovereign is not built");
    }

    @Test
    void theSpellNamesMastersAndSaints() {
        assertEquals("Master", AwakeningService.title(SoulRank.ASCENDED));
        assertEquals("Saint", AwakeningService.title(SoulRank.TRANSCENDENT));
    }
}
