package dev.spud.shadowslave.dreamrealm;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.dreamrealm.DreamRealmVerticalSliceDefinition.Placement;
import dev.spud.shadowslave.world.entity.AshBurrowerEntity;
import dev.spud.shadowslave.world.entity.NightmareCreatureEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.Set;

/** Development-only physical execution adapter for one Java-owned Dream Realm region. */
public final class DreamRealmPreviewService {
    public static final ResourceKey<Level> DREAM_REALM_LEVEL = ResourceKey.create(
            Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "dream_realm"));
    private static final BlockPos ORIGIN = new BlockPos(0, 160, 0);
    private static final int RADIUS = 24;

    private DreamRealmPreviewService() {}

    public static void enter(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        ServerLevel level = server.getLevel(DREAM_REALM_LEVEL);
        if (level == null) throw new IllegalStateException("The bundled Dream Realm preview dimension is unavailable");

        var integration = DreamRealmWorldStoryIntegration.cinderRest();
        build(level, integration.slice());
        DreamRealmStoryNpcRuntime.ensureAshenWatchCaptain(level,
                ORIGIN.offset(integration.x(), integration.y(), integration.z()));
        var encounter = DreamRealmCreatureEncounterBinding.ashenExpanseAshBurrower();
        ensureAshBurrower(level, encounter);
        player.teleportTo(level, 0.5, ORIGIN.getY() + 2.0, 0.5, Set.of(), 0.0F, 0.0F);
        player.sendSystemMessage(Component.literal("Dream Realm preview — " + integration.slice().region().displayName())
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal(integration.slice().region().arrivalCue()).withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal("Cinder Rest — " + integration.watchCaptain().factionName())
                .withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.literal("The Watch Captain is inside the refuge; right-click them for Java-owned settlement information.")
                .withStyle(ChatFormatting.YELLOW));
        player.sendSystemMessage(Component.literal(encounter.creature().presentationCue()).withStyle(ChatFormatting.RED));
    }

    public static void exit(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        ServerLevel overworld = server.overworld();
        BlockPos spawn = overworld.getSharedSpawnPos();
        player.teleportTo(overworld, spawn.getX() + 0.5, spawn.getY() + 1.0, spawn.getZ() + 0.5,
                Set.of(), player.getYRot(), player.getXRot());
        player.sendSystemMessage(Component.literal("Left the Dream Realm development slice.").withStyle(ChatFormatting.GRAY));
    }

    public static boolean isInside(ServerPlayer player) {
        return player.serverLevel().dimension().equals(DREAM_REALM_LEVEL);
    }

    private static void ensureAshBurrower(ServerLevel level, DreamRealmCreatureEncounterBinding.Encounter encounter) {
        AABB sliceBounds = new AABB(
                ORIGIN.getX() - RADIUS, ORIGIN.getY(), ORIGIN.getZ() - RADIUS,
                ORIGIN.getX() + RADIUS + 1, ORIGIN.getY() + 19, ORIGIN.getZ() + RADIUS + 1);
        if (!level.getEntitiesOfClass(AshBurrowerEntity.class, sliceBounds).isEmpty()) return;

        AshBurrowerEntity creature = NightmareCreatureEntities.ASH_BURROWER.get().create(level);
        if (creature == null) throw new IllegalStateException("Could not create Ash Burrower executor");
        BlockPos spawn = ORIGIN.offset(encounter.x(), encounter.y(), encounter.z());
        creature.moveTo(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, 0.0F, 0.0F);
        creature.setPersistenceRequired();
        if (!level.addFreshEntity(creature)) throw new IllegalStateException("Could not place Ash Burrower executor");
    }

    private static void build(ServerLevel level, DreamRealmVerticalSliceDefinition.Slice slice) {
        clearVolume(level);
        buildAshField(level);
        buildShelter(level);
        buildCinderRestLanternRing(level);
        for (Placement placement : slice.landmarks()) buildLandmark(level, placement);
        for (Placement placement : slice.resources()) buildResourceHook(level, placement);
    }

    private static void clearVolume(ServerLevel level) {
        for (int x = -RADIUS; x <= RADIUS; x++) for (int z = -RADIUS; z <= RADIUS; z++) for (int y = 0; y <= 18; y++)
            set(level, x, y, z, Blocks.AIR.defaultBlockState());
    }

    private static void buildAshField(ServerLevel level) {
        for (int x = -RADIUS; x <= RADIUS; x++) for (int z = -RADIUS; z <= RADIUS; z++) {
            BlockState surface = Math.floorMod(x * 31 + z * 17, 11) < 3 ? Blocks.GRAVEL.defaultBlockState() : Blocks.TUFF.defaultBlockState();
            set(level, x, -1, z, Blocks.DEEPSLATE.defaultBlockState());
            set(level, x, 0, z, surface);
        }
        for (int x = -4; x <= 4; x++) for (int z = -3; z <= 3; z++) set(level, x, 1, z, Blocks.SMOOTH_STONE.defaultBlockState());
    }

    private static void buildShelter(ServerLevel level) {
        for (int y = 1; y <= 4; y++) {
            set(level, -4, y, -3, Blocks.STONE_BRICKS.defaultBlockState());
            set(level, 4, y, -3, Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
        }
        for (int x = -4; x <= 4; x++) set(level, x, 4, -3, Blocks.STONE_BRICKS.defaultBlockState());
        set(level, 0, 3, -2, Blocks.SOUL_LANTERN.defaultBlockState());
    }

    private static void buildCinderRestLanternRing(ServerLevel level) {
        for (var lamp : CinderRestLanternRingBinding.cinderRest().lamps()) {
            set(level, lamp.x(), lamp.y() - 1, lamp.z(), Blocks.POLISHED_BLACKSTONE_BRICK_WALL.defaultBlockState());
            set(level, lamp.x(), lamp.y(), lamp.z(), Blocks.SOUL_LANTERN.defaultBlockState());
        }
    }

    private static void buildLandmark(ServerLevel level, Placement placement) {
        switch (placement.hookId()) {
            case "buried_watchtower" -> buildWatchtower(level, placement);
            case "black_obelisk" -> buildObelisk(level, placement);
            case "shattered_causeway" -> buildCauseway(level, placement);
            default -> throw new IllegalStateException("Unmapped Ashen Expanse landmark hook " + placement.hookId());
        }
    }

    private static void buildWatchtower(ServerLevel level, Placement placement) {
        for (int y = 1; y <= 8; y++) {
            for (int dx = -2; dx <= 2; dx++) if (Math.abs(dx) == 2) {
                set(level, placement.x() + dx, y, placement.z() - 2, Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
                set(level, placement.x() + dx, y, placement.z() + 2, Blocks.STONE_BRICKS.defaultBlockState());
            }
            for (int dz = -1; dz <= 1; dz++) {
                set(level, placement.x() - 2, y, placement.z() + dz, Blocks.STONE_BRICKS.defaultBlockState());
                set(level, placement.x() + 2, y, placement.z() + dz, Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
            }
        }
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++)
            set(level, placement.x() + dx, 8, placement.z() + dz, Blocks.POLISHED_BLACKSTONE.defaultBlockState());
    }

    private static void buildObelisk(ServerLevel level, Placement placement) {
        for (int y = 1; y <= 11; y++) {
            set(level, placement.x(), y, placement.z(), Blocks.OBSIDIAN.defaultBlockState());
            if (y <= 3) set(level, placement.x() + 1, y, placement.z(), Blocks.CRYING_OBSIDIAN.defaultBlockState());
        }
    }

    private static void buildCauseway(ServerLevel level, Placement placement) {
        for (int x = -12; x <= 12; x++) if (Math.floorMod(x, 5) != 0) {
            set(level, placement.x() + x, 1, placement.z(), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
            set(level, placement.x() + x, 1, placement.z() + 1, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
        }
    }

    private static void buildResourceHook(ServerLevel level, Placement placement) {
        BlockState state = switch (placement.hookId()) {
            case "bone_char" -> Blocks.BONE_BLOCK.defaultBlockState();
            case "ruin_metal" -> Blocks.RAW_IRON_BLOCK.defaultBlockState();
            case "dry_fungus" -> Blocks.BROWN_MUSHROOM_BLOCK.defaultBlockState();
            default -> throw new IllegalStateException("Unmapped Ashen Expanse resource hook " + placement.hookId());
        };
        set(level, placement.x(), 1, placement.z(), state);
        set(level, placement.x(), 2, placement.z(), state);
        set(level, placement.x() + 1, 1, placement.z(), state);
    }

    private static void set(ServerLevel level, int x, int localY, int z, BlockState state) {
        level.setBlock(ORIGIN.offset(x, localY, z), state, 3);
    }
}
