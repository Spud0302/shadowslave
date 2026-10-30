package dev.spud.shadowslave.creature.goal;

import dev.spud.shadowslave.creature.CreatureClass;
import dev.spud.shadowslave.creature.NightmareCreature;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Monster package: an ambusher. While its prey is close but looking away it
 * holds still, then lunges when the prey comes within reach or turns round.
 */
public final class AmbushLungeGoal extends Goal {
    static final double LUNGE_RANGE = 6.0;
    static final double HOLD_RANGE = 14.0;
    static final int COOLDOWN_TICKS = 160;

    private final NightmareCreature creature;
    private long readyAt;

    public AmbushLungeGoal(NightmareCreature creature) {
        this.creature = creature;
        setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    /** Whether {@code watcher} is facing roughly toward {@code subject}. */
    public static boolean isLookingAt(LivingEntity watcher, LivingEntity subject, double threshold) {
        Vec3 look = watcher.getViewVector(1.0F).normalize();
        Vec3 toSubject = subject.getEyePosition().subtract(watcher.getEyePosition()).normalize();
        return look.dot(toSubject) > threshold;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = creature.getTarget();
        if (creature.creatureClass() != CreatureClass.MONSTER || target == null || !creature.onGround() || creature.silenced()
                || creature.level().getGameTime() < readyAt) {
            return false;
        }
        double distance = creature.distanceTo(target);
        return distance < HOLD_RANGE && distance > 2.5 && !isLookingAt(target, creature, 0.5)
                && creature.getSensing().hasLineOfSight(target);
    }

    @Override
    public void start() {
        creature.getNavigation().stop();
    }

    @Override
    public void tick() {
        LivingEntity target = creature.getTarget();
        if (target == null) {
            return;
        }
        creature.getLookControl().setLookAt(target, 30.0F, 30.0F);
        creature.getNavigation().stop();
        if (creature.distanceTo(target) <= LUNGE_RANGE || isLookingAt(target, creature, 0.5)) {
            Vec3 toward = target.position().subtract(creature.position()).normalize();
            creature.setDeltaMovement(toward.x * 1.1, 0.42, toward.z * 1.1);
            creature.playSound(net.minecraft.sounds.SoundEvents.RAVAGER_ROAR, 0.6F, 1.6F);
            readyAt = creature.level().getGameTime() + COOLDOWN_TICKS;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return !creature.silenced() && creature.level().getGameTime() >= readyAt && creature.getTarget() != null && creature.getTarget().isAlive();
    }
}
