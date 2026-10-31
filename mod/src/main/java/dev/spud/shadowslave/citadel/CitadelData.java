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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Every Citadel the Dream Realm has woken, saved with it, keyed by its Gateway. */
public final class CitadelData extends SavedData {
    private static final String NAME = "shadowslave_citadels";

    final List<Citadel> citadels = new ArrayList<>();

    /** Who holds a Citadel: its Guardian, nobody (conquered, the Gateway open), a player clan, or an NPC clan. */
    public enum Hold {
        GUARDIAN, CONQUERED, CLAN, NPC;

        GatewayBlock.State gateway() {
            return switch (this) {
                case GUARDIAN -> GatewayBlock.State.SEALED;
                case CONQUERED -> GatewayBlock.State.OPEN;
                case CLAN, NPC -> GatewayBlock.State.CLAIMED;
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
        /** What the Gateway has left to eat (Run C upkeep). */
        double provisions;
        /** How long it has had nothing to eat. */
        long hungryTicks;
        /** The stage of neglect last announced to the clan. */
        Neglect stage = Neglect.FED;
        /** Built tier of each upgrade, by id. */
        final Map<String, Integer> tiers = new LinkedHashMap<>();
        /** Upgrades a lost raid damaged; each works a tier lower until repaired. */
        final Set<String> damaged = new LinkedHashSet<>();
        /** Members anchored to this Gateway, as far as it knows; the Hall limits how many. */
        final Set<UUID> anchored = new LinkedHashSet<>();
        /** The raid under way, if a Seed has bloomed at its walls. */
        Raid raid;
        /** Where its Link Anchor stands once its Gateway Links are built (Run I supply line). */
        BlockPos linkAnchor;
        /** Whether an enemy at war has cut the Link Anchor; the Gateway starves until it is mended. */
        boolean linkCut;
        /** The NPC clan holding it, while {@link Hold#NPC}. */
        UUID npcClan;
        /** Guards and residents it has, counted whether or not they stand in the world. */
        int guards;
        int residents;
        /** When the last lost guard or resident was replaced (game time). */
        long regrownAt;

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

        public double provisions() {
            return provisions;
        }

        public long hungryTicks() {
            return hungryTicks;
        }

        public int tier(String upgrade) {
            return tiers.getOrDefault(upgrade, 0);
        }

        public boolean damaged(String upgrade) {
            return damaged.contains(upgrade);
        }

        public Set<UUID> anchored() {
            return Set.copyOf(anchored);
        }

        public Optional<Raid> raid() {
            return Optional.ofNullable(raid);
        }

        public Optional<BlockPos> linkAnchor() {
            return Optional.ofNullable(linkAnchor);
        }

        public boolean linkCut() {
            return linkCut;
        }

        public Optional<UUID> npcClan() {
            return Optional.ofNullable(npcClan);
        }

        public int guards() {
            return guards;
        }

        public int residents() {
            return residents;
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
            tag.putDouble("provisions", provisions);
            tag.putLong("hungry_ticks", hungryTicks);
            tag.putString("stage", stage.getSerializedName());
            CompoundTag built = new CompoundTag();
            tiers.forEach(built::putInt);
            tag.put("tiers", built);
            ListTag broken = new ListTag();
            damaged.forEach(id -> broken.add(net.minecraft.nbt.StringTag.valueOf(id)));
            tag.put("damaged", broken);
            ListTag anchors = new ListTag();
            anchored.forEach(id -> anchors.add(NbtUtils.createUUID(id)));
            tag.put("anchored", anchors);
            if (raid != null) {
                tag.put("raid", raid.save());
            }
            if (linkAnchor != null) {
                tag.put("link_anchor", NbtUtils.writeBlockPos(linkAnchor));
            }
            tag.putBoolean("link_cut", linkCut);
            if (npcClan != null) {
                tag.putUUID("npc_clan", npcClan);
            }
            tag.putInt("guards", guards);
            tag.putInt("residents", residents);
            tag.putLong("regrown_at", regrownAt);
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
            citadel.provisions = tag.getDouble("provisions");
            citadel.hungryTicks = tag.getLong("hungry_ticks");
            for (Neglect stage : Neglect.values()) {
                if (stage.getSerializedName().equals(tag.getString("stage"))) {
                    citadel.stage = stage;
                }
            }
            CompoundTag built = tag.getCompound("tiers");
            for (String id : built.getAllKeys()) {
                citadel.tiers.put(id, built.getInt(id));
            }
            for (Tag id : tag.getList("damaged", Tag.TAG_STRING)) {
                citadel.damaged.add(id.getAsString());
            }
            for (Tag id : tag.getList("anchored", Tag.TAG_INT_ARRAY)) {
                citadel.anchored.add(NbtUtils.loadUUID(id));
            }
            citadel.raid = tag.contains("raid", Tag.TAG_COMPOUND) ? Raid.load(tag.getCompound("raid")) : null;
            citadel.linkAnchor = NbtUtils.readBlockPos(tag, "link_anchor").orElse(null);
            citadel.linkCut = tag.getBoolean("link_cut");
            citadel.npcClan = tag.hasUUID("npc_clan") ? tag.getUUID("npc_clan") : null;
            citadel.guards = tag.getInt("guards");
            citadel.residents = tag.getInt("residents");
            citadel.regrownAt = tag.getLong("regrown_at");
            return citadel;
        }
    }

    /**
     * A raid on a Citadel (DESIGN, Run C): the Gate a Seed opened at its walls,
     * when it began, how far the breach has filled, and every member who came
     * to defend it.
     */
    public static final class Raid {
        final UUID gate;
        final long startedAt;
        final int category;
        int breach;
        /** When a defender last stood near; a raid nobody answers for long is lost. */
        long lastDefended;
        /** Set once the raid is lost, so the closing Gate is not read as held. */
        boolean lost;
        /** Ticks since the echo last struck; not saved. */
        int echoCooldown;
        final Set<UUID> defenders = new HashSet<>();

        Raid(UUID gate, long startedAt, int category) {
            this.gate = gate;
            this.startedAt = startedAt;
            this.category = category;
            this.lastDefended = startedAt;
        }

        public UUID gate() {
            return gate;
        }

        public long startedAt() {
            return startedAt;
        }

        public int category() {
            return category;
        }

        public int breach() {
            return breach;
        }

        public Set<UUID> defenders() {
            return Set.copyOf(defenders);
        }

        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("gate", gate);
            tag.putLong("started_at", startedAt);
            tag.putInt("category", category);
            tag.putInt("breach", breach);
            tag.putBoolean("lost", lost);
            tag.putLong("last_defended", lastDefended);
            ListTag list = new ListTag();
            defenders.forEach(id -> list.add(NbtUtils.createUUID(id)));
            tag.put("defenders", list);
            return tag;
        }

        static Raid load(CompoundTag tag) {
            Raid raid = new Raid(tag.getUUID("gate"), tag.getLong("started_at"), tag.getInt("category"));
            raid.breach = tag.getInt("breach");
            raid.lost = tag.getBoolean("lost");
            raid.lastDefended = tag.contains("last_defended") ? tag.getLong("last_defended") : raid.startedAt;
            for (Tag id : tag.getList("defenders", Tag.TAG_INT_ARRAY)) {
                raid.defenders.add(NbtUtils.loadUUID(id));
            }
            return raid;
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
