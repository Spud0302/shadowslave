package dev.spud.shadowslave.dreamrealm;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CinderRestLanternRingBindingTest {
    private static final Path PREVIEW = Path.of(
            "src/main/java/dev/spud/shadowslave/dreamrealm/DreamRealmPreviewService.java");
    private static final Path RUNTIME = Path.of(
            "src/main/java/dev/spud/shadowslave/dreamrealm/CinderRestLanternRingRuntime.java");
    private static final Path MOD = Path.of(
            "src/main/java/dev/spud/shadowslave/ShadowSlaveMod.java");

    @Test
    void derivesSettlementPresentationFromExistingJavaStoryBinding() {
        var lanterns = CinderRestLanternRingBinding.cinderRest();
        var story = DreamRealmStoryNpcExecutionBinding.ashenWatchCaptain();

        assertEquals(story.moduleId(), lanterns.moduleId());
        assertEquals(story.settlementName(), lanterns.settlementName());
        assertEquals(story.factionName(), lanterns.factionName());
        assertEquals(story.serviceLabels(), lanterns.serviceLabels());
        assertEquals(story.standingRule(), lanterns.standingRule());
        assertEquals("ashen_watch", lanterns.moduleId());
        assertEquals("Cinder Rest", lanterns.settlementName());
        assertEquals("Grey Lanterns", lanterns.factionName());
    }

    @Test
    void definesFiveDistinctInteractiveLampsWithoutHijackingInteriorLantern() {
        var binding = CinderRestLanternRingBinding.cinderRest();
        var distinct = new HashSet<String>();

        assertEquals(5, binding.lamps().size());
        for (var lamp : binding.lamps()) {
            assertTrue(CinderRestLanternRingBinding.isLamp(binding, lamp.x(), lamp.y(), lamp.z()));
            assertTrue(distinct.add(lamp.x() + ":" + lamp.y() + ":" + lamp.z()));
        }
        assertEquals(5, distinct.size());
        assertFalse(CinderRestLanternRingBinding.isLamp(binding, 0, 3, -2));
    }

    @Test
    void physicalBuildAndRuntimeConsumeOnlyTheBoundLanternRing() throws IOException {
        String preview = Files.readString(PREVIEW);
        String runtime = Files.readString(RUNTIME);
        String mod = Files.readString(MOD);

        assertTrue(preview.contains("buildCinderRestLanternRing(level);"));
        assertTrue(preview.contains("CinderRestLanternRingBinding.cinderRest().lamps()"));
        assertTrue(preview.contains("Blocks.POLISHED_BLACKSTONE_BRICK_WALL"));
        assertTrue(preview.contains("Blocks.SOUL_LANTERN"));

        assertTrue(runtime.contains("CinderRestLanternRingBinding.cinderRest()"));
        assertTrue(runtime.contains("CinderRestLanternRingBinding.isLamp"));
        assertTrue(runtime.contains("event.setCanceled(true);"));
        assertTrue(runtime.contains("event.getHand() != InteractionHand.MAIN_HAND"));
        assertTrue(runtime.indexOf("event.setCanceled(true);") < runtime.indexOf("instanceof ServerPlayer player"));
        assertFalse(runtime.contains("MemoryOwnershipData"));
        assertFalse(runtime.contains("SoulData"));
        assertFalse(runtime.contains("setData("));

        assertTrue(mod.contains("CinderRestLanternRingRuntime::onRightClickBlock"));
    }
}
