package dev.spud.shadowslave.dreamrealm;

import dev.spud.shadowslave.dreamrealm.StormLanternCoastSitePlan.Piece;

import java.util.Objects;

/**
 * Pure bridge from the Java-owned era-linked site plan into native chunk placement.
 *
 * <p>The current Dream Realm biome is still a development-wide fixed biome, so this
 * slice reserves one deterministic macro-area well away from the legacy preview
 * fixtures. The selected belfry chunk is derived from the existing site seed; the
 * rest of the site's future global coordinates are then projected from the same
 * local offsets already used by the command preview. This lets later native pieces
 * migrate without inventing a second geography.</p>
 */
public final class StormLanternCoastNativePlacementPlan {
    public static final int MACRO_MIN_CHUNK_X = 64;
    public static final int MACRO_MIN_CHUNK_Z = -16;
    public static final int MACRO_SPAN_CHUNKS = 32;
    public static final String NATIVE_ANCHOR_ID = "storm_belfry";

    private StormLanternCoastNativePlacementPlan() {
    }

    public record NativeSite(
            long worldSeed,
            long siteSeed,
            int anchorChunkX,
            int anchorChunkZ,
            int siteOriginX,
            int siteOriginZ,
            Piece anchorPiece
    ) {
        public NativeSite {
            anchorPiece = Objects.requireNonNull(anchorPiece, "anchorPiece");
            if (!anchorPiece.anchorId().equals(NATIVE_ANCHOR_ID)) {
                throw new IllegalArgumentException("native anchor must be " + NATIVE_ANCHOR_ID);
            }
            if (!anchorPiece.historicalAnchor()) {
                throw new IllegalArgumentException("native anchor must preserve a historical landmark");
            }
            if (anchorChunkX < MACRO_MIN_CHUNK_X || anchorChunkX >= MACRO_MIN_CHUNK_X + MACRO_SPAN_CHUNKS) {
                throw new IllegalArgumentException("anchorChunkX outside reserved macro-area");
            }
            if (anchorChunkZ < MACRO_MIN_CHUNK_Z || anchorChunkZ >= MACRO_MIN_CHUNK_Z + MACRO_SPAN_CHUNKS) {
                throw new IllegalArgumentException("anchorChunkZ outside reserved macro-area");
            }
        }

        public boolean ownsChunk(int chunkX, int chunkZ) {
            return chunkX == anchorChunkX && chunkZ == anchorChunkZ;
        }

        public int globalX(Piece piece) {
            return siteOriginX + Objects.requireNonNull(piece, "piece").x();
        }

        public int globalZ(Piece piece) {
            return siteOriginZ + Objects.requireNonNull(piece, "piece").z();
        }
    }

    /** Same world seed always reserves the same native Drowned Bell anchor chunk. */
    public static NativeSite drownedBellLater(long worldSeed) {
        StormLanternCoastSitePlan.Plan site = StormLanternCoastSitePlan.drownedBellLater(worldSeed);
        Piece anchor = site.pieces().stream()
                .filter(piece -> piece.anchorId().equals(NATIVE_ANCHOR_ID))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Later Drowned Bell site is missing " + NATIVE_ANCHOR_ID));

        int anchorChunkX = MACRO_MIN_CHUNK_X + boundedChunkOffset(site.siteSeed());
        int anchorChunkZ = MACRO_MIN_CHUNK_Z + boundedChunkOffset(Long.rotateLeft(site.siteSeed(), 29));
        int anchorBlockX = anchorChunkX * 16 + 8;
        int anchorBlockZ = anchorChunkZ * 16 + 8;

        // Project the complete site's local coordinate frame from the belfry anchor.
        int siteOriginX = anchorBlockX - anchor.x();
        int siteOriginZ = anchorBlockZ - anchor.z();
        return new NativeSite(worldSeed, site.siteSeed(), anchorChunkX, anchorChunkZ,
                siteOriginX, siteOriginZ, anchor);
    }

    private static int boundedChunkOffset(long mixedSeed) {
        return (int) Math.floorMod(mixedSeed, (long) MACRO_SPAN_CHUNKS);
    }
}
