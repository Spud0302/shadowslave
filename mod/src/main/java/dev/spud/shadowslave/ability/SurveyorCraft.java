package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.dreamrealm.DreamRealm;
import dev.spud.shadowslave.dreamrealm.seed.SeedData;
import dev.spud.shadowslave.fieldguide.FieldGuideData;
import dev.spud.shadowslave.fieldguide.FieldGuideService;
import dev.spud.shadowslave.gate.GateData;
import dev.spud.shadowslave.gate.GateEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Surveyor (Field Guide): Wide Eye maps a wider ring, Survey Pulse charts the
 * land around in one go and outlines the structures and cave hollows in it,
 * Triangulate pins the nearest Seed or Gate, Fault Line cracks the ground, and
 * Uncharted Dread makes every ability cost double off the map.
 */
public final class SurveyorCraft {
    /** Extra chunks of map a Surveyor's guide fills around them. */
    public static final int WIDE_EYE = 2;
    /** Survey Pulse's reach, in chunks (48 blocks). */
    static final int PULSE_CHUNKS = 3;
    static final int PULSE_BLOCKS = PULSE_CHUNKS * 16;
    static final int OUTLINE_TICKS = 600;
    /** How far below the surface a pulse looks for hollows. */
    static final int HOLLOW_DEPTH = 48;
    static final int HOLLOW_STEP = 4;
    static final int MAX_OUTLINE_POINTS = 360;

    /** What a pulse found: chunks charted, structures and cave hollows outlined. */
    public record Survey(int charted, List<BoundingBox> structures, List<BlockPos> hollows) {
    }

    record Outline(ResourceKey<Level> level, List<BoundingBox> structures, List<BlockPos> hollows, long until) {
    }

    private static final Map<UUID, Outline> OUTLINES = new ConcurrentHashMap<>();

    private SurveyorCraft() {
    }

    public static boolean isSurveyor(ServerPlayer player) {
        return KitRules.hasInnate(player, AspectKitCatalog.SURVEYOR.innateId());
    }

    // ---- Uncharted Dread -------------------------------------------------------------------------------------------

    static boolean dreads(ServerPlayer player) {
        return KitRules.hasFlaw(player, AspectKitCatalog.SURVEYOR.flawEffectId());
    }

    /** Whether the ground under the player is on their guide's map. */
    public static boolean charted(ServerPlayer player) {
        return FieldGuideService.get(player).explored(FieldGuideService.dimension(player.level()), player.chunkPosition().toLong());
    }

    /** Uncharted Dread: off the map, every ability costs double. */
    static int costFor(ServerPlayer player, int cost) {
        return dreads(player) && !charted(player) ? cost * 2 : cost;
    }

    // ---- Survey Pulse ----------------------------------------------------------------------------------------------

    /** Charts {@link #PULSE_CHUNKS} chunks around the player and outlines what is in them for the player alone. */
    static Survey pulse(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        int charted = FieldGuideService.explore(player, true, PULSE_CHUNKS);
        List<BoundingBox> structures = structures(level, player.chunkPosition(), PULSE_CHUNKS);
        List<BlockPos> hollows = hollows(level, player.blockPosition());
        OUTLINES.put(player.getUUID(), new Outline(level.dimension(), structures, hollows, player.server.getTickCount() + OUTLINE_TICKS));
        show(player, OUTLINES.get(player.getUUID()));
        return new Survey(charted, structures, hollows);
    }

