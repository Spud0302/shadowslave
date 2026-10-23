package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hourglass's signature rule: creatures held out of time stay where they
 * are and can still be struck. Blows aimed at them are banked (Banked Blows)
 * and land all at once, a quarter harder, when time catches up. Every second
 * held is paid back after (Borrowed Time): the Hourglass's essence will not
 * refill for as long.
 */
public final class Stasis {
    static final float BANK_BONUS = 1.25F;

    record Held(ResourceKey<Level> dimension, UUID owner, Vec3 pos, long until, boolean wasNoAi, float banked) {
    }

    private static final Map<UUID, Held> HELD = new ConcurrentHashMap<>();
    /** Borrowed Time: the game time each Hourglass's refill waits for. */
    private static final Map<UUID, Long> DEBT = new ConcurrentHashMap<>();

    private Stasis() {
    }

    /** Holds a creature out of time for {@code ticks}; the hold is borrowed from the owner's refill. */
    static void hold(ServerPlayer owner, Mob mob, int ticks) {
        long now = mob.level().getGameTime();
        Held old = HELD.get(mob.getUUID());
        boolean wasNoAi = old != null ? old.wasNoAi() : mob.isNoAi();
        HELD.put(mob.getUUID(), new Held(mob.level().dimension(), owner.getUUID(), mob.position(), now + ticks, wasNoAi,
                old == null ? 0.0F : old.banked()));
        mob.setNoAi(true);
        mob.setDeltaMovement(Vec3.ZERO);
        if (KitRules.hasFlaw(owner, AspectKitCatalog.HOURGLASS.flawEffectId())) {
            long paidFrom = Math.max(DEBT.getOrDefault(owner.getUUID(), now), now + ticks);
            DEBT.put(owner.getUUID(), paidFrom + ticks);
        }
        ((ServerLevel) mob.level()).sendParticles(ParticleTypes.ENCHANT, mob.getX(), mob.getY() + mob.getBbHeight() * 0.6, mob.getZ(),
                30, 0.4, 0.5, 0.4, 0.5);
    }

    public static boolean held(Entity entity) {
        return HELD.containsKey(entity.getUUID());
    }

    static float banked(Entity entity) {
        Held held = HELD.get(entity.getUUID());
        return held == null ? 0.0F : held.banked();
    }

    /** Borrowed Time: no refill while the debt is still being paid. */
    public static float regenFactor(ServerPlayer player) {
        Long until = DEBT.get(player.getUUID());
        return until != null && until > player.serverLevel().getGameTime() ? 0.0F : 1.0F;
    }

    /** Banked Blows: a blow at a held creature is held back; held creatures land none of their own. */
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() != null && held(event.getSource().getEntity())) {
            event.setCanceled(true);
            return;
        }
        Held held = HELD.get(event.getEntity().getUUID());
        if (held == null || event.getAmount() <= 0) {
            return;
        }
        event.setCanceled(true);
        HELD.put(event.getEntity().getUUID(), new Held(held.dimension(), held.owner(), held.pos(), held.until(), held.wasNoAi(),
                held.banked() + event.getAmount()));
        ServerLevel level = (ServerLevel) event.getEntity().level();
        level.sendParticles(ParticleTypes.ENCHANTED_HIT, event.getEntity().getX(), event.getEntity().getY() + event.getEntity().getBbHeight() * 0.6,
                event.getEntity().getZ(), 8, 0.3, 0.3, 0.3, 0.1);
        level.playSound(null, event.getEntity().blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 0.6F);
    }

    /** Holds every held creature still, and lets time catch up with each when its hold ends. */
    public static void onServerTick(ServerTickEvent.Post event) {
        if (HELD.isEmpty()) {
            return;
        }
        MinecraftServer server = event.getServer();
        for (Iterator<Map.Entry<UUID, Held>> it = HELD.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<UUID, Held> entry = it.next();
            Held held = entry.getValue();
            ServerLevel level = server.getLevel(held.dimension());
            Entity entity = level == null ? null : level.getEntity(entry.getKey());
            if (!(entity instanceof Mob mob) || !mob.isAlive()) {
                it.remove();
                continue;
            }
            long now = level.getGameTime();
            if (held.until() <= now) {
                it.remove();
                release(server, mob, held);
                continue;
            }
            mob.setDeltaMovement(Vec3.ZERO);
            mob.teleportTo(held.pos().x, held.pos().y, held.pos().z);
            if (now % 4 == 0) {
                float angle = now * 0.4F;
                level.sendParticles(ParticleTypes.ENCHANT, mob.getX() + Mth.cos(angle) * 0.7, mob.getY() + mob.getBbHeight() + 0.2,
                        mob.getZ() + Mth.sin(angle) * 0.7, 3, 0.05, 0.05, 0.05, 0.3);
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, mob.getX(), mob.getY() + mob.getBbHeight() * 0.5, mob.getZ(),
                        2, 0.3, 0.4, 0.3, 0.0);
            }
        }
    }

    /** Time catches up: the creature moves again, and every banked blow lands at once. */
    private static void release(MinecraftServer server, Mob mob, Held held) {
        mob.setNoAi(held.wasNoAi());
        if (held.banked() <= 0) {
            return;
        }
        ServerPlayer owner = server.getPlayerList().getPlayer(held.owner());
        float damage = held.banked() * BANK_BONUS;
        mob.invulnerableTime = 0;
        mob.hurt(owner != null ? mob.damageSources().indirectMagic(owner, owner) : mob.damageSources().magic(), damage);
        ServerLevel level = (ServerLevel) mob.level();
        level.sendParticles(ParticleTypes.CRIT, mob.getX(), mob.getY() + mob.getBbHeight() * 0.5, mob.getZ(), 30, 0.4, 0.5, 0.4, 0.4);
        level.playSound(null, mob.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 0.6F);
    }

    /** Lets time catch up with everything at once: when the server stops, and in tests. */
    static void releaseAll(MinecraftServer server) {
        Map<UUID, Held> all = Map.copyOf(HELD);
        HELD.clear();
        all.forEach((id, held) -> {
            ServerLevel level = server.getLevel(held.dimension());
            if (level != null && level.getEntity(id) instanceof Mob mob) {
                release(server, mob, held);
            }
        });
    }

    public static void onServerStopping(ServerStoppingEvent event) {
        releaseAll(event.getServer());
    }

    public static void forget(UUID player) {
        DEBT.remove(player);
    }
}
