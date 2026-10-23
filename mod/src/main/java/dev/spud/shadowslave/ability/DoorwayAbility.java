package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Threshold, Awakened (movement): the first press opens a door where you
 * stand (free); the second, within a minute and in the same world, opens its
 * twin and pays. For thirty seconds you and your allies who walk into either
 * door step out of the other.
 */
public final class DoorwayAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.DOORWAY.abilityId();
    static final int ESSENCE_COST = 40;
    static final int PENDING_TICKS = 20 * 60;
    static final int OPEN_TICKS = 20 * 30;
    static final double DOOR_REACH = 0.9;
    static final int STEP_COOLDOWN = 30;

    record Pending(ResourceKey<Level> dimension, Vec3 at, long until) {
    }

    record Doors(ResourceKey<Level> dimension, Vec3 a, Vec3 b, long until) {
    }

    private static final Map<UUID, Pending> PENDING = new ConcurrentHashMap<>();
    private static final Map<UUID, Doors> OPEN = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> STEPPED = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.DOORWAY.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        long now = level.getGameTime();
        if (OPEN.containsKey(player.getUUID())) {
            AbilityService.feedback(player, "Your doors are still open.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        Pending pending = PENDING.get(player.getUUID());
        if (pending == null || pending.until() <= now || !pending.dimension().equals(level.dimension())) {
            PENDING.put(player.getUUID(), new Pending(level.dimension(), player.position(), now + PENDING_TICKS));
            level.playSound(null, player.blockPosition(), SoundEvents.WOODEN_DOOR_OPEN, SoundSource.PLAYERS, 1.0F, 0.6F);
            AbilityService.feedback(player, "A door opens behind you. Press again elsewhere to open its twin.", ChatFormatting.LIGHT_PURPLE);
            return Activation.PRIMED;
        }
        if (pending.at().distanceToSqr(player.position()) < 9.0) {
            AbilityService.feedback(player, "The twin needs to open further away.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        PENDING.remove(player.getUUID());
        OPEN.put(player.getUUID(), new Doors(level.dimension(), pending.at(), player.position(), now + OPEN_TICKS));
        STEPPED.put(player.getUUID(), now);
        PocketSpace.doorOpened(player);
        level.playSound(null, player.blockPosition(), SoundEvents.WOODEN_DOOR_OPEN, SoundSource.PLAYERS, 1.0F, 0.8F);
        level.playSound(null, player.blockPosition(), SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS, 0.3F, 1.6F);
        return Activation.USED;
    }

    @Override
    public void tick(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        long now = level.getGameTime();
        Pending pending = PENDING.get(player.getUUID());
        if (pending != null) {
            if (pending.until() <= now) {
                PENDING.remove(player.getUUID());
                AbilityService.feedback(player, "The lone door closes.", ChatFormatting.GRAY);
                AbilityService.syncStatus(player);
            } else if (now % 4 == 0 && pending.dimension().equals(level.dimension())) {
                frame(level, pending.at(), now);
            }
        }
        Doors doors = OPEN.get(player.getUUID());
        if (doors == null) {
            return;
        }
        if (doors.until() <= now) {
            OPEN.remove(player.getUUID());
            AbilityService.feedback(player, "Your doors close.", ChatFormatting.GRAY);
            return;
        }
        if (!doors.dimension().equals(level.dimension())) {
            return;
        }
        if (now % 4 == 0) {
            frame(level, doors.a(), now);
            frame(level, doors.b(), now);
        }
        step(player, level, doors.a(), doors.b(), now);
        step(player, level, doors.b(), doors.a(), now);
    }

    /** Anyone allowed through who stands in one door steps out of the other. */
    private static void step(ServerPlayer owner, ServerLevel level, Vec3 from, Vec3 to, long now) {
        AABB door = new AABB(from.x - DOOR_REACH, from.y, from.z - DOOR_REACH, from.x + DOOR_REACH, from.y + 2.0, from.z + DOOR_REACH);
        for (LivingEntity walker : level.getEntitiesOfClass(LivingEntity.class, door,
                entity -> entity == owner || CarryTheFlameAbility.isAlly(owner, entity))) {
            Long last = STEPPED.get(walker.getUUID());
            if (last != null && now - last < STEP_COOLDOWN) {
                // Still standing in a door just stepped through: wait until they walk out and back in.
                STEPPED.put(walker.getUUID(), now);
                continue;
            }
            STEPPED.put(walker.getUUID(), now);
            walker.teleportTo(to.x, to.y, to.z);
            walker.resetFallDistance();
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, to.x, to.y + 1.0, to.z, 20, 0.3, 0.6, 0.3, 0.05);
            level.playSound(null, BlockPos.containing(to), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5F, 1.3F);
        }
    }

    /** A tall ring of light where a door stands. */
    private static void frame(ServerLevel level, Vec3 at, long now) {
        for (int i = 0; i < 12; i++) {
            float angle = i * Mth.TWO_PI / 12 + now * 0.05F;
            double x = at.x + Mth.cos(angle) * 0.6;
            double y = at.y + 1.0 + Mth.sin(angle) * 1.0;
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, x, y, at.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
        level.sendParticles(ParticleTypes.PORTAL, at.x, at.y + 1.0, at.z, 3, 0.2, 0.5, 0.2, 0.2);
    }

    @Override
    public void clear(ServerPlayer player) {
        PENDING.remove(player.getUUID());
        OPEN.remove(player.getUUID());
    }

    @Override
    public boolean primed(ServerPlayer player) {
        return PENDING.containsKey(player.getUUID());
    }

    static boolean open(ServerPlayer player) {
        return OPEN.containsKey(player.getUUID());
    }

    /** Pretends the player walked into a door now, for tests: steps everyone standing in either. */
    static void stepNow(ServerPlayer player) {
        Doors doors = OPEN.get(player.getUUID());
        if (doors != null) {
            STEPPED.clear();
            long now = player.serverLevel().getGameTime();
            step(player, player.serverLevel(), doors.a(), doors.b(), now);
            step(player, player.serverLevel(), doors.b(), doors.a(), now);
        }
    }
}
