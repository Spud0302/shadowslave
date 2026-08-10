package dev.spud.shadowslave.world.entity;

import dev.spud.shadowslave.nightmare.content.NightmareCreatureContentCatalog;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DrownedListenerEntityBindingTest {
    @Test
    void executionAdapterConsumesExistingJavaOwnedProfile() {
        NightmareCreatureContentCatalog.CreatureProfile profile = DrownedListenerExecutionBinding.contentProfile();

        assertEquals("drowned_listener", DrownedListenerExecutionBinding.CONTENT_ID);
        assertEquals(DrownedListenerExecutionBinding.CONTENT_ID, profile.id());
        assertEquals("Drowned Listener", profile.displayName());
        assertEquals(NightmareCreatureContentCatalog.Rank.DORMANT, profile.rank());
        assertEquals(NightmareCreatureContentCatalog.CreatureClass.MONSTER, profile.creatureClass());
        assertTrue(profile.senses().contains(NightmareCreatureContentCatalog.Sense.SOUND));
        assertTrue(profile.senses().contains(NightmareCreatureContentCatalog.Sense.VIBRATION));
        assertTrue(profile.locomotion().contains(NightmareCreatureContentCatalog.Locomotion.SWIM));
        assertTrue(profile.locomotion().contains(NightmareCreatureContentCatalog.Locomotion.GROUND));
        assertTrue(profile.pressures().contains(NightmareCreatureContentCatalog.Pressure.AMBUSH));
        assertTrue(profile.pressures().contains(NightmareCreatureContentCatalog.Pressure.PURSUIT));
        assertTrue(profile.counterplayTags().contains("dry_ground"));
    }
}
