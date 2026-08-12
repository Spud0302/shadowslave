package dev.spud.shadowslave.dreamrealm;

import com.mojang.serialization.Codec;
import dev.spud.shadowslave.dreamrealm.StormLanternCoastSitePlan.Piece;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Native world-generation executor for migrated later-era Drowned Bell landmarks.
 *
 * <p>The storm belfry, broken Sea Gate and collapsed quarry cut currently cross this
 * boundary. All are projected from the same Java-owned historical-site frame. The
 * surrounding coast, drowned terraces, encounters and clues still use the bounded
 * command fixture until subsequent slices migrate them.</p>
 */
public final class DrownedBellLaterAnchorFeature extends Feature<NoneFeatureConfiguration> {
    public DrownedBellLaterAnchorFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        StormLanternCoastNativePlacementPlan.NativeSite nativeSite =
                StormLanternCoastNativePlacementPlan.drownedBellLater(level.getSeed());
        BlockPos attemptOrigin = context.origin();
        int chunkX = attemptOrigin.getX() >> 4;
        int chunkZ = attemptOrigin.getZ() >> 4;

        Piece piece = nativeSite.nativePieceForChunk(chunkX, chunkZ).orElse(null);
        if (piece == null) return false;

        int centerX = nativeSite.globalX(piece);
        int centerZ = nativeSite.globalZ(piece);
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, centerX, centerZ);
        BlockPos base = new BlockPos(centerX, surfaceY, centerZ);

        return switch (piece.anchorId()) {
            case StormLanternCoastNativePlacementPlan.NATIVE_ANCHOR_ID -> {
                buildRuinedBelfry(level, base, piece);
                yield true;
            }
            case StormLanternCoastNativePlacementPlan.NATIVE_SEA_GATE_ID -> {
                buildBrokenSeaGate(level, base, piece);
                yield true;
            }
            case StormLanternCoastNativePlacementPlan.NATIVE_QUARRY_ID -> {
                buildCollapsedQuarryCut(level, base, piece);
                yield true;
            }
            default -> false;
        };
    }

    private static void buildRuinedBelfry(WorldGenLevel level, BlockPos base, Piece piece) {
        int brokenSide = Math.floorMod(piece.variant() + piece.rotation().ordinal(), 4);
        for (int y = 0; y <= 11; y++) {
            for (int side = 0; side < 4; side++) {
                if (side == brokenSide && y > 5) continue;
                int[] offset = sideOffset(side, 3);
                set(level, base.offset(offset[0], y, offset[1]),
                        y < 3
                                ? Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
                                : Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
            }
        }

        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                if (Math.abs(x) == 3 || Math.abs(z) == 3) {
                    set(level, base.offset(x, 9, z), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
                }
            }
        }
        set(level, base.offset(0, 8, 0), Blocks.BELL.defaultBlockState());
    }

    /**
     * Compact one-chunk-safe silhouette for the later broken Sea Gate works.
     * The authored sea-gate anchor currently projects to local chunk coordinates
     * (14, 11), so this migration deliberately grows only along Z and upward.
     */
    private static void buildBrokenSeaGate(WorldGenLevel level, BlockPos base, Piece piece) {
        int brokenPillar = Math.floorMod(piece.variant() + piece.rotation().ordinal(), 2);
        for (int pillar = 0; pillar < 2; pillar++) {
            int z = pillar == 0 ? -2 : 2;
            int height = pillar == brokenPillar ? 3 : 5;
            for (int y = 0; y <= height; y++) {
                set(level, base.offset(0, y, z),
                        y <= 1
                                ? Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
                                : Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
            }
        }

        // A fractured lintel keeps the old gate silhouette readable without
        // crossing the generated chunk boundary.
        int lintelY = 5;
        for (int z = -1; z <= 1; z++) {
            if (z == 0 && brokenPillar == 0) continue;
            set(level, base.offset(0, lintelY, z), Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
        }
        set(level, base.offset(0, 0, 0), Blocks.PRISMARINE_BRICKS.defaultBlockState());
    }

    /**
     * Chunk-safe later-era quarry approach. The stable quarry anchor projects to
     * local chunk coordinate (4, 5), leaving room for a narrow fractured cut on all
     * sides without writing into a neighbouring chunk during feature generation.
     *
     * <p>The shape provides a readable approach and partial shelter/information value:
     * a gravel floor leads between broken retaining shoulders toward a collapsed
     * arch and rubble choke instead of representing the landmark as a decorative
     * marker block.</p>
     */
    private static void buildCollapsedQuarryCut(WorldGenLevel level, BlockPos base, Piece piece) {
        int collapseSide = Math.floorMod(piece.variant() + piece.rotation().ordinal(), 2) == 0 ? -1 : 1;

        // Traversable cut floor and retaining shoulders.
        for (int z = -1; z <= 5; z++) {
            for (int x = -2; x <= 2; x++) {
                set(level, base.offset(x, 0, z),
                        Math.floorMod(x + z + piece.variant(), 3) == 0
                                ? Blocks.COARSE_DIRT.defaultBlockState()
                                : Blocks.GRAVEL.defaultBlockState());
                if (Math.abs(x) <= 1) {
                    for (int y = 1; y <= 3; y++) {
                        set(level, base.offset(x, y, z), Blocks.AIR.defaultBlockState());
                    }
                }
            }
            for (int y = 1; y <= 3; y++) {
                set(level, base.offset(-3, y, z),
                        y == 3 ? Blocks.TUFF.defaultBlockState() : Blocks.COBBLED_DEEPSLATE.defaultBlockState());
                set(level, base.offset(3, y, z),
                        y == 3 ? Blocks.TUFF.defaultBlockState() : Blocks.COBBLED_DEEPSLATE.defaultBlockState());
            }
        }

        // Fractured terminal arch: recognizable as an old worked entrance, but visibly collapsed.
        for (int x = -3; x <= 3; x++) {
            if (x == collapseSide || x == collapseSide * 2) continue;
            set(level, base.offset(x, 4, 5), Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
        }
        for (int y = 1; y <= 4; y++) {
            set(level, base.offset(-3, y, 5), Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
            if (!(collapseSide > 0 && y >= 3)) {
                set(level, base.offset(3, y, 5), Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
            }
        }

        // A small deterministic rubble choke leaves the passage readable but not pristine.
        set(level, base.offset(collapseSide, 1, 4), Blocks.TUFF.defaultBlockState());
        set(level, base.offset(collapseSide * 2, 1, 5), Blocks.COBBLED_DEEPSLATE.defaultBlockState());
        if (piece.variant() == 2) {
            set(level, base.offset(0, 1, 5), Blocks.GRAVEL.defaultBlockState());
        }
    }

    private static int[] sideOffset(int side, int radius) {
        return switch (side) {
            case 0 -> new int[]{-radius, 0};
            case 1 -> new int[]{radius, 0};
            case 2 -> new int[]{0, -radius};
            case 3 -> new int[]{0, radius};
            default -> throw new IllegalArgumentException("invalid side");
        };
    }

    private static void set(WorldGenLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, 2);
    }
}
