package dev.spud.shadowslave.dreamrealm.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DreamRealmStoryContentCatalogTest {
    @Test
    void waveOneCoversEveryAuthoredFrontierRegionExactlyOnce() {
        var modules = DreamRealmStoryContentCatalog.waveOne();
        var regions = DreamRealmRegionContentCatalog.waveOne();

        assertEquals(10, modules.size());
        assertEquals(regions.stream().map(DreamRealmRegionContentCatalog.RegionProfile::id).collect(java.util.stream.Collectors.toSet()),
                modules.stream().map(DreamRealmStoryContentCatalog.StoryModule::regionId).collect(java.util.stream.Collectors.toSet()));
        assertEquals(modules.size(), modules.stream().map(DreamRealmStoryContentCatalog.StoryModule::id).distinct().count());
        assertEquals(modules.size(), modules.stream().map(DreamRealmStoryContentCatalog.StoryModule::settlementName).distinct().count());
        assertEquals(modules.size(), modules.stream().map(DreamRealmStoryContentCatalog.StoryModule::factionName).distinct().count());
    }

    @Test
    void modulesProvideBroadServicesRolesNpcsAndIndependentHooks() {
        var modules = DreamRealmStoryContentCatalog.waveOne();
        Set<DreamRealmStoryContentCatalog.Service> services = new HashSet<>();
        Set<DreamRealmStoryContentCatalog.FactionRole> roles = new HashSet<>();
        Set<String> npcs = new HashSet<>();
        Set<String> hooks = new HashSet<>();

        modules.forEach(module -> {
            services.addAll(module.services());
            roles.add(module.factionRole());
            npcs.addAll(module.npcArchetypes());
            hooks.addAll(module.storyHooks());
            assertTrue(module.services().size() >= 3);
            assertTrue(module.npcArchetypes().size() >= 3);
            assertTrue(module.tensions().size() >= 3);
            assertTrue(module.storyHooks().size() >= 3);
            assertTrue(module.arrivalCue().length() >= 40);
            assertTrue(module.standingRule().length() >= 50);
        });

        assertEquals(Set.of(DreamRealmStoryContentCatalog.FactionRole.WARDENS,
                DreamRealmStoryContentCatalog.FactionRole.GUIDES,
                DreamRealmStoryContentCatalog.FactionRole.SALVAGERS,
                DreamRealmStoryContentCatalog.FactionRole.TRADERS,
                DreamRealmStoryContentCatalog.FactionRole.HUNTERS,
                DreamRealmStoryContentCatalog.FactionRole.KEEPERS,
                DreamRealmStoryContentCatalog.FactionRole.FERRYMEN,
                DreamRealmStoryContentCatalog.FactionRole.SCHOLARS), roles);
        assertEquals(Set.of(DreamRealmStoryContentCatalog.Service.SHELTER,
                DreamRealmStoryContentCatalog.Service.TRADE,
                DreamRealmStoryContentCatalog.Service.RUMORS,
                DreamRealmStoryContentCatalog.Service.REPAIR,
                DreamRealmStoryContentCatalog.Service.GUIDANCE,
                DreamRealmStoryContentCatalog.Service.PROVISIONS,
                DreamRealmStoryContentCatalog.Service.ESCORT,
                DreamRealmStoryContentCatalog.Service.MEDIATION,
                DreamRealmStoryContentCatalog.Service.SALVAGE,
                DreamRealmStoryContentCatalog.Service.SCOUTING), services);
        assertTrue(npcs.size() >= 28);
        assertTrue(hooks.size() >= 30);
    }

    @Test
    void storyModulesRemainAuthoredContentRatherThanProgressionAuthority() {
        for (var module : DreamRealmStoryContentCatalog.waveOne()) {
            assertTrue(module.standingRule().toLowerCase().contains("standing"));
            assertTrue(module.storyHooks().stream().noneMatch(hook -> hook.contains("rank_up") || hook.contains("award_aspect")));
        }
    }
}
