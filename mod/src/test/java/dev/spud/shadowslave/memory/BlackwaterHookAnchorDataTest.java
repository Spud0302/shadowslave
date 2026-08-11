package dev.spud.shadowslave.memory;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlackwaterHookAnchorDataTest {
    @Test
    void persistentRoundTripRetainsExactTerrainAnchor() {
        BlackwaterHookAnchorData original = BlackwaterHookAnchorData.empty()
                .anchored("minecraft:overworld", 123456789L);

        var encoded = BlackwaterHookAnchorData.CODEC.codec().encodeStart(JsonOps.INSTANCE, original).getOrThrow();
        BlackwaterHookAnchorData decoded = BlackwaterHookAnchorData.CODEC.codec()
                .parse(JsonOps.INSTANCE, encoded).getOrThrow();

        assertEquals(original, decoded);
        assertEquals("minecraft:overworld", decoded.anchor().orElseThrow().dimension());
        assertEquals(123456789L, decoded.anchor().orElseThrow().blockPos());
    }

    @Test
    void absentAnchorRoundTripsAsEmptyForOldSaves() {
        BlackwaterHookAnchorData decoded = BlackwaterHookAnchorData.CODEC.codec()
                .parse(JsonOps.INSTANCE, new JsonObject()).getOrThrow();
        assertTrue(decoded.anchor().isEmpty());
        assertFalse(decoded.clear().anchor().isPresent());
    }

    @Test
    void blankDimensionsFailClosed() {
        assertThrows(IllegalArgumentException.class,
                () -> new BlackwaterHookAnchorData.Anchor("  ", 0L));
    }
}
