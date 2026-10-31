package dev.spud.shadowslave.trial;

import dev.spud.shadowslave.trial.content.NightmarePack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * One running Nightmare: its cohort, the role each of them was given, its own
 * clock, and what has happened in it so far. A short one lives only while the
 * server runs; {@link TrialData} keeps what a player needs to be put back if
 * it is lost. A Long Nightmare is saved whole in {@link LongNightmareData}, so
 * it outlives logouts and restarts.
 */
public final class TrialInstance {
    final UUID id;
    final ResourceLocation nightmareId;
    final NightmarePack pack;
    final Optional<ResourceLocation> site;
    final Optional<UUID> seed;
    final int category;
    final int slot;
    final BlockPos origin;
    final Map<UUID, Member> members = new LinkedHashMap<>();
    final Set<UUID> spawned = new HashSet<>();
    /** Ticks the Nightmare has run, on its own clock. */
    int ticks;
    int nextEvent;
    boolean tyrantSpawned;
    UUID tyrant;
    boolean tyrantSlain;
    boolean over;
    int falls;
    /** A First Nightmare: born from the Seed inside its challenger, and the way they Awaken. */
    boolean first;
    ServerBossEvent tyrantBar;
    /** The last day of a Long Nightmare the Spell announced. */
    int dayAnnounced = 1;
    /** Server game time when a challenger was last inside and awake; a Long Nightmare left long enough fades. */
    long lastWalked;
    /** The ending a Long Nightmare closed with, kept until every challenger who slept through it has been appraised. */
    String endedWith = "";

    /** A challenger and the role the Spell gave them. */
    public static final class Member {
        final UUID player;
        final String name;
        final NightmarePack.Role role;
        boolean inside = true;
        /** Kills by what was slain ("Awakened Monster, Chainback"), in order. */
        final Map<String, Integer> kills = new LinkedHashMap<>();
        int killScore;
        boolean slewTyrant;
        /** Tyrants or stronger, and named creatures, slain. */
        int uniqueKills;
        /** Kills of creatures above their own rank. */
        int aboveRank;
        /** Others of the cohort they struck, each counted once. */
        final Set<UUID> struck = new HashSet<>();
        /** Creatures they slew while those were hunting another of the cohort. */
        int alliesSaved;
        /** Times they fell to a quarter of their health or less. */
        int closeCalls;
        /** Whether they are low right now, so one close call is counted once. */
        boolean low;
        /** Logged out inside a Long Nightmare: they stay in it, held by a lingering figure. */
        boolean away;
        /** The lingering figure that holds their place while they are away. */
        UUID lingerer;
        /** Their lingering figure was slain: they fell while away, and learn it when they return. */
        boolean fellAway;
        /** The Nightmare ended while they were away and alive inside it: they are appraised when they return. */
        boolean pending;

        Member(UUID player, String name, NightmarePack.Role role) {
            this.player = player;
            this.name = name;
            this.role = role;
        }

        public NightmarePack.Role role() {
            return role;
        }

        public boolean inside() {
            return inside;
        }

        public boolean away() {
            return away;
        }

        public UUID player() {
            return player;
        }

        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("player", player);
            tag.putString("name", name);
            tag.putString("role", role.id());
            tag.putBoolean("inside", inside);
            CompoundTag killTag = new CompoundTag();
            kills.forEach(killTag::putInt);
            tag.put("kills", killTag);
            tag.putInt("kill_score", killScore);
            tag.putBoolean("slew_tyrant", slewTyrant);
            tag.putInt("unique_kills", uniqueKills);
            tag.putInt("above_rank", aboveRank);
            ListTag struckTag = new ListTag();
            struck.forEach(id -> struckTag.add(NbtUtils.createUUID(id)));
            tag.put("struck", struckTag);
            tag.putInt("allies_saved", alliesSaved);
            tag.putInt("close_calls", closeCalls);
            tag.putBoolean("low", low);
            tag.putBoolean("away", away);
            if (lingerer != null) {
                tag.putUUID("lingerer", lingerer);
            }
            tag.putBoolean("fell_away", fellAway);
            tag.putBoolean("pending", pending);
            return tag;
        }

