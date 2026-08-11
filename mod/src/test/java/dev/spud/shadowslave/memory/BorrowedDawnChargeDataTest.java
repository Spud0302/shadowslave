package dev.spud.shadowslave.memory;

import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BorrowedDawnChargeDataTest {
    @Test
    void codecRoundTripRetainsStoredLight() {
        var encoded = BorrowedDawnChargeData.CODEC.encodeStart(JsonOps.INSTANCE, BorrowedDawnChargeData.storedLight()).getOrThrow();
        var decoded = BorrowedDawnChargeData.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertTrue(decoded.charged());
    }

    @Test
    void explicitStatesRemainBinary() {
        assertFalse(BorrowedDawnChargeData.empty().charged());
        assertTrue(BorrowedDawnChargeData.storedLight().charged());
    }
}
