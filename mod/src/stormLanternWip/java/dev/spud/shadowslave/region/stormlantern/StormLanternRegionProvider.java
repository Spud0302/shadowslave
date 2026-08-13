package dev.spud.shadowslave.region.stormlantern;

import dev.spud.shadowslave.dreamrealm.spi.DreamRealmRegionProvider;

import java.util.Set;

public final class StormLanternRegionProvider implements DreamRealmRegionProvider {
    public static final String REGION_ID = "storm_lantern_coast";

    @Override
    public String providerId() {
        return "ss_region_storm_lantern";
    }

    @Override
    public Set<String> supportedRegionIds() {
        return Set.of(REGION_ID);
    }

    @Override
    public Set<String> supportedHistoricalSiteIds() {
        return Set.of();
    }
}
