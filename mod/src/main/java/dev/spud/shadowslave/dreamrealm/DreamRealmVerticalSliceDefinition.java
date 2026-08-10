package dev.spud.shadowslave.dreamrealm;

import dev.spud.shadowslave.dreamrealm.content.DreamRealmRegionContentCatalog;

import java.util.List;

/**
 * Bounded DESIGN binding between one Java-owned Dream Realm region profile and
 * its first removable Minecraft execution slice.
 */
public final class DreamRealmVerticalSliceDefinition {
    public static final String REGION_ID = "ashen_expanse";

    private DreamRealmVerticalSliceDefinition() {
    }

    public record Placement(String hookId, int x, int y, int z) {
    }

    public record Slice(
            DreamRealmRegionContentCatalog.RegionProfile region,
            List<Placement> landmarks,
            List<Placement> resources
    ) {
        public Slice {
            landmarks = List.copyOf(landmarks);
            resources = List.copyOf(resources);
        }
    }

    public static Slice ashenExpanse() {
        DreamRealmRegionContentCatalog.RegionProfile region = DreamRealmRegionContentCatalog.waveOne().stream()
                .filter(candidate -> candidate.id().equals(REGION_ID))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing Java-owned Dream Realm region " + REGION_ID));

        return new Slice(
                region,
                List.of(
                        new Placement("buried_watchtower", 14, 0, -9),
                        new Placement("black_obelisk", -15, 0, 8),
                        new Placement("shattered_causeway", 0, 0, 14)
                ),
                List.of(
                        new Placement("bone_char", -7, 0, -10),
                        new Placement("ruin_metal", 9, 0, 6),
                        new Placement("dry_fungus", -11, 0, 13)
                )
        );
    }
}
