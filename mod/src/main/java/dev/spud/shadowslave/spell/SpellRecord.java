package dev.spud.shadowslave.spell;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * The Spell's memory of one world, saved with it (DESIGN, Jack 2026-10-29: the
 * Recorder). A rolling log of notable events, a running tally per player that
 * outlives the log, and the server's firsts. Plain game data: nothing here
 * needs, or is written by, an AI.
 */
public final class SpellRecord extends SavedData {
    private static final String NAME = "shadowslave_spell_record";
    /** How many recent grades and behaviours a tally remembers. */
    static final int RECENT = 16;

    final Deque<SpellEvent> events = new ArrayDeque<>();
    final Map<UUID, Tally> tallies = new HashMap<>();
    /** Each first, by key ("nightmare_2", "kill_fallen_demon"), and who did it. */
    final Map<String, First> firsts = new LinkedHashMap<>();
    /** Every True Name the Spell has granted in this world, by bearer, so no two souls share one. */
    final Map<UUID, String> trueNames = new LinkedHashMap<>();

    public record First(UUID player, String name, long time, String what) {
    }

    /** What the Spell remembers of one player, however long ago it happened. */
    public static final class Tally {
        /** Kills by "rank class", e.g. "awakened monster". */
        final Map<String, Integer> kills = new LinkedHashMap<>();
        int uniqueKills;
        int cleared;
        int failed;
        int fled;
        int deaths;
        int citadels;
        /** Raids on a Citadel they helped hold, and how many of those an appraisal has already recalled. */
        int raidsHeld;
        int raidsRecalled;
        /** Recent appraisal grades, oldest first. */
        final List<String> grades = new ArrayList<>();
        /** Recent behaviours the Judge read in their appraisals, oldest first. */
        final List<String> behaviours = new ArrayList<>();
        /** How their last Nightmare ended for them: cleared, failed or fled. */
        String lastOutcome = "";

        public Map<String, Integer> kills() {
            return Map.copyOf(kills);
        }

        public int totalKills() {
            return kills.values().stream().mapToInt(Integer::intValue).sum();
        }

        public int uniqueKills() {
            return uniqueKills;
        }

        public int cleared() {
            return cleared;
        }

        public int failed() {
            return failed;
        }

        public int fled() {
            return fled;
        }

        public int deaths() {
            return deaths;
        }

        public int citadels() {
            return citadels;
        }

        public int raidsHeld() {
            return raidsHeld;
        }

        /** Raids they held that no appraisal has recalled yet. */
        public int raidsUnrecalled() {
            return Math.max(0, raidsHeld - raidsRecalled);
        }

        public void heldRaid() {
            raidsHeld++;
        }

        public void recallRaids() {
            raidsRecalled = raidsHeld;
        }

        public List<String> grades() {
            return List.copyOf(grades);
        }

        public Optional<String> lastGrade() {
            return grades.isEmpty() ? Optional.empty() : Optional.of(grades.get(grades.size() - 1));
        }

        public List<String> behaviours() {
            return List.copyOf(behaviours);
        }

        public Optional<String> lastBehaviour() {
            return behaviours.isEmpty() ? Optional.empty() : Optional.of(behaviours.get(behaviours.size() - 1));
        }

        public String lastOutcome() {
            return lastOutcome;
        }

        /** A copy as it stands now, e.g. to compare with after an appraisal. */
        public Tally copy() {
            return load(save());
        }

        static void push(List<String> list, String value) {
            list.add(value);
            while (list.size() > RECENT) {
                list.remove(0);
            }
        }

        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            CompoundTag killTag = new CompoundTag();
            kills.forEach(killTag::putInt);
            tag.put("kills", killTag);
            tag.putInt("unique_kills", uniqueKills);
            tag.putInt("cleared", cleared);
            tag.putInt("failed", failed);
            tag.putInt("fled", fled);
            tag.putInt("deaths", deaths);
            tag.putInt("citadels", citadels);
            tag.putInt("raids_held", raidsHeld);
            tag.putInt("raids_recalled", raidsRecalled);
            tag.put("grades", strings(grades));
            tag.put("behaviours", strings(behaviours));
            tag.putString("last_outcome", lastOutcome);
            return tag;
        }

