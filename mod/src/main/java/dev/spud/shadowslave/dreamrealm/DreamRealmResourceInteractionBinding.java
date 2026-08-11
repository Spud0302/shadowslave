package dev.spud.shadowslave.dreamrealm;

import dev.spud.shadowslave.dreamrealm.DreamRealmVerticalSliceDefinition.Placement;

import java.util.List;

/**
 * DESIGN execution bindings for the already-authored Ashen Expanse resource hooks.
 * Hook identity remains owned by the Java region definition; this class supplies
 * bounded inspection copy and physical cluster footprints only.
 */
public final class DreamRealmResourceInteractionBinding {
    private DreamRealmResourceInteractionBinding() {}

    public record Interaction(
            String regionId,
            String resourceId,
            String physicalBlockId,
            int x,
            int y,
            int z,
            String inspection,
            String boundary
    ) {}

    public static List<Interaction> ashenExpanseResources() {
        return List.of(
                bind(
                        "bone_char",
                        "minecraft:bone_block",
                        "Bone char — pale, fire-marked remains break through the ash here. This preview records the find without deciding whether it is useful material.",
                        "Inspection reveals only the authored resource hook; no item, food, Soul Shard, progression, ownership, or canonical material use is granted."),
                bind(
                        "ruin_metal",
                        "shadowslave:ruin_metal",
                        "Ruin metal — old worked metal survives beneath the ash. It may be worth salvaging, but this preview does not invent a material yield.",
                        "Inspection reveals only the authored SALVAGE opportunity; no item, currency, Soul Shard, progression, ownership, or canonical resource value is granted."),
                bind(
                        "dry_fungus",
                        "minecraft:brown_mushroom_block",
                        "Dry fungus — brittle growth clings to a patch sheltered from the worst ash. Its presence is observable; edibility and other uses remain unknown.",
                        "Inspection reveals only the authored resource hook; no food, healing, item, progression, ownership, or canonical biological property is granted.")
        );
    }

    private static Interaction bind(String resourceId, String physicalBlockId, String inspection, String boundary) {
        var slice = DreamRealmVerticalSliceDefinition.ashenExpanse();
        Placement placement = slice.resources().stream()
                .filter(candidate -> candidate.hookId().equals(resourceId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing authored Dream Realm resource hook " + resourceId));

        if (!slice.region().resourceHooks().contains(resourceId)) {
            throw new IllegalStateException("Physical resource is not authorized by the Java-owned region profile: " + resourceId);
        }

        return new Interaction(
                slice.region().id(),
                resourceId,
                physicalBlockId,
                placement.x(), placement.y(), placement.z(),
                inspection,
                boundary
        );
    }

    /** Matches the three-block cluster shape built for every resource by DreamRealmPreviewService. */
    public static boolean isPhysicalClusterOffset(Interaction interaction, int x, int y, int z) {
        return (x == interaction.x() && y == interaction.y() + 1 && z == interaction.z())
                || (x == interaction.x() && y == interaction.y() + 2 && z == interaction.z())
                || (x == interaction.x() + 1 && y == interaction.y() + 1 && z == interaction.z());
    }
}
