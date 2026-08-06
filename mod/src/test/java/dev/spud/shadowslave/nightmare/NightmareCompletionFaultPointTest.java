package dev.spud.shadowslave.nightmare;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightmareCompletionFaultPointTest {
    @Test
    void everyFaultPointRoundTripsThroughItsDocumentedPropertyValue() {
        for (NightmareCompletionFaultPoint point : NightmareCompletionFaultPoint.values()) {
            assertEquals(point, NightmareCompletionFaultPoint.parse(point.serializedName()).orElseThrow());
            assertEquals(point, NightmareCompletionFaultPoint.parse(point.name()).orElseThrow());
        }
    }

    @Test
    void absentBlankAndUnknownValuesLeaveFaultInjectionDisabled() {
        assertTrue(NightmareCompletionFaultPoint.parse(null).isEmpty());
        assertTrue(NightmareCompletionFaultPoint.parse("  ").isEmpty());
        assertTrue(NightmareCompletionFaultPoint.parse("after_some_other_save").isEmpty());
    }
}
