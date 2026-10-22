package dev.spud.shadowslave.creature.goal;

import dev.spud.shadowslave.creature.CreatureClass;
import dev.spud.shadowslave.creature.NightmareCreature;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Monster package: when badly hurt it breaks off, recovers out of reach and
 * comes back for the same prey.
 */
public final class RetreatToRecoverGoal extends Goal {
    static final float RETREAT_BELOW = 0.4F;
    static final int RETREAT_TICKS = 100;
    static final int COOLDOWN_TICKS = 600;

    private final NightmareCreature creature;
    private LivingEntity prey;
    private int ticks;
    private long readyAt;

    public RetreatToRecoverGoal(NightmareCreature creature) {
        this.creature = creature;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = creature.getTarget();
        return creature.creatureClass() == CreatureClass.MONSTER && target != null
                && creature.level().getGameTime() >= readyAt
                && creature.getHealth() < creature.getMaxHealth() * RETREAT_BELOW;
    }

    @Override
    public void start() {
        prey = creature.getTarget();
        ticks = RETREAT_TICKS;
        readyAt = creature.level().getGameTime() + COOLDOWN_TICKS;
        creature.addEffect(new MobEffectInstance(MobEffects.REGENERATION, RETREAT_TICKS, 1));
        creature.playSound(net.minecraft.sounds.SoundEvents.RAVAGER_STUNNED, 0.8F, 1.2F);
        flee();
    }

    private void flee() {
        Vec3 away = DefaultRandomPos.getPosAway(creature, 16, 6, prey.position());
        if (away != null) {
            creature.getNavigation().moveTo(away.x, away.y, away.z, 1.35);
        }
    }

    @Override
    public void tick() {
        ticks--;
        if (creature.getNavigation().isDone() && creature.distanceToSqr(prey) < 100.0) {
            flee();
        }
    }

    @Override
    public boolean canContinueToUse() {
        return ticks > 0 && prey != null && prey.isAlive();
    }

    @Override
    public void stop() {
        if (prey != null && prey.isAlive()) {
            creature.setTarget(prey);
        }
        prey = null;
    }
}
