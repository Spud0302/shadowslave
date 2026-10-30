package dev.spud.shadowslave.dreamrealm;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.creature.NightmareCreature;
import dev.spud.shadowslave.dreamrealm.seed.SeedService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Nightmare Creatures roaming the Dream Realm (DESIGN; the lore doc's
 * "Deeper regions" hold creatures to match their Seeds). Around each dreamer
 * standing in a region that lists {@code creatures}, a group may come out of
 * the dark now and then, as {@link RoamingSettings} tunes it. Roamers are
 * marked so they can be let go: one with no dreamer near it fades.
 */
public final class DreamRealmCreatures {
    static final String ROAMER_TAG = "shadowslave_roamer";
    /** A roamer this far from every dreamer fades. */
    static final double FADE_RANGE = 128.0;

    private DreamRealmCreatures() {
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        RoamingSettings settings = DreamRealmContent.get().roaming();
        if (server.getTickCount() % settings.interval() != 0) {
            return;
        }
        ServerLevel realm = DreamRealm.level(server);
        if (realm == null) {
            return;
        }
        fadeStrays(realm);
        if (!DreamRealmRules.creatures(server)) {
            return;
        }
        for (ServerPlayer player : List.copyOf(realm.players())) {
            if (player.isSpectator() || realm.getRandom().nextFloat() >= settings.chance()) {
                continue;
            }
            Optional<Map.Entry<ResourceLocation, DreamRegion>> region = DreamRealmContent.get().regionAt(player.blockPosition());
            if (region.isEmpty() || region.get().getValue().creatures().isEmpty() || crowded(realm, player, settings)) {
                continue;
            }
            roam(realm, player.blockPosition(), region.get().getValue(), settings, realm.getRandom());
        }
    }

    private static boolean crowded(ServerLevel realm, ServerPlayer player, RoamingSettings settings) {
        return realm.getEntitiesOfClass(NightmareCreature.class, player.getBoundingBox().inflate(settings.capRange()),
                Entity::isAlive).size() >= settings.cap();
    }

    /** Sends one group of a region's creatures out near a spot. Returns what came; empty if nowhere fit. */
    public static List<Mob> roam(ServerLevel realm, BlockPos near, DreamRegion region, RoamingSettings settings, RandomSource random) {
        Optional<DreamRegion.Roamer> roamer = pick(region.creatures(), random);
        if (roamer.isEmpty()) {
            return List.of();
        }
        Optional<BlockPos> at = findGround(realm, near, settings, random);
        if (at.isEmpty()) {
            return List.of();
        }
        int count = roamer.get().min() + random.nextInt(roamer.get().max() - roamer.get().min() + 1);
        List<Mob> spawned = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            spawn(realm, at.get().offset(random.nextInt(5) - 2, 0, random.nextInt(5) - 2), roamer.get()).ifPresent(spawned::add);
        }
        return spawned;
    }

    static Optional<DreamRegion.Roamer> pick(List<DreamRegion.Roamer> roamers, RandomSource random) {
        int total = roamers.stream().mapToInt(DreamRegion.Roamer::weight).sum();
        if (total <= 0) {
            return Optional.empty();
        }
        int roll = random.nextInt(total);
        for (DreamRegion.Roamer roamer : roamers) {
            roll -= roamer.weight();
            if (roll < 0) {
                return Optional.of(roamer);
            }
        }
        return Optional.empty();
    }

    private static Optional<BlockPos> findGround(ServerLevel realm, BlockPos near, RoamingSettings settings, RandomSource random) {
        for (int attempt = 0; attempt < 8; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2;
            int distance = settings.minDistance() + random.nextInt(Math.max(1, settings.maxDistance() - settings.minDistance() + 1));
            int x = near.getX() + (int) Math.round(Math.cos(angle) * distance);
            int z = near.getZ() + (int) Math.round(Math.sin(angle) * distance);
            if (!realm.hasChunk(x >> 4, z >> 4)) {
                continue;
            }
            BlockPos top = new BlockPos(x, realm.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
            BlockState ground = realm.getBlockState(top.below());
            if (!ground.isFaceSturdy(realm, top.below(), net.minecraft.core.Direction.UP) || !ground.getFluidState().isEmpty()) {
                continue;
            }
            if (SeedService.wardingCandle(realm, top, settings.candleWard()).isPresent()) {
                continue;
            }
            return Optional.of(top);
        }
        return Optional.empty();
    }

    private static Optional<Mob> spawn(ServerLevel realm, BlockPos at, DreamRegion.Roamer roamer) {
        Optional<EntityType<?>> type = BuiltInRegistries.ENTITY_TYPE.getOptional(roamer.entity());
        if (type.isEmpty() || !(type.get().create(realm) instanceof Mob mob)) {
            ShadowSlaveMod.LOGGER.error("A Dream Realm region roams {}, which is not a mob", roamer.entity());
            return Optional.empty();
        }
        mob.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, realm.getRandom().nextFloat() * 360.0F, 0.0F);
        mob.finalizeSpawn(realm, realm.getCurrentDifficultyAt(at), MobSpawnType.NATURAL, null);
        if (mob instanceof NightmareCreature creature) {
            creature.setRankAndClass(roamer.rank().orElse(creature.nativeRank()), roamer.creatureClass().orElse(creature.nativeClass()));
        }
        mob.getPersistentData().putBoolean(ROAMER_TAG, true);
        realm.addFreshEntity(mob);
        return Optional.of(mob);
    }

    /** Roamers with no dreamer near them fade back into the dark. */
    private static void fadeStrays(ServerLevel realm) {
        List<Entity> strays = new ArrayList<>();
        for (Entity entity : realm.getAllEntities()) {
            if (entity instanceof Mob mob && mob.getPersistentData().getBoolean(ROAMER_TAG)
                    && realm.getNearestPlayer(mob, FADE_RANGE) == null) {
                strays.add(mob);
            }
        }
        strays.forEach(Entity::discard);
    }
}
