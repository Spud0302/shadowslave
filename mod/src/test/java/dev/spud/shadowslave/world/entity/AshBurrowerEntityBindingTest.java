package dev.spud.shadowslave.world.entity;

import dev.spud.shadowslave.nightmare.content.NightmareCreatureContentCatalog;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AshBurrowerEntityBindingTest {
    @Test
    void resolvesExistingAuthoredCreatureProfile() {
        var profile = AshBurrowerExecutionBinding.profile();

        assertEquals("ash_burrower", profile.id());
        assertEquals("Ash Burrower", profile.displayName());
        assertEquals(NightmareCreatureContentCatalog.Rank.DORMANT, profile.rank());
        assertEquals(NightmareCreatureContentCatalog.CreatureClass.BEAST, profile.creatureClass());
        assertTrue(profile.senses().contains(NightmareCreatureContentCatalog.Sense.VIBRATION));
        assertTrue(profile.locomotion().contains(NightmareCreatureContentCatalog.Locomotion.BURROW));
        assertTrue(profile.locomotion().contains(NightmareCreatureContentCatalog.Locomotion.GROUND));
        assertTrue(profile.pressures().contains(NightmareCreatureContentCatalog.Pressure.AMBUSH));
        assertTrue(profile.pressures().contains(NightmareCreatureContentCatalog.Pressure.DISPLACEMENT));
        assertTrue(profile.counterplayTags().contains("bait_vibration"));
    }
}
