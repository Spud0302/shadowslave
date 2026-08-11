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
 * First native world-generation executor for an era-linked Dream Realm landmark.
 *
 * <p>This intentionally places only the later-era storm belfry. The surrounding
 * coast, remaining historical anchors, encounters and clues still use the bounded
 * command fixture until subsequent slices migrate them. Java site identity and
 * deterministic anchor geography remain outside this Minecraft executor.</p>
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
        if (!nativeSite.ownsChunk(chunkX, chunkZ)) return false;

        Piece belfry = nativeSite.anchorPiece();
        int centerX = chunkX * 16 + 8;
        int centerZ = chunkZ * 16 + 8;
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, centerX, centerZ);
        BlockPos base = new BlockPos(centerX, surfaceY, centerZ);
        buildRuinedBelfry(level, base, belfry);
        return true;
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
