package dev.spud.shadowslave.dreamrealm;

import dev.spud.shadowslave.dreamrealm.StormLanternCoastSitePlan.Piece;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Pure bridge from the Java-owned era-linked site plan into native chunk placement.
 *
 * <p>The current Dream Realm biome is still a development-wide fixed biome, so this
 * slice reserves one deterministic macro-area well away from the legacy preview
 * fixtures. The selected belfry chunk is derived from the existing site seed; the
 * rest of the site's global coordinates are projected from the same local offsets
 * already used by the command preview. Native migration can therefore happen one
 * chunk-safe historical piece at a time without inventing a second geography.</p>
 */
public final class StormLanternCoastNativePlacementPlan {
    public static final int MACRO_MIN_CHUNK_X = 64;
    public static final int MACRO_MIN_CHUNK_Z = -16;
    public static final int MACRO_SPAN_CHUNKS = 32;
    public static final String NATIVE_ANCHOR_ID = "storm_belfry";
    public static final String NATIVE_SEA_GATE_ID = "sea_gate";
    public static final String NATIVE_QUARRY_ID = "collapsed_quarry_cut";
    private static final List<String> NATIVE_HISTORICAL_PIECES = List.of(
            NATIVE_ANCHOR_ID,
            NATIVE_SEA_GATE_ID,
            NATIVE_QUARRY_ID
    );

    private StormLanternCoastNativePlacementPlan() {
    }

    public record NativeSite(
            long worldSeed,
            long siteSeed,
            int anchorChunkX,
            int anchorChunkZ,
            int siteOriginX,
            int siteOriginZ,
            Piece anchorPiece,
            List<Piece> pieces
    ) {
        public NativeSite {
            anchorPiece = Objects.requireNonNull(anchorPiece, "anchorPiece");
            pieces = List.copyOf(Objects.requireNonNull(pieces, "pieces"));
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
            for (String required : NATIVE_HISTORICAL_PIECES) {
                Piece piece = pieces.stream()
                        .filter(candidate -> candidate.anchorId().equals(required))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("site plan missing native piece " + required));
                if (!piece.historicalAnchor()) {
                    throw new IllegalArgumentException("native piece must preserve historical landmark: " + required);
                }
            }
        }

        /** Retained compatibility helper for the first belfry-only native slice. */
        public boolean ownsChunk(int chunkX, int chunkZ) {
            return chunkX == anchorChunkX && chunkZ == anchorChunkZ;
        }

        public int globalX(Piece piece) {
            return siteOriginX + Objects.requireNonNull(piece, "piece").x();
        }

        public int globalZ(Piece piece) {
            return siteOriginZ + Objects.requireNonNull(piece, "piece").z();
        }

        public int chunkX(Piece piece) {
            return Math.floorDiv(globalX(piece), 16);
        }

        public int chunkZ(Piece piece) {
            return Math.floorDiv(globalZ(piece), 16);
        }

        public Piece piece(String anchorId) {
            String checked = Objects.requireNonNull(anchorId, "anchorId").trim();
            if (checked.isEmpty()) throw new IllegalArgumentException("anchorId cannot be blank");
            return pieces.stream()
                    .filter(piece -> piece.anchorId().equals(checked))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("unknown site piece " + checked));
        }

        /**
         * Return the currently migrated historical piece owned by this generated chunk.
         * Only explicitly admitted pieces can execute through native worldgen.
         */
        public Optional<Piece> nativePieceForChunk(int chunkX, int chunkZ) {
            return pieces.stream()
                    .filter(piece -> NATIVE_HISTORICAL_PIECES.contains(piece.anchorId()))
                    .filter(piece -> chunkX(piece) == chunkX && chunkZ(piece) == chunkZ)
                    .findFirst();
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
                siteOriginX, siteOriginZ, anchor, site.pieces());
    }

    private static int boundedChunkOffset(long mixedSeed) {
        return (int) Math.floorMod(mixedSeed, (long) MACRO_SPAN_CHUNKS);
    }
}
