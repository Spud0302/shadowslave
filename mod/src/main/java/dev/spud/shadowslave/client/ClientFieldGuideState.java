package dev.spud.shadowslave.client;

import dev.spud.shadowslave.fieldguide.FieldGuideData;
import dev.spud.shadowslave.network.payload.FieldGuideMapPayload;
import dev.spud.shadowslave.network.payload.FieldGuidePathPayload;
import dev.spud.shadowslave.network.payload.FieldGuideStatusPayload;
import net.minecraft.world.level.ChunkPos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Client-only cache of the Field Guide: the latest reading, the explored map and traced paths of each dimension, and which layers are shown. */
public final class ClientFieldGuideState {
    public enum Layer {
        PINS("Pins"), GATES("Gates"), PATHS("Paths"), DANGER("Danger"), CREATURES("Creatures"), THREATS("Threats");

        final String label;

        Layer(String label) {
            this.label = label;
        }
    }

    private static FieldGuideStatusPayload status = FieldGuideStatusPayload.NONE;
    private static final Map<String, Map<Long, byte[]>> MAPS = new HashMap<>();
    /** Per dimension: the tiles (see {@link FieldGuideMapRenderer#TILE_CHUNKS}) that hold at least one explored chunk. */
    private static final Map<String, Set<Long>> TILES = new HashMap<>();
    private static final Set<Layer> HIDDEN = new HashSet<>();
    /** Per dimension: path cell to how it shows ({@link FieldGuidePathPayload#FAINT} or {@link FieldGuidePathPayload#ESTABLISHED}). */
    private static final Map<String, Map<Long, Byte>> PATHS = new HashMap<>();
    private static int pathCell = 4;

    private ClientFieldGuideState() {
    }

    public static void update(FieldGuideStatusPayload payload) {
        status = payload;
    }

    public static FieldGuideStatusPayload status() {
        return status;
    }

    public static void applyMap(FieldGuideMapPayload payload) {
        if (payload.reset()) {
            MAPS.remove(payload.dimension());
            TILES.remove(payload.dimension());
            FieldGuideMapRenderer.forget(payload.dimension());
        }
        Map<Long, byte[]> chunks = MAPS.computeIfAbsent(payload.dimension(), key -> new HashMap<>());
        Set<Long> tiles = TILES.computeIfAbsent(payload.dimension(), key -> new HashSet<>());
        int size = FieldGuideData.CHUNK_BYTES;
        for (int i = 0; i < payload.chunks().length; i++) {
            long key = payload.chunks()[i];
            byte[] colours = new byte[size];
            System.arraycopy(payload.colours(), i * size, colours, 0, size);
            chunks.put(key, colours);
            long tile = FieldGuideMapRenderer.tileOf(ChunkPos.getX(key), ChunkPos.getZ(key));
            tiles.add(tile);
            FieldGuideMapRenderer.markDirty(payload.dimension(), tile);
        }
    }

    public static void applyPaths(FieldGuidePathPayload payload) {
        if (payload.reset()) {
            PATHS.remove(payload.dimension());
        }
        pathCell = Math.max(1, payload.cell());
        Map<Long, Byte> cells = PATHS.computeIfAbsent(payload.dimension(), key -> new HashMap<>());
        for (int i = 0; i < payload.cells().length; i++) {
            cells.put(payload.cells()[i], payload.strength()[i]);
        }
    }

    public static Map<Long, Byte> paths(String dimension) {
        return PATHS.getOrDefault(dimension, Map.of());
    }

    /** Width in blocks of one path cell. */
    public static int pathCell() {
        return pathCell;
    }

    public static Map<Long, byte[]> chunks(String dimension) {
        return MAPS.getOrDefault(dimension, Map.of());
    }

    public static boolean hasTile(String dimension, long tile) {
        Set<Long> tiles = TILES.get(dimension);
        return tiles != null && tiles.contains(tile);
    }

    public static boolean explored(String dimension, int chunkX, int chunkZ) {
        return chunks(dimension).containsKey(ChunkPos.asLong(chunkX, chunkZ));
    }

    public static boolean shown(Layer layer) {
        return !HIDDEN.contains(layer);
    }

    public static void toggle(Layer layer) {
        if (!HIDDEN.remove(layer)) {
            HIDDEN.add(layer);
        }
    }

    public static void clear() {
        status = FieldGuideStatusPayload.NONE;
        MAPS.clear();
        TILES.clear();
        PATHS.clear();
        FieldGuideMapRenderer.forgetAll();
    }
}
