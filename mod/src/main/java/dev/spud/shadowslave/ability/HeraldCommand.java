package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The always-on halves of the Herald: Command Presence (allies fighting near
 * you strike harder), Burden of Command (an ally's death near you weakens
 * you), and the bond Oath of Arms lays between a Herald and an ally.
 */
public final class HeraldCommand {
    static final double PRESENCE_RADIUS = 8.0;
    static final float PRESENCE_BONUS = 1.15F;
    static final double BURDEN_RADIUS = 16.0;
    static final int BURDEN_TICKS = 20 * 30;
    /** Oath of Arms: the share of each blow on the sworn ally that the Herald takes instead. */
    static final float OATH_SHARE = 0.5F;

    /** A Herald's oath: the ally sworn to, and when it ends. */
    record Oath(UUID ally, long until) {
    }

    private static final Map<UUID, Oath> OATHS = new ConcurrentHashMap<>();
    /** Set while a Herald takes their share, so a Herald sworn to a Herald cannot pass it back forever. */
    private static boolean sharing;

    private HeraldCommand() {
    }

    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (event.getSource().getEntity() instanceof LivingEntity attacker && commanded(attacker)) {
            event.setAmount(event.getAmount() * PRESENCE_BONUS);
        }
        if (!sharing) {
            swornHerald(event.getEntity()).ifPresent(herald -> {
                float share = event.getAmount() * OATH_SHARE;
                event.setAmount(event.getAmount() - share);
                sharing = true;
                try {
                    herald.hurt(herald.damageSources().magic(), share);
                } finally {
                    sharing = false;
                }
                ((ServerLevel) herald.level()).sendParticles(ParticleTypes.ENCHANTED_HIT, event.getEntity().getX(),
                        event.getEntity().getY() + 1.0, event.getEntity().getZ(), 6, 0.3, 0.4, 0.3, 0.1);
            });
        }
    }

    /** Command Presence: an ally fights within reach of a Herald who is not themselves. */
    static boolean commanded(LivingEntity attacker) {
        if (!(attacker instanceof Player) && !(attacker instanceof TamableAnimal)) {
            return false;
        }
        return !attacker.level().getEntitiesOfClass(ServerPlayer.class, attacker.getBoundingBox().inflate(PRESENCE_RADIUS),
                herald -> herald != attacker && herald.isAlive() && CarryTheFlameAbility.isAlly(herald, attacker)
                        && KitRules.hasInnate(herald, AspectKitCatalog.HERALD.innateId())).isEmpty();
    }

    /** Burden of Command: a Herald who sees an ally fall nearby is weakened. */
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        if (dead.level().isClientSide() || (!(dead instanceof Player) && !(dead instanceof TamableAnimal))) {
            return;
        }
        for (ServerPlayer herald : dead.level().getEntitiesOfClass(ServerPlayer.class, dead.getBoundingBox().inflate(BURDEN_RADIUS),
                herald -> herald != dead && herald.isAlive() && CarryTheFlameAbility.isAlly(herald, dead)
                        && KitRules.hasFlaw(herald, AspectKitCatalog.HERALD.flawEffectId()))) {
            burden(herald);
        }
    }

    static void burden(ServerPlayer herald) {
        herald.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, BURDEN_TICKS, 0, false, true, true));
        herald.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, BURDEN_TICKS, 0, false, true, true));
        AbilityService.feedback(herald, "One you led has fallen. The weight of it slows you.", ChatFormatting.DARK_RED);
    }

    // ---- Oath of Arms ---------------------------------------------------------------------------------------------

    static void swear(ServerPlayer herald, LivingEntity ally, int ticks) {
        OATHS.put(herald.getUUID(), new Oath(ally.getUUID(), herald.serverLevel().getGameTime() + ticks));
    }

    static Optional<Oath> oath(ServerPlayer herald) {
        Oath oath = OATHS.get(herald.getUUID());
        if (oath != null && oath.until() <= herald.serverLevel().getGameTime()) {
            OATHS.remove(herald.getUUID());
            return Optional.empty();
        }
        return Optional.ofNullable(oath);
    }

    static void release(UUID herald) {
        OATHS.remove(herald);
    }

    /** The Herald (online, alive, in the same world) who has sworn to take a share of this creature's blows. */
    static Optional<ServerPlayer> swornHerald(LivingEntity ally) {
        if (OATHS.isEmpty() || ally.getServer() == null) {
            return Optional.empty();
        }
        long now = ally.level().getGameTime();
        for (Map.Entry<UUID, Oath> entry : OATHS.entrySet()) {
            if (!entry.getValue().ally().equals(ally.getUUID()) || entry.getValue().until() <= now) {
                continue;
            }
            ServerPlayer herald = ally.getServer().getPlayerList().getPlayer(entry.getKey());
            if (herald != null && herald != ally && herald.isAlive() && herald.level() == ally.level()) {
                return Optional.of(herald);
            }
        }
        return Optional.empty();
    }

    public static void forget(UUID player) {
        OATHS.remove(player);
    }
}
