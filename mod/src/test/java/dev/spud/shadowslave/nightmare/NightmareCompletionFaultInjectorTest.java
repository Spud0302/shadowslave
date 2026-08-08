package dev.spud.shadowslave.nightmare;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightmareCompletionFaultInjectorTest {
    @AfterEach
    void clearFaultProperty() {
        System.clearProperty(NightmareCompletionFaultInjector.PROPERTY);
    }

    @Test
    void noPropertyLeavesPhysicalFaultInjectionDisabled() {
        assertTrue(NightmareCompletionFaultInjector.configuredPoint().isEmpty());
    }

    @Test
    void serializedBoundarySelectsExactlyThatFaultPoint() {
        System.setProperty(
                NightmareCompletionFaultInjector.PROPERTY,
                NightmareCompletionFaultPoint.AFTER_RETURN_REGISTRY_SAVE.serializedName()
        );

        Optional<NightmareCompletionFaultPoint> configured = NightmareCompletionFaultInjector.configuredPoint();

        assertEquals(Optional.of(NightmareCompletionFaultPoint.AFTER_RETURN_REGISTRY_SAVE), configured);
    }

    @Test
    void invalidBoundaryFailsClosedInsteadOfSilentlyDisablingHarness() {
        System.setProperty(NightmareCompletionFaultInjector.PROPERTY, "after_return_regsitry_save");

        IllegalStateException failure = assertThrows(
                IllegalStateException.class,
                NightmareCompletionFaultInjector::configuredPoint
        );

        assertTrue(failure.getMessage().contains("after_return_regsitry_save"));
        assertTrue(failure.getMessage().contains("after_return_registry_save"));
    }
}
