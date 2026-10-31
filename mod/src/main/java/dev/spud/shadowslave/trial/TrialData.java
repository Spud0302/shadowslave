package dev.spud.shadowslave.trial;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * What every player carries between Nightmares, saved with the world:
 * the snapshot taken as they entered the one they are in (so a death, a
 * crash or a logout can always put them back), the Seeds that have judged
 * them, and the Nightmares they have conquered.
 */
public final class TrialData extends SavedData {
    private static final String NAME = "shadowslave_nightmare_trials";

    final Map<UUID, Record> players = new HashMap<>();

    /** Where and how a player stood when they entered a Nightmare. */
    public record Entry(UUID instance, ResourceLocation nightmare, Optional<UUID> seed, CompoundTag snapshot,
                        ResourceKey<Level> dimension, Vec3 pos, float yRot, float xRot, String team) {
        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("instance", instance);
            tag.putString("nightmare", nightmare.toString());
            seed.ifPresent(id -> tag.putUUID("seed", id));
            tag.put("snapshot", snapshot);
            tag.putString("dimension", dimension.location().toString());
            tag.putDouble("x", pos.x);
            tag.putDouble("y", pos.y);
            tag.putDouble("z", pos.z);
            tag.putFloat("y_rot", yRot);
            tag.putFloat("x_rot", xRot);
            tag.putString("team", team);
            return tag;
        }

        static Entry load(CompoundTag tag) {
            return new Entry(tag.getUUID("instance"), ResourceLocation.parse(tag.getString("nightmare")),
                    tag.hasUUID("seed") ? Optional.of(tag.getUUID("seed")) : Optional.empty(),
                    tag.getCompound("snapshot"),
                    ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(tag.getString("dimension"))),
                    new Vec3(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z")),
                    tag.getFloat("y_rot"), tag.getFloat("x_rot"), tag.getString("team"));
        }
    }

    public static final class Record {
        Entry active;
        final Set<UUID> judgedBy = new HashSet<>();
        final Map<Integer, Integer> clears = new LinkedHashMap<>();
        final Map<ResourceLocation, String> bestGrades = new LinkedHashMap<>();
        /** The game time before which the Seed inside a fallen carrier stays quiet. */
        long seedQuietUntil;

        public Optional<Entry> active() {
            return Optional.ofNullable(active);
        }

        public boolean judgedBy(UUID seed) {
            return judgedBy.contains(seed);
        }

        /** How many Nightmares of exactly this Category the player has conquered. */
        public int clears(int category) {
            return clears.getOrDefault(category, 0);
        }

        /** Whether the player has conquered any Nightmare of this Category or higher. */
        public boolean cleared(int category) {
            return clears.keySet().stream().anyMatch(cleared -> cleared >= category);
        }

        public Map<ResourceLocation, String> bestGrades() {
            return Map.copyOf(bestGrades);
        }

        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            if (active != null) {
                tag.put("active", active.save());
            }
            ListTag judged = new ListTag();
            judgedBy.forEach(seed -> judged.add(NbtUtils.createUUID(seed)));
            tag.put("judged_by", judged);
            CompoundTag clearTag = new CompoundTag();
            clears.forEach((category, count) -> clearTag.putInt(Integer.toString(category), count));
            tag.put("clears", clearTag);
            CompoundTag grades = new CompoundTag();
            bestGrades.forEach((id, grade) -> grades.putString(id.toString(), grade));
            tag.put("best_grades", grades);
            if (seedQuietUntil > 0) {
                tag.putLong("seed_quiet_until", seedQuietUntil);
            }
            return tag;
        }

        static Record load(CompoundTag tag) {
            Record record = new Record();
            if (tag.contains("active", Tag.TAG_COMPOUND)) {
                record.active = Entry.load(tag.getCompound("active"));
            }
            for (Tag seed : tag.getList("judged_by", Tag.TAG_INT_ARRAY)) {
                record.judgedBy.add(NbtUtils.loadUUID(seed));
            }
            CompoundTag clearTag = tag.getCompound("clears");
            for (String key : clearTag.getAllKeys()) {
                try {
                    record.clears.put(Integer.parseInt(key), clearTag.getInt(key));
                } catch (NumberFormatException ignored) {
                    // A hand-edited save; skip the bad key.
                }
            }
            record.seedQuietUntil = tag.getLong("seed_quiet_until");
            CompoundTag grades = tag.getCompound("best_grades");
            for (String key : grades.getAllKeys()) {
                ResourceLocation id = ResourceLocation.tryParse(key);
                if (id != null) {
                    record.bestGrades.put(id, grades.getString(key));
                }
            }
            return record;
        }
    }

    public static TrialData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(new Factory<>(TrialData::new, TrialData::load), NAME);
    }

    public Record record(UUID player) {
        return players.computeIfAbsent(player, id -> new Record());
    }

    public Optional<Record> find(UUID player) {
        return Optional.ofNullable(players.get(player));
    }

    void begin(UUID player, Entry entry) {
        record(player).active = entry;
        setDirty();
    }

    void end(UUID player) {
        find(player).ifPresent(record -> record.active = null);
        setDirty();
    }

    void quietSeed(UUID player, long until) {
        record(player).seedQuietUntil = until;
        setDirty();
    }

    /** The game time until which this player's inner Seed stays quiet; 0 if it is not. */
    public long seedQuietUntil(UUID player) {
        return find(player).map(record -> record.seedQuietUntil).orElse(0L);
    }

    void judge(UUID player, UUID seed) {
        record(player).judgedBy.add(seed);
        setDirty();
    }

    void clear(UUID player, int category, ResourceLocation nightmare, String grade) {
        Record record = record(player);
        record.clears.merge(category, 1, Integer::sum);
        record.bestGrades.merge(nightmare, grade, (old, next) -> rankOf(next) >= rankOf(old) ? next : old);
        setDirty();
    }

    /** Forgets everything about a player's Nightmares except one they are inside; for operators. */
    void forget(UUID player) {
        find(player).ifPresent(record -> {
            record.judgedBy.clear();
            record.clears.clear();
            record.bestGrades.clear();
            record.seedQuietUntil = 0;
        });
        setDirty();
    }

    private static int rankOf(String grade) {
        var grades = dev.spud.shadowslave.trial.content.TrialContent.get().settings().grades();
        for (int i = 0; i < grades.size(); i++) {
            if (grades.get(i).name().equals(grade)) {
                return i;
            }
        }
        return -1;
    }

    private static TrialData load(CompoundTag tag, HolderLookup.Provider registries) {
        TrialData data = new TrialData();
        CompoundTag players = tag.getCompound("players");
        for (String key : players.getAllKeys()) {
            try {
                data.players.put(UUID.fromString(key), Record.load(players.getCompound(key)));
            } catch (IllegalArgumentException ignored) {
                // Not a player id; skip it.
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag players = new CompoundTag();
        this.players.forEach((id, record) -> players.put(id.toString(), record.save()));
        tag.put("players", players);
        return tag;
    }
}
