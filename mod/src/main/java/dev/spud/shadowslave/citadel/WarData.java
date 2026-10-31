package dev.spud.shadowslave.citadel;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Every war between clans (DESIGN, Run I), saved with the Overworld: who
 * declared on whom and when, the assaults each side has announced, their
 * cooldowns, the truces lost wars left behind, and which Citadels were raided
 * lately. Times are real-time milliseconds, shifted by {@link #clockOffset}
 * when an operator skips ahead.
 */
public final class WarData extends SavedData {
    private static final String NAME = "shadowslave_wars";

    final List<War> wars = new ArrayList<>();
    final List<Truce> truces = new ArrayList<>();
    /** Until when each raided Citadel is spared further raids. */
    final Map<UUID, Long> raidedUntil = new LinkedHashMap<>();
    /** Milliseconds an operator has skipped ahead, added to the clock. */
    long clockOffset;

    /** A war between two clans; either side may raid, strike and assault the other. */
    public static final class War {
        final UUID id;
        final UUID attacker;
        final UUID defender;
        final long declaredAt;
        final long activeAt;
        /** The assault each side has announced, keyed by the assaulting clan. */
        final Map<UUID, Assault> assaults = new LinkedHashMap<>();
        /** Until when each side may not announce another assault, after a failed one. */
        final Map<UUID, Long> cooldownUntil = new LinkedHashMap<>();
        /**
         * How far the Spell has warned about each clan nobody logs into: 1 once
         * it warned that the seat will turn Abandoned, 2 once it has. Cleared
         * when a member logs in.
         */
        final Map<UUID, Integer> warned = new LinkedHashMap<>();
        /** Whether both clans have heard that the notice is over. */
        boolean begun;
        /** Attacker-seconds channelled at each enemy seat, keyed by the assaulting clan; not saved. */
        final Map<UUID, Integer> channel = new HashMap<>();

        War(UUID id, UUID attacker, UUID defender, long declaredAt, long activeAt) {
            this.id = id;
            this.attacker = attacker;
            this.defender = defender;
            this.declaredAt = declaredAt;
            this.activeAt = activeAt;
        }

        public UUID id() {
            return id;
        }

        public UUID attacker() {
            return attacker;
        }

        public UUID defender() {
            return defender;
        }

        public long declaredAt() {
            return declaredAt;
        }

        public long activeAt() {
            return activeAt;
        }

        public boolean involves(UUID clan) {
            return attacker.equals(clan) || defender.equals(clan);
        }

        /** The other side. */
        public UUID enemyOf(UUID clan) {
            return attacker.equals(clan) ? defender : attacker;
        }

        public boolean active(long now) {
            return now >= activeAt;
        }

        public Optional<Assault> assault(UUID by) {
            return Optional.ofNullable(assaults.get(by));
        }

        public long cooldownUntil(UUID clan) {
            return cooldownUntil.getOrDefault(clan, 0L);
        }

        public int channel(UUID by) {
            return channel.getOrDefault(by, 0);
        }

        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("id", id);
            tag.putUUID("attacker", attacker);
            tag.putUUID("defender", defender);
            tag.putLong("declared_at", declaredAt);
            tag.putLong("active_at", activeAt);
            tag.putBoolean("begun", begun);
            ListTag list = new ListTag();
            assaults.values().forEach(assault -> list.add(assault.save()));
            tag.put("assaults", list);
            CompoundTag cooldowns = new CompoundTag();
            cooldownUntil.forEach((clan, until) -> cooldowns.putLong(clan.toString(), until));
            tag.put("cooldowns", cooldowns);
            CompoundTag warnedTag = new CompoundTag();
            warned.forEach((clan, stage) -> warnedTag.putInt(clan.toString(), stage));
            tag.put("warned", warnedTag);
            return tag;
        }

        static War load(CompoundTag tag) {
            War war = new War(tag.getUUID("id"), tag.getUUID("attacker"), tag.getUUID("defender"), tag.getLong("declared_at"),
                    tag.getLong("active_at"));
            war.begun = tag.getBoolean("begun");
            for (Tag entry : tag.getList("assaults", Tag.TAG_COMPOUND)) {
                Assault assault = Assault.load((CompoundTag) entry);
                war.assaults.put(assault.by, assault);
            }
            CompoundTag cooldowns = tag.getCompound("cooldowns");
            for (String key : cooldowns.getAllKeys()) {
                try {
                    war.cooldownUntil.put(UUID.fromString(key), cooldowns.getLong(key));
                } catch (IllegalArgumentException ignored) {
                    // A bad key loses only that cooldown.
                }
            }
            CompoundTag warnedTag = tag.getCompound("warned");
            for (String key : warnedTag.getAllKeys()) {
                try {
                    war.warned.put(UUID.fromString(key), warnedTag.getInt(key));
                } catch (IllegalArgumentException ignored) {
                    // A bad key only forgets that warning.
                }
            }
            return war;
        }
    }

    /**
     * An assault a clan has announced on the enemy's seat: when it begins, and
     * whether both clans have been told it has.
     */
    public static final class Assault {
        final UUID by;
        final UUID citadel;
        final long at;
        boolean begun;

        Assault(UUID by, UUID citadel, long at) {
            this.by = by;
            this.citadel = citadel;
            this.at = at;
        }

        public UUID by() {
            return by;
        }

        public UUID citadel() {
            return citadel;
        }

        public long at() {
            return at;
        }

        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("by", by);
            tag.putUUID("citadel", citadel);
            tag.putLong("at", at);
            tag.putBoolean("begun", begun);
            return tag;
        }

        static Assault load(CompoundTag tag) {
            Assault assault = new Assault(tag.getUUID("by"), tag.getUUID("citadel"), tag.getLong("at"));
            assault.begun = tag.getBoolean("begun");
            return assault;
        }
    }

    /** No war may be declared between these two clans until {@code until}. */
    public record Truce(UUID first, UUID second, long until) {
        boolean between(UUID a, UUID b) {
            return first.equals(a) && second.equals(b) || first.equals(b) && second.equals(a);
        }

        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("first", first);
            tag.putUUID("second", second);
            tag.putLong("until", until);
            return tag;
        }

        static Truce load(CompoundTag tag) {
            return new Truce(tag.getUUID("first"), tag.getUUID("second"), tag.getLong("until"));
        }
    }

    public static WarData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(new Factory<>(WarData::new, WarData::load), NAME);
    }

    private static WarData load(CompoundTag tag, HolderLookup.Provider registries) {
        WarData data = new WarData();
        for (Tag war : tag.getList("wars", Tag.TAG_COMPOUND)) {
            data.wars.add(War.load((CompoundTag) war));
        }
        for (Tag truce : tag.getList("truces", Tag.TAG_COMPOUND)) {
            data.truces.add(Truce.load((CompoundTag) truce));
        }
        CompoundTag raided = tag.getCompound("raided_until");
        for (String key : raided.getAllKeys()) {
            try {
                data.raidedUntil.put(UUID.fromString(key), raided.getLong(key));
            } catch (IllegalArgumentException ignored) {
                // A bad key loses only that cooldown.
            }
        }
        data.clockOffset = tag.getLong("clock_offset");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        wars.forEach(war -> list.add(war.save()));
        tag.put("wars", list);
        ListTag truceList = new ListTag();
        truces.forEach(truce -> truceList.add(truce.save()));
        tag.put("truces", truceList);
        CompoundTag raided = new CompoundTag();
        raidedUntil.forEach((citadel, until) -> raided.putLong(citadel.toString(), until));
        tag.put("raided_until", raided);
        tag.putLong("clock_offset", clockOffset);
        return tag;
    }

    public List<War> wars() {
        return List.copyOf(wars);
    }

    /** The wars a clan is in, declared or under way. */
    public List<War> of(UUID clan) {
        return wars.stream().filter(war -> war.involves(clan)).toList();
    }

    public Optional<War> between(UUID a, UUID b) {
        return wars.stream().filter(war -> war.involves(a) && war.involves(b)).findFirst();
    }

    public Optional<Truce> truce(UUID a, UUID b, long now) {
        return truces.stream().filter(truce -> truce.between(a, b) && truce.until > now).findFirst();
    }
}
