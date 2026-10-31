package dev.spud.shadowslave.clan;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.fieldguide.FieldGuideData;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Every clan on the server, saved with the Overworld (DESIGN, Jack
 * 2026-10-29: clans own Citadels, and a single player can be a clan of
 * one). A clan keeps its members, the invitations it has sent out, and its
 * Field Guide board: what its members have shared at their Citadels.
 */
public final class ClanData extends SavedData {
    private static final String NAME = "shadowslave_clans";

    final Map<UUID, Clan> clans = new LinkedHashMap<>();

    public static final class Clan {
        final UUID id;
        String name;
        UUID leader;
        /** Members in the order they joined, with the name each last went by. */
        final LinkedHashMap<UUID, String> members = new LinkedHashMap<>();
        final Set<UUID> invites = new LinkedHashSet<>();
        FieldGuideData board = FieldGuideData.empty();
        /** The clan's seat (Run I): the Citadel whose capture ends a war against it. */
        UUID seat;
        /** When the seat last moved, in real time; 0 lets it move freely. */
        long seatMovedAt;
        /** When a member was last seen logged in, in real time; 0 until first seen. */
        long lastSeen;

        Clan(UUID id, String name, UUID leader) {
            this.id = id;
            this.name = name;
            this.leader = leader;
        }

        public UUID id() {
            return id;
        }

        public String name() {
            return name;
        }

        public UUID leader() {
            return leader;
        }

        public Collection<UUID> members() {
            return List.copyOf(members.keySet());
        }

        public String memberName(UUID member) {
            return members.getOrDefault(member, "?");
        }

        public boolean has(UUID player) {
            return members.containsKey(player);
        }

        public boolean invited(UUID player) {
            return invites.contains(player);
        }

        public FieldGuideData board() {
            return board;
        }

        public Optional<UUID> seat() {
            return Optional.ofNullable(seat);
        }

        public long seatMovedAt() {
            return seatMovedAt;
        }

        /** Names the clan's seat; {@code movedAt} 0 leaves it free to move again. */
        public void setSeat(UUID seat, long movedAt) {
            this.seat = seat;
            this.seatMovedAt = movedAt;
        }

        public long lastSeen() {
            return lastSeen;
        }

        public void seen(long now) {
            lastSeen = now;
        }

        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("id", id);
            tag.putString("name", name);
            tag.putUUID("leader", leader);
            ListTag members = new ListTag();
            this.members.forEach((uuid, name) -> {
                CompoundTag member = new CompoundTag();
                member.putUUID("id", uuid);
                member.putString("name", name);
                members.add(member);
            });
            tag.put("members", members);
            ListTag invites = new ListTag();
            this.invites.forEach(uuid -> invites.add(UUIDUtil.CODEC.encodeStart(NbtOps.INSTANCE, uuid).getOrThrow()));
            tag.put("invites", invites);
            FieldGuideData.CODEC.encodeStart(NbtOps.INSTANCE, board).result().ifPresent(saved -> tag.put("board", saved));
            if (seat != null) {
                tag.putUUID("seat", seat);
            }
            tag.putLong("seat_moved_at", seatMovedAt);
            tag.putLong("last_seen", lastSeen);
            return tag;
        }

        static Clan load(CompoundTag tag) {
            Clan clan = new Clan(tag.getUUID("id"), tag.getString("name"), tag.getUUID("leader"));
            for (Tag member : tag.getList("members", Tag.TAG_COMPOUND)) {
                CompoundTag entry = (CompoundTag) member;
                clan.members.put(entry.getUUID("id"), entry.getString("name"));
            }
            for (Tag invite : tag.getList("invites", Tag.TAG_INT_ARRAY)) {
                UUIDUtil.CODEC.parse(NbtOps.INSTANCE, invite).result().ifPresent(clan.invites::add);
            }
            if (tag.contains("board")) {
                clan.board = FieldGuideData.CODEC.parse(NbtOps.INSTANCE, tag.get("board"))
                        .resultOrPartial(error -> ShadowSlaveMod.LOGGER.warn("Bad Field Guide board for clan {}: {}", clan.name, error))
                        .orElseGet(FieldGuideData::empty);
            }
            clan.seat = tag.hasUUID("seat") ? tag.getUUID("seat") : null;
            clan.seatMovedAt = tag.getLong("seat_moved_at");
            clan.lastSeen = tag.getLong("last_seen");
            return clan;
        }
    }

    public static ClanData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(new Factory<>(ClanData::new, ClanData::load), NAME);
    }

    private static ClanData load(CompoundTag tag, HolderLookup.Provider registries) {
        ClanData data = new ClanData();
        for (Tag clan : tag.getList("clans", Tag.TAG_COMPOUND)) {
            Clan loaded = Clan.load((CompoundTag) clan);
            data.clans.put(loaded.id, loaded);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        clans.values().forEach(clan -> list.add(clan.save()));
        tag.put("clans", list);
        return tag;
    }

    public List<Clan> clans() {
        return new ArrayList<>(clans.values());
    }

    public Optional<Clan> byId(UUID id) {
        return Optional.ofNullable(clans.get(id));
    }

    public Optional<Clan> of(UUID player) {
        return clans.values().stream().filter(clan -> clan.has(player)).findFirst();
    }

    public Optional<Clan> named(String name) {
        String wanted = name.trim().toLowerCase(Locale.ROOT);
        return clans.values().stream().filter(clan -> clan.name.toLowerCase(Locale.ROOT).equals(wanted)).findFirst();
    }
}