    /** Every structure with a piece within {@code radius} chunks of {@code centre}, each once. */
    static List<BoundingBox> structures(ServerLevel level, ChunkPos centre, int radius) {
        Map<BoundingBox, Boolean> found = new LinkedHashMap<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (!level.hasChunk(centre.x + dx, centre.z + dz)) {
                    continue;
                }
                ChunkAccess chunk = level.getChunk(centre.x + dx, centre.z + dz);
                for (Map.Entry<Structure, it.unimi.dsi.fastutil.longs.LongSet> refs : chunk.getAllReferences().entrySet()) {
                    for (long ref : refs.getValue()) {
                        ChunkPos at = new ChunkPos(ref);
                        StructureStart start = level.getChunk(at.x, at.z, ChunkStatus.STRUCTURE_STARTS).getStartForStructure(refs.getKey());
                        if (start != null && start.isValid()) {
                            found.putIfAbsent(start.getBoundingBox(), true);
                        }
                    }
                }
            }
        }
        return new ArrayList<>(found.keySet());
    }

    /** Pockets of air under the surface within the pulse's reach: the top of each cave the pulse finds below a column. */
    static List<BlockPos> hollows(ServerLevel level, BlockPos centre) {
        List<BlockPos> found = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -PULSE_BLOCKS; dx <= PULSE_BLOCKS; dx += HOLLOW_STEP) {
            for (int dz = -PULSE_BLOCKS; dz <= PULSE_BLOCKS; dz += HOLLOW_STEP) {
                if (dx * dx + dz * dz > PULSE_BLOCKS * PULSE_BLOCKS) {
                    continue;
                }
                int x = centre.getX() + dx;
                int z = centre.getZ() + dz;
                if (!level.hasChunkAt(cursor.set(x, centre.getY(), z))) {
                    continue;
                }
                int surface = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
                int bottom = Math.max(level.getMinBuildHeight() + 1, surface - HOLLOW_DEPTH);
                boolean roofed = false;
                for (int y = surface - 3; y >= bottom; y--) {
                    boolean air = level.getBlockState(cursor.set(x, y, z)).isAir();
                    if (!air) {
                        roofed = true;
                    } else if (roofed) {
                        found.add(cursor.immutable());
                        break;
                    }
                }
            }
        }
        return found;
    }

    /** Draws an outline for its surveyor alone: structure edges in end-rod light, cave hollows in soul fire. */
    static void show(ServerPlayer player, Outline outline) {
        ServerLevel level = player.serverLevel();
        for (BoundingBox box : outline.structures()) {
            for (BlockPos point : edges(box)) {
                level.sendParticles(player, ParticleTypes.END_ROD, true, point.getX() + 0.5, point.getY() + 0.5, point.getZ() + 0.5,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
        }
        for (BlockPos hollow : outline.hollows()) {
            level.sendParticles(player, ParticleTypes.SOUL_FIRE_FLAME, true, hollow.getX() + 0.5, hollow.getY() + 0.5, hollow.getZ() + 0.5,
                    2, 0.2, 0.2, 0.2, 0.0);
        }
    }

    /** Points along the twelve edges of a box, spaced so a big one stays within {@link #MAX_OUTLINE_POINTS}. */
    static List<BlockPos> edges(BoundingBox box) {
        int perimeter = 4 * (box.getXSpan() + box.getYSpan() + box.getZSpan());
        int step = Math.max(1, (int) Math.ceil(perimeter / (double) MAX_OUTLINE_POINTS));
        List<BlockPos> points = new ArrayList<>();
        int[] xs = {box.minX(), box.maxX()};
        int[] ys = {box.minY(), box.maxY()};
        int[] zs = {box.minZ(), box.maxZ()};
        for (int y : ys) {
            for (int z : zs) {
                for (int x = box.minX(); x <= box.maxX(); x += step) {
                    points.add(new BlockPos(x, y, z));
                }
            }
            for (int x : xs) {
                for (int z = box.minZ(); z <= box.maxZ(); z += step) {
                    points.add(new BlockPos(x, y, z));
                }
            }
        }
        for (int x : xs) {
            for (int z : zs) {
                for (int y = box.minY(); y <= box.maxY(); y += step) {
                    points.add(new BlockPos(x, y, z));
                }
            }
        }
        return points;
    }

    static Optional<Outline> outline(ServerPlayer player) {
        Outline outline = OUTLINES.get(player.getUUID());
        return outline == null || outline.until() <= player.server.getTickCount() ? Optional.empty() : Optional.of(outline);
    }

    // ---- Triangulate -----------------------------------------------------------------------------------------------

    /** Where Triangulate points: a Seed or Gate, its spot, and its name. */
    public record Fix(FieldGuideData.PinKind kind, BlockPos pos, String label, UUID ref) {
    }

    /** The nearest open Gate in the player's world or, in the Dream Realm, the nearest Seed if it is nearer. */
    static Optional<Fix> triangulate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos from = player.blockPosition();
        Fix best = null;
        double bestDistance = Double.MAX_VALUE;
        for (GateEvent gate : GateData.get(level).gates()) {
            double distance = gate.center().distSqr(from);
            if (gate.phase() != GateEvent.Phase.CLOSED && distance < bestDistance) {
                best = new Fix(FieldGuideData.PinKind.GATE, gate.center(), "Category " + gate.category() + " Gate", gate.id());
                bestDistance = distance;
            }
        }
        if (DreamRealm.is(level)) {
            for (SeedData.Seed seed : SeedData.get(level).seeds()) {
                double distance = seed.pos().distSqr(from);
                if (distance < bestDistance) {
                    best = new Fix(FieldGuideData.PinKind.SEED, seed.pos(), "Nightmare Seed", seed.id());
                    bestDistance = distance;
                }
            }
        }
        return Optional.ofNullable(best);
    }

    /** Pins a fix in the player's guide. Returns whether it was new to the guide. */
    static boolean pin(ServerPlayer player, Fix fix) {
        return FieldGuideService.pin(player, new FieldGuideData.Pin(fix.kind(), FieldGuideService.dimension(player.level()),
                fix.pos().getX(), fix.pos().getZ(), fix.label(), Optional.of(fix.ref())));
    }

    // ---- ticks and cleanup -----------------------------------------------------------------------------------------

    public static void onServerTick(ServerTickEvent.Post event) {
        if (OUTLINES.isEmpty()) {
            return;
        }
        MinecraftServer server = event.getServer();
        long now = server.getTickCount();
        if (now % 20 != 0) {
            return;
        }
        for (Map.Entry<UUID, Outline> entry : List.copyOf(OUTLINES.entrySet())) {
            ServerPlayer player = KitRules.online(server, entry.getKey());
            Outline outline = entry.getValue();
            if (player == null || outline.until() <= now) {
                OUTLINES.remove(entry.getKey());
            } else if (player.level().dimension() == outline.level()) {
                show(player, outline);
            }
        }
    }

    static void forget(UUID player) {
        OUTLINES.remove(player);
    }

    public static void onServerStopping(ServerStoppingEvent event) {
        OUTLINES.clear();
    }
}
