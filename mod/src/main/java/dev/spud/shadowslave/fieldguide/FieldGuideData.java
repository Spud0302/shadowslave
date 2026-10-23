package dev.spud.shadowslave.fieldguide;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.LongStream;

/**
 * What one player's Field Guide knows: creature pages, the explored map of
 * each dimension, logged places and pins, and the danger marks behind the
 * ratings. It belongs to the player, not the book, so a lost guide can be
 * crafted again without losing anything (DESIGN). Mutable; the server
 * changes it in place and sends the client what changed.
 */
public final class FieldGuideData {
    /** Map colours sampled per chunk side; each chunk keeps {@code SAMPLES * SAMPLES} packed vanilla map colours. */
    public static final int SAMPLES = 4;
    public static final int CHUNK_BYTES = SAMPLES * SAMPLES;
    public static final int MAX_PINS = 128;

    public enum PinKind implements StringRepresentable {
        PLACE("place"), WARNING("warning"), GATE("gate"), SEED("seed"), ANCHOR("anchor");

        public static final Codec<PinKind> CODEC = StringRepresentable.fromEnum(PinKind::values);
        private final String name;

        PinKind(String name) {
            this.name = name;
        }

        /** Whether the player placed it themselves (and so may remove it). */
        public boolean named() {
            return this == PLACE || this == WARNING;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public record Pin(PinKind kind, String dimension, int x, int z, String label, Optional<UUID> ref) {
        static final Codec<Pin> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                PinKind.CODEC.fieldOf("kind").forGetter(Pin::kind),
                Codec.STRING.fieldOf("dim").forGetter(Pin::dimension),
                Codec.INT.fieldOf("x").forGetter(Pin::x),
                Codec.INT.fieldOf("z").forGetter(Pin::z),
                Codec.STRING.optionalFieldOf("label", "").forGetter(Pin::label),
                UUIDUtil.CODEC.optionalFieldOf("ref").forGetter(Pin::ref)
        ).apply(instance, Pin::new));
    }

    public static final class Creature {
        int sightings;
        int kills;
        int ranksSeen;
        long lastSighting = -1;
        String lastDimension = "";
        int lastX;
        int lastZ;

        public int sightings() {
            return sightings;
        }

        public int kills() {
            return kills;
        }

        /** Bit {@code n} is set once the creature has been met at rank ordinal {@code n}. */
        public int ranksSeen() {
            return ranksSeen;
        }

        public Optional<String> lastDimension() {
            return lastDimension.isEmpty() ? Optional.empty() : Optional.of(lastDimension);
        }

        public int lastX() {
            return lastX;
        }

        public int lastZ() {
            return lastZ;
        }

        void meet(int rank, String dimension, int x, int z) {
            ranksSeen |= 1 << Math.max(0, Math.min(rank, 30));
            lastDimension = dimension;
            lastX = x;
            lastZ = z;
        }

