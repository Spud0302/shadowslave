package dev.spud.shadowslave.ability.combat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.Objects;

/**
 * The one door abilities use to hurt things. Today it does plain vanilla
 * damage; a later Advanced Combat update can swap in its own implementation
 * with {@link #install(AbilityCombat)} without touching any ability.
 */
public interface AbilityCombat {
    /** Deals ability damage from the caster. Returns whether the target took it. */
    boolean damage(ServerPlayer caster, LivingEntity target, float amount);

    /** Sets the target alight for the given number of seconds. */
    void ignite(ServerPlayer caster, LivingEntity target, int seconds);

    static AbilityCombat get() {
        return Holder.current;
    }

    static void install(AbilityCombat combat) {
        Holder.current = Objects.requireNonNull(combat, "combat");
    }

    final class Holder {
        private static volatile AbilityCombat current = new VanillaAbilityCombat();

        private Holder() {
        }
    }
}