        static Tally load(CompoundTag tag) {
            Tally tally = new Tally();
            CompoundTag killTag = tag.getCompound("kills");
            for (String key : killTag.getAllKeys()) {
                tally.kills.put(key, killTag.getInt(key));
            }
            tally.uniqueKills = tag.getInt("unique_kills");
            tally.cleared = tag.getInt("cleared");
            tally.failed = tag.getInt("failed");
            tally.fled = tag.getInt("fled");
            tally.deaths = tag.getInt("deaths");
            tally.citadels = tag.getInt("citadels");
            tally.raidsHeld = tag.getInt("raids_held");
            tally.raidsRecalled = tag.getInt("raids_recalled");
            readStrings(tag.getList("grades", Tag.TAG_STRING), tally.grades);
            readStrings(tag.getList("behaviours", Tag.TAG_STRING), tally.behaviours);
            tally.lastOutcome = tag.getString("last_outcome");
            return tally;
        }
    }

    public static SpellRecord get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(new Factory<>(SpellRecord::new, SpellRecord::load), NAME);
    }

    /** Adds an event, forgetting the oldest past {@code cap}. */
    void add(SpellEvent event, int cap) {
        events.addLast(event);
        while (events.size() > cap) {
            events.removeFirst();
        }
        setDirty();
    }

    public Tally tally(UUID player) {
        return tallies.computeIfAbsent(player, id -> new Tally());
    }

    public Optional<Tally> find(UUID player) {
        return Optional.ofNullable(tallies.get(player));
    }

    public Optional<First> first(String key) {
        return Optional.ofNullable(firsts.get(key));
    }

    public Map<String, First> firsts() {
        return Map.copyOf(firsts);
    }

    /** The most recent events that match, newest first. */
    public List<SpellEvent> recent(Predicate<SpellEvent> filter, int limit) {
        List<SpellEvent> out = new ArrayList<>();
        var iterator = events.descendingIterator();
        while (iterator.hasNext() && out.size() < limit) {
            SpellEvent event = iterator.next();
            if (filter.test(event)) {
                out.add(event);
            }
        }
        return out;
    }

    /** Whether anyone in this world already bears the name (case and spacing aside). */
    public boolean nameTaken(String name) {
        return bearerOf(name).isPresent();
    }

    public Optional<UUID> bearerOf(String name) {
        return trueNames.entrySet().stream()
                .filter(entry -> dev.spud.shadowslave.spell.truename.TrueNameBuilder.same(entry.getValue(), name))
                .map(Map.Entry::getKey).findFirst();
    }

    public Map<UUID, String> trueNames() {
        return Map.copyOf(trueNames);
    }

    /** Records a granted True Name, or with an empty name forgets the bearer's. */
    public void setTrueName(UUID bearer, String name) {
        if (name.isEmpty()) {
            trueNames.remove(bearer);
        } else {
            trueNames.put(bearer, name);
        }
        setDirty();
    }

    public int size() {
        return events.size();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag eventTags = new ListTag();
        events.forEach(event -> eventTags.add(event.save()));
        tag.put("events", eventTags);
        CompoundTag tallyTags = new CompoundTag();
        tallies.forEach((id, tally) -> tallyTags.put(id.toString(), tally.save()));
        tag.put("tallies", tallyTags);
        CompoundTag firstTags = new CompoundTag();
        firsts.forEach((key, first) -> {
            CompoundTag one = new CompoundTag();
            one.putUUID("player", first.player());
            one.putString("name", first.name());
            one.putLong("time", first.time());
            one.putString("what", first.what());
            firstTags.put(key, one);
        });
        tag.put("firsts", firstTags);
        CompoundTag nameTags = new CompoundTag();
        trueNames.forEach((id, name) -> nameTags.putString(id.toString(), name));
        tag.put("true_names", nameTags);
        return tag;
    }

    private static SpellRecord load(CompoundTag tag, HolderLookup.Provider registries) {
        SpellRecord record = new SpellRecord();
        for (Tag each : tag.getList("events", Tag.TAG_COMPOUND)) {
            SpellEvent.load((CompoundTag) each).ifPresent(record.events::addLast);
        }
        CompoundTag tallyTags = tag.getCompound("tallies");
        for (String key : tallyTags.getAllKeys()) {
            try {
                record.tallies.put(UUID.fromString(key), Tally.load(tallyTags.getCompound(key)));
            } catch (IllegalArgumentException ignored) {
                // A malformed id is skipped rather than failing the whole record.
            }
        }
        CompoundTag firstTags = tag.getCompound("firsts");
        for (String key : firstTags.getAllKeys()) {
            CompoundTag one = firstTags.getCompound(key);
            if (one.hasUUID("player")) {
                record.firsts.put(key, new First(one.getUUID("player"), one.getString("name"), one.getLong("time"), one.getString("what")));
            }
        }
        CompoundTag nameTags = tag.getCompound("true_names");
        for (String key : nameTags.getAllKeys()) {
            try {
                record.trueNames.put(UUID.fromString(key), nameTags.getString(key));
            } catch (IllegalArgumentException ignored) {
                // A malformed id is skipped rather than failing the whole record.
            }
        }
        return record;
    }

    static ListTag strings(List<String> values) {
        ListTag list = new ListTag();
        values.forEach(value -> list.add(StringTag.valueOf(value)));
        return list;
    }

    static void readStrings(ListTag list, List<String> into) {
        for (int i = 0; i < list.size(); i++) {
            into.add(list.getString(i));
        }
    }
}
