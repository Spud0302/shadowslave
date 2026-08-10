package dev.spud.shadowslave.dreamrealm;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DreamRealmVerticalSliceDefinitionTest {
    @Test
    void ashenExpanseConsumesEveryAuthoredLandmarkAndResourceHook() {
        var slice = DreamRealmVerticalSliceDefinition.ashenExpanse();

        assertEquals("ashen_expanse", slice.region().id());
        assertEquals(3, slice.landmarks().size());
        assertEquals(3, slice.resources().size());

        Set<String> landmarkIds = slice.landmarks().stream()
                .map(DreamRealmVerticalSliceDefinition.Placement::hookId)
                .collect(Collectors.toSet());
        Set<String> resourceIds = slice.resources().stream()
                .map(DreamRealmVerticalSliceDefinition.Placement::hookId)
                .collect(Collectors.toSet());

        assertEquals(slice.region().landmarkHooks(), landmarkIds);
        assertEquals(slice.region().resourceHooks(), resourceIds);
        assertTrue(slice.landmarks().stream().allMatch(placement -> Math.abs(placement.x()) <= 20 && Math.abs(placement.z()) <= 20));
        assertTrue(slice.resources().stream().allMatch(placement -> Math.abs(placement.x()) <= 20 && Math.abs(placement.z()) <= 20));
    }
}
