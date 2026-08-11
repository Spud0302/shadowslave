package dev.spud.shadowslave.dreamrealm;

import dev.spud.shadowslave.dreamrealm.content.DreamRealmRegionContentCatalog;
import dev.spud.shadowslave.nightmare.content.NightmareHistoricalSiteCatalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SplittableRandom;

/**
 * Pure deterministic placement plan for the later-era Drowned Bell site inside
 * Storm Lantern Coast. Minecraft blocks are an execution detail handled by the
 * preview service; this class owns only reproducible piece selection/placement.
 */
public final class StormLanternCoastSitePlan {
    public static final String REGION_ID = "storm_lantern_coast";
    public static final String SITE_ID = "drowned_bell_cliff_settlement";

    private StormLanternCoastSitePlan() {
    }

    public enum Rotation {
        NORTH,
        EAST,
        SOUTH,
        WEST
    }

    public record Piece(
            String anchorId,
            String pieceFamily,
            int variant,
            int x,
            int y,
            int z,
            Rotation rotation,
            boolean historicalAnchor
    ) {
        public Piece {
            anchorId = requireText(anchorId, "anchorId");
            pieceFamily = requireText(pieceFamily, "pieceFamily");
            if (variant < 0) throw new IllegalArgumentException("variant cannot be negative");
            rotation = Objects.requireNonNull(rotation, "rotation");
        }
    }

    public record Plan(
            long siteSeed,
            DreamRealmRegionContentCatalog.RegionProfile region,
            NightmareHistoricalSiteCatalog.Site site,
            List<Piece> pieces
    ) {
        public Plan {
            region = Objects.requireNonNull(region, "region");
            site = Objects.requireNonNull(site, "site");
            pieces = List.copyOf(Objects.requireNonNull(pieces, "pieces"));
            if (!region.id().equals(REGION_ID)) throw new IllegalArgumentException("wrong region");
            if (!site.id().equals(SITE_ID)) throw new IllegalArgumentException("wrong historical site");
            if (pieces.stream().filter(Piece::historicalAnchor).count() < 4) {
                throw new IllegalArgumentException("later-era plan must preserve all four recognizable historical anchors");
            }
        }
    }

    /** Same world seed + stable site identity always returns the same layout. */
    public static Plan drownedBellLater(long worldSeed) {
        DreamRealmRegionContentCatalog.RegionProfile region = DreamRealmRegionContentCatalog.waveOne().stream()
                .filter(candidate -> candidate.id().equals(REGION_ID))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing Dream Realm region " + REGION_ID));
        NightmareHistoricalSiteCatalog.Site site = NightmareHistoricalSiteCatalog.drownedBell();
        long siteSeed = mix(worldSeed, SITE_ID);
        SplittableRandom random = new SplittableRandom(siteSeed);

        ArrayList<Piece> pieces = new ArrayList<>();
        // Stable silhouettes/anchors: exact variants and rotations vary by world,
        // but their relative geography remains recognizable across the later site.
        pieces.add(piece(random, "storm_belfry", "ruined_belfry", 3, -20, 3, -17, true));
        pieces.add(piece(random, "sea_gate", "broken_sea_gate", 3, 18, 0, 18, true));
        pieces.add(piece(random, "collapsed_quarry_cut", "collapsed_quarry", 3, 24, 5, -20, true));
        pieces.add(piece(random, "drowned_harbour_terraces", "drowned_terraces", 3, -10, -2, 20, true));

        // Generic exploration pieces are deterministic selections from modular
        // families rather than one giant fixed structure.
        pieces.add(piece(random, "coast_watch_0", "cliff_lantern", 3, -32, 7, 4, false));
        pieces.add(piece(random, "coast_watch_1", "cliff_lantern", 3, 31, 8, 8, false));
        pieces.add(piece(random, "storm_shelter", "collapsed_shelter", 4, 4, 3, -31, false));
        pieces.add(piece(random, "salvage_ledge", "storm_wreckage", 4, 13, 1, 28, false));
        return new Plan(siteSeed, region, site, pieces);
    }

    private static Piece piece(SplittableRandom random, String anchorId, String family, int variants,
                               int x, int y, int z, boolean historicalAnchor) {
        return new Piece(anchorId, family, random.nextInt(variants), x, y, z,
                Rotation.values()[random.nextInt(Rotation.values().length)], historicalAnchor);
    }

    private static long mix(long worldSeed, String stableId) {
        long value = worldSeed ^ 0x9E3779B97F4A7C15L;
        for (int i = 0; i < stableId.length(); i++) {
            value ^= stableId.charAt(i) * 0x100000001B3L;
            value = Long.rotateLeft(value, 13) * 0xC2B2AE3D27D4EB4FL;
        }
        value ^= value >>> 29;
        value *= 0x165667B19E3779F9L;
        return value ^ (value >>> 32);
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) throw new IllegalArgumentException(name + " cannot be blank");
        return checked;
    }
}
