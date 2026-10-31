package dev.spud.shadowslave.citadel;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * The NPC Lesser Legacy Clans a world was born with (DESIGN, Jack 2026-10-31),
 * and every player's standing with each. Saved with the overworld, beside the
 * player clans.
 */
public final class NpcClanData extends SavedData {
    private static final String NAME = "shadowslave_npc_clans";

    final List<NpcClan> clans = new ArrayList<>();
    /** Standing points, by player then by NPC clan. */
    final Map<UUID, Map<UUID, Integer>> standing = new HashMap<>();

    /**
     * An NPC clan: its name and colours, the Great Clan it answers to, and the
     * Saint who leads it and binds its Citadels.
     */
    public static final class NpcClan {
        final UUID id;
        final String name;
        final String greatClan;
        final DyeColor colour;
        final String lord;
        final String lordNature;

        NpcClan(UUID id, String name, String greatClan, DyeColor colour, String lord, String lordNature) {
            this.id = id;
            this.name = name;
            this.greatClan = greatClan;
            this.colour = colour;
            this.lord = lord;
            this.lordNature = lordNature;
        }

        public UUID id() {
            return id;
        }

        public String name() {
            return name;
        }

        public String greatClan() {
            return greatClan;
        }

        public DyeColor colour() {
            return colour;
        }

        public String lord() {
            return lord;
        }

        public String lordNature() {
            return lordNature;
        }

        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("id", id);
            tag.putString("name", name);
            tag.putString("great_clan", greatClan);
            tag.putString("colour", colour.getSerializedName());
            tag.putString("lord", lord);
            tag.putString("lord_nature", lordNature);
            return tag;
        }

        static NpcClan load(CompoundTag tag) {
            return new NpcClan(tag.getUUID("id"), tag.getString("name"), tag.getString("great_clan"),
                    DyeColor.byName(tag.getString("colour"), DyeColor.GRAY), tag.getString("lord"), tag.getString("lord_nature"));
        }
    }

    public static NpcClanData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(new Factory<>(NpcClanData::new, NpcClanData::load), NAME);
    }

    private static NpcClanData load(CompoundTag tag, HolderLookup.Provider registries) {
        NpcClanData data = new NpcClanData();
        for (Tag clan : tag.getList("clans", Tag.TAG_COMPOUND)) {
            data.clans.add(NpcClan.load((CompoundTag) clan));
        }
        CompoundTag players = tag.getCompound("standing");
        for (String player : players.getAllKeys()) {
            CompoundTag withClans = players.getCompound(player);
            Map<UUID, Integer> points = new HashMap<>();
            for (String clan : withClans.getAllKeys()) {
                points.put(UUID.fromString(clan), withClans.getInt(clan));
            }
            data.standing.put(UUID.fromString(player), points);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        clans.forEach(clan -> list.add(clan.save()));
        tag.put("clans", list);
        CompoundTag players = new CompoundTag();
        standing.forEach((player, points) -> {
            CompoundTag withClans = new CompoundTag();
            points.forEach((clan, value) -> withClans.putInt(clan.toString(), value));
            players.put(player.toString(), withClans);
        });
        tag.put("standing", players);
        return tag;
    }

    public List<NpcClan> clans() {
        return List.copyOf(clans);
    }

    public Optional<NpcClan> byId(UUID id) {
        return clans.stream().filter(clan -> clan.id.equals(id)).findFirst();
    }

    public int standing(UUID player, UUID clan) {
        return standing.getOrDefault(player, Map.of()).getOrDefault(clan, 0);
    }

    void addStanding(UUID player, UUID clan, int points) {
        standing.computeIfAbsent(player, ignored -> new HashMap<>()).merge(clan, points, Integer::sum);
        setDirty();
    }
}
