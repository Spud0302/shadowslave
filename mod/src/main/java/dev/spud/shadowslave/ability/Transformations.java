package dev.spud.shadowslave.ability;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.Optional;

/** Routes harm to and from a transformed Saint into their form's hooks. */
public final class Transformations {
    private Transformations() {
    }

    /** The form this player holds right now, if any. */
    public static Optional<TransformationAbility> active(ServerPlayer player) {
        return AbilityRegistry.all().stream()
                .filter(TransformationAbility.class::isInstance)
                .map(TransformationAbility.class::cast)
                .filter(form -> form.transformed(player))
                .findFirst();
    }

    public static boolean transformed(ServerPlayer player) {
        return active(player).isPresent();
    }

    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            active(player).ifPresent(form -> form.onIncoming(player, event));
        }
        if (!event.isCanceled() && event.getSource().getEntity() instanceof ServerPlayer attacker && attacker != event.getEntity()) {
            active(attacker).ifPresent(form -> form.onOutgoing(attacker, event));
        }
    }

    public static void onDamage(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            active(player).ifPresent(form -> form.onHurt(player, event));
        }
        LivingEntity target = event.getEntity();
        if (event.getSource().getEntity() instanceof ServerPlayer attacker && attacker != target) {
            active(attacker).ifPresent(form -> form.onHit(attacker, target, event));
        }
    }
}
