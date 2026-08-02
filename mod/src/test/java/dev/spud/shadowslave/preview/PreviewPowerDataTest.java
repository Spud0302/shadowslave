package dev.spud.shadowslave.preview;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PreviewPowerDataTest {
    @Test
    void codecRoundTripsCooldown() {
        PreviewPowerData original = new PreviewPowerData(12345L);
        JsonElement encoded = PreviewPowerData.CODEC.codec()
                .encodeStart(JsonOps.INSTANCE, original)
                .getOrThrow();
        PreviewPowerData decoded = PreviewPowerData.CODEC.codec()
                .parse(JsonOps.INSTANCE, encoded)
                .getOrThrow();

        assertEquals(original, decoded);
    }

    @Test
    void malformedStoredCooldownReturnsDataErrorInsteadOfThrowing() {
        JsonElement malformed = JsonParser.parseString("""
                {
                  "kindle_cooldown_until": -1
                }
                """);

        DataResult<PreviewPowerData> result = assertDoesNotThrow(
                () -> PreviewPowerData.CODEC.codec().parse(JsonOps.INSTANCE, malformed)
        );

        assertTrue(result.error().isPresent());
    }

    @Test
    void negativeCooldownIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new PreviewPowerData(-1L));
    }
}
