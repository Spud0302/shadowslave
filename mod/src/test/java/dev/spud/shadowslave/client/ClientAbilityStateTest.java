package dev.spud.shadowslave.client;

import dev.spud.shadowslave.network.payload.AbilityStatusPayload;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientAbilityStateTest {
    @AfterEach
    void reset() {
        ClientAbilityState.clear();
    }

    @Test
    void cooldownRunsOnTheClientClock() {
        ClientAbilityState.update(new AbilityStatusPayload("shadowslave:a", "A", 100, 200, false), 1000);
        assertEquals(100, ClientAbilityState.remainingTicks(1000));
        assertEquals(0.5F, ClientAbilityState.cooldownFraction(1000, 0.0F), 1.0e-6);
        assertEquals(40, ClientAbilityState.remainingTicks(1060));
        assertEquals(-1, ClientAbilityState.ticksSinceReady(1099));
        assertEquals(0, ClientAbilityState.ticksSinceReady(1100));
        assertEquals(0.0F, ClientAbilityState.cooldownFraction(1200, 0.0F));
    }

    @Test
    void readyCueSoundsOncePerCooldown() {
        ClientAbilityState.update(new AbilityStatusPayload("shadowslave:a", "A", 20, 200, false), 1000);
        assertFalse(ClientAbilityState.consumeReadyCue(1019));
        assertTrue(ClientAbilityState.consumeReadyCue(1020));
        assertFalse(ClientAbilityState.consumeReadyCue(1021));
    }

    @Test
    void readyWithoutACooldownNeverFlashes() {
        ClientAbilityState.update(new AbilityStatusPayload("shadowslave:a", "A", 0, 200, false), 1000);
        assertEquals(-1, ClientAbilityState.ticksSinceReady(1005));
        assertFalse(ClientAbilityState.consumeReadyCue(1005));
    }

    @Test
    void statusRoundTripsThroughItsCodec() {
        AbilityStatusPayload payload = new AbilityStatusPayload("shadowslave:a", "A", 12, 200, true);
        ByteBuf buffer = Unpooled.buffer();
        AbilityStatusPayload.STREAM_CODEC.encode(buffer, payload);
        assertEquals(payload, AbilityStatusPayload.STREAM_CODEC.decode(buffer));
    }
}
