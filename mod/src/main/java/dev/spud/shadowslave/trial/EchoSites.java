package dev.spud.shadowslave.trial;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.dreamrealm.DreamCrossing;
import dev.spud.shadowslave.dreamrealm.DreamRealmRules;
import dev.spud.shadowslave.dreamrealm.seed.SeedData;
import dev.spud.shadowslave.dreamrealm.seed.SeedService;
import dev.spud.shadowslave.trial.content.EchoSite;
import dev.spud.shadowslave.trial.content.TrialContent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

/**
 * Echo sites in the Dream Realm (DESIGN, Jack 2026-10-29). The first time a
 * dreamer comes near one, what is left of the place today is raised there.
 * While no Seed grows at a site, each Seed check may plant one at it, so its
 * Nightmare can be challenged. A site whose Nightmare was just conquered rests
 * for a day before another Seed grows there.
 */
public final class EchoSites {
    /** How often dreamers near echo sites are checked. */
    static final int CHECK_INTERVAL = 100;
    /** How long a conquered site rests before a new Seed can grow at it. */
    static final long REST_TICKS = 24000L;
    /** Structure layers below a ruin's ground floor: its foundation. */
    static final int RUIN_FOUNDATION = 3;

    private EchoSites() {
    }

    /** Which sites have their ruins raised, and until when each rests. Saved with the Dream Realm. */
    static final class SiteData extends SavedData {
        private static final String NAME = "shadowslave_echo_sites";
        final Set<ResourceLocation> raised = new HashSet<>();
        final Map<ResourceLocation, Long> restUntil = new HashMap<>();

        static SiteData get(ServerLevel realm) {
            return realm.getDataStorage().computeIfAbsent(new Factory<>(SiteData::new, SiteData::load), NAME);
        }

        private static SiteData load(CompoundTag tag, HolderLookup.Provider registries) {
            SiteData data = new SiteData();
            CompoundTag raised = tag.getCompound("raised");
            raised.getAllKeys().forEach(key -> Optional.ofNullable(ResourceLocation.tryParse(key)).ifPresent(data.raised::add));
            CompoundTag rest = tag.getCompound("rest_until");
            for (String key : rest.getAllKeys()) {
                Optional.ofNullable(ResourceLocation.tryParse(key)).ifPresent(id -> data.restUntil.put(id, rest.getLong(key)));
            }
            return data;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            CompoundTag raised = new CompoundTag();
            this.raised.forEach(id -> raised.putBoolean(id.toString(), true));
            tag.put("raised", raised);
            CompoundTag rest = new CompoundTag();
            restUntil.forEach((id, until) -> rest.putLong(id.toString(), until));
            tag.put("rest_until", rest);
            return tag;
        }
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % CHECK_INTERVAL != 0 || TrialContent.get().sites().isEmpty()) {
            return;
        }
        Optional<ServerLevel> realm = SeedService.realm(server);
        if (realm.isEmpty()) {
            return;
        }
        int range = TrialContent.get().settings().siteNoticeRange();
        float chance = TrialContent.get().settings().siteSeedChance()
                * CHECK_INTERVAL / Math.max(1.0F, dev.spud.shadowslave.dreamrealm.DreamRealmContent.get().seeds().spawnInterval());
        for (Entry<ResourceLocation, EchoSite> site : TrialContent.get().sites().entrySet()) {
            List<ServerPlayer> near = realm.get().players().stream()
                    .filter(player -> !player.isSpectator() && horizontalSq(player.blockPosition(), center(site.getValue()))
                            <= (double) range * range)
                    .toList();
            if (near.isEmpty()) {
                continue;
            }
            raise(realm.get(), site.getKey(), site.getValue());
            boolean wanted = DreamRealmRules.seeds(server) && near.stream().anyMatch(SeedService::wantsSeeds);
            if (wanted && realm.get().getRandom().nextFloat() < chance) {
                grow(realm.get(), site.getKey(), site.getValue()).ifPresent(seed -> near.forEach(player ->
                        DreamCrossing.spell(player, Component.literal("Something stirs among the ruins of "
                                + site.getValue().displayName() + ".").withStyle(ChatFormatting.DARK_PURPLE))));
            }
        }
    }

    static BlockPos center(EchoSite site) {
        return new BlockPos(site.x(), 0, site.z());
    }

    /** Raises a site's ruin, once. Returns true if it was raised now. */
    public static boolean raise(ServerLevel realm, ResourceLocation id, EchoSite site) {
        SiteData data = SiteData.get(realm);
        if (data.raised.contains(id)) {
            return false;
        }
        data.raised.add(id);
        data.setDirty();
        if (site.ruin().isEmpty()) {
            return true;
        }
        Optional<StructureTemplate> template = realm.getServer().getStructureManager().get(site.ruin().get());
        if (template.isEmpty()) {
            ShadowSlaveMod.LOGGER.error("Echo site {} names a missing ruin {}", id, site.ruin().get());
            return true;
        }
        var size = template.get().getSize();
        realm.getChunk(site.x() >> 4, site.z() >> 4);
        int ground = realm.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, site.x(), site.z());
        BlockPos origin = new BlockPos(site.x() - size.getX() / 2, ground - 1 - RUIN_FOUNDATION, site.z() - size.getZ() / 2);
        template.get().placeInWorld(realm, origin, origin, new StructurePlaceSettings(), realm.getRandom(), Block.UPDATE_CLIENTS);
        return true;
    }

    /**
     * Grows a Seed at a site if none grows there and it is not resting.
     * Operators can call it directly.
     */
    public static Optional<SeedData.Seed> grow(ServerLevel realm, ResourceLocation id, EchoSite site) {
        SiteData data = SiteData.get(realm);
        if (data.restUntil.getOrDefault(id, 0L) > realm.getGameTime() || seedAt(realm, id).isPresent()) {
            return Optional.empty();
        }
        int x = site.x() + site.seedOffsetX();
        int z = site.z() + site.seedOffsetZ();
        realm.getChunk(x >> 4, z >> 4);
        BlockPos pos = new BlockPos(x, realm.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
        return SeedService.plantAtEcho(realm, pos, id);
    }

    public static Optional<SeedData.Seed> seedAt(ServerLevel realm, ResourceLocation site) {
        return SeedData.get(realm).seeds().stream().filter(seed -> seed.echoSite().filter(site::equals).isPresent()).findFirst();
    }

    /** A site whose Nightmare was conquered rests before another Seed grows there. */
    static void rest(MinecraftServer server, ResourceLocation site) {
        SeedService.realm(server).ifPresent(realm -> {
            SiteData data = SiteData.get(realm);
            data.restUntil.put(site, realm.getGameTime() + REST_TICKS);
            data.setDirty();
        });
    }

    /** Lets a site grow a Seed again at once; for operators and tests. */
    public static void wake(ServerLevel realm, ResourceLocation site) {
        SiteData data = SiteData.get(realm);
        data.restUntil.remove(site);
        data.setDirty();
    }

    private static double horizontalSq(BlockPos a, BlockPos b) {
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        return dx * dx + dz * dz;
    }
}
