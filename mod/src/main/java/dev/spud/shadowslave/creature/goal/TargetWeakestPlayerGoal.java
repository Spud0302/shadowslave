package dev.spud.shadowslave.creature.goal;

import dev.spud.shadowslave.creature.CreatureClass;
import dev.spud.shadowslave.creature.NightmareCreature;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

/** Demon package: picks the weakest player in reach (lowest health, then armour), and re-picks now and then. */
public final class TargetWeakestPlayerGoal extends TargetGoal {
    static final double RANGE = 24.0;
    private final NightmareCreature creature;
    private Player chosen;
    private int recheck;

    public TargetWeakestPlayerGoal(NightmareCreature creature) {
        super(creature, true);
        this.creature = creature;
        setFlags(EnumSet.of(Flag.TARGET));
    }

    static Comparator<Player> weakestFirst() {
        return Comparator.<Player>comparingDouble(Player::getHealth).thenComparingInt(Player::getArmorValue);
    }

    private Player weakest() {
        List<Player> players = creature.level().getNearbyPlayers(
                TargetingConditions.forCombat().range(RANGE), creature, creature.getBoundingBox().inflate(RANGE, 8.0, RANGE));
        return players.stream().filter(player -> creature.getSensing().hasLineOfSight(player))
                .min(weakestFirst()).orElse(null);
    }

    @Override
    public boolean canUse() {
        if (creature.creatureClass() != CreatureClass.DEMON || !creature.usesSightTargeting() || (recheck-- > 0 && creature.getTarget() != null)) {
            return false;
        }
        recheck = 60;
        chosen = weakest();
        return chosen != null && chosen != creature.getTarget();
    }

    @Override
    public void start() {
        creature.setTarget(chosen);
        super.start();
    }
}
