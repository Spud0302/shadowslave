package dev.spud.shadowslave.dreamrealm;

import dev.spud.shadowslave.dreamrealm.content.DreamRealmStoryContentCatalog;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DreamRealmStoryNpcExecutionBindingTest {
    @Test
    void ashenWatchCaptainConsumesExistingStoryModuleIdentity() {
        var binding = DreamRealmStoryNpcExecutionBinding.ashenWatchCaptain();
        var module = DreamRealmStoryContentCatalog.waveOne().stream().filter(candidate -> candidate.id().equals("ashen_watch")).findFirst().orElseThrow();
        assertEquals(module.id(), binding.moduleId());
        assertEquals(module.regionId(), binding.regionId());
        assertEquals(module.settlementName(), binding.settlementName());
        assertEquals(module.factionName(), binding.factionName());
        assertEquals(Set.of("rumors", "scouting", "shelter"), Set.copyOf(binding.serviceLabels()));
        assertFalse(binding.serviceLabels().contains("trade"));
    }

    @Test
    void unknownModuleAndUnauthoredArchetypeFailClosed() {
        assertThrows(IllegalArgumentException.class, () -> DreamRealmStoryNpcExecutionBinding.resolve("unknown_module", "watch_captain"));
        assertThrows(IllegalArgumentException.class, () -> DreamRealmStoryNpcExecutionBinding.resolve("ashen_watch", "ferry_master"));
    }
}
