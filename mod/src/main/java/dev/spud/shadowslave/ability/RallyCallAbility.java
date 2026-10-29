package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import dev.spud.shadowslave.dreamrealm.DreamCrossing;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Herald, Awakened (movement): sound a call. Every other player within
 * {@link #CALL_RANGE} blocks in your world hears it, and those who answer by
 * sneaking within ten seconds are drawn to your side; nobody is pulled who
 * did not choose to come. Your pets within {@link #PET_RANGE} come at once.
 */
public final class RallyCallAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.RALLY_CALL.abilityId();
    static final int ESSENCE_COST = 40;
    static final double CALL_RANGE = 256.0;
    static final double PET_RANGE = 64.0;
    static final int ANSWER_TICKS = 20 * 10;

    record Call(long until, Set<UUID> waiting) {
    }

    private static final Map<UUID, Call> CALLS = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.RALLY_CALL.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        if (CALLS.containsKey(player.getUUID())) {
            AbilityService.feedback(player, "Your call still carries.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        ServerLevel level = player.serverLevel();
        List<ServerPlayer> hearers = level.players().stream()
                .filter(other -> other != player && !other.isSpectator() && other.isAlive()
                        && other.distanceToSqr(player) <= CALL_RANGE * CALL_RANGE)
                .toList();
        List<TamableAnimal> pets = level.getEntitiesOfClass(TamableAnimal.class, player.getBoundingBox().inflate(PET_RANGE),
                pet -> pet.isOwnedBy(player) && pet.isAlive() && !pet.isOrderedToSit());
        if (hearers.isEmpty() && pets.isEmpty()) {
            AbilityService.feedback(player, "No one is near enough to hear your call.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        Set<UUID> waiting = ConcurrentHashMap.newKeySet();
        for (ServerPlayer hearer : hearers) {
            waiting.add(hearer.getUUID());
            hearer.displayClientMessage(Component.literal(player.getGameProfile().getName()
                    + " sounds a Rally Call. Sneak within ten seconds to answer.").withStyle(ChatFormatting.GOLD), true);
            level.playSound(null, hearer.blockPosition(), SoundEvents.RAID_HORN.value(), SoundSource.PLAYERS, 0.4F, 1.2F);
        }
        pets.forEach(pet -> answer(player, pet));
        CALLS.put(player.getUUID(), new Call(level.getGameTime() + ANSWER_TICKS, waiting));
        level.playSound(null, player.blockPosition(), SoundEvents.RAID_HORN.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
        return Activation.USED;
    }

    @Override
    public void tick(ServerPlayer player) {
        Call call = CALLS.get(player.getUUID());
        if (call == null) {
            return;
        }
        if (call.until() <= player.serverLevel().getGameTime()) {
            CALLS.remove(player.getUUID());
            return;
        }
        for (UUID id : new HashSet<>(call.waiting())) {
            ServerPlayer hearer = player.server.getPlayerList().getPlayer(id);
            if (hearer == null || hearer.level() != player.level()) {
                call.waiting().remove(id);
            } else if (hearer.isShiftKeyDown()) {
                call.waiting().remove(id);
                answer(player, hearer);
            }
        }
    }

    /** Draws one who answered (or a pet) to the Herald's side. */
    static void answer(ServerPlayer herald, net.minecraft.world.entity.Entity who) {
        ServerLevel level = herald.serverLevel();
        level.sendParticles(ParticleTypes.CLOUD, who.getX(), who.getY() + 1.0, who.getZ(), 12, 0.3, 0.5, 0.3, 0.02);
        Vec3 spot = DreamCrossing.standBeside(level, herald.blockPosition());
        if (who instanceof ServerPlayer answering) {
            answering.teleportTo(level, spot.x, spot.y, spot.z, Set.of(), answering.getYRot(), answering.getXRot());
            answering.displayClientMessage(Component.literal("You answer the call.").withStyle(ChatFormatting.GOLD), true);
        } else {
            who.teleportTo(spot.x, spot.y, spot.z);
        }
        who.resetFallDistance();
        level.sendParticles(ParticleTypes.FLAME, spot.x, spot.y + 1.0, spot.z, 12, 0.3, 0.5, 0.3, 0.02);
    }

    /** The players still free to answer, for tests. */
    static Set<UUID> waiting(ServerPlayer herald) {
        Call call = CALLS.get(herald.getUUID());
        return call == null ? Set.of() : call.waiting();
    }

    @Override
    public void clear(ServerPlayer player) {
        CALLS.remove(player.getUUID());
    }

    @Override
    public boolean primed(ServerPlayer player) {
        return CALLS.containsKey(player.getUUID());
    }
}
