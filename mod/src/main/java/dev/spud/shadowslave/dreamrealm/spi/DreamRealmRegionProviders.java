package dev.spud.shadowslave.dreamrealm.spi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;

/** Base-owned discovery/validation for optional Dream Realm region providers. */
public final class DreamRealmRegionProviders {
    private DreamRealmRegionProviders() {
    }

    /** Discover providers visible to the current runtime class loader. */
    public static List<DreamRealmRegionProvider> discover() {
        ArrayList<DreamRealmRegionProvider> providers = new ArrayList<>();
        ServiceLoader.load(DreamRealmRegionProvider.class).forEach(providers::add);
        return validate(providers);
    }

    /** Validate a provider set without requiring optional providers to exist. */
    public static List<DreamRealmRegionProvider> validate(List<DreamRealmRegionProvider> providers) {
        ArrayList<DreamRealmRegionProvider> checked = new ArrayList<>();
        HashSet<String> providerIds = new HashSet<>();
        for (DreamRealmRegionProvider provider : Objects.requireNonNull(providers, "providers")) {
            DreamRealmRegionProvider value = Objects.requireNonNull(provider, "provider");
            String providerId = stableId(value.providerId(), "providerId");
            if (!providerIds.add(providerId)) {
                throw new IllegalArgumentException("Duplicate Dream Realm region provider id " + providerId);
            }
            validateIds(value.supportedRegionIds(), "supportedRegionIds");
            validateIds(value.supportedHistoricalSiteIds(), "supportedHistoricalSiteIds");
            checked.add(value);
        }
        return List.copyOf(checked);
    }

    public static boolean hasProviderForRegion(List<DreamRealmRegionProvider> providers, String regionId) {
        String checkedRegionId = stableId(regionId, "regionId");
        return validate(providers).stream()
                .anyMatch(provider -> provider.supportedRegionIds().contains(checkedRegionId));
    }

    private static void validateIds(Set<String> ids, String name) {
        for (String id : Objects.requireNonNull(ids, name)) {
            stableId(id, name + " entry");
        }
    }

    private static String stableId(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (!checked.matches("[a-z0-9_]+")) {
            throw new IllegalArgumentException(name + " must contain only lowercase letters, numbers and underscores");
        }
        return checked;
    }
}
