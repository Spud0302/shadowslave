package dev.spud.shadowslave.world.entity;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DrownedBellListenerExecutionBindingTest {
    @Test
    void replacesOnlyTaggedVanillaDrownedPlaceholder() {
        assertTrue(DrownedBellListenerExecutionBinding.shouldReplace(
                "minecraft:drowned",
                Set.of("other", DrownedBellListenerExecutionBinding.PLACEHOLDER_TAG)
        ));
        assertFalse(DrownedBellListenerExecutionBinding.shouldReplace("minecraft:drowned", Set.of()));
        assertFalse(DrownedBellListenerExecutionBinding.shouldReplace(
                "shadowslave:drowned_listener",
                Set.of(DrownedBellListenerExecutionBinding.PLACEHOLDER_TAG)
        ));
        assertFalse(DrownedBellListenerExecutionBinding.shouldReplace(
                "minecraft:zombie",
                Set.of(DrownedBellListenerExecutionBinding.PLACEHOLDER_TAG)
        ));
    }
}
