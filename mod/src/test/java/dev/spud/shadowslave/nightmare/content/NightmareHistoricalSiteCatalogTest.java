package dev.spud.shadowslave.nightmare.content;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightmareHistoricalSiteCatalogTest {
    @Test
    void drownedBellMapsHistoricalSettlementToStormLanternCoast() {
        NightmareHistoricalSiteCatalog.Site site = NightmareHistoricalSiteCatalog.drownedBell();

        assertEquals(DrownedBellScenarioDefinition.SCENARIO_ID, site.scenarioId());
        assertEquals("storm_lantern_coast", site.dreamRealmRegionId());
        assertEquals("storm_belfry", site.historicalToFutureLandmarks().get("bell_tower"));
        assertEquals("sea_gate", site.historicalToFutureLandmarks().get("sea_gate"));
        assertEquals("collapsed_quarry_cut", site.historicalToFutureLandmarks().get("quarry_tunnels"));
        assertEquals("drowned_harbour_terraces", site.historicalToFutureLandmarks().get("lower_village"));
    }

    @Test
    void drownedBellOriginalHistoryDefinesWeightedFateAxes() {
        NightmareHistoricalSiteCatalog.Site site = NightmareHistoricalSiteCatalog.drownedBell();

        assertEquals("silent", site.originalHistory().get("warning_bell").originalValue());
        assertEquals("sealed", site.originalHistory().get("quarry_route").originalValue());
        assertEquals("failed", site.originalHistory().get("sea_gate").originalValue());
        assertEquals("inundated", site.originalHistory().get("lower_village").originalValue());
        assertEquals("survived", site.originalHistory().get("drowned_listener").originalValue());
        assertTrue(site.originalHistory().values().stream().allMatch(axis -> axis.weight() > 0));
    }

    @Test
    void everyWaveOneSiteLinksToAnAuthoredDreamRealmRegion() {
        assertEquals(1, NightmareHistoricalSiteCatalog.waveOne().size());
        assertEquals(
                NightmareHistoricalSiteCatalog.drownedBell(),
                NightmareHistoricalSiteCatalog.requireScenario(DrownedBellScenarioDefinition.SCENARIO_ID)
        );
    }
}
