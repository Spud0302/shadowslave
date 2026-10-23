package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.network.payload.WaymarkPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wayfarer's Dormant ability. A press sets a Waymark where you stand (the
 * oldest goes once there are three); you and anyone near you see them through
 * walls. Sneak and press to snap back to your newest Waymark within
 * {@link #SNAP_RANGE} blocks: an escape you set up in advance.
 */
public final class WaymarkAbility implements AspectAbility {
    public static final ResourceLocation ID = AspectKitCatalog.WAYFARER.abilityId();
    static final int ESSENCE_COST = 20;
    static final int MAX_MARKS = 3;
    static final double SNAP_RANGE = 64.0;
    /** Players this close to a Wayfarer see their Waymarks too. */
    static final double SHARE_RANGE = 64.0;
    static final int SYNC_TICKS = 20;

    record Mark(ResourceKey<Level> dimension, BlockPos pos) {
    }

    private static final Map<UUID, Deque<Mark>> MARKS = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return AspectKitCatalog.WAYFARER.abilityName();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        return player.isShiftKeyDown() ? snap(player) : place(player);
    }

    private static Activation place(ServerPlayer player) {
        Deque<Mark> marks = MARKS.computeIfAbsent(player.getUUID(), id -> new ArrayDeque<>());
        marks.addLast(new Mark(player.level().dimension(), player.blockPosition()));
        while (marks.size() > MAX_MARKS) {
            marks.removeFirst();
        }
        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 0.2, player.getZ(), 20, 0.2, 0.6, 0.2, 0.02);
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.2F);
        AbilityService.feedback(player, "Waymark set (" + marks.size() + " of " + MAX_MARKS + "). Sneak and press to return.",
                ChatFormatting.GOLD);
        sync(player);
        return Activation.USED;
    }

    private static Activation snap(ServerPlayer player) {
        Optional<Mark> mark = newestWithin(player, SNAP_RANGE);
        if (mark.isEmpty()) {
            AbilityService.feedback(player, "No Waymark of yours within " + (int) SNAP_RANGE + " blocks.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        MARKS.get(player.getUUID()).remove(mark.get());
        ServerLevel level = player.serverLevel();
        Vec3 from = player.position();
        Vec3 to = Vec3.atBottomCenterOf(mark.get().pos());
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, from.x, from.y + 1.0, from.z, 30, 0.3, 0.6, 0.3, 0.05);
        player.teleportTo(to.x, to.y, to.z);
        player.resetFallDistance();
        level.sendParticles(ParticleTypes.END_ROD, to.x, to.y + 1.0, to.z, 24, 0.3, 0.6, 0.3, 0.04);
        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.7F, 1.3F);
        sync(player);
        return Activation.USED;
    }

    /** The newest Waymark in the player's dimension within {@code range} blocks. */
    static Optional<Mark> newestWithin(ServerPlayer player, double range) {
        Deque<Mark> marks = MARKS.get(player.getUUID());
        if (marks == null) {
            return Optional.empty();
        }
        var it = marks.descendingIterator();
        while (it.hasNext()) {
            Mark mark = it.next();
            if (mark.dimension().equals(player.level().dimension())
                    && mark.pos().distToCenterSqr(player.position()) <= range * range) {
                return Optional.of(mark);
            }
        }
        return Optional.empty();
    }

    /** This player's Waymarks in their current dimension, oldest first. */
    static List<BlockPos> marksHere(ServerPlayer player) {
        Deque<Mark> marks = MARKS.get(player.getUUID());
        if (marks == null) {
            return List.of();
        }
        return marks.stream().filter(mark -> mark.dimension().equals(player.level().dimension())).map(Mark::pos).toList();
    }

    @Override
    public void tick(ServerPlayer player) {
        if (player.tickCount % SYNC_TICKS == 0 && MARKS.containsKey(player.getUUID())) {
            sync(player);
        }
    }

    @Override
    public void clear(ServerPlayer player) {
        if (MARKS.remove(player.getUUID()) != null && player.connection != null) {
            sync(player);
        }
    }

    /** Shows the Wayfarer's Waymarks to them and to every player near them. */
    private static void sync(ServerPlayer player) {
        WaymarkPayload payload = new WaymarkPayload(player.getId(), marksHere(player));
        for (ServerPlayer viewer : player.serverLevel().players()) {
            if (viewer.distanceToSqr(player) <= SHARE_RANGE * SHARE_RANGE
                    && viewer.connection != null && viewer.connection.hasChannel(WaymarkPayload.TYPE)) {
                PacketDistributor.sendToPlayer(viewer, payload);
            }
        }
    }
}
