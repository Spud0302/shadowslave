package dev.spud.shadowslave.fieldguide;

import dev.spud.shadowslave.ability.ForewarningService;
import dev.spud.shadowslave.citadel.CitadelService;
import dev.spud.shadowslave.creature.CreatureRank;
import dev.spud.shadowslave.creature.NightmareCreature;
import dev.spud.shadowslave.dreamrealm.DreamCrossing;
import dev.spud.shadowslave.dreamrealm.DreamRealm;
import dev.spud.shadowslave.dreamrealm.DreamRealmContent;
import dev.spud.shadowslave.dreamrealm.DreamRealmRules;
import dev.spud.shadowslave.dreamrealm.DreamRegion;
import dev.spud.shadowslave.dreamrealm.seed.SeedData;
import dev.spud.shadowslave.gate.GateData;
import dev.spud.shadowslave.gate.GateEvent;
import dev.spud.shadowslave.network.payload.FieldGuideMapPayload;
import dev.spud.shadowslave.network.payload.FieldGuidePinRequestPayload;
import dev.spud.shadowslave.network.payload.FieldGuideStatusPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * The server side of the Field Guide: it hands out the first guide, uncovers
 * the map around each player, counts spyglass sightings and kills, logs the
 * Gates and Seeds a player comes near, keeps the danger marks behind the
 * ratings, and sends each player what their guide shows.
 */
public final class FieldGuideService {
    private static final int EXPLORE_INTERVAL = 10;
    private static final int SIGHT_INTERVAL = 2;
    private static final int PLACE_INTERVAL = 20;
    private static final int STATUS_INTERVAL = 10;

    /** Per-player bookkeeping that need not survive a restart. */
    private static final class Tracking {
        String dimension = "";
        long chunk = Long.MIN_VALUE;
        UUID target;
        int held;
        boolean dirty = true;
    }

    private static final Map<UUID, Tracking> TRACKING = new HashMap<>();

    private FieldGuideService() {
    }

    public static FieldGuideData get(ServerPlayer player) {
        return player.getData(FieldGuideRegistry.FIELD_GUIDE);
    }

    private static Tracking tracking(ServerPlayer player) {
        return TRACKING.computeIfAbsent(player.getUUID(), key -> new Tracking());
    }

    private static FieldGuideSettings settings() {
        return FieldGuideContent.get().settings();
    }

    /** Explored chunks a guide remembers per dimension. */
    public static int maxChunks() {
        return settings().maxChunks();
    }

    public static String dimension(Level level) {
        return level.dimension().location().toString();
    }

