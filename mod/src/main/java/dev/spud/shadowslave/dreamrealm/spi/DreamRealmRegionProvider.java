package dev.spud.shadowslave.dreamrealm.spi;

import java.util.Set;

/** Contract implemented by optional Dream Realm region content modules. */
public interface DreamRealmRegionProvider {
    String providerId();

    Set<String> supportedRegionIds();

    Set<String> supportedHistoricalSiteIds();
}
