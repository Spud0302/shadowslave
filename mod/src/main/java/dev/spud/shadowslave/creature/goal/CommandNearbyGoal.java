package dev.spud.shadowslave.creature.goal;

import dev.spud.shadowslave.creature.CreatureClass;
import dev.spud.shadowslave.creature.NightmareCreature;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Tyrant package: commands the weaker creatures around it. Every two seconds
 * it hands its prey to idle creatures nearby and drives them on with Speed.
 */
public final class CommandNearbyGoal extends Goal {
    static final double RANGE = 24.0;
    static final int INTERVAL_TICKS = 40;
    private final NightmareCreature creature;
    private long nextAt;

    public CommandNearbyGoal(NightmareCreature creature) {
        this.creature = creature;
        setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        return creature.creatureClass().ordinal() >= CreatureClass.TYRANT.ordinal() && creature.getTarget() != null && !creature.silenced()
                && creature.level().getGameTime() >= nextAt;
    }

    @Override
    public void start() {
        nextAt = creature.level().getGameTime() + INTERVAL_TICKS;
        LivingEntity prey = creature.getTarget();
        for (NightmareCreature follower : creature.level().getEntitiesOfClass(NightmareCreature.class,
                creature.getBoundingBox().inflate(RANGE), other -> other != creature && other.isAlive()
                        && other.creatureClass().ordinal() < creature.creatureClass().ordinal())) {
            if (follower.getTarget() == null || !follower.getTarget().isAlive()) {
                follower.setTarget(prey);
            }
            follower.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, INTERVAL_TICKS + 10, 0, false, false));
        }
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }
}