    // Events

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            giveFirstGuide(player);
            TRACKING.remove(player.getUUID());
            sendWholeMap(player);
            explore(player, true);
            sendStatus(player, false);
        }
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        TRACKING.remove(event.getEntity().getUUID());
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isSpectator()) {
            return;
        }
        int tick = player.tickCount;
        if (tick % EXPLORE_INTERVAL == 0) {
            explore(player, false);
        }
        if (tick % SIGHT_INTERVAL == 0) {
            sight(player);
        }
        if (tick % PLACE_INTERVAL == 0) {
            logPlaces(player);
        }
        Tracking tracking = tracking(player);
        if (tracking.dirty && tick % STATUS_INTERVAL == 0) {
            sendStatus(player, false);
        }
    }

    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }
        Entity killer = event.getSource().getEntity();
        if (event.getEntity() instanceof NightmareCreature creature && killer instanceof ServerPlayer player) {
            recordKill(player, creature);
        }
        if (event.getEntity() instanceof ServerPlayer player && killer instanceof NightmareCreature creature) {
            recordDeath(player, creature);
        }
    }

    // First guide

    /** Hands a player their first guide, once, if the rules say so. */
    public static void giveFirstGuide(ServerPlayer player) {
        FieldGuideData data = get(player);
        if (data.given()) {
            return;
        }
        data.markGiven();
        if (!settings().giveOnFirstJoin()) {
            return;
        }
        ItemStack guide = new ItemStack(FieldGuideRegistry.FIELD_GUIDE_ITEM.get());
        if (!player.getInventory().add(guide)) {
            player.drop(guide, false);
        }
    }

    // Map

    /** Uncovers the chunks around the player; with {@code force}, even if they have not moved. */
    public static void explore(ServerPlayer player, boolean force) {
        ServerLevel level = player.serverLevel();
        String dimension = dimension(level);
        ChunkPos centre = player.chunkPosition();
        Tracking tracking = tracking(player);
        if (!force && dimension.equals(tracking.dimension) && centre.toLong() == tracking.chunk) {
            return;
        }
        tracking.dimension = dimension;
        tracking.chunk = centre.toLong();
        FieldGuideData data = get(player);
        int radius = settings().exploreRadius();
        List<Long> changed = new ArrayList<>();
        List<byte[]> colours = new ArrayList<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int cx = centre.x + dx;
                int cz = centre.z + dz;
                if (!level.hasChunk(cx, cz)) {
                    continue;
                }
                LevelChunk chunk = level.getChunk(cx, cz);
                byte[] sampled = MapSampler.sample(level, chunk);
                long key = ChunkPos.asLong(cx, cz);
                if (data.explore(dimension, key, sampled, settings().maxChunks())) {
                    changed.add(key);
                    colours.add(sampled);
                }
            }
        }
        if (!changed.isEmpty()) {
            send(player, mapPayload(dimension, false, changed, colours));
        }
    }

    /** Sends every dimension's explored map, in as many packets as it takes. */
    public static void sendWholeMap(ServerPlayer player) {
        FieldGuideData data = get(player);
        for (String dimension : data.exploredDimensions()) {
            List<Long> keys = new ArrayList<>();
            List<byte[]> colours = new ArrayList<>();
            boolean first = true;
            for (Map.Entry<Long, byte[]> entry : data.exploredChunks(dimension).entrySet()) {
                keys.add(entry.getKey());
                colours.add(entry.getValue());
                if (keys.size() == FieldGuideMapPayload.MAX_CHUNKS_PER_PACKET) {
                    send(player, mapPayload(dimension, first, keys, colours));
                    first = false;
                    keys.clear();
                    colours.clear();
                }
            }
            if (!keys.isEmpty() || first) {
                send(player, mapPayload(dimension, first, keys, colours));
            }
        }
    }

    /** Sends to players whose client speaks the guide's payloads (not test or fake players). */
    private static void send(ServerPlayer player, net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) {
        if (player.connection != null && player.connection.hasChannel(payload.type())) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }

    private static FieldGuideMapPayload mapPayload(String dimension, boolean reset, List<Long> keys, List<byte[]> colours) {
        long[] chunks = new long[keys.size()];
        byte[] bytes = new byte[keys.size() * FieldGuideData.CHUNK_BYTES];
        for (int i = 0; i < keys.size(); i++) {
            chunks[i] = keys.get(i);
            System.arraycopy(colours.get(i), 0, bytes, i * FieldGuideData.CHUNK_BYTES, FieldGuideData.CHUNK_BYTES);
        }
        return new FieldGuideMapPayload(dimension, reset, chunks, bytes);
    }

    // Creatures

    /** Checks what a scoping player is looking at, and counts a sighting once they have held it long enough. */
    static void sight(ServerPlayer player) {
        Tracking tracking = tracking(player);
        if (!player.isScoping()) {
            tracking.target = null;
            tracking.held = 0;
            return;
        }
        Optional<NightmareCreature> seen = inScope(player, settings().sightingRange());
        if (seen.isEmpty()) {
            tracking.target = null;
            tracking.held = 0;
            return;
        }
        NightmareCreature creature = seen.get();
        if (creature.getUUID().equals(tracking.target)) {
            tracking.held += SIGHT_INTERVAL;
        } else {
            tracking.target = creature.getUUID();
            tracking.held = SIGHT_INTERVAL;
        }
        if (tracking.held >= settings().sightingHoldTicks()) {
            recordSighting(player, creature);
        }
    }

    /** The creature nearest the middle of a spyglass, if one is in clear view. */
    public static Optional<NightmareCreature> inScope(ServerPlayer player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        AABB box = player.getBoundingBox().expandTowards(look.scale(range)).inflate(4.0);
        NightmareCreature best = null;
        double bestDot = -1.0;
        for (NightmareCreature creature : player.level().getEntitiesOfClass(NightmareCreature.class, box, NightmareCreature::isAlive)) {
            Vec3 to = creature.getBoundingBox().getCenter().subtract(eye);
            double distance = to.length();
            if (distance > range || distance < 0.5) {
                continue;
            }
            double tolerance = Math.max(Math.toRadians(3.0), Math.atan2(creature.getBbHeight() * 0.6, distance));
            double dot = to.scale(1.0 / distance).dot(look);
            if (dot > Math.cos(tolerance) && dot > bestDot && player.hasLineOfSight(creature)) {
                best = creature;
                bestDot = dot;
            }
        }
        return Optional.ofNullable(best);
    }

    /** Counts a sighting of a creature (subject to the per-kind cooldown) and tells the player what it revealed. */
    public static boolean recordSighting(ServerPlayer player, NightmareCreature creature) {
        FieldGuideData data = get(player);
        ResourceLocation type = BuiltInRegistries.ENTITY_TYPE.getKey(creature.getType());
        int before = revealedCount(data, type);
        boolean firstPage = data.creature(type).map(entry -> entry.sightings() + entry.kills() == 0).orElse(true);
        int amount = 1 + (ForewarningService.isSeer(player) ? settings().seerBonus() : 0);
        BlockPos pos = creature.blockPosition();
        String dimension = dimension(creature.level());
        boolean counted = data.sight(type, creature.rank().ordinal(), dimension, pos.getX(), pos.getZ(),
                player.level().getGameTime(), settings().sightingCooldown(), amount);
        markSeen(data, dimension, pos, creature.rank());
        tracking(player).dirty = true;
        if (counted) {
            announce(player, type, creature, firstPage, before);
        }
        return counted;
    }

    static void recordKill(ServerPlayer player, NightmareCreature creature) {
        FieldGuideData data = get(player);
        ResourceLocation type = BuiltInRegistries.ENTITY_TYPE.getKey(creature.getType());
        int before = revealedCount(data, type);
        boolean firstPage = data.creature(type).map(entry -> entry.sightings() + entry.kills() == 0).orElse(true);
        BlockPos pos = creature.blockPosition();
        String dimension = dimension(creature.level());
        data.kill(type, creature.rank().ordinal(), dimension, pos.getX(), pos.getZ());
        markSeen(data, dimension, pos, creature.rank());
        tracking(player).dirty = true;
        announce(player, type, creature, firstPage, before);
    }

    static void recordDeath(ServerPlayer player, NightmareCreature killer) {
        FieldGuideData data = get(player);
        String dimension = dimension(player.level());
        markSeen(data, dimension, killer.blockPosition(), killer.rank());
        int cell = settings().dangerCell();
        data.markDeath(dimension, DangerRating.key(DangerRating.cell(player.getBlockX(), cell), DangerRating.cell(player.getBlockZ(), cell)));
        tracking(player).dirty = true;
    }

    private static void markSeen(FieldGuideData data, String dimension, BlockPos pos, CreatureRank rank) {
        int cell = settings().dangerCell();
        data.markSeen(dimension, DangerRating.key(DangerRating.cell(pos.getX(), cell), DangerRating.cell(pos.getZ(), cell)), rank.ordinal());
    }

    private static int revealedCount(FieldGuideData data, ResourceLocation type) {
        Optional<CreaturePage> page = FieldGuideContent.get().page(type);
        return data.creature(type).flatMap(entry -> page.map(p -> p.revealed(entry.sightings(), entry.kills()).size())).orElse(0);
    }

    private static void announce(ServerPlayer player, ResourceLocation type, NightmareCreature creature, boolean firstPage, int before) {
        FieldGuideData data = get(player);
        String name = creatureName(type);
        Optional<CreaturePage> page = FieldGuideContent.get().page(type);
        List<CreaturePage.Section> revealed = data.creature(type)
                .flatMap(entry -> page.map(p -> p.revealed(entry.sightings(), entry.kills())))
                .orElse(List.of());
        Component message = null;
        if (firstPage) {
            message = Component.literal("Field Guide: new page, " + name).withStyle(ChatFormatting.GOLD);
        } else if (revealed.size() > before) {
            message = Component.literal("Field Guide: " + name + ", " + revealed.get(revealed.size() - 1).title() + " revealed")
                    .withStyle(ChatFormatting.YELLOW);
        }
        if (message != null) {
            player.displayClientMessage(message, true);
            player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    public static String creatureName(ResourceLocation type) {
        return FieldGuideContent.get().page(type).map(CreaturePage::name)
                .orElseGet(() -> BuiltInRegistries.ENTITY_TYPE.get(type).getDescription().getString());
    }

    // Places

    /** Logs the Gates and Seeds a player has come near. */
    static void logPlaces(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        String dimension = dimension(level);
        FieldGuideData data = get(player);
        FieldGuideSettings settings = settings();
        for (GateEvent gate : GateData.get(level).gates()) {
            if (gate.phase() == GateEvent.Phase.CLOSED || data.hasPin(gate.id())
                    || horizontalDistance(player.blockPosition(), gate.center()) > settings.placeRange()) {
                continue;
            }
            if (data.addPin(new FieldGuideData.Pin(FieldGuideData.PinKind.GATE, dimension, gate.center().getX(), gate.center().getZ(),
                    "Category " + gate.category() + " Gate", Optional.of(gate.id())))) {
                player.displayClientMessage(Component.literal("Field Guide: Gate logged at " + gate.center().getX() + ", "
                        + gate.center().getZ()).withStyle(ChatFormatting.LIGHT_PURPLE), true);
                tracking(player).dirty = true;
            }
        }
        if (CitadelService.logCitadels(player, data, dimension)) {
            tracking(player).dirty = true;
        }
        if (DreamRealm.is(level)) {
            for (SeedData.Seed seed : SeedData.get(level).seeds()) {
                if (data.hasPin(seed.id()) || horizontalDistance(player.blockPosition(), seed.pos()) > settings.seedRange()) {
                    continue;
                }
                if (data.addPin(new FieldGuideData.Pin(FieldGuideData.PinKind.SEED, dimension, seed.pos().getX(), seed.pos().getZ(),
                        "Nightmare Seed", Optional.of(seed.id())))) {
                    BlockPos gate = DreamRealm.toOverworld(seed.pos(), DreamRealmRules.ratio(player.server));
                    player.displayClientMessage(Component.literal("Field Guide: Seed logged. Its Gate would open near "
                            + gate.getX() + ", " + gate.getZ() + " in the waking world.").withStyle(ChatFormatting.DARK_RED), true);
                    tracking(player).dirty = true;
                }
            }
        }
    }

    private static double horizontalDistance(BlockPos a, BlockPos b) {
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    // Pins from the map

    public static void requestPin(ServerPlayer player, FieldGuidePinRequestPayload request) {
        FieldGuideData data = get(player);
        String label = request.label().strip();
        if (label.length() > FieldGuidePinRequestPayload.MAX_LABEL) {
            label = label.substring(0, FieldGuidePinRequestPayload.MAX_LABEL);
        }
        switch (request.action()) {
            case FieldGuidePinRequestPayload.PLACE, FieldGuidePinRequestPayload.WARNING -> {
                FieldGuideData.PinKind kind = request.action().equals(FieldGuidePinRequestPayload.WARNING)
                        ? FieldGuideData.PinKind.WARNING : FieldGuideData.PinKind.PLACE;
                if (label.isEmpty()) {
                    label = kind == FieldGuideData.PinKind.WARNING ? "Danger" : "Marked place";
                }
                if (!data.addPin(new FieldGuideData.Pin(kind, request.dimension(), request.x(), request.z(), label, Optional.empty()))) {
                    player.displayClientMessage(Component.literal("Your Field Guide has no room for more pins.").withStyle(ChatFormatting.GRAY), true);
                }
            }
            case FieldGuidePinRequestPayload.REMOVE -> data.removePin(pin -> pin.kind().named()
                    && pin.dimension().equals(request.dimension())
                    && Math.abs(pin.x() - request.x()) <= 2 && Math.abs(pin.z() - request.z()) <= 2);
            default -> {
                return;
            }
        }
        sendStatus(player, false);
    }

    // Reading

    public static void open(ServerPlayer player) {
        explore(player, true);
        logPlaces(player);
        sendStatus(player, true);
    }

    public static void sendStatus(ServerPlayer player, boolean open) {
        tracking(player).dirty = false;
        send(player, status(player, open));
    }

    public static FieldGuideStatusPayload status(ServerPlayer player, boolean open) {
        MinecraftServer server = player.server;
        FieldGuideData data = get(player);
        FieldGuideContent content = FieldGuideContent.get();
        String realm = DreamRealm.key(server).location().toString();
        int ratio = DreamRealmRules.ratio(server);
        FieldGuideSettings settings = content.settings();

        List<FieldGuideStatusPayload.Creature> creatures = new ArrayList<>();
        data.creatures().forEach((type, entry) -> {
            if (entry.sightings() + entry.kills() == 0) {
                return;
            }
            Optional<CreaturePage> page = content.page(type);
            List<FieldGuideStatusPayload.Section> sections = page.map(p -> p.revealed(entry.sightings(), entry.kills()).stream()
                    .map(section -> new FieldGuideStatusPayload.Section(section.title(), section.text())).toList()).orElse(List.of());
            int locked = page.map(p -> p.sections().size()).orElse(0) - sections.size();
            List<String> ranks = new ArrayList<>();
            for (CreatureRank rank : CreatureRank.values()) {
                if ((entry.ranksSeen() & (1 << rank.ordinal())) != 0) {
                    ranks.add(rank.displayName());
                }
            }
            creatures.add(new FieldGuideStatusPayload.Creature(type.toString(), creatureName(type),
                    page.map(CreaturePage::summary).orElse(""), entry.sightings(), entry.kills(), ranks, sections, locked,
                    page.map(p -> p.nextHint(entry.sightings(), entry.kills())).orElse(""),
                    entry.lastDimension().orElse(""), entry.lastX(), entry.lastZ()));
        });

        pruneSpentPins(server, data, realm);
        Optional<BlockPos> anchor = DreamCrossing.data(player).anchor();
        boolean anchorShown = false;
        List<FieldGuideStatusPayload.Pin> pins = new ArrayList<>();
        for (FieldGuideData.Pin pin : data.pins()) {
            String detail = "";
            boolean active = true;
            int gateX = 0;
            int gateZ = 0;
            if (pin.kind() == FieldGuideData.PinKind.SEED) {
                BlockPos gate = DreamRealm.toOverworld(new BlockPos(pin.x(), 0, pin.z()), ratio);
                gateX = gate.getX();
                gateZ = gate.getZ();
                detail = "Its Gate would open near " + gateX + ", " + gateZ + " in the waking world";
            } else if (pin.kind() == FieldGuideData.PinKind.GATE) {
                active = pin.ref().flatMap(id -> gate(server, pin.dimension(), id)).filter(gate -> gate.phase() != GateEvent.Phase.CLOSED).isPresent();
                detail = active ? "Open" : "Closed";
            } else if (pin.kind() == FieldGuideData.PinKind.CITADEL) {
                detail = pin.ref().map(id -> CitadelService.describe(server, id)).orElse("");
                if (anchor.filter(at -> at.getX() == pin.x() && at.getZ() == pin.z()).isPresent()) {
                    // Anchored to this Citadel's Gateway: one pin says both.
                    detail += " · your Dream Anchor";
                    anchorShown = true;
                }
            }
            pins.add(new FieldGuideStatusPayload.Pin(pin.kind().getSerializedName(), pin.dimension(), pin.x(), pin.z(), pin.label(),
                    detail, active, gateX, gateZ));
        }
        if (anchor.isPresent() && !anchorShown) {
            pins.add(new FieldGuideStatusPayload.Pin("anchor", realm, anchor.get().getX(), anchor.get().getZ(), "Dream Anchor",
                    "You arrive here when you dream", true, 0, 0));
        }

        List<FieldGuideStatusPayload.Region> regions = new ArrayList<>();
        for (DreamRegion region : DreamRealmContent.get().regions().values()) {
            regions.add(new FieldGuideStatusPayload.Region(region.displayName(), region.minDistance(), region.maxDistance(),
                    region.dangerRank().ordinal()));
        }

        List<FieldGuideStatusPayload.Danger> danger = new ArrayList<>();
        for (String dimension : dangerDimensions(data)) {
            data.dangerMarks(dimension).forEach((cell, marks) -> {
                int baseline = dimension.equals(realm) ? baseline(cell, settings.dangerCell()) : DangerRating.NONE;
                int rating = DangerRating.rate(baseline, FieldGuideData.strongest(marks), FieldGuideData.deaths(marks), settings.deathStep());
                if (rating >= 0) {
                    danger.add(new FieldGuideStatusPayload.Danger(dimension, DangerRating.keyX(cell), DangerRating.keyZ(cell), rating));
                }
            });
        }
        return new FieldGuideStatusPayload(creatures, pins, danger, regions, realm, ratio, settings.dangerCell(), open);
    }

    private static List<String> dangerDimensions(FieldGuideData data) {
        List<String> dimensions = new ArrayList<>(data.exploredDimensions());
        for (String dimension : List.of(Level.OVERWORLD.location().toString(), Level.NETHER.location().toString(),
                DreamRealm.LEVEL.location().toString())) {
            if (!dimensions.contains(dimension)) {
                dimensions.add(dimension);
            }
        }
        return dimensions;
    }

    /** A Dream Realm danger cell's baseline: the rank of the region its middle lies in. */
    private static int baseline(long cell, int cellSize) {
        BlockPos middle = new BlockPos(DangerRating.keyX(cell) * cellSize + cellSize / 2, 0, DangerRating.keyZ(cell) * cellSize + cellSize / 2);
        return DreamRealmContent.get().regionAt(middle).map(entry -> entry.getValue().dangerRank().ordinal()).orElse(DangerRating.NONE);
    }

    /** Seeds that bloomed or were cleansed leave the guide. */
    private static void pruneSpentPins(MinecraftServer server, FieldGuideData data, String realm) {
        ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(realm)));
        if (level == null) {
            return;
        }
        List<UUID> living = SeedData.get(level).seeds().stream().map(SeedData.Seed::id).toList();
        data.removePin(pin -> pin.kind() == FieldGuideData.PinKind.SEED && pin.ref().filter(living::contains).isEmpty());
    }

    private static Optional<GateEvent> gate(MinecraftServer server, String dimension, UUID id) {
        ResourceLocation location = ResourceLocation.tryParse(dimension);
        ServerLevel level = location == null ? null : server.getLevel(ResourceKey.create(Registries.DIMENSION, location));
        if (level == null) {
            return Optional.empty();
        }
        return GateData.get(level).gates().stream().filter(gate -> gate.id().equals(id)).findFirst();
    }
}
