package dev.spud.shadowslave.client;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AbilityCastEffectsTest {
    @Test
    void dormantAbilitiesGlowInTheirNaturesColours() {
        assertEquals(Optional.of(AbilityCastEffects.NatureGlow.EMBER), AbilityCastEffects.glowFor(AspectKitCatalog.EMBER.abilityId()));
        assertEquals(Optional.of(AbilityCastEffects.NatureGlow.MIST), AbilityCastEffects.glowFor(AspectKitCatalog.MIST.abilityId()));
        assertEquals(Optional.of(AbilityCastEffects.NatureGlow.STONE), AbilityCastEffects.glowFor(AspectKitCatalog.STONE.abilityId()));
        assertEquals(Optional.of(AbilityCastEffects.NatureGlow.TIDE), AbilityCastEffects.glowFor(AspectKitCatalog.TIDE.abilityId()));
    }

    @Test
    void awakenedAbilitiesKeepTheirNaturesLook() {
        assertEquals(Optional.of(AbilityCastEffects.NatureGlow.EMBER), AbilityCastEffects.glowFor(KitLadder.EMBER_WARD.abilityId()));
        assertEquals(Optional.of(AbilityCastEffects.NatureGlow.MIST), AbilityCastEffects.glowFor(KitLadder.VEIL.abilityId()));
        assertEquals(Optional.of(AbilityCastEffects.NatureGlow.STONE), AbilityCastEffects.glowFor(KitLadder.QUAKE.abilityId()));
        assertEquals(Optional.of(AbilityCastEffects.NatureGlow.TIDE), AbilityCastEffects.glowFor(KitLadder.UNDERTOW.abilityId()));
    }

    @Test
    void everyKitAbilityHasAGlow() {
        for (var variant : KitLadder.all()) {
            assertEquals(true, AbilityCastEffects.glowFor(variant.abilityId()).isPresent(), variant.name());
        }
    }

    @Test
    void unknownAbilitiesPlayNoGlow() {
        assertEquals(Optional.empty(), AbilityCastEffects.glowFor(ResourceLocation.fromNamespaceAndPath("shadowslave", "generation/ability/unknown")));
    }
}
