package dev.spud.shadowslave.creature.goal;

import dev.spud.shadowslave.creature.CreatureClass;
import dev.spud.shadowslave.creature.NightmareCreature;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/** Devil package: tries the creature's one signature power every few seconds while it has a target. */
public final class SignaturePowerGoal extends Goal {
    static final int COOLDOWN_TICKS = 200;
    private final NightmareCreature creature;
    private long readyAt;

    public SignaturePowerGoal(NightmareCreature creature) {
        this.creature = creature;
        setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = creature.getTarget();
        return creature.creatureClass() == CreatureClass.DEVIL && target != null && target.isAlive()
                && creature.level().getGameTime() >= readyAt;
    }

    @Override
    public void start() {
        LivingEntity target = creature.getTarget();
        boolean used = target != null && creature.useSignaturePower(target);
        readyAt = creature.level().getGameTime() + (used ? COOLDOWN_TICKS : 40);
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }
}
