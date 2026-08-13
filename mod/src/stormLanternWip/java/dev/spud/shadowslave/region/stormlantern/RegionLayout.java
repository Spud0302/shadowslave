package dev.spud.shadowslave.region.stormlantern;

import java.util.List;
import java.util.SplittableRandom;

/** Extracted deterministic placement vocabulary from the reviewed Storm Lantern branch. */
public final class RegionLayout {
    private RegionLayout() {}

    public record Piece(String anchorId, String family, int variant, int x, int y, int z, boolean historical) {}

    public static List<Piece> create(long worldSeed) {
        SplittableRandom random = new SplittableRandom(worldSeed ^ 0x9E3779B97F4A7C15L);
        return List.of(
                piece(random, "storm_belfry", "ruined_belfry", 3, -20, 3, -17, true),
                piece(random, "sea_gate", "broken_sea_gate", 3, 18, 0, 18, true),
                piece(random, "collapsed_quarry_cut", "collapsed_quarry", 3, 24, 5, -20, true),
                piece(random, "harbour_terraces", "drowned_terraces", 3, -10, -2, 20, true),
                piece(random, "coast_watch_0", "cliff_lantern", 3, -32, 7, 4, false),
                piece(random, "coast_watch_1", "cliff_lantern", 3, 31, 8, 8, false),
                piece(random, "storm_shelter", "collapsed_shelter", 4, 4, 3, -31, false),
                piece(random, "salvage_ledge", "storm_wreckage", 4, 13, 1, 28, false)
        );
    }

    private static Piece piece(SplittableRandom random, String anchorId, String family, int variants,
                               int x, int y, int z, boolean historical) {
        return new Piece(anchorId, family, random.nextInt(variants), x, y, z, historical);
    }
}
