package dev.spud.shadowslave.dreamrealm.seed;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** The Nightmare Seeds growing in the Dream Realm, saved with the world so they grow while unloaded. */
public final class SeedData extends SavedData {
    private static final String NAME = "shadowslave_dream_seeds";
    /** Growth is counted in hundredths of a tick, so a growth speed in percent adds whole numbers. */
    public static final long POINTS_PER_TICK = 100;

    final List<Seed> seeds = new ArrayList<>();

    /** One Seed: where it grows, which region it took its Category from, how far it has grown, and who it grew near. */
    public static final class Seed {
        final UUID id;
        final BlockPos pos;
        final ResourceLocation region;
        long growth;
        UUID owner;
        /** The Gate a player opened to cleanse it, while that fight lasts. */
        UUID cleansing;
        int cleansingStage;
        /** The echo site this Seed grew at, if any: its Category and Nightmare come from there. */
        ResourceLocation echoSite;

        Seed(UUID id, BlockPos pos, ResourceLocation region) {
            this.id = id;
            this.pos = pos.immutable();
            this.region = region;
        }

        public UUID id() {
            return id;
        }

        public BlockPos pos() {
            return pos;
        }

        public ResourceLocation region() {
            return region;
        }

        /** Ticks grown at normal speed. */
        public long grownTicks() {
            return growth / POINTS_PER_TICK;
        }

        public Optional<UUID> owner() {
            return Optional.ofNullable(owner);
        }

        public boolean cleansing() {
            return cleansing != null;
        }

        public Optional<ResourceLocation> echoSite() {
            return Optional.ofNullable(echoSite);
        }

        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("id", id);
            tag.put("pos", NbtUtils.writeBlockPos(pos));
            tag.putString("region", region.toString());
            tag.putLong("growth", growth);
            if (owner != null) {
                tag.putUUID("owner", owner);
            }
            if (cleansing != null) {
                tag.putUUID("cleansing", cleansing);
                tag.putInt("cleansing_stage", cleansingStage);
            }
            if (echoSite != null) {
                tag.putString("echo_site", echoSite.toString());
            }
            return tag;
        }

        static Seed load(CompoundTag tag) {
            Seed seed = new Seed(tag.getUUID("id"), NbtUtils.readBlockPos(tag, "pos").orElse(BlockPos.ZERO),
                    ResourceLocation.parse(tag.getString("region")));
            seed.growth = tag.getLong("growth");
            seed.owner = tag.hasUUID("owner") ? tag.getUUID("owner") : null;
            seed.cleansing = tag.hasUUID("cleansing") ? tag.getUUID("cleansing") : null;
            seed.cleansingStage = tag.getInt("cleansing_stage");
            seed.echoSite = tag.contains("echo_site") ? ResourceLocation.tryParse(tag.getString("echo_site")) : null;
            return seed;
        }
    }

    public static SeedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new Factory<>(SeedData::new, SeedData::load), NAME);
    }

    private static SeedData load(CompoundTag tag, HolderLookup.Provider registries) {
        SeedData data = new SeedData();
        for (Tag seed : tag.getList("seeds", Tag.TAG_COMPOUND)) {
            data.seeds.add(Seed.load((CompoundTag) seed));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        seeds.forEach(seed -> list.add(seed.save()));
        tag.put("seeds", list);
        return tag;
    }

    public List<Seed> seeds() {
        return List.copyOf(seeds);
    }

    public Optional<Seed> at(BlockPos pos) {
        return seeds.stream().filter(seed -> seed.pos.equals(pos)).findFirst();
    }

    public Optional<Seed> byId(UUID id) {
        return seeds.stream().filter(seed -> seed.id.equals(id)).findFirst();
    }
}
