package dev.spud.shadowslave.dreamrealm;

import dev.spud.shadowslave.dreamrealm.StormLanternCoastSitePlan.Piece;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

/**
 * Development executor for the first later-era historical site. It intentionally
 * lives beside, rather than replacing, the fixed Ashen Expanse regression slice.
 */
public final class StormLanternCoastPreviewService {
    private static final BlockPos ORIGIN = new BlockPos(224, 148, 0);
    private static final int RADIUS = 42;

    private StormLanternCoastPreviewService() {
    }

    public static void enter(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        ServerLevel level = server.getLevel(DreamRealmPreviewService.DREAM_REALM_LEVEL);
        if (level == null) throw new IllegalStateException("The bundled Dream Realm dimension is unavailable");

        StormLanternCoastSitePlan.Plan plan = StormLanternCoastSitePlan.drownedBellLater(level.getSeed());
        build(level, plan);
        player.teleportTo(level, ORIGIN.getX() + 0.5, ORIGIN.getY() + 7.0, ORIGIN.getZ() - 32.5,
                Set.of(), 0.0F, 8.0F);
        player.sendSystemMessage(Component.literal("Dream Realm — " + plan.region().displayName())
                .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal(plan.region().arrivalCue()).withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal(plan.site().futureStateCue()).withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.literal("No quest marker is provided: identify the belfry, sea gate, quarry cut and drowned terraces by exploring the coast.")
                .withStyle(ChatFormatting.YELLOW));
    }

    private static void build(ServerLevel level, StormLanternCoastSitePlan.Plan plan) {
        clear(level);
        buildCoast(level);
        for (Piece piece : plan.pieces()) buildPiece(level, piece);
    }