        static Optional<Member> load(CompoundTag tag, NightmarePack pack) {
            Optional<NightmarePack.Role> role = pack.role(tag.getString("role"));
            if (role.isEmpty() || !tag.hasUUID("player")) {
                return Optional.empty();
            }
            Member member = new Member(tag.getUUID("player"), tag.getString("name"), role.get());
            member.inside = tag.getBoolean("inside");
            CompoundTag killTag = tag.getCompound("kills");
            killTag.getAllKeys().forEach(key -> member.kills.put(key, killTag.getInt(key)));
            member.killScore = tag.getInt("kill_score");
            member.slewTyrant = tag.getBoolean("slew_tyrant");
            member.uniqueKills = tag.getInt("unique_kills");
            member.aboveRank = tag.getInt("above_rank");
            tag.getList("struck", Tag.TAG_INT_ARRAY).forEach(id -> member.struck.add(NbtUtils.loadUUID(id)));
            member.alliesSaved = tag.getInt("allies_saved");
            member.closeCalls = tag.getInt("close_calls");
            member.low = tag.getBoolean("low");
            member.away = tag.getBoolean("away");
            member.lingerer = tag.hasUUID("lingerer") ? tag.getUUID("lingerer") : null;
            member.fellAway = tag.getBoolean("fell_away");
            member.pending = tag.getBoolean("pending");
            return Optional.of(member);
        }
    }

    TrialInstance(UUID id, ResourceLocation nightmareId, NightmarePack pack, Optional<ResourceLocation> site, Optional<UUID> seed,
                  int category, int slot, BlockPos origin) {
        this.id = id;
        this.nightmareId = nightmareId;
        this.pack = pack;
        this.site = site;
        this.seed = seed;
        this.category = category;
        this.slot = slot;
        this.origin = origin;
    }

    public UUID id() {
        return id;
    }

    public ResourceLocation nightmareId() {
        return nightmareId;
    }

    public NightmarePack pack() {
        return pack;
    }

    public boolean isLong() {
        return pack.isLong();
    }

    /** The day of a Long Nightmare its clock stands in, from 1; always 1 for a short one. */
    public int day() {
        return pack.longRun().map(run -> run.dayAt(ticks)).orElse(1);
    }

    public int category() {
        return category;
    }

    public boolean first() {
        return first;
    }

    public Optional<UUID> seed() {
        return seed;
    }

    public BlockPos origin() {
        return origin;
    }

    public int ticks() {
        return ticks;
    }

    public boolean over() {
        return over;
    }

    public Optional<Member> member(UUID player) {
        return Optional.ofNullable(members.get(player));
    }

    public List<Member> members() {
        return List.copyOf(members.values());
    }

    /** Challengers still inside. */
    public List<Member> inside() {
        return members.values().stream().filter(Member::inside).toList();
    }

    /** A point of the pack's structure, in the world. */
    public BlockPos at(String point) {
        return origin.offset(pack.point(point));
    }

    CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("id", id);
        tag.putString("nightmare", nightmareId.toString());
        site.ifPresent(value -> tag.putString("site", value.toString()));
        seed.ifPresent(value -> tag.putUUID("seed", value));
        tag.putInt("category", category);
        tag.putInt("slot", slot);
        tag.put("origin", NbtUtils.writeBlockPos(origin));
        tag.putInt("ticks", ticks);
        tag.putInt("next_event", nextEvent);
        tag.putBoolean("tyrant_spawned", tyrantSpawned);
        if (tyrant != null) {
            tag.putUUID("tyrant", tyrant);
        }
        tag.putBoolean("tyrant_slain", tyrantSlain);
        tag.putBoolean("over", over);
        tag.putInt("falls", falls);
        tag.putInt("day_announced", dayAnnounced);
        tag.putLong("last_walked", lastWalked);
        tag.putString("ended_with", endedWith);
        ListTag memberTag = new ListTag();
        members.values().forEach(member -> memberTag.add(member.save()));
        tag.put("members", memberTag);
        return tag;
    }

    /** Rebuilds a saved run; empty if its Nightmare is gone from the data packs. */
    static Optional<TrialInstance> load(CompoundTag tag, java.util.function.Function<ResourceLocation, Optional<NightmarePack>> packs) {
        ResourceLocation nightmare = ResourceLocation.tryParse(tag.getString("nightmare"));
        if (nightmare == null || !tag.hasUUID("id")) {
            return Optional.empty();
        }
        Optional<NightmarePack> pack = packs.apply(nightmare);
        if (pack.isEmpty()) {
            return Optional.empty();
        }
        TrialInstance instance = new TrialInstance(tag.getUUID("id"), nightmare, pack.get(),
                Optional.ofNullable(tag.contains("site") ? ResourceLocation.tryParse(tag.getString("site")) : null),
                tag.hasUUID("seed") ? Optional.of(tag.getUUID("seed")) : Optional.empty(),
                tag.getInt("category"), tag.getInt("slot"),
                NbtUtils.readBlockPos(tag, "origin").orElse(TrialWorld.slotOrigin(tag.getInt("slot"))));
        instance.ticks = tag.getInt("ticks");
        instance.nextEvent = tag.getInt("next_event");
        instance.tyrantSpawned = tag.getBoolean("tyrant_spawned");
        instance.tyrant = tag.hasUUID("tyrant") ? tag.getUUID("tyrant") : null;
        instance.tyrantSlain = tag.getBoolean("tyrant_slain");
        instance.over = tag.getBoolean("over");
        instance.falls = tag.getInt("falls");
        instance.dayAnnounced = Math.max(1, tag.getInt("day_announced"));
        instance.lastWalked = tag.getLong("last_walked");
        instance.endedWith = tag.getString("ended_with");
        for (Tag member : tag.getList("members", Tag.TAG_COMPOUND)) {
            Member.load((CompoundTag) member, pack.get()).ifPresent(loaded -> instance.members.put(loaded.player, loaded));
        }
        return Optional.of(instance);
    }
}
