package dev.spud.shadowslave.citadel;

import dev.spud.shadowslave.clan.ClanData;
import dev.spud.shadowslave.spell.SpellRecorder;
import dev.spud.shadowslave.clan.ClanService;
import dev.spud.shadowslave.creature.CreatureRank;
import dev.spud.shadowslave.creature.NightmareCreature;
import dev.spud.shadowslave.creature.goal.SpawnLesserCopiesGoal;
import dev.spud.shadowslave.dreamrealm.DreamCrossing;
import dev.spud.shadowslave.dreamrealm.DreamRealm;
import dev.spud.shadowslave.dreamrealm.DreamRealmContent;
import dev.spud.shadowslave.dreamrealm.DreamRegion;
import dev.spud.shadowslave.fieldguide.FieldGuideData;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SoulService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Citadels (DESIGN, Jack 2026-10-29; CANON ch. 23, 358, 1591: a Citadel is
 * personally conquered and claimed through its Gateway). Each generated
 * Citadel wakes the first time its Gateway ticks and is held by a Guardian
 * at the rank of its region. Slay the Guardian and the Gateway opens; use it
 * to claim the Citadel for your clan (a clan of one if you have none).
 * Members anchor to its Gateway, so they arrive there when they dream, and
 * share their Field Guides with the clan's board whenever they visit
 * (CANON, ch. 877). A member who uses the Gateway feeds it what they hold,
 * or opens its screen for upkeep, upgrades and links ({@link CitadelLife});
 * raids are in {@link CitadelRaids}. Generated Citadels may wake held by an
 * NPC clan or fallen ({@link NpcCitadels}). Anyone may conquer a Citadel, but
 * only a Saint binds one (CANON, ch. 1074, 1802; Jack 2026-10-31: binding
 * wipes the old owner and writes the Citadel to the Saint's clan); below
 * that rank a conquered Gateway only holds anchors.
 */
public final class CitadelService {
    static final String CITADEL_TAG = "shadowslave_citadel";
    private static final int VISIT_INTERVAL = 40;
    /** Which claimed Citadel each player is visiting now; not saved, so a login counts as a fresh visit. */
    private static final Map<UUID, UUID> VISITING = new HashMap<>();

    private CitadelService() {
    }

    /** Lets clans hand their Citadels on, or let them fall open. */
    public static void bootstrap() {
        ClanService.addListener(new ClanService.Listener() {
            @Override
            public void dissolved(MinecraftServer server, UUID clan) {
                forEachHeldBy(server, clan, (level, citadel) -> {
                    citadel.hold = CitadelData.Hold.CONQUERED;
                    citadel.clan = null;
                    citadel.conqueror = null;
                    citadel.conquerorClan = null;
                    citadel.conqueredAt = 0;
                    citadel.raid = null;
                    citadel.anchored.clear();
                    showState(level, citadel);
                    SpellRecorder.citadelLost(server, citadel.name, clan);
                });
            }

            @Override
            public void merged(MinecraftServer server, UUID from, UUID into) {
                forEachHeldBy(server, from, (level, citadel) -> citadel.clan = into);
            }
        });
    }

    private static CitadelSettings settings() {
        return CitadelContentLoader.settings();
    }

    // Finding Citadels

    public static Optional<CitadelData.Citadel> at(ServerLevel level, BlockPos gateway) {
        return CitadelData.get(level).at(gateway);
    }

    /** The Citadel whose Gateway lies within {@code radius} blocks of {@code pos}, nearest first. */
    public static Optional<CitadelData.Citadel> near(ServerLevel level, BlockPos pos, double radius) {
        CitadelData.Citadel nearest = null;
        double best = radius * radius;
        for (CitadelData.Citadel citadel : CitadelData.get(level).citadels) {
            double distance = citadel.gateway.distSqr(pos);
            if (distance <= best) {
                best = distance;
                nearest = citadel;
            }
        }
        return Optional.ofNullable(nearest);
    }

    /** Whether the player stands on land their clan has claimed: near the Gateway of a Citadel it holds. */
    public static boolean onClanLand(ServerPlayer player) {
        if (!DreamRealm.is(player.level())) {
            return false;
        }
        Optional<UUID> clan = ClanService.clanOf(player.server, player.getUUID()).map(ClanData.Clan::id);
        if (clan.isEmpty()) {
            return false;
        }
        double reach = (double) settings().landRadius() * settings().landRadius();
        return CitadelData.get(player.serverLevel()).citadels.stream()
                .anyMatch(citadel -> citadel.hold == CitadelData.Hold.CLAN && clan.get().equals(citadel.clan)
                        && citadel.gateway.distSqr(player.blockPosition()) <= reach);
    }

    /** The Citadels the player's clan holds, nearest to {@code from} first. */
    public static List<CitadelData.Citadel> clanCitadels(ServerPlayer player, ServerLevel realm, BlockPos from) {
        Optional<UUID> clan = ClanService.clanOf(player.server, player.getUUID()).map(ClanData.Clan::id);
        if (clan.isEmpty()) {
            return List.of();
        }
        return CitadelData.get(realm).citadels.stream()
                .filter(citadel -> citadel.hold == CitadelData.Hold.CLAN && clan.get().equals(citadel.clan))
                .sorted(java.util.Comparator.comparingDouble(citadel -> citadel.gateway.distSqr(from)))
                .toList();
    }

    /** Names of the Citadels a clan holds. */
    public static List<String> heldBy(MinecraftServer server, UUID clan) {
        ServerLevel realm = DreamRealm.level(server);
        if (realm == null) {
            return List.of();
        }
        return CitadelData.get(realm).citadels.stream().filter(citadel -> clan.equals(citadel.clan))
                .map(citadel -> citadel.name + " (" + citadel.gateway.getX() + ", " + citadel.gateway.getZ() + ")").toList();
    }

    private static void forEachHeldBy(MinecraftServer server, UUID clan, java.util.function.BiConsumer<ServerLevel, CitadelData.Citadel> action) {
        ServerLevel realm = DreamRealm.level(server);
        if (realm == null) {
            return;
        }
        CitadelData data = CitadelData.get(realm);
        for (CitadelData.Citadel citadel : data.citadels) {
            if (clan.equals(citadel.clan)) {
                action.accept(realm, citadel);
            }
        }
        data.setDirty();
    }

    /** Raises a Citadel in a loaded world, for operators and tests; generated ones come from {@link CitadelPiece}. */
    public static void build(ServerLevel level, BlockPos centre, int half, CitadelPalette palette) {
        BoundingBox bounds = CitadelLayout.bounds(centre, half);
        for (int x = bounds.minX() >> 4; x <= bounds.maxX() >> 4; x++) {
            for (int z = bounds.minZ() >> 4; z <= bounds.maxZ() >> 4; z++) {
                level.getChunk(x, z);
            }
        }
        CitadelLayout.build(new CitadelLayout.Canvas() {
            @Override
            public BlockState get(BlockPos pos) {
                return level.getBlockState(pos);
            }

            @Override
            public void set(BlockPos pos, BlockState state) {
                level.setBlock(pos, state, Block.UPDATE_CLIENTS);
            }
        }, centre, half, palette);
    }

    // Waking and guarding

    /** Wakes the Citadel behind a Gateway if this is its first tick, then keeps it as its record says. */
    static void tickGateway(ServerLevel level, BlockPos gateway) {
        CitadelData.Citadel citadel = at(level, gateway).orElseGet(() -> wakeGenerated(level, gateway));
        showState(level, citadel);
        if (citadel.hold == CitadelData.Hold.GUARDIAN) {
            keepGuardian(level, citadel);
        } else if (citadel.hold == CitadelData.Hold.NPC) {
            NpcCitadels.tick(level, citadel);
        }
    }

    /** Records a Citadel for a Gateway and posts its Guardian. */
    public static CitadelData.Citadel wake(ServerLevel level, BlockPos gateway) {
        return wake(level, gateway, NpcCitadels.Kind.WILD);
    }

    /** Wakes a Citadel world generation raised, as the world seed has it: held, wild or fallen. */
    static CitadelData.Citadel wakeGenerated(ServerLevel level, BlockPos gateway) {
        return wake(level, gateway, NpcCitadels.roll(level, gateway));
    }

    /** Records a Citadel for a Gateway as {@code kind}: a wild one gets its Guardian, a held one its NPC clan. */
    public static CitadelData.Citadel wake(ServerLevel level, BlockPos gateway, NpcCitadels.Kind kind) {
        CitadelData data = CitadelData.get(level);
        Optional<CitadelData.Citadel> existing = data.at(gateway);
        if (existing.isPresent()) {
            return existing.get();
        }
        CreatureRank rank = DreamRealmContent.get().regionAt(gateway).map(entry -> entry.getValue().dangerRank())
                .orElse(CreatureRank.DORMANT);
        List<String> names = settings().names();
        String name = names.get(Math.floorMod(Long.hashCode(gateway.asLong() * 31L), names.size()));
        CitadelData.Citadel citadel = new CitadelData.Citadel(UUID.randomUUID(), gateway, name, rank);
        data.citadels.add(citadel);
        data.setDirty();
        switch (kind) {
            case WILD -> spawnGuardian(level, citadel);
            case HELD -> NpcCitadels.hold(level, citadel);
            case FALLEN -> NpcCitadels.fall(level, citadel);
        }
        showState(level, citadel);
        return citadel;
    }

    private static void keepGuardian(ServerLevel level, CitadelData.Citadel citadel) {
        Entity guardian = citadel.guardian == null ? null : level.getEntity(citadel.guardian);
        if (guardian != null && guardian.isAlive()) {
            citadel.missing = 0;
            citadel.guardianPos = guardian.blockPosition();
            if (guardian instanceof PathfinderMob mob && !mob.hasRestriction()) {
                mob.restrictTo(citadel.gateway, settings().guardRadius());
            }
            return;
        }
        if (!level.areEntitiesLoaded(ChunkPos.asLong(citadel.guardianPos)) || level.getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        citadel.missing += 20;
        if (citadel.guardian == null || citadel.missing >= settings().guardianReturnSeconds() * 20) {
            spawnGuardian(level, citadel);
        }
    }

    /** Posts a Guardian of the Citadel's rank in the courtyard before the dais. */
    static Optional<Mob> spawnGuardian(ServerLevel level, CitadelData.Citadel citadel) {
        citadel.missing = 0;
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return Optional.empty();
        }
        CitadelSettings settings = settings();
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(settings.guardian());
        if (!(type.create(level) instanceof Mob guardian)) {
            return Optional.empty();
        }
        BlockPos post = citadel.gateway.offset(0, -1, 6);
        guardian.moveTo(post.getX() + 0.5, post.getY(), post.getZ() + 0.5, 180.0F, 0.0F);
        guardian.finalizeSpawn(level, level.getCurrentDifficultyAt(post), MobSpawnType.STRUCTURE, null);
        if (guardian instanceof NightmareCreature creature) {
            creature.setRankAndClass(citadel.rank, settings.guardianClass());
        }
        guardian.setPersistenceRequired();
        if (guardian instanceof PathfinderMob mob) {
            mob.restrictTo(citadel.gateway, settings.guardRadius());
        }
        guardian.getPersistentData().putUUID(CITADEL_TAG, citadel.id);
        level.addFreshEntity(guardian);
        citadel.guardian = guardian.getUUID();
        citadel.guardianPos = post;
        CitadelData.get(level).setDirty();
        return Optional.of(guardian);
    }

    /** Sets the Gateway block to show who holds the Citadel, if its chunk is loaded. */
    static void showState(ServerLevel level, CitadelData.Citadel citadel) {
        if (!level.isLoaded(citadel.gateway)) {
            return;
        }
        BlockState state = level.getBlockState(citadel.gateway);
        if (state.is(CitadelRegistry.GATEWAY.get()) && state.getValue(GatewayBlock.STATE) != citadel.hold.gateway()) {
            level.setBlock(citadel.gateway, state.setValue(GatewayBlock.STATE, citadel.hold.gateway()), Block.UPDATE_ALL);
        }
    }

    // Conquest

    /** A Guardian's death opens its Citadel's Gateway. */
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level) || !event.getEntity().getPersistentData().hasUUID(CITADEL_TAG)) {
            return;
        }
        UUID id = event.getEntity().getPersistentData().getUUID(CITADEL_TAG);
        CitadelData.get(level).byId(id)
                .filter(citadel -> citadel.hold == CitadelData.Hold.GUARDIAN && event.getEntity().getUUID().equals(citadel.guardian))
                .ifPresent(citadel -> conquer(level, citadel, killer(event)));
    }

    private static Optional<ServerPlayer> killer(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            return Optional.of(player);
        }
        if (event.getEntity().getKillCredit() instanceof ServerPlayer player) {
            return Optional.of(player);
        }
        return Optional.empty();
    }

    /** The Guardian has fallen: the Gateway opens, and whoever slew it has first claim. */
    public static void conquer(ServerLevel level, CitadelData.Citadel citadel, Optional<ServerPlayer> by) {
        citadel.hold = CitadelData.Hold.CONQUERED;
        citadel.conqueror = by.map(Player::getUUID).orElse(null);
        citadel.conquerorClan = by.flatMap(player -> ClanService.clanOf(level.getServer(), player.getUUID())).map(ClanData.Clan::id).orElse(null);
        citadel.conqueredAt = level.getGameTime();
        CitadelData.get(level).setDirty();
        if (citadel.guardian != null) {
            for (NightmareCreature lesser : level.getEntitiesOfClass(NightmareCreature.class, new AABB(citadel.gateway).inflate(64),
                    other -> citadel.guardian.equals(SpawnLesserCopiesGoal.spawnerOf(other)))) {
                level.sendParticles(ParticleTypes.ASH, lesser.getX(), lesser.getY() + lesser.getBbHeight() / 2, lesser.getZ(),
                        30, lesser.getBbWidth() / 2, lesser.getBbHeight() / 2, lesser.getBbWidth() / 2, 0.02);
                lesser.discard();
            }
        }
        showState(level, citadel);
        level.playSound(null, citadel.gateway, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 2.0F, 0.6F);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, citadel.gateway.getX() + 0.5, citadel.gateway.getY() + 1.0,
                citadel.gateway.getZ() + 0.5, 120, 0.6, 1.2, 0.6, 0.1);
        for (ServerPlayer player : level.getPlayers(player -> player.blockPosition().closerThan(citadel.gateway, 96))) {
            DreamCrossing.title(player, Component.literal("Citadel Conquered").withStyle(ChatFormatting.LIGHT_PURPLE),
                    Component.literal(citadel.name + " has lost its Guardian").withStyle(ChatFormatting.GRAY));
        }
        by.ifPresent(player -> DreamCrossing.spell(player, Component.literal("The Gateway of " + citadel.name
                + " lies open. Use it to claim the Citadel.").withStyle(ChatFormatting.LIGHT_PURPLE)));
    }

    // The Gateway

    /** A player uses a Gateway: learn who holds it, claim it, or anchor to it and share the board. */
    public static void useGateway(ServerPlayer player, BlockPos gateway) {
        ServerLevel level = player.serverLevel();
        CitadelData.Citadel citadel = at(level, gateway).orElseGet(() -> wakeGenerated(level, gateway));
        switch (citadel.hold) {
            case GUARDIAN -> {
                Entity guardian = citadel.guardian == null ? null : level.getEntity(citadel.guardian);
                if (level.getDifficulty() == Difficulty.PEACEFUL && guardian == null) {
                    // With the world at peace no Guardian stands; the Gateway yields to whoever reaches it.
                    conquer(level, citadel, Optional.of(player));
                    claimOrAnchor(player, citadel);
                    return;
                }
                player.displayClientMessage(Component.literal("The Gateway is sealed. A " + citadel.rank.displayName() + " "
                        + settings().guardianClass().displayName() + " still holds " + citadel.name + ".")
                        .withStyle(ChatFormatting.DARK_RED), true);
            }
            case CONQUERED -> claimOrAnchor(player, citadel);
            case NPC -> NpcCitadels.useGateway(player, citadel);
            case CLAN -> {
                Optional<ClanData.Clan> clan = Optional.ofNullable(citadel.clan).flatMap(id -> ClanService.byId(player.server, id));
                if (clan.isEmpty() || !clan.get().has(player.getUUID())) {
                    Optional<ClanData.Clan> mine = ClanService.clanOf(player.server, player.getUUID());
                    boolean enemy = clan.isPresent() && mine.isPresent() && CitadelWars.atWar(player.server, mine.get().id(), clan.get().id());
                    player.displayClientMessage(Component.literal(citadel.name + " is held by the clan "
                            + clan.map(ClanData.Clan::name).orElse("of another") + (enemy ? ", your enemy"
                            + (CitadelWars.isSeat(player.server, citadel) ? ". It is their seat: take it in an assault" : "") : "") + ".")
                            .withStyle(enemy ? ChatFormatting.DARK_RED : ChatFormatting.GRAY), true);
                    return;
                }
                if (CitadelLife.feedFromHand(player, citadel)) {
                    return;
                }
                String message = "";
                if (!isAnchoredHere(player, citadel)) {
                    if (CitadelLife.hasRoom(player, citadel)) {
                        anchor(player, citadel);
                    } else {
                        message = "The Hall of " + citadel.name + " has room for " + CitadelLife.anchorSlots(citadel)
                                + " anchors, and all are taken. Build up its Hall.";
                    }
                }
                visit(player, citadel, clan.get(), false);
                CitadelLife.open(player, citadel, message);
            }
        }
    }

    /** Whether the player's soul is strong enough to bind a Citadel's Gateway (a Saint, unless a server lowers it). */
    public static boolean canBind(ServerPlayer player) {
        SoulRank needed = settings().claimRank();
        return SoulService.get(player).soulRank().filter(rank -> rank.compareTo(needed) >= 0).isPresent();
    }

    /** A Saint binds an open Gateway; anyone else may only anchor to it. */
    static void claimOrAnchor(ServerPlayer player, CitadelData.Citadel citadel) {
        if (canBind(player)) {
            claim(player, citadel);
            return;
        }
        if (!isAnchoredHere(player, citadel)) {
            anchor(player, citadel);
        }
        player.displayClientMessage(Component.literal("Only a " + rankName(settings().claimRank()) + " can bind the Gateway of "
                + citadel.name + ". Until one does, it holds anyone's anchor and belongs to nobody.").withStyle(ChatFormatting.GRAY), false);
    }

    static String rankName(SoulRank rank) {
        return switch (rank) {
            case DORMANT -> "Sleeper";
            case AWAKENED -> "Awakened";
            case ASCENDED -> "Master";
            case TRANSCENDENT -> "Saint";
            case SUPREME -> "Sovereign";
            case SACRED -> "Sacred";
            case DIVINE -> "Divine";
        };
    }

    /** Claims an open Gateway for the player's clan, founding a clan of one if they have none; only a Saint may. */
    public static boolean claim(ServerPlayer player, CitadelData.Citadel citadel) {
        ServerLevel level = DreamRealm.level(player.server);
        if (level == null) {
            return false;
        }
        if (!canBind(player)) {
            player.displayClientMessage(Component.literal("Only a " + rankName(settings().claimRank()) + " can bind the Gateway of "
                    + citadel.name + ".").withStyle(ChatFormatting.GRAY), true);
            return false;
        }
        long priority = settings().claimPrioritySeconds() * 20L;
        boolean firstClaim = citadel.conqueror != null && level.getGameTime() - citadel.conqueredAt < priority;
        Optional<ClanData.Clan> current = ClanService.clanOf(player.server, player.getUUID());
        boolean conquerors = player.getUUID().equals(citadel.conqueror)
                || current.map(ClanData.Clan::id).filter(id -> id.equals(citadel.conquerorClan)).isPresent();
        if (firstClaim && !conquerors) {
            long seconds = (priority - (level.getGameTime() - citadel.conqueredAt)) / 20;
            player.displayClientMessage(Component.literal("Those who slew the Guardian have first claim on " + citadel.name
                    + " for " + seconds + " more seconds.").withStyle(ChatFormatting.GRAY), true);
            return false;
        }
        ClanData.Clan clan = ClanService.ensureClan(player);
        citadel.hold = CitadelData.Hold.CLAN;
        citadel.clan = clan.id();
        CitadelLife.claimed(player.server, citadel);
        CitadelWars.claimed(player.server, citadel);
        CitadelData.get(level).setDirty();
        showState(level, citadel);
        level.playSound(null, citadel.gateway, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 0.8F, 1.2F);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, citadel.gateway.getX() + 0.5, citadel.gateway.getY() + 1.0,
                citadel.gateway.getZ() + 0.5, 60, 0.5, 0.8, 0.5, 0.04);
        DreamCrossing.title(player, Component.literal("Citadel Claimed").withStyle(ChatFormatting.LIGHT_PURPLE),
                Component.literal(citadel.name + " belongs to " + clan.name()).withStyle(ChatFormatting.GRAY));
        ClanService.tell(player.server, clan, player.getGameProfile().getName() + " claimed " + citadel.name + " for "
                + clan.name() + ". Use its Gateway to anchor there.");
        SpellRecorder.citadelClaimed(player, citadel.name);
        anchor(player, citadel);
        visit(player, citadel, clan, true);
        return true;
    }

    // Anchoring

    static boolean isAnchoredHere(ServerPlayer player, CitadelData.Citadel citadel) {
        return DreamCrossing.data(player).anchor().filter(citadel.gateway::equals).isPresent();
    }

    /** Makes a claimed Citadel's Gateway a member's Dream Anchor (CANON, ch. 23: Awakened anchor to a Gateway). */
    public static void anchor(ServerPlayer player, CitadelData.Citadel citadel) {
        DreamCrossing.anchorAt(player, citadel.gateway);
        citadel.anchored.add(player.getUUID());
        DreamCrossing.spell(player, Component.literal("You are anchored to " + citadel.name
                + ". When you dream, you will arrive at its Gateway.").withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    /**
     * Whether a Gateway can hold this player's anchor: it belongs to a
     * Citadel their clan holds. Used by the Dream Realm when they arrive.
     */
    public static boolean welcomes(ServerPlayer player, ServerLevel realm, BlockPos gateway) {
        Optional<CitadelData.Citadel> citadel = at(realm, gateway);
        if (citadel.isEmpty()) {
            return false;
        }
        return switch (citadel.get().hold) {
            case GUARDIAN -> false;
            // An open Gateway belongs to nobody and holds anyone's anchor (CANON, ch. 752).
            case CONQUERED -> true;
            case NPC -> NpcCitadels.welcomes(player, citadel.get());
            case CLAN -> citadel.get().clan != null && ClanService.byId(player.server, citadel.get().clan)
                    .filter(clan -> clan.has(player.getUUID())).isPresent();
        };
    }

    public static Optional<String> nameAt(ServerLevel realm, BlockPos gateway) {
        return at(realm, gateway).map(CitadelData.Citadel::name);
    }

    // Visiting and the Field Guide board

    /** Members who come within the visit radius of their clan's Citadel share their Field Guides with its board. */
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % VISIT_INTERVAL != 0) {
            return;
        }
        if (!DreamRealm.is(player.level())) {
            VISITING.remove(player.getUUID());
            return;
        }
        Optional<CitadelData.Citadel> here = near(player.serverLevel(), player.blockPosition(), settings().visitRadius())
                .filter(citadel -> citadel.hold == CitadelData.Hold.CLAN && citadel.clan != null);
        Optional<ClanData.Clan> clan = here.flatMap(citadel -> ClanService.byId(player.server, citadel.clan))
                .filter(found -> found.has(player.getUUID()));
        if (here.isEmpty() || clan.isEmpty()) {
            VISITING.remove(player.getUUID());
            return;
        }
        if (!here.get().id.equals(VISITING.get(player.getUUID()))) {
            visit(player, here.get(), clan.get(), false);
        }
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        VISITING.remove(event.getEntity().getUUID());
    }

    /** Shares the member's Field Guide with the clan board and tells them what they learned. */
    static void visit(ServerPlayer player, CitadelData.Citadel citadel, ClanData.Clan clan, boolean always) {
        VISITING.put(player.getUUID(), citadel.id);
        FieldGuideData.Learned learned = ClanService.shareBoard(player, clan);
        if (learned.any()) {
            List<String> parts = learned.describe();
            DreamCrossing.spell(player, Component.literal("The Field Guide board of " + citadel.name + " fills your guide: "
                    + String.join(", ", parts) + ".").withStyle(ChatFormatting.LIGHT_PURPLE));
            player.level().playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 1.0F);
        } else if (always) {
            player.displayClientMessage(Component.literal("Your Field Guide and the clan's board already agree.")
                    .withStyle(ChatFormatting.GRAY), true);
        }
    }

    // The Field Guide

    /** Logs the Citadels a player comes near as pins in their Field Guide. Returns whether any were added. */
    public static boolean logCitadels(ServerPlayer player, FieldGuideData data, String dimension) {
        if (!DreamRealm.is(player.level())) {
            return false;
        }
        int range = settings().logRange();
        boolean added = false;
        for (CitadelData.Citadel citadel : CitadelData.get(player.serverLevel()).citadels) {
            if (data.hasPin(citadel.id) || !citadel.gateway.closerThan(player.blockPosition(), range)) {
                continue;
            }
            if (data.addPin(new FieldGuideData.Pin(FieldGuideData.PinKind.CITADEL, dimension, citadel.gateway.getX(),
                    citadel.gateway.getZ(), citadel.name, Optional.of(citadel.id)))) {
                player.displayClientMessage(Component.literal("Field Guide: Citadel logged: " + citadel.name)
                        .withStyle(ChatFormatting.LIGHT_PURPLE), true);
                added = true;
            }
        }
        return added;
    }

    /** What a Citadel pin says: who holds it now. */
    public static String describe(MinecraftServer server, UUID id) {
        ServerLevel realm = DreamRealm.level(server);
        Optional<CitadelData.Citadel> citadel = realm == null ? Optional.empty() : CitadelData.get(realm).byId(id);
        if (citadel.isEmpty()) {
            return "";
        }
        return switch (citadel.get().hold) {
            case GUARDIAN -> "Held by a " + citadel.get().rank.displayName() + " Guardian";
            case CONQUERED -> "Conquered, its Gateway unclaimed";
            case NPC -> "Held by " + NpcCitadels.clanOf(server, citadel.get()).map(clan -> clan.name() + ", sworn to " + clan.greatClan())
                    .orElse("an NPC clan");
            case CLAN -> "Held by " + Optional.ofNullable(citadel.get().clan).flatMap(clan -> ClanService.byId(server, clan))
                    .map(ClanData.Clan::name).orElse("a clan");
        };
    }

    /** The region a Citadel stands in, for operators. */
    public static Optional<DreamRegion> region(CitadelData.Citadel citadel) {
        return DreamRealmContent.get().regionAt(citadel.gateway).map(Map.Entry::getValue);
    }
}
