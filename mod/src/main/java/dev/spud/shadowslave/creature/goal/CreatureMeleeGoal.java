package dev.spud.shadowslave.creature.goal;

import dev.spud.shadowslave.creature.CreatureClass;
import dev.spud.shadowslave.creature.NightmareCreature;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.phys.Vec3;

/**
 * Melee for every Class. Beasts charge a little faster than they walk. Demons
 * feint (DESIGN): every few closings they stop short, sidestep and back off
 * instead of striking, so the player who swings early is open when it
 * returns.
 */
public final class CreatureMeleeGoal extends MeleeAttackGoal {
    static final int FEINT_TICKS = 16;

    private final NightmareCreature creature;
    private int closings;
    private int feintTicks;
    private int strafe;

    public CreatureMeleeGoal(NightmareCreature creature) {
        super(creature, 1.0, true);
        this.creature = creature;
    }

    private double charge() {
        return creature.creatureClass() == CreatureClass.BEAST ? 1.25 : 1.0;
    }

    @Override
    public void start() {
        super.start();
        LivingEntity target = creature.getTarget();
        if (target != null) {
            creature.getNavigation().moveTo(target, charge());
        }
    }

    @Override
    public void tick() {
        LivingEntity target = creature.getTarget();
        if (target == null) {
            return;
        }
        if (feintTicks > 0) {
            feintTicks--;
            creature.getLookControl().setLookAt(target, 30.0F, 30.0F);
            Vec3 away = creature.position().subtract(target.position()).normalize();
            Vec3 side = new Vec3(-away.z, 0, away.x).scale(strafe);
            creature.getMoveControl().strafe(-0.6F, 0.5F * strafe);
            creature.setDeltaMovement(creature.getDeltaMovement().add(away.add(side).scale(0.03)));
            return;
        }
        boolean demon = creature.creatureClass() == CreatureClass.DEMON;
        double reach = creature.getBbWidth() * 2.0 + target.getBbWidth() + 1.0;
        if (demon && creature.distanceToSqr(target) < reach * reach && getTicksUntilNextAttack() <= 0) {
            closings++;
            if (closings % 3 == 1) {
                feintTicks = FEINT_TICKS;
                strafe = creature.getRandom().nextBoolean() ? 1 : -1;
                creature.getNavigation().stop();
                resetAttackCooldown();
                return;
            }
        }
        super.tick();
    }
}
