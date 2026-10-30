package dev.spud.shadowslave.dreamrealm.seed;

import dev.spud.shadowslave.citadel.CitadelRaids;
import dev.spud.shadowslave.dreamrealm.DreamCrossing;
import dev.spud.shadowslave.dreamrealm.DreamRealm;
import dev.spud.shadowslave.dreamrealm.DreamRealmContent;
import dev.spud.shadowslave.dreamrealm.DreamRealmRegistry;
import dev.spud.shadowslave.dreamrealm.DreamRealmRules;
import dev.spud.shadowslave.dreamrealm.DreamRegion;
import dev.spud.shadowslave.dreamrealm.DreamSeedSettings;
import dev.spud.shadowslave.gate.GateEvent;
import dev.spud.shadowslave.gate.GateForecast;
import dev.spud.shadowslave.gate.GateService;
import dev.spud.shadowslave.gate.GateTrigger;
import dev.spud.shadowslave.trial.TrialService;
import dev.spud.shadowslave.trial.content.EchoSite;
import dev.spud.shadowslave.trial.content.TrialContent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Natural Nightmare Seeds (DESIGN, Jack 2026-10-23; after ch. 458-459, where a
 * Seed blooms into a Gate and is far better challenged before it does).
 * Seeds take root near Awakened players in the Dream Realm and grow over
 * in-game days. A player can cleanse one early by fighting at the Seed, for
 * loot that is richer the younger the Seed. A Seed left alone blooms: its Gate
 * opens through {@link GateService#open} at the matching Overworld spot,
 * unless a lit Dream Candle there wards it, and then the Seed withers. A Seed
 * that blooms near a claimed Citadel raids it instead ({@link CitadelRaids}).
 */
public final class SeedService {
    /** How often Seeds grow and are checked for blooming. */
    static final int GROW_INTERVAL = 20;
    /** A Seed whose Gate could not open (another Gate too close) tries again this much later. */
    static final long RETRY_TICKS = 60 * 20;

    private SeedService() {
    }

    public static void bootstrap() {
        GateService.addCloseListener(SeedService::onGateClosed);
    }

    public static Optional<ServerLevel> realm(MinecraftServer server) {
        return Optional.ofNullable(DreamRealm.level(server));
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        Optional<ServerLevel> realm = realm(server);
        if (realm.isEmpty() || !DreamRealmRules.seeds(server)) {
            return;
        }
        DreamSeedSettings settings = DreamRealmContent.get().seeds();
        if (server.getTickCount() % GROW_INTERVAL == 0) {
            grow(realm.get(), (long) GROW_INTERVAL * DreamRealmRules.seedGrowthPercent(server));
        }
        if (server.getTickCount() % settings.spawnInterval() == 0) {
            for (ServerPlayer player : List.copyOf(realm.get().players())) {
                if (wantsSeeds(player) && player.getRandom().nextFloat() < settings.spawnChance()) {
                    trySpawnNear(realm.get(), player);
                }
            }
        }
    }

    /** Awakened players who are not spectating, and, if the server makes Seeds opt-in, who chose them. */
    public static boolean wantsSeeds(ServerPlayer player) {
        return !player.isSpectator() && DreamCrossing.canDream(player)
                && (!DreamRealmRules.seedsOptIn(player.server) || DreamCrossing.data(player).seedsOptIn());
    }

    // ---- taking root ---------------------------------------------------------------------------------------------

    /** Tries to plant a Seed in loaded ground near a player; returns it if one took root. */
    public static Optional<SeedData.Seed> trySpawnNear(ServerLevel realm, ServerPlayer player) {
        DreamSeedSettings settings = DreamRealmContent.get().seeds();
        SeedData data = SeedData.get(realm);
        BlockPos at = player.blockPosition();
        long near = data.seeds.stream().filter(seed -> seed.pos.distSqr(at) < sq(settings.nearRange())).count();
        if (near >= settings.maxNear()) {
            return Optional.empty();
        }
        RandomSource random = player.getRandom();
        for (int attempt = 0; attempt < 8; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double distance = settings.minDistance() + random.nextDouble() * Math.max(0, settings.maxDistance() - settings.minDistance());
            int x = (int) Math.floor(at.getX() + Math.cos(angle) * distance);
            int z = (int) Math.floor(at.getZ() + Math.sin(angle) * distance);
            if (!realm.hasChunk(x >> 4, z >> 4)) {
                continue;
            }
            BlockPos pos = new BlockPos(x, realm.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
            Optional<SeedData.Seed> seed = plant(realm, pos, player);
            if (seed.isPresent()) {
                player.displayClientMessage(Component.literal("Something takes root in the dream nearby.")
                        .withStyle(ChatFormatting.DARK_PURPLE), true);
                realm.playSound(null, pos, SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.HOSTILE, 0.6F, 0.5F);
                return seed;
            }
        }
        return Optional.empty();
    }

    /**
     * Plants a Seed at {@code pos} if the ground holds it, a region covers it
     * and no other Seed grows within the spacing. Operators can call it directly.
     */
    public static Optional<SeedData.Seed> plant(ServerLevel realm, BlockPos pos, ServerPlayer near) {
        DreamSeedSettings settings = DreamRealmContent.get().seeds();
        SeedData data = SeedData.get(realm);
        BlockPos below = pos.below();
        if (!realm.getBlockState(pos).canBeReplaced() || !realm.getFluidState(pos).isEmpty()
                || !realm.getBlockState(below).isFaceSturdy(realm, below, Direction.UP)) {
            return Optional.empty();
        }
        if (data.seeds.stream().anyMatch(seed -> seed.pos.distSqr(pos) < sq(settings.spacing()))) {
            return Optional.empty();
        }
        Optional<Map.Entry<ResourceLocation, DreamRegion>> region = DreamRealmContent.get().regionAt(pos);
        if (region.isEmpty()) {
            return Optional.empty();
        }
        SeedData.Seed seed = new SeedData.Seed(UUID.randomUUID(), pos, region.get().getKey());
        seed.owner = near == null ? null : near.getUUID();
        data.seeds.add(seed);
        data.setDirty();
        realm.setBlockAndUpdate(pos, DreamRealmRegistry.NIGHTMARE_SEED.get().defaultBlockState());
        realm.sendParticles(ParticleTypes.REVERSE_PORTAL, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 40, 0.3, 0.4, 0.3, 0.05);
        return Optional.of(seed);
    }

    /**
     * Plants a Seed at an echo site, whatever the spacing to other Seeds: the
     * site's weight sets its Category and its Nightmare waits inside. Returns
     * empty if the ground cannot hold it or no region covers it.
     */
    public static Optional<SeedData.Seed> plantAtEcho(ServerLevel realm, BlockPos pos, ResourceLocation site) {
        SeedData data = SeedData.get(realm);
        BlockPos below = pos.below();
        if (!realm.getBlockState(pos).canBeReplaced() || !realm.getFluidState(pos).isEmpty()
                || !realm.getBlockState(below).isFaceSturdy(realm, below, Direction.UP)) {
            return Optional.empty();
        }
        Optional<Map.Entry<ResourceLocation, DreamRegion>> region = DreamRealmContent.get().regionAt(pos);
        if (region.isEmpty()) {
            return Optional.empty();
        }
        SeedData.Seed seed = new SeedData.Seed(UUID.randomUUID(), pos, region.get().getKey());
        seed.echoSite = site;
        data.seeds.add(seed);
        data.setDirty();
        realm.setBlockAndUpdate(pos, DreamRealmRegistry.NIGHTMARE_SEED.get().defaultBlockState());
        realm.sendParticles(ParticleTypes.REVERSE_PORTAL, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 40, 0.3, 0.4, 0.3, 0.05);
        return Optional.of(seed);
    }

    /** Removes a Seed for good, as when its Nightmare is conquered. Returns false if it was already gone. */
    public static boolean spend(ServerLevel realm, UUID id) {
        SeedData data = SeedData.get(realm);
        Optional<SeedData.Seed> seed = data.byId(id);
        if (seed.isEmpty()) {
            return false;
        }
        data.seeds.remove(seed.get());
        data.setDirty();
        if (realm.getBlockState(seed.get().pos).is(DreamRealmRegistry.NIGHTMARE_SEED.get())) {
            realm.removeBlock(seed.get().pos, false);
        }
        realm.sendParticles(ParticleTypes.END_ROD, seed.get().pos.getX() + 0.5, seed.get().pos.getY() + 0.5,
                seed.get().pos.getZ() + 0.5, 40, 0.4, 0.6, 0.4, 0.06);
        return true;
    }

    // ---- growing and blooming ------------------------------------------------------------------------------------

    /** Grows every Seed by {@code points} (hundredths of a tick) and blooms the ripe ones. */
    public static void grow(ServerLevel realm, long points) {
        DreamSeedSettings settings = DreamRealmContent.get().seeds();
        SeedData data = SeedData.get(realm);
        if (data.seeds.isEmpty()) {
            return;
        }
        Iterator<SeedData.Seed> seeds = data.seeds.iterator();
        while (seeds.hasNext()) {
            SeedData.Seed seed = seeds.next();
            if (seed.cleansing()) {
                continue;
            }
            if (realm.isLoaded(seed.pos) && !realm.getBlockState(seed.pos).is(DreamRealmRegistry.NIGHTMARE_SEED.get())) {
                // Torn out by an operator or a creative player.
                seeds.remove();
                continue;
            }
            if (TrialService.waking(seed.id)) {
                // A Seed holds still while its Nightmare runs, however long that is.
                continue;
            }
            seed.growth += points;
            showStage(realm, seed, settings);
            if (seed.grownTicks() >= settings.growthTicks() && bloom(realm, seed)) {
                seeds.remove();
            }
        }
        data.setDirty();
    }

    /** Puts the Seed block in step with its growth, if its chunk is loaded. */
    static void showStage(ServerLevel realm, SeedData.Seed seed, DreamSeedSettings settings) {
        if (!realm.isLoaded(seed.pos)) {
            return;
        }
        BlockState state = realm.getBlockState(seed.pos);
        int stage = settings.stage(seed.grownTicks());
        if (state.is(DreamRealmRegistry.NIGHTMARE_SEED.get()) && state.getValue(NightmareSeedBlock.STAGE) != stage) {
            realm.setBlockAndUpdate(seed.pos, state.setValue(NightmareSeedBlock.STAGE, stage));
            realm.playSound(null, seed.pos, SoundEvents.AMETHYST_CLUSTER_PLACE, SoundSource.BLOCKS, 1.0F, 0.5F);
        }
    }

    /**
     * Blooms a ripe Seed: its Gate opens at the matching Overworld spot unless
     * a lit Dream Candle wards it. Returns true once the Seed is spent; false
     * if the Gate could not open yet and the Seed should try again later.
     */
    public static boolean bloom(ServerLevel realm, SeedData.Seed seed) {
        MinecraftServer server = realm.getServer();
        DreamSeedSettings seeds = DreamRealmContent.get().seeds();
        // A Seed blooming at the walls of a claimed Citadel raids it instead (Run C).
        Optional<Boolean> raid = CitadelRaids.bloomNear(realm, seed, forecast(seed));
        if (raid.isPresent()) {
            if (!raid.get()) {
                seed.growth = (seeds.growthTicks() - RETRY_TICKS) * SeedData.POINTS_PER_TICK;
                return false;
            }
            realm.removeBlock(seed.pos, false);
            realm.playSound(null, seed.pos, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.0F, 1.4F);
            realm.sendParticles(ParticleTypes.SCULK_SOUL, seed.pos.getX() + 0.5, seed.pos.getY() + 0.8, seed.pos.getZ() + 0.5,
                    30, 0.4, 0.6, 0.4, 0.08);
            return true;
        }
        ServerLevel overworld = server.overworld();
        BlockPos column = DreamRealm.toOverworld(seed.pos, DreamRealmRules.ratio(server));
        overworld.getChunk(column);
        int y = overworld.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column.getX(), column.getZ());
        BlockPos center = new BlockPos(column.getX(), y - 1, column.getZ());
        Optional<ServerPlayer> owner = seed.owner().map(uuid -> server.getPlayerList().getPlayer(uuid));
        DreamSeedSettings settings = DreamRealmContent.get().seeds();

        Optional<BlockPos> ward = wardingCandle(overworld, center, settings.wardRadius());
        if (ward.isPresent()) {
            wither(realm, seed);
            owner.ifPresent(player -> DreamCrossing.spell(player, Component.literal(
                    "A Nightmare Seed has withered. A Dream Candle near " + ward.get().getX() + ", " + ward.get().getZ()
                            + " warded the waking world.").withStyle(ChatFormatting.LIGHT_PURPLE)));
            return true;
        }
        Optional<GateEvent> gate = GateService.open(overworld, center, GateTrigger.SEED_BLOOM, forecast(seed), seed.id);
        if (gate.isEmpty()) {
            seed.growth = (settings.growthTicks() - RETRY_TICKS) * SeedData.POINTS_PER_TICK;
            return false;
        }
        realm.removeBlock(seed.pos, false);
        realm.playSound(null, seed.pos, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.0F, 1.4F);
        realm.sendParticles(ParticleTypes.SCULK_SOUL, seed.pos.getX() + 0.5, seed.pos.getY() + 0.8, seed.pos.getZ() + 0.5,
                30, 0.4, 0.6, 0.4, 0.08);
        for (ServerPlayer player : realm.players()) {
            if (player.blockPosition().distSqr(seed.pos) < sq(96)) {
                DreamCrossing.spell(player, Component.literal("A Nightmare Seed has bloomed. Its Gate opens in the waking world.")
                        .withStyle(ChatFormatting.DARK_RED));
            }
        }
        owner.filter(player -> !DreamRealm.is(player.level()) || player.blockPosition().distSqr(seed.pos) >= sq(96))
                .ifPresent(player -> DreamCrossing.spell(player, Component.literal("A Nightmare Seed you left has bloomed. A Gate is opening near "
                        + center.getX() + ", " + center.getZ() + ".").withStyle(ChatFormatting.DARK_RED)));
        return true;
    }

    /** The nearest lit Dream Candle within {@code radius} blocks across of an Overworld spot, at any height. */
    public static Optional<BlockPos> wardingCandle(ServerLevel level, BlockPos center, int radius) {
        if (radius <= 0) {
            return Optional.empty();
        }
        PoiManager pois = level.getPoiManager();
        pois.ensureLoadedAndValid(level, center, radius);
        ResourceKey<net.minecraft.world.entity.ai.village.poi.PoiType> key = DreamRealmRegistry.LIT_DREAM_CANDLE.getKey();
        // A column, not a sphere: a candle in a cellar under the spot wards it too.
        return pois.getInSquare(type -> type.is(key), center, radius, PoiManager.Occupancy.ANY)
                .map(net.minecraft.world.entity.ai.village.poi.PoiRecord::getPos)
                .filter(pos -> horizontalSq(pos, center) <= sq(radius))
                .min(java.util.Comparator.comparingDouble(pos -> horizontalSq(pos, center)));
    }

    private static void wither(ServerLevel realm, SeedData.Seed seed) {
        realm.removeBlock(seed.pos, false);
        realm.playSound(null, seed.pos, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.BLOCKS, 1.0F, 0.4F);
        realm.sendParticles(ParticleTypes.ASH, seed.pos.getX() + 0.5, seed.pos.getY() + 0.5, seed.pos.getZ() + 0.5,
                40, 0.3, 0.4, 0.3, 0.02);
    }

    public static GateForecast forecast(SeedData.Seed seed) {
        Optional<Integer> echoCategory = seed.echoSite().flatMap(TrialContent.get()::site).map(TrialContent.get()::category);
        if (echoCategory.isPresent()) {
            return GateForecast.certain(echoCategory.get());
        }
        return DreamRealmContent.get().region(seed.region).map(DreamRegion::seedForecast).orElse(GateForecast.certain(1));
    }

    // ---- reading and challenging ---------------------------------------------------------------------------------

    /** What the Spell tells a player who touches a Seed. */
    public static void describe(ServerPlayer player, BlockPos pos) {
        Optional<SeedData.Seed> found = SeedData.get(player.serverLevel()).at(pos);
        if (found.isEmpty()) {
            return;
        }
        SeedData.Seed seed = found.get();
        DreamSeedSettings settings = DreamRealmContent.get().seeds();
        showStage(player.serverLevel(), seed, settings);
        String region = DreamRealmContent.get().region(seed.region).map(DreamRegion::displayName).orElse("the Dream Realm");
        long left = Math.max(0, settings.growthTicks() - seed.grownTicks());
        int percent = DreamRealmRules.seedGrowthPercent(player.server);
        String bloom = percent <= 0 ? "it is not growing"
                : "it blooms in about " + describeTime(left * 100 / percent);
        Optional<EchoSite> site = seed.echoSite().flatMap(TrialContent.get()::site);
        if (site.isPresent()) {
            DreamCrossing.spell(player, Component.literal("A Nightmare Seed of " + site.get().displayName() + ". Category "
                    + TrialContent.get().category(site.get()) + ".").withStyle(ChatFormatting.DARK_PURPLE));
            if (!site.get().lore().isEmpty()) {
                DreamCrossing.spell(player, Component.literal(site.get().lore()));
            }
            DreamCrossing.spell(player, Component.literal("Growth " + (settings.stage(seed.grownTicks()) + 1) + " of " + DreamSeedSettings.STAGES
                    + "; " + bloom + ". Sneak and use it to challenge its Nightmare."));
            return;
        }
        DreamCrossing.spell(player, Component.literal("A Nightmare Seed of " + region + ". " + forecast(seed).describeOdds() + ".")
                .withStyle(ChatFormatting.DARK_PURPLE));
        DreamCrossing.spell(player, Component.literal("Growth " + (settings.stage(seed.grownTicks()) + 1) + " of " + DreamSeedSettings.STAGES
                + "; " + bloom + ". Sneak and use it to challenge it now."));
    }

    static String describeTime(long ticks) {
        if (ticks >= 24000) {
            double days = ticks / 24000.0;
            return String.format(java.util.Locale.ROOT, "%.1f days", days);
        }
        long minutes = Math.max(1, ticks / 1200);
        return minutes + (minutes == 1 ? " minute" : " minutes");
    }

    /** Sneak-use on a Seed: the Seed wakes and its Gate opens here, in the Dream Realm. */
    public static boolean challenge(ServerPlayer player, BlockPos pos) {
        ServerLevel realm = player.serverLevel();
        SeedData data = SeedData.get(realm);
        Optional<SeedData.Seed> found = data.at(pos);
        if (found.isEmpty() || found.get().cleansing()) {
            return false;
        }
        if (!DreamCrossing.canDream(player)) {
            player.displayClientMessage(Component.literal("Only the Awakened can challenge a Nightmare Seed.")
                    .withStyle(ChatFormatting.GRAY), true);
            return false;
        }
        SeedData.Seed seed = found.get();
        if (seed.echoSite().isPresent()) {
            // A Seed at an echo site holds a hand-made Nightmare, not a Gate.
            return TrialService.challenge(player, realm, seed);
        }
        int stage = DreamRealmContent.get().seeds().stage(seed.grownTicks());
        Optional<GateEvent> gate = GateService.open(realm, pos.below(), GateTrigger.SEED_CLEANSE, forecast(seed), seed.id);
        if (gate.isEmpty()) {
            player.displayClientMessage(Component.literal("Another Gate is open too close by.").withStyle(ChatFormatting.GRAY), true);
            return false;
        }
        seed.cleansing = gate.get().id();
        seed.cleansingStage = stage;
        data.setDirty();
        realm.removeBlock(pos, false);
        DreamCrossing.spell(player, Component.literal("The Seed stirs. Survive its Gate and slay the Guardian to cleanse it.")
                .withStyle(ChatFormatting.DARK_PURPLE));
        return true;
    }

    /** A cleansing Gate's Guardian fell: the Seed is cleansed and pays out. If the Gate closed some other way, the Seed returns. */
    static void onGateClosed(ServerLevel level, GateEvent gate, boolean guardianSlain) {
        if (gate.trigger() != GateTrigger.SEED_CLEANSE || !DreamRealm.is(level)) {
            return;
        }
        SeedData data = SeedData.get(level);
        Optional<SeedData.Seed> found = gate.source().flatMap(data::byId);
        if (found.isEmpty()) {
            return;
        }
        SeedData.Seed seed = found.get();
        if (guardianSlain) {
            data.seeds.remove(seed);
            reward(level, seed);
        } else {
            seed.cleansing = null;
            level.setBlockAndUpdate(seed.pos, DreamRealmRegistry.NIGHTMARE_SEED.get().defaultBlockState()
                    .setValue(NightmareSeedBlock.STAGE, DreamRealmContent.get().seeds().stage(seed.grownTicks())));
        }
        data.setDirty();
    }

    /** The region's cleanse loot, rolled more times the younger the Seed was when it was challenged. */
    static void reward(ServerLevel realm, SeedData.Seed seed) {
        Vec3 at = Vec3.atCenterOf(seed.pos);
        for (ItemStack stack : rollReward(realm, seed)) {
            Containers.dropItemStack(realm, at.x, at.y, at.z, stack);
        }
        realm.playSound(null, seed.pos, SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.5F, 0.7F);
        realm.sendParticles(ParticleTypes.END_ROD, at.x, at.y, at.z, 40, 0.4, 0.6, 0.4, 0.06);
        for (ServerPlayer player : realm.players()) {
            if (player.blockPosition().distSqr(seed.pos) < sq(96)) {
                DreamCrossing.spell(player, Component.literal("The Nightmare Seed is cleansed. It will never bloom.")
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        }
    }

    static List<ItemStack> rollReward(ServerLevel realm, SeedData.Seed seed) {
        int rolls = DreamRealmContent.get().seeds().cleanseRolls(seed.cleansingStage);
        Optional<ResourceLocation> lootId = DreamRealmContent.get().region(seed.region).map(DreamRegion::cleanseLoot);
        List<ItemStack> items = new ArrayList<>();
        if (lootId.isPresent()) {
            LootTable table = realm.getServer().reloadableRegistries()
                    .getLootTable(ResourceKey.create(Registries.LOOT_TABLE, lootId.get()));
            for (int i = 0; i < rolls; i++) {
                LootParams params = new LootParams.Builder(realm).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(seed.pos))
                        .create(LootContextParamSets.CHEST);
                items.addAll(table.getRandomItems(params));
            }
        }
        return items;
    }

    /** The Seed nearest a spot in the Dream Realm. */
    public static Optional<SeedData.Seed> nearest(ServerLevel realm, BlockPos pos) {
        return SeedData.get(realm).seeds.stream().min((a, b) -> Double.compare(a.pos.distSqr(pos), b.pos.distSqr(pos)));
    }

    /** Adds growth to a Seed, for operators and tests. */
    public static void addGrowth(ServerLevel realm, SeedData.Seed seed, long ticks) {
        seed.growth += ticks * SeedData.POINTS_PER_TICK;
        showStage(realm, seed, DreamRealmContent.get().seeds());
        SeedData.get(realm).setDirty();
    }

    /** Blooms a Seed now, for operators and tests. Returns true if it bloomed or withered. */
    public static boolean bloomNow(ServerLevel realm, SeedData.Seed seed) {
        SeedData data = SeedData.get(realm);
        if (seed.cleansing() || !bloom(realm, seed)) {
            return false;
        }
        data.seeds.remove(seed);
        data.setDirty();
        return true;
    }

    /** Withers a Seed on the spot without opening its Gate, as a ward would. */
    public static void witherNow(ServerLevel realm, SeedData.Seed seed) {
        SeedData data = SeedData.get(realm);
        wither(realm, seed);
        data.seeds.remove(seed);
        data.setDirty();
    }

    private static double horizontalSq(BlockPos a, BlockPos b) {
        return sq(a.getX() - b.getX()) + sq(a.getZ() - b.getZ());
    }

    private static double sq(double value) {
        return value * value;
    }
}
