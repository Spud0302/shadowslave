package dev.spud.shadowslave.nightmare;

import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightmareReturnOriginGuardTest {
    @Test
    void NightmareDimensionCannotBecomeFreshReturnOrigin() {
        assertFalse(NightmareService.entryOriginAllowed(NightmareService.NIGHTMARE_LEVEL));
    }

    @Test
    void ordinaryDimensionCanBecomeFreshReturnOrigin() {
        assertTrue(NightmareService.entryOriginAllowed(Level.OVERWORLD));
    }
}
