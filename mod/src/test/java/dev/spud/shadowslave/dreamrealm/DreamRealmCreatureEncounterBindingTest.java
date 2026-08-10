package dev.spud.shadowslave.dreamrealm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DreamRealmCreatureEncounterBindingTest {
    @Test
    void bindsAuthoredAshBurrowerAffinityInsidePhysicalSlice() {
        var encounter = DreamRealmCreatureEncounterBinding.ashenExpanseAshBurrower();

        assertEquals(DreamRealmVerticalSliceDefinition.REGION_ID, encounter.region().id());
        assertEquals("ash_burrower", encounter.creature().id());
        assertTrue(encounter.region().creatureAffinityIds().contains(encounter.creature().id()));
        assertTrue(Math.abs(encounter.x()) <= 24);
        assertTrue(Math.abs(encounter.z()) <= 24);
        assertTrue(encounter.y() >= 1 && encounter.y() <= 18);

        var ruinMetal = DreamRealmVerticalSliceDefinition.ashenExpanse().resources().stream()
                .filter(placement -> placement.hookId().equals("ruin_metal"))
                .findFirst()
                .orElseThrow();
        int planarDistance = Math.abs(encounter.x() - ruinMetal.x()) + Math.abs(encounter.z() - ruinMetal.z());
        assertTrue(planarDistance <= 4, "encounter should remain near the authored ruin-metal hook");
    }
}
