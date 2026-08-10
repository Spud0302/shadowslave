package dev.spud.shadowslave.dreamrealm;

import dev.spud.shadowslave.dreamrealm.DreamRealmVerticalSliceDefinition.Placement;

/**
 * DESIGN execution binding for one already-authored Dream Realm resource hook.
 * The hook identity remains owned by the Java region definition; this class only
 * supplies bounded Minecraft interaction copy and the physical cluster footprint.
 */
public final class DreamRealmResourceInteractionBinding {
    public static final String RESOURCE_ID = "ruin_metal";

    private DreamRealmResourceInteractionBinding() {}

    public record Interaction(
            String regionId,
            String resourceId,
            int x,
            int y,
            int z,
            String inspection,
            String boundary
    ) {}

    public static Interaction ashenExpanseRuinMetal() {
        var slice = DreamRealmVerticalSliceDefinition.ashenExpanse();
        Placement placement = slice.resources().stream()
                .filter(candidate -> candidate.hookId().equals(RESOURCE_ID))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing authored Dream Realm resource hook " + RESOURCE_ID));

        if (!slice.region().resourceHooks().contains(RESOURCE_ID)) {
            throw new IllegalStateException("Physical resource is not authorized by the Java-owned region profile: " + RESOURCE_ID);
        }

        return new Interaction(
                slice.region().id(),
                RESOURCE_ID,
                placement.x(), placement.y(), placement.z(),
                "Ruin metal — old worked metal survives beneath the ash. It may be worth salvaging, but this preview does not invent a material yield.",
                "Inspection reveals only the authored SALVAGE opportunity; no item, currency, Soul Shard, progression, ownership, or canonical resource value is granted."
        );
    }

    /** Matches the three-block cluster built by DreamRealmPreviewService. */
    public static boolean isPhysicalClusterOffset(Interaction interaction, int x, int y, int z) {
        return (x == interaction.x() && y == interaction.y() + 1 && z == interaction.z())
                || (x == interaction.x() && y == interaction.y() + 2 && z == interaction.z())
                || (x == interaction.x() + 1 && y == interaction.y() + 1 && z == interaction.z());
    }
}
