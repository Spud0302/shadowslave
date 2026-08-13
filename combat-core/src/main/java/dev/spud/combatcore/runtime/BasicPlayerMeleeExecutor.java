package dev.spud.combatcore.runtime;

import dev.spud.combatcore.api.CombatActionDefinition;
import dev.spud.combatcore.api.CombatActionState;
import dev.spud.combatcore.api.MeleeGeometry;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Deliberately tiny first playable executor: ordinary server-side player melee is
 * committed to a single wind-up/active/recovery action before damage resolves.
 *
 * This is a proof path, not a weapon catalogue or final vanilla-equivalence layer.
 */
public final class BasicPlayerMeleeExecutor {
    private static final CombatActionDefinition BASIC_ATTACK =
            new CombatActionDefinition("basic_melee", 4, 1, 6, 3.25);
    private static final double HALF_ARC_DEGREES = 55.0;
    private static final Map<UUID, PendingAttack> ATTACKS = new ConcurrentHashMap<>();

    private BasicPlayerMeleeExecutor() {}

    public static void onAttack(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getTarget() instanceof LivingEntity target)) return;

        event.setCanceled(true);

        long now = player.serverLevel().getGameTime();
        PendingAttack pending = ATTACKS.computeIfAbsent(player.getUUID(), ignored -> new PendingAttack());
        if (pending.state.start(BASIC_ATTACK, now)) {
            pending.targetId = target.getId();
        }
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        PendingAttack pending = ATTACKS.get(player.getUUID());
        if (pending == null) return;

        long now = player.serverLevel().getGameTime();
        if (pending.state.markResolved(now)) {
            resolve(player, pending.targetId, BASIC_ATTACK.reach());
        }

        if (!pending.state.actionReserved(now)) {
            ATTACKS.remove(player.getUUID(), pending);
        }
    }

    private static void resolve(ServerPlayer player, int targetId, double reach) {
        Entity entity = player.serverLevel().getEntity(targetId);
        if (!(entity instanceof LivingEntity target) || !target.isAlive()) return;

        Vec3 origin = player.getEyePosition();
        Vec3 facing = player.getLookAngle();
        Vec3 targetPoint = target.getBoundingBox().getCenter();
        if (!MeleeGeometry.withinArc(
                toCore(origin),
                toCore(facing),
                toCore(targetPoint),
                reach,
                HALF_ARC_DEGREES)) {
            return;
        }

        float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        target.hurt(player.damageSources().playerAttack(player), damage);
    }

    private static MeleeGeometry.Vec3 toCore(Vec3 value) {
        return new MeleeGeometry.Vec3(value.x, value.y, value.z);
    }

    private static final class PendingAttack {
        private final CombatActionState state = new CombatActionState();
        private int targetId = -1;
    }
}
