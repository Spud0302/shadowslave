package dev.spud.shadowslave.world.entity;

import dev.spud.shadowslave.nightmare.content.NightmareCreatureContentCatalog;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChainbackEntityBindingTest {
    @Test
    void executionAdapterConsumesExistingJavaOwnedProfile() {
        NightmareCreatureContentCatalog.CreatureProfile profile = ChainbackExecutionBinding.contentProfile();

        assertEquals("chainback", ChainbackExecutionBinding.CONTENT_ID);
        assertEquals(ChainbackExecutionBinding.CONTENT_ID, profile.id());
        assertEquals("Chainback", profile.displayName());
        assertEquals(NightmareCreatureContentCatalog.Rank.AWAKENED, profile.rank());
        assertEquals(NightmareCreatureContentCatalog.CreatureClass.MONSTER, profile.creatureClass());
        assertTrue(profile.locomotion().contains(NightmareCreatureContentCatalog.Locomotion.GROUND));
        assertTrue(profile.locomotion().contains(NightmareCreatureContentCatalog.Locomotion.CLIMB));
        assertTrue(profile.pressures().contains(NightmareCreatureContentCatalog.Pressure.PURSUIT));
        assertTrue(profile.pressures().contains(NightmareCreatureContentCatalog.Pressure.DISPLACEMENT));
    }
}
