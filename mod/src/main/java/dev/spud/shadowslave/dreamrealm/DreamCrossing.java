package dev.spud.shadowslave.dreamrealm;

import dev.spud.shadowslave.awakening.DreamCandleRitual;
import dev.spud.shadowslave.block.ModBlocks;
import dev.spud.shadowslave.citadel.CitadelRegistry;
import dev.spud.shadowslave.citadel.CitadelService;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.trial.TrialService;
import dev.spud.shadowslave.trial.TrialWorld;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Crossing into the Dream Realm and back (DESIGN, Jack 2026-10-23; after
 * ch. 17 and 23, where the Awakened return to the Dream Realm whenever they
 * sleep). An Awakened player who sleeps beside a lit Dream Candle crosses
 * instead of skipping the night and arrives at their Dream Anchor, the last
 * Dream Candle they lit there. Their body stays asleep in the bed (a server
 * option, until Master rank, when Ascension merges body and soul, ch. 560).
 * Using a lit Dream Candle in the Dream Realm with an empty hand wakes them.
 */
public final class DreamCrossing {
    /** Ticks in bed before the dream takes hold; under the 100 vanilla needs to skip the night. */
    public static final int ENTRY_TICKS = 60;
    private static final TicketType<ChunkPos> BODY_TICKET =
            TicketType.create("shadowslave_sleeping_body", Comparator.comparingLong(ChunkPos::toLong));

    /** Why a dreamer woke, for the line they read. */
    public enum Reason {
        CANDLE("The candlelight carries you back to your body."),
        BODY_HURT("Pain drags you out of the dream. Something is hurting your body."),
        BODY_KILLED("Your sleeping body is dying."),
        RETURNED("You wake in your bed."),
        COMMAND("You wake.");

        private final String line;

        Reason(String line) {
            this.line = line;
        }
    }

    private DreamCrossing() {
    }

    public static DreamData data(ServerPlayer player) {
        return player.getData(DreamRealmRegistry.DREAM);
    }

    public static void setData(ServerPlayer player, DreamData data) {
        player.setData(DreamRealmRegistry.DREAM, data);
    }

    /** Awakened and above dream; Dormant players are still bound to the waking world. */
    public static boolean canDream(ServerPlayer player) {
        return SoulService.get(player).soulRank().filter(rank -> rank.compareTo(SoulRank.AWAKENED) >= 0).isPresent();
    }

    /** Masters and above cross whole: Ascension merges body and soul, so nothing is left behind (ch. 560). */
    static boolean leavesBody(ServerPlayer player) {
        return DreamRealmRules.sleepingBody(player.server)
                && SoulService.get(player).soulRank().filter(rank -> rank.compareTo(SoulRank.ASCENDED) < 0).isPresent();
    }

