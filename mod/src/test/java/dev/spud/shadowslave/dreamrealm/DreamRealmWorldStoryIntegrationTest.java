package dev.spud.shadowslave.dreamrealm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DreamRealmWorldStoryIntegrationTest {
    @Test
    void cinderRestPlacesAuthoredWatchCaptainInsideAshenExpanseRefuge() {
        var integration = DreamRealmWorldStoryIntegration.cinderRest();

        assertEquals("ashen_expanse", integration.slice().region().id());
        assertEquals("ashen_expanse", integration.watchCaptain().regionId());
        assertEquals("Cinder Rest", integration.watchCaptain().settlementName());
        assertEquals("Grey Lanterns", integration.watchCaptain().factionName());
        assertEquals("watch_captain", integration.watchCaptain().archetypeId());
        assertTrue(Math.abs(integration.x()) <= 4);
        assertTrue(Math.abs(integration.z()) <= 3);
        assertEquals(1, integration.y());
    }
}
