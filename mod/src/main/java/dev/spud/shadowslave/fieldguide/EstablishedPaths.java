package dev.spud.shadowslave.fieldguide;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

/**
 * How often every path cell on the server has been walked, by everyone with
 * Trace on (DESIGN, the Shared Field Guide plan: "a route walked many times,
 * or by many players on a server, becomes an established path"). Each
 * player's share of a cell is capped by {@code walks_per_player}, so the
 * counts here are the sum of those capped shares. Saved with the Overworld.
 */
public final class EstablishedPaths extends SavedData {
    private static final String NAME = "shadowslave_field_guide_paths";
    private final Map<String, Map<Long, Integer>> walks = new HashMap<>();

    public static EstablishedPaths get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(new Factory<>(EstablishedPaths::new, EstablishedPaths::load), NAME);
    }

    public int walks(String dimension, long cell) {
        Map<Long, Integer> cells = walks.get(dimension);
        return cells == null ? 0 : cells.getOrDefault(cell, 0);
    }

    /** Adds one walk to a cell and returns the new count. */
    public int add(String dimension, long cell) {
        int count = walks.computeIfAbsent(dimension, key -> new HashMap<>()).merge(cell, 1, Integer::sum);
        setDirty();
        return count;
    }

    private static EstablishedPaths load(CompoundTag tag, HolderLookup.Provider registries) {
        EstablishedPaths data = new EstablishedPaths();
        CompoundTag dimensions = tag.getCompound("walks");
        for (String dimension : dimensions.getAllKeys()) {
            if (dimensions.getTagType(dimension) != Tag.TAG_LONG_ARRAY) {
                continue;
            }
            long[] values = dimensions.getLongArray(dimension);
            Map<Long, Integer> cells = new HashMap<>();
            for (int i = 0; i + 1 < values.length; i += 2) {
                cells.put(values[i], (int) values[i + 1]);
            }
            data.walks.put(dimension, cells);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag dimensions = new CompoundTag();
        walks.forEach((dimension, cells) -> {
            long[] values = new long[cells.size() * 2];
            int i = 0;
            for (Map.Entry<Long, Integer> entry : cells.entrySet()) {
                values[i++] = entry.getKey();
                values[i++] = entry.getValue();
            }
            dimensions.put(dimension, new LongArrayTag(values));
        });
        tag.put("walks", dimensions);
        return tag;
    }
}
