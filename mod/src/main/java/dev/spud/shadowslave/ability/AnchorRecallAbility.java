package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import dev.spud.shadowslave.dreamrealm.DreamCrossing;
import dev.spud.shadowslave.dreamrealm.DreamRealm;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wayfarer, Awakened (escape): a three second channel that carries you home.
 * In the Dream Realm home is your Dream Anchor; in the waking world it is your
 * bed (or other respawn point) when it lies in the same world. Taking a hit
 * breaks the channel, and a broken channel costs nothing.
 */
public final class AnchorRecallAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.ANCHOR_RECALL.abilityId();
    static final int ESSENCE_COST = 40;
    static final int CHANNEL_TICKS = 60;

    record Channel(long until, BlockPos home) {
    }

    private static final Map<UUID, Channel> CHANNELS = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.ANCHOR_RECALL.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        if (CHANNELS.containsKey(player.getUUID())) {
            AbilityService.feedback(player, "You are already finding your way home.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        Optional<BlockPos> home = home(player);
        if (home.isEmpty()) {
            AbilityService.feedback(player, DreamRealm.is(player.level()) ? "You have no Dream Anchor to return to."
                    : "Your bed does not lie in this world.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        CHANNELS.put(player.getUUID(), new Channel(player.serverLevel().getGameTime() + CHANNEL_TICKS, home.get()));
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 1.0F, 1.4F);
        AbilityService.feedback(player, "Finding your way home...", ChatFormatting.GOLD);
        return Activation.PRIMED;
    }

    /** Where the recall leads: the Dream Anchor in the Dream Realm, else the respawn point in this world. */
    static Optional<BlockPos> home(ServerPlayer player) {
        if (DreamRealm.is(player.level())) {
            return DreamCrossing.data(player).anchor();
        }
        BlockPos bed = player.getRespawnPosition();
        if (bed == null || !player.getRespawnDimension().equals(player.level().dimension())) {
            return Optional.empty();
        }
        return Optional.of(bed);
    }

    @Override
    public void tick(ServerPlayer player) {
        Channel channel = CHANNELS.get(player.getUUID());
        if (channel == null) {
            return;
        }
        ServerLevel level = player.serverLevel();
        if (player.hurtTime > 0) {
            CHANNELS.remove(player.getUUID());
            AbilityService.feedback(player, "The way home slips away.", ChatFormatting.GRAY);
            AbilityService.syncStatus(player);
            return;
        }
        long left = channel.until() - level.getGameTime();
        if (left > 0) {
            float angle = level.getGameTime() * 0.6F;
            double radius = 0.4 + 0.6 * left / CHANNEL_TICKS;
            for (int i = 0; i < 3; i++) {
                float a = angle + i * Mth.TWO_PI / 3;
                level.sendParticles(ParticleTypes.END_ROD, player.getX() + Mth.cos(a) * radius,
                        player.getY() + 0.2 + (CHANNEL_TICKS - left) * 0.03, player.getZ() + Mth.sin(a) * radius,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
            return;
        }
        CHANNELS.remove(player.getUUID());
        Vec3 to = DreamCrossing.standBeside(level, channel.home());
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 40, 0.3, 0.7, 0.3, 0.05);
        player.teleportTo(to.x, to.y, to.z);
        player.resetFallDistance();
        level.sendParticles(ParticleTypes.END_ROD, to.x, to.y + 1.0, to.z, 30, 0.3, 0.7, 0.3, 0.04);
        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 0.9F);
        AbilityService.finish(player, this);
    }

    @Override
    public void clear(ServerPlayer player) {
        CHANNELS.remove(player.getUUID());
    }

    @Override
    public boolean primed(ServerPlayer player) {
        return CHANNELS.containsKey(player.getUUID());
    }
}
