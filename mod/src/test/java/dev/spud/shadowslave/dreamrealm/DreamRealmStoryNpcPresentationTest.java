package dev.spud.shadowslave.dreamrealm;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DreamRealmStoryNpcPresentationTest {
    private static final Path RUNTIME = Path.of(
            "src/main/java/dev/spud/shadowslave/dreamrealm/DreamRealmStoryNpcRuntime.java");

    @Test
    void watchCaptainUsesDistinctBoundedVanillaPresentation() throws IOException {
        String runtime = Files.readString(RUNTIME);

        assertTrue(runtime.contains("EntityType.PILLAGER.create(level)"));
        assertFalse(runtime.contains("new Villager"));
        assertFalse(runtime.contains("EntityType.VILLAGER"));
        assertTrue(runtime.contains("Items.IRON_HELMET"));
        assertTrue(runtime.contains("Items.SPYGLASS"));
        assertTrue(runtime.contains("Items.SOUL_LANTERN"));
        assertTrue(runtime.contains("captain.setNoAi(true)"));
        assertTrue(runtime.contains("captain.setInvulnerable(true)"));
    }

    @Test
    void interactionsConsumeJavaOwnedStandingRuleWithoutInventingState() throws IOException {
        String runtime = Files.readString(RUNTIME);

        assertTrue(runtime.contains("DreamRealmStoryNpcExecutionBinding.ashenWatchCaptain()"));
        assertTrue(runtime.contains("player.isShiftKeyDown()"));
        assertTrue(runtime.contains("binding.standingRule()"));
        assertTrue(runtime.contains("binding.serviceLabels()"));
        assertFalse(runtime.contains("setRespawnPosition"));
        assertFalse(runtime.contains("giveExperiencePoints"));
        assertFalse(runtime.contains("award"));
    }
}
