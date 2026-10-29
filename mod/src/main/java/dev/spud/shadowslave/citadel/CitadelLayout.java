package dev.spud.shadowslave.citadel;

import dev.spud.shadowslave.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * The shape of a Citadel (DESIGN): a square curtain wall with a gate in the
 * south side and a tower at each corner, around a courtyard where the
 * Gateway stands on a dais under a stone arch, between lit Dream Candles.
 * The same layout is laid by world generation, chunk by chunk, and by the
 * operator command, so it only talks to a {@link Canvas}. Wear (cracked
 * stone, fallen merlons) is a pure function of the position, so every chunk
 * agrees on it.
 */
public final class CitadelLayout {
    /** Walls stand this many blocks over the courtyard floor; towers rise higher. */
    static final int WALL_HEIGHT = 6;
    static final int TOWER_HEIGHT = 9;
    /** How deep the foundation may reach down to find the ground. */
    static final int FOUNDATION = 12;
    static final int CLEAR = TOWER_HEIGHT + 2;

    /** Where a Citadel's blocks go. */
    public interface Canvas {
        BlockState get(BlockPos pos);

        void set(BlockPos pos, BlockState state);

        /** Whether this canvas holds a block; world generation only reaches one chunk at a time. */
        default boolean covers(BlockPos pos) {
            return true;
        }
    }

    private CitadelLayout() {
    }

    /** The Gateway's place for a Citadel standing at {@code centre} (the courtyard floor's walking level). */
    public static BlockPos gateway(BlockPos centre) {
        return centre.above();
    }

    /** Everything a Citadel of this size touches, foundation and towers included. */
    public static BoundingBox bounds(BlockPos centre, int half) {
        int reach = half + 1;
        return new BoundingBox(centre.getX() - reach, centre.getY() - 1 - FOUNDATION, centre.getZ() - reach,
                centre.getX() + reach, centre.getY() + CLEAR, centre.getZ() + reach);
    }