    // ---- crossing over -------------------------------------------------------------------------------------------

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.isSleeping()
                && player.getSleepTimer() == ENTRY_TICKS && !DreamRealm.is(player.level())) {
            tryCross(player);
        }
    }

    /** Crosses a sleeping player if a lit Dream Candle burns by their bed and they are Awakened. */
    public static boolean tryCross(ServerPlayer player) {
        Optional<BlockPos> bed = player.getSleepingPos();
        if (bed.isEmpty() || DreamCandleRitual.findLitCandle(player.serverLevel(), bed.get()).isEmpty()) {
            return false;
        }
        if (!canDream(player)) {
            return false;
        }
        return cross(player, GlobalPos.of(player.level().dimension(), bed.get()));
    }

    /** Sends a player into the Dream Realm; they will wake at {@code bed}. */
    public static boolean cross(ServerPlayer player, GlobalPos bed) {
        MinecraftServer server = player.server;
        ServerLevel realm = DreamRealm.level(server);
        ServerLevel bedLevel = server.getLevel(bed.dimension());
        if (realm == null || bedLevel == null || data(player).isDreaming()) {
            return false;
        }
        if (player.isSleeping()) {
            player.stopSleepInBed(true, true);
        }
        Optional<UUID> body = Optional.empty();
        if (leavesBody(player)) {
            body = Optional.of(SleepingBodyEntity.layDown(bedLevel, player, bed.pos()).getUUID());
            bedLevel.getChunkSource().addRegionTicket(BODY_TICKET, new ChunkPos(bed.pos()), 2, new ChunkPos(bed.pos()));
        }
        setData(player, data(player).dreamingFrom(bed, body));

        Vec3 arrival = arrival(player, realm, bed.pos());
        player.teleportTo(realm, arrival.x, arrival.y, arrival.z, Set.of(), player.getYRot(), 0.0F);
        player.resetFallDistance();
        realm.playSound(null, BlockPos.containing(arrival), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 0.5F);
        realm.sendParticles(ParticleTypes.REVERSE_PORTAL, arrival.x, arrival.y + 1.0, arrival.z, 60, 0.4, 0.8, 0.4, 0.05);

        Optional<DreamRegion> region = DreamRealmContent.get().regionAt(BlockPos.containing(arrival)).map(java.util.Map.Entry::getValue);
        title(player, Component.literal("The Dream Realm").withStyle(ChatFormatting.DARK_PURPLE),
                Component.literal(region.map(DreamRegion::displayName).orElse("")).withStyle(ChatFormatting.GRAY));
        region.filter(found -> !found.arrival().isEmpty()).ifPresent(found ->
                spell(player, Component.literal(found.arrival())));
        if (body.isPresent()) {
            spell(player, Component.literal("Your body sleeps on in the waking world. If it is hurt, you will wake."));
        }
        return true;
    }

    /** Where a dreamer appears: beside their Dream Anchor, or, the first time, over their bed, where a new anchor is lit. */
    static Vec3 arrival(ServerPlayer player, ServerLevel realm, BlockPos bed) {
        Optional<BlockPos> anchor = data(player).anchor();
        if (anchor.isPresent()) {
            realm.getChunk(anchor.get());
            if (realm.getBlockState(anchor.get()).is(CitadelRegistry.GATEWAY.get())) {
                // A Citadel's Gateway holds the anchor only while the dreamer's clan holds the Citadel.
                if (CitadelService.welcomes(player, realm, anchor.get())) {
                    return standBeside(realm, anchor.get());
                }
                spell(player, Component.literal(CitadelService.nameAt(realm, anchor.get()).orElse("That Citadel")
                        + " is no longer your clan's. Its Gateway lets your anchor go.").withStyle(ChatFormatting.GRAY));
                setData(player, new DreamData(Optional.empty(), data(player).seedsOptIn(), data(player).dreaming()));
                return arrival(player, realm, bed);
            }
            if (!isLitCandle(realm.getBlockState(anchor.get()))) {
                spell(player, Component.literal("Your Dream Anchor has gone dark. Light a Dream Candle to set a new one.")
                        .withStyle(ChatFormatting.GRAY));
            }
            return standBeside(realm, anchor.get());
        }
        BlockPos column = DreamRealm.toRealm(bed, DreamRealmRules.ratio(player.server));
        BlockPos candle = firstAnchor(realm, column);
        setData(player, data(player).withAnchor(candle));
        spell(player, Component.literal("A Dream Candle burns where you arrived. It is your Dream Anchor: you return here each time you dream.")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        spell(player, Component.literal("Use a lit Dream Candle with an empty hand to wake. Light another to move your anchor.")
                .withStyle(ChatFormatting.GRAY));
        return standBeside(realm, candle);
    }

    /** Lights the first anchor candle on solid ground in a column of the Dream Realm. */
    static BlockPos firstAnchor(ServerLevel realm, BlockPos column) {
        realm.getChunk(column);
        int y = realm.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column.getX(), column.getZ());
        if (y <= realm.getMinBuildHeight() + 1) {
            y = realm.getSeaLevel() + 1;
        }
        BlockPos ground = new BlockPos(column.getX(), y - 1, column.getZ());
        if (!realm.getBlockState(ground).isFaceSturdy(realm, ground, Direction.UP)) {
            // Water or nothing at all: a small floor of tuff to stand on.
            for (BlockPos pos : BlockPos.betweenClosed(ground.offset(-1, 0, -1), ground.offset(2, 0, 1))) {
                realm.setBlockAndUpdate(pos, Blocks.TUFF.defaultBlockState());
            }
        }
        BlockPos candle = ground.above().east();
        if (!realm.getBlockState(candle.below()).isFaceSturdy(realm, candle.below(), Direction.UP)) {
            realm.setBlockAndUpdate(candle.below(), Blocks.TUFF.defaultBlockState());
        }
        realm.setBlockAndUpdate(candle, ModBlocks.DREAM_CANDLE.get().defaultBlockState().setValue(CandleBlock.LIT, true));
        return candle.immutable();
    }

    /** A spot to stand next to a block: two blocks of room over solid ground, nearest first. */
    public static Vec3 standBeside(ServerLevel level, BlockPos pos) {
        for (int radius = 1; radius <= 2; radius++) {
            for (int dy = 0; dy >= -1; dy--) {
                for (BlockPos spot : BlockPos.betweenClosed(pos.offset(-radius, dy, -radius), pos.offset(radius, dy, radius))) {
                    if (Math.max(Math.abs(spot.getX() - pos.getX()), Math.abs(spot.getZ() - pos.getZ())) != radius) {
                        continue;
                    }
                    if (level.getBlockState(spot).getCollisionShape(level, spot).isEmpty()
                            && level.getBlockState(spot.above()).getCollisionShape(level, spot.above()).isEmpty()
                            && level.getBlockState(spot.below()).isFaceSturdy(level, spot.below(), Direction.UP)) {
                        return Vec3.atBottomCenterOf(spot);
                    }
                }
            }
        }
        return Vec3.atBottomCenterOf(pos.above());
    }

    // ---- waking --------------------------------------------------------------------------------------------------

    /** Wakes a dreamer in their bed, taking up their body. Returns false if they were not dreaming. */
    public static boolean wake(ServerPlayer player, Reason reason) {
        Optional<DreamData.Dreaming> dreaming = data(player).dreaming();
        if (dreaming.isEmpty()) {
            return false;
        }
        // A dreamer inside a Nightmare is pulled out of it first.
        TrialService.interrupt(player);
        setData(player, data(player).awake());
        ServerLevel bedLevel = player.server.getLevel(dreaming.get().bed().dimension());
        if (bedLevel == null) {
            bedLevel = player.server.overworld();
        }
        Optional<Float> bodyHealth = releaseBody(bedLevel, dreaming.get());
        BlockPos bed = dreaming.get().bed().pos();
        Vec3 stand = standUp(bedLevel, bed, player);
        player.teleportTo(bedLevel, stand.x, stand.y, stand.z, Set.of(), player.getYRot(), player.getXRot());
        player.resetFallDistance();
        bodyHealth.ifPresent(health -> player.setHealth(Math.min(player.getHealth(), Math.max(1.0F, health))));
        bedLevel.playSound(null, bed, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 0.6F);
        player.displayClientMessage(Component.literal(reason.line).withStyle(
                reason == Reason.BODY_HURT || reason == Reason.BODY_KILLED ? ChatFormatting.RED : ChatFormatting.GRAY), true);
        return true;
    }

    /** Removes a dreamer's body and its chunk ticket; returns the body's health if it was found. */
    private static Optional<Float> releaseBody(ServerLevel bedLevel, DreamData.Dreaming dreaming) {
        if (dreaming.body().isEmpty()) {
            return Optional.empty();
        }
        ChunkPos chunk = new ChunkPos(dreaming.bed().pos());
        bedLevel.getChunkSource().removeRegionTicket(BODY_TICKET, chunk, 2, chunk);
        Entity entity = bedLevel.getEntity(dreaming.body().get());
        if (entity instanceof SleepingBodyEntity body) {
            float health = body.getHealth();
            if (body.isAlive()) {
                body.stopSleeping();
                body.discard();
            }
            return Optional.of(health);
        }
        return Optional.empty();
    }

    private static Vec3 standUp(ServerLevel level, BlockPos bed, ServerPlayer player) {
        BlockState state = level.getBlockState(bed);
        if (state.getBlock() instanceof BedBlock) {
            Optional<Vec3> spot = BedBlock.findStandUpPosition(EntityType.PLAYER, level, bed, state.getValue(BedBlock.FACING), player.getYRot());
            if (spot.isPresent()) {
                return spot.get();
            }
        }
        return standBeside(level, bed);
    }

    // ---- the sleeping body ---------------------------------------------------------------------------------------

    /** Whether a body still belongs to a dreamer who is online and dreaming in it. */
    static boolean isBodyOf(SleepingBodyEntity body, MinecraftServer server) {
        return owner(body, server).isPresent();
    }

    private static Optional<ServerPlayer> owner(SleepingBodyEntity body, MinecraftServer server) {
        return body.owner()
                .map(uuid -> server.getPlayerList().getPlayer(uuid))
                .filter(player -> data(player).dreaming().flatMap(DreamData.Dreaming::body).filter(body.getUUID()::equals).isPresent());
    }

    /** A wound to the body pulls its dreamer back awake, once the tick that hurt it is over. */
    static void bodyHurt(SleepingBodyEntity body, MinecraftServer server) {
        owner(body, server).ifPresent(player -> later(server, () -> wake(player, Reason.BODY_HURT)));
    }

    /**
     * If the body dies, so does its dreamer, where they stand in the Dream
     * Realm: what they carried stays there, by their anchor's road, and they
     * respawn as usual. Dying ends the dream (see {@link #onLivingDeath}).
     */
    static void bodyDied(SleepingBodyEntity body, DamageSource cause, MinecraftServer server) {
        owner(body, server).ifPresent(player -> later(server, () -> {
            TrialService.interrupt(player);
            player.displayClientMessage(Component.literal(Reason.BODY_KILLED.line).withStyle(ChatFormatting.RED), true);
            if (player.isChangingDimension()) {
                // Still shielded by a crossing the client has not confirmed; the dream has them now.
                player.hasChangedDimension();
            }
            DamageSource severed = new DamageSource(player.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                    .getHolderOrThrow(DreamRealmRegistry.SEVERED_DREAM), cause.getEntity());
            player.hurt(severed, Float.MAX_VALUE);
            endWithoutWaking(player);
        }));
    }

    private static void later(MinecraftServer server, Runnable task) {
        server.tell(new TickTask(server.getTickCount() + 1, task));
    }

    // ---- candles in the Dream Realm ------------------------------------------------------------------------------

    /**
     * In the Dream Realm, using a lit Dream Candle with an empty hand wakes you
     * (sneaking still meditates, see SoulSeaService), and lighting one makes it
     * your Dream Anchor.
     */
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !DreamRealm.is(player.level())
                || event.getHand() != InteractionHand.MAIN_HAND || player.isShiftKeyDown()) {
            return;
        }
        BlockPos pos = event.getPos();
        BlockState state = player.level().getBlockState(pos);
        if (!state.is(ModBlocks.DREAM_CANDLE.get())) {
            return;
        }
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty() && state.getValue(CandleBlock.LIT)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            if (!wake(player, Reason.CANDLE)) {
                player.displayClientMessage(Component.literal("You are not dreaming; this candle cannot wake you.")
                        .withStyle(ChatFormatting.GRAY), true);
            }
        } else if (held.getItem() instanceof FlintAndSteelItem || held.getItem() instanceof FireChargeItem) {
            BlockPos candle = pos.immutable();
            later(player.server, () -> {
                if (player.level() == event.getLevel() && isLitCandle(player.level().getBlockState(candle))) {
                    anchorAt(player, candle);
                }
            });
        }
    }

    /** Makes a lit Dream Candle in the Dream Realm a player's anchor. */
    public static void anchorAt(ServerPlayer player, BlockPos candle) {
        if (data(player).anchor().filter(candle::equals).isPresent()) {
            return;
        }
        setData(player, data(player).withAnchor(candle));
        player.displayClientMessage(Component.literal("Your Dream Anchor is set here.").withStyle(ChatFormatting.LIGHT_PURPLE), true);
        player.serverLevel().sendParticles(ParticleTypes.SOUL_FIRE_FLAME, candle.getX() + 0.5, candle.getY() + 0.6,
                candle.getZ() + 0.5, 10, 0.1, 0.2, 0.1, 0.01);
    }

    static boolean isLitCandle(BlockState state) {
        return state.is(ModBlocks.DREAM_CANDLE.get()) && state.getValue(CandleBlock.LIT);
    }

    // ---- leaving the dream other ways ----------------------------------------------------------------------------

    /** Logging out lets the body go; logging back in wakes the dreamer in their bed. */
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            data(player).dreaming().ifPresent(dreaming -> {
                ServerLevel bedLevel = player.server.getLevel(dreaming.bed().dimension());
                if (bedLevel != null) {
                    releaseBody(bedLevel, dreaming);
                }
            });
        }
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        // A challenger who comes back into a Long Nightmare stays in the dream: the Nightmare kept them.
        if (event.getEntity() instanceof ServerPlayer player && data(player).isDreaming() && !TrialService.inside(player)) {
            wake(player, Reason.RETURNED);
        }
    }

    /** Dying in the Dream Realm ends the dream; you respawn as usual and your body is gone from the bed. */
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            endWithoutWaking(player);
        }
    }

    /**
     * Leaving the Dream Realm by any other road (a command, a portal) ends the
     * dream where you are. A Nightmare is a dream within the dream: going into
     * one keeps the dream, and its challengers come back to the Dream Realm.
     */
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getFrom().equals(DreamRealm.key(player.server))
                && !event.getTo().equals(DreamRealm.key(player.server)) && !event.getTo().equals(TrialWorld.key(player.server))) {
            endWithoutWaking(player);
        }
    }

    private static void endWithoutWaking(ServerPlayer player) {
        Optional<DreamData.Dreaming> dreaming = data(player).dreaming();
        if (dreaming.isEmpty()) {
            return;
        }
        setData(player, data(player).awake());
        ServerLevel bedLevel = player.server.getLevel(dreaming.get().bed().dimension());
        if (bedLevel != null) {
            releaseBody(bedLevel, dreaming.get());
        }
    }

    // ---- the Spell's voice ---------------------------------------------------------------------------------------

    public static void spell(ServerPlayer player, Component line) {
        player.sendSystemMessage(line.copy().withStyle(style -> style.withItalic(true)
                .withColor(line.getStyle().getColor() == null
                        ? net.minecraft.network.chat.TextColor.fromLegacyFormat(ChatFormatting.GRAY)
                        : line.getStyle().getColor())));
    }

    public static void title(ServerPlayer player, Component title, Component subtitle) {
        player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 60, 20));
        player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
        player.connection.send(new ClientboundSetTitleTextPacket(title));
    }
}
