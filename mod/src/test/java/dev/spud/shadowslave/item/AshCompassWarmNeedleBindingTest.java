package dev.spud.shadowslave.item;

import dev.spud.shadowslave.world.entity.AshBurrowerExecutionBinding;
import dev.spud.shadowslave.world.entity.ChainbackExecutionBinding;
import dev.spud.shadowslave.world.entity.DrownedListenerExecutionBinding;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AshCompassWarmNeedleBindingTest {
    @Test
    void bindingConsumesTheExistingAuthoredMemoryEnchantment() {
        var profile = AshCompassWarmNeedleBinding.memoryProfile();

        assertEquals(AshCompassWarmNeedleBinding.MEMORY_ID, profile.id());
        assertTrue(profile.enchantments().stream()
                .anyMatch(enchantment -> enchantment.id().equals(AshCompassWarmNeedleBinding.ENCHANTMENT_ID)));
    }

    @Test
    void warningTargetsExactlyTheExistingHostilePhysicalExecutors() {
        assertEquals(Set.of(
                        AshBurrowerExecutionBinding.CONTENT_ID,
                        ChainbackExecutionBinding.CONTENT_ID,
                        DrownedListenerExecutionBinding.CONTENT_ID),
                AshCompassWarmNeedleBinding.threatContentIds());
        assertFalse(AshCompassWarmNeedleBinding.isThreatContentId("ash_burrower_echo"));
        assertFalse(AshCompassWarmNeedleBinding.isThreatContentId("minecraft:zombie"));
    }

    @Test
    void detectionRangeIsBoundedAndFailClosed() {
        double rangeSquared = AshCompassWarmNeedleBinding.DETECTION_RANGE * AshCompassWarmNeedleBinding.DETECTION_RANGE;

        assertTrue(AshCompassWarmNeedleBinding.detects(0.0D));
        assertTrue(AshCompassWarmNeedleBinding.detects(rangeSquared));
        assertFalse(AshCompassWarmNeedleBinding.detects(rangeSquared + 0.001D));
        assertThrows(IllegalArgumentException.class, () -> AshCompassWarmNeedleBinding.detects(-0.001D));
    }
}
