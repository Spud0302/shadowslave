package dev.spud.shadowslave.dreamrealm;

import dev.spud.shadowslave.dreamrealm.content.DreamRealmStoryContentCatalog;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DreamRealmStoryNpcExecutionBindingTest {
    @Test
    void ashenWatchCaptainConsumesExistingStoryModuleIdentity() {
        var binding = DreamRealmStoryNpcExecutionBinding.ashenWatchCaptain();
        var module = DreamRealmStoryContentCatalog.waveOne().stream()
                .filter(candidate -> candidate.id().equals("ashen_watch"))
                .findFirst()
                .orElseThrow();

        assertEquals(module.id(), binding.moduleId());
        assertEquals(module.displayName(), binding.moduleDisplayName());
        assertEquals(module.regionId(), binding.regionId());
        assertEquals(module.settlementName(), binding.settlementName());
        assertEquals(module.factionName(), binding.factionName());
        assertEquals("watch_captain", binding.archetypeId());
        assertEquals("Watch Captain", binding.archetypeDisplayName());
        assertEquals(module.arrivalCue(), binding.arrivalCue());
        assertEquals(module.standingRule(), binding.standingRule());
        assertEquals(
                module.services().stream().map(service -> service.name().toLowerCase()).collect(java.util.stream.Collectors.toSet()),
                Set.copyOf(binding.serviceLabels())
        );
    }

    @Test
    void bindingDoesNotInventServicesOutsideJavaOwnedModule() {
        var binding = DreamRealmStoryNpcExecutionBinding.ashenWatchCaptain();
        assertEquals(Set.of("rumors", "scouting", "shelter"), Set.copyOf(binding.serviceLabels()));
        assertFalse(binding.serviceLabels().contains("trade"));
        assertFalse(binding.serviceLabels().contains("repair"));
    }

    @Test
    void unknownModuleAndUnauthoredArchetypeFailClosed() {
        assertThrows(IllegalArgumentException.class,
                () -> DreamRealmStoryNpcExecutionBinding.resolve("unknown_module", "watch_captain"));
        assertThrows(IllegalArgumentException.class,
                () -> DreamRealmStoryNpcExecutionBinding.resolve("ashen_watch", "ferry_master"));
        assertThrows(IllegalArgumentException.class,
                () -> DreamRealmStoryNpcExecutionBinding.resolve(" ", "watch_captain"));
    }

    @Test
    void authoredArchetypeRemainsPartOfSourceModule() {
        var module = DreamRealmStoryContentCatalog.waveOne().stream()
                .filter(candidate -> candidate.id().equals("ashen_watch"))
                .findFirst()
                .orElseThrow();
        assertTrue(module.npcArchetypes().contains(DreamRealmStoryNpcExecutionBinding.WATCH_CAPTAIN_ARCHETYPE_ID));
    }
}
