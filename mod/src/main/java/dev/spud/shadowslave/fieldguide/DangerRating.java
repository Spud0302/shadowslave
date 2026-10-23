package dev.spud.shadowslave.fieldguide;

import dev.spud.shadowslave.creature.CreatureRank;

import java.util.Optional;

/**
 * Danger ratings on the lore's creature ranks (DESIGN). An area starts at its
 * baseline (a Dream Realm region's rank, or unrated in the waking world),
 * rises to the strongest creature seen or slain there, and each step a
 * creature-caused death adds on top, up to Unholy.
 */
public final class DangerRating {
    public static final int NONE = -1;

    private DangerRating() {
    }

    /** The rating as a rank ordinal, or {@link #NONE} when nothing is known about the area. */
    public static int rate(int baseline, int strongestSeen, int deaths, int deathStep) {
        int rating = Math.max(baseline, strongestSeen);
        if (deaths > 0 && deathStep > 0) {
            rating = Math.max(rating, 0) + deathStep;
        }
        return Math.min(rating, CreatureRank.UNHOLY.ordinal());
    }

    public static Optional<CreatureRank> rank(int rating) {
        return rating < 0 ? Optional.empty() : Optional.of(CreatureRank.byOrdinal(rating));
    }

    /** The area an x or z coordinate falls in. */
    public static int cell(int block, int cellSize) {
        return Math.floorDiv(block, cellSize);
    }

    public static long key(int cellX, int cellZ) {
        return ((long) cellX << 32) | (cellZ & 0xFFFFFFFFL);
    }

    public static int keyX(long key) {
        return (int) (key >> 32);
    }

    public static int keyZ(long key) {
        return (int) key;
    }
}
