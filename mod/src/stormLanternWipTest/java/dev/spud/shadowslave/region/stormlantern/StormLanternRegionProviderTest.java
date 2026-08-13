package dev.spud.shadowslave.region.stormlantern;

import dev.spud.shadowslave.dreamrealm.spi.DreamRealmRegionProvider;
import dev.spud.shadowslave.dreamrealm.spi.DreamRealmRegionProviders;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StormLanternRegionProviderTest {
    @Test
    void serviceLoaderDiscoversOnlyStormLanternProviderWhenWipIsPresent() {
        List<DreamRealmRegionProvider> providers = DreamRealmRegionProviders.discover();

        assertEquals(1, providers.size());
        assertEquals("ss_region_storm_lantern", providers.getFirst().providerId());
        assertTrue(DreamRealmRegionProviders.hasProviderForRegion(providers, StormLanternRegionProvider.REGION_ID));
        assertTrue(providers.getFirst().supportedHistoricalSiteIds()
                .contains(StormLanternRegionProvider.HISTORICAL_SITE_ID));
    }

    @Test
    void encounterPlanningIsDeterministicAndWithinBoundedBudget() {
        long worldSeed = 0x5A17C0A57L;

        RegionEncounterPlan.Plan first = RegionEncounterPlan.create(worldSeed);
        RegionEncounterPlan.Plan second = RegionEncounterPlan.create(worldSeed);

        assertEquals(first, second);
        assertTrue(first.encounters().size() >= 2);
        assertTrue(first.encounters().size() <= 4);
    }
}
