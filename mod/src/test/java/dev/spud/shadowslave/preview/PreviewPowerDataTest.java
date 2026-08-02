package dev.spud.shadowslave.preview;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    void negativeCooldownIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new PreviewPowerData(-1L));
    }
}
