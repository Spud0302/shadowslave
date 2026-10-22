package dev.spud.shadowslave.ability.combat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/** Default combat hook: vanilla magic damage credited to the caster, vanilla fire. */
public final class VanillaAbilityCombat implements AbilityCombat {
    @Override
    public boolean damage(ServerPlayer caster, LivingEntity target, float amount) {
        return target.hurt(caster.damageSources().indirectMagic(caster, caster), amount);
    }

    @Override
    public void ignite(ServerPlayer caster, LivingEntity target, int seconds) {
        target.igniteForSeconds(seconds);
        target.setLastHurtByPlayer(caster);
    }
}
