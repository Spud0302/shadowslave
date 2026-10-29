package dev.spud.shadowslave.citadel;

import dev.spud.shadowslave.creature.CreatureRank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Every Citadel the Dream Realm has woken, saved with it, keyed by its Gateway. */
public final class CitadelData extends SavedData {
    private static final String NAME = "shadowslave_citadels";

    final List<Citadel> citadels = new ArrayList<>();

    /** Who holds a Citadel: its Guardian, nobody (conquered, the Gateway open), or a clan. */
    public enum Hold {
        GUARDIAN, CONQUERED, CLAN;

        GatewayBlock.State gateway() {
            return switch (this) {
                case GUARDIAN -> GatewayBlock.State.SEALED;
                case CONQUERED -> GatewayBlock.State.OPEN;
                case CLAN -> GatewayBlock.State.CLAIMED;
            };
        }
    }

    public static final class Citadel {
        final UUID id;
        final BlockPos gateway;
        final String name;
        final CreatureRank rank;
        Hold hold = Hold.GUARDIAN;
        UUID guardian;
        BlockPos guardianPos;
        /** Ticks the Guardian has been missing while its post was loaded; not saved. */
        int missing;
        UUID conqueror;
        UUID conquerorClan;
        long conqueredAt;
        UUID clan;

        Citadel(UUID id, BlockPos gateway, String name, CreatureRank rank) {
            this.id = id;
            this.gateway = gateway.immutable();
            this.name = name;
            this.rank = rank;
            this.guardianPos = gateway;
        }

        public UUID id() {
            return id;
        }

        public BlockPos gateway() {
            return gateway;
        }

        public String name() {
            return name;
        }

        public CreatureRank rank() {
            return rank;
        }

        public Hold hold() {
            return hold;
        }

        public Optional<UUID> clan() {
            return Optional.ofNullable(clan);
        }

        public Optional<UUID> guardian() {
            return Optional.ofNullable(guardian);
        }

        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("id", id);
            tag.put("gateway", NbtUtils.writeBlockPos(gateway));
            tag.putString("name", name);
            tag.putString("rank", rank.getSerializedName());
            tag.putString("hold", hold.name());
            if (guardian != null) {
                tag.putUUID("guardian", guardian);
            }
            tag.put("guardian_pos", NbtUtils.writeBlockPos(guardianPos));
            if (conqueror != null) {
                tag.putUUID("conqueror", conqueror);
            }
            if (conquerorClan != null) {
                tag.putUUID("conqueror_clan", conquerorClan);
            }
            tag.putLong("conquered_at", conqueredAt);
            if (clan != null) {
                tag.putUUID("clan", clan);
            }
            return tag;
        }

        static Citadel load(CompoundTag tag) {
            BlockPos gateway = NbtUtils.readBlockPos(tag, "gateway").orElse(BlockPos.ZERO);
            CreatureRank rank = Optional.ofNullable(tag.get("rank")).flatMap(saved -> CreatureRank.CODEC.parse(NbtOps.INSTANCE, saved).result())
                    .orElse(CreatureRank.DORMANT);
            Citadel citadel = new Citadel(tag.getUUID("id"), gateway, tag.getString("name"), rank);
            try {
                citadel.hold = Hold.valueOf(tag.getString("hold"));
            } catch (IllegalArgumentException exception) {
                citadel.hold = Hold.GUARDIAN;
            }
            citadel.guardian = tag.hasUUID("guardian") ? tag.getUUID("guardian") : null;
            citadel.guardianPos = NbtUtils.readBlockPos(tag, "guardian_pos").orElse(gateway);
            citadel.conqueror = tag.hasUUID("conqueror") ? tag.getUUID("conqueror") : null;
            citadel.conquerorClan = tag.hasUUID("conqueror_clan") ? tag.getUUID("conqueror_clan") : null;
            citadel.conqueredAt = tag.getLong("conquered_at");
            citadel.clan = tag.hasUUID("clan") ? tag.getUUID("clan") : null;
            return citadel;
        }
    }

    public static CitadelData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new Factory<>(CitadelData::new, CitadelData::load), NAME);
    }

    private static CitadelData load(CompoundTag tag, HolderLookup.Provider registries) {
        CitadelData data = new CitadelData();
        for (Tag citadel : tag.getList("citadels", Tag.TAG_COMPOUND)) {
            data.citadels.add(Citadel.load((CompoundTag) citadel));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        citadels.forEach(citadel -> list.add(citadel.save()));
        tag.put("citadels", list);
        return tag;
    }

    public List<Citadel> citadels() {
        return List.copyOf(citadels);
    }

    public Optional<Citadel> at(BlockPos gateway) {
        return citadels.stream().filter(citadel -> citadel.gateway.equals(gateway)).findFirst();
    }

    public Optional<Citadel> byId(UUID id) {
        return citadels.stream().filter(citadel -> citadel.id.equals(id)).findFirst();
    }
}
