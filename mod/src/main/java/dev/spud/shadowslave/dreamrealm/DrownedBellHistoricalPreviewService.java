package dev.spud.shadowslave.dreamrealm;

import dev.spud.shadowslave.dreamrealm.DrownedBellHistoricalSitePlan.Piece;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

/** Development executor for the intact historical Drowned Bell settlement. */
public final class DrownedBellHistoricalPreviewService {
    private static final BlockPos ORIGIN = new BlockPos(448, 148, 0);
    private static final int RADIUS = 42;

    private DrownedBellHistoricalPreviewService() {}

    public static void enter(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        ServerLevel level = server.getLevel(DreamRealmPreviewService.DREAM_REALM_LEVEL);
        if (level == null) throw new IllegalStateException("The bundled Dream Realm dimension is unavailable");

        DrownedBellHistoricalSitePlan.Plan plan = DrownedBellHistoricalSitePlan.drownedBell();
        build(level, plan);
        player.teleportTo(level, ORIGIN.getX() + 0.5, ORIGIN.getY() + 7.0, ORIGIN.getZ() - 32.5,
                Set.of(), 0.0F, 8.0F);
        player.sendSystemMessage(Component.literal("Nightmare historical site — " + plan.scenario().displayName())
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal(plan.scenario().premise()).withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal("The Bell Tower, Sea Gate, quarry approach and lower terraces occupy the same local geography as their later Storm Lantern Coast ruins.")
                .withStyle(ChatFormatting.GOLD));
    }

    private static void build(ServerLevel level, DrownedBellHistoricalSitePlan.Plan plan) {
        clear(level);
        buildHistoricalCoast(level);
        for (Piece piece : plan.pieces()) buildPiece(level, piece);
    }

    private static void clear(ServerLevel level) {
        for (int x = -RADIUS; x <= RADIUS; x++) for (int z = -RADIUS; z <= RADIUS; z++) for (int y = -5; y <= 20; y++)
            set(level, x, y, z, Blocks.AIR.defaultBlockState());
    }

    private static void buildHistoricalCoast(ServerLevel level) {
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int z = -RADIUS; z <= RADIUS; z++) {
                boolean sea = z > 14 + Math.floorMod(x * 5, 7);
                if (sea) {
                    for (int y = -4; y <= -1; y++) set(level, x, y, z, Blocks.STONE.defaultBlockState());
                    for (int y = 0; y <= 2; y++) set(level, x, y, z, Blocks.WATER.defaultBlockState());
                } else {
                    int surface = Math.max(0, 6 - Math.max(0, z + 4) / 11);
                    for (int y = -4; y < surface; y++) set(level, x, y, z,
                            y == surface - 1 ? Blocks.STONE.defaultBlockState() : Blocks.DEEPSLATE.defaultBlockState());
                }
            }
        }
        for (int z = -34; z <= 10; z++) for (int x = -2; x <= 2; x++)
            set(level, x, 5, z, Blocks.STONE_BRICKS.defaultBlockState());
    }

    private static void buildPiece(ServerLevel level, Piece piece) {
        switch (piece.pieceFamily()) {
            case "intact_bell_tower" -> buildBellTower(level, piece);
            case "working_sea_gate" -> buildSeaGate(level, piece);
            case "open_quarry_tunnels" -> buildQuarry(level, piece);
            case "inhabited_harbour_terraces" -> buildTerraces(level, piece);
            default -> throw new IllegalStateException("Unmapped historical Drowned Bell piece " + piece.pieceFamily());
        }
    }

    private static void buildBellTower(ServerLevel level, Piece piece) {
        for (int y = 0; y <= 12; y++) {
            for (int side = 0; side < 4; side++) {
                int[] off = sideOffset(side, 3);
                set(level, piece.x() + off[0], piece.y() + y, piece.z() + off[1], Blocks.STONE_BRICKS.defaultBlockState());
            }
        }
        for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++) {
            if (Math.abs(x) == 3 || Math.abs(z) == 3) set(level, piece.x() + x, piece.y() + 10, piece.z() + z, Blocks.STONE_BRICKS.defaultBlockState());
        }
        set(level, piece.x(), piece.y() + 9, piece.z(), Blocks.BELL.defaultBlockState());
        set(level, piece.x(), piece.y() + 7, piece.z(), Blocks.LANTERN.defaultBlockState());
    }

    private static void buildSeaGate(ServerLevel level, Piece piece) {
        for (int x = -7; x <= 7; x++) for (int y = 0; y <= 6; y++) {
            if (Math.abs(x) <= 2 && y <= 4) continue;
            set(level, piece.x() + x, piece.y() + y, piece.z(), Blocks.STONE_BRICKS.defaultBlockState());
        }
        for (int x = -2; x <= 2; x++) for (int y = 0; y <= 4; y++)
            set(level, piece.x() + x, piece.y() + y, piece.z(), Blocks.IRON_BARS.defaultBlockState());
        set(level, piece.x() - 5, piece.y() + 7, piece.z(), Blocks.LANTERN.defaultBlockState());
        set(level, piece.x() + 5, piece.y() + 7, piece.z(), Blocks.LANTERN.defaultBlockState());
    }

    private static void buildQuarry(ServerLevel level, Piece piece) {
        for (int d = 0; d < 10; d++) {
            int z = piece.z() - d;
            for (int x = -3; x <= 3; x++) {
                set(level, piece.x() + x, piece.y(), z, Blocks.STONE.defaultBlockState());
                if (Math.abs(x) == 3) for (int y = 1; y <= 3; y++)
                    set(level, piece.x() + x, piece.y() + y, z, Blocks.STONE_BRICKS.defaultBlockState());
            }
        }
        set(level, piece.x() - 2, piece.y() + 2, piece.z() - 4, Blocks.LANTERN.defaultBlockState());
        set(level, piece.x() + 2, piece.y() + 2, piece.z() - 8, Blocks.LANTERN.defaultBlockState());
    }

    private static void buildTerraces(ServerLevel level, Piece piece) {
        for (int tier = 0; tier < 4; tier++) {
            int width = 12 - tier * 2;
            int y = piece.y() + tier;
            int z = piece.z() - tier * 3;
            for (int x = -width / 2; x <= width / 2; x++) set(level, piece.x() + x, y, z, Blocks.STONE_BRICKS.defaultBlockState());
            for (int house = -1; house <= 1; house += 2) {
                int hx = piece.x() + house * Math.max(2, width / 4);
                for (int dy = 1; dy <= 3; dy++) {
                    set(level, hx - 1, y + dy, z, Blocks.OAK_PLANKS.defaultBlockState());
                    set(level, hx + 1, y + dy, z, Blocks.OAK_PLANKS.defaultBlockState());
                }
                set(level, hx, y + 3, z, Blocks.OAK_PLANKS.defaultBlockState());
                set(level, hx, y + 2, z, Blocks.LANTERN.defaultBlockState());
            }
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
