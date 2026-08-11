package dev.spud.shadowslave.dreamrealm;

import dev.spud.shadowslave.dreamrealm.content.DreamRealmRegionContentCatalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SplittableRandom;

/**
 * Pure deterministic encounter budget for the later-era Storm Lantern Coast preview.
 * Creature identity comes from the Java-owned region profile; Minecraft entities are
 * only physical executors in {@link StormLanternCoastPreviewService}.
 */
public final class StormLanternCoastEncounterPlan {
    private static final Set<String> EXECUTABLE_AFFINITIES = Set.of("drowned_listener", "chainback");

    private StormLanternCoastEncounterPlan() {
    }

    public enum Pressure {
        FLOOD_EDGE,
        RUIN_GUARD,
        EXPOSED_ROUTE
    }

    public record Encounter(
            String creatureId,
            String anchorId,
            Pressure pressure,
            int x,
            int y,
            int z
    ) {
        public Encounter {
            creatureId = requireText(creatureId, "creatureId");
            anchorId = requireText(anchorId, "anchorId");
            pressure = Objects.requireNonNull(pressure, "pressure");
            if (!EXECUTABLE_AFFINITIES.contains(creatureId)) {
                throw new IllegalArgumentException("encounter creature is not physically executable: " + creatureId);
            }
        }
    }

    public record Plan(long encounterSeed, List<Encounter> encounters) {
        public Plan {
            encounters = List.copyOf(Objects.requireNonNull(encounters, "encounters"));
            if (encounters.size() < 2 || encounters.size() > 4) {
                throw new IllegalArgumentException("Storm Lantern encounter budget must contain 2-4 encounters");
            }
        }
    }

    public static Plan forSite(StormLanternCoastSitePlan.Plan sitePlan) {
        Objects.requireNonNull(sitePlan, "sitePlan");
        DreamRealmRegionContentCatalog.RegionProfile region = sitePlan.region();
        if (!StormLanternCoastSitePlan.REGION_ID.equals(region.id())) {
            throw new IllegalArgumentException("wrong region");
        }
        if (!region.creatureAffinityIds().containsAll(EXECUTABLE_AFFINITIES)) {
            throw new IllegalStateException("Storm Lantern Coast no longer authorizes the executable encounter affinities");
        }

        long encounterSeed = mix(sitePlan.siteSeed(), "encounters");
        SplittableRandom random = new SplittableRandom(encounterSeed);
        ArrayList<Encounter> encounters = new ArrayList<>();

        // The drowned terraces are the strongest flood-edge pressure point and always
        // receive the region-affine Listener executor, but the exact offset varies.
        StormLanternCoastSitePlan.Piece terraces = piece(sitePlan, "drowned_harbour_terraces");
        encounters.add(new Encounter(
                "drowned_listener",
                terraces.anchorId(),
                Pressure.FLOOD_EDGE,
                terraces.x() + jitter(random, 4),
                terraces.y() + 3,
                terraces.z() + jitter(random, 4)
        ));

        // Quarry/belfry pressure varies by seed so the same region identity does not
        // collapse into one guaranteed encounter composition or one fixed coordinate.
        StormLanternCoastSitePlan.Piece ruin = random.nextBoolean()
                ? piece(sitePlan, "collapsed_quarry_cut")
                : piece(sitePlan, "storm_belfry");
        encounters.add(new Encounter(
                "chainback",
                ruin.anchorId(),
                Pressure.RUIN_GUARD,
                ruin.x() + jitter(random, 6),
                ruin.y() + 2,
                ruin.z() + jitter(random, 6)
        ));

        // Exposure budget: some seeds add one or two roaming pressure points around
        // the high route / cliff lanterns. These use only region-authorized executable
        // creatures and remain reproducible from the site seed.
        int extra = random.nextInt(3); // 0-2, producing a total budget of 2-4.
        for (int i = 0; i < extra; i++) {
            StormLanternCoastSitePlan.Piece watch = piece(sitePlan, "coast_watch_" + i);
            String creature = random.nextBoolean() ? "chainback" : "drowned_listener";
            encounters.add(new Encounter(
                    creature,
                    watch.anchorId(),
                    Pressure.EXPOSED_ROUTE,
                    watch.x() + jitter(random, 7),
                    watch.y() + 1,
                    watch.z() + jitter(random, 7)
            ));
        }

        return new Plan(encounterSeed, encounters);
    }

    private static StormLanternCoastSitePlan.Piece piece(StormLanternCoastSitePlan.Plan plan, String anchorId) {
        return plan.pieces().stream()
                .filter(piece -> piece.anchorId().equals(anchorId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing Storm Lantern site anchor " + anchorId));
    }

    private static int jitter(SplittableRandom random, int radius) {
        return random.nextInt(radius * 2 + 1) - radius;
    }

    private static long mix(long seed, String stableId) {
        long value = seed ^ 0xD6E8FEB86659FD93L;
        for (int i = 0; i < stableId.length(); i++) {
            value ^= stableId.charAt(i) * 0x9E3779B97F4A7C15L;
            value = Long.rotateLeft(value, 17) * 0x94D049BB133111EBL;
        }
        value ^= value >>> 31;
        return value;
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) throw new IllegalArgumentException(name + " cannot be blank");
        return checked;
    }
}
