package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.awakening.KitLadder;
import dev.spud.shadowslave.awakening.Longing;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DreamKitRulesTest {
    @Test
    void everyDreamAbilityIsRegistered() {
        for (var id : List.of(AspectKitCatalog.WAYFARER.abilityId(), AspectKitCatalog.LULL.abilityId(),
                KitLadder.ANCHOR_RECALL.abilityId(), KitLadder.TRAILBLAZE.abilityId(),
                KitLadder.DREAMFALL.abilityId(), KitLadder.SLEEPWALKER.abilityId())) {
            assertTrue(AbilityRegistry.isActivatable(id), id.toString());
        }
    }

    @Test
    void wayfarerAndLullHaveLongings() {
        assertEquals("Wayfarer", Longing.WANDER.natureName());
        assertEquals("Lull", Longing.REST.natureName());
    }

    @Test
    void wanderlustFadesAfterTheGraceAndBottomsOut() {
        assertEquals(1.0F, DreamKitPassives.wanderlustFactor(0));
        assertEquals(1.0F, DreamKitPassives.wanderlustFactor(DreamKitPassives.WANDER_GRACE_TICKS));
        float halfway = DreamKitPassives.wanderlustFactor(DreamKitPassives.WANDER_GRACE_TICKS + DreamKitPassives.WANDER_FADE_TICKS / 2);
        assertEquals(0.625F, halfway, 0.001F);
        assertEquals(DreamKitPassives.WANDER_FLOOR,
                DreamKitPassives.wanderlustFactor(DreamKitPassives.WANDER_GRACE_TICKS + DreamKitPassives.WANDER_FADE_TICKS * 3));
    }
}