    public static void build(Canvas canvas, BlockPos centre, int half, CitadelPalette palette) {
        int reach = half + 1;
        int y = centre.getY();
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                if (!inFootprint(dx, dz, half)) {
                    continue;
                }
                BlockPos column = centre.offset(dx, 0, dz);
                if (!canvas.covers(column)) {
                    continue;
                }
                for (int up = 0; up <= CLEAR; up++) {
                    canvas.set(column.above(up), Blocks.AIR.defaultBlockState());
                }
                canvas.set(column.below(), floor(dx, dz, half, palette));
                for (int down = 2; down <= FOUNDATION + 1; down++) {
                    BlockPos below = column.below(down);
                    BlockState there = canvas.get(below);
                    if (!there.isAir() && !there.canBeReplaced() && there.getFluidState().isEmpty()) {
                        break;
                    }
                    canvas.set(below, palette.wall());
                }
            }
        }
        walls(canvas, centre, half, palette);
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sz = -1; sz <= 1; sz += 2) {
                tower(canvas, centre.offset(sx * (half - 1), 0, sz * (half - 1)), palette);
                lampPost(canvas, centre.offset(sx * (half - 4), 0, sz * (half - 4)), palette);
            }
        }
        dais(canvas, centre, palette);
    }

    private static boolean inFootprint(int dx, int dz, int half) {
        int ax = Math.abs(dx);
        int az = Math.abs(dz);
        return Math.max(ax, az) <= half || (ax >= half - 3 && az >= half - 3);
    }

    private static BlockState floor(int dx, int dz, int half, CitadelPalette palette) {
        if (Math.max(Math.abs(dx), Math.abs(dz)) >= half) {
            return palette.wall();
        }
        if (Math.abs(dx) <= 1 && dz > 3) {
            return palette.trim();
        }
        return palette.floor();
    }

    private static void walls(Canvas canvas, BlockPos centre, int half, CitadelPalette palette) {
        for (int i = -half; i <= half; i++) {
            for (int[] side : new int[][]{{i, -half}, {i, half}, {-half, i}, {half, i}}) {
                int dx = side[0];
                int dz = side[1];
                boolean gate = dz == half && Math.abs(dx) <= 1;
                for (int up = 0; up < WALL_HEIGHT; up++) {
                    BlockPos pos = centre.offset(dx, up, dz);
                    if (gate && up < 4) {
                        continue;
                    }
                    BlockState stone = gate && up == 4 ? palette.accent() : worn(pos, centre, palette, 0.18);
                    canvas.set(pos, stone);
                }
                if (Math.floorMod(dx + dz, 2) == 0 && chance(centre.offset(dx, WALL_HEIGHT, dz), centre) > 0.08) {
                    canvas.set(centre.offset(dx, WALL_HEIGHT, dz), worn(centre.offset(dx, WALL_HEIGHT, dz), centre, palette, 0.25));
                }
            }
        }
    }

    private static void tower(Canvas canvas, BlockPos base, CitadelPalette palette) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                boolean edge = Math.abs(dx) == 2 || Math.abs(dz) == 2;
                boolean corner = Math.abs(dx) == 2 && Math.abs(dz) == 2;
                for (int up = 0; up <= TOWER_HEIGHT; up++) {
                    BlockPos pos = base.offset(dx, up, dz);
                    if (up == TOWER_HEIGHT) {
                        canvas.set(pos, palette.trim());
                    } else if (corner) {
                        canvas.set(pos, palette.pillar());
                    } else if (edge) {
                        canvas.set(pos, up == TOWER_HEIGHT - 2 && (dx == 0 || dz == 0) ? palette.glow() : worn(pos, base, palette, 0.15));
                    }
                }
                if (edge && Math.floorMod(dx + dz, 2) == 0) {
                    canvas.set(base.offset(dx, TOWER_HEIGHT + 1, dz), palette.wall());
                }
            }
        }
        canvas.set(base.above(TOWER_HEIGHT + 1), lantern(palette, false));
    }

    private static void lampPost(Canvas canvas, BlockPos base, CitadelPalette palette) {
        for (int up = 0; up < 3; up++) {
            canvas.set(base.above(up), palette.pillar());
        }
        canvas.set(base.above(3), lantern(palette, false));
    }

    private static void dais(Canvas canvas, BlockPos centre, CitadelPalette palette) {
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                boolean rim = Math.abs(dx) == 3 || Math.abs(dz) == 3;
                canvas.set(centre.offset(dx, 0, dz), rim ? palette.accent() : palette.trim());
            }
        }
        canvas.set(centre, palette.glow());
        canvas.set(gateway(centre), CitadelRegistry.GATEWAY.get().defaultBlockState());
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int up = 1; up <= 4; up++) {
                canvas.set(centre.offset(sx * 2, up, 0), palette.pillar());
            }
            for (int sz = -1; sz <= 1; sz += 2) {
                canvas.set(centre.offset(sx * 2, 1, sz * 2), ModBlocks.DREAM_CANDLE.get().defaultBlockState()
                        .setValue(CandleBlock.CANDLES, 3).setValue(CandleBlock.LIT, true));
            }
        }
        for (int dx = -2; dx <= 2; dx++) {
            canvas.set(centre.offset(dx, 5, 0), dx == 0 ? palette.glow() : palette.accent());
        }
        canvas.set(centre.above(4), lantern(palette, true));
    }

    private static BlockState lantern(CitadelPalette palette, boolean hanging) {
        BlockState light = palette.light();
        return light.hasProperty(LanternBlock.HANGING) ? light.setValue(LanternBlock.HANGING, hanging) : light;
    }

    private static BlockState worn(BlockPos pos, BlockPos centre, CitadelPalette palette, double odds) {
        return chance(pos, centre) < odds ? palette.wallWorn() : palette.wall();
    }

    /** A number in [0, 1) fixed by the block and the Citadel it belongs to. */
    static double chance(BlockPos pos, BlockPos centre) {
        long seed = Mth.getSeed(pos) ^ (centre.asLong() * 0x9E3779B97F4A7C15L);
        seed ^= seed >>> 33;
        seed *= 0xff51afd7ed558ccdL;
        seed ^= seed >>> 33;
        return (seed >>> 11) * 0x1.0p-53;
    }
}
