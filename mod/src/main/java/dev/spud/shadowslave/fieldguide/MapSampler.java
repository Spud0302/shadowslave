package dev.spud.shadowslave.fieldguide;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;

/**
 * Reads a chunk's surface the way a vanilla map does: {@link FieldGuideData#SAMPLES}
 * by {@link FieldGuideData#SAMPLES} spots, each the top block's map colour,
 * shaded lighter or darker by the slope to the spot north of it. Colours are
 * packed vanilla map ids, so the client draws them with vanilla's palette.
 */
public final class MapSampler {
    private static final int STEP = 16 / FieldGuideData.SAMPLES;

    private MapSampler() {
    }

    public static byte[] sample(ServerLevel level, LevelChunk chunk) {
        byte[] colours = new byte[FieldGuideData.CHUNK_BYTES];
        int baseX = chunk.getPos().getMinBlockX();
        int baseZ = chunk.getPos().getMinBlockZ();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int sx = 0; sx < FieldGuideData.SAMPLES; sx++) {
            int previous = Integer.MIN_VALUE;
            for (int sz = 0; sz < FieldGuideData.SAMPLES; sz++) {
                int localX = sx * STEP + STEP / 2;
                int localZ = sz * STEP + STEP / 2;
                int height = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, localX, localZ);
                pos.set(baseX + localX, height, baseZ + localZ);
                BlockState state = chunk.getBlockState(pos);
                MapColor colour = state.getMapColor(level, pos);
                MapColor.Brightness brightness = MapColor.Brightness.NORMAL;
                if (colour == MapColor.WATER) {
                    brightness = waterShade(chunk, pos, height);
                } else if (previous != Integer.MIN_VALUE) {
                    brightness = height > previous ? MapColor.Brightness.HIGH
                            : height < previous ? MapColor.Brightness.LOW : MapColor.Brightness.NORMAL;
                }
                previous = height;
                colours[sz * FieldGuideData.SAMPLES + sx] = colour.getPackedId(brightness);
            }
        }
        return colours;
    }

    /** Deep water reads darker, as on a vanilla map. */
    private static MapColor.Brightness waterShade(LevelChunk chunk, BlockPos.MutableBlockPos pos, int top) {
        int depth = 0;
        while (depth < 10 && chunk.getBlockState(pos.setY(top - depth - 1)).getMapColor(chunk, pos) == MapColor.WATER) {
            depth++;
        }
        pos.setY(top);
        return depth > 6 ? MapColor.Brightness.LOW : depth > 2 ? MapColor.Brightness.NORMAL : MapColor.Brightness.HIGH;
    }
}