    private static void clear(ServerLevel level) {
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int z = -RADIUS; z <= RADIUS; z++) {
                for (int y = -5; y <= 20; y++) set(level, x, y, z, Blocks.AIR.defaultBlockState());
            }
        }
    }

    private static void buildCoast(ServerLevel level) {
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int z = -RADIUS; z <= RADIUS; z++) {
                int cliff = 5 + Math.floorMod(x * 17 + z * 29, 4);
                boolean sea = z > 10 + Math.floorMod(x * 7, 9);
                if (sea) {
                    for (int y = -4; y <= -1; y++) set(level, x, y, z, Blocks.DEEPSLATE.defaultBlockState());
                    for (int y = 0; y <= 2; y++) set(level, x, y, z, Blocks.WATER.defaultBlockState());
                } else {
                    int surface = Math.max(0, cliff - Math.max(0, z + 4) / 10);
                    for (int y = -4; y < surface; y++) {
                        BlockState stone = y == surface - 1 && Math.floorMod(x * 11 + z * 5, 5) == 0
                                ? Blocks.TUFF.defaultBlockState()
                                : Blocks.DEEPSLATE.defaultBlockState();
                        set(level, x, y, z, stone);
                    }
                }
            }
        }
        // A deliberately readable high path from arrival toward the historical core.
        for (int z = -34; z <= 8; z++) {
            for (int x = -2; x <= 2; x++) set(level, x, 5, z, Blocks.COBBLED_DEEPSLATE.defaultBlockState());
        }
    }

    private static void buildPiece(ServerLevel level, Piece piece) {
        switch (piece.pieceFamily()) {
            case "ruined_belfry" -> buildBelfry(level, piece);
            case "broken_sea_gate" -> buildSeaGate(level, piece);
            case "collapsed_quarry" -> buildQuarry(level, piece);
            case "drowned_terraces" -> buildTerraces(level, piece);
            case "cliff_lantern" -> buildCliffLantern(level, piece);
            case "collapsed_shelter" -> buildShelter(level, piece);
            case "storm_wreckage" -> buildWreckage(level, piece);
            default -> throw new IllegalStateException("Unmapped Storm Lantern Coast piece family " + piece.pieceFamily());
        }
    }

    private static void buildBelfry(ServerLevel level, Piece piece) {
        int brokenSide = piece.variant() % 4;
        for (int y = 0; y <= 11; y++) {
            for (int side = 0; side < 4; side++) {
                if (side == brokenSide && y > 5) continue;
                int[] off = sideOffset(side, 3);
                set(level, piece.x() + off[0], piece.y() + y, piece.z() + off[1],
                        y < 3 ? Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState() : Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
            }
        }
        for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++) {
            if (Math.abs(x) == 3 || Math.abs(z) == 3) set(level, piece.x() + x, piece.y() + 9, piece.z() + z, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
        }
        set(level, piece.x(), piece.y() + 8, piece.z(), Blocks.BELL.defaultBlockState());
    }

    private static void buildSeaGate(ServerLevel level, Piece piece) {
        for (int x = -7; x <= 7; x++) {
            if (Math.abs(x) <= 2 + piece.variant()) continue;
            for (int y = 0; y <= 5; y++) set(level, piece.x() + x, piece.y() + y, piece.z(), Blocks.DEEPSLATE_BRICKS.defaultBlockState());
        }
        for (int x = -3; x <= 3; x++) set(level, piece.x() + x, piece.y(), piece.z(), Blocks.IRON_BARS.defaultBlockState());
        set(level, piece.x() - 5, piece.y() + 6, piece.z(), Blocks.SOUL_LANTERN.defaultBlockState());
        set(level, piece.x() + 5, piece.y() + 6, piece.z(), Blocks.SOUL_LANTERN.defaultBlockState());
    }

    private static void buildQuarry(ServerLevel level, Piece piece) {
        int depth = 7 + piece.variant();
        for (int d = 0; d < depth; d++) {
            int z = piece.z() - d;
            for (int x = -3; x <= 3; x++) {
                set(level, piece.x() + x, piece.y(), z, Blocks.COBBLED_DEEPSLATE.defaultBlockState());
                if (Math.abs(x) == 3) set(level, piece.x() + x, piece.y() + 1, z, Blocks.DEEPSLATE_BRICKS.defaultBlockState());
            }
        }
        for (int x = -3; x <= 3; x++) for (int y = 1; y <= 4; y++) {
            if (Math.floorMod(x + y + piece.variant(), 4) != 0) set(level, piece.x() + x, piece.y() + y, piece.z() - depth, Blocks.GRAVEL.defaultBlockState());
        }
    }

    private static void buildTerraces(ServerLevel level, Piece piece) {
        for (int tier = 0; tier < 4; tier++) {
            int width = 12 - tier * 2;
            int y = piece.y() + tier;
            int z = piece.z() - tier * 3;
            for (int x = -width / 2; x <= width / 2; x++) {
                set(level, piece.x() + x, y, z, Blocks.STONE_BRICKS.defaultBlockState());
                if (Math.floorMod(x + tier + piece.variant(), 5) == 0) set(level, piece.x() + x, y + 1, z, Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
            }
        }
    }

    private static void buildCliffLantern(ServerLevel level, Piece piece) {
        int height = 3 + piece.variant();
        for (int y = 0; y < height; y++) set(level, piece.x(), piece.y() + y, piece.z(), Blocks.POLISHED_BLACKSTONE_BRICK_WALL.defaultBlockState());
        set(level, piece.x(), piece.y() + height, piece.z(), Blocks.SOUL_LANTERN.defaultBlockState());
    }

    private static void buildShelter(ServerLevel level, Piece piece) {
        int missing = piece.variant() % 4;
        for (int x = -4; x <= 4; x++) for (int z = -3; z <= 3; z++) {
            if (Math.abs(x) != 4 && Math.abs(z) != 3) continue;
            int side = Math.abs(x) == 4 ? (x < 0 ? 0 : 1) : (z < 0 ? 2 : 3);
            if (side == missing && Math.floorMod(x + z, 2) == 0) continue;
            set(level, piece.x() + x, piece.y(), piece.z() + z, Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
            set(level, piece.x() + x, piece.y() + 1, piece.z() + z, Blocks.STONE_BRICKS.defaultBlockState());
        }
    }

    private static void buildWreckage(ServerLevel level, Piece piece) {
        for (int i = 0; i < 5 + piece.variant(); i++) {
            int dx = Math.floorMod(i * 5 + piece.variant(), 9) - 4;
            int dz = Math.floorMod(i * 7 + piece.variant() * 2, 7) - 3;
            set(level, piece.x() + dx, piece.y(), piece.z() + dz,
                    i % 2 == 0 ? Blocks.DARK_OAK_LOG.defaultBlockState() : Blocks.CHAIN.defaultBlockState());
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

    private static void set(ServerLevel level, int x, int localY, int z, BlockState state) {
        level.setBlock(ORIGIN.offset(x, localY, z), state, 3);
    }
}
