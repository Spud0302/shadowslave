package dev.spud.shadowslave.dreamrealm;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

/**
 * The Dream Realm as data packs describe it: its regions, the Seed
 * tuning and how creatures roam. {@link DreamRealmContentLoader} replaces it on every reload.
 */
public record DreamRealmContent(DreamSeedSettings seeds, RoamingSettings roaming, Map<ResourceLocation, DreamRegion> regions) {
    private static volatile DreamRealmContent current = new DreamRealmContent(DreamSeedSettings.DEFAULT, Map.of());

    public DreamRealmContent {
        regions = Map.copyOf(regions);
    }

    public DreamRealmContent(DreamSeedSettings seeds, Map<ResourceLocation, DreamRegion> regions) {
        this(seeds, RoamingSettings.DEFAULT, regions);
    }

    public static DreamRealmContent get() {
        return current;
    }

    public static void set(DreamRealmContent content) {
        current = content;
    }

    /** The region over a Dream Realm block: of the regions that cover it, the one that starts furthest out. */
    public Optional<Map.Entry<ResourceLocation, DreamRegion>> regionAt(BlockPos realm) {
        double distance = DreamRealm.distanceFromCentre(realm);
        return regions.entrySet().stream()
                .filter(entry -> entry.getValue().contains(distance))
                .max(Comparator.<Map.Entry<ResourceLocation, DreamRegion>>comparingInt(entry -> entry.getValue().minDistance())
                        .thenComparing(entry -> entry.getKey().toString()));
    }

    public Optional<DreamRegion> region(ResourceLocation id) {
        return Optional.ofNullable(regions.get(id));
    }
}
