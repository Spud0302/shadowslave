package dev.spud.shadowslave.ability;

import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KitPassivesTest {
    @Test
    void tidalDebtFallsDueAfterOneDayWithoutWater() {
        assertFalse(KitPassives.tidalDebtDue(1000L, 1000L + 23999L));
        assertTrue(KitPassives.tidalDebtDue(1000L, 1000L + 24000L));
    }

    @Test
    void flawStateSurvivesASaveRoundTrip() {
        FlawStateData state = FlawStateData.empty().withWaterTouch(4242L);
        var encoded = FlawStateData.CODEC.codec().encodeStart(JsonOps.INSTANCE, state).getOrThrow();
        assertEquals(state, FlawStateData.CODEC.codec().parse(JsonOps.INSTANCE, encoded).getOrThrow());
        assertEquals(FlawStateData.NEVER, FlawStateData.empty().lastWaterTouch());
    }
}