        private record Saved(int sightings, int kills, int ranks, long last, String dim, int x, int z) {
            static final Codec<Saved> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.optionalFieldOf("sightings", 0).forGetter(Saved::sightings),
                    Codec.INT.optionalFieldOf("kills", 0).forGetter(Saved::kills),
                    Codec.INT.optionalFieldOf("ranks", 0).forGetter(Saved::ranks),
                    Codec.LONG.optionalFieldOf("last", -1L).forGetter(Saved::last),
                    Codec.STRING.optionalFieldOf("dim", "").forGetter(Saved::dim),
                    Codec.INT.optionalFieldOf("x", 0).forGetter(Saved::x),
                    Codec.INT.optionalFieldOf("z", 0).forGetter(Saved::z)
            ).apply(instance, Saved::new));
        }

        static final Codec<Creature> CODEC = Saved.CODEC.xmap(saved -> {
            Creature creature = new Creature();
            creature.sightings = saved.sightings;
            creature.kills = saved.kills;
            creature.ranksSeen = saved.ranks;
            creature.lastSighting = saved.last;
            creature.lastDimension = saved.dim;
            creature.lastX = saved.x;
            creature.lastZ = saved.z;
            return creature;
        }, creature -> new Saved(creature.sightings, creature.kills, creature.ranksSeen, creature.lastSighting,
                creature.lastDimension, creature.lastX, creature.lastZ));
    }

    private boolean given;
    private final Map<ResourceLocation, Creature> creatures = new LinkedHashMap<>();
    /** Per dimension: chunk position to its sampled colours, oldest first. */
    private final Map<String, LinkedHashMap<Long, byte[]>> explored = new HashMap<>();
    /** Per dimension: danger cell to its marks, (strongest rank seen + 1) in the low byte and deaths above it. */
    private final Map<String, Map<Long, Integer>> danger = new HashMap<>();
    private final List<Pin> pins = new ArrayList<>();

    public static FieldGuideData empty() {
        return new FieldGuideData();
    }

    public boolean given() {
        return given;
    }

    public void markGiven() {
        given = true;
    }

    // Creatures

    public Map<ResourceLocation, Creature> creatures() {
        return java.util.Collections.unmodifiableMap(creatures);
    }

    public Optional<Creature> creature(ResourceLocation type) {
        return Optional.ofNullable(creatures.get(type));
    }

    /**
     * Counts a sighting unless the same kind was sighted less than
     * {@code cooldown} ticks ago. Returns whether it counted.
     */
    public boolean sight(ResourceLocation type, int rank, String dimension, int x, int z, long now, int cooldown, int amount) {
        Creature creature = creatures.computeIfAbsent(type, key -> new Creature());
        creature.meet(rank, dimension, x, z);
        if (creature.lastSighting >= 0 && now - creature.lastSighting < cooldown) {
            return false;
        }
        creature.lastSighting = now;
        creature.sightings += Math.max(1, amount);
        return true;
    }

    public void kill(ResourceLocation type, int rank, String dimension, int x, int z) {
        Creature creature = creatures.computeIfAbsent(type, key -> new Creature());
        creature.meet(rank, dimension, x, z);
        creature.kills++;
    }

    // Map

    public boolean explored(String dimension, long chunk) {
        Map<Long, byte[]> chunks = explored.get(dimension);
        return chunks != null && chunks.containsKey(chunk);
    }

    public Map<Long, byte[]> exploredChunks(String dimension) {
        return java.util.Collections.unmodifiableMap(explored.getOrDefault(dimension, new LinkedHashMap<>()));
    }

    public java.util.Set<String> exploredDimensions() {
        return java.util.Collections.unmodifiableSet(explored.keySet());
    }

    /**
     * Records a chunk's colours. Returns whether anything changed (a new
     * chunk, or one whose colours differ from what was remembered).
     */
    public boolean explore(String dimension, long chunk, byte[] colours, int maxChunks) {
        LinkedHashMap<Long, byte[]> chunks = explored.computeIfAbsent(dimension, key -> new LinkedHashMap<>());
        byte[] previous = chunks.get(chunk);
        if (previous != null && java.util.Arrays.equals(previous, colours)) {
            return false;
        }
        chunks.put(chunk, colours.clone());
        Iterator<Long> oldest = chunks.keySet().iterator();
        while (chunks.size() > maxChunks && oldest.hasNext()) {
            oldest.next();
            oldest.remove();
        }
        return true;
    }

    // Danger

    public Map<Long, Integer> dangerMarks(String dimension) {
        return java.util.Collections.unmodifiableMap(danger.getOrDefault(dimension, Map.of()));
    }

    /** Raises a cell's strongest-seen rank. Returns whether it rose. */
    public boolean markSeen(String dimension, long cell, int rank) {
        Map<Long, Integer> cells = danger.computeIfAbsent(dimension, key -> new HashMap<>());
        int marks = cells.getOrDefault(cell, 0);
        if (strongest(marks) >= rank) {
            return false;
        }
        cells.put(cell, (marks & ~0xFF) | (rank + 1));
        return true;
    }

    public void markDeath(String dimension, long cell) {
        Map<Long, Integer> cells = danger.computeIfAbsent(dimension, key -> new HashMap<>());
        int marks = cells.getOrDefault(cell, 0);
        cells.put(cell, (marks & 0xFF) | ((deaths(marks) + 1) << 8));
    }

    public static int strongest(int marks) {
        return (marks & 0xFF) - 1;
    }

    public static int deaths(int marks) {
        return marks >>> 8;
    }

    // Pins

    public List<Pin> pins() {
        return java.util.Collections.unmodifiableList(pins);
    }

    public boolean hasPin(UUID ref) {
        return pins.stream().anyMatch(pin -> pin.ref().filter(ref::equals).isPresent());
    }

    /** Adds a pin; returns false once the guide is full. */
    public boolean addPin(Pin pin) {
        if (pins.size() >= MAX_PINS) {
            return false;
        }
        pins.add(pin);
        return true;
    }

    public boolean removePin(java.util.function.Predicate<Pin> which) {
        return pins.removeIf(which);
    }

    // Saving

    private record Saved(boolean given, Map<ResourceLocation, Creature> creatures, Map<String, LongStream> explored,
                         Map<String, LongStream> danger, List<Pin> pins) {
    }

    public static final Codec<FieldGuideData> CODEC = RecordCodecBuilder.<Saved>create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("given", false).forGetter(Saved::given),
            Codec.unboundedMap(ResourceLocation.CODEC, Creature.CODEC).optionalFieldOf("creatures", Map.of()).forGetter(Saved::creatures),
            Codec.unboundedMap(Codec.STRING, Codec.LONG_STREAM).optionalFieldOf("explored", Map.of()).forGetter(Saved::explored),
            Codec.unboundedMap(Codec.STRING, Codec.LONG_STREAM).optionalFieldOf("danger", Map.of()).forGetter(Saved::danger),
            Pin.CODEC.listOf().optionalFieldOf("pins", List.of()).forGetter(Saved::pins)
    ).apply(instance, Saved::new)).xmap(FieldGuideData::fromSaved, FieldGuideData::toSaved);

    private static FieldGuideData fromSaved(Saved saved) {
        FieldGuideData data = new FieldGuideData();
        data.given = saved.given;
        data.creatures.putAll(saved.creatures);
        saved.explored.forEach((dimension, stream) -> {
            long[] values = stream.toArray();
            LinkedHashMap<Long, byte[]> chunks = new LinkedHashMap<>();
            for (int i = 0; i + 2 < values.length; i += 3) {
                chunks.put(values[i], unpack(values[i + 1], values[i + 2]));
            }
            data.explored.put(dimension, chunks);
        });
        saved.danger.forEach((dimension, stream) -> {
            long[] values = stream.toArray();
            Map<Long, Integer> cells = new HashMap<>();
            for (int i = 0; i + 1 < values.length; i += 2) {
                cells.put(values[i], (int) values[i + 1]);
            }
            data.danger.put(dimension, cells);
        });
        data.pins.addAll(saved.pins);
        return data;
    }

    private Saved toSaved() {
        Map<String, LongStream> exploredOut = new HashMap<>();
        explored.forEach((dimension, chunks) -> {
            long[] values = new long[chunks.size() * 3];
            int i = 0;
            for (Map.Entry<Long, byte[]> entry : chunks.entrySet()) {
                values[i++] = entry.getKey();
                values[i++] = pack(entry.getValue(), 0);
                values[i++] = pack(entry.getValue(), 8);
            }
            exploredOut.put(dimension, LongStream.of(values));
        });
        Map<String, LongStream> dangerOut = new HashMap<>();
        danger.forEach((dimension, cells) -> {
            long[] values = new long[cells.size() * 2];
            int i = 0;
            for (Map.Entry<Long, Integer> entry : cells.entrySet()) {
                values[i++] = entry.getKey();
                values[i++] = entry.getValue();
            }
            dangerOut.put(dimension, LongStream.of(values));
        });
        return new Saved(given, new LinkedHashMap<>(creatures), exploredOut, dangerOut, new ArrayList<>(pins));
    }

    static long pack(byte[] colours, int from) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value |= (colours[from + i] & 0xFFL) << (8 * i);
        }
        return value;
    }

    static byte[] unpack(long low, long high) {
        byte[] colours = new byte[CHUNK_BYTES];
        for (int i = 0; i < 8; i++) {
            colours[i] = (byte) (low >>> (8 * i));
            colours[8 + i] = (byte) (high >>> (8 * i));
        }
        return colours;
    }
}
