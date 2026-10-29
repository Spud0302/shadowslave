package dev.spud.shadowslave.trial;

import dev.spud.shadowslave.trial.content.NightmarePack;
import net.minecraft.core.BlockPos;
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
 * clock, and what has happened in it so far. It lives only while the server
 * runs; {@link TrialData} keeps what a player needs to be put back if it is lost.
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
    ServerBossEvent tyrantBar;

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

    public int category() {
        return category;
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
}
