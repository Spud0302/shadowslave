package dev.spud.shadowslave.client;

import dev.spud.shadowslave.network.payload.AbilityStatusPayload;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientAbilityStateTest {
    @AfterEach
    void reset() {
        ClientAbilityState.clear();
    }

    private static AbilityStatusPayload pool(float essence, int... costs) {
        List<AbilityStatusPayload.Slot> slots = java.util.Arrays.stream(costs)
                .mapToObj(cost -> new AbilityStatusPayload.Slot("shadowslave:a" + cost, "A", cost, false))
                .toList();
        return new AbilityStatusPayload(slots, essence, 100, 0.5F);
    }

    @Test
    void essenceRefillsOnTheClientClockUpToTheCap() {
        ClientAbilityState.update(pool(20.0F, 30), 1000);
        assertEquals(20.0F, ClientAbilityState.essence(1000, 0.0F), 1.0e-4);
        assertEquals(30.0F, ClientAbilityState.essence(1020, 0.0F), 1.0e-4);
        assertEquals(0.3F, ClientAbilityState.essenceFraction(1020, 0.0F), 1.0e-4);
        assertEquals(100.0F, ClientAbilityState.essence(5000, 0.0F));
    }

    @Test
    void slotsShadeUntilThePoolCoversTheirCost() {
        ClientAbilityState.update(pool(20.0F, 40, 10), 1000);
        assertEquals(0.5F, ClientAbilityState.shortfall(0, 1000, 0.0F), 1.0e-4);
        assertEquals(0.0F, ClientAbilityState.shortfall(1, 1000, 0.0F));
        assertEquals(-1, ClientAbilityState.ticksSinceAffordable(0, 1039));
        assertEquals(0, ClientAbilityState.ticksSinceAffordable(0, 1040));
        assertEquals(0.0F, ClientAbilityState.shortfall(0, 1040, 0.0F));
        assertEquals(-1, ClientAbilityState.ticksSinceAffordable(1, 1040), "never short, never flashes");
    }

    @Test
    void readyCueSoundsOnceWhenASlotCanBeUsedAgain() {
        ClientAbilityState.update(pool(20.0F, 30), 1000);
        assertFalse(ClientAbilityState.consumeReadyCue(1019));
        assertTrue(ClientAbilityState.consumeReadyCue(1020));
        assertFalse(ClientAbilityState.consumeReadyCue(1021));
    }

    @Test
    void aFullPoolNeverChimes() {
        ClientAbilityState.update(pool(100.0F, 30), 1000);
        assertFalse(ClientAbilityState.consumeReadyCue(1005));
    }

    @Test
    void statusRoundTripsThroughItsCodec() {
        AbilityStatusPayload payload = new AbilityStatusPayload(
                List.of(new AbilityStatusPayload.Slot("shadowslave:a", "A", 35, true),
                        new AbilityStatusPayload.Slot("shadowslave:b", "B", 50, false)),
                12.5F, 200, 0.25F);
        ByteBuf buffer = Unpooled.buffer();
        AbilityStatusPayload.STREAM_CODEC.encode(buffer, payload);
        assertEquals(payload, AbilityStatusPayload.STREAM_CODEC.decode(buffer));
    }
}
