package dev.spud.shadowslave.dreamrealm.spi;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DreamRealmRegionProvidersTest {
    @Test
    void baseDiscoveryHasNoRequiredOptionalRegionProvider() {
        assertTrue(DreamRealmRegionProviders.discover().isEmpty(),
                "base test runtime must remain valid with zero optional region providers");
    }

    @Test
    void emptyProviderSetIsValidAndCannotClaimRegionAvailability() {
        assertTrue(DreamRealmRegionProviders.validate(List.of()).isEmpty());
        assertFalse(DreamRealmRegionProviders.hasProviderForRegion(List.of(), "storm_lantern_coast"));
    }
}
